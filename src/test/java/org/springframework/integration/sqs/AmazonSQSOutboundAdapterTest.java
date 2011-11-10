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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.aws.sqs.AmazonSQSMessageHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Amol Nayak
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:sqs-outbound-test.xml"})
public class AmazonSQSOutboundAdapterTest {

	@Autowired
	@Qualifier("sqsOutboundChannel")
	private MessageChannel channel;	
		
	@Test
	public void sendStringMessage() {
		System.out.println("Sending String message over the message channel");
		channel.send(MessageBuilder.withPayload("Some String").build());
	}
	
	@Test
	public void sendIntegerMessage() {
		System.out.println("Sending Integer message over the message channel");
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("SENT_TIME", System.currentTimeMillis() + "");
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(AmazonSQSMessageHeaders.MESSAGE_ATTRIBUTES, attributes);
		channel.send(MessageBuilder.withPayload(Integer.valueOf(10))
				.copyHeaders(headers)
				.build());
	}
	

}
