package gov.nih.tbi.commons.service;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import com.jcraft.jsch.JSchException;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.MetaStudyStatus;
import gov.nih.tbi.commons.model.exceptions.DoiWsValidationException;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyAccessRecord;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyDocumentation;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyKeyword;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyLabel;
import gov.nih.tbi.metastudy.model.hibernate.ResearchManagementMeta;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
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
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public interface MetaStudyManager {

	public boolean isTitleUnique(String title);
	
	public MetaStudy saveMetaStudy(Account account, MetaStudy metaStudy);
	
	public MetaStudy getMetaStudyById(long id);
	
	public List<MetaStudy> getMetaStudyListByIds(Set<Long> ids);
	
	public List<MetaStudy> getMetaStudyListFilterByStatus(Set<Long> ids, Set<MetaStudyStatus> status);
	
	public List<MetaStudy> getMetaStudyListFilterByStatus(Account account, Set<MetaStudyStatus> status);
	
	public UserFile uploadFile(Long userId, File uploadFile, String fileName, String fileDescription, String fileType,
			Date uploadDate) throws SocketException, IOException, JSchException;

	public UserFile saveUserFile(UserFile userFile);
	
	public void removeUserFile(UserFile userFile);
	
	public FileType getSavedQueryFileType();
	
	public List<MetaStudyKeyword> searchKeywords(String searchKey);
	
	public List<MetaStudyLabel> searchLabels(String searchKey);
	
	public List<MetaStudyKeyword> retrieveAllKeywords();
	
	public List<MetaStudyLabel> retrieveAllLabels();
	
	public List<MetaStudy> getAllMetaStudies();

	public void delete(Long id);
	
	public Long getMetaStudyKeywordCount(String keyword);
	
	public Long getMetaStudyLabelCount(String label);
	
	public void addAccessRecord(MetaStudy metaStudy, Account userAccount, MetaStudyDocumentation supportingDoc,
			MetaStudyData dataFile);
	
	public MetaStudyAccessRecord saveMetaStudyAccessRecord(MetaStudyAccessRecord accessRecord);
	
	public List<MetaStudyAccessRecord> getAccessRecordByMetaStudyId(Long id);
	
	/**
	 * Given a metaStudyId, returns the number of access records for that metaStudyId.
	 * 
	 * @param metaStudyId
	 * @return
	 */
	public int countAccessRecords(Long metaStudyId);

	SavedQuery cloneSavedQuery(SavedQuery originalQuery, String description);
	
	public void linkSavedQuery(Account account, long metaStudyId, long savedQueryId);
	
	public boolean isMetaStudyDataTitleUnique(String fileName, long metaStudyId);
	
	public MetaStudy assocaiteDataFileToMetaStudy(long metaStudyId, String fileDescription, String fileName, String filePath, long fileSize, Account userAccount);

	/**
	 * Creates a DOI record with the OSTI/IAD system for the passed in meta study object. Once the DOI is successfully
	 * created, the given meta study object's DOI and OSTI ID properties will be updated with the values returned by the
	 * IAD web service.
	 * 
	 * @param metaStudy - The MetaStudy object to which a DOI will be created for.
	 * @throws IllegalStateException When there is an error while translating the given MetaStudy object to a DOI
	 *         record.
	 * @throws WebApplicationException When there is an error while sending the DOI create request to the IAD web
	 *         service.
	 * @throws DoiWsValidationException When the response back from the IAD web service indicates that there was a
	 *         validation error for the sent DOI record.
	 */
	public void createDoiForMetaStudy(MetaStudy metaStudy)
			throws IllegalStateException, WebApplicationException, DoiWsValidationException;
	
	public List<MetaStudy> metaStudyPublicSearch();
	
	public List<SupportingDocumentation> getPublicationsByMetastudyId(Long metaStudyId);
	
	public ResearchManagementMeta getMetastudyPrimaryInvestigatorImage(Long metastudyId);
	
	public SupportingDocumentation getPublicationDocument(Long metaStudyId, Long supportingDocId) throws Exception;
	
	public MetaStudy getPublicMetaStudyById(Long metaStudyId);
	
	public byte[] getFileByteArray(UserFile userFile) throws Exception;
	
	public boolean isMetaStudyPublic(Long metaStudyId);
	
	public ResearchManagementMeta getMetaStudyManagementImage(Long metastudyId, Long rmId);

	public List<MetaStudyAccessRecord> searchAccessRecords(MetaStudy metaStudy, String searchText, Long daysOld,
			PaginationData pageData);

	public List<MetaStudyAccessRecord> getMetaStudyAccessRecords(MetaStudy metaStudy, String startDateStr, String endDateStr,
			PaginationData pageData);
	
    public void saveMetaStudyTherapeuticAgent(Set<MetaStudyTherapeuticAgent> therapeuticAgentSet, String metaStudyId);
	
	public void saveMetaStudyTherapyType(Set<MetaStudyTherapyType> therapyTypeSet, String metaStudyId);
	
	public void saveMetaStudyTherapeuticTarget(Set<MetaStudyTherapeuticTarget> therapeuticTargetSet, String metaStudyId);
	
	public void saveMetaStudyModelType(Set<MetaStudyModelType> modelTypeSet, String metaStudyId);
	
	public void saveMetaStudyModelName(Set<MetaStudyModelName> modelNameSet, String metaStudyId);
	
	public List<TherapeuticAgent> getAllTherapeuticAgents();
	
	public List<TherapyType> getAllTherapyTypes();
	
	public List<TherapeuticTarget> getAllTherapeuticTargets();
	
	public List<ModelName> getAllModelNames();
	
	public List<ModelType> getAllModelTypes();
}
