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

/**
 * The strategy interface that tracks the successful and failed message delivery 
 * for the SQS messages and advices if redelivery of the message is needed
 * 
 * @author Amol Nayak
 */
public interface AmazonSQSMessageDeliveryStrategy {

	/**
	 * The invoker can check if the message with this id can be re delivered.
	 * If the redelivery cannot happen, the invoker can delete the message with 
	 * the given request id from the SQS Queue
	 * 
	 * @param messageId
	 * @return
	 */
	boolean canRedeliver(String messageId);
	
	/**
	 * Notifies the failure to deliver the SQS message with request id provided
	 * @param messageId
	 */
	void notifyFailure(String messageId);
	
	/**
	 * Notifies success in delivery of the message with the given messageId
	 * @param messageId
	 */
	void notifySuccess(String messageId);
	
	/**
	 * Cleans up all the resources held against that message id 
	 * @param messageId
	 */
	void cleanup(String messageId);
}
	

