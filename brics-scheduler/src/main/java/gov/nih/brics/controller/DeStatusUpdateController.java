package gov.nih.brics.controller;

import gov.nih.brics.job.DataElementStatusUpdateJob;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("deStatusUpdate")
@Scope("request")
public class DeStatusUpdateController {

	private static Logger log = Logger.getLogger(DeStatusUpdateController.class);

	@Autowired
	ThreadPoolTaskScheduler scheduler;

	@Autowired
	DataElementStatusUpdateJob statusUpdateJobs;

	@RequestMapping(method = RequestMethod.GET)
	public String getCreateForm(Model model) {
		return "deStatusUpdate/welcome";
	}

	@RequestMapping(method = RequestMethod.POST)
	@Scope("request")
	public String create(HttpSession session) {

		if (log.isInfoEnabled() == true) {
			log.info("triggering rdf job");
		}

		scheduler.schedule(new Runnable() {
			public void run() {
				statusUpdateJobs.updateDataElementStatus();
			}
		}, new java.util.Date());

		return "deStatusUpdate/jobStarted";
	}
}
