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
package org.springframework.integration.aws.s3.core;

import org.springframework.integration.aws.core.AmazonWSOperationException;

/**
 * An operation indicating a failure in an Amazon S3 Operation
 * @author Amol Nayak
 *
 */
public class AmazonS3OperationException extends AmazonWSOperationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9518185510906801L;
	private String bucket;
	private String objectName;
	
	/**
	 * 
	 * @param accessKey
	 * @param bucket
	 * @param objectName
	 */
	public AmazonS3OperationException(String accessKey, String bucket,
			String objectName) {
		super(accessKey);
		this.bucket = bucket;
		this.objectName = objectName;
	}
	
	

	public AmazonS3OperationException(String accessKey, String bucket,
			String objectName,String message,
			Throwable cause) {
		super(accessKey, message, cause);
		this.bucket = bucket;
		this.objectName = objectName;
	}

	public AmazonS3OperationException(String accessKey, String bucket,
			String objectName,String message) {
		super(accessKey, message);	
		this.bucket = bucket;
		this.objectName = objectName;
	}

	public AmazonS3OperationException(String accessKey, String bucket,
			String objectName,Throwable cause) {
		super(accessKey, cause);
		this.bucket = bucket;
		this.objectName = objectName;
	}
	

	/**
	 * Gets the bucket name for which an S3 operation failed
	 * @return
	 */
	public String getBucket() {
		return bucket;
	}

	/**
	 * Gets the object name where an S3 operation failed
	 * @return
	 */
	public String getObjectName() {
		return objectName;
	}	
}
