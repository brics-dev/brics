package gov.nih.tbi.metastudy.service.hibernate;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.jcraft.jsch.JSchException;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.service.complex.BaseManagerImpl;
import gov.nih.tbi.commons.dao.FileTypeDao;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.FileClassification;
import gov.nih.tbi.commons.model.MetaStudyStatus;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.exceptions.DoiWsValidationException;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.MetaStudyManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.doi.model.OSTIRecord;
import gov.nih.tbi.doi.util.DoiUtil;
import gov.nih.tbi.doi.ws.DoiMinterProvider;
import gov.nih.tbi.metastudy.dao.MetaStudyAccessRecordDao;
import gov.nih.tbi.metastudy.dao.MetaStudyDao;
import gov.nih.tbi.metastudy.dao.MetaStudyDataDao;
import gov.nih.tbi.metastudy.dao.MetaStudyLabelDao;
import gov.nih.tbi.metastudy.dao.ResearchManagementMetaDao;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyAccessRecord;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyDocumentation;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyKeyword;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyLabel;
import gov.nih.tbi.metastudy.model.hibernate.ResearchManagementMeta;
import gov.nih.tbi.query.dao.SavedQueryDao;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.repository.dao.DatafileEndpointInfoDao;
import gov.nih.tbi.repository.dao.KeywordDao;
import gov.nih.tbi.repository.dao.ModelNameDao;
import gov.nih.tbi.repository.dao.ModelNameDataDao;
import gov.nih.tbi.repository.dao.ModelTypeDao;
import gov.nih.tbi.repository.dao.ModelTypeDataDao;
import gov.nih.tbi.repository.dao.SupportingDocumentationDao;
import gov.nih.tbi.repository.dao.TherapeuticAgentDao;
import gov.nih.tbi.repository.dao.TherapeuticAgentDataDao;
import gov.nih.tbi.repository.dao.TherapeuticTargetDao;
import gov.nih.tbi.repository.dao.TherapeuticTargetDataDao;
import gov.nih.tbi.repository.dao.TherapyTypeDao;
import gov.nih.tbi.repository.dao.TherapyTypeDataDao;
import gov.nih.tbi.repository.dao.UserFileDao;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelName;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelType;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapyType;
import gov.nih.tbi.repository.model.alzped.ModelName;
import gov.nih.tbi.repository.model.alzped.ModelType;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.TherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.TherapyType;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;

@Service
@Scope("singleton")
public class MetaStudyManagerImpl extends BaseManagerImpl implements MetaStudyManager {

	private static final long serialVersionUID = -6942551737214546264L;

	private static Logger logger = Logger.getLogger(MetaStudyManagerImpl.class);

	@Autowired
	AccountManager accountManager;

	@Autowired
	MetaStudyDao metaStudyDao;

	@Autowired
	UserFileDao userFileDao;

	@Autowired
	DatafileEndpointInfoDao datafileEndpointInfoDao;

	@Autowired
	FileTypeDao fileTypeDao;

	@Autowired
	KeywordDao keywordDao;

	@Autowired
	MetaStudyLabelDao metaStudyLabelDao;

	@Autowired
	SavedQueryDao savedQueryDao;
	
	@Autowired
	MetaStudyAccessRecordDao metaStudyAccessRecordDao;
	
	@Autowired
	MetaStudyDataDao metaStudyDataDao;
	
	@Autowired
	ResearchManagementMetaDao researchManagementMetaDao;
	
	@Autowired
	RepositoryManager repositoryManager;
	
	@Autowired
	SupportingDocumentationDao supportingDocumentationDao;
	
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
	
	@Autowired
	TherapeuticAgentDao therapeuticAgentDao;
	
	@Autowired
	TherapeuticTargetDao therapeuticTargetDao;
	
	@Autowired
	TherapyTypeDao therapyTypesDao;
	
	@Autowired
	ModelNameDao modelNameDao;
	
	@Autowired
	ModelTypeDao modelTypeDao;
	

	public boolean isTitleUnique(String title) {

		return metaStudyDao.isTitleUnique(title);
	}

