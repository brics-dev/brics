package gov.nih.tbi.repository.service.hibernate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.dao.EntityMapDao;
import gov.nih.tbi.account.dao.PermissionGroupDao;
import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.VisualizationEntityMap;
import gov.nih.tbi.account.service.complex.BaseManagerImpl;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.dao.FileTypeDao;
import gov.nih.tbi.commons.model.Author;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.ClinicalStudy;
import gov.nih.tbi.commons.model.DataSource;
import gov.nih.tbi.commons.model.DatasetFileStatus;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.commons.model.GuidJoinedData;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.JobParameterKey;
import gov.nih.tbi.commons.model.JobType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.Publication;
import gov.nih.tbi.commons.model.QueryParameter;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.model.exceptions.DoiWsValidationException;
import gov.nih.tbi.commons.model.exceptions.DownloadQueueException;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.BadParameterException;
import gov.nih.tbi.commons.service.DataLoader;
import gov.nih.tbi.commons.service.DataLoaderManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.exceptions.DictionaryVersioningException;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.SemanticFormStructureList;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.ws.RestDictionaryProvider;
import gov.nih.tbi.doi.model.OSTIRecord;
import gov.nih.tbi.doi.util.DoiUtil;
import gov.nih.tbi.doi.ws.DoiMinterProvider;
import gov.nih.tbi.query.dao.StudySubmittedFormsSparqlDao;
import gov.nih.tbi.query.model.QTDownloadPackage;
import gov.nih.tbi.repository.dao.AccessRecordDao;
import gov.nih.tbi.repository.dao.BasicDatasetDao;
import gov.nih.tbi.repository.dao.BasicDatasetFileDao;
import gov.nih.tbi.repository.dao.BasicStudyDao;
import gov.nih.tbi.repository.dao.DataStoreBinaryInfoDao;
import gov.nih.tbi.repository.dao.DataStoreDao;
import gov.nih.tbi.repository.dao.DataStoreInfoDao;
import gov.nih.tbi.repository.dao.DataStoreTabularColumnInfoDao;
import gov.nih.tbi.repository.dao.DataStoreTabularInfoDao;
import gov.nih.tbi.repository.dao.DatafileEndpointInfoDao;
import gov.nih.tbi.repository.dao.DatasetDao;
import gov.nih.tbi.repository.dao.DatasetDataStructureDao;
import gov.nih.tbi.repository.dao.DatasetFileDao;
import gov.nih.tbi.repository.dao.DatasetSubjectDao;
import gov.nih.tbi.repository.dao.DownloadFileDatasetDao;
import gov.nih.tbi.repository.dao.DownloadPackageDao;
import gov.nih.tbi.repository.dao.DownloadableDao;
import gov.nih.tbi.repository.dao.EventLogDao;
import gov.nih.tbi.repository.dao.KeywordDao;
import gov.nih.tbi.repository.dao.ModelNameDataDao;
import gov.nih.tbi.repository.dao.ModelTypeDataDao;
import gov.nih.tbi.repository.dao.RepositoryDao;
import gov.nih.tbi.repository.dao.ResearchManagementDao;
import gov.nih.tbi.repository.dao.ScheduledJobDao;
import gov.nih.tbi.repository.dao.StudyDao;
import gov.nih.tbi.repository.dao.StudyFormDao;
import gov.nih.tbi.repository.dao.SubmissionRecordJoinDao;
import gov.nih.tbi.repository.dao.SupportingDocumentationDao;
import gov.nih.tbi.repository.dao.TherapeuticAgentDataDao;
import gov.nih.tbi.repository.dao.TherapeuticTargetDataDao;
import gov.nih.tbi.repository.dao.TherapyTypeDataDao;
import gov.nih.tbi.repository.dao.UserFileDao;
import gov.nih.tbi.repository.dao.VisualizationAccessRecordDao;
import gov.nih.tbi.repository.model.DownloadPackageOrigin;
import gov.nih.tbi.repository.model.StudySubmittedFormCache;
import gov.nih.tbi.repository.model.SubmissionDataFile;
import gov.nih.tbi.repository.model.SubmissionPackage;
import gov.nih.tbi.repository.model.SubmissionTicket;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.alzped.ModelName;
import gov.nih.tbi.repository.model.alzped.ModelType;
import gov.nih.tbi.repository.model.alzped.StudyModelName;
import gov.nih.tbi.repository.model.alzped.StudyModelType;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.StudyTherapyType;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.TherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.TherapyType;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.BasicDatasetFile;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.BasicStudySearch;
import gov.nih.tbi.repository.model.hibernate.DataStoreBinaryInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularInfo;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.repository.model.hibernate.DatasetDownloadFile;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.DatasetSubject;
import gov.nih.tbi.repository.model.hibernate.DownloadFileDataset;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Downloadable;
import gov.nih.tbi.repository.model.hibernate.EventLog;
import gov.nih.tbi.repository.model.hibernate.Grant;
import gov.nih.tbi.repository.model.hibernate.QueryToolDownloadFile;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.ScheduledJob;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudyForm;
import gov.nih.tbi.repository.model.hibernate.StudyKeyword;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.model.hibernate.VisualizationAccessData;
import gov.nih.tbi.repository.model.hibernate.VisualizationAccessRecord;
import gov.nih.tbi.repository.rdf.RDFGeneratorManagerImpl;
import gov.nih.tbi.repository.service.RepositoryTableBuilder;
import gov.nih.tbi.repository.service.exception.DataLoaderException;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;
import gov.nih.tbi.repository.xml.exception.XmlValidationException;
import gov.nih.tbi.repository.xml.model.XmlInstanceMetaData;
import gov.nih.tbi.repository.xml.model.XmlValidationResponse;
import gov.nih.tbi.repository.xml.parser.DataParser;
import gov.nih.tbi.repository.xml.parser.MetaParser;

/**
 * Implementation of the Repository Manager Interface.
 * 
 * Uses the SftpClientManager singleton to open and get connections to SFTP
 * sites
 * 
 * @author Andrew Johnson
 * 
 */
@Service
@Scope("singleton")
public class RepositoryManagerImpl extends BaseManagerImpl implements RepositoryManager {
	private static final Logger logger = Logger.getLogger(RepositoryManagerImpl.class);
	private static final long serialVersionUID = 7730224803772719143L;
	private static final String PV_MAPPING = "pv_mapping";
	private static final String PV_MAPPING_FILE_NAME_SUFFIX = "pv_mapping.csv";
	
	@Autowired
	MailEngine mailEngine;

	// After dictionary split all dictionary objects aquired through web service
	// @Autowired
	// DictionaryToolManager dictionaryManager;

	@Autowired
	AccountManager accountManager;

	@Autowired
	StudyDao studyDao;

	@Autowired
	UserFileDao userFileDao;

	@Autowired
	DatafileEndpointInfoDao datafileEndpointInfoDao;

	@Autowired
	DataStoreDao dataStoreDao;

	@Autowired
	DataStoreInfoDao dataStoreInfoDao;

	@Autowired
	DataStoreTabularInfoDao dataStoreTabularInfoDao;

	@Autowired
	DataStoreTabularColumnInfoDao dataStoreTabularColumnInfoDao;

	@Autowired
	DataStoreBinaryInfoDao dataStoreBinaryInfoDao;

	@Autowired
	EntityMapDao entityMapDao;

	@Autowired
	PermissionGroupDao permissionGroupDao;

	@Autowired
	StaticReferenceManager staticManager;

	@Autowired
	DatasetDao datasetDao;

	@Autowired
	EventLogDao eventLogDao;

	@Autowired
	ScheduledJobDao scheduledJobDao;

	@Autowired
	DatasetFileDao datasetFileDao;

	@Autowired
	SubmissionRecordJoinDao submissionRecordJoinDao;

	@Autowired
	DatasetDataStructureDao datasetDataStructureDao;

	@Autowired
	DatasetSubjectDao datasetSubjectDao;

	@Autowired
	FileTypeDao fileTypeDao;

	@Autowired
	ModulesConstants modulesConstants;

	@Autowired
	DataLoader dataLoader;

	@Autowired
	RDFGeneratorManagerImpl rdfGenerator;

	@Autowired
	RepositoryDao repoDao;

	@Autowired
	AccessRecordDao accessRecordDao;

	@Autowired
	VisualizationAccessRecordDao visualizationAccessRecordDao;

	@Autowired
	DownloadPackageDao downloadPackageDao;

	@Autowired
	BasicDatasetDao basicDatasetDao;

	@Autowired
	DownloadableDao downloadableDao;

	@Autowired
	DownloadFileDatasetDao downloadFileDatasetDao;

	@Autowired
	MessageSource messageSource;

	@Autowired
	BasicDatasetFileDao basicDatasetFileDao;

	@Autowired
	BasicStudyDao basicStudyDao;

	@Autowired
	StudyDao StudyDao;

	@Autowired
	StudyFormDao studyFormDao;

	@Autowired
	KeywordDao keywordDao;

	@Autowired
	SupportingDocumentationDao supportingDocumentationDao;
	@Autowired
	DataLoaderManager dataLoaderManager;

	@Autowired
	RepositoryTableBuilder repositoryTableBuilder;

	@Autowired
	ResearchManagementDao researchManagementDao;

	@Autowired
	StudySubmittedFormsSparqlDao studySubmittedFormsSparqlDao;

	@Autowired
	TherapeuticAgentDataDao therapeuticAgentDataDao;

	@Autowired
	TherapeuticTargetDataDao therapeuticTargetDataDao;

	@Autowired
	TherapyTypeDataDao therapyTypesDataDao;

	@Autowired
	ModelNameDataDao modelNameDataDao;

	@Autowired
	ModelTypeDataDao modelTypeDataDao;

	/**
	 * @throws JSchException {@inheritDoc}
	 */
	public UserFile uploadFile(Long userId, File uploadFile, String fileName, String fileDescription, String fileType,
			Date uploadDate) throws SocketException, IOException, JSchException {

		FileType currentFileType = fileTypeDao.get(fileType);
		String filePath = ServiceConstants.TBI_DEFAULT_FILE_PATH + userId + ServiceConstants.FILE_SEPARATER;
		DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);

		logger.info("Uploading " + fileName + "...");

		if (info != null) {
			logger.debug(" RepositoryManagerImpl :: uploadFile :: " + " , dataFileEndpointInfo name : "
					+ info.getEndpointName() + " , dataFileEndpointInfo url  : " + info.getUrl() + " , fileName : "
					+ fileName + " , filePath : " + filePath);
		}

		SftpClient client = SftpClientManager.getClient(info);

		UserFile userFile = new UserFile();
		userFile.setDatafileEndpointInfo(info);
		userFile.setDescription(fileDescription);
		userFile.setName(fileName);
		userFile.setFileType(currentFileType);
		userFile.setUploadedDate(uploadDate);
		userFile.setPath(filePath);
		userFile.setUserId(userId);
		userFile.setSize(uploadFile.length());

		userFile = userFileDao.save(userFile);

