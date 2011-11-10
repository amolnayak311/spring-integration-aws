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
package org.springframework.integration.aws.s3.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.aws.core.AmazonWSCommonUtils;
import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.core.AmazonWSOperationException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CanonicalGrantee;
import com.amazonaws.services.s3.model.EmailAddressGrantee;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import com.amazonaws.services.s3.transfer.Upload;


/**
 * The implementation classes of {@link AmazonS3Operations}
 * @author Amol Nayak
 *
 */
public class AmazonS3OperationsImpl implements AmazonS3Operations, InitializingBean {

	
	private final Log logger = LogFactory.getLog(getClass());
	
	private AmazonWSCredentials credentials;
	
	private AmazonS3Client client;
	
	private TransferManager transferManager;	//Used to upload to S3 
	
	private long multipartUploadThreshold;
	
	private ThreadPoolExecutor threadPoolExecutor;
	
	private File temporaryDirectory = new  File(System.getProperty("java.io.tmpdir"));
	
	private String temporaryFileSuffix = ".writing";
	
	public final String PATH_SEPARATOR = "/";	

	/**
	 * Constructor
	 * @param credentials
	 */
	public AmazonS3OperationsImpl(AmazonWSCredentials credentials) {
		if(credentials == null)
			throw new AmazonWSOperationException(null, "Credentials cannot be null, provide a non null valid set of credentials");
		this.credentials = credentials;
		
		client = new AmazonS3Client(new BasicAWSCredentials(credentials.getAccessKey(), 
				credentials.getSecretKey()));		
	}
	
	
	/**
	 * The implemented afterPropertiesSet method
	 */
	public void afterPropertiesSet() throws Exception {
		if(threadPoolExecutor == null) {
			//Will use the Default Executor, 
			//See com.amazonaws.services.s3.transfer.internal.TransferManagerUtils for more details			
			transferManager = new TransferManager(client);
		} else {
			transferManager = new TransferManager(client, threadPoolExecutor);
		}		
		//As per amazon it is recommended to use Multi part upload above 100 MB
		if(multipartUploadThreshold > 0) {
			TransferManagerConfiguration config = new TransferManagerConfiguration();
			if(multipartUploadThreshold > Integer.MAX_VALUE)
				config.setMultipartUploadThreshold(Integer.MAX_VALUE);		//2GB
			else
				config.setMultipartUploadThreshold((int)multipartUploadThreshold);
			
			transferManager.setConfiguration(config);
		}		
		//If none is set, we use the default
	}




	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.AmazonS3Operations#listObjects(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public PaginatedObjectsView listObjects(String bucketName, String folder,String nextMarker,int pageSize) {
		if(logger.isDebugEnabled()) {
			logger.debug("Listing objects from bucket " + bucketName + " and folder " + folder);
			logger.debug("Next marker is " + nextMarker  + " and pageSize is " + pageSize);
		}
			
		Assert.notNull(StringUtils.hasText(bucketName), "Bucket name should be non null and non empty");
		String prefix = null;
		if(folder != null && !"/".equals(folder)) {
			prefix = folder;
		}
		ListObjectsRequest listObjectsRequest = 
			new ListObjectsRequest()
			.withBucketName(bucketName)
			.withPrefix(prefix)
			.withMarker(nextMarker);
		
		if(pageSize > 0) {
			listObjectsRequest.withMaxKeys(pageSize);
		}
			
		ObjectListing listing = client.listObjects(listObjectsRequest);
		PaginatedObjectsView view = null;
		List<com.amazonaws.services.s3.model.S3ObjectSummary> summaries = listing.getObjectSummaries();
		if(summaries != null && !summaries.isEmpty()) {
			List<S3ObjectSummary> objectSummaries = new ArrayList<S3ObjectSummary>();
			for(final com.amazonaws.services.s3.model.S3ObjectSummary summary:summaries) {
				S3ObjectSummary summ = new S3ObjectSummary() {
					
					
					public long getSize() {
						return summary.getSize();
					}
					
					
					public Date getLastModified() {						
						return summary.getLastModified();
					}
					
					
					public String getKey() {
						return summary.getKey();
					}
					
					
					public String getETag() {
						return summary.getETag();
					}
					
					
					public String getBucketName() {						
						return summary.getBucketName();
					}
				};
				objectSummaries.add(summ);				
			}
			view = new PagninatedObjectsViewImpl(objectSummaries,listing.getNextMarker());
		}
		return view;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.AmazonS3Operations#putObject(java.lang.String, java.lang.String, java.lang.String, org.springframework.integration.aws.s3.AmazonS3Object)
	 */
	