	public MetaStudy saveMetaStudy(Account account, MetaStudy metaStudy) {

		boolean newMetaStudy = (metaStudy.getId() == null);

		Date now = new Date();
		metaStudy.setLastUpdatedDate(now);
		
		if (newMetaStudy) {
			metaStudy.setDateCreated(now);
			metaStudy.setStatus(MetaStudyStatus.DRAFT);
		}

		metaStudy = metaStudyDao.save(metaStudy);

		if (newMetaStudy) {
			// Build prefixId and save the change in the database
			StringBuilder sb = new StringBuilder();
			sb.append(modulesConstants.getModulesOrgName(Long.valueOf(account.getDiseaseKey())));
			sb.append(ModelConstants.PREFIX_META_STUDY);

			int idStrLength = metaStudy.getId().toString().length();
			for (int i = 7; i > idStrLength; i--) {
				sb.append(ModelConstants.PREFIX_LEAD_NUM);
			}
			sb.append(metaStudy.getId());

			metaStudy.setPrefixId(sb.toString());
			metaStudy = metaStudyDao.save(metaStudy);
		}

		return metaStudy;
	}

	public void addAccessRecord(MetaStudy metaStudy, Account userAccount, MetaStudyDocumentation supportingDoc, MetaStudyData dataFile){
		metaStudyAccessRecordDao.save(new MetaStudyAccessRecord(userAccount, supportingDoc, dataFile, metaStudy));
		return;
	}


	/**
	 * This method returns a MetaStudy object from the ID passed in.
	 * @return a MetaStudy object based on the ID passed in.
	 */
	public MetaStudy getMetaStudyById(long id) {
		// Change to eager loading
		return metaStudyDao.get(id);
	}
	
	public MetaStudy getPublicMetaStudyById(Long metaStudyId){
		return metaStudyDao.getPublicMetaStudyById(metaStudyId);
	}


	/**
	 * Returns a list of MetaStudy objects from the Set of IDs
	 * @return list of MetaStudy objects based on the Set of IDs.
	 */
	public List<MetaStudy> getMetaStudyListByIds(Set<Long> ids) {
		return metaStudyDao.getMetaStudyListByIds(ids);
	}
	
	/**
	 * Returns a list of MetaStudy objects from the Set of IDs, based on published flag
	 * @return list of MetaStudy objects based on the Set of IDs and published flag.
	 */
	public List<MetaStudy> getMetaStudyListFilterByStatus(Set<Long> ids, Set<MetaStudyStatus> status) {
		return metaStudyDao.getMetaStudyListFilterByStatus(ids,status);
	}
	
	
	/**
	 * Returns a list of MetaStudy objects from the Set of IDs, based on published flag, and user account role
	 * @return list of MetaStudy objects based on the Set of IDs and published flag.
	 */
	public List<MetaStudy> getMetaStudyListFilterByStatus(Account account, Set<MetaStudyStatus> status) {
		
		Set<Long> ids = null;
		if (isMetaStudyAdmin(account)) {
			return getMetaStudyListFilterByStatus(ids,status);
		} else {
			//get users with WRITE permission	
			// get the list of valid saved Query ids
			ids = accountManager.listUserAccess(account, EntityType.META_STUDY,PermissionType.WRITE);		
		}
		if(!ids.isEmpty()){
			return getMetaStudyListFilterByStatus(ids,status);
		} else {
			return new ArrayList<MetaStudy>();
		}
	}

	public List<MetaStudyKeyword> searchKeywords(String searchKey) {

		List<MetaStudyKeyword> keywords = (List<MetaStudyKeyword>)keywordDao.search(searchKey, MetaStudyKeyword.class);

		return keywords;
	}

	public List<MetaStudyLabel> searchLabels(String searchKey) {
		return metaStudyLabelDao.search(searchKey);
	}

	public List<MetaStudy> getAllMetaStudies() {
		return metaStudyDao.getAll();
	}


	/**
	 * This method will be used to upload user file to sftp server and returns a UserFile object without saving it in
	 * the database. With this approach we don't need to save file content in the memory but uploaded files may be left
	 * at sftp server if user cancels the process.
	 * 
	 * @param userId
	 * @param uploadFile
	 * @param fileName
	 * @param fileDescription
	 * @param fileType
	 * @param uploadDate
	 * @return UserFile object
	 * @throws SocketException
	 * @throws IOException
	 * @throws JSchException
	 */
	public UserFile uploadFile(Long userId, File uploadFile, String fileName, String fileDescription, String fileType,
			Date uploadDate) throws SocketException, IOException, JSchException {

		FileType currentFileType = fileTypeDao.get(fileType);
		String filePath = ServiceConstants.META_STUDY_FILE_PATH + userId + ServiceConstants.FILE_SEPARATER;

		DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);
		
