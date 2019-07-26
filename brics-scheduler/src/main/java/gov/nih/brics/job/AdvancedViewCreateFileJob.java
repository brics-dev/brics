package gov.nih.brics.job;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.commons.service.AdvancedVisualizationManager;

@Component
@Scope("singleton")
public class AdvancedViewCreateFileJob {
	private static Logger log = Logger.getLogger(AdvancedViewCreateFileJob.class);
	
	@Autowired
	private AdvancedVisualizationManager advancedVisualizationManager;
	
	public void storeJson() {
		log.info("*************************************");
		log.info("STARTING Advanced Visualization Json");
		log.info("*************************************");
		
		advancedVisualizationManager.buildFile();
		
		log.info("*************************************");
		log.info("ENDING Advanced Visualization Generate and Store Json ");
		log.info("*************************************");
	}

}
