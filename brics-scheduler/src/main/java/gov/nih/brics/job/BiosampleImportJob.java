package gov.nih.brics.job;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import gov.nih.tbi.repository.dataimport.BiosampleImportService;

@Component
@Scope("singleton")
public class BiosampleImportJob extends EmailJobs {

	private static final Logger logger = Logger.getLogger(BiosampleImportJob.class);
	
	@Autowired
	private BiosampleImportService biosampleImportService;
	
	private static final String ERROR_EMAIL_SUBJECT = "Error importing IU catalog";
	private static final String ERROR_EMAIL_BODY = "The IU Biosample catalog failed import. Please check the logs for more information";
	private static final String FAILED = "Failed";
	private static final String NOT_CONFIGURED = "Not configured";
	
	@Async
	public void doJob() {
		
		String result = biosampleImportService.processBiosampleSubmission();
		
		if(result.contentEquals(FAILED)){
			logger.error("Error completing proforms data load");
			try {
				sendEmail(ERROR_EMAIL_SUBJECT, ERROR_EMAIL_BODY, modulesConstants.getModulesDevEmail());
			} catch (MessagingException e1) {
				logger.error("Error sending email", e1);
			}
		} else if(result.contentEquals(NOT_CONFIGURED)) {
			logger.info("Non configured instance ran the job. Do Nothing");
		}
				
		
	}
}
