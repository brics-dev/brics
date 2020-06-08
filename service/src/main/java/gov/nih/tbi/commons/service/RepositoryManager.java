package gov.nih.tbi.commons.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBException;

import com.google.gson.JsonArray;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.ClinicalStudy;
import gov.nih.tbi.commons.model.DataSource;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.GuidJoinedData;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.exceptions.DoiWsValidationException;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.exceptions.DictionaryVersioningException;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.SemanticFormStructureList;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.query.model.QTDownloadPackage;
import gov.nih.tbi.repository.model.StudySubmittedFormCache;
import gov.nih.tbi.repository.model.SubmissionTicket;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.alzped.StudyModelName;
import gov.nih.tbi.repository.model.alzped.StudyModelType;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.StudyTherapyType;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.BasicStudySearch;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.DownloadFileDataset;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Downloadable;
import gov.nih.tbi.repository.model.hibernate.EventLog;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudyForm;
import gov.nih.tbi.repository.model.hibernate.StudyKeyword;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.model.hibernate.VisualizationAccessData;
import gov.nih.tbi.repository.model.hibernate.VisualizationAccessRecord;
import gov.nih.tbi.repository.service.exception.DataLoaderException;
import gov.nih.tbi.repository.xml.exception.XmlValidationException;
import gov.nih.tbi.repository.xml.model.XmlInstanceMetaData;
import gov.nih.tbi.repository.xml.model.XmlValidationResponse;

/**
 * Repository Manager Interface. This manages files and repositories.
 * 
 * @author Andrew Johnson
 * 
 */
public interface RepositoryManager {


	public List<BasicDataset> getBasicDatasetsByIds(Set<Long> ids);

	/**
	 * Uploads a file
	 * 
	 * @param user
	 * @param uploadFile
	 * @param fileName
	 * @param fileDescription
	 * @param fileType
	 * @return
	 * @throws SocketException
	 * @throws IOException
	 * @throws JSchException
	 */
	public UserFile uploadFile(Long userId, File uploadFile, String fileName, String fileDescription, String fileType,
			Date uploadDate) throws SocketException, IOException, JSchException;

	/**
	 * 
	 * File Uploader specific for DDT, these files will be stored under the IBIS_Data_Drop User
	 * 
	 * @param userId
	 * @param uploadFile
	 * @param fileName
	 * @param fileDescription
	 * @param fileType
	 * @return
	 * @throws SocketException
	 * @throws IOException
	 * @throws JSchException
	 */
	public UserFile uploadFileDDT(Long userId, File uploadFile, String fileName, String fileDescription,
			String fileType, Date dateUploaded) throws SocketException, IOException, JSchException;

	/**
	 * File uploader that allow for a dynamic path
	 * 
	 * @param userId
	 * @param uploadFile
	 * @param fileName
	 * @param fileDescription
	 * @param fileType
	 * @param dateUploaded
	 * @param path
	 * @return
	 */
	public UserFile uploadFileDDTWithPath(Long userId, File uploadFile, String fileName, String fileDescription,
			String fileType, Date dateUploaded, String path);

	/**
	 * copies the file from one location to another. This is used when a user copies an eform
	 * 
	 * @param userId
	 * @param uploadFile
	 * @param currentFilePath
	 * @param fileName
	 * @param fileDescription
	 * @param fileType
	 * @param dateUploaded
	 * @param newFilePath
	 * @return
	 */
	public boolean copyFileDDT(UserFile oldUserFile, UserFile newUserFile) throws JSchException;

	/**
	 * Overload of the original method, uploads byte array instead.
	 * 
	 * @param user
	 * @param uploadFile
	 * @param fileName
	 * @param fileDescription
	 * @return
	 * @throws SocketException
	 * @throws IOException
	 * @throws JSchException
	 */
	public UserFile uploadFile(Long userId, byte[] uploadFile, String fileName, String fileDescription, String fileType,
			Date uploadDate) throws SocketException, IOException, JSchException;

	/**
	 * Determines files available to the user
	 * 
	 * @param user
	 * @param fileType
	 * @return
	 */
	public List<UserFile> getAvailableUserFiles(Long userId, String fileType);

	/**
	 * Lookup by Id
	 * 
	 * @param user
	 * @param fileId
	 * @return
	 */
	public UserFile getFileById(Long fileId);

