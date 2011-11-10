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
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.aws.s3.FileEvent;
import org.springframework.integration.aws.s3.FileEventHandler;
import org.springframework.integration.aws.s3.InboundLocalFileOperations;
import org.springframework.integration.aws.s3.InboundLocalFileOperationsImpl;
import org.springframework.integration.common.BaseTestCase;

/**
 * @author Amol Nayak
 *
 */
public class FileOperationsTest extends BaseTestCase {

	private InboundLocalFileOperations fileOperations;
	
	@Before
	public void setup() {
		fileOperations = new InboundLocalFileOperationsImpl();
		fileOperations.addEventListener(new FileEventHandler() {			
			
			public void onEvent(FileEvent event) {
				System.out.println("Event Type is " + event.getFileOperation());
				System.out.println("File written is " + event.getFile().getAbsolutePath());
			}
		});			
	}
	
	@Test
	public void writeFile() throws Exception{		
		File directory = new File(props.getProperty("fileoperations.target.directory"));
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("awscredentials.properties");
		fileOperations.writeToFile(directory, "awscredentials.properties",in);
	}
}
