package gov.nih.tbi.repository.ws;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AccountProvider;
import gov.nih.tbi.account.ws.model.UserLogin;
import gov.nih.tbi.dictionary.model.UpdateFormStructureReferencePayload;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.repository.model.SubmissionTicket;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;

/**
 * Contract of functions all repo web service providers should provide.
 * 
 */
@WebService
public interface RepositoryWebService {

	public static final String hashSalt = "BRICS is an awesome project.";

	/**
	 * Returns a list of studies a user can select from.
	 * 
	 * @return
	 */
	public Set<Study> getStudies(UserLogin userLogin);

	/**
	 * Creates a study
	 * 
	 * @return
	 * @throws RunTimeException
	 */
	public Study createStudy(Study study, File dataSubmissionDocument, String fileDescription, Long diseaseId,
			UserLogin userLogin);

	/**
	 * Saves a dataset and returns the new one
	 * 
	 * @param dataset
	 * @param userName
	 * @return
	 */
	public Dataset saveDataset(Dataset dataset, UserLogin userLogin);

	public List<Dataset> getUploadingDataset(UserLogin userLogin);

	public Set<DatasetFile> getDatasetFiles(Long id, UserLogin userLogin);

	public boolean createTableFromDataStructure(StructuralFormStructure datastructure, UserLogin userLogin);

	public void setDatasetFileToComplete(Long id, UserLogin userLogin) throws MalformedURLException;

	public BasicStudy getStudyFromDatasetFile(Long id, UserLogin userLogin);

	/**
	 * Deletes the dataset by name
	 * 
	 * @param name
	 * @param user
	 * @throws SQLException
	 */
	void deleteDatasetByName(String studyName, String name, UserLogin userLogin);

	public String getNumOfCompletedFiles(String studyName, String name, UserLogin userLogin);

	public List<DownloadPackage> getDownloadPackage(UserLogin userLogin);

	public List<UserFile> getAvailbleUserFiles(UserLogin userLogin, String fileType);

	/**
	 * Lookup file by Id
	 * 
	 * @param user
	 * @param fileId
	 * @return
	 */
	public UserFile getFileById(UserLogin userLogin, Long fileId);

	/**
	 * Uploads given file
	 * 
	 * @param userLogin
	 * @param uploadFile
	 * @param fileName
	 * @param fileDescription
	 * @param fileType
	 * @return
	 */
	public UserFile uploadFile(UserLogin userLogin, File uploadFile, String fileName, String fileDescription,
			String fileType);

	/**
	 * Uploads a given byte array
	 * 
	 * @param userLogin
	 * @param uploadFile
	 * @param fileName
	 * @param fileDescription
	 * @param fileType
	 * @return
	 */
	public UserFile uploadByteArray(UserLogin userLogin, byte[] uploadFile, String fileName, String fileDescription,
			String fileType);

	public boolean setDataStoreInfoArchive(UserLogin userLogin, Long dataStructureId, boolean isArchived);

	/**
	 * Returns an account provider that uses administrator credentials which are defined in modules.properties
	 * 
	 * @return
	 */
	public AccountProvider getAdminAccountProvider(UserLogin userLogin);

	/**
	 * Returns the account of the user
	 * 
	 * @param userLogin
	 * @return
	 * @throws MalformedURLException
	 */
	public Account getUserAccount(UserLogin userLogin);

	public void sendDownloadReport(String report);

	Dataset processSubmissionTicket(SubmissionTicket submissionTicket, String localPath, String serverPath,
			String studyName, String datasetName, boolean isDerived, boolean isProformsSubmission, Date submitDate,
			Long administeredFormId, boolean isSubjectSubmitted, UserLogin userLogin);

	public DownloadPackage getUserDownloadById(Long id, UserLogin userLogin);

	public boolean updateFormStructureReferences(UserLogin userLogin, UpdateFormStructureReferencePayload payload);

	public Study getStudyByPrefixedId(String prefixedId, UserLogin userLogin);
}
