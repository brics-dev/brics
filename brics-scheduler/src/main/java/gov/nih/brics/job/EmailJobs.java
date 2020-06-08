
package gov.nih.brics.job;

import java.util.Date;

import javax.activation.DataSource;
import javax.mail.MessagingException;

import gov.nih.brics.JobScheduler;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.service.util.MailEngine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * This class contains the implementation of the various jobs that are to be run at a pre-defined schedule. The jobs are
 * actually scheduled in the {@link JobScheduler}
 * 
 * @author vivek pachauri
 * 
 */
@Component
public abstract class EmailJobs
{

    @Autowired
    private MailEngine mailEngine;

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    protected ModulesConstants modulesConstants;

    public abstract void doJob();

    protected void sendEmail(String subject, String body, String emailReceiver) throws MessagingException
    {

        this.mailEngine.sendMail(subject, body, null, emailReceiver);
    }
    
    protected void sendEmail(String subject, String body, String emailReceiver, String cc) throws MessagingException
    {

        this.mailEngine.sendMail(subject, body, null, new String[]{emailReceiver}, new String[]{cc});
    }
    
    protected void sendEmailWithAttachment(String subject, String body, String emailReceiver, DataSource source, String fileName) throws MessagingException
    {

        this.mailEngine.sendMailWithAttachment(subject, body, source, fileName, null, emailReceiver);
    }
    
    
    /**
     * Given a millisecond from epoch, returns the number of days from today.  If it's in the future, return zero.
     * @param expirationTimeMillisecond
     * @return
     */
    protected int getDaysAwayFromToday(long expirationTimeMillisecond)
    {

        // get the current date
        Date currentDate = new Date();

        double currentTime = currentDate.getTime();
        double differenceTime = expirationTimeMillisecond - currentTime;

        // convert from milliseconds to days. Use ceiling?
        int daysAway = (int) Math.ceil(differenceTime / BRICSTimeDateUtil.ONE_DAY);

        return daysAway > 0 ? daysAway : 0; //if days away is greater than 0, return.  Otherwise, return 0.
    }
}
