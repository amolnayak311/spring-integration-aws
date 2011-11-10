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

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.integration.Message;
import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.s3.core.AmazonS3OperationsImpl;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

/**
 * The message source used to receive the File instances stored on the local file system
 * synchronized from the S3
 *  
 * @author Amol Nayak
 *
 */
public class AmazonS3InboundSynchronizationMessageSource extends
		IntegrationObjectSupport implements MessageSource<File>,FileEventHandler {


	private InboundFileSynchronizer synchronizer;
	private String bucket;
	private String remoteDirectory;
	private File directory;
	private AmazonWSCredentials credentials;
	private ThreadPoolExecutor threadPoolExecutor;
	private String temporarySuffix;
	private int maxObjectsPerBatch;
	private String fileWildcard;
	private String fileNameRegex;
	private BlockingQueue<File> filesQueue;
	private int queueSize;
	//We will hard code the max queue capacity here
	private final int MAX_QUEUE_CAPACITY = 1024;
	
	
	public Message<File> receive() {
		File headElement = filesQueue.poll();
		if(headElement == null) {
			synchronizer.synchronizeToLocalDirectory(directory, bucket, remoteDirectory);
			//Now check the queue again
			headElement = filesQueue.poll();
		}
		if(headElement != null)
			return MessageBuilder.withPayload(headElement).build();
		else
			return null;
	}

	
	protected void onInit() throws Exception {		
		Assert.notNull(bucket,"Providing a valid S3 Bucket name is mandatory");		
		Assert.isTrue(directory != null && directory.exists() && directory.isDirectory(), 
				"Please provide a valid local directory to synchronize the remote files with");
		
		//First instantiate the SSOperations instance 
		AmazonS3OperationsImpl s3Operations = new AmazonS3OperationsImpl(credentials);	
		s3Operations.setTemporaryFileSuffix(temporarySuffix);
		s3Operations.setThreadPoolExecutor(threadPoolExecutor);		
		s3Operations.afterPropertiesSet();
		
		//Now the file operations class
		InboundLocalFileOperationsImpl fileOperations = new InboundLocalFileOperationsImpl();
		fileOperations.setTemporaryFileSuffix(temporarySuffix);
		fileOperations.addEventListener(this);
		
		
		InboundFileSynchronizationImpl synchronizationImpl = 
			new InboundFileSynchronizationImpl(s3Operations, fileOperations);
		synchronizationImpl.setSynchronizingBatchSize(maxObjectsPerBatch);
		synchronizationImpl.setFileWildcard(fileWildcard);
		synchronizationImpl.setFileNamePattern(fileNameRegex);
		synchronizationImpl.afterPropertiesSet();
		this.synchronizer = synchronizationImpl;
		
		filesQueue = new ArrayBlockingQueue<File>(queueSize > 0 && queueSize < MAX_QUEUE_CAPACITY?queueSize:MAX_QUEUE_CAPACITY);
	}
	
	//-- For Spring DI

	public void setCredentials(AmazonWSCredentials credentials) {
		this.credentials = credentials;
	}

	public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
		this.threadPoolExecutor = threadPoolExecutor;
	}

	public void setTemporarySuffix(String temporarySuffix) {
		this.temporarySuffix = temporarySuffix;
	}
	
	public void setMaxObjectsPerBatch(int maxObjectsPerBatch) {
		this.maxObjectsPerBatch = maxObjectsPerBatch;
	}

	public void setFileWildcard(String fileWildcard) {
		this.fileWildcard = fileWildcard;
	}

	public void setFileNameRegex(String fileNameRegex) {
		this.fileNameRegex = fileNameRegex;
	}	
	
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public void setRemoteDirectory(String remoteDirectory) {
		this.remoteDirectory = remoteDirectory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}
	
	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}
	
	//----

	

	
	public void onEvent(FileEvent event) {
		//We are interested in Create new file events only
		if(FileOperationType.CREATE.equals(event.getFileOperation())) {
			try {
				filesQueue.put(event.getFile());
				//The call hierarchy is
				//if, no file found in queue, then 
				// receive() 
				//	-> InboundFileSynchronizer.synchronizeToLocalDirectory()
				//	->InboundLocalFileOperations.writeToFile()
				//	->onEvent()
				//If the Queue is full and the thread blocks, the lock in synchronizeToLocalDirectory
				//stays and hence preventing further concurrent synchronization
			} catch (InterruptedException e) {				
				logger.error("Interrupted while waiting to put the event on the filesQueue", e);
			}
		}
		
	}
}