		if(info != null) {
			logger.info(" MetaStudyManagerImpl :: uploadFile :: " +
					" , dataFileEndpointInfo name : " + info.getEndpointName() + 
					" , dataFileEndpointInfo url  : " + info.getUrl() + 
					" , filename : " + fileName + ", " + 
					" , filepath : " + filePath);
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

		try {
			client.upload(uploadFile, filePath, fileName);
		} catch (Exception e) {
			logger.error("Exception occured when uploading file " + filePath + " " + fileName);
			throw new IOException(e);
		}

		SftpClientManager.closeAll();

		return userFile;
	}
	
	/*
	 * private function to determine if the user has admin access to the meta study module
	 */
	private boolean isMetaStudyAdmin(Account account){
		//loop over all the user's roles
		for(AccountRole accountRole : account.getAccountRoleList()){
			//if the user is a global admin and the roles is active they have admin meta study access
			if(accountRole.getRoleType().equals(RoleType.ROLE_ADMIN) && accountRole.getIsActive()){
				return true;
			}
			//if the user is a meta study admin and the roles is active
			else if(accountRole.getRoleType().equals(RoleType.ROLE_METASTUDY_ADMIN) && accountRole.getIsActive()){
				return true;
			}
		}
		//no active admin roles were associated with the account
		return false;
	}


	/**
	 * This method saves the UserFile object in the database and returns it, it's called when user submits the Meta
	 * Study form. 
	 * @return the saved userFile.
	 */
	public UserFile saveUserFile(UserFile userFile) {

		if (userFile != null) {
			userFile = userFileDao.save(userFile);
		}
		return userFile;
	}

	public void delete(Long id) {
		metaStudyDao.remove(id);
	}

	/**
	 * This method removes the UserFile from database.
	 */
	public void removeUserFile(UserFile userFile) {

		if (userFile != null && userFile.getId() != null) {
			userFileDao.remove(userFile.getId());
		}
	}

	/**
	 * @return SavedQuery file type
	 */
	public FileType getSavedQueryFileType() {
		return fileTypeDao.get(ServiceConstants.FILE_TYPE_SAVED_QUERY);
	}

	public List<MetaStudyKeyword> retrieveAllKeywords() {
		return (List<MetaStudyKeyword>)keywordDao.getAllKeywords(MetaStudyKeyword.class);
	}

	public Long getMetaStudyKeywordCount(String keyword) {
		return keywordDao.getCountByKeyword(keyword, MetaStudyKeyword.class);
	}

	public List<MetaStudyLabel> retrieveAllLabels() {
		return metaStudyLabelDao.getAll();
	}

	public Long getMetaStudyLabelCount(String label) {
		return metaStudyLabelDao.getCountByLabel(label);
	}
	
	public MetaStudyAccessRecord saveMetaStudyAccessRecord(MetaStudyAccessRecord accessRecord){
		return metaStudyAccessRecordDao.save(accessRecord);
	}
	
	public List<MetaStudyAccessRecord> getAccessRecordByMetaStudyId(Long id){
		return metaStudyAccessRecordDao.getAccessRecordByMetaStudyId(id);
	}

	@Override
	public SavedQuery cloneSavedQuery(SavedQuery originalQuery, String description) {
		SavedQuery clone = new SavedQuery(originalQuery);
		clone.setId(null);
		clone.setCopyFlag(true);
		clone.setLastUpdated(new Date());
		clone.setDescription(description);
		clone.setName(originalQuery.getName() + ServiceConstants.META_NAME_SUFFIX);

		clone = savedQueryDao.save(clone);

		return clone;
	}
	
