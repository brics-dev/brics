package gov.nih.brics.job;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.sun.mail.iap.ProtocolException;

import gov.nih.tbi.repository.dataimport.EmailDataLoadError;
import gov.nih.tbi.repository.dataimport.ImportCSVGenerator;

@Component
@Scope("singleton")
public class DataImportJob extends EmailJobs {

	private static final Logger logger = Logger.getLogger(DataImportJob.class);
	private static String EMAIL_SUBJECT = "Dataload Error Report";
	private static String ERROR_EMAIL_SUBJECT = "Error In Nightly Import Process";
	private static String ERROR_EMAIL_BODY = "The nightly import process failed, please check the logs";
	
	@Autowired
	private ImportCSVGenerator importCSVGenerator;
	
	@Autowired
	private EmailDataLoadError emailDataLoadError;
	
	@Async
	public void doJob() {
		try {
			importCSVGenerator.processSubmissions();
			String messageBody = emailDataLoadError.getEmailDataLoadErrorBody();
			if(!messageBody.equals("")){
				sendEmail(EMAIL_SUBJECT, messageBody, modulesConstants.getModulesOrgEmail());
			} else {
				logger.info("No submissions in error during last 24 hours. Not sending email");
			}
		} catch (ProtocolException | IOException | SQLException | MessagingException e) { //send email to devs if failed
			logger.error("Error completing proforms data load");
			try {
				sendEmail(ERROR_EMAIL_SUBJECT, ERROR_EMAIL_BODY, modulesConstants.getModulesDevEmail());
			} catch (MessagingException e1) {
				logger.error("Error sending email", e1);
			}
			e.printStackTrace();
		} 
	}
}
