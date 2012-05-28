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
package org.springframework.integration.aws.sqs.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.core.config.AbstractAWSInboundChannelAdapterParser;
import org.springframework.integration.aws.sqs.AmazonSQSMessageSource;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;

/**
 * The inbound adapter parser for Amazon SQS Queue
 * @author Amol Nayak
 *
 */
public class AmazonSQSInboundChannelAdapterParser extends
		AbstractAWSInboundChannelAdapterParser {

	private static final String IS_TRANSACTIONAL = "transactional";
	private static final String MAX_REDELIVERY_ATTEMPTS = "max-redelivery-attempts";
	private static final String SQS_QUEUE = "sqs-queue";
	private static final String SQS_OPERATIONS = "sqs-operations";

	/* (non-Javadoc)
	 * @see org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser#parseSource(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
	 */

	@Override
	protected BeanMetadataElement parseSource(Element element, ParserContext parserContext) {
		String awsCredentials = registerAmazonWSCredentials(element, parserContext);
		//Mandated at xsd level, so has to be present
		String sqsQueue = element.getAttribute(SQS_QUEUE);
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
										.genericBeanDefinition(AmazonSQSMessageSource.class)
										.addConstructorArgReference(awsCredentials)
										.addConstructorArgValue(sqsQueue);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, IS_TRANSACTIONAL);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element,MAX_REDELIVERY_ATTEMPTS);
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, SQS_OPERATIONS);
		return builder.getBeanDefinition();
	}
}
