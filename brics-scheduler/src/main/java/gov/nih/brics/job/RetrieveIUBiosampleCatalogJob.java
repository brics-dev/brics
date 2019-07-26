package gov.nih.brics.job;

import gov.nih.tbi.commons.service.BioRepositoryManager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class RetrieveIUBiosampleCatalogJob {

	private static Logger log = Logger.getLogger(RetrieveIUBiosampleCatalogJob.class);

	@Autowired
	private BioRepositoryManager bioRepositoryManager;

	public void retrieveIUBiosampleCatalog() {
		bioRepositoryManager.retrieveIUBiosampleCatalog();
	}
}
