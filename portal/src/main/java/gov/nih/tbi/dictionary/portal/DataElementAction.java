package gov.nih.tbi.dictionary.portal;

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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.SchemaMappingManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.WebServiceManager;
import gov.nih.tbi.dictionary.model.DataElementForm;
import gov.nih.tbi.dictionary.model.KeywordForm;
import gov.nih.tbi.dictionary.model.SessionDictionaryStatusChange;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.model.ValueRangeForm;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;
import gov.nih.tbi.dictionary.model.hibernate.DictionarySupportingDocumentation;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.ExternalId;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringType;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringUnit;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.service.rulesengine.RulesEngineUtils;
import gov.nih.tbi.dictionary.service.rulesengine.model.RulesEngineException;
import gov.nih.tbi.dictionary.ws.RestDictionaryProvider;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.repository.model.SupportingDocumentationInterface;
import gov.nih.tbi.taglib.datatableDecorators.DataElementListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.DictionaryEventLogListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.SupportDocIdtListDecorator;


/**
 * Defines actions for data elements.
 * 
 * @author Francis Chen
 */
public class DataElementAction extends BaseDictionaryAction {

	private static final long serialVersionUID = 9217937588706821860L;

	static Logger logger = Logger.getLogger(DataElementAction.class);

	/******************************************************************************************************/

	@Autowired
	WebServiceManager webServiceManager;

	@Autowired
	SchemaMappingManager schemaMappingManager;

	DataElement currentDataElement;

	List<FormStructure> attachedDataStructures;

	Long dataStructureId;

	DataElementForm dataElementForm;

	KeywordForm keywordForm;

	String dataType;

	Boolean deletable;

	String currentPage;

	ValueRangeForm valueRangeForm;

	Keyword newKeyword;

	Keyword newLabel;

	String keywordNew;

	String keywordSearchKey;

	String labelSearchKey;

	Set<Keyword> sessionKeywords;

	Long publicationId;

	String reason;

	String fileErrors = "";

	Boolean rulesEngineException;

	String auditNote;

	String modulesDDTURL;

	// Map to store external ID values for binding with UI, key: ExternalId enum name, value: input value
	private Map<String, String> externalIdMap;

	List<String> changeStrings;
	SeverityLevel dataElementChangeSeverity;
	Boolean shortNameChangeRequired;
	
	
	//Dataelement status change
	String statusReason;
	Long currentId;
	boolean inDataElementPage=true;
	private Set <DictionaryEventLog> eventLogList = new HashSet<DictionaryEventLog>() ;
	boolean bulkStatusChange=false;
	private List<DataElement> dataElementList;
	private String checkedElementIds;
	
	//normal flow to view dataelement is set as true.If dataelement
	// is viewed from status change, this variable is set to false
	boolean showDataElementDetail =true;
	
	//minorOrmajor data element change
	private Set <DictionaryEventLog> minorMajorChangeLogList = new HashSet<DictionaryEventLog>();
	//status change list e.g. published,deprecated
	private Set <DictionaryEventLog> statusChangeLogList = new HashSet<DictionaryEventLog>();
	
	// this should be true if being loaded from query tool
	private Boolean queryArea;
		
	@Autowired
	SessionDictionaryStatusChange sessionDictionaryStatusChange;
	
	
	/******************************************************************************************************/
	//BaseDataElementAction functionalities
	public PermissionType getSessionDataElementPermission() throws UnsupportedEncodingException, UserAccessDeniedException, UserAccessDeniedException{
		if(sessionDataElement.getSessionDataElementUserPermissionType()!=null){
			return sessionDataElement.getSessionDataElementUserPermissionType();
		} else {
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			PermissionType userEntityAccess = restProvider.getAccess(getAccount().getId(), EntityType.DATA_ELEMENT, getCurrentDataElement().getId()).getPermission();
			
			sessionDataElement.setSessionDataElementUserPermissionType(userEntityAccess);
			return sessionDataElement.getSessionDataElementUserPermissionType();
		}
	}
	
