package gov.nih.tbi.repository.portal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.ClinicalStudy;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RecruitmentStatus;
import gov.nih.tbi.commons.model.ResearchManagementRole;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.model.exceptions.DoiWsValidationException;
import gov.nih.tbi.commons.model.hibernate.Country;
import gov.nih.tbi.commons.model.hibernate.State;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.util.BRICSFilesUtils;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.SemanticFormStructureList;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.idt.ws.IdtColumnDescriptor;
import gov.nih.tbi.idt.ws.IdtFilterDescription;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.IdtRequest;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.repository.dao.BasicStudyDao;
import gov.nih.tbi.repository.dao.StudyDao;
import gov.nih.tbi.repository.model.StudyDetailsForm;
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
import gov.nih.tbi.repository.model.hibernate.BasicStudySearch;
import gov.nih.tbi.repository.model.hibernate.ClinicalTrial;
import gov.nih.tbi.repository.model.hibernate.FundingSource;
import gov.nih.tbi.repository.model.hibernate.Grant;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudyForm;
import gov.nih.tbi.repository.model.hibernate.StudyKeyword;
import gov.nih.tbi.repository.model.hibernate.StudySite;
import gov.nih.tbi.repository.model.hibernate.StudySponsorInfo;
import gov.nih.tbi.repository.model.hibernate.StudySupportingDocumentation;
import gov.nih.tbi.repository.model.hibernate.StudyType;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.service.util.AccessReportExportUtil;
import gov.nih.tbi.taglib.datatableDecorators.AccessReportIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.GrantIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.ResearchMgmtIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.SponsorInfoIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.StudyFsIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.StudyListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.StudySiteIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.SupportDocIdtListDecorator;

/**
 * Contains actions for study related requests.
 * 
 * @author Michael Valeiras
 */
public class StudyAction extends BaseRepositoryAction {

	private static final long serialVersionUID = -6130441485707145645L;

	private static Logger logger = Logger.getLogger(StudyAction.class);

	@Autowired
	private StudyDao studyDao;
	
	private Study currentStudy;
	private StudyDetailsForm studyDetailsForm;
	private ClinicalStudy currentClinicalStudy;

	private List<Study> studyList;
	private Map<Long, EntityMap> permissionList;

	// PaginationData
	private Integer numSearchResults;
	private Integer page;
	private Integer pageSize;
	private Boolean ascending;
	private String sort;
	private String key;
	private Long ownerId;
	private Long filterId;
	private Long daysOld;

	private StudySponsorInfo sponsorInfoEntry;
	private String clinicalTrialId;
	private ResearchManagement researchMgmtEntry;
	private String selectedResMgmtToEdit;

	private StudySite siteEntry;
	private Grant grantEntry;

	private StudyKeyword newKeyword;
	private String keywordSearchKey;

	// Research Management Picture
	private File picture;
	private String pictureContentType;
	private String pictureFileName;

	// Data Submission Document
	private File upload;
	private String uploadContentType;
	private String uploadFileName;


	// Access report download
	private String startAccessReportDate;
	private String endAccessReportDate;
	private String exportFileName;
	private InputStream inputStream;
	private String selectedStudies;

	private StudyForm studyFormEntry;
	private String responseJson = "{}";
	private String inputJson = "{}";
	private String errRespMsg;
	private Long fsId;

	private Set<StudyTherapeuticAgent> therapeuticAgentSet;
	private Set<StudyTherapeuticTarget> therapeuticTargetSet;
	private Set<StudyTherapyType> therapyTypeSet;
	private Set<StudyModelName> modelNameSet;
	private Set<StudyModelType> modelTypeSet;

	private static final String COLUMN_ACCESSREPORT_DSID = "id";
	private static final String COLUMN_ACCESSREPORT_NAME = "name";
	private static final String COLUMN_ACCESSREPORT_STATUS = "status";
	private static final String COLUMN_ACCESSREPORT_USERNAME = "username";
	private static final String COLUMN_ACCESSREPORT_DATE = "date";
	private static final String COLUMN_ACCESSREPORT_RECORDCOUNT = "recordCount";
	private static final String COLUMN_ACCESSREPORT_DOWNLOADLOCATION = "source";

	@Autowired
	BasicStudyDao basicStudyDao;


	public Study getCurrentStudy() {

		if (getSessionStudy() != null) {
			return getSessionStudy().getStudy();
		} else {
			return null;
		}
	}

	public void setPermissionList(List<EntityMap> permissionList) {

		HashMap<Long, EntityMap> mapList = new HashMap<Long, EntityMap>();

		for (EntityMap entity : permissionList) {
			Long entityId = entity.getEntityId();

			EntityMap existingEntityMap = mapList.get(entityId);

			// if the entity map already exists, we want to add the entityMap with the highest level of permission type
			if (existingEntityMap != null) {
				PermissionType currentPermissionType = entity.getPermission();
				PermissionType existingPermissionType = existingEntityMap.getPermission();

				// if the current permission type is greater than the existing permission type, then we want to add the
				// current one to the map. Otherwise, do nothing!
				if (PermissionType.compare(currentPermissionType, existingPermissionType) > 0) {
					mapList.put(entity.getEntityId(), entity);
				}
			} else {
				mapList.put(entity.getEntityId(), entity);
			}
		}

		setPermissionList(mapList);
	}

	public EntityMap getPermissionList(int index) {

		if (index + 1 > permissionList.size()) {
			return null;
		}
		return permissionList.get(new Long(index));
	}

	public boolean getIsCreate() {
		return getCurrentStudy().getId() == null;
	}

	public boolean getIsRequest() {
		return StudyStatus.REQUESTED.equals(getCurrentStudy().getStudyStatus());
	}

	/***************************************************************************/

	/**
	 * A helper function to serve the total number of results pages to the jsp
	 * 
	 * @return
	 */
	public Integer getNumPages() {
		return (int) Math.ceil(((double) numSearchResults) / ((double) pageSize));
	}

	public RecruitmentStatus[] getRecruitmentStatuses() {
		return RecruitmentStatus.getStudyOnlyRecruitmentStatus();
	}

	public StudyType[] getStudyTypes() {
		
		List<StudyType> studyTypeList = staticManager.getStudyTypeList();
		StudyType[] studyTypeArray = new StudyType[studyTypeList.size()];
		studyTypeList.toArray(studyTypeArray);
		
		return studyTypeArray;
	}

	public ResearchManagementRole[] getRoleList() {
		return ResearchManagementRole.values();
	}

	public List<Country> getCountryList() {
		return staticManager.getCountryList();
	}

	public List<State> getStateList() {
		return staticManager.getStateList();
	}

	public List<FundingSource> getFundingSourceList() {

		List<FundingSource> fundingSortList = staticManager.getFundingSourceList();



		Collections.sort(fundingSortList, new Comparator<FundingSource>() {
			@Override
			public int compare(FundingSource lhs, FundingSource rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}
		});

		return fundingSortList;

	}

	/************************ ACTIONS *****************************/

