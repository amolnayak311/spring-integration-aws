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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The strategy where we specify a max number of attempts of re delivery
 * @author Amol Nayak
 *
 */
public class AmazonSQSRedeliveryCountDeliveryStrategy implements
		AmazonSQSMessageDeliveryStrategy {

	private final int maxRedeliveryAttempts;
	private final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<String, Lock>();
	private final ConcurrentHashMap<String, AtomicInteger> redeliveryCount = new ConcurrentHashMap<String, AtomicInteger>();

	/**
	 * Constructor used to initialize with the max number of re delivery attempts
	 * @param maxRedeliveryAttempts
	 */
	public AmazonSQSRedeliveryCountDeliveryStrategy(int maxRedeliveryAttempts) {
		this.maxRedeliveryAttempts = maxRedeliveryAttempts;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.sqs.AmazonSQSMessageDeliveryStrategy#canRedeliver(java.lang.String)
	 */

	public boolean canRedeliver(String messageId) {
		return redeliveryCount.get(messageId).intValue() < maxRedeliveryAttempts;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.sqs.AmazonSQSMessageDeliveryStrategy#notifyFailure(java.lang.String)
	 */

	public void notifyFailure(String messageId) {
		if(!locks.containsKey(messageId))
			locks.putIfAbsent(messageId, new ReentrantLock());
		Lock lock = locks.get(messageId);
		lock.lock();
		try {
			if(!redeliveryCount.containsKey(messageId)) {
				AtomicInteger failureCount = new AtomicInteger(1);
				failureCount = redeliveryCount.putIfAbsent(messageId, failureCount);
				if(failureCount != null) {
					failureCount.incrementAndGet();
				}
			} else {
				redeliveryCount.get(messageId).incrementAndGet();
			}
		} finally {
			lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.sqs.AmazonSQSMessageDeliveryStrategy#notifySuccess(java.lang.String)
	 */

	public void notifySuccess(String messageId) {
		cleanup(messageId);
	}



	public void cleanup(String messageId) {
		//cleanup for the message id
		if(locks.containsKey(messageId)) {
			Lock lock = locks.get(messageId);
			lock.lock();
			try {
				redeliveryCount.remove(messageId);
				locks.remove(messageId);
			} finally {
				lock.unlock();
			}
		}

	}

	public int getFailureCount(String messageId) {
		if(redeliveryCount.containsKey(messageId)) {
			return redeliveryCount.get(messageId).get();
		} else
			return -1;
	}

}