	public void linkSavedQuery(Account account, long metaStudyId, long savedQueryId){
		
		MetaStudy linkMetaStudy = this.getMetaStudyById(metaStudyId);

		
		Set<MetaStudyData> dataSet = linkMetaStudy.getMetaStudyDataSet();
		MetaStudyData newMSD = new MetaStudyData();
		SavedQuery linkQuery = savedQueryDao.get(savedQueryId);
		FileType fileType = fileTypeDao.get(ServiceConstants.FILE_TYPE_SAVED_QUERY);
		//set copy flag to true to saved query
		linkQuery.setCopyFlag(true);
		//add saved query to MSD
		newMSD.setSavedQuery(linkQuery);
		newMSD.setDescription(linkQuery.getDescription());
		newMSD.setDateCreated(new Date());
		newMSD.setMetaStudy(linkMetaStudy);
		newMSD.setFileType(fileType);
		newMSD.setSource(ServiceConstants.QUERY_TOOL);
		dataSet.add(newMSD);
		
		// set the MS status back to draft if it's in awaiting publication
		if (MetaStudyStatus.AWAITING_PUBLICATION.equals(linkMetaStudy.getStatus())) {
			linkMetaStudy.setStatus(MetaStudyStatus.DRAFT);
		}

		linkMetaStudy = saveMetaStudy(account, linkMetaStudy);
	}
	
	public boolean isMetaStudyDataTitleUnique(String fileName, long metaStudyId){
		return metaStudyDataDao.isMetaStudyDataTitleUnique(fileName, metaStudyId);
	}
	

	public MetaStudy assocaiteDataFileToMetaStudy(long metaStudyId, String fileDescription, String fileName, String filePath, long fileSize, Account userAccount){
		
		MetaStudy metaStudy = getMetaStudyById(metaStudyId);
		FileType fileType = fileTypeDao.getFileTypeByNameAndClassification(ServiceConstants.FILE_TYPE_RESULTS,FileClassification.META_STUDY_DATA);
		
		if(metaStudy.isPublished()){
			return null;
		}
		
		UserFile userFile = new UserFile();
		userFile.setDatafileEndpointInfo(datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID));
		userFile.setDescription(fileDescription);
		userFile.setFileType(fileType);
		userFile.setName(fileName);
		userFile.setPath(filePath);
		userFile.setSize(fileSize);
		userFile.setUploadedDate(new Date());
		userFile.setUserId(userAccount.getUserId());
		
		UserFile storedFile = saveUserFile(userFile);
		
		MetaStudyData dataFileWrapper =null;	
		Set<MetaStudyData> metaStudyDatasets =new HashSet<MetaStudyData>();
		if(metaStudy.getMetaStudyDataSet()!=null){
			 metaStudyDatasets = metaStudy.getMetaStudyDataSet();
		}
		
		for(MetaStudyData metaStudyData:metaStudyDatasets){
			if(metaStudyData.getUserFile()!=null){
				if(metaStudyData.getUserFile().getName().equalsIgnoreCase(fileName)){
					dataFileWrapper=metaStudyData;
				}
			}
		}
		
		if(dataFileWrapper==null){
			dataFileWrapper = new MetaStudyData();
		}
		
		dataFileWrapper.setDateCreated(new Date());
		dataFileWrapper.setDescription(userFile.getDescription());
		dataFileWrapper.setFileType(fileType);
		dataFileWrapper.setMetaStudy(metaStudy);
		dataFileWrapper.setSource(ServiceConstants.QUERY_TOOL);
		dataFileWrapper.setUserFile(storedFile);
		dataFileWrapper.setVersion(ServiceConstants.VERSION_ONE);
		
		metaStudy.addMetaStudyData(dataFileWrapper);
		
		metaStudyDao.save(metaStudy);
		
		return metaStudy;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createDoiForMetaStudy(MetaStudy metaStudy)
			throws IllegalStateException, WebApplicationException, DoiWsValidationException {
		// Find the owner of this meta study.
		List<EntityMap> entities = accountManager.listEntityAccess(metaStudy.getId(), EntityType.META_STUDY);
		Account owner = null;

		for (EntityMap em : entities) {
			if (em.getPermission() == PermissionType.OWNER) {
				owner = em.getAccount();
				break;
			}
		}

		// Check if an owner was found.
		if (owner == null) {
			throw new IllegalStateException("No owner was found for the " + metaStudy.getTitle() + " meta study.");
		}

		// Create a DOI record object from the meta study data.
		DoiUtil doiUtil = new DoiUtil(modulesConstants);
		OSTIRecord record = null;

		try {
			record = doiUtil.modelToDoiRecord(EntityType.META_STUDY, metaStudy, owner);
		} catch (IllegalArgumentException iae) {
			throw new IllegalStateException("Error while translating meta study to a DOI record object.", iae);
		}

		// Save the new DOI record to the IAD web service.
		DoiMinterProvider doiProvider = new DoiMinterProvider(modulesConstants.getIadWsUrl(),
				modulesConstants.getIadUsername(), modulesConstants.getIadPassword());

		doiProvider.saveDoiToIad(record);

		// Record new DOI and OSTI ID data in the given meta study object.
		metaStudy.setDoi(record.getDoi());
		metaStudy.setOstiId(record.getOstiId());
	}

