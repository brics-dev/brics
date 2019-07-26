package gov.nih.brics.job;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import gov.nih.tbi.commons.service.PublicSiteManager;

@Component
@Scope("singleton")
public class PublicSiteStudiesCreateFileJob {
	
private static Logger log = Logger.getLogger(PublicSiteStudiesCreateFileJob.class);
	
	@Autowired
	private PublicSiteManager publicSiteManager;
	
	public void storeJson() {
		log.info("*************************************");
		log.info("STARTING PublicSite Studies Json");
		log.info("*************************************");
		
		publicSiteManager.buildFile();
		
		log.info("*************************************");
		log.info("ENDING PublicSite Studies Generate and Store Json ");
		log.info("*************************************");
	}

}