	/**
	 * Returns the input stream for the user to download the file.
	 * 
	 * @param userFile
	 * @return
	 * @throws JSchException
	 * @throws SftpException
	 */
	public InputStream getFileStream(UserFile userFile) throws JSchException, SftpException;

	/**
	 * Returns the input stream for the user to download the file.
	 * 
	 * @param userFile
	 * @return
	 * @throws JSchException
	 * @throws SftpException
	 */
	public byte[] getFileByteArray(UserFile userFile) throws Exception;

	/**
	 * Gets a copy of the file referenced in the UserFile object from the SFTP site. This should be more memory
	 * efficient, but may be slightly slower than copying into memory. This is the preferred way to access files from
	 * the SFTP site. Especially if the file size is not known or if a bunch of files will need to be downloaded from
	 * the SFTP site.
	 * 
	 * Since the returned file is a temporary copy of the file referenced in the SFTP store, the returned file should be
	 * deleted after it is no longer used.
	 * 
	 * @param userFile - A description of the file stored in the SFTP site.
	 * @return A temporary file that is copied from the referenced file from the SFTP site.
	 * @throws IOException When there is an error creating or writing to the temporary file.
	 * @throws JSchException When there is an error with opening a connection to the SFTP site.
	 * @throws SftpException When there is an error with reading the referenced file from the SFTP site.
	 */
	public File getFile(UserFile userFile) throws IOException, JSchException, SftpException;

	/**
	 * Retrieves the size of the user's file from the SFTP site. The size will be in bytes.
	 * 
	 * @param userFile - The file from which the file size will be determined.
	 * @return The size of the user's file in bytes.
	 * @throws JSchException When a SFTP connection could not be established with the SFTP site.
	 * @throws SftpException When there is an error determining the user's file size.
	 */
	public Long getFileSize(UserFile userFile) throws JSchException, SftpException;

	/**
	 * Creates a sequence and a table for each repeatable group in the data structure. The tables will have an id column
	 * for each record as well as a reference to a join table for tracking records to the dataset within the submission.
	 * 
	 * @param datastructure - the data structure to create the tables and metadata for
	 * @throws SQLException
	 * @throws UserPermissionException
	 */
	public void createTableFromDataStructure(StructuralFormStructure datastructure,
			Account account) throws SQLException, UserPermissionException;

	/**
	 * Creates a folder and metadata for storing submission data.
	 * 
	 * @throws JSchException
	 * @throws SftpException
	 * @throws IOException
	 */
	public void createFileStore(String relativePath) throws JSchException, SftpException;

	/**
	 * Gets all of the DataStoreInfo in the default order.
	 * 
	 * @return list of DataStoreInfo
	 */
	public List<DataStoreInfo> getAllDataStoreInfo();

	/**
	 * Gets a list of DataStoreInfo sorted by the column specified.
	 * 
	 * @param sortColumn the column name to sort
	 * @param sortAsc true for ascending or false for descending
	 * @return sorted list of DataStoreInfo
	 */
	public List<DataStoreInfo> getAllDataStoreInfoSorted(String sortColumn, boolean sortAsc);

	/**
	 * Returns a DataStoreInfo with and ID matching currentDataStoreId.
	 * 
	 * @param currentDataStoreId the ID of the DataStoreInfo to return
	 * @return
	 */
	public DataStoreInfo getDataStoreById(long currentDataStoreId);

	/**
	 * Gets a list of all userfiles that have description matching an admin file type
	 * 
	 * @param user
	 * @return
	 */
	public List<UserFile> getAdminFiles(Long userId);

	/**
	 * Changes the state of archived flag on the DataStoreInfo associated with the Data Structure.
	 * 
	 * @param dataStruture the data structure being archived
	 */
	public void setArchive(Long dataStrutureId, boolean isArchived);

	/**
	 * Returns a list of studies that the user has the specified access to (namespace aware).
	 * 
	 * @param currentAccount : Used to get permissions.
	 * @param proxyTicket : Used for the permissions web-service. If null, then only publicly available access would be
	 *        returned. Can be null, cannot be empty string.
	 * @param permission : The level of permission we want the user to have to return to them. If null then "READ" will
	 *        be assumed.
	 * @return
	 */
	public List<Study> listStudies(Account currentAccount, String proxyTicket, PermissionType permission);

