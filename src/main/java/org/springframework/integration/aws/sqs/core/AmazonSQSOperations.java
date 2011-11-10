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

import java.util.Collection;

/**
 * The common interface used to perform various Amazon SQS operations.
 * @author Amol Nayak
 *
 */
public interface AmazonSQSOperations {

	/**
	 * Sends a message to the given queue URL with the given message payload
	 * @param queueURL
	 * @param message
	 * @return the 
	 */
	AmazonSQSSendMessageResponse sendMessage(String queueURL,AmazonSQSMessage message);
	
	
	/**
	 * Reads messages from the given queue
	 * @param queue 
	 * @param maxNumberOfMessages: The maximum number of messages to be read in one operation
	 * @return The {@link Collection} of the {@link AmazonSQSMessage} read, the length will be
	 * of maximum maxNumberOfMessages 
	 */
	Collection<AmazonSQSMessage> receiveMessages(String queueURL,int maxNumberOfMessages);
	
	/**
	 * Deletes the message with the given receipt handle.
	 * NOTE: SQS requires to delete using the latest handle received from the receive,
	 * however, even if we use a previously read message receipt, the message still gets
	 * deleted
	 * 
	 * @param receiptHandle
	 * @param queueURL
	 */
	public void deleteMessage(String receiptHandle,String queueURL);
}
