package gov.nih.tbi.dictionary.service.hibernate;

import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.service.complex.BaseManagerImpl;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.commons.service.SchemaMappingManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.util.DataElementFilter;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.dao.AliasDao;
import gov.nih.tbi.dictionary.dao.ClassificationDiseaseDao;
import gov.nih.tbi.dictionary.dao.ConditionDao;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.dao.DataElementSparqlDao;
import gov.nih.tbi.dictionary.dao.DictionaryEventLogDao;
import gov.nih.tbi.dictionary.dao.DiseaseDao;
import gov.nih.tbi.dictionary.dao.DomainSubDomainDao;
import gov.nih.tbi.dictionary.dao.FormStructureDao;
import gov.nih.tbi.dictionary.dao.FormStructureSparqlDao;
import gov.nih.tbi.dictionary.dao.FormStructureSqlDao;
import gov.nih.tbi.dictionary.dao.KeywordSparqlDao;
import gov.nih.tbi.dictionary.dao.LabelSparqlDao;
import gov.nih.tbi.dictionary.dao.MapElementDao;
import gov.nih.tbi.dictionary.dao.RepeatableGroupDao;
import gov.nih.tbi.dictionary.dao.SchemaDao;
import gov.nih.tbi.dictionary.dao.SchemaPvDao;
import gov.nih.tbi.dictionary.dao.StructuralDataElementDao;
import gov.nih.tbi.dictionary.dao.SubgroupDiseaseDao;
import gov.nih.tbi.dictionary.dao.ValidationPluginDao;
import gov.nih.tbi.dictionary.model.ClassificationFacet;
import gov.nih.tbi.dictionary.model.ClassificationFacetValue;
import gov.nih.tbi.dictionary.model.CsvToDataElement;
import gov.nih.tbi.dictionary.model.DateFacet;
import gov.nih.tbi.dictionary.model.DictionaryData;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.DiseaseFacet;
import gov.nih.tbi.dictionary.model.DiseaseFacetValue;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.FormStructureStandardization;
import gov.nih.tbi.dictionary.model.MissingSemanticObjectException;
import gov.nih.tbi.dictionary.model.MissingStructuralObjectException;
import gov.nih.tbi.dictionary.model.StringFacet;
import gov.nih.tbi.dictionary.model.TBIMappingStrategy;
import gov.nih.tbi.dictionary.model.hibernate.Alias;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.ExternalId;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.SchemaPv;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.ValidationPlugin;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.FormStructureExport;
import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.MapElementExport;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.dictionary.service.ValueRangeSorter;
import gov.nih.tbi.repository.dao.UserFileDao;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.ws.RestRepositoryProvider;

/**
 * Dictionary Tool Manager class
 * 
 * @author Andrew Johnson
 * @author Francis Chen
 * 
 */

@Service
@Scope("singleton")
public class DictionaryToolManagerImpl extends BaseManagerImpl implements DictionaryToolManager {
	private static final long serialVersionUID = 5090898812732207354L;
	private static final Logger logger = Logger.getLogger(DictionaryToolManagerImpl.class);

	/*********************************************************************/

	@Autowired
	StructuralDataElementDao structuralDataElementDao;

	@Autowired
	DataElementSparqlDao dataElementSparqlDao;

	@Autowired
	FormStructureSparqlDao formStructureSparqlDao;

	@Autowired
	FormStructureSqlDao formStructureSqlDao;

	@Autowired
	SubgroupDiseaseDao subgroupDiseaseDao;

	@Autowired
	ClassificationDiseaseDao classificationDiseaseDao;

	@Autowired
	FormStructureDao formStructureDao;

	@Autowired
	MapElementDao mapElementDao;

	@Autowired
	DataElementDao dataElementDao;

	@Autowired
	ValidationPluginDao validationPluginDao;

	@Autowired
	AliasDao aliasDao;

	@Autowired
	RepeatableGroupDao repeatableGroupDao;

	@Autowired
	CsvToDataElement csvToDataElement;

	@Autowired
	StaticReferenceManager staticManager;

	@Autowired
	ConditionDao conditionDao;

	@Autowired
	DomainSubDomainDao domainSubDomainDao;

	@Autowired
	DictionarySampleDataGenerator dictionarySampleDataGenerator;

	@Autowired
	ModulesConstants modulesConstants;

	@Autowired
	DiseaseDao diseaseDao;

	@Autowired
	KeywordSparqlDao keywordDao;

	@Autowired
	LabelSparqlDao labelDao;

	@Autowired
	UserFileDao userFileDao;

	@Autowired
	SchemaDao schemaDao;

	@Autowired
	SchemaPvDao schemaPvDao;

	@Autowired
	protected SchemaMappingManager schemaMappingManager;

	@Autowired
	DictionaryEventLogDao dictionaryEventLogDao;


	private final String SYSTEM_GENERATED = "System Generated: ";
	private final String RETIRED_COMMENT = " has passed its given until date of ";
	private final String DEPRECATED_COMMENT = " had an until date of ";
	private final String PUBLISHED_COMMENT = " did not have an until-Date ";
	private final String DATAELEMENT = "DataElement ";
	private final String FUTURE = " in the future.";

	/******************************* LIST ********************************/

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	@Override
	public List<FormStructure> listDataStructures(Account account, PaginationData pageData, String proxyTicket)
			throws MalformedURLException, UnsupportedEncodingException {

		RestAccountProvider accountProvider = new RestAccountProvider(
				modulesConstants.getModulesAccountURL(Long.valueOf(account.getDiseaseKey())), proxyTicket);
		Set<Long> ids =
				accountProvider.listUserAccess(account.getId(), EntityType.DATA_STRUCTURE, PermissionType.READ, false);

		return formStructureDao.listDataStructures(ids, pageData);
	}

	@Override
	public List<DataElement> listDataElements(Account account, String proxyTicket)
			throws MalformedURLException, UnsupportedEncodingException {

		RestAccountProvider accountProvider = new RestAccountProvider(
				modulesConstants.getModulesAccountURL(Long.valueOf(account.getDiseaseKey())), proxyTicket);
		Set<Long> ids =
				accountProvider.listUserAccess(account.getId(), EntityType.DATA_ELEMENT, PermissionType.READ, false);

		return dataElementDao.getByIdList(new ArrayList<Long>(ids));
	}

	/**
	 * {@inheritDoc}
	 */
	public Keyword getKeyword(String keyword) {

		return keywordDao.getByName(keyword);
	}