	/**
	 * Returns a complete list of all studies.
	 * 
	 * NOTE: THIS IS TEMPORARY AND WILL BE REMOVED ONCE DATA TRANSFER TESTING IS COMPLETE
	 * 
	 * 
	 */

	public List<Study> getStudyList();

	public Set<SubmissionType> getStudySubmissionTypes(Long id);

	public Map<Long, Set<SubmissionType>> getStudySubmissionTypes(Set<Long> ids);

	/**
	 * Return a list of datasets based on the search and pagination criteria. key, filterId, ownerId or any of the
	 * properties in pageData may be null (including pageData itself).
	 * 
	 * @param key
	 * @param account
	 * @param filterId
	 * @param ownerId
	 * @param pageData
	 * @return
	 */
	public List<Dataset> searchDatasets(String key, Account account, Long filterId, Long ownerId,
			PaginationData pageData);

	/**
	 * Return a list of datasets based on the search and pagination criteria. key, filterId, ownerId or any of the
	 * properties in pageData may be null (including pageData itself).
	 * 
	 * @param key
	 * @param account
	 * @param statusNow
	 * @param statusRequest
	 * @param ownerId
	 * @param pageData
	 * @return
	 */
	public List<Dataset> searchDatasets(String key, List<String> searchColumns, Account account, Long statusNow, Long statusRequest, Long ownerId,
			PaginationData pageData);

	/**
	 * Return a list of repositories based on the search and pagination criteria.
	 * 
	 * @param key
	 * @param typeId
	 * @param federatedId
	 * @param archivedId
	 * @param pageData
	 * @return
	 */
	public List<DataStoreInfo> searchDataStores(String key, Long typeId, Long federatedId, Long archivedId,
			PaginationData pageData);

	/**
	 * Returns true if the new clinical trial id is unique, false otherwise.
	 * 
	 * @param newId
	 * @param clinicalTrialList
	 * @return
	 */
	public boolean validateUniqueClinicalTrialId(String newId, List<String> clinicalTrialList);

	/**
	 * Returns true if the new grant ID is unique, false otherwise.
	 * 
	 * @param fieldValue
	 * @param grantList
	 * @return
	 */
	public boolean validateUniqueGrantId(String newId, List<String> grantList);

	/**
	 * Saves the study to database
	 * 
	 * @param currentStudy
	 * @return
	 */
	public Study saveStudy(Account account, Study currentStudy);

	/**
	 * Gets the Study from the database for the given id if the account has at least read access.
	 * 
	 * @param user
	 * @param id
	 * @return
	 * @throws UserPermissionException
	 */
	public Study getStudy(Account account, Long id) throws UserPermissionException;

	/**
	 * Gets a Study from the database for the given id
	 * 
	 * @param id
	 * @return
	 */
	public Study getStudy(Long id);

	public Study getPublicStudySearchById(Long id);



	/**
	 * Gets a Study with just its study sites from the database for the given id
	 * 
	 * @param id
	 * @return
	 */
	public Study getStudySites(Long studyId);

	/**
	 * Unregister the entity, then attempts to delete the study
	 * 
	 * @param account
	 * @param currentStudy
	 * @param admin
	 * @return
	 */
	public boolean deleteStudy(Account account, Study currentStudy, boolean admin);

	/**
	 * Takes a study prefix id and returns long ID FITBIR00000231 -> 231
	 * 
	 * @param prefixId
	 * @return
	 */
	public Long parsePrefixId(String prefix, String prefixId);

	/**
	 * Checks clinicalTrial.gov to validate the clinical trial ID
	 * 
	 * @param fieldValue
	 * @return
	 * @throws IOException
	 */
	public boolean validateClinicalTrialId(String clinicalTrialId) throws IOException;

	/**
	 * Gets clinical study from clinicaltrial.gov.
	 * 
	 * @param clinicalTrialId
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	public ClinicalStudy getClinicalStudy(String clinicalTrialId) throws IOException, JAXBException;

	/**
	 * Gets the dataset with specified id from dao
	 * 
	 * @param account
	 * @param datasetId
	 * @return
	 */
	public Dataset getDataset(Long datasetId);

	/**
	 * Gets the basic dataset with specified id from dao
	 * 
	 * @param account
	 * @param datasetId
	 * @return
	 */
	public BasicDataset getBasicDataset(Long datasetId);

