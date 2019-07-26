
package gov.nih.tbi.commons.service.util;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.DataLoaderManager;
import gov.nih.tbi.repository.service.exception.DataLoaderException;

@ContextConfiguration({"/context.xml"})
public class DataLoadTest extends AbstractTestNGSpringContextTests {

	static Logger logger = Logger.getLogger(DataLoadTest.class);

	@Autowired
	private DataLoaderManager dataLoaderManager;

	@Autowired
	private AccountManager accountManager;

	@Test(groups = {"dataLoad"})
	public void testDataLoad() throws MessagingException, DataLoaderException {

		System.out.println("Way to go!");

		Account account = accountManager.getAccountByUserName("administrator");

		Assert.assertNotNull(account);

		long start = System.currentTimeMillis();
		// repoMan.initializeDatasetFile(account, 273L, null); // 10x1
		// repoMan.initializeDatasetFile(account, 275L, null); // 10x2
		// repoMan.initializeDatasetFile(account, 276L, null); // 10x10
		// repoMan.initializeDatasetFile(account, 277L, null); // 10x20
		// repoMan.initializeDatasetFile(account, 274L, null); // 10x100
		// repoMan.initializeDatasetFile(account, 332L, null); // 10x1000



		// repoMan.initializeDatasetFile(account, 294L, null); // 100x1
		// repoMan.initializeDatasetFile(account, 294L, null); // 100x2
		// repoMan.initializeDatasetFile(account, 294L, null); // 100x10
		// repoMan.initializeDatasetFile(account, 294L, null); // 100x20
		dataLoaderManager.initializeDatasetFile(account, 294L, null);		// 100x100



		System.out.println("\n:  " + (System.currentTimeMillis() - start) / 1000L);

		// mailEngine.sendMail("testSendMail", "test my sendmail method", null, "lordxuqra@gmail.com");
	}
}
