package gov.nih.tbi.metastudy.portal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.ClinicalStudy;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.MetaStudyStatus;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RecruitmentStatus;
import gov.nih.tbi.commons.model.ResearchManagementRole;
import gov.nih.tbi.commons.model.exceptions.DoiWsValidationException;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.MetaStudyManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.WebServiceManager;
import gov.nih.tbi.commons.util.BRICSFilesUtils;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.commons.util.SavedQueryUtil;
import gov.nih.tbi.idt.ws.IdtColumnDescriptor;
import gov.nih.tbi.idt.ws.IdtFilterDescription;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.IdtRequest;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.metastudy.dao.MetaStudyDao;
import gov.nih.tbi.metastudy.model.MetaStudyDetailsForm;
import gov.nih.tbi.metastudy.model.MetaStudyKeywordForm;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyAccessRecord;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyClinicalTrial;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyDocumentation;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyGrant;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyKeyword;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyLabel;
import gov.nih.tbi.metastudy.model.hibernate.ResearchManagementMeta;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.query.dao.SavedQueryDao;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.repository.model.alzped.ModelName;
import gov.nih.tbi.repository.model.alzped.ModelType;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.TherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.TherapyType;
import gov.nih.tbi.repository.model.hibernate.FundingSource;
import gov.nih.tbi.repository.model.hibernate.StudyType;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.service.util.AccessReportExportUtil;
import gov.nih.tbi.taglib.datatableDecorators.MetaStudyAccessRecordsIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.MetaStudyClinicalTrialIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.MetaStudyDataIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.MetaStudyGrantIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.ResearchMgmtMetaListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.SupportDocIdtListDecorator;

public class MetaStudyAction extends BaseMetaStudyAction {

	private static final long serialVersionUID = 4636984499164824419L;

	private static Logger logger = Logger.getLogger(MetaStudyAction.class);

	@Autowired
	private MetaStudyDao metaStudyDao;

	private MetaStudy currentMetaStudy;
	private MetaStudyDetailsForm metaStudyDetailsForm;
	private ClinicalStudy currentClinicalStudy;
	private MetaStudyGrant grantEntry;
	private MetaStudyKeywordForm metaStudyKeywordForm;
	private String grantId;

	private String savedQueryText;
	private Long savedQueryId;
	private List<EntityMap> savedQueryPermissions;

	// PaginationData
	private Boolean ascending;
	private String sort;
	private String key;
	private Long daysOld;

	@Autowired
	protected WebServiceManager webServiceManager;

	private Account currentMetaStudyOwner;
	private EntityMap currentPermissions;

	private String viewMode;
	public static final String VIEW_MODE_PREVIEW = "1";
	public static final String VIEW_MODE_UNPUBLISHED = "2";  // default
	public static final String VIEW_MODE_PUBLISHED = "3";

	// keyword-label form parameters
	private MetaStudyKeyword newKeyword;
	private MetaStudyLabel newLabel;
	private String keywordSearchKey;
	private String labelSearchKey;

	private String duration;

	private long metaStudyId;
	private List<MetaStudyAccessRecord> metaStudyAccessRecords;

	// meta study access record stats
	private int dataFileDownloads;
	private int documentFileDownloads;
	private int totalDownloads;
	private double totalDownloadSize;

	// Access report download
	private String startAccessReportDate;
	private String endAccessReportDate;
	private String exportFileName;
	private InputStream inputStream;

	private ResearchManagementMeta researchMgmtMetaEntry;
	private String selectedResMgmtToEdit;
	// Research Management Picture
	private File picture;
	private String pictureContentType;
	private String pictureFileName;


	private String clinicalTrialId;
	private String responseJson;
	private String errRespMsg;

	@Autowired
	SavedQueryDao savedQueryDao;

	@Autowired
	MetaStudyManager metaStudyManager;

	/**
	 * Saves form data to session
	 */
	public void saveSession() {

		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		if (metaStudyDetailsForm != null) {
			metaStudyDetailsForm.adapt(currentMetaStudy, false);
		} else {
			metaStudyDetailsForm = new MetaStudyDetailsForm(currentMetaStudy);
			metaStudyDetailsForm.setTherapeuticAgentSet(getSessionMetaStudy().getTherapeuticAgentSet());
			metaStudyDetailsForm.setTherapeuticTargetSet(getSessionMetaStudy().getTherapeuticTargetSet());
			metaStudyDetailsForm.setTherapyTypeSet(getSessionMetaStudy().getTherapyTypeSet());
			metaStudyDetailsForm.setModelNameSet(getSessionMetaStudy().getModelNameSet());
			metaStudyDetailsForm.setModelTypeSet(getSessionMetaStudy().getModelTypeSet());
		}

		if (metaStudyKeywordForm != null) {
			metaStudyKeywordForm.copyToMetaStudy(currentMetaStudy);
		} else {
			metaStudyKeywordForm = new MetaStudyKeywordForm(currentMetaStudy);
		}

		getSessionMetaStudy().setTherapeuticAgentSet(metaStudyDetailsForm.getTherapeuticAgentSet());
		getSessionMetaStudy().setTherapeuticTargetSet(metaStudyDetailsForm.getTherapeuticTargetSet());
		getSessionMetaStudy().setTherapyTypeSet(metaStudyDetailsForm.getTherapyTypeSet());
		getSessionMetaStudy().setModelNameSet(metaStudyDetailsForm.getModelNameSet());
		getSessionMetaStudy().setModelTypeSet(metaStudyDetailsForm.getModelTypeSet());

		getSessionMetaStudy().setMetaStudy(currentMetaStudy);
	}


	public MetaStudy getCurrentStudy() {

		if (getSessionMetaStudy() != null) {
			return getSessionMetaStudy().getMetaStudy();
		} else {
			return null;
		}
	}

	/**
	 * This action is called from Create Meta Study menu tab. It clears the session and creates a new MetaStudy object.
	 * 
	 * @return a string that directs to Create Meta Study page
	 */
	public String create() {
		getSessionMetaStudy().clear();

		currentMetaStudy = new MetaStudy();
		currentMetaStudy.setStatus(MetaStudyStatus.DRAFT);


		getSessionMetaStudy().setMetaStudy(currentMetaStudy);

		metaStudyDetailsForm = new MetaStudyDetailsForm();
		this.addDefaultDataManager();
		return PortalConstants.ACTION_INPUT;
	}


	// Add the login user as the default data manager
	private void addDefaultDataManager() {

		User loginUser = getUser();
		ResearchManagementMeta dataMgr = new ResearchManagementMeta();
		dataMgr.setId(-1L);
		dataMgr.setResearchMgmtRole(ResearchManagementRole.DATA_MANAGER);
		dataMgr.setFirstName(loginUser.getFirstName());
		dataMgr.setLastName(loginUser.getLastName());
		dataMgr.setEmail(loginUser.getEmail());
		dataMgr.setOrgName(getAccount().getAffiliatedInstitution());

		getCurrentStudy().getResearchMgmtMetaSet().add(dataMgr);
	}


