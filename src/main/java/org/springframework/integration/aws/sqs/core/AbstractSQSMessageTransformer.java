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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The base class for all SQS message transformers 
 * @author Amol Nayak
 *
 */
public abstract class AbstractSQSMessageTransformer implements
		AmazonSQSMessageTransformer {

	protected final Log logger = LogFactory.getLog(getClass());
	
	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.sqs.core.AmazonSQSMessageTransformer#serialize(org.springframework.integration.aws.sqs.core.AmazonSQSMessage)
	 */
	
	public String serialize(AmazonSQSMessage message) {
		if(logger.isDebugEnabled())
			logger.debug("Serializing message");
		String serializedMessage;
		try {
			serializedMessage = serializeInternal(message);
		} catch (Exception e) {
			logger.error("Exception while serializing the given message",e);
			throw new AmazonSQSMessageSerializationException(message, 
					"Caught Exception while serializing the message, see root exception for more details",
					e);	
			
		}
		return serializedMessage;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.sqs.core.AmazonSQSMessageTransformer#deserialize(java.lang.String)
	 */
	
	public AmazonSQSMessage deserialize(String messagePayload) {
		if(logger.isDebugEnabled())
			logger.debug("deserializing message");
		AmazonSQSMessage sqsMessage; 
		try {
			sqsMessage = deserializeInternal(messagePayload);
		} catch (Exception e) {
			// Possibly not the format which the deserializer expected or 
			//some exception while parsing a valid format
			logger.warn("Unable to parse the given message payload, see root cause exception",e);			
			logger.info("Using the provided String payload as is in the message");
			sqsMessage = new AmazonSQSMessage();
			sqsMessage.setMessagePayload(messagePayload);
		}		
		return sqsMessage;
	}
	
	/**
	 * The implementing class implements this method that performs the serialization
	 * It can throw any Exception if it is not able to serialize the message 
	 * @param message
	 * @return
	 */
	protected abstract String serializeInternal(AmazonSQSMessage message) throws Exception;
	
	/**
	 * The implementing class implements this method that performs the de serialization
	 * It can throw any Exception if it is not able to de serialize the message
	 * @param message
	 * @return
	 */
	protected abstract AmazonSQSMessage deserializeInternal(String message) throws Exception;

}
