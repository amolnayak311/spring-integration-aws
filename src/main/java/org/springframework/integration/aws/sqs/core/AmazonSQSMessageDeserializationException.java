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

/**
 * To be thrown if the deserialization of the SQS String message to {@link AmazonSQSMessage} 
 * fails
 *  
 * @author Amol Nayak
 *
 */
public class AmazonSQSMessageDeserializationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String messagePayload;
	
	

	public AmazonSQSMessageDeserializationException(String messagePayload) {
		this(messagePayload,null,null);
	}


	public AmazonSQSMessageDeserializationException(String messagePayload,String message) {
		this(messagePayload,null,null);
	}

	public AmazonSQSMessageDeserializationException(String messagePayload,String message,
			Throwable cause) {
		super(message, cause);
		this.messagePayload = messagePayload;
	}

	public String getMessagePayload() {
		return messagePayload;
	}	

}
