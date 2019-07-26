package gov.nih.tbi.commons.service;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.hibernate.Alias;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.ValidationPlugin;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.FormStructureExport;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

import org.apache.commons.httpclient.HttpException;
import org.apache.http.impl.cookie.DateParseException;



/**
 * Dictionary Tool Manager
 * 
 * @author Andrew Johnson
 * @author Francis Chen
 * 
 */
public interface DictionaryToolManager extends BaseManager {

	// LISTS

	/**
	 * Returns a List of all BasicDataStructures
	 * 
	 * @param curUser
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<FormStructure> listDataStructures(Account account, PaginationData pageData, String proxyticket)
			throws MalformedURLException, UnsupportedEncodingException;

	/**
	 * Returns a list of all BasicDataStructures with the statuses contained in array statusStringList. Accepts an array
	 * of Strings and matches them to the known statuses.
	 * 
	 * @Deprecated, instead use method that accepts known enumerations.
	 * 
	 * @param curUser
	 * @param statusStringList
	 * @return
	 */

	/**
	 * Returns a List of all BasicDataElement
	 * 
	 * @param curUser
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<DataElement> listDataElements(Account account, String proxyTicket) throws MalformedURLException,
			UnsupportedEncodingException;

	// GETS

	public Keyword getKeyword(String keyword);

	public Keyword getLabel(String label);

	public FormStructure getDataStructure(Long dataStructureId);

	StructuralFormStructure getSqlDataStructure(Long structId);

	public FormStructure getDataStructure(String shortName, String version);

	public FormStructure getLatestDataStructure(String shortName);

	public FormStructure getDataStructureLatestVersion(String shortName);

	public MapElement getMapElement(Long mapElementId);

	public DataElement getDataElement(Long dataElementId);

	public DataElement getDataElement(String shortName, String version);


	/**
	 * Gets a list of DataElements that match the list of IDs
	 * 
	 * @param ids List of DataElement ids to get
	 * @return List of DataElements
	 */
	public List<DataElement> getDataElementsListByIds(List<Long> ids);

	/**
	 * Gets the latest version of the data element with the given name
	 * 
	 * @param dataElementName
	 * @return
	 */
	public DataElement getLatestDataElementByName(String dataElementName);

	/**
	 * Gets a list of DataStructures that are attached to DataElement with the specified ID
	 * 
	 * @param nameAndVersion
	 * @return
	 */
	public List<FormStructure> getAttachedDataStructure(String deName, String deVersion);

	/**
	 * Saves a a data structure into the database. Returns any EntityMaps that need to be registered with UM
	 * 
	 * @param acocunt
	 * @param permission
	 * @param dataStructure
	 * @param errors
	 * @param warnings
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws HttpException
	 */
	public FormStructure saveDataStructure(Account acocunt, PermissionType permission, FormStructure dataStructure,
			List<String> errors, List<String> warnings, SeverityLevel severityLevel, String proxyTicket)
			throws MalformedURLException, UnsupportedEncodingException, HttpException, IOException;

	/**
	 * Saves a dataElement in the new database. Can be used for updating and saving new structures NOTE: User must check
	 * for write permissions before using this function
	 * 
	 * @param account
	 * @param permission
	 * @param dataElement
	 * @param errors
	 * @param warnings
	 * @param severityLevel
	 * @param twoProxyTickets
	 * @return returns true if new entityMap entries must be created in UM
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws HttpException
	 */
	public DataElement saveDataElement(Account account, DataElement dataElement, List<String> errors,
			List<String> warnings, SeverityLevel severityLevel, String[] twoProxyTickets, DataElementStatus status,
			boolean isPublicationChange) throws MalformedURLException, UnsupportedEncodingException, HttpException,
			IOException, UserAccessDeniedException;

	/**
	 * Creates a deep copy of a form structure. If the severityLevel is major, then the version is increased by one. If
	 * it is new then the version is set to 1.
	 * 
	 * @param currentAccount
	 * @param dataStructure
	 * @return
	 */

	public FormStructure formStructureCopy(Account currentAccount, FormStructure dataStructure,
			SeverityLevel severityLevel);

