package gov.nih.brics.job;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.commons.service.AccountManager;
/**
 * Class to delete request pending account older than two business days.
 * 
 * @author khanaly
 *
 */
@Component
@Scope("singleton")
public class DeleteLoginPendingApprovedAccountJob {

	private static Logger log = Logger
			.getLogger(DeleteLoginPendingApprovedAccountJob.class);
	@Autowired
	private AccountManager accountManager;

	public void deleteLoginPendingAccountOlderThanTwoBusinessDays() {
		log.info("*****************************************");
		log.info("STARTING deleting account.");
		log.info("*****************************************");

		accountManager.deleteLoginPendingApprovedAccount();
		log.info("*****************************************");
		log.info("ENDING deleting account.");
		log.info("*****************************************");

	}

}
