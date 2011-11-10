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
 * The interface that would be used to transform the {@link AmazonSQSMessage} to String 
 * that would be sent as a payload to the SQS Queues and then transform the string payload
 * back to {@link AmazonSQSMessage}
 * @author Amol Nayak
 *
 */
public interface AmazonSQSMessageTransformer {

	/**
	 * Serializes the {@link AmazonSQSMessage} to a String that would be sent as a payload
	 * over the SQS Message sent over the queue
	 * @param message
	 * @return
	 */
	public String serialize(AmazonSQSMessage message);
	
	
	 /**
	  * Deserialize the  payload of the message received over the SQS Queue into the
	  * {@link AmazonSQSMessage}
	  * @param the messagePayload
	  */			
	public AmazonSQSMessage deserialize(String messagePayload);
	
}