	/**
	 * Creates a deep copy of a data element. If the severityLevel is major, then the version is increased by one. If it
	 * is new then the version is set to 1. CreatedDate is set to current time. Alisases are not copied (must be
	 * unique).
	 * 
	 * @param currentAccount
	 * @param dataElement
	 * @param severityLevel
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	public DataElement dataElementCopy(Account currentAccount, DataElement dataElement, SeverityLevel severityLevel)
			throws MalformedURLException, UnsupportedEncodingException;

	// DELETE

	public Boolean deleteDataStructure(Account account, FormStructure dataStructure, String proxyTicket);

	Boolean deleteSqlDataStructure(Account account, Long structID, String proxyTicket);

	// SEARCH

	public List<Keyword> searchKeywords(String searchKey);

	public List<Keyword> searchLabels(String searchKey);

	/**
	 * Sets up and exectues a query for a list of data elments based on pagination information, filters, and account
	 * access
	 * 
	 * @param account : Account performing the search. If null, then search is limited to public data elements only
	 * @param diseaseSelection
	 * @param domainSelection
	 * @param subDomainSelection
	 * @param populationSelection
	 * @param subgroupSelection
	 * @param classificationSelection
	 * @param filterId : The id of the DataElementStatus we want to search for. If null or -1, then we want to search
	 *        all status types
	 * @param elementTypeSelection
	 * @param searchKey
	 * @param pageData
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<DataElement> searchElements(Account account, String diseaseSelection, Boolean generalSearch,
			String domainSelection, String subDomainSelection, String populationSelection, String subgroupSelection,
			String classificationSelection, Long filterId, Category category, String searchKey,
			PaginationData pageData, String proxyTicket) throws MalformedURLException, UnsupportedEncodingException;

	// ADD

	/**
	 * The elements listed in the array are added to the repeatableGroup object. If the mapElements that are created do
	 * not have ids, they are assigned temporary (negative) ids starting at tempId and iterating downward. These
	 * temporary ids allow the map elements to be identified in the sessionDataStructure until they are added to the
	 * database.
	 * 
	 * @param dataElementIds
	 * @param currentDataStructure
	 * @param tempId
	 */
	public void addDataElementsByNames(String[] dataElementIds, RepeatableGroup repeatableGroup, Integer tempId,
			MapElement me, FormStructure DataStructure);


	/**
	 * Returns the number of deprecated and retired data elements in the given set of data element names.
	 * resultArray[0]: deprecated DE count, resultArray[1]: retired DE count
	 * 
	 * @param dataElementNames - Array of data element names.
	 */
	public int[] getDeprecatedRetiredDECount(String[] dataElementNames);

	/**
	 * This method adds a MapElement to a RepeatableGroup, using the MapElement's position to insert it in the correct
	 * place. If no position is given, it is added to the end, if a negative number is given, then it is placed at the
	 * beginning.
	 * 
	 * @param mapElement
	 * @param dataStructure
	 */
	public void addMapElementToList(MapElement mapElement, RepeatableGroup repeatableGroup);

	/**
	 * Adds a repeatable group to a Data Structure. Returns the Data Structure with the new repeatbale group in its list
	 * 
	 * @param repeatableGroup
	 * @param dataStructure
	 */
	public FormStructure addRepeatableGroupToList(RepeatableGroup repeatableGroup, FormStructure dataStructure);

	// REMOVE

	/**
	 * Removes a MapElement from a DataStructure based on the mapElementId Updated to include repeatable groups
	 * 
	 * @param mapElementId
	 * @param groupElementId
	 * @param dataStructure
	 * @return
	 */
	public MapElement removeMapElementFromList(Long mapElementId, Long groupElementId, FormStructure dataStructure);

	/**
	 * Removes a MapElement from a DataStructure based on the dataElementName
	 * 
	 * @param dataElementName
	 * @param dataStructure
	 * @return
	 */
	public MapElement removeMapElementFromList(String dataElementName, RepeatableGroup repeatableGroup);

	/**
	 * Removes a Repeatable Group from a DataStructure based on the id of the RG
	 * 
	 * @param repeatableGroupId
	 * @param dataStructure
	 * @return
	 */

	public RepeatableGroup removeRepeatableGroupFromList(Long repeatableGroupId, FormStructure dataStructure);

	public Boolean getIsLatestVersion(DataElement de);

	public Boolean getIsLatestVersion(FormStructure fs);
	
	public Boolean getIsFormStructurePublished(String shortName);

	// FIND

	/**
	 * Returns the MapElement that is attached to a RepeatableGroup based on mapElementId Updated to include
	 * RepeatableGroup table
	 * 
	 * @param mapElementId
	 * @param repeatableGroup
	 * @return
	 */
	public MapElement findMapElementInList(Long mapElementId, RepeatableGroup repeatableGroup);

	public RepeatableGroup findRepeatableGroupInList(Long groupElemenetId, FormStructure dataStructure);

	/**
	 * Returns the MapElement that is attached to a RepeatableGroup based on dataElementName
	 * 
	 * @param dataElementName
	 * @param repeatableGroup
	 * @return
	 */
	public MapElement findMapElementInList(String dataElementName, RepeatableGroup repeatableGroup);

	// UTIL

	/**
	 * Changes a MapElement's position in the DataStructure to given newPosition
	 * 
	 * @param mapElementId
	 * @param newPosition
	 * @param dataStructure
	 */
	public void moveMapElementInList(Long mapElementId, Integer newPosition, Long groupElementId,
			FormStructure dataStructure);

	/**
	 * Changes a RepeatableGroups's position in the DataStructure to given newPosition
	 * 
	 * @param RepeatableGroupId
	 * @param newPosition
	 * @param dataStructure
	 */
	public void moveGroupInList(Long repeatableGroupId, Integer newPosition, FormStructure dataStructure);

	/**
	 * This method returns a list of validation plugins
	 * 
	 * @return String
	 */
	public List<ValidationPlugin> getValidationPlugins();

	/**
	 * Validates the alias
	 * 
	 * @param dataStructure : if non-null, the validator will validate against all the map elements in this
	 *        dataStructure as well as the database
	 * @param alias
	 * @param dataElement
	 * @return boolean
	 */
	public boolean validateAlias(FormStructure dataStructure, Alias alias, DataElement dataElement);

	/**
	 * Determines if a short name is valid. true = valid, false = invalid.
	 * 
	 * @param dataStructure
	 * @param fieldValue
	 * @return
	 */
	public boolean validateShortName(FormStructure dataStructure, String newShortName);
	
	public FormStructure saveFormStructure(FormStructure formStructure);

	public String getNextMajorDataStructureVersion(String shortName);

	public String getNextMinorDataStructureVersion(String shortName);

	public String getNextMinorDataElementVersion(String shortName);

	public String getNextMajorDataElementVersion(String shortName);

	/**
	 * Delete the data element with the given short name. All version are deleted.
	 * 
	 * @param name
	 */

	public void deleteStructuralDataElement(String name);

	public void deleteDataElement(String name);

	/**
	 * Changes the status of the dataStructure and saves it.
	 * 
	 * @param account
	 * @param permission
	 * @param dataStructure
	 * @param status
	 * @return AbstractDataStructure
	 * @throws UserPermissionException
	 * @throws MalformedURLException
	 */
	public FormStructure editDataStructureStatus(Account account, PermissionType permission,
			FormStructure dataStructure, StatusType status) throws UserPermissionException, MalformedURLException;

	public FormStructure editDataStructureStatusWithoutSave(Account account, PermissionType permission,
			FormStructure dataStructure, StatusType status) throws UserPermissionException, MalformedURLException;
	/**
	 * Returns true if element name is unique, false otherwise. To compensate for different versions having the same
	 * name, an oldName can now optionally be supplied. If the current name matches the new name then the DE is allowed
	 * to pass validation.
	 * 
	 * @param dataElement
	 * @param oldName
	 * @return Boolean
	 */
	public Boolean validateDataElementName(DataElement dataElement, String oldName);