	public void putObject(String bucketName, String folder, String objectName,
			AmazonS3Object s3Object) {
		if(logger.isDebugEnabled()) {
			logger.debug("Putting object to bucket " + bucketName + " and folder " + folder);
			logger.debug("Object Name is " + objectName);
		}

		if(objectName == null)
			throw new AmazonS3OperationException(credentials.getAccessKey(), 
					bucketName, 
					objectName,
					"Object Name is Mandatory");
		
		boolean isTempFile = false;
		File file = s3Object.getFileSource();
		InputStream in = s3Object.getInputStream();
		
		if(file != null && in != null)
			throw new AmazonS3OperationException(credentials.getAccessKey(), 
					bucketName, 
					objectName,
					"File Object and Input Stream in the S3 Object are mutually exclusive");
		
		if(file == null && in == null)
			throw new AmazonS3OperationException(credentials.getAccessKey(), 
					bucketName, 
					objectName,
					"At lease one of File object or Input Stream in the S3 Object are mandatory");
		
		String key;
		if(folder != null) {
			key = folder.endsWith(PATH_SEPARATOR)?
					folder + objectName:folder + PATH_SEPARATOR + objectName;
		} else {
			key = objectName;
		}
		
		if(in != null) {
			file = getTempFile(in,bucketName,objectName);
			isTempFile = true;
		}
		
		PutObjectRequest request;
		if(file != null) {
			request = new PutObjectRequest(bucketName, key, file);
			//if the size of the file is greater than the threshold for multipart upload,
			//set the Content-MD5 header for this upload. This header will also come handy
			//later in inbound-channel-adapter where we cant find the MD5 sum of the 
			//multipart upload file from its ETag
			String stringContentMD5 = null;
			try {
				stringContentMD5 = 
					AmazonWSCommonUtils.encodeHex(AmazonWSCommonUtils.getContentsMD5AsBytes(file));
			} catch (UnsupportedEncodingException e) {				
				logger.error("Exception while generating the content's MD5 of the file " + file.getAbsolutePath(), e);
			}
			
			if(stringContentMD5 != null) {
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentMD5(stringContentMD5);
				request.withMetadata(metadata);
			}
		} else 
			throw new AmazonS3OperationException(
					credentials.getAccessKey(), bucketName, objectName, 
					"Unable to get the File handle to upload the file to S3");
		
		Upload upload;
		try {
			upload = transferManager.upload(request);
		} catch (Exception e) {
			throw new AmazonS3OperationException(
					credentials.getAccessKey(), bucketName, 
					objectName, 
					"Encountered Exception while invoking upload on multipart/single thread file, " +
					"see nested exceptions for more details", 
					e); 
		}
		//Wait till the upload completes, the call to putObject is synchronous
		try {
			if(logger.isInfoEnabled())
				logger.info("Waiting for Upload to complete");
			upload.waitForCompletion();
			if(logger.isInfoEnabled())
				logger.info("Upload completed");
		} catch (Exception e) {
			throw new AmazonS3OperationException(
					credentials.getAccessKey(), bucketName, 
					objectName, 
					"Encountered Exception while uploading the multipart/single thread file, " +
					"see nested exceptions for more details", 
					e);
		} 
		if(isTempFile) {
			//Delete the temp file
			if(logger.isDebugEnabled())
				logger.debug("Deleting temp file: " + file.getName());
			file.delete();
		}
		
		//Now since the object is present on S3, set the AccessControl list on it
		//Please note that it is not possible to set the object ACL with the
		//put object request, and hence both these operations cannot be atomic
		//it is possible the objects is uploaded and the ACl not set due to some
		//failure
		
		AmazonS3ObjectACL acl = s3Object.getObjectACL();
		AccessControlList objectACL = getAccessControlList(bucketName,key,acl);
		if(objectACL != null) {
			if(logger.isInfoEnabled())
				logger.info("Setting Access control list for key " + key);
			try {
				client.setObjectAcl(bucketName, key, objectACL);
			} catch (Exception e) {
				throw new AmazonS3OperationException(
						credentials.getAccessKey(), bucketName, 
						objectName, 
						"Encountered Exception while setting the Object ACL for key , " + key + 
						"see nested exceptions for more details", 
						e);
			}
			if(logger.isDebugEnabled())
				logger.debug("Successfully set the object ACL");
		} else {
			if(logger.isInfoEnabled())
				logger.info("No Object ACL found to be set");
		}
	}
	