	/**
	 * {@inheritDoc}
	 */
	public Keyword getLabel(String label) {

		return labelDao.getByName(label);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ValidationPlugin> getValidationPlugins() {

		return validationPluginDao.getAll();
	}

	/**
	 * {@inheritDoc}
	 */
	public FormStructure getDataStructure(Long dataStructureId) {

		if (dataStructureId == null) {
			throw new RuntimeException("dataStructureId cannot be null");
		}

		return formStructureDao.getById(dataStructureId);
	}

	public StructuralFormStructure getSqlDataStructure(Long structId) {

		if (structId == null) {
			throw new RuntimeException("dataStructureId cannot be null");
		}

		return this.formStructureSqlDao.get(structId);
	}

	public List<String> getFormStructureNames(List<Long> formStructureIds) {
		if (formStructureIds.isEmpty()) {
			return new ArrayList<String>();

		}

		return formStructureDao.getNamesByIds(formStructureIds);
	}

	public FormStructure getDataStructure(String shortName, String version) {

		FormStructure dataStructure = formStructureDao.get(shortName, version);

		return dataStructure;
	}

	public FormStructure getLatestDataStructure(String shortName) {
		FormStructure result = formStructureDao.getLatestVersionByShortName(shortName);

		return result;
	}

	public FormStructure getDataStructureLatestVersion(String shortName) {

		FormStructure dataStructure = formStructureDao.getLatestVersionByShortName(shortName);

		return dataStructure;
	}

	/**
	 * {@inheritDoc}
	 */
	public DataElement getDataElement(Long dataElementId) {

		if (dataElementId == null) {
			throw new IllegalArgumentException("dataElementId cannot be null");
		}

		// TODO: Why are you not checking entitymap before calling the DAO?
		DataElement dataElement = dataElementDao.get(dataElementId);

		return dataElement;
	}

	/**
	 * {@inheritDoc}
	 */
	public DataElement getDataElement(String shortName, String version) {

		// TODO: Should this funtion run a permissions check?
		DataElement dataElement = dataElementDao.getByNameAndVersion(shortName, version);

		return dataElement;
	}


	/**
	 * @inheritDoc
	 */
	public MapElement getMapElement(Long mapElementId) {

		MapElement me = mapElementDao.get(mapElementId);
		return me;
	}

	/**
	 * @inheritDoc
	 */
	public DataElement getLatestDataElementByName(String dataElementName) {

		// This escaping should occur in the dao layer. It is different for rdf and sql.
		// dataElementName = dataElementName.replace(ServiceConstants.UNDERSCORE,
		// ServiceConstants.SQL_ESCAPED_UNDERSCORE);
		DataElement dataElement = dataElementDao.getLatestByName(dataElementName);

		return dataElement;
	}

	/**
	 * @inheritDoc
	 */
	public Map<String, DataElement> getLatestDataElementByNameListIntoMap(Set<String> names) {

		Map<String, DataElement> deMap = dataElementDao.getLatestByNameListIntoMap(names);

		return deMap;
	}

	/**
	 * @inheritDoc
	 */
	public List<DataElement> getLatestDataElementByNameList(Set<String> names) {

		List<DataElement> dataElements = dataElementDao.getLatestByNameList(names);

		return dataElements;
	}

	/**
	 * @param dataElement
	 */
	@Deprecated
	public List<ValueRange> orderValueRange(DataElement dataElement) {

		Set<ValueRange> valueRange = dataElement.getValueRangeList();
		if (valueRange != null) {
			List<ValueRange> listOfValueRange = new ArrayList<ValueRange>(dataElement.getValueRangeList());
			Collections.sort(listOfValueRange, new ValueRangeSorter());
			return listOfValueRange;
		}
		return null;
	}

	public FormStructure getFormStructureByName(String formStructureName) {

		// This escaping should occur in the dao layer. It is different for rdf and sql.
		// formStructureName = formStructureName.replace(ServiceConstants.UNDERSCORE,
		// ServiceConstants.SQL_ESCAPED_UNDERSCORE);
		FormStructure formStructure = formStructureDao.getLatestVersionByShortName(formStructureName);
		return formStructure;
	}

	/**
	 * @inheritDoc
	 */
	public List<FormStructure> getAttachedDataStructure(String deName, String deVersion) {

		return formStructureDao.getAttachedDataStructure(deName, deVersion);
	}

	/*
	 * overloaded method for the public search
	 */
	public List<FormStructure> getAttachedDataStructure(String deName, String deVersion, boolean isPublicData) {

		return formStructureDao.getAttachedDataStructure(deName, deVersion, isPublicData);
	}

	/***************************** SAVE *********************************/

	/**
	 * @throws IOException
	 * @throws HttpException
	 * @inheritDoc
	 * @param account : The account of the current user in session
	 * @param proxyTicket : The proxyTicket used to validate the user for the first ws call in this function
	 * @param proxyTicket2 : The proxyTicket used to validate the user for the second ws call in this function
	 */
	public DataElement saveDataElement(Account account, DataElement dataElement, List<String> errors,
			List<String> warnings, SeverityLevel severityLevel, String[] twoProxyTickets, DataElementStatus status,
			boolean isPublicationUpdate) throws HttpException, IOException, UserAccessDeniedException {

		// Determine if this is a new data element (editing to be "new" does not count)
		boolean newDataElement = false;
		if (dataElement.getId() == null && !isPublicationUpdate) {
			newDataElement = true;
			// set the status to draft for data elements that don't have an id
			dataElement.setStatus(DataElementStatus.DRAFT);
		}

		// Permission logic
		// If the data element is new = no perm check required
		// Existing but a new change = read perm required
		// Exsiting and major/minor change = write permission required
		if (!newDataElement) {
			PermissionType permRequired = PermissionType.WRITE;
			if (SeverityLevel.NEW.equals(severityLevel)) {
				permRequired = PermissionType.READ;
			}
			RestAccountProvider accountProvider = new RestAccountProvider(
					modulesConstants.getModulesAccountURL(Long.valueOf(account.getDiseaseKey())), twoProxyTickets[0]);
			PermissionType permission = accountProvider
					.getAccess(account.getId(), EntityType.DATA_ELEMENT, dataElement.getId()).getPermission();
			if (!(PermissionType.compare(permission, permRequired) >= 0)
					|| (!isAdmin(account) && dataElement.getStatus() == DataElementStatus.PUBLISHED)) {
				errors.add("The account " + account + " does not have access to write this object");
			}
		}

		Long oldDataElementId = null;

		// Rules Engine
		if (!newDataElement) {
			oldDataElementId = dataElement.getId();

			if (severityLevel != null && !isPublicationUpdate) {
				dataElement = dataElementCopy(account, dataElement, severityLevel);
				newDataElement = true; // This form structure is now new (It was not new for the checks above this
										 // point).
			}
		}

		// Editing an awaiting publication data element makes it draft unless the change is making the data element
		// in awaiting publication status set severity level to null in the action layer
		if (DataElementStatus.AWAITING.equals(dataElement.getStatus()) && severityLevel != null) {
			dataElement.setStatus(DataElementStatus.DRAFT);
		}

		// Enforce rules that define the size of a alphanumeric element
		if (!DataType.ALPHANUMERIC.equals(dataElement.getType()) && !DataType.BIOSAMPLE.equals(dataElement.getType())) {
			dataElement.setSize(null);
		}

		// Enfore rule that only free-form numeric values have a min/max value
		if (!(InputRestrictions.FREE_FORM.equals(dataElement.getRestrictions())
				&& DataType.NUMERIC.equals(dataElement.getType()))) {
			dataElement.setMaximumValue(null);
			dataElement.setMinimumValue(null);
		}

		// Enfore rule that only numeric and Alphanumeric Data Types can have a measuring unit.
		if (!(DataType.NUMERIC.equals(dataElement.getType()) || DataType.ALPHANUMERIC.equals(dataElement.getType()))) {
			dataElement.setMeasuringUnit(null);
		}

		// no need to validate a new data element
		if (!newDataElement) {
			validateDataElement(dataElement, errors, warnings);
		} else {
			dataElement.setId(null);
		}

		// write entry if there are no errors on the page
		if (errors.size() == 0) {
			dataElement = saveDataElementUpdate(dataElement);

			if (newDataElement) {

				// Register the owner if the data element is new.
				RestAccountProvider accountProvider = new RestAccountProvider(
						modulesConstants.getModulesAccountURL(Long.valueOf(account.getDiseaseKey())),
						twoProxyTickets[1]);
				accountProvider.registerEntity(account.getId(), EntityType.DATA_ELEMENT, dataElement.getId(),
						PermissionType.OWNER, null);

			}

			if (oldDataElementId != null) {
				List<SchemaPv> schemaPvs = schemaPvDao.getAllByDataElementId(oldDataElementId);

				for (SchemaPv schemaPv : schemaPvs) {
					if (schemaPv.getValueRange() == null) {
						schemaPv.setId(null);
						schemaPv.setDataElement(dataElement.getStructuralObject());

						schemaPvDao.save(schemaPv);
					}
				}
			}
		}

		return dataElement;
	}

	public DataElement saveDataElementUpdate(DataElement dataElement) {
		return dataElementDao.save(dataElement);
	}

	/**
	 * dataElement - new data element to save deOver - old data element that was compared to
	 */
	public DataElement update(Account account, DataElement deOver, DataElement dataElement, ArrayList<String> errors,
			ArrayList<String> warnings, SeverityLevel severityLevel, String[] proxyTickets) throws UserAccessDeniedException {

		if (deOver != null) {
			// Its awaiting pub or draft, set the existing DE's id with the overWrite DE
			dataElement.setId(deOver.getId());

			TreeSet<ClassificationElement> tree = new TreeSet<ClassificationElement>();
			tree.addAll(dataElement.getClassificationElementList());
			deOver.setClassificationElementList(tree);

			if (deOver.getExternalIdSet() != null) {
				for (ExternalId ei : deOver.getExternalIdSet()) {
					ei.setSemanticDataElement(dataElement.getSemanticObject());
				}
			}
			// If there is a case in which the Alias List is not instantiated, this call will at least create an
			// empty hash set so the DE can be saved (overwrite) in the future.
			dataElement.getAliasList();

			for (Alias a : deOver.getAliasList()) {
				a.setDataElement(dataElement.getStructuralObject());
			}
			logger.info(overwriteLogger(dataElement, deOver));
		}

		DataElement toRtn = null;

		try {
			toRtn = saveDataElement(account, dataElement, errors, warnings, severityLevel, proxyTickets, null, false);

			// if the change creates a new version of a DE (determined by the severity level) update all the map
			// elements
			// to point to the new data element
			if (dataElement.getId() != null && toRtn.getId() != null && !dataElement.getId().equals(toRtn.getId())) {
				mapElementDao.updateFormStructuresWithLatestDataElement(dataElement.getName(), toRtn.getId());
			}
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return toRtn;
	}

	/**
	 * Upon submitting the data element details form, each non-disabled classification select box will call this
	 * function. Updates the classificationElement with the subgroub matching 'index' to have the classification
	 * matching 'value'. If the classificationElementList does not exist then a new one is constructed.
	 * 
	 * @param index
	 * @param value
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public TreeSet<ClassificationElement> setClassificationElementList(Set<ClassificationElement> ovrClassElementList)
			throws MalformedURLException, UnsupportedEncodingException {

		TreeSet<ClassificationElement> classificationElementList = new TreeSet<ClassificationElement>();

		for (ClassificationElement ovrCe : ovrClassElementList) {
			if (ovrCe.getSubgroup().getId() != null) {
				ClassificationElement ce = new ClassificationElement();
				// look up the subgroup by id
				for (Subgroup s : staticManager.getSubgroupList()) {
					if (s.getId().equals(ovrCe.getSubgroup().getId())) {
						ce.setSubgroup(s);
						break;
					}
				}
				// if value is blank then a classification was not selected.
				if (ovrCe.getClassification().getId() != null) {
					// look up classification by id
					for (Classification c : staticManager.getClassificationList(true)) {
						if (c.getId().equals(ovrCe.getClassification().getId())) {
							ce.setClassification(c);
							break;
						}
					}
				}
				classificationElementList.add(ce);
			}
		}
		return classificationElementList;
	}

	/*
	 * This method converts the old FS version to the new one to validate and save
	 */
	public FormStructure retrieveImportDataStructure(FormStructureExport xmlDataStructure, String userOrg) {

		FormStructure workingDataStructure = new FormStructure();
		workingDataStructure.setStatus(StatusType.DRAFT);

		// For this datastructure
		// extract and update with shell information
		workingDataStructure.setTitle(xmlDataStructure.getTitle().trim());
		workingDataStructure.setDescription(xmlDataStructure.getDescription());
		workingDataStructure.setValidatable(null);
		workingDataStructure.setIsCopyrighted(xmlDataStructure.getIsCopyrighted());
		workingDataStructure.setShortName(xmlDataStructure.getShortName().trim());
		workingDataStructure.setOrganization(xmlDataStructure.getOrganization());
		workingDataStructure.setFileType(xmlDataStructure.getFileType());
		workingDataStructure.setStandardization(FormStructureStandardization.getByDisplay(xmlDataStructure.getStandardization()));
		// added by Ching-Heng
		workingDataStructure.setIsCat(xmlDataStructure.isCAT());
		workingDataStructure.setCatOid(xmlDataStructure.getCatOid());
		workingDataStructure.setMeasurementType(xmlDataStructure.getMeasurementType());
		
		if (xmlDataStructure.getRequired()) {
			workingDataStructure.addInstancesRequiredFor(userOrg);
		}

		// contains a list of DE short names for batch processing
		Set<String> dataElementNames = new HashSet<String>();

		// need to retrieve a list of all DE short names
		for (gov.nih.tbi.dictionary.model.hibernate.formstructure.export.RepeatableGroup xmlRepeatableGroup : xmlDataStructure
				.getRepeatableGroups()) {
			for (MapElementExport mapElement : xmlRepeatableGroup.getMapElements()) {
				String elementName = mapElement.getDataElement().getName();
				if (elementName != null && !elementName.isEmpty()) {
					dataElementNames.add(mapElement.getDataElement().getName());
				}
			}
		}

		List<DataElement> validDataElementList = new ArrayList<DataElement>();

		// for some strange reason we allow FSs to be created without any data elements
		// if a FS is uploaded without any this list will be empty.
		// no need to make a DB call with an empty list.
		if (dataElementNames != null && !dataElementNames.isEmpty())
			validDataElementList = dataElementDao.getLatestByNameList(dataElementNames);

		// For Each Repeatable Group
		// Create the repeatable Group
		// extract and update with shell information
		for (gov.nih.tbi.dictionary.model.hibernate.formstructure.export.RepeatableGroup xmlRepeatableGroup : xmlDataStructure
				.getRepeatableGroups()) {

			RepeatableGroup aRepeatableGroup = new RepeatableGroup();
			aRepeatableGroup.setName(xmlRepeatableGroup.getName());
			RepeatableType rgType = xmlRepeatableGroup.getType();
			if (rgType != null) {
				aRepeatableGroup.setType(xmlRepeatableGroup.getType());
			}
			aRepeatableGroup.setThreshold(xmlRepeatableGroup.getThreshold());
			aRepeatableGroup.setPosition(xmlRepeatableGroup.getPosition());

			workingDataStructure.getRepeatableGroups().add(aRepeatableGroup);

			// for all mapElements in this group, retrieve the DataElementIds
			for (MapElementExport xmlMapElement : xmlRepeatableGroup.getMapElements()) {
				MapElement mapElement = new MapElement();
				DataElement dataElement = new DataElement();

				for (DataElement de : validDataElementList) {
					if (de.getName().equalsIgnoreCase(xmlMapElement.getDataElement().getName())) {
						dataElement = de;
					}
				}
				// if a data element was found, add it to the ME
				if (dataElement.getName() != null && !dataElement.getName().equalsIgnoreCase("")) {
					mapElement.setStructuralDataElement(dataElement.getStructuralObject());
					mapElement.setPosition(xmlMapElement.getPosition());
					mapElement.setRequiredType(xmlMapElement.getRequiredType());
					mapElement.setRepeatableGroup(aRepeatableGroup);
				} else {
					// if the DE was not found, create a new one that will be caught later in the action
					dataElement.setName(xmlMapElement.getDataElement().getName());
					mapElement.setStructuralDataElement(dataElement.getStructuralObject());
					mapElement.setRequiredType(xmlMapElement.getRequiredType());
				}

				addMapElementToList(mapElement, aRepeatableGroup);
				aRepeatableGroup.setDataStructure(workingDataStructure.getFormStructureSqlObject());
			}
		}

		// DiseaseStructure is required to set in the workingDataStructure
		// DiseaseElement is required to set in MapElement
		Set<DiseaseStructure> diseaseSet = new HashSet<DiseaseStructure>();

		// This loops through the diseases in the data structure and adds the diseases to the workingDataStructure
		for (DiseaseStructure xmlDiseaseList : xmlDataStructure.getDiseaseList()) {
			try {
				// Obtain the disease from the xml file and validate it against the static manager
				String diseaseName = xmlDiseaseList.getDisease().getName();
				Disease disease = staticManager.getDiseaseByName(diseaseName);
				if (disease != null) {

					DiseaseStructure newStructure = new DiseaseStructure();
					newStructure.setDisease(disease);
					newStructure.setFormStructure(workingDataStructure.getFormStructureSqlObject());
					diseaseSet.add(newStructure);

				}
				// If the disease could not be found, we want to save the name to display it to the user.
				// This error will be caught in the action
				else {
					Disease placeHolder = new Disease();
					placeHolder.setName(diseaseName);
					DiseaseStructure newStructure = new DiseaseStructure();
					newStructure.setDisease(placeHolder);
					diseaseSet.add(newStructure);

				}
			} catch (MalformedURLException e) {
				// This means that the disease was not found in the disease list
				logger.error(
						"Seems like the disease was not found in the list. This error is handled later in the process."
								+ e);
			} catch (UnsupportedEncodingException e) {
				// This error would be thrown if the character encoding is not accepted.
				logger.error(
						"Seems like the character encoding was not accepted. This error is handled later in the process."
								+ e);
			}
		}

		workingDataStructure.setDiseaseList(diseaseSet);
		return workingDataStructure;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isImportFSNameUnique(FormStructure dataStructure, String shortName) {

		// Look for all data structures with the matching short name
		List<FormStructure> bdsList = formStructureDao.findByShortName(shortName);

		// If there were none from the database then it is a totally unique short name and is valid
		if (bdsList != null && !bdsList.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @throws IOException
	 * @throws HttpException
	 * @inheritDoc
	 */
	public FormStructure saveDataStructure(Account account, PermissionType permission, FormStructure dataStructure,
			List<String> errors, List<String> warnings, SeverityLevel severityLevel, String proxyTicket)
			throws HttpException, IOException {

		boolean newDataStructure = false;

		Long dataStructureID = dataStructure.getId();

		if (dataStructureID != null) {
			if (!(PermissionType.compare(permission, PermissionType.WRITE) >= 0)) {
				errors.add("This account does not have access to write this object");
			}
		} else {
			newDataStructure = true;
		}

		// Version the form structure if necessary
		if (!newDataStructure) {
			if (severityLevel != null) {
				dataStructure = formStructureCopy(account, dataStructure, severityLevel);
				// data structure should automatically be published once a newer version is created and it is a new FS
				if (!dataStructure.getVersion().equals("1.0")) {
					dataStructure.setStatus(StatusType.PUBLISHED);
				}
				newDataStructure = true;
			}
		}

		// if status is awaiting publication or shared draft, change data structure back to draft
		if (dataStructure.getStatus() == null || dataStructure.getStatus().equals(StatusType.AWAITING_PUBLICATION)
				|| dataStructure.getStatus().equals(StatusType.SHARED_DRAFT)) {

			if (newDataStructure) {
				dataStructure.setStatus(StatusType.DRAFT);
			} else {
				try {
					dataStructure = editDataStructureStatusWithoutSave(account, permission, dataStructure, StatusType.DRAFT);
				} catch (UserPermissionException e) {
					errors.add("This account does not have access to write this object");
				}
			}

		}

		// validation functions
		validateDataStructure(dataStructure, null, errors, warnings);

		for (RepeatableGroup repeatableGroup : dataStructure.getRepeatableGroups()) {
			// Validate each repeatable group
			validateRepeatableGroup(repeatableGroup, dataStructure, errors, warnings);

			// Validate each mapElement in the reapeatableGroup
			for (MapElement mapElement : repeatableGroup.getMapElements()) {

				if (mapElement.getId() != null && mapElement.getId() < 1) {
					mapElement.setId(null);
				}

				if (mapElement.getRepeatableGroup() == null) {
					mapElement.setRepeatableGroup(repeatableGroup);
				}
			}
		}

		dataStructure = formStructureDao.save(dataStructure);

		// If form structure is new then save the entity owner.
		// When a user edits a form structure (minor or major change) and they were not the owner of the FS being edited
		// (and did not set themselves to be the owner when editing), then this is going to incorrectly set them as the
		// owner. The rest of the submit action will then save a second ownership permission who whoever actually has
		// the
		// ownership entity (if not changed during edit then this should be the old owner, not the current user). I do
		// not think this code block is ever necessary but this close to the release I am just going to make it not run
		// when creating a minor or major change. If you are reading this after the 2.2 release and have some free time
		// you should investigate removing this code.
		if (errors.size() == 0 && newDataStructure) {
			if (severityLevel == null) {
				RestAccountProvider accountProvider = new RestAccountProvider(
						modulesConstants.getModulesAccountURL(Long.valueOf(account.getDiseaseKey())), proxyTicket);
				accountProvider.registerEntity(account.getId(), EntityType.DATA_STRUCTURE, dataStructure.getId(),
						PermissionType.OWNER, null);
			}
		}

		return dataStructure;
	}

	/**
	 * {@inheritDoc}
	 */
	public FormStructure formStructureCopy(Account currentAccount, FormStructure dataStructure,
			SeverityLevel severityLevel) {

		if (dataStructure == null || currentAccount == null || severityLevel == null) {
			throw new IllegalArgumentException("All arguments must be non-null.");
		}

		FormStructure newFormStructure = new FormStructure(dataStructure);

		// Altered properties
		newFormStructure.setVersion(getNextFormStructureVersion(dataStructure.getShortName(), severityLevel));
		newFormStructure.setStatus(StatusType.DRAFT);
		newFormStructure.setModifiedUserId(currentAccount.getUserId());
		newFormStructure.setModifiedDate(new Date());
		newFormStructure.setId(null);
		newFormStructure.addAllInstancesRequiredFor(dataStructure.getInstancesRequiredFor());
		newFormStructure.setStandardization(dataStructure.getStandardization());
		newFormStructure.setFormLabelList(dataStructure.getFormLabelList());

		if (SeverityLevel.NEW.equals(severityLevel)) {
			newFormStructure.setPublicationDate(null);
		}

		return newFormStructure;
	}

	/*
	 * This function will get the next form structure version based on the severity level
	 */
	private String getNextFormStructureVersion(String formStructureShortName, SeverityLevel severityLevel) {
		if (SeverityLevel.MAJOR.equals(severityLevel)) {
			return getNextMajorDataStructureVersion(formStructureShortName);
		} else if (SeverityLevel.MINOR.equals(severityLevel)) {
			return getNextMinorDataStructureVersion(formStructureShortName);
		} else {
			return "1.0";
		}
	}

	/*
	 * This method will get the latest form structure from the system and iterate it up in the next minor version
	 */
	public String getNextMinorDataStructureVersion(String shortName) {
		FormStructure fs = formStructureDao.getLatestVersionByShortName(shortName);

		if (fs == null)
			return "1.0";

		return getNextMinorVersion(fs.getVersion());
	}

	/*
	 * This method will get the latest data element from the system and iterate it up in the next minor version
	 */
	public String getNextMinorDataElementVersion(String shortName) {
		DataElement de = dataElementDao.getLatestByName(shortName);

		if (de == null)
			return "1.0";

		return getNextMinorVersion(de.getVersion());
	}

	/*
	 * this method will take the version being passed and iterate it to the next minor version ex: 1.0 yields 1.1
	 */
	private String getNextMinorVersion(String highVersion) {
		int versionInt = Integer.parseInt(highVersion.substring(highVersion.indexOf(".") + 1, highVersion.length()));
		versionInt = versionInt + 1;
		return highVersion.substring(0, highVersion.indexOf(".") + 1).concat(String.valueOf(versionInt));
	}

	/*
	 * This method will get the latest form structure from the system and iterate it up in the next major version
	 */
	public String getNextMajorDataElementVersion(String shortName) {
		DataElement de = dataElementDao.getLatestByName(shortName);

		if (de == null)
			return "1.0";

		return getNextMajorVersion(de.getVersion());
	}

	/*
	 * This method will get the latest data element from the system and iterate it up in the next major version
	 */
	public String getNextMajorDataStructureVersion(String shortName) {
		FormStructure fs = formStructureDao.getLatestVersionByShortName(shortName);

		if (fs == null)
			return "1.0";

		return getNextMajorVersion(fs.getVersion());
	}

	private String getNextMajorVersion(String higestVersion) {
		int versionInt = Integer.parseInt(higestVersion.substring(0, higestVersion.indexOf(".")));
		versionInt = versionInt + 1;
		return String.valueOf(versionInt).concat(".0");
	}

	public DataElement dataElementCopy(Account currentAccount, DataElement dataElement, SeverityLevel severityLevel)
			throws MalformedURLException, UnsupportedEncodingException {

		DataElement newDataElement = new DataElement(dataElement);

		if (currentAccount == null || dataElement == null || severityLevel == null) {
			throw new IllegalArgumentException("All arguments must be non-null.");
		}

		if (SeverityLevel.MAJOR.equals(severityLevel)) {
			newDataElement.setVersion(getNextMajorDataElementVersion(dataElement.getName()));
			newDataElement.setStatus(dataElement.getStatus());
			newDataElement.setModifiedDate(new Date());

		} else if (SeverityLevel.NEW.equals(severityLevel)) {
			// format always need to be version.subversion ie 1.0, 1.2, 2.0
			newDataElement.setVersion("1.0");
			newDataElement.setStatus(DataElementStatus.DRAFT);
			newDataElement.setDateCreated(new Date());
			newDataElement.setCreatedBy(currentAccount.getUserName());

		} else {
			// This means the change requires a minor fix
			newDataElement.setVersion(getNextMinorDataElementVersion(dataElement.getName()));
			newDataElement.setStatus(dataElement.getStatus());
			newDataElement.setModifiedDate(new Date());
		}

		return newDataElement;
	}

	/**
	 * @throws IOException
	 * @throws HttpException
	 * @inheritDoc
	 */
	public DataElement editDataElementStatus(Account account, DataElement dataElement, DataElementStatus status,
			String[] threeProxyTickets) throws HttpException, IOException, UserAccessDeniedException {

		RestAccountProvider accountProvider = new RestAccountProvider(
				modulesConstants.getModulesAccountURL(Long.valueOf(account.getDiseaseKey())), threeProxyTickets[0]);
		PermissionType permission = accountProvider
				.getAccess(account.getId(), EntityType.DATA_ELEMENT, dataElement.getId()).getPermission();

		if (DataElementStatus.PUBLISHED.equals(status)) {

			if (hasRole(account, RoleType.ROLE_DICTIONARY_ADMIN)
					|| PermissionType.compare(permission, PermissionType.ADMIN) >= 0) {
				dataElement.setStatus(status);
			}
		} else {
			if (PermissionType.compare(permission, PermissionType.WRITE) >= 0) {
				dataElement.setStatus(status);
			}
		}

		// Pass the remaining two proxy tickets to saveDataElment(...)
		String[] twoProxyTickets = new String[2];
		twoProxyTickets[0] = threeProxyTickets[1];
		twoProxyTickets[1] = threeProxyTickets[2];

		return this.saveDataElement(account, dataElement, new ArrayList<String>(), new ArrayList<String>(), null,
				twoProxyTickets, status, true);
	}

	/**
	 * @throws MalformedURLException
	 * @inheritDoc
	 */
	public FormStructure editDataStructureStatus(Account account, PermissionType permission,
			FormStructure dataStructure, StatusType status) throws UserPermissionException, MalformedURLException {

		if (dataStructure == null) {
			throw new IllegalArgumentException(ServiceConstants.NULL_DATASTUCTURE);
		} else if (status == null) {
			throw new IllegalArgumentException(ServiceConstants.NULL_STATUS);
		}

		//get formstructure from the database to make sure we are only changing the status
		FormStructure formStructure = formStructureDao.getById(dataStructure.getId());

		//we only want to set publish pending to draft or awaiting publication
		//this might happen when the thread for creating repo tables and publishing fs finishes before 
		//other changes we are making to the form structure
		if(status==StatusType.PUBLISH_PENDING && formStructure.getStatus()== StatusType.PUBLISHED){
			status = StatusType.PUBLISHED;
		}
		
		formStructure.setStatus(status);
		formStructure.setModifiedUserId(account.getUserId());
		formStructure.setModifiedDate(new Date());
		
		//if publishing form structure, set the publication date here
		if(status==StatusType.PUBLISHED) {
			formStructure.setPublicationDate(new Date());
		}
		
		

		if (!(PermissionType.compare(permission, PermissionType.ADMIN) >= 0)) {
			throw new UserPermissionException("Only Users with Admin permission can change Form Structure Status");
		}

		formStructureDao.save(formStructure);

		return formStructure;
	}
	
	public FormStructure editDataStructureStatusWithoutSave(Account account, PermissionType permission,
			FormStructure dataStructure, StatusType status) throws UserPermissionException, MalformedURLException {
		if (dataStructure == null) {
			throw new IllegalArgumentException(ServiceConstants.NULL_DATASTUCTURE);
		} else if (status == null) {
			throw new IllegalArgumentException(ServiceConstants.NULL_STATUS);
		}

		dataStructure.setStatus(status);
		dataStructure.setModifiedUserId(account.getUserId());
		dataStructure.setModifiedDate(new Date());

		if (!(PermissionType.compare(permission, PermissionType.ADMIN) >= 0)) {
			throw new UserPermissionException("Only Users with Admin permission can change Form Structure Status");
		}

		return dataStructure;
	}

	/*************************** DELETE ********************************/

	/**
	 * @inheritDoc
	 */
	public Boolean deleteDataStructure(Account account, FormStructure dataStructure, String proxyTicket) {

		long structID = dataStructure.getId();

		try {

			Long originalEntity =
					formStructureSqlDao.getOriginalFormStructureByName(dataStructure.getShortName()).getId();
			Set<DictionaryEventLog> eventLogs = dictionaryEventLogDao.searchFSEventLogs(originalEntity);
			dictionaryEventLogDao.removeAll(new ArrayList<DictionaryEventLog>(eventLogs));

			formStructureDao.remove(structID);
			RestAccountProvider accountProvider = new RestAccountProvider(
					modulesConstants.getModulesAccountURL(Long.valueOf(account.getDiseaseKey())), proxyTicket);
			accountProvider.unregisterEntity(structID, EntityType.DATA_STRUCTURE);

			return true;
		} catch (Exception e) {

			e.printStackTrace();

			return false;
		}
	}

	public Boolean deleteSqlDataStructure(Account account, Long structID, String proxyTicket) {

		try {

			StructuralFormStructure formStructure = formStructureSqlDao.get(structID);

			Long originalEntity =
					formStructureSqlDao.getOriginalFormStructureByName(formStructure.getShortName()).getId();
			Set<DictionaryEventLog> eventLogs = dictionaryEventLogDao.searchFSEventLogs(originalEntity);
			dictionaryEventLogDao.removeAll(new ArrayList<DictionaryEventLog>(eventLogs));

			this.formStructureSqlDao.remove(structID);

			RestAccountProvider accountProvider = new RestAccountProvider(
					modulesConstants.getModulesAccountURL(Long.valueOf(account.getDiseaseKey())), proxyTicket);
			accountProvider.unregisterEntity(structID, EntityType.DATA_STRUCTURE);

			return true;
		} catch (Exception e) {

			e.printStackTrace();

			return false;
		}
	}

	/**************************** SEARCH ******************************/

	/**
	 * @inheritDoc
	 */
	public List<Keyword> searchKeywords(String searchKey) {

		List<Keyword> keywords = keywordDao.search(searchKey);

		return keywords;
	}

	/**
	 * @inheritDoc
	 */
	public List<Keyword> searchLabels(String searchKey) {

		List<Keyword> labels = labelDao.search(searchKey);

		return labels;
	}

	/**
	 * @inheritDoc
	 */
	public List<DataElement> searchElements(Account account, String diseaseSelection, Boolean generalSearch,
			String domainSelection, String subDomainSelection, String populationSelection, String subgroupSelection,
			String classificationSelection, Long filterId, Category category, String searchKey, PaginationData pageData,
			String proxyTicket) throws UnsupportedEncodingException, MalformedURLException {

		logger.debug("Here is a list of all the variables: diseaseSelection-" + diseaseSelection + " generalSearch-"
				+ generalSearch + " domainSelection-" + domainSelection + " subDomainSelection-" + subDomainSelection
				+ " populationSelection-" + populationSelection + " subgroupSelection-" + subgroupSelection
				+ " classificationSelection-" + classificationSelection + " filterId-" + filterId + " searchKey-"
				+ searchKey);
		List<DataElement> returnList = null;

		// Access manager (remove if this is too slow)
		Set<Long> ids = null;
		if (account != null && !hasRole(account, RoleType.ROLE_DICTIONARY_ADMIN)) {
			ids = listUserAccessDEs(account, proxyTicket);
		}

		DataElementStatus status = null;
		// A statusId of -1 means that the user selected the all filter
		// The status remains null which means the dao will not add a status constraint.
		if (filterId != null && filterId != -1) {
			status = DataElementStatus.getById(filterId);
		}

		// Get objects to filter on based on filter values
		Disease selectedDisease = null;
		Domain selectedDomain = null;
		SubDomain selectedSubDomain = null;
		Population selectedPopulation = null;
		Subgroup selectedSubgroup = null;
		Classification selectedClassification = null;

		if (diseaseSelection != null && !ServiceConstants.EMPTY_STRING.equals(diseaseSelection)) {
			selectedDisease = staticManager.getDiseaseByName(diseaseSelection);
		}
		if (domainSelection != null && !ServiceConstants.EMPTY_STRING.equals(domainSelection)) {
			selectedDomain = staticManager.getDomainByName(domainSelection);
		}
		if (subDomainSelection != null && !ServiceConstants.EMPTY_STRING.equals(subDomainSelection)) {
			selectedSubDomain = staticManager.getSubDomainByName(subDomainSelection);
		}
		if (populationSelection != null && !ServiceConstants.EMPTY_STRING.equals(populationSelection)) {
			selectedPopulation = staticManager.getPopulationByName(populationSelection);
		}
		if (subgroupSelection != null && !ServiceConstants.EMPTY_STRING.equals(subgroupSelection)) {
			selectedSubgroup = staticManager.getSubgroupByName(subgroupSelection);
		}
		if (classificationSelection != null && !ServiceConstants.EMPTY_STRING.equals(classificationSelection)) {
			selectedClassification = staticManager.getClassificationByName(classificationSelection);
		}

		DataElementFilter dataElementFilter = new DataElementFilter(selectedDomain, selectedSubDomain, selectedDisease,
				selectedPopulation, selectedClassification, selectedSubgroup, generalSearch);
		returnList = dataElementDao.search(ids, category, status, searchKey, pageData, dataElementFilter);

		return returnList;
	}

	public Set<Long> listUserAccessDEs(Account account, String proxyTicket) {

		Set<Long> ids = null;
		RestAccountProvider accountProvider = new RestAccountProvider(
				modulesConstants.getModulesAccountURL(Long.valueOf(account.getDiseaseKey())), proxyTicket);
		try {
			ids = accountProvider.listUserAccess(account.getId(), EntityType.DATA_ELEMENT, PermissionType.READ, false);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ids;
	}

	/****************************** ADD ********************************/

	/**
	 * @inheritDoc
	 */
	public void addDataElementsByNames(String[] dataElementNames, RepeatableGroup repeatableGroup, Integer tempId,
			MapElement me, FormStructure DataStructure) {

		List<DataElement> deList =
				dataElementDao.getBasicLatestByNameList(new HashSet<String>(Arrays.asList(dataElementNames)));

		for (DataElement dataElement : deList) {
			// r.s. add this to included Data elements to the form structure.
			DataStructure.getDataElements().put(dataElement.getNameAndVersion(), dataElement);

			MapElement mapElement = new MapElement();
			mapElement.setStructuralDataElement(dataElement.getStructuralObject());
			mapElement.setPosition(repeatableGroup.getMapElements().size() + 1);
			mapElement.setId(Long.valueOf(tempId--));
			mapElement.setRepeatableGroup(repeatableGroup);
			if (me != null) {
				String replacedMeName = me.getStructuralDataElement().getName();
				if (replacedMeName.equals(dataElement.getName())) {
					mapElement.setRequiredType(me.getRequiredType());
				} else {
					mapElement.setRequiredType(RequiredType.RECOMMENDED);
				}
			} else {
				mapElement.setRequiredType(RequiredType.RECOMMENDED);

			}

			addMapElementToList(mapElement, repeatableGroup);
		}
	}

	/**
	 * @inheritDoc
	 */
	public int[] getDeprecatedRetiredDECount(String[] dataElementNames) {

		Set<String> dataElementNameSet = new HashSet<String>(Arrays.asList(dataElementNames));

		List<DataElement> dataElements = dataElementDao.getBasicLatestByNameList(dataElementNameSet);

		int deprecatedDECount = 0, retiredDECount = 0;
		for (DataElement de : dataElements) {

			if (DataElementStatus.DEPRECATED == de.getStatus()) {
				deprecatedDECount++;
			} else if (DataElementStatus.RETIRED == de.getStatus()) {
				retiredDECount++;
			}
		}

		return new int[] {deprecatedDECount, retiredDECount};
	}

	/**
	 * @inheritDoc
	 */
	public void addMapElementToList(MapElement mapElement, RepeatableGroup repeatableGroup) {

		// determine the position of the new mapElement in the dataStructure
		if (mapElement.getPosition() == null || mapElement.getPosition() > repeatableGroup.getMapElements().size()) {
			mapElement.setPosition(repeatableGroup.getMapElements().size() + 1);
		} else if (mapElement.getPosition() <= 0) {
			mapElement.setPosition(1);
		}

		// add the new element to the data structure
		boolean inserted = false;
		Set<MapElement> dataElementSet = new LinkedHashSet<MapElement>();
		mapElement.setRepeatableGroup(repeatableGroup);
		for (MapElement me : repeatableGroup.getMapElements()) {
			if (me.getPosition() != null) {
				if (me.getPosition().equals(mapElement.getPosition())) {
					dataElementSet.add(mapElement);
					inserted = true;
				}

				if (me.getPosition() >= mapElement.getPosition()) {
					me.setPosition(me.getPosition() + 1);
				}
			}
			dataElementSet.add(me);
		}

		// this is a new map element and must be added to the dataElement table
		if (inserted == false) {
			dataElementSet.add(mapElement);
		}

		repeatableGroup.getMapElements().clear();
		repeatableGroup.getMapElements().addAll(dataElementSet);
	}

	/**
	 * @inheritDoc
	 */
	public FormStructure addRepeatableGroupToList(RepeatableGroup repeatableGroup, FormStructure dataStructure) {

		Set<RepeatableGroup> rgSet = dataStructure.getRepeatableGroups();
		if (rgSet != null) {
			for (RepeatableGroup rg : rgSet) {
				if (rg.getId().equals(repeatableGroup.getId())) {
					// This is to remove the old
					dataStructure.getRepeatableGroups().remove(rg);
					break;
				}
			}
		} else {
			logger.error("Error: Data Structure: " + dataStructure.getReadableName() + " has no Repeatable Groups.");
		}
		dataStructure.getRepeatableGroups().add(repeatableGroup);

		return dataStructure;
	}

	/****************************** REMOVE ********************************/

	public void deleteStructuralDataElement(String name) {

		List<StructuralDataElement> elementsToRemove = this.structuralDataElementDao.getAllByName(name);

		boolean foundAttachedFormStructure = false;
		for (StructuralDataElement sde : elementsToRemove) {
			List<FormStructure> attachedToList = getAttachedDataStructure(sde.getName(), sde.getVersion());
			if (attachedToList != null && !attachedToList.isEmpty()) {
				logger.error("Cannot delete data element " + sde.getName() + " V" + sde.getVersion()
						+ ". Data element is attached to " + attachedToList.size() + " form structure(s).");
				foundAttachedFormStructure = true;
			}
		}

		if (!foundAttachedFormStructure) {
			// This second loop is slightly less efficient, but it is a little safer.
			for (StructuralDataElement sde : elementsToRemove) {

				Long originalId = structuralDataElementDao.getOriginalDataElementByName(sde.getName()).getId();
				Set<DictionaryEventLog> eventLogs = dictionaryEventLogDao.searchDEEventLogs(originalId);
				dictionaryEventLogDao.removeAll(new ArrayList<DictionaryEventLog>(eventLogs));

				long elementID = sde.getId();
				structuralDataElementDao.remove(elementID);
			}
		}

	}

	/**
	 * @inheritDoc
	 */
	public void deleteDataElement(String name) {

		List<DataElement> elementsToRemove = dataElementDao.getAllByName(name);

		// Verify every data element is unattached before deleting any of them.
		boolean foundAttachedFormStructure = false;
		for (DataElement de : elementsToRemove) {
			List<FormStructure> attachedToList = getAttachedDataStructure(de.getName(), de.getVersion());
			if (attachedToList != null && !attachedToList.isEmpty()) {
				logger.error("Cannot delete data element " + de.getName() + " V" + de.getVersion()
						+ ". Data element is attached to " + attachedToList.size() + " form structure(s).");
				foundAttachedFormStructure = true;
			}
		}
		if (!foundAttachedFormStructure) {
			// This second loop is slightly less efficient, but it is a little safer.
			for (DataElement de : elementsToRemove) {

				Long originalId = structuralDataElementDao.getOriginalDataElementByName(de.getName()).getId();
				Set<DictionaryEventLog> eventLogs = dictionaryEventLogDao.searchDEEventLogs(originalId);
				dictionaryEventLogDao.removeAll(new ArrayList<DictionaryEventLog>(eventLogs));

				dataElementDao.remove(de);
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public MapElement removeMapElementFromList(Long mapElementId, Long groupElementId, FormStructure dataStructure) {

		RepeatableGroup rg = findRepeatableGroupInList(groupElementId, dataStructure);
		MapElement me = findMapElementInList(mapElementId, rg);

		removeMapElement(me, rg);

		return me;
	}

	/**
	 * @inheritDoc
	 */
	public MapElement removeMapElementFromList(String dataElementName, RepeatableGroup repeatableGroup) {

		MapElement me = findMapElementInList(dataElementName, repeatableGroup);

		removeMapElement(me, repeatableGroup);

		return me;
	}

	/**
	 * @inheritDoc
	 */
	public void removeMapElement(MapElement removeElement, RepeatableGroup repeatableGroup) {

		repeatableGroup.getMapElements().remove(removeElement);

		for (MapElement me : repeatableGroup.getMapElements()) {
			if (removeElement != null && me.getPosition() >= removeElement.getPosition()) {
				me.setPosition(me.getPosition() - 1);
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public RepeatableGroup removeRepeatableGroupFromList(Long repeatableGroupId, FormStructure dataStructure) {

		RepeatableGroup repeatableGroup = findRepeatableGroupInList(repeatableGroupId, dataStructure);
		dataStructure.getRepeatableGroups().remove(repeatableGroup);

		// Shift all the positions greater then the removed group
		for (RepeatableGroup rg : dataStructure.getRepeatableGroups()) {
			if (repeatableGroup != null && rg.getPosition() >= repeatableGroup.getPosition()) {
				rg.setPosition(rg.getPosition() - 1);
			}
		}

		return repeatableGroup;
	}

	/****************************** FIND ********************************/

	/**
	 * @inheritDoc
	 */
	public MapElement findMapElementInList(Long mapElementId, RepeatableGroup repeatableGroup) {

		for (MapElement mapElement : repeatableGroup.getMapElements()) {
			if (mapElement.getId().equals(mapElementId)) {
				return (MapElement) mapElement;
			}
		}

		return null;
	}

	public RepeatableGroup findRepeatableGroupInList(Long repeatableGroupId, FormStructure dataStructure) {

		if (dataStructure != null)
			for (RepeatableGroup repeatableGroup : dataStructure.getRepeatableGroups()) {
				if (repeatableGroup.getId().equals(repeatableGroupId)) {
					return repeatableGroup;
				}
			}

		return null;
	}

	/**
	 * @inheritDoc
	 */
	public MapElement findMapElementInList(String dataElementName, RepeatableGroup repeatableGroup) {

		for (MapElement mapElement : repeatableGroup.getMapElements()) {
			if (mapElement.getStructuralDataElement().getName().equals(dataElementName)) {
				return mapElement;
			}
		}

		return null;
	}

	/****************************** UTILS ********************************/

	/**
	 * @inheritDoc
	 */
	public ByteArrayOutputStream exportDataStructure(String serverLocation, String proxyTicket,
			PermissionType permission, FormStructure dataStructure, boolean includeData, Long diseaseId)
			throws IOException {

		RestRepositoryProvider repositoryProvider = new RestRepositoryProvider(serverLocation, proxyTicket);

		List<String> lineBufferList = new ArrayList<String>();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter outputStream = new OutputStreamWriter(baos);
		CSVWriter writer = new CSVWriter(outputStream, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

		if (dataStructure != null & (PermissionType.compare(permission, PermissionType.READ) >= 0)) {

			// Add the shortname and version number to the first line
			lineBufferList.add(dataStructure.getShortName());


			String[] lineBuffer = new String[lineBufferList.size()];
			lineBuffer = lineBufferList.toArray(lineBuffer);
			writer.writeNext(lineBuffer);

			lineBufferList.clear();

			if (includeData == false) {
				lineBufferList.add(ModelConstants.RECORD_STRING);

				// add the main group first
				for (RepeatableGroup repeatableGroup : dataStructure.getRepeatableGroups()) {
					if (ServiceConstants.MAIN.equalsIgnoreCase(repeatableGroup.getName())) {
						for (MapElement mapElement : repeatableGroup.getMapElements()) {
							lineBufferList.add(repeatableGroup.getName() + ServiceConstants.PERIOD
									+ mapElement.getStructuralDataElement().getName());
						}
						break;
					}
				}

				// add the rest of the groups
				for (RepeatableGroup repeatableGroup : dataStructure.getRepeatableGroups()) {
					if (!ServiceConstants.MAIN.equalsIgnoreCase(repeatableGroup.getName())) {
						for (MapElement mapElement : repeatableGroup.getMapElements()) {
							lineBufferList.add(repeatableGroup.getName() + ServiceConstants.PERIOD
									+ mapElement.getStructuralDataElement().getName());
						}
					}
				}

				lineBuffer = new String[lineBufferList.size()];
				lineBuffer = lineBufferList.toArray(lineBuffer);
				writer.writeNext(lineBuffer);
			} else {
				DictionaryData data = dictionarySampleDataGenerator.generateDataStructureData(repositoryProvider,
						dataStructure, diseaseId);

				// Write headers
				lineBuffer = data.getColumnNames();
				writer.writeNext(lineBuffer);

				writer = new CSVWriter(outputStream, CSVWriter.DEFAULT_SEPARATOR);

				// Write each line of data
				for (String[] row : data.getData()) {
					writer.writeNext(row);
				}
			}

			writer.close();
			outputStream.close();
		}

		return baos;
	}

	/**
	 * @inheritDoc
	 */
	public ByteArrayOutputStream exportToCsvBasic(List<DataElement> elementList) throws IOException {

		// setting the headers
		String[] headers = {ServiceConstants.ID_READABLE, ServiceConstants.NAME_READABLE,
				ServiceConstants.SHORT_DESCRIPTION_READABLE, ServiceConstants.TYPE_READABLE};

		// start a new writer
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(baos));
		// write the header
		writer.writeNext(headers);

		List<String> currentRow = new ArrayList<String>();
		for (DataElement dataElement : elementList) {

			if (dataElement != null) {
				// id
				currentRow.add(
						dataElement.getId() != null ? dataElement.getId().toString() : ServiceConstants.EMPTY_STRING);
				// name
				currentRow.add(dataElement.getName());
				// short description
				currentRow.add(dataElement.getShortDescription());
				// type
				currentRow.add(dataElement.getType() != null ? dataElement.getType()
						.getValue() : ServiceConstants.EMPTY_STRING);
			}

			String[] out = new String[currentRow.size()];
			out = currentRow.toArray(out);
			// write the current row
			writer.writeNext(out);
			// clear the current array
			currentRow.clear();
		}
		// close the writer
		writer.close();

		return baos;
	}

	/**
	 * Returns semi-colon delimited list for a given list of permissible values
	 * 
	 * @param permissibleValues
	 * @return
	 */
	private String permissibleValueDescriptionToCSVString(Set<ValueRange> permissibleValues) {

		StringBuffer pvBuffer = new StringBuffer();

		if (permissibleValues != null && !permissibleValues.isEmpty()) {

			for (ValueRange pv : permissibleValues) {
				if (pv.getDescription() == null) {
					// Correction: this field is allowed to be empty
					// throw new NullPointerException("Permissible value description fields cannot be null");
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
	 * Returns semi-colon delimited list for a given list of permissible value output codes
	 * 
	 * @param permissibleValues
	 * @return
	 */
	private String permissibleValueOutputCodeToCSVString(Set<ValueRange> permissibleValues) {

		StringBuilder sb = new StringBuilder();

		if (permissibleValues != null && !permissibleValues.isEmpty()) {
			for (ValueRange pv : permissibleValues) {
				sb.append(pv.getOutputCode() != null ? pv.getOutputCode() : ServiceConstants.EMPTY_STRING);
				sb.append(ServiceConstants.CSV_LIST_SEPARATER);
			}

			int lastDelimitorIndex = sb.length() - ServiceConstants.CSV_LIST_SEPARATER.length();
			sb.replace(lastDelimitorIndex, sb.length(), ServiceConstants.EMPTY_STRING);
		}

		return sb.toString();
	}

	/**
	 * Returns semi-colon delimited list for a given list of permissible value descriptions
	 * 
	 * @param permissibleValues
	 * @return
	 */
	public String permissibleValueToCSVString(Set<ValueRange> permissibleValues) {

		StringBuffer pvBuffer = new StringBuffer();

		if (permissibleValues != null && !permissibleValues.isEmpty()) {

			for (ValueRange pv : permissibleValues) {
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
	
	/** added by Ching-Heng
	 * Returns semi-colon delimited list for a given list of permissible value descriptions
	 * 
	 * @param permissibleValues
	 * @return
	 */
	public String permissibleValueElementOidToCSVString(Set<ValueRange> permissibleValues) {

		StringBuffer pvBuffer = new StringBuffer();
		boolean flag= false;
		if (permissibleValues != null && !permissibleValues.isEmpty()) {

			for (ValueRange pv : permissibleValues) {
				if (pv == null) {
					throw new NullPointerException("Permissible value fields cannot be null");
				}
				if(pv.getElementOid()!=null) {
					pvBuffer.append(pv.getElementOid()).append(ServiceConstants.CSV_LIST_SEPARATER);
					flag = true;
				}
			}
			if(flag) {
				int lastDelimitorIndex = pvBuffer.length() - ServiceConstants.CSV_LIST_SEPARATER.length();
				pvBuffer.replace(lastDelimitorIndex, pvBuffer.length(), ServiceConstants.EMPTY_STRING);
			}
		}

		return pvBuffer.toString();
	}
	
	/** added by Ching-Heng
	 * Returns semi-colon delimited list for a given list of permissible value descriptions
	 * 
	 * @param permissibleValues
	 * @return
	 */
	public String permissibleValueItemResponseOidToCSVString(Set<ValueRange> permissibleValues) {

		StringBuffer pvBuffer = new StringBuffer();
		boolean flag= false;
		if (permissibleValues != null && !permissibleValues.isEmpty()) {

			for (ValueRange pv : permissibleValues) {
				if (pv == null) {
					throw new NullPointerException("Permissible value fields cannot be null");
				}
				if(pv.getItemResponseOid()!=null) {
					pvBuffer.append(pv.getItemResponseOid()).append(ServiceConstants.CSV_LIST_SEPARATER);
					flag = true;
				}
			}
			if(flag) {
				int lastDelimitorIndex = pvBuffer.length() - ServiceConstants.CSV_LIST_SEPARATER.length();
				pvBuffer.replace(lastDelimitorIndex, pvBuffer.length(), ServiceConstants.EMPTY_STRING);
			}
		}

		return pvBuffer.toString();
	}

	/**
	 * returns a hash map of disease names to their disease element object
	 * 
	 * @param de
	 * @return
	 */
	private Map<String, SubDomainElement> getDiseaseNameToDiseaseMap(DataElement de) {

		Map<String, SubDomainElement> diseaseNameMap = new HashMap<String, SubDomainElement>();

		if (de.getSubDomainElementList() == null) {
			return diseaseNameMap;
		}

		for (SubDomainElement subDomainElement : de.getSubDomainElementList()) {
			diseaseNameMap.put(subDomainElement.getDisease().getName(), subDomainElement);
		}

		return diseaseNameMap;
	}

	/**
	 * returns the disease name from the given header
	 * 
	 * @param header
	 * @return
	 */
	private String getDiseaseNameFromHeader(String header) {

		String[] headerParts = header.split("\\" + ServiceConstants.PERIOD);

		if (headerParts.length == 2) {
			return headerParts[1].toLowerCase();
		}

		return ServiceConstants.EMPTY_STRING;
	}

	/**
	 * Returns true if the given header name starts with 'classification.'
	 * 
	 * @param currentColumn
	 * @return
	 */
	private boolean isClassificationColumn(String currentColumn) {

		return currentColumn != null ? currentColumn.startsWith(ServiceConstants.CLASSIFICATION_PREFIX) : false;
	}

	/**
	 * Dynamically get the domain pair string from the disease in the column name
	 * 
	 * @param diseaseElementMap
	 * @param column
	 * @return
	 */
	private String getDomainPairStringFromColumn(Map<String, SubDomainElement> diseaseElementMap, String column) {

		SubDomainElement diseaseElement = diseaseElementMap.get(getDiseaseNameFromHeader(column));

		if (diseaseElement == null || diseaseElement.getDomain() == null || diseaseElement.getSubDomain() == null) {
			return ServiceConstants.EMPTY_STRING;
		} else {
			return diseaseElement.getDomain().getName() + "." + diseaseElement.getSubDomain().getName();
		}
	}

	private String getDomainPairStringFromColumn(DataElement de, String column) {

		if (de.getSubDomainElementList() == null || de.getSubDomainElementList().isEmpty()) {
			return ServiceConstants.EMPTY_STRING;
		}

		StringBuffer domainBuffer = new StringBuffer();
		String diseaseName = getDiseaseNameFromHeader(column);
		for (SubDomainElement subDomainElement : de.getSubDomainElementList()) {
			if (subDomainElement.getDisease().getName().equalsIgnoreCase(diseaseName)) {
				domainBuffer
						.append(subDomainElement.getDomain().getName() + "."
								+ subDomainElement.getSubDomain().getName())
						.append(ServiceConstants.CSV_LIST_SEPARATER);
			}
		}
		if (domainBuffer.length() > 0) {
			int lastDelimitorIndex = domainBuffer.length() - ServiceConstants.CSV_LIST_SEPARATER.length();
			domainBuffer.replace(lastDelimitorIndex, domainBuffer.length(), ServiceConstants.EMPTY_STRING);

			return domainBuffer.toString();
		} else {
			return ServiceConstants.EMPTY_STRING;
		}
	}

	/**
	 * Returns true is the given column header is a domain one. Basically tests the header name for 'domain.' prefix
	 * 
	 * @param columnName
	 * @return
	 */
	private boolean isDomainColumn(String columnName) {

		return columnName != null ? columnName.startsWith(ServiceConstants.DOMAIN_PREFIX) : false;
	}

	/**
	 * For the data element, returns a hash mapp of subgroups to their respective classification
	 * 
	 * @param dataElement
	 * @return
	 */
	private Map<String, String> getSubgroupToClassificationMap(DataElement dataElement) {

		Map<String, String> subgroupToClassification = new HashMap<String, String>();

		if (dataElement.getClassificationElementList() == null) {
			return subgroupToClassification;
		}

		for (ClassificationElement ce : dataElement.getClassificationElementList()) {
			if (ce.getSubgroup() != null && ce.getClassification() != null) {
				subgroupToClassification.put(ce.getSubgroup().getSubgroupName().toLowerCase(),
						ce.getClassification().getName());
			}
		}

		return subgroupToClassification;
	}

	private static String[] toPrimitive(List<String> list) {

		if (list == null) {
			return new String[0];
		}

		String[] primitive = new String[list.size()];

		Iterator<String> iterator = list.iterator();

		for (int i = 0; i < primitive.length; i++) {
			primitive[i] = iterator.next();
		}

		return primitive;
	}

	/**
	 * @throws DateParseException
	 * @inheritDoc
	 */
	public ByteArrayOutputStream exportToCsvDetailed(List<DataElement> elementList)
			throws IOException, DateParseException {

		logger.debug(
				"Both need to find out how many DEs there are here: " + (elementList != null ? elementList.size() : 0));

		// create new writer
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(baos));

		// write headers into the csv
		
		if(modulesConstants.isNTRRInstance()){
			writer.writeNext(toPrimitive(ServiceConstants.EXPORT_NTI_CSV_HEADERS));
		}else{
		writer.writeNext(toPrimitive(ServiceConstants.EXPORT_CSV_HEADERS));
		}
		

		if (elementList == null) {
			writer.close();
			throw new NullPointerException("Data Element cannot be null");
		}

		// write each DE into a row
		List<String> currentRow = new ArrayList<String>();
		for (DataElement currentDataElement : elementList) {
			if (currentDataElement == null) {
				writer.close();
				throw new NullPointerException("Data Element cannot be null");
			}
			dataElmentToStringArray(currentRow, currentDataElement);
			writer.writeNext(currentRow.toArray(new String[0]));
			currentRow.clear();
		}

		writer.close();

		return baos;
	}

	public ByteArrayOutputStream exportToZippedCsvDetailed(List<DataElement> elementList)
			throws IOException, DateParseException {
		logger.debug(
				"Both need to find out how many DEs there are here: " + (elementList != null ? elementList.size() : 0));

		// create new writer
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(zos));

		// create the regular DE export CSV file
		ZipEntry dataElementEntry = new ZipEntry("dataElementExport.csv");
		zos.putNextEntry(dataElementEntry);
		// write headers into the csv
		
		if(modulesConstants.isNTRRInstance()){
			writer.writeNext(toPrimitive(ServiceConstants.EXPORT_NTI_CSV_HEADERS));
		}else{
		writer.writeNext(toPrimitive(ServiceConstants.EXPORT_CSV_HEADERS));
		}
		
		
		if (elementList == null) {
			writer.close();
			throw new NullPointerException("Data Element cannot be null");
		}
		List<String> currentRow = new ArrayList<String>();
		for (DataElement currentDataElement : elementList) {
			if (currentDataElement == null) {
				writer.close();
				throw new NullPointerException("Data Element cannot be null");
			}

			dataElmentToStringArray(currentRow, currentDataElement);
			writer.writeNext(currentRow.toArray(new String[0]));
			currentRow.clear();
		}
		writer.flush();
		zos.closeEntry();

		// Write out PV mapping
		String schemaKey = null;
		List<SchemaPv> pvs = null;
		HashSet<String> schemaSet = new HashSet<>();
		ZipEntry idMappingEntry = new ZipEntry("ExternalIDMapping.csv");
		zos.putNextEntry(idMappingEntry);
		writer.writeNext(toPrimitive(ServiceConstants.EXPORT_SCHEMA_PV_CSV_HEADERS));
		for (DataElement currentDataElement : elementList) {
			pvs = schemaMappingManager.getAllMappings(currentDataElement);
			schemaSet = new HashSet<>();
			if (!pvs.isEmpty()) {
				for (SchemaPv pv : pvs) {
					currentRow.add(currentDataElement.getName());
					currentRow.add(pv.getSchema().getName());
					currentRow.add(pv.getSchemaDeId());
					currentRow.add(pv.getValueRange() != null ? pv.getValueRange()
							.getValueRange() : ServiceConstants.EMPTY_STRING);
					currentRow.add(pv.getSchemaDataElementName());
					currentRow.add((pv.getPermissibleValue() != null && !(pv.getPermissibleValue().equals(ServiceConstants.NULL))) 
							? pv.getPermissibleValue() : ServiceConstants.EMPTY_STRING);
					currentRow.add(pv.getSchemaPvId());
					writer.writeNext(currentRow.toArray(new String[0]));
					currentRow.clear();
					schemaKey = new String(currentDataElement.getName() + "_" + pv.getSchema().getName() + "_"
							+ pv.getSchemaDeId());
					schemaSet.add(schemaKey);
				}
			}
			// clean and reuse pvs and schemaMap objects
			pvs.clear();
			schemaSet.clear();
		}
		writer.flush();
		zos.closeEntry();
		zos.close();

		return baos;
	}
	
	// added by Ching-Heng
	public ByteArrayOutputStream exportPromisZippedFsDe(FormStructure formStructure, List<DataElement> elementList) 
			throws IOException, DateParseException{
		// create new writer
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		OutputStreamWriter outputStream = new OutputStreamWriter(zos);
		CSVWriter writer = new CSVWriter(outputStream, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
		// form structure===============================================
		ZipEntry formStructureEntry = new ZipEntry("formStructure.csv");
		zos.putNextEntry(formStructureEntry);		
		writer.writeNext(createFormStructureHeader()); // Header
		writer.writeNext(createFormStructureInfo(formStructure)); // meta information
		writer.writeNext(createdAssociatedDiseaseTitle());// Associated Disease Title
		writer.writeNext(createdAssociatedDisease(formStructure));// Associated Diseases
		List<RepeatableGroup> rGroups = formStructure.getSortedRepeatableGroups();
		for(RepeatableGroup group : rGroups) {
			writer.writeNext(createGroupTitle(group));
			writer.writeNext(createGroup(group));			
			writer.writeNext(createElementsTitle(group));	
			LinkedHashSet<MapElement> elements = (LinkedHashSet<MapElement>) group.getDataElements();
			for(MapElement element:elements) {
				writer.writeNext(createElement(element));				
			}
		}			
		writer.flush();
		zos.closeEntry();		
		// data elements=====================================================
		ZipEntry dataElementEntry = new ZipEntry("dataElementExport.csv");
		zos.putNextEntry(dataElementEntry);
		
		if(modulesConstants.isNTRRInstance()){
			writer.writeNext(toPrimitive(ServiceConstants.EXPORT_NTI_CSV_HEADERS));
		}else{
		writer.writeNext(toPrimitive(ServiceConstants.EXPORT_CSV_HEADERS));
		}
		
		if (elementList == null) {
			writer.close();
			throw new NullPointerException("Data Element cannot be null");
		}
		List<String> currentRow = new ArrayList<String>();
		for (DataElement currentDataElement : elementList) {
			if (currentDataElement == null) {
				writer.close();
				throw new NullPointerException("Data Element cannot be null");
			}
			dataElmentToStringArray(currentRow, currentDataElement);
			writer.writeNext(currentRow.toArray(new String[0]));
			currentRow.clear();
		}
		writer.flush();
		zos.closeEntry();		
		// readMe.txt
		ZipEntry readMe = new ZipEntry("Read Me.txt");
		zos.putNextEntry(readMe);
				
		outputStream.write("If you change the content of the field [variable name] in dataElementExport.csv, you need to modify the Element Names in formStructure.csv to make sure they are consistent\r\n");
		outputStream.write("Please DO NOT modify the following fields\r\n");
		outputStream.write("===In dataElementExport.csv ===\r\n");
		outputStream.write("Field L: [permissible values],\r\nField M: [permissible value descriptions],\r\nField N: [permissible value output codes],\r\nField O: [Item Response OID],\r\nField P: [Element OID]\r\n");
		outputStream.write("===In formStructure.csv ===\r\n");
		outputStream.write("Field I: [is CAT],\r\nField J: [CAT OID],\r\nField K: [Measurement Type]");
		
				
		outputStream.flush();
		
		try {			
			writer.close();
			outputStream.close();
			zos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return baos;
	}
	
	private String[] createFormStructureHeader() {
		List<String> lineBufferList = new ArrayList<String>();
		lineBufferList.add("Title");
		lineBufferList.add("Short Name");
		lineBufferList.add("Description");
		lineBufferList.add("Organization");
		//lineBufferList.add("Required Program Form");
		lineBufferList.add("Standardization");
		lineBufferList.add("Form Type");
		lineBufferList.add("is Required Program Form");
		lineBufferList.add("is CopyRight");
		lineBufferList.add("is CAT");
		lineBufferList.add("CAT OID");
		lineBufferList.add("Measurement Type");
		String[] lineBuffer = new String[lineBufferList.size()];
		lineBuffer = lineBufferList.toArray(lineBuffer);
		return lineBuffer;
	}

	private String[] createFormStructureInfo(FormStructure formStructure) {
		List<String> lineBufferList = new ArrayList<String>();
		lineBufferList.add(formStructure.getTitle());//Title
		lineBufferList.add(formStructure.getShortName());//Short Name
		lineBufferList.add(formStructure.getDescription());//Description
		lineBufferList.add(formStructure.getOrganization());//Organization
		//lineBufferList.add("Required Program Form");
		lineBufferList.add(formStructure.getStandardization().name());//Standardization
		lineBufferList.add(formStructure.getFileType().name());//Form Type
		lineBufferList.add("false");//is Required Program Form
		lineBufferList.add(formStructure.getIsCopyrighted().toString());//isCopyRight
		lineBufferList.add(String.valueOf(formStructure.isCAT()));//isCat
		lineBufferList.add(formStructure.getCatOid());//CAT_OID
		lineBufferList.add(formStructure.getMeasurementType());// measurementType
		String[] lineBuffer = new String[lineBufferList.size()];
		lineBuffer = lineBufferList.toArray(lineBuffer);
		return lineBuffer;
	}
	
	private String[] createdAssociatedDiseaseTitle() {
		List<String> lineBufferList = new ArrayList<String>();
		lineBufferList.add("Assosiated Diseases");
		String[] lineBuffer = new String[lineBufferList.size()];
		lineBuffer = lineBufferList.toArray(lineBuffer);
		return lineBuffer;
	}
	
	private String[] createdAssociatedDisease(FormStructure formStructure) {
		Set<DiseaseStructure> diseases = formStructure.getDiseaseList();
		List<String> lineBufferList = new ArrayList<String>();
		for(DiseaseStructure disease:diseases) {
			lineBufferList.add(disease.getDisease().getName());
		}		
		String[] lineBuffer = new String[lineBufferList.size()];
		lineBuffer = lineBufferList.toArray(lineBuffer);
		return lineBuffer;
	}
	
	private String[] createGroupTitle(RepeatableGroup group) {
		List<String> lineBufferList = new ArrayList<String>();
		lineBufferList.add("GROUP-");
		lineBufferList.add("Group Name");
		lineBufferList.add("Repeatable Type");
		lineBufferList.add("Threshold");
		lineBufferList.add("Position Number");
		String[] lineBuffer = new String[lineBufferList.size()];
		lineBuffer = lineBufferList.toArray(lineBuffer);
		return lineBuffer;
	}
	
	private String[] createGroup(RepeatableGroup group) {
		List<String> lineBufferList = new ArrayList<String>();
		lineBufferList.add(""); // leave space
		lineBufferList.add(group.getName());//"Group Name"
		lineBufferList.add(group.getType().name());//"Repeatable Type"
		lineBufferList.add(group.getThreshold().toString());//Threshold
		lineBufferList.add(group.getPosition().toString());//Position Number
		String[] lineBuffer = new String[lineBufferList.size()];
		lineBuffer = lineBufferList.toArray(lineBuffer);
		return lineBuffer;
	}
	
	private String[] createElementsTitle(RepeatableGroup group) {
		List<String> lineBufferList = new ArrayList<String>();
		lineBufferList.add("ELEMENTS-");
		lineBufferList.add("Element Name");
		lineBufferList.add("Required");
		lineBufferList.add("Position Number");
		String[] lineBuffer = new String[lineBufferList.size()];
		lineBuffer = lineBufferList.toArray(lineBuffer);
		return lineBuffer;
	}
	
	private String[] createElement(MapElement element) {
		List<String> lineBufferList = new ArrayList<String>();
		StructuralDataElement sde = element.getStructuralDataElement();
		String name = sde.getName();
		lineBufferList.add("");// leave space
		lineBufferList.add(name);//Element Name
		if(name.equalsIgnoreCase("GUID")) {
			lineBufferList.add("REQUIRED");
		}else{
			lineBufferList.add("RECOMMENDED");
		}		
		lineBufferList.add(element.getPosition().toString());//Position Number
		String[] lineBuffer = new String[lineBufferList.size()];
		lineBuffer = lineBufferList.toArray(lineBuffer);
		return lineBuffer;
	}
	
	//=======================================
	/**
	 * This converts a List of keywords into a single semi-colon delimited string list. Works on labels too.
	 * 
	 * @param keywordSet
	 * @return
	 */
	private String keywordListToString(Set<Keyword> keywordSet) {

		StringBuffer kwBuffer = new StringBuffer();

		if (keywordSet != null && !keywordSet.isEmpty()) {

			for (Keyword k : keywordSet) {
				kwBuffer.append(k.getKeyword()).append(ServiceConstants.CSV_LIST_SEPARATER);
			}

			int lastDelimitorIndex = kwBuffer.length() - ServiceConstants.CSV_LIST_SEPARATER.length();
			kwBuffer.replace(lastDelimitorIndex, kwBuffer.length(), ServiceConstants.EMPTY_STRING);
		}

		return kwBuffer.toString();
	}

	private String pvExternalCodesToCSVString(Set<ValueRange> valueRanges, String schemaName) {
		StringBuffer pvBuffer = new StringBuffer();
		if (valueRanges.size() > 0) {
			// order here works the same as it does in permissibleValueDescriptionToCSVString
			// if either is wrong, change both
			for (ValueRange vr : valueRanges) {
				if (pvBuffer.length() > 0) {
					pvBuffer.append(ServiceConstants.CSV_LIST_SEPARATER);
				}
				SchemaPv spv = vr.getSchemaPvBySchema(schemaName);
				if (spv == null) {
					pvBuffer.append(ServiceConstants.EMPTY_STRING);
				} else {
					pvBuffer.append(spv.getPermissibleValue());
				}
			}
			// ServiceConstants.CSV_LIST_SEPARATER
			// int lastDelimitorIndex = pvBuffer.length() - ServiceConstants.CSV_LIST_SEPARATER.length();
			// pvBuffer.replace(lastDelimitorIndex, pvBuffer.length(), ServiceConstants.EMPTY_STRING);

		}
		return pvBuffer.toString();
	}

	/**
	 * @throws IOException
	 * @throws Exception
	 * @inheritDoc
	 */
	// TODO: Should have the functionality to parse xml in the future.
	public List<DataElement> parseDataElement(File upload, String uploadContentType,
			HashMap<String, ArrayList<String>> pvValidateMap) throws IOException {

		List<DataElement> list = new ArrayList<DataElement>();
		CSVReader reader = null;

		try {
			reader = new CSVReader(new FileReader(upload), CSVParser.DEFAULT_SEPARATOR,
					CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.NULL_CHARACTER);
			TBIMappingStrategy strat = new TBIMappingStrategy();
			strat.setType(DataElement.class);
			list = csvToDataElement.parse(strat, reader, pvValidateMap);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			throw new IOException(e.getMessage(), e);
		} catch (InstantiationException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (IntrospectionException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				throw new IOException(e); // cant handle this, throw up
			}
		}

		return list;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isStringValid(String string) {

		return (string != null && !ServiceConstants.EMPTY_STRING.equals(string.trim()));
	}

	/**
	 * @inheritDoc
	 */
	public boolean validateAlias(FormStructure dataStructure, Alias alias, DataElement dataElement) {

		if (dataElement.getName() == null) {
			dataElement.setName("");
		}

		if (alias.getName() == null) {
			alias.setName("");
		}

		alias.setName(alias.getName().trim());

		// Check current DE name (for create)
		if (dataElement != null && dataElement.getName().trim().equals(alias.getName())) {
			return false;
		}

		if (ServiceConstants.EMPTY_STRING.equals(alias.getName())) {
			return false;
		}

		// Alias Names must be unique alias names, but must also not be data element names!
		if ((aliasDao.getAliasByName(alias.getName()) != null)
				|| (getLatestDataElementByName(alias.getName()) != null)) {
			return false;
		}

		if (dataElement != null && dataElement.getAliasList() != null) {
			for (Alias testAlias : dataElement.getAliasList()) {
				String t1 = testAlias.getName();
				String t2 = alias.getName();
				if (t1.equalsIgnoreCase(t2)) {
					return false;
				}
			}
		}

		// Validate against and mapped elements of dataStructure
		if (dataStructure != null) {
			for (DataElement me : dataStructure.getDataElements().values()) {
				StructuralDataElement d = me.getStructuralObject();
				if (d.getName().equalsIgnoreCase(alias.getName())) {
					return false;
				}
				for (Alias a : d.getAliasList()) {
					if (a.getName().equalsIgnoreCase(alias.getName())) {
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * @inheritDoc
	 */
	public void moveMapElementInList(Long mapElementId, Integer newPosition, Long repeatableGroupId,
			FormStructure dataStructure) {

		MapElement me = removeMapElementFromList(mapElementId, repeatableGroupId, dataStructure);

		me.setPosition(newPosition + 1);

		addMapElementToList(me, findRepeatableGroupInList(repeatableGroupId, dataStructure));
	}

	/**
	 * @inheritDoc
	 */
	public void moveGroupInList(Long repeatableGroupId, Integer newPosition, FormStructure dataStructure) {

		// Remove the repeatable group from its old position
		RepeatableGroup movedGroup = removeRepeatableGroupFromList(repeatableGroupId, dataStructure);
		movedGroup.setPosition(newPosition);

		// Move elements to a new list, and add the group back in at the correct spot.
		LinkedHashSet<RepeatableGroup> newList = new LinkedHashSet<RepeatableGroup>();
		for (RepeatableGroup rg : dataStructure.getRepeatableGroups()) {
			newList.add(rg);

			if (rg.getPosition().equals(newPosition - 1)) {
				newList.add(movedGroup);
			}
			if (rg.getPosition() >= newPosition) {
				rg.setPosition(rg.getPosition() + 1);
			}
		}

		dataStructure.setRepeatableGroups(newList);
	}

	/**
	 * @inheritDoc
	 */
	public List<Keyword> getAllKeywords() {

		return keywordDao.getAll();
	}

	/**
	 * @inheritDoc
	 */
	public List<Keyword> getAllLabels() {

		return labelDao.getAll();
	}

	/**
	 * @inheritDoc
	 */
	private void validateDataStructure(FormStructure dataStructure, EntityMap permission, List<String> errors,
			List<String> warnings) {

		if (!validateShortName(dataStructure, dataStructure.getShortName())) {
			errors.add("Data Structure Short Names must be unique");
		}

	}

	public FormStructure saveFormStructure(FormStructure formStructure) {
		return formStructureDao.save(formStructure);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean validateShortName(FormStructure dataStructure, String newShortName) {

		// oldName == newName: The name has not changed. Skip the rest of the validation.
		if (dataStructure != null && dataStructure.getShortName() != null
				&& dataStructure.getShortName() == newShortName) {
			return true;
		}
		// Since we know the name has been changed we don't have to worry about false negatives caused by previous
		// versions.

		// Get all the form structures with the same short name and see if the match is because we are editing this data
		// element.
		List<FormStructure> sameName = formStructureDao.findByShortName(newShortName);
		if (sameName != null && !sameName.isEmpty()) {
			boolean found = false;
			for (FormStructure fs : sameName) {
				if (fs.getId().equals(dataStructure.getId())) {
					found = true;
				}
			}
			if (!found) {
				return false;
			}
		}

		logger.debug("return true");
		return true;
	}

	/**
	 * @inheritDoc
	 */
	public Boolean validateDataElementName(DataElement dataElement, String oldName) {

		// oldName == newName: The name has not changed. Skip the rest of the validation.
		if (oldName != null && oldName == dataElement.getName()) {
			return true;
		}
		// Since we know the name has been changed we don't have to worry about false negatives caused by previous
		// versions.

		// Data Element Names can not conflict with existing alias names
		Alias dbAlias = aliasDao.getAliasByName(dataElement.getName());

		if (dbAlias != null && (dbAlias.getDataElement().getId() != dataElement.getId())) {
			logger.debug("dbalias");
			return false;
		}

		// Check current Alias's incase some were added

		for (Alias alias : dataElement.getAliasList()) {
			if (alias.getName().trim().equals(dataElement.getName().trim())) {
				logger.debug("currentalias");
				return false;
			}
		}

		// Get all the data elements with the same shortname and see if the match is because we are editing this data
		// element.
		List<StructuralDataElement> sameName = structuralDataElementDao.findByShortName(dataElement.getName());
		if (sameName != null && !sameName.isEmpty()) {
			boolean found = false;
			for (StructuralDataElement de : sameName) {
				if (de.getId().equals(dataElement.getId())) {
					found = true;
				}
			}
			if (!found) {
				return false;
			}
		}

		logger.debug("return true");
		return true;
	}

	public Boolean validateKeywordName(String keywordName) {

		List<Keyword> keyword = keywordDao.getAll(); // Dev Note MG: I'm using this method in lieu of the getByName()
													 // because the getByName wasnt retriving Keywords that existed
		for (Keyword key : keyword) {
			if (key.getKeyword().equals(keywordName)) {
				return true;
			}

		}

		return false;
	}

	/**
	 * @inheritDoc
	 */
	public Boolean validateLabelName(String labelName) {

		List<Keyword> labels = labelDao.getAll();
		for (Keyword label : labels) {
			if (label.getKeyword().equals(labelName)) {
				return true;
			}

		}

		return false;

	}

	/**
	 * @inheritDoc
	 */
	public Boolean validateRepeatableGroupName(RepeatableGroup repeatableGroup) {

		boolean valid = true;
		List<Long> list = repeatableGroupDao.getIdsByNameAndDS(repeatableGroup.getName(),
				repeatableGroup.getDataStructure().getId());
		for (Long id : list) {
			if (!id.equals(repeatableGroup.getId())) {
				valid = false;
			}
		}
		return valid;
	}

	/**
	 * @inheritDoc
	 */
	public Boolean validateRepeatableGroupName(String groupName, Long formId) {

		boolean valid = true;
		List<Long> list = repeatableGroupDao.getIdsByNameAndDS(groupName, formId);

		if (list.size() > 0) {
			valid = false;
		}

		return valid;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isPermissibleValueUnique(Collection<ValueRange> valueRanges, String permissibleValue) {

		for (ValueRange valueRange : valueRanges) {
			if (permissibleValue.equalsIgnoreCase(valueRange.getValueRange())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Validates Data Element information
	 * 
	 * @param dataElement
	 * @param repeatableGroup
	 * @param errors
	 * @param warnings
	 */
	private void validateDataElement(DataElement dataElement, List<String> errors, List<String> warnings) {

		// Check Data Element Fields
		if (dataElement.getName() == null || dataElement.getName().trim().equals("")) {
			errors.add(ServiceConstants.NO_NAME_DE);
		}

		if (!validateDataElementName(dataElement, null)) {
			// If something in the database matches the data element name
			// but does not have the same dataElement Id than throw an
			// error.

			errors.add(ServiceConstants.NOT_UNIQUE_DE);
		}

		if (dataElement.getSubDomainElementList() != null) {
			for (SubDomainElement subDomainElement : dataElement.getSubDomainElementList()) {
				validateSubDomainElement(subDomainElement, errors, warnings);
			}
		}
	}

	/**
	 * @inheritDoc
	 */

	private void validateRepeatableGroup(RepeatableGroup repeatableGroup, FormStructure dataStructure,
			List<String> errors, List<String> warnings) {

		// Validate Data Structure
		if (repeatableGroup.getDataStructure() == null) {
			if (dataStructure != null) {
				repeatableGroup.setDataStructure(dataStructure.getFormStructureSqlObject());
				warnings.add(ServiceConstants.ADDED_DS_TO_RG + dataStructure.getId());
			} else {
				errors.add(ServiceConstants.NO_DATA_STRUCTURE);
			}
		} else if ((dataStructure != null) && (dataStructure.getFormStructureSqlObject() != null)
				&& (!repeatableGroup.getDataStructure().equals(dataStructure.getFormStructureSqlObject()))) {
			errors.add(ServiceConstants.MISMATCHED_DS);
		}

		// Validate threshold
		if (repeatableGroup.getThreshold() < 0) {
			errors.add(ServiceConstants.INCORRECT_THRESHOLD);
		}

		// Validate type
		try {
			RepeatableType type = repeatableGroup.getType();
			if (type == null) {
				errors.add(ServiceConstants.INCORRECT_RG_TYPE);
			}
		} catch (Exception e) {
			errors.add(ServiceConstants.INCORRECT_RG_TYPE);
		}

		// Validate Name
		String name = repeatableGroup.getName();
		if (name == "" || name == null) {
			errors.add(ServiceConstants.NO_NAME_RG);
		}
	}

	private void validateSubDomainElement(SubDomainElement subDomainElement, List<String> errors,
			List<String> warnings) {

		if (subDomainElement.getDisease() == null) {
			errors.add("A SubDomainElement must be associated with a Disease.");
		}
		if (subDomainElement.getDomain() == null) {
			errors.add("A SubDomainElement must be associated with a Domain.");
		}
		if (subDomainElement.getSubDomain() == null) {
			errors.add("A SubDomainElement must be associated with a SubDomain.");
		}

	}

	/**
	 * @inheritDoc
	 */
	public List<String> validateImportedDataElement(File upload, DataElement dataElement, boolean inAdmin)
			throws IOException {

		List<String> validateList = new ArrayList<String>();

		CSVReader csvReader = new CSVReader(new FileReader(upload));
		List<String[]> fileString = csvReader.readAll();
		HashMap<Integer, ArrayList<Character>> specialCharacterMap = isValidForXml(fileString);

		if (!specialCharacterMap.isEmpty()) {
			for (Integer lineNumber : specialCharacterMap.keySet()) {
				validateList.add(String.format(ServiceConstants.SPEC_CHAR_ERROR,
						specialCharacterMap.get(lineNumber).toString(), lineNumber));
			}
		}

		if (dataElement != null) {

			// if name does not exist, then give this element a fake name for identification.
			if (dataElement.getName() == null || dataElement.getName().trim().equals(ServiceConstants.EMPTY_STRING)) {
				dataElement.setName("[No Name]");
				validateList.add(ServiceConstants.NO_NAME_ERROR);
			} else {
				// the rest of name validation
				if (!isStringValid(dataElement.getName())) {
					validateList.add(ServiceConstants.NAME_READABLE + ServiceConstants.REQUIRED_MESSAGE);
				}

				if (dataElement.getName() != null && StringEscapeUtils.unescapeHtml(dataElement.getName())
						.length() > ServiceConstants.SIZE_LIMIT_30) {
					validateList.add(ServiceConstants.NAME_READABLE + ServiceConstants.OVER_CHARACTER_30);
				}

				if (dataElement.getName() != null
						&& !Pattern.matches(ServiceConstants.DATABASE_NAME_REGEX, dataElement.getName())) {
					validateList.add(ServiceConstants.NAME_READABLE + ServiceConstants.NAME_ERROR);
				}
			}
			// title
			if (!isStringValid(dataElement.getTitle())) {
				validateList.add(ServiceConstants.TITLE_READABLE + ServiceConstants.REQUIRED_MESSAGE);
			}

			if (dataElement.getTitle() != null && StringEscapeUtils.unescapeHtml(dataElement.getTitle())
					.length() > ServiceConstants.SIZE_LIMIT_255) {
				validateList.add(ServiceConstants.TITLE_READABLE + ServiceConstants.OVER_CHARACTER_255);
			}

			// short description
			if (!isStringValid(dataElement.getShortDescription())) {
				validateList.add(ServiceConstants.SHORT_DESCRIPTION_READABLE + ServiceConstants.REQUIRED_MESSAGE);
			}

			if (dataElement.getShortDescription() != null && StringEscapeUtils
					.unescapeHtml(dataElement.getShortDescription()).length() > ServiceConstants.SIZE_LIMIT_255) {
				validateList.add(ServiceConstants.SHORT_DESCRIPTION_READABLE + ServiceConstants.SIZE_LIMIT_255);
			}
			// size

			if ((DataType.ALPHANUMERIC.equals(dataElement.getType())
					|| DataType.BIOSAMPLE.equals(dataElement.getType()))) {
				if (InputRestrictions.FREE_FORM.equals(dataElement.getRestrictions())) {
					if (dataElement.getSize() == null) {
						validateList.add(ServiceConstants.SIZE_READABLE + ServiceConstants.REQUIRED_MESSAGE);
					} else {
						/**
						 * If a DataElement is of type BioSample then the size needs to be see even if was previously
						 * null.
						 */
						if (DataType.BIOSAMPLE.equals(dataElement.getType())
								&& dataElement.getSize() > ServiceConstants.MAX_BIOSAMPLE_LENGTH) {
							dataElement.setSize(ServiceConstants.MAX_BIOSAMPLE_LENGTH);
							validateList.add(ServiceConstants.SIZE_TOO_LARGE);
						} else if ((DataType.ALPHANUMERIC.equals(dataElement.getType())
								&& (dataElement.getSize() > 4000))) {
							validateList.add(ServiceConstants.SIZE_READABLE + ServiceConstants.BAD_SIZE);
						}
					}
				} else {
					if (dataElement.getSize() != null) {
						validateList.add(ServiceConstants.SIZE_READABLE + ServiceConstants.MISPLACE_SIZE);
					}
				}
			}

			// if ((!DataType.ALPHANUMERIC.equals(dataElement.getType())
			// && !DataType.BIOSAMPLE.equals(dataElement.getType()) && !InputRestrictions.FREE_FORM
			// .equals(dataElement.getRestrictions())) && dataElement.getSize() != null)
			// {
			// validateList.add(ServiceConstants.SIZE_READABLE + ServiceConstants.MISPLACE_SIZE);
			// }

			if (!DataType.ALPHANUMERIC.equals(dataElement.getType())
					&& !DataType.BIOSAMPLE.equals(dataElement.getType())) {
				if (dataElement.getSize() != null) {
					validateList.add(ServiceConstants.SIZE_READABLE + ServiceConstants.MISPLACE_SIZE);
				}
			}

			if (dataElement.getMeasuringUnit() != null) {
				if (!(DataType.ALPHANUMERIC.equals(dataElement.getType())
						|| DataType.NUMERIC.equals(dataElement.getType()))) {
					dataElement.setMeasuringUnit(null);
					validateList.add(ServiceConstants.MEASUREMENT_UNIT_READABLE + ServiceConstants.MISPLACE_MU);
				}
			}

			// min<max
			if (dataElement.getMinimumValue() != null && dataElement.getMaximumValue() != null
					&& dataElement.getMinimumValue().compareTo(dataElement.getMaximumValue()) >= 0) {
				validateList.add(ServiceConstants.MIN_MAX_ERROR);
			}

			// type
			if (dataElement.getType() == null) {
				validateList.add(ServiceConstants.TYPE_READABLE + ServiceConstants.REQUIRED_MESSAGE);
			} else {

				if (dataElement.getRestrictions() != null
						&& !InputRestrictions.FREE_FORM.equals(dataElement.getRestrictions())) {
					if (DataType.GUID.equals(dataElement.getType()) || DataType.FILE.equals(dataElement.getType())
							|| DataType.DATE.equals(dataElement.getType())
							|| DataType.THUMBNAIL.equals(dataElement.getType())
							|| DataType.TRIPLANAR.equals(dataElement.getType())) {
						validateList.add(ServiceConstants.TYPE_ERROR1 + dataElement.getType().getValue()
								+ ServiceConstants.TYPE_ERROR2);
					}
				}

				if (!DataType.NUMERIC.equals(dataElement.getType())) {
					// Only if the dE is free form
					if (InputRestrictions.FREE_FORM.equals(dataElement.getRestrictions())) {
						if (dataElement.getMinimumValue() != null) {
							validateList
									.add(ServiceConstants.MINIMUM_VALUE_READABLE + ServiceConstants.PROHIBITED_MESSAGE);
						}

						if (dataElement.getMaximumValue() != null) {
							validateList
									.add(ServiceConstants.MAXIMUM_VALUE_READABLE + ServiceConstants.PROHIBITED_MESSAGE);
						}
					}
				}
			}

			// population
			if (dataElement.getPopulation() == null) {
				validateList.add(ServiceConstants.POPULATION_READABLE + ServiceConstants.REQUIRED_MESSAGE);
			}

			// category
			if (dataElement.getCategory() == null) {
				validateList.add(ServiceConstants.CATEGORY_READABLE + ServiceConstants.REQUIRED_MESSAGE);
			} else if (!inAdmin && dataElement.getCategory().getId().equals(1L)) {
				validateList.add(ServiceConstants.ADMIN_CATEGORY_ERROR);
			}

			// description
			if (dataElement.getDescription() != null && StringEscapeUtils.unescapeHtml(dataElement.getDescription())
					.length() > ServiceConstants.SIZE_LIMIT_4000) {
				validateList.add(ServiceConstants.DEFINITION_READABLE + ServiceConstants.SIZE_LIMIT_4000);
			}

			// restriction
			if (dataElement.getRestrictions() == null) {
				validateList.add(ServiceConstants.RESTRICTIONS_READABLE + ServiceConstants.REQUIRED_MESSAGE);
			} else {
				if (dataElement.getType() != null
						&& (DataType.ALPHANUMERIC.equals(dataElement.getType())
								|| DataType.NUMERIC.equals(dataElement.getType()))
						&& !InputRestrictions.FREE_FORM.equals(dataElement.getRestrictions())
						&& dataElement.getValueRangeList().isEmpty()) {
					validateList.add(ServiceConstants.PERMISSIBLE_VALUES_READABLE + ServiceConstants.REQUIRED_MESSAGE);
				} else if (dataElement.getType() != null
						&& (DataType.ALPHANUMERIC.equals(dataElement.getType())
								|| DataType.NUMERIC.equals(dataElement.getType()))
						&& !InputRestrictions.FREE_FORM.equals(dataElement.getRestrictions())
						&& !dataElement.getValueRangeList().isEmpty()) {
					// Iterate through the set of valueRanges
					// For each valueRanges check if the description is empty
					// If the valueRange description is null
					// Add message that ValueRange of the DE needs a value description
					// for (ValueRange valueRange : dataElement.getValueRangeList())
					// {
					// if (valueRange.getDescription() == null
					// || valueRange.getDescription().equals(ServiceConstants.EMPTY_STRING))
					// {
					// validateList.add(ServiceConstants.MISSING_DESCRIPTION_VALUE
					// + valueRange.getValueRange() + " in ");
					// }
					// }

					/**
					 * Dev's Note Matt Green: This has been commented out for bug fix BRIC-1669. I commented it out
					 * instead removing the code because this functionality was asked for in a seperate story/bug fix.
					 * Just in case its needed.
					 */
				}
			}

			// valueRange
			for (ValueRange valueRange : dataElement.getValueRangeList()) {
				Set<ValueRange> valueRangeWithoutCurrent = new HashSet<ValueRange>(dataElement.getValueRangeList());
				valueRangeWithoutCurrent.remove(valueRange);
				if (valueRange.getValueRange() == null && valueRange.getDescription() != null) {
					validateList.add(ServiceConstants.MISSING_RANGE_VALUE + valueRange.getDescription() + " for ");
					break;
				}

				if (valueRange.getValueRange() != null
						&& !isPermissibleValueUnique(valueRangeWithoutCurrent, valueRange.getValueRange())) {
					validateList.add(ServiceConstants.PERMISSIBLE_VALUES_READABLE + ServiceConstants.NOT_UNIQUE
							+ valueRange.getValueRange() + " for ");
					break;
				}

				if (valueRange.getDescription() != null && valueRange.getDescription().length() > 1000) {
					validateList.add(ServiceConstants.PERMISSIBLE_VALUES_DESCRIPTION_READABLE
							+ ServiceConstants.OVER_CHARACTER_1000);
				}

				if (valueRange.getValueRange() != null && valueRange.getValueRange().length() > 200) {
					validateList
							.add(ServiceConstants.PERMISSIBLE_VALUES_READABLE + ServiceConstants.OVER_CHARACTER_200);
				}

				if (DataType.NUMERIC.equals(dataElement.getType())) {
					try {
						Double.valueOf(valueRange.getValueRange());
					} catch (NumberFormatException e) {
						validateList
								.add(ServiceConstants.PERMISSIBLE_VALUES_READABLE + ServiceConstants.NUMERIC_MISMATCH);
						break;
					}
				}
			}

			// Labels
			if (!inAdmin && dataElement.getLabels() != null && !dataElement.getLabels().isEmpty()) {
				validateList.add(ServiceConstants.ADMIN_LABEL_ERROR);
			}

			// notes
			if (dataElement.getNotes() != null && StringEscapeUtils.unescapeHtml(dataElement.getNotes())
					.length() > ServiceConstants.SIZE_LIMIT_4000) {
				validateList.add(ServiceConstants.NOTES_READABLE + ServiceConstants.OVER_CHARACTER_4000);
			}

			// guidelines
			if (dataElement.getGuidelines() != null && StringEscapeUtils.unescapeHtml(dataElement.getGuidelines())
					.length() > ServiceConstants.SIZE_LIMIT_4000) {
				validateList.add(ServiceConstants.GUIDELINES_READABLE + ServiceConstants.OVER_CHARACTER_4000);
			}

			// historical notes
			if (dataElement.getHistoricalNotes() != null && StringEscapeUtils
					.unescapeHtml(dataElement.getHistoricalNotes()).length() > ServiceConstants.SIZE_LIMIT_4000) {
				validateList.add(ServiceConstants.HISTORICAL_NOTES_READABLE + ServiceConstants.OVER_CHARACTER_4000);
			}

			// reference
			if (dataElement.getReferences() != null && StringEscapeUtils.unescapeHtml(dataElement.getReferences())
					.length() > ServiceConstants.SIZE_LIMIT_4000) {
				validateList.add(ServiceConstants.REFERENCES_READABLE + ServiceConstants.OVER_CHARACTER_4000);
			}

			// preferred question text
			if (dataElement.getSuggestedQuestion() != null && StringEscapeUtils.unescapeHtml(dataElement.getSuggestedQuestion())
					.length() > ServiceConstants.SIZE_LIMIT_4000) {
				validateList.add(ServiceConstants.PREFERRED_QUESTION_TEXT_READABLE + ServiceConstants.OVER_CHARACTER_4000);
			}
			// external IDs
			if (dataElement.getExternalIdSet() != null) {
				for (ExternalId eId : dataElement.getExternalIdSet()) {
					if (eId.getValue() != null && eId.getValue().length() > ServiceConstants.SIZE_LIMIT_55) {
						validateList.add(ServiceConstants.EXTERNAL_ID_READABLE + " " + eId.getSchema().getName()
								+ ServiceConstants.OVER_CHARACTER_55);
					}
				}
			}

			// Keywords
			for (Keyword k : dataElement.getKeywords()) {
				if (k != null && k.getKeyword() != null
						&& !Pattern.matches(ServiceConstants.DATABASE_NAME_REGEX, k.getKeyword())) {
					validateList.add(ServiceConstants.KEYWORD_LIST_READABLE + ServiceConstants.NAME_ERROR);
				}

				if (k != null && k.getKeyword().length() > ServiceConstants.SIZE_LIMIT_55) {
					validateList.add(ServiceConstants.KEYWORD_LIST_READABLE + ServiceConstants.OVER_CHARACTER_55);
				}
			}

			// Labels (same as keywords)
			if (dataElement.getLabels() != null) {
				for (Keyword k : dataElement.getLabels()) {
					if (k != null && k.getKeyword() != null
							&& !Pattern.matches(ServiceConstants.DATABASE_NAME_REGEX, k.getKeyword())) {
						validateList.add(ServiceConstants.LABEL_READABLE + ServiceConstants.NAME_ERROR);
					}

					if (k != null && k.getKeyword().length() > ServiceConstants.SIZE_LIMIT_55) {
						validateList.add(ServiceConstants.LABEL_READABLE + ServiceConstants.OVER_CHARACTER_55);
					}
				}
			}
			// disease and related fields
			if (dataElement.getSubDomainElementList() == null || dataElement.getSubDomainElementList().size() == 0) {
				// case: There are no diseases listed
				validateList.add(ServiceConstants.DISEASE_LIST_READABLE + ServiceConstants.REQUIRED_MESSAGE);
			} else {
				// Classification, domain, subdomian are validated for each diseaseElment object (disease can be assumed
				// to be valid here).
				for (SubDomainElement se : dataElement.getSubDomainElementList()) {
					Disease disease = se.getDisease();
					// classification
					if (dataElement.getClassificationElementList() == null
							|| dataElement.getClassificationElementList().size() == 0) {
						for (Subgroup s : getSubgroupsByDisease(disease)) {
							validateList.add(ServiceConstants.CLASSIFICATION_ERROR1 + s.getSubgroupName()
									+ ServiceConstants.CLASSIFICATION_ERROR2);
						}
					} else {
						for (Subgroup s : getSubgroupsByDisease(disease)) {
							boolean subgroupFound = false;
							for (ClassificationElement ce : dataElement.getClassificationElementList()) {
								if (ce.getSubgroup().getSubgroupName().equals(s.getSubgroupName())) {
									subgroupFound = true;

									// check if classification is valid for that disease
									boolean classificationFound = false;
									for (Classification classification : getClassificationList(disease, true)) {
										if (ce.getClassification() != null
												&& classification.getName().equals(ce.getClassification().getName())) {
											classificationFound = true;
										}

										if (!inAdmin) {
											if (classification.getName().equals(ce.getClassification().getName())) {
												if (!classification.getCanCreate()) {
													validateList.add(ServiceConstants.ADMIN_CLASSIFICATION_ERROR1
															+ s.getSubgroupName()
															+ ServiceConstants.ADMIN_CLASSIFICATION_ERROR2
															+ classification.getName()
															+ ServiceConstants.ADMIN_CLASSIFICATION_ERROR3);
												}
											}
										}
									}

									if (!classificationFound) {
										// This code will execute if the domain is a valid entry, but the classification
										// is invalid. This allows us to avoid
										// the NPE thrown by getName() when classification is null, while keeping the
										// desired behavior otherwise
										String message = (ce.getClassification() != null) ? ce.getClassification()
												.getName() : s.getSubgroupName();
										validateList.add(ServiceConstants.BAD_CLASSIFICATION + message
												+ ServiceConstants.CLASSIFICATION_ERROR2);
									}
								}
							}
							if (!subgroupFound) {
								validateList.add(ServiceConstants.CLASSIFICATION_ERROR1 + s.getSubgroupName()
										+ ServiceConstants.CLASSIFICATION_ERROR2);
							}
						}
					}

					// domains + subdomains
					if (se.getDomain() == null) {
						validateList.add(ServiceConstants.DOMAIN_READABLE + ServiceConstants.REQUIRED_MESSAGE
								+ disease.getName() + " for data element: ");
					} else {

						boolean valid = false;
						// If the disease is general then any domain is valid
						if (disease != null && disease.getName() != null
								&& disease.getName().equals("General (For all diseases)")) {
							valid = true;
						}

						// checks if the domain is valid for a particular disease
						for (Domain validDomain : getDomainsByDisease(disease)) {
							if (validDomain.getName().equals(se.getDomain().getName())) {
								valid = true;
							}
						}

						if (!valid) {
							validateList.add(ServiceConstants.DOMAIN_READABLE + ServiceConstants.BAD_DOMAIN_1
									+ disease.getName() + ServiceConstants.BAD_DOMAIN_2);
						}

						// duplicate domains/subdomain pairs
						for (SubDomainElement otherSE : dataElement.getSubDomainElementList()) {
							if (se != otherSE && otherSE.getDisease() != null && otherSE.getDisease().getName() != null
									&& otherSE.getDomain() != null && otherSE.getDomain().getName() != null
									&& otherSE.getSubDomain() != null && otherSE.getSubDomain().getName() != null
									&& otherSE.getDisease().getName().equals(se.getDisease().getName())
									&& otherSE.getDomain().getName().equals(se.getDomain().getName())
									&& otherSE.getSubDomain().getName().equals(se.getSubDomain().getName())) {
								validateList.add(otherSE.getDomain().getName() + "." + otherSE.getSubDomain().getName()
										+ ServiceConstants.DUPLICATE_DOMAIN + otherSE.getDisease().getName()
										+ ServiceConstants.BAD_DOMAIN_2);
							}
						}

					}

					// subdomain
					if (se.getSubDomain() == null) {
						validateList.add(ServiceConstants.SUBDOMAIN_READABLE + ServiceConstants.REQUIRED_MESSAGE);
					}
					if (se.getSubDomain() != null && se.getDomain() != null
							&& !domainSubDomainDao.legalPair(se.getDisease(), se.getDomain(), se.getSubDomain())) {
						validateList.add(
								se.getSubDomain().getName() + ServiceConstants.BAD_SUBDOMAIN + se.getDomain().getName()
										+ ServiceConstants.BAD_SUBDOMAIN_2 + se.getDisease().getName() + " :");
					}

				}
				// Check for extra classifications
				for (ClassificationElement ce : dataElement.getClassificationElementList()) {
					Disease disease = ce.getDisease();
					boolean subdomainFound = false;
					for (SubDomainElement se : dataElement.getSubDomainElementList()) {
						if (disease.equals(se.getDisease())) {
							subdomainFound = true;
							break;
						}
					}
					if (!subdomainFound) {
						if (ce.getClassification() != null) {
							validateList
									.add(ce.getSubgroup().getSubgroupName() + ServiceConstants.EXTRA_CLASSIFICATION);
						} else {
							validateList.add(ServiceConstants.BAD_CLASSIFICATION + ce.getSubgroup().getSubgroupName()
									+ ServiceConstants.CLASSIFICATION_ERROR2);
						}
					}
				}
			}// End disease validation

			validateImportedDEBasicDetails(validateList, dataElement);
		}

		return validateList;
	}

	public List<String> validateImportedDEBasicDetails(List<String> validateList, DataElement dataElement) {

		// Submitting Org Name
		if (dataElement.getSubmittingOrgName() != null && dataElement.getSubmittingOrgName().length() != 0) {
			// Max Length
			if (dataElement.getSubmittingOrgName().length() > ServiceConstants.SIZE_LIMIT_255) {
				validateList.add(
						ServiceConstants.SUBMITTING_ORGANIZATION_NAME_READABLE + ServiceConstants.OVER_CHARACTER_255);
			}
			// Required
		} else {
			validateList
					.add(ServiceConstants.SUBMITTING_ORGANIZATION_NAME_READABLE + ServiceConstants.REQUIRED_MESSAGE);
		}

		// Submitting Contact Name
		// Max Length
		if (dataElement.getSubmittingContactName() != null
				&& dataElement.getSubmittingContactName().length() > ServiceConstants.SIZE_LIMIT_255) {
			validateList.add(ServiceConstants.SUBMITTING_CONTACT_NAME_READABLE + ServiceConstants.OVER_CHARACTER_255);
		}

		// Submitting Contact Info
		// Max Length
		if (dataElement.getSubmittingContactInfo() != null
				&& dataElement.getSubmittingContactInfo().length() > ServiceConstants.SIZE_LIMIT_255) {
			validateList.add(
					ServiceConstants.SUBMITTING_CONTACT_INFORMATION_READABLE + ServiceConstants.OVER_CHARACTER_255);
		}

		// Steward Org Name
		if (dataElement.getStewardOrgName() != null && dataElement.getStewardOrgName().length() != 0) {
			// Max Length
			if (dataElement.getStewardOrgName().length() > ServiceConstants.SIZE_LIMIT_255) {
				validateList
						.add(ServiceConstants.STEWARD_ORGANIZATION_NAME_READABLE + ServiceConstants.OVER_CHARACTER_255);
			}
			// Required
		} else {
			validateList.add(ServiceConstants.STEWARD_ORGANIZATION_NAME_READABLE + ServiceConstants.REQUIRED_MESSAGE);
		}

		// Steward Contact Name
		// Max Length
		if (dataElement.getSubmittingContactName() != null
				&& dataElement.getSubmittingContactName().length() > ServiceConstants.SIZE_LIMIT_255) {
			validateList.add(ServiceConstants.STEWARD_CONTACT_NAME_READABLE + ServiceConstants.OVER_CHARACTER_255);
		}

		// Steward Contact Info
		// Max Length
		if (dataElement.getSubmittingContactInfo() != null
				&& dataElement.getSubmittingContactInfo().length() > ServiceConstants.SIZE_LIMIT_255) {
			validateList
					.add(ServiceConstants.STEWARD_CONTACT_INFORMATION_READABLE + ServiceConstants.OVER_CHARACTER_255);
		}

		// Until Date
		// Date not in the past
		if (dataElement.getUntilDate() != null && isDateBeforeTodayDate(dataElement.getUntilDate())) {
			validateList.add(ServiceConstants.UNTIL_DATE_READABLE + ServiceConstants.DATE_IN_PAST);
		}

		return validateList;
	}

	private Boolean isDateBeforeTodayDate(Date date) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		// set the calendar to start of today
		Date today = c.getTime();
		if (date.before(today)) {
			return true;
		}
		return false;
	}

	/**
	 * For DataElements that are already have been published, the values of non core items must be validated against the
	 * preexisting DE. Any errors that are found they are added to the list to be returned.
	 * 
	 * @param ovr
	 * @return
	 */
	public List<String> validateOverwriteDE(DataElement ovr) {

		// Perform Null checks on ALL FIELDs

		List<String> errors = new ArrayList<String>();
		DataElement toOvr = getLatestDataElementByName(ovr.getName());
		if (toOvr != null) {
			// Check to see if the Title is the same
			if (!ovr.getTitle().equals(toOvr.getTitle())) {
				errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.TITLE_READABLE
						+ ServiceConstants.PUBLISHED_DATA_ELEMENT + ServiceConstants.DE_VAR_NAME);
			}

			// Check to see if the Size is the same
			if ((toOvr.getSize() != null && ovr.getSize() == null)
					|| ((toOvr.getSize() == null && ovr.getSize() != null))) {
				errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.SIZE_READABLE
						+ ServiceConstants.PUBLISHED_DATA_ELEMENT + ServiceConstants.DE_VAR_NAME);
			} else if (ovr.getSize() != null && toOvr.getSize() != null) {
				if (!(ovr.getSize().compareTo(toOvr.getSize()) == 0)) {
					// Error the size must be bigger or equal to the previously set size
					errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.SIZE_READABLE
							+ ServiceConstants.PUBLISHED_DATA_ELEMENT + ServiceConstants.DE_VAR_NAME);

				}
			}

			// Check if the Data Types are the same
			if (ovr.getType().ordinal() != toOvr.getType().ordinal()) {
				errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.TYPE_READABLE
						+ ServiceConstants.PUBLISHED_DATA_ELEMENT + ServiceConstants.DE_VAR_NAME);

			}

			// Check if the Measuring Units are the same
			if ((toOvr.getMeasuringUnit() != null && ovr.getMeasuringUnit() == null)
					|| ((toOvr.getMeasuringUnit() == null && ovr.getMeasuringUnit() != null))) {
				errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.MEASUREMENT_UNIT_READABLE
						+ ServiceConstants.PUBLISHED_DATA_ELEMENT + ServiceConstants.DE_VAR_NAME);
			} else if (ovr.getMeasuringUnit() != null && toOvr.getMeasuringUnit() != null) {
				if ((ovr.getMeasuringUnit().getName().compareTo(toOvr.getMeasuringUnit().getName()) != 0)) {
					// Error the size must be bigger or equal to the previously set size
					errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.MEASUREMENT_UNIT_READABLE
							+ ServiceConstants.PUBLISHED_DATA_ELEMENT + ServiceConstants.DE_VAR_NAME);

				}
			}

			// Check to see if the Maximum Value

			if ((toOvr.getMaximumValue() != null && ovr.getMaximumValue() == null)
					|| ((toOvr.getMaximumValue() == null && ovr.getMaximumValue() != null))) {
				errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.MAXIMUM_VALUE_READABLE
						+ ServiceConstants.PUBLISHED_DATA_ELEMENT + ServiceConstants.DE_VAR_NAME);
			} else if ((toOvr.getMaximumValue() != null && ovr.getMaximumValue() != null)) {
				if (ovr.getMaximumValue().compareTo(toOvr.getMaximumValue()) != 0) {
					errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.MAXIMUM_VALUE_READABLE
							+ ServiceConstants.PUBLISHED_DATA_ELEMENT + ServiceConstants.DE_VAR_NAME);

				}
			}

			// Check to see if the Minimum Value

			if ((toOvr.getMinimumValue() != null && ovr.getMinimumValue() == null)
					|| ((toOvr.getMinimumValue() == null && ovr.getMinimumValue() != null))) {
				errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.MINIMUM_VALUE_READABLE
						+ ServiceConstants.PUBLISHED_DATA_ELEMENT);
			} else if ((toOvr.getMaximumValue() != null && ovr.getMaximumValue() != null)) {
				if (ovr.getMinimumValue().compareTo(toOvr.getMinimumValue()) != 0) {
					errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.MINIMUM_VALUE_READABLE
							+ ServiceConstants.PUBLISHED_DATA_ELEMENT);

				}
			}

			// Potential NPE if the imported DE is overwriting yet has errors, the category is used to determine whether
			// not
			// if can be imported
			if (ovr.getCategory() != null) {
				if (!ovr.getCategory().getShortName().equals(toOvr.getCategory().getShortName())) {
					errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.CATEGORY_READABLE
							+ ServiceConstants.PUBLISHED_DATA_ELEMENT);

				}
			}

			if (!validatePermVals(ovr.getValueRangeList(), toOvr.getValueRangeList())) {
				errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.PERMISSIBLE_VALUES_READABLE
						+ ServiceConstants.PUBLISHED_DATA_ELEMENT);

			}

			if (ovr.getRestrictions().getId().compareTo(toOvr.getRestrictions().getId()) != 0) {
				errors.add(ServiceConstants.OVERWRITE_ERROR + ServiceConstants.RESTRICTIONS_READABLE
						+ ServiceConstants.PUBLISHED_DATA_ELEMENT);

			}
		}
		return errors;

	}

	public String overwriteLogger(DataElement overWriter, DataElement getOverWrtn) {

		StringBuilder loggingStatement = new StringBuilder();
		loggingStatement.append("\n" + ServiceConstants.OVERWRITE_NOTICE + ": ");
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		loggingStatement.append(getOverWrtn.getName() + " was overwritten on " + dateFormat.format(date) + "\n");
		loggingStatement.append(ServiceConstants.CHANGES_MADE);
		ArrayList<String> changesMade = compareTo(overWriter, getOverWrtn);
		int newLine = 0;
		if (!changesMade.isEmpty()) {
			for (String s : changesMade) {
				if (newLine == 4) {
					loggingStatement.append("\n");
					newLine = 0;
				}
				loggingStatement.append(s + ", ");
				newLine++;
			}
		}

		return loggingStatement.toString();
	}

	/**
	 * A compare function that is used to build a logging statement that defines what changes were made during the
	 * during overwrite process
	 * 
	 * @param ow
	 * @param getOW
	 * @return
	 */
	public ArrayList<String> compareTo(DataElement ow, DataElement getOW) {

		ArrayList<String> ovrwrtnAtrbts = new ArrayList<String>();
		String arrow = " -> ";
		final String NULL = "null";

		/********************************* DESCRIPTION ************************************/
		if (ow.getDescription() != null && getOW.getDescription() != null) {
			if (!ow.getDescription().equals(getOW.getDescription())) {
				ovrwrtnAtrbts.add(ServiceConstants.DEFINITION_READABLE + ": " + getOW.getDescription().toString()
						+ arrow + ow.getDescription().toString());
			}
		} else {
			if ((getOW.getDescription() != null && ow.getDescription() == null)
					|| ((getOW.getDescription() == null && ow.getDescription() != null))) {
				ovrwrtnAtrbts.add(ServiceConstants.DEFINITION_READABLE + ": "
						+ (getOW.getDescription() != null ? getOW.getDescription().toString() : NULL) + arrow
						+ (ow.getDescription() != null ? ow.getDescription().toString() : NULL));
			}
		}

		/******************************** Short Description ***********************************/
		if (ow.getShortDescription() != null && getOW.getShortDescription() != null) {
			if (!ow.getShortDescription().equals(getOW.getShortDescription())) {
				ovrwrtnAtrbts.add(ServiceConstants.SHORT_DESCRIPTION_READABLE + ": "
						+ getOW.getShortDescription().toString() + arrow + ow.getShortDescription().toString());
			}
		} else {
			if ((getOW.getShortDescription() != null && ow.getShortDescription() == null)
					|| ((getOW.getShortDescription() == null && ow.getShortDescription() != null))) {
				ovrwrtnAtrbts.add(ServiceConstants.SHORT_DESCRIPTION_READABLE + ": "
						+ (getOW.getShortDescription() != null ? getOW.getShortDescription().toString() : NULL) + arrow
						+ (ow.getShortDescription() != null ? ow.getShortDescription().toString() : NULL));
			}
		}

		/*********************************** NOTES *****************************************/

		if (ow.getNotes() != null && getOW.getNotes() != null) {
			if (!ow.getNotes().equals(getOW.getNotes())) {
				ovrwrtnAtrbts.add(ServiceConstants.NOTES_READABLE + ": " + getOW.getNotes().toString() + arrow
						+ ow.getNotes().toString());
			}
		} else if ((getOW.getNotes() != null && ow.getNotes() == null)
				|| ((getOW.getNotes() == null && ow.getNotes() != null))) {
			ovrwrtnAtrbts.add(ServiceConstants.NOTES_READABLE + ": "
					+ (getOW.getNotes() != null ? getOW.getNotes().toString() : NULL) + arrow
					+ (ow.getNotes() != null ? ow.getNotes().toString() : NULL));
		}

		/********************************** GUIDELINES ***************************************/
		if (ow.getGuidelines() != null && getOW.getGuidelines() != null) {
			if (!ow.getGuidelines().equals(getOW.getGuidelines())) {
				ovrwrtnAtrbts.add(ServiceConstants.GUIDELINES_READABLE + ": " + getOW.getGuidelines().toString() + arrow
						+ ow.getGuidelines().toString());
			}
		} else if ((getOW.getGuidelines() != null && ow.getGuidelines() == null)
				|| ((getOW.getGuidelines() == null && ow.getGuidelines() != null))) {
			ovrwrtnAtrbts.add(ServiceConstants.GUIDELINES_READABLE + ": "
					+ (getOW.getGuidelines() != null ? getOW.getGuidelines().toString() : NULL) + arrow
					+ (ow.getGuidelines() != null ? ow.getGuidelines().toString() : NULL));
		}

		/********************************** HISTORICAL NOTES ***************************************/
		if (ow.getHistoricalNotes() != null && getOW.getHistoricalNotes() != null) {
			if (!ow.getHistoricalNotes().equals(getOW.getHistoricalNotes())) {
				ovrwrtnAtrbts.add(ServiceConstants.HISTORICAL_NOTES_READABLE + ": "
						+ getOW.getHistoricalNotes().toString() + arrow + ow.getHistoricalNotes().toString());
			}
		} else if ((getOW.getHistoricalNotes() != null && ow.getHistoricalNotes() == null)
				|| ((getOW.getHistoricalNotes() == null && ow.getHistoricalNotes() != null))) {
			ovrwrtnAtrbts.add(ServiceConstants.HISTORICAL_NOTES_READABLE + ": "
					+ (getOW.getHistoricalNotes() != null ? getOW.getHistoricalNotes().toString() : NULL) + arrow
					+ (ow.getHistoricalNotes() != null ? ow.getHistoricalNotes().toString() : NULL));
		}

		/********************************** REFERENCES **************************************/
		if (ow.getReferences() != null & getOW.getReferences() != null) {
			if (!ow.getReferences().equals(getOW.getReferences())) {
				ovrwrtnAtrbts.add(ServiceConstants.REFERENCES_READABLE + ": " + getOW.getReferences().toString() + arrow
						+ ow.getReferences().toString());
			}
		} else if ((getOW.getReferences() != null && ow.getReferences() == null)
				|| ((getOW.getReferences() == null && ow.getReferences() != null))) {
			ovrwrtnAtrbts.add(ServiceConstants.REFERENCES_READABLE + ": "
					+ (getOW.getReferences() != null ? getOW.getReferences().toString() : NULL) + arrow
					+ (ow.getReferences() != null ? ow.getReferences().toString() : NULL));
		}
		/********************************** MEASURING UNIT ********************************/

		if (ow.getMeasuringUnit() != null && getOW.getMeasuringUnit() != null) {
			if (!ow.getMeasuringUnit().getName().equals(getOW.getMeasuringUnit().getName())) {
				ovrwrtnAtrbts.add(ServiceConstants.MEASUREMENT_UNIT_READABLE + ": "
						+ getOW.getMeasuringUnit().toString() + arrow + ow.getMeasuringUnit().toString());
			}
		} else if ((getOW.getMeasuringUnit() != null && ow.getMeasuringUnit() == null)
				|| ((getOW.getMeasuringUnit() == null && ow.getMeasuringUnit() != null))) {
			ovrwrtnAtrbts.add(ServiceConstants.MEASUREMENT_UNIT_READABLE + ": "
					+ (getOW.getMeasuringUnit() != null ? getOW.getMeasuringUnit().toString() : NULL) + arrow
					+ (ow.getMeasuringUnit() != null ? ow.getMeasuringUnit().toString() : NULL));
		}

		if (!validateClassifications(ow.getClassificationElementList(), getOW.getClassificationElementList())) {
			ovrwrtnAtrbts.add(
					ServiceConstants.CLASSIFICATION_READABLE + ": " + getOW.getClassificationElementList().toString()
							+ arrow + ow.getClassificationElementList().toString());
		}
		if (!validateSubDomainElementList(ow.getSubDomainElementList(), getOW.getSubDomainElementList())) {
			ovrwrtnAtrbts.add(ServiceConstants.DISEASE_LIST_READABLE + ": " + getOW.getSubDomainElementList().toString()
					+ arrow + ow.getSubDomainElementList().toString());
		}

		if (!ow.getTitle().equals(getOW.getTitle())) {
			ovrwrtnAtrbts.add(ServiceConstants.TITLE_READABLE + ": " + getOW.getTitle().toString() + arrow
					+ ow.getTitle().toString());
		}

		/******************************************** SIZE ***************************************************/
		// Check to see if the Size is the same
		if ((getOW.getSize() != null && ow.getSize() == null) || ((getOW.getSize() == null && ow.getSize() != null))) {
			String getOwSize = getOW.getSize() != null ? getOW.getSize().toString() : NULL;
			String owSize = ow.getSize() != null ? ow.getSize().toString() : NULL;
			ovrwrtnAtrbts.add(ServiceConstants.SIZE_READABLE + ": " + getOwSize + arrow + owSize);
		} else {
			if ((getOW.getSize() != null && ow.getSize() != null)) {
				if (!(ow.getSize().compareTo(getOW.getSize()) == 0)) {
					// Error the size must be bigger or equal to the previously set size
					ovrwrtnAtrbts.add(ServiceConstants.SIZE_READABLE + ": " + getOW.getSize().toString() + arrow
							+ ow.getSize().toString());
				}
			}
		}

		// Check if the Data Types are the same
		if (ow.getType().ordinal() != getOW.getType().ordinal()) {
			ovrwrtnAtrbts.add(ServiceConstants.TYPE_READABLE + ": " + getOW.getType().toString() + arrow
					+ ow.getType().toString());

		}

		// Check to see if the Maximum Value

		if ((getOW.getMaximumValue() != null && ow.getMaximumValue() == null)
				|| ((getOW.getMaximumValue() == null && ow.getMaximumValue() != null))) {
			ovrwrtnAtrbts.add(ServiceConstants.MAXIMUM_VALUE_READABLE + ": "
					+ (getOW.getMaximumValue() != null ? getOW.getMaximumValue().toString() : NULL) + arrow
					+ (ow.getMaximumValue() != null ? ow.getMaximumValue().toString() : NULL));
		} else if ((getOW.getMaximumValue() != null && ow.getMaximumValue() != null)) {
			if (ow.getMaximumValue().compareTo(getOW.getMaximumValue()) != 0) {
				ovrwrtnAtrbts.add(ServiceConstants.MAXIMUM_VALUE_READABLE + ": " + getOW.getGuidelines().toString()
						+ arrow + ow.getGuidelines().toString());

			}
		}

		// Check to see if the Minimum Value

		if ((getOW.getMinimumValue() != null && ow.getMinimumValue() == null)
				|| ((getOW.getMinimumValue() == null && ow.getMinimumValue() != null))) {

			ovrwrtnAtrbts.add(ServiceConstants.MINIMUM_VALUE_READABLE + ": "
					+ (getOW.getMinimumValue() != null ? getOW.getMinimumValue().toString() : NULL) + arrow
					+ (ow.getMinimumValue() != null ? ow.getMinimumValue().toString() : NULL));
		} else if ((getOW.getMinimumValue() != null && ow.getMinimumValue() != null)) {
			if (ow.getMinimumValue().compareTo(getOW.getMinimumValue()) != 0) {
				ovrwrtnAtrbts.add(ServiceConstants.MINIMUM_VALUE_READABLE + ": " + getOW.getMinimumValue().toString()
						+ arrow + ow.getMinimumValue().toString());

			}
		}

		if (!ow.getCategory().getId().equals(getOW.getCategory().getId())) {
			ovrwrtnAtrbts.add(ServiceConstants.CATEGORY_READABLE + ": " + getOW.getCategory().toString() + arrow
					+ ow.getCategory().toString());

		}

		if (!validatePermVals(ow.getValueRangeList(), getOW.getValueRangeList())) {
			ovrwrtnAtrbts.add(ServiceConstants.PERMISSIBLE_VALUES_READABLE + ": " + getOW.getValueRangeList().toString()
					+ arrow + ow.getValueRangeList().toString());

		}

		if (!ow.getRestrictions().getId().equals(getOW.getRestrictions().getId())) {
			ovrwrtnAtrbts.add(ServiceConstants.RESTRICTIONS_READABLE + ": " + getOW.getRestrictions().toString() + arrow
					+ ow.getRestrictions().toString());

		}
		return ovrwrtnAtrbts;

	}

	/**
	 * Compares both sets of value ranges by comparing both the permissible value (Value Range) and the permissible
	 * value description (Value Range Description)
	 * 
	 * @param overwrittingValRngSet
	 * @param valRngBeingOverWritten
	 * @return
	 */
	public boolean validatePermVals(Set<ValueRange> overwrittingValRngSet, Set<ValueRange> valRngBeingOverWritten) {

		Set<ValueRange> overwriteCopy = new HashSet<ValueRange>();
		overwriteCopy.addAll(overwrittingValRngSet);

		Set<ValueRange> copyOfOverwritten = new HashSet<ValueRange>();
		copyOfOverwritten.addAll(valRngBeingOverWritten);
		if (overwrittingValRngSet.size() != valRngBeingOverWritten.size()) {
			// If the sizes arent the same then we know that a change has taken place.
			return false;
		}
		Iterator<ValueRange> overwrittenValRngItr = copyOfOverwritten.iterator();
		while (overwrittenValRngItr.hasNext()) {
			ValueRange overwrittenValRng = overwrittenValRngItr.next();
			Boolean foundOverwritingValRng = false;
			Iterator<ValueRange> overwriteValRngItr = overwriteCopy.iterator();
			while (!foundOverwritingValRng && overwriteValRngItr.hasNext()) {
				ValueRange overwrittingValRng = overwriteValRngItr.next();
				if (overwrittenValRng.getValueRange().equalsIgnoreCase(overwrittingValRng.getValueRange())) {
					if (overwrittenValRng.getDescription() != null && !overwrittenValRng.getDescription()
							.equalsIgnoreCase(overwrittingValRng.getDescription())) {
						// If the Value Range are the same but the description are different then we know a change was
						// made
						return false;
					} else {
						foundOverwritingValRng = true;
						overwrittenValRngItr.remove();
					}
				}
			}
			if (!foundOverwritingValRng) {
				// If a match to a perm value was found then we know a change has taken place
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares both sets of Classification Elements by comparing the Classification Elements
	 * 
	 * @param ovr
	 * @param toOvr
	 * @return
	 */
	public boolean validateClassifications(Set<ClassificationElement> ovr, Set<ClassificationElement> toOvr) {

		// Loops through the list of Overwriting classification first
		for (ClassificationElement ovrCE : ovr) {
			if (toOvr.contains(ovrCE)) {
				for (ClassificationElement toOvrCE : toOvr) {
					if (!ovrCE.equals(toOvrCE)) {
						return false;

					}
				}
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares both sets of Disease Elements by comparing the Disease Elements
	 * 
	 * @param ovr
	 * @param toOvr
	 * @return
	 */
	public boolean validateSubDomainElementList(Set<SubDomainElement> ovr, Set<SubDomainElement> toOvr) {

		if (ovr.size() != toOvr.size()) {
			return false;
		}
		return true;
	}

	/**
	 * Compares both sets of Keywords using the their keyword strings for comparison. Works for labels too
	 * 
	 * @param ovr
	 * @param toOvr
	 * @return
	 */
	public boolean validateKeywordList(Set<Keyword> ovr, Set<Keyword> toOvr) {

		for (Keyword ovrKeyWrd : ovr) {
			if (toOvr.contains(ovrKeyWrd)) {
				for (Keyword toOvrKeyWrd : toOvr) {
					if (!ovrKeyWrd.getKeyword().equals(toOvrKeyWrd.getKeyword())) {
						return false;

					}
				}
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares both sets of ExternalIDs using the their external strings for comparison
	 * 
	 * @param ovr
	 * @param toOvr
	 * @return
	 */
	public boolean validateExternalID(Set<ExternalId> ovr, Set<ExternalId> toOvr) {

		for (ExternalId ovrExId : ovr) {
			if (toOvr.contains(ovrExId)) {
				for (ExternalId toOvrExId : toOvr) {
					if (!ovrExId.getValue().equals(toOvrExId.getValue())) {
						return false;

					}
				}
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * @inheritDoc
	 */
	public List<DataElement> getDataElementsListByIds(List<Long> ids) {

		return dataElementDao.getByIdList(ids);
	}

	/**
	 * @inheritDoc
	 */
	public Long getNumDSWithStatus(Long statusId) {

		if (statusId == null) {
			return formStructureDao.getStatusCount(null);
		}
		return formStructureDao.getStatusCount(StatusType.statusOf(statusId));
	}

	/**
	 * @inheritDoc
	 */
	public Long getNumDEWithStatus(Long statusId) {

		if (statusId == null) {
			return dataElementDao.getStatusCount(null, null);
		}
		return dataElementDao.getStatusCount(DataElementStatus.getById(statusId), null);
	}

	/**
	 * @inheritDoc
	 */
	public Long getNumDEWithStatusAndCategory(Long statusId, Category category) {

		if (statusId == null) {
			return dataElementDao.getStatusCount(null, category);
		}
		return dataElementDao.getStatusCount(DataElementStatus.getById(statusId), category);
	}

	/**
	 * @inheritDoc
	 */
	public Long getNumDEWithCategory(Category category) {

		return dataElementDao.getStatusCount(null, category);
	}

	/**
	 * @inheritDoc
	 */
	public List<String> validateExtraColumns(File upload, String uploadContentType) throws IOException {

		List<String> errors = new ArrayList<String>();

		CSVReader reader = new CSVReader(new FileReader(upload));
		TBIMappingStrategy strat = new TBIMappingStrategy();
		String[] columns = reader.readNext();
		for (String column : columns) {
			if (strat.getColumnName(column) == null && !column.equals(ServiceConstants.EMPTY_STRING)) {
				errors.add(ServiceConstants.WARNING + column + ServiceConstants.COLUMN_MISMATCH);
			}
		}

		return errors;
	}

	/**
	 * @inheritDoc
	 */
	public void deleteCondition(Long id) {

		conditionDao.remove(id);
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public List<SubDomain> getSubDomainList(Domain domain, Disease disease)
			throws MalformedURLException, UnsupportedEncodingException {

		if (domain == null) {
			return new ArrayList<SubDomain>();
		}

		return staticManager.getSubDomainsList(domain, disease);

	}

	public List<FormStructure> getDataStructureByIds(List<Long> dsIdList) {

		return formStructureDao.getAllById(dsIdList);
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public List<Domain> getDomainsByDisease(Disease disease)
			throws MalformedURLException, UnsupportedEncodingException {

		if (disease == null) {
			return new ArrayList<Domain>();
		}

		return staticManager.getDomainsByDisease(disease);
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public List<Subgroup> getSubgroupsByDisease(Disease disease)
			throws MalformedURLException, UnsupportedEncodingException {

		if (disease == null) {
			return new ArrayList<Subgroup>();
		}

		return staticManager.getSubgroupsByDisease(disease);
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public List<Classification> getClassificationList(Disease disease, boolean isAdmin)
			throws MalformedURLException, UnsupportedEncodingException {

		if (disease == null) {
			return new ArrayList<Classification>();
		}

		return staticManager.getClassificationList(disease, isAdmin);
	}

	public Disease getDiseaseByName(String diseaseName) throws MalformedURLException, UnsupportedEncodingException {

		return staticManager.getDiseaseByName(diseaseName);
	}

	/**
	 * @inheritDoc
	 */
	public List<String> locations(String stringIn) {

		if (stringIn == null) {
			return null;
		}
		return Arrays.asList(stringIn.split(" "));
	}

	/**
	 * @inheritDoc
	 */
	public String getDiseasePrefix(Long diseaseId) {

		return diseaseDao.getPrefix(diseaseId);
	}

	/**
	 * @inheritDoc
	 */
	public boolean isValidForXml(char character) {

		if (!(character == 0x9 || character == 0xA || character == 0xD || (character >= 0x20 && character <= 0xD7FF)
				|| (character >= 0xE000 && character <= 0xFFFD) || (character >= 0x100000 && character <= 0x10FFFF))) {
			return false;
		}

		return true;
	}

	/**
	 * @inheritDoc
	 */
	public HashMap<Integer, ArrayList<Character>> isValidForXml(List<String[]> line) {

		// this will store a hashmap of line number to an array of invalid characters
		HashMap<Integer, ArrayList<Character>> errorMap = new HashMap<Integer, ArrayList<Character>>();

		char current; // Used to reference the current character.

		if (line == null || line.isEmpty()) {
			return errorMap;
		}

		for (int l = 1; l < line.size(); l++) {
			int lineCount = l + 1; // current line number
			ArrayList<Character> specialCharacterList = null; // this stores an array of invalid special
			// characters

			for (String lineString : line.get(l)) {
				for (int i = 0; i < lineString.length(); i++) {
					current = lineString.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not
													 // happen.

					// checks each character for special characters
					if (!isValidForXml(current)) {
						if (specialCharacterList == null) // initializes the array if it's empty
						{
							specialCharacterList = new ArrayList<Character>();
						}

						specialCharacterList.add(current);
					}
				}
			}

			if (specialCharacterList != null) // character list into the line number
			{
				errorMap.put(lineCount, specialCharacterList);
			}

			lineCount++;
		}

		return errorMap;
	}

	public FormStructure retrieveImportDataStructure(FormStructure workingDataStructure) {

		// TODO Auto-generated method stub
		return null;
	}

	public Boolean getIsLatestVersion(DataElement de) {

		SemanticDataElement latestDataElement = dataElementSparqlDao.getLatestByName(de.getName());
		if (de.getVersion().equals(latestDataElement.getVersion())) {
			return true;
		}

		return false;
	}

	public List<SemanticFormStructure> getLatestFormStructuresByIdAndStatus(List<String> shortNames) {
		return formStructureSparqlDao.getLatestByNames(shortNames);
	}

	public Boolean getIsLatestVersion(FormStructure fs) {

		SemanticFormStructure latestDataElement = formStructureSparqlDao.getLatest(fs.getShortName());
		if (fs.getVersion().equals(latestDataElement.getVersion())) {
			return true;
		}

		return false;
	}

	public Boolean getIsFormStructurePublished(String shortName) {
		SemanticFormStructure latestFormStructure = formStructureSparqlDao.getLatest(shortName);
		if (latestFormStructure.getStatus().equals(StatusType.PUBLISHED)) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean doesDataElementExist(String dataElementName) {

		DataElement checking = null;
		try {
			checking = dataElementDao.getLatestByName(dataElementName);
		} catch (MissingSemanticObjectException e) {
			// ignore if thrown, since we just want to see if it exists or not
			return false;
		} catch (MissingStructuralObjectException e) {
			return false;
		}
		return true;
	}

	public Boolean isLatestFormStructureVersion(FormStructure fs) {

		FormStructure latestFS = formStructureDao.getLatestVersionByShortName(fs.getShortName());

		if (latestFS.equals(fs)) {
			return true;
		}
		return false;

	}

	/*
	 * This method will change every form structure to point to the l
	 */
	public void updateFormStructuresWithLatestDataElement(String elementName, Long newDataElementID) {

		if (!StringUtils.isBlank(elementName)) {
			mapElementDao.updateFormStructuresWithLatestDataElement(elementName, newDataElementID);
		}

	}

	/*
	 * This method needs to be removed once the public search has been refactored
	 */
	public List<String> parseSelectedOptions(String options) {

		List<String> optionSet = new ArrayList<String>();
		if (options != null && !options.isEmpty()) {
			String[] optionArr = options.split(",");
			optionSet.addAll(Arrays.asList(optionArr));
		}

		return optionSet;
	}

	public Set<String> parseSelectedOptionsSet(String options) {

		Set<String> optionSet = new HashSet<String>();
		if (options != null && !options.isEmpty()) {
			String[] optionArr = options.split(",");
			optionSet.addAll(Arrays.asList(optionArr));
		}

		return optionSet;
	}

	public DictionarySearchFacets buildFacets(String searchKey, String selectedStatuses, String selectedElementTypes,
			String populationSelection, String selectedDiseases, String selectedDomains, String selectedSubdomains,
			String selectedClassifications, String dataElementLocations, String modifiedDate) {

		DictionarySearchFacets facets = new DictionarySearchFacets();

		if (modifiedDate != null && !modifiedDate.isEmpty()) {
			Date oldestModifiedDate = getOldestModifiedDate(Integer.valueOf(modifiedDate));

			if (oldestModifiedDate != null) {
				facets.addFacet(new DateFacet(FacetType.MODIFIED_DATE, oldestModifiedDate));
			}
		}

		if (selectedStatuses != null && !selectedStatuses.isEmpty() && !selectedStatuses.equals("all")) {
			List<String> statusList = this.parseSelectedOptions(selectedStatuses);
			if (!statusList.isEmpty()) {
				facets.addFacet(new StringFacet(FacetType.STATUS, statusList));
			}
		}

		if (selectedElementTypes != null && !selectedElementTypes.isEmpty()) {
			List<String> elementTypeSet = this.parseSelectedOptions(selectedElementTypes);
			if (!elementTypeSet.isEmpty()) {
				facets.addFacet(new StringFacet(FacetType.CATEGORY, elementTypeSet));
			}
		}

		if (populationSelection != null && !populationSelection.isEmpty()) {
			List<String> populationSet = this.parseSelectedOptions(populationSelection);
			if (!populationSet.isEmpty()) {
				facets.addFacet(new StringFacet(FacetType.POPULATION, populationSet));
			}
		}

		if (!"".equals(selectedDiseases) || !"".equals(selectedSubdomains) || !"".equals(selectedDomains)) {
			facets.addFacet(
					new DiseaseFacet(buildDiseaseFacetValues(selectedSubdomains, selectedDomains, selectedDiseases)));
		}

		if (selectedClassifications != null && !selectedClassifications.isEmpty()) {
			facets.addFacet(new ClassificationFacet(buildClassificationFacetValues(selectedClassifications,
					selectedSubdomains, selectedDomains, selectedDiseases)));
		}

		return facets;
	}

	public Date getOldestModifiedDate(int daysOld) {

		return new Date(BRICSTimeDateUtil.getStartOfCurrentDay() - (daysOld * BRICSTimeDateUtil.ONE_DAY));
	}

	public List<DiseaseFacetValue> buildDiseaseFacetValues(String selectedSubdomains, String selectedDomains,
			String selectedDiseases) {

		List<DiseaseFacetValue> values = new ArrayList<DiseaseFacetValue>();
		List<String> addedDomains = new ArrayList<String>();
		List<String> addedDiseases = new ArrayList<String>();

		if (selectedSubdomains != null && !selectedSubdomains.isEmpty() && !selectedSubdomains.equals("all")) {
			List<String> subdomainList = this.parseSelectedOptions(selectedSubdomains);

			for (String subDomainCombinations : subdomainList) {
				String[] subdomainParts = subDomainCombinations.split("\\.");
				String disease = subdomainParts[0];
				String domain = subdomainParts[1];
				String subdomain = subdomainParts[2];

				addedDomains.add(domain);
				addedDiseases.add(disease);

				DiseaseFacetValue value = new DiseaseFacetValue();
				value.setDisease(disease);
				value.setDomain(domain);
				value.setSubdomain(subdomain);
				values.add(value);
			}
		}

		if (selectedDomains != null && !selectedDomains.isEmpty() && !selectedDomains.equals("all")) {
			List<String> domains = this.parseSelectedOptions(selectedDomains);

			for (String domainCombination : domains) {
				String[] domainParts = domainCombination.split("\\.");
				String disease = domainParts[0];
				String domain = domainParts[1];

				if (!addedDomains.contains(domain)) {
					addedDiseases.add(disease);
					addedDomains.add(domain);
					DiseaseFacetValue value = new DiseaseFacetValue();
					value.setDisease(disease);
					value.setDomain(domain);
					values.add(value);
				}
			}
		}

		if (selectedDiseases != null && !selectedDiseases.isEmpty() && !selectedDiseases.equals("all")) {
			List<String> diseases = this.parseSelectedOptions(selectedDiseases);

			for (String disease : diseases) {
				if (!addedDiseases.contains(disease)) {
					addedDiseases.add(disease);
					DiseaseFacetValue value = new DiseaseFacetValue();
					value.setDisease(disease);
					values.add(value);
				}
			}
		}

		return values;
	}

	// Throw away method
	public List<Disease> getDiseaseOptions() {

		try {
			return staticManager.getDiseaseList();
		} catch (MalformedURLException e) {
			logger.error("Not able to retrieve the static disease list.");
			logger.error(e.getMessage());
			return null;
		} catch (UnsupportedEncodingException e) {
			logger.error("Not able to retrieve the static disease list.");
			logger.error(e.getMessage());
			return null;
		}
	}

	/**
	 * @inheritDoc
	 */
	public List<UserFile> getUserFiles(List<Long> accessIds) {
		List<UserFile> userFiles = new ArrayList<UserFile>();

		userFiles = userFileDao.getById(accessIds);

		return userFiles;
	}

	/**
	 * @inheritDoc
	 */
	public UserFile getUserFile(Long fileId) {
		UserFile file = new UserFile();

		file = userFileDao.get(fileId);

		return file;
	}

	private List<ClassificationFacetValue> buildClassificationFacetValues(String selectedClassifications,
			String selectedSubdomains, String selectedDomains, String selectedDiseases) {

		Set<String> addedDiseases = new HashSet<String>();
		Set<String> toBeAddedDisease = new HashSet<String>();
		List<ClassificationFacetValue> values = new ArrayList<ClassificationFacetValue>();

		if (selectedClassifications != null && !selectedClassifications.isEmpty()
				&& !selectedClassifications.equals("all")) {
			List<String> classificationList = this.parseSelectedOptions(selectedClassifications);

			for (String classificationCombination : classificationList) {
				String[] classificationParts =
						classificationCombination.split(ServiceConstants.DE_DISEASE_SPLIT_EXPRESSION);
				String disease = classificationParts[ServiceConstants.DISEASE_INDEX];
				String subgroup = classificationParts[ServiceConstants.SUBGROUP_INDEX];
				String classification = classificationParts[ServiceConstants.CLASSIFICATION_INDEX];

				ClassificationFacetValue value = new ClassificationFacetValue(classification, subgroup);
				values.add(value);
				addedDiseases.add(disease);
			}
		}

		if (selectedSubdomains != null && !selectedSubdomains.isEmpty() && !selectedSubdomains.equals("all")) {
			List<String> subdomainList = this.parseSelectedOptions(selectedSubdomains);

			for (String subDomainCombinations : subdomainList) {
				String[] subdomainParts = subDomainCombinations.split(ServiceConstants.DE_DISEASE_SPLIT_EXPRESSION);
				String disease = subdomainParts[ServiceConstants.DISEASE_INDEX];
				if (!addedDiseases.contains(disease)) {
					addedDiseases.add(disease);
					toBeAddedDisease.addAll(getDiseaseOrSubgroup(disease));
				}
			}
		}

		if (selectedDomains != null && !selectedDomains.isEmpty() && !selectedDomains.equals("all")) {
			List<String> domains = this.parseSelectedOptions(selectedDomains);

			for (String domainCombination : domains) {
				String[] domainParts = domainCombination.split(ServiceConstants.DE_DISEASE_SPLIT_EXPRESSION);
				String disease = domainParts[ServiceConstants.DISEASE_INDEX];
				if (!addedDiseases.contains(disease)) {
					addedDiseases.add(disease);
					toBeAddedDisease.addAll(getDiseaseOrSubgroup(disease));
				}
			}
		}

		if (selectedDiseases != null && !selectedDiseases.isEmpty() && !selectedDiseases.equals("all")) {
			List<String> diseases = this.parseSelectedOptions(selectedDiseases);

			for (String disease : diseases) {
				if (!addedDiseases.contains(disease)) {
					addedDiseases.add(disease);
					toBeAddedDisease.addAll(getDiseaseOrSubgroup(disease));
				}
			}
		}

		for (String disease : toBeAddedDisease) {
			ClassificationFacetValue value = new ClassificationFacetValue(null, disease);
			values.add(value);
		}

		return values;
	}

	/**
	 * Given a string of a disease, returns the disease string back if no subgroup exists, or return the list of
	 * subgroup names if the disease has subgroups.
	 * 
	 * @param disease
	 * @return
	 */
	private List<String> getDiseaseOrSubgroup(String disease) {

		List<String> toBeAdded = new ArrayList<String>();

		List<Subgroup> subgroupList = null;
		try {
			subgroupList = staticManager.getSubgroupsByDisease(staticManager.getDiseaseByName(disease));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (subgroupList == null || subgroupList.isEmpty()) {
			toBeAdded.add(disease);
		} else {
			for (Subgroup subgroup : subgroupList) {
				toBeAdded.add(subgroup.getSubgroupName());
			}
		}

		return toBeAdded;
	}

	/**
	 * This method returns a map of data element name to map of permissible values for the given data element name set.
	 * 
	 * @param deNames - Set of data element names
	 * @return a map of data element name to map of permissible value to permissible value object.
	 */
	public Map<String, Map<String, ValueRange>> getDEValueRangeMap(Set<String> deNames) {

		return structuralDataElementDao.getDEValueRangeMap(deNames);
	}

	/**
	 * This method is called daily by brics scheduler to check data elements in dictionary RDF and update their status
	 * based on the following rules:
	 * 
	 * 1. If a DE has an until_date in the past and its status is either Published or Deprecated, change its status to
	 * Retired; 2. If a DE has an until_date in the future and its status is either Published or Retired, change its
	 * status to Deprecated; 3. If a DE does not have an until_date and its status is either Deprecated or Retired,
	 * change its status to Published.
	 */
	public void updateDEStatusWithUntilDate() {

		List<DataElement> dataElementsForUpdate = dataElementDao.getDataElementsForStatusUpdate();

		if (dataElementsForUpdate == null || dataElementsForUpdate.isEmpty()) {
			logger.info("No data element needs a status update.");
		}

		int count = 0;
		for (DataElement de : dataElementsForUpdate) {

			Date untilDate = de.getUntilDate();
			String dateString = BRICSTimeDateUtil.formatDate(untilDate);

			DataElementStatus status = de.getStatus();

			Date currentDate = new Date();
			if (untilDate != null) {
				if (untilDate.before(currentDate)
						&& (status == DataElementStatus.PUBLISHED || status == DataElementStatus.DEPRECATED)) {

					DataElementStatus old = status;

					de.setStatus(DataElementStatus.RETIRED);
					dataElementDao.save(de);


					// save status change
					String comment = SYSTEM_GENERATED + DATAELEMENT + de.getName() + RETIRED_COMMENT + dateString;

					DictionaryEventLog eventLog = new DictionaryEventLog();
					Long originalEntityId = structuralDataElementDao.getOriginalDataElementByName(de.getName()).getId();
					eventLog.setDataElementID(originalEntityId);

					saveEventLog(eventLog, old.getId().toString(), DataElementStatus.RETIRED.getId().toString(),
							comment, EventType.STATUS_CHANGE_RETIRED);


					count++;

				} else if (untilDate.after(currentDate)
						&& (status == DataElementStatus.PUBLISHED || status == DataElementStatus.RETIRED)) {

					DataElementStatus old = status;

					de.setStatus(DataElementStatus.DEPRECATED);

					logger.info("Change status for " + de.getName() + " from " + status.getName() + " to Deprecated.");
					dataElementDao.save(de);

					// save status change
					String comment =
							SYSTEM_GENERATED + DATAELEMENT + de.getName() + DEPRECATED_COMMENT + dateString + FUTURE;

					DictionaryEventLog eventLog = new DictionaryEventLog();
					Long originalEntityId = structuralDataElementDao.getOriginalDataElementByName(de.getName()).getId();
					eventLog.setDataElementID(originalEntityId);

					saveEventLog(eventLog, old.getId().toString(), DataElementStatus.DEPRECATED.getId().toString(),
							comment, EventType.STATUS_CHANGE_DEPRECATED);

					count++;
				}

			} else {
				if (status == DataElementStatus.RETIRED || status == DataElementStatus.DEPRECATED) {

					DataElementStatus old = status;

					de.setStatus(DataElementStatus.PUBLISHED);
					dataElementDao.save(de);

					// save status change
					String comment = SYSTEM_GENERATED + DATAELEMENT + de.getName() + PUBLISHED_COMMENT;

					DictionaryEventLog eventLog = new DictionaryEventLog();
					Long originalEntityId = structuralDataElementDao.getOriginalDataElementByName(de.getName()).getId();
					eventLog.setDataElementID(originalEntityId);

					saveEventLog(eventLog, old.getId().toString(), DataElementStatus.PUBLISHED.getId().toString(),
							comment, EventType.STATUS_CHANGE_TO_PUBLISHED);

					logger.info("Change status for " + de.getName() + " from " + status.getName() + " to Published.");
					count++;
				}
			}
		}

		logger.info("Finished updating status of " + count + " data elements.");
	}

	/**
	 * @inheritDoc
	 */
	public List<String> getDataElementNamesByType(DataType dataType) {
		return structuralDataElementDao.getDataElementNamesByType(dataType);
	}

	/**
	 * @inheritDoc
	 */
	public List<Schema> getAllSchemas() {
		return schemaDao.getAll();
	}

	public void loadNestedSemanticDataElement(List<DataElement> dataElements) {
		Map<String, SemanticDataElement> semanticDataElementMap = new HashMap<String, SemanticDataElement>();
		for (DataElement de : dataElements) {
			semanticDataElementMap.put(de.getUri(), de.getSemanticObject());
		}

		dataElementSparqlDao.loadNestedFields(semanticDataElementMap, true);
	}

	/*
	 * Convert DE object to arrayList with all info for CSV export
	 */

	public void dataElmentToStringArray(List<String> currentRow, DataElement currentDataElement)
			throws DateParseException {
		if (currentRow == null) {
			currentRow = new ArrayList<String>();
		}
		Map<String, String> subgroupToClassification = getSubgroupToClassificationMap(currentDataElement);
		
		List<String> csvheader = new ArrayList<String>();
		
		if(modulesConstants.isNTRRInstance()){
			csvheader = ServiceConstants.EXPORT_NTI_CSV_HEADERS;
		}else{
			csvheader = ServiceConstants.EXPORT_CSV_HEADERS;
		}
		for (String currentColumn : csvheader) {
			if (ServiceConstants.NAME_READABLE.equals(currentColumn)) // name
			{
				currentRow.add(currentDataElement.getName() != null ? currentDataElement
						.getName() : ServiceConstants.EMPTY_STRING);
			} else if (ServiceConstants.TITLE_READABLE.equals(currentColumn)) // title
			{
				currentRow.add(currentDataElement.getTitle() != null ? currentDataElement
						.getTitle() : ServiceConstants.EMPTY_STRING);
			} 			
			else if (ServiceConstants.CATEGORY_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getCategory() != null ? currentDataElement.getCategory()
						.getName() : ServiceConstants.EMPTY_STRING);
			} else if (ServiceConstants.VERSION_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getVersion() != null ? currentDataElement.getVersion()
						.toString() : ServiceConstants.EMPTY_STRING);
			} else if (ServiceConstants.DEFINITION_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getDescription());
			} else if (ServiceConstants.SHORT_DESCRIPTION_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getShortDescription());
			} else if (ServiceConstants.TYPE_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getType() != null ? currentDataElement.getType()
						.getValue() : ServiceConstants.EMPTY_STRING);
			} else if (ServiceConstants.SIZE_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getSize() != null ? currentDataElement.getSize()
						.toString() : ServiceConstants.EMPTY_STRING);
			} else if (ServiceConstants.RESTRICTIONS_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getRestrictions() != null ? currentDataElement.getRestrictions()
						.getValue() : ServiceConstants.EMPTY_STRING);
			} else if (ServiceConstants.MINIMUM_VALUE_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getMinimumValue() != null ? currentDataElement.getMinimumValue()
						.toString() : ServiceConstants.EMPTY_STRING);
			} else if (ServiceConstants.MAXIMUM_VALUE_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getMaximumValue() != null ? currentDataElement.getMaximumValue()
						.toString() : ServiceConstants.EMPTY_STRING);
			} else if (ServiceConstants.PERMISSIBLE_VALUES_READABLE.equals(currentColumn)) {
				currentRow.add(permissibleValueToCSVString(currentDataElement.getValueRangeList()));
			} else if (ServiceConstants.PERMISSIBLE_VALUES_DESCRIPTION_READABLE.equals(currentColumn)) {
				currentRow.add(permissibleValueDescriptionToCSVString(currentDataElement.getValueRangeList()));
			} else if (ServiceConstants.PERMISSIBLE_VALUES_OUTPUT_CODES_READABLE.equals(currentColumn)) {
				currentRow.add(permissibleValueOutputCodeToCSVString(currentDataElement.getValueRangeList()));
			} else if(ServiceConstants.ELEMENT_OID_READABLE.equals(currentColumn)) {
				currentRow.add(permissibleValueElementOidToCSVString(currentDataElement.getValueRangeList()));				
			} else if(ServiceConstants.ITEM_RESPONSE_OID_READABLE.equals(currentColumn)) {
				currentRow.add(permissibleValueItemResponseOidToCSVString(currentDataElement.getValueRangeList()));				
			} else if (ServiceConstants.MEASUREMENT_UNIT_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getMeasuringUnit() != null ? currentDataElement.getMeasuringUnit()
						.getName() : ServiceConstants.EMPTY_STRING);
			} else if (ServiceConstants.GUIDELINES_INSTRUCTIONS_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getGuidelines());
			} else if (ServiceConstants.NOTES_READABLE.equals(currentColumn)) // notes
			{
				currentRow.add(currentDataElement.getNotes());
			} else if (ServiceConstants.PREFERRED_QUESTION_TEXT_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getSuggestedQuestion());
			} else if (ServiceConstants.KEYWORD_READABLE.equals(currentColumn)) {
				currentRow.add(keywordListToString(currentDataElement.getKeywords()));
			} else if (ServiceConstants.LABEL_READABLE.equals(currentColumn)) {
				currentRow.add(keywordListToString(currentDataElement.getLabels()));
			} else if (ServiceConstants.HISTORICAL_NOTES_READABLE.equals(currentColumn)) // Historical
			// Notes
			{
				currentRow.add(currentDataElement.getHistoricalNotes());
			} else if (ServiceConstants.TYPE_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getType() != null ? currentDataElement.getType()
						.getValue() : ServiceConstants.EMPTY_STRING);
			} else if (ServiceConstants.REFERENCES_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getReferences());
			} else if (ServiceConstants.POPULATION_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getPopulation() != null ? currentDataElement.getPopulation()
						.getName() : ServiceConstants.EMPTY_STRING);
			} else if (isDomainColumn(currentColumn)) {
				currentRow.add(getDomainPairStringFromColumn(currentDataElement, currentColumn));
			} else if (isClassificationColumn(currentColumn)) {
				String subgroup = getDiseaseNameFromHeader(currentColumn).toLowerCase();
				String classification = subgroupToClassification.get(subgroup);
				currentRow.add(classification != null ? classification : ServiceConstants.EMPTY_STRING);
			} else if (ServiceConstants.SEE_ALSO_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getSeeAlso());
			} else if (ServiceConstants.SUBMITTING_ORGANIZATION_NAME_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getSubmittingOrgName());
			} else if (ServiceConstants.SUBMITTING_CONTACT_NAME_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getSubmittingContactName());
			} else if (ServiceConstants.SUBMITTING_CONTACT_INFORMATION_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getSubmittingContactInfo());
			} else if (ServiceConstants.EFFECTIVE_DATE_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getEffectiveDateString());
			} else if (ServiceConstants.UNTIL_DATE_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getUntilDateString());
			} else if (ServiceConstants.STEWARD_ORGANIZATION_NAME_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getStewardOrgName());
			} else if (ServiceConstants.STEWARD_CONTACT_NAME_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getStewardContactName());
			} else if (ServiceConstants.STEWARD_CONTACT_INFORMATION_READABLE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getStewardContactInfo());
			} else if (ServiceConstants.CREATION_DATE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getDateCreatedString());
			} else if (ServiceConstants.LAST_CHANGE_DATE.equals(currentColumn)) {
				currentRow.add(currentDataElement.getModifiedDateString());
			} else if (ServiceConstants.ADMINISTRATIVE_STATUS.equals(currentColumn)) {
				currentRow.add(currentDataElement.getStatus().getName());
			}else if(ServiceConstants.CAT_OID_READABLE.equals(currentColumn)){ // added by Ching-Heng
				currentRow.add(currentDataElement.getCatOid());
			} else if(ServiceConstants.FORM_ITEM_OID_READABLE.equals(currentColumn)){ // added by Ching-Heng
				currentRow.add(currentDataElement.getFormItemId());
			}
		}
		return;
	}

	public List<StructuralFormStructure> getAllSqlFormStructures() {

		List<StructuralFormStructure> result = this.formStructureSqlDao.getAllNoChildren();

		return result;
	}

	@Override
	public DictionaryEventLog saveEventLog(DictionaryEventLog eventLog, String oldVal, String newVal, String comment,
			EventType eventType) {

		eventLog.setComment(comment);
		eventLog.setOldValue(oldVal);
		eventLog.setNewValue(newVal);
		eventLog.setEventType(eventType);

		dictionaryEventLogDao.save(eventLog);

		return eventLog;
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

	public String getDEShortNameByNameIgnoreCases(String deName){
		String result = dataElementDao.getDEShortNameFromVirtuosoIgnoreCases(deName);
		return result;
	}
	
	public JSONArray getBatteryItemsJsonArray(String batteryAapiUrl, String apiUrl, String formOID, String asciiEncoded) {
		JSONArray batteryItems = new JSONArray();
		URL url;
		byte[] postData = "".getBytes();		
		HttpURLConnection connection = null;
		DataOutputStream writer = null;
		String output;
		
		try {
			url = new URL(batteryAapiUrl + formOID+".json");
			connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.addRequestProperty("Authorization", "Basic "+asciiEncoded);
			connection.addRequestProperty("Content-Length", "0");
			connection.connect();
			writer = new DataOutputStream(connection.getOutputStream());
			writer.write(postData);
			BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			JSONObject batteryFormObj = new JSONObject();
			while ((output = br.readLine()) != null) {
				batteryFormObj = new JSONObject(output);
			}
			connection.disconnect();

			JSONArray batteryFormArr =batteryFormObj.getJSONArray("Forms");
			for(int i=0;i<batteryFormArr.length();i++) {	
				JSONObject ob = (JSONObject) batteryFormArr.get(i);
				String batteryFormOid = ob.getString("FormOID");
						
				url = new URL(apiUrl + batteryFormOid + ".json");
				connection = (HttpsURLConnection) url.openConnection();
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
				connection.addRequestProperty("Authorization", "Basic "+asciiEncoded);
				connection.addRequestProperty("Content-Length", "0");
				connection.connect();
				writer = new DataOutputStream(connection.getOutputStream());
				writer.write(postData);
				JSONObject batteryItemObj = null;
				BufferedReader batteryBr = new BufferedReader(new InputStreamReader((connection.getInputStream())));
				while ((output = batteryBr.readLine()) != null) {
					batteryItemObj = new JSONObject(output);
				}
				connection.disconnect();

				JSONArray items = batteryItemObj.getJSONArray("Items");
				for(int j = 0 ; j < items.length(); j++) {
					batteryItems.put(items.get(j));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return batteryItems;
	}
	
	public List<String> getQuestionOIDListForBattery(String batteryAapiUrl, String formApiUrl, String formOID, String asciiEncoded) {
		ArrayList<String> questionsOIDList = new ArrayList<String>();
		URL url;
		byte[] postData = "".getBytes();		
		HttpURLConnection connection = null;
		DataOutputStream writer = null;
		String output;
		try {
			url = new URL(batteryAapiUrl + formOID + ".json");
			connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.addRequestProperty("Authorization", "Basic "+asciiEncoded);
			connection.addRequestProperty("Content-Length", "0");
			connection.connect();
			writer = new DataOutputStream(connection.getOutputStream());
			writer.write(postData);
			BufferedReader batteryBr = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			JSONObject batteryFormObj = new JSONObject();
			while ((output = batteryBr.readLine()) != null) {
				batteryFormObj = new JSONObject(output);
			}
			connection.disconnect();
	
			JSONArray batteryFormArr = batteryFormObj.getJSONArray("Forms");
			for(int i=0;i<batteryFormArr.length();i++) {	
				JSONObject ob = (JSONObject) batteryFormArr.get(i);
				String batteryFormOid = ob.getString("FormOID");
	
				url = new URL(formApiUrl + batteryFormOid + ".json");
				connection = (HttpsURLConnection) url.openConnection();
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
				connection.addRequestProperty("Authorization", "Basic "+asciiEncoded);
				connection.addRequestProperty("Content-Length", "0");
				connection.connect();
				writer = new DataOutputStream(connection.getOutputStream());
				writer.write(postData);
				JSONObject batteryItemObj = null;
				BufferedReader batteryInFormBr = new BufferedReader(new InputStreamReader((connection.getInputStream())));
				while ((output = batteryInFormBr.readLine()) != null) {
					batteryItemObj = new JSONObject(output);
				}
				connection.disconnect();
	
				JSONArray items = batteryItemObj.getJSONArray("Items");
				for(int j = 0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);	
					questionsOIDList.add(item.getString("FormItemOID"));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return questionsOIDList;
	}
}
