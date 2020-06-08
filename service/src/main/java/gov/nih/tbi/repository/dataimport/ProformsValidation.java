package gov.nih.tbi.repository.dataimport;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nih.tbi.repository.dao.ProformsDataImportDao;

@Service
public class ProformsValidation {
	private static final Logger logger = Logger.getLogger(ProformsValidation.class);
	
	private static final String IMPORT_FAILED_MESSAGE = "Failure";
	private static final int SUBMITTED = 2;
	private static final int VALIDATION_FAILED = 3;
	
	@Autowired
	ProformsDataImportDao proformsDataImportDao;

	protected void updateProformsSubmissionStatus(String validationMessage, String adminFormId) throws SQLException {
		
		boolean submissionFailed = validationMessage.contains(IMPORT_FAILED_MESSAGE);
		if(submissionFailed) {
			logger.info("Validation failed. Admin Form Id: " + adminFormId);
			proformsDataImportDao.updateAdminFormSubmissionStatus(VALIDATION_FAILED, adminFormId);
			
		} else {
			logger.info("Validation passed. Admin Form Id: " + adminFormId);
			proformsDataImportDao.updateAdminFormSubmissionStatus(SUBMITTED, adminFormId);
		}
		
		proformsDataImportDao.deleteAdminFormFromDataSubmissionTable(adminFormId);

	}	

}
