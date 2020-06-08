
package gov.nih.brics.controller;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nih.brics.job.AccountExpirationEmailJob;
import gov.nih.brics.job.DucExpirationEmailJob;
import gov.nih.brics.job.PasswordExpirationEmailJob;

@Controller
@RequestMapping("emailJob")
@Scope("request")
public class EmailJobController
{
    private static Logger log = Logger.getLogger(EmailJobController.class);
    
    @Autowired
    ThreadPoolTaskScheduler scheduler;
    
    @Autowired
    private AccountExpirationEmailJob accountExpirationEmailJob;
    
    @Autowired
    private PasswordExpirationEmailJob passwordExpirationEmailJob;
    
    @Autowired
    private DucExpirationEmailJob ducExpiratonEmailJob;

    @RequestMapping(method = RequestMethod.GET)
    public String getCreateForm(Model model)
    {
        return "emailJob/welcome";
    }
    
    @RequestMapping(method = RequestMethod.POST)
    @Scope("request")
    public String create(HttpSession session) {

            if (log.isInfoEnabled() == true) {
                    log.info("triggering email job");
            }
            
            accountExpirationEmailJob.doJob();
            passwordExpirationEmailJob.doJob();
            ducExpiratonEmailJob.doJob();

            /* session.setAttribute("future", val); */
            return "emailJob/jobStarted";
    }
}
