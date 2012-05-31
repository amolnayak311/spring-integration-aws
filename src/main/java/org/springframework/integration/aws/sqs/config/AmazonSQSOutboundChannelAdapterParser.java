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

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.aws.core.config.AbstractAWSOutboundChannelAdapterParser;
import org.springframework.integration.aws.sqs.AmazonSQSMessageHandler;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperationsImpl;
import org.springframework.integration.config.ExpressionFactoryBean;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.core.MessageHandler;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * The outbound channel adapter parser for Amazon SQS
 * @author Amol Nayak
 *
 */
public class AmazonSQSOutboundChannelAdapterParser extends
		AbstractAWSOutboundChannelAdapterParser {

	private static final String DEFAULT_SQS_QUEUE 	= "default-sqs-queue";
	private static final String DESTINATION_QUEUE_EXPRESSION = "destination-queue-expression";
	private static final String VERIFY_SENT_MESSAGE = "verify-sent-message";
	private static final String DESTINATION_QUEUE 	= "destination-queue";
	private static final String SQS_OPERATIONS 		= "sqs-operations";
	private static final String MESSAGE_TRANSFORMER = "message-transformer";



	@Override
	protected Class<? extends MessageHandler> getMessageHandlerImplementation() {
		return AmazonSQSMessageHandler.class;
	}


	@Override
	protected void processBeanDefinition(BeanDefinitionBuilder builder,
			String awsCredentialsGeneratedName, Element element,
			ParserContext context) {
		//lets see the 3 attributes of the bean
		//defaultSQSQueue, destinationQueueProcessor, verifySentMessage
		String defaultSQSDestination = element.getAttribute(DEFAULT_SQS_QUEUE);
		builder.addConstructorArgValue(defaultSQSDestination);
		String destinationQueueExpression = element.getAttribute(DESTINATION_QUEUE_EXPRESSION);
		String destinationQueue = element.getAttribute(DESTINATION_QUEUE);
		boolean hasDestinationQueueExpression = StringUtils.hasText(destinationQueueExpression);
		boolean hasDestinationQueue = StringUtils.hasText(destinationQueue);
		if(!(hasDestinationQueue ^ hasDestinationQueueExpression)) {
			throw new BeanDefinitionStoreException("Exactly one of " + DESTINATION_QUEUE_EXPRESSION + " or "
					+ DESTINATION_QUEUE + " is required");
		}
		AbstractBeanDefinition expression;
		if(hasDestinationQueue) {
			expression = BeanDefinitionBuilder
						.genericBeanDefinition(LiteralExpression.class)
						.addConstructorArgValue(destinationQueue)
						.getBeanDefinition();
		} else {
			expression = BeanDefinitionBuilder
						.genericBeanDefinition(ExpressionFactoryBean.class)
						.addConstructorArgValue(destinationQueueExpression)
						.getBeanDefinition();
		}
		builder.addPropertyValue("destinationQueueExpression", expression);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, VERIFY_SENT_MESSAGE);
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, SQS_OPERATIONS);
		String transformerRef = element.getAttribute(MESSAGE_TRANSFORMER);
		boolean hasTransformerRef = StringUtils.hasText(transformerRef);
		if(!element.hasAttribute(SQS_OPERATIONS)) {
			BeanDefinitionBuilder sqsOpsBuilder =
				BeanDefinitionBuilder.genericBeanDefinition(AmazonSQSOperationsImpl.class)
				.addConstructorArgReference(awsCredentialsGeneratedName);
			if(hasTransformerRef) {
				sqsOpsBuilder.addPropertyReference("messageTransformer", transformerRef);
			}

			String sqsOps = BeanDefinitionReaderUtils.registerWithGeneratedName(sqsOpsBuilder.getBeanDefinition(),
					context.getRegistry());
			builder.addPropertyReference("sqsOperations", sqsOps);
		}
		else {
			if(hasTransformerRef) {
				throw new BeanDefinitionStoreException("Both the attributes,  \"sqs-operations\" and \"message-transformer\" are " +
						"not supported together. Consider injecting the messageTransformer in the sqsOperation's bean definition" +
						" and provide \"sqs-operations\" attribute only");
			}
		}
	}
}
