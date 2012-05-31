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

import java.util.Map;

import org.codehaus.jackson.JsonNode;

/**
 * The concrete implementation of the transformer that uses JSON for transforming
 * the messages to JSON and back from JSON to required message
 * @author Amol Nayak
 *
 */
public class AmazonSQSMessageJSONTransformer extends
		AbstractSQSMessageTransformer {

	private static final String MESSAGE_PAYLOAD = "messagePayload";
	private static final String ORIGINAL_MESSAGE_PAYLOAD_TYPE = "originalMessagePayloadType";
	private static final String MESSAGE_ATTRIBUTES = "messageAttributes";


	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.sqs.core.AbstractSQSMessageTransformer#serializeInternal(org.springframework.integration.aws.sqs.core.AmazonSQSMessage)
	 */

	@Override
	protected String serializeInternal(AmazonSQSMessage message) throws Exception {
		return mapper.writeValueAsString(message);
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.sqs.core.AbstractSQSMessageTransformer#deserializeInternal(java.lang.String)
	 */

	@Override
	@SuppressWarnings("unchecked")
	protected AmazonSQSMessage deserializeInternal(String messagePayload) throws Exception {
		JsonNode rootNode = mapper.readValue(messagePayload.getBytes(), JsonNode.class);
		JsonNode attributes = rootNode.get(MESSAGE_ATTRIBUTES);
		JsonNode originalPayloadClass = rootNode.get(ORIGINAL_MESSAGE_PAYLOAD_TYPE);
		JsonNode messagePayloadNode = rootNode.get(MESSAGE_PAYLOAD);
		AmazonSQSMessage message;
		if (messagePayloadNode != null) {
			String payload = mapper.readValue(messagePayloadNode,String.class);
			if(payload == null)
				throw new AmazonSQSMessageDeserializationException(messagePayload,
						"Mandatory node \"" + MESSAGE_PAYLOAD + "\" not found in the provided JSON String");

			Map<String, String> attributesMap = null;
			if(attributes != null)
				attributesMap= mapper.readValue(attributes, Map.class);

			Class<?> payloadClass;
			if(originalPayloadClass != null) {
				payloadClass = Class.forName(mapper.readValue(originalPayloadClass,String.class));
			}
			else {
				payloadClass = String.class;
			}

			message = new AmazonSQSMessage();
			message.setOriginalMessagePayloadType(payloadClass);
			message.setMessagePayload(payload);
			message.setMessageAttributes(attributesMap);
		}
		else {
			message = new AmazonSQSMessage();
			message.setMessagePayload(messagePayload);
			message.setOriginalMessagePayloadType(String.class);
		}
		return message;
	}
}