
package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.commons.service.SchemaMappingManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.WebServiceManager;
import gov.nih.tbi.dictionary.model.CsvToDataElement;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;
import gov.nih.tbi.dictionary.service.DictionaryService;
import gov.nih.tbi.dictionary.service.rulesengine.RulesEngineUtils;
import gov.nih.tbi.dictionary.service.rulesengine.model.RulesEngineException;
import gov.nih.tbi.portal.PortalUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.bytecode.opencsv.CSVParser;

/**
 * Action for importing CDEs
 * 
 * @author Francis Chen
 * 
 */
public class ImportDataElementAction extends BaseDictionaryAction {

	private static final long serialVersionUID = 8118778482237264037L;



	static Logger logger = Logger.getLogger(ImportDataElementAction.class);

	@Autowired
	CsvToDataElement csvToDataElement;

	@Autowired
	WebServiceManager webServiceManager;

	@Autowired
	protected SchemaMappingManager schemaMappingManager;

	@Autowired
	protected DictionaryService dictionaryService;

	/******************************************************************************************************/

	private File upload;

	private String uploadContentType;

	private String uploadFileName;

	private String checkedList;

	private List<DataElement> newDataElementList;

	private List<DataElement> alreadyAttachedDataElementList;

	private List<DataElement> existingDataElementList;

	private List<DataElement> errorImportedDataElementList;

	private DataElement currentDataElement;

	private Map<DataElement, Boolean> importedDataElementMap;

	private Map<DataElement, SeverityLevel> overwriteDeSeverityMap;

	private List<String> checkboxList;

	private Set<String> disabledList;

	private boolean changeHistoryRequired;

	private boolean isExistingDataElementChecked;

	private String auditNote;

	/*****************************************************************************************************/

