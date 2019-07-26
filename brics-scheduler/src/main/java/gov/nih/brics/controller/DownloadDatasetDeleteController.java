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

import gov.nih.brics.job.DownloadDatasetDeleteJob;

@Controller
@RequestMapping("downloadDatasetDelete")
@Scope("request")
public class DownloadDatasetDeleteController {
	
	@Autowired
	ThreadPoolTaskScheduler scheduler;

	@Autowired
	private DownloadDatasetDeleteJob downloadDatasetDeleteJob;
	
	  @RequestMapping(method = RequestMethod.GET)
	    public String getCreateForm(Model model) {
	        return "downloadDatasetDelete/welcome";
	    }
	
	@RequestMapping(method = RequestMethod.POST)
	@Scope("request")
	public String create(HttpSession session) {
		scheduler.schedule(new Runnable() {
			public void run() {
				downloadDatasetDeleteJob.doJob();
			}
		}, new Date());

		return "downloadDatasetDelete/jobStarted";
	}

}
