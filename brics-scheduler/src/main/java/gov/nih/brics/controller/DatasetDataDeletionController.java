package gov.nih.brics.controller;

import gov.nih.brics.job.DatasetDataDeletionJob;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

/**
 * Created by amakar on 9/8/2016.
 */

@Controller
@RequestMapping("datasetDataDeletion")
@Scope("request")
public class DatasetDataDeletionController {

    private static Logger logger = Logger.getLogger(DatasetDataDeletionController.class);
    @Autowired
    private DatasetDataDeletionJob datasetDataDeletionJob;

    @RequestMapping(method = RequestMethod.GET)
    public String getCreateForm(Model model) {
        return "datasetDataDeletion/welcome";
    }

    @RequestMapping(method = RequestMethod.POST)
    @Scope("request")
    public String create(HttpSession session) {

        if (logger.isInfoEnabled() == true) {
            logger.info("triggering dataset deletion job");
        }

        this.datasetDataDeletionJob.doJob();

            /* session.setAttribute("future", val); */
        return "datasetDataDeletion/jobStarted";
    }
}
