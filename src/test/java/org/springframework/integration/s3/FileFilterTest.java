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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.aws.s3.AbstractFileNameFilter;
import org.springframework.integration.aws.s3.AlwaysTrueFileNamefilter;

/**
 * File name filter test
 * @author Amol Nayak
 *
 */
public class FileFilterTest {

	private AbstractFileNameFilter filter;
	
	@Before
	public void init() {
		filter = new AlwaysTrueFileNamefilter();
		filter.setFolderName("test");
	}
	
	@Test
	public void testFilePaths() {
		System.out.println("Testing file path test.txt");
		Assert.assertFalse(filter.accept("test.txt"));
		System.out.println("Testing file path test/test.txt");
		Assert.assertTrue(filter.accept("test/test.txt"));
		System.out.println("Testing file path test/anotherfolder/test.txt");
		Assert.assertFalse(filter.accept("test/anotherfolder/test.txt"));
		System.out.println("testing for root folder");
		filter.setFolderName(null);
		System.out.println("Testing file path test.txt");
		Assert.assertTrue(filter.accept("test.txt"));
		System.out.println("Testing file path test/test.txt");
		Assert.assertFalse(filter.accept("test/test.txt"));
		System.out.println("Testing file path test/anotherfolder/test.txt");
		Assert.assertFalse(filter.accept("test/anotherfolder/test.txt"));
		System.out.println("testing for root folder");
	}
	
}
