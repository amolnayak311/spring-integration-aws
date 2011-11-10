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
package org.springframework.integration.aws.sqs;

import org.springframework.integration.Message;

/**
 * The Headers in the {@link Message} that would be sent to Amazon SQS or that is 
 * received from Amazon SQS 
 * @author Amol Nayak
 *
 */
public interface AmazonSQSMessageHeaders {

	static final String MESSAGE_ATTRIBUTES = "message_attributes";
	static final String SERVER_SIDE_PAYLOAD_MD5 = "server_side_payload_md5";
	static final String MESSAGE_ID = "mesage_id";
	static final String MESSAGE_RECEIPT_HANDLE = "message_receipt_handle";
	
}