	/**
	 * Gets the {@link AccessControlList} from the given {@link AmazonS3ObjectACL} 
	 * @param acl
	 * @return 
	 */
	private AccessControlList getAccessControlList(String bucketName,String key,AmazonS3ObjectACL acl) {
		AccessControlList accessControlList = null;
		if(acl != null) {
			if(!acl.getGrants().isEmpty()) {
				accessControlList = client.getObjectAcl(bucketName, key);
				for(ObjectGrant objGrant:acl.getGrants()) {
					Grantee grantee = objGrant.getGrantee();
					com.amazonaws.services.s3.model.Grantee awsGrantee;
					if(grantee.getGranteeType() == GranteeType.CANONICAL_GRANTEE_TYPE) {
						awsGrantee = new CanonicalGrantee(grantee.getIdentifier());
					} else if(grantee.getGranteeType() == GranteeType.EMAIL_GRANTEE_TYPE) {
						awsGrantee = new EmailAddressGrantee(grantee.getIdentifier());
					} else {
						awsGrantee = GroupGrantee.parseGroupGrantee(grantee.getIdentifier());
						if(awsGrantee == null) {
							logger.warn("Group grantee with identifier: \"" + grantee.getIdentifier() + "\" not found. skipping this grant");
							continue;
						}						
					}
					ObjectPermissions perm = objGrant.getPermission();
					Permission permission;
					if(perm == ObjectPermissions.READ) {
						permission = Permission.Read;
					} else if(perm == ObjectPermissions.READ_ACP) {
						permission = Permission.ReadAcp;
					} else
						permission = Permission.WriteAcp;
					
					accessControlList.grantPermission(awsGrantee, permission);
				}				
			}
		}
		return accessControlList;
	}
	
