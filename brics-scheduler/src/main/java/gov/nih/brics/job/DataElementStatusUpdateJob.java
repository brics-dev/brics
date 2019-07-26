package gov.nih.brics.job;

import gov.nih.tbi.commons.service.DictionaryToolManager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class DataElementStatusUpdateJob {

	private static Logger log = Logger.getLogger(DataElementStatusUpdateJob.class);

	@Autowired
    private DictionaryToolManager dictionaryManager;
    
    public void updateDataElementStatus() {
    	
		log.info("*****************************************");
		log.info("STARTING update statuses of data elements");
		log.info("*****************************************");

		dictionaryManager.updateDEStatusWithUntilDate();
		
		log.info("***************************************");
		log.info("ENDING update statuses of data elements");
		log.info("***************************************");

    }
}