	/**
	 * Deletes the dataset with ID
	 * 
	 * @param id
	 * @throws SQLException
	 */
	public void deleteDataset(Dataset dataset, User user, String comment, EventLog eventLog) throws SQLException;

	/**
	 *
	 * @param dataset
	 * @return
	 */

	public EventLog getDatasetLatestEvent(Dataset dataset);

	/**
	 * Register dataset entity map if it is a new dataset and save to database
	 * 
	 * @param account
	 * @param currentDataset
	 * @return
	 */
	public Dataset saveDataset(Account account, Dataset currentDataset);

	/**
	 * Gets the study with the specified name
	 * 
	 * @param studyName
	 * @return
	 */
	public Study getStudyByName(String studyName);

	/**
	 * Gets the BasicStudy with the specified name
	 * 
	 * @param studyName
	 * @return
	 */
	public BasicStudy getBasicStudyByName(String studyName);

	/**
	 * Gets the BasicStudy with the prefix id
	 * 
	 * @param studyName
	 * @return
	 */
	public BasicStudy getBasicStudyByPrefixId(String prefixId);

	/**
	 * This gets a list of studies for the public site view the aggregate data is specific to the public site, so it
	 * gets a different method
	 * 
	 * @return
	 */
	public List<BasicStudySearch> getPublicSiteSearchBasicStudies();
	
	/**
	 * This gets a map of studies for the public site view the aggregate data is specific to the public site, so it
	 * gets a different method. The key for this map is the study id
	 * 
	 * @return
	 */
	public Map<Long,BasicStudySearch> getPublicSiteSearchBasicStudiesMap();

	/**
	 * Gets the BasicStudy with the study id
	 * 
	 * @param studyName
	 * @return
	 */
	public BasicStudy getBasicStudyById(Long studyId);

	/**
	 * Saves the userfile
	 * 
	 * @param userfile
	 * @return
	 */
	public UserFile saveUserFile(UserFile userfile);

	/**
	 * Gets a list of all datasets for the user that have pending uploads
	 * 
	 * @return
	 */
	public List<Dataset> getUploadingDataset(String userName);

	/**
	 * Gets a set of DatasetFiles from a Dataset's ID
	 * 
	 * @param id
	 * @return
	 */
	public Set<DatasetFile> getDatasetFileFromDatasetId(Long id);

	/**
	 * Gets a set of DatasetFiles from a dataset Object (assumes DatasetFile was lazy loaded and hibernate is not
	 * playing nice)
	 * 
	 * @param ds
	 * @return
	 */
	public List<DatasetFile> getDatasetFiles(Dataset ds);

	/**
	 * Gets Study from DatasetFile's ID
	 * 
	 * @param id
	 * @return
	 */
	public BasicStudy getStudyFromDatasetFile(Long id);

	/**
	 * Set the DatasetFile status to complete, also checks if the parent Dataset should be loading or not
	 * 
	 * @param id
	 */
	public void setDatasetFileToComplete(Long id, Account account) throws MalformedURLException;

	/**
	 * Validates that the study title is unique
	 * 
	 * @return
	 */
	public boolean validateStudyTitle(Long id, String title);

	/**
	 * return the number of studies with a given status based on the status id.
	 * 
	 * @param statusId
	 * @return
	 */
	public Long getNumStudiesWithStatus(Long statusId);

	/**
	 * return the number of studies with a given status (or requeted status) based on the status id.
	 * 
	 * @param statusId
	 * @param requested : true if we want to search through the requested status
	 * @return
	 */
	public Long getNumDatasetsWithStatus(Long statusId, boolean requested);

	/**
	 * Returns a list of DatasetSubject with the specified subject. Will only return the the objects that references
	 * datasets that the given account has access to.
	 * 
	 * @param SecureSubject
	 * @return
	 */
	public List<? extends GuidJoinedData> getGuidJoinedDataList(Account account, String guid);


	/**
	 * Deletes the dataset by name, will also remove all userfiles as well as datastore data
	 * 
	 * @param name
	 * @throws SQLException
	 */
	public void deleteDatasetByName(String studyName, String datasetName, User user);

	/**
	 * Deletes the dataset and associated data files
	 * 
	 * @param name
	 * @throws SQLException
	 */
	public boolean deleteDatasetCascadeByName(String studyName, String datasetName);

