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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.util.StringUtils;

/**
 * The base class for all SQS message transformers
 * @author Amol Nayak
 *
 */
public abstract class AbstractSQSMessageTransformer implements
		AmazonSQSMessageTransformer {

	protected final Log logger = LogFactory.getLog(getClass());

	protected final ObjectMapper mapper;

	private boolean checkSNSNotification;

	private String snsHeaderPrefix;

	private static final String SNS_TYPE_ATTRIBUTE 			= "Type";
	private static final String SNS_MESSAGE_ID_ATTRIBUTE 	= "MessageId";
	private static final String SNS_ARN_ATTRIBUTE 			= "TopicArn";
	private static final String SNS_SUBJECT_ATTRIBUTE 		= "Subject";
	private static final String SNS_TIMESTAMP_ATTRIBUTE 	= "Timestamp";
	private static final String SNS_SIGNVERSION_ATTRIBUTE 	= "SignatureVersion";
	private static final String SNS_SIGNATURE_ATTRIBUTE 	= "Signature";
	private static final String SNS_SIGN_CERT_URL_ATTRIBUTE = "SigningCertURL";
	private static final String SNS_UNSUB_URL_ATTRIBUTE 	= "UnsubscribeURL";
	private static final String SNS_MESSAGE_ATTRIBUTE 		= "Message";

	//The array of sns attributes except the type attribute
	private static final String[] snsAttributes =
					{SNS_MESSAGE_ID_ATTRIBUTE,SNS_ARN_ATTRIBUTE,SNS_SUBJECT_ATTRIBUTE,SNS_TIMESTAMP_ATTRIBUTE,
					SNS_SIGNVERSION_ATTRIBUTE,SNS_SIGNATURE_ATTRIBUTE,SNS_SIGN_CERT_URL_ATTRIBUTE,SNS_UNSUB_URL_ATTRIBUTE};


	/**
	 *
	 */
	public AbstractSQSMessageTransformer() {
		mapper = new ObjectMapper();
		mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
		mapper.getSerializationConfig().set(Feature.SORT_PROPERTIES_ALPHABETICALLY, true);
	}


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
		if (messagePayload == null)
			return null;

		AmazonSQSMessage sqsMessage = null;
		if(checkSNSNotification) {
			//We will check if the received message is a SNS notification
			try {
				JsonNode jsonNode = mapper.readValue(messagePayload.getBytes(), JsonNode.class);
				//Document is a valid JSON document
				JsonNode type = jsonNode.get(SNS_TYPE_ATTRIBUTE);
				if(type != null) {
					//possibly a SNS notification
					String snsType = mapper.readValue(type, String.class);
					if (StringUtils.hasText(snsType) && "Notification".equals(snsType)) {
						//Yes, this is a notification from SNS
						String prefix = StringUtils.hasText(snsHeaderPrefix)?snsHeaderPrefix:"";

						sqsMessage = new AmazonSQSMessage();
						Map<String,String> attributes = new HashMap<String, String>();
						sqsMessage.setMessageAttributes(attributes);
						attributes.put(prefix + SNS_TYPE_ATTRIBUTE, snsType);

						//now read other attributes and populate in the message attributes
						for(String attribute:snsAttributes) {
							JsonNode attributeNode = jsonNode.get(attribute);
							if(attributeNode != null) {
								String attributeValue = mapper.readValue(attributeNode, String.class);
								if(StringUtils.hasText(attributeValue)) {
									attributes.put(prefix + attribute, attributeValue);
								}
							}
						}

						//now populate the payload
						JsonNode messageNode = jsonNode.get(SNS_MESSAGE_ATTRIBUTE);
						//This should not be null ideally
						if (messageNode != null) {
							String message = mapper.readValue(messageNode,String.class);
							sqsMessage.setMessagePayload(message);
							sqsMessage.setOriginalMessagePayloadType(String.class);
						}
						else {
							logger.warn("SNS Notification does not have the \"" + SNS_MESSAGE_ATTRIBUTE + "\" node");
						}
					}
				}
			} catch (Exception e) {
				//The document is not a valid JSON doc, carry one.
				if(logger.isDebugEnabled())
					logger.debug("Incoming payload is not an SNS notification");
			}

		}
		if(sqsMessage != null) {
			sqsMessage = postProcessSNSNotification(sqsMessage);
			//if an exception if thrown, let it propagate
		}
		else {
			//The message is not an SNS notification or we have decided not to check the message for sns notification
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

		}



		return sqsMessage;
	}

	/**
	 * This method of intended to be overridden by child classes if they want to generate a message
	 * different from a standard {@link AmazonSQSMessage} generated by this abstract class.
	 * Not overriding this method will ensure that the default format will be used. The format is as follows
	 * Following are the attributes of the notification JSON message sent by SNS
	 *
	 * "Type", "MessageId","TopicArn", "Subject", "Timestamp", "SignatureVersion",
	 * "Signature","SigningCertURL", UnsubscribeURL", "Message"
	 *
	 * All these attributes except the "Message" will form the attributes (which eventually will be a part of
	 * the spring integration message header) of the {@link AmazonSQSMessage} and the contents of the
	 * "Message" attribute will be the payload of the {@link AmazonSQSMessage}. If any of the attributes has
	 * an empty string as the value, it will not be included in the attributes. The key entry of the attribute
	 * (and the name of the header in the spring integration's message ) will be same as the name of the
	 * attribute in the JSON unless a <i>snsHeaderPrefix</i> is specified, by default the prefix is null. If say the
	 * prefix is <i>SNS_</i>, the names of the attribute keys will be "SNS_Type", "SNS_MessageId","SNS_TopicArn"
	 * and so on.
	 * Returning null from the method will not send out the SNS notification message but will send the
	 * entire SNS notification JSON as the payload of the message. This behavior is same as when
	 * <i>checkSNSNotification</i> is set to false. If this behavior is desired, it would be more efficient to
	 * set the flag <i>checkSNSNotification</i> to false
	 *
	 * @param sqsMessage the default constructed {@link AmazonSQSMessage} for the SNS notification
	 * @return the {@link AmazonSQSMessage} created by the subclass that needs to be further processed
	 */
	protected AmazonSQSMessage postProcessSNSNotification(AmazonSQSMessage sqsMessage) {
		return sqsMessage;
	}


	/**
	 * Set to true if the transformer should check for the incoming payload for SNS notification
	 *
	 * @param checkSNSNotification
	 */
	public void setCheckSNSNotification(boolean checkSNSNotification) {
		this.checkSNSNotification = checkSNSNotification;
	}


	/**
	 * The prefix for the SNS message attributes that will be added to the message attributes
	 * @param snsHeaderPrefix
	 */
	public void setSnsHeaderPrefix(String snsHeaderPrefix) {
		this.snsHeaderPrefix = snsHeaderPrefix;
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
