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

import java.util.Map;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.Expression;
import org.springframework.integration.Message;
import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.sqs.core.AmazonSQSException;
import org.springframework.integration.aws.sqs.core.AmazonSQSMessage;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperations;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperationsImpl;
import org.springframework.integration.aws.sqs.core.AmazonSQSSendMessageResponse;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.handler.ExpressionEvaluatingMessageProcessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The message handler for the outbound adapter for SQS
 * @author Amol Nayak
 *
 */
public class AmazonSQSMessageHandler extends AbstractMessageHandler {
	
	private AmazonWSCredentials credentials;
	private AmazonSQSOperations sqsClient;
	private String defaultSQSQueue;
	private ExpressionEvaluatingMessageProcessor<String> destinationQueueProcessor;
	private boolean verifySentMessages;
	
	/**
	 * The constructor accepting the AWS credentials and the default queue on which to 
	 * publish the message on
	 * @param credentials
	 */
	public AmazonSQSMessageHandler(AmazonWSCredentials credentials,String defaultSQSQueue) {		
		this.credentials = credentials;
		this.defaultSQSQueue = defaultSQSQueue;
		sqsClient = new AmazonSQSOperationsImpl(credentials);
	}
	
	
	
	
	protected void onInit() throws Exception {
		Assert.notNull(destinationQueueProcessor, "Destination queue processor must be non null");
	}



	/**
	 * The constructor accepting the AmazonWCredentials only, the default queue 
	 * will not be present in this case
	 * @param credentials
	 */
	public AmazonSQSMessageHandler(AmazonWSCredentials credentials) {
		this(credentials,null);
	}



	private ConversionService getPayloadConversionService() {
		ConversionService service = getConversionService();
		if(service == null) {
			service = new DefaultConversionService();
			setConversionService(service);
		}
		return service;
	}
	
	/**
	 * This Expression will be evaluated against the message to determine the destination queue
	 * if no queue is determined after evaluating the expression, the default output queue
	 * is used.
	 * @param expresion
	 */
	public void setDestinationQueueExpression(Expression expression) {
		destinationQueueProcessor = new ExpressionEvaluatingMessageProcessor<String>(expression);
	}
	
	
	
	/**
	 *This will indicate if the outgoing Message content's MD5 sum is to be validated against the 
	 *MD5 sum returned by the Amazon SQS.  
	 */
	public void setVerifySentMessages(boolean verifySentMessages) {
		this.verifySentMessages = verifySentMessages;
	}

	
	@SuppressWarnings("unchecked")
	protected void handleMessageInternal(Message<?> message) throws Exception {		 
		Object payload = message.getPayload();
		String destinationQueue = destinationQueueProcessor.processMessage(message);
		if(!StringUtils.hasText(destinationQueue)) {
			if(StringUtils.hasText(defaultSQSQueue))
				destinationQueue = defaultSQSQueue;
			else
				throw new AmazonSQSException(credentials.getAccessKey(),
						"Destination cannot be determined for the message",null,payload);
		}
		if(logger.isDebugEnabled())
			logger.debug("Sending message to queue " + destinationQueue);
		AmazonSQSSendMessageResponse sendMessageResponse;
		String messagePayload;
		AmazonSQSMessage sqsMsg = new AmazonSQSMessage();
		Map<String, String> messageAttributes = null;
		try {
			messageAttributes = 
				(Map<String, String>)message.getHeaders().get(AmazonSQSMessageHeaders.MESSAGE_ATTRIBUTES);
		} catch (ClassCastException e) {			
			logger.warn("Message attributes expected of type Map<String,String>, ignoring message attributes header" +
					"see root cause exception for more details", e);
		}
		sqsMsg.setMessageAttributes(messageAttributes);
		if(payload instanceof String) {
			messagePayload = (String)payload;			
			sqsMsg.setMessagePayload(messagePayload);
			sqsMsg.setOriginalMessagePayloadType(String.class);
			sendMessageResponse = sqsClient.sendMessage(destinationQueue, sqsMsg);
		} else {			
			ConversionService conversionService = getPayloadConversionService();
			if(conversionService.canConvert(payload.getClass(), String.class)) {
				messagePayload = conversionService.convert(payload, String.class);
				sqsMsg.setMessagePayload(messagePayload);
				sqsMsg.setOriginalMessagePayloadType(payload.getClass());
				sendMessageResponse = 
					sqsClient.sendMessage(destinationQueue, sqsMsg);
			} else {
				throw new AmazonSQSException(credentials.getAccessKey(),
						"No suitable converter found to transform object of class " 
						+ payload.getClass() + " to String",destinationQueue,payload);
			}
			if(logger.isDebugEnabled())
				logger.debug("Message id is " + sendMessageResponse.getMessageId());
			
			if(verifySentMessages) {
				//TODO: We need have the MD5 sum of the exact String payload sent to the SQS and 
				//Not the payload we set here. Let the SQS Client calculate the MD of payload
				//here return it to us.
				//Here we need to calculate the MD5 sum of the Message being sent, and compare that 
				//with the MD5 digest of the response received
				String serverMD5Sum = sendMessageResponse.getResponseMD5Sum();
				if(logger.isDebugEnabled())
					logger.debug("Server MD5 Sum is  " + serverMD5Sum);
				
//				String clientSideMD5Hash = AmazonWSCommonUtils.encodeHex(
//						AmazonWSCommonUtils.getContentsMD5AsBytes(messagePayload));
//				if(logger.isDebugEnabled())
//					logger.debug("Computed client side MD5 digest is " + clientSideMD5Hash);
//				
//				if(clientSideMD5Hash.equals(serverMD5Sum)) {
//					if(logger.isDebugEnabled())
//						logger.debug("Client side MD5 sum matched with that returned from server");
//				} else {
//					logger.warn("client side MD5 \""+ clientSideMD5Hash + 
//							"\" didnt match with the server side MD5 sum \"" + serverMD5Sum + "\"");
//					//We possibly cannot delete the message here,
//				}				
			}
		}
	}
}
