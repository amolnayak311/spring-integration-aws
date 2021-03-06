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
package org.springframework.integration.aws.ses.config;

import org.springframework.integration.aws.core.config.AbstractAWSOutboundChannelAdapterParser;
import org.springframework.integration.aws.ses.AmazonSESMessageHandler;
import org.springframework.integration.core.MessageHandler;

/**
 * parse the &lt;outbound-channel-adapter/&gt; of the "aws-ses" namespace
 * @author Amol Nayak
 *
 */
public class AmazonSESOutboundAdapterParser extends
		AbstractAWSOutboundChannelAdapterParser {

	
	public Class<? extends MessageHandler> getMessageHandlerImplementation() {		
		return AmazonSESMessageHandler.class;
	}
	
}