	/**
	 * Returns string X/Y where x is the number of datasetfiles with status of complete and y is the total number of
	 * datasetfiles in the dataset
	 * 
	 * @param name - name of the dataset in question
	 */
	public String getNumOfCompletedFiles(String datasetId, Account account)
			throws UserPermissionException, BadParameterException;

	/**
	 * Loads the data from the dataset
	 * 
	 * @param account
	 * @param dataset
	 * @throws DataLoaderException 
	 */
	public void loadDataFromDataset(Account account, Long datasetId, String proxyTicket) throws DataLoaderException;

	/**
	 * Adds the dataset to the user's download queue
	 * 
	 * @param dataset
	 * @param account
	 */
	public void addDatasetToDownloadQueue(Dataset dataset, Account account, List<FormStructure> fsList,
			Boolean sendEmail);

	/**
	 * Deletes a Downloadable by ID
	 * 
	 * @param id
	 */
	public void deleteDownloadableById(Long id);

	/**
	 * This method will delete all of the downloadables from the database that have the same IDs as the ones in the
	 * given list
	 * 
	 * @param ids
	 */
	public void deleteDownloadableByIds(List<Long> ids);

	/**
	 * Get the study by the prefixed ID
	 * 
	 * @param account
	 * @param studyId
	 * @return
	 * @throws UserPermissionException
	 */
	public Study getStudyByPrefixedId(Account account, String studyId) throws UserPermissionException;


	/**
	 * Get the study by the prefixed ID
	 * 
	 * @param account
	 * @param studyId
	 * @return
	 * @throws UserPermissionException
	 */
	public Study getStudyByPrefixedIdExcludingDataset(Account account, String studyId) throws UserPermissionException;

	/**
	 * Gets the dataset by its prefixed ID
	 * 
	 * @param datasetId
	 * @param account
	 * @return
	 */
	public Dataset getDatasetByPrefixedId(String datasetId, Account account)
			throws UserPermissionException, BadParameterException;

	/**
	 * Gets the dataset by its prefixed ID, this version of the get will include researchManagement info, which is
	 * usually lazy loaded.
	 * 
	 * @param datasetId
	 * @param account
	 * @return
	 */
	public Dataset getDatasetByPrefixedIdWithStudyInfo(String datasetId, Account account)
			throws UserPermissionException, BadParameterException;

	/**
	 * Checks if the dataset exists, used to determine whether our deleteDatasetByName should be called
	 * 
	 * @param studyName
	 * @param datasetName
	 */
	public Boolean doesDatasetExist(String studyName, String datasetName);

	public Dataset getDatasetByName(String studyName, String datasetName);

	public Dataset processSubmissionTicket(SubmissionTicket submissionTicket, String localPath, String serverPath,
			String userName, String studyName, String datasetName, boolean isDerived, boolean isProformsSubmission,
			Date submitDate, Long administeredFormId, boolean isSubjectSubmitted);

	/**
	 * Create a new AccessRecord object at the current timestamp and the given number of records. The date can also be
	 * specified.
	 * 
	 * @param dataset
	 * @param account
	 * @param dataSource
	 * @param recordCount
	 * @param queueDate
	 * @return
	 */
	public AccessRecord createAccessRecord(BasicDataset dataset, Account account, DataSource dataSource,
			Long recordCount, Date queueDate);

	/**
	 * Create a new AccessRecord object at the current timestamp and the given number of records.
	 * 
	 * @param dataset
	 * @param account
	 * @param dataSource
	 * @param recordCount
	 * @return
	 */
	public AccessRecord createAccessRecord(BasicDataset dataset, Account account, DataSource dataSource,
			Long recordCount);

	/**
	 * Create a new AccessRecord object at the current timestamp. Set recordCount as the total number of records in the
	 * dataset.
	 * 
	 * @param dataset
	 * @param account
	 * @param dataSource
	 * @return
	 */
	public AccessRecord createAccessRecord(BasicDataset dataset, Account account, DataSource dataSource);

	/**
	 * Uses the submission record join table to find out how many record are connected to a given dataset.
	 * 
	 * @param datasetId
	 * @return
	 */
	public Long getDatasetRecordCount(Long datasetId);

