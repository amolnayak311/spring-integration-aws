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
package org.springframework.integration.sqs;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.core.BasicAWSCredentials;
import org.springframework.integration.aws.sqs.core.AmazonSQSMessage;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperations;
import org.springframework.integration.aws.sqs.core.AmazonSQSOperationsImpl;
import org.springframework.integration.aws.sqs.core.AmazonSQSSendMessageResponse;
import org.springframework.integration.common.BaseTestCase;

/**
 * @author Amol Nayak
 *
 */
public class AmazonSQSOperationsTest extends BaseTestCase {
	
	private AmazonSQSOperations operations;
	private String queue;
	
	@Before
	public void setup() {
		AmazonWSCredentials credentials = new BasicAWSCredentials										
			(getProperty("aws.access.key"), getProperty("aws.secret.key"));
		operations = new AmazonSQSOperationsImpl(credentials);
		queue = getProperty("sqs.queue");
	}
	
	@Test
	public void sendMessage() {		
		System.out.println("\n\nSending message to " + queue);
		AmazonSQSMessage msg = new AmazonSQSMessage();
		msg.setMessagePayload("test payload");
		AmazonSQSSendMessageResponse resp = 
			operations.sendMessage(queue, 
				msg);
		System.out.println("Message id is " + resp.getMessageId());
		System.out.println("MD5 sum is " + resp.getResponseMD5Sum());
	}
	
	//@Test
	public void receiveMessage() {
		System.out.println("\n\nReceiving message from " + queue);
		Collection<AmazonSQSMessage> messages = operations.receiveMessages(queue, 1);
		Assert.assertTrue(messages != null && !messages.isEmpty());
		for(AmazonSQSMessage msg:messages) {
			System.out.println("MD5 Sum of the message body is: " + msg.getMD5OfBody());
			System.out.println("Receipt handle is : " + msg.getReceiptHandle());
			System.out.println("Message Id is: " + msg.getMessageId());
			System.out.println("Payload is " + msg.getMessagePayload());
		}
	}
	
	@Test
	public void deleteMessage()  {
		System.out.println("\n\nDeleting message from " + queue);
		Collection<AmazonSQSMessage> messages;
		do {
			messages = operations.receiveMessages(queue, 1);
		}while(messages.isEmpty());		
		
		String receiptHandle = null;
		for(AmazonSQSMessage msg:messages) {			
			receiptHandle = msg.getReceiptHandle();
			System.out.println("Receipt Handle is " + receiptHandle);
			break;
		}
		System.out.println("Message read, waiting for 45 secs");
		try {
			Thread.sleep(45 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}//let wait for 45 secs 
		System.out.println("Receiving the message again");
		Collection<AmazonSQSMessage> msg ;
		do {
			msg = operations.receiveMessages(queue, 1);		//This will generate a new handle, but we'll delete with old one
		} while(msg.isEmpty());
		
		System.out.println("Receipt handle is " + msg.iterator().next().getReceiptHandle());	
		System.out.println("Deleting with handle " + receiptHandle);
		operations.deleteMessage(receiptHandle, queue);
		System.out.println("Message deleted successfully");
	}
}
