package gov.nih.brics.controller;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nih.brics.job.StuckDatasetStatusChangeJob;

@Controller
@RequestMapping("stuckDatasetStatusChangeJob")
@Scope("request")
public class StuckDatasetStatusChangeController {

	private static Logger logger = Logger.getLogger(StuckDatasetStatusChangeController.class);

	@Autowired
	private StuckDatasetStatusChangeJob stuckDatasetStatusChangeJob;

	@RequestMapping(method = RequestMethod.GET)
	public String getCreateForm(Model model) {
		return "stuckDatasetStatusChangeJob/welcome";
	}

	@RequestMapping(method = RequestMethod.POST)
	@Scope("request")
	public String create(HttpSession session) {

		if (logger.isInfoEnabled() == true) {
			logger.info("triggering stuckDatasetStatusChange job");
		}

		stuckDatasetStatusChangeJob.stuckDatasetStatusChange();

		return "stuckDatasetStatusChangeJob/jobStarted";
	}

}
