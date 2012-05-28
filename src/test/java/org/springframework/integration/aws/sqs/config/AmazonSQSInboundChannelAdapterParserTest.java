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
package org.springframework.integration.aws.sqs.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.TestUtils;
import org.springframework.integration.aws.sqs.AmazonSQSMessageDeliveryStrategy;
import org.springframework.integration.aws.sqs.AmazonSQSMessageSource;
import org.springframework.integration.aws.sqs.AmazonSQSRedeliveryCountDeliveryStrategy;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperations;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;

/**
 * The test class for the {@link AmazonSQSInboundChannelAdapterParser}
 * @author Amol Nayak
 *
 */
public class AmazonSQSInboundChannelAdapterParserTest {

	private SourcePollingChannelAdapter consumer;
	private ClassPathXmlApplicationContext ctx;

	@Before
	public void setup() {
		ctx = new ClassPathXmlApplicationContext("classpath:SQSInboundChannelAdapterParserTest.xml");
		consumer = ctx.getBean("inboundChannelAdapter",SourcePollingChannelAdapter.class);
	}

	@After
	public void destroy() {
		ctx.close();
	}

	@Test
	public void testSQSInboundChannelAdapterParser() {
		AmazonSQSMessageSource source =
			TestUtils.getPropertyValue(consumer, "source", AmazonSQSMessageSource.class);
		assertNotNull(source);
		String sqsQueue = TestUtils.getPropertyValue(source, "sqsQueue", String.class);
		assertEquals("https://queue.amazonaws.com/439454740675/MyTestQueue", sqsQueue);
		assertEquals(true, TestUtils.getPropertyValue(source, "isTransactional", Boolean.class));
		assertEquals(3, TestUtils.getPropertyValue(source, "maxRedeliveryAttempts", Integer.class).longValue());
		AmazonSQSMessageDeliveryStrategy redeliveryStrategy =
			TestUtils.getPropertyValue(source, "redeliveryStrategy", AmazonSQSMessageDeliveryStrategy.class);
		assertEquals(AmazonSQSRedeliveryCountDeliveryStrategy.class.getName(),
				redeliveryStrategy.getClass().getName());
		assertEquals(DummyAmazonSQSOperation.class.getName(),
						TestUtils.getPropertyValue(source, "sqsOperations", AmazonSQSOperations.class).getClass().getName());

	}
}