	/**
	 * Returns true if the string passed is a unique keyword name, false otherwise
	 * 
	 * @param keywordName
	 * @return
	 */
	public Boolean validateKeywordName(String keywordName);

	/**
	 * Returns true if the string passed is a unique label name, false otherwise
	 * 
	 * @param labelName
	 * @return
	 */
	public Boolean validateLabelName(String labelName);

	/**
	 * This method parses a file into a list of map elements. Only handles csv at the moment.
	 * 
	 * @param upload
	 * @return
	 * @throws Exception
	 */
	public List<DataElement> parseDataElement(File upload, String uploadContentType,
			HashMap<String, ArrayList<String>> pvValidateMap) throws Exception;

	/**
	 * Returns a mapping of mapElement to a list of invalid fields. A field is invalid if it is required and not filled
	 * out.
	 * 
	 * @param mapElementList
	 * @param true if user is doing an admin import rather than attachin to a data structure
	 * @return
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public List<String> validateImportedDataElement(File upload, DataElement dataElement, boolean inAdmin)
			throws MalformedURLException, FileNotFoundException, IOException;

	/**
	 * Gets a list of all keywords
	 * 
	 * @return List<Keyword>
	 */
	public List<Keyword> getAllKeywords();

	/**
	 * Get a list of all labels
	 * 
	 * @return List<Keyword>
	 */
	public List<Keyword> getAllLabels();

	/**
	 * Overload of the other method with the same name. Uses a String for name and Long for formId
	 * 
	 * @param groupName
	 * @param dsId
	 * @return Boolean
	 */
	public Boolean validateRepeatableGroupName(String groupName, Long dsId);

	/**
	 * Returns the output stream of a basic CSV file to be exported
	 * 
	 * @param elementList
	 * @return
	 */
	public ByteArrayOutputStream exportToCsvBasic(List<DataElement> elementList) throws IOException;

	/**
	 * Returns the output stream of a detailed CSV file to be exported
	 * 
	 * @param elementList
	 * @throws DateParseException
	 */
	public ByteArrayOutputStream exportToCsvDetailed(List<DataElement> elementList) throws IOException,
			DateParseException;

	/**
	 * Returns the output stream of a zip file with multiple DE and mapping CSV files
	 * 
	 * @param elementList
	 * @throws DateParseException
	 */
	public ByteArrayOutputStream exportToZippedCsvDetailed(List<DataElement> elementList) throws IOException,
			DateParseException;

	
	/** added by Ching-Heng
	 * Returns the output stream of a zip file with form structure associated multiple DE
	 * 
	 * @param Form Structure object
	 * @throws IOException 
	 * @throws DateParseException
	 */
	public ByteArrayOutputStream exportPromisZippedFsDe(FormStructure formStructure,List<DataElement> elementList) 
			throws IOException, DateParseException;
	
	public List<SemanticFormStructure> getLatestFormStructuresByIdAndStatus(List<String> shortNames);

	/**
	 * Checks to see if a set of ValueRanges contains a ValueRange with the same value as permissibleValue
	 * 
	 * @param valueRanges
	 * @param permissibleValue
	 * @return
	 */
	public boolean isPermissibleValueUnique(Collection<ValueRange> valueRanges, String permissibleValue);

	/**
	 * Changes the state of the Data Element by checking permissions first. NOTE: After this function is called user
	 * must check return value to see if new entityMaps must be created with WS
	 * 
	 * @param account
	 * @param dataElement
	 * @param status
	 * @return : true if new entityMaps must be registered with UM
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws HttpException
	 */
	public DataElement editDataElementStatus(Account account, DataElement dataElement, DataElementStatus status,
			String[] threeProxyTickets) throws MalformedURLException, UnsupportedEncodingException, HttpException,
			IOException, UserAccessDeniedException;

	/**
	 * This method returns an output stream of the csv of a datastructure to be exported
	 * 
	 * @param permission
	 * @param dataStructure
	 * @param includeData
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream exportDataStructure(String serverLocation, String proxyTicket,
			PermissionType permission, FormStructure dataStructure, boolean includeData, Long diseaseId)
			throws IOException;

	/**
	 * return the number of data structures with a specific status determined by the status id. If statusId is null,
	 * then return the total number of data structures.
	 * 
	 * @param statusId
	 * @return
	 */
	public Long getNumDSWithStatus(Long statusId);

