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

import org.springframework.integration.Message;
import org.springframework.integration.aws.s3.FileNameGenerationStrategy;

/**
 * The dummy file name generation strategy to be used in the test cases
 * @author Amol Nayak
 *
 */
public class DummyFilenameGenerationStrategy implements
		FileNameGenerationStrategy {

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.FileNameGenerationStrategy#generateFileName(org.springframework.integration.Message)
	 */
	public String generateFileName(Message<?> message) {
		return null;
	}

}
