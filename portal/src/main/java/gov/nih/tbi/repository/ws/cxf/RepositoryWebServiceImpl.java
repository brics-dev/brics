package gov.nih.tbi.repository.ws.cxf;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AccountProvider;
import gov.nih.tbi.account.ws.model.UserLogin;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.dictionary.exceptions.DictionaryVersioningException;
import gov.nih.tbi.dictionary.model.UpdateFormStructureReferencePayload;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.repository.dao.DatasetDao;
import gov.nih.tbi.repository.model.SubmissionTicket;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.ws.RepositoryWebService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.jcraft.jsch.JSchException;

/**
 * CXF based web service for Repository.
 * 
 */
@WebService(endpointInterface = "gov.nih.tbi.repository.ws.RepositoryWebService")
public class RepositoryWebServiceImpl extends HashMethods implements RepositoryWebService {

	/***************************************************************************************************/

	private static Logger logger = Logger.getLogger(RepositoryWebServiceImpl.class);

	/***************************************************************************************************/

	@Autowired
	AccountManager accountManager;

	@Autowired
	RepositoryManager repositoryManager;

	@Autowired
	DatasetDao datasetDao;

	@Autowired
	protected ModulesConstants modulesConstants;

	@Override
	public Set<Study> getStudies(UserLogin userLogin) {

		logger.debug("getStudies() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		Account account = accountManager.getAccountByUserName(userLogin.getUserName());

		Set<Study> userStudies = new HashSet<Study>();
		Set<Long> ids = accountManager.listUserAccess(account, EntityType.STUDY, PermissionType.WRITE);

		for (Long id : ids) {
			Study currStudy = repositoryManager.getStudy(id);
			if (StudyStatus.PRIVATE.equals(currStudy.getStudyStatus())
					|| StudyStatus.PUBLIC.equals(currStudy.getStudyStatus())) {
				userStudies.add(repositoryManager.getStudy(id));
			}
		}

		return userStudies;
	}

	@Override
	public Study getStudyByPrefixedId(String prefixedId, UserLogin userLogin) {
		logger.debug("getStudyByPrefixedId() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		Study study = repositoryManager.getStudyByPrefixedId(prefixedId);

		return study;
	}

	@Override
	public Study createStudy(Study study, File dataSubmissionDocument, String fileDescription, Long diseaseId,
			UserLogin userLogin) {

		logger.debug("createStudy() -- ");

		// Validate User
		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		// Get User
		Account account = accountManager.getAccountByUserName(userLogin.getUserName());

		if (!accountManager.hasRole(account, RoleType.ROLE_STUDY)) {
			throw new RuntimeException(ServiceConstants.WRITE_ACCESS_DENIED);
		}

		//
		// TODO: Validate Study Info!
		//
		// TODO:FIXME NO SERIOUSLY IMPLEMENT ME
		//

		try {
			// Upload file to FTP Site
			UserFile submissionFile;
			submissionFile = repositoryManager.uploadFile(account.getUser().getId(), dataSubmissionDocument,
					dataSubmissionDocument.getName(), fileDescription, ServiceConstants.FILE_TYPE_STUDY, null);

			// Associate uploaded file w/ Study
			study.setDataSubmissionDocument(submissionFile);

			// Currently just setting Study Status to requested until workflow is determined between proforms and BRICs
			study.setStudyStatus(StudyStatus.REQUESTED);

			// study.setDataManager("WEBSERVICE");
			// study.setDataManagerEmail("WEBSERVICEEMAIL");

			// Save Study
			repositoryManager.saveStudy(account, study);

			// Return ID
			return study;
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (JSchException e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public Dataset saveDataset(Dataset dataset, UserLogin userLogin) {

		logger.debug("saveDataset() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		return repositoryManager.saveDataset(accountManager.getAccountByUserName(userLogin.getUserName()), dataset);
	}

	@Override
	public Dataset processSubmissionTicket(SubmissionTicket submissionTicket, String localPath, String serverPath,
			String studyName, String datasetName, boolean isDerived, boolean isProformsSubmission, Date submitDate,
			Long administeredFormId, boolean isSubjectSubmitted, UserLogin userLogin) {

		logger.debug("processSubmissionTicket() -- ");

		return repositoryManager.processSubmissionTicket(submissionTicket, localPath, serverPath,
				userLogin.getUserName(), studyName, datasetName, isDerived, isProformsSubmission, submitDate,
				administeredFormId, isSubjectSubmitted);
	}

	@Override
	public List<Dataset> getUploadingDataset(UserLogin userLogin) {

		logger.debug("getUploadingDataset() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		return repositoryManager.getUploadingDataset(userLogin.getUserName());
	}

	@Override
	public Set<DatasetFile> getDatasetFiles(Long id, UserLogin userLogin) {

		logger.debug("getDatasetFiles() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		return repositoryManager.getDatasetFileFromDatasetId(id);
	}

	@Override
	public void setDatasetFileToComplete(Long id, UserLogin userLogin) throws MalformedURLException {

		logger.debug("setDatasetFileToComplete() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		repositoryManager.setDatasetFileToComplete(id, this.getUserAccount(userLogin));
	}

	@Override
	public Account getUserAccount(UserLogin userLogin) {

		try {
			if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
				throw new RuntimeException("Failed to validate Username.");
			}

			AccountProvider provider = new AccountProvider(modulesConstants.getModulesAccountURL(),
					modulesConstants.getAdministratorUsername(),
					HashMethods.getServerHash(modulesConstants.getAdministratorUsername(),
							modulesConstants.getSaltedAdministratorPassword()));
			return provider.getByUserName(userLogin.getUserName());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public BasicStudy getStudyFromDatasetFile(Long id, UserLogin userLogin) {

		logger.debug("getStudyFromDatasetFile() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		return repositoryManager.getStudyFromDatasetFile(id);
	}

	@Override
	public void deleteDatasetByName(String studyName, String datasetName, UserLogin userLogin) {

		logger.debug("deleteDatasetByName() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		String username = userLogin.getUserName();
		Account account = this.accountManager.getAccountByUserName(username);
		User user = account.getUser();

		repositoryManager.deleteDatasetByName(studyName, datasetName, user);
	}


	@Override
	public String getNumOfCompletedFiles(String studyName, String datasetName, UserLogin userLogin) {

		return datasetName;

		// logger.debug("getNumOfCompletedFiles() -- ");
		//
		// if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
		// {
		// throw new RuntimeException("Failed to validate Username.");
		// }
		//
		// return repositoryManager.getNumOfCompletedFiles(studyName, datasetName);
	}

	@Override
	public boolean createTableFromDataStructure(StructuralFormStructure datastructure, UserLogin userLogin) {

		logger.debug("createTableFromDataStructure() -- ");

		boolean success = true;

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		Account account = accountManager.getAccountByUserName(userLogin.getUserName());

		/* // TODO: Need to check if datastructure is in dictionary module (another webservice call?) */

		Thread createTableThread = new Thread(new Runnable() {
			public void run() {
				try {
					repositoryManager.createTableFromDataStructure(datastructure, modulesConstants.getModulesDevEmail(),
							account);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UserPermissionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		createTableThread.setName("ThreadWithTimeStamp" + System.currentTimeMillis());
		createTableThread.start();

		return success;

	}

	/***************************************
	 * 
	 * 
	 * Download Queue Web Services
	 * 
	 * 
	 ***************************************/

	public DownloadPackage getUserDownloadById(Long id, UserLogin userLogin) {
		logger.debug("getUserDownloadById() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		return repositoryManager.getDownloadPackageById(id);
	}

	@Override
	public List<DownloadPackage> getDownloadPackage(UserLogin userLogin) {

		logger.debug("getUserDownload() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}
		String userName = new String(userLogin.getUserName());
		Account account = accountManager.getAccountByUserName(userName);

		return repositoryManager.getDownloadPackageByUser(account.getUser());

	}

	/***************************************
	 * 
	 * 
	 * User File Web Services
	 * 
	 * 
	 ***************************************/
	@Override
	public List<UserFile> getAvailbleUserFiles(UserLogin userLogin, String fileType) {

		logger.debug("getAvailbleUserFiles() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		Account account = accountManager.getAccountByUserName(userLogin.getUserName());

		return repositoryManager.getAvailableUserFiles(account.getUser().getId(), fileType);

	}

	@Override
	public UserFile getFileById(UserLogin userLogin, Long fileId) {

		logger.debug("getFileById() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		// TODO:FIXME
		// This method is insecure and does not check if the user should be returned this file
		return repositoryManager.getFileById(fileId);

	}

	@Override
	public UserFile uploadFile(UserLogin userLogin, File uploadFile, String fileName, String fileDescription,
			String fileType) {

		logger.debug("uploadFile() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		Account account = accountManager.getAccountByUserName(userLogin.getUserName());

		try {
			if (!modulesConstants.getModulesDDTEnabled())
				return repositoryManager.uploadFile(account.getUserId(), uploadFile, fileName, fileDescription,
						fileType, null);
			else
				return repositoryManager.uploadFileDDT(account.getUserId(), uploadFile, fileName, fileDescription,
						fileType, null);
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (JSchException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public UserFile uploadByteArray(UserLogin userLogin, byte[] uploadFile, String fileName, String fileDescription,
			String fileType) {

		logger.debug("uploadByteArray() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		Account account = accountManager.getAccountByUserName(userLogin.getUserName());

		try {
			return repositoryManager.uploadFile(account.getUserId(), uploadFile, fileName, fileDescription, fileType,
					null);
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (JSchException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean setDataStoreInfoArchive(UserLogin userLogin, Long dataStructureId, boolean isArchived) {

		logger.debug("setDataStoreInfoArchive() -- ");

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		try {
			repositoryManager.setArchive(dataStructureId, isArchived);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @inheritDoc
	 */
	public AccountProvider getAdminAccountProvider(UserLogin userLogin) {

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		try {
			return new AccountProvider(modulesConstants.getModulesAccountURL(),
					modulesConstants.getAdministratorUsername(), modulesConstants.getAdministratorPassword());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Add a report dro
	 */
	@Override
	public void sendDownloadReport(String report) {

		logger.info(report);
		// TODO Auto-generated method stub
	}

	@Override
	public boolean updateFormStructureReferences(UserLogin userLogin, UpdateFormStructureReferencePayload payload) {
		logger.debug("updateFormStructureReferences() -- ");

		boolean success = true;

		if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1())) {
			throw new RuntimeException("Failed to validate Username.");
		}

		try {
			repositoryManager.updateFormStructureReferences(payload.getOldFormStructureId(),
					payload.getNewFormStructureId(), payload.getOldNewRepeatableGroupIdMap());
		} catch (DictionaryVersioningException e) {
			e.printStackTrace();
			success = false;
		}

		return success;
	}

}