	/**
	 * Returns all the accessRecords for a specific study. If no study is supplied, then all records are obtained.
	 * 
	 * @param study
	 * @return
	 */
	public List<AccessRecord> getAccessRecords(Study study, String startDateStr, String endDateStr,
			PaginationData pageData);

	/**
	 * Returns all the accessRecords for selected studies.
	 * 
	 * @param studyList
	 * @return
	 */
	public List<AccessRecord> getAccessRecordsByStudyList(Set<Long> studyIdList, String startDateStr, String endDateStr,
			PaginationData pageData);

	/**
	 * Returns all the accessRecords for a specific users.
	 * 
	 * @param study
	 * @return
	 */
	public List<VisualizationAccessRecord> getAccessRecordsPerUser();
	
	/**
	 * Returns all the accessRecords for a specific users.
	 * 
	 * @return
	 */
	public List<VisualizationAccessData> getAllAccessRecordsPerUser();


	/**
	 * Returns all the accessRecord objects that fit the supplied filters. Uses sort and ascending from pageData
	 * 
	 * @param study : The study we want to retrieve access records for. if study is null, then access records across
	 *        study will be retrieved.
	 * @param searchText: Will filter for a disjunction on the following fields (dataset id, dataset name, dataset
	 *        status, username, download date, number of records). Will alter what fileds are searched on based on the
	 *        format of the search string (e.g. will only search date field if ISO date is entered and will only search
	 *        num of records if integer is entered).
	 * @param daysOld : Only return records that were added more recently then X days ago (rounded to the complete day).
	 * @param pageData : Contains the field name to sort on and the ascending/descending. (Other fields are currently
	 *        ignored).
	 * @return : A list of access records for a given study
	 */
	public List<AccessRecord> searchAccessRecords(Study study, String searchText, Long daysOld,
			PaginationData pageData);

	/**
	 * Given a studyId, returns the number of access records for that study;
	 * 
	 * @param studyId
	 * @return
	 */
	public int countAccessRecordsStudy(Long studyId);

	/**
	 * Given a datasetId, returns the number of access records for that dataset.
	 * 
	 * @param datasetId
	 * @return
	 */
	public int countAccessRecords(Long datasetId);

	/**
	 * Returns the download packages owned by the given user
	 * 
	 * @param user
	 * @return
	 */
	public List<DownloadPackage> getDownloadPackageByUser(User user);

	public void addQueryToolDownloadPackage(Account account, QTDownloadPackage downloadPackage,
			List<UserFile> userFiles);

	/**
	 * Returns the download package with the given ID
	 * 
	 * @param id
	 * @return
	 */
	public DownloadPackage getDownloadPackageById(Long id);

	/**
	 * Given a list of IDs, delete the download packages with those IDs
	 * 
	 * @param ids
	 */
	public void deleteDownloadPackagesByIds(List<Long> ids);

	/**
	 * Sends email notification to the user about add to download queue operation's completion
	 * 
	 * @param account
	 * @param downloadPackage - the download package we want to notify the user about
	 */
	public void sendDownloadAddNotification(Account account, DownloadPackage downloadPackage);

	public DownloadPackage getDownloadPackageByDatasetAndUser(DownloadFileDataset downloadFileDataset, User user);

	/**
	 * Run the MetaParser on the given data and return the given object.
	 * 
	 * @param xmlData
	 * @throws XmlValidationException
	 * @return
	 */
	public XmlInstanceMetaData inspectXmlData(byte[] xmlData) throws XmlValidationException;

	/**
	 * Given a form structure short name, makes a web service call to the dictionary API (at the given domain) for the
	 * schema of the latest version of that form structure.
	 * 
	 * @param fsName
	 * @param domain
	 * @throws XmlValidationException
	 * @return
	 */
	public byte[] getDictionarySchema(String fsName, String domain) throws XmlValidationException;

	/**
	 * Validates the given xml instance data against the provided schema with the BRICS xml sax validator.
	 * 
	 * @param xmlData
	 * @param schemaData
	 * @throws XmlValidationException
	 * @return
	 */
	public XmlValidationResponse validateXmlData(byte[] xmlData, byte[] schemaData) throws XmlValidationException;

	public List<Dataset> getDatasetsByStudy(Study study);

	/**
	 * Deletes the user file from the DB
	 * 
	 * @param userFile
	 */
	public void removeUserFile(UserFile userFile);