	/**
	 * return the number of data elements with a specific status determined by the status id. If the id is null, then
	 * the total count of DEs is returned
	 * 
	 * @param statusId
	 * @return
	 */
	public Long getNumDEWithStatus(Long statusId);

	/**
	 * return the number of data elements with a specific status determined by the status id and with a given category.
	 * If either argument is null then then the query is not filtered by that parameter
	 * 
	 * @param statusId
	 * @param category
	 * @return
	 */
	public Long getNumDEWithStatusAndCategory(Long statusId, Category category);

	/**
	 * return the number of data elements with a certain category. If the category is null then query all categories.
	 * 
	 * @param category
	 * @return
	 */
	public Long getNumDEWithCategory(Category category);

	/**
	 * Returns an warning for each column present in the data file which does not match a property in DataElement
	 * 
	 * @param upload
	 * @param uploadContentType
	 * @return
	 * @throws IOException
	 */
	public List<String> validateExtraColumns(File upload, String uploadContentType) throws IOException;

	/**
	 * Deletes the condition from the database
	 * 
	 * @param id - id of the condition to delete
	 */
	public void deleteCondition(Long id);

	/**
	 * This method removes the MapElement and reorders the rest of the MapElements' positions
	 * 
	 * @param removeElement
	 * @param dataStructure
	 */
	public void removeMapElement(MapElement removeElement, RepeatableGroup repeatableGroup);

	/**
	 * Get the list of subDomains that are under the given domain
	 * 
	 * @param domain
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<SubDomain> getSubDomainList(Domain domain, Disease disease) throws MalformedURLException,
			UnsupportedEncodingException;

	public List<FormStructure> getDataStructureByIds(List<Long> dsIdList);

	/**
	 * Returns the domain listing for a particular disease
	 * 
	 * @param disease - disease used to filter the domain list
	 * @return list of domains relevant to the disease
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<Domain> getDomainsByDisease(Disease disease) throws MalformedURLException, UnsupportedEncodingException;

	/**
	 * Gets the disease object, given the disease name
	 * 
	 * @param diseaseName
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public Disease getDiseaseByName(String diseaseName) throws MalformedURLException, UnsupportedEncodingException;

	/**
	 * Returns a list of subgroups based on the disease selected
	 * 
	 * @param disease
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<Subgroup> getSubgroupsByDisease(Disease disease) throws MalformedURLException,
			UnsupportedEncodingException;

	/**
	 * Returns a list of classifications
	 * 
	 * @param disease
	 * @param isAdmin - true if getting admin choices for classification, false if getting user choices
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<Classification> getClassificationList(Disease disease, boolean isAdmin) throws MalformedURLException,
			UnsupportedEncodingException;

	/**
	 * Converts a \n deliminated String into a List
	 * 
	 * @param stringIn
	 */
	public List<String> locations(String stringIn);

	/**
	 * Returns the prefix of the disease with id
	 * 
	 * @param diseaseId
	 * @return
	 */
	public String getDiseasePrefix(Long diseaseId);

	/**
	 * Returns true if the character is valid for xml, false otherwise.
	 * 
	 * @param character
	 * @return
	 */
	public boolean isValidForXml(char character);

	/**
	 * Returns a hashmap of line number to invalid characters. This is used to display user friendly errors.
	 * 
	 * @param line
	 * @return
	 */
	public HashMap<Integer, ArrayList<Character>> isValidForXml(List<String[]> line);

	/**
	 * Returns a new DataStructure that can be saved given an imported XML DataStructure object
	 * 
	 * @param workingDataStructure
	 * @return
	 */
	public FormStructure retrieveImportDataStructure(FormStructureExport workingDataStructure, String userOrg);

	/**
	 * This will verify that a short name provided by the user has not been uploaded to the system.
	 * 
	 * @param dataStructure
	 * @param shortName
	 * @return
	 */
	public boolean isImportFSNameUnique(FormStructure dataStructure, String shortName);

