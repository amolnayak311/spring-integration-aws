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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.TestUtils;
import org.springframework.integration.aws.sqs.AmazonSQSMessageHandler;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperations;
import org.springframework.integration.endpoint.EventDrivenConsumer;

/**
 * The test class  for {@link AmazonSQSOutboundChannelAdapterParser}
 *
 * @author Amol Nayak
 *
 */
public class AmazonSQSOutboundChannelAdapterParserTest {

	private static final String SQS_DESTINATION = "https://ap-southeast-1.queue.amazonaws.com/439454740675/APAC_TEST_QUEUE";
	private ClassPathXmlApplicationContext ctx;
	private EventDrivenConsumer consumer;

	@Before
	public void setup() {
		ctx = new ClassPathXmlApplicationContext("classpath:SQSOutboundChannelAdapterParserTest.xml");
		consumer = ctx.getBean("outboundAdapter",EventDrivenConsumer.class);

	}

	@After
	public void destroy() {
		ctx.close();
	}

	@Test
	public void testSQSOutboundChannelAdapterParser() {
		AmazonSQSMessageHandler handler = TestUtils.getPropertyValue(consumer, "handler", AmazonSQSMessageHandler.class);
		String destinationString = TestUtils.getPropertyValue(handler, "destinationQueueProcessor.expression.literalValue",String.class);
		assertEquals(SQS_DESTINATION, destinationString);
		assertEquals(SQS_DESTINATION, TestUtils.getPropertyValue(handler, "defaultSQSQueue", String.class));
		AmazonSQSOperations operations = TestUtils.getPropertyValue(handler, "sqsOperations",AmazonSQSOperations.class);
		assertEquals(DummyAmazonSQSOperation.class.getName(), operations.getClass().getName());
	}
}