	public String edit() throws UserPermissionException, MalformedURLException, UnsupportedEncodingException,
			UserAccessDeniedException {

		currentMetaStudy = getMetaStudyFromRequest();
		if (currentMetaStudy == null) {
			return null;
		}

		if (!getHasWritePermission()) {
			sessionMetaStudy.clear();
			throw new UserPermissionException(ServiceConstants.WRITE_ACCESS_DENIED);
		}

		// set form details to reuse create functionality
		getSessionMetaStudy().clear();
		getSessionMetaStudy().setMetaStudy(currentMetaStudy);
		metaStudyDetailsForm = new MetaStudyDetailsForm(currentMetaStudy);
		metaStudyKeywordForm = new MetaStudyKeywordForm(currentMetaStudy);

		// get meta study permissions
		currentMetaStudyOwner = accountManager.getEntityOwnerAccount(currentMetaStudy.getId(), EntityType.META_STUDY);
		currentPermissions = accountManager.getAccess(getAccount(), EntityType.META_STUDY, currentMetaStudy.getId());

		return PortalConstants.ACTION_META_STUDY_EDIT;
	}

	public String keywords() {
		return PortalConstants.KEYWORDS;
	}

	public String documentations() {
		return PortalConstants.ACTION_EDIT_DOCUMENTATION;
	}

	public String editPermissions() throws UserPermissionException, MalformedURLException, UnsupportedEncodingException,
			UserAccessDeniedException {
		currentMetaStudy = getMetaStudyFromRequest();
		if (!getHasAdminPermission()) {
			sessionMetaStudy.clear();
			throw new UserPermissionException(ServiceConstants.ADMIN_ACCESS_DENIED);
		}
		getSessionMetaStudy().clear();
		getSessionMetaStudy().setMetaStudy(currentMetaStudy);

		return PortalConstants.ACTION_PERMISSIONS;
	}

	public String submitPermissions() throws UserPermissionException, MalformedURLException,
			UnsupportedEncodingException, UserAccessDeniedException {
		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		if (!getHasAdminPermission()) {
			sessionMetaStudy.clear();
			throw new UserPermissionException(ServiceConstants.ADMIN_ACCESS_DENIED);
		}

		// These lists in session were created and maintained in MetaStudyPermissionsAction
		List<EntityMap> entitiesToRemove = getSessionMetaStudy().getRemovedMapList();

		// if a permission was added and removed immediately, then the permission has not yet been saved in the db. no
		// need to delete these.
		Iterator<EntityMap> entitiesToRemoveIterator = entitiesToRemove.iterator();

		while (entitiesToRemoveIterator.hasNext()) {
			EntityMap currentEntityMap = entitiesToRemoveIterator.next();

			if (currentEntityMap.getId() == null) {
				entitiesToRemoveIterator.remove();
			}
		}

		if (entitiesToRemove != null && !entitiesToRemove.isEmpty()) {
			accountManager.unregisterEntity(entitiesToRemove);
		}

		List<EntityMap> entitiesToAdd = getSessionMetaStudy().getEntityMapList();
		if (entitiesToAdd != null && !entitiesToAdd.isEmpty()) {
			accountManager.saveUpdateEntity(entitiesToAdd);
		}

		return PortalConstants.ACTION_REDIRECT_TO_VIEW;
	}


	/**
	 * This method returns a MetaStudy object based on metaStudyId value obtained from request parameter.
	 * 
	 * @return MetaStudy object or null if not found
	 */
	private MetaStudy getMetaStudyFromRequest() {

		String idStr = getRequest().getParameter(PortalConstants.META_STUDY_ID);
		if (!StringUtils.isBlank(idStr)) {
			try {
				long metaStudyId = Long.parseLong(idStr);
				return metaStudyManager.getMetaStudyById(metaStudyId);

			} catch (NumberFormatException ne) {
				logger.error("MetaStudyId passed in is not a valid number " + idStr, ne);
			}
		}
		return null;
	}


	public String view() throws UserPermissionException, MalformedURLException, UnsupportedEncodingException,
			UserAccessDeniedException {

		currentMetaStudy = getMetaStudyFromRequest();
		if (currentMetaStudy == null) {
			return null;
		}
		getSessionMetaStudy().setMetaStudy(currentMetaStudy);


		if (!getHasReadPermission()) {
			sessionMetaStudy.clear();
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}


		// metaStudyDetailsForm = new MetaStudyDetailsForm(currentMetaStudy);

		// decide which version of the page to show
		if (currentMetaStudy.getId() == null) {
			setViewMode(MetaStudyAction.VIEW_MODE_PREVIEW);
		} else if (currentMetaStudy.getStatus().equals(MetaStudyStatus.PUBLISHED)) {
			setViewMode(MetaStudyAction.VIEW_MODE_PUBLISHED);
		} else {
			setViewMode(MetaStudyAction.VIEW_MODE_UNPUBLISHED);
		}

		Date startDate = currentMetaStudy.getDateCreated();
		Date endDate = new Date();
		if (currentMetaStudy.getStatus().equals(MetaStudyStatus.PUBLISHED)) {
			endDate = currentMetaStudy.getPublishedDate();
		}

		// converts the time range above into DAYS
		duration = String.valueOf((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));

		// get the permission and owner information
		currentMetaStudyOwner = accountManager.getEntityOwnerAccount(currentMetaStudy.getId(), EntityType.META_STUDY);
		currentPermissions = accountManager.getAccess(getAccount(), EntityType.META_STUDY, currentMetaStudy.getId());

		return PortalConstants.ACTION_VIEW;
	}

