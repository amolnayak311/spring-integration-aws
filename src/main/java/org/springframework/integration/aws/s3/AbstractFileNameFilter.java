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

import org.springframework.util.StringUtils;

/**
 * The abstract file name filter that first filters out the file if it is 
 * not eligible for filtering based on the name.
 * FOr e.g, if a particular folder on S3 is to be synchronized with the
 * local file system, then the name of the key is initially accepted
 * only if it corresponds to an object directly under that folder on S3.
 * All other keys are ignored
 * @author Amol Nayak
 *
 */
public abstract class AbstractFileNameFilter implements FileNameFilter {

	private String folderName;
	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.FileNameFilter#accept(java.lang.String)
	 */
	
	public boolean accept(String fileName) {
		if(!StringUtils.hasText(fileName))	
			return false;
		
		if(StringUtils.hasText(folderName)) {
			if(fileName.startsWith(folderName)) {
				//This file is in the folder or in a child folder or the given folder
				String relativePath = fileName.substring(folderName.length());
				if(relativePath.indexOf("/") != -1 || relativePath.length() == 0)
					return false;
			} else
				return false;
		} else {
			//Its the folder entry within the bucket
			if(fileName.indexOf("/") != -1)
				return false;
		}
		return isFileNameAccepted(fileName);
	}
	
	
	/**
	 * Gets the folder whose file are to be accepted, this path is relative to the
	 * bucket.
	 * @return
	 */
	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		if(StringUtils.hasText(folderName)) {
			if(!folderName.endsWith("/"))
				this.folderName = folderName + "/";
			else
				this.folderName = folderName;
		} else
			this.folderName = null;
			
	}

	public abstract boolean isFileNameAccepted(String fileName);

}
