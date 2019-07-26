package gov.nih.tbi.commons.service;

import java.util.List;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.repository.model.hibernate.ReportType;
import gov.nih.tbi.repository.model.hibernate.Study;

/**
 * Repository Manager Interface. This manages files and repositories.
 * 
 * @author Andrew Johnson
 * 
 */
public interface ReportingManager {


	/**
	 * Returns a complete list of all studies.
	 * 
	 * NOTE: THIS IS TEMPORARY AND WILL BE REMOVED ONCE DATA TRANSFER TESTING IS COMPLETE
	 * 
	 * 
	 */

	public List<Study> getStudyList();
	

	/**
	 * Given a studyId, returns the number of access records for that study;
	 * 
	 * @param studyId
	 * @return
	 */
	public int countAccessRecordsStudy(Long studyId);
	
	
	public List<ReportType> listAllReports() ;
	
	
	public List<Account> listAllAccounts() ;
	
	public List<Study> getAllStudiesWithKeyword();
	
	public Long getStatusCount(StudyStatus status);
}
