package gov.nih.brics.controller;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nih.brics.job.UpdateAccountPrivilegesExpirationJob;



@Controller
@RequestMapping("updateAccountPrivilegesExpirationJob")
@Scope("request")
public class UpdateAccountPrivilegesExpirationController {

	private static Logger logger = Logger.getLogger(UpdateAccountPrivilegesExpirationController.class);
	
	@Autowired
	private UpdateAccountPrivilegesExpirationJob updateAccountPrivilegesExpirationSoonJob;
	
	
	@RequestMapping(method = RequestMethod.GET)
	public String getCreateForm(Model model) {
		return "updateAccountPrivilegesExpirationJob/welcome";
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@Scope("request")
	public String create(HttpSession session) {

		if (logger.isInfoEnabled() == true) {
			logger.info("triggering updateAccountPrivilegesExpirationJob");
		}

		updateAccountPrivilegesExpirationSoonJob.doJob();

		return "updateAccountPrivilegesExpirationJob/jobStarted";
	}
}
