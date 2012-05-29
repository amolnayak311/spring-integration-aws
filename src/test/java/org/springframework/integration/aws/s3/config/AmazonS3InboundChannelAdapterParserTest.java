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
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.TestUtils;
import org.springframework.integration.aws.s3.AmazonS3InboundSynchronizationMessageSource;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;

/**
 * The test class for {@link AmazonS3InboundChannelAdapterParser}
 * @author Amol Nayak
 *
 */
public class AmazonS3InboundChannelAdapterParserTest {

	private ClassPathXmlApplicationContext ctx;
	private SourcePollingChannelAdapter adapterOne;
	private SourcePollingChannelAdapter adapterTwo;

	@Before
	public void setup() {
		ctx = new ClassPathXmlApplicationContext("classpath:S3InboundChannelAdapterParserTest.xml");
		adapterOne = ctx.getBean("inboundOne",SourcePollingChannelAdapter.class);
		adapterTwo = ctx.getBean("inboundTwo",SourcePollingChannelAdapter.class);
	}

	@Test
	public void testS3InboundChannelAdapterParser() {
		AmazonS3InboundSynchronizationMessageSource messageSource =
			TestUtils.getPropertyValue(adapterOne, "source", AmazonS3InboundSynchronizationMessageSource.class);
		AmazonS3InboundSynchronizationMessageSource messageSourceOne =
			TestUtils.getPropertyValue(adapterTwo, "source", AmazonS3InboundSynchronizationMessageSource.class);
		assertEquals("test_bucket",TestUtils.getPropertyValue(messageSource, "bucket", String.class));
		assertEquals(".write",TestUtils.getPropertyValue(messageSource, "synchronizer.client.temporaryFileSuffix", String.class));
		assertEquals("test",TestUtils.getPropertyValue(messageSource, "remoteDirectory", String.class));
		File file = TestUtils.getPropertyValue(messageSource, "directory", File.class);
		assertEquals(10,TestUtils.getPropertyValue(messageSource, "synchronizer.maxObjectsPerBatch", Integer.class).intValue());
		assertEquals("C:\\Windows\\Temp",file.getAbsolutePath());
		String[] wildcards = TestUtils.getPropertyValue(messageSource, "synchronizer.filter.filter.wildcards", String[].class);
		assertNotNull(wildcards);
		assertEquals(1, wildcards.length);
		assertEquals("*.txt", wildcards[0]);
		Pattern pattern = TestUtils.getPropertyValue(messageSourceOne, "synchronizer.filter.filter.pattern", Pattern.class);
		assertEquals("[a-zA-Z0-9]+\\.txt", pattern.pattern());
	}

	@After
	public void destroy() {
		ctx.close();
	}
}
