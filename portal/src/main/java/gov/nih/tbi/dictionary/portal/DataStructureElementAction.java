package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.dictionary.model.DataStructureForm;
import gov.nih.tbi.dictionary.model.RepeatableGroupForm;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.portal.PortalUtils;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.struts2.result.StreamResult;

public class DataStructureElementAction extends DataElementSearchAction {

	/**
	 * Supports communication between struts and the server
	 * 
	 * @author mvalei
	 */
	private static final long serialVersionUID = -3437401823167431396L;

	/******************************************************************************************************/
	FormStructure currentDataStructure;

	DataStructureForm dataStructureForm;

	MapElement mapElement;

	DataElement currentDataElement;

	RepeatableGroup repeatableGroup;

	RepeatableGroupForm repeatableGroupForm;

	Boolean editMode;

	String formType;

	String groupElementId;

	Long statusId;

	/******************************************************************************************************/

	public List<String> getSessionRepeatableGroupNames() {

		List<String> out = new ArrayList<String>();
		if (getSessionDataStructure() != null) {
			if (getSessionDataStructure().getDataStructure() != null) {
				Set<RepeatableGroup> rgList = (getSessionDataStructure().getDataStructure()).getRepeatableGroups();

				for (RepeatableGroup rg : rgList) {
					out.add(rg.getName());
				}
			}
		}

		return out;
	}

	public Boolean getMappedList() {

		return true;
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

		if (repeatableGroup == null) {
			repeatableGroup =
					dictionaryManager
							.findRepeatableGroupInList(Long.valueOf(groupElementId), getCurrentDataStructure());
		}
		return repeatableGroup;
	}

	public void setRepeatableGroup(RepeatableGroup repeatableGroup) {

		this.repeatableGroup = repeatableGroup;
	}

	public FormStructure getCurrentDataStructure() {

		if (currentDataStructure == null) {
			currentDataStructure = getSessionDataStructure().getDataStructure();
		}

		return currentDataStructure;
	}

	public DataElement getCurrentDataElement() {


		return currentDataElement;
	}

	public DataStructureForm getDataStructureForm() {

		return dataStructureForm;
	}

	public void setDataStructureForm(DataStructureForm dataStructureForm) {

		this.dataStructureForm = dataStructureForm;
	}

	public RepeatableGroupForm getRepeatableGroupForm() {

		return repeatableGroupForm;
	}

	public void setRepeatableGroupForm(RepeatableGroupForm repeatableGroupForm) {

		this.repeatableGroupForm = repeatableGroupForm;
	}

	public Long getGroupElementId() {

		return getSessionDataStructure().getRepeatableGroup().getId();
	}

	public void setGroupElementId(String groupElementId) {

		this.groupElementId = groupElementId;
	}

	public String getFormType() {

		if (getSessionDataStructure() == null || getSessionDataStructure().getDataStructure() == null
				|| getSessionDataStructure().getDataStructure().getId() == null)

		{
			return PortalConstants.FORMTYPE_CREATE;
		} else {
			return PortalConstants.FORMTYPE_EDIT;
		}
	}

	public Boolean getIsDataStructureAdmin() {

		// return dictionaryManager.isDataStructureAdmin(getUser(), currentDataStructure);
		// TODO:Webservice
		return dictionaryManager.hasRole(getAccount(), RoleType.ROLE_DICTIONARY_ADMIN);
	}

	public Boolean getEnforceStaticFields() {

		/*
		 * Previously we were checking for version > 1L Updated to check if the version is not version 1.0, ends up
		 * being the same thing
		 */
		if (currentDataStructure.getStatus().equals(StatusType.PUBLISHED)
				|| currentDataStructure.getStatus().equals(StatusType.ARCHIVED)
				|| !(currentDataStructure.getVersion().equals("1.0"))) {
			return true;
		}

		return false;
	}

	/**
	 * This method returns a list of all repeatableTypes for populating form select options.
	 * 
	 * @return List<RepeatableType>
	 */
	public List<RepeatableType> getRepeatableTypes() {

		List<RepeatableType> repeatableTypeList = new ArrayList<RepeatableType>();
		repeatableTypeList.add(RepeatableType.EXACTLY);
		repeatableTypeList.add(RepeatableType.MORETHAN);
		repeatableTypeList.add(RepeatableType.LESSTHAN);

		return repeatableTypeList;
	}

	public Boolean getEditMode() {

		return editMode;
	}

	public void setEditMode(Boolean editMode) {

		this.editMode = editMode;
	}

	public List<RepeatableGroup> getCurrentRepeatableGroups() {

		List<RepeatableGroup> out = new ArrayList<RepeatableGroup>();
		out.addAll((getSessionDataStructure().getDataStructure()).getRepeatableGroups());
		return out;
	}

	/******************************************************************************************************/
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