	public void removeUserFile(long userFileId);

	public String getDatasetFilePath(String prefixedId, String datasetName, String fileName);

	/**
	 * Given a form structure ID, return all the DatasetDataStructure objects with that ID
	 * 
	 * @param id
	 * @return
	 */
	public List<DatasetDataStructure> getDatasetDataStructureByDataStructureId(Long id);

	/**
	 * Given a new form structure ID and an old form structure Id, change all of the form structure IDs for all
	 * DatasetDataStructures with the old form structure ID to the new form structure ID
	 * 
	 * @param newFormStructureId
	 * @param oldFormStructureId
	 * @throws DictionaryVersioningException
	 */
	public void updateFormStructureReferences(Long oldFormStructureId, Long newFormStructureId,
			Map<Long, Long> oldToNewRepeatableGroupId) throws DictionaryVersioningException;

	/**
	 * Creates a study for Performance Benchmarking from existing study with replacing PDBP with TEST in prefixedId and
	 * adding TEST- to title
	 * 
	 * @return
	 * @throws RunTimeException
	 */
	public Study createStudyForPB(Study study, Account account);


	/**
	 * Given entityID and entityType, returns all the event Logs
	 * 
	 * @param entityID
	 * @param entityType
	 */
	public Set<EventLog> getEventLogs(Long entityID, EntityType entityType);


	public List<Dataset> getDatasets(Set<Long> datasetIds);


	public Dataset changeDatasetStatus(Dataset dataset, DatasetStatus datasetStatus, Account account,
			String statusChangecomment, EventLog eventLog) throws SQLException;

	public Dataset approveDatasetStatus(Dataset dataset, Account account, String statusChangecomment, EventLog eventLog)
			throws SQLException;

	public Dataset rejectDatasetStatus(Dataset dataset, Account account, String statusChangecomment, EventLog eventLog);

	/**
	 * For Performance Benchmarking, adds all dataset to the download queue
	 * 
	 * @param account
	 * @param proxyTicket
	 */
	public List<Dataset> addDatasetsToDownloadQueueByStudy(Study study, Account account, String proxyTicket);

	/**
	 * Remove the download packages by given list of ids
	 * 
	 * @param ids
	 */
	public void deleteDownloadPackageByIds(List<Long> ids);

	/**
	 * Modify the csv file for further submission adding the given information in the first line and update the form
	 * structure name create a new csv file and remove the old csv file
	 * 
	 * @param account
	 * @param dataFile
	 * @param destFile
	 * @param formName
	 * @param updataDSName
	 * @param studyId
	 * 
	 */
	public String modifyCSVFile(Account account, File dataFile, File destFile, String formName, String updatedDSName,
			String studyId) throws FileNotFoundException, NumberFormatException, UnsupportedEncodingException;

	/**
	 * Download data by given study
	 * 
	 * @param account
	 * @param study
	 * @param proxyTicket
	 * @param downloadDir
	 * 
	 */
	public List<Downloadable> downloadDataByStudy(Account account, Study study, String proxyTicket, String downloadDir)
			throws UnsupportedEncodingException, JSchException, SftpException, UserPermissionException;


	/**
	 * Save dataset with EventLog for status change
	 * 
	 * @param account
	 * @param currentDataset
	 * @return
	 */
	public void saveDatasetEventLog(Account account, Dataset dataset, EventLog eventLog, String statusChangeComment)
			throws SQLException;

	public void stuckDatasetChangeStatus(int cutOffDay);

	public StudyForm getStudyForm(Long studyId, String shortName, String version);

	public InputStream getSftpInputStream(String filePath, String fileName) throws JSchException, SftpException;

	public List<StudyKeyword> retrieveAllStudyKeywords();

	public List<StudyKeyword> searchKeywords(String searchKey);

	public Long getKeywordCount(String keyword);

	public SemanticFormStructureList getPublishedFS(SessionAccount account, String proxyTicket);

	public SemanticFormStructureList getPublihedAndAwaitingFS(SessionAccount account, String proxyTicket);

	public FormStructure getFormStructureDetails(SessionAccount account, String shortName, String version,
			String proxyTicket);

	public List<Long> getDataSetIdsByStudyListAndDatasetStatus(Long studyIds, Set<DatasetStatus> datasetStatuses);

