package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.WebServiceManager;
import gov.nih.tbi.dictionary.exceptions.DictionaryVersioningException;
import gov.nih.tbi.dictionary.model.DataStructureForm;
import gov.nih.tbi.dictionary.model.FormStructureStandardization;
import gov.nih.tbi.dictionary.model.InstanceRequiredFor;
import gov.nih.tbi.dictionary.model.SessionDictionaryStatusChange;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.model.UpdateFormStructureReferencePayload;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;
import gov.nih.tbi.dictionary.model.hibernate.DictionarySupportingDocumentation;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.dictionary.service.rulesengine.RulesEngineUtils;
import gov.nih.tbi.dictionary.ws.RestDictionaryProvider;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.SupportingDocumentationInterface;
import gov.nih.tbi.taglib.datatableDecorators.DataElementListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.DictionaryEventLogListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.SupportDocIdtListDecorator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.mail.MessagingException;
import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.result.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class DataStructureAction extends BaseDictionaryAction {
	private static final long serialVersionUID = -3437401823167431396L;
	private static final Logger logger = Logger.getLogger(DataStructureAction.class);

	private static final String ACTION_DETAILS = "detailsPage";
	private static final String ACTION_ELEMENTS = "elementsPage";
	private static final String ACTION_PERMISSIONS = "permissionsPage";
	private static final String ACTION_DOCUMENTATIONS = "documentationsPage";

	/******************************************************************************************************/

	@Autowired
	WebServiceManager webServiceManager;

	FormStructure currentDataStructure;

	DataStructureForm dataStructureForm;

	MapElement mapElement;

	RepeatableGroup repeatableGroup;

	Long statusId;

	Boolean readOnly;

	Boolean fromRepository;

	Boolean shortNameChangeRequired;

	List<Disease> diseaseOptions;

	List<Subgroup> subDomainOptions;

	String reason;

	String fileErrors = "";

	List<String> errors;

	String auditNote;

	Boolean hasAssociatedEforms;

	private List<String> changeStrings;
	private SeverityLevel formStructureChangeSeverity;

	// this should be true if being loaded from query tool
	private Boolean queryArea;


	// FormStructure status change
	String statusReason;
	String attachedDEReason;
	Long currentId;
	boolean backNavigation = false;
	boolean inDataElementPage = false;

	private Set<DictionaryEventLog> eventLogList = new HashSet<DictionaryEventLog>();
	private Set<DataElement> dataElementList;

	// minorOrmajor data element change
	private Set<DictionaryEventLog> minorMajorChangeLogList = new HashSet<DictionaryEventLog>();
	// status change list e.g. published,deprecated
	private Set<DictionaryEventLog> statusChangeLogList = new HashSet<DictionaryEventLog>();

	@Autowired
	SessionDictionaryStatusChange sessionDictionaryStatusChange;


	/******************************************************************************************************/
	// BaseDatastructureAction functionalities
	public PermissionType getSessionFormStructurePermission() throws UnsupportedEncodingException, UserAccessDeniedException {
		if (sessionDataStructure.getSessionDataStructureUserPermissionType() != null) {
			return sessionDataStructure.getSessionDataStructureUserPermissionType();
		} else {
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider =
					new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			PermissionType userEntityAccess = restProvider
					.getAccess(getAccount().getId(), EntityType.DATA_STRUCTURE, getCurrentDataStructure().getId())
					.getPermission();

			sessionDataStructure.setSessionDataStructureUserPermissionType(userEntityAccess);
			return sessionDataStructure.getSessionDataStructureUserPermissionType();
		}
	}

	public Boolean getIsDataStructureAdmin() throws UserAccessDeniedException{

		try {
			return (getSessionFormStructurePermission() != null
					&& PermissionType.compare(getSessionFormStructurePermission(), PermissionType.ADMIN) >= 0);
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			return false;
		}

	}

	public boolean getCanEdit() throws UserAccessDeniedException{

		// If the form is published (or archived), we are no longer using access
		// but just going with if the user is a
		// dictionary admin or not.
		if (StatusType.PUBLISHED.equals(getCurrentDataStructure().getStatus())
				|| StatusType.ARCHIVED.equals(getCurrentDataStructure().getStatus())) {
			return getIsDictionaryAdmin();
		} else {
			try {
				return (getSessionFormStructurePermission() != null
						&& PermissionType.compare(getSessionFormStructurePermission(), PermissionType.WRITE) >= 0);
			} catch (UnsupportedEncodingException uee) {
				uee.printStackTrace();
				return false;
			}
		}
	}

	public boolean getCanRead() throws UserAccessDeniedException{
		try {
			return (getSessionFormStructurePermission() != null
					&& PermissionType.compare(getSessionFormStructurePermission(), PermissionType.READ) >= 0);
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			return false;
		}
	}

	public boolean getCanAdmin() throws UserAccessDeniedException{

		if (sessionDataStructure.getNewStructure() == false) {

			try {
				return (getSessionFormStructurePermission() != null
						&& PermissionType.compare(getSessionFormStructurePermission(), PermissionType.ADMIN) >= 0);
			} catch (UnsupportedEncodingException uee) {
				uee.printStackTrace();
			}
		} else {
			return true;
		}

		return false;
	}

	/******************************************************************************************************/
	public Date getCurrentDate() {

		return new Date();
	}

	public Boolean getQueryArea() {

		return queryArea;
	}

	public void setQueryArea(Boolean queryArea) {

		this.queryArea = queryArea;
	}

	public String getReason() {

		return reason;
	}

	public void setReason(String reason) {

		this.reason = reason;
	}

	public void setStatusId(Long statusId) {

		this.statusId = statusId;
	}

	public MapElement getMapElement() {

		return mapElement;
	}

	public void setMapElement(MapElement mapElement) {

		this.mapElement = mapElement;
	}

	public RepeatableGroup getRepeatableGroup() {

		return repeatableGroup;
	}

	public void setRepeatableGroup(RepeatableGroup repeatableGroup) {

		this.repeatableGroup = repeatableGroup;
	}

	public boolean getIsRequired() {
		currentDataStructure = getSessionDataStructure().getDataStructure();
		if (currentDataStructure.getInstancesRequiredFor() == null) {
			return false;
		} else {
			return currentDataStructure.getInstancesRequiredFor().contains(new InstanceRequiredFor(getOrgName()));
		}
	}

	/**
	 * The StructuralFormStructure object containing a set of RGs prevents order form working correctly. Changing the
	 * model to use a list instead of a set is too high of a risk change to make 2 days form a release. This is tracked
	 * in Technical Debt ticket PS-529. This method is also included in DataStructureElementAction.java this will need
	 * to be looked at again.
	 * 
	 * @return
	 */
	public List<RepeatableGroup> getAllRepeatableGroups() {

		List<RepeatableGroup> returnList = new ArrayList<RepeatableGroup>(currentDataStructure.getRepeatableGroups());
		// insertion sort
		int i;
		int j;
		RepeatableGroup newValue;
		for (i = 1; i < returnList.size(); i++) {
			newValue = returnList.get(i);
			j = i;
			while (j > 0 && returnList.get(j - 1).getPosition() > newValue.getPosition()) {
				returnList.set(j, returnList.get(j - 1));
				j--;
			}
			returnList.set(j, newValue);
		}
		return returnList;
	}

	public Boolean getReadOnly() {

		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {

		this.readOnly = readOnly;
	}

	public Boolean getFromRepository() {

		return fromRepository;
	}

	public void setFromRepository(boolean fromRepository) {

		this.fromRepository = fromRepository;
	}

	public Boolean getShortNameChangeRequired() {

		return shortNameChangeRequired;
	}

	public SubmissionType[] getFileTypeList() {

		return SubmissionType.getMainTypes();
	}

	public static FormStructureStandardization[] getMainStandardizationTypes() {
		return FormStructureStandardization.getMainStandardizationTypes();
	}

	public List<String> getChangeStrings() {

		return changeStrings;
	}

	public SeverityLevel getFormStructureChangeSeverity() {

		return formStructureChangeSeverity;
	}

	public void setFormStructureChangeSeverity(String formStructureChangeSeverity) {

		this.formStructureChangeSeverity = SeverityLevel.getByName(formStructureChangeSeverity);
	}

	public void setAuditNote(String auditNote) {

		this.auditNote = auditNote;
	}

	public String getAuditNote() {

		return auditNote;
	}

	public Boolean getIsAllPublished() {

		currentDataStructure = getSessionDataStructure().getDataStructure();

		if (currentDataStructure != null) {
			for (DataElement mapElement : (currentDataStructure).getDataElements().values()) {
				if (DataElementStatus.DRAFT == mapElement.getStatus()
						|| DataElementStatus.AWAITING == mapElement.getStatus()) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Returns true if the form structure has a data element in Retired status
	 * 
	 * @return
	 */
	public int getRetiredDataElementCount() {

		int count = 0;
		currentDataStructure = getSessionDataStructure().getDataStructure();

		if (currentDataStructure != null) {
			for (DataElement de : (currentDataStructure).getDataElements().values()) {
				if (DataElementStatus.RETIRED == de.getStatus()) {
					count++;
				}
			}
		}

		return count;
	}

	/**
	 * Returns true if the form structure has a data element in Deprecated status
	 * 
	 * @return
	 */
	public int getDeprecatedDataElementCount() {

		int count = 0;
		currentDataStructure = getSessionDataStructure().getDataStructure();

		if (currentDataStructure != null) {
			for (DataElement de : (currentDataStructure).getDataElements().values()) {
				if (DataElementStatus.DEPRECATED == de.getStatus()) {
					count++;
				}
			}
		}

		return count;
	}

	/**
	 * returns true if the from structure in session is the latest version.
	 * 
	 * @return
	 */
	public boolean getIsLatestVersion() {

		FormStructure isLatestVer = getSessionDataStructure().getDataStructure();
		return dictionaryManager.getIsLatestVersion(isLatestVer);
	}

	public FormStructure getCurrentDataStructure() {

		if (currentDataStructure == null) {
			currentDataStructure = getSessionDataStructure().getDataStructure();
		}


		return currentDataStructure;
	}

	public DataStructureForm getDataStructureForm() {

		return dataStructureForm;
	}

	public void setDataStructureForm(DataStructureForm dataStructureForm) {

		this.dataStructureForm = dataStructureForm;
	}

	public String getFileErrors() {

		return fileErrors;
	}

	public void setFileErrors(String fileErrors) {

		this.fileErrors = fileErrors;
	}

	public Boolean getEnforceStaticFields() {

		FormStructure currentDataStructure = getSessionDataStructure().getDataStructure();

		if (currentDataStructure == null) {
			return true;
		}

		switch (currentDataStructure.getStatus()) {
			case DRAFT:
				return false;
			case AWAITING_PUBLICATION: // global admins can edit awaiting
										 // publication forms
				return !webServiceManager.getAccountProvider(getAccount(), getDiseaseId()).hasRole(getAccount(),
						RoleType.ROLE_ADMIN);
			case PUBLISHED:
				return true;
			case ARCHIVED:
				return true;
			default:
				return true;
		}
	}

	/**
	 * This method returns a list of all requiredTypes for populating form select options.
	 * 
	 * @return List<RequiredType>
	 */
	public RequiredType[] getRequiredTypes() {

		return RequiredType.values();
	}

	public List<Disease> getDiseaseOptions() throws MalformedURLException, UnsupportedEncodingException {

		return staticManager.getDiseaseList();
	}

	/**
	 * return a list of the sub domain groups for iteration and display in the DE table
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<Subgroup> getSubDomainOptions() throws MalformedURLException, UnsupportedEncodingException {

		return staticManager.getSubgroupList();
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws UserPermissionException
	 ****************************************************************************************************/

	public String view() throws MalformedURLException, UnsupportedEncodingException, UserPermissionException, UserAccessDeniedException {

		// clear FS permission session
		sessionDataStructure.clearPermission();

		// clear statuschange session
		sessionDictionaryStatusChange.clear();

		// set the data structure in the session
		sessionDataStructure();

		Long originalEntityId = dictionaryService
				.getOriginalFormStructureIdByName(getSessionDataStructure().getDataStructure().getShortName());
		eventLogList = dictionaryService.getAllFSEventLogs(originalEntityId);

		updateEventLogListWithUsers(eventLogList);

		return PortalConstants.ACTION_VIEW;
	}

	/*
	 * This method is called by the view and detailedDEReport methods it sets the session data structure according to
	 * the parameters contained in the url
	 */
	public void sessionDataStructure()
			throws MalformedURLException, UnsupportedEncodingException, UserPermissionException, UserAccessDeniedException {

		// TODO: MV 6/10/2014 - It is no longer possible to fetch a
		// DataStructure (or DataElement) by id so that part of
		// the code should be removed.
		readOnly = true;
		fromRepository = false;

		if (getInPublicNamespace()) {
			publicArea = true;
		} else {
			publicArea = false;
		}

		String dsName = getRequest().getParameter(PortalConstants.DATASTRUCTURE_NAME);
		String repository = getRequest().getParameter(PortalConstants.REPOSITORY);

		if (repository != null && Long.valueOf(repository) >= 1) {
			fromRepository = true;
		}

		if (!publicArea) {
			String dsVersion = getRequest().getParameter(PortalConstants.VERSION);
			String dsId = getRequest().getParameter(PortalConstants.DATASTRUCTURE_ID);

			if (dsName != null) {
				if (dsVersion != null) {
					currentDataStructure = dictionaryManager.getDataStructure(dsName, dsVersion);
				} else {
					currentDataStructure = dictionaryManager.getDataStructureLatestVersion(dsName);
				}
			} else {
				// Convert String ID into a long
				Long dsIdLong = Long.valueOf(dsId);

				currentDataStructure = dictionaryManager.getDataStructure(dsIdLong);
			}
		} else {
			if (dsName != null) {
				RestDictionaryProvider dictionaryProvider =
						new RestDictionaryProvider(modulesConstants.getModulesDDTURL(), null);
				currentDataStructure = dictionaryProvider.getLatestDataStructureByName(dsName);
			}
		}

		if (!publicArea) {
			currentDataStructure.setCreatedBy(getDisplayNameByUsername(currentDataStructure.getCreatedBy()));
		} else {
			currentDataStructure.setCreatedBy(null);
		}

		// Permissions check
		// All users should be able to access PUBLISHED and AWAITING
		// PUBLICATION DS
		// To revert the system back to only being able to publicly see
		// PUBLISHED DSs, just remove this if block and
		// keep what is in the else block as the only permissions check.
		// Also, need to not check for permission via the webservice because then the session will be logged into the
		// first disease ID. Not good.
		if (publicArea || (queryArea != null && queryArea)) {
			if (StatusType.DRAFT.equals(currentDataStructure.getStatus())) {
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
			}
		} else {
			// User has permission to see this data element.
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider =
					new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));

			if (currentDataStructure == null) {
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);

			}
			PermissionType permission = restProvider
					.getAccess(getAccount().getId(), EntityType.DATA_STRUCTURE, currentDataStructure.getId())
					.getPermission();
			// Null permisson means user doesn't have read, write or owner any
			// type of permsisson
			if (permission == null) {
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
			}
			if (!(PermissionType.compare(permission, PermissionType.READ) >= 0)
					&& StatusType.DRAFT.equals(currentDataStructure.getStatus())) {
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
			}
		}

		FormStructure viewDataStructure = currentDataStructure;
		logger.info("view() viewDataStructure createdBy: " + viewDataStructure.getCreatedBy());
		// Store cast to DataStructure so we don't have to do it over and
		// over
		if (!publicArea) {
			Account account = webServiceManager.getEntityOwnerAccountRestful(getAccount(), currentDataStructure.getId(),
					EntityType.DATA_STRUCTURE);

			String ownerName = account.getUser().getFullName();
			getSession().setAttribute("ownerName", ownerName);
		}
		getSessionDataStructure().clear();
		getSessionDataStructure().setDataStructure(viewDataStructure);
		Set<MapElement> sessionList = viewDataStructure.getFormStructureSqlObject().getDataElements();
		getSessionDataElementList().setMapElements(sessionList);

		List<BasicEform> associatedBasicEforms =
				eformManager.basicEformSearch(null, null, viewDataStructure.getShortName(), null, null);
		if (associatedBasicEforms != null && !associatedBasicEforms.isEmpty()) {
			setHasAssociatedEforms(Boolean.TRUE);
		} else {
			setHasAssociatedEforms(Boolean.FALSE);
		}

	}

	/**
	 * If publicArea is true, then data is gathered for view with dictionaryProvider so brics can act as a proxy for
	 * dictionary on the public site.
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws UserPermissionException
	 */
	public String lightboxView() throws MalformedURLException, UnsupportedEncodingException, UserPermissionException, UserAccessDeniedException {

		String publicString = getRequest().getParameter(PortalConstants.PUBLIC_AREA);
		if (publicString != null) {
			publicArea = new Boolean(publicString);
		}

		String queryString = getRequest().getParameter(PortalConstants.QUERY_AREA);
		if (queryString != null) {
			queryArea = new Boolean(publicString);
		} else {
			queryArea = false;
		}

		view();

		return PortalConstants.ACTION_LIGHTBOX;
	}

	/**
	 * This function is "Create Draft Copy" workflow and is very poorly named. After creating the session required for
	 * the create draft copy, it should drop the user into the standard edit workflow.
	 * 
	 * @return
	 * @throws UserPermissionException
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public String changeCurrentStructure()
			throws UserPermissionException, MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		getSessionDataStructure().setDraftCopy(true);

		// Create the copy (latest version only) here and then go to edit.
		String shortName = getRequest().getParameter(PortalConstants.DATASTRUCTURE_NAME);

		// The permission check has been moved here from edit so that it can be
		// done before the id of the FS in session
		// is set to null.
		FormStructure origFormStructure = dictionaryManager.getDataStructureLatestVersion(shortName);

		if (origFormStructure != null) {
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider =
					new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			PermissionType permission =
					restProvider.getAccess(getAccount().getId(), EntityType.DATA_STRUCTURE, origFormStructure.getId())
							.getPermission();

			// only allow people with read access to the old FS to create a new draft copy
			if (!(PermissionType.compare(permission, PermissionType.READ) >= 0)) {
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
			}
		}
		currentDataStructure = dictionaryManager.formStructureCopy(getAccount(), origFormStructure, SeverityLevel.NEW);

		// Clear the name and reset the permissions
		currentDataStructure.setShortName(null);
		currentDataStructure.setCreatedBy(getDisplayNameByUsername(currentDataStructure.getCreatedBy()));
		currentDataStructure.setDateCreated(getCurrentDate());
		EntityMap em = new EntityMap(getAccount(), EntityType.DATA_STRUCTURE, null, PermissionType.OWNER);
		getSessionDataStructure().setEntityMapList(new ArrayList<EntityMap>());
		getSessionDataStructure().getEntityMapList().add(em);

		getSessionDataStructure().setDataStructure(currentDataStructure);

		// The groups need to be considered new. A value which is tracked in
		// session.
		getSessionDataStructure().setNewRepeatableGroups(
				getSessionDataStructure().getNewRepeatableGroups() - currentDataStructure.getRepeatableGroups().size());

		getSessionDataStructure().setNewMappedElements(
				getSessionDataStructure().getNewMappedElements() - currentDataStructure.getMapElements().size());

		return edit();
	}

	public String create() throws MalformedURLException, UnsupportedEncodingException {

		currentDataStructure = new FormStructure();
		currentDataStructure.setVersion(PortalConstants.VERSION_NEW);
		currentDataStructure.setStatus(StatusType.DRAFT);
		currentDataStructure.setCreatedBy(getAccount().getDisplayName());

		for (Disease disease : staticManager.getDiseaseList()) {
			if (PortalConstants.TBI.equals(disease.getName())) {
				Set<DiseaseStructure> newDiseaseSet = new HashSet<DiseaseStructure>();
				DiseaseStructure diseaseStructure =
						new DiseaseStructure(disease, currentDataStructure.getFormStructureSqlObject());
				newDiseaseSet.add(diseaseStructure);
				currentDataStructure.setDiseaseList(newDiseaseSet);
			}
		}

		getSessionDataStructure().clear();

		// Create the main repeatable Group
		RepeatableGroup rg = new RepeatableGroup();
		rg.setName(ModelConstants.MAIN_STRING);
		rg.setThreshold(1);
		rg.setPosition(0);
		rg.setType(RepeatableType.EXACTLY);
		rg.setDataStructure(currentDataStructure.getFormStructureSqlObject());

		// New groups that exist in session but not in the database get negative
		// ids. Each new group decrements the
		// counter on negative ids
		rg.setId(Long.valueOf(getSessionDataStructure().getNewRepeatableGroups()));
		getSessionDataStructure().setNewRepeatableGroups(getSessionDataStructure().getNewRepeatableGroups() - 1);
		Set<RepeatableGroup> groups = new LinkedHashSet<RepeatableGroup>();
		groups.add(rg);
		(currentDataStructure).setRepeatableGroups(groups);
		currentDataStructure.setModifiedDate(new Date());
		currentDataStructure.setModifiedUserId(getAccount().getUserId());

		getSessionDataStructure().setDataStructure(currentDataStructure);
		getSessionDataStructure().setNewStructure(true);

		// Creates a new owner entity for the current user and adds it to
		// session (it will be saved on submit).
		createNewOwnership();

		if (dataStructureForm == null) {
			dataStructureForm = new DataStructureForm(currentDataStructure, getOrgName());
		}

		return PortalConstants.ACTION_INPUT;
	}

	public String edit() throws UserPermissionException, MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		// clear FS permission session
		sessionDataStructure.clearPermission();

		currentDataStructure = getSessionDataStructure().getDataStructure();

		// plain edit requires write access. create draft copy requires read
		// access.
		// Draft Copy access used to be checked in an else statement here but
		// since you have already cleared the id of
		// the orgional FS it was moved to the changeCurrentStructure function.
		if (currentDataStructure != null && currentDataStructure.getId() != null
				&& !getSessionDataStructure().getDraftCopy()) {
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider =
					new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			PermissionType permission = restProvider
					.getAccess(getAccount().getId(), EntityType.DATA_STRUCTURE, currentDataStructure.getId())
					.getPermission();
			if (!(PermissionType.compare(permission, PermissionType.WRITE) >= 0)) {
				throw new UserPermissionException(ServiceConstants.WRITE_ACCESS_DENIED);
			}
		}

		dataStructureForm = new DataStructureForm(currentDataStructure, getOrgName());

		getSessionDataStructure().setDataStructure(currentDataStructure);

		if (getRequest().getParameter(PortalConstants.REDIRECT) != null) {
			return getRequest().getParameter(PortalConstants.REDIRECT);
		} else {
			return PortalConstants.ACTION_INPUT;
		}
	}

	/**
	 * if we are versioning this form structure, then we will need to update multiple tables that references the form
	 * structure to reference the latest form structure version instead. This method calls the repository webservice in
	 * order to update references
	 * 
	 * @param oldId
	 * @param newId
	 * @throws DictionaryVersioningException
	 */
	private void updateFormStructureReferences(UpdateFormStructureReferencePayload payload)
			throws DictionaryVersioningException {
		boolean success;

		try {
			success = webServiceManager.updateFormStructureReferences(getAccount(), payload);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DictionaryVersioningException(
					"An error occurred while trying to update the form structure references for versioning.");
		}

		if (!success) {
			throw new DictionaryVersioningException(
					"An error occurred while trying to update the form structure references for versioning.");
		}
		// String repositoryUrl = modulesConstants.getModulesSTURL(getDiseaseId());
		// RestRepositoryProvider restRepositoryProvider =
		// new RestRepositoryProvider(repositoryUrl, PortalUtils.getProxyTicket(repositoryUrl));
		//
		// try {
		// // PORTAL_URL/dataset/update_form_structure_references
		// restRepositoryProvider.updateFormStructureReferences(payload);
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// } catch (JAXBException e) {
		// e.printStackTrace();
		// }
	}

	private Map<String, Long> buildRepeatableGroupIdMap(FormStructure fs) {
		// map of repeatable group names to its id
		Map<String, Long> repeatableGroupIdMap = new HashMap<String, Long>();

		for (RepeatableGroup currentGroup : fs.getRepeatableGroups()) {
			repeatableGroupIdMap.put(currentGroup.getName(), currentGroup.getId());
		}

		return repeatableGroupIdMap;
	}

	public String submit() throws HttpException, IOException, DictionaryVersioningException, UserAccessDeniedException {

		// copies changes to session variabled
		saveChangesToSession();

		// assigns values from the form to a data structure object
		dataStructureForm.adapt(currentDataStructure, getEnforceStaticFields());

		// List to hold any errors or warnings
		errors = new ArrayList<String>();
		List<String> warnings = new ArrayList<String>();

		currentDataStructure.setCreatedBy(getDisplayNameByUsername(currentDataStructure.getCreatedBy()));

		// Call WS
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
		PermissionType permission =
				restProvider.getAccess(getAccount().getId(), EntityType.DATA_STRUCTURE, currentDataStructure.getId())
						.getPermission();

		// before the save we need to know if this form structure is new form
		// structure or if a user is editing a current data structure
		// we need to determine this for entity map permissions below
		boolean isNewFormStructure = false;

		Long oldId = null;
		Map<String, Long> oldRepeatableGroupIdMap = null;


		if (currentDataStructure.getId() != null) {
			isNewFormStructure = true;
			oldId = currentDataStructure.getId();
			oldRepeatableGroupIdMap = buildRepeatableGroupIdMap(currentDataStructure);

			logger.info("New form structure has been saved...no need to update form structure references...");
		}

		// save function
		String proxyTicket = PortalUtils.getProxyTicket(modulesConstants.getModulesAccountURL(getDiseaseId()));
		currentDataStructure = dictionaryManager.saveDataStructure(getAccount(), permission, currentDataStructure,
				errors, warnings, getFormStructureChangeSeverity(), proxyTicket);

		// if we are versioning this form structure, then we will need to update multiple tables that references the
		// form structure to reference the latest form structure version instead.
		if (oldId != null && !getSessionDataStructure().getDraftCopy()) {
			logger.info(
					"Form structure has been versioned...need to update form structure references in repository...");

			Map<String, Long> newRepeatableGroupIdMap = buildRepeatableGroupIdMap(currentDataStructure);

			Long newId = currentDataStructure.getId();

			UpdateFormStructureReferencePayload payload = new UpdateFormStructureReferencePayload();
			payload.setOldFormStructureId(oldId);
			payload.setNewFormStructureId(newId);

			// this has to be explicitly defined as hashmap so the jaxb adapter would work
			HashMap<Long, Long> oldNewRepeatableGroupIdMap = new HashMap<Long, Long>();

			for (Entry<String, Long> oldRepeatableGroupIdEntry : oldRepeatableGroupIdMap.entrySet()) {
				String repeatableGroupName = oldRepeatableGroupIdEntry.getKey();
				Long oldRepeatableGroupId = oldRepeatableGroupIdEntry.getValue();
				Long newRepeatableGroupId = newRepeatableGroupIdMap.get(repeatableGroupName);

				if (newRepeatableGroupId == null) {
					throw new DictionaryVersioningException("Error occurred while versioning a form structure");
				}

				oldNewRepeatableGroupIdMap.put(oldRepeatableGroupId, newRepeatableGroupId);
			}

			payload.setOldNewRepeatableGroupIdMap(oldNewRepeatableGroupIdMap);

			updateFormStructureReferences(payload);
		}



		// todo: this saveDataStructure method removes the dataElements from the
		// currentDataSTructure, we need to change
		// this so it keeps it.

		// If we versioned a form structure (severity is new or minor), then we
		// need to register it as published.
		if (StatusType.PUBLISHED.equals(currentDataStructure.getStatus())
				&& (SeverityLevel.MAJOR.equals(getFormStructureChangeSeverity())
						|| SeverityLevel.MINOR.equals(getFormStructureChangeSeverity()))) {
			addToPublicGroups(currentDataStructure);
		}

		// save audit note (if there is one)
		if (auditNote != null && !auditNote.isEmpty()) {

			List<SeverityRecord> severityRecords = getSessionDataStructure().getSeverityRecords();
			SeverityLevel highestSeverityLevel = getSessionDataStructure().getfSChangeSeverity();


			DictionaryEventLog eventLog = new DictionaryEventLog(getDiseaseId(), getAccount().getUserId());
			Long originalEntityId =
					dictionaryService.getOriginalFormStructureIdByName(currentDataStructure.getShortName());
			eventLog.setFormStructureID(originalEntityId);

			dictionaryService.saveChangeHistoryEventlog(eventLog, severityRecords, highestSeverityLevel,
					EntityType.DATA_STRUCTURE, oldId, currentDataStructure.getId(), auditNote);

		}

		getSessionDataStructure().setDataStructure(currentDataStructure);

		if (errors.size() > 0) {
			for (String error : errors) {
				this.addActionError(error);
				logger.error(error);
			}

			return PortalConstants.ACTION_INPUT;
		} else {
			boolean groupInRemovalList = false;
			boolean userInRemovalList = false;
			boolean userInMemberGroupList = false;

			EntityMap removeEntityMap = null;

			// This block updates the current user's permission
			{

				// This code sets up the next block for success without being
				// invasive so close to a release.
				// CASE: severity change == new: We don't want to copy the
				// permissions because that would result in a
				// DRAFT FS that is in the public group. Instead we will build
				// our own ownership.
				if (SeverityLevel.NEW.equals(getFormStructureChangeSeverity())) {
					createNewOwnership();
				}

				// If a user edits a FS and it does not result in the change in version IE permissions change and the
				// user does not
				// visit the permissions page, the entity map is not loaded. We need to load the map for all the checks
				// below
				// or else two owners are assigned to the form structure. once b/c the system thinks this is a new FS
				// and creates
				// a new permission b/c the map is null and a second time when it does a get for permissions later.
				// this could be done in the statement below... but it's already too big. a permissions s would be a
				// better option
				if (getFormStructureChangeSeverity() == null && sessionDataStructure.getEntityMapList() == null) {
					loadEntityMap();
				}

				// At this point the data structure has already been saved. If
				// the data structure is a current draft version
				// the hasFormStrutureID will be flagged as true. We need this
				// boolean to tell if the entity mape needs to be loaded
				// if the entity map has not already been loaded and the FS is
				// not new, load the entity map into memory.
				// NOTE: We only need to do this for awaiting publication, draft
				// and shared draft. copying permission is taken take of else
				// where
				// when dealing with published FS
				if (isNewFormStructure && sessionDataStructure.getEntityMapList() == null
						&& (sessionDataStructure.getDataStructure().getStatus() == StatusType.DRAFT
								|| sessionDataStructure.getDataStructure()
										.getStatus() == StatusType.AWAITING_PUBLICATION
								|| sessionDataStructure.getDataStructure().getStatus() == StatusType.SHARED_DRAFT)) {
					// this method loads the entity map from the data structure
					// id in session
					loadEntityMap();
				}

				// copying permission for published FS
				if (isNewFormStructure && sessionDataStructure.getEntityMapList() == null
						&& sessionDataStructure.getDataStructure().getStatus() == StatusType.PUBLISHED) {
					loadEntityMapForPublishedFS(oldId);
				}

				// CASE: entityMapList == null. User did not visit perm page
				// before saving.
				//
				// Create Draft Copy: entityMapList should never be null. Owner
				// is set in action with
				// createNewOwnership()
				//
				// Major/Minor Change: New FS (same shortname, new version)
				// added to public groups earlier in action.
				// Just needs an owner.
				// TODO: In the future we may want this to copy all the
				// permissions not just set the owner but it
				// shouldn't matter because this FS is already published and
				// ownership no longer matters when published.
				//
				// New Change: We don't want this in the public groups and they
				// didn't manually set any owners so just
				// default current user to owner.
				if (sessionDataStructure.getEntityMapList() == null) {
					createNewOwnership();
				}

				if (sessionDataStructure.getEntityMapList() != null) {
					// Find the user in the list
					for (EntityMap em : sessionDataStructure.getEntityMapList()) {
						// Update the entity Id of each entity map [needed
						// for when new users are added during
						// creation]
						em.setEntityId(currentDataStructure.getId());

						if (getAccount().equals(em.getAccount())) {
							removeEntityMap = em;
							// break; -- Can't break here because we need to
							// update all the EntityIds!
						}
						if (em.getPermissionGroup() != null) {
							restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
							// This has been separated as it is a ws call we
							// want to avoid unless necessary
							if (restProvider.isGroupMember(em.getPermissionGroup().getGroupName(),
									getAccount().getId())) {
								userInMemberGroupList = true; // We have
																 // determined
																 // that the
																 // permission
																 // group is
																 // in
																 // the list
							}
						}

						// Based on a review of the logic in the entity register
						// ws call this does not appear to be
						// needed
						// it also creates issues with initial creation of a
						// structure in regards to permissions and
						// unique ownership

						// Remove the user
						// sessionDataStructure.getEntityMapList().remove(removeEntityMap);

						// Update the [already stored] user map with the changes
						// from the UI
						// oldEntityMap.setPermission(removeEntityMap.getPermission());

						// Store it back into the list
						// sessionDataStructure.getEntityMapList().add(oldEntityMap);

					}
				}

				for (EntityMap em : sessionDataStructure.getRemovedMapList()) {
					if (em.getPermissionGroup() != null) {
						restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));

						// This has been separated as it is a ws call we want to
						// avoid unless necessary
						if (restProvider.isGroupMember(em.getPermissionGroup().getGroupName(), getAccount().getId())) {
							groupInRemovalList = true; // We have determined
														 // that the permission
														 // group is in the list
						}
					}
					if (getAccount().equals(em.getAccount())) {
						userInRemovalList = true; // This is for cases where the
													 // user has removed
													 // themselves.
					}
					em.setEntityId(currentDataStructure.getId());
				}

				if (sessionDataStructure.getRemovedMapList() != null
						&& !sessionDataStructure.getRemovedMapList().isEmpty()) {
					webServiceManager.unregisterEntityListRestful(getAccount(),
							sessionDataStructure.getRemovedMapList());
				}
				if (sessionDataStructure.getEntityMapList() != null
						&& !sessionDataStructure.getEntityMapList().isEmpty()) {
					// ticket array = portal get multiple proxy tickets, one for
					// each modules account get disease id
					String[] proxyTicketArr =
							PortalUtils.getMultipleProxyTickets(modulesConstants.getModulesAccountURL(getDiseaseId()),
									sessionDataStructure.getEntityMapList().size());
					webServiceManager.registerEntityListRestful(getAccount(), sessionDataStructure.getEntityMapList(),
							proxyTicketArr);
				}
			}

			if (StatusType.DRAFT.equals(currentDataStructure.getStatus())) {
				removeFromPublicGroups(currentDataStructure);
			}

			getSessionDataStructure().clear();
			readOnly = true;

			FormStructure viewDataStructure = dictionaryManager.getDataStructure(currentDataStructure.getShortName(),
					currentDataStructure.getVersion());

			getSessionDataStructure().setDataStructure(viewDataStructure);
			// set currentDataStructure
			currentDataStructure = viewDataStructure;

			Set<MapElement> sessionList = viewDataStructure.getFormStructureSqlObject().getDataElements();
			getSessionDataElementList().setMapElements(sessionList);

			if (groupInRemovalList == true && (removeEntityMap == null)) {
				return PortalConstants.ACTION_REDIRECT; // This will cover
														 // situations were the
														 // user is in the
														 // accounts
														 // group
														 // and not in the main
														 // user group as an
														 // individual account.
			}
			if (userInRemovalList == true && (userInMemberGroupList == false)) {
				return PortalConstants.ACTION_REDIRECT; // This handles
														 // situations where the
														 // user is not in a
														 // member
														 // list
														 // but has removed
														 // themselves.
			}

			return PortalConstants.ACTION_VIEW;
		}
	}

	/**
	 * Cancel redirect
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws UserPermissionException
	 */
	public String cancel() throws MalformedURLException, UnsupportedEncodingException, UserPermissionException, UserAccessDeniedException {

		String dsId = getRequest().getParameter(PortalConstants.DATASTRUCTURE_ID);
		logger.info(dsId);

		if (dsId == null || dsId.equals("")) {

			saveChangesToSession();

			dataStructureForm.adapt(currentDataStructure, getEnforceStaticFields());

			if (currentDataStructure.getId() == null) {
				return PortalConstants.REDIRECT;
			}

			getSessionDataStructure().clear();

			readOnly = true;

			FormStructure viewDataStructure = dictionaryManager.getDataStructure(currentDataStructure.getId());

			currentDataStructure = viewDataStructure;

			getSessionDataStructure().setDataStructure(viewDataStructure);

			Set<MapElement> sessionList =
					new HashSet<MapElement>(viewDataStructure.getFormStructureSqlObject().getDataElements());
			getSessionDataElementList().setMapElements(sessionList);

			return PortalConstants.ACTION_VIEW;
		}

		return view();
	}

	public String delete() throws UserPermissionException, MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		currentDataStructure = getSessionDataStructure().getDataStructure();

		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
		PermissionType permission =
				restProvider.getAccess(getAccount().getId(), EntityType.DATA_STRUCTURE, currentDataStructure.getId())
						.getPermission();

		String dsId = getRequest().getParameter(PortalConstants.DATASTRUCTURE_ID);

		try {
			currentDataStructure = dictionaryManager.getDataStructure(Long.valueOf(dsId));
		} catch (NumberFormatException e) {
			throw new NumberFormatException("String dsId cannot be empty");
		}

		List<String> errors = new ArrayList<String>();

		if (!(PermissionType.compare(permission, PermissionType.WRITE) >= 0)) {
			throw new UserPermissionException(ServiceConstants.WRITE_ACCESS_DENIED);
		}

		String proxyTicket = PortalUtils.getProxyTicket(modulesConstants.getModulesAccountURL(getDiseaseId()));
		this.dictionaryManager.deleteDataStructure(getAccount(), currentDataStructure, proxyTicket);

		if (errors.size() > 0) {
			return PortalConstants.ACTION_INPUT;
		}

		getSessionDataStructure().clear();
		return PortalConstants.ACTION_REDIRECT;
	}

	/**
	 * Changes the publication state of the current data structure and performs any necessary steps.
	 * 
	 * Flow is Draft > Awaiting Publication > Published > Archived
	 * 
	 * @return
	 * @throws Exception
	 */
	public String publication() throws Exception {

		StatusType newStatus;
		StatusType oldStatus;
		EventType eventType = null;

		newStatus = StatusType.statusOf(statusId);

		String dsId = getRequest().getParameter(PortalConstants.DATASTRUCTURE_ID);

		try {
			currentDataStructure = dictionaryManager.getDataStructure(Long.valueOf(dsId));
			oldStatus = currentDataStructure.getStatus();
		} catch (NumberFormatException e) {
			throw new NumberFormatException("String dsId cannot be empty");
		}

		if (!oldStatus.equals(newStatus)) {
			// Use the account web service to find out what access the current
			// user has to the current data structure.
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider =
					new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			PermissionType permission = restProvider
					.getAccess(getAccount().getId(), EntityType.DATA_STRUCTURE, currentDataStructure.getId())
					.getPermission();

			// if the DE is published and cancel is sent
			// cancel the publication

			if ((oldStatus.equals(StatusType.PUBLISHED) && newStatus.equals(StatusType.DRAFT))
					|| (oldStatus.equals(StatusType.PUBLISHED) && newStatus.equals(StatusType.AWAITING_PUBLICATION))) {
				return PortalConstants.ACTION_PUBLICATION;
			}

			// If we are publishing for the first time, we need to create the
			// tables and metadata describing them for
			// submission

			if (dictionaryManager.hasRole(getAccount(), RoleType.ROLE_DICTIONARY_ADMIN)
					&& newStatus == StatusType.PUBLISHED) {

				// Add the data structure to public groups and
				addToPublicGroups(currentDataStructure);
				// If we are unarchiving we don't want to create tables again,
				// just flip the flag

				// need to refresh the object so we don't load unnecessary de supportingDocumentation here
				currentDataStructure = dictionaryManager.getDataStructure(Long.valueOf(dsId));

				if (!oldStatus.equals(StatusType.ARCHIVED)) {
					currentDataStructure.setPublicationDate(new Date());
					webServiceManager.createTableFromDataStructure(getAccount(),
							currentDataStructure.getFormStructureSqlObject());

					eventType = EventType.STATUS_CHANGE_TO_PUBLISHED;
				} else {
					eventType = EventType.STATUS_CHANGE_UNARCHIVED;
				}

			}
			if ((oldStatus.equals(StatusType.DRAFT) && newStatus.equals(StatusType.SHARED_DRAFT))) {
				webServiceManager.registerEntityToPermissionGroup(getAccount(),
						ServiceConstants.PUBLISHED_FORM_STRUCTURES, EntityType.DATA_STRUCTURE,
						currentDataStructure.getId(), PermissionType.READ);

				eventType = EventType.STATUS_CHANGE_SHARED_DRAFT;

			}

			if ((oldStatus.equals(StatusType.SHARED_DRAFT) && newStatus.equals(StatusType.DRAFT))) {
				removeFromPublicGroups(currentDataStructure);

				eventType = EventType.STATUS_CHANGE_REVERT_DRAFT;
			}

			if ((oldStatus.equals(StatusType.AWAITING_PUBLICATION) && newStatus.equals(StatusType.DRAFT))) {
				removeFromPublicGroups(currentDataStructure);
			}

			// if we are setting a draft form structure to awaiting publication, add the form structure to the public
			// group
			if (StatusType.DRAFT.equals(oldStatus) && StatusType.AWAITING_PUBLICATION.equals(newStatus)) {
				webServiceManager.registerEntityToPermissionGroup(getAccount(),
						ServiceConstants.PUBLISHED_FORM_STRUCTURES, EntityType.DATA_STRUCTURE,
						currentDataStructure.getId(), PermissionType.READ);
			}


			// get documentation for eventlog
			Set<DictionarySupportingDocumentation> currentDocSet =
					getSessionDictionaryStatusChange().getfSEventLogDocumentation();

			// get comment and supporting documentation
			// this needs to be done before data element status is updated to make sure the supporting document is also
			// saved if there is a status change
			// repositoryManager.saveUserFile takes time and if a user refreshes in the middle, the supporing doc might
			// not be saved
			String comment = getSessionDictionaryStatusChange().getStatusReason();


			DictionaryEventLog eventLog = new DictionaryEventLog(getDiseaseId(), getAccount().getUserId());
			Long originalEntityId =
					dictionaryService.getOriginalFormStructureIdByName(currentDataStructure.getShortName());
			eventLog.setFormStructureID(originalEntityId);

			if (currentDocSet != null && !currentDocSet.isEmpty()) {
				for (DictionarySupportingDocumentation sd : currentDocSet) {
					if (sd.getUserFile() != null)
						sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));

					sd.setDictionaryEventLog(eventLog);
				}
			}
			eventLog.setSupportingDocumentationSet(currentDocSet);

			currentDataStructure = dictionaryManager.editDataStructureStatus(getAccount(), permission,
					currentDataStructure, newStatus);

			if (dictionaryManager.hasRole(getAccount(), RoleType.ROLE_DICTIONARY_ADMIN)
					&& newStatus == StatusType.ARCHIVED) {
				// Call webservice
				webServiceManager.setDataStoreInfoArchive(getAccount(), currentDataStructure, true);

				eventType = EventType.STATUS_CHANGE_TO_ARCHIVE;

			}

			// saves the status change made on the form structure
			// if eventType is null it means we don't want to record the status change being made
			if (eventType != null) {
				dictionaryManager.saveEventLog(eventLog, oldStatus.getId().toString(), newStatus.getId().toString(),
						comment, eventType);
			}


			getSessionDataStructure().setDataStructure(currentDataStructure);
		}

		getSessionDictionaryStatusChange().clear();

		return PortalConstants.ACTION_PUBLICATION;
	}

	/**
	 * If there was a dataStructureForm submitted, copy the changes over to the session variables
	 */
	public void saveChangesToSession() {

		currentDataStructure = getSessionDataStructure().getDataStructure();

		if (dataStructureForm != null) {
			dataStructureForm.adapt(currentDataStructure, getEnforceStaticFields());
		} else {
			dataStructureForm = new DataStructureForm(currentDataStructure, getOrgName());
		}

		getSessionDataStructure().setDataStructure(currentDataStructure);
	}

	/**
	 * save submitted changes to session and load form for input
	 * 
	 * @return
	 */
	public String moveToDetails() {

		saveChangesToSession();

		return ACTION_DETAILS;
	}

	public String moveToDocumentations() {

		saveChangesToSession();

		return ACTION_DOCUMENTATIONS;
	}

	/**
	 * save submitted changes and load information about elements
	 * 
	 * @return
	 */
	public String moveToElements() {

		saveChangesToSession();

		return ACTION_ELEMENTS;
	}

	/**
	 * save submitted changes and load permission information
	 * 
	 * @return
	 */
	public String moveToPermissions() {

		saveChangesToSession();

		return ACTION_PERMISSIONS;
	}

	/**
	 * save submitted changes and load review information
	 * 
	 * @return
	 */
	public String moveToReview() {

		// clear severity record in session to make sure we have the right
		// changes
		getSessionDataStructure().clearSeverityRecords();

		// We will mark this true later if there is a problem with the form
		// structure's short name
		shortNameChangeRequired = false;

		saveChangesToSession();

		FormStructure currentFS = this.getCurrentDataStructure();

		if (currentFS != null && currentFS.getId() != null) {
			// This creates an error if the short name is changed.
			// FormStructure originalFSByName =
			// dictionaryManager.getDataStructure(currentFS.getShortName(),
			// currentFS.getVersion());
			FormStructure originalFS = dictionaryManager.getDataStructure(currentFS.getId());

			if (currentFS != null && originalFS != null) {
				// get the list of changes from the rules engine
				List<SeverityRecord> severityRecords =
						dictionaryService.evaluateFormStructureChangeSeverity(originalFS, currentFS);
				changeStrings = new ArrayList<String>();

				getSessionDataStructure().setSeverityRecords(severityRecords);
				getSessionDataStructure().setOriginalFSId(originalFS.getId());

				// for every severity record, see if its the highest and convert
				// it into a string.
				if (severityRecords != null && !severityRecords.isEmpty()) {
					SeverityLevel highestSeverityLevel = null;
					for (SeverityRecord severityRecord : severityRecords) {
						if (highestSeverityLevel == null
								|| severityRecord.getSeverityLevel().getId() > highestSeverityLevel.getId()) {
							highestSeverityLevel = severityRecord.getSeverityLevel();
						}

						// get a string version of this change
						changeStrings.add(RulesEngineUtils.generateSeverityRecordString(severityRecord));
					}

					// The short names have to be different if the change type
					// is new
					if (SeverityLevel.NEW.equals(highestSeverityLevel)
							&& currentFS.getShortName().equals(originalFS.getShortName())) {
						shortNameChangeRequired = true;
					}

					formStructureChangeSeverity = highestSeverityLevel;
				}
			}

			// getSessionDataStructure().setDataStructure(currentFS);

			/*
			 * Set<MapElement> sessionList = new HashSet<MapElement>(viewDataStructure.getFormStructureSqlObject()
			 * .getDataElements());
			 */

			Set<MapElement> sessionList = currentFS.getFormStructureSqlObject().getDataElements();
			getSessionDataElementList().setMapElements(sessionList);

			getSessionDataStructure().setfSChangeSeverity(formStructureChangeSeverity);

		}

		return PortalConstants.ACTION_REVIEW;
	}

	/**
	 * save the data structure
	 * 
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws DictionaryVersioningException
	 */
	public String saveAndFinish() throws HttpException, IOException, DictionaryVersioningException, UserAccessDeniedException {

		return submit();
	}

	/**
	 * Approves the publication of the Data Structure
	 * 
	 * @throws MessagingException
	 * @throws UserPermissionException
	 * @throws SQLException
	 * @throws IOException
	 * @throws HttpException
	 */
	public String approve()
			throws MessagingException, UserPermissionException, SQLException, HttpException, IOException, UserAccessDeniedException {

		boolean success = false;

		// can't use the object from session, trust me, there are hibernate saving issues with it if you have a support
		// documentation attached to any data elements
		currentDataStructure = getSessionDataStructure().getDataStructure();

		// Use the account web service to find out what access the current user
		// has to the current data structure. Used
		// for access check in service layer

		Account currentDataStructureAccount = webServiceManager.getEntityOwnerAccountRestful(getAccount(),
				currentDataStructure.getId(), EntityType.DATA_STRUCTURE);
		String ownerName = currentDataStructureAccount.getUser().getFullName();

		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
		PermissionType permission =
				restProvider.getAccess(getAccount().getId(), EntityType.DATA_STRUCTURE, currentDataStructure.getId())
						.getPermission();

		// Checks the form structure via database to confirm that it is awaiting
		// publication to avoid concurrent
		// modification of data element status
		if (StatusType.AWAITING_PUBLICATION
				.equals(dictionaryManager.getDataStructure(currentDataStructure.getId()).getStatus())) {

			try {
				addToPublicGroups(currentDataStructure);

				currentDataStructure = dictionaryManager.getDataStructure(currentDataStructure.getId());
				currentDataStructure.setPublicationDate(new Date());

				// Call webservice
				success = webServiceManager.createTableFromDataStructure(getAccount(),
						currentDataStructure.getFormStructureSqlObject());

				currentDataStructure = dictionaryManager.editDataStructureStatus(getAccount(), permission,
						currentDataStructure, StatusType.PUBLISHED);

				// save the status change made on the form structure
				String comment = getSessionDictionaryStatusChange().getStatusReason();

				DictionaryEventLog eventLog = new DictionaryEventLog(getDiseaseId(), getAccount().getUserId());
				Long originalEntityId =
						dictionaryService.getOriginalFormStructureIdByName(currentDataStructure.getShortName());
				eventLog.setFormStructureID(originalEntityId);

				// get documentation for eventlog
				Set<DictionarySupportingDocumentation> currentDocSet =
						getSessionDictionaryStatusChange().getfSEventLogDocumentation();

				if (currentDocSet != null && !currentDocSet.isEmpty()) {
					for (DictionarySupportingDocumentation sd : currentDocSet) {
						if (sd.getUserFile() != null)
							sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));

						sd.setDictionaryEventLog(eventLog);
					}
				}
				eventLog.setSupportingDocumentationSet(currentDocSet);

				dictionaryManager.saveEventLog(eventLog, StatusType.AWAITING_PUBLICATION.getId().toString(),
						StatusType.PUBLISHED.getId().toString(), comment, EventType.REQUESTED_PUBLISH_APPROVED);

			} catch (MalformedURLException e) {
				success = false;
				e.printStackTrace();
			} catch (IOException e) {

				// If publication of associated data elements fails, revert
				// structure back to 'awaiting publication' status
				currentDataStructure.setPublicationDate(null);
				currentDataStructure = dictionaryManager.editDataStructureStatus(getAccount(), permission,
						currentDataStructure, StatusType.AWAITING_PUBLICATION);

				success = false;

				logger.error(e);

				throw e;
			} catch (Exception e) {
				success = false;
				e.printStackTrace();
			}

			reason = getSessionDictionaryStatusChange().getApproveReason();
			if (success) {

				boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();

				// construct subject line and message text to send through email
				if (!isLocalEnv) {
					String subject = this.getText(
							PortalConstants.MAIL_RESOURCE_ACCEPTED_DATASTRUCTURE
									+ PortalConstants.MAIL_RESOURCE_SUBJECT,
							Arrays.asList(currentDataStructure.getReadableName()));

					String messageText =
							this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgPhone(getDiseaseId()), ownerName, reason,
											currentDataStructure.getReadableName()))
									+ this.getText(
											PortalConstants.MAIL_RESOURCE_ACCEPTED_DATASTRUCTURE
													+ PortalConstants.MAIL_RESOURCE_BODY,
											Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
													modulesConstants.getModulesOrgPhone(getDiseaseId()), ownerName,
													reason, currentDataStructure.getReadableName()))
									+ this.getText(
											PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
											Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
													modulesConstants.getModulesOrgPhone(getDiseaseId()), ownerName,
													reason, currentDataStructure.getReadableName()));

					String from = modulesConstants.getModulesOrgEmail(getDiseaseId());

					dictionaryManager.sendEmail(currentDataStructureAccount, subject, messageText, from);
				} else {
					logger.info("[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the approval of FS: "
							+ currentDataStructure.getReadableName());
				}
			}

			readOnly = true;
		}

		return PortalConstants.ACTION_REDIRECT_TO_VIEW;
	}

	/**
	 * Converts the Form Structure to a Shared Draft
	 * 
	 * @throws MessagingException
	 * @throws UserPermissionException
	 * @throws SQLException
	 * @throws IOException
	 * @throws HttpException
	 */
	public String createGroupDraft()
			throws MessagingException, UserPermissionException, SQLException, HttpException, IOException, UserAccessDeniedException {

		currentDataStructure = getSessionDataStructure().getDataStructure();

		// Use the account web service to find out what access the current user
		// has to the current data structure. Used
		// for access check in service layer

		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
		PermissionType permission =
				restProvider.getAccess(getAccount().getId(), EntityType.DATA_STRUCTURE, currentDataStructure.getId())
						.getPermission();

		// Checks the form structure via database to confirm that it is awaiting
		// publication to avoid concurrent
		// modification of data element status
		if (StatusType.SHARED_DRAFT
				.equals(dictionaryManager.getDataStructure(currentDataStructure.getId()).getStatus())) {

			currentDataStructure.setPublicationDate(new Date());
			dictionaryManager.editDataStructureStatus(getAccount(), permission, currentDataStructure,
					StatusType.SHARED_DRAFT);

			webServiceManager.registerEntityToPermissionGroup(getAccount(), ServiceConstants.PUBLISHED_FORM_STRUCTURES,
					EntityType.DATA_STRUCTURE, currentDataStructure.getId(), PermissionType.READ);

			readOnly = true;
		}

		return PortalConstants.ACTION_VIEW;
	}

	/**
	 * Denies the publication of the Data Structure
	 * 
	 * @throws MessagingException
	 * @throws UserPermissionException
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public String deny()
			throws MessagingException, UserPermissionException, MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		currentDataStructure = getSessionDataStructure().getDataStructure();

		// Use the account web service to find out what access the current user
		// has to the current data structure. Used
		// for access check in service layer

		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
		PermissionType permission =
				restProvider.getAccess(getAccount().getId(), EntityType.DATA_STRUCTURE, currentDataStructure.getId())
						.getPermission();

		Account currentDataStructureAccount = webServiceManager.getEntityOwnerAccountRestful(getAccount(),
				currentDataStructure.getId(), EntityType.DATA_STRUCTURE);
		String ownerName = currentDataStructureAccount.getUser().getFullName();

		currentDataStructure = dictionaryManager.editDataStructureStatus(getAccount(), permission, currentDataStructure,
				StatusType.DRAFT);
		removeFromPublicGroups(currentDataStructure);

		// save the status change made on the form structure
		String comment = getSessionDictionaryStatusChange().getStatusReason();

		DictionaryEventLog eventLog = new DictionaryEventLog(getDiseaseId(), getAccount().getUserId());
		Long originalEntityId = dictionaryService.getOriginalFormStructureIdByName(currentDataStructure.getShortName());
		eventLog.setFormStructureID(originalEntityId);

		// get documentation for eventlog
		Set<DictionarySupportingDocumentation> currentDocSet =
				getSessionDictionaryStatusChange().getfSEventLogDocumentation();

		if (currentDocSet != null && !currentDocSet.isEmpty()) {
			for (DictionarySupportingDocumentation sd : currentDocSet) {
				if (sd.getUserFile() != null)
					sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));

				sd.setDictionaryEventLog(eventLog);
			}
		}
		eventLog.setSupportingDocumentationSet(currentDocSet);

		dictionaryManager.saveEventLog(eventLog, StatusType.AWAITING_PUBLICATION.getId().toString(),
				StatusType.DRAFT.getId().toString(), comment, EventType.REQUESTED_PUBLISH_REJECTED);

		reason = getSessionDictionaryStatusChange().getApproveReason();
		// construct subject line and message text to send through email

		boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();

		if (!isLocalEnv) {
			String subject = this.getText(
					PortalConstants.MAIL_RESOURCE_REJECTED_DATASTRUCTURE + PortalConstants.MAIL_RESOURCE_SUBJECT,
					Arrays.asList(currentDataStructure.getReadableName()));

			String messageText =
					this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
									modulesConstants.getModulesOrgPhone(getDiseaseId()), ownerName, reason,
									currentDataStructure.getReadableName()))
							+ this.getText(
									PortalConstants.MAIL_RESOURCE_REJECTED_DATASTRUCTURE
											+ PortalConstants.MAIL_RESOURCE_BODY,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgPhone(getDiseaseId()), ownerName, reason,
											currentDataStructure.getReadableName()))
							+ this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgPhone(getDiseaseId()), ownerName, reason,
											currentDataStructure.getReadableName()));

			String from = modulesConstants.getModulesOrgEmail(getDiseaseId());

			dictionaryManager.sendEmail(currentDataStructureAccount, subject, messageText, from);
		} else {
			logger.info("[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the denial of FS: "
					+ currentDataStructure.getReadableName());
		}

		// The currentDataStructure has changed. The new one needs to be saved
		// to session.
		getSessionDataStructure().setDataStructure(currentDataStructure);

		return PortalConstants.ACTION_REDIRECT_TO_VIEW;
	}

	/**
	 * Adds the datastructure to public groups of all diseases
	 * 
	 * @param dataStructure
	 * @throws IOException
	 * @throws HttpException
	 */
	public void addToPublicGroups(FormStructure dataStructure) throws HttpException, IOException, UserAccessDeniedException {

		// if the map element attached is not published, publish it and add it
		// to the public permission groups
		for (RepeatableGroup rg : dataStructure.getRepeatableGroups()) {
			for (MapElement me : rg.getMapElements()) {
				StructuralDataElement structuralDE = me.getStructuralDataElement();
				if (DataElementStatus.DRAFT == structuralDE.getStatus()
						|| DataElementStatus.AWAITING == structuralDE.getStatus()) {
					String[] threeProxyTickets = PortalUtils
							.getMultipleProxyTickets(modulesConstants.getModulesAccountURL(getDiseaseId()), 3);

					DataElement dataElement = dictionaryManager.getDataElement(structuralDE.getId());

					DataElementStatus oldStatus = dataElement.getStatus();
					dataElement = dictionaryManager.editDataElementStatus(getAccount(), dataElement,
							DataElementStatus.PUBLISHED, threeProxyTickets);
					webServiceManager.registerEntityToPermissionGroup(getAccount(),
							ServiceConstants.PUBLIC_DATA_ELEMENTS, EntityType.DATA_ELEMENT, dataElement.getId(),
							PermissionType.READ);
					me.setStructuralDataElement(dataElement.getStructuralObject()); // This prevents a FS save
																					 // in the
																					 // same action from
																					 // overwriting
																					 // structural
																					 // publication.

					// save the status change made on dataelments due to status change in form structure
					String comment = getSessionDictionaryStatusChange().getAttachedDEReason();

					DictionaryEventLog eventLog = new DictionaryEventLog(getDiseaseId(), getAccount().getUserId());
					Long originalEntityId = dictionaryService.getOriginalDataElementIdByName(dataElement.getName());
					eventLog.setDataElementID(originalEntityId);

					// get documentation for eventlog
					Set<DictionarySupportingDocumentation> currentDocSet =
							getSessionDictionaryStatusChange().getDEEventLogDocumentation();

					if (currentDocSet != null && !currentDocSet.isEmpty()) {
						for (DictionarySupportingDocumentation sd : currentDocSet) {
							if (sd.getUserFile() != null)
								sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));

							sd.setDictionaryEventLog(eventLog);
						}
					}
					eventLog.setSupportingDocumentationSet(currentDocSet);

					dictionaryManager.saveEventLog(eventLog, oldStatus.getId().toString(),
							DataElementStatus.PUBLISHED.getId().toString(), comment,
							EventType.STATUS_CHANGE_TO_PUBLISHED);
				}
			}
		}

		webServiceManager.registerEntityToPermissionGroup(getAccount(), ServiceConstants.PUBLISHED_FORM_STRUCTURES,
				EntityType.DATA_STRUCTURE, dataStructure.getId(), PermissionType.READ);
	}

	public void removeFromPublicGroups(FormStructure dataStructure) {

		try {
			webServiceManager.unregisterEntityToPermissionGroup(ServiceConstants.PUBLISHED_FORM_STRUCTURES,
					EntityType.DATA_STRUCTURE, dataStructure.getId(), PermissionType.READ);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getOwnerName() throws MalformedURLException, UnsupportedEncodingException {

		// Make call to get account information on Data Structure
		Account account = webServiceManager.getEntityOwnerAccountRestful(getAccount(), currentDataStructure.getId(),
				EntityType.DATA_STRUCTURE);

		// set full name to owner name
		String ownerName = account.getUser().getFullName();

		return ownerName;

	}

	/**
	 * When new form structures are created (with a new change or with create draft copy) the permissions page can be
	 * skipped. This creates a default ownership of the current user.
	 */
	private void createNewOwnership() {

		// Add New Owner Permission
		EntityMap em = new EntityMap();
		em.setAccount(getAccount());
		// em.setEntityId( null );
		em.setPermission(PermissionType.OWNER);
		em.setType(EntityType.DATA_STRUCTURE);

		getSessionDataStructure().setEntityMapList(new ArrayList<EntityMap>());
		getSessionDataStructure().getEntityMapList().add(em);
		getSessionDataStructure().getEntityMapAuthNameList().add(em.getAccount().getDisplayName());
	}

	public void loadEntityMap() throws MalformedURLException, UnsupportedEncodingException {

		// If there is not entityMapList then try and retrieved one with the web
		// service call
		if (sessionDataStructure.getEntityMapList() == null) {
			List<EntityMap> entityMapList = null;

			// NOTE: If there are no rows returned from the entity map table,
			// THE ENTITY MAP WILL RETURN NULL
			entityMapList = webServiceManager.listEntityAccessRestful(getAccount(),
					sessionDataStructure.getDataStructure().getId(), EntityType.DATA_STRUCTURE);

			sessionDataStructure.setEntityMapList(entityMapList);
		}

		return;
	}

	public void loadEntityMapForPublishedFS(Long oldId) throws MalformedURLException, UnsupportedEncodingException {

		// If there is not entityMapList then try and retrieved one with the web
		// service call
		if (sessionDataStructure.getEntityMapList() == null) {
			List<EntityMap> entityMapList = null;

			// NOTE: If there are no rows returned from the entity map table,
			// THE ENTITY MAP WILL RETURN NULL
			entityMapList = webServiceManager.listEntityAccessRestful(getAccount(), oldId, EntityType.DATA_STRUCTURE);

			sessionDataStructure.setEntityMapList(entityMapList);
		}

		return;
	}

	// url:
	// http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/dataStructureAction!getSupportingDocumentationList.action
	public String getSupportingDocumentationList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<SupportingDocumentationInterface> outputList = new ArrayList<SupportingDocumentationInterface>(
					getSessionDataStructure().getDataStructure().getSupportingDocumentationSet());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new SupportDocIdtListDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	public StreamResult editDocumentDataTableSave() throws MalformedURLException, UnsupportedEncodingException {

		currentDataStructure = getSessionDataStructure().getDataStructure();
		saveChangesToSession();

		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	public Boolean getHasAssociatedEforms() {
		return this.hasAssociatedEforms;
	}

	public void setHasAssociatedEforms(Boolean hasAssociatedEforms) {
		this.hasAssociatedEforms = hasAssociatedEforms;
	}

	public Set<DictionaryEventLog> getEventLogList() {
		return eventLogList;
	}

	public void setEventLogList(Set<DictionaryEventLog> eventLogList) {
		this.eventLogList = eventLogList;
	}

	public Set<DictionaryEventLog> getMinorMajorChangeLogList() throws UnsupportedEncodingException {
		return minorMajorChangeLogList;
	}

	public void setMinorMajorChangeLogList(Set<DictionaryEventLog> minorMajorChangeLogList) {
		this.minorMajorChangeLogList = minorMajorChangeLogList;
	}

	public Set<DictionaryEventLog> getStatusChangeLogList() {
		return statusChangeLogList;
	}

	public void setStatusChangeLogList(Set<DictionaryEventLog> statusChangeLogList) {
		this.statusChangeLogList = statusChangeLogList;
	}


	/*-----------------------------Form structure status change event log------------------------------------*/
	public String formStructureStatusChange() {

		// no need to clear the session if the user is navigating between the status change dialogs
		if (backNavigation == false) {
			getSessionDictionaryStatusChange().clear();

		}

		getSessionDictionaryStatusChange().setNewStatusType(StatusType.statusOf(statusId));

		getSessionDictionaryStatusChange().setInDataElementPage(false);

		return PortalConstants.ACTION_STATUS_CHANGE;
	}

	public String attachedDEStatusChange() {

		getSessionDictionaryStatusChange().setStatusReason(statusReason);
		getSessionDictionaryStatusChange().setApproveReason(reason);

		getSessionDictionaryStatusChange().setInDataElementPage(true);

		return PortalConstants.ATTACHED_DE_STATUS_CHANGE;
	}

	public StreamResult submitFSStatusChange() throws SQLException, Exception {

		getSessionDictionaryStatusChange().setAttachedDEReason(attachedDEReason);

		// if status reason is not set, there is no status change on data elements due to
		// status change on this form structure , will have to check this so that it will not be overridden by null
		if (getSessionDictionaryStatusChange().getStatusReason() == null) {
			getSessionDictionaryStatusChange().setStatusReason(statusReason);
		}

		if (getSessionDictionaryStatusChange().getApproveReason() == null) {
			getSessionDictionaryStatusChange().setApproveReason(reason);
		}
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	public String refreshEventLogTable() throws UnsupportedEncodingException {

		Long originalEntityId = dictionaryService
				.getOriginalFormStructureIdByName(getSessionDataStructure().getDataStructure().getShortName());
		eventLogList = dictionaryService.getAllFSEventLogs(originalEntityId);

		updateEventLogListWithUsers(eventLogList);

		return PortalConstants.ACTION_EVENT_LOG_TABLE;
	}

	public String refreshSupportDoc() {
		return PortalConstants.ACTION_SUPPORT_DOC_TABLE;
	}

	public String listAttachedDEs() {

		dataElementList = getSessionDictionaryStatusChange().getAttachedDEs();

		return PortalConstants.LIST_ATTACHED_DES;
	}

	public String getStatusReason() {

		return statusReason;
	}

	public void setStatusReason(String statusReason) {

		this.statusReason = statusReason;
	}

	public String getAttachedDEReason() {
		return attachedDEReason;
	}

	public void setAttachedDEReason(String attachedDEReason) {
		this.attachedDEReason = attachedDEReason;

	}

	public Set<DataElement> getDataElementList() {
		return dataElementList;
	}

	public void setDataElementList(Set<DataElement> dataElementList) {
		this.dataElementList = dataElementList;
	}

	public boolean getIsRequestedStatusChange() {

		currentDataStructure = getSessionDataStructure().getDataStructure();

		if (StatusType.AWAITING_PUBLICATION == currentDataStructure.getStatus()) {
			return true;
		}
		return false;
	}



	public int getNumberOfAffectedDE() {

		int count = 0;

		currentDataStructure = getSessionDataStructure().getDataStructure();
		if (currentDataStructure != null) {
			for (DataElement dataElement : (currentDataStructure).getDataElements().values()) {
				if (DataElementStatus.DRAFT == dataElement.getStatus()
						|| DataElementStatus.AWAITING == dataElement.getStatus()) {

					getSessionDictionaryStatusChange().getAttachedDEs().add(dataElement);
					count++;
				}
			}
		}

		return count;
	}

	public SessionDictionaryStatusChange getSessionDictionaryStatusChange() {

		if (sessionDictionaryStatusChange == null) {
			sessionDictionaryStatusChange = new SessionDictionaryStatusChange();
		}

		return sessionDictionaryStatusChange;
	}

	public String getNewStatus() {
		return getSessionDictionaryStatusChange().getNewStatusType().getType();
	}

	public Long getCurrentId() {
		return currentId;
	}

	public void setCurrentId(Long currentId) {
		this.currentId = currentId;
	}

	public boolean isBackNavigation() {
		return backNavigation;
	}

	public void setBackNavigation(boolean backNavigation) {
		this.backNavigation = backNavigation;
	}



	/*-----------------------------End  of Form structure status change event log------------------------------------*/


	/*------documentation upload, 
	 * kept this part here because document upload is in the same page as the form structure status change----*/
	public int getEventLogSupportDocSize() {

		int size = 0;
		if (getEventLogSupportDocList() != null)
			size = getEventLogSupportDocList().size();

		return size;

	}

	public Boolean getIsDocumentLimitNR() {
		if (getEventLogSupportDocSize() < getDocumentationLimit())
			return true;
		return false;
	}

	public int getDocumentationLimit() {
		return PortalConstants.MAXIMUM_DOCUMENT_UPLOAD;
	}

	public String getActionName() {
		return PortalConstants.ACTION_FS_EVENT_LOG_DOC;
	}

	public Set<DictionarySupportingDocumentation> getEventLogSupportDocList() {

		boolean inDataElementPage = getSessionDictionaryStatusChange().getInDataElementPage();

		Set<DictionarySupportingDocumentation> supportDocListSession;

		// if admin is in the attached dataelement dialog, the documents uploaded will only be
		// associated with dataelements
		if (inDataElementPage) {
			supportDocListSession = getSessionDictionaryStatusChange().getDEEventLogDocumentation();
		} else {
			supportDocListSession = getSessionDictionaryStatusChange().getfSEventLogDocumentation();
		}

		return supportDocListSession;

	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/dataStructureAction!getDataElementsList.action
	public String getDataElementsList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			dataElementList = getSessionDictionaryStatusChange().getAttachedDEs();
			ArrayList<DataElement> outputList = new ArrayList<DataElement>(getDataElementList());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new DataElementListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url:
	// http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/dataStructureAction!getSupportingDocumentationList.action
	public String getEventLogDocumentationList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<DictionarySupportingDocumentation> outputList =
					new ArrayList<DictionarySupportingDocumentation>(getEventLogSupportDocList());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new SupportDocIdtListDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	/*-----------------------------End  of documentation upload------------------------------------*/
	/**
	 * @param eventLogList
	 * @throws UnsupportedEncodingException
	 * 
	 *         Since dictionary db doesn't have account table, this method makes rest call per disease to get user
	 *         details and update eventLog with the user.
	 */
	public void updateEventLogListWithUsers(Set<DictionaryEventLog> eventLogList) throws UnsupportedEncodingException {

		SetMultimap<Long, Long> diseaseIdUserIdMap = HashMultimap.create();

		for (DictionaryEventLog eventLog : eventLogList) {
			if (eventLog.getDiseaseId() != null && eventLog.getUserId() != null) {
				diseaseIdUserIdMap.put(eventLog.getDiseaseId(), eventLog.getUserId());
			} else if (eventLog.getMinorMajorChange()) {
				minorMajorChangeLogList.add(eventLog);
			} else {
				statusChangeLogList.add(eventLog);
			}
		}
		// get all set of disease ids
		Set<Long> diseaseIds = diseaseIdUserIdMap.keySet();

		Set<Long> userIds;
		String accountUrl;
		String proxyTicket;
		RestAccountProvider accountProvider;

		Map<Long, List<User>> diseaseIdUserListMap = new HashMap<Long, List<User>>();

		for (Long diseaseId : diseaseIds) {
			userIds = diseaseIdUserIdMap.get(diseaseId);
			accountUrl = modulesConstants.getModulesAccountURL(diseaseId);
			proxyTicket = PortalUtils.getProxyTicket(accountUrl);

			accountProvider = new RestAccountProvider(modulesConstants.getModulesAccountURL(Long.valueOf(diseaseId)),
					proxyTicket);

			List<User> users = accountProvider.getUserDetailsByIds(userIds);
			diseaseIdUserListMap.put(diseaseId, users);

		}

		for (DictionaryEventLog eventLog : eventLogList) {
			if (eventLog.getDiseaseId() != null && eventLog.getUserId() != null) {
				List<User> users = diseaseIdUserListMap.get(eventLog.getDiseaseId());

				for (User user : users) {
					if (user.getId().equals(eventLog.getUserId())) {
						eventLog.setUser(user);
					}
				}

				if (eventLog.getMinorMajorChange()) {
					minorMajorChangeLogList.add(eventLog);
				} else {
					statusChangeLogList.add(eventLog);
				}
			}
		}
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/dataStructureAction!getHistoryLogList.action
	public String getHistoryLogList() throws UnsupportedEncodingException {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			Long originalEntityId = getCurrentDataStructure().getId();
			eventLogList = dictionaryService.getAllFSEventLogs(originalEntityId);

			updateEventLogListWithUsers(eventLogList);
			ArrayList<DictionaryEventLog> outputList = new ArrayList<DictionaryEventLog>(getMinorMajorChangeLogList());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new DictionaryEventLogListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url:
	// http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/dataStructureAction!getDictionaryEventLogList.action
	public String getDictionaryEventLogList() throws UnsupportedEncodingException {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			Long originalEntityId = dictionaryService
					.getOriginalFormStructureIdByName(getSessionDataStructure().getDataStructure().getShortName());
			eventLogList = dictionaryService.getAllFSEventLogs(originalEntityId);

			updateEventLogListWithUsers(eventLogList);
			ArrayList<DictionaryEventLog> outputList = new ArrayList<DictionaryEventLog>(getStatusChangeLogList());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new DictionaryEventLogListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

}