		try {
			client.upload(uploadFile, filePath, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SftpClientManager.closeAll();

		logger.info("Done with upload!");
		return userFile;
	}

	/**
	 * 
	 * File Uploader specific for DDT, these files will be stored under the
	 * IBIS_Data_Drop User
	 * 
	 */
	public UserFile uploadFileDDT(Long userId, File uploadFile, String fileName, String fileDescription,
			String fileType, Date dateUploaded) throws SocketException, IOException, JSchException {

		FileType currentFileType = fileTypeDao.get(fileType);

		DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.DDT_DATAFILE_ENDPOINT_ID);
		String filePath = ServiceConstants.DDT_DEFAULT_FILE_PATH + userId + ServiceConstants.FILE_SEPARATER;

		if (info != null) {
			logger.info(" RepositoryManagerImpl :: uploadFileDDT :: " + " , dataFileEndpointInfo name : "
					+ info.getEndpointName() + " , dataFileEndpointInfo url  : " + info.getUrl() + " , fileName : "
					+ fileName + " , filePath : " + filePath);
		}

		SftpClient client = SftpClientManager.getClient(info);

		UserFile userFile = new UserFile();
		userFile.setDatafileEndpointInfo(info);
		userFile.setDescription(fileDescription);
		userFile.setName(fileName);
		userFile.setFileType(currentFileType);
		userFile.setUploadedDate(dateUploaded);
		userFile.setPath(filePath);
		userFile.setUserId(userId);

		userFile = userFileDao.save(userFile);

		try {
			client.upload(uploadFile, filePath, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SftpClientManager.closeAll();

		return userFile;
	}

	/**
	 * 
	 * File Uploader specific for DDT, these files will be stored under the
	 * IBIS_Data_Drop User
	 * 
	 * @throws JSchException
	 * 
	 */
	public boolean copyFileDDT(UserFile oldUserFile, UserFile newUserFile) throws JSchException {

		DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.DDT_DATAFILE_ENDPOINT_ID);

		if (oldUserFile != null && oldUserFile.getDatafileEndpointInfo() != null) {
			logger.info(" RepositoryManagerImpl :: copyFileDDT :: oldFile name : " + oldUserFile.getName()
					+ " , path : " + oldUserFile.getPath() + " , dataFileEndpointInfo name : "
					+ oldUserFile.getDatafileEndpointInfo().getEndpointName() + " , dataFileEndpointInfo url  : "
					+ oldUserFile.getDatafileEndpointInfo().getUrl());
		}
		if (newUserFile != null) {
			logger.info(" RepositoryManagerImpl :: copyFileDDT :: newUserFile name : " + newUserFile.getName()
					+ " , path : " + newUserFile.getPath());
		}

		SftpClient client = SftpClientManager.getClient(info);

		boolean fileSaved = false;

		try {
			fileSaved = client.copyToNewLocation(oldUserFile.getPath(), newUserFile.getName(), newUserFile.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		SftpClientManager.closeAll();

		return fileSaved;
	}

	/**
	 * 
	 * File Uploader specific for DDT, these files will be stored under the
	 * IBIS_Data_Drop User
	 * 
	 * @throws
	 * 
	 */
	public UserFile uploadFileDDTWithPath(Long userId, File uploadFile, String fileName, String fileDescription,
			String fileType, Date dateUploaded, String path) {

		FileType currentFileType = fileTypeDao.get(fileType);
		UserFile userFile = new UserFile();

		DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.DDT_DATAFILE_ENDPOINT_ID);

		if (info != null) {
			logger.info(" RepositoryManagerImpl :: uploadFileDDTWithPath :: " + " , dataFileEndpointInfo name :  "
					+ info.getEndpointName() + " , dataFileEndpointInfo url  : " + info.getUrl() + " , fileName : "
					+ fileName + " , filePath : " + path);
		}

		try {
			SftpClient client = SftpClientManager.getClient(info);

			userFile.setDatafileEndpointInfo(info);
			userFile.setDescription(fileDescription);
			userFile.setName(fileName);
			userFile.setFileType(currentFileType);
			userFile.setUploadedDate(dateUploaded);
			userFile.setPath(path);
			userFile.setUserId(userId);

			userFile = userFileDao.save(userFile);

			client.upload(uploadFile, path, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SftpClientManager.closeAll();

		return userFile;
	}

	/**
	 * {@inheritDoc}
	 */
	public UserFile uploadFile(Long userId, byte[] uploadFile, String fileName, String fileDescription, String fileType,
			Date uploadDate) throws SocketException, IOException, JSchException {

		FileType currentFileType = fileTypeDao.get(fileType);
		String filePath = ServiceConstants.TBI_DEFAULT_FILE_PATH + userId + ServiceConstants.FILE_SEPARATER;

		DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);

		if (info != null) {
			logger.info(" RepositoryManagerImpl :: uploadFile :: " + " , dataFileEndpointInfo name : "
					+ info.getEndpointName() + "," + " , dataFileEndpointInfo url  : " + info.getUrl() + ","
					+ " , filename : " + fileName + ", " + " , filepath : " + filePath);
		}

		SftpClient client = SftpClientManager.getClient(info);

		UserFile userFile = new UserFile();
		userFile.setDatafileEndpointInfo(info);
		userFile.setDescription(fileDescription);
		userFile.setFileType(currentFileType);
		userFile.setName(fileName);
		userFile.setPath(filePath);
		userFile.setUserId(userId);
		userFile.setUploadedDate(uploadDate);

		userFile = userFileDao.save(userFile);

		try {
			client.upload(uploadFile, filePath, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SftpClientManager.closeAll();

		return userFile;
	}

	/**
	 * This method removes the UserFile from database.
	 */
	public void removeUserFile(UserFile userFile) {

		if (userFile != null) {
			userFileDao.remove(userFile.getId());
		}
	}

	public InputStream getSftpInputStream(String filePath, String fileName) throws JSchException, SftpException {
		logger.info(
				" RepositoryManagerImpl :: getSftpInputStream :: filePath : " + filePath + " fileName : " + fileName);

		DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);

		if (info != null) {
			logger.info(" dataFileEndpointInfo name : " + info.getEndpointName() + " , dataFileEndpointInfo url  : "
					+ info.getUrl());
		}

		SftpClient client = SftpClientManager.getClient(info);
		return client.getFileStream(filePath, fileName);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<UserFile> getAvailableUserFiles(Long userId, String fileType) {

		FileType type = fileTypeDao.get(fileType);

		return userFileDao.getByUserId(userId, type);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<UserFile> getAdminFiles(Long userId) {

		List<UserFile> out = new ArrayList<UserFile>();

		if (userId != null) {
			for (UserFile userFile : userFileDao.getByUserId(userId)) {
				if (staticManager.getAdminFileTypeByName(userFile.getDescription()) != null) {
					out.add(userFile);
				}
			}
		}

		return out;
	}

	/**
	 * {@inheritDoc}
	 */
	public UserFile getFileById(Long fileId) {

		// TODO:FIXME NEED TO ADD SOME TYPE OF SECURITY CHECK!
		return userFileDao.get(fileId);
	}

	/**
	 * {@inheritDoc}
	 */
	public InputStream getFileStream(UserFile userFile) throws JSchException, SftpException {
		if (userFile != null) {
			logger.info(" RepositoryManagerImpl :: getFileStream :: filePath : " + userFile.getPath() + " fileName : "
					+ userFile.getName());

			if (userFile.getDatafileEndpointInfo() != null) {
				logger.info(" RepositoryManagerImpl :: getFileStream :: " + " , dataFileEndpointInfo name : "
						+ userFile.getDatafileEndpointInfo().getEndpointName() + " , dataFileEndpointInfo url  : "
						+ userFile.getDatafileEndpointInfo().getUrl());
			}
		}

		SftpClient client = SftpClientManager.getClient(userFile.getDatafileEndpointInfo());
		InputStream data = client.getFileStream(userFile.getPath(), userFile.getName());
		return data;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	public byte[] getFileByteArray(UserFile userFile) throws Exception {
		if (userFile != null) {
			logger.info(" RepositoryManagerImpl :: getFileByteArray :: filePath : " + userFile.getPath()
					+ " , fileName : " + userFile.getName());

			if (userFile.getDatafileEndpointInfo() != null) {
				logger.info(" RepositoryManagerImpl :: getFileByteArray :: " + " , dataFileEndpointInfo name : "
						+ userFile.getDatafileEndpointInfo().getEndpointName() + " , dataFileEndpointInfo url  : "
						+ userFile.getDatafileEndpointInfo().getUrl());
			}
		}

		SftpClient client = SftpClientManager.getClient(userFile.getDatafileEndpointInfo());
		byte[] data = client.downloadBytes(userFile.getName(), userFile.getPath());
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getFile(UserFile userFile) throws IOException, JSchException, SftpException {
		if (userFile != null) {
			logger.info(" RepositoryManagerImpl :: getFile :: filePath : " + userFile.getPath() + " , fileName : "
					+ userFile.getName());

			if (userFile.getDatafileEndpointInfo() != null) {
				logger.info(" RepositoryManagerImpl :: getFile :: " + " , dataFileEndpointInfo name : "
						+ userFile.getDatafileEndpointInfo().getEndpointName() + " , dataFileEndpointInfo url  : "
						+ userFile.getDatafileEndpointInfo().getUrl());
			}
		}

		SftpClient client = SftpClientManager.getClient(userFile.getDatafileEndpointInfo());
		File file = client.downloadFile(userFile.getName(), userFile.getPath());

		return file;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getFileSize(UserFile userFile) throws JSchException, SftpException {
		if (userFile != null) {
			logger.info(" RepositoryManagerImpl :: getFileSize :: fileName : " + userFile.getName() + " filePath : "
					+ userFile.getPath());

			if (userFile.getDatafileEndpointInfo() != null) {
				logger.info(" RepositoryManagerImpl :: getFileSize :: " + " , dataFileEndpointInfo name : "
						+ userFile.getDatafileEndpointInfo().getEndpointName() + " , dataFileEndpointInfo url  : "
						+ userFile.getDatafileEndpointInfo().getUrl());
			}
		}

		SftpClient client = SftpClientManager.getClient(userFile.getDatafileEndpointInfo());
		Long data = client.getFileSize(userFile.getPath(), userFile.getName());
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	public UserFile saveUserFile(UserFile userfile) {

		return userFileDao.save(userfile);
	}

	public DataStoreInfo getDataStoreFromDataStructure(FormStructure dataStructure) {

		return dataStoreInfoDao.getByDataStructureId(dataStructure.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	public void createTableFromDataStructure(StructuralFormStructure datastructure, Account account)
			throws SQLException, UserPermissionException {

		repositoryTableBuilder.createRepositoryStore(datastructure, account);
	}

	/**
	 * @throws JSchException
	 * @throws SftpException {@inheritDoc}
	 */
	public void createFileStore(String relativePath) throws JSchException, SftpException {

		DatafileEndpointInfo dataFileEndpointInfo = datafileEndpointInfoDao
				.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);

		if (dataFileEndpointInfo != null) {
			logger.info(" RepositoryManagerImpl :: createFileStore :: " + " , dataFileEndpointInfo name : "
					+ dataFileEndpointInfo.getEndpointName() + " , dataFileEndpointInfo url  : "
					+ dataFileEndpointInfo.getUrl());
		}

		SftpClient client = SftpClientManager.getClient(dataFileEndpointInfo);
		String filePath = ServiceConstants.TBI_REPO_DEFAULT_FILE_PATH + relativePath;

		logger.info(" RepositoryManagerImpl :: createFileStore :: filePath : " + filePath);

		client.createFolder(filePath);

		DataStoreBinaryInfo dataStoreBinaryInfo = dataStoreBinaryInfoDao
				.save(new DataStoreBinaryInfo(dataFileEndpointInfo, filePath));
		dataStoreInfoDao.save(new DataStoreInfo(dataStoreBinaryInfo, false, false));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DataStoreInfo> getAllDataStoreInfo() {

		List<DataStoreInfo> dataStoreInfos = dataStoreInfoDao.getAll();
		if (dataStoreInfos == null) {
			dataStoreInfos = new ArrayList<DataStoreInfo>();
		}

		return dataStoreInfos;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DataStoreInfo> getAllDataStoreInfoSorted(String sortColumn, boolean sortAsc) {

		List<DataStoreInfo> dataStoreInfos = dataStoreInfoDao.getAllSorted(sortColumn, sortAsc);
		if (dataStoreInfos == null) {
			dataStoreInfos = new ArrayList<DataStoreInfo>();
		}

		return dataStoreInfos;
	}

	/**
	 * {@inheritDoc}
	 */
	public DataStoreInfo getDataStoreById(long currentDataStoreId) {

		return dataStoreInfoDao.get(currentDataStoreId);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setArchive(Long dataStructureId, boolean isArchived) {

		// TODO:NEED permission stuff here...
		DataStoreInfo info = dataStoreInfoDao.getByDataStructureId(dataStructureId);
		info.setArchived(isArchived);
		dataStoreInfoDao.save(info);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Study> listStudies(Account currentAccount, String proxyTicket, PermissionType permission) {

		// If the user is an administrator then we can just pass "null" to grab all of
		// the studies
		// without performing the permission check. (not using GenericDao.getAll()
		// because we need
		// them sorted by id). UNLESS THE USER ONLY WANTS OWNER! THEN CONTINUE AS NORMAL
		if (accountManager.hasRole(currentAccount, RoleType.ROLE_STUDY_ADMIN)
				&& !PermissionType.OWNER.equals(permission)) {
			return studyDao.getByIds(null);
		}

		// Default the permission to read
		PermissionType p = PermissionType.READ;
		if (permission != null) {
			p = permission;
		}

		// Make a web service call that gets the ids the user has the chosen access to.
		Set<Long> accessIds = null;
		RestAccountProvider accountProvider = new RestAccountProvider(
				modulesConstants.getModulesAccountURL(Long.valueOf(currentAccount.getDiseaseKey())), proxyTicket);
		try {
			accessIds = accountProvider.listUserAccess(currentAccount.getId(), EntityType.STUDY, p, false);
		} catch (UnsupportedEncodingException e) {
			logger.error("Unable to retrieve studies permission for list study view.", e);
		}

		// If accessIds is empty (or null?) then there is no need to search (and doing
		// so will lead
		// to a SQLException)
		if (accessIds == null || accessIds.isEmpty()) {
			return new ArrayList<Study>();
		}

		List<Study> s = studyDao.getByIds(accessIds);
		return s;
	}

	public Set<SubmissionType> getStudySubmissionTypes(Long id) {
		Set<Long> ids = new HashSet<Long>();
		ids.add(id);
		Map<Long, Set<SubmissionType>> types = studyDao.getStudySumissionTypes(ids);
		return types.get(id);
	}

	public Map<Long, Set<SubmissionType>> getStudySubmissionTypes(Set<Long> ids) {
		return studyDao.getStudySumissionTypes(ids);
	}

	public List<Dataset> searchDatasets(String key, List<String> searchColumns, Account account, Long statusNow,
			Long statusRequest, Long ownerId, PaginationData pageData) {

		final long MINE = 0L;

		Set<Long> ids = null;
		if (ownerId != null) {
			if (ownerId != null && ownerId.equals(MINE)) {
				ids = accountManager.listUserAccess(account, EntityType.DATASET, PermissionType.OWNER);
			} else {
				ids = accountManager.listUserAccess(account, EntityType.DATASET, PermissionType.READ);
			}
		}

		// Logic for determining the what the status enums should be
		DatasetStatus requestStatus = null;
		DatasetStatus currentStatus = null;
		if (statusNow != null) {
			currentStatus = DatasetStatus.getById(statusNow);
		}
		if (statusNow != null) {
			requestStatus = DatasetStatus.getById(statusRequest);
		}

		pageData.setNumSearchResults(datasetDao.countAll());
		return datasetDao.search(ids, key, searchColumns, currentStatus, requestStatus, pageData);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Dataset> searchDatasets(String key, Account account, Long filterId, Long ownerId,
			PaginationData pageData) {

		// symbolic constants for filter types mine and all
		final long MINE = 0L;
		final long FILTERALL = -1L;

		Set<Long> ids = null;
		if (ownerId != null) {
			if (ownerId != null && ownerId.equals(MINE)) {
				ids = accountManager.listUserAccess(account, EntityType.DATASET, PermissionType.OWNER);
			} else {
				ids = accountManager.listUserAccess(account, EntityType.DATASET, PermissionType.READ);
			}
		}

		// Logic for determining the what the status enums should be
		boolean requested = false;
		DatasetStatus datasetStatus = null;
		if (filterId != null && filterId != FILTERALL) {
			if (filterId >= 10) {
				filterId -= 10;
				requested = true;
			}
			datasetStatus = DatasetStatus.getById(filterId);
		}

		pageData.setNumSearchResults(datasetDao.countAll());
		return datasetDao.search(ids, key, datasetStatus, requested, pageData);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DataStoreInfo> searchDataStores(String key, Long typeId, Long federatedId, Long archivedId,
			PaginationData pageData) {

		Boolean federated = null;
		Boolean archived = null;
		Boolean tabular = null;

		if (federatedId != null && federatedId == 1) {
			federated = true;
		}
		if (federatedId != null && federatedId == 0) {
			federated = false;
		}
		if (archivedId != null && archivedId == 1) {
			archived = true;
		}
		if (archivedId != null && archivedId == 0) {
			archived = false;
		}
		if (typeId != null && typeId == 1) {
			tabular = true;
		}
		if (typeId != null && typeId == 0) {
			tabular = false;
		}

		return dataStoreInfoDao.search(key, tabular, federated, archived, pageData);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean validateUniqueClinicalTrialId(String newId, List<String> clinicalTrialList) {

		if (clinicalTrialList == null) {
			return true;
		}

		for (String clinicalTrialId : clinicalTrialList) {
			if (newId.equalsIgnoreCase(clinicalTrialId)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean validateUniqueGrantId(String newId, List<String> grantList) {

		if (grantList == null) {
			return true;
		}

		for (String grantId : grantList) {
			if (newId.equalsIgnoreCase(grantId)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Study saveStudy(Account account, Study currentStudy) {

		boolean newStudy = false;

		// Get write permission for a pre-existing data entry
		if (currentStudy.getId() != null) {
			if (!accountManager.getAccess(account, EntityType.STUDY, currentStudy.getId(), PermissionType.WRITE))
				throw new RuntimeException("Account does not have access to study.");

			if (currentStudy.getIsPrivate())
				accountManager.removeFromPublicStudy(account, currentStudy);
		} else {
			newStudy = true;
			currentStudy.setDateCreated(new Date());

			// If the user is a study admin, skip requested and set status straight to
			// public
			if (accountManager.hasRole(account, RoleType.ROLE_STUDY_ADMIN)) {
				currentStudy.setStudyStatus(StudyStatus.PUBLIC);
			}
		}

		currentStudy = studyDao.save(currentStudy);

		// Entity Register the new Study
		if (newStudy) {
			String prefix = modulesConstants.getModulesOrgName(Long.valueOf(account.getDiseaseKey()))
					+ ModelConstants.PREFIX_STUDY;
			String prefixedId = prependZeroesToId(prefix, currentStudy.getId());

			accountManager.registerEntity(account, EntityType.STUDY, currentStudy.getId(), PermissionType.OWNER);
			currentStudy.setPrefixedId(prefixedId);
			currentStudy = studyDao.save(currentStudy);
		}

		// If the study is public, then add it to the public group
		// Note: This function saveStudy(Account, Study) will only properly register the
		// group into
		// public study on the
		// creation of a new study
		// BY AN ADMIN. Studies created by non-admin users must be made public by
		// StudyAction.setVisibilty to be added
		// to the public study group. (poor design)
		if (newStudy && StudyStatus.PUBLIC.equals(currentStudy.getStudyStatus())) {
			accountManager.registerEntity(accountManager.getPermissionGroup(ServiceConstants.PUBLIC_STUDY),
					EntityType.STUDY, currentStudy.getId(), PermissionType.READ);
		}

		return currentStudy;
	}

	/**
	 * Prepends 0s in front of the ID until there are 7 digits
	 * 
	 * @param prefix
	 * @param id
	 * @return
	 */
	private String prependZeroesToId(String prefix, Long id) {

		int leadingZeroes = 7 - id.toString().length();

		for (int i = 0; i < leadingZeroes; i++)
			prefix += ModelConstants.PREFIX_LEAD_NUM;

		prefix += id;

		return prefix;
	}

	/**
	 * {@inheritDoc}
	 */
	public Study getStudy(Account account, Long id) throws UserPermissionException {

		if (accountManager.getAccess(account, EntityType.STUDY, id, PermissionType.READ)) {
			return studyDao.getStudyWithChildren(id);
		} else {
			throw new UserPermissionException(ServiceConstants.WRITE_ACCESS_DENIED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Study getStudyByPrefixedId(Account account, String studyId) throws UserPermissionException {

		Study study = studyDao.getByPrefixedId(studyId);

		if (study != null && !accountManager.getAccess(account, EntityType.STUDY, study.getId(), PermissionType.READ)) {
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}

		return study;
	}

	/**
	 * {@inheritDoc}
	 */
	public Study getStudyByPrefixedIdExcludingDataset(Account account, String studyId) throws UserPermissionException {

		Study study = studyDao.getByPrefixedIdExcludingDataset(studyId);

		if (study != null && !accountManager.getAccess(account, EntityType.STUDY, study.getId(), PermissionType.READ)) {
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}

		return study;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Dataset> getDatasetById(Set<Long> datasetId) {

		List<Dataset> dataset = datasetDao.getByIds(datasetId);
		return dataset;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateDatasets(Set<Dataset> datasets) {
		for (Dataset dataset : datasets) {
			datasetDao.save(dataset);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Dataset> getStudyDataset(Set<Long> studyIds) {
		List<Long> ids = datasetDao.getDatasetIdsByStudyIds(studyIds);

		Set<Long> datasetIds = new HashSet<Long>(ids);
		List<Dataset> datasets = datasetDao.getByIds(datasetIds);
		return datasets;
	}

	/**
	 * {@inheritDoc}
	 */
	public Study getStudyByPrefixedId(String prefixedId) {
		Study study = studyDao.getByPrefixedId(prefixedId);
		return study;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteStudy(Account account, Study study, boolean admin) {

		if (admin || accountManager.getAccess(account, EntityType.STUDY, study.getId(), PermissionType.OWNER)) {
			try {
				studyDao.remove(study.getId());
				accountManager.unregisterEntity(EntityType.STUDY, study.getId());

				return true;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Long parsePrefixId(String prefix, String prefixId) {

		Pattern pattern = Pattern.compile(prefix);
		Matcher matcher = pattern.matcher(prefixId.trim());

		return Long.valueOf(matcher.matches() ? matcher.group(1) : prefixId);
	}

	/**
	 * @throws IOException {@inheritDoc}
	 */
	public boolean validateClinicalTrialId(String clinicalTrialId) throws IOException {

		if (clinicalTrialId == null || clinicalTrialId.trim().length() <= 0
				|| !clinicalTrialId.toUpperCase().startsWith(ServiceConstants.CLINICAL_TRIAL_PREFIX))
			return false;

		try {
			// example URL: http://clinicaltrials.gov/show/NCT00000300?displayxml=true
			String url = "https://clinicaltrials.gov/show/" + clinicalTrialId + "?displayxml=true";

			URL xmlUrl = new URL(url);
			URLConnection conn = xmlUrl.openConnection();
			conn.setConnectTimeout(10000); // 10sec
			conn.setReadTimeout(10000);

			InputStream in = conn.getInputStream(); // throws FileNotFoundException if clinical
													// trial site is not avail
			// openConnection().getInputStream()

			in.close();

			return true;
		} catch (FileNotFoundException fnfe) { // invalid clinical trial ID
			return false;
		} catch (IOException ioe) {
			throw new IOException("-------- clinical trial exception (IOException) message is: " + ioe.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ClinicalStudy getClinicalStudy(String clinicalTrialId) throws IOException, JAXBException {

		if (clinicalTrialId == null || clinicalTrialId.trim().length() <= 0
				|| !clinicalTrialId.toUpperCase().startsWith(ServiceConstants.CLINICAL_TRIAL_PREFIX))
			return null;

		try {
			// example URL: http://clinicaltrials.gov/show/NCT00000300?displayxml=true
			String url = "https://clinicaltrials.gov/show/" + clinicalTrialId + "?displayxml=true";

			URL xmlUrl = new URL(url);
			URLConnection conn = xmlUrl.openConnection();
			conn.setConnectTimeout(10000); // 10sec
			conn.setReadTimeout(10000);

			InputStream in = conn.getInputStream(); // throws FileNotFoundException if clinical
													// trial site is not avail
			ClassLoader cl = gov.nih.tbi.commons.model.ObjectFactory.class.getClassLoader();
			JAXBContext jc = JAXBContext.newInstance("gov.nih.tbi.commons.model", cl);
			Unmarshaller um = jc.createUnmarshaller();
			ClinicalStudy clinicalStudy = (ClinicalStudy) um.unmarshal(in);
			in.close();
			return clinicalStudy;
		} catch (FileNotFoundException fnfe) { // invalid clinical trial ID
			throw new FileNotFoundException(fnfe.getMessage());
		} catch (IOException ioe) {
			throw new IOException("-------- clinical trial exception (IOException) message is: " + ioe.getMessage());
		} catch (JAXBException e) {
			throw new JAXBException(e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Dataset getDataset(Long datasetId) {

		Dataset d = datasetDao.get(datasetId);
		return d;
	}

	/**
	 * {@inheritDoc}
	 */
	public BasicDataset getBasicDataset(Long datasetId) {

		return basicDatasetDao.get(datasetId);
	}

	/**
	 * @throws SQLException {@inheritDoc}
	 */
	public void deleteDataset(Dataset dataset, User user, String comment, EventLog eventLog) throws SQLException {

		if (countAccessRecords(dataset.getId()) > 0) {
			return;
		}

		if (dataset.hasData()) {

			this.scheduleDatasetDataDeletion(dataset, user);
		}

		eventLog = setValuesToEventLog(dataset.getId(), EntityType.DATASET, EventType.DATASET_DELETION, user, comment,
				eventLog, DatasetStatus.DELETED.getId().toString(), dataset.getDatasetStatus().getId().toString());

		eventLog.setNewValue(DatasetStatus.DELETED.getId().toString());

		this.eventLogDao.save(eventLog);

		dataset.setDatasetStatus(DatasetStatus.DELETED);
		dataset.setDatasetRequestStatus(null);
		this.datasetDao.save(dataset);
	}

	public EventLog getDatasetLatestEvent(Dataset dataset) {

		long datasetID = dataset.getId();

		EventLog result = this.eventLogDao.findLatestOfTypeForEntityID(datasetID, EventType.DATASET_DELETION);

		return result;
	}

	private void scheduleDatasetDataDeletion(Dataset dataset, User user) {

		Long datasetID = dataset.getId();

		ScheduledJob datasetDeleteJob = new ScheduledJob(JobType.DATASET_DATA_DELETION, user);

		datasetDeleteJob = this.scheduledJobDao.save(datasetDeleteJob);

		datasetDeleteJob.addParameter(JobParameterKey.DATASET_ID, datasetID);

		this.scheduledJobDao.save(datasetDeleteJob);
	}

	public String buildSubmissionJoinDeletionQuery(String submissionId) {

		StringBuilder builder = new StringBuilder(CoreConstants.DELETE1);
		builder.append(ServiceConstants.TABLE_SUBMISSION_JOIN);
		builder.append(CoreConstants.DELETE2);
		builder.append(CoreConstants.ID);
		builder.append(CoreConstants.EQUALS);
		builder.append(submissionId);
		builder.append(CoreConstants.SEMICOLON);
		return builder.toString();
	}

	/**
	 * Builds the query to delete from datastore via a submission ID
	 * 
	 * @param tableName
	 * @param submissionId
	 * @return
	 */
	public String buildDataStoreDeletionQuery(String tableName, String submissionId) {

		StringBuilder builder = new StringBuilder(CoreConstants.DELETE1);
		builder.append(tableName);
		builder.append(CoreConstants.DELETE2);
		builder.append(ServiceConstants.COLUMN_SUBMISSION_JOIN);
		builder.append(CoreConstants.EQUALS);
		builder.append(submissionId);
		builder.append(CoreConstants.SEMICOLON);
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Dataset saveDataset(Account account, Dataset currentDataset) {

		boolean newDataset = false;

		if (currentDataset.getId() == null) {
			newDataset = true;
		}

		currentDataset = datasetDao.save(currentDataset);

		if (newDataset) {
			String prefix = modulesConstants.getModulesOrgName() + ModelConstants.PREFIX_DATASET;
			String prefixedId = prependZeroesToId(prefix, currentDataset.getId());

			accountManager.registerEntity(account, EntityType.DATASET, currentDataset.getId(), PermissionType.OWNER);
			currentDataset.setPrefixedId(prefixedId);
			currentDataset = datasetDao.save(currentDataset);
		}

		// update permission group
		if (DatasetStatus.SHARED.equals(currentDataset.getDatasetStatus())) { // add dataset to
																				// public study if
																				// shared
			PermissionGroup publicStudy = permissionGroupDao.getByName(ServiceConstants.PUBLIC_STUDY);
			if (publicStudy != null) {
				accountManager.registerEntity(publicStudy, EntityType.DATASET, currentDataset.getId(),
						PermissionType.READ);
			}
		} else if (DatasetStatus.PRIVATE.equals(currentDataset.getDatasetStatus())
				|| DatasetStatus.ARCHIVED.equals(currentDataset.getDatasetStatus())) { // remove
																						// dataset
																						// from
																						// public
																						// study if
																						// private
			accountManager.removeEntityFromPermissionGroup(account, ServiceConstants.PUBLIC_STUDY, EntityType.DATASET,
					currentDataset.getId());
		}

		return currentDataset;
	}

	/**
	 * {@inheritDoc}
	 */
	public Study getStudy(Long id) {

		return studyDao.get(id);
	}

	public Study getStudyExcludingDatasets(Long id) {
		return studyDao.getStudyWithOutDatasets(id);
	}

	public Study getPublicStudySearchById(Long id) {
		return studyDao.getPublicStudySearchById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public Study getStudyByName(String studyName) {

		return studyDao.getStudyByName(studyName);
	}

	public Study getStudyWithDatasetsByName(String studyTitle) {
		return studyDao.getStudyWithDatasetsByName(studyTitle);
	}

	public List<BasicDataset> getBasicDatasetsByIds(Set<Long> datasets) {
		return basicDatasetDao.getByIds(datasets);
	}

	public List<DownloadFileDataset> getDownloadFileDatasetsByIds(Set<Long> ids) {
		return downloadFileDatasetDao.getByIds(ids);
	}

	public List<Dataset> getDatasetsByStudy(Study study) {
		return datasetDao.getDatasetsByStudy(study);
	}

	public BasicStudy getBasicStudyByPrefixId(String prefixId) {
		return basicStudyDao.getBasicStudyByPrefixId(prefixId);
	}

	public BasicStudy getBasicStudyById(Long studyId) {
		return basicStudyDao.get(studyId);
	}

	public List<BasicStudySearch> getPublicSiteSearchBasicStudies() {
		return basicStudyDao.getPublicSiteSearchBasicStudies();
	}

	public Map<Long, BasicStudySearch> getPublicSiteSearchBasicStudiesMap() {
		List<BasicStudySearch> results = basicStudyDao.getPublicSiteSearchBasicStudies();
		Map<Long, BasicStudySearch> studies = new HashMap<Long, BasicStudySearch>();
		for (BasicStudySearch b : results) {
			studies.put(b.getId().longValue(), b);
		}
		return studies;
	}

	/**
	 * {@inheritDoc}
	 */
	public BasicStudy getBasicStudyByName(String studyName) {

		return studyDao.getBasicStudyByName(studyName);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Dataset> getUploadingDataset(String userName) {

		User user = accountManager.getAccountByUserName(userName).getUser();
		return datasetDao.getUploadingDataset(user);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<DatasetFile> getDatasetFileFromDatasetId(Long id) {

		Dataset dataset = datasetDao.getDatasetWithChildren(id);
		return dataset.getDatasetFileSet();
	}

	/**
	 * {@inheritDoc}
	 */
	public Dataset getDatasetWithDatasetFiles(Long id) {

		return datasetDao.getDatasetWithFiles(id);

	}

	public List<DatasetFile> getDatasetFiles(Dataset ds) {
		return datasetFileDao.getByDatasetId(ds.getId());
	}

	public BigDecimal getTotalDatasetFilesSize(Long datasetId) {
		return datasetFileDao.getTotalFilesSizeByDatasetId(datasetId);
	}

	public HashSet<SubmissionType> getDatasetWithSubmissionTypes(long datasetId) {

		List<Integer> submissionTypesList = datasetDao.getSubmissionTypeseByDatasetId(datasetId);
		HashSet<SubmissionType> submissionTypes = new HashSet<SubmissionType>();

		for (Integer id : submissionTypesList) {
			SubmissionType r = SubmissionType.values()[id];
			submissionTypes.add(r);
		}
		return submissionTypes;
	}

	/**
	 * {@inheritDoc}
	 */
	public BasicStudy getStudyFromDatasetFile(Long id) {

		Long studyId = studyDao.getStudyIdByDatasetFileId(id);
		BasicStudy basicStudy = basicStudyDao.get(studyId);
		return basicStudy;
	}

	/**
	 * 
	 * This function is called by both a webstart that still uses SOAP services and
	 * a method in Portal, so the proxyTicket may be null. Set's the dataset file to
	 * complete. Used in the Upload Manager. SOAP ok.
	 * 
	 * {@inheritDoc}
	 * 
	 * @throws DataLoaderException
	 */
	public void loadDataFromDataset(Account account, Long datasetId, String proxyTicket) throws DataLoaderException {
		dataLoaderManager.initializeDatasetFile(account, datasetId, proxyTicket);
	}

	/**
	 * @throws MalformedURLException {@inheritDoc}
	 */

	/**
	 * Set's the dataset file to complete. Used in the Upload Manager. SOAP ok.
	 * 
	 * Matt Green's Note:
	 * 
	 * loadDataFromDataset(account, datasetFile.getDataset().getId(),null); This
	 * line might create a problem in the future, because the null might cause
	 * problems if initializeDatasetFile is called.
	 * 
	 */
	public void setDatasetFileToComplete(Long id, Account account) throws MalformedURLException {

		DatasetFile datasetFile = datasetFileDao.get(id);

		if (accountManager.getAccess(account, EntityType.DATASET, datasetFile.getDataset(), PermissionType.WRITE)) {
			datasetFile.setDatasetFileStatus(DatasetFileStatus.COMPLETE);
			datasetFileDao.save(datasetFile);
			logger.info("Setting datasetFile with id " + id + " to complete : calling loadDataFromDataset ");
			Dataset dataset = getDatasetOnly(datasetFile.getDataset());

			if ((getDatasetPendingFileCount(datasetFile.getDataset()).longValue() == 0)
					&& (DatasetStatus.UPLOADING.equals(dataset.getDatasetStatus())
							|| DatasetStatus.ERROR.equals(dataset.getDatasetStatus()))) {
				try {
					loadDataFromDataset(account, datasetFile.getDataset(), null);
				} catch (DataLoaderException e) {
					logger.error("Error while storing dataset with id " + dataset.getId() + ": " + e.getMessage(), e);
					// change dataset status from loading to error
					dataset.setDatasetStatus(DatasetStatus.ERROR);
					List<String> dataDetails = new ArrayList<>();
					dataDetails.add(dataset.getDatasetDetail());
					sendDataErrorLoadNotification(dataDetails);
					// Remove any information that has been inserted
					User user = account.getUser();
					this.scheduleDatasetDataDeletion(dataset, user);
					saveDataset(account, dataset);
				}
			}
		} else {
			throw new AccessDeniedException("User does not have write permission for this dataset");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean validateStudyTitle(Long id, String title) {

		for (Study currStudy : studyDao.getAll()) {
			if (currStudy.getTitle().equals(title) && id == null) {
				return false;
			} else if (!currStudy.getId().equals(id)
					&& currStudy.getTitle().toLowerCase().equals(title.toLowerCase())) {
				return false;
			}
		}

		return true;
	}

	public Dataset processSubmissionTicket(SubmissionTicket submissionTicket, String localPath, String serverPath,
			String userName, String studyName, String datasetName, boolean isDerived, boolean isProformsSubmission,
			Date submitDate, Long administeredFormId, boolean isSubjectSubmitted) {

		Account currentAccount = accountManager.getAccountByUserName(userName);
		User currentUser = currentAccount.getUser();

		Dataset dataset = new Dataset();
		dataset.setDatasetStatus(DatasetStatus.UPLOADING);
		dataset.setName(datasetName);
		dataset.setSubmitter(currentUser);
		dataset.setStudy(getStudyByName(studyName));
		dataset.setIsDerived(isDerived);
		dataset.setAdministeredFormId(administeredFormId);
		dataset.setIsSubjectSubmitted(isSubjectSubmitted);

		if (submitDate == null) {
			dataset.setSubmitDate(new Date());
		} else {
			dataset.setSubmitDate(submitDate);
		}
		dataset.setIsProformsSubmission(isProformsSubmission);
		dataset = saveDataset(currentAccount, dataset);

		// Adds the data file into the dataset
		DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);
		UserFile newUserFile = new UserFile();

		newUserFile.setName(datasetName + ".xml");

		newUserFile.setDatafileEndpointInfo(info);
		newUserFile.setPath(serverPath);
		for (SubmissionPackage sp : submissionTicket.getSubmissionPackages()) {
			if (sp.getName().equals(datasetName)) {
				newUserFile.setSize(sp.getDataFileBytes());

				newUserFile.setUserId(currentUser.getId());
				newUserFile = saveUserFile(newUserFile);
				DatasetFile newDatasetFile = new DatasetFile();
				newDatasetFile.setIsQueryable(false);
				newDatasetFile.setFileType(SubmissionType.DATA_FILE);
				newDatasetFile.setDatasetFileStatus(DatasetFileStatus.PENDING);
				newDatasetFile.setLocalLocation(localPath + newUserFile.getName());
				newDatasetFile.setUserFile(newUserFile);
				newDatasetFile.setDataset(dataset.getId());
				dataset.getDatasetFileSet().add(newDatasetFile);

				for (SubmissionDataFile dataFile : sp.getDatasets()) {
					UserFile userFile = new UserFile();
					userFile.setName(dataFile.getPath().replace("\\", "/")
							.substring(dataFile.getPath().replace("\\", "/").lastIndexOf("/") + 1));
					userFile.setDatafileEndpointInfo(info);
					userFile.setPath(serverPath);
					userFile.setSize(dataFile.getBytes());
					userFile.setUserId(currentUser.getId());
					userFile = saveUserFile(userFile);
					DatasetFile datasetFile = new DatasetFile();
					datasetFile.setIsQueryable(true);
					datasetFile.setFileType(dataFile.getType());
					datasetFile.setDatasetFileStatus(DatasetFileStatus.PENDING);
					datasetFile.setLocalLocation(dataFile.getPath());
					datasetFile.setUserFile(userFile);
					datasetFile.setDataset(dataset.getId());
					dataset.getDatasetFileSet().add(datasetFile);
					dataset.getSubmissionTypes().add(dataFile.getType()); // Add type to dataset
				}

				for (SubmissionDataFile dataFile : sp.getAssociatedFiles()) {
					UserFile userFile = new UserFile();
					userFile.setName(dataFile.getName());
					userFile.setDatafileEndpointInfo(info);
					userFile.setPath(serverPath);
					userFile.setSize(dataFile.getBytes());
					userFile.setUserId(currentUser.getId());
					userFile = saveUserFile(userFile);
					DatasetFile datasetFile = new DatasetFile();
					datasetFile.setIsQueryable(false);
					datasetFile.setFileType(dataFile.getType());
					datasetFile.setDatasetFileStatus(DatasetFileStatus.PENDING);
					datasetFile.setLocalLocation(dataFile.getPath());
					datasetFile.setUserFile(userFile);
					datasetFile.setDataset(dataset.getId());
					dataset.getDatasetFileSet().add(datasetFile);
				}
			}
		}

		return saveDataset(currentAccount, dataset);
	}

	/**
	 * {@inheritDoc}
	 */
	public Long getNumStudiesWithStatus(Long statusId) {

		if (statusId == null) {
			return null;
		}
		return studyDao.getStatusCount(StudyStatus.getById(statusId));
	}

	/**
	 * {@inheritDoc}
	 */
	public Long getNumDatasetsWithStatus(Long statusId, boolean requested) {

		if (statusId == null) {
			return null;
		}
		return datasetDao.getStatusCount(DatasetStatus.getById(statusId), requested);
	}

	/**
	 * Saves the secure subject
	 * 
	 * @param subject
	 * @return
	 */
	public DatasetSubject saveDatasetSubject(DatasetSubject datasetSubject) {

		return datasetSubjectDao.save(datasetSubject);
	}

	/**
	 * @InheritDoc
	 */
	public List<? extends GuidJoinedData> getGuidJoinedDataList(Account account, String guid) {
		List<DatasetSubject> datasetSubjects = datasetSubjectDao.getDatasetSubjectListByGuid(guid);

		Iterator<DatasetSubject> datasetSubjectsIterator = datasetSubjects.iterator();

		// here, we are to remove all of the datasetSubjects that references datasets
		// the user does not have access to
		while (datasetSubjectsIterator.hasNext()) {
			DatasetSubject currentDatasetSubject = datasetSubjectsIterator.next();
			boolean hasAccess = accountManager.getDatasetAccess(account, currentDatasetSubject.getDataset(),
					PermissionType.READ);

			if (!hasAccess) {
				datasetSubjectsIterator.remove();
			}
		}

		return datasetSubjects;
	}

	/**
	 * {@inheritDoc}
	 */
	public Dataset getDatasetByName(String studyName, String datasetName) {

		return datasetDao.getDatasetByName(studyName, datasetName);
	}

	/**
	 * @throws SQLException {@inheritDoc}
	 */
	public void deleteDatasetByName(String studyName, String datasetNname, User user) {

		Dataset dataset = datasetDao.getDatasetByName(studyName, datasetNname);
		EventLog eventLog = new EventLog();
		if (dataset != null) {
			try {
				// this.deleteDataset(dataset);
				this.deleteDataset(dataset, user, "REST call", eventLog);
			} catch (SQLException e) {
				logger.error("There was an exception while deleting a dataset " + e.getMessage());
			}
		}
	}

	/**
	 * @param filePath
	 * @throws SQLException {@inheritDoc}
	 */
	public boolean deleteDatasetCascadeByName(String studyName, String datasetNname) {
		// delete the file folder from ftp file sever
		Dataset dataset = datasetDao.getDatasetByName(studyName, datasetNname);
		Study study = studyDao.getStudyByName(studyName);
		try {
			DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);
			SftpClient client = SftpClientManager.getClient(info);

			logger.info(" RepositoryManagerImpl :: deleteDatasetCascadeByName :: "
					+ ServiceConstants.REPO_SUBMISSION_UPLOAD_PATH + " : " + study.getPrefixedId() + " : "
					+ dataset.getDataName());

			client.deleteDatasetFolder(ServiceConstants.REPO_SUBMISSION_UPLOAD_PATH, study.getPrefixedId(),
					dataset.getDataName());
			logger.info("SFTP dataset deletion completed: " + dataset.getName());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("SFTP dataset deletion error: " + dataset.getName());
			return false;
		}
		if (dataset != null) {
			logger.info("Deleting dataset with id: " + dataset.getId());
			this.datasetDao.remove(dataset.getId());
		}
		// remove the files from the remove ftp sever
		return true;
	}

	/**
	 * @param filePath
	 * @throws SQLException {@inheritDoc}
	 */
	public boolean deleteDatasetFileByName(UserFile userFile) {
		try {
			DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);
			SftpClient client = SftpClientManager.getClient(info);
			logger.info(" RepositoryManagerImpl :: deleteDatasetFileByName deleting filePath :: " + userFile.getPath()
					+ " fileName : " + userFile.getName());
			client.delete(userFile.getPath(), userFile.getName());
			logger.info(" RepositoryManagerImpl :: deleteDatasetFileByName file deletion complete :: "
					+ userFile.getPath() + " fileName : " + userFile.getName());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("SFTP dataset deletion error: filePath -  " + userFile.getPath() + " fileName - "
					+ userFile.getName());
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	/**
	 * @throws UserPermissionException {@inheritDoc}
	 */
	public String getNumOfCompletedFiles(String datasetId, Account account)
			throws UserPermissionException, BadParameterException {

		Dataset dataset = getDatasetByPrefixedId(datasetId, account);
		int total = dataset.getDatasetFileSet().size();
		int i = 0;
		for (DatasetFile datasetFile : dataset.getDatasetFileSet()) {
			if (DatasetFileStatus.COMPLETE.equals(datasetFile.getDatasetFileStatus())) {
				i++;
			}
		}

		return ServiceConstants.EMPTY_STRING + i + "/" + total;
	}

	/**
	 * Returns semi-colon delimited list for a given list of permissible value
	 * descriptions
	 * 
	 * @param permissibleValues
	 * @return
	 */
	public String permissibleValueToCSVString(List<ValueRange> vrList) {

		StringBuffer pvBuffer = new StringBuffer();

		if (vrList != null && !vrList.isEmpty()) {

			for (ValueRange pv : vrList) {
				if (pv.getValueRange() == null) {
					throw new NullPointerException("Permissible value fields cannot be null");
				}

				pvBuffer.append(pv.getValueRange()).append(ServiceConstants.CSV_LIST_SEPARATER);
			}

			int lastDelimitorIndex = pvBuffer.length() - ServiceConstants.CSV_LIST_SEPARATER.length();
			pvBuffer.replace(lastDelimitorIndex, pvBuffer.length(), ServiceConstants.EMPTY_STRING);
		}

		return pvBuffer.toString();
	}

	/**
	 * Returns semi-colon delimited list for a given list of permissible values
	 * 
	 * @param permissibleValues
	 * @return
	 */
	private String permissibleValueDescriptionToCSVString(List<ValueRange> vrList) {

		StringBuffer pvBuffer = new StringBuffer();
		if (vrList != null && !vrList.isEmpty()) {

			for (ValueRange pv : vrList) {
				if (pv.getDescription() == null) {
					pv.setDescription(ServiceConstants.EMPTY_STRING);
				}

				pvBuffer.append(pv.getDescription()).append(ServiceConstants.CSV_LIST_SEPARATER);
			}

			int lastDelimitorIndex = pvBuffer.length() - ServiceConstants.CSV_LIST_SEPARATER.length();
			pvBuffer.replace(lastDelimitorIndex, pvBuffer.length(), ServiceConstants.EMPTY_STRING);
		}

		return pvBuffer.toString();
	}

	/**
	 * Returns semi-colon delimited list for a given list of permissible value
	 * output codes
	 * 
	 * @param permissibleValues
	 * @return
	 */
	private String permissibleValueOutputCodeToCSVString(List<ValueRange> vrList) {
		StringBuilder sb = new StringBuilder();
		if (vrList != null && !vrList.isEmpty()) {
			for (ValueRange pv : vrList) {
				sb.append(pv.getOutputCode() != null ? pv.getOutputCode() : ServiceConstants.EMPTY_STRING);
				sb.append(ServiceConstants.CSV_LIST_SEPARATER);
			}

			int lastDelimitorIndex = sb.length() - ServiceConstants.CSV_LIST_SEPARATER.length();
			sb.replace(lastDelimitorIndex, sb.length(), ServiceConstants.EMPTY_STRING);
		}

		return sb.toString();
	}

	private List<ValueRange> pvSorter(Set<ValueRange> permissibleValues) {
		List<ValueRange> vrList = new ArrayList<ValueRange>(permissibleValues);
		Collections.sort(vrList);
		return vrList;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addDatasetToDownloadQueue(Dataset dataset, Account account, List<FormStructure> fsList,
			Boolean sendEmail) {
		FileWriter writer = null;
		File pvFile = null;
		List<File> pvFiles = new ArrayList<File>();
		try {
			for (FormStructure fs : fsList) {
				pvFile = File.createTempFile(fs.getShortName() + "_pv_mapping", ".csv");
				writer = new FileWriter(pvFile, false);
				writer.append(ServiceConstants.NAME_READABLE);
				writer.append(ServiceConstants.COMMA);
				writer.append(ServiceConstants.TITLE_READABLE);
				writer.append(ServiceConstants.COMMA);
				writer.append(ServiceConstants.SHORT_DESCRIPTION_READABLE);
				writer.append(ServiceConstants.COMMA);
				writer.append(ServiceConstants.PERMISSIBLE_VALUES_READABLE);
				writer.append(ServiceConstants.COMMA);
				writer.append(ServiceConstants.PERMISSIBLE_VALUES_DESCRIPTION_READABLE);
				writer.append(ServiceConstants.COMMA);
				writer.append(ServiceConstants.PERMISSIBLE_VALUES_OUTPUT_CODES_READABLE);

				// Map<String, DataElement> deMap = fs.getDataElements();
				// Map<String, DataElement> deMap = sortByComparator(uSorteddeMap);
				List<DataElement> deList = new ArrayList<DataElement>(fs.getDataElements().values());
				Collections.sort(deList, new Comparator<DataElement>() {
					public int compare(DataElement de1, DataElement de2) {
						return de1.getName().compareTo(de2.getName());
					}
				});
				for (DataElement de : deList) {
					if (!de.getRestrictions().equals(InputRestrictions.FREE_FORM)) {

						List<ValueRange> vrList = this.pvSorter(de.getValueRangeList());
						writer.append(ServiceConstants.NEWLINE);
						writer.append(de.getName());
						writer.append(ServiceConstants.COMMA);
						writer.append(ServiceConstants.QUOTE + de.getTitle() + ServiceConstants.QUOTE);
						writer.append(ServiceConstants.COMMA);
						writer.append(ServiceConstants.QUOTE + String.valueOf(de.getShortDescription())
								+ ServiceConstants.QUOTE);

						writer.append(ServiceConstants.COMMA);
						writer.append(permissibleValueToCSVString(vrList));

						writer.append(ServiceConstants.COMMA);
						writer.append(ServiceConstants.QUOTE + permissibleValueDescriptionToCSVString(vrList)
								+ ServiceConstants.QUOTE);

						writer.append(ServiceConstants.COMMA);
						writer.append(permissibleValueOutputCodeToCSVString(vrList));
					}

				}		
				pvFiles.add(pvFile);
				writer.flush();

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);
		List<UserFile> userFiles = new ArrayList<>();
		for(File pvFileItem: pvFiles) {
			UserFile newUserFile = new UserFile();
			String trimmedPvFileName = pvFileItem.getName();
			trimmedPvFileName = trimmedPvFileName.substring(0, trimmedPvFileName.indexOf(PV_MAPPING)) + PV_MAPPING_FILE_NAME_SUFFIX;
			newUserFile.setName(trimmedPvFileName);
			newUserFile.setDescription(ServiceConstants.PV_FS_DE_MAPPING_DESCRIPTION_TEXT);
			newUserFile.setDatafileEndpointInfo(info);
			newUserFile.setSize(Long.valueOf(pvFileItem.length()));
			newUserFile.setUploadedDate(new Date());
			newUserFile.setPath(ServiceConstants.REPO_SUBMISSION_UPLOAD_PATH + dataset.getStudy().getTitle()
					+ ServiceConstants.FILE_SEPARATER + dataset.getName() + ServiceConstants.FILE_SEPARATER);
			try {
				SftpClient client = SftpClientManager.getClient(info);
				client.upload(pvFileItem, newUserFile.getPath(), trimmedPvFileName);
			} catch (JSchException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
				newUserFile = saveUserFile(newUserFile);
				userFiles.add(newUserFile);
		}

		DownloadFileDataset downloadFileDataset = new DownloadFileDataset(dataset);

		DownloadPackage downloadPackage = new DownloadPackage();
		downloadPackage.setOrigin(DownloadPackageOrigin.DATASET);
		Date dateAdded = new Date();
		downloadPackage.setDateAdded(dateAdded);
		downloadPackage.setName(dataset.getName() + "_" + dataset.getStudy().getId());
		downloadPackage.setUser(account.getUser());

		for(UserFile userFile: userFiles) {
			DatasetDownloadFile newDatasetDownloadFile = new DatasetDownloadFile();
			newDatasetDownloadFile.setDownloadPackage(downloadPackage);
			newDatasetDownloadFile.setDataset(downloadFileDataset);
			newDatasetDownloadFile.setUserFile(userFile);
			newDatasetDownloadFile.setType(SubmissionType.DATA_FILE);
			downloadPackage.addDownloadable(newDatasetDownloadFile);
		}
		List<DatasetFile> dataFile = getDatasetFiles(dataset.getId());
		if (dataFile != null) {
			for (DatasetFile dsFile : dataFile) {
				DatasetDownloadFile datasetDownloadFile = new DatasetDownloadFile();
				datasetDownloadFile.setDownloadPackage(downloadPackage);
				datasetDownloadFile.setDataset(downloadFileDataset);
				datasetDownloadFile.setUserFile(dsFile.getUserFile());
				datasetDownloadFile.setType(dsFile.getFileType());
				downloadPackage.addDownloadable(datasetDownloadFile);
			}
		}

		downloadPackage = downloadPackageDao.save(downloadPackage);

		if (sendEmail) {
			sendDownloadAddNotification(account, downloadPackage);
		}

		// if the save was successful, then add an access Record
		if (downloadPackage != null && downloadPackage.getId() != null) {
			BasicDataset bd = basicDatasetDao.get(dataset.getId());
			createAccessRecord(bd, account, DataSource.REPOSITORY);
		} else {
			logger.error(
					"Dataset " + dataset.getId() + " was not added to " + account.getUserName() + "'s download queue.");
		}
	}

	public void deleteDownloadPackagesByIds(List<Long> ids) {
		downloadPackageDao.deleteByIds(ids);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteDownloadableById(Long id) {
		downloadableDao.remove(id);
		downloadPackageDao.removeEmptyPackages();
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteDownloadableByIds(List<Long> ids) {
		downloadableDao.deleteByIds(ids);
		downloadPackageDao.removeEmptyPackages();

	}

	/**
	 * {@inheritDoc}
	 */
	public Dataset getDatasetByPrefixedId(String prefixID, Account account)
			throws UserPermissionException, BadParameterException {

		if (prefixID == null) {
			throw new BadParameterException(QueryParameter.DATASET_PREFIXED_ID, null);
		}

		Dataset result = datasetDao.getByPrefixedIdWithoutSubjects(prefixID);

		if (result == null) {
			throw new BadParameterException(QueryParameter.DATASET_PREFIXED_ID, prefixID);
		}

		Study study = result.getStudy();
		Long studyID = study.getId();
		boolean hasAccess = accountManager.getAccess(account, EntityType.STUDY, studyID, PermissionType.READ);

		if (!hasAccess) {
			throw new UserPermissionException(ServiceConstants.WRITE_ACCESS_DENIED);
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public Dataset getDatasetByPrefixedIdWithStudyInfo(String prefixID, Account account)
			throws UserPermissionException, BadParameterException {

		if (prefixID == null) {
			throw new BadParameterException(QueryParameter.DATASET_PREFIXED_ID, null);
		}

		Dataset result = datasetDao.getByPrefixedIdWithStudyInfo(prefixID);

		if (result == null) {
			throw new BadParameterException(QueryParameter.DATASET_PREFIXED_ID, prefixID);
		}

		Study study = result.getStudy();
		Long studyID = study.getId();
		boolean hasAccess = accountManager.getAccess(account, EntityType.STUDY, studyID, PermissionType.READ);

		if (!hasAccess) {
			throw new UserPermissionException(ServiceConstants.WRITE_ACCESS_DENIED);
		}

		return result;
	}

	@Override
	public List<Study> getStudyList() {

		// NOTE: THIS IS TEMPORARY AND WILL BE REMOVED ONCE DATA TRANSFER TESTING IS
		// COMPLETE
		return studyDao.getAll();
	}

	/**
	 * Check to see if a Dataset exists given the name
	 */
	public Boolean doesDatasetExist(String studyName, String datasetName) {

		Dataset dataset = datasetDao.getDatasetByName(studyName, datasetName);
		if (dataset != null)
			return true;
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public AccessRecord createAccessRecord(BasicDataset dataset, Account account, DataSource dataSource,
			Long recordCount, Date queueDate) {
		AccessRecord ar = new AccessRecord(dataset, account, queueDate, recordCount, dataSource);
		ar = accessRecordDao.save(ar);
		logger.debug("New AccessRecord object saved: " + ar.toString());
		return ar;
	}

	/**
	 * {@inheritDoc}
	 */
	public AccessRecord createAccessRecord(BasicDataset dataset, Account account, DataSource dataSource,
			Long recordCount) {
		Date queueDate = new Date();
		return createAccessRecord(dataset, account, dataSource, recordCount, queueDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public AccessRecord createAccessRecord(BasicDataset dataset, Account account, DataSource dataSource) {
		return createAccessRecord(dataset, account, dataSource, getDatasetRecordCount(dataset.getId()));
	}

	/**
	 * {@inheritDoc}
	 */
	public Long getDatasetRecordCount(Long datasetId) {
		return submissionRecordJoinDao.getNumRecordsInDataset(datasetId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AccessRecord> getAccessRecords(Study study, String startDateStr, String endDateStr,
			PaginationData pageData) {
		Date startDate = null, endDate = null;
		if (startDateStr != null) {
			startDate = BRICSTimeDateUtil.stringToDate(startDateStr);
		}
		if (endDateStr != null) {
			endDate = BRICSTimeDateUtil.stringToDate(endDateStr);
		}
		// swap dates if dates are not in order
		if (startDate != null && endDate != null && startDate.after(endDate)) {
			Date temp = startDate;
			startDate = endDate;
			endDate = temp;
		}

		if (study == null) {
			return accessRecordDao.getAll(Long.MIN_VALUE, startDate, endDate, pageData);
		}
		return accessRecordDao.getAll(study.getId(), startDate, endDate, pageData);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AccessRecord> getAccessRecordsByStudyList(Set<Long> studyIdList, String startDateStr, String endDateStr,
			PaginationData pageData) {
		Date startDate = null, endDate = null;
		if (startDateStr != null) {
			startDate = BRICSTimeDateUtil.stringToDate(startDateStr);
		}
		if (endDateStr != null) {
			endDate = BRICSTimeDateUtil.stringToDate(endDateStr);
		}
		// swap dates if dates are not in order
		if (startDate != null && endDate != null && startDate.after(endDate)) {
			Date temp = startDate;
			startDate = endDate;
			endDate = temp;
		}
		return accessRecordDao.getByStudyIdList(new ArrayList(studyIdList), startDate, endDate, pageData);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<VisualizationAccessRecord> getAccessRecordsPerUser() {
		Date startDate = null, endDate = null;
		return visualizationAccessRecordDao.getAll(Long.MIN_VALUE, startDate, endDate, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<VisualizationAccessData> getAllAccessRecordsPerUser() {
		return visualizationAccessRecordDao.getAllAccessRecords();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AccessRecord> searchAccessRecords(Study study, String searchText, Long daysOld,
			PaginationData pageData) {

		// Create arguments for searchAccessRecords. Will be populated based on
		// searchText
		// String dsPrefixId = null;
		String dsId = null;
		Long recCount = null;
		String dsName = null;
		DatasetStatus status = null;
		Long dataSource = null;
		String username = null;
		Date queueDate = null;

		if (searchText != null && !searchText.trim().isEmpty()) {
			// dsPrefixId = searchText;
			dsName = searchText;
			username = searchText;
			status = DatasetStatus.getByName(searchText);
			dsId = searchText;

			// Using an exception to determine if this is numeric is slow. Using regex would
			// be faster.
			try {
				recCount = Long.valueOf(searchText);
				dataSource = Long.valueOf(searchText);
			} catch (NumberFormatException e) {
				logger.debug("searchAccessRecords searchText is not numeric.");
			}

			// Again using an exception to determine if string is a valid date. Test code
			// using
			// BRICSTimeDateUtil may be faster.
			try {
				queueDate = new SimpleDateFormat("yyyy-MM-dd").parse(searchText);
			} catch (ParseException e) {
				logger.debug("searchAccessRecords searchText is not an ISO date.");
			}

		}

		// convert days old into a Date
		Date oldestDate = null;
		if (daysOld != null) {
			oldestDate = new Date(BRICSTimeDateUtil.getStartOfCurrentDay() - (daysOld * BRICSTimeDateUtil.ONE_DAY));
		}

		return accessRecordDao.searchAccessRecords(study.getId(), dsId, recCount, dsName, status, searchText,
				dataSource, username, queueDate, oldestDate, pageData);
	}

	public int countAccessRecordsStudy(Long studyId) {
		return accessRecordDao.countAccessRecordsStudy(studyId);
	}

	/**
	 * {@inheritDoc}
	 */
	public int countAccessRecords(Long datasetId) {
		return accessRecordDao.countAccessRecords(datasetId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DownloadPackage> getDownloadPackageByUser(User user) {

		List<DownloadPackage> packages = downloadPackageDao.getByUser(user);
		return packages;
	}

	// solution for consolidated download package
	public void addQueryToolDownloadPackage(Account account, QTDownloadPackage qtDownloadPackage,
			List<UserFile> dataFiles) {

		logger.debug("Adding Query Tool download package to queue...");

		Set<Long> datasetIds = qtDownloadPackage.getDatasetIds();

		List<DownloadFileDataset> datasets = getDownloadFileDatasetsByIds(datasetIds);
		DownloadPackage qtPackage = new DownloadPackage();
		qtPackage.setOrigin(DownloadPackageOrigin.QUERY_TOOL);
		qtPackage.setDateAdded(qtDownloadPackage.getDateAdded());
		qtPackage.setName(qtDownloadPackage.getDirectoryName());
		qtPackage.setUser(account.getUser());

		for (UserFile uFile : dataFiles) {
			QueryToolDownloadFile qtResultFile = new QueryToolDownloadFile();
			qtResultFile.setDatasets(new HashSet<DownloadFileDataset>(datasets));
			qtResultFile.setDownloadPackage(qtPackage);
			qtResultFile.setUserFile(uFile);
			qtResultFile.setType(SubmissionType.DATA_FILE);
			qtPackage.addDownloadable(qtResultFile);
		}

		// XXX: this query in the loop may be a performance concern. may need to
		// refactor this if it becomes a problem
		// add the attached files to the download package
		if (qtDownloadPackage.getAttachedFilesMap() != null) {
			for (Entry<Long, Collection<String>> attachedFileEntry : qtDownloadPackage.getAttachedFilesMap().asMap()
					.entrySet()) {
				Long datasetId = attachedFileEntry.getKey();
				Collection<String> fileNames = attachedFileEntry.getValue();
				BasicDataset dataset = basicDatasetDao.get(datasetId);
				List<BasicDatasetFile> datasetFiles = basicDatasetFileDao.getUserFileByDatasetId(datasetId);

				Iterator<BasicDatasetFile> datasetFileIterator = datasetFiles.iterator();

				// need to remove user files that do not appear in our query result
				// use iterator here to prevent concurrency errors
				while (datasetFileIterator.hasNext()) {
					BasicDatasetFile datasetFile = datasetFileIterator.next();
					if (!fileNames.contains(datasetFile.getUserFile().getName())) {
						datasetFileIterator.remove();
					}
				}

				// if no files are missing, then this list should be empty.
				List<String> missingFiles = getMissingFiles(fileNames, datasetFiles);

				if (missingFiles.isEmpty()) {
					for (BasicDatasetFile file : datasetFiles) {
						DatasetDownloadFile dsDownloadFile = new DatasetDownloadFile();
						dsDownloadFile.setDataset(new DownloadFileDataset(dataset));
						dsDownloadFile.setDownloadPackage(qtPackage);
						dsDownloadFile.setUserFile(file.getUserFile());
						dsDownloadFile.setType(file.getFileType());
						// dsDownloadFile.setType();
						qtPackage.addDownloadable(dsDownloadFile);
					}
				} else { // throw exception and log and email about the missing files.
					logMissingUserFiles(account, dataset, missingFiles);
					sendMissingUserFileNotification(account, dataset, missingFiles);
					throw new DownloadQueueException("There are files missing from this query download package.");
				}
			}
		}

		logger.debug("Package object created, total files: " + qtPackage.getDownloadables().size());
		downloadPackageDao.save(qtPackage);
		sendDownloadAddNotification(account, qtPackage);
		logger.debug("Package successfully saved to the database");
	}

	private void logMissingUserFiles(Account account, BasicDataset dataset, List<String> missingFiles) {
		if (missingFiles.isEmpty()) {
			throw new DownloadQueueException(
					"The list of missing files is empty.  No reason to call this method if there are no missing files.");
		}

		for (String missingFile : missingFiles) {
			logger.error(account.getUserName() + ": " + "The dataset, " + dataset.getName()
					+ ", referenced in a query tool result is missing file, " + missingFile);
		}
	}

	private void sendMissingUserFileNotification(Account account, BasicDataset dataset, List<String> missingFiles) {
		if (missingFiles.isEmpty()) {
			throw new DownloadQueueException(
					"The list of missing files is empty.  No reason to call this method if there are no missing files.");
		}

		String emailAddress = modulesConstants.getModulesInfraEmail();
		String userName = account.getUserName();
		String datasetName = dataset.getName();
		String mailSubject = messageSource.getMessage(ServiceConstants.DOWNLOAD_QUEUE_MISSING_FILE_SUBJECT, null, null);

		StringBuffer missingFileListBuffer = new StringBuffer();
		missingFileListBuffer.append("<ul>");
		for (String missingFile : missingFiles) {
			missingFileListBuffer.append("<li>");
			missingFileListBuffer.append(missingFile);
			missingFileListBuffer.append("</li>");
		}
		missingFileListBuffer.append("</ul>");

		Object[] bodyArgs = new Object[] { userName, datasetName, missingFileListBuffer.toString() };
		String mailBody = messageSource.getMessage(ServiceConstants.DOWNLOAD_QUEUE_MISSING_FILE_BODY, bodyArgs, null);

		try {
			mailEngine.sendMail(mailSubject, mailBody, null, emailAddress);
		} catch (MessagingException e) {
			logger.error(
					"Error occured while trying to email infrastructure about missing files from a user's query tool result");
			e.printStackTrace();
		}
	}

	/*
	 * send email notification to OPS team for error during load status.
	 */

	private void sendDataErrorLoadNotification(List<String> datasetFiles) {

		String emailAddress = modulesConstants.getModulesOrgEmail();
		String mailSubject = messageSource.getMessage(ServiceConstants.MAIL_RESOURCE_ERROR_LOAD_SUBMISSION_SUBJECT,
				null, null);

		String modulesDTURLStr = modulesConstants.getModulesDTURL();

		// List of Dataset Name, Dataset System ID., Study title User: [Username] [email
		// address]
		StringBuffer dataFileListBuffer = new StringBuffer();
		dataFileListBuffer.append("<ul>");
		for (String fileName : datasetFiles) {
			dataFileListBuffer.append("<li>");
			dataFileListBuffer.append(fileName);
			dataFileListBuffer.append("</li>");
		}
		dataFileListBuffer.append("</ul>");

		Object[] bodyArgs = new Object[] { dataFileListBuffer.toString(), modulesConstants.getModulesOrgName(),
				modulesDTURLStr };
		String mailBody = messageSource.getMessage(ServiceConstants.MAIL_RESOURCE_ERROR_LOAD_SUBMISSION_BODY, bodyArgs,
				null);
		try {
			mailEngine.sendMail(mailSubject, mailBody, null, emailAddress);
		} catch (MessagingException e) {
			logger.error("Error occured while trying to email to the OPS team Error During Load Status.");
			e.printStackTrace();
		}
	}

	private List<String> getMissingFiles(Collection<String> fileNames, List<BasicDatasetFile> datasetFiles) {

		if (fileNames == null || datasetFiles == null) {
			throw new DownloadQueueException(
					"List of file names for a dataset is null, or the list of userFiles returned by the database is null.");
		}

		List<String> missingFiles = new ArrayList<String>();
		List<String> userFileNames = new ArrayList<String>();

		// collect a list of user file names from the userFile objects
		for (BasicDatasetFile datasetFile : datasetFiles) {
			userFileNames.add(datasetFile.getUserFile().getName());
		}

		// determine which UserFiles are missing from the user file list
		for (String fileName : fileNames) {
			if (!userFileNames.contains(fileName)) {
				missingFiles.add(fileName);
			}
		}

		return missingFiles;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DownloadPackage getDownloadPackageById(Long id) {
		return downloadPackageDao.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendDownloadAddNotification(Account account, DownloadPackage downloadPackage) {
		Date dateTimeAdded = downloadPackage.getDateAdded();
		DownloadPackageOrigin origin = downloadPackage.getOrigin();
		String packageName = downloadPackage.getName();
		String emailAddress = account.getUser().getEmail();
		String fullName = account.getUser().getFullName();
		String mailSubject = messageSource.getMessage(ServiceConstants.DOWNLOAD_QUEUE_NOTIFICATION_SUBJECT, null, null);
		Object[] bodyArgs = new Object[] { fullName, dateTimeAdded, origin.getName(), packageName,
				modulesConstants.getModulesOrgName() };
		String mailBody = messageSource.getMessage(ServiceConstants.DOWNLOAD_QUEUE_NOTIFICATION_BODY, bodyArgs, null);

		try {
			mailEngine.sendMail(mailSubject, mailBody, null, emailAddress);
		} catch (MessagingException e) {
			logger.error("Error occured while trying to email user about finished add to Query Tool");
			e.printStackTrace();
		}
	}

	@Override
	public DownloadPackage getDownloadPackageByDatasetAndUser(DownloadFileDataset downloadFileDataset, User user) {
		return downloadPackageDao.getDownloadPackageByDatasetAndUser(downloadFileDataset, user);
	}

	/**
	 * {@inheritDoc}
	 */
	public XmlInstanceMetaData inspectXmlData(byte[] xmlData) throws XmlValidationException {
		MetaParser metaParser = new MetaParser(xmlData);
		return metaParser.parse();
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getDictionarySchema(String fsName, String domain) throws XmlValidationException {
		byte[] schemaBytes = null;
		String url = domain + "portal/ws/api/dictionary/FormStructure/" + fsName;
		logger.debug("Schema WS call: " + url);
		GetMethod get = new GetMethod(url);
		HttpClient httpClient = new HttpClient();
		try {
			httpClient.executeMethod(get);
			schemaBytes = get.getResponseBody();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (get.getStatusCode() == 403) {
			logger.error("Schema web service response is 404. WS Call: " + url);
			throw new XmlValidationException("User does not have access to the request form structure: " + fsName);
		} else if (get.getStatusCode() == 404) {
			logger.error("Schema web service response is 404. WS Call: " + url);
			throw new XmlValidationException("The form structure " + fsName + " could not be found.");
		} else if (get.getStatusCode() != 200) {
			logger.error("Schema web service response is " + get.getStatusCode());
			throw new XmlValidationException(
					"There was an internal error looking up your form structure. Please contanct the help desk.");
		}

		logger.debug(new String(schemaBytes));

		return schemaBytes;
	}

	/**
	 * @throws DictionaryVersioningException
	 * @inheritDoc
	 */
	public void updateFormStructureReferences(Long oldFormStructureId, Long newFormStructureId,
			Map<Long, Long> oldToNewRepeatableGroupId) throws DictionaryVersioningException {
		logger.info("Updating form structure references to the latest version..");
		updateDatasetDataStructureReferences(oldFormStructureId, newFormStructureId);
		updateDataStoreFormStructureReference(oldFormStructureId, newFormStructureId, oldToNewRepeatableGroupId);
	}

	/**
	 * Update the form structure reference for DatasetDataStructure references
	 * 
	 * @param oldFormStructureId
	 * @param newFormStructureId
	 */
	private void updateDatasetDataStructureReferences(Long oldFormStructureId, Long newFormStructureId) {
		List<DatasetDataStructure> datasetDataStructures = getDatasetDataStructureByDataStructureId(oldFormStructureId);

		for (DatasetDataStructure datasetDataStructure : datasetDataStructures) {
			logger.info("Updating datasetDataStructure " + datasetDataStructure.getId()
					+ " form structure reference from " + oldFormStructureId + " to " + newFormStructureId + "...");
			datasetDataStructure.setDataStructureId(newFormStructureId);
			datasetDataStructureDao.save(datasetDataStructure);
		}
	}

	/**
	 * Update the form structure reference for a dataStoreInfo `
	 * 
	 * @param oldFormStructureId
	 * @param newFormStructureId
	 * @throws DictionaryVersioningException
	 */
	private void updateDataStoreFormStructureReference(Long oldFormStructureId, Long newFormStructureId,
			Map<Long, Long> oldToNewRepeatableGroupId) throws DictionaryVersioningException {
		DataStoreInfo dataStoreInfo = dataStoreInfoDao.getByDataStructureId(oldFormStructureId);
		if (dataStoreInfo != null) {
			logger.info("Datastore info Form Structure ID from " + oldFormStructureId + " to " + newFormStructureId
					+ "...");
			dataStoreInfo.setDataStructureId(newFormStructureId);

			for (DataStoreTabularInfo tabularInfo : dataStoreInfo.getDataStoreTabularInfos()) {
				Long oldRepeatableGroupId = tabularInfo.getRepeatableGroupId();
				Long newRepeatableGroupId = oldToNewRepeatableGroupId.get(oldRepeatableGroupId);
				if (newRepeatableGroupId == null) {
					throw new DictionaryVersioningException(
							"Error occurred while trying to update the repeatable group ID of " + oldRepeatableGroupId);
				}

				tabularInfo.setRepeatableGroup(newRepeatableGroupId);
			}

			dataStoreInfoDao.save(dataStoreInfo);
		} else {
			logger.warn("Datastore info for Form Structure #" + oldFormStructureId + " not found...skipping...");
		}
	}

	/**
	 * @inheritDoc
	 */
	public List<DatasetDataStructure> getDatasetDataStructureByDataStructureId(Long id) {
		return datasetDataStructureDao.getByFormStructureId(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public XmlValidationResponse validateXmlData(byte[] xmlData, byte[] schemaData) throws XmlValidationException {
		DataParser dataParser = new DataParser(xmlData, schemaData);
		return dataParser.validate();
	}

	/**
	 * @inheritDoc
	 */
	public String getDatasetFilePath(String studyPrefixedId, String datasetName, String fileName) {
		return datasetDao.getDatasetFilePath(studyPrefixedId, datasetName, fileName);
	}

	/**
	 * Create study for doubling the amount of study appending "_DD" to the
	 * prefixedId in the new study
	 * 
	 * @param study
	 * @param account
	 */
	@Override
	public Study createStudyForPB(Study study, Account account) {

		logger.debug("createStudyForPB() -- ");

		if (!accountManager.hasRole(account, RoleType.ROLE_STUDY)) {
			throw new RuntimeException(ServiceConstants.WRITE_ACCESS_DENIED);
		}

		Study newStudy = new Study();
		newStudy.setTitle(study.getTitle() + ModelConstants.SURFIX_PERFORM_BM);
		newStudy.setAbstractText(study.getAbstractText());
		newStudy.setRecruitmentStatus(study.getRecruitmentStatus());
		newStudy.setStudyStartDate(study.getStudyStartDate());
		newStudy.setStudyEndDate(study.getStudyEndDate());

		newStudy = saveStudy(account, newStudy);

		String prefix = modulesConstants.getModulesOrgName(Long.valueOf(account.getDiseaseKey()))
				+ ModelConstants.PREFIX_STUDY;
		String prefixedId = prependZeroesToId(prefix, newStudy.getId()) + ModelConstants.SURFIX_PERFORM_BM;
		newStudy.setPrefixedId(prefixedId);
		newStudy = studyDao.save(newStudy);

		return newStudy;
	}

	@Override
	public Set<EventLog> getEventLogs(Long entityID, EntityType entityType) {
		return eventLogDao.search(entityID, entityType);
	}

	@Override
	public List<Dataset> getDatasets(Set<Long> datasetIds) {
		return datasetDao.getByIds(datasetIds);
	}

	@Override
	public Dataset changeDatasetStatus(Dataset dataset, DatasetStatus datasetStatus, Account account,
			String statusChangecomment, EventLog eventLog) throws SQLException {

		EventType eventType = null;

		switch (datasetStatus) {
			case ARCHIVED:
				eventType = EventType.STATUS_CHANGE_TO_ARCHIVE;
				break;
			case DELETED:
				eventType = EventType.DATASET_DELETION;
				break;
			case SHARED:
				eventType = EventType.STATUS_CHANGE_TO_SHARED;
				break;
			default:
				break;
		}

		if (eventType == EventType.DATASET_DELETION) {

			deleteDataset(dataset, account.getUser(), statusChangecomment, eventLog);

		} else if (eventType == EventType.STATUS_CHANGE_TO_SHARED) {
			dataset.setShareDate(new Date());
			dataset.setDatasetStatus(DatasetStatus.SHARED);
			this.datasetDao.save(dataset);
		} else {
			eventLog = setValuesToEventLog(dataset.getId(), EntityType.DATASET, eventType, account.getUser(),
					statusChangecomment, eventLog, datasetStatus.getId().toString(),
					dataset.getDatasetStatus().getId().toString());

			this.eventLogDao.save(eventLog);

			dataset.setDatasetStatus(datasetStatus);
			this.datasetDao.save(dataset);
		}

		return dataset;

	}

	@Override
	public Dataset approveDatasetStatus(Dataset dataset, Account account, String statusChangecomment, EventLog eventLog)
			throws SQLException {

		EventType eventType = null;

		switch (dataset.getDatasetRequestStatus()) {
			case ARCHIVED:
				eventType = EventType.REQUESTED_ARCHIVED_APPROVED;
				break;
			case DELETED:
				eventType = EventType.REQUESTED_DELETE_APPROVED;
				break;
			case SHARED:
				// since we are sharing this dataset, set a new share date
				dataset.setShareDate(new Date());
				eventType = EventType.REQUESTED_SHARED_APPROVED;
				break;
			default:
				break;
		}

		if (eventType == EventType.REQUESTED_DELETE_APPROVED) {

			deleteDataset(dataset, account.getUser(), statusChangecomment, eventLog);

		} else {

			eventLog = setValuesToEventLog(dataset.getId(), EntityType.DATASET, eventType, account.getUser(),
					statusChangecomment, eventLog, dataset.getDatasetRequestStatus().getId().toString(),
					dataset.getDatasetStatus().getId().toString());

			this.eventLogDao.save(eventLog);

			dataset.setDatasetStatus(dataset.getDatasetRequestStatus());
			dataset.setDatasetRequestStatus(null);
			this.datasetDao.save(dataset);
		}

		return dataset;
	}

	@Override
	public Dataset rejectDatasetStatus(Dataset dataset, Account account, String statusChangecomment,
			EventLog eventLog) {
		EventType eventType = null;

		switch (dataset.getDatasetRequestStatus()) {
			case ARCHIVED:
				eventType = EventType.REQUESTED_ARHIVED_REJECTED;
				break;
			case DELETED:
				eventType = EventType.REQUESTED_DELETE_REJECTED;
				break;
			case SHARED:
				eventType = EventType.REQUESTED_SHARED_REJECTED;
				break;
			default:
				break;
		}

		eventLog = setValuesToEventLog(dataset.getId(), EntityType.DATASET, eventType, account.getUser(),
				statusChangecomment, eventLog, dataset.getDatasetStatus().getId().toString(),
				dataset.getDatasetStatus().getId().toString());

		this.eventLogDao.save(eventLog);

		dataset.setDatasetRequestStatus(null);
		this.datasetDao.save(dataset);

		return dataset;

	}

	public EventLog setValuesToEventLog(Long entityId, EntityType entityType, EventType eventType, User user,
			String statusChangecomment, EventLog eventLog, String newValue, String oldValue) {

		eventLog.setEntityID(entityId);
		eventLog.setEventType(eventType);
		eventLog.setUser(user);
		eventLog.setCreateTime(new Date());

		eventLog.setComment(statusChangecomment);
		eventLog.setType(entityType);
		eventLog.setOldValue(oldValue);
		eventLog.setNewValue(newValue);

		return eventLog;
	}

	public List<Dataset> addDatasetsToDownloadQueueByStudy(Study study, Account account, String proxyTicket) {
		List<Dataset> datasetList = new ArrayList<Dataset>();

		datasetList.addAll(getDatasetsByStudy(study));
		try {
			for (Dataset dataset : datasetList) {
				logger.debug("dataset id: " + dataset.getId());
				List<Long> dsIdList = new ArrayList<Long>();
				for (DatasetDataStructure datasetDataStructure : datasetDataStructureDao
						.getByDatasetId(dataset.getId())) {
					logger.debug(
							"datasetDataStructure.getDataStructureId(): " + datasetDataStructure.getDataStructureId());
					dsIdList.add(datasetDataStructure.getDataStructureId());
				}

				String dictionaryUrl = modulesConstants.getModulesDDTURL(ServiceConstants.DEFAULT_PROVIDER);
				RestDictionaryProvider restProvider = new RestDictionaryProvider(dictionaryUrl, proxyTicket);
				List<FormStructure> fsList = restProvider.getDataStructureDetailsByIds(dsIdList);

				if (fsList.size() > 0) {
					addDatasetToDownloadQueue(dataset, account, fsList, false);
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		return datasetList;
	}

	public void deleteDownloadPackageByIds(List<Long> ids) {
		downloadPackageDao.deleteByIds(ids);
	}

	public String modifyCSVFile(Account account, File dataFile, File destFile, String formName, String updatedDSName,
			String studyId) throws FileNotFoundException, NumberFormatException, UnsupportedEncodingException {

		String result = "Success";
		String userEmail = account.getUser().getEmail();
		String studyName = "";

		try {
			studyName = getStudy(account, Long.valueOf(studyId)).getTitle();
		} catch (UserPermissionException e) {
			logger.error("Unable to modify csv file " + dataFile.getName() + " due to the following errors:", e);
			result = "Error: Not Authonrized User";
		}

		List<String> firstLineArr = new ArrayList<String>();
		firstLineArr.add(userEmail);
		firstLineArr.add(updatedDSName);
		firstLineArr.add(studyName);

		CSVReader reader = null;
		CSVWriter writer = null;
		try {
			reader = new CSVReader(new FileReader(dataFile));
			writer = new CSVWriter(new FileWriter(destFile), CSVWriter.DEFAULT_SEPARATOR);

			/* write the information line for submission */
			String[] firstLine = new String[firstLineArr.size()];
			firstLine = firstLineArr.toArray(firstLine);
			writer.writeNext(firstLine);

			String[] nextLine;
			int i = 0;

			while ((nextLine = reader.readNext()) != null) {
				if (i == 0) {
					nextLine[0] = formName;
				}

				writer.writeNext(nextLine);
				i++;
			}
		} catch (IOException e) {
			logger.error("Unable to modify csv file " + dataFile.getName() + " due to the following errors:", e);
			result = "Error: File Failed to Close";
		}

		try {
			if (writer != null)
				writer.close();
			if (writer != null)
				reader.close();
		} catch (IOException e) {
			logger.error("Unable to modify csv file " + dataFile.getName() + " due to the following errors:", e);
			result = "Error: File Not Accessible";
		}

		return result;

	}

	public List<Downloadable> downloadDataByStudy(Account account, Study study, String proxyTicket, String downloadDir)
			throws UnsupportedEncodingException, JSchException, SftpException, UserPermissionException {

		List<Dataset> datasetList = new ArrayList<Dataset>();
		datasetList.addAll(addDatasetsToDownloadQueueByStudy(study, account, proxyTicket));

		SftpClient sftp = null;
		List<Long> downloadPackageIdList = new ArrayList<Long>();
		List<DownloadPackage> downloadPackageList = new ArrayList<DownloadPackage>();
		List<Downloadable> downloadableList = null;

		for (Dataset dataset : datasetList) {
			DownloadFileDataset downloadFileDataset = new DownloadFileDataset(dataset);
			DownloadPackage downloadPackage = getDownloadPackageByDatasetAndUser(downloadFileDataset,
					account.getUser());
			if (downloadPackage != null) {
				downloadPackageList.add(downloadPackage);
				downloadPackageIdList.add(downloadPackage.getId());

				if (downloadPackage.getOrigin().equals(DownloadPackageOrigin.DATASET)) {
					downloadableList = new ArrayList<Downloadable>(downloadPackage.getDownloadables());

					Iterator<Downloadable> downloadableIterator = downloadPackage.getDownloadables().iterator();
					String destPath = downloadDir + dataset.getName() + "_" + dataset.getStudy().getId();

					while (downloadableIterator.hasNext()) {
						Downloadable downloadable = downloadableIterator.next();
						UserFile userFile = downloadable.getUserFile();

						if (!userFile.getName().endsWith(".xml") && !userFile.getName().endsWith("_pv_mapping.csv")) { // not
																														// downloading
																														// dataFile-*.xml)
																														// and
																														// pv
																														// mapping
																														// file
							try {
								logger.info("userFile.getPath(): " + userFile.getPath() + "****userFile.getName(): "
										+ userFile.getName());
								sftp = SftpClientManager.getClient(userFile.getDatafileEndpointInfo());

								synchronized (sftp) {
									sftp.download(userFile.getName(), userFile.getPath(), destPath);
								}
							} catch (Exception e) {
								downloadableList.remove(downloadable);
								e.printStackTrace();
								logger.warn(e.getMessage());
							}
						} else {
							downloadableList.remove(downloadable);
						}
					}
				} // end if
			}
		}

		// remove from download queue
		deleteDownloadPackageByIds(downloadPackageIdList);

		return downloadableList;

	}

	@Override
	public void saveDatasetEventLog(Account account, Dataset dataset, EventLog eventLog, String statusChangeComment)
			throws SQLException {

		EventType eventType = null;

		switch (dataset.getDatasetRequestStatus()) {
			case ARCHIVED:
				eventType = EventType.REQUESTED_ARCHIVED_APPROVED;
				break;
			case DELETED:
				eventType = EventType.REQUESTED_DELETE_APPROVED;
				break;
			case SHARED:
				eventType = EventType.REQUESTED_SHARED_APPROVED;
				break;
			default:
				break;
		}

		if (eventType == EventType.REQUESTED_DELETE_APPROVED) {

			return;

		} else {

			eventLog = setValuesToEventLog(dataset.getId(), EntityType.DATASET, eventType, account.getUser(),
					statusChangeComment, eventLog, dataset.getDatasetRequestStatus().getId().toString(),
					dataset.getDatasetStatus().getId().toString());

			this.eventLogDao.save(eventLog);
		}
	}

	@Override
	public void stuckDatasetChangeStatus(int cutOffDay) {

		Date current = BRICSTimeDateUtil.getCurrentTime();
		Date cutOffDate = DateUtils.addDays(current, -cutOffDay);
		List<String> datasetDetails = new ArrayList<String>();
		List<Dataset> datasets = datasetDao.getByStatusesAndDate(cutOffDate, DatasetStatus.LOADING);

		for (Dataset dataset : datasets) {
			dataset.setDatasetStatus(DatasetStatus.ERROR);
			datasetDetails.add(dataset.getDatasetDetail());
			this.datasetDao.save(dataset);
		}

		if (!datasetDetails.isEmpty()) {
			sendDataErrorLoadNotification(datasetDetails);
		} else {
			logger.warn(
					"No datasets are detected to have Error with Load status, the email notification is not needed!");
		}
	}

	@Override
	public Study getStudySites(Long studyId) {
		return studyDao.getStudySites(studyId);

	}

	public StudyForm getStudyForm(Long studyId, String shortName, String version) {
		return studyFormDao.getSingle(studyId, shortName, version);
	}

	public List<StudyKeyword> retrieveAllStudyKeywords() {
		@SuppressWarnings("unchecked")
		List<StudyKeyword> allStudyKeywords = (List<StudyKeyword>) keywordDao.getAllKeywords(StudyKeyword.class);
		return allStudyKeywords;
	}

	public List<StudyKeyword> searchKeywords(String searchKey) {
		List<StudyKeyword> keywords = (List<StudyKeyword>) keywordDao.search(searchKey, StudyKeyword.class);
		return keywords;
	}

	public Long getKeywordCount(String keyword) {
		return keywordDao.getCountByKeyword(keyword, StudyKeyword.class);
	}

	public SemanticFormStructureList getPublishedFS(SessionAccount account, String proxyTicket) {
		RestDictionaryProvider restProvider = new RestDictionaryProvider(modulesConstants.getModulesDDTURL(),
				proxyTicket);
		try {
			return restProvider.getPublishedDataStructures();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return new SemanticFormStructureList();
		}
	}

	public SemanticFormStructureList getPublihedAndAwaitingFS(SessionAccount account, String proxyTicket) {
		RestDictionaryProvider restProvider = new RestDictionaryProvider(modulesConstants.getModulesDDTURL(),
				proxyTicket);
		try {
			return restProvider.getPublishedAndAwaitingFS();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return new SemanticFormStructureList();
		}
	}

	public FormStructure getFormStructureDetails(SessionAccount account, String shortName, String version,
			String proxyTicket) {
		RestDictionaryProvider restProvider = new RestDictionaryProvider(modulesConstants.getModulesDDTURL(),
				proxyTicket);

		FormStructure fs = null;
		try {
			fs = restProvider.getDataStructureDetailsByShortName(shortName, version);
		} catch (UnsupportedEncodingException e) {
			logger.error("Requesting Form Structure details failed due to an UnsupportedEncodingException");
			e.printStackTrace();
		}

		return fs;
	}

	public List<Long> getDataSetIdsByStudyListAndDatasetStatus(Long studyId, Set<DatasetStatus> datasetStatuses) {
		Set<Long> studyIds = new HashSet<Long>();
		studyIds.add(studyId);
		return datasetDao.getDatasetIdsByStudyIds(studyIds, datasetStatuses);
	}

	public List<Long> getDataSetIdsByStudyListAndDatasetStatus(Set<Long> studyIds, Set<DatasetStatus> datasetStatuses) {
		return datasetDao.getDatasetIdsByStudyIds(studyIds, datasetStatuses);
	}

	public SemanticFormStructureList getFormStructuresForCollectedDataWithStatuses(Set<Long> studyIds,
			Set<DatasetStatus> datasetStatuses) throws UnsupportedEncodingException {

		List<Long> datasetIdList = getDataSetIdsByStudyListAndDatasetStatus(studyIds, datasetStatuses);

		Set<Long> datasetIds = new HashSet<Long>();
		datasetIds.addAll(datasetIdList);
		List<Dataset> datasetList = getDatasets(datasetIds);

		Set<Long> formStructureIds = new HashSet<Long>();

		for (Dataset ds : datasetList) {
			for (DatasetDataStructure dsd : ds.getDatasetDataStructure()) {
				formStructureIds.add(dsd.getDataStructureId());
			}
		}

		// empty proxy ticket because we are in an unauthenticated space
		RestDictionaryProvider dictionaryProvider = new RestDictionaryProvider(modulesConstants.getModulesDDTURL(), "");
		return dictionaryProvider.getLatestFormStructureByIdSet(formStructureIds);
	}

	public List<SupportingDocumentation> getPublicationsByStudyId(Long studyId) {
		return supportingDocumentationDao.getStudyPublicationDocumentation(studyId);
	}

	public ResearchManagement getPrimaryInvestigatorByStudyId(Long studyId) {
		return researchManagementDao.getStudyPrimaryInvestigator(studyId);
	}

	public ResearchManagement getStudyManagementImage(Long studyId, Long rmId) {
		return researchManagementDao.getStudyManagementImage(studyId, rmId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createDoiForStudy(Study study)
			throws IllegalStateException, WebApplicationException, DoiWsValidationException {
		// Find the owner of this study.
		List<EntityMap> entities = accountManager.listEntityAccess(study.getId(), EntityType.STUDY);
		Account owner = null;

		for (EntityMap em : entities) {
			if (em.getPermission() == PermissionType.OWNER) {
				owner = em.getAccount();
				break;
			}
		}

		// Check if an owner was found.
		if (owner == null) {
			throw new IllegalStateException("No owner was found for the \"" + study.getTitle() + "\" study.");
		}

		// Create a DOI record object from the study data.
		DoiUtil doiUtil = new DoiUtil(modulesConstants);
		OSTIRecord record = null;

		try {
			record = doiUtil.modelToDoiRecord(EntityType.STUDY, study, owner);
		} catch (IllegalArgumentException iae) {
			throw new IllegalStateException("Error while translating study to a DOI record object.", iae);
		}

		// Save the new DOI record to the IAD web service.
		DoiMinterProvider doiProvider = new DoiMinterProvider(modulesConstants.getIadWsUrl(),
				modulesConstants.getIadUsername(), modulesConstants.getIadPassword());

		doiProvider.saveDoiToIad(record);

		// Record new DOI and OSTI ID data in the given study object.
		study.setDoi(record.getDoi());
		study.setOstiId(record.getOstiId());
	}

	public boolean isStudyPublic(Long studyId) {
		return studyDao.isStudyPublic(studyId);
	}

	/**
	 * {@inheritDoc}
	 */
	public Study getLazyStudy(Long id) {

		return studyDao.getLazy(id);
	}

	public JsonArray getPublicSiteSubmittedData(StudySubmittedFormCache studySubmittedFormCache) {
		// long startTime = System.currentTimeMillis();
		JsonArray submittedDataJsonArray = new JsonArray();
		List<BasicStudySearch> basicStudySearchList = this.getPublicSiteSearchBasicStudies();
		for (BasicStudySearch basicStudySearch : basicStudySearchList) {
			Long studyId = Long.valueOf(basicStudySearch.getId());
			Study study = studyDao.getPublicSubmittedDataStudyById(studyId, StudyStatus.PUBLIC);
			if (study != null) {
				JsonObject submittedDataJson = new JsonObject();
				submittedDataJson.addProperty("id", basicStudySearch.getId());
				submittedDataJson.addProperty("principleName", basicStudySearch.getPrincipleName());
				submittedDataJson.addProperty("fundingSource", basicStudySearch.getFundingSource());
				submittedDataJson.addProperty("title", basicStudySearch.getTitle());

				Set<Grant> grantSet = new HashSet<Grant>(study.getGrantSet());
				List<String> grantIdList = new ArrayList<String>();
				for (Grant grant : grantSet) {
					grantIdList.add(grant.getGrantId());
				}
				String grantId = grantIdList.size() == 0 ? "" : StringUtils.join(grantIdList);
				if (grantId.length() > 2) {// remove square blankets
					grantId = grantId.substring(1, grantId.length() - 1);
				}
				submittedDataJson.addProperty("grantId", grantId);

				Integer numbOfSubject = studySubmittedFormCache
						.getSubjectCountByStudy(String.valueOf(basicStudySearch.getId()));
				submittedDataJson.addProperty("subjectCount", numbOfSubject);

				Integer totalRecordCount = studySubmittedFormCache
						.getRowCountByStudy(String.valueOf(basicStudySearch.getId()));
				submittedDataJson.addProperty("recordCount", totalRecordCount);

				Integer formWithDataCount = studySubmittedFormCache
						.getFormCountByStudy(String.valueOf(basicStudySearch.getId()));
				submittedDataJson.addProperty("formWithDataCount", formWithDataCount);

				submittedDataJson.addProperty("privateDatasetCount", basicStudySearch.getPrivateDatasetCount());
				submittedDataJson.addProperty("sharedDatasetCount", basicStudySearch.getSharedDatasetCount());

				submittedDataJsonArray.add(submittedDataJson);
			}
		}
		// long stopTime = System.currentTimeMillis();
		// long elapseTime = stopTime -startTime;
		// logger.debug("Excution time: " + elapseTime);
		logger.debug("submittedDataJsonArray: " + submittedDataJsonArray);
		return submittedDataJsonArray;
	}

	public JsonArray getPublicSiteSubmittedDataByStudy(List<BasicStudySearch> basicStudySearchList, Study study,
			StudySubmittedFormCache studySubmittedFormCache) {
		JsonArray submittedDataJsonArray = new JsonArray();

		for (BasicStudySearch basicStudySearch : basicStudySearchList) {
			if (study.getId().equals(Long.valueOf(basicStudySearch.getId()))) {
				JsonObject submittedDataJson = new JsonObject();
				submittedDataJson.addProperty("id", basicStudySearch.getId());
				submittedDataJson.addProperty("principleName", basicStudySearch.getPrincipleName());
				submittedDataJson.addProperty("fundingSource", basicStudySearch.getFundingSource());
				submittedDataJson.addProperty("title", basicStudySearch.getTitle());

				Set<Grant> grantSet = new HashSet<Grant>(study.getGrantSet());
				List<String> grantIdList = new ArrayList<String>();
				for (Grant grant : grantSet) {
					grantIdList.add(grant.getGrantId());
				}
				String grantId = grantIdList.size() == 0 ? "" : StringUtils.join(grantIdList);
				if (grantId.length() > 2) {// remove square blankets
					grantId = grantId.substring(1, grantId.length() - 1);
				}
				submittedDataJson.addProperty("grantId", grantId);

				Integer numbOfSubject = studySubmittedFormCache
						.getSubjectCountByStudy(String.valueOf(basicStudySearch.getId()));
				submittedDataJson.addProperty("subjectCount", numbOfSubject);

				Integer totalRecordCount = studySubmittedFormCache
						.getRowCountByStudy(String.valueOf(basicStudySearch.getId()));
				submittedDataJson.addProperty("recordCount", totalRecordCount);

				Integer formWithDataCount = studySubmittedFormCache
						.getFormCountByStudy(String.valueOf(basicStudySearch.getId()));
				submittedDataJson.addProperty("formWithDataCount", formWithDataCount);

				submittedDataJson.addProperty("privateDatasetCount", basicStudySearch.getPrivateDatasetCount());
				submittedDataJson.addProperty("sharedDatasetCount", basicStudySearch.getSharedDatasetCount());

				submittedDataJsonArray.add(submittedDataJson);

			}
		}
		// long stopTime = System.currentTimeMillis();
		// long elapseTime = stopTime -startTime;
		// logger.debug("Excution time: " + elapseTime);
		logger.debug("submittedDataJsonArray: " + submittedDataJsonArray);
		return submittedDataJsonArray;
	}

	private String getAllAuthorsByPubMedId(String pubMedId) {
		List<String> authorList = new ArrayList<String>();

		String pubMedWsUrl = CoreConstants.pubmedWsUrl + pubMedId;

		try {
			Document doc = Jsoup.connect(pubMedWsUrl).get();

			Elements error = doc.select("error");
			String errorText = error.html();
			if (!StringUtils.isEmpty(errorText)) {
				String err = "Error in retreiving author for pubMedId: " + pubMedId;
				authorList.add(err);
			}

			// First Author, PubMed returns in the format of LastName FIMI (First Name
			// Initial and Middle Name Initial)
			Elements authElements = doc.select("Item[Name=Author]");
			for (Element authEle : authElements) {
				String auth = authEle.html();
				authorList.add(auth);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return StringUtils.join(authorList, ", ");
	}

	public JsonArray getPublicationJsonByStudyId(Long studyId) {
		JsonArray publicationJsonArr = new JsonArray();
		List<SupportingDocumentation> supportingDocList = getPublicationsByStudyId(studyId);
		for (SupportingDocumentation supportingDoc : supportingDocList) {

			Publication publication = supportingDoc.getPublication();
			if (publication != null) {
				JsonObject publicationJson = new JsonObject();

				publicationJson.addProperty("id", publication.getId());

				UserFile userFile = supportingDoc.getUserFile();
				if (userFile == null) {
					publicationJson.addProperty("userFileId", "");
				} else {
					publicationJson.addProperty("userFileId", userFile.getId());
				}

				Calendar cal = Calendar.getInstance();
				cal.setTime(publication.getPublicationDate());
				Integer pubYear = cal.get(Calendar.YEAR);
				publicationJson.addProperty("year", String.valueOf(pubYear));
				publicationJson.addProperty("title", publication.getTitle());

				String pubMedId = publication.getPubmedId();
				String allAuthors = "";
				if (!pubMedId.isEmpty()) {
					try {
						allAuthors = new String(getAllAuthorsByPubMedId(pubMedId).getBytes("utf-8"), "ISO-8859-1"); // to
																													// escape
																													// latin
																													// characters
																													// like
																													// é
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				} else {
					Author authorFirst = publication.getFirstAuthor();
					String firstAuthName = authorFirst.getFirstName() + " " + authorFirst.getLastName();
					Author authorLast = publication.getLastAuthor();
					String lastAuthName = authorLast.getFirstName() + " " + authorLast.getLastName();
					allAuthors += firstAuthName.replaceAll("\\s+", "").isEmpty() ? "" : firstAuthName.trim();
					allAuthors += lastAuthName.replaceAll("\\s+", "").isEmpty() ? "" : ", " + lastAuthName.trim();
				}
				publicationJson.addProperty("authors", allAuthors);

				if (userFile == null) {
					publicationJson.addProperty("fileNameUrl", supportingDoc.getUrl());
				} else {
					publicationJson.addProperty("fileNameUrl", userFile.getName());
				}

				publicationJson.addProperty("pubMedId", pubMedId);

				publicationJsonArr.add(publicationJson);
			}
		}

		return publicationJsonArr;
	}

	public InputStream getSftpInputStream(Long datafileEndpointInfo, String filePath, String fileName)
			throws JSchException, SftpException {
		logger.info(
				" RepositoryManagerImpl :: getSftpInputStream :: filePath : " + filePath + " fileName : " + fileName);

		DatafileEndpointInfo info = datafileEndpointInfoDao.get(datafileEndpointInfo);

		if (info != null) {
			logger.info(" RepositoryManagerImpl :: getSftpInputStream :: " + " , dataFileEndpointInfo name : "
					+ info.getEndpointName() + " , dataFileEndpointInfo url : " + info.getUrl());
		}

		SftpClient client = SftpClientManager.getClient(info);
		return client.getFileStream(filePath, fileName);
	}

	public Study getStudyGraphicFileById(Integer studyId) {
		return studyDao.getStudyGraphicFileById(studyId);
	}

	public SupportingDocumentation getSupportingDocument(Long supportingDocId) {
		return supportingDocumentationDao.get(supportingDocId);
	}

	public List<DatasetFile> getDatasetFiles(Long datasetId) {
		return datasetFileDao.getByDatasetId(datasetId);
	}

	public Dataset getDatasetExcludingDatasetFiles(Long datasetId) {
		return datasetDao.getDatasetExcludingDatasetFiles(datasetId);
	}

	public Dataset getDatasetOnly(Long datasetId) {
		return datasetDao.getDataset(datasetId);
	}

	public void updateDatasetStatus(DatasetStatus status, Dataset currentDataset) {
		datasetDao.updateDatasetStatus(status, currentDataset.getId());
	}

	public BigInteger getDatasetPendingFileCount(Long datasetId) {
		return datasetFileDao.getDatasetPendingFileCount(datasetId);
	}

	/**
	 * This method removes the UserFile from database.
	 */
	public void removeUserFile(long userFileId) {
		userFileDao.remove(userFileId);
	}

	public boolean moveAsNewFileName(String filePath, String fileName, String newFileName) throws JSchException {
		DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);

		SftpClient client = SftpClientManager.getClient(info);

		try {
			client.copyAsNewFileName(filePath, fileName, newFileName);
			client.delete(filePath, fileName);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		SftpClientManager.closeAll();
		return true;

	}

	public SupportingDocumentation getPublicationDocument(Long studyId, Long publicationId) {
		return supportingDocumentationDao.getPublicationDocumentation(studyId, publicationId);
	}

	public void batchSaveAccessRecord(Set<AccessRecord> accessRecords) {
		accessRecordDao.batchSave(accessRecords);
	}

	/**
	 * @param filePath
	 * @throws SQLException {@inheritDoc}
	 */
	public boolean deleteDatasetCascadeByName(Dataset dataset) {
		// delete the file folder from ftp file sever
		Study study = dataset.getStudy();
		try {
			DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);
			SftpClient client = SftpClientManager.getClient(info);
			logger.info(" RepositoryManagerImpl :: deleteDatasetCascadeByName :: "
					+ ServiceConstants.REPO_SUBMISSION_UPLOAD_PATH + " : " + study.getPrefixedId() + " : "
					+ dataset.getDataName());
			client.deleteDatasetFolder(ServiceConstants.REPO_SUBMISSION_UPLOAD_PATH, study.getPrefixedId(),
					dataset.getDataName());
			logger.info("SFTP dataset deletion completed: " + dataset.getName());
		} catch (Exception e) {
			// e.printStackTrace();
			logger.error("SFTP dataset deletion error: " + dataset.getName() + " id :: " + dataset.getId());
		}
		if (dataset != null) {
			logger.info("Deleting meta records for dataset ID -- " + dataset.getId() + " status "
					+ dataset.getDatasetStatus().getName());
			this.datasetDao.remove(dataset.getId());
		}
		return true;
	}

	public void saveStudyTherapeuticAgent(Set<StudyTherapeuticAgent> therapeuticAgentSet, String studyId) {
		List<TherapeuticAgent> listAllTAgents = studyDao.getAllTherapeuticAgents();

		// remove existing StudyTherapeuticAgent study type
		List<StudyTherapeuticAgent> list = studyDao.getTherapeuticAgentsForStudy(studyId);

		Set<StudyTherapeuticAgent> insertRecords = new HashSet<>();
		Set<StudyTherapeuticAgent> deleteRecords = new HashSet<>();

		if (therapeuticAgentSet == null) {
			deleteRecords.addAll(list);
		} else if (therapeuticAgentSet.size() == 0 && list.size() > 0) {
			deleteRecords.addAll(list);
		}

		if (therapeuticAgentSet != null) {
			for (StudyTherapeuticAgent s : therapeuticAgentSet) {

				boolean recordInsert = true;

				for (StudyTherapeuticAgent tAgent : list) {
					if (tAgent.getTherapeuticAgent().getId() == s.getTherapeuticAgent().getId()) {
						recordInsert = false;
					}
				}

				if (recordInsert) {
					insertRecords.add(s);
				}
			}

			for (StudyTherapeuticAgent tAgent : list) {
				boolean recordDelete = true;

				for (StudyTherapeuticAgent s : therapeuticAgentSet) {
					if (tAgent.getTherapeuticAgent().getId() == s.getTherapeuticAgent().getId()) {
						recordDelete = false;
					}
				}

				if (recordDelete) {
					deleteRecords.add(tAgent);
				}
			}
		}

		for (StudyTherapeuticAgent s : insertRecords) {
			therapeuticAgentDataDao.saveTherapeuticAgentData(s, studyId);
		}

		for (StudyTherapeuticAgent s : deleteRecords) {
			therapeuticAgentDataDao.deleteTherapeuticAgentData(s, studyId);
		}
	}

	public void saveStudyTherapyType(Set<StudyTherapyType> therapyTypeSet, String studyId) {
		List<TherapyType> listAllTTypes = studyDao.getAllTherapyTypes();

		// remove existing StudyTherapyType study type
		List<StudyTherapyType> list = studyDao.getTherapyTypesForStudy(studyId);

		Set<StudyTherapyType> insertRecords = new HashSet<>();
		Set<StudyTherapyType> deleteRecords = new HashSet<>();

		if (therapyTypeSet == null) {
			deleteRecords.addAll(list);
		} else if (therapyTypeSet.size() == 0 && list.size() > 0) {
			deleteRecords.addAll(list);
		}

		if (therapyTypeSet != null) {

			for (StudyTherapyType s : therapyTypeSet) {

				boolean recordInsert = true;

				for (StudyTherapyType tAgent : list) {
					if (tAgent.getTherapyType().getId() == s.getTherapyType().getId()) {
						recordInsert = false;
					}
				}

				if (recordInsert) {
					insertRecords.add(s);
				}
			}

			for (StudyTherapyType tAgent : list) {
				boolean recordDelete = true;

				for (StudyTherapyType s : therapyTypeSet) {
					if (tAgent.getTherapyType().getId() == s.getTherapyType().getId()) {
						recordDelete = false;
					}
				}

				if (recordDelete) {
					deleteRecords.add(tAgent);
				}
			}
		}

		for (StudyTherapyType s : insertRecords) {
			therapyTypesDataDao.saveTherapyTypeData(s, studyId);
		}

		for (StudyTherapyType s : deleteRecords) {
			therapyTypesDataDao.deleteTherapyTypeData(s, studyId);
		}
	}

	public void saveStudyTherapeuticTarget(Set<StudyTherapeuticTarget> therapeuticTargetSet, String studyId) {

		List<TherapeuticTarget> listAllTTargets = studyDao.getAllTherapeuticTargets();

		// remove existing StudyTherapeuticAgent study type
		List<StudyTherapeuticTarget> list = studyDao.getTherapeuticTargetsForStudy(studyId);

		Set<StudyTherapeuticTarget> insertRecords = new HashSet<>();
		Set<StudyTherapeuticTarget> deleteRecords = new HashSet<>();

		if (therapeuticTargetSet == null) {
			deleteRecords.addAll(list);
		} else if (therapeuticTargetSet.size() == 0 && list.size() > 0) {
			deleteRecords.addAll(list);
		}

		if (therapeuticTargetSet != null) {

			for (StudyTherapeuticTarget s : therapeuticTargetSet) {

				boolean recordInsert = true;

				for (StudyTherapeuticTarget tAgent : list) {
					if (tAgent.getTherapeuticTarget().getId() == s.getTherapeuticTarget().getId()) {
						recordInsert = false;
					}
				}

				if (recordInsert) {
					insertRecords.add(s);
				}
			}

			for (StudyTherapeuticTarget tAgent : list) {
				boolean recordDelete = true;

				for (StudyTherapeuticTarget s : therapeuticTargetSet) {
					if (tAgent.getTherapeuticTarget().getId() == s.getTherapeuticTarget().getId()) {
						recordDelete = false;
					}
				}

				if (recordDelete) {
					deleteRecords.add(tAgent);
				}
			}
		}

		for (StudyTherapeuticTarget s : insertRecords) {
			therapeuticTargetDataDao.saveTherapeuticTargetData(s, studyId);
		}

		for (StudyTherapeuticTarget s : deleteRecords) {
			therapeuticTargetDataDao.deleteTherapeuticTargetData(s, studyId);
		}

	}

	public void saveStudyModelType(Set<StudyModelType> modelTypeSet, String studyId) {

		List<ModelType> listAllModelTypes = studyDao.getAllModelTypes();

		// remove existing StudyTherapeuticAgent study type
		List<StudyModelType> list = studyDao.getModelTypesForStudy(studyId);

		Set<StudyModelType> insertRecords = new HashSet<>();
		Set<StudyModelType> deleteRecords = new HashSet<>();

		if (modelTypeSet == null) {
			deleteRecords.addAll(list);
		} else if (modelTypeSet.size() == 0 && list.size() > 0) {
			deleteRecords.addAll(list);
		}

		if (modelTypeSet != null) {

			for (StudyModelType s : modelTypeSet) {

				boolean recordInsert = true;

				for (StudyModelType tAgent : list) {
					if (tAgent.getModelType().getId() == s.getModelType().getId()) {
						recordInsert = false;
					}
				}

				if (recordInsert) {
					insertRecords.add(s);
				}
			}

			for (StudyModelType tAgent : list) {
				boolean recordDelete = true;

				for (StudyModelType s : modelTypeSet) {
					if (tAgent.getModelType().getId() == s.getModelType().getId()) {
						recordDelete = false;
					}
				}

				if (recordDelete) {
					deleteRecords.add(tAgent);
				}
			}
		}

		for (StudyModelType s : insertRecords) {
			modelTypeDataDao.saveModelTypeData(s, studyId);
		}

		for (StudyModelType s : deleteRecords) {
			modelTypeDataDao.deleteModelTypeData(s, studyId);
		}
	}

	public void saveStudyModelName(Set<StudyModelName> modelNameSet, String studyId) {

		List<ModelName> listAllModelName = studyDao.getAllModelNames();

		// remove existing StudyTherapeuticAgent study type
		List<StudyModelName> list = studyDao.getModelNamesForStudy(studyId);

		Set<StudyModelName> insertRecords = new HashSet<>();
		Set<StudyModelName> deleteRecords = new HashSet<>();

		if (modelNameSet == null) {
			deleteRecords.addAll(list);
		} else if (modelNameSet.size() == 0 && list.size() > 0) {
			deleteRecords.addAll(list);
		}

		if (modelNameSet != null) {
			for (StudyModelName s : modelNameSet) {

				boolean recordInsert = true;

				for (StudyModelName tAgent : list) {
					if (tAgent.getModelName().getId() == s.getModelName().getId()) {
						recordInsert = false;
					}
				}

				if (recordInsert) {
					insertRecords.add(s);
				}
			}

			for (StudyModelName tAgent : list) {
				boolean recordDelete = true;

				for (StudyModelName s : modelNameSet) {
					if (tAgent.getModelName().getId() == s.getModelName().getId()) {
						recordDelete = false;
					}
				}

				if (recordDelete) {
					deleteRecords.add(tAgent);
				}
			}
		}

		for (StudyModelName s : insertRecords) {
			modelNameDataDao.saveModelNameData(s, studyId);
		}

		for (StudyModelName s : deleteRecords) {
			modelNameDataDao.deleteModelNameData(s, studyId);
		}
	}

	@Override
	public void cleanRepoTables(StructuralFormStructure datastructure) throws SQLException {

		DataStoreInfo dataStoreInfo = dataStoreInfoDao.getByDataStructureId(datastructure.getId());

		if (dataStoreInfo != null) {
			Set<DataStoreTabularInfo> dataStoreTabularInfos = dataStoreInfo.getDataStoreTabularInfos();

			List<String> tableNames = new ArrayList<String>();
			List<String> sequenceNames = new ArrayList<String>();

			for (DataStoreTabularInfo dataStoreTabularInfo : dataStoreTabularInfos) {
				tableNames.add(dataStoreTabularInfo.getTableName());
				sequenceNames.add(dataStoreTabularInfo.getTableName() + "_seq");
			}

			dataStoreDao.dropTables(StringUtils.join(tableNames, ","));
			dataStoreDao.dropSequences(StringUtils.join(sequenceNames, ","));

			dataStoreInfoDao.remove(dataStoreInfo.getId());
		}

	}

	@Override
	public void emailDevPublicationError(StructuralFormStructure datastructure, String errorMessage) {
		try {
			mailEngine.sendMail("Error: Unable to publish Form Structure " + datastructure.getShortName()
					+ " to repository database", errorMessage, "error", modulesConstants.getModulesDevEmail());
		} catch (MessagingException e1) {
			logger.error("Could not send email for createTableFromDataStructure ERROR!");
		}

	}
}
