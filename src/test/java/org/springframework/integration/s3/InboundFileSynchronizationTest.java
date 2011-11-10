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
package org.springframework.integration.s3;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.core.BasicAWSCredentials;
import org.springframework.integration.aws.s3.InboundFileSynchronizationImpl;
import org.springframework.integration.aws.s3.InboundLocalFileOperationsImpl;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.aws.s3.core.AmazonS3OperationsImpl;
import org.springframework.integration.common.BaseTestCase;

/**
 * @author Amol Nayak
 *
 */
public class InboundFileSynchronizationTest extends BaseTestCase {

	private InboundFileSynchronizationImpl synchronizer;
	
	//TODO: test synchronizing a MP upload file with MD5 hash and one with MD5 hash metadata
	
		
	@Before
	public void init() throws Exception {
		AmazonWSCredentials credentials = 
			new BasicAWSCredentials(props.getProperty("aws.access.key"), 
					props.getProperty("aws.secret.key"));
		AmazonS3Operations operations = new AmazonS3OperationsImpl(credentials);
		synchronizer = new InboundFileSynchronizationImpl(operations,new InboundLocalFileOperationsImpl());
		synchronizer.setFileWildcard("*.txt");
		synchronizer.afterPropertiesSet();
		
	}
	
	@Test
	public void testSynchronize() {
		synchronizer.synchronizeToLocalDirectory(new File(getProperty("local.sync.directory")), 
				getProperty("bucket.name"), getProperty("folder.name"));
	}
	
	@Test
	public void testSynchronizeEmptyBucket() {
		synchronizer.synchronizeToLocalDirectory(new File(getProperty("local.sync.directory")), 
				getProperty("empty.bucket.name"), null);
	}
	
	@Test
	public void testSynchronizeNonEmptyBucketRoot() {
		synchronizer.synchronizeToLocalDirectory(new File(getProperty("local.sync.directory")), 
				getProperty("bucket.name"), "/");
	}
	
	@Test
	public void testSynchronizeTwoLevelBucket() {
		synchronizer.synchronizeToLocalDirectory(new File(getProperty("local.sync.directory")), 
				getProperty("bucket.name"), getProperty("inner.folder.name"));
	}	
}