	public List<MetaStudy> metaStudyPublicSearch(){
		return metaStudyDao.metaStudyPublicSiteSearch();
	}
	
	public List<SupportingDocumentation> getPublicationsByMetastudyId(Long metaStudyId){
		return supportingDocumentationDao.getMetastudyPublicationDocumentation(metaStudyId);
	}
	
	public ResearchManagementMeta getMetaStudyManagementImage(Long metastudyId, Long rmId) {
		return researchManagementMetaDao.getMetaStudyManagementImage(metastudyId, rmId);
	}
	
	public ResearchManagementMeta getMetastudyPrimaryInvestigatorImage(Long metastudyId) {
		return researchManagementMetaDao.getMetastudyPrimaryInvestigator(metastudyId);
	}
	
	public SupportingDocumentation getPublicationDocument(Long metaStudyId, Long supportingDocId) throws Exception{
		return supportingDocumentationDao.getPublicationDocumentation(supportingDocId);
	}
	
	public byte[] getFileByteArray(UserFile userFile) throws Exception {

		if(userFile != null) {
			logger.info(" MetaStudyManagerImpl :: getFileByteArray :: filePath : " + userFile.getPath() + 
					" , fileName : " + userFile.getName() + " , fileid : " + userFile.getId());
		
			if(userFile.getDatafileEndpointInfo() != null) {
				logger.info(" MetaStudyManagerImpl :: getFileByteArray :: " +
						" , dataFileEndpointInfo name : " + userFile.getDatafileEndpointInfo().getEndpointName() + 
						" , dataFileEndpointInfo url  : " + userFile.getDatafileEndpointInfo().getUrl());
			}
		}	
		
		
		SftpClient client = SftpClientManager.getClient(userFile.getDatafileEndpointInfo());

		return client.downloadBytes(userFile.getName(), userFile.getPath());
	}
	
	public boolean isMetaStudyPublic(Long metaStudyId){
		MetaStudy metaStudy = metaStudyDao.getBasicMetaStudy(metaStudyId);
		
		if(metaStudy == null || metaStudy.getStatus() != MetaStudyStatus.PUBLISHED){
			return false;
		} else {
			return true;
		}
	}

	@Override
	public int countAccessRecords(Long metaStudyId) {
		return metaStudyAccessRecordDao.countAccessRecords(metaStudyId);
	}