	public String delete() throws UserPermissionException, MalformedURLException, UnsupportedEncodingException,
			UserAccessDeniedException {

		currentMetaStudy = getMetaStudyFromRequest();
		if (currentMetaStudy == null) {
			return null;
		}
		getSessionMetaStudy().setMetaStudy(currentMetaStudy);

		if (!getHasReadPermission()) {
			sessionMetaStudy.clear();
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}

		metaStudyManager.delete(currentMetaStudy.getId());

		return PortalConstants.ACTION_LIST;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/metastudy/metaStudyAction!getResearchMgmtMetaSet.action
	public String getResearchMgmtMetaSet() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<ResearchManagementMeta> outputList = new ArrayList<ResearchManagementMeta>(
					getSessionMetaStudy().getMetaStudy().getResearchMgmtMetaSet());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ResearchMgmtMetaListDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/metastudy/metaStudyAction!getDocumentionList.action
	public String getDocumentionList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<MetaStudyDocumentation> outputList = new ArrayList<MetaStudyDocumentation>(
					getSessionMetaStudy().getMetaStudy().getSupportingDocumentationSet());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new SupportDocIdtListDecorator(getSessionMetaStudy().getMetaStudy()));
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/metastudy/metaStudyAction!getMetaStudyDataSet.action
	public String getMetaStudyDataSet() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<MetaStudyData> outputList =
					new ArrayList<MetaStudyData>(getSessionMetaStudy().getMetaStudy().getMetaStudyDataSet());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new MetaStudyDataIdtListDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/metastudy/metaStudyAction!getClinicalTrialMetaSet.action
	public String getClinicalTrialMetaSet() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<MetaStudyClinicalTrial> outputList = new ArrayList<MetaStudyClinicalTrial>(
					getSessionMetaStudy().getMetaStudy().getClinicalTrialMetaSet());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new MetaStudyClinicalTrialIdtListDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/metastudy/metaStudyAction!getGrantMetaSet.action
	public String getGrantMetaSet() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<MetaStudyGrant> outputList =
					new ArrayList<MetaStudyGrant>(getSessionMetaStudy().getMetaStudy().getGrantMetaSet());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new MetaStudyGrantIdtListDecorator());
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
		sort = null;
		if (orderColumn != null) {
			String orderData = orderColumn.getData();
			if (orderData.equals("id")) {
				sort = "d.id";
			} else if (orderData.equals("name")) {
				sort = "d.name";
			}

			ascending = orderColumn.getOrderDirection().equals(IdtRequest.ORDER_ASCENDING);
		}
	}

	private ArrayList<MetaStudyAccessRecord> paginateAccessRecords(ArrayList<MetaStudyAccessRecord> outputAr,
			IdtRequest request) {
		ArrayList<MetaStudyAccessRecord> output = null;
		int startIndex = request.getStart();
		int requestLength = request.getLength();
		int dataLength = outputAr.size();
		boolean returnAll = (requestLength == 0);
		if (returnAll || (startIndex == 0 && requestLength == dataLength)) {
			output = new ArrayList<MetaStudyAccessRecord>(outputAr);
		} else if (requestLength < dataLength) {
			int endIndex = startIndex + request.getLength();
			if (endIndex > (outputAr.size() - 1)) {
				endIndex = outputAr.size() - 1;
			}
			output = new ArrayList<MetaStudyAccessRecord>(outputAr.subList(startIndex, endIndex));
		} else {
			// otherwise, request length is greater than or equal to data length, so just give up data
			output = outputAr;
		}
		return output;
	}

	/**
	 * This action is called when user submits the create/edit meta study form to save a new MetaStudy object.
	 * 
	 * @return a String that directs to View page
	 * @throws SocketException
	 * @throws IOException
	 * @throws JSchException
	 */
	public String submit() throws SocketException, IOException, JSchException, UserAccessDeniedException {

		saveSession();
		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		boolean isNewMetaStudy = (currentMetaStudy.getId() == null);

		// no point in doing a permission check if the meta study is only in session
		if (!isNewMetaStudy && !getHasWritePermission()) {
			sessionMetaStudy.clear();
			return PortalConstants.ACTION_ERROR;
		}

		Set<MetaStudyDocumentation> docSet = currentMetaStudy.getSupportingDocumentationSet();
		if (docSet != null && !docSet.isEmpty()) {
			for (MetaStudyDocumentation sd : docSet) {
				if (!sd.getIsUrl() && sd.getUserFile() != null) {
					sd.setUserFile(metaStudyManager.saveUserFile(sd.getUserFile()));
				}
			}
		}

		Set<MetaStudyData> newMetaStudyData = getSessionMetaStudy().getNewMetaStudyData();
		Set<MetaStudyData> dataSet = currentMetaStudy.getMetaStudyDataSet();
		if (dataSet != null && !dataSet.isEmpty()) {
			for (MetaStudyData msd : dataSet) {
				if (msd.getSavedQuery() == null && msd.getUserFile() != null) {
					msd.setUserFile(metaStudyManager.saveUserFile(msd.getUserFile()));
				} else if (msd.getSavedQuery() != null && newMetaStudyData.contains(msd)) {
					SavedQuery clonedQuery =
							metaStudyManager.cloneSavedQuery(msd.getSavedQuery(), msd.getDescription());
					msd.setSavedQuery(clonedQuery);

					String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
					RestAccountProvider restProvider =
							new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
					restProvider.registerEntity(getAccount().getId(), EntityType.SAVED_QUERY, clonedQuery.getId(),
							PermissionType.OWNER, null);
				}
			}
		}

		// set the MS status back to draft if you edit the MS
		if (currentMetaStudy.getStatus() != null
				&& currentMetaStudy.getStatus().equals(MetaStudyStatus.AWAITING_PUBLICATION)) {
			currentMetaStudy.setStatus(MetaStudyStatus.DRAFT);
		}

		currentMetaStudy = metaStudyManager.saveMetaStudy(getAccount(), currentMetaStudy);

		// adding DAO calls to save sets seperately
		metaStudyManager.saveMetaStudyTherapeuticAgent(metaStudyDetailsForm.getTherapeuticAgentSet(),
				String.valueOf(currentMetaStudy.getId()));
		metaStudyManager.saveMetaStudyTherapyType(metaStudyDetailsForm.getTherapyTypeSet(),
				String.valueOf(currentMetaStudy.getId()));
		metaStudyManager.saveMetaStudyTherapeuticTarget(metaStudyDetailsForm.getTherapeuticTargetSet(),
				String.valueOf(currentMetaStudy.getId()));
		metaStudyManager.saveMetaStudyModelType(metaStudyDetailsForm.getModelTypeSet(),
				String.valueOf(currentMetaStudy.getId()));
		metaStudyManager.saveMetaStudyModelName(metaStudyDetailsForm.getModelNameSet(),
				String.valueOf(currentMetaStudy.getId()));

		// Set owner permission if it's a new meta study
		if (isNewMetaStudy) {
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider =
					new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			restProvider.registerEntity(getAccount().getId(), EntityType.META_STUDY, currentMetaStudy.getId(),
					PermissionType.OWNER, null);
		}

		getSessionMetaStudy().clear();
		getSessionMetaStudy().setMetaStudy(currentMetaStudy);

		setViewMode(MetaStudyAction.VIEW_MODE_UNPUBLISHED);

		// get the permission and owner information
		currentMetaStudyOwner = accountManager.getEntityOwnerAccount(currentMetaStudy.getId(), EntityType.META_STUDY);
		currentPermissions = accountManager.getAccess(getAccount(), EntityType.META_STUDY, currentMetaStudy.getId());

		return PortalConstants.ACTION_REDIRECT_TO_VIEW;
	}

	/**
	 * This action is called when user clicks the Details chevron in the Meta Study Creation workflow.
	 * 
	 * @return a String that directs to Enter Meta Study Details page
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public String moveToDetails()
			throws MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		// permissions check - if the id is null the meta study hasn't been created yet
		if (currentMetaStudy.getId() != null && !getHasWritePermission()) {
			sessionMetaStudy.clear();
			return PortalConstants.ACTION_ERROR;
		}
		saveSession();
		return PortalConstants.ACTION_INPUT;
	}


	/**
	 * This action is called when user clicks the Documentation chevron in the Meta Study Creation workflow.
	 * 
	 * @return a String that directs to Enter Meta Study Documentation page
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public String moveToDocumentation()
			throws MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		if (currentMetaStudy.getId() != null && !getHasWritePermission()) {
			sessionMetaStudy.clear();
			return PortalConstants.ACTION_ERROR;
		}
		saveSession();
		return PortalConstants.ACTION_DOCUMENTATION;
	}


	/**
	 * This action is called when user clicks the Data chevron in the Meta Study Creation workflow.
	 * 
	 * @return a String that directs to Enter Meta Study Data page
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public String moveToData() throws MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		if (currentMetaStudy.getId() != null && !getHasWritePermission()) {
			sessionMetaStudy.clear();
			return PortalConstants.ACTION_ERROR;
		}
		saveSession();
		return PortalConstants.ACTION_DATASET;
	}


	/**
	 * This action is called when user clicks the Preview chevron in the Meta Study Creation workflow.
	 * 
	 * @return a String that directs to Enter Meta Study Preview page
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public String moveToPreview()
			throws MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		viewMode = MetaStudyAction.VIEW_MODE_PREVIEW;
		if (currentMetaStudy.getId() != null && !getHasWritePermission()) {
			sessionMetaStudy.clear();
			return PortalConstants.ACTION_ERROR;
		}
		saveSession();
		return PortalConstants.ACTION_VIEW;
	}


	public String moveToKeyword()
			throws MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		if (currentMetaStudy.getId() != null && !getHasWritePermission()) {
			sessionMetaStudy.clear();
			return PortalConstants.ACTION_ERROR;
		}
		saveSession();
		metaStudyKeywordForm = new MetaStudyKeywordForm(currentMetaStudy);
		return PortalConstants.META_STUDY_KEYWORD;
	}

	// first part of the 2 part save for editing a meta study
	public StreamResult editDetailsSave() throws UserAccessDeniedException {
		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		// permissions check - if the id is null the meta study hasn't been created yet
		if (currentMetaStudy.getId() != null && !getHasWritePermission()) {
			sessionMetaStudy.clear();
			return new StreamResult(new ByteArrayInputStream((ERROR).getBytes()));
		}
		saveSession();
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	public StreamResult editDocumentDataTableSave() throws MalformedURLException, UnsupportedEncodingException {

		currentMetaStudy = getSessionMetaStudy().getMetaStudy();
		saveSession();

		return validationSuccess();
	}

	public StreamResult editDataDataTableSave() throws MalformedURLException, UnsupportedEncodingException {

		currentMetaStudy = getSessionMetaStudy().getMetaStudy();
		saveSession();

		return validationSuccess();
	}

	public String editKeywordSave() throws UserAccessDeniedException {
		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		// permissions check - if the id is null the meta study hasn't been created yet
		if (currentMetaStudy.getId() != null && !getHasWritePermission()) {
			sessionMetaStudy.clear();
			return PortalConstants.ACTION_ERROR;
		}
		saveSession();
		return "keywordsAndLabels";
	}

	public String editDataSave() throws MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		// permissions check - if the id is null the meta study hasn't been created yet
		if (currentMetaStudy.getId() != null && !getHasWritePermission()) {
			sessionMetaStudy.clear();
			return PortalConstants.ACTION_ERROR;
		}
		saveSession();
		return PortalConstants.ACTION_EDIT_DATA;
	}


	public String displayGrants() {
		return PortalConstants.ACTION_GRANT;
	}

	public String dataRefresh() {
		return PortalConstants.ACTION_EDIT_DATA;
	}


	/**
	 * Adds a new grant to session
	 */
	public String addGrant() {

		MetaStudyGrant grant = new MetaStudyGrant(grantEntry);
		getCurrentStudy().getGrantMetaSet().add(grant);

		grantEntry = new MetaStudyGrant();
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
				MetaStudyGrant grantToRemove = (MetaStudyGrant) gson.fromJson(grantJson, MetaStudyGrant.class);
				if (grantToRemove != null) {
					getCurrentStudy().getGrantMetaSet().remove(grantToRemove);
				}
			} catch (JsonSyntaxException jse) {
				jse.printStackTrace();
			}
		}

		return PortalConstants.ACTION_GRANT;
	}

	public String viewGrantInfo() {
		return "grantDetails";
	}


	public List<MetaStudyDocumentation> getSupportDocList() {

		List<MetaStudyDocumentation> supportDocList = new ArrayList<MetaStudyDocumentation>();
		supportDocList.addAll(getSessionMetaStudy().getMetaStudy().getSupportingDocumentationSet());

		return supportDocList;
	}

	public List<MetaStudyData> getMetaStudyDataList() {

		List<MetaStudyData> dataList = new ArrayList<MetaStudyData>();
		dataList.addAll(getSessionMetaStudy().getMetaStudy().getMetaStudyDataSet());

		return dataList;
	}

	/**
	 * This action is performed as an ajax call when creating a new keyword A new keyword object is created here and
	 * stored in newKeyword where the json call retrieves it.
	 * 
	 * @return a string directing struts to a json object
	 */
	public String createKeyword() {

		newKeyword = new MetaStudyKeyword();
		newKeyword.setKeyword(keywordSearchKey);
		newKeyword.setCount(0L);
		getSessionMetaStudy().addNewKeyword(newKeyword);

		return PortalConstants.ACTION_ADDKEYWORD;
	}

	/**
	 * This action is performed as an ajax call when creating a new keyword A new keyword object is created here and
	 * stored in newKeyword where the json call retrieves it.
	 * 
	 * @return a string directing struts to a json object
	 */
	public String createLabel() {

		newLabel = new MetaStudyLabel();
		newLabel.setLabel(labelSearchKey);
		newLabel.setCount(0L);
		getSessionMetaStudy().addNewLabel(newLabel);

		return PortalConstants.ACTION_ADDLABEL;
	}

	/**
	 * This action is called when user clicks cancel button. It clears the SessionMetaStudy object and navigates to the
	 * list page.
	 * 
	 * @return a String that directs to list Meta Study page.
	 */
	public String cancel() {
		sessionMetaStudy.clear();
		return PortalConstants.ACTION_LIST;
	}


	/**
	 * This action is called when non-admin user requests publication for a meta study. It moves a meta study status
	 * from Draft to Awaiting Publication.
	 * 
	 * @return a string that directs to View page
	 */
	public String requestPublication() throws UserAccessDeniedException {
		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		if (currentMetaStudy.getId() != null && !getHasWritePermission()) {
			return PortalConstants.ACTION_ERROR;
		}

		if (currentMetaStudy.getStatus() != MetaStudyStatus.DRAFT) {
			logger.error("Cannot request publication if a meta study is not in Draft status.");
			return PortalConstants.ACTION_ERROR;
		}

		currentMetaStudy.setStatus(MetaStudyStatus.AWAITING_PUBLICATION);
		currentMetaStudy = metaStudyManager.saveMetaStudy(getAccount(), currentMetaStudy);
		getSessionMetaStudy().setMetaStudy(currentMetaStudy);

		return PortalConstants.ACTION_REDIRECT_TO_VIEW;
	}


	/**
	 * This action is called when an admin user publishes a meta study. It changes the meta study status from Draft or
	 * Awaiting Publication to Published, and also sets the PublishedDate to now.
	 * 
	 * @return a string that directs to View page
	 */
	public String approve() throws UserAccessDeniedException {
		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		if (currentMetaStudy.getId() != null && !getHasAdminPermission()) {
			return PortalConstants.ACTION_ERROR;
		}

		currentMetaStudy.setStatus(MetaStudyStatus.PUBLISHED);
		currentMetaStudy.setPublishedDate(new Date());
		accountManager.registerEntity(accountManager.getPermissionGroup(ServiceConstants.PUBLISHED_META_STUDIES),
				EntityType.META_STUDY, currentMetaStudy.getId(), PermissionType.READ);

		currentMetaStudy = metaStudyManager.saveMetaStudy(getAccount(), currentMetaStudy);
		getSessionMetaStudy().setMetaStudy(currentMetaStudy);

		return PortalConstants.ACTION_REDIRECT_TO_VIEW;
	}


	/**
	 * This action is called when an admin user rejects the publication request a meta study. It changes the meta study
	 * status from Awaiting Publication back to Draft.
	 * 
	 * @return a string that directs to View page
	 */
	public String reject() throws UserAccessDeniedException {
		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		if (currentMetaStudy.getId() != null && !getHasAdminPermission()) {
			return PortalConstants.ACTION_ERROR;
		}

		currentMetaStudy.setStatus(MetaStudyStatus.DRAFT);
		currentMetaStudy = metaStudyManager.saveMetaStudy(getAccount(), currentMetaStudy);
		getSessionMetaStudy().setMetaStudy(currentMetaStudy);

		return PortalConstants.ACTION_REDIRECT_TO_VIEW;
	}


	/**
	 * This action is called when an admin user unpublishes a meta study. It changes the meta study status from
	 * Published back to Draft, and also sets the PublishedDate to null.
	 * 
	 * @return a string that directs to View page
	 */
	public String unpublish() throws UserAccessDeniedException {
		currentMetaStudy = getSessionMetaStudy().getMetaStudy();

		if (currentMetaStudy.getId() != null && !getHasAdminPermission()) {
			return PortalConstants.ACTION_ERROR;
		}

		currentMetaStudy.setStatus(MetaStudyStatus.DRAFT);
		currentMetaStudy.setPublishedDate(null);
		accountManager.removeEntityFromPermissionGroup(getAccount(), ServiceConstants.PUBLISHED_META_STUDIES,
				EntityType.META_STUDY, currentMetaStudy.getId());

		currentMetaStudy = metaStudyManager.saveMetaStudy(getAccount(), currentMetaStudy);
		getSessionMetaStudy().setMetaStudy(currentMetaStudy);

		return PortalConstants.ACTION_REDIRECT_TO_VIEW;
	}

	public String viewSavedQuery() throws Exception {
		SavedQuery sq = savedQueryDao.get(savedQueryId);
		if (sq == null) {
			throw new Exception("The saved query with ID " + savedQueryId + " could not be found");
		} else {
			JsonObject output = SavedQueryUtil.savedQueryToJsonView(sq);
			output.addProperty("name", sq.getName());	// update name in queryData with current saved query name for
															// cloned saved query
			// just in case the json doesn't have this info
			output.addProperty("description", sq.getDescription());
			output.addProperty("lastUpdated", BRICSTimeDateUtil.dateToDateTimeString(sq.getLastUpdated()));
			savedQueryText = output.toString();
			setSavedQueryPermissions(getSavedQueryEntityMap());
		}
		return "viewSavedQuery";
	}

	public String viewSavedQueryInQueryTool() throws Exception {

		if (savedQueryId != null) {
			SavedQuery sq = savedQueryDao.get(savedQueryId);
			if (sq == null) {
				throw new Exception("The saved query with ID " + savedQueryId + " could not be found");
			} else {
				JsonObject output = SavedQueryUtil.savedQueryToJsonView(sq);
				output.addProperty("name", sq.getName());	// update name in queryData with current saved query name
																// for cloned saved query
				// just in case the json doesn't have this info
				output.addProperty("description", sq.getDescription());
				output.addProperty("lastUpdated", BRICSTimeDateUtil.dateToDateTimeString(sq.getLastUpdated()));
				savedQueryText = output.toString();
				setSavedQueryPermissions(getSavedQueryEntityMap());
			}
		}
		return "viewSavedQueryInQueryTool";
	}



	public List<EntityMap> getSavedQueryEntityMap() throws MalformedURLException, UnsupportedEncodingException {

		List<EntityMap> entityMapList = new ArrayList<EntityMap>();
		if (savedQueryId != null) {

			// NOTE: If there are no rows returned from the entity map table, THE ENTITY MAP WILL RETURN NULL
			entityMapList =
					webServiceManager.listEntityAccessRestful(getAccount(), savedQueryId, EntityType.SAVED_QUERY);
		}

		return entityMapList;
	}

	public String searchReports() {
		List<MetaStudyAccessRecord> accessRecords = metaStudyManager.getAccessRecordByMetaStudyId(metaStudyId);
		setMetaStudyAccessRecords(accessRecords);

		metaStudyAccessRecordStatistics(accessRecords);
		return "metaStudyAccessTable";
	}

	/**
	 * The search and table update function for metaStudyAccessRecord. Uses the study in session and any filters applied
	 * to search for access reports.
	 * 
	 * @return : string MetaStudyAccessRecord for struts
	 * @throws UserPermissionException : if the current user does not have admin access to the current study
	 */
	public String searchReportsList() throws UserPermissionException, UserAccessDeniedException {
		// Permission Check. This search is only visible to admins and those with admin acces to the current study.
		if (!getIsAdmin() && !getHasAdminPermission()) {

			throw new UserPermissionException(ServiceConstants.ADMIN_ACCESS_DENIED);
		}

		IdtInterface idt;
		try {
			idt = new Struts2IdtInterface();
			IdtRequest request = idt.getRequest();
			currentMetaStudy = getSessionMetaStudy().getMetaStudy();
			int countTotalRcord = metaStudyManager.countAccessRecords(currentMetaStudy.getId());

			idt.setTotalRecordCount(countTotalRcord);

			updateAccessRecordOrder(request.getOrderColumn());
			updateAccessRecordFilter(request.getFilters());
			updateAccessRecordSearch(request);

			PaginationData pageData = new PaginationData();
			// these parameters are set by the methods above
			pageData.setAscending(ascending);
			pageData.setSort(sort);
			pageData.setPageSize(request.getLength());
			pageData.setPage(request.getPageNumber());
			long startTime = System.nanoTime();

			List<MetaStudyAccessRecord> recordList =
					metaStudyManager.searchAccessRecords(getSessionMetaStudy().getMetaStudy(), key, daysOld, pageData);

			getSessionMetaStudy().setAccessRecordList(recordList);
			ArrayList<MetaStudyAccessRecord> outputAr = new ArrayList<MetaStudyAccessRecord>(recordList);
			if (key == null && daysOld == null) {
				idt.setFilteredRecordCount(countTotalRcord);
			} else {
				idt.setFilteredRecordCount(outputAr.size());
			}
			outputAr = paginateAccessRecords(outputAr, request);

			idt.setList(outputAr);
			idt.decorate(new MetaStudyAccessRecordsIdtListDecorator());

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

	/**
	 * This search grabs the accessRecordList stored in session (it needs to be set by searchReportsList() or another
	 * source) and converts it to CSV. This action assumes that the user already has access to the report.
	 * 
	 * @return : struts result "export"
	 */
	public String downloadReport() {
		ByteArrayOutputStream baos = AccessReportExportUtil
				.exportMetaStudyAccessReportToCSV(getSessionMetaStudy().getAccessRecordList(), false);
		inputStream = new ByteArrayInputStream(baos.toByteArray());
		exportFileName = getSessionMetaStudy().getMetaStudy().getPrefixId() + "_Access_Report_"
				+ BRICSTimeDateUtil.formatDate(new Date()) + ".csv";
		return PortalConstants.ACTION_EXPORT;
	}

	public String adminDownloadReport() throws UserPermissionException {
		if (!getIsAdmin()) {
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
		List<MetaStudyAccessRecord> records = null;
		do {
			pageData.setPage(pageData.getPage() + 1);
			records = metaStudyManager.getMetaStudyAccessRecords(null, startAccessReportDate, endAccessReportDate,
					pageData);
			util.writeMetaStudyAccessData(records);

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

	/*
	 * this method will take a list of access records and set class variables to display on the stats page
	 */
	private void metaStudyAccessRecordStatistics(List<MetaStudyAccessRecord> accessRecords) {
		int supportingDocumentCount = 0;
		int dataCount = 0;
		double totalFileSizeCount = 0;

		for (MetaStudyAccessRecord msar : accessRecords) {
			if (msar.getMetaStudyData() != null) {
				dataCount++;
				totalFileSizeCount = totalFileSizeCount + msar.getMetaStudyData().getUserFile().getSize();
			} else {
				supportingDocumentCount++;
				totalFileSizeCount = totalFileSizeCount + msar.getSupportingDocumentation().getUserFile().getSize();
			}
		}
		setDataFileDownloads(dataCount);
		setDocumentFileDownloads(supportingDocumentCount);
		setTotalDownloads(dataCount + supportingDocumentCount);
		setTotalDownloadSize(bytesToMetabytes(totalFileSizeCount));

	}

	public String addResearchManagement() throws SocketException, IOException, JSchException {

		ResearchManagementMeta resMgmt = new ResearchManagementMeta(researchMgmtMetaEntry);

		resMgmt.setId(Long.valueOf(getSessionMetaStudy().getNewResearchManagement()));
		getSessionMetaStudy().setNewResearchManagement(getSessionMetaStudy().getNewResearchManagement() - 1);

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
		}
		logger.debug("add: " + resMgmt.getId());
		getSessionMetaStudy().addResearchMgmt(resMgmt.getId(), resMgmt);
		getCurrentStudy().getResearchMgmtMetaSet().add(resMgmt);

		// Clear the entry fields
		researchMgmtMetaEntry = new ResearchManagementMeta();
		getSessionUploadFile().clear();
		picture = null;

		return PortalConstants.ACTION_EDIT_RESEARCH_MANAGEMENT;
	}

	public ResearchManagementMeta convertJsonStrToResMgmtMetaObj(JsonObject resMgmtMetaJsonObj) {

		logger.debug("convertJsonStrToResMgmtMetaObj: " + resMgmtMetaJsonObj);

		Gson gson = new Gson();
		return (ResearchManagementMeta) gson.fromJson(resMgmtMetaJsonObj, ResearchManagementMeta.class);

	}

	public String getResMgmtMetaEntryToEdit() throws SocketException, IOException, JSchException, SftpException {
		String researchMgmtEntryJson = getRequest().getParameter("researchMgmtEntryJson");
		JsonParser jsonParser = new JsonParser();
		JsonObject resMgmtMetaJsonObj = jsonParser.parse(researchMgmtEntryJson).getAsJsonObject();
		researchMgmtMetaEntry = convertJsonStrToResMgmtMetaObj(resMgmtMetaJsonObj);

		UserFile pictureFile = null;

		Map<Long, ResearchManagementMeta> resMgmtSet = getSessionMetaStudy().getResearchMgmtMap();
		if (resMgmtSet != null && resMgmtSet.containsKey(researchMgmtMetaEntry.getId())) {
			pictureFile = resMgmtSet.get(researchMgmtMetaEntry.getId()).getPictureFile();
		}

		if (pictureFile == null && researchMgmtMetaEntry.getId() > 0) {
			pictureFile = metaStudyManager
					.getMetaStudyManagementImage(getCurrentStudy().getId(), researchMgmtMetaEntry.getId())
					.getPictureFile();
		}

		if (pictureFile != null) {
			pictureFileName = pictureFile.getName();

		} else {
			pictureFileName = "";
		}

		return PortalConstants.ACTION_EDIT_RESEARCH_MANAGEMENT;
	}

	public String editResearchMgmtAjax() throws SocketException, IOException, JSchException {
		logger.debug("selectedResMgmtToEdit: " + selectedResMgmtToEdit);

		JsonParser jsonParser = new JsonParser();
		JsonObject resMgmtMetaJsonObj = jsonParser.parse(selectedResMgmtToEdit).getAsJsonObject();
		ResearchManagementMeta resMgmtToEdit = convertJsonStrToResMgmtMetaObj(resMgmtMetaJsonObj); // old entry
		ResearchManagementMeta resMgmtEdited = new ResearchManagementMeta(researchMgmtMetaEntry); // instance from
																									 // formData
		resMgmtEdited.setId(resMgmtToEdit.getId());

		logger.debug("editResearchManagement resMgmtEdited.getRole: " + resMgmtEdited.getRole() + " mi: "
				+ resMgmtEdited.getMi());

		Long resMgmtEditedId = resMgmtToEdit.getId();
		UserFile pictureFileOld = null;
		boolean isRemoved = false;

		if (resMgmtEditedId != null && resMgmtEditedId > 0) { /* add attached new userfile to existing entry */

			isRemoved = removeResearchManagementObj(resMgmtToEdit);
			if (!isRemoved) {
				logger.error("Failed to remove the old ResearchManagement Entry!");
			} else {
				ResearchManagementMeta rearchMgt =
						metaStudyManager.getMetaStudyManagementImage(getCurrentStudy().getId(), resMgmtEditedId);
				resMgmtEdited.setId(resMgmtEditedId);

				if (pictureFileName != null) {
					UserFile picFile = repositoryManager.uploadFile(getUser().getId(), picture, pictureFileName, null,
							ServiceConstants.FILE_TYPE_RESEARCHER_PICTURE, new Date());
					if (picFile != null) {
						rearchMgt.setPictureFile(picFile);
						resMgmtEdited.setPictureFile(picFile);
					}
				} else {
					resMgmtEdited.setPictureFile(rearchMgt.getPictureFile());
				}

				getSessionMetaStudy().addResearchMgmt(resMgmtEdited.getId(), resMgmtEdited);
				getCurrentStudy().getResearchMgmtMetaSet().add(resMgmtEdited);
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
			getSessionMetaStudy().addResearchMgmt(resMgmtEdited.getId(), resMgmtEdited);
			getCurrentStudy().getResearchMgmtMetaSet().add(resMgmtEdited);


		}

		researchMgmtMetaEntry = new ResearchManagementMeta();
		return PortalConstants.ACTION_EDIT_RESEARCH_MANAGEMENT;
	}

	private Boolean removeResearchManagementObj(ResearchManagementMeta resMgmtToEdit) {
		Boolean isRemoved = false;
		for (Iterator<ResearchManagementMeta> iterator = getCurrentStudy().getResearchMgmtMetaSet().iterator(); iterator
				.hasNext();) {
			ResearchManagementMeta metaManagement = iterator.next();
			if ((metaManagement).stringEquals(resMgmtToEdit)) {
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

		String researchMgmtJsonStr =
				getRequest().getParameter("researchMgmtJson").replaceAll("\\\\", "").replaceAll("^\"|\"$", "");

		logger.debug("researchMgmtJsonArr: " + researchMgmtJsonStr);

		if (!StringUtils.isEmpty(researchMgmtJsonStr)) {

			JsonParser jsonParser = new JsonParser();
			JsonArray researchMgmtJsonArr = (JsonArray) jsonParser.parse(researchMgmtJsonStr);

			for (JsonElement resMgmtMetaJson : researchMgmtJsonArr) {
				JsonObject resMgmtMetaJsonObj = resMgmtMetaJson.getAsJsonObject();
				logger.debug(resMgmtMetaJsonObj);

				try {
					ResearchManagementMeta resMgmtToRemove = convertJsonStrToResMgmtMetaObj(resMgmtMetaJsonObj);

					if (resMgmtToRemove != null) {
						// if it's a new research management, we also delete the picture file associated with it.
						if (resMgmtToRemove.getId() == null && resMgmtToRemove.getPictureFile() != null) {
							repositoryManager.removeUserFile(resMgmtToRemove.getPictureFile());
						}

						boolean isremoved = removeResearchManagementObj(resMgmtToRemove);
						if (!isremoved) {
							logger.error("Failed to remove meta study research management entry");
						}
					}
				} catch (JsonSyntaxException jse) {
					jse.printStackTrace();
				}

			} // end for loop
		} // end if

		return PortalConstants.ACTION_EDIT_RESEARCH_MANAGEMENT;
	}


	/**
	 * Adds a new clinical trial to session
	 */
	public String addClinicalTrial() {

		if (!StringUtils.isEmpty(clinicalTrialId)) {
			MetaStudyClinicalTrial ct = new MetaStudyClinicalTrial();
			ct.setClinicalTrialId(clinicalTrialId);
			getCurrentStudy().getClinicalTrialMetaSet().add(ct);
		}

		clinicalTrialId = null;
		return PortalConstants.ACTION_CLINICAL_TRIAL;
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
	 * Removes a clinical trial
	 * 
	 * @return
	 */
	public String removeClinicalTrial() {

		String ctIdToRemove = getRequest().getParameter("ctIdToRemove");

		if (!StringUtils.isEmpty(ctIdToRemove)) {
			Iterator<MetaStudyClinicalTrial> it = getCurrentStudy().getClinicalTrialMetaSet().iterator();
			while (it.hasNext()) {
				MetaStudyClinicalTrial ct = it.next();
				if (ctIdToRemove.equals(ct.getClinicalTrialId())) {
					it.remove();
					break;
				}
			}
		}

		return PortalConstants.ACTION_CLINICAL_TRIAL;
	}

	/**
	 * This action method will attempt to create a DOI for the current meta study object. Once a DOI is created, it will
	 * be sent back to the user's browser as JSON.
	 * 
	 * @return The "success" result if a DOI was created, or "error" if there was an error.
	 */
	public String createDOIForMetaStudy() throws UserAccessDeniedException {
		MetaStudy currMs = getSessionMetaStudy().getMetaStudy();

		// Create a DOI for this published meta study, if able.
		if ((currMs != null) && canAssignDoiForMetaStudy()) {
			logger.info("Creating DOI for the " + currMs.getPrefixId() + " meta study...");

			try {
				metaStudyManager.createDoiForMetaStudy(currMs);
				logger.info("Created the " + currMs.getDoi() + " DOI for the " + currMs.getPrefixId() + " meta study.");

				// Save DOI data to the database and session.
				metaStudyManager.saveMetaStudy(getAccount(), currMs);

				// Create the JSON response.
				JsonObject output = new JsonObject();

				output.addProperty("doi", currMs.getDoi());
				setResponseJson(output.toString());
			} catch (DoiWsValidationException dwve) {
				logger.error("Validation error from the minter service, when creating a DOI for the "
						+ currMs.getPrefixId() + " meta study.", dwve);
				errRespMsg = dwve.getMessage();

				return ERROR;
			} catch (Exception e) {
				logger.error("Error occured while creating a DOI for the " + currMs.getPrefixId() + " meta study.", e);
				errRespMsg = e.getMessage();

				return ERROR;
			}
		} else {
			String msg = "DOI creation is not enabled for this instance, or there was a permissions violation.";

			logger.error(msg);
			errRespMsg = msg;

			return ERROR;
		}

		return SUCCESS;
	}



	public String getDocumentsActionName() {
		return "metaStudyDocAction";
	}

	// need to move this into a utils file and rewrite to make recursive
	private double bytesToMetabytes(double countBytes) {
		DecimalFormat sizeFormat = new DecimalFormat(".##");
		countBytes = countBytes / 1024;
		countBytes = countBytes / 1024;
		return Double.parseDouble(sizeFormat.format(countBytes));
	}


	public ResearchManagementRole[] getRoleList() {
		return ResearchManagementRole.values();
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

	@Override
	public void validate() {
		if (hasFieldErrors()) {
			logger.debug("validate() has field errors: " + getFieldErrors().size());
			getSessionUploadFile().clear();
			if (picture != null) {
				try {
					getSessionUploadFile().setUploadFile(BRICSFilesUtils.copyFile(picture));
				} catch (IOException e) {
					logger.error("Failed to read upload picture to byte array in validation.");
					e.printStackTrace();
				}
			}

		}
	}

	public StudyType[] getMetaStudyTypes() {

		List<StudyType> studyTypeList = staticManager.getStudyTypeList();
		StudyType[] studyTypeArray = new StudyType[studyTypeList.size()];
		studyTypeList.toArray(studyTypeArray);

		return studyTypeArray;
	}

	public RecruitmentStatus[] getRecruitmentStatuses() {
		return RecruitmentStatus.values();
	}

	public MetaStudyManager getMetaStudyManager() {
		return metaStudyManager;
	}

	public void setMetaStudyManager(MetaStudyManager metaStudyManager) {
		this.metaStudyManager = metaStudyManager;
	}

	public MetaStudy getCurrentMetaStudy() {
		return currentMetaStudy;
	}

	public void setCurrentMetaStudy(MetaStudy currentMetaStudy) {
		this.currentMetaStudy = currentMetaStudy;
	}

	public MetaStudyDetailsForm getMetaStudyDetailsForm() {
		return metaStudyDetailsForm;
	}

	public void setMetaStudyDetailsForm(MetaStudyDetailsForm metaStudyDetailsForm) {
		this.metaStudyDetailsForm = metaStudyDetailsForm;
	}

	public String getGrantId() {
		return grantId;
	}

	public void setGrantId(String grantId) {
		this.grantId = grantId;
	}

	public void setNewKeyword(MetaStudyKeyword newKeyword) {
		this.newKeyword = newKeyword;
	}

	public MetaStudyKeyword getNewKeyword() {
		return newKeyword;
	}

	public void setNewLabel(MetaStudyLabel newLabel) {
		this.newLabel = newLabel;
	}

	public MetaStudyLabel getNewLabel() {
		return newLabel;
	}

	public String getKeywordSearchKey() {
		return keywordSearchKey;
	}

	public void setKeywordSearchKey(String keywordSearchKey) {
		this.keywordSearchKey = keywordSearchKey;
	}

	public String getLabelSearchKey() {
		return labelSearchKey;
	}

	public void setLabelSearchKey(String labelSearchKey) {
		this.labelSearchKey = labelSearchKey;
	}

	public String getViewMode() {
		return viewMode;
	}

	public void setViewMode(String viewMode) {
		this.viewMode = viewMode;
	}

	public Account getCurrentMetaStudyOwner() {
		return currentMetaStudyOwner;
	}

	public void setCurrentMetaStudyOwner(Account currentMetaStudyOwner) {
		this.currentMetaStudyOwner = currentMetaStudyOwner;
	}

	public EntityMap getCurrentPermissions() {
		return currentPermissions;
	}

	public void setCurrentPermissions(EntityMap currentPermissions) {
		this.currentPermissions = currentPermissions;
	}

	public MetaStudyKeywordForm getMetaStudyKeywordForm() {
		return metaStudyKeywordForm;
	}

	public void setMetaStudyKeywordForm(MetaStudyKeywordForm metaStudyKeywordForm) {
		this.metaStudyKeywordForm = metaStudyKeywordForm;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getSavedQueryText() {
		return savedQueryText;
	}

	public void setSavedQueryText(String savedQueryText) {
		this.savedQueryText = savedQueryText;
	}

	public Long getSavedQueryId() {
		return savedQueryId;
	}

	public void setSavedQueryId(Long savedQueryId) {
		this.savedQueryId = savedQueryId;
	}

	public List<EntityMap> getSavedQueryPermissions() {
		return savedQueryPermissions;
	}

	public void setSavedQueryPermissions(List<EntityMap> savedQueryPermissions) {
		this.savedQueryPermissions = savedQueryPermissions;
	}

	public Long getMetaStudyId() {
		return metaStudyId;
	}

	public void setMetaStudyId(Long metaStudyId) {
		this.metaStudyId = metaStudyId;
	}

	public List<MetaStudyAccessRecord> getMetaStudyAccessRecords() {
		return metaStudyAccessRecords;
	}

	public void setMetaStudyAccessRecords(List<MetaStudyAccessRecord> metaStudyAccessRecords) {
		this.metaStudyAccessRecords = metaStudyAccessRecords;
	}

	public int getDataFileDownloads() {
		return dataFileDownloads;
	}

	public void setDataFileDownloads(int dataFileDownloads) {
		this.dataFileDownloads = dataFileDownloads;
	}

	public int getDocumentFileDownloads() {
		return documentFileDownloads;
	}

	public void setDocumentFileDownloads(int documentFileDownloads) {
		this.documentFileDownloads = documentFileDownloads;
	}

	public int getTotalDownloads() {
		return totalDownloads;
	}

	public void setTotalDownloads(int totalDownloads) {
		this.totalDownloads = totalDownloads;
	}

	public double getTotalDownloadSize() {
		return totalDownloadSize;
	}

	public void setTotalDownloadSize(double totalDownloadSize) {
		this.totalDownloadSize = totalDownloadSize;
	}



	public String getClinicalTrialId() {
		return clinicalTrialId;
	}


	public void setClinicalTrialId(String clinicalTrialId) {
		this.clinicalTrialId = clinicalTrialId;
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


	public ResearchManagementMeta getResearchMgmtMetaEntry() {
		return researchMgmtMetaEntry;
	}


	public void setResearchMgmtMetaEntry(ResearchManagementMeta researchMgmtMetaEntry) {
		this.researchMgmtMetaEntry = researchMgmtMetaEntry;
	}


	public ClinicalStudy getCurrentClinicalStudy() {
		return currentClinicalStudy;
	}


	public void setCurrentClinicalStudy(ClinicalStudy currentClinicalStudy) {
		this.currentClinicalStudy = currentClinicalStudy;
	}

	public MetaStudyGrant getGrantEntry() {
		return grantEntry;
	}

	public void setGrantEntry(MetaStudyGrant grantEntry) {
		this.grantEntry = grantEntry;
	}

	public String getResponseJson() {
		return responseJson;
	}


	public void setResponseJson(String responseJson) {
		this.responseJson = responseJson;
	}


	public String getErrRespMsg() {
		return errRespMsg;
	}


	public void setErrRespMsg(String errRespMsg) {
		this.errRespMsg = errRespMsg;
	}

	public String getSelectedResMgmtToEdit() {
		return selectedResMgmtToEdit;
	}

	public void setSelectedResMgmtToEdit(String selectedResMgmtToEdit) {
		this.selectedResMgmtToEdit = selectedResMgmtToEdit;
	}

	public List<TherapeuticAgent> getAllTherapeuticAgents() {
		return metaStudyManager.getAllTherapeuticAgents();
	}

	public List<TherapyType> getAllTherapyTypes() {
		return metaStudyManager.getAllTherapyTypes();
	}

	public List<TherapeuticTarget> getAllTherapeuticTargets() {
		return metaStudyManager.getAllTherapeuticTargets();
	}

	public List<ModelName> getAllModelNames() {
		return metaStudyManager.getAllModelNames();
	}

	public List<ModelType> getAllModelTypes() {
		return metaStudyManager.getAllModelTypes();
	}

	public List<TherapeuticAgent> getTherapeuticAgentSet() {
		return metaStudyDao.getMetaStudyTherapeuticAgents(getSessionMetaStudy().getMetaStudy().getId().toString());
	}

	public List<TherapeuticTarget> getTherapeuticTargetSet() {
		return metaStudyDao.getMetaStudyTherapeuticTargets(getSessionMetaStudy().getMetaStudy().getId().toString());
	}

	public List<TherapyType> getTherapyTypeSet() {
		return metaStudyDao.getMetaStudyTherapyTypes(getSessionMetaStudy().getMetaStudy().getId().toString());
	}

	public List<ModelName> getModelNameSet() {
		return metaStudyDao.getMetaStudyModelNames(getSessionMetaStudy().getMetaStudy().getId().toString());
	}

	public List<ModelType> getModelTypeSet() {
		return metaStudyDao.getMetaStudyModelTypes(getSessionMetaStudy().getMetaStudy().getId().toString());
	}
}
