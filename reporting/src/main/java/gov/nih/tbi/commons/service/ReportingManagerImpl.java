package gov.nih.tbi.commons.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.service.complex.BaseManagerImpl;
import gov.nih.tbi.constants.ReportingModulesConstants;
import gov.nih.tbi.repository.dao.AccessRecordDao;
import gov.nih.tbi.repository.dao.ReportTypeDao;
import gov.nih.tbi.repository.dao.StudyDao;
import gov.nih.tbi.repository.model.hibernate.ReportType;
import gov.nih.tbi.repository.model.hibernate.Study;

/**
 * Implementation of the Repository Manager Interface.
 * 
 * Uses the SftpClientManager singleton to open and get connections to SFTP sites
 * 
 * @author Andrew Johnson
 * 
 */
@Service
@Scope("singleton")
public class ReportingManagerImpl extends BaseManagerImpl implements ReportingManager {
	private static final Logger logger = Logger.getLogger(ReportingManagerImpl.class);
	private static final long serialVersionUID = 7730224803772719143L;

	@Autowired
	StudyDao studyDao;

	@Autowired
	ReportTypeDao reportTypeDao;
	
	@Autowired
	AccountDao accountDao;
	
	@Autowired
	AccessRecordDao accessRecordDao;

	@Autowired
	ReportingModulesConstants modulesConstants;
	
	@Override
	public List<Study> getStudyList() {

		return studyDao.getAll();
	}
	
	public int countAccessRecordsStudy(Long studyId) {
		return accessRecordDao.countAccessRecordsStudy(studyId);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public List<Study> listStudies(Account currentAccount, String proxyTicket, PermissionType permission) {
		List<Study> s = studyDao.getAll();
		return s;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<ReportType> listAllReports() {
		List<ReportType> allReports = reportTypeDao.getAll();
		return allReports;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public List<Account> listAllAccounts() {
		List<Account> allAccount = accountDao.getAll();
		return allAccount;
	}
	
	public List<Study> getAllStudiesWithKeyword() {
		return studyDao.getAllStudiesWithKeyword();
	}
	
	public Long getStatusCount(StudyStatus status) {
		return studyDao.getStatusCount(status);
	}

}