	public StreamResult validationSuccess() {
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	public String showDownloadAccessReportLightbox() {
		return "accessRecordLightbox";
	}

	/**
	 * Navigates user to the list study page
	 * 
	 * @return
	 */
	public String list() {
		return PortalConstants.ACTION_LIST;
	}

	/**
	 * Search though study records and return a single page or results.
	 * 
	 * @return
	 */
	public String search() throws UnsupportedEncodingException {
		long startTime = System.nanoTime();
		// Constants for ownerId
		final long MINE = 0;
		PermissionType permission = PermissionType.READ;
		if (ownerId != null && ownerId == MINE) {
			permission = PermissionType.OWNER;
		}

		// Get the studies
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		setStudyList(repositoryManager.listStudies(getAccount(), PortalUtils.getProxyTicket(accountUrl), permission));

		// Set the permissions list. Looks different based on namespace (current user permissions or owner permissions).
		if (!getInAdmin()) {
			boolean getPublic = false;
			setPermissionList(accountManager.listUserAccessEntities(getAccount(), EntityType.STUDY, permission, getPublic));
		} else {
			// This list is generated in the service function list studies and could be reused to
			// increase performance.
			List<Long> ids = new ArrayList<Long>();
			for (Study s : studyList) {
				ids.add(s.getId());
			}
			List<EntityMap> ownerEntityList = accountManager.getOwnerEntityMaps(ids, EntityType.STUDY);
			setPermissionList(ownerEntityList);
		}
		long endTime = System.nanoTime();
		logger.info("Execution time for " + studyList.size() + " studies: " + (endTime - startTime));

		return PortalConstants.ACTION_SEARCH;
	}

	/**
	 * Saves form data to session
	 */
	public void saveSession() {

		currentStudy = getSessionStudy().getStudy();

		if (studyDetailsForm != null) {
			studyDetailsForm.adapt(currentStudy, getEnforceStaticFields());
		} else {
			studyDetailsForm = new StudyDetailsForm(currentStudy);
			studyDetailsForm.setTherapeuticAgentSet(getSessionStudy().getTherapeuticAgentSet());
			studyDetailsForm.setTherapeuticTargetSet(getSessionStudy().getTherapeuticTargetSet());
			studyDetailsForm.setTherapyTypeSet(getSessionStudy().getTherapyTypeSet());
			studyDetailsForm.setModelNameSet(getSessionStudy().getModelNameSet());
			studyDetailsForm.setModelTypeSet(getSessionStudy().getModelTypeSet());
		}

		getSessionStudy().saveKeywords();
		
		getSessionStudy().setTherapeuticAgentSet(studyDetailsForm.getTherapeuticAgentSet());
		getSessionStudy().setTherapeuticTargetSet(studyDetailsForm.getTherapeuticTargetSet());
		getSessionStudy().setTherapyTypeSet(studyDetailsForm.getTherapyTypeSet());
		getSessionStudy().setModelNameSet(studyDetailsForm.getModelNameSet());
		getSessionStudy().setModelTypeSet(studyDetailsForm.getModelTypeSet());
		
		getSessionStudy().setStudy(currentStudy);
		
	}

	/**
	 * Action for creation of new studies
	 * 
	 * @return
	 */
	public String create() {

		getSessionStudy().clear();
		currentStudy = new Study();

		// maybe auto-approved if admin is creating it
		currentStudy.setStudyStatus(StudyStatus.REQUESTED);
		initializeFormEntries();

		getSessionStudy().setStudy(currentStudy);
		addDefaultDataManager();

		return PortalConstants.ACTION_INPUT;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initializeFormEntries() {
		studyDetailsForm = new StudyDetailsForm(currentStudy);
		
		if(getSessionStudy().getStudy() != null && getSessionStudy().getStudy().getId() != null) {
			studyDetailsForm.setTherapeuticAgentSet(new HashSet(studyDao.getStudyTherapeuticAgents(getSessionStudy().getStudy().getId().toString())));
			studyDetailsForm.setTherapeuticTargetSet(new HashSet(studyDao.getStudyTherapeuticTargets(getSessionStudy().getStudy().getId().toString())));
			studyDetailsForm.setTherapyTypeSet(new HashSet(studyDao.getStudyTherapyTypes(getSessionStudy().getStudy().getId().toString())));
			studyDetailsForm.setModelNameSet(new HashSet(studyDao.getStudyModelNames(getSessionStudy().getStudy().getId().toString())));
			studyDetailsForm.setModelTypeSet(new HashSet(studyDao.getStudyModelTypes(getSessionStudy().getStudy().getId().toString())));
		}else {
			studyDetailsForm.setTherapeuticAgentSet(new HashSet());
			studyDetailsForm.setTherapeuticTargetSet(new HashSet());
			studyDetailsForm.setTherapyTypeSet(new HashSet());
			studyDetailsForm.setModelNameSet(new HashSet());
			studyDetailsForm.setModelTypeSet(new HashSet());
		}

		try {
			UserFile studyPicture = currentStudy.getGraphicFile();
			
			File tempFile = null;
			String graphicFileName = "";
			if(studyPicture!=null) {
				tempFile = repositoryManager.getFile(studyPicture);
				graphicFileName = studyPicture.getName();
			}

			studyDetailsForm.setUpload(tempFile);
			studyDetailsForm.setUploadFileName(graphicFileName);	
		} catch (Exception e) {
			logger.error("There was an error loading study picture", e);
		}
		sponsorInfoEntry = new StudySponsorInfo();
		researchMgmtEntry = new ResearchManagement();
		siteEntry = new StudySite();
		grantEntry = new Grant();
		studyFormEntry = new StudyForm();
	}

	// Add the login user as the default data manager
	private void addDefaultDataManager() {

		User loginUser = getUser();
		ResearchManagement dataMgr = new ResearchManagement();
		dataMgr.setResearchMgmtRole(ResearchManagementRole.DATA_MANAGER);
		dataMgr.setFirstName(loginUser.getFirstName());
		dataMgr.setLastName(loginUser.getLastName());
		dataMgr.setEmail(loginUser.getEmail());
		dataMgr.setOrgName(getAccount().getAffiliatedInstitution());

		getCurrentStudy().getResearchMgmtSet().add(dataMgr);
	}

	public String getPublishedFsList() {
		Study sessionStudy = getCurrentStudy();
		JsonObject output = new JsonObject();
		String dictionaryUrl = modulesConstants.getModulesDDTURL(getDiseaseId());
		SemanticFormStructureList allStructures =
				repositoryManager.getPublihedAndAwaitingFS(sessionAccount, PortalUtils.getProxyTicket(dictionaryUrl));
		List<SemanticFormStructure> structureList = allStructures.getList();

		logger.info("published FS list retrieved.  Results: " + structureList.size());

		JsonArray fsArray = new JsonArray();
		JsonArray selectedFsArray = new JsonArray();
		JsonArray nonSelectedFsArray = new JsonArray();

		for (int i = 0; i < structureList.size(); i++) {
			SemanticFormStructure sfs = structureList.get(i);
			JsonArray structArray = new JsonArray();
			// build the form structure array
			structArray.add(new JsonPrimitive(
					"<input type=\"checkbox\" name=\"fsList\" value=\"" + sfs.getShortNameAndVersion() + "\" />")); // built
																													 // by
																													 // the
																													 // javascript
																													 // on
																													 // the
																													 // front
			structArray.add(new JsonPrimitive(sfs.getShortNameAndVersion()));
			structArray.add(new JsonPrimitive(sfs.getTitle()));
			structArray.add(new JsonPrimitive(sfs.getSubmissionType().name()));
			structArray.add(new JsonPrimitive(sfs.getSubmissionType().getId()));
			structArray.add(new JsonPrimitive(sfs.getShortName()));
			structArray.add(new JsonPrimitive(sfs.getVersion()));
			structArray.add(new JsonPrimitive(generateDescriptionEllipsis(sfs.getDescription())));

			StudyForm form =
					new StudyForm(sfs.getShortName(), sfs.getVersion(), sfs.getTitle(), sfs.getSubmissionType());

			if (sessionStudy.getStudyForms().contains(form)) {
				structArray.add(new JsonPrimitive(sfs.getShortNameAndVersion()));
				selectedFsArray.add(structArray);
			} else {
				nonSelectedFsArray.add(structArray);
				structArray.add(new JsonPrimitive(""));
			}
		}
		fsArray.addAll(selectedFsArray);
		fsArray.addAll(nonSelectedFsArray);

		output.add("aaData", fsArray);
		responseJson = output.toString();
		return PortalConstants.ACTION_JSON_RESPONSE;
	}

	private JsonArray generateStudyFormListRow(StudyForm sf) {
		JsonArray fsElement = new JsonArray();
		String shortNameAndVersion = sf.getShortName() + "V" + sf.getVersion();
		String fsIdInput = "<input type=\"hidden\" id=\"" + shortNameAndVersion + "\" />";

		fsElement.add(new JsonPrimitive(fsIdInput + shortNameAndVersion));
		fsElement.add(new JsonPrimitive(sf.getTitle()));
		fsElement.add(new JsonPrimitive(sf.getShortName()));
		fsElement.add(new JsonPrimitive(sf.getSubmissionType().name()));
		fsElement.add(new JsonPrimitive(
				"<a href=\"javascript:;\" class=\"removeFsLink\" shortName=\"" + sf.getShortName() + "\" version=\""
						+ sf.getVersion() + "\" onclick=\"handleClickRemoveFsFromStudy(this)\">Remove</a>"));
		return fsElement;
	}

	private String generateDescriptionEllipsis(String desc) {
		String descLine = "";
		if (desc.length() > PortalConstants.ELLIPSIS_CHARACTER_COUNT) {
			descLine = "<span class=\"descBeginning\">";
			descLine += desc.substring(0, PortalConstants.ELLIPSIS_CHARACTER_COUNT + 1);
			descLine +=
					" <a href=\"javascript:;\" class=\"ellipsisExpandCollapse\" onclick=\"ellipsisExpandCollapse(this)\">...</a></span>";
			descLine += "<span class=\"descAll\">";
			descLine += desc;
			descLine +=
					" <a href=\"javascript:;\" class=\"ellipsisExpandCollapse\" onclick=\"ellipsisExpandCollapse(this)\">collapse</a></span>";
		} else {
			descLine = desc;
		}
		return descLine;
	}

	// http://fitbir-portal-local.cit.nih.gov:8080/portal/study/studyAction!getAllFormStructures.action
	public String getAllFormStructures() {
		String dictionaryUrl = modulesConstants.getModulesDDTURL(getDiseaseId());
		SemanticFormStructureList allStructures =
				repositoryManager.getPublihedAndAwaitingFS(sessionAccount, PortalUtils.getProxyTicket(dictionaryUrl));

		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<SemanticFormStructure> structureList = (ArrayList<SemanticFormStructure>) allStructures.getList();
			idt.setList(structureList);
			idt.setTotalRecordCount(structureList.size());
			idt.setFilteredRecordCount(structureList.size());
			idt.decorate(new StudyFsIdtDecorator(getSessionStudy()));
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * Fills in the "Study Form Structure" table to enable editing the form structure list from the local sessionStudy.
	 * 
	 * @return
	 */
	public String getFsForStudy() {
		JsonObject output = new JsonObject();
		JsonArray aaData = new JsonArray();
		Set<StudyForm> studyForms = getCurrentStudy().getStudyForms();
		for (StudyForm form : studyForms) {
			aaData.add(generateStudyFormListRow(form));
		}
		output.add("status", new JsonPrimitive("success"));
		output.add("aaData", aaData);

		responseJson = output.toString();
		return PortalConstants.ACTION_JSON_RESPONSE;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/study/studyAction!getFormList.action
	public String getFormList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<StudyForm> outputList = new ArrayList<StudyForm>(getCurrentStudy().getStudyForms());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Responds to a request to add an FS to the sessionStudy
	 * 
	 * @return response indicating that a JSON string will be returned in responseJson
	 */
	public String addFs() {
		JsonObject output = new JsonObject();
		JsonObject row = new JsonObject();
		Study sessionStudy = getSessionStudy().getStudy();
		sessionStudy.getStudyForms().add(studyFormEntry);

		// build row to go back to the front end
		StudyForm sf = studyFormEntry;
		row.add("DT_RowId", new JsonPrimitive(sf.getShortNameAndVersion()));
		row.add("title", new JsonPrimitive(sf.getTitle()));
		row.add("shortName", new JsonPrimitive(sf.getShortName()));
		row.add("submissionType", new JsonPrimitive(sf.getSubmissionType().name()));
		row.add("version", new JsonPrimitive(sf.getVersion()));

		output.add("status", new JsonPrimitive("success"));
		output.add("row", row);

		responseJson = output.toString();
		return PortalConstants.ACTION_JSON_RESPONSE;
	}

	/**
	 * Responds to a request from the front end to remove a FS from the sessionStudy
	 * 
	 * @return response indicating that a JSON string will be returned in responseJson
	 */
	public String removeFs() {
		JsonObject output = new JsonObject();
		Study sessionStudy = getSessionStudy().getStudy();
		boolean success = false;
		Set<StudyForm> forms = sessionStudy.getStudyForms();

		for (Iterator<StudyForm> it = forms.iterator(); it.hasNext();) {
			StudyForm form = it.next();
			if (studyFormEntry.getShortName().equals(form.getShortName())
					&& studyFormEntry.getVersion().equals(form.getVersion())) {
				it.remove();
				forms.remove(form);
				output.add("status", new JsonPrimitive("success"));
				success = true;
				break;
			}
		}

		if (!success) {
			output.add("status", new JsonPrimitive("failure"));
			output.add("reason", new JsonPrimitive("404"));
		}

		responseJson = output.toString();
		return PortalConstants.ACTION_JSON_RESPONSE;
	}

	/**
	 * Action for form submission.
	 * 
	 * @return
	 * @throws JSchException
	 * @throws IOException
	 * @throws SocketException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String submit() throws SocketException, IOException, JSchException {

		logger.info("Submit Study " + getSessionStudy().getStudy().getTitle());
		saveSession();

		getDatasetCount(getSessionStudy().getStudy());

		String result;

		if (getSessionUploadFile().getUploadFile() != null && upload == null) {
			upload = getSessionUploadFile().getUploadFile();
		}
		// upload the data submission document if new study
		if (getIsCreate()) {
			currentStudy.setDateCreated(new Date());
			if (!StringUtils.isEmpty(uploadFileName)){			
				UserFile submissionFile = repositoryManager.uploadFile(getUser().getId(), upload, uploadFileName, null,
						ServiceConstants.FILE_TYPE_STUDY, new Date());
				currentStudy.setDataSubmissionDocument(submissionFile);
			}
			if(studyDetailsForm.getUpload() != null) {
			UserFile graphicFile = repositoryManager.uploadFile(getUser().getId(), studyDetailsForm.getUpload(), 
					studyDetailsForm.getUploadFileName(), null, ServiceConstants.FILE_TYPE_STUDY, new Date());

			currentStudy.setGraphicFile(graphicFile);
			}

			if (!accountManager.hasRole(getAccount(), RoleType.ROLE_STUDY_ADMIN)) {
				result = PortalConstants.ACTION_CONFIRM;
			} else {
				result = PortalConstants.ACTION_VIEW;
			}
		} else {
			result = PortalConstants.ACTION_VIEW;
		}

		// Entity Map Stuff
		EntityMap toRemove = null;
		if (currentStudy.getIsPrivate() && getSessionStudy().getEntityMapList() != null) {
			for (EntityMap entity : getSessionStudy().getEntityMapList()) {
				if (entity.getPermissionGroup() != null
						&& ServiceConstants.PUBLIC_STUDY.equals(entity.getPermissionGroup().getGroupName())) {
					toRemove = entity;
				}
			}
		}

		if (toRemove != null) {
			getSessionStudy().getEntityMapList().remove(toRemove);
		}

		
		if(studyDetailsForm.getUpload() != null){

		UserFile graphicFile = repositoryManager.uploadFile(getUser().getId(), studyDetailsForm.getUpload(), 
				studyDetailsForm.getUploadFileName(), "Study Profile Picture: " + currentStudy.getId() , ServiceConstants.FILE_TYPE_STUDY, new Date());
		
			currentStudy.setGraphicFile(graphicFile);
		}

		accountManager.unregisterEntity(getSessionStudy().getRemovedMapList());
		accountManager.registerEntity(getSessionStudy().getEntityMapList());
		
		
		// while saving new study if study has model name set it to null
		if(currentStudy.getId() == null) {
			currentStudy.setModelTypeSet(null);
			currentStudy.setModelNameSet(null);
			currentStudy.setTherapeuticAgentSet(null);
			currentStudy.setTherapyTypeSet(null);
			currentStudy.setTherapeuticTargetSet(null);
		}
		
		currentStudy = repositoryManager.saveStudy(getAccount(), currentStudy);

		
		//adding DAO calls to save sets seperately
		repositoryManager.saveStudyTherapeuticAgent(studyDetailsForm.getTherapeuticAgentSet(), String.valueOf(currentStudy.getId()));
		repositoryManager.saveStudyTherapyType(studyDetailsForm.getTherapyTypeSet(), String.valueOf(currentStudy.getId()));
		repositoryManager.saveStudyTherapeuticTarget(studyDetailsForm.getTherapeuticTargetSet(), String.valueOf(currentStudy.getId()));
		repositoryManager.saveStudyModelType(studyDetailsForm.getModelTypeSet(), String.valueOf(currentStudy.getId()));
		repositoryManager.saveStudyModelName(studyDetailsForm.getModelNameSet(), String.valueOf(currentStudy.getId()));

		
		Long studyId = null;
		
		if(getSessionStudy().getStudy() != null) {
			studyId = getSessionStudy().getStudy().getId();
		}else {
			studyId = currentStudy.getId();
		}
		
		if(studyId != null) {
			studyDetailsForm.setTherapeuticAgentSet(new HashSet(studyDao.getStudyTherapeuticAgents(studyId.toString())));
			studyDetailsForm.setTherapeuticTargetSet(new HashSet(studyDao.getStudyTherapeuticTargets(studyId.toString())));
			studyDetailsForm.setTherapyTypeSet(new HashSet(studyDao.getStudyTherapyTypes(studyId.toString())));
			studyDetailsForm.setModelNameSet(new HashSet(studyDao.getStudyModelNames(studyId.toString())));
			studyDetailsForm.setModelTypeSet(new HashSet(studyDao.getStudyModelTypes(studyId.toString())));		
		}
		
		logger.info("Saved the Study " + currentStudy.getTitle());

		getSessionStudy().setStudy(currentStudy);

		// Clear uploaded file.
		getSessionUploadFile().clear();
		upload = null;

		return result;
	}


	/**
	 * Action for editing studies
	 * 
	 * @return
	 * @throws UserPermissionException
	 */
	public String edit() throws UserPermissionException {

		getSessionStudy().clear();
		currentStudy = getStudyExcludingDataset();

		if (!accountManager.getAccess(getAccount(), EntityType.STUDY, currentStudy.getId(), PermissionType.WRITE)) {
			throw new UserPermissionException(ServiceConstants.WRITE_ACCESS_DENIED);
		}

		// The user should not be shown the edit button if they only have read access so this should only return an
		// error if they use the URL to try and directly edit a study.
		accountManager.checkEntityAccess(getAccount(), EntityType.STUDY, currentStudy.getId(), PermissionType.WRITE);

		getSessionStudy().setStudy(currentStudy);
		initializeFormEntries();

		// After user edit, the study should be set to requested again
		if (!getInAdmin() && StudyStatus.REJECTED.equals(currentStudy.getStudyStatus())) {
			currentStudy.setStudyStatus(StudyStatus.REQUESTED);
		}

		return PortalConstants.ACTION_INPUT;
	}

	/**
	 * Navigate to details
	 * 
	 * @return
	 */
	public String moveToDetails() {

		// The user should not be shown the edit button if they only have read access so this should only return an
		// error if they use the URL to try and directly edit a study.
		currentStudy = getSessionStudy().getStudy();
		if (!accountManager.getAccess(getAccount(), EntityType.STUDY, currentStudy.getId(), PermissionType.WRITE)) {
			return PortalConstants.ACTION_ERROR;
		}
		saveSession();
		return PortalConstants.ACTION_INPUT;
	}

	/**
	 * Save to session and move to documentation
	 * 
	 * @return
	 */
	public String moveToDocumentation() {

		// The user should not be shown the edit button if they only have read access so this should only return an
		// error if they use the URL to try and directly edit a study.
		currentStudy = getSessionStudy().getStudy();
		if (!accountManager.getAccess(getAccount(), EntityType.STUDY, currentStudy.getId(), PermissionType.WRITE)) {
			return PortalConstants.ACTION_ERROR;
		}
		saveSession();
		return PortalConstants.ACTION_DOCUMENTATION;
	}

	/**
	 * Save to session and move to Dataset
	 * 
	 * @return
	 */
	public String moveToDataset() {

		// The user should not be shown the edit button if they only have read access so this should only return an
		// error if they use the URL to try and directly edit a study.
		currentStudy = getSessionStudy().getStudy();
		if (!accountManager.getAccess(getAccount(), EntityType.STUDY, currentStudy.getId(), PermissionType.WRITE)) {
			return PortalConstants.ACTION_ERROR;
		}
		saveSession();
		return PortalConstants.ACTION_DATASET;
	}

	/**
	 * Save to session and move to permissions
	 * 
	 * @return
	 */
	public String moveToPermissions() {

		// The user should not be shown the edit button if they only have read access so this should only return an
		// error if they use the URL to try and directly edit a study.
		currentStudy = getSessionStudy().getStudy();
		if (!accountManager.getAccess(getAccount(), EntityType.STUDY, currentStudy.getId(), PermissionType.ADMIN)) {
			return PortalConstants.ACTION_ERROR;
		}
		saveSession();
		return PortalConstants.ACTION_PERMISSIONS;
	}

	public String addSponsorInfo() {
		StudySponsorInfo sponsorInfo = new StudySponsorInfo(sponsorInfoEntry);
		getCurrentStudy().getSponsorInfoSet().add(sponsorInfo);

		sponsorInfoEntry = new StudySponsorInfo();    // Clear the entry fields
		return PortalConstants.ACTION_SPONSOR_INFO_TABLE;
	}

	/**
	 * Removes a sponsor info
	 * 
	 * @return
	 */
	public String removeSponsorInfo() {

		String sponsorInfoJson = getRequest().getParameter("sponsorInfoJson");

		if (!StringUtils.isEmpty(sponsorInfoJson)) {
			Gson gson = new Gson();
			try {
				StudySponsorInfo sponsorInfoToRemove =
						(StudySponsorInfo) gson.fromJson(sponsorInfoJson, StudySponsorInfo.class);

				if (sponsorInfoToRemove != null) {
					getCurrentStudy().getSponsorInfoSet().remove(sponsorInfoToRemove);
				}
			} catch (JsonSyntaxException jse) {
				jse.printStackTrace();
			}
		}

		return PortalConstants.ACTION_SPONSOR_INFO_TABLE;
	}

	/**
	 * Adds a new clinical trial to session
	 */
	public String addClinicalTrial() {

		if (!StringUtils.isEmpty(clinicalTrialId)) {
			ClinicalTrial ct = new ClinicalTrial();
			ct.setClinicalTrialId(clinicalTrialId);
			getCurrentStudy().getClinicalTrialSet().add(ct);
		}

		clinicalTrialId = null;
		return PortalConstants.ACTION_CLINICAL_TRIAL;
	}

	/**
	 * Removes a clinical trial
	 * 
	 * @return
	 */
	public String removeClinicalTrial() {

		String ctIdToRemove = getRequest().getParameter("ctIdToRemove");

		if (!StringUtils.isEmpty(ctIdToRemove)) {
			Iterator<ClinicalTrial> it = getCurrentStudy().getClinicalTrialSet().iterator();
			while (it.hasNext()) {
				ClinicalTrial ct = it.next();
				if (ctIdToRemove.equals(ct.getClinicalTrialId())) {
					it.remove();
					break;
				}
			}
		}

		return PortalConstants.ACTION_CLINICAL_TRIAL;
	}

	public String addResearchManagement() throws SocketException, IOException, JSchException {
		ResearchManagement resMgmt = new ResearchManagement(researchMgmtEntry);
		
		resMgmt.setId(Long.valueOf(getSessionStudy().getNewResearchManagement()));
		getSessionStudy().setNewResearchManagement(getSessionStudy().getNewResearchManagement()-1);

		if (getSessionUploadFile().getUploadFile() != null && picture == null) {
			picture = getSessionUploadFile().getUploadFile();
		}

		// Upload the picture
		if (pictureFileName != null) {
			UserFile picFile = repositoryManager.uploadFile(getUser().getId(), picture, pictureFileName, null,
					ServiceConstants.FILE_TYPE_RESEARCHER_PICTURE, new Date());
			if (picFile != null) {
				resMgmt.setPictureFile(picFile);
			}

			// Clear the uploaded file.
			getSessionUploadFile().clear();
			picture = null;
		}

		getSessionStudy().addResearchMgmt(resMgmt.getId(),resMgmt);
		getCurrentStudy().getResearchMgmtSet().add(resMgmt);

		researchMgmtEntry = new ResearchManagement(); // Clear the entry fields
		return PortalConstants.ACTION_EDIT_RESEARCH_MANAGEMENT;
	}
	
	public String getEmptyResMgmtEntryToAdd() {
		researchMgmtEntry = new ResearchManagement();
		return PortalConstants.ACTION_EDIT_RESEARCH_MANAGEMENT;
	}

	public String getResMgmtEntryToEdit() throws SocketException, IOException, JSchException, SftpException {
		String researchMgmtEntryJson = URLDecoder.decode(getRequest().getParameter("researchMgmtEntryJson"), "UTF-8");
		
		JsonParser jsonParser = new JsonParser();
		JsonObject resMgmtMetaJsonObj = jsonParser.parse(researchMgmtEntryJson).getAsJsonObject();
		researchMgmtEntry = convertJsonStrToResMgmtObj(resMgmtMetaJsonObj);

		UserFile pictureFile = null;
		
		Map<Long,ResearchManagement> resMgmtSet = getSessionStudy().getResearchMgmtMap();
		if(resMgmtSet!=null && resMgmtSet.containsKey(researchMgmtEntry.getId())){
			pictureFile = resMgmtSet.get(researchMgmtEntry.getId()).getPictureFile();
		}
		
		if(pictureFile == null && researchMgmtEntry.getId() != null && researchMgmtEntry.getId()>0){
			pictureFile = repositoryManager
					.getStudyManagementImage(getCurrentStudy().getId(), researchMgmtEntry.getId()).getPictureFile();
		} 
			
		if (pictureFile != null) {
			pictureFileName = pictureFile.getName();

		} else {
			pictureFileName = "";
		}
		
		return PortalConstants.ACTION_EDIT_RESEARCH_MANAGEMENT;
	}

	public ResearchManagement convertJsonStrToResMgmtObj(JsonObject resMgmtJsonObj) {

		logger.debug("convertJsonStrToResMgmtMetaObj: " + resMgmtJsonObj);
		
		if(!resMgmtJsonObj.get("id").toString().replaceAll("^\"|\"$", "").equals("")) { //remove leading and trailing quotation marks
			Gson gson = new Gson();
			return (ResearchManagement) gson.fromJson(resMgmtJsonObj, ResearchManagement.class);	
		} else {
			ResearchManagement resMgmtEntry= new ResearchManagement();
			resMgmtEntry.setRole(Long.valueOf(resMgmtJsonObj.get("role").toString().replaceAll("^\"|\"$", "")));
			resMgmtEntry.setFirstName(resMgmtJsonObj.get("firstName").toString().replaceAll("^\"|\"$", ""));
			resMgmtEntry.setMi(resMgmtJsonObj.get("mi").toString().replaceAll("^\"|\"$", ""));
			resMgmtEntry.setLastName(resMgmtJsonObj.get("lastName").toString().replaceAll("^\"|\"$", ""));
			resMgmtEntry.setSuffix(resMgmtJsonObj.get("suffix").toString().replaceAll("^\"|\"$", ""));
			resMgmtEntry.setEmail(resMgmtJsonObj.get("email").toString().replaceAll("^\"|\"$", ""));
			resMgmtEntry.setOrgName(resMgmtJsonObj.get("orgName").toString().replaceAll("^\"|\"$", ""));
			resMgmtEntry.setOrcId(resMgmtJsonObj.get("orcId").toString().replaceAll("^\"|\"$", ""));
			return resMgmtEntry;
		}
		
	}

	public String editResearchManagement() throws SocketException, IOException, JSchException {
		logger.debug("selectedResMgmtToEdit: " + selectedResMgmtToEdit);

		JsonParser jsonParser = new JsonParser();
		JsonObject resMgmtJsonObj = jsonParser.parse(URLDecoder.decode(selectedResMgmtToEdit, "UTF-8")).getAsJsonObject();
		ResearchManagement resMgmtToEdit = convertJsonStrToResMgmtObj(resMgmtJsonObj); // old entry
		ResearchManagement resMgmtEdited = new ResearchManagement(researchMgmtEntry);

		Long resMgmtToEditId = resMgmtToEdit.getId();
		UserFile pictureFileOld = null;
		boolean isRemoved = false;

		if (resMgmtToEditId!=null && resMgmtToEditId >0) { /* add attached new userfile to existing entry */

			isRemoved = removeResearchManagementObj(resMgmtToEdit);
			if (!isRemoved) {
				logger.error("Failed to remove the old ResearchManagement Entry!");
			} else {
				ResearchManagement rearchMgt = repositoryManager.getStudyManagementImage(getCurrentStudy().getId(), resMgmtToEditId);
				resMgmtEdited.setId(resMgmtToEditId);
				
				if (pictureFileName != null) {
					UserFile picFile = repositoryManager.uploadFile(getUser().getId(), picture, pictureFileName, null,
							ServiceConstants.FILE_TYPE_RESEARCHER_PICTURE, new Date());
					if (picFile != null) {
						rearchMgt.setPictureFile(picFile);
						resMgmtEdited.setPictureFile(picFile);
					}
				} else{
					resMgmtEdited.setPictureFile(rearchMgt.getPictureFile());
				}
				getSessionStudy().addResearchMgmt(resMgmtEdited.getId(),resMgmtEdited);
				getCurrentStudy().getResearchMgmtSet().add(resMgmtEdited);
			}

		} else { /* newly added ResearchManagement entry, but has not been saved */
			pictureFileOld = resMgmtToEdit.getPictureFile();

			if (pictureFileOld != null) {
				/* remove attached userFile by checking filename and filesize changed or not */
				if (pictureFileName != null && (!pictureFileName.equals(pictureFileOld.getName())
						|| picture.length() != pictureFileOld.getSize())) {
					repositoryManager.removeUserFile(pictureFileOld);
				}
			}
			removeResearchManagementObj(resMgmtToEdit);
			if (pictureFileName != null) {
				/* add attached userFile from form for new entry */
				UserFile picFile = repositoryManager.uploadFile(getUser().getId(), picture, pictureFileName, null,
						ServiceConstants.FILE_TYPE_RESEARCHER_PICTURE, new Date());
				if (picFile != null) {
					resMgmtEdited.setPictureFile(picFile);
				}
			}
			getSessionStudy().addResearchMgmt(resMgmtEdited.getId(),resMgmtEdited);
			getCurrentStudy().getResearchMgmtSet().add(resMgmtEdited);

		}

		researchMgmtEntry = new ResearchManagement();
		return PortalConstants.ACTION_EDIT_RESEARCH_MANAGEMENT;
	}

	private Boolean removeResearchManagementObj(ResearchManagement resMgmtToEdit) {

		Boolean isRemoved = false;
		for (Iterator<ResearchManagement> iterator = getCurrentStudy().getResearchMgmtSet().iterator(); iterator
				.hasNext();) {
			ResearchManagement management = iterator.next();
			if (management.stringEquals(resMgmtToEdit)) {
				iterator.remove();
				isRemoved = true;
				break;
			}
		}
		return isRemoved;
	}

	/**
	 * Removes a Research Management
	 * 
	 * @return
	 */

	public String removeResearchMgmt() {

		String researchMgmtJson = getRequest().getParameter("researchMgmtJson");
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(researchMgmtJson.replaceAll("^\"+|\"+$", ""));
		JsonArray resMgmtJsonArr = element.getAsJsonArray();
		if (resMgmtJsonArr != null && resMgmtJsonArr.size() > 0) {
			for (int i = 0; i < resMgmtJsonArr.size(); i++) {
				JsonObject resMgmtJsonObj = resMgmtJsonArr.get(i).getAsJsonObject();

				try {

					ResearchManagement resMgmtToRemove = convertJsonStrToResMgmtObj(resMgmtJsonObj);

					if (resMgmtToRemove != null) {
						// if it's a new research management, we also delete the picture file associated
						// with it.
						if (resMgmtToRemove.getId() == null && resMgmtToRemove.getPictureFile() != null) {
							repositoryManager.removeUserFile(resMgmtToRemove.getPictureFile());
						}
						removeResearchManagementObj(resMgmtToRemove);
					}
				} catch (JsonSyntaxException jse) {
					jse.printStackTrace();
				}
			}
		}
		return PortalConstants.ACTION_EDIT_RESEARCH_MANAGEMENT;
	}

	public String addStudySite() {

		Country country = siteEntry.getAddress().getCountry();
		if (country != null && country.getId() != null) {
			for (Country tmpCountry : getCountryList()) {
				if (country.getId().equals(tmpCountry.getId())) {
					siteEntry.getAddress().setCountry(tmpCountry);
					break;
				}
			}
		} else {
			siteEntry.getAddress().setCountry(null);
		}

		State state = siteEntry.getAddress().getState();
		if (state != null && state.getId() != null) {
			for (State tmpState : getStateList()) {
				if (state.getId().equals(tmpState.getId())) {
					siteEntry.getAddress().setState(tmpState);
					break;
				}
			}
		} else {
			siteEntry.getAddress().setState(null);
		}

		StudySite site = new StudySite(siteEntry);
		getCurrentStudy().getStudySiteSet().add(site);

		siteEntry = new StudySite();    // Clear the entry fields
		return PortalConstants.ACTION_EDIT_STUDY_SITE;
	}

	/**
	 * Removes a Study Site
	 * 
	 * @return
	 */
	public String removeStudySite() {

		String siteJson = getRequest().getParameter("siteJson");

		if (!StringUtils.isEmpty(siteJson)) {
			Gson gson = new Gson();

			try {
				StudySite siteToRemove = (StudySite) gson.fromJson(siteJson, StudySite.class);

				if (siteToRemove != null) {
					getCurrentStudy().getStudySiteSet().remove(siteToRemove);
				}
			} catch (JsonSyntaxException jse) {
				jse.printStackTrace();
			}
		}

		return PortalConstants.ACTION_EDIT_STUDY_SITE;
	}

	/**
	 * Adds a new grant to session
	 */
	public String addGrant() {

		Grant grant = new Grant(grantEntry);
		getCurrentStudy().getGrantSet().add(grant);

		grantEntry = new Grant();
		return PortalConstants.ACTION_GRANT;
	}


	/**
	 * Removes a grant
	 * 
	 * @return
	 */
	public String removeGrant() {

		String grantJson = getRequest().getParameter("grantJson");

		if (!StringUtils.isEmpty(grantJson)) {
			Gson gson = new Gson();

			try {
				Grant grantToRemove = (Grant) gson.fromJson(grantJson, Grant.class);
				if (grantToRemove != null) {
					getCurrentStudy().getGrantSet().remove(grantToRemove);
				}
			} catch (JsonSyntaxException jse) {
				jse.printStackTrace();
			}
		}

		return PortalConstants.ACTION_GRANT;
	}


	/**
	 * gets clinical study to display the clinical trial lightbox
	 * 
	 * @return
	 */
	public String viewClinicalTrial() {

		try {
			currentClinicalStudy = repositoryManager.getClinicalStudy(clinicalTrialId);
		} catch (IOException | JAXBException e) {
			e.printStackTrace();
		}
		return PortalConstants.ACTION_CLINICAL_TRIAL_DETAILS;
	}

	/**
	 * This action is performed as an ajax call when creating a new keyword A new keyword object is created here and
	 * stored in newKeyword where the json call retrieves it.
	 * 
	 * @return a string directing struts to a json object
	 */
	public String createKeyword() {

		newKeyword = new StudyKeyword();
		newKeyword.setKeyword(keywordSearchKey);
		newKeyword.setCount(0L);
		getSessionStudy().getNewKeywords().add(newKeyword);

		return PortalConstants.ACTION_ADDKEYWORD;
	}

	/**
	 * Retrieves the list of keywords available for this user to attach to the current data element
	 * 
	 * @return list of keywords that can be attached
	 */
	public Set<StudyKeyword> getAvailableKeywords() {

		String searchKey = getRequest().getParameter("keywordSearchKey");

		if (StringUtils.isEmpty(searchKey)) {
			searchKey = "";
		}

		// Query for the keywords and add them to the list of available keywords
		Set<StudyKeyword> availableKeywords = new LinkedHashSet<StudyKeyword>();
		List<StudyKeyword> searchedKeywords = repositoryManager.searchKeywords(searchKey);

		if (searchedKeywords != null && !searchedKeywords.isEmpty()) {
			availableKeywords.addAll(searchedKeywords);
		}

		// Add list of keywords added this session
		availableKeywords.addAll(getSessionStudy().getNewKeywords());

		// Strip out any keywords that are already attached to the study
		if (getCurrentStudy() != null && getCurrentStudy().getKeywordSet() != null) {
			for (StudyKeyword k : getCurrentStudy().getKeywordSet()) {
				for (StudyKeyword keyword : availableKeywords) {
					if (keyword.getKeyword().equalsIgnoreCase(k.getKeyword())) {
						availableKeywords.remove(keyword);
						break;
					}
				}
			}
		}

		return availableKeywords;
	}

	/**
	 * Retrieves the list of keywords that are attached to this data structure
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Set<StudyKeyword> getCurrentKeywords() throws UnsupportedEncodingException {

		Set<StudyKeyword> current = new LinkedHashSet<StudyKeyword>();

		if (getCurrentStudy() != null && getCurrentStudy().getKeywordSet() != null) {
			// Add each keyword from the keywordElement list to current
			for (StudyKeyword k : getCurrentStudy().getKeywordSet()) {
				k.setCount(repositoryManager.getKeywordCount(k.getKeyword()));
				current.add(k);
			}
		}

		return current;
	}

	/**
	 * The search and table update function for accessReports. Uses the study in session and any filters applied to
	 * search for access reports. The results are set to accessRecordList.
	 * 
	 * @return : string accessRecord for struts
	 * @throws UserPermissionException : if the current user does not have admin access to the current study
	 */
	public String searchReports() throws UserPermissionException {
		// Permission Check. This search is only visible to admins and those with admin acces to the current study.
		if (!getIsPortalAdmin() && !getHasAdminAccess()) {

			throw new UserPermissionException(ServiceConstants.ADMIN_ACCESS_DENIED);
		}

		IdtInterface idt;
		try {
			idt = new Struts2IdtInterface();
			IdtRequest request = idt.getRequest();

			int countTotalRcord = repositoryManager.countAccessRecordsStudy(getCurrentStudy().getId());

			idt.setTotalRecordCount(countTotalRcord);

			updateAccessRecordOrder(request.getOrderColumn());
			updateAccessRecordFilter(request.getFilters());
			//updateAccessRecordSearch(request);

			PaginationData pageData = new PaginationData();
			// these parameters are set by the methods above
			pageData.setAscending(ascending);
			pageData.setSort(sort);
//			pageData.setPageSize(request.getLength());
//			pageData.setPage(request.getPageNumber());
			pageData.setPageLength(request.getPageLength());
			long startTime = System.nanoTime();

			this.setKey("");
			List<AccessRecord> recordList =
					repositoryManager.searchAccessRecords(getCurrentStudy(), key, daysOld, pageData);

			/*getting the search value from data table search box and the values of the selected columns 
			 * from idt search drop checkboxes. And filtering data from the data table search input and selected columns.
			 */
			List<IdtColumnDescriptor> columns = new ArrayList<IdtColumnDescriptor>(request.getColumnDescriptions());
			// pre-filter out columns we don't want to search
			Iterator<IdtColumnDescriptor> columnsIterator = columns.iterator();
			while (columnsIterator.hasNext()) {
				IdtColumnDescriptor column = columnsIterator.next();
				if (!column.isSearchable()) {
					columnsIterator.remove();
				}
			}
			List<AccessRecord> displayRecordList = new ArrayList<AccessRecord>();
			String searchVal = request.getSearchVal();
			if (columns.size() != 0 && !searchVal.isEmpty()){
				for(AccessRecord ar : recordList) {
					boolean isInList = false;
					for(IdtColumnDescriptor column : columns){
						String checkedColumn = column.getName();
						switch (checkedColumn) {
						case "id":
							if (String.valueOf(ar.getDataset().getId()).toLowerCase().contains(searchVal)) isInList = true;
							break;
						case "name":
							if (ar.getDataset().getName().toLowerCase().contains(searchVal)) isInList = true;
							break;
						case "status":
							if (ar.getDataset().getDatasetStatus().getName().toLowerCase().contains(searchVal)) isInList = true;
							break;
						case "username":
							if (ar.getAccount().getDisplayName().toLowerCase().contains(searchVal)) isInList = true;
							break;
						case "date":
							if (BRICSTimeDateUtil.dateToDateString(ar.getQueueDate()).toLowerCase().contains(searchVal)) isInList = true;
							break;
						case "recordCount":
							if (String.valueOf(ar.getRecordCount()).toLowerCase().contains(searchVal)) isInList = true;
							break;
						case "source":
							if (ar.getDataSource().getName().toLowerCase().contains(searchVal)) isInList = true;
							break;
						}
						if(isInList){
							displayRecordList.add(ar);
							isInList = false;
						}
					}
				}
			} else if(searchVal.isEmpty()){
				displayRecordList.addAll(recordList);
			}
			
			getSessionStudy().setAccessRecordList(recordList);
			ArrayList<AccessRecord> outputAr = new ArrayList<AccessRecord>(displayRecordList);
			
			idt.setTotalRecordCount(outputAr.size());
			idt.setFilteredRecordCount(outputAr.size());
	

			idt.setList(outputAr);
			idt.decorate(new AccessReportIdtListDecorator());

			long endTime = System.nanoTime();
			logger.debug("ReportSearchTest: " + recordList.size() + " records retrieved. Duration: "
					+ (endTime - startTime));

			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}

		return null;
	}

	public String searchReportsPage() {
		return PortalConstants.ACTION_ACCESS_RECORD;
	}

	/**
	 * This search grabs the accessRecordList stored in session (it needs to be set by searchReports() or another
	 * source) and converts it to CSV. This action assumes that the user already has access to the report.
	 * 
	 * @return : struts result "export"
	 */
	public String downloadReport() {
		ByteArrayOutputStream baos =
				AccessReportExportUtil.exportAccessReportToCSV(getSessionStudy().getAccessRecordList(), false);
		inputStream = new ByteArrayInputStream(baos.toByteArray());
		exportFileName = getSessionStudy().getStudy().getPrefixedId() + "_Access_Report_"
				+ BRICSTimeDateUtil.formatDate(new Date()) + ".csv";
		return PortalConstants.ACTION_EXPORT;
	}

	public String adminDownloadReport() throws UserPermissionException {
		if (!getIsPortalAdmin()) {
			throw new UserPermissionException(ServiceConstants.ADMIN_ACCESS_DENIED);
		}
		long startTime = System.nanoTime();

		// NEW PAGINATED EXPORT STARTS HERE
		int recordCountForDebug = 0;
		AccessReportExportUtil util = new AccessReportExportUtil(true);
		PaginationData pageData = new PaginationData();
		final int CHUNK_SIZE = 20000; // Number of records to fetch at once (should be tested and
										 // optimized)
		pageData.setPageSize(CHUNK_SIZE);
		pageData.setPage(0); // page 0 is actually not valid. We will flip the page before fetching first result
		List<AccessRecord> records = null;

		logger.debug("selectedStudies: " + selectedStudies);
		String[] studyIds = selectedStudies.split(",");
		Set<Long> studyIdSet = new HashSet<Long>();
		for (String studyId : studyIds) {
			studyIdSet.add(Long.valueOf(studyId));
		}
		do {
			pageData.setPage(pageData.getPage() + 1);
			records = repositoryManager.getAccessRecordsByStudyList(studyIdSet, startAccessReportDate,
					endAccessReportDate, pageData);
			util.writeData(records);

			// DEBUG
			logger.debug("Page " + pageData.getPage() + ": " + records.size() + " records.");
			if (logger.isDebugEnabled()) {
				recordCountForDebug = recordCountForDebug + records.size();
			}
		} while (records.size() == CHUNK_SIZE);

		ByteArrayOutputStream baos = util.getOutputStream();
		// NEW PAGINATED EXPORT ENDS HERE

		inputStream = new ByteArrayInputStream(baos.toByteArray());
		exportFileName = "Admin_Access_Report_" + BRICSTimeDateUtil.formatDate(new Date()) + ".csv";
		long endTime = System.nanoTime();

		logger.debug("adminDownloadReportTest: " + recordCountForDebug + " records downloaded as " + pageData.getPage()
				+ " pages. Duration: " + (endTime - startTime));
		return PortalConstants.ACTION_EXPORT;
	}

	/**
	 * This action method will attempt to create a DOI for the current study object that is stored in the session. Once
	 * a DOI is created, it will be sent back to the user's browser as JSON.
	 * 
	 * @return The "success" result if a DOI was created, or "error" if there was an error.
	 */
	public String createDoiForStudy() {
		Study cStudy = getSessionStudy().getStudy();

		// Check if there is a study object in the session.
		if (cStudy == null) {
			String msg = "No study in session to create a DOI with.";
			logger.error(msg);
			errRespMsg = msg;

			return ERROR;
		}

		// Check if the DOI is enabled for this instance.
		if (!canShowCreateDoiUi()) {
			logger.error("DOI creation is not allowed due to a permissions violation for the " + cStudy.getPrefixedId()
					+ " study.");
			errRespMsg = "DOI creation is not allowed due to a permissions violation for this study.";

			return ERROR;
		}

		// Attempt to create a DOI for the current study.
		try {
			logger.info("Attempting to create a DOI for the " + cStudy.getPrefixedId() + " study...");
			repositoryManager.createDoiForStudy(cStudy);
			logger.info("Created the " + cStudy.getDoi() + " DOI for the " + cStudy.getPrefixedId() + " study.");

			// Save the new DOI to the database.
			repositoryManager.saveStudy(getAccount(), cStudy);

			// Create the JSON response.
			JsonObject output = new JsonObject();

			output.addProperty("doi", cStudy.getDoi());
			responseJson = output.toString();
		} catch (DoiWsValidationException dwve) {
			logger.error(
					"Validation error from the minter service, when creating a DOI for the " + cStudy.getPrefixedId()
							+ " study.", dwve);
			errRespMsg = dwve.getMessage();

			return ERROR;
		} catch (Exception e) {
			logger.error("Error occured while creating a DOI for the " + cStudy.getPrefixedId() + " study.", e);
			errRespMsg = e.getMessage();

			return ERROR;
		}

		return SUCCESS;
	}


	public String getDocumentsActionName() {
		return "studyDocumentationAction";
	}

	public List<Study> getStudyList() {
		if (studyList == null) {
			studyList = new ArrayList<Study>();
		}
		return studyList;
	}

	// http://fitbir-portal-local.cit.nih.gov:8080/portal/study/studyAction!getStudyTableList.action
	public List<Study> getStudyTableList() throws UnsupportedEncodingException {
		logger.info("Calling dao");
		List<BasicStudySearch> basicStudySearches = basicStudyDao.getPublicSiteSearchBasicStudies();
		logger.info("Finished dao");
		try {
			IdtInterface idt = new Struts2IdtInterface();
			search();
			ArrayList<Study> outputList = new ArrayList<Study>(getStudyList());
			idt.setList(outputList);
			idt.decorate(new ResearchMgmtIdtListDecorator());
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new StudyListIdtDecorator(basicStudySearches));
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// http://fitbir-portal-local.cit.nih.gov:8080/portal/study/studyAction!getResearchMgmtSet.action
	public List<ResearchManagement> getResearchMgmtSet() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<ResearchManagement> outputList =
					new ArrayList<ResearchManagement>(getCurrentStudy().getResearchMgmtSet());
			idt.setList(outputList);
			idt.decorate(new ResearchMgmtIdtListDecorator());
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/study/studyAction!getDocumentation.action
	public String getDocumentation() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<StudySupportingDocumentation> sdList =
					new ArrayList<StudySupportingDocumentation>(getCurrentStudy().getSupportingDocumentationSet());
			idt.setList(sdList);
			idt.setTotalRecordCount(sdList.size());
			idt.setFilteredRecordCount(sdList.size());
			idt.decorate(new SupportDocIdtListDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/study/studyAction!getFormStructureList.action
	public String getFormStructureList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<StudyForm> outputList = new ArrayList<StudyForm>(getCurrentStudy().getStudyForms());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/study/studyAction!getSponsorInfoSet.action
	public String getSponsorInfoSet() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<StudySponsorInfo> outputList =
					new ArrayList<StudySponsorInfo>(getCurrentStudy().getSponsorInfoSet());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new SponsorInfoIdtListDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/study/studyAction!getStudySiteSet.action
	public String getStudySiteSet() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<StudySite> outputList = new ArrayList<StudySite>(getCurrentStudy().getStudySiteSet());
			idt.setList(outputList);
			idt.decorate(new StudySiteIdtListDecorator());
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/study/studyAction!getClinicalTrialSet.action
	public String getClinicalTrialSet() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<ClinicalTrial> outputList = new ArrayList<ClinicalTrial>(getCurrentStudy().getClinicalTrialSet());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/study/studyAction!getGrantSet.action
	public String getGrantSet() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<Grant> outputList = new ArrayList<Grant>(getCurrentStudy().getGrantSet());
			idt.setList(outputList);
			idt.decorate(new GrantIdtListDecorator());
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	private void updateAccessRecordFilter(List<IdtFilterDescription> filters) {
		daysOld = null;
		if (filters.size() != 0) {
			IdtFilterDescription filter = filters.get(0);
			if (filter.getName().equals("Time Filter: all")) {
				if (filter.getValue().equals("lastWeek")) {
					daysOld = 7L;
				} else if (filter.getValue().equals("lastMonth")) {
					daysOld = 31L;
				} else if (filter.getValue().equals("lastYear")) {
					daysOld = 365L;
				}
			}
		}
	}

	private void updateAccessRecordSearch(IdtRequest request) {
		key = null;
		String searchVal = request.getSearchVal();
		if (searchVal != null && !searchVal.equals("")) {
			key = searchVal;
		}
	}

	private void updateAccessRecordOrder(IdtColumnDescriptor orderColumn) {

		// these are intentionally NOT mapped 1:1
		if (orderColumn != null) {
			String orderData = orderColumn.getData();
			sort = orderData;
			ascending = orderColumn.getOrderDirection().equals(IdtRequest.ORDER_ASCENDING);
		}
	}

	private ArrayList<AccessRecord> paginateAccessRecords(ArrayList<AccessRecord> outputAr, IdtRequest request) {
		ArrayList<AccessRecord> output = null;
		int startIndex = request.getStart();
		int requestLength = request.getLength();
		int dataLength = outputAr.size();
		boolean returnAll = (requestLength == 0);
		if (returnAll || (startIndex == 0 && requestLength == dataLength)) {
			output = new ArrayList<AccessRecord>(outputAr);
		} else if (requestLength < dataLength) {
			int endIndex = startIndex + request.getLength();
			if (endIndex > (outputAr.size() - 1)) {
				endIndex = outputAr.size() - 1;
			}
			output = new ArrayList<AccessRecord>(outputAr.subList(startIndex, endIndex));
		} else {
			// otherwise, request length is greater than or equal to data length, so just give up data
			output = outputAr;
		}
		return output;
	}

	@Override
	public void validate() {
		if (hasFieldErrors()) {
			logger.debug("validate() has field errors: " + getFieldErrors().size());
			getSessionUploadFile().clear();
			/*Note: upload and picture are not validated at the same time 
			 * since picture in research management is added/edited in a dialog*/
			File tempFile = null;

			if (upload != null) {
				try {
					tempFile = BRICSFilesUtils.copyFile(upload);
				} catch (IOException e) {
					logger.error("Failed to read upload file to byte array in validation.");
					e.printStackTrace();
				}
				getSessionUploadFile().setUploadFile(tempFile);
			} else if (picture != null) {
				try {
					tempFile = BRICSFilesUtils.copyFile(picture);
				} catch (IOException e) {
					logger.error("Failed to read upload picture to byte array in validation.");
					e.printStackTrace();
				}
				getSessionUploadFile().setUploadFile(tempFile);
			}
		}
	}

	public List<TherapeuticAgent> getAllTherapeuticAgents() {
		return studyDao.getAllTherapeuticAgents();
	}
	
	public List<TherapyType> getAllTherapyTypes() {
		return studyDao.getAllTherapyTypes();
	}

	public List<TherapeuticTarget> getAllTherapeuticTargets() {
		return studyDao.getAllTherapeuticTargets();
	}
	
	public List<ModelName> getAllModelNames() {
		return studyDao.getAllModelNames();
	}
	
	public List<ModelType> getAllModelTypes() {
		return studyDao.getAllModelTypes();
	}
	
	public void setStudyList(List<Study> studyList) {
		this.studyList = studyList;
	}

	public Map<Long, EntityMap> getPermissionList() {
		return permissionList;
	}

	public void setPermissionList(Map<Long, EntityMap> permissionList) {
		this.permissionList = permissionList;
	}

	public ClinicalStudy getCurrentClinicalStudy() {
		return currentClinicalStudy;
	}

	public void setCurrentClinicalStudy(ClinicalStudy currentClinicalStudy) {
		this.currentClinicalStudy = currentClinicalStudy;
	}

	public File getUpload() {
		return upload;
	}

	public void setUpload(File upload) {
		this.upload = upload;
	}

	public String getUploadContentType() {
		return uploadContentType;
	}

	public void setUploadContentType(String uploadContentType) {
		this.uploadContentType = uploadContentType;
	}

	public String getUploadFileName() {
		return uploadFileName;
	}

	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}

	public String getExportFileName() {
		return exportFileName;
	}

	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}

	public String getClinicalTrialId() {
		return clinicalTrialId;
	}

	public void setClinicalTrialId(String clinicalTrialId) {
		this.clinicalTrialId = clinicalTrialId;
	}

	public StudyDetailsForm getStudyDetailsForm() {
		return studyDetailsForm;
	}

	public void setStudyDetailsForm(StudyDetailsForm studyDetailsForm) {
		this.studyDetailsForm = studyDetailsForm;
	}

	public void setCurrentStudy(Study currentStudy) {
		this.currentStudy = currentStudy;
	}

	public String getStartAccessReportDate() {
		return startAccessReportDate;
	}

	public void setStartAccessReportDate(String startAccessReportDate) {
		this.startAccessReportDate = startAccessReportDate;
	}

	public String getEndAccessReportDate() {
		return endAccessReportDate;
	}

	public void setEndAccessReportDate(String endAccessReportDate) {
		this.endAccessReportDate = endAccessReportDate;
	}

	public String getCurrentDate() {
		return BRICSTimeDateUtil.formatDate(new Date());
	}

	public Integer getNumSearchResults() {
		return numSearchResults;
	}

	public void getNumSearchResults(Integer numSearchResults) {
		this.numSearchResults = numSearchResults;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Boolean getAscending() {
		return ascending;
	}

	public void setAscending(Boolean ascending) {
		this.ascending = ascending;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public boolean getEnforceStaticFields() {
		return false;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public Long getFilterId() {
		return filterId;
	}

	public void setFilterId(Long filterId) {
		this.filterId = filterId;
	}

	public Long getDaysOld() {
		return daysOld;
	}

	public void setDaysOld(Long daysOld) {
		this.daysOld = daysOld;
	}

	public StudySponsorInfo getSponsorInfoEntry() {
		return sponsorInfoEntry;
	}

	public void setSponsorInfoEntry(StudySponsorInfo sponsorInfoEntry) {
		this.sponsorInfoEntry = sponsorInfoEntry;
	}

	public ResearchManagement getResearchMgmtEntry() {
		return researchMgmtEntry;
	}

	public void setResearchMgmtEntry(ResearchManagement researchMgmtEntry) {
		this.researchMgmtEntry = researchMgmtEntry;
	}

	public StudySite getSiteEntry() {
		return siteEntry;
	}

	public void setSiteEntry(StudySite siteEntry) {
		this.siteEntry = siteEntry;
	}

	public Grant getGrantEntry() {
		return grantEntry;
	}

	public void setGrantEntry(Grant grantEntry) {
		this.grantEntry = grantEntry;
	}

	public File getPicture() {
		return picture;
	}

	public void setPicture(File picture) {
		this.picture = picture;
	}

	public String getPictureContentType() {
		return pictureContentType;
	}

	public void setPictureContentType(String pictureContentType) {
		this.pictureContentType = pictureContentType;
	}

	public String getPictureFileName() {
		return pictureFileName;
	}

	public void setPictureFileName(String pictureFileName) {
		this.pictureFileName = pictureFileName;
	}

	public String getResponseJson() {
		return responseJson;
	}

	public void setResponseJson(String responseJson) {
		this.responseJson = responseJson;
	}

	public StudyKeyword getNewKeyword() {
		return newKeyword;
	}

	public void setNewKeyword(StudyKeyword newKeyword) {
		this.newKeyword = newKeyword;
	}

	public String getKeywordSearchKey() {
		return keywordSearchKey;
	}

	public void setKeywordSearchKey(String keywordSearchKey) {
		this.keywordSearchKey = keywordSearchKey;
	}

	public Long getFsId() {
		return fsId;
	}

	public void setFsId(Long fsId) {
		this.fsId = fsId;
	}

	public StudyForm getStudyFormEntry() {
		return studyFormEntry;
	}

	public void setStudyFormEntry(StudyForm studyFormEntry) {
		this.studyFormEntry = studyFormEntry;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getInputJson() {
		return inputJson;
	}

	public void setInputJson(String inputJson) {
		this.inputJson = inputJson;
	}

	public String getErrRespMsg() {
		return errRespMsg;
	}

	public void setErrRespMsg(String errRespMsg) {
		this.errRespMsg = errRespMsg;
	}

	public String getSelectedStudies() {
		return selectedStudies;
	}

	public void setSelectedStudies(String selectedStudies) {
		this.selectedStudies = selectedStudies;
	}

	public String getSelectedResMgmtToEdit() {
		return selectedResMgmtToEdit;
	}

	public void setSelectedResMgmtToEdit(String selectedResMgmtToEdit) {
		this.selectedResMgmtToEdit = selectedResMgmtToEdit;
	}

	public Set<StudyTherapeuticAgent> getTherapeuticAgentSet() {
		return studyDetailsForm.getTherapeuticAgentSet();
	}

	public Set<StudyTherapeuticTarget> getTherapeuticTargetSet() {
		return studyDetailsForm.getTherapeuticTargetSet();
	}

	public Set<StudyTherapyType> getTherapyTypeSet() {
		return studyDetailsForm.getTherapyTypeSet();
	}

	public Set<StudyModelName> getModelNameSet() {
		return studyDetailsForm.getModelNameSet();
	}

	public Set<StudyModelType> getModelTypeSet() {
		return studyDetailsForm.getModelTypeSet();
	}

	public void setTherapeuticAgentSet(Set<StudyTherapeuticAgent> therapeuticAgentSet) {
		this.therapeuticAgentSet = therapeuticAgentSet;
	}

	public void setTherapeuticTargetSet(Set<StudyTherapeuticTarget> therapeuticTargetSet) {
		this.therapeuticTargetSet = therapeuticTargetSet;
	}

	public void setTherapyTypeSet(Set<StudyTherapyType> therapyTypeSet) {
		this.therapyTypeSet = therapyTypeSet;
	}

	public void setModelNameSet(Set<StudyModelName> modelNameSet) {
		this.modelNameSet = modelNameSet;
	}

	public void setModelTypeSet(Set<StudyModelType> modelTypeSet) {
		this.modelTypeSet = modelTypeSet;
	}

}
