package gov.nih.brics.job;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.brics.JobScheduler;
import gov.nih.tbi.repository.rdf.RDFGeneratorManagerImpl;

/**
 * This class contains the implementation of the various jobs that are to be run at a pre-defined schedule. The jobs are
 * actually scheduled in the {@link JobScheduler}
 * 
 * @author Npatel
 * 
 */
@Component
@Scope("singleton")
public class RDFJobs {
	private static Logger log = Logger.getLogger(RDFJobs.class);

	@Autowired
	private RDFGeneratorManagerImpl rdfGeneratorManager;

	public void generateAndUploadRDF() {

		log.info("*************************************");
		log.info("STARTING Generate and Upload RDF");
		log.info("*************************************");

		// Generate RDF Files
		rdfGeneratorManager.generateAllRDF();
		System.gc();

		log.info("*************************************");
		log.info("ENDING Generate and Upload RDF");
		log.info("*************************************");
	}
}
