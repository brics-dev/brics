package gov.nih.brics.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nih.brics.job.AdvancedViewCreateFileJob;

@Controller
@RequestMapping("advancedViewCreateFile")
@Scope("request")
public class AdvancedViewCreateFileController {
	
	@Autowired
	private AdvancedViewCreateFileJob advancedViewCreateFileJob;
	
	@Autowired
	ThreadPoolTaskScheduler scheduler;
	
	@RequestMapping(method = RequestMethod.GET)
	public String getCreateForm(Model model) {
		return "advancedViewCreateFile/welcome";
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@Scope("request")
	public String create(HttpSession session) {
		scheduler.schedule(new Runnable() {
			public void run() {
				advancedViewCreateFileJob.storeJson();
			}
		}, new java.util.Date());

		return "advancedViewCreateFile/jobStarted";
	}
}