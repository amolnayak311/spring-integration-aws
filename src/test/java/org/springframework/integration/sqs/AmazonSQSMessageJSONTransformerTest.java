/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.sqs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.integration.aws.sqs.core.AmazonSQSMessage;
import org.springframework.integration.aws.sqs.core.AmazonSQSMessageJSONTransformer;

/**
 * The test case for the default JSON transformer provided
 *
 * @author Amol Nayak
 *
 */
public class AmazonSQSMessageJSONTransformerTest {

	AmazonSQSMessageJSONTransformer transformer = new AmazonSQSMessageJSONTransformer();



	/**
	 *
	 */
	public AmazonSQSMessageJSONTransformerTest() {
		transformer.setCheckSNSNotification(true);
		transformer.setSnsHeaderPrefix("SNS_");
	}

	private static final String serializedMessage = "{\"messageAttributes\":"
		 		+ "{\"AttributeOne\":\"ValueOne\",\"AttributeTwo\":\"ValueTwo\"},"
		 		 + "\"messagePayload\":\"10\",\"originalMessagePayloadType\":\"java.lang.Integer\"}";

	private static final String serializedNestedJson = "{\"messageAttributes\":{\"AttributeOne\":" +
			"\"ValueOne\",\"AttributeTwo\":\"ValueTwo\"},\"messagePayload\":\"{\\\"Name\\\":\\\"Test\\\"}\",\"" +
			"originalMessagePayloadType\":\"java.lang.String\"}";

	private static final String snsNotification = "{\"Type\" : \"Notification\", \"MessageId\" : " +
			"\"e8441870-78ca-4fc1-a5cf-eeb456fb71b2\", \"TopicArn\" : " +
			"\"arn:aws:sns:us-east-1:439454740675:TEST_TOPIC\",\"Subject\" " +
			": \"Test Message Subject\", \"Message\" : \"Test Message Content\"," +
			"\"Timestamp\" : \"2012-05-28T18:06:49.137Z\", \"SignatureVersion\" : " +
			"\"1\",\"Signature\" : \"ZEnX7kCINS4NoDZZZyEaMNWqwidxP7RkwGwDxyXq/RVm/1nFh/dwWVezVKYadwgXoV7GPe5H2jP0ghoW1RPV76PgGV/xNp5/8JGxBhKTgp5HeE9alS4aRGs2ZSqOeyTb/4+N+YAOe5guXgzxbJOZijDXnpDKVRGuJ2Wxetskp6E=\"," +
			"\"SigningCertURL\" : \"https://sns.us-east-1.amazonaws.com/SimpleNotificationService-f3ecfb7224c7233fe7bb5f59f96de52f.pem\"," +
			"\"UnsubscribeURL\" : \"https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:439454740675:TEST_TOPIC:01810af0-48b8-4d85-ad7c-0f6a17fa0c63\"}";

	@Test
	public void serialize() {
		AmazonSQSMessage message = new AmazonSQSMessage();
		message.setOriginalMessagePayloadType(Integer.class);
		message.setMessagePayload("10");
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("AttributeOne", "ValueOne");
		attributes.put("AttributeTwo", "ValueTwo");
		message.setMessageAttributes(attributes);
		assertEquals(serializedMessage, transformer.serialize(message));
	}

	@Test
	public void serializeNestedJson() {
		AmazonSQSMessage message = new AmazonSQSMessage();
		message.setOriginalMessagePayloadType(String.class);
		message.setMessagePayload("{\"Name\":\"Test\"}");
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("AttributeOne", "ValueOne");
		attributes.put("AttributeTwo", "ValueTwo");
		message.setMessageAttributes(attributes);
		assertEquals(serializedNestedJson, transformer.serialize(message));
	}


	@Test
	public void deserializeRecognizedJson() {
		AmazonSQSMessage message = transformer.deserialize(serializedMessage);
		Map<String, String> attributes = message.getMessageAttributes();
		assertNotNull(attributes);
		assertEquals(2, attributes.size());
		assertEquals("ValueOne", attributes.get("AttributeOne"));
		assertEquals("ValueTwo", attributes.get("AttributeTwo"));
		assertEquals(Integer.class,message.getOriginalMessagePayloadType());
		assertEquals("10", message.getMessagePayload());
	}

	@Test
	public void deserializeNonJson() {
		AmazonSQSMessage message = transformer.deserialize("Hello World");
		assertNull(message.getMessageAttributes());
		assertEquals("Hello World", message.getMessagePayload());
	}

	@Test
	public void deserializeNestedJson() {
		AmazonSQSMessage message = transformer.deserialize(serializedNestedJson);
		Map<String, String> attributes = message.getMessageAttributes();
		assertNotNull(attributes);
		assertEquals(2, attributes.size());
		assertEquals("ValueOne", attributes.get("AttributeOne"));
		assertEquals("ValueTwo", attributes.get("AttributeTwo"));
		assertEquals(String.class,message.getOriginalMessagePayloadType());
		assertEquals("{\"Name\":\"Test\"}", message.getMessagePayload());
	}

	@Test
	public void deserializeSNSNotification() {
		AmazonSQSMessage message = transformer.deserialize(snsNotification);
		Map<String, String> attributes = message.getMessageAttributes();
		assertNotNull(attributes);
		assertEquals(9, attributes.size());
		assertEquals("Notification", attributes.get("SNS_Type"));
		assertEquals("e8441870-78ca-4fc1-a5cf-eeb456fb71b2", attributes.get("SNS_MessageId"));
		assertEquals("arn:aws:sns:us-east-1:439454740675:TEST_TOPIC", attributes.get("SNS_TopicArn"));
		assertEquals("Test Message Subject", attributes.get("SNS_Subject"));
		assertEquals("2012-05-28T18:06:49.137Z", attributes.get("SNS_Timestamp"));
		assertEquals("1", attributes.get("SNS_SignatureVersion"));
		assertEquals("ZEnX7kCINS4NoDZZZyEaMNWqwidxP7RkwGwDxyXq/RVm/1nFh/dwWVezVKYadwgXoV7GPe5H2jP0ghoW1RPV76PgGV/xNp5/8JGxBhKTgp5HeE9alS4aRGs2ZSqOeyTb/4+N+YAOe5guXgzxbJOZijDXnpDKVRGuJ2Wxetskp6E=",attributes.get("SNS_Signature"));
		assertEquals("https://sns.us-east-1.amazonaws.com/SimpleNotificationService-f3ecfb7224c7233fe7bb5f59f96de52f.pem", attributes.get("SNS_SigningCertURL"));
		assertEquals("https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:439454740675:TEST_TOPIC:01810af0-48b8-4d85-ad7c-0f6a17fa0c63", attributes.get("SNS_UnsubscribeURL"));
		assertEquals("Test Message Content", message.getMessagePayload());
	}


}
