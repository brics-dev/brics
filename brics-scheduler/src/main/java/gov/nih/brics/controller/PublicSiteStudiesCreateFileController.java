package gov.nih.brics.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nih.brics.job.PublicSiteStudiesCreateFileJob;

@Controller
@RequestMapping("publicSiteStudiesMetricsCreateFile")
@Scope("request")
public class PublicSiteStudiesCreateFileController {

	@Autowired
	ThreadPoolTaskScheduler scheduler;
	
	@Autowired
	PublicSiteStudiesCreateFileJob publicSiteStudiesCreateFileJob;
	
	@RequestMapping(method = RequestMethod.GET)
	public String getCreateForm(Model model) {
		return "publicSiteStudiesCreateFile/welcome";
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@Scope("request")
	public String create(HttpSession session) {
		scheduler.schedule(new Runnable() {
			public void run() {
				publicSiteStudiesCreateFileJob.storeJson();
			}
		}, new java.util.Date());

		return "publicSiteStudiesCreateFile/jobStarted";
	}
}
