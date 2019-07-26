
package gov.nih.tbi.scheduler;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.service.util.MailEngine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * This class contains the implementation of the various jobs that are to be run at a pre-defined schedule. The jobs are
 * actually scheduled in the {@link JobScheduler}
 * 
 * @author vivek pachauri
 * 
 */
public class Jobs
{

    private static Logger log = Logger.getLogger(gov.nih.tbi.scheduler.Jobs.class);

    // @Autowired
    AccountDao accountDao;

    public AccountDao getAccountDao()
    {

        return accountDao;
    }

    public void setAccountDao(AccountDao accountDao)
    {

        this.accountDao = accountDao;
    }

    // @Autowired
    private MailEngine mailEngine;

    public MailEngine getMailEngine()
    {

        return mailEngine;
    }

    public void setMailEngine(MailEngine mailEngine)
    {

        this.mailEngine = mailEngine;
    }

    // @Autowired
    MessageSource messageSource;

    // @Autowired
    protected ModulesConstants modulesConstants;

    public ModulesConstants getModulesConstants()
    {

        return modulesConstants;
    }

    public void setModulesConstants(ModulesConstants modulesConstants)
    {

        this.modulesConstants = modulesConstants;
    }

    public MessageSource getMessageSource()
    {

        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource)
    {

        this.messageSource = messageSource;
    }

    /**
     * This methods checks the password expiration dates of all the users in the system and for all those user whose
     * passwords are about to expire within the next 14 days, it sends out an email to them notifying them of the number
     * of days left for their password to expire
     */
    @Async
    public void passwordExpirationMailJob()
    {

        if (log.isInfoEnabled() == true)
        {
            log.info("**********email job begin**********");
        }
        /*
         * get the list of all the users in the system
         */
        List<Account> accounts = accountDao.getAll();
        if (accounts == null || (accounts.isEmpty() == true))
        {
            // log message
            if (log.isEnabledFor(Level.ERROR) == true)
            {
                log.error("No user accounts found in the passwordExpirationMailJob() periodic task using the AccountDao object");
            }
            return;
        }

        /*
         * generate a list of all the users whose passwords are about to expire
         * in the next 14 days or less
         */
        List<Account> accountsExpiringIn14Days = new ArrayList<Account>();
        long oneSecond = 1000; // milli seconds
        long oneMinute = 60 * oneSecond;
        long oneHour = 60 * oneMinute;
        long oneDay = 24 * oneHour;
        long numberOfMillisFor14Days = oneDay * 14;
        // get the current date
        Date currentDate = new Date();

        for (Account account : accounts)
        {
            // get the date of password expiration
            Date passwordExpirationDate = account.getPasswordExpirationDate();
            
            // some of the never expiring account contains null password expiration date so ignore those accounts
            //if the account is not active, do not send the user a password expiration email BRIC-2563
            if (passwordExpirationDate == null || !account.getIsActive()){
                continue;
            }

            // checking if current date is less than 14 days before password
            // expiration date
            long passwordExpirationTime = passwordExpirationDate.getTime();
            long currentTime = currentDate.getTime();
            long differenceTime = passwordExpirationTime - currentTime;
            if ((passwordExpirationTime > currentTime) && (differenceTime <= numberOfMillisFor14Days))
            {
                // if the difference between the password expiration date and current date is less than or equal to
                // number of milli secs in 14 days then add this account to the list of
                // accounts expiring in 14 days
                if (log.isDebugEnabled() == true)
                {
                    log.debug("found an account whose password is expiring in the next 14 days or less: "
                            + account.toString());
                }
                if (log.isDebugEnabled() == true)
                {
                    log.debug("adding account: " + account + " username: " + account.getUserName() + " expiring on: "
                            + account.getPasswordExpirationDate()
                            + " to the list of accounts whose passwords are expiring in the next 14 days");
                }
                accountsExpiringIn14Days.add(account);
            }
        }

        /*
         * get a handle to mail engine
         */
        // auto injected

        /*
         * for each user in the selected user list
         */
        for (Account account : accountsExpiringIn14Days)
        {
            /*
             * compile a list of all the variables needed to populate the mail
             * message text
             */
            // String userName;
            String applicationName;
            // String passwordExpirationDate;
            String numberOfDaysToExpiration;
            String numberOfHoursToExpiration;
            String passwordRenewURL;
            // String nihPasswordHintURL;
            String supportPhoneNumber;
            String supportEmailAddress;
            String mailTitle;
            String mailBody;

            // initialize these variables
            // userName = account.getUserName();
            applicationName = modulesConstants.getModulesOrgName();
            // passwordExpirationDate =
            // account.getPasswordExpirationDate().toString();
            long difference = account.getPasswordExpirationDate().getTime() - currentDate.getTime();
            long daysRemaining = (difference / oneDay);
            long hoursRemaining = (difference - (daysRemaining * oneDay)) / oneHour;
            numberOfDaysToExpiration = String.valueOf(daysRemaining);
            numberOfHoursToExpiration = String.valueOf(hoursRemaining);
            passwordRenewURL = messageSource.getMessage(ModulesConstants.RENEW_PASSWORD_URL_PROPERTY,
                    new Object[] { modulesConstants.getModulesAccountURL() }, modulesConstants.getModulesAccountURL(),
                    null);
            // nihPasswordHintURL =
            // messageSource.getMessage(ModulesConstants.NIH_PASSWORD_HINT_URL_PROPERTY,
            // null, null);
            supportPhoneNumber = modulesConstants.getModulesOrgPhone();
            supportEmailAddress = modulesConstants.getModulesOrgEmail();

            mailTitle = messageSource.getMessage(ModulesConstants.RENEW_PASSWORD_SUBJECT_PROPERTY,
                    new Object[] { applicationName }, "Application Name", null);
            mailBody = messageSource.getMessage(ModulesConstants.RENEW_PASSWORD_MESSAGE_BODY_PROPERTY, new Object[] {
                    applicationName, numberOfDaysToExpiration, numberOfHoursToExpiration, passwordRenewURL,
                    supportEmailAddress, supportPhoneNumber, account.getUser().getFirstName() }, "", null);
            /*
             * send a mail to the user
             */
            if (log.isDebugEnabled() == true)
            {
                log.debug("sending password renew email to the address: " + account.getUser().getEmail()
                        + " of account: " + account);
                log.debug("mail title: " + mailTitle);
                log.debug("mail body:\n" + mailBody);
            }
            try
            {
                if (log.isDebugEnabled() == true)
                {
                    log.debug("sending mail to user: " + account.getUserName());
                }
                this.mailEngine.sendMail(mailTitle, mailBody, null, account.getUser().getEmail());
            }
            catch (MessagingException e)
            {
                log.error("Exception occurred while trying to send mail in scheduled job", e);
            }
        }

        /* send out email to my own sapient address */

        if (log.isInfoEnabled() == true)
        {
            log.info("**********email job end**********");
        }
    }
}
