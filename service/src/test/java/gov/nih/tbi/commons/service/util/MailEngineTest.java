
package gov.nih.tbi.commons.service.util;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration({"/context.xml"})
public class MailEngineTest extends AbstractTestNGSpringContextTests {

	@Autowired
	private MailEngine mailEngine;

    @Test(groups = { "mail" })
	public void testSendMail() throws MessagingException {
        mailEngine.sendMail("testSendMail", "test my sendmail method", null, "lordxuqra@gmail.com");
    }
}