	/**
	 * Reads the stream provided and writes the file to the temp location
	 * @param in the Stream from which the data of the Object is to be read
	 * @param objectName the name of the object that would be used to upload the file
	 */
	private File getTempFile(InputStream in,String bucketName,String objectName) {
		InputStream inStream;
		if(!(in instanceof BufferedInputStream) && !(in instanceof ByteArrayInputStream)) {
			inStream = new BufferedInputStream(in);
		} else 
			inStream = in;
		String fileName;
		if(objectName.contains(PATH_SEPARATOR)) {
			String[] splits = objectName.split(PATH_SEPARATOR);
			fileName = splits[splits.length - 1];
		} else 
			fileName = objectName;
		String filePath = temporaryDirectory.getAbsoluteFile() + File.separator + fileName + temporaryFileSuffix;
		
		if(logger.isDebugEnabled())
			logger.debug("Temporary file path is " + filePath);
			
		//Write data to temporary file
		File tempFile = new File(filePath);
		try {
			FileOutputStream fos = new FileOutputStream(tempFile);
			byte[] bytes = new byte[1024];
			int read = 0;
			while(true) {
				read = inStream.read(bytes);
				if(read == -1)
					break;
				fos.write(bytes, 0, read);
			}
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {			
			throw new AmazonS3OperationException(credentials.getAccessKey(), 
					 bucketName, 
					 objectName, 
					 "Exception caught while writing the temporary file from input stream", e);
		} catch(IOException ioe) {
			throw new AmazonS3OperationException(credentials.getAccessKey(), 
					 bucketName, 
					 objectName, 
					 "Exception caught while reading from the provided input stream", ioe);
		}
		return tempFile;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.AmazonS3Operations#getObject(java.lang.String, java.lang.String, java.lang.String)
	 */
	
	public AmazonS3Object getObject(String bucketName, String folder,
			String objectName) {
		if(logger.isDebugEnabled())
			logger.debug("Getting from bucket " + bucketName + 
					", from folder " + folder + " the  object name " + objectName);
		GetObjectRequest request = new GetObjectRequest(bucketName, objectName);
		S3Object s3Object = client.getObject(request);
		AmazonS3Object object = new AmazonS3Object(s3Object.getObjectMetadata().getUserMetadata(), 
				s3Object.getObjectMetadata().getRawMetadata(), 
				s3Object.getObjectContent(), 
				null);
		return object;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.AmazonS3Operations#removeObject(java.lang.String, java.lang.String, java.lang.String)
	 */
	
	public boolean removeObject(String bucketName, String folder,
			String objectName) {
		return false;
	}


	/**
	 * Get the threshold value in bytes above which multi part upload will be used 
	 * @return
	 */
	public long getMultipartUploadThreshold() {
		return multipartUploadThreshold;
	}


	/**
	 * The threshold value in bytes above which the service will use multi part upload. 
	 * All the uploads below this value will be uploaded in a single thread
	 * Minimum value for the threshold is 5120 Bytes (5 KB). 
	 * It is recommended by Amazon to use Multi part uploads for all the uploads 
	 * above 100 MB
	 * If the value is set to a number above Integer.MAX_VALUE, the value will be
	 * set to  Integer.MAX_VALUE.
	 * 
	 * @param multipartUploadThreshold
	 */
	public void setMultipartUploadThreshold(long multipartUploadThreshold) {
		this.multipartUploadThreshold = multipartUploadThreshold;
	}


	/**
	 * Gets the thread pool executor that will be used to upload the object in multiparts
	 * concurrently 
	 * @return
	 */
	public ThreadPoolExecutor getThreadPoolExecutor() {
		return threadPoolExecutor;
	}


	/**
	 * Used only when we upload the data using multi part upload. The thread pool will be used
	 * to upload the data concurrently 
	 * @param threadPool
	 */
	public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
		if(threadPoolExecutor != null)
			this.threadPoolExecutor = threadPoolExecutor;
	}


	/**
	 * Gets the temporary directory
	 * @return
	 */
	public File getTemporaryDirectory() {
		return temporaryDirectory;
	}

	/**
	 * The temporary directory that will be used to write the files received over stream 
	 * @param temporaryDirectory
	 */
	public void setTemporaryDirectory(File temporaryDirectory) {
		Assert.notNull(temporaryDirectory, "Provided directory is null");
		this.temporaryDirectory = temporaryDirectory;
		if(!temporaryDirectory.exists())
			throw new IllegalArgumentException("The given temporary directory does not exist");
		
		if(!temporaryDirectory.isDirectory())
			throw new IllegalArgumentException("The given temporary directory path has to be a directory");
	}
	

	/**
	 * The temporary directory that will be used to write the files received over stream 
	 * @param temporaryDirectory
	 */
	public void setTemporaryDirectory(String temporaryDirectory) {
		if(!StringUtils.hasText(temporaryDirectory))
			throw new IllegalArgumentException("Provided temporary directory string is null or empty");
		File file = new File(temporaryDirectory);
		setTemporaryDirectory(file);		
	}

	
	/**
	 * Gets the temporary file suffix that is appended to the file while writing to 
	 * the temporary directory
	 * @return
	 */
	public String getTemporaryFileSuffix() {
		return temporaryFileSuffix;
	}


	/**
	 * Gets the temporary file suffix
	 * @param temporaryFileSuffix
	 */
	public void setTemporaryFileSuffix(String temporaryFileSuffix) {
		if(!StringUtils.hasText(temporaryFileSuffix))
			return;	//Ignore and use the default suffix
		
		if(!temporaryFileSuffix.startsWith("."))
			temporaryFileSuffix = "." + temporaryFileSuffix;
		
		this.temporaryFileSuffix = temporaryFileSuffix;
	}	
}

class PagninatedObjectsViewImpl implements PaginatedObjectsView {

	private List<S3ObjectSummary> objectSummary;
	private String nextMarker;	
	
	public PagninatedObjectsViewImpl(List<S3ObjectSummary> objectSummary,
			String nextMarker) {		
		this.objectSummary = objectSummary;
		this.nextMarker = nextMarker;
	}

	
	public List<S3ObjectSummary> getObjectSummary() {
		return objectSummary != null?objectSummary:new ArrayList<S3ObjectSummary>();
	}

	
	public boolean hasMoreResults() {		
		return nextMarker != null;
	}

	
	public String getNextMarker() {		
		return nextMarker;
	}
	
}
