package gov.nih.brics.controller;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nih.brics.job.CheckIUStatusAndRetrievManifestJob;

@Controller
@RequestMapping("checkIuStatus")
@Scope("request")
public class CheckIuStatusController {
	private static Logger log = Logger.getLogger(CheckIuStatusController.class);

	@Autowired
	ThreadPoolTaskScheduler scheduler;

	@Autowired
	CheckIUStatusAndRetrievManifestJob checkIuStatusJob;

	@RequestMapping(method = RequestMethod.GET)
	public String getCreateForm(Model model) {
		return "checkIuStatus/welcome";
	}

	@RequestMapping(method = RequestMethod.POST)
	@Scope("request")
	public String create(HttpSession session) {

		if (log.isInfoEnabled() == true) {
			log.info("triggering check IU status job");
		}

		scheduler.schedule(new Runnable() {
			public void run() {
				checkIuStatusJob.checkIUStatusAndRetrieveManifest();
			}
		}, new Date());

		return "checkIuStatus/jobStarted";
	}
}
