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

import org.springframework.integration.aws.core.AmazonWSOperationException;

/**
 * The Exception thrown when an operation on SQS fails
 * @author Amol Nayak
 *
 */
public class AmazonSQSException extends AmazonWSOperationException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String queueURL;
	private Object messagePayload; 

	public AmazonSQSException(String accessKey, String message, String queueURL, 
								Object messagePayload,Throwable cause) {
		super(accessKey, message, cause);
		this.queueURL = queueURL;
		this.messagePayload = messagePayload;
		
	}

	public AmazonSQSException(String accessKey, String message,String queueURL, Object messagePayload) {
		super(accessKey, message);
		this.queueURL = queueURL;
		this.messagePayload = messagePayload;
		
	}

	public AmazonSQSException(String accessKey, Throwable cause,String queueURL, Object messagePayload) {
		super(accessKey, cause);
		this.queueURL = queueURL;
		this.messagePayload = messagePayload;
	}

	public AmazonSQSException(String accessKey,String queueURL, Object messagePayload) {
		super(accessKey);
		this.queueURL = queueURL;
		this.messagePayload = messagePayload;
	}

	/**
	 * The Queue URL where the Send or recieve operation failed
	 * @return
	 */
	public String getQueueURL() {
		return queueURL;
	}

	/**
	 * The message payload from the failed message
	 * @return
	 */
	public Object getMessagePayload() {
		return messagePayload;
	}	
}
