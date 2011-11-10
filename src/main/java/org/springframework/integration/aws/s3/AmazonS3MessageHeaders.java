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
package org.springframework.integration.aws.s3;

/**
 * Interface containing the Constants defining 
 * @author Amol Nayak
 *
 */
public interface AmazonS3MessageHeaders {

	public static final String FILE_NAME 			= "file_name";
	public static final String USER_METADATA		= "user_meta_data";
	public static final String METADATA				= "meta_data";
	public static final String OBJECT_ACLS			= "object_acls";
}
