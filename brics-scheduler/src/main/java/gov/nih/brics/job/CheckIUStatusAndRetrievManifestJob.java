package gov.nih.brics.job;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.commons.service.BioRepositoryManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class CheckIUStatusAndRetrievManifestJob {
	private static Logger log = Logger.getLogger(CheckIUStatusAndRetrievManifestJob.class);

	@Autowired
	protected ModulesConstants modulesConstants;

	@Autowired
	private BioRepositoryManager bioRepositoryManager;


	public void checkIUStatusAndRetrieveManifest() {
		log.log(Level.INFO, "---------Beginning checkIUStatusAndRetrieveManifest job");
		bioRepositoryManager.checkIUStatusAndRetrieveManifest();
		log.log(Level.INFO, "---------End checkIUStatusAndRetrieveManifest job");
	}
}
