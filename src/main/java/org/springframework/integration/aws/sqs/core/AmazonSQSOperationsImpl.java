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
package org.springframework.integration.aws.sqs.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.aws.core.AbstractAmazonWSClientFactory;
import org.springframework.integration.aws.core.AmazonWSClientFactory;
import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.core.AmazonWSOperationException;
import org.springframework.util.Assert;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

/**
 * The default implementation of the {@link AmazonSQSOperations}
 * @author Amol Nayak
 *
 */
public class AmazonSQSOperationsImpl implements AmazonSQSOperations {

	private final Log logger = LogFactory.getLog(AmazonSQSOperationsImpl.class);

	private final AmazonWSCredentials credentials;

	private AmazonSQSMessageTransformer messageTransformer = new AmazonSQSMessageJSONTransformer();

	private AmazonWSClientFactory<AmazonSQSClient> clientFactory;

	/**
	 * The default constructor
	 * @param credentials
	 */
	public AmazonSQSOperationsImpl(final AmazonWSCredentials credentials) {
		if(credentials == null)
			throw new AmazonWSOperationException(null, "Credentials cannot be null, provide a non null valid set of credentials");

		this.credentials = credentials;
		clientFactory = new AbstractAmazonWSClientFactory<AmazonSQSClient>() {

			@Override
			protected AmazonSQSClient getClientImplementation() {
				return new AmazonSQSClient(new BasicAWSCredentials(credentials.getAccessKey(),
						credentials.getSecretKey()));
			}
		};
	}



	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.sqs.core.AmazonSQSOperations#sendMessage(java.lang.String, java.lang.String)
	 */

	public AmazonSQSSendMessageResponse sendMessage(String queueURL,
			AmazonSQSMessage message) {
		if(logger.isDebugEnabled())
			logger.info("Sending message to queue " + queueURL);
		String transformedPayload = null;
		try {
			transformedPayload = messageTransformer.serialize(message);
			SendMessageResult result =
				clientFactory.getClient(queueURL).sendMessage(new SendMessageRequest(queueURL, transformedPayload));
			if(logger.isDebugEnabled())
				logger.debug("Message successfully sent");
			return new AmazonSQSSendMessageResponse(result.getMessageId(), result.getMD5OfMessageBody());
		} catch (Exception e) {
			logger.error("Exception thrown while sending a message to queue \"" + queueURL +
					"\", check exception for more details",e);
			throw new AmazonSQSException(credentials.getAccessKey(),
					"Exception while sending message to the queue \"" +
						queueURL + "\", see nested exception for more details",
					queueURL, transformedPayload, e);
		}
	}



	/**
	 * Gets the specified number of Messages from SQS
	 */
	public Collection<AmazonSQSMessage> receiveMessages(String queueURL,
			int maxNumberOfMessages) {
		ReceiveMessageRequest request = new ReceiveMessageRequest(queueURL);
		if(maxNumberOfMessages > 0)
			request.withMaxNumberOfMessages(maxNumberOfMessages);
		else
			request.withMaxNumberOfMessages(1);

		ReceiveMessageResult result;
		try {
			result = clientFactory.getClient(queueURL).receiveMessage(request);
		} catch (Exception e) {
			logger.error("Exception thrown while receiving a message from the queue \"" + queueURL +
					"\", check exception for more details",e);
			throw new AmazonSQSException(credentials.getAccessKey(),
					"Exception while receiving message from the queue \"" +
					queueURL + "\", see nested exception for more details",
					queueURL, null, e);
		}
		List<Message> messages = result.getMessages();
		Collection<AmazonSQSMessage> response = new ArrayList<AmazonSQSMessage>();
		for(Message message:messages) {
			buildSQSMessage(response, message);
		}
		return response;
	}



	/**
	 * Extracts the payload from the SQS message and builds the AmazonSQSMessage
	 * after transforming the message payload
	 * @param response
	 * @param message
	 */
	private void buildSQSMessage(Collection<AmazonSQSMessage> response,
			Message message) {
		AmazonSQSMessage sqsMessage = messageTransformer.deserialize(message.getBody());
		response.add(sqsMessage);
		sqsMessage.setMD5OfBody(message.getMD5OfBody());
		sqsMessage.setMessageId(message.getMessageId());
		sqsMessage.setReceiptHandle(message.getReceiptHandle());
	}

	/**
	 * Deletes the message with the given receiptHandle from the given queue
	 */
	public void deleteMessage(String receiptHandle, String queueURL) {
		try {
			clientFactory.getClient(queueURL).deleteMessage(new DeleteMessageRequest(queueURL,receiptHandle));
		} catch (Exception e) {
			logger.error("Exception thrown while deleteing a message from the queue \"" + queueURL +
					"\", check exception for more details",e);
			throw new AmazonSQSException(credentials.getAccessKey(),
					"Exception while deleting the message from the queue \"" +
					queueURL + "\", see nested exception for more details",
					queueURL, null, e);

		}
	}


	public AmazonSQSMessageTransformer getMessageTransformer() {
		return messageTransformer;
	}

	public void setMessageTransformer(AmazonSQSMessageTransformer messageTransformer) {
		Assert.notNull(messageTransformer,"Provide a non null Message Transformer");
		this.messageTransformer = messageTransformer;
	}
}
