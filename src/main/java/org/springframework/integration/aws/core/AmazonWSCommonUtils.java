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
package org.springframework.integration.aws.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The common utilities methods
 * @author Amol Nayak
 *
 */
public class AmazonWSCommonUtils {

	private static final Log logger = LogFactory.getLog(AmazonWSCommonUtils.class);
	
	/**
	 * Generates the MD5 hash of the file provided
	 * @param file
	 * @return
	 */
	public static byte[] getContentsMD5AsBytes(File file) {		
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file),32768);
			byte[] bytesRead = new byte[8192];
			for (int read = 0;(read = bin.read(bytesRead)) != -1;) {
				digest.update(bytesRead, 0, read);
			}
			return digest.digest();
			
		} catch (NoSuchAlgorithmException e) {
			logger.error("Caught Exception while generating a MessageDigest instance", e);			
		} catch (FileNotFoundException e) {
			logger.error("File " + file.getName() + " not found",e);
		} catch(IOException e) {
			logger.error("IO Exception  occurred while reading file " + file.getName(), e);
		}
		return null;
	}
	
	/**
	 * Compute the MD5 hash of the provided String
	 * @param the String whose MD5 sun is to be computed
	 * @return
	 */
	public static byte[] getContentsMD5AsBytes(String contents) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			return digest.digest(contents.getBytes());
		} catch (NoSuchAlgorithmException e) {			
			logger.error("Unable to digest the input String",e);
		}
		return null;
	}
	
	/**
	 * Encodes the given raw bytes into hex
	 * @param rawBytes
	 * @return
	 */
	public static String encodeHex(byte[] rawBytes) throws UnsupportedEncodingException {
		return new String(Base64.encodeBase64(rawBytes),"UTF-8");
	}	
}