	public boolean getIsOwner() throws MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		return PermissionType.compare(getSessionDataElementPermission(),
				PermissionType.OWNER) == 0;
	}
	public Boolean getIsDataElementAdmin() throws MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		return PermissionType.compare(getSessionDataElementPermission(),
				PermissionType.ADMIN) >= 0;
	}
	public boolean hasWritePermission() throws MalformedURLException, UnsupportedEncodingException, UserAccessDeniedException {

		return PermissionType.compare(getSessionDataElementPermission(),
				PermissionType.WRITE) >= 0;
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 ****************************************************************************************************/

	public List<Disease> getDiseaseList() throws MalformedURLException, UnsupportedEncodingException {

		return staticManager.getDiseaseList();
	}

	public void setPublicationId(Long publicationId) {

		this.publicationId = publicationId;
	}

	public String getReason() {

		return reason;
	}

	public void setReason(String reason) {

		this.reason = reason;
	}

	public List<String> getChangeStrings() {

		return changeStrings;
	}

	public void setChangeStrings(List<String> changeStrings) {

		this.changeStrings = changeStrings;
	}

	public SeverityLevel getDataElementChangeSeverity() {

		return dataElementChangeSeverity;
	}

	public void setDataElementChangeSeverity(String dataElementChangeSeverity) {

		this.dataElementChangeSeverity = SeverityLevel.getByName(dataElementChangeSeverity);
	}

	public Boolean getShortNameChangeRequired() {

		return shortNameChangeRequired;
	}

	public void setShortNameChangeRequired(Boolean shortNameChangeRequired) {

		this.shortNameChangeRequired = shortNameChangeRequired;
	}

	public DataElement getCurrentDataElement() {

		// WARNING: Changing this breaks a ton of shit
		return getSessionDataElement().getDataElement();
	}

	public DataElement getDataElementSession() {

		return getSessionDataElement().getDataElement();
	}

	public ValueRangeForm getValueRangeForm() {

		return valueRangeForm;
	}

	public void setValueRangeForm(ValueRangeForm valueRangeForm) {

		this.valueRangeForm = valueRangeForm;
	}

	public String getNindsId() {
		return schemaMappingManager.getDeSchemaSystemId(currentDataElement, "NINDS");
	}

	public KeywordForm getKeywordForm() {

		return keywordForm;
	}

	// Work around so the viewDataElement-details page can display information for data element
	public MapElement getMapElement() {

		MapElement newElement = new MapElement();
		newElement.setStructuralDataElement(currentDataElement.getStructuralObject());
		return newElement;
	}

	// Used by struts to capture the new keyword name on form submission
	// Captures the string from the form element "keywordNew"
	public void setKeywordNew(String keywordNew) {

		this.keywordNew = keywordNew;
	}

	public void setKeywordSearchKey(String keywordSearchKey) {

		this.keywordSearchKey = keywordSearchKey;
	}

	public void setLabelSearchKey(String labelSearchKey) {

		this.labelSearchKey = labelSearchKey;
	}

	/**
	 * returns true if the data element in session is the latest version.
	 * 
	 * @return
	 */
	public boolean getIsLatestVersion() {

		DataElement isLatestDe = getSessionDataElement().getDataElement();
		return dictionaryManager.getIsLatestVersion(isLatestDe);
	}

	public String getKeywordNew() {

		return keywordNew;
	}

	public String getKeywordSearchKey() {

		return keywordSearchKey;
	}

	public String getLabelSearchKey() {

		return labelSearchKey;
	}

	public void setAuditNote(String auditNote) {

		this.auditNote = auditNote;
	}

	public String getAuditNote() {

		return auditNote;
	}

	// This function is called due to the hidden field 'sessionKeywords' on the addKeywords.jsp page
	// The actual value of the field is not stored, but instead the set of Keywords that have been added
	// in the current session are saved to the sessionKeywords variable.
	// Once there they are called by the KeywordNameValidator
	public void setSessionKeywords(String sessionKeywords) {

		this.sessionKeywords = getSessionDataElement().getNewKeywords();
	}

	public void setSessionLabels(String sessionLabels) {

		this.sessionKeywords = getSessionDataElement().getNewKeywords();
	}

	public Set<Keyword> getSessionKeywords() {

		return getSessionDataElement().getNewKeywords();
	}

	public Set<Keyword> getSessionLabels() {

		return getSessionDataElement().getNewKeywords();
	}

	public String getCurrentPage() {

		return currentPage;
	}

	public Boolean getRulesEngineException() {

		return rulesEngineException;
	}

	public void setCurrentPage(String currentPage) {

		getSessionDataElement().setPrevPage(currentPage);
		this.currentPage = currentPage;
	}

	public String getFileErrors() {

		return fileErrors;
	}

	public void setFileErrors(String fileErrors) {

		this.fileErrors = fileErrors;
	}

	public DataElementForm getDataElementForm() {

		return dataElementForm;
	}

	public void setDataElementForm(DataElementForm dataElementForm) {

		this.dataElementForm = dataElementForm;
	}

	public void setKeywordForm(KeywordForm keywordForm) {

		this.keywordForm = keywordForm;
	}

	public String getDataType() {

		return PortalConstants.DATAELEMENT;
	}

	public void setDataType(String dataType) {

		this.dataType = dataType;
	}

	public Boolean getDeletable() {

		return deletable;
	}

	public void setDeletable(Boolean deletable) {

		this.deletable = deletable;
	}

	public void setNewKeyword(Keyword newKeyword) {

		this.newKeyword = newKeyword;
	}

	public Keyword getNewKeyword() {

		return newKeyword;
	}

	public void setNewLabel(Keyword newLabel) {

		this.newLabel = newLabel;
	}

	public Keyword getNewLabel() {

		return newLabel;
	}
	
	public Boolean getQueryArea() {
		return queryArea;
	}

	public void setQueryArea(Boolean queryArea) {
		this.queryArea = queryArea;
	}

	public Boolean getIsPublished() {

		return DataElementStatus.PUBLISHED.equals(getSessionDataElement().getDataElement().getStatus());
	}

	/**
	 * Retrieves the list of keywords available for this user to attach to the current data element
	 * 
	 * @return list of keywords that can be attached
	 */
	public Set<Keyword> getAvailableKeywords() {

		String searchKey = getRequest().getParameter("keywordSearchKey");

		if (searchKey == null || searchKey.trim().equals(PortalConstants.EMPTY_STRING)) {
			searchKey = "";
		}

		// Query for the keywords and add them to the list of available keywords
		Set<Keyword> availableKeywords = new LinkedHashSet<Keyword>();
		for (Keyword key : dictionaryManager.searchKeywords(searchKey)) {
			availableKeywords.add(key);
		}

		// Add list of keywords added this session
		for (Keyword key : getSessionDataElement().getNewKeywords()) {
			availableKeywords.add(key);
		}

		// Strip out any keywords that are already attached to the data element
		if (sessionDataElement != null && sessionDataElement.getDataElement() != null
				&& sessionDataElement.getDataElement().getKeywords() != null) {
			Set<Keyword> keywordList = sessionDataElement.getDataElement().getKeywords();
			for (Keyword k : keywordList) {
				for (Keyword keyword : availableKeywords) {
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
	public Set<Keyword> getCurrentKeywords() throws UnsupportedEncodingException {

		LinkedHashSet<Keyword> current = new LinkedHashSet<Keyword>();
		RestDictionaryProvider restProvider =
				new RestDictionaryProvider(modulesConstants.getModulesDDTURL(getDiseaseId()), null);
		Long deID = getMapElement().getStructuralDataElement().getId();

		if (sessionDataElement != null && sessionDataElement.getDataElement() != null
				&& sessionDataElement.getDataElement().getKeywords() != null) {

			// Add each keyword from the keywordElement list to current
			for (Keyword k : sessionDataElement.getDataElement().getKeywords()) {
				current.add(k);
			}

			return current;
		} else if (restProvider.getKeywords(deID) != null) {

			for (Keyword k : restProvider.getKeywords(deID)) {
				current.add(k);
			}
		}

		return current;
	}

	/**
	 * Retrieves the list of labels available for this user to attach to the current data element
	 * 
	 * @return list of labels that can be attached
	 */
	public Set<Keyword> getAvailableLabels() {

		String searchKey = getRequest().getParameter("labelSearchKey");

		if (searchKey == null || searchKey.trim().equals(PortalConstants.EMPTY_STRING)) {
			searchKey = "";
		}

		// Query for the keywords and add them to the list of available keywords
		LinkedHashSet<Keyword> availableLabels = new LinkedHashSet<Keyword>();
		for (Keyword label : dictionaryManager.searchLabels(searchKey)) {
			availableLabels.add(label);
		}

		// Add list of keywords added this session
		for (Keyword label : getSessionDataElement().getNewLabels()) {
			availableLabels.add(label);
		}

		// Strip out any keywords that are already attached to the data element
		if (sessionDataElement != null && sessionDataElement.getDataElement() != null
				&& sessionDataElement.getDataElement().getLabels() != null) {
			Set<Keyword> labelList = sessionDataElement.getDataElement().getLabels();
			for (Keyword l : labelList) {
				for (Keyword label : availableLabels) {
					if (label.getKeyword().equalsIgnoreCase(l.getKeyword())) {
						availableLabels.remove(label);
						break;
					}
				}
			}
		}

		return availableLabels;
	}

	/**
	 * Retrieves the list of keywords that are attached to this data structure
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Set<Keyword> getCurrentLabels() throws UnsupportedEncodingException {

		LinkedHashSet<Keyword> current = new LinkedHashSet<Keyword>();
		RestDictionaryProvider restProvider =
				new RestDictionaryProvider(modulesConstants.getModulesDDTURL(getDiseaseId()), null);
		Long deID = getMapElement().getStructuralDataElement().getId();

		if (sessionDataElement != null && sessionDataElement.getDataElement() != null
				&& sessionDataElement.getDataElement().getLabels() != null) {

			// Add each keyword from the keywordElement list to current
			for (Keyword l : sessionDataElement.getDataElement().getLabels()) {
				current.add(l);
			}

			return current;
		} else if (restProvider.getKeywords(deID) != null) {

			for (Keyword l : restProvider.getKeywords(deID)) {
				current.add(l);
			}
		}

		return current;
	}

	/**
	 * Gets the list of subgroups by disease
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<Subgroup> getSubgroupList() throws MalformedURLException, UnsupportedEncodingException {

		return dictionaryManager.getSubgroupsByDisease(getDisease());
	}

	/**
	 * Gets the static list of Populations
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<Population> getPopulationList() throws MalformedURLException, UnsupportedEncodingException {

		return staticManager.getPopulationList();
	}

	/**
	 * Convenience method for getting a single disease
	 * 
	 * @return
	 */
	// TODO: MV 6/10/2014 - This function and any dependency on it (jsp and java) needs to be refactored. This function
	// is only intended for single disease front end.
	public Disease getDisease() {

		if (valueRangeForm != null && valueRangeForm.getSubDomainElementList() != null) {
			Set<String> diseases = new HashSet<String>();
			Disease singleDisease = null;
			for (SubDomainElement se : valueRangeForm.getSubDomainElementList()) {
				singleDisease = se.getDisease();
				diseases.add(se.getDisease().getName());
			}
			if (diseases.size() == 1) {
				return singleDisease;
			}
		}

		return null;
	}

	/**
	 * Gets the list of Domains
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<Domain> getDomainList() throws MalformedURLException, UnsupportedEncodingException {

		return dictionaryManager.getDomainsByDisease(this.getDisease());
	}

	/**
	 * Gets the static list of Measurement Types
	 */
	public List<MeasuringType> getMeasuringTypeList() {

		return staticManager.getMeasuringTypeList();
	}

	/**
	 * Gets the static list of Measurement Units
	 */
	public List<MeasuringUnit> getMeasuringUnitList() {

		return staticManager.getMeasuringUnitList();
	}

	/*********************************************************************************************************/

	/**
	 * This action returns a list of data structures that are attached to the current data element.
	 * 
	 * @return List<BasicDataStructure>
	 */
	public List<FormStructure> getAttachedDataStructures() throws MalformedURLException, UnsupportedEncodingException {
		String publicString = getRequest().getParameter(PortalConstants.PUBLIC_AREA);
		if (publicString != null || getInPublicNamespace()) {
			publicArea = true;
		} else {
			publicArea = false;
		}
		if(publicArea){
			for (Iterator<FormStructure> attachedDSItr = attachedDataStructures.iterator(); attachedDSItr.hasNext();) {
				FormStructure attachedDataStructure = attachedDSItr.next();
				if(StatusType.DRAFT.equals(attachedDataStructure.getStatus())
						|| StatusType.UNKNOWN.equals(attachedDataStructure.getStatus())){
					attachedDSItr.remove();
				}
			}

		} else if(!getIsDictionaryAdmin() || !dictionaryManager.hasRole(getAccount(), RoleType.ROLE_ADMIN)){
			List<Long> formStructureIds = new ArrayList<Long>();
			for (FormStructure formStructure : attachedDataStructures) {
				formStructureIds.add(formStructure.getId());
			}
	 
			
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			List<EntityMap> entityMaps = restProvider.getEntityMapsByEntityIds(formStructureIds, EntityType.DATA_STRUCTURE);

			Map<Long, EntityMap> entityIdToEntityMapMap = new HashMap<Long, EntityMap>();

			for (EntityMap entityMap : entityMaps) {
				entityIdToEntityMapMap.put(entityMap.getEntityId(), entityMap);
			}

			Iterator<FormStructure> attDataStrItr = attachedDataStructures.iterator();

			while (attDataStrItr.hasNext()) {
				FormStructure attachedDataStructure = attDataStrItr.next();

				if (!getCanView(attachedDataStructure, entityIdToEntityMapMap)) {
					attDataStrItr.remove();
				}
			}
		}
		

		return attachedDataStructures;
	}

	public boolean getCanView(FormStructure attachedDataStructure, Map<Long, EntityMap> entityIdToEntityMapMap)
			throws MalformedURLException, UnsupportedEncodingException {

		// If the form is published (or archived), we are no longer using access
		// just display form structure.
		if (StatusType.PUBLISHED.equals(attachedDataStructure.getStatus())
				|| StatusType.ARCHIVED.equals(attachedDataStructure.getStatus())) {
			return true;
		} else {
			EntityMap entityAccess = entityIdToEntityMapMap.get(attachedDataStructure.getId());

			// if entity map entry does not exist, do not grant permission
			if (entityAccess == null) {
				return false;
			}

			if (entityAccess.getPermission() != null) {
				return (PermissionType.compare(entityAccess.getPermission(), PermissionType.READ) >= 0);
			} else {
				return false;
			}
		}
	}

	public String getFormType() {

		if (getSessionDataElement() == null || getSessionDataElement().getDataElement() == null
				|| getSessionDataElement().getDataElement().getId() == null)

		{
			return PortalConstants.FORMTYPE_CREATE;
		} else {
			return PortalConstants.FORMTYPE_EDIT;
		}
	}

	// TODO: Implement this when we have fields that needs to be static
	public Boolean getEnforceStaticFields() {

		return false;
	}

	/**
	 * Returns list of available statuses
	 * 
	 * @return
	 */
	public List<DataType> getTypes() {

		List<DataType> typeList = new ArrayList<DataType>();

		for (DataType type : DataType.values()) {
			typeList.add(type);
		}

		return typeList;
	}

	public List<ValueRange> getValueRangeList() {

		DataElement element = this.getSessionDataElement().getDataElement();
		Set <ValueRange> result = element.getValueRangeList();
		return new ArrayList(result);

	}

	/**
	 * @return the dataStructureId
	 */
	public Long getDataStructureId() {

		return dataStructureId;
	}

	/**
	 * @param dataStructureId the dataStructureId to set
	 */
	public void setDataStructureId(Long dataStructureId) {

		this.dataStructureId = dataStructureId;
	}

	public void checkDataElement(DataElement currentDataElement) {

		if (currentDataElement == null) {
			throw new RuntimeException("No such Data Element");
		}
	}

	/******************************************************************************************************/

	/**
	 * This action is for the view data element page
	 * 
	 * @return String
	 * @throws MalformedURLException
	 * @throws UserPermissionException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public String view() throws MalformedURLException, UserPermissionException, UnsupportedEncodingException, UserAccessDeniedException {
		
		// clear DE permission session
		sessionDataElement.clearPermission();
		
		getSessionDictionaryStatusChange().clear();

		String publicString = getRequest().getParameter(PortalConstants.PUBLIC_AREA);
		if (publicString != null || getInPublicNamespace()) {
			publicArea = true;
		} else {
			publicArea = false;
		}

		String deName = getRequest().getParameter(PortalConstants.DATA_ELEMENT_NAME);

		if (publicArea) {

			// used to make a WS call before the iframe pointed to the dictionary.
			// there is a catch just below this to see if the DE is draft (which is not displayable in the public site
			currentDataElement = dictionaryManager.getLatestDataElementByName(deName);
		} else {

			String deId = getRequest().getParameter(PortalConstants.DATAELEMENT_ID);
			String deVersion = getRequest().getParameter(PortalConstants.VERSION);

			if (deName != null) {
				if (deVersion != null) {
					currentDataElement = dictionaryManager.getDataElement(deName, deVersion);
				} else {
					currentDataElement = dictionaryManager.getLatestDataElementByName(deName);
				}
			} else {
				try {
					currentDataElement = dictionaryManager.getDataElement(Long.valueOf(deId));
				} catch (NumberFormatException e) {
					throw new NumberFormatException("deId cannot be null");
				}
			}
		}

		if (!publicArea) {
			currentDataElement.setCreatedBy(getDisplayNameByUsername(currentDataElement.getCreatedBy()));
		} else {
			currentDataElement.setCreatedBy(null);
		}
		// Permissions check
		// All users should be able to access PUBLISHED and AWAITING PUBLICATION DS
		// To revert the system back to only being able to publicly see PUBLISHED DEs, just remove this if block and
		// keep what is in the else block as the only permissions check.
		if (publicArea || (queryArea != null && queryArea)) {
			if (DataElementStatus.DRAFT.equals(currentDataElement.getStatus())) {
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
			}
		} else {
			// User has permission to see this data element.
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			String[] twoProxyTickets = PortalUtils.getMultipleProxyTickets(accountUrl, 2);

			RestAccountProvider restProvider;
			restProvider = new RestAccountProvider(accountUrl, twoProxyTickets[0]);
			PermissionType permission = restProvider
					.getAccess(getAccount().getId(), EntityType.DATA_ELEMENT, currentDataElement.getId())
					.getPermission();

			// If the permission returned null, then the user does not have any
			// access to the given entity
			// check if the user has access to the given entity through its
			// attached form structure
			// if the user has permission to the form structure, grant a read
			// permission to attached data elements
			if (permission == null) {
				attachedDataStructures = dictionaryManager.getAttachedDataStructure(currentDataElement.getName(),
						currentDataElement.getVersion());

				Set<Long> attachedIds = getAttachedFSIds(attachedDataStructures);
				Set<Long> accessIds = null;

				restProvider = new RestAccountProvider(accountUrl, twoProxyTickets[1]);
				accessIds = restProvider.listUserAccess(getAccount().getId(), EntityType.DATA_STRUCTURE,
						PermissionType.READ, false);

				// if the user has access to at least one attached form
				// structure, the user will be able to view de
				if (CollectionUtils.intersection(attachedIds, accessIds).size() >=1) {
					permission = PermissionType.READ;
				}

			}

			if (permission == null || (!(PermissionType.compare(permission, PermissionType.READ) >= 0)
					&& DataElementStatus.DRAFT.equals(currentDataElement.getStatus()))) {
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
			}				
		 }

		this.checkDataElement(currentDataElement);

		getSessionDataElement().setDataElement(currentDataElement);

		attachedDataStructures = dictionaryManager.getAttachedDataStructure(currentDataElement.getName(),
				currentDataElement.getVersion());
	
		Long originalEntityId = dictionaryService.getOriginalDataElementIdByName(currentDataElement.getName());		
		eventLogList = dictionaryService.getAllDEEventLogs(originalEntityId);
		
		updateEventLogListWithUsers(eventLogList);

		return PortalConstants.ACTION_VIEW;
	}
	
	public String getDataElementPvList(){
		
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<ValueRange> valueRangeList =
					new ArrayList<ValueRange>(getSessionDataElement().getDataElement().getValueRangeList());
			
			idt.setList(valueRangeList);
			idt.setTotalRecordCount(valueRangeList.size());
			idt.setFilteredRecordCount(valueRangeList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		
		return null;
	}

	public String viewDetails() throws NumberFormatException, UnsupportedEncodingException, UserPermissionException, UserAccessDeniedException {

		modulesDDTURL = "";

		String deName = getRequest().getParameter(PortalConstants.DATA_ELEMENT_NAME);
		
		//we don't want to display viewDetails link if we are viewing the dataelement
		//from status change dialog. Only link from status change dialog has status change parameter	
		if(getRequest().getParameterMap().containsKey(PortalConstants.DATA_ELEMENT_STATUS_CHANGE)){		
			showDataElementDetail =false;
		}

		String publicString = getRequest().getParameter(PortalConstants.PUBLIC_AREA);
		// need to check if we are in the "publicData" namespace. If we are that means a user is not logged in
		// they should not be able to view draft DEs
		if (getInPublicNamespace() || publicString != null ) {
			publicArea = true;
		} else {
			publicArea = false;
		}

		if (publicArea) {
			// not sure why we need 2 of these properties.
			String dictionaryUrl = modulesConstants.getModulesDDTURL();
			modulesDDTURL = dictionaryUrl + "portal/";
			RestDictionaryProvider restProvider = new RestDictionaryProvider(dictionaryUrl, null);
			// currentDataElement = restProvider.getDataElementById(Long.valueOf(deId));
			currentDataElement = restProvider.getDataElementByName(deName);
		} else {
			modulesDDTURL = modulesConstants.getModulesDDTURL(getDiseaseId()) + "portal/";
			if (deName != null) {
				currentDataElement = dictionaryManager.getLatestDataElementByName(deName);
			} else {
				String deNameDup = getRequest().getParameter(PortalConstants.DATA_ELEMENT_NAME);
				for (DataElement dataElement : sessionDataElementList.getDataElements()) {
					if (deNameDup.compareTo(dataElement.getName()) == 0) {
						DataElement preexst =
								dictionaryManager.getDataElement(dataElement.getName(), dataElement.getVersion());
						if (preexst != null) {
							dataElement.setStatus(preexst.getStatus());
						}
						currentDataElement = dataElement;
						break;
					}
				}
			}
		}

		if (!publicArea) {
			currentDataElement.setCreatedBy(getDisplayNameByUsername(currentDataElement.getCreatedBy()));
		} else {
			currentDataElement.setCreatedBy(null);
		}

		this.checkDataElement(currentDataElement);

		// Permissions check
		// All users should be able to access PUBLISHED and AWAITING PUBLICATION DS
		// To revert the system back to only being able to publicly see PUBLISHED DEs, just remove this if block and
		// keep what is in the else block as the only permissions check.
		if (publicArea || (queryArea != null && queryArea)) {
			if (DataElementStatus.DRAFT.equals(currentDataElement.getStatus())) {
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
			}
		}
		// Case: If the currentDataElement is null, then the data element exisits in session and not in the database (is
		// currently being created). In this case the user has permission to view.
		else if (currentDataElement.getId() != null) {
			// User has permission to see this data element.
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			
			String[] twoProxyTickets =PortalUtils.getMultipleProxyTickets(accountUrl, 2);
			
			RestAccountProvider restProvider;
			
			restProvider = new RestAccountProvider(accountUrl, twoProxyTickets[0]);
			PermissionType permission =
					restProvider.getAccess(getAccount().getId(), EntityType.DATA_ELEMENT, currentDataElement.getId())
							.getPermission();
		
			// If the permission returned null, then the user does not have any access to the given entity
			//check if the user has access to the given entity through its attached form structure
			// if the user has permission to the form structure, grant a read permission to attached data elements
			if(permission == null){
				attachedDataStructures = dictionaryManager.getAttachedDataStructure(currentDataElement.getName(),
						currentDataElement.getVersion());
				
				Set<Long> attachedIds = getAttachedFSIds(attachedDataStructures);
				Set<Long> accessIds = null;
				
				restProvider = new RestAccountProvider(accountUrl, twoProxyTickets[1]);
				accessIds = restProvider.listUserAccess(getAccount().getId(), EntityType.DATA_STRUCTURE, PermissionType.READ,
	                    false);
			
				//if the user has access to atleast one attached form structure, the user will be able to view de
				if(CollectionUtils.intersection(attachedIds, accessIds).size()>=1){
					permission=PermissionType.READ;
				}
			
				
			}
			
			if (permission == null || (!(PermissionType.compare(permission, PermissionType.READ) >= 0)
					&& DataElementStatus.DRAFT.equals(currentDataElement.getStatus()))) {
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
			}
			
		}
		getSessionDataElement().setDataElement(currentDataElement);

		return PortalConstants.ACTION_VIEW_DETAILS;
	}

	/**
	 * Action for editing Data Element
	 * 
	 * @return String
	 * @throws UserPermissionException
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws NumberFormatException
	 */
	@SuppressWarnings("unchecked")
	public String edit()
			throws UserPermissionException, MalformedURLException, NumberFormatException, UnsupportedEncodingException, UserAccessDeniedException {

		// clear DE permission session
		sessionDataElement.clearPermission();
				
		// getSessionDataElement().clear();
		currentPage = "details";
		String deName = getRequest().getParameter(PortalConstants.DATA_ELEMENT_NAME);

		if (deName == null) {
			currentDataElement = getSessionDataElement().getDataElement();
		} else {
			try {
				currentDataElement = dictionaryManager.getLatestDataElementByName(deName);
			} catch (Exception e) {
				throw new NumberFormatException("deName cannot be null");
			}
		}

		this.checkDataElement(currentDataElement);

		// only check if not a new Data element
		if (deName != null) {
			Long deId = currentDataElement.getId();
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider =
					new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			PermissionType permission =
					restProvider.getAccess(getAccount().getId(), EntityType.DATA_ELEMENT, deId).getPermission();
			if (!(PermissionType.compare(permission, PermissionType.WRITE) >= 0)) {
				throw new UserPermissionException(ServiceConstants.WRITE_ACCESS_DENIED);
			}

		}

		getSessionDataElement().setDataElement((DataElement) currentDataElement);

		currentDataElement = getSessionDataElement().getDataElement();

		if (dataElementForm == null) {
			dataElementForm = new DataElementForm(currentDataElement);
		}

		if (deName != null) {
			attachedDataStructures = (List<FormStructure>) dictionaryManager
					.getAttachedDataStructure(currentDataElement.getName(), currentDataElement.getVersion());

			if (attachedDataStructures == null || attachedDataStructures.size() == 0) {
				deletable = true;
			} else {
				deletable = false;
			}
		}

		return PortalConstants.ACTION_INPUT;
	}

	// /**
	// * Set the new domain and subgroup list based on the current disease
	// *
	// * @return
	// * @throws MalformedURLException
	// * @throws UnsupportedEncodingException
	// */
	// public String diseaseUpdate() throws MalformedURLException, UnsupportedEncodingException
	// {
	//
	// for (Subgroup s : this.getSubgroupList())
	// {
	// ClassificationElement ce = new ClassificationElement();
	//
	// ce.setSubgroup(s);
	// valueRangeForm.getClassificationElementList().add(ce);
	// }
	//
	// return "updateClassifications";
	// }

	/**
	 * Action for tabbing the page to edit details
	 * 
	 * @return String
	 */
	public String editDetails() {

		saveSession(); // save form to session

		dataElementForm = new DataElementForm(getSessionDataElement().getDataElement());
		setCurrentPage(PortalConstants.DETAILS);

		return PortalConstants.ACTION_EDITDETAILS;
	}

	public String editDocumentation() throws UnsupportedEncodingException {
		saveSession(); // save form to session

		return PortalConstants.ACTION_EDIT_DOCUMENTATION;
	}

	/**
	 * Action for tabbing the page to edit keywords
	 * 
	 * @return String
	 */
	public String editKeywords() {

		saveSession(); // save form to session

		keywordForm = new KeywordForm(getSessionDataElement().getDataElement());
		setCurrentPage(CoreConstants.KEYWORDS);

		return PortalConstants.ACTION_EDITKEYWORDS;
	}

	/**
	 * Action for tabbing the page to edit value range
	 * 
	 * @return String
	 */
	public String editValueRange() {

		saveSession(); // save form to session

		valueRangeForm = new ValueRangeForm(staticManager, getSessionDataElement().getDataElement());
		setCurrentPage(CoreConstants.VALUERANGE);

		return PortalConstants.ACTION_EDITVALUERANGE;
	}

	/**
	 * Action for tabbing the page to step 4
	 * 
	 * @return String
	 */
	public String editStandardDetails() {

		saveSession(); // save form to session

		this.externalIdMap = new HashMap<String, String>();
		if (getDataElementSession().getExternalIdSet() != null) {
			for (ExternalId externalId : getDataElementSession().getExternalIdSet()) {
				externalIdMap.put(externalId.getSchema().getName(), externalId.getValue());
			}
		}

		setCurrentPage(CoreConstants.STANDARDDETAILS);

		return PortalConstants.ACTION_EDITSTANDARDDETAILS;
	}

	/**
	 * Action for tabbing the page to review
	 * 
	 * @return String
	 * @throws UnsupportedEncodingException
	 */
	public String review() throws UnsupportedEncodingException {
		
		// clear severity record in session to make sure we have the right
		// changes
		getSessionDataElement().clearSeverityRecords();
				
		// We will mark this true later if there is a problem with the form structure's short name
		shortNameChangeRequired = false;
		
		// will set this to true in the catch block
		rulesEngineException = false;

		saveSession(); // save form to session
		setCurrentPage("review");

		// Calls Rule Engine to evaluate change severity
		DataElement currentDE = this.getDataElementSession();
		currentDataElement.setCreatedBy(getDisplayNameByUsername(currentDataElement.getCreatedBy()));

		// required field validation attributes tab
		if (currentDE.getPopulation() == null) {
			addActionError("Please fill in required fields in  attributes tab.");
		}

		// required field validation details tab
		if (currentDE.getSubmittingOrgName() == null) {
			addActionError("Please fill in required fields in  details tab.");
		}
		cleanUpExternalIdSet(currentDE.getExternalIdSet());
		if (currentDE != null && currentDE.getId() != null) {
			DataElement originalDE = dictionaryManager.getDataElement(currentDE.getId());

			if (originalDE != null && currentDE != null) {
				// get the list of changes from the rules engine
				List<SeverityRecord> severityRecords = new ArrayList<SeverityRecord>();
				try {
					severityRecords = dictionaryService.evaluateDataElementChangeSeverity(originalDE, currentDE);
					getSessionDataElement().setSeverityRecords(severityRecords);
					getSessionDataElement().setOriginalDEId(originalDE.getId());
				} catch (RulesEngineException e) {
					e.printStackTrace();
					rulesEngineException = true;
				}
				changeStrings = new ArrayList<String>();

				// for every severity record, see if its the highest and convert it into a string.
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

					// The short names have to be different if the change type is new
					if (SeverityLevel.NEW.equals(highestSeverityLevel)
							&& currentDE.getName().equals(originalDE.getName())) {
						// if the short name is still the same as the DB short name, the user has to change it if the
						// severity is high
						// if the short name has been changed, the user can save the DE with the new changes and a new
						// DE should be created.
						if (originalDE.getName().equals(currentDE.getName())) {
							shortNameChangeRequired = true;
						}
					}

					dataElementChangeSeverity = highestSeverityLevel;
				}
				getSessionDataElement().setDataElementChangeSeverity(dataElementChangeSeverity);
			}
		}

		return PortalConstants.ACTION_REVIEW;
	}

	public String removeDataElement()
			throws MalformedURLException, UnsupportedEncodingException, UserPermissionException, UserAccessDeniedException {

		String deName = getRequest().getParameter("dataElementName");
		// used by at least getIsDataElementAdmin()
		currentDataElement = dictionaryManager.getLatestDataElementByName(deName);

		if (!((getIsDataElementAdmin() && !getIsPublished()) || getIsDictionaryAdmin())) {
			throw new UserPermissionException(ServiceConstants.ADMIN_ACCESS_DENIED);
		}

		// This function handles making sure data will not be corrupted.
		dictionaryManager.deleteDataElement(deName);
		getSessionDataElement().clear();
		return PortalConstants.ACTION_INPUT;
	}

	/**
	 * This action is performed as an ajax call when creating a new keyword A new keyword object is created here and
	 * stored in newKeyword where the json call retrieves it.
	 * 
	 * @return a string directing struts to a json object
	 */
	public String createKeyword() {

		newKeyword = new Keyword();
		newKeyword.setKeyword(keywordSearchKey);
		newKeyword.setCount(0L);
		// Set temporary id and update session id. Add element to session newKeyword list
		getSessionDataElement().setNewKeywordId(getSessionDataElement().getKeywordId() - 1);
		getSessionDataElement().addNewKeyword(newKeyword);

		return PortalConstants.ACTION_ADDKEYWORD;
	}

	/**
	 * This action is performed as an ajax call when creating a new keyword A new keyword object is created here and
	 * stored in newKeyword where the json call retrieves it.
	 * 
	 * @return a string directing struts to a json object
	 */
	public String createLabel() {

		newLabel = new Keyword();
		newLabel.setKeyword(labelSearchKey);
		newLabel.setCount(0L);
		// Set temporary id and update session id. Add element to session newKeyword list
		getSessionDataElement().setNewLabelId(getSessionDataElement().getLabelId() - 1);
		getSessionDataElement().addNewLabel(newLabel);

		return PortalConstants.ACTION_ADDKEYWORD;
	}

	public String create() throws MalformedURLException, UnsupportedEncodingException {

		getSessionDataElement().clear();
		
		currentPage = "details";
		deletable = false;
		currentDataElement = new DataElement();
		currentDataElement.setDateCreated(new Date());
		currentDataElement.setCreatedBy(getAccount().getDisplayName());
		currentDataElement.setVersion("1.0");

		getSessionDataElement().setDataElement(currentDataElement);

		if (dataElementForm == null) {
			dataElementForm = new DataElementForm(currentDataElement);
		}

		return PortalConstants.ACTION_INPUT;
	}

	@SuppressWarnings("unchecked")
	public String submit() throws HttpException, IOException, UserAccessDeniedException {

		saveSession();
		currentDataElement = getSessionDataElement().getDataElement();

		// If data element is in awaiting status and has been edited change it back to draft. also need to remove it
		// from the public group
		if (DataElementStatus.AWAITING.equals(currentDataElement.getStatus())) {
			currentDataElement.setStatus(DataElementStatus.DRAFT);
		}

		// if the data element is a new data element assign boolean to true
		boolean isNewDataElement = currentDataElement.getId() == null ? true : false;
		Long oldId = null;

		// get the data element ID before a new one is generated if this is a versioned data element
		if (!isNewDataElement) {
			oldId = currentDataElement.getId();
		}

		List<String> errors = new ArrayList<String>();
		List<String> warnings = new ArrayList<String>();

		String[] twoProxyTickets =
				PortalUtils.getMultipleProxyTickets(modulesConstants.getModulesAccountURL(getDiseaseId()), 2);

		currentDataElement = dictionaryManager.saveDataElement(getAccount(), currentDataElement, errors, warnings,
				getDataElementChangeSeverity(), twoProxyTickets, null, false);
		getSessionDataElement().setDataElement(currentDataElement);
		
		// If we just created a new de though versioning, then save it as public.
		if (DataElementStatus.PUBLISHED.equals(currentDataElement.getStatus())
				&& (SeverityLevel.MAJOR.equals(getDataElementChangeSeverity())
						|| SeverityLevel.MINOR.equals(getDataElementChangeSeverity()))) {
			addToPublicGroup(currentDataElement);
		}

		// Save the audit note (if there is a note or not is controlled by jsp logic)
		if (auditNote != null && !auditNote.isEmpty()) {

			List<SeverityRecord> severityRecords = getSessionDataElement().getSeverityRecords();
			SeverityLevel highestSeverityLevel = getSessionDataElement().getDataElementChangeSeverity();

			
			DictionaryEventLog eventLog = new DictionaryEventLog(getDiseaseId(),getAccount().getUserId());
			Long originalEntityId = dictionaryService.getOriginalDataElementIdByName(currentDataElement.getName());
			eventLog.setDataElementID(originalEntityId);
			
			dictionaryService.saveChangeHistoryEventlog(eventLog,severityRecords, highestSeverityLevel,EntityType.DATA_ELEMENT,oldId,currentDataElement.getId(),auditNote);
		}

		this.checkDataElement(currentDataElement);

		// AP Data Elements are shared in the public group. If this element is draft (possibly from AP) then make sure
		// it is not in the public group
		if (DataElementStatus.DRAFT.equals(currentDataElement.getStatus())) {
			removeFromPublicGroup(currentDataElement);
		}
		// if the data element is not a draft update the map element references to point to the new DE version
		else if (!DataElementStatus.AWAITING.equals(currentDataElement.getStatus())) {
			dictionaryManager.updateFormStructuresWithLatestDataElement(currentDataElement.getName(), currentDataElement.getId());
		}

		attachedDataStructures = dictionaryManager.getAttachedDataStructure(currentDataElement.getName(),
				currentDataElement.getVersion());

		if (isNewDataElement) {
			return PortalConstants.ACTION_CONFIRM;
		}
		return PortalConstants.ACTION_VIEW;
	}

	/**
	 * This method updates dataElement in session with new data from form
	 */
	public void saveSession() {

		currentDataElement = getSessionDataElement().getDataElement();

		if (dataElementForm != null) {
			dataElementForm.copyToDataElement(currentDataElement);
		}

		if (valueRangeForm != null) {
			valueRangeForm.copyToDataElement(currentDataElement);
		}

		if (keywordForm != null) {
			keywordForm.copyToDataElement(currentDataElement);
		}

		if (externalIdMap != null) {
			Set<ExternalId> externalIdSet = new HashSet<ExternalId>();
			for (String key : externalIdMap.keySet()) {
				externalIdSet.add(new ExternalId(staticManager.getSchemaByName(key), externalIdMap.get(key)));
			}
			getDataElementSession().setExternalIdSet(externalIdSet);
		}

	}

	/**
	 * Changes the publication state
	 * 
	 * @param publicationId
	 * @return
	 */
	public String viewPublication() {

		return PortalConstants.ACTION_PUBLICATION;
	}

	/**
	 * Changes the publication state
	 * 
	 * @param publicationId
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 */
	public String changePublication() throws HttpException, IOException, UserAccessDeniedException {

		currentDataElement = getSessionDataElement().getDataElement();
		
		DataElementStatus oldStatus = currentDataElement.getStatus();

		for (DataElementStatus status : DataElementStatus.values()) {
			if (status.getId().equals(publicationId)) {
				// The function DictionaryManager.editDataElementStatus can make up to 3 calls to the account web
				// service
				String[] threeProxyTickets =
						PortalUtils.getMultipleProxyTickets(modulesConstants.getModulesAccountURL(getDiseaseId()), 3);
				
				//get documentation for eventlog
				Set<DictionarySupportingDocumentation> currentDocSet=getSessionDictionaryStatusChange().getDEEventLogDocumentation();
				
				//get comment and supporting documentation 
				//this needs to be done before data element status is updated to make sure the supporting document is also saved if there is a status change
				//repositoryManager.saveUserFile takes time and if a user refreshes in the middle, the supporting doc might be lost 
				String comment = getSessionDictionaryStatusChange().getStatusReason();	
			
				
				DictionaryEventLog eventLog = new DictionaryEventLog(getDiseaseId(),getAccount().getUserId());
				Long originalEntityId = dictionaryService.getOriginalDataElementIdByName(currentDataElement.getName());
				eventLog.setDataElementID(originalEntityId);
				
				if(currentDocSet!=null && !currentDocSet.isEmpty()){
					for (DictionarySupportingDocumentation sd : currentDocSet) {
						if (sd.getUserFile() != null) 
							sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));
						
						sd.setDictionaryEventLog(eventLog);
					}
				}
				eventLog.setSupportingDocumentationSet(currentDocSet);

				dictionaryManager.editDataElementStatus(getAccount(), currentDataElement, status, threeProxyTickets);

				if (DataElementStatus.PUBLISHED.equals(status) || DataElementStatus.AWAITING.equals(status)) {
					addToPublicGroup(currentDataElement);
					
					//save the status change if data element is published
					if(DataElementStatus.PUBLISHED.equals(status)){
						
						dictionaryManager.saveEventLog(eventLog, oldStatus.getId().toString(), DataElementStatus.PUBLISHED.getId().toString(), comment,EventType.STATUS_CHANGE_TO_PUBLISHED);
					}
				} else if (DataElementStatus.DRAFT.equals(status)) {
					removeFromPublicGroup(currentDataElement);
				}
			}
		}
		
		getSessionDictionaryStatusChange().clear();
		
		return PortalConstants.ACTION_PUBLICATION;
	}

	/**
	 * Data element short names are provided by checkboxes. The list is looped through and every data element is
	 * published. The list returns to the search page.
	 * 
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	public String bulkPublish() throws MalformedURLException, UnsupportedEncodingException, HttpException, IOException, UserAccessDeniedException {

		String dataElementNameParam = getRequest().getParameter(PortalConstants.DATAELEMENT_NAMES);

		// If there are no dataElements selected then simply return to search page.
		if (dataElementNameParam == "" || !getInAdmin()) {
			return PortalConstants.ACTION_LIST;
		}

		Set<String> dataElementNames =
				new HashSet<String>(Arrays.asList(dataElementNameParam.split(PortalConstants.COMMA)));
		List<DataElement> dataElements = dictionaryManager.getLatestDataElementByNameList(dataElementNames);

		for (DataElement de : dataElements) {
			// Skip if data element is already published
			if (DataElementStatus.PUBLISHED.equals(de.getStatus())) {
				continue;
			}
			
			//get documentation for eventlog
			Set<DictionarySupportingDocumentation> currentDocSet=getSessionDictionaryStatusChange().getDEEventLogDocumentation();
			
			//get comment and supporting documentation 
			//this needs to be done before data element status is updated to make sure the supporting document is also saved if there is a status change.
			//repositoryManager.saveUserFile takes time and if a user refreshes in the middle, the supporting doc might be lost 
			String comment = getSessionDictionaryStatusChange().getStatusReason();	
			
		
			DictionaryEventLog eventLog = new DictionaryEventLog(getDiseaseId(),getAccount().getUserId());
			Long originalEntityId = dictionaryService.getOriginalDataElementIdByName(de.getName());
			eventLog.setDataElementID(originalEntityId);
			
			if(currentDocSet!=null && !currentDocSet.isEmpty()){
				for (DictionarySupportingDocumentation sd : currentDocSet) {
					if (sd.getUserFile() != null) 
						sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));
					
					sd.setDictionaryEventLog(eventLog);
				}
			}
			eventLog.setSupportingDocumentationSet(currentDocSet);

			String[] threeProxyTickets =
					PortalUtils.getMultipleProxyTickets(modulesConstants.getModulesAccountURL(getDiseaseId()), 3);
			dictionaryManager.editDataElementStatus(getAccount(), de, DataElementStatus.PUBLISHED, threeProxyTickets);
			
			addToPublicGroup(de);
			
			dictionaryManager.saveEventLog(eventLog, DataElementStatus.AWAITING.getId().toString(), DataElementStatus.PUBLISHED.getId().toString(), comment,EventType.REQUESTED_PUBLISH_APPROVED);
			
		}
  
		getSessionDictionaryStatusChange().clear();
		
		return PortalConstants.ACTION_LIST;
	}

	/**
	 * Data element short names are provided by checkboxes. The list is looped through and every data element is
	 * published. The list returns to the search page.
	 * 
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	public String bulkAP() throws MalformedURLException, UnsupportedEncodingException, HttpException, IOException, UserAccessDeniedException {

		String dataElementNameParam = getRequest().getParameter(PortalConstants.DATAELEMENT_NAMES);

		// If there are no dataElements selected then simply return to search page.
		if (dataElementNameParam == "" || !getInAdmin()) {
			return PortalConstants.ACTION_LIST;
		}

		Set<String> dataElementNames =
				new HashSet<String>(Arrays.asList(dataElementNameParam.split(PortalConstants.COMMA)));
		List<DataElement> dataElements = dictionaryManager.getLatestDataElementByNameList(dataElementNames);

		for (DataElement de : dataElements) {
			// Skip data element if it is not in draft (only status that can be set to AP)
			if (!DataElementStatus.DRAFT.equals(de.getStatus())) {
				continue;
			}

			String[] threeProxyTickets =
					PortalUtils.getMultipleProxyTickets(modulesConstants.getModulesAccountURL(getDiseaseId()), 3);
			dictionaryManager.editDataElementStatus(getAccount(), de, DataElementStatus.AWAITING, threeProxyTickets);
			
			addToPublicGroup(de);

		}

		return PortalConstants.ACTION_LIST;
	}

	/**
	 * Approves the publication of the Data Element
	 * 
	 * @throws MessagingException
	 * @throws IOException
	 * @throws HttpException
	 */
	public String approve() throws MessagingException, HttpException, IOException, UserAccessDeniedException {

		currentDataElement = getSessionDataElement().getDataElement();

		Account account = webServiceManager.getEntityOwnerAccountRestful(getAccount(), currentDataElement.getId(),
				EntityType.DATA_ELEMENT);

		String ownerName = account.getUser().getFullName();

		// Checks the data element via database to confirm that it is awaiting publication. Too avoid concurrent
		// modification of data element status
		if (DataElementStatus.AWAITING
				.equals(dictionaryManager.getDataElement(currentDataElement.getId()).getStatus())) {
			String[] threeProxyTickets =
					PortalUtils.getMultipleProxyTickets(modulesConstants.getModulesAccountURL(getDiseaseId()), 3);
			
			//get  status change comment
			String comment = getSessionDictionaryStatusChange().getStatusReason();
			
		
			DictionaryEventLog eventLog = new DictionaryEventLog(getDiseaseId(),getAccount().getUserId());
			Long originalEntityId = dictionaryService.getOriginalDataElementIdByName(currentDataElement.getName());
			eventLog.setDataElementID(originalEntityId);
			
			//get documentation for eventlog
			Set<DictionarySupportingDocumentation> currentDocSet=getSessionDictionaryStatusChange().getDEEventLogDocumentation();
			
			if(currentDocSet!=null && !currentDocSet.isEmpty()){
				for (DictionarySupportingDocumentation sd : currentDocSet) {
					if (sd.getUserFile() != null) 
						sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));
					
					sd.setDictionaryEventLog(eventLog);
				}
			}
			eventLog.setSupportingDocumentationSet(currentDocSet);
			

			dictionaryManager.editDataElementStatus(getAccount(), currentDataElement, DataElementStatus.PUBLISHED,
					threeProxyTickets);

			addToPublicGroup(currentDataElement);
				
			dictionaryManager.saveEventLog(eventLog, DataElementStatus.AWAITING.getId().toString(), DataElementStatus.PUBLISHED.getId().toString(), comment,EventType.REQUESTED_PUBLISH_APPROVED);
			
			reason = getSessionDictionaryStatusChange().getApproveReason();

			boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();
			// construct subject line and message text to send through email
			if (!isLocalEnv) {
				String subject = this.getText(
						PortalConstants.MAIL_RESOURCE_ACCEPTED_DATAELEMENT + PortalConstants.MAIL_RESOURCE_SUBJECT,
						Arrays.asList(currentDataElement.getName()));

				String messageText =
						this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
								Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
										modulesConstants.getModulesOrgPhone(getDiseaseId()), ownerName, reason,
										currentDataElement.getName()))
								+ this.getText(
										PortalConstants.MAIL_RESOURCE_ACCEPTED_DATAELEMENT
												+ PortalConstants.MAIL_RESOURCE_BODY,
										Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
												modulesConstants.getModulesOrgPhone(getDiseaseId()), ownerName, reason,
												currentDataElement.getName()))
								+ this.getText(
										PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
										Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
												modulesConstants.getModulesOrgPhone(getDiseaseId()), ownerName, reason,
												currentDataElement.getName()));

				String from = modulesConstants.getModulesOrgEmail(getDiseaseId());

				dictionaryManager.sendEmail(account, subject, messageText, from);
			} else {
				logger.info("[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the approval of DE: "
						+ currentDataElement.getName());
			}
		}
		
		getSessionDictionaryStatusChange().clear();
		
		return PortalConstants.ACTION_REDIRECT_TO_VIEW;
	}

	/**
	 * Denies the publication of the Data Element
	 * 
	 * @throws MessagingException
	 * @throws IOException
	 * @throws HttpException
	 */
	public String deny() throws MessagingException, HttpException, IOException, UserAccessDeniedException {

		currentDataElement = getSessionDataElement().getDataElement();
		Account account = webServiceManager.getEntityOwnerAccountRestful(getAccount(), currentDataElement.getId(),
				EntityType.DATA_ELEMENT);

		String ownerName = account.getUser().getFullName();

		String[] threeProxyTickets =
				PortalUtils.getMultipleProxyTickets(modulesConstants.getModulesAccountURL(getDiseaseId()), 3);
		
		//get  status change comment
		String comment = getSessionDictionaryStatusChange().getStatusReason();
		
		
		DictionaryEventLog eventLog = new DictionaryEventLog(getDiseaseId(),getAccount().getUserId());
		Long originalEntityId = dictionaryService.getOriginalDataElementIdByName(currentDataElement.getName());
		eventLog.setDataElementID(originalEntityId);
		
		//get documentation for eventlog
		Set<DictionarySupportingDocumentation> currentDocSet=getSessionDictionaryStatusChange().getDEEventLogDocumentation();
		
		if(currentDocSet!=null && !currentDocSet.isEmpty()){
			for (DictionarySupportingDocumentation sd : currentDocSet) {
				if (sd.getUserFile() != null) 
					sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));
				
				sd.setDictionaryEventLog(eventLog);
			}
		}
		eventLog.setSupportingDocumentationSet(currentDocSet);
		

		dictionaryManager.editDataElementStatus(getAccount(), currentDataElement, DataElementStatus.DRAFT,
				threeProxyTickets);

		removeFromPublicGroup(currentDataElement);
		
		dictionaryManager.saveEventLog(eventLog, DataElementStatus.AWAITING.getId().toString(), DataElementStatus.DRAFT.getId().toString(), comment,EventType.REQUESTED_PUBLISH_REJECTED);
		
		reason = getSessionDictionaryStatusChange().getApproveReason();

		boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();

		// construct subject line and message text to send through email
		if (!isLocalEnv) {
			String subject = this.getText(
					PortalConstants.MAIL_RESOURCE_REJECTED_DATAELEMENT + PortalConstants.MAIL_RESOURCE_SUBJECT,
					Arrays.asList(currentDataElement.getName()));

			String messageText =
					this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
									modulesConstants.getModulesOrgPhone(getDiseaseId()), ownerName, reason,
									currentDataElement.getName()))
							+ this.getText(
									PortalConstants.MAIL_RESOURCE_REJECTED_DATAELEMENT
											+ PortalConstants.MAIL_RESOURCE_BODY,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgPhone(getDiseaseId()), ownerName, reason,
											currentDataElement.getName()))
							+ this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgPhone(getDiseaseId()), ownerName, reason,
											currentDataElement.getName()));

			String from = modulesConstants.getModulesOrgEmail(getDiseaseId());

			dictionaryManager.sendEmail(account, subject, messageText, from);
		} else {
			logger.info("[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the denial of DE: "
					+ currentDataElement.getName());
		}
		
		getSessionDictionaryStatusChange().clear();
		
		return PortalConstants.ACTION_REDIRECT_TO_VIEW;
	}

	/**
	 * Returns streamresult of the current data element status
	 * 
	 * @return
	 */
	public StreamResult updateStatus() {

		return new StreamResult(
				new ByteArrayInputStream(getSessionDataElement().getDataElement().getStatus().getName().getBytes()));
	}

	/**
	 * Adds the data element to the permission group
	 * 
	 * @param dataElement
	 * @throws IOException
	 * @throws HttpException
	 */
	public void addToPublicGroup(DataElement dataElement) throws HttpException, IOException {

		webServiceManager.registerEntityToPermissionGroup(getAccount(), ServiceConstants.PUBLIC_DATA_ELEMENTS,
				EntityType.DATA_ELEMENT, dataElement.getId(), PermissionType.READ);
	}

	public void removeFromPublicGroup(DataElement dataElement) throws HttpException, IOException {

		webServiceManager.unregisterEntityToPermissionGroup(ServiceConstants.PUBLIC_DATA_ELEMENTS,
				EntityType.DATA_ELEMENT, dataElement.getId(), PermissionType.READ);
	}

	public Map<String, String> getExternalIdMap() {

		return externalIdMap;
	}

	public void setExternalIdMap(Map<String, String> externalIdMap) {

		this.externalIdMap = externalIdMap;
	}

	private void cleanUpExternalIdSet(Set<ExternalId> listOfExternalIds) {

		Iterator<ExternalId> listItr = listOfExternalIds.iterator();
		while (listItr.hasNext()) {
			ExternalId externalId = (ExternalId) listItr.next();
			if (externalId.getValue().equals("")) {
				listItr.remove();
			}
		}
	}


	public StreamResult editDocumentDataTableSave() throws MalformedURLException, UnsupportedEncodingException {

		currentDataElement = getSessionDataElement().getDataElement();
		saveSession();

		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	public String getModulesDDTURL() {
		return modulesDDTURL;
	}

	public void setModulesDDTURL(String modulesDDTURL) {
		this.modulesDDTURL = modulesDDTURL;
	}

	public Set<DictionaryEventLog> getMinorMajorChangeLogList() {
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

	public boolean getShowDataElementDetail() {
		return showDataElementDetail;
	}

	public void setShowDataElementDetail(boolean showDataElementDetail) {
		this.showDataElementDetail = showDataElementDetail;
	}

	public List<FormStructure> getAllAttachedDataStructures() throws MalformedURLException, UnsupportedEncodingException {

		return attachedDataStructures;
	}
	
	/*-----------------------------Data element status change event log------------------------------------*/

	public Set<Long> getAttachedFSIds(List<FormStructure> attachedFs){
		
		Set<Long> ids = new HashSet<Long>();
		
		for(FormStructure fs:attachedFs){
			ids.add(fs.getId());
		}
		
		return ids;
	}
	
	public String dataElementStatusChange(){
		
	   getSessionDictionaryStatusChange().clear();		
	   getSessionDictionaryStatusChange().setNewDEStatusType(DataElementStatus.getById(publicationId));
	   
	   if(bulkStatusChange){
	   	   
		   Set<String> dataElementNames =
					new HashSet<String>(Arrays.asList(checkedElementIds.split(PortalConstants.COMMA)));
		   
		   List <DataElement> dataElements = dictionaryManager.getLatestDataElementByNameList(dataElementNames);
		   
		   if(!dataElements.isEmpty()){
			   getSessionDictionaryStatusChange().setCurrentDEStatusType(dataElements.get(0).getStatus());
		   }
		   
		   getSessionDictionaryStatusChange().setBulkDEs(dataElements);
		   
		   return PortalConstants.ACTION_BULK_STATUS_CHANGE;
	   }
		
		return PortalConstants.ACTION_STATUS_CHANGE;
	}
	
	public StreamResult submitDEStatusChange() throws SQLException, Exception{
			
		getSessionDictionaryStatusChange().setApproveReason(reason);
		getSessionDictionaryStatusChange().setStatusReason(statusReason);
		
		
		 return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}
	
	public String refreshEventLogTable () throws UnsupportedEncodingException {
		
		Long originalEntityId = dictionaryService.getOriginalDataElementIdByName(getSessionDataElement().getDataElement().getName());		
		eventLogList = dictionaryService.getAllDEEventLogs(originalEntityId);
		
		updateEventLogListWithUsers(eventLogList);
		
		return PortalConstants.ACTION_EVENT_LOG_TABLE;
	}
	
	public String refreshSupportDoc () {
		
		return PortalConstants.ACTION_SUPPORT_DOC_TABLE;
	}
	
	public String getStatusReason() {

		return statusReason;
	}

	public void setStatusReason(String statusReason) {

		this.statusReason = statusReason;
	}
	
	public boolean getIsRequestedStatusChange() {
		
		currentDataElement = getSessionDataElement().getDataElement();
		
		if(DataElementStatus.AWAITING== currentDataElement.getStatus()){
			return true;
		}
		return false;
	}
	
	public SessionDictionaryStatusChange getSessionDictionaryStatusChange(){
		
		if(sessionDictionaryStatusChange==null){
			sessionDictionaryStatusChange = new SessionDictionaryStatusChange();
		}
		
		return sessionDictionaryStatusChange;
	}
	
	public String getNewStatus(){
	
		return getSessionDictionaryStatusChange().getNewDEStatusType().getName();
	}
	
	public Long getCurrentId() {
		return currentId;
	}

	public void setCurrentId(Long currentId) {
		this.currentId = currentId;
	}

	public Set<DictionaryEventLog> getEventLogList() {
		return eventLogList;
	}

	public void setEventLogList(Set<DictionaryEventLog> eventLogList) {
		this.eventLogList = eventLogList;
	}

	public boolean isInDataElementPage() {
		return inDataElementPage;
	}

	public void setInDataElementPage(boolean inDataElementPage) {
		this.inDataElementPage = inDataElementPage;
	}

	public Long getPublicationId() {
		return publicationId;
	}
	public boolean getIsBulkStatusChange() {
		return bulkStatusChange;
	}

	public void setBulkStatusChange(boolean bulkStatusChange) {
		this.bulkStatusChange = bulkStatusChange;
	}
	
	public String getCurrentStatus(){
		return getSessionDictionaryStatusChange().getCurrentDEStatusType().getName();
	}
	
	public List<DataElement> getDataElementList() {
		return dataElementList;
	}

	public void setDataElementList(List<DataElement> dataElementList) {
		this.dataElementList = dataElementList;
	}
	
	public String listBulkDEs(){
		
		dataElementList = getSessionDictionaryStatusChange().getBulkDEs();
		
		return PortalConstants.LIST_BULK_DES;
	}
	
	public String getCheckedElementIds() {
		return checkedElementIds;
	}

	public void setCheckedElementIds(String checkedElementIds) {
		this.checkedElementIds = checkedElementIds;
	}
	
	/*-----------------------------End  of data element status change event log------------------------------------*/	
	/*------documentation upload, 
	 * kept this part here because document upload is in the same page as dataelement status change---------------*/
	public int getEventLogSupportDocSize(){
	
		int size = 0;
		if(getEventLogSupportDocList()!=null)
			 size = getEventLogSupportDocList().size();
			
		return size;
		
	}
	
	public Boolean getIsDocumentLimitNR(){
		if(getEventLogSupportDocSize()<getDocumentationLimit())
			return true;
		return false;
	}
	
	public int getDocumentationLimit() {
		
		return PortalConstants.MAXIMUM_DOCUMENT_UPLOAD;
	
	}
	
	public String getActionName() {
	    return PortalConstants.ACTION_DE_EVENT_LOG_DOC;
	}
	
	public Set <DictionarySupportingDocumentation> getEventLogSupportDocList() { 
		
		 Set <DictionarySupportingDocumentation> supportDocListSession=getSessionDictionaryStatusChange().getDEEventLogDocumentation();
					
		return supportDocListSession;
			
	}
	
	/*-----------------------------End  of documentation upload------------------------------------*/
	
	
	/**
	 * @param eventLogList
	 * @throws UnsupportedEncodingException
	 * 
	 * Since dictionary db doesn't have account table, this method
	 * makes rest call per disease to get user details and update
	 * eventLog with the user.
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
	
	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/dataElementAction!getDataElementsList.action
	public String getDataElementsList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			dataElementList = getSessionDictionaryStatusChange().getBulkDEs();
			ArrayList<DataElement> outputList =
					new ArrayList<DataElement>(dataElementList);
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
	
	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/dataElementAction!getSupportingDocumentationList.action
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
	
	
	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/dataElementAction!getSupportingDocumentationList.action
	public String getSupportingDocumentationList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<SupportingDocumentationInterface> outputList =
					new ArrayList<SupportingDocumentationInterface>(getSessionDataElement().getDataElement().getSupportingDocumentationSet());
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
	
	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/dataElementAction!getHistoryLogList.action
	public String getHistoryLogList() throws UnsupportedEncodingException {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			Long originalEntityId = dictionaryService.getOriginalDataElementIdByName(getSessionDataElement().getDataElement().getName());		
			eventLogList = dictionaryService.getAllDEEventLogs(originalEntityId);
			
			updateEventLogListWithUsers(eventLogList);
			ArrayList<DictionaryEventLog> outputList =
					new ArrayList<DictionaryEventLog>(getMinorMajorChangeLogList());
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
	
	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/dataElementAction!getDictionaryEventLogList.action
	public String getDictionaryEventLogList() throws UnsupportedEncodingException {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			Long originalEntityId = dictionaryService.getOriginalDataElementIdByName(getSessionDataElement().getDataElement().getName());		
			eventLogList = dictionaryService.getAllDEEventLogs(originalEntityId);
			
			updateEventLogListWithUsers(eventLogList);
			ArrayList<DictionaryEventLog> outputList =
					new ArrayList<DictionaryEventLog>(getStatusChangeLogList());
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
