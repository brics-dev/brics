package gov.nih.brics.job;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.commons.service.RepositoryManager;

@Component
@Scope("singleton")
public class StuckDatasetStatusChangeJob {
	
	private static Logger log = Logger.getLogger(StuckDatasetStatusChangeJob.class);	
	private static int cutOffDay =2;
	
	@Autowired
	private RepositoryManager repositoryManager;
	
	
	public void stuckDatasetStatusChange(){
		log.log(Level.INFO, "---------Beginning stuckDatasetStatusChange job");	
		repositoryManager.stuckDatasetChangeStatus(cutOffDay);
		log.log(Level.INFO, "---------End stuckDatasetStatusChange job");
	}
	

}