	/**
	 * jsp will call this with a comma delimited list.
	 * 
	 * @param elementNames
	 * @throws Exception
	 */
	public void setCheckboxList(String elementNames) throws Exception {

		if (checkboxList == null) {
			this.checkboxList = new ArrayList<String>();
		}

		// use csv parser to parse the list.
		CSVParser myParser = new CSVParser();
		try {
			String[] elementList = myParser.parseLine(elementNames);
			for (String element : elementList) {
				checkboxList.add(element.trim());
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	public Set<String> getDisabledList() {

		return disabledList;
	}

	public void setDisabledList(Set<String> disabledList) {

		this.disabledList = disabledList;
	}

	public void setUpload(File upload) {

		this.upload = upload;
	}

	public List<DataElement> getAlreadyAttachedDataElementList() {

		return alreadyAttachedDataElementList;
	}

	public void setAlreadyAttachedDataElementList(List<DataElement> alreadyAttachedDataElementList) {

		this.alreadyAttachedDataElementList = alreadyAttachedDataElementList;
	}

	public List<DataElement> getExistingDataElementList() {

		return existingDataElementList;
	}

	public void setExistingDataElementList(List<DataElement> existingDataElementList) {

		this.existingDataElementList = existingDataElementList;
	}

	public void setCheckedList(String checkedList) {

		this.checkedList = checkedList;
	}

	public void setUploadContentType(String uploadContentType) {

		this.uploadContentType = uploadContentType;
	}

	public void setUploadFileName(String uploadFileName) {

		this.uploadFileName = uploadFileName;
	}

	public List<DataElement> getNewDataElementList() {

		return newDataElementList;
	}

	public void setNewDataElementList(List<DataElement> newDataElementList) {

		this.newDataElementList = newDataElementList;
	}

	public List<DataElement> getErrorImportedDataElementList() {

		return errorImportedDataElementList;
	}

	public void setErrorImportedDataElementList(List<DataElement> errorImportedDataElementList) {

		this.errorImportedDataElementList = errorImportedDataElementList;
	}

	public Map<DataElement, Boolean> getImportedDataElementMap() {

		return importedDataElementMap;
	}

	public void setImportedDataElementMap(Map<DataElement, Boolean> importedDataElementMap) {

		this.importedDataElementMap = importedDataElementMap;
	}

	/**
	 * @return the overwriteDeSeverityMap
	 */
	public Map<DataElement, SeverityLevel> getOverwriteDeSeverityMap() {

		return overwriteDeSeverityMap;
	}

	/**
	 * @param overwriteDeSeverityMap the overwriteDeSeverityMap to set
	 */
	public void setOverwriteDeSeverityMap(Map<DataElement, SeverityLevel> overwriteDeSeverityMap) {

		this.overwriteDeSeverityMap = overwriteDeSeverityMap;
	}

	/**
	 * Validation on any data elements potentially about to be imported
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void validateDataElementList(HashMap<String, ArrayList<String>> pvValidateMap)
			throws FileNotFoundException, IOException {

		List<String> newElementNames = new ArrayList<String>();
		errorImportedDataElementList = new ArrayList<DataElement>();
		// Validate each element
		for (DataElement de : newDataElementList) {

			// Validate this data element
			List<String> validateList =
					dictionaryManager.validateImportedDataElement(upload, de, isAdmin(getAccount()));
			String deName = de.getName();

			if (!validateList.isEmpty()) {
				errorImportedDataElementList.add(de); // POSSIBLE NPE
				for (String s : validateList) {
					addActionError(s + deName + ServiceConstants.PERIOD);

					// If there was an error here or a dupicate in the checks
					// below we are going to clear the category
					// of
					// this data element. This will cause the checkbox on the
					// import page to be disabled.
					de.setCategory(null);
				}
			}

			// get the pv validation errors

			if (pvValidateMap.containsKey(deName)) {
				// this means there are pv errors dor this de
				ArrayList<String> pvValidateList = pvValidateMap.get(deName);
				for (String error : pvValidateList) {
					addActionError(error);
					de.setCategory(null);
				}
			}

			// Check for duplicate names within the file
			for (String name : newElementNames) {
				if (deName == null || name == null || name.equalsIgnoreCase(de.getName())) {
					addActionError(ServiceConstants.DUPLICATE_NAME + deName + ServiceConstants.PERIOD);
					de.setCategory(null);
				}
			}
			newElementNames.add(deName);

		}
	}

	/**
	 * Taking the list of of created and validated incoming DE from the spreadsheet, all DE shortnames are then compared
	 * to all preexisting DE to see if any are being overwritten. Those that are not being overwritten are saved in to
	 * the DataElement list, the remaining are then processes to see they can be overwritten or not.
	 * 
	 * If any of the changes that are made have a severity of "NEW" then the user will not be able to overwrite that DE
	 */
	public void checkForOverwriteDe() {

		importedDataElementMap = new HashMap<DataElement, Boolean>();
		overwriteDeSeverityMap = new HashMap<DataElement, SeverityLevel>();
		// ArrayList<DataElement> overwriteDEs = new ArrayList<DataElement>();
		// importedDataElementList = new ArrayList<DataElement>();
		// Taking the list of of created and validated DE from the spreadsheet

		Set<String> deNames = getNameSetFromDataElements(newDataElementList);
		Map<String, DataElement> deMap = dictionaryManager.getLatestDataElementByNameListIntoMap(deNames);

		// check the already existing data element names for duplicates
		for (DataElement ide : newDataElementList) {
			if (deMap.get(ide.getName()) != null) {
				importedDataElementMap.put(ide, true);
				checkIsChangeHistoryRequired(deMap, ide);

			} else {
				importedDataElementMap.put(ide, false);
			}
		}
	}

	/**
	 * Takes in a list of DEs the user wishes to Overwrite and determines whether or not they are able to be
	 * overwritten. First the method checks if the DE's have errors reported from the validation() step. Second the
	 * method checks if the user is allowed to even edit the DE, which requires admin access or DE ownership
	 * 
	 * @param overwriteDEs
	 * @param gdOvrwrtDEs
	 */
	private void filterOverwriteDE() {

		ArrayList<DataElement> gdOvrwrtDEs = new ArrayList<DataElement>();
		Account acct = getAccount();
		// Retrieves a list of ids to DE that the user has permission to access
		Set<Long> ids = dictionaryManager.listUserAccessDEs(acct,
				PortalUtils.getProxyTicket(modulesConstants.getModulesAccountURL(getDiseaseId())));

		Set<String> importedDeNames = getNameSetFromDataElements(importedDataElementMap.keySet());

		// data element map from the database
		Map<String, DataElement> dbDeMap = dictionaryManager.getLatestDataElementByNameListIntoMap(importedDeNames);

		for (DataElement de : importedDataElementMap.keySet()) {
			// If being overwritten the value should be true
			if (importedDataElementMap.get(de)) {
				// If the dataelement previously already had errors during
				// validation they should be automatically
				// ignored
				if (!errorImportedDataElementList.contains(de)) {
					// If the user is not an admin then we will have to check if
					// the if the
					if (!isAdmin(acct)) {

						// From here the ids of overwriting DEs need to be
						// checked to see if they match any of the ids
						// of DE
						// that they have access to. If they have access
						// continue
						DataElement deOver = dbDeMap.get(de.getName());

						if (!ids.contains(deOver.getId())) {
							// If THEY DON'T HAVE ACCESS CONSIDER THEM TRYING TO
							// REWRITING A PREEXISITING ELEMENT THAT
							// THEY
							// DONT
							// PERMISSION TO. FAIL

							de.setCategory(null);
							importedDataElementMap.put(de, false);
							// This was a snippet from the original validate
							// method that i took and repurposed it, when
							// users don't have
							// the privilige to overwrite the system will assume
							// that the are creating a NEW data
							// element
							// with the same name
							addActionError(ServiceConstants.EXISTING_NAME + de.getName() + ServiceConstants.PERIOD);

						} else {
							importedDataElementMap.put(de, true);

						}
					}
				} else {
					de.setCategory(null);
					importedDataElementMap.put(de, true);
				}
			}
		}
		// return gdOvrwrtDEs;
	}

	/**
	 * Taking the list of of created and validated incoming DE from the spreadsheet, checks to see if any are
	 * overwriting any preexisting DEs. If some are, they are checked first to see if there were any preexisting
	 * validation errors. The uploading user's account compared with the owners of the overwriting DE without errors to
	 * see if they are allowed to be changed. Additionally, the status of the DE are checked as well.
	 */
	public void overwriteDataElements() throws UserAccessDeniedException{

		checkForOverwriteDe();
		// Create another list that tracks the de that will be allowed to be
		// overwritten due to ownership
		filterOverwriteDE();
		// Loop through that list checking the status

		Set<String> importedDataElementNames = getNameSetFromDataElements(importedDataElementMap.keySet());
		Map<String, DataElement> originalDeMap =
				dictionaryManager.getLatestDataElementByNameListIntoMap(importedDataElementNames);

		for (DataElement incomingDataElement : importedDataElementMap.keySet()) {

			if (importedDataElementMap.get(incomingDataElement)) {
				DataElement savedOriginalDE = originalDeMap.get(incomingDataElement.getName());

				// since data element import no longer affects external ID set, we will need to set the original
				// external IDs back to the incoming changes in order to avoid removing the external IDs every time we
				// import.
				incomingDataElement.setExternalIdSet(savedOriginalDE.getExternalIdSet());

				// If the DE status is Draft or Awaiting Publication
				if ((savedOriginalDE.getStatus().equals(DataElementStatus.PUBLISHED))) {
					// The DE is Published
					if (!isAdmin(getAccount())) // If the user doing the
												 // overwriting is not an admin
												 // then they can't
												 // overwrite a DE even if they
												 // own it once it's published
					{
						List<String> ovrValErrs = dictionaryManager.validateOverwriteDE(incomingDataElement);
						// If there are errors in the overwrite validation
						if (ovrValErrs != null && (!ovrValErrs.isEmpty())) {
							// The DE is added to the imported list; false
							// overwrite flag
							incomingDataElement.setCategory(null);
							importedDataElementMap.put(incomingDataElement, true);
							// If there are errors in the validation process for
							// a published DE then the errors should
							// be
							// printed to the page
							for (String error : ovrValErrs) {
								addActionError(error + savedOriginalDE.getName() + ServiceConstants.PERIOD);
							}
						}
					} else {
						List<SeverityRecord> severityRecords = null;

						try {
							severityRecords = dictionaryService.evaluateDataElementChangeSeverity(savedOriginalDE,
									incomingDataElement);

						} catch (Exception e) {
							e.printStackTrace();
						}
						if (severityRecords != null) {
							for (SeverityRecord sr : severityRecords) {
								addActionError(incomingDataElement.getTitle() + " - "
										+ RulesEngineUtils.generateSeverityRecordString(sr));
							}
							SeverityLevel highestLevel = findHighestSeverityLevel(severityRecords);
							// If there is a value in this map we can
							// automatically assume that the Data Element was
							// already
							// published
							if (highestLevel != null) {
								overwriteDeSeverityMap.put(incomingDataElement, highestLevel);

								if (highestLevel.compareTo(SeverityLevel.NEW) == 0) {
									incomingDataElement.setCategory(null);
								}
							}
						}
					}
				} else if ((savedOriginalDE.getStatus().equals(DataElementStatus.AWAITING))) {
					// Ide is the overwriting Data Ele
					overwriteAPCheckPermissions(incomingDataElement, savedOriginalDE);
				}

			}
		}
		Set<DataElement> importDEKS = importedDataElementMap.keySet();
		ArrayList<DataElement> orderedList = new ArrayList<DataElement>();

		for (DataElement mappedDE : importDEKS) {
			for (DataElement de : newDataElementList) {
				if (de.getName().equals(mappedDE.getName())) {
					orderedList.add(mappedDE);
					break;
				}
			}
		}
		newDataElementList.clear();
		newDataElementList = orderedList;
		// After the first loop there should be a second list with the DE's that
		// the user will be allowed to changed
		// Loop through that list checking the status

	}

	private void overwriteAPCheckPermissions(DataElement ovrWrtDe, DataElement deOver) throws UserAccessDeniedException{

		// We're seeing if the user has permission
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
		PermissionType permission = null;
		try {
			permission = restProvider.getAccess(getAccount().getId(), EntityType.DATA_ELEMENT, deOver.getId())
					.getPermission();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (permission == null || (!(PermissionType.compare(permission, PermissionType.WRITE) >= 0))) {

			ovrWrtDe.setCategory(null);
			addActionError(ovrWrtDe.getName() + " " + ServiceConstants.INVALID_PERMISSION + ServiceConstants.PERIOD);

		}
	}

	/**
	 * Checks wether not a user is either a dictionary Admin or a global admin.
	 * 
	 * @param acct
	 * @return
	 */
	public Boolean isAdmin(Account acct) {

		for (AccountRole ar : acct.getAccountRoleList()) {
			if (ar.getRoleType().equals(RoleType.ROLE_ADMIN)
					|| ar.getRoleType().equals(RoleType.ROLE_DICTIONARY_ADMIN)) {
				if (!ar.isExpired() && ar.getIsActive())
					return true;
			}
		}
		return false;
	}

	public String adminUpload() throws Exception {

		if (ServiceConstants.CSV_FILE.equals(uploadContentType) || "text/csv".equals(uploadContentType)
				|| ServiceConstants.APPLICATION_CSV_FILE.equals(uploadContentType)) {

			HashMap<String, ArrayList<String>> pvValidateMap = new HashMap<String, ArrayList<String>>();
			try {

				newDataElementList = dictionaryManager.parseDataElement(upload, uploadContentType, pvValidateMap);

			} catch (Exception e) {
				e.printStackTrace();
				addActionError(e.getMessage());
				return PortalConstants.ACTION_IMPORT;
			}

			if (!newDataElementList.isEmpty()) {
				// Look for extra columns in the datafile
				// NOTE: These are just warnings and should not stop the
				// validation process.
				List<String> columnValidateList = dictionaryManager.validateExtraColumns(upload, uploadContentType);
				for (String error : columnValidateList) {
					addActionError(error);
				}

				// Validate the data elements. This function will add
				// errors/warnings to the actionErrors section
				// and set the commonFlags of any errored elements to false.
				validateDataElementList(pvValidateMap);

				// Check to see if any data elements are being overwritten, are
				// they are being overwritten,
				// determine whether or not the user has the proper permissions
				// to do so
				overwriteDataElements();

				// Check to see if any for Data Elements are overwriting any
				// preexisting Data Elements in the system.
				// preexistingDataElementCheck();
			} else {
				// list is empty, but not null. set it to null for display
				// purposes
				newDataElementList = null;
				addActionError("There is no valid data in the file.");
				return PortalConstants.ACTION_IMPORT;
			}
		}

		else {
			// File uploaded ins not a CSV
			addActionError(
					"Invalid file type. If the file is open in another application please close it and try again.");
		}

		getSessionDataElementList().setDataElements(newDataElementList);
		return PortalConstants.ACTION_IMPORT;
	}

	// CRIT-6168:Change History For DE- There is no change history option during data element import process
	// change history is required if the DataElement exists in the system and is published.
	public String checkExistingDataElements() {

		newDataElementList = (List<DataElement>) sessionDataElementList.getDataElements();

		checkForOverwriteDe();

		List<DataElement> listToSave = new ArrayList<DataElement>();
		Map<String, DataElement> deOverMap = new HashMap<String, DataElement>();

		if (!checkedList.equals("")) {
			String[] flags = checkedList.split(",");

			for (int i = 0; i < flags.length; i++) {

				int position = Integer.valueOf(flags[i]);
				listToSave.add(newDataElementList.get(position));
			}

			if (!listToSave.isEmpty()) {
				Set<String> listToSaveNames = getNameSetFromDataElements(listToSave);
				deOverMap = dictionaryManager.getLatestDataElementByNameListIntoMap(listToSaveNames);

				for (DataElement de : listToSave) {
					if (importedDataElementMap.get(de)
							&& DataElementStatus.PUBLISHED.equals(deOverMap.get(de.getName()).getStatus())) {

						List<SeverityRecord> severityRecords = null;

						if (deOverMap.get(de.getName()) != null) {
							try {
								severityRecords = dictionaryService
										.evaluateDataElementChangeSeverity(deOverMap.get(de.getName()), de);

							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						if (severityRecords != null && !severityRecords.isEmpty() && isAdmin(getAccount())) {
							isExistingDataElementChecked = true;
						}
					}
				}
			}
		}

		return PortalConstants.ACTION_IMPORT;
	}

	public String adminSaveDataElements() throws HttpException, IOException, UserAccessDeniedException {

		newDataElementList = (List<DataElement>) sessionDataElementList.getDataElements();
		checkForOverwriteDe();

		List<DataElement> listToSave = new ArrayList<DataElement>();
		if (!checkedList.equals("")) {
			String[] flags = checkedList.split(",");

			for (int i = 0; i < flags.length; i++) {

				int position = Integer.valueOf(flags[i]);
				listToSave.add(newDataElementList.get(position));
			}
		}

		if (!listToSave.isEmpty()) {
			Set<String> listToSaveNames = getNameSetFromDataElements(listToSave);
			Map<String, DataElement> deOverMap =
					dictionaryManager.getLatestDataElementByNameListIntoMap(listToSaveNames);

			// Save the list
			for (DataElement de : listToSave) {
				String[] proxyTickets =
						PortalUtils.getMultipleProxyTickets(modulesConstants.getModulesAccountURL(getDiseaseId()), 2);
				// Temporarily saving the overwritten DataElement before its
				// overwritten in order to determine the
				// status and other information
				DataElement deOver = deOverMap.get(de.getName());
				DataElement toRtn;
				SeverityLevel highestSL = null;
				//
				if (deOver != null) {
					de.setVersion(deOver.getVersion());
					de.setStatus(deOver.getStatus());
					de.setCreatedBy(getDisplayNameByUsername(deOver.getCreatedBy()));
					de.setDateCreated(deOver.getDateCreated());
					if (deOver.getStatus().equals(DataElementStatus.PUBLISHED)) {
						highestSL = retriveHighestSeverityLevel(de, deOver);
					}
				} else {
					de.setVersion(PortalConstants.VERSION_NEW);
					de.setCreatedBy(getAccount().getDisplayName());
					de.setDateCreated(new Date());
				}

				// just pass the deOver we already have instead of making
				// another dao call
				toRtn = dictionaryManager.update(getAccount(), deOver, de, new ArrayList<String>(),
						new ArrayList<String>(), highestSL, proxyTickets);

				// save minor major change eventlog if the dataelement is published
				// CRIT-6168 Change History For DE- There is no change history option during data element import process
				List<SeverityRecord> severityRecords = null;

				if (deOver != null) {
					try {
						severityRecords = dictionaryService.evaluateDataElementChangeSeverity(deOver, de);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (severityRecords != null && !severityRecords.isEmpty()
						&& (DataElementStatus.PUBLISHED.equals(deOver.getStatus()) && toRtn.getId() != null)) {
					SeverityLevel highestLevel = findHighestSeverityLevel(severityRecords);

										
					DictionaryEventLog eventLog = new DictionaryEventLog(getDiseaseId(),getAccount().getUserId());
					Long originalEntityId = dictionaryService.getOriginalDataElementIdByName(de.getName());
					eventLog.setDataElementID(originalEntityId);
									
					dictionaryService.saveChangeHistoryEventlog(eventLog,severityRecords, highestLevel,EntityType.DATA_ELEMENT,deOver.getId(),de.getId(),auditNote);

				}

				// The import may need it's own save function that could feed
				// into the saveDataElement function

				// If being overwritten the value should be true
				if (importedDataElementMap.get(de)) {
					if (deOver.getStatus().getName().equals(DataElementStatus.PUBLISHED.getName())) {
						try {
							// If the Data Element is being overwritten and it's
							// published, then these tickets will be
							// used to
							// publish the incoming DE

							webServiceManager.registerEntityToPermissionGroup(getAccount(),
									ServiceConstants.PUBLIC_DATA_ELEMENTS, EntityType.DATA_ELEMENT, toRtn.getId(),
									PermissionType.READ);
						} catch (HttpException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (deOver.getStatus().getName().equals(DataElementStatus.AWAITING.getName())) {

						webServiceManager.unregisterEntityToPermissionGroup(ServiceConstants.PUBLIC_DATA_ELEMENTS,
								EntityType.DATA_ELEMENT, deOver.getId(), PermissionType.READ);
					}
				}
			}
		}

		return PortalConstants.ACTION_LIST;
	}

	public String organizeSeverityRecords(List<SeverityRecord> srs) {

		StringBuilder changeString = new StringBuilder();
		if (!srs.isEmpty()) {

			for (SeverityRecord sr : srs) {
				changeString.append(RulesEngineUtils.generateSeverityRecordString(sr) + "\n");

			}
		}
		return changeString.toString();
	}

	private Set<String> getNameSetFromDataElements(Collection<DataElement> deList) {

		Set<String> names = new HashSet<String>();

		if (deList == null) {
			return names;
		}

		for (DataElement de : deList) {
			names.add(de.getName());
		}

		return names;
	}

	public String adminDSImport() {

		return PortalConstants.ACTION_DSIMPORT;
	}

	public String userImport() {

		return PortalConstants.ACTION_IMPORT;
	}

	/*
	 * This method forwards the user to the admin upload form strucutre page.
	 */
	public String adminFormStructureImport() {

		return PortalConstants.ACTION_IMPORT_FORM_STRUCTURE;
	}

	public String adminDataElementSchemaImport() {
		return PortalConstants.ACTION_IMPORT_DATA_ELEMENT_SCHEMA;
	}

	public String viewDetails() throws NumberFormatException, UnsupportedEncodingException, UserPermissionException {

		// Retrives the Data Element name from the
		String dataElementName = getRequest().getParameter("dataElementName");
		Iterator<DataElement> importDEItr = getSessionDataElementList().getDataElements().iterator();
		boolean foundDE = false;
		while (!foundDE && importDEItr.hasNext()) {
			DataElement importedDataElement = importDEItr.next();
			if (importedDataElement.getName().equals(dataElementName)) {
				DataElement preexst = dictionaryManager.getLatestDataElementByName(importedDataElement.getName());
				if (preexst != null) {
					importedDataElement.setStatus(preexst.getStatus());
				}
				currentDataElement = importedDataElement;
				foundDE = true;
			}
		}

		return PortalConstants.ACTION_VIEW_DETAILS;
	}

	public DataElement getCurrentDataElement() {

		return currentDataElement;
	}

	public boolean isChangeHistoryRequired() {
		return changeHistoryRequired;
	}

	public void setChangeHistoryRequired(boolean changeHistoryRequired) {
		this.changeHistoryRequired = changeHistoryRequired;
	}

	public boolean getIsExistingDataElementChecked() {
		return isExistingDataElementChecked;
	}

	public void setExistingDataElementChecked(boolean isExistingDataElementChecked) {
		this.isExistingDataElementChecked = isExistingDataElementChecked;
	}

	public String getAuditNote() {
		return auditNote;
	}

	public void setAuditNote(String auditNote) {
		this.auditNote = auditNote;
	}

	public SeverityLevel retriveHighestSeverityLevel(DataElement incomingDataElement, DataElement savedOriginalDE) {

		List<SeverityRecord> changesMade = null;
		try {
			changesMade = dictionaryService.evaluateDataElementChangeSeverity(savedOriginalDE, incomingDataElement);
		} catch (RulesEngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return findHighestSeverityLevel(changesMade);
	}

	public SeverityLevel findHighestSeverityLevel(List<SeverityRecord> changesMade) {

		SeverityLevel highest = null;
		if (changesMade != null) {
			if (!changesMade.isEmpty()) {
				for (SeverityRecord sr : changesMade) {
					if (highest == null)
						highest = sr.getSeverityLevel();
					else {
						if (highest.compareTo(sr.getSeverityLevel()) == 1) {
							highest = sr.getSeverityLevel();
						}
					}
				}
			}
		}
		return highest;
	}

	// CRIT-6168:Change History For DE- There is no change history option during data element import process
	// change history is required if the DataElement exists in the system and is published.
	public void checkIsChangeHistoryRequired(Map<String, DataElement> deMap, DataElement ide) {

		if (DataElementStatus.PUBLISHED.equals(deMap.get(ide.getName()).getStatus())) {

			List<SeverityRecord> severityRecords = null;

			if (deMap.get(ide.getName()) != null) {
				try {
					severityRecords =
							dictionaryService.evaluateDataElementChangeSeverity(deMap.get(ide.getName()), ide);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (severityRecords != null && !severityRecords.isEmpty() && isAdmin(getAccount())) {
				changeHistoryRequired = true;
			}
		}
	}
}
