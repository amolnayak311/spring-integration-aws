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
 * This implementation is used when re delivery of the message is not to be supported
 * in case the transaction rollsback due to some exceptional condition while delivering the message.
 * 
 * @author Amol Nayak
 *
 */
public class AmazonSQSNoRedeliveryMessageDeliveryStrategy implements AmazonSQSMessageDeliveryStrategy {

	
	public boolean canRedeliver(String messageId) {		
		return false;
	}

	
	public void notifyFailure(String messageId) {
		//NOP: We are anyways not going with redelivery, so not maintaining any statistics
		
	}

	
	public void notifySuccess(String messageId) {
		// NOP again		
	}

	
	public void cleanup(String messageId) {
		//NOP again		
	}
	
	
}
