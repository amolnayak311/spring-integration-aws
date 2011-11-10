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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.integration.Message;
import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.s3.core.AmazonS3Object;
import org.springframework.integration.aws.s3.core.AmazonS3OperationException;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.handler.ExpressionEvaluatingMessageProcessor;
import org.springframework.util.Assert;

/**
 * The Message handler for the S3 outbound channel adapter 
 * @author Amol Nayak
 *
 */
public class AmazonS3MessageHandler extends AbstractMessageHandler {
	
	
	private AmazonWSCredentials credentials;
	
	private AmazonS3Operations operations;
	
	private String charset = "UTF-8";
	
	private String bucket;	
	
	private ExpressionEvaluatingMessageProcessor<String> remoteDirectoryProcessor;
	
	private FileNameGenerationStrategy fileNameGenerator;
	
	
	
	
	protected void onInit() throws Exception {		
		super.onInit();
		Assert.notNull(remoteDirectoryProcessor, "Remote Directory processor should be present");
		Assert.notNull(fileNameGenerator,"File name generation strategy should be present");
	}


	/**
	 * 
	 * @param credentials
	 * @param operations
	 */
	public AmazonS3MessageHandler(AmazonWSCredentials credentials,AmazonS3Operations operations) {
		Assert.notNull(operations,"S3 Operations should be non null");
		Assert.notNull(credentials,"AWS Credentials should be non null");
		this.credentials = credentials;
		this.operations = operations;
	}


	/* (non-Javadoc)
	 * @see org.springframework.integration.handler.AbstractMessageHandler#handleMessageInternal(org.springframework.integration.Message)
	 */	
	@SuppressWarnings("unchecked")
	protected void handleMessageInternal(Message<?> message) throws Exception {
		
		Object payload = message.getPayload();
		//The payload can be only of type java.io.File, java.io.InputStream, byte[] or String
		File file = null;
		InputStream in = null;		 
		
		//Below headers are expected to be of correct type
		Map<String, String> userMetaData = 
			(Map<String, String>)message.getHeaders().get(AmazonS3MessageHeaders.USER_METADATA);
		
		Map<String, Object> metaData = 
			(Map<String, Object>)message.getHeaders().get(AmazonS3MessageHeaders.METADATA);
		
		Map<String, Collection<String>> objectAcls = 
			(Map<String, Collection<String>>)message.getHeaders().get(AmazonS3MessageHeaders.OBJECT_ACLS);
		
		AmazonS3ObjectBuilder builder = AmazonS3ObjectBuilder
		.getInstance()
		.withMetaData(metaData)
		.withUserMetaData(userMetaData)
		.withObjectACL(objectAcls);
		
		
		String folder = this.remoteDirectoryProcessor.processMessage(message);
		
		String objectName = this.fileNameGenerator.generateFileName(message);
		
		if(payload instanceof File) {
			file = (File)payload;
		} else if (payload instanceof InputStream) {
			in = (InputStream)in;
		} else if(payload instanceof byte[]) {
			in = new ByteArrayInputStream((byte[])payload);
		} else if(payload instanceof String) {
			in = new ByteArrayInputStream(((String)payload).getBytes(charset));
		} else
			throw new AmazonS3OperationException
			(credentials.getAccessKey(), 
					bucket, objectName, "The Message payload is of unexpected type " 
					+ payload.getClass().getCanonicalName() + ", only supported types are"
					+" java.io.File, java.io.InputStream, byte[] and java.lang.String");
		if(file != null) {
			builder.fromFile(file);
		} else if (in != null) {
			builder.fromInputStream(in);
		}
		
		AmazonS3Object object = builder.build();
		
		if(logger.isDebugEnabled())
			logger.debug("Uploading Object to bucket " + bucket + ", to folder " + folder + ", with object name " + objectName);
		
		operations.putObject(bucket, folder, objectName, object);		
	}


	/**
	 * Sets the charset for the String payload received
	 * @param charset
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}


	/**
	 * Sets the S3 Bucket to which the files are to be uploaded
	 * @param bucket
	 */
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	
	/**
	 * Sets the directory evaluating expression for finding the remote directory in S3 
	 * @param expression
	 */
	public void setRemoteDirectoryExpression(Expression expression) {
		remoteDirectoryProcessor = new ExpressionEvaluatingMessageProcessor<String>(expression);
	}

	/**
	 * Sets the file name generation strategy 
	 * @param fileNameGenerator
	 */
	public void setFileNameGenerator(FileNameGenerationStrategy fileNameGenerator) {
		this.fileNameGenerator = fileNameGenerator;
	}	
}
