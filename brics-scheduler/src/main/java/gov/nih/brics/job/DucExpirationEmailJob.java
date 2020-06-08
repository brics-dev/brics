package gov.nih.brics.job;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.repository.model.hibernate.UserFile;

@Component
@Scope("singleton")
public class DucExpirationEmailJob extends EmailJobs {

	private static final Logger log = Logger.getLogger(DucExpirationEmailJob.class);

	@Autowired
	private AccountManager accountManager;

	@Autowired
	private RepositoryManager repositoryManager;

	// list of the days away from expiration date the email should be sent out.
    private static List<Integer> daysFromExpirationToEmail = Collections.unmodifiableList(Arrays.asList(30, 14));
	
	private static final String DUC_FILE_DESCRIPTION = "Genomics DUC";

	@Async
	public void doJob() {

		if (log.isInfoEnabled() == true) {
			log.info("**********DUC expiration email job begin**********");
		}
		if (log.isDebugEnabled() == true) {
			log.debug("retrieving list of users");
		}

		List<Account> accounts = accountManager.getActiveAccounts();
		if (log.isDebugEnabled() == true) {
			log.debug("finished retrieving list of users");
		}

		if (accounts == null || (accounts.isEmpty() == true)) {
			log.error("No user accounts found in the DucExpiratonEmailJob() periodic task");
			return;
		}

		// For every active account check if the days from DUC expiration date is within 
		// the list of days to email notification
		for (Account account : accounts) {
			Date ducExpirationDate = getDucExpirationDate(account);
			
			if (ducExpirationDate != null) {
				int daysAway = getDaysAwayFromToday(ducExpirationDate);
				
				if (daysFromExpirationToEmail.contains(daysAway)) {
					log.info("sending email to " + account.getUser().getEmail());
					sendDucExpirationNotification(account, daysAway);
				}
				
				// If DUC has expired 
				if (daysAway == 0) {
					sendDucExpiriedToOps(account);
				}
			}
		}

		if (log.isInfoEnabled() == true) {
			log.info("**********DUC expiration email job end**********");
		}
	}

	/**
	 * Using the specified account, create and send the message to the account's
	 * email
	 * 
	 * @param account
	 */
	private void sendDucExpirationNotification(Account account, int daysAway) {

        String supportEmailAddress = modulesConstants.getModulesOrgEmail();
        String userFirstName = account.getUser().getFirstName();
        
		String applicationName = modulesConstants.getModulesOrgName();
		String mailTitle = messageSource.getMessage(ServiceConstants.DUC_EXPIRATION_SUBJECT_PROPERTY,
				new Object[] { applicationName }, "Application Name", null);
		String mailBody = messageSource.getMessage(ServiceConstants.DUC_EXPIRATION_MESSAGE_BODY_PROPERTY,
				new Object[]{userFirstName, daysAway, supportEmailAddress}, "", null);

		if (log.isDebugEnabled() == true) {
			log.debug("sending DUC expiration email to the address: " + account.getUser().getEmail());
			log.debug("mail title: " + mailTitle);
			log.debug("mail body:\n" + mailBody);
		}
		
		try {
			if (log.isDebugEnabled() == true) {
				log.debug("sending mail to user: " + account.getUserName());
			}

			this.sendEmail(mailTitle, mailBody, account.getUser().getEmail(), supportEmailAddress);
			
		} catch (MessagingException e) {
			log.error("Exception occurred while trying to send DUC expiration email in scheduled job", e);
		}
	}
	
	
	private void sendDucExpiriedToOps(Account account) {

        String supportEmailAddress = modulesConstants.getModulesOrgEmail();
        String userFirstName = account.getUser().getFirstName();
        String userLastName = account.getUser().getLastName();
        
		String applicationName = modulesConstants.getModulesOrgName();
		String mailTitle = messageSource.getMessage(ServiceConstants.DUC_EXPIRED_OPS_SUBJECT_PROPERTY,
				new Object[] { applicationName }, "Application Name", null);
		String mailBody = messageSource.getMessage(ServiceConstants.DUC_EXPIRED_OPS_MESSAGE_BODY_PROPERTY,
				new Object[]{userLastName, userFirstName}, "", null);

		if (log.isDebugEnabled() == true) {
			log.debug("sending DUC expired email to OPS: " + supportEmailAddress);
			log.debug("mail title: " + mailTitle);
			log.debug("mail body:\n" + mailBody);
		}
		
		try {
			this.sendEmail(mailTitle, mailBody, supportEmailAddress);
			
		} catch (MessagingException e) {
			log.error("Exception occurred while trying to send DUC expired OPS email in scheduled job", e);
		}
	}


	private Date getDucExpirationDate(Account account) {

		List<UserFile> adminFiles = repositoryManager.getAdminFiles(account.getUserId());
		if (adminFiles != null && !adminFiles.isEmpty()) {
			for (UserFile adminFile : adminFiles) {
				if (DUC_FILE_DESCRIPTION.equals(adminFile.getDescription()) && adminFile.getUploadedDate() != null) {
					// DUC will expire in 1 year after its uploaded date
					Date ducExpirationDate = DateUtils.addYears(adminFile.getUploadedDate(), 1);
					return ducExpirationDate;
				}
			}
		}

		return null;
	}

	
    // Given a date, returns the number of days from today.  
    private int getDaysAwayFromToday(Date date) {

        Date currentDate = new Date();
        double currentTime = currentDate.getTime();
        double differenceTime = date.getTime() - currentTime;

        int daysAway = (int) Math.ceil(differenceTime / BRICSTimeDateUtil.ONE_DAY);
        return daysAway;
    }
}