	public List<Long> getDataSetIdsByStudyListAndDatasetStatus(Set<Long> studyIds, Set<DatasetStatus> datasetStatuses);

	public SemanticFormStructureList getFormStructuresForCollectedDataWithStatuses(Set<Long> studyIds,
			Set<DatasetStatus> datasetStatuses) throws UnsupportedEncodingException;

	public List<SupportingDocumentation> getPublicationsByStudyId(Long studyId);

	public ResearchManagement getPrimaryInvestigatorByStudyId(Long studyId);

	public ResearchManagement getStudyManagementImage(Long studyId, Long rmId);



	/**
	 * Creates a DOI record with the OSTI/IAD system for the passed in study object. Once the DOI is successfully
	 * created, the given study object's DOI and OSTI ID properties will be updated with the values returned by the IAD
	 * web service.
	 * 
	 * @param study - The Study object to which a DOI will be created for.
	 * @throws IllegalStateException When there is an error while translating the given Study object to a DOI record.
	 * @throws WebApplicationException When there is an error while sending the DOI create request to the IAD web
	 *         service.
	 * @throws DoiWsValidationException When the response back from the IAD web service indicates that there was a
	 *         validation error for the sent DOI record.
	 */
	public void createDoiForStudy(Study study)
			throws IllegalStateException, WebApplicationException, DoiWsValidationException;

	public boolean isStudyPublic(Long studyId);

	/**
	 * This is a simple get study by title with datasets eager loaded
	 * 
	 * @param studyTitle
	 * @return
	 */
	public Study getStudyWithDatasetsByName(String studyTitle);

	/**
	 * Get prefixed ID...this is the version without permissions check
	 * 
	 * @param prefixedId
	 * @return
	 */
	public Study getStudyByPrefixedId(String prefixedId);

	public List<Dataset> getStudyDataset(Set<Long> studyIds);

	public List<Dataset> getDatasetById(Set<Long> datasetId);

	public void updateDatasets(Set<Dataset> dataset);

	public List<DatasetFile> getDatasetFiles(Long datasetId);

	public Study getStudyExcludingDatasets(Long id);

	public Study getLazyStudy(Long id);

	/**
	 * This gets a list of submitted data information for the public site overview
	 * 
	 * @return
	 */
	public JsonArray getPublicSiteSubmittedData(StudySubmittedFormCache studySubmittedFormCache);

	public JsonArray getPublicationJsonByStudyId(Long StudyId);

	public InputStream getSftpInputStream(Long datafileEndpointInfo, String filePath, String fileName)
			throws JSchException, SftpException;

	public Study getStudyGraphicFileById(Integer studyId);

	public boolean moveAsNewFileName(String filePath, String fileName, String newFileName) throws JSchException;

	public SupportingDocumentation getSupportingDocument(Long supportingDocId);

	public Dataset getDatasetWithDatasetFiles(Long id);

	public Dataset getDatasetExcludingDatasetFiles(Long datasetId);

	public Dataset getDatasetOnly(Long id);

	public void updateDatasetStatus(DatasetStatus status, Dataset dataset);

	public BigInteger getDatasetPendingFileCount(Long datasetId);

	public SupportingDocumentation getPublicationDocument(Long studyId, Long supportingDocId);

	public void batchSaveAccessRecord(Set<AccessRecord> accessRecords);

	public boolean deleteDatasetCascadeByName(Dataset dataset);

	public void saveStudyTherapeuticAgent(Set<StudyTherapeuticAgent> therapeuticAgentSet, String studyId);

	public void saveStudyTherapyType(Set<StudyTherapyType> therapyTypeSet, String studyId);

	public void saveStudyTherapeuticTarget(Set<StudyTherapeuticTarget> therapeuticTargetSet, String studyId);

	public void saveStudyModelType(Set<StudyModelType> modelTypeSet, String studyId);

	public void saveStudyModelName(Set<StudyModelName> modelNameSet, String studyId);

	public boolean deleteDatasetFileByName(UserFile userFile);
	
	public void cleanRepoTables(StructuralFormStructure datastructure) throws SQLException;
	
	public void emailDevPublicationError(StructuralFormStructure datastructure, String errorMessage);

	public BigDecimal getTotalDatasetFilesSize(Long arDatasetId);

	public HashSet<SubmissionType> getDatasetWithSubmissionTypes(long datasetId);


}
