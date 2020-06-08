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

import gov.nih.brics.job.DataImportJob;
import gov.nih.brics.job.DatasetStatusEmailJob;
import gov.nih.brics.job.DownloadDatasetDeleteJob;

@Controller
@RequestMapping("pubj")
@Scope("request")
public class DataImportController {
	
	@Autowired
	ThreadPoolTaskScheduler scheduler;

	@Autowired
	private DataImportJob dataImportJob;
	
	  @RequestMapping(method = RequestMethod.GET)
	    public String getCreateForm(Model model) {
	        return "pubj/welcome";
	    }
	
	@RequestMapping(method = RequestMethod.POST)
	@Scope("request")
	public String create(HttpSession session) {
		scheduler.schedule(new Runnable() {
			public void run() {
				dataImportJob.doJob();
			}
		}, new Date());

		return "pubj/jobStarted";
	}

}
