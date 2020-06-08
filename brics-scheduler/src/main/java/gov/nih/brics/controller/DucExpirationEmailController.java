package gov.nih.brics.controller;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nih.brics.job.DucExpirationEmailJob;

@Controller
@RequestMapping("ducExpirationEmail")
@Scope("request")
public class DucExpirationEmailController {

	@Autowired
	ThreadPoolTaskScheduler scheduler;

	@Autowired
	private DucExpirationEmailJob ducExpirationEmailJob;

	@RequestMapping(method = RequestMethod.GET)
	public String getCreateForm(Model model) {
		return "ducExpiratonEmail/welcome";
	}

	@RequestMapping(method = RequestMethod.POST)
	@Scope("request")
	public String create(HttpSession session) {
		scheduler.schedule(new Runnable() {
			public void run() {
				ducExpirationEmailJob.doJob();
			}
		}, new Date());

		return "ducExpirationEmail/jobStarted";
	}
}
