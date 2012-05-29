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
package org.springframework.integration.aws.s3.config;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.TestUtils;
import org.springframework.integration.aws.s3.FileNameGenerationStrategy;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.endpoint.EventDrivenConsumer;

/**
 * The test class for {@link AmazonS3OutboundChannelAdapterParser}
 * @author Amol Nayak
 *
 */
public class AmazonS3OutboundChannelAdapterParserTest {

	private ClassPathXmlApplicationContext ctx;
	private EventDrivenConsumer adapter1;
	private EventDrivenConsumer adapter2;

	@Before
	public void setup() {
		ctx = new ClassPathXmlApplicationContext("classpath:S3OutboundChannelAdapterParserTest.xml");
		adapter1 = ctx.getBean("adapterOne", EventDrivenConsumer.class);
		adapter2 = ctx.getBean("adapterTwo", EventDrivenConsumer.class);
	}

	@Test
	public void testS3OutboundChannelAdapterParser() {
		MessageHandler handlerOne = TestUtils.getPropertyValue(adapter1, "handler", MessageHandler.class);
		MessageHandler handlerTwo = TestUtils.getPropertyValue(adapter2, "handler", MessageHandler.class);
		assertEquals("ISO-8859-1", TestUtils.getPropertyValue(handlerOne, "charset", String.class));
		assertEquals("test_bucket", TestUtils.getPropertyValue(handlerOne, "bucket",String.class));
		String remoteDir = TestUtils.getPropertyValue(handlerOne, "remoteDirectoryProcessor.expression.literalValue", String.class);
		assertEquals("test", remoteDir);
		File tempDirectory = TestUtils.getPropertyValue(handlerOne, "operations.temporaryDirectory", File.class);
		assertEquals("C:\\Windows\\Temp", tempDirectory.getAbsolutePath());
		assertEquals(10240,TestUtils.getPropertyValue(handlerOne, "operations.multipartUploadThreshold",Long.class).longValue());
		assertEquals(".temp",TestUtils.getPropertyValue(handlerOne, "operations.temporaryFileSuffix",String.class));
		FileNameGenerationStrategy fNameStrategy = TestUtils.getPropertyValue(handlerOne, "fileNameGenerator", FileNameGenerationStrategy.class);
		assertEquals(DummyFilenameGenerationStrategy.class.getName(), fNameStrategy.getClass().getName());
		String expression = TestUtils.getPropertyValue(handlerTwo, "remoteDirectoryProcessor.expression.expression", String.class);
		assertEquals("headers['tempdir']", expression);
		String fNameExpression = TestUtils.getPropertyValue(handlerTwo, "fileNameGenerator.fileNameExpression", String.class);
		assertEquals("headers['file-name']", fNameExpression);
	}

	@After
	public void destroy() {
		ctx.close();
	}
}
