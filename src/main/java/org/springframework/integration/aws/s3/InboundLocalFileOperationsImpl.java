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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The Implementation class for the {@link InboundLocalFileOperations}
 * @author Amol Nayak
 *
 */
public class InboundLocalFileOperationsImpl implements
		InboundLocalFileOperations {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private List<FileEventHandler> handlers = new ArrayList<FileEventHandler>();
	private String tempFileSuffix = ".writing";
	
	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.InboundLocalFileOperations#addEventListener(org.springframework.integration.aws.s3.FileEventHandler)
	 */
	
	public void addEventListener(FileEventHandler handler) {
		Assert.notNull(handler, "Handler instance must non null");
		handlers.add(handler);
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.InboundLocalFileOperations#setEventListeners(java.util.List)
	 */
	
	public void setEventListeners(List<FileEventHandler> handlers) {
		Assert.notNull(handlers, "Handlers must be non null and non empty");
		Assert.notEmpty(handlers, "Handlers must be non null and non empty");
		this.handlers.addAll(handlers);
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.InboundLocalFileOperations#setTemporaryFileSuffix(java.lang.String)
	 */
	
	public void setTemporaryFileSuffix(String tempFileSuffix) {
		if(!StringUtils.hasText(tempFileSuffix))
			return;
		if(!tempFileSuffix.startsWith("."))
			this.tempFileSuffix = "." + tempFileSuffix;
		else	
			this.tempFileSuffix = tempFileSuffix;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.InboundLocalFileOperations#writeToFile(java.io.File, java.lang.String, java.io.InputStream)
	 */
	
	public void writeToFile(File directory, String fileName, InputStream in)
		throws IOException {
		Assert.isTrue(directory!= null && StringUtils.hasText(fileName) && in != null,
				"Please provide a valid directory location, non empty file name and non null InputStream");
		if(!directory.isDirectory())
			throw new IllegalArgumentException("File parameter passed should be a directory");		
		
		if(!(in instanceof ByteArrayInputStream) 
				&& !(in instanceof BufferedInputStream)) {
			in = new BufferedInputStream(in);
		}
		String tempFileName = fileName + tempFileSuffix;
		byte[] bytes = new byte[4096];	//4K
		
		String absoluteDirectoryPath = directory.getAbsolutePath();
		String filePath;
		if(absoluteDirectoryPath.endsWith(File.separator)) 
			filePath = absoluteDirectoryPath + tempFileName;
		else
			filePath = absoluteDirectoryPath + File.separator + tempFileName;
		
		final File fileToWrite = new File(filePath);
		FileOutputStream fos = new FileOutputStream(fileToWrite);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		for(int read = 0;(read = in.read(bytes)) != -1;) {
			bos.write(bytes, 0, read);
		}		
		bos.close();
		//Now rename the file
		final File dest = new File(filePath.substring(0, filePath.indexOf(tempFileSuffix)));
		//ifDestination file exists, delete it
		if(dest.exists()) {
			boolean isDeleteSuccessful = dest.delete();
			if(isDeleteSuccessful) {
				if(logger.isDebugEnabled())
					logger.debug("Delete of file " + dest.getName() + " successful");
			} else {
				if(logger.isWarnEnabled())
					logger.warn("Deletion of file " + dest.getName() + " not successful");
			}				
			//TODO: Check why the delete is not working			
		}
		final boolean isRenameSuccessful = fileToWrite.renameTo(dest);
		if(isRenameSuccessful ) {
			if(logger.isDebugEnabled())
				logger.debug("Renaming of file " + dest.getName() + " to " 
						+ fileToWrite.getName() + " successful");
		} else {
			if(logger.isWarnEnabled())
				logger.warn("Renaming of file " + dest.getName() + " to " 
						+ fileToWrite.getName() + " unsuccessful");
		}				
		//notify the listeners
		if(!handlers.isEmpty()) {
			FileEvent event = new FileEvent() {
				
				
				public FileOperationType getFileOperation() {					
					return FileOperationType.CREATE;
				}
				
				
				public File getFile() {
					if(isRenameSuccessful)
						return dest;
					else						
						return fileToWrite;
				}
			};
			for(FileEventHandler handler:handlers) {
				try {
					handler.onEvent(event);
				} catch (Exception e) {
					if(logger.isInfoEnabled())
						logger.info("Exception occurred while notifying the handler class " 
								+ handler.getClass().getName(), e);
				}
			}
		}
	}
}

