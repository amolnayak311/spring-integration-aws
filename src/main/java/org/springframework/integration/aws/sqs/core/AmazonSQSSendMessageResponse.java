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
 * The class containing the response from the Send message operation
 * @author Amol Nayak
 *
 */
public class AmazonSQSSendMessageResponse {

	private String messageId;
	
	private String responseMD5Sum;
	
	
	/**
	 * Default constructor
	 * @param messageId
	 * @param responseMD5Sum
	 */
	public AmazonSQSSendMessageResponse(String messageId, String responseMD5Sum) {
		this.messageId = messageId;
		this.responseMD5Sum = responseMD5Sum;
	}

	/**
	 * Gets the Message id of the message sent
	 * @return
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * Gets the MD5 sum of the content received by the server, this
	 * can be compared against the MD5 sum of the message content
	 * before sending it.
	 * @return
	 */
	public String getResponseMD5Sum() {
		return responseMD5Sum;
	}	
}
