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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Amol Nayak
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:s3-outbound-test.xml"})
public class AWSS3OutboundAdapterTest {

	@Autowired
	@Qualifier("s3OutboundChannel")
	private MessageChannel channel;
	
	@Autowired
	@Qualifier("props")
	private Properties props;
	
	@Test
	public void sendStringWithoutFileName() {
		Message<String> message = MessageBuilder.withPayload("Some Text content").build();
		channel.send(message);
	}
	
	@Test
	public void sendStringWithFileName() {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("file_name", props.getProperty("outbound.adapter.upload.file"));
		Map<String, Collection<String>> fileACL = getFileACL();
		headers.put("object_acls",fileACL);
		Message<String> message = 
			MessageBuilder
			.withPayload("This is the content of the file FileUploadedFromS3OutboundAdapter.txt")
			.copyHeaders(headers)
			.build();
		channel.send(message);
	}
	
	private Map<String, Collection<String>> getFileACL() {
		Map<String, Collection<String>> acl = new HashMap<String, Collection<String>>();
		acl.put(props.getProperty("grantee.mail"), Collections.singletonList("write acp"));
		acl.put(props.getProperty("canonical.mail"), Arrays.asList("read","read acp"));
		acl.put(props.getProperty("authenticated.users.group"), Collections.singletonList("read acp"));
		return acl;
	}
	
	@Test
	public void sendFileFromFileSystem() {
		File file = new File(props.getProperty("local.file.path"));
		Message<File> message = MessageBuilder
		.withPayload(file)
		.build();
		channel.send(message);
	}
}
