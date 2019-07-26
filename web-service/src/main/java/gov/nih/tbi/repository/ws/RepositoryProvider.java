package gov.nih.tbi.repository.ws;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AccountProvider;
import gov.nih.tbi.account.ws.AuthenticationProvider;
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
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;

/**
 * Client side functionality for the RepositoryWebService.
 * 
 */
public class RepositoryProvider extends AuthenticationProvider {

	private static final String namespaceURI = "http://cxf.ws.repository.tbi.nih.gov/";
	private static final String serviceName = "RepositoryWebServiceImplService";
	private static final String portName = "RepositoryWebServiceImplPort";
	private static final String wsdlExtension = "portal/ws/repositoryWebService?wsdl";

	private static URL WSDL_LOCATION;

	private Service service;
	private RepositoryWebService repositoryWebService;

	public RepositoryProvider(String serverLocation, String userName, String password) throws MalformedURLException {

		super(serverLocation, userName, password);
		WSDL_LOCATION = new URL(RepositoryProvider.class.getResource("."), serverLocation + wsdlExtension);

		service = new RepositoryService(WSDL_LOCATION, new QName(namespaceURI, serviceName));
		repositoryWebService = getRepositoryService();
	}

	/**
	 * Gets the repositoryWebService to interact with.
	 * 
	 * @return
	 */
	@WebEndpoint(name = "repositoryWebService")
	public RepositoryWebService getRepositoryService() {

		return service.getPort(new QName(namespaceURI, portName), RepositoryWebService.class);
	}

	private class RepositoryService extends Service {

		protected RepositoryService(URL wsdlDocumentLocation, QName serviceName) {

			super(wsdlDocumentLocation, serviceName);
		}
	}

	/**
	 * Wrapper method to obscure username hashing
	 * 
	 * @return
	 */
	// Used by Upload Mgr
	public Set<Study> getStudies() {

		return repositoryWebService.getStudies(getUserLogin());
	}

	public Study createStudy(Study study, File dataSubmissionDocument, String fileDescription, Long diseaseId) {

		return repositoryWebService.createStudy(study, dataSubmissionDocument, fileDescription, diseaseId,
				getUserLogin());
	}

	/**
	 * Wrapper method to obscure username hashing
	 * 
	 * @param submissionTicket
	 * @param localPath
	 * @param serverPath
	 * @param studyName
	 * @param datasetName
	 * @return
	 */
	// Used by Upload MGR
	public Dataset processSubmissionTicket(SubmissionTicket submissionTicket, String localPath, String serverPath,
			String studyName, String datasetName, boolean isDerived, boolean isProformsSubmission, Date submitDate,
			Long administeredFormId, boolean isSubjectSubmitted) {

		return repositoryWebService.processSubmissionTicket(submissionTicket, localPath, serverPath, studyName,
				datasetName, isDerived, isProformsSubmission, submitDate, administeredFormId, isSubjectSubmitted, getUserLogin());
	}

	public Study getStudyByPrefixedId(String prefixedId) {
		return repositoryWebService.getStudyByPrefixedId(prefixedId, getUserLogin());
	}

	/**
	 * Wrapper method to obscure username hashing
	 * 
	 * @return
	 */
	// Upload MGR
	public List<Dataset> getUploadingDataset() {

		return repositoryWebService.getUploadingDataset(getUserLogin());
	}

	/**
	 * Wrapper method to obscure username hashing
	 * 
	 * @param id
	 * @return
	 */
	// Upload Mgr
	public Set<DatasetFile> getDatasetFiles(Long id) {

		return repositoryWebService.getDatasetFiles(id, getUserLogin());
	}

	/**
	 * Wrapper method to obscure username hashing. This is used in the Upload Manager
	 * 
	 * @param id
	 * @throws MalformedURLException
	 */
	// Upload Mgr
	public void setDatasetFileToComplete(Long id) throws MalformedURLException {

		repositoryWebService.setDatasetFileToComplete(id, getUserLogin());
	}

	/**
	 * Wrapper method to obscure username hashing
	 * 
	 * @param id
	 * @return
	 */
	// Upload Manager
	public BasicStudy getStudyFromDatasetFile(Long id) {

		return repositoryWebService.getStudyFromDatasetFile(id, getUserLogin());
	}

	/**
	 * Wrapper method to obscure username hashing
	 * 
	 * @param name
	 */
	// Upload Manager
	public void deleteDatasetByName(String studyName, String name) {

		repositoryWebService.deleteDatasetByName(studyName, name, getUserLogin());
	}

	// Upload Manager
	public String getNumOfCompletedFiles(String studyName, String name) {

		return repositoryWebService.getNumOfCompletedFiles(studyName, name, getUserLogin());
	}

	/**
	 * Creates repository tables for given datastructure Assumes Datastructure is in dictionary db but it does not check
	 * as of yet
	 * 
	 * @param DataStructure from dictionary module
	 * @param User
	 * @return Success
	 * @throws Exception
	 * @throws SQLException
	 */
	public boolean createTableFromDataStructure(StructuralFormStructure datastructure) throws SQLException, Exception {

		return repositoryWebService.createTableFromDataStructure(datastructure, getUserLogin());
	}

	public List<DownloadPackage> getDownloadPackage() {

		return repositoryWebService.getDownloadPackage(getUserLogin());
	}

	public DownloadPackage getUserDownloadById(Long id) {

		return repositoryWebService.getUserDownloadById(id, getUserLogin());
	}

	/**
	 * Sends reports to the server
	 * 
	 * @param string
	 */
	public void sendDownloadReport(String report) {

		// TODO Auto-generated method stub
		repositoryWebService.sendDownloadReport(report);
	}

	/**
	 * Get List of User Files for logged in user
	 * 
	 * @param FileType
	 * @return List<UserFile>
	 */
	public List<UserFile> getAvailableUserFiles(String fileType) {

		return repositoryWebService.getAvailbleUserFiles(getUserLogin(), fileType);
	}

	/**
	 * Get UserFile by Id
	 * 
	 * @param FileType
	 * @return List<UserFile>
	 */
	public UserFile getFileById(Long fileId) {

		return repositoryWebService.getFileById(getUserLogin(), fileId);
	}

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
	public UserFile uploadFile(File uploadFile, String fileName, String fileDescription, String fileType) {

		return repositoryWebService.uploadFile(getUserLogin(), uploadFile, fileName, fileDescription, fileType);
	}

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
	public UserFile uploadByteArray(byte[] uploadFile, String fileName, String fileDescription, String fileType) {

		return repositoryWebService.uploadByteArray(getUserLogin(), uploadFile, fileName, fileDescription, fileType);
	}

	public boolean setDataStoreInfoArchive(Long dataStructureId, boolean isArchived) {

		return repositoryWebService.setDataStoreInfoArchive(getUserLogin(), dataStructureId, isArchived);
	}

	public Account getUserAccount() {

		return repositoryWebService.getUserAccount(getUserLogin());
	}

	public AccountProvider getAccountProvider() {

		return repositoryWebService.getAdminAccountProvider(getUserLogin());
	}

	public boolean updateFormStructureReferences(UpdateFormStructureReferencePayload payload) {
		return repositoryWebService.updateFormStructureReferences(getUserLogin(), payload);
	}

}