	public Set<Long> listUserAccessDEs(Account account, String proxyTicket);

	public List<String> validateOverwriteDE(DataElement ovr);
	
	public DataElement saveDataElementUpdate(DataElement dataElement);

	public DataElement update(Account account, DataElement deOver, DataElement dataElement, ArrayList<String> errors,
			ArrayList<String> warnings, SeverityLevel severityLevel, String[] proxyTickets) throws UserAccessDeniedException;

	/**
	 * Takes in a dataElement String and
	 * 
	 * @param dataElementName
	 * @return
	 */
	public Boolean doesDataElementExist(String dataElementName);

	/*
	 * Tests to see if this is the lastest version of a form structure.
	 */
	public Boolean isLatestFormStructureVersion(FormStructure fs);

	/*
	 * This method will change every form structure to point to the latest data element in the system
	 */
	public void updateFormStructuresWithLatestDataElement(String elementName, Long newDataElementID);

	/**
	 * Given a list of data element names, returns the list of latest versions of the data element objects
	 * 
	 * @param names
	 * @return
	 */
	public List<DataElement> getLatestDataElementByNameList(Set<String> names);

	/**
	 * Given a list of data element names, returns the map of data element names to its respective latest versions of
	 * data element objects.
	 * 
	 * @param names
	 * @return
	 */
	public Map<String, DataElement> getLatestDataElementByNameListIntoMap(Set<String> names);

	/*
	 * This method needs to be removed once the public search has been refactored
	 */
	public List<String> parseSelectedOptions(String options);

	public DictionarySearchFacets buildFacets(String searchKey, String selectedStatuses, String selectedElementTypes,
			String populationSelection, String selectedDiseases, String selectedDomains, String selectedSubdomains,
			String selectedClassifications, String dataElementLocations, String modifiedDate);

	public Set<String> parseSelectedOptionsSet(String options);

	// throw away method
	public List<Disease> getDiseaseOptions();

	/**
	 * Return a list of userfiles based on given list of ids
	 * 
	 * @param accessIds (List<Long>) list of id values used to retrieve userfiles.
	 * 
	 * @return List<Userfile>
	 */
	public List<UserFile> getUserFiles(List<Long> accessIds);

	/**
	 * Return a userfile based on given id
	 * 
	 * @param fileId (Long) id value used to retrieve userfile.
	 * 
	 * @return Userfile
	 */
	public UserFile getUserFile(Long fileId);

	public List<ValueRange> orderValueRange(DataElement dataElement);

	public Map<String, Map<String, ValueRange>> getDEValueRangeMap(Set<String> deNames);

	/**
	 * This method is called daily by brics scheduler to check data elements in dictionary RDF and update their status
	 * based on the following rules:
	 * 
	 * 1. If a DE has an until_date in the past and its status is either Published or Deprecated, change its status to
	 * Retired; 2. If a DE has an until_date in the future and its status is either Published or Retired, change its
	 * status to Deprecated; 3. If a DE does not have an until_date and its status is either Deprecated or Retired,
	 * change its status to Published.
	 */

	public void updateDEStatusWithUntilDate();

	public List<String> getFormStructureNames(List<Long> formStructureIds);

	/**
	 * Return the list of data element short names with the given data type.
	 * @param dataType
	 * @return
	 */
	public List<String> getDataElementNamesByType(DataType dataType);
	
	/**
	 * Returns a list of Schema object in the system.
	 * @return a list of Schema object in the system.
	 */
	public List<Schema> getAllSchemas();
	
	/**
	 * Given a list of data elements that have lazy loaded semantic data elements, load the nested fields into the semantic data element part
	 * @param dataElements
	 */
	public void loadNestedSemanticDataElement(List<DataElement> dataElements);

	List<StructuralFormStructure> getAllSqlFormStructures();
	
	public DictionaryEventLog saveEventLog(DictionaryEventLog eventLog,String oldVal,String newVal,String comment,EventType eventType);
	
	public String getDEShortNameByNameIgnoreCases(String deName);

	
}