	public String moveMapElement() {

		String mapElementId = getRequest().getParameter(PortalConstants.MAPELEMENT_ID);
		String rowId = getRequest().getParameter(PortalConstants.MAPELEMENT_ROWID);
		String groupElementId = getRequest().getParameter(PortalConstants.GROUPELEMENT_ID);

		currentDataStructure = getSessionDataStructure().getDataStructure();

		dictionaryManager.moveMapElementInList(Long.valueOf(mapElementId), Integer.valueOf(rowId),
				Long.valueOf(groupElementId), currentDataStructure);

		return PortalConstants.ACTION_ELEMENTS;
	}

	public String moveRepeatableGroup() {

		Long groupId = Long.valueOf(getRequest().getParameter(PortalConstants.GROUPELEMENT_ID));
		Integer position = Integer.valueOf(getRequest().getParameter(PortalConstants.POSITION));
		currentDataStructure = getSessionDataStructure().getDataStructure();

		dictionaryManager.moveGroupInList(groupId, position, currentDataStructure);

		return PortalConstants.ACTION_ELEMENTS;
	}

	public String viewMapElement() throws UserAccessDeniedException{

		String mapElementId = getRequest().getParameter(PortalConstants.MAPELEMENT_ID);
		String groupElementId = getRequest().getParameter(PortalConstants.GROUPELEMENT_ID);

		currentDataStructure = getSessionDataStructure().getDataStructure();

		RepeatableGroup rg =
				dictionaryManager.findRepeatableGroupInList(Long.valueOf(groupElementId), currentDataStructure);
		mapElement = dictionaryManager.findMapElementInList(Long.valueOf(mapElementId), rg);



		String elementName = mapElement.getStructuralDataElement().getName();

		String version = mapElement.getStructuralDataElement().getVersion();

		try {
			currentDataElement =
					dictionaryService.getDataElement(getAccount(), elementName, version,
							PortalUtils.getProxyTicket(modulesConstants.getModulesAccountURL(getDiseaseId())));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UserPermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return PortalConstants.ACTION_VIEW;
	}

	public String removeAllMapElements() {

		String groupElementId = getRequest().getParameter(PortalConstants.GROUPELEMENT_ID);
		currentDataStructure = getSessionDataStructure().getDataStructure();

		RepeatableGroup rg =
				dictionaryManager.findRepeatableGroupInList(Long.valueOf(groupElementId), currentDataStructure);
		rg.getMapElements().clear();

		return PortalConstants.ACTION_ELEMENTS;
	}

	public String removeMapElement() {

		String mapElementId = getRequest().getParameter(PortalConstants.MAPELEMENT_ID);
		String groupElementId = getRequest().getParameter(PortalConstants.GROUPELEMENT_ID);

		currentDataStructure = getSessionDataStructure().getDataStructure();

		RepeatableGroup rg =
				dictionaryManager.findRepeatableGroupInList(Long.valueOf(groupElementId), currentDataStructure);
		MapElement me = dictionaryManager.findMapElementInList(Long.valueOf(mapElementId), rg);
		getSessionDataStructure().setMapElement(me);
		/*
		 * for (MapElement mapElement : currentDataStructure.getDataElements()) { if (mapElement.getCondition() != null
		 * && mapElement.getCondition().getMapElement() != null && me.equals(mapElement.getCondition().getMapElement()))
		 * { mapElement.setCondition(null); } }
		 */

		dictionaryManager.removeMapElement(me, rg);

		return PortalConstants.ACTION_ELEMENTS;
	}

	public String removeRepeatableGroup() {

		String groupElementId = getRequest().getParameter(PortalConstants.GROUPELEMENT_ID);

		currentDataStructure = getSessionDataStructure().getDataStructure();

		dictionaryManager.removeRepeatableGroupFromList(Long.valueOf(groupElementId), currentDataStructure);

		return PortalConstants.ACTION_ELEMENTS;
	}

	public String addDataElements() {

		String dataElementNameParam = getRequest().getParameter(PortalConstants.DATAELEMENT_NAMES);
		String groupElementId = getRequest().getParameter(PortalConstants.GROUPELEMENT_ID);

		// If there are no dataElements to add then simply return
		if (PortalConstants.EMPTY_STRING.equals(dataElementNameParam)) {
			return PortalConstants.ACTION_EDIT;
		}

		currentDataStructure = getSessionDataStructure().getDataStructure();

		String[] dataElementNames = dataElementNameParam.split(PortalConstants.COMMA);

		RepeatableGroup rg =
				dictionaryManager.findRepeatableGroupInList(Long.valueOf(groupElementId), currentDataStructure);

		MapElement me = getSessionDataStructure().getMapElement();
		dictionaryManager.addDataElementsByNames(dataElementNames, rg,
				getSessionDataStructure().getNewMappedElements(), me, currentDataStructure);
		getSessionDataStructure().setMapElement(null);

		// Update the newMappedElements count manually based on the number of elements added
		getSessionDataStructure().setNewMappedElements(
				getSessionDataStructure().getNewMappedElements() - dataElementNames.length);

		return PortalConstants.ACTION_EDIT;
	}


	public StreamResult checkRetiredDataElements() {

		String dataElementNameParam = getRequest().getParameter(PortalConstants.DATAELEMENT_NAMES);

		if (dataElementNameParam == "") {
			return null;
		}

		String[] dataElementNames = dataElementNameParam.split(PortalConstants.COMMA);

		int[] deprecatedRetiredDECount = dictionaryManager.getDeprecatedRetiredDECount(dataElementNames);
		int deprecatedDECount = deprecatedRetiredDECount[0];
		int retiredDECount = deprecatedRetiredDECount[1];

		String result = deprecatedDECount + "|" + retiredDECount;

		return new StreamResult(new ByteArrayInputStream(result.getBytes()));
	}


	public String changeRequiredType() {

		String groupElementId = getRequest().getParameter(PortalConstants.GROUPELEMENT_ID);
		String mapElementId = getRequest().getParameter(PortalConstants.MAPELEMENT_ID);
		String requiredTypeId = getRequest().getParameter(PortalConstants.REQUIREDTYPE_ID);

		currentDataStructure = getSessionDataStructure().getDataStructure();
		RepeatableGroup rg =
				dictionaryManager.findRepeatableGroupInList(Long.valueOf(groupElementId), currentDataStructure);
		MapElement me = dictionaryManager.findMapElementInList(Long.valueOf(mapElementId), rg);
		RequiredType newType = RequiredType.getById(Long.valueOf(requiredTypeId));

		me.setRequiredType(newType);

		return PortalConstants.ACTION_REFRESH;
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
	}

	/****************************************** SUBMIT *************************************************/

	public StreamResult addRepeatableElementGroup() {

		currentDataStructure = getSessionDataStructure().getDataStructure();

		RepeatableGroup rg = new RepeatableGroup();
		if (repeatableGroupForm != null) {
			repeatableGroupForm.adapt(rg, false);

		} else {
			throw new RuntimeException("Action addRepeatableElementGroup fired but repeatableGroupForm is null!");
		}

		// Set a temporary id for the repeatable group
		rg.setId(Long.valueOf(getSessionDataStructure().getNewRepeatableGroups()));
		getSessionDataStructure().setNewRepeatableGroups(getSessionDataStructure().getNewRepeatableGroups() - 1);
		// Assign a position to the repeatable group (Equal to the number of repeatable groups)
		rg.setPosition((currentDataStructure).getRepeatableGroups().size());
		currentDataStructure = dictionaryManager.addRepeatableGroupToList(rg, currentDataStructure);
		return new StreamResult(new ByteArrayInputStream(rg.getId().toString().getBytes()));
	}

	public StreamResult editRepeatableElementGroup() {

		// Get the current repeatable group
		currentDataStructure = getSessionDataStructure().getDataStructure();
		repeatableGroup = getSessionDataStructure().getRepeatableGroup();

		// if the repeatable not null
		if (repeatableGroupForm != null) {
			// take the rgForm and adapt

			repeatableGroupForm.adapt(repeatableGroup, false);
		} else {
			repeatableGroupForm = new RepeatableGroupForm(repeatableGroup);
		}

		currentDataStructure = dictionaryManager.addRepeatableGroupToList(repeatableGroup, currentDataStructure);
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));

	}

	/**
	 * Navigate to the search Elements page to add elements
	 * 
	 * @return
	 */
	public String searchElements() {

		String groupElementId = getRequest().getParameter(PortalConstants.GROUPELEMENT_ID);

		currentDataStructure = getSessionDataStructure().getDataStructure();
		repeatableGroup =
				dictionaryManager.findRepeatableGroupInList(Long.valueOf(groupElementId), currentDataStructure);
		getSessionDataStructure().setRepeatableGroup(repeatableGroup);
		getSessionDataElementList().setMapElements(repeatableGroup.getMapElements());
		// updateFilters(null);

		return PortalConstants.ACTION_SEARCH;
	}

	public String showAddGroupLightbox() {

		return PortalConstants.ACTION_GROUPLIGHTBOX;
	}

	public String showEditGroupLightbox() {

		groupElementId = getRequest().getParameter(PortalConstants.GROUPELEMENT_ID);
		currentDataStructure = getSessionDataStructure().getDataStructure();

		repeatableGroup =
				dictionaryManager.findRepeatableGroupInList(Long.valueOf(groupElementId), currentDataStructure);

		getSessionDataStructure().setRepeatableGroup(repeatableGroup);

		setEditMode(true);
		return PortalConstants.ACTION_GROUPLIGHTBOX;
	}

	public String moveToElements() {

		currentDataStructure = getSessionDataStructure().getDataStructure();

		return PortalConstants.ACTION_REFRESH;
	}

}
