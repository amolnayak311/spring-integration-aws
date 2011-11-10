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
import java.io.FileInputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.core.BasicAWSCredentials;
import org.springframework.integration.aws.s3.core.AmazonS3Object;
import org.springframework.integration.aws.s3.core.AmazonS3ObjectACL;
import org.springframework.integration.aws.s3.core.AmazonS3OperationsImpl;
import org.springframework.integration.aws.s3.core.Grantee;
import org.springframework.integration.aws.s3.core.GranteeType;
import org.springframework.integration.aws.s3.core.GroupGranteeType;
import org.springframework.integration.aws.s3.core.ObjectGrant;
import org.springframework.integration.aws.s3.core.ObjectPermissions;
import org.springframework.integration.aws.s3.core.PaginatedObjectsView;
import org.springframework.integration.aws.s3.core.S3ObjectSummary;
import org.springframework.integration.common.BaseTestCase;
/**
 * @author Amol Nayak
 *
 */
public class AmazonS3OperationsImplTest extends BaseTestCase {

	
	
	private AmazonS3OperationsImpl operations;	
	
	
	@Before
	public void setup() {
		AmazonWSCredentials credentials = new BasicAWSCredentials										
							(getProperty("aws.access.key"), getProperty("aws.secret.key"));
		operations = new AmazonS3OperationsImpl(credentials);
		//Max of 5K by single upload
		operations.setMultipartUploadThreshold(5*1024);
		try {
			operations.afterPropertiesSet();
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUploadFromFile() {
		String fileLocation = getProperty("local.file.path");
		System.out.println("Uploading file location");
		AmazonS3ObjectACL acl = new AmazonS3ObjectACL();
		AmazonS3Object object = new AmazonS3Object(null, null, null, new File(fileLocation),acl);		
		//Add grants to amolnayak.spring with read access
		acl.addGrant(new ObjectGrant(
				new Grantee(getProperty("grantee.mail"), GranteeType.EMAIL_GRANTEE_TYPE), 
				ObjectPermissions.READ));
		//Add Write ACL Rights to Canonical ID of amolnayak.spring
		acl.addGrant(new ObjectGrant
				(new Grantee(getProperty("canonical.id"),
						GranteeType.CANONICAL_GRANTEE_TYPE), 
						ObjectPermissions.WRITE_ACP));
		//Add Read ACL rights to all authenticated users group
		acl.addGrant(new ObjectGrant
				(new Grantee(GroupGranteeType.AuthenticatedUsers.getIdentifier(), 
						GranteeType.GROUP_GRANTEE_TYPE), ObjectPermissions.READ_ACP));
		
		operations.putObject(getProperty("bucket.name"),
				getProperty("folder.name"),
				getProperty("uploaded.file.name"),
				object);
		System.out.println("File successfully uploaded");
	}
	
	@Test
	public void uploadFromStream() throws Exception {		
		String fileLocation = getProperty("local.file.path");
		File file = new File(fileLocation);
		FileInputStream fin = new FileInputStream(file); 
		System.out.println("Uploading file location");			

		AmazonS3Object object = new AmazonS3Object(null, null, fin, null);
		operations.putObject(getProperty("bucket.name"), 
				getProperty("folder.name"),
				getProperty("uploaded.file.name"),
				object);		
		System.out.println("File successfully uploaded");		
	}
	
	@Test
	public void listObjects() throws Exception{
		PaginatedObjectsView page;
		String marker = null;
		do {
			page = operations.listObjects(getProperty("bucket.name"), 
					getProperty("folder.name"), 
					marker,2);
			if(page != null) {
				List<S3ObjectSummary> names = page.getObjectSummary();
				for(S3ObjectSummary name:names) {
					System.out.println("\t Object name is: " + name.getKey());
				}
				marker = page.getNextMarker();
				System.out.println("Next Marker is " + marker);
			} else {
				System.out.println("No Object found in the listing");
				break;
			}			
		}while(page.hasMoreResults());
		
	}
}
