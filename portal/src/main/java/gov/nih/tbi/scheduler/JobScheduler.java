
package gov.nih.tbi.scheduler;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This class will have all the methods that are to be run at periodic schedule. The pattern is to implement the job
 * code in the Jobs class and in every scheduled method in this class, call the particular method in the Jobs class
 * which implements the code to be run.
 * 
 * @author vivek pachauri
 * 
 */

public class JobScheduler implements ApplicationContextAware
{

    private static Logger log = Logger.getLogger(JobScheduler.class);

//    @Autowired
    private Jobs jobs;

    public Jobs getJobs()
    {

        return jobs;
    }

    public void setJobs(Jobs jobs)
    {

        if (log.isDebugEnabled() == true)
        {
            log.debug("########## setJobs() called by Thread: " + Thread.currentThread().getName() + "##########");
        }
        this.jobs = jobs;
    }

    /**
     * This method will run the email job every day at midnight
     */
    @Scheduled(cron = "${renewPassword.cronExpression}")
    public void scheduleEmailJob()
    {

        if (log.isDebugEnabled() == true)
        {
            log.debug("executing scheduled email job");
        }
        jobs.passwordExpirationMailJob();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {

        // TODO Auto-generated method stub
        if ( log.isInfoEnabled() == true )
        {
            log.info("#############Current Context Info#############");
            log.info("#### Context display name: " + applicationContext.getDisplayName());
            log.info("#### ID: " + applicationContext.getId());
            if ( applicationContext.getParent() != null )
            {
              log.info("#### Parent: "  + applicationContext.getParent().getDisplayName());
            }

            log.info("#### Startup date: " + new Date(applicationContext.getStartupDate()));
        }
    }

}
