
package gov.nih.brics.job;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.HibernateManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class PasswordExpirationEmailJob extends EmailJobs
{

    @Autowired
    private AccountManager accountManager;
    
    @Autowired
	private HibernateManager hibernateManager;

    private static Logger log = Logger.getLogger(PasswordExpirationEmailJob.class);

    // list of the days away from expiration date the emails should be sent out.
    // Don't try to modify this list on runtime
    private static List<Integer> daysFromExpirationToEmail = Collections.unmodifiableList(Arrays.asList(14, 7, 6, 5, 4,
            3, 2, 1));

    /**
     * This methods checks the password expiration dates of all the users in the system and for all those user whose
     * passwords are about to expire within the next 14 days, it sends out an email to them notifying them of the number
     * of days left for their password to expire
     */
    @Async
    public void doJob()
    {
    	hibernateManager.clearMetaHibernateCache();
    	
        if (log.isInfoEnabled() == true)
        {
            log.info("**********password expiration email job begin**********");
        }
        /*
         * get the list of all the users in the system
         */
        if (log.isDebugEnabled() == true)
        {
            log.debug("retrieving list of users");
        }
        List<Account> accounts = accountManager.getActiveAccounts();
        if (log.isDebugEnabled() == true)
        {
            log.debug("finished retrieving list of users");
        }
        if (accounts == null || (accounts.isEmpty() == true))
        {
            // log message

            log.error("No user accounts found in the passwordExpirationMailJob() periodic task using the AccountDao object");
            return;
        }

        // if the days from expiration date for the account is within the list
        // of days to email notification, then add it to the set
        Set<Account> accountsToEmail = new HashSet<Account>();
        for (Account account : accounts)
        {
            int daysFromExpiration = getDaysAwayFromExpiration(account);

            if (daysFromExpirationToEmail.contains(daysFromExpiration))
            {
                accountsToEmail.add(account);
            }
        }

        /*
         * for each user in the selected user list
         */
        for (Account account : accountsToEmail)
        {
            log.info("sending email to " + account.getUser().getEmail());
            sendPasswordExpirationNotification(account);
        }

        if (log.isInfoEnabled() == true)
        {
            log.info("**********password expiration email job end**********");
        }
    }

    /**
     * Using the specified account, create and send the message to the account's email
     * 
     * @param account
     */
    private void sendPasswordExpirationNotification(Account account)
    {

        Date currentDate = new Date();

        /*
         * compile a list of all the variables needed to populate the mail
         * message text
         */
        // String userName;
        String applicationName = modulesConstants.getModulesOrgName();
        long difference = account.getPasswordExpirationDate().getTime() - currentDate.getTime();
        long daysRemaining = (difference / BRICSTimeDateUtil.ONE_DAY);
        long hoursRemaining = (difference - (daysRemaining * BRICSTimeDateUtil.ONE_DAY)) / BRICSTimeDateUtil.ONE_HOUR;
        String numberOfDaysToExpiration = String.valueOf(daysRemaining);
        String numberOfHoursToExpiration = String.valueOf(hoursRemaining);
        String passwordRenewURL = messageSource
                .getMessage(ModulesConstants.RENEW_PASSWORD_URL_PROPERTY,
                        new Object[] { modulesConstants.getModulesAccountURL() },
                        modulesConstants.getModulesAccountURL(), null);

        String supportPhoneNumber = modulesConstants.getModulesOrgPhone();
        String supportEmailAddress = modulesConstants.getModulesOrgEmail();

        String mailTitle = messageSource.getMessage(ModulesConstants.RENEW_PASSWORD_SUBJECT_PROPERTY,
                new Object[] { applicationName }, "Application Name", null);
        String mailBody = messageSource.getMessage(ModulesConstants.RENEW_PASSWORD_MESSAGE_BODY_PROPERTY, new Object[] {
                applicationName, numberOfDaysToExpiration, numberOfHoursToExpiration, passwordRenewURL,
                supportEmailAddress, supportPhoneNumber, account.getUser().getFirstName() }, "", null);
        /*
         * send a mail to the user
         */
        if (log.isDebugEnabled() == true)
        {
            log.debug("sending password renew email to the address: " + account.getUser().getEmail() + " of account: "
                    + account);
            log.debug("mail title: " + mailTitle);
            log.debug("mail body:\n" + mailBody);
        }
        try
        {
            if (log.isDebugEnabled() == true)
            {
                log.debug("sending mail to user: " + account.getUserName());
            }

            this.sendEmail(mailTitle, mailBody, account.getUser().getEmail());
            System.out.println("******" + account.getUser().getEmail());
        }
        catch (MessagingException e)
        {
            log.error("Exception occurred while trying to send password expiration email in scheduled job", e);
        }
    }

    /**
     * Returns the number of days from expiration the account is
     * 
     * @param account
     * @return
     */
    private int getDaysAwayFromExpiration(Account account)
    {

        Date passwordExpirationDate = account.getPasswordExpirationDate();

        if (passwordExpirationDate == null)
        {
            return Integer.MAX_VALUE;
        }

        return getDaysAwayFromToday(passwordExpirationDate.getTime());
    }
}
