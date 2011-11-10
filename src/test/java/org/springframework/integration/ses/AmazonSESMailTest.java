/*
 * Copyright 2002-2010 the original author or authors.
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
package org.springframework.integration.ses;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.aws.ses.AmazonSESMailHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Amol Nayak
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:ses-test.xml"})
public class AmazonSESMailTest {
	
	@Autowired
	@Qualifier("outboundAdapterChannel")
	private MessageChannel channel;
	
	@Autowired
	@Qualifier("props")
	private Properties props;
	
	
	@Test
	public void sendSimpleMail() {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(AmazonSESMailHeaders.TO_EMAIL_ID, props.getProperty("to.email.id"));		
		headers.put(AmazonSESMailHeaders.HTML_FORMAT, props.getProperty("html.format"));
		headers.put(AmazonSESMailHeaders.SUBJECT, props.getProperty("simplemail.subject"));
		headers.put(AmazonSESMailHeaders.FROM_EMAIL_ID, props.getProperty("from.email.id"));
		
		Message<String> msg = 
			MessageBuilder.withPayload(props.getProperty("simplemail.content"))
		.copyHeaders(headers)
		.build();
		channel.send(msg);
	}
	
	@Test
	public void sendRawMail() throws Exception {
		Session session = Session.getDefaultInstance(new Properties());		
		MimeMessageHelper helper = new MimeMessageHelper(new MimeMessage(session),true);
		helper.setTo(props.getProperty("to.email.id"));
		helper.setFrom(props.getProperty("from.email.id"));
		helper.setText(props.getProperty("rawmail.content"));
		File file = new File(props.getProperty("attachment.location"));		
		helper.addAttachment(file.getName(), file);
		helper.setSubject(props.getProperty("rawmail.subject"));
		Message<MimeMessage> message = MessageBuilder.withPayload(helper.getMimeMessage()).build();
		channel.send(message);		
	}
}
