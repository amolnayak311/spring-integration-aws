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
package org.springframework.integration.sqs;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.MessagingException;
import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.core.BasicAWSCredentials;
import org.springframework.integration.aws.sqs.AmazonSQSMessageHeaders;
import org.springframework.integration.aws.sqs.core.AmazonSQSMessage;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperations;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperationsImpl;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Amol Nayak
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:sqs-inbound-test.xml","classpath:sqs-outbound-test.xml"})
public class AmazonSQSInboundAdapterTest {

	@Autowired
	@Qualifier("inboundSQSAdapter")
	private SubscribableChannel subscribableChannel;

	@Autowired
	@Qualifier("sqsOutboundChannel")
	private MessageChannel channel;

	@Autowired
	@Qualifier("errorChannel")
	private PublishSubscribeChannel errorChannel;

	private AmazonSQSOperations operations;

	@Autowired
	@Qualifier("props")
	private Properties props;



	@Before
	public void init() {
		AmazonWSCredentials credentials = new BasicAWSCredentials
		(props.getProperty("aws.access.key"), props.getProperty("aws.secret.key"));
		operations = new AmazonSQSOperationsImpl(credentials);

		errorChannel.subscribe(new MessageHandler() {


			public void handleMessage(Message<?> message) throws MessagingException {
				System.out.println("\n\nReceived Message over Error chnnel");
				MessageHeaders headers = message.getHeaders();
				Set<String> headerKeys = headers.keySet();
				for(String key:headerKeys) {
					System.out.println("\t" + key + ": " + headers.get(key));
				}
				Object payload = message.getPayload();
				System.out.println("Message payload is " + payload + " of type " + payload.getClass());

			}
		});

		subscribableChannel.subscribe(new MessageHandler() {

			public void handleMessage(Message<?> msg) throws MessagingException {
				System.out.println("Received message with following Details:");
				System.out.println("All Header values are:");
				MessageHeaders headers = msg.getHeaders();
				Set<String> headerKeys = headers.keySet();
				for(String key:headerKeys) {
					System.out.println("\t" + key + ": " + headers.get(key));
				}
				Object payload = msg.getPayload();
				System.out.println("Message payload is " + payload + " of type " + payload.getClass());

			}
		});
	}

	//@Test
	public void sendCorrectMessage() {
		System.out.println("\n\nSending Correct Integer message over the message channel");
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("SENT_TIME", System.currentTimeMillis() + "");
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(AmazonSQSMessageHeaders.MESSAGE_ATTRIBUTES, attributes);
		channel.send(MessageBuilder.withPayload(Integer.valueOf(10))
				.copyHeaders(headers)
				.build());
	}

	//@Test
	public void sendGarbledMessage() {
		System.out.println("\n\nSending garbled message");
		AmazonSQSMessage msg = new AmazonSQSMessage();
		msg.setOriginalMessagePayloadType(Integer.class);
		msg.setMessagePayload("Some String");
		operations.sendMessage(props.getProperty("sqs.queue"), msg);
	}

	@Test
	public void waitForever() throws Exception {
		System.out.println("\n\nWaiting forever");
		while(true) {
		Thread.sleep(10000);
		}
	}

}