	@Override
	public List<MetaStudyAccessRecord> searchAccessRecords(MetaStudy metastudy, String searchText, Long daysOld,
			PaginationData pageData) {

		Date queueDate = null;

		if (searchText != null && !searchText.trim().isEmpty()) {
			// Again using an exception to determine if string is a valid date. Test code using
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

		return metaStudyAccessRecordDao.searchAccessRecords(metastudy.getId(), searchText, queueDate, oldestDate,
				pageData);

	}
	
	@Override
	public List<MetaStudyAccessRecord> getMetaStudyAccessRecords(MetaStudy metaStudy, String startDateStr,
			String endDateStr, PaginationData pageData) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void saveMetaStudyTherapeuticAgent(Set<MetaStudyTherapeuticAgent> therapeuticAgentSet, String metaStudyId) {
		
		List<MetaStudyTherapeuticAgent> list = metaStudyDao.getTherapeuticAgentsForMetaStudy(metaStudyId);
		
		Set<MetaStudyTherapeuticAgent> insertRecords = new HashSet<MetaStudyTherapeuticAgent>();
		Set<MetaStudyTherapeuticAgent> deleteRecords = new HashSet<MetaStudyTherapeuticAgent>();
		
		if(therapeuticAgentSet == null) {
			deleteRecords.addAll(list);
		}else if(therapeuticAgentSet.size() == 0 && list.size() > 0) {
			deleteRecords.addAll(list);
		}
		
		if(therapeuticAgentSet != null) {
			for(MetaStudyTherapeuticAgent s : therapeuticAgentSet) {
				
				boolean recordInsert = true;
				
				for(MetaStudyTherapeuticAgent tAgent : list) {
					if(tAgent.getTherapeuticAgent().getId().equals( s.getTherapeuticAgent().getId())) {
						recordInsert = false;
						break;
					}
				}
				
				if(recordInsert) {
					insertRecords.add(s);
				}
			}
	
			for(MetaStudyTherapeuticAgent tAgent : list) {
				boolean recordDelete = true;
				
				for(MetaStudyTherapeuticAgent s : therapeuticAgentSet) {
					if(tAgent.getTherapeuticAgent().getId().equals( s.getTherapeuticAgent().getId())) {
						recordDelete = false;
						break;
					}
				}
				
				if(recordDelete) {
					deleteRecords.add(tAgent);
				}
			}
		}
		
		
		int batchSize = ServiceConstants.META_STUDY_BACTH_SIZE;
		
		List<MetaStudyTherapeuticAgent> insertRecordsList = new ArrayList<MetaStudyTherapeuticAgent>(insertRecords);
		List<List<MetaStudyTherapeuticAgent>> insertBatchList = Lists.partition(insertRecordsList, batchSize);
		
		for(List<MetaStudyTherapeuticAgent> s: insertBatchList) {
			therapeuticAgentDataDao.saveTherapeuticAgentDataBulk(s, metaStudyId);
		}
		
		List<MetaStudyTherapeuticAgent> deleteRecordList = new ArrayList<MetaStudyTherapeuticAgent>(deleteRecords);
		List<List<MetaStudyTherapeuticAgent>> deleteBatchList = Lists.partition(deleteRecordList, batchSize);
		
		for(List<MetaStudyTherapeuticAgent> s : deleteBatchList) {
			therapeuticAgentDataDao.deleteTherapeuticAgentDataBulk(s, metaStudyId);
		}
		
	}

	@Override
	public void saveMetaStudyTherapyType(Set<MetaStudyTherapyType> therapyTypeSet, String metaStudyId) {
		
		List<MetaStudyTherapyType> list = metaStudyDao.getTherapyTypesForMetaStudy(metaStudyId);
		
		Set<MetaStudyTherapyType> insertRecords = new HashSet<MetaStudyTherapyType>();
		Set<MetaStudyTherapyType> deleteRecords = new HashSet<MetaStudyTherapyType>();

		
		if(therapyTypeSet == null) {
			deleteRecords.addAll(list);
		}else if(therapyTypeSet.size() == 0 && list.size() > 0) {
			deleteRecords.addAll(list);
		}
		
		if(therapyTypeSet != null) {
	
			for(MetaStudyTherapyType s : therapyTypeSet) {
				
				boolean recordInsert = true;
				
				for(MetaStudyTherapyType tAgent : list) {
					if(tAgent.getTherapyType().getId().equals( s.getTherapyType().getId())) {
						recordInsert = false;
						break;
					}
				}
				
				if(recordInsert) {
					insertRecords.add(s);
				}
			}
	
			for(MetaStudyTherapyType tAgent : list) {
				boolean recordDelete = true;
				
				for(MetaStudyTherapyType s : therapyTypeSet) {
					if(tAgent.getTherapyType().getId().equals(s.getTherapyType().getId())) {
						recordDelete = false;
						break;
					}
				}
				
				if(recordDelete) {
					deleteRecords.add(tAgent);
				}
			}
		}	
		
		int batchSize = ServiceConstants.META_STUDY_BACTH_SIZE;
		
		List<MetaStudyTherapyType> insertRecordsList = new ArrayList<MetaStudyTherapyType>();
		List<List<MetaStudyTherapyType>> insertBatchList = Lists.partition(insertRecordsList, batchSize);
			
		for(List<MetaStudyTherapyType> s: insertBatchList) {
			therapyTypesDataDao.saveTherapyTypeDataBulk(s, metaStudyId);
		}
		
		List<MetaStudyTherapyType> deleteRecordsList = new ArrayList<MetaStudyTherapyType> (deleteRecords);
		List<List<MetaStudyTherapyType>> deleteBatchList = Lists.partition(deleteRecordsList,batchSize);
		
		for(List<MetaStudyTherapyType> s: deleteBatchList) {
			therapyTypesDataDao.deleteTherapyTypeDataBulk(s, metaStudyId);
		}
	}

	@Override
	public void saveMetaStudyTherapeuticTarget(Set<MetaStudyTherapeuticTarget> therapeuticTargetSet,
			String metaStudyId) {
		
		List<MetaStudyTherapeuticTarget> list = metaStudyDao.getTherapeuticTargetsForMetaStudy(metaStudyId);
		
		Set<MetaStudyTherapeuticTarget> insertRecords = new HashSet<MetaStudyTherapeuticTarget>();
		Set<MetaStudyTherapeuticTarget> deleteRecords = new HashSet<MetaStudyTherapeuticTarget>();
		
		if(therapeuticTargetSet == null) {
			deleteRecords.addAll(list);
		}else if(therapeuticTargetSet.size() == 0 && list.size() > 0) {
			deleteRecords.addAll(list);
		}
		
		if(therapeuticTargetSet != null) {
		
			for(MetaStudyTherapeuticTarget s : therapeuticTargetSet) {
				
				boolean recordInsert = true;
				
				for(MetaStudyTherapeuticTarget tAgent : list) {
					if(tAgent.getTherapeuticTarget().getId().equals(s.getTherapeuticTarget().getId())) {
						recordInsert = false;
						break;
					}
				}
				
				if(recordInsert) {
					insertRecords.add(s);
				}
			}
	
			for(MetaStudyTherapeuticTarget tAgent : list) {
				boolean recordDelete = true;
				
				for(MetaStudyTherapeuticTarget s : therapeuticTargetSet) {
					if(tAgent.getTherapeuticTarget().getId().equals( s.getTherapeuticTarget().getId())) {
						recordDelete = false;
						break;
					}
				}
				
				if(recordDelete) {
					deleteRecords.add(tAgent);
				}
			}
		}
		
		int batchSize = ServiceConstants.META_STUDY_BACTH_SIZE;
		
		List<MetaStudyTherapeuticTarget> insertRecordsList = new ArrayList<MetaStudyTherapeuticTarget>(insertRecords);
		List<List<MetaStudyTherapeuticTarget>> insertBatchList = Lists.partition(insertRecordsList, batchSize);
		
		for(List<MetaStudyTherapeuticTarget> s: insertBatchList) {
			therapeuticTargetDataDao.saveTherapeuticTargetDataBulk(s, metaStudyId);
		}
		
		List<MetaStudyTherapeuticTarget> deleteRecordsList = new ArrayList<MetaStudyTherapeuticTarget>(deleteRecords);
		List<List<MetaStudyTherapeuticTarget>> deleteBatchList = Lists.partition(deleteRecordsList, batchSize);
		
		for(List<MetaStudyTherapeuticTarget> s:deleteBatchList){
			therapeuticTargetDataDao.deleteTherapeuticTargetDataBulk(s, metaStudyId);
		}
	}

	@Override
	public void saveMetaStudyModelType(Set<MetaStudyModelType> modelTypeSet, String metaStudyId) {
	
		List<MetaStudyModelType> list = metaStudyDao.getModelTypesForMetaStudy(metaStudyId);
		
		Set<MetaStudyModelType> insertRecords = new HashSet<MetaStudyModelType>();
		Set<MetaStudyModelType> deleteRecords = new HashSet<MetaStudyModelType>();

		if(modelTypeSet == null) {
			deleteRecords.addAll(list);
		}else if(modelTypeSet.size() == 0 && list.size() > 0) {
			deleteRecords.addAll(list);
		}
		
		if(modelTypeSet != null) {

			for(MetaStudyModelType s : modelTypeSet) {
				
				boolean recordInsert = true;
				
				for(MetaStudyModelType tAgent : list) {
					if(tAgent.getModelType().getId().equals(s.getModelType().getId())) {
						recordInsert = false;
						break;
					}
				}
				
				if(recordInsert) {
					insertRecords.add(s);
				}
			}
	
			for(MetaStudyModelType tAgent : list) {
				boolean recordDelete = true;
				
				for(MetaStudyModelType s : modelTypeSet) {
					if(tAgent.getModelType().getId().equals(s.getModelType().getId())) {
						recordDelete = false;
						break;
					}
				}
				
				if(recordDelete) {
					deleteRecords.add(tAgent);
				}
			}
		}	
		
		int batchSize = ServiceConstants.META_STUDY_BACTH_SIZE;
		
		List<MetaStudyModelType> insertRecordsList = new ArrayList<MetaStudyModelType>(insertRecords);
		List<List<MetaStudyModelType>> insertBatchList = Lists.partition(insertRecordsList,batchSize);
		
		for(List<MetaStudyModelType> s:insertBatchList) {
			modelTypeDataDao.saveModelTypeDataBulk(s, metaStudyId);
		}
		
		List<MetaStudyModelType> deleteRecordsList = new ArrayList<MetaStudyModelType>(deleteRecords);
		List<List<MetaStudyModelType>> deleteBatchList = Lists.partition(deleteRecordsList,batchSize);
		
		for(List<MetaStudyModelType> s:deleteBatchList) {
			modelTypeDataDao.saveModelTypeDataBulk(s, metaStudyId);
		}
		
	}

	@Override
	public void saveMetaStudyModelName(Set<MetaStudyModelName> modelNameSet, String metaStudyId) {

		List<MetaStudyModelName> list = metaStudyDao.getModelNamesForMetaStudy(metaStudyId);
		
		Set<MetaStudyModelName> insertRecords = new HashSet<MetaStudyModelName>();
		Set<MetaStudyModelName> deleteRecords = new HashSet<MetaStudyModelName>();
		
		if(modelNameSet == null) {
			deleteRecords.addAll(list);
		}else if(modelNameSet.size() == 0 && list.size() > 0) {
			deleteRecords.addAll(list);
		}
		
		if(modelNameSet != null) {		
			for(MetaStudyModelName s : modelNameSet) {
				
				boolean recordInsert = true;
				
				for(MetaStudyModelName tAgent : list) {
					if(tAgent.getModelName().getId().equals(s.getModelName().getId())) {
						recordInsert = false;
						break;
					}
				}
				
				if(recordInsert) {
					insertRecords.add(s);
				}
			}
	
			for(MetaStudyModelName tAgent : list) {
				boolean recordDelete = true;
				
				for(MetaStudyModelName s : modelNameSet) {
					if(tAgent.getModelName().getId().equals(s.getModelName().getId())) {
						recordDelete = false;
						break;
					}
				}
				
				if(recordDelete) {
					deleteRecords.add(tAgent);
				}
			}
		}	
		
		int batchSize = ServiceConstants.META_STUDY_BACTH_SIZE;
		
		List<MetaStudyModelName> insertRecordsList = new ArrayList<MetaStudyModelName>(insertRecords);
		List<List<MetaStudyModelName>> insertBatchList = Lists.partition(insertRecordsList,batchSize);
		
		for(List<MetaStudyModelName> s:insertBatchList) {
			modelNameDataDao.saveModelNameDataBulk(s, metaStudyId);
		}
		
		List<MetaStudyModelName> deleteRecordsList = new ArrayList<MetaStudyModelName>(deleteRecords);
		List<List<MetaStudyModelName>> deleteBatchList = Lists.partition(deleteRecordsList,batchSize);
		for(List<MetaStudyModelName> s: deleteBatchList) {
			modelNameDataDao.deleteModelNameDataBulk(s, metaStudyId);
		}
		
	}
	
	@Override
	public List<TherapeuticAgent> getAllTherapeuticAgents() {
		return therapeuticAgentDao.getAll();
	}
	
	@Override
	public List<TherapyType> getAllTherapyTypes() {
		return therapyTypesDao.getAll();
	}

	@Override
	public List<TherapeuticTarget> getAllTherapeuticTargets() {
		return therapeuticTargetDao.getAll();
	}
	
	@Override
	public List<ModelName> getAllModelNames() {
		return modelNameDao.getAll();
	}
	
	@Override
	public List<ModelType> getAllModelTypes() {
		return modelTypeDao.getAll();
	}
	
}
