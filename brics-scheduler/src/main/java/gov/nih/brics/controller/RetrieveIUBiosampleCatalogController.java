package gov.nih.brics.controller;

import gov.nih.brics.job.RetrieveIUBiosampleCatalogJob;

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
@RequestMapping("updateCatalog")
@Scope("request")
public class RetrieveIUBiosampleCatalogController {
	private static Logger log = Logger.getLogger(RetrieveIUBiosampleCatalogController.class);

	@Autowired
	ThreadPoolTaskScheduler scheduler;

	@Autowired
	RetrieveIUBiosampleCatalogJob catalogJob;

	@RequestMapping(method = RequestMethod.GET)
	public String getCreateForm(Model model) {
		return "updateCatalog/welcome";
	}

	@RequestMapping(method = RequestMethod.POST)
	@Scope("request")
	public String create(HttpSession session) {

		if (log.isInfoEnabled() == true) {
			log.info("triggering update catalog job");
		}

		scheduler.schedule(new Runnable() {
			public void run() {
				catalogJob.retrieveIUBiosampleCatalog();
			}
		}, new java.util.Date());

		return "updateCatalog/jobStarted";
	}
}
