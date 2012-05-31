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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.core.config.AbstractAWSInboundChannelAdapterParser;
import org.springframework.integration.aws.sqs.AmazonSQSMessageSource;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperationsImpl;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * The inbound adapter parser for Amazon SQS Queue
 * @author Amol Nayak
 *
 */
public class AmazonSQSInboundChannelAdapterParser extends
		AbstractAWSInboundChannelAdapterParser {


	private static final Log logger = LogFactory.getLog(AmazonSQSInboundChannelAdapterParser.class);

	private static final String IS_TRANSACTIONAL = "transactional";
	private static final String MAX_REDELIVERY_ATTEMPTS = "max-redelivery-attempts";
	private static final String SQS_QUEUE = "sqs-queue";
	private static final String SQS_OPERATIONS = "sqs-operations";
	private static final String CHECK_SNS_NOTIFICATION = "check-sns-notification";
	private static final String SNS_HEADER_PREFIX = "sns-header-prefix";
	private static final String MESSAGE_TRANSFORMER = "message-transformer";



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
		String messageTransformerRef = element.getAttribute(MESSAGE_TRANSFORMER);
		boolean hasMessageTransformerRef = StringUtils.hasText(messageTransformerRef);

		boolean hasSnsHeaderPrefix = false;
		String snsNotificationAttribute = element.getAttribute(CHECK_SNS_NOTIFICATION);
		boolean checkSnsNotification = StringUtils.hasText(snsNotificationAttribute);
		String snsHeaderPrefix = null;
		if(checkSnsNotification) {
			snsHeaderPrefix = element.getAttribute(SNS_HEADER_PREFIX);
			hasSnsHeaderPrefix = StringUtils.hasText(snsHeaderPrefix);
		}

		if(!element.hasAttribute(SQS_OPERATIONS)) {
			BeanDefinitionBuilder sqsOperationsBuilder =
				BeanDefinitionBuilder.genericBeanDefinition(AmazonSQSOperationsImpl.class)
			.addConstructorArgReference(awsCredentials);
			if(hasMessageTransformerRef) {
				sqsOperationsBuilder.addPropertyReference("messageTransformer", messageTransformerRef);
			}

			if(checkSnsNotification) {
				sqsOperationsBuilder.addPropertyValue("checkSnsNotification", true);
				if(hasSnsHeaderPrefix) {
					sqsOperationsBuilder.addPropertyValue("snsHeaderPrefix",snsHeaderPrefix);
				}
			}

			//sqs_operations attribute not defined, register the default one
			String operationsBean = BeanDefinitionReaderUtils.registerWithGeneratedName(
					sqsOperationsBuilder.getBeanDefinition()
					,parserContext.getRegistry());
			builder.addPropertyReference("sqsOperations", operationsBean);
		}
		else {
			if(hasMessageTransformerRef) {
				//This means, we have a reference to both sqs operations and message transformer provided
				throw new BeanDefinitionStoreException("Both the attributes,  \"sqs-operations\" and \"message-transformer\" are " +
						"not supported together. Consider injecting the messageTransformer in the sqsOperation's bean definition" +
						" and provide \"sqs-operations\" attribute only");
			}
			if(checkSnsNotification) {
				logger.warn("check-sns-notification and sns-header-prefix attributes are supported" +
						" only when default implementation of sqs operations is used");
			}

		}
		return builder.getBeanDefinition();
	}
}
