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

import java.util.Collection;

import org.springframework.integration.aws.sqs.core.AmazonSQSMessage;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperations;
import org.springframework.integration.aws.sqs.core.AmazonSQSSendMessageResponse;

/**
 * The dummy implementation of the SQS Operations
 * @author Amol Nayak
 *
 */
public class DummyAmazonSQSOperation implements AmazonSQSOperations {

	public AmazonSQSSendMessageResponse sendMessage(String queueURL,
			AmazonSQSMessage message) {

		return null;
	}

	public Collection<AmazonSQSMessage> receiveMessages(String queueURL,
			int maxNumberOfMessages) {

		return null;
	}

	public void deleteMessage(String receiptHandle, String queueURL) {


	}
}
