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
package org.springframework.integration.aws.ses.core;

import org.springframework.integration.aws.core.AmazonWSOperationException;

/**
 * This exception will be thrown upon failure in sending a mail from Amazon SES
 * @author Amol Nayak
 *
 */
public class AmazonSESMailSendException extends AmazonWSOperationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3035267174544370619L;
	
	private AmazonSESSimpleMailMessage mailMessage;

	public AmazonSESMailSendException(String accessKey, String message,
			Throwable cause,AmazonSESSimpleMailMessage mailMessage) {
		super(accessKey, message, cause);
		this.mailMessage = mailMessage;
	}

	public AmazonSESMailSendException(String accessKey, 
			String message,AmazonSESSimpleMailMessage mailMessage) {
		super(accessKey, message);
		this.mailMessage = mailMessage;
	}

	public AmazonSESMailSendException(String accessKey, 
			Throwable cause,AmazonSESSimpleMailMessage mailMessage) {
		super(accessKey, cause);
		this.mailMessage = mailMessage;
	}

	public AmazonSESMailSendException(String accessKey,AmazonSESSimpleMailMessage mailMessage) {
		super(accessKey);
		this.mailMessage = mailMessage;
	}

	/**
	 * The failed mail message
	 * @return
	 */
	public AmazonSESSimpleMailMessage getMailMessage() {
		return mailMessage;
	}
	
}
