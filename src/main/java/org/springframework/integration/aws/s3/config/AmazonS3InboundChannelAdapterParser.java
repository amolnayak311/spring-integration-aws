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
package org.springframework.integration.aws.s3.config;

import java.io.File;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.core.config.AbstractAWSInboundChannelAdapterParser;
import org.springframework.integration.aws.s3.AmazonS3InboundSynchronizationMessageSource;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * The channel adapter for S3 inbound parser
 * @author Amol Nayak
 *
 */
public class AmazonS3InboundChannelAdapterParser extends
		AbstractAWSInboundChannelAdapterParser {

	private static final String S3_BUCKET 						= 	"bucket";
	private static final String TEMPORARY_SUFFIX				=	"temporary-suffix";
	private static final String THREADPOOL_EXECUTOR				=	"thread-pool-executor";
	private static final String REMOTE_DIRECTORY				=	"remote-directory";
	private static final String LOCAL_DIRECTORY					=	"directory";
	private static final String AWS_CREDENTIAL					=	"credentials";
	private static final String MAX_OBJECTS_PER_BATCH			=	"max-objects-per-batch";
	private static final String FILE_WILDCARD					=	"file-wildcard";
	private static final String FILE_NAME_REGEX					=	"file-name-regex";	
	
	/* (non-Javadoc)
	 * @see org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser#parseSource(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
	 */
	
	protected BeanMetadataElement parseSource(Element element,
			ParserContext parserContext) {
		String awsCredentials = registerAmazonWSCredentials(element, parserContext);
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
			.genericBeanDefinition(AmazonS3InboundSynchronizationMessageSource.class);
		builder.addPropertyReference(AWS_CREDENTIAL, awsCredentials);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, S3_BUCKET);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, TEMPORARY_SUFFIX);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, REMOTE_DIRECTORY);
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, THREADPOOL_EXECUTOR);
		String directory = element.getAttribute(LOCAL_DIRECTORY);
		if(StringUtils.hasText(directory))
			builder.addPropertyValue(LOCAL_DIRECTORY, new File(directory));
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, MAX_OBJECTS_PER_BATCH);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, FILE_WILDCARD);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, FILE_NAME_REGEX);
		
		return builder.getBeanDefinition();
	}

}
