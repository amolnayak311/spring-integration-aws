/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.aws.sqs;

import java.util.Collection;
import java.util.Map;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.integration.Message;

import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.sqs.core.AmazonSQSException;
import org.springframework.integration.aws.sqs.core.AmazonSQSMessage;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperations;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperationsImpl;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The MessageSource that would be used by the inbound SQS channel adapter 
 * @author Amol Nayak
 *
 */
public class AmazonSQSMessageSource extends IntegrationObjectSupport implements
		MessageSource<Object> {

	
	private AmazonWSCredentials credentials;
	private AmazonSQSOperations client;
	private String sqsQueue;
	private boolean isTransactional;
	private Integer maxRedeliveryAttempts;
	
	//By default, no redelivery
	private AmazonSQSMessageDeliveryStrategy redeliveryStrategy
						= new AmazonSQSNoRedeliveryMessageDeliveryStrategy();
	
	
	
	public AmazonSQSMessageSource(AmazonWSCredentials credentials,String sqsQueue) {
		Assert.notNull(credentials,"Provide non null AWS credentials");
		this.credentials = credentials;
		Assert.isTrue(StringUtils.hasText(sqsQueue), "Provide a non null, non empty queue");
		this.sqsQueue = sqsQueue;
		client = new AmazonSQSOperationsImpl(credentials);
	}	

	
	protected void onInit() throws Exception {
		if(isTransactional && maxRedeliveryAttempts > 0)
			redeliveryStrategy = new AmazonSQSRedeliveryCountDeliveryStrategy(maxRedeliveryAttempts);
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.core.MessageSource#receive()
	 */
	
	public Message<Object> receive() {
		if(isTransactional && 
				(!TransactionSynchronizationManager.isActualTransactionActive()
				|| TransactionSynchronizationManager.isCurrentTransactionReadOnly())) 
			throw new AmazonSQSException(credentials.getAccessKey(), 
					"Expecting an incoming non read only transaction while reading the message from SQS Queue", sqsQueue, null);
		//We will read just one message as this is invoked the max messages per poll will
		//be handled by AbstractPollingEndpoint's Poller which will invoke the call method on the 
		//Callable method max number of messages per poll times
		Collection<AmazonSQSMessage> messages;
		try {
			messages = client.receiveMessages(sqsQueue, 1);
		} catch (RuntimeException e) {
			logger.error("Caught Exception while receiving mesage",e);			
			throw e;
			
		}
		
		if(!messages.isEmpty()) {
			AmazonSQSMessage sqsMessage = messages.iterator().next();	//Since we will receive just one message				
			if(isTransactional) {
				//If transaction is present, register a Synchronization
				final String receiptHandle = sqsMessage.getReceiptHandle();
				final String messageId = sqsMessage.getMessageId();
				TransactionSynchronizationManager
				.registerSynchronization(new TransactionSynchronizationAdapter() {
					
					public void beforeCommit(boolean readOnly) {
						if(!readOnly) {
							//Transaction successfully committing, delete message now
							//NOTE: If the transaction is longer than the visibility timeout of
							//the message, then the message would possibly be consumed by some 
							//other consumer too.								
							client.deleteMessage(receiptHandle, sqsQueue);
						} else {
							//Should never reach here as we would have already thrown an exception if the 
							//transaction is read only
							if(logger.isInfoEnabled())
								logger.info("Not deleting the message with receipt " + receiptHandle 
										+ " as the transaction is read only");
						}
						redeliveryStrategy.notifySuccess(messageId);
					}

					
					public void afterCompletion(int status) {
						if(status == TransactionSynchronization.STATUS_ROLLED_BACK) {
							if(!redeliveryStrategy.canRedeliver(messageId)) {
								if(logger.isInfoEnabled()) {
									if(redeliveryStrategy instanceof AmazonSQSRedeliveryCountDeliveryStrategy) {
										AmazonSQSRedeliveryCountDeliveryStrategy strat = 
											(AmazonSQSRedeliveryCountDeliveryStrategy)redeliveryStrategy;
										logger.info("Deleting message with message id " + messageId + 
												" from SQS after " + strat.getFailureCount(messageId) 
												+ " unsuccessful of attempts to receive");
									}
								}
								client.deleteMessage(receiptHandle, sqsQueue);
								redeliveryStrategy.cleanup(messageId);
							}								
						}
					}
				});
			}
			try {
				Class<?> originalPayloadType = sqsMessage.getOriginalMessagePayloadType();
				String messagePayload = sqsMessage.getMessagePayload();
				ConversionService convService = getPayloadConversionService();
				if(originalPayloadType == null) {
					return buildMessage(sqsMessage, messagePayload);
				}else if(convService.canConvert(String.class, originalPayloadType)) {
					try {
						Object convertedPayload = 
							convService.convert(messagePayload, originalPayloadType);
						return buildMessage(sqsMessage, convertedPayload);
					} catch (RuntimeException e) {
						logger.error("Caught Exception while converting the message and building it",e);
						if(isTransactional)
							redeliveryStrategy.notifyFailure(sqsMessage.getMessageId());
						throw e;
					}
				} else {
					if(isTransactional)
						redeliveryStrategy.notifyFailure(sqsMessage.getMessageId());
					throw new AmazonSQSException(credentials.getAccessKey(),
							"Cannot convert from the source payload type " + originalPayloadType 
							+ " to type String",sqsQueue,messagePayload);
				}
			}finally {
				if(!isTransactional)
					client.deleteMessage(sqsMessage.getReceiptHandle(), sqsQueue);
				
					
			}
		}
		return null;
	}



	/**
	 * @param sqsMessage
	 * @param convertedPayload
	 * @return
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	private Message<Object> buildMessage(AmazonSQSMessage sqsMessage,
			Object convertedPayload) {
		Map<String, String> attributes = sqsMessage.getMessageAttributes();		
		MessageBuilder builder = MessageBuilder
		.withPayload(convertedPayload)
		.setHeader(AmazonSQSMessageHeaders.SERVER_SIDE_PAYLOAD_MD5, sqsMessage.getMD5OfBody())
		.setHeader(AmazonSQSMessageHeaders.MESSAGE_ID, sqsMessage.getMessageId())
		.setHeader(AmazonSQSMessageHeaders.MESSAGE_RECEIPT_HANDLE, sqsMessage.getReceiptHandle());
		
		if(attributes != null && !attributes.isEmpty())
			builder.setHeader(AmazonSQSMessageHeaders.MESSAGE_ATTRIBUTES, attributes);
		
		return builder.build();
	}

	//TODO: This is duplicated and not thread safe, if two threads find the service as null 
	private ConversionService getPayloadConversionService() {
		ConversionService service = getConversionService();
		if(service == null) {
			service = new DefaultConversionService();
			setConversionService(service);
		}
		return service;
	}



	/**
	 * Indicates whether the adapter can participate in an incoming transaction. 
	 * if the adapter can, then the message is deleted from the queue only if the 
	 * transaction successfully commits, else the message is deleted immediately 
	 * after the message is read
	 * @return
	 */
	public boolean isTransactional() {
		return isTransactional;
	}


	/**
	 * Sets the transactional behaviour of the adapter
	 * @param isTransactional
	 */
	public void setTransactional(boolean isTransactional) {
		this.isTransactional = isTransactional;
	}

	/**
	 * Gets the redelivery strategy for the adapter
	 * @return
	 */
	public AmazonSQSMessageDeliveryStrategy getRedeliveryStrategy() {
		return redeliveryStrategy;
	}
	
	/**
	 * Sets a particular redelivery strategy for the adapter
	 * @param redeliveryStrategy
	 */
	public void setRedeliveryStrategy(
			AmazonSQSMessageDeliveryStrategy redeliveryStrategy) {
		this.redeliveryStrategy = redeliveryStrategy;
	}


	/**
	 * Gets the max number of redelivery attempts for the message that failed.
	 * If the adapter is not transactional, this attribute is ignored
	 * @return
	 */
	public Integer getMaxRedeliveryAttempts() {
		return maxRedeliveryAttempts;
	}
	
	/**
	 * Sets the max redelivery attempts
	 * @param maxRedeliveryAttempts
	 */
	public void setMaxRedeliveryAttempts(Integer maxRedeliveryAttempts) {
		this.maxRedeliveryAttempts = maxRedeliveryAttempts;
	}	

}
