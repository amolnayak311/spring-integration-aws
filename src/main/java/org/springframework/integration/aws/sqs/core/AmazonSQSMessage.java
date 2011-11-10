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

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.convert.ConversionService;

/**
 * The class representing the Amazon SQS message
 * @author Amol Nayak
 *
 */
public class AmazonSQSMessage {

	private String messageId;
	
	private String receiptHandle;
	
	private String mD5OfBody;
	
	private Map<String, String> messageAttributes;
	
	private String messagePayload;
	
	private Class<?> originalMessagePayloadType;

	/**
	 * Sets the attributes associated with the message
	 * @return
	 */
	public Map<String, String> getMessageAttributes() {
		return messageAttributes;
	}

	/**
	 * Gets the attributes associated with the message
	 * @param messageAttributes
	 */
	public void setMessageAttributes(Map<String, String> messageAttributes) {
		this.messageAttributes = messageAttributes;
	}

	/**
	 * Gets the message payload
	 * @return
	 */
	public String getMessagePayload() {
		return messagePayload;
	}

	/**
	 * Sets the message payload
	 * @param messagePayload
	 */
	public void setMessagePayload(String messagePayload) {
		this.messagePayload = messagePayload;
	}

	/**
	 * Gets the unique message identifier identifying the message
	 * @return
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * Sets the Unique message identifier 
	 * @param messageId
	 */
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	/**
	 * The Receipt handle received during the retrieval of the message.
	 * This message handle will not be valid if some other receiver 
	 * receives the message after its visibility timeout expires
	 * @return
	 */
	public String getReceiptHandle() {
		return receiptHandle;
	}

	/**
	 * Sets the receipt handle
	 * @param receiptHandle
	 */
	public void setReceiptHandle(String receiptHandle) {
		this.receiptHandle = receiptHandle;
	}

	/**
	 * Gets the MD5 checksum of the message body
	 * @return
	 */
	public String getMD5OfBody() {
		return mD5OfBody;
	}

	/**
	 * Sets the MD5 checksum of the message body
	 * @param mD5OfBody
	 */
	public void setMD5OfBody(String mD5OfBody) {
		this.mD5OfBody = mD5OfBody;
	}
	
	
	/**
	 * The Original message sent might not be of type String and can be of any type
	 * We have used the {@link ConversionService} to convert the original
	 * type to String this type will be used to convert the payload back to 
	 * the original type. 
	 * @return
	 */
	public Class<?> getOriginalMessagePayloadType() {
		return originalMessagePayloadType;
	}

	/**
	 * Sets the original 
	 * @param originalMessagePayloadType
	 */
	public void setOriginalMessagePayloadType(Class<?> originalMessagePayloadType) {
		this.originalMessagePayloadType = originalMessagePayloadType;
	}

	/**
	 * Convenience method to add the attributes
	 * @param attributeName
	 * @param attributeValue
	 */
	public void addAttribute(String attributeName,String attributeValue) {
		if(messageAttributes == null)
			messageAttributes = new HashMap<String, String>();
		messageAttributes.put(attributeName, attributeValue);
	}

	
	public String toString() {
		return "AmazonSQSMessage [messageId=" + messageId + ", receiptHandle="
				+ receiptHandle + ", mD5OfBody=" + mD5OfBody
				+ ", messageAttributes=" + messageAttributes
				+ ", messagePayload=" + messagePayload
				+ ", originalMessagePayloadType=" + originalMessagePayloadType
				+ "]";
	}
	
	
}
