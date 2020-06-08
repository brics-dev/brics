package gov.nih.tbi.commons.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

public class ServiceConstants {
	public final static String RETIRED_DE_IN_FS_MSG = " contains following Retired DE:\t";
	public final static String PV_DESCRIPTION = "permissible value descriptions";
	public final static String PV_OUTPUT_CODE = "permissible value output codes";
	public final static String PV_MAPPING_FILENAME_EXTENSION = "_pv_mapping.csv";
	public final static String PV_FS_DE_MAPPING_DESCRIPTION_TEXT = "FS DE PV mapping file";
	public final static String WHITESPACE = " ";
	public final static String ENTITY_DATA_ELEMENT = "DataElement";
	public final static String ENTITY_DATA_STRUCTURE = "DataStructure";
	public final static String COMMA = ",";
	public final static String NEWLINE = "\n";
	public final static String PERIOD = ".";
	public static final String FILE_SEPARATER = "/";
	public static final String QUOTE = "\"";
	public static final String SINGLE_QUOTE = "'";
	public static final String UNDERSCORE = "_";
	// why is this necessary when we can use StringUtils.EMPTY (Appache Commons helper)?
	public final static String EMPTY_STRING = "";
	public static final String NULL = "null";
	public final static String DEFAULT = "Default";
	public final static String REQUIRED_MESSAGE = " field is required for ";
	public final static String PROHIBITED_MESSAGE = " field is only allowed on data elements of type numeric for ";
	public final static String NUMERIC_MISMATCH = " field can only contain numeric values for ";
	public final static String MISPLACE_SIZE =
			" field can only have a value for data elements of free-form, alphanumeric, or biosample: ";
	public final static String MISPLACE_MU =
			" field can only have a value for data elements of type alphanumeric or numeric: ";
	public final static String BAD_SIZE = " field must be between 1 and 4000 for element: ";
	public final static String NOT_VALID = " field is not valid domain for this disease for ";
	public final static String OVER_CHARACTER_10 = " field must be 10 characters or less for ";
	public final static String OVER_CHARACTER_20 = " field must be 20 characters or less for ";
	public final static String OVER_CHARACTER_30 = " field must be 30 characters or less for ";
	public final static String OVER_CHARACTER_55 = " field must be 55 characters or less for ";
	public final static String OVER_CHARACTER_255 = " field must be 255 characters or less for ";
	public final static String OVER_CHARACTER_4000 = " field must be 4000 characters or less for ";
	public final static String OVER_CHARACTER_200 = " field must be 200 characters or less for ";
	public final static String OVER_CHARACTER_1000 = " field must be 1000 characters or less for ";
	public final static String NOT_UNIQUE = " list has a duplicate element ";
	public final static String DATE_IN_PAST = " field cannot be in the past for ";
	public static final String ERROR_COLUMN_MISSING =
			"The required column header '%s' is missing from the CSV, please reference the Import CDE Template.";
	public static final String ERROR_SPECIAL_CHARACTER =
			"The CSV contains an invalid special character at line %d, please remove all special characters and try again.";
	public final static String NO_NAME_ERROR =
			"A data element is missing a name. All data elements must have a unique name: ";
	public final static String NAME_ERROR =
			" field must start with an alphabet and not contain any special characters for ";
	public final static String MIN_MAX_ERROR =
			"The Minimum Value must be less than the Maximum Value for the data element:  ";
	public final static int SIZE_LIMIT_20 = 20;
	public final static int SIZE_LIMIT_30 = 30;
	public final static int SIZE_LIMIT_55 = 55;
	public final static int SIZE_LIMIT_255 = 255;
	public final static int SIZE_LIMIT_4000 = 4000;
	public final static String CSV_FILE = "application/vnd.ms-excel";
	public final static String APPLICATION_ZIP_FILE = "application/zip";
	public final static String APPLICATION_CSV_FILE = "application/csv";
	public final static String XML_FILE = "text/xml";
	public final static String XSD_FILE = "text/xsd";
	public final static int DATA_STRUCTURE_LIMIT = 1000;
	public final static int EFORM_IMPORT_LIMIT = 1;
	public final static String STATIC_MANAGER = "staticManager";
	public final static String DICTIONARY_SERVICE = "dictionaryService";
	public final static String ACCOUNT_MANAGER = "accountManager";
	public static final String DATABASE_NAME_REGEX = "[A-Za-z][A-Za-z0-9_]*";
	public static final String USERNAME__REGEX = "^[A-Za-z]{1}[A-Za-z0-9@\\._-]{2,44}$";
	public static final String INTEGER_REGEX = "[0-9]*";
	public static final String CSV_EXPORT_NAME = "dataElementExport.csv";
	public static final String CSV_LIST_SEPARATER = ";";
	public static final int GUIDELINE_NOTES_REFERENCES_IMPORT_LENGTH_LIMIT = 4000; // maximum string length for those 3
	public static final String TIMEZONE_APPEND = " -04"; // fields
	public static final String URL_PREFIX_HTTP = "http://";
	public static final String URL_PREFIX_HTTPS = "https://";
	public static final String URL_PREFIX_FTP = "ftp://";
	public static final Long DEFAULT_PROVIDER = -1L;
	public static final String STUDY_PREFIX = "STDY";
	public static final String DATASET_PREFIX = "DATA";

	/****************************** Login Failure ***************************/

	public final static String UNLOCK_DATE_PARAM = "unlockDate";

	public final static String LOGIN_FAILURE_DEFAULT = "/jsp/login.jsp?login_error=1";
	public final static String LOGIN_FAILURE_EXPIRED =
			"/publicAccounts/passwordRecoveryAction!input.action?login_error=1";
	public final static int LOCKOUT_ATTEMPTS_FIRST = 3;
	public final static int LOCKOUT_WINDOW_FIRST = -15; // 15 minute window
	public final static int LOCKOUT_LENGTH_FIRST = 15; // 15 minutes
	public final static int LOCKOUT_ATTEMPTS_SECOND = 6;
	public final static int LOCKOUT_WINDOW_SECOND = 0; // we don't have a window
	public final static int LOCKOUT_LENGTH_SECOND = 60; // 60 minutes

	/****************************** Exception Messages ***************************/

	public final static String NULL_STATUS = "status cannot be null";
	public final static String NULL_DATASTUCTURE = "dataStructure cannot be null";
	public final static String NULL_CURUSER = "curUser cannot be null";
	public final static String NULL_ACCOUNT = "account cannot be null";
	public final static String INVALID_PASSWORD = "account cannot be null";

	/****************************** DEFAULT FILE INFORMATION ***************************/

	public static final Long TBI_DATAFILE_ENDPOINT_ID = 1L;
	public static final Long DDT_DATAFILE_ENDPOINT_ID = 3L;
	public static final Long CORRIEL_DATAFILE_ENDPOINT_ID = 4L;
	public static final Long PDBP_HOST_DEV_ENDPOINT_ID = 5L;
	public static final Long TBI_DRUPAL_DATAFILE_ENDPOINT_ID = 6L;
	public static final Long TBI_PUBLIC_DRUPAL_DATAFILE_ENDPOINT_ID = 7L;

	public static final String TBI_DEFAULT_FILE_PATH = "testFolder/";
	public static final String DDT_DEFAULT_FILE_PATH = "userDocUploads/";
	public static final String DDT_EFORM_FILE_PATH = "eformDocumenatation/";
	public static final String TBI_REPO_DEFAULT_FILE_PATH = "repoFolder/";
	public static final String TBI_ORDER_FILE_PATH = "orderXmls/";
	public static final String ORDER_MANAGER_FILE_PATH = "ordermanager/";
	public static final String QT_PV_MAPPING_FILE_PREFIX = "mapping_";
	public static final String META_STUDY_FILE_PATH = "metaStudy/";
	public static final String REPO_SUBMISSION_UPLOAD_PATH = "submissionUpload/";
	public static final String ADVANCED_VIEWER_UPLOAD_PATH = "/export/adv_visual/";
	public static final String QUERY_TOOL_FILE_PATH = "queryTool/";
	public static final String TBI_PUBLIC_SITE_FILE_PATH = "/export/adv_visual/study_metrics/";

	public static final String[] BIOSAMPLE_CATALOG_FILE_NAMES = {"PDBP_CatalogData.csv", "BioFIND_CatalogData.csv"};


	/****************************** CDE Fields in CSV **********************************/
	public final static String ID_READABLE = "ID";
	public final static String NAME_READABLE = "variable name";
	public final static String VERSION_READABLE = "version";
	public final static String TITLE = "title";
	public final static String TITLE_READABLE = "title";
	public final static String SHORT_DESCRIPTION = "shortDescription";
	public final static String SHORT_DESCRIPTION_READABLE = "short description";
	public final static String DEFINITION_READABLE = "definition";
	public final static String FORMAT = "format";
	public final static String FORMAT_READABLE = "Format";
	public final static String SIZE = "size";
	public final static String NOTES = "notes";
	public final static String NOTES_READABLE = "notes";
	public final static String VALUE_RANGES = "valueRanges";
	public final static String PERMISSIBLE_VALUES_READABLE = "permissible values";
	public final static String VALUE_RANGE_DESCRIPTIONS = "valueRangeDescriptions";
	public final static String PERMISSIBLE_VALUES_DESCRIPTION_READABLE = "permissible value descriptions";
	public final static String VALUE_RANGE_OUTPUT_CODES = "valueRangeOutputCodes";
	public final static String PERMISSIBLE_VALUES_OUTPUT_CODES_READABLE = "permissible value output codes";
	public final static String GUIDELINES = "guidelines";
	public final static String GUIDELINES_READABLE = "Guidelines/Instructions";
	public final static String HISTORICAL_NOTES = "historicalNotes";
	public final static String HISTORICAL_NOTES_READABLE = "historical notes";
	public final static String LABEL_READABLE = "Label(s)";
	public final static String SEE_ALSO_READABLE = "see also";
	public final static String SEE_ALSO = "seeAlso";
	public final static String TYPE = "type";
	public final static String TYPE_READABLE = "datatype";
	public final static String TYPE_ERROR1 = "Data Elements of type ";
	public final static String TYPE_ERROR2 = " must be a Free-Form Entry for, ";
	public final static String REFERENCES = "references";
	public final static String REFERENCES_READABLE = "references";
	public final static String CLASSIFICATION = "classification";
	public final static String CLASSIFICATION_READABLE = "Classification";
	public static final String PERMISSIBLE_VALUE_CODE_READABLE = "permissible value code";
	public static final String UNIT_OF_MEASURE_READABLE = "unit of measure";
	public static final String GUIDELINES_INSTRUCTIONS_READABLE = "guidelines/instructions";
	public static final String PREFERRED_QUESTION_TEXT_READABLE = "preferred question text";
	public final static String KEYWORD = "keyword";
	public final static String KEYWORD_READABLE = "keywords";
	public final static String LABEL = "label";
	public final static String SPEC_CHAR_ERROR = "Invalid character(s) %s has been detected in line %d for, ";
	public final static String CLASSIFICATION_ERROR1 = "A classification is required for the subgroup: ";
	public final static String CLASSIFICATION_ERROR2 = ", on the data element: ";
	public final static String BAD_CLASSIFICATION = "The classification is invalid for the subgroup: ";
	public final static String ADMIN_CLASSIFICATION_ERROR1 = "It is illegal to give the subgroup '";
	public final static String ADMIN_CLASSIFICATION_ERROR2 = "' a classification of '";
	public final static String ADMIN_CLASSIFICATION_ERROR3 = "' as a non-admin user, on the data element: ";
	public final static String EXTRA_CLASSIFICATION =
			" has a classification but no corresponding domain for the data element ";
	public final static String ADMIN_CATEGORY_ERROR =
			"It is illegal to create a Common Data Element (CDE) as a non-admin user, on the data element: ";
	public final static String ADMIN_LABEL_ERROR =
			"It is illegal to provide Label as a non-admin user, on the data element: ";
	public final static String BAD_DOMAIN_1 = " is not a valid domain for the '";
	public final static String BAD_DOMAIN_2 = "' disease for the data element ";
	public final static String DUPLICATE_DOMAIN = " is duplicated in the '";
	public final static String BAD_SUBDOMAIN = " is not a valid sub-domain for the domain ";
	public final static String BAD_SUBDOMAIN_2 = " in the disease ";
	public final static String POPULATION = "population";
	public final static String POPULATION_READABLE = "population.all";
	public final static String DISEASE_LIST = "diseaseList";
	public final static String CLASSIFICATION_LIST = "classificationElementList";
	public final static String DISEASE_LIST_READABLE = "Diseases";
	public final static String DOMAIN = "domain";
	public final static String DOMAIN_PREFIX = "domain.";
	public final static String DOMAIN_GENERAL = DOMAIN_PREFIX + "general (for all diseases)";
	public final static String DOMAIN_TBI = DOMAIN_PREFIX + "traumatic brain injury";
	public final static String DOMAIN_PD = DOMAIN_PREFIX + "Parkinson's disease";
	public final static String DOMAIN_FREDREICHS_ATAXIA = DOMAIN_PREFIX + "Friedreich's ataxia";
	public final static String DOMAIN_STROKE = DOMAIN_PREFIX + "stroke";
	public final static String DOMAIN_AMYOTROPHIC_LATERAL_SCLEROSIS = DOMAIN_PREFIX + "amyotrophic lateral sclerosis";
	public final static String DOMAIN_HUNTINGTONS_DISEASE = DOMAIN_PREFIX + "Huntington's disease";
	public final static String DOMAIN_MULTIPLE_SCLEROSIS = DOMAIN_PREFIX + "multiple sclerosis";
	public final static String DOMAIN_NEUROMUSCULAR_DISEASES = DOMAIN_PREFIX + "neuromuscular diseases";
	public final static String DOMAIN_MYASTHENIA_GRAVIS = DOMAIN_PREFIX + "myasthenia gravis";
	public final static String DOMAIN_SPINAL_MUSCULAR_ATROPHY = DOMAIN_PREFIX + "spinal muscular atrophy";
	public final static String DOMAIN_DUCHENNE_MUSCULAR_DYSTROPHY_BECKER_MUSCULAR_DYSTROPHY =
			DOMAIN_PREFIX + "Duchenne muscular dystrophy/Becker muscular dystrophy";
	public final static String DOMAIN_CONGENITAL_MUSCULAR_DYSTROPH = DOMAIN_PREFIX + "congenital muscular dystrophy";
	public final static String DOMAIN_SPINAL_CORD_INJURY = DOMAIN_PREFIX + "spinal cord injury";
	public final static String DOMAIN_HEADACHE = DOMAIN_PREFIX + "headache";
	public final static String DOMAIN_EPILEPSY = DOMAIN_PREFIX + "epilepsy";
	public final static String DOMAIN_TRAUMA = DOMAIN_PREFIX + "trauma";
	public final static String CLASSIFICATION_PREFIX = "classification.";
	public final static String CLASSIFICATION_GENERAL = CLASSIFICATION_PREFIX + "general (for all diseases)";
	public final static String CLASSIFICATION_ACUTE_HOSPITALIZED = CLASSIFICATION_PREFIX + "acute hospitalized";
	public final static String CLASSIFICATION_CONCUSSION_MILD_TBI = CLASSIFICATION_PREFIX + "concussion/mild TBI";
	public final static String CLASSIFICATION_EPIDEMIOLOGY = CLASSIFICATION_PREFIX + "epidemiology";
	public final static String CLASSIFICATION_MODERATE_SEVERE_TBI =
			CLASSIFICATION_PREFIX + "moderate/severe TBI: rehabilitation";
	public final static String CLASSIFICATION_PD = CLASSIFICATION_PREFIX + "Parkinson's disease";
	public final static String CLASSIFICATION_FRIEDREICHS_ATAXIA = CLASSIFICATION_PREFIX + "Friedreich's ataxia";
	public final static String CLASSIFICATION_STROKE = CLASSIFICATION_PREFIX + "stroke";
	public final static String CLASSIFICATION_AMYOTROPHIC_LATERAL_SCLEROSIS =
			CLASSIFICATION_PREFIX + "amyotrophic lateral sclerosis";
	public final static String CLASSIFICATION_HUNTINGONS_DISEASE = CLASSIFICATION_PREFIX + "Huntington's disease";
	public final static String CLASSIFICATION_MULTIPLE_SCEROSIS = CLASSIFICATION_PREFIX + "multiple sclerosis";
	public final static String CLASSIFICATION_NEUROMUSCULAR_DIEASES = CLASSIFICATION_PREFIX + "neuromuscular diseases";
	public final static String CLASSIFICATION_MYASTHENIA_GRAVIS = CLASSIFICATION_PREFIX + "myasthenia gravis";
	public final static String CLASSIFICATION_SPINAL_MUSCULAR_ATROPHY =
			CLASSIFICATION_PREFIX + "spinal muscular atrophy";
	public final static String CLASSIFICATION_DUCHENNE_MUSCULAR_DYSTROPHY =
			CLASSIFICATION_PREFIX + "Duchenne muscular dystrophy/Becker muscular dystrophy";
	public final static String CLASSIFICATION_CONGENITAL_MUSCULAR_DYSTROPHY =
			CLASSIFICATION_PREFIX + "congenital muscular dystrophy";
	public final static String CLASSIFICATION_SPINAL_CORD_INJURY = CLASSIFICATION_PREFIX + "spinal cord injury";
	public final static String CLASSIFICATION_HEADACHE = CLASSIFICATION_PREFIX + "headache";
	public final static String CLASSIFICATION_EPILEPSY = CLASSIFICATION_PREFIX + "epilepsy";
	public final static String CLASSIFICATION_NTRR_CORE = CLASSIFICATION_PREFIX + "NTRR core";
	public final static String CLASSIFICATION_OUTCOME_QOL = CLASSIFICATION_PREFIX + "Outcome/QOL";
	public final static String CLASSIFICATION_REHAB = CLASSIFICATION_PREFIX + "Rehab";
	public final static String CLASSIFICATION_INPATIENT = CLASSIFICATION_PREFIX + "Inpatient";
	public final static String CLASSIFICATION_PREHOSPITAL = CLASSIFICATION_PREFIX + "Prehospital";
	public final static String SUBMITTING_ORGANIZATION_NAME_READABLE = "submitting organization name";
	public final static String SUBMITTING_ORGANIZATION_NAME = "submittingOrgName";
	public final static String SUBMITTING_CONTACT_NAME_READABLE = "submitting contact name";
	public final static String SUBMITTING_CONTACT_NAME = "submittingContactName";
	public final static String SUBMITTING_CONTACT_INFORMATION_READABLE = "submitting contact information";
	public final static String SUBMITTING_CONTACT_INFORMATION = "submittingContactInfo";
	public final static String EFFECTIVE_DATE_READABLE = "effective date";
	public final static String EFFECTIVE_DATE = "effectiveDate";
	public final static String UNTIL_DATE_READABLE = "until date";
	public final static String UNTIL_DATE = "untilDate";
	public final static String STEWARD_ORGANIZATION_NAME_READABLE = "steward organization name";
	public final static String STEWARD_ORGANIZATION_NAME = "stewardOrgName";
	public final static String STEWARD_CONTACT_NAME_READABLE = "steward contact name";
	public final static String STEWARD_CONTACT_NAME = "stewardContactName";
	public final static String STEWARD_CONTACT_INFORMATION_READABLE = "steward contact information";
	public final static String STEWARD_CONTACT_INFORMATION = "stewardContactInfo";

	public final static String CREATION_DATE = "creation date";
	public final static String LAST_CHANGE_DATE = "last change date";
	public final static String CHANGED_FORMAT_DATE = "2020-02-27";
	public final static String DEFAULT_FORMAT_DATE = "yyyy-MM-dd";
	public final static String UNIVERSAL_FORMAT_DATE = "yyyy-MM-dd HH:mm:ss.S";
	public final static String ADMINISTRATIVE_STATUS = "administrative status";
	public final static String SUBDOMAIN = "subdomain";
	public final static String SUBDOMAIN_READABLE = "Sub-Domain";
	public final static String CRF_MODULE_LIST_READABLE = "CRF Modules";
	public final static String KEYWORD_LIST = "keywords";
	public final static String LABEL_LIST = "labels";
	public final static String KEYWORD_LIST_READABLE = "Keywords";
	public final static String ALIASES = "aliases";
	public final static String ALIASES_READABLE = "Aliases";
	public final static String REQUIRED_TYPE = "requiredType";
	public final static String REQUIRED_TYPE_READABLE = "Required Type";
	public final static String SECTION = "section";
	public final static String SECTION_READABLE = "Section";
	public final static String RESTRICTIONS = "restrictions";
	public final static String RESTRICTIONS_READABLE = "input restriction";
	public final static String MEASUREMENT_TYPE = "measurementType";
	public final static String MEASUREMENT_TYPE_READABLE = "Measurement Type";
	public final static String MEASUREMENT_UNIT = "measuringUnit";
	public final static String MEASUREMENT_UNIT_READABLE = "unit of measure";
	public final static String EXTERNAL_ID_PREFIX = "external ID.";
	public final static String LOINC_READABLE = EXTERNAL_ID_PREFIX + "LOINC";
	public final static String LOINC_PV_READABLE = LOINC_READABLE + ".Permissible Values";
	public final static String SNOMED_READABLE = EXTERNAL_ID_PREFIX + "SNOMED";
	public final static String SNOMED_PV_READABLE = SNOMED_READABLE + ".Permissible Values";
	public final static String CADSR_READABLE = EXTERNAL_ID_PREFIX + "caDSR";
	public final static String CADSR_PV_READABLE = CADSR_READABLE + ".Permissible Values";
	public final static String CDISC_READABLE = EXTERNAL_ID_PREFIX + "CDISC";
	public final static String CDISC_PV_READABLE = CDISC_READABLE + ".Permissible Values";
	public final static String NINDS_READABLE = EXTERNAL_ID_PREFIX + "NINDS";
	public final static String NINDS_PV_READABLE = NINDS_READABLE + ".Permissible Values";
	public final static String EXTERNAL_ID_READABLE = "External ID";
	public final static String LOINC = "loinc";
	public final static String SNOMED = "snomed";
	public final static String CADSR = "cadsr";
	public final static String CDISC = "cdisc";
	public final static String NINDS = "ninds";
	public final static String CATEGORY_READABLE = "element type";
	public final static String CATEGORY = "category";
	public final static String MINIMUM_VALUE_READABLE = "minimum value";
	public final static String MINIMUM_VALUE = "minimumValue";
	public final static String MAXIMUM_VALUE_READABLE = "maximum value";
	public final static String SIZE_READABLE = "maximum character quantity";
	public final static String MAXIMUM_VALUE = "maximumValue";
	public final static String QUESTION_TEXT_READABLE = "Suggested Question Text";
	public final static String QUESTION_TEXT = "suggestedQuestion";
	// added by Ching-Heng
	public final static String CAT_OID_READABLE = "CAT OID";
	public final static String CAT_OID = "catOid";
	public final static String FORM_ITEM_OID_READABLE = "Form Item OID";
	public final static String FORM_ITEM_OID = "formItemId";
	public final static String ITEM_RESPONSE_OID = "ItemResponseOID";
	public final static String ITEM_RESPONSE_OID_READABLE = "Item Response OID";
	public final static String ELEMENT_OID = "ElementOID";
	public final static String ELEMENT_OID_READABLE = "Element OID";
	
	/****************************** Error Messages **********************************/
	public static final String WARNING = "Warning: ";
	public static final String COLUMN_MISMATCH = " does not match any of the columns for a data element.";
	public static final String DUPLICATE_COLUMN = "Duplicate column found in CSV: ";
	public static final Long FILTER_BY_MINE_ID = -5L;
	public static final Long FILTER_BY_ALL_ID = -10L;
	public static final String FILTER_BY_MINE = "Mine";
	public static final String FILTER_BY_ALL = "All";
	public static final String FILTER_BY_UNATTACHED = "Unattached";
	public static final String INVALID_VALIDATOR = "Error: Invalid validator!";
	public static final String NO_DATA_STRUCTURE = "Repeatable Groups must be associated with a Data Structure";
	public static final String NO_REPEATABLE_GROUP = "Map Elements must be associated with a Repeatable Group";
	public static final String MISMATCHED_DS = "Repeatable Groups must be associated with the same Data Structure";
	public static final String MISMATCHED_RG = "Map Elements must be associated with the same Repeatable Group";
	public static final String NO_DATA_ELEMENT = "Map Elements must be associated with a Data Element";
	public static final String INCORRECT_TYPE = "Map Elements must have a required type";
	public static final String INCORRECT_THRESHOLD = "Repeatable Groups must have a threshold value greater than 0";
	public static final String INCORRECT_RG_TYPE = "Repeatable Groups must have a required type";
	public static final String ADDED_DS_TO_RG = "Repeatable Groups DS_id column was set to ";
	public static final String NO_NAME_DE = "All Elements must have a name.";
	public static final String NOT_UNIQUE_DE = "All Element names must be unique.";
	public static final String NO_NAME_RG = "All Repeatable Element Groups must have a name.";
	public static final String NOT_UNIQUE_RG =
			"Repeatable Element Groups cannot share a name with another" + " group in the same data structure.";
	public static final String DUPLICATE_NAME = "The import file contains multiple elements with the name: ";
	public static final String EXISTING_NAME = "This data element's name conflicts with one already in the database: ";
	public static final String MISSING_DESCRIPTION_VALUE = "Permissible Value Description missing for: ";
	public static final String MISSING_RANGE_VALUE = "Permissible Value missing for: ";
	public final static String PER_VALUE_RANGE_MISMATCH =
			"The number of Permissible Values and Permissible Value Descriptions are not equal for ";
	public final static String PER_VALUE_RANGE_OUTOUT_CODES_MISMATCH =
			"The number of Permissible Values and Permissible Value Output Codes are not equal for ";
	public final static String PER_VALUE_RANGE_OUTOUT_CODES_BADINTEGERANGE =
			"Permissible Value Output Code must be an integer number between -10000 and 10000 for ";
	public final static String PER_VALUE_RANGE_OUTOUT_CODES_DUPLICATES =
			"Permissible Value Output Code must be unique for ";
	public final static String PUBLISHED_DATA_ELEMENT =
			" cannot be overwritten because the Data Element has been published.";
	public final static String OVERWRITE_ERROR = "Overwrite Error: ";
	public final static String DE_VAR_NAME = " Data Element - ";
	public final static String SIZE_TOO_LARGE = "The size provided is larger than 100 for Data Element: ";
	public final static String INVALID_MEASURING_UNIT = "The measuring unit provided is invalid for: ";
	public final static String INVALID_PERMISSION =
			"cannot be overwritten beacuse you don't have proper permission to edit this Data Element.";

	/****************************** Meta Study CSV Constants **********************************/
	public final static String DOI = "DOI";
	public final static String FILE_NAME = "File Name";
	public final static String USER_NAME = "User Name";
	public final static String DOWNLOAD_DATE = "Download Date";

	public static final String[] EXPORT_META_STUDY_ACCESS_RECORD_CSV_HEADERS =
			{DOI, FILE_NAME, USER_NAME, DOWNLOAD_DATE};

	/****************************** BiospecimenOrder CSV Constants **********************************/
	public final static String ORDER_ID = "Order ID";
	public final static String ORDER_TITLE = "Order Title";
	public final static String SUBMITTED_DATE = "Submitted on Date";
	public final static String SUBMITTER_NAME = "Submitter Name";
	public final static String SAMPLE_ID = "Sample ID";
	public final static String REPOSITORY = "Repository";
	public final static String SAMPLE_TYPE = "Sample Type";
	public final static String GUID = "Guid";
	public final static String VISIT_TYPE = "Visit Type";
	public final static String INVENTORY = "Inventory";
	public final static String INVENTORY_DATE = "Inventory Date";
	public final static String QUANTITY = "Quantity";
	public final static String UNIT_NUMBER = "Unit Number";
	public final static String UNIT_OF_MEASURE = "Unit of Measure";

	// This list maintains the order for the BiospecimenOrder Export Function - TB
	public static final List<String> EXPORT_BIOSPECIMEN_CSV_HEADERS;
	static {
		ArrayList<String> biospecimenCsvHeaders = new ArrayList<String>();
		biospecimenCsvHeaders.add(ORDER_ID);
		biospecimenCsvHeaders.add(ORDER_TITLE);
		biospecimenCsvHeaders.add(SUBMITTED_DATE);
		biospecimenCsvHeaders.add(SUBMITTER_NAME);
		biospecimenCsvHeaders.add(SAMPLE_ID);
		biospecimenCsvHeaders.add(REPOSITORY);
		biospecimenCsvHeaders.add(SAMPLE_TYPE);
		biospecimenCsvHeaders.add(GUID);
		biospecimenCsvHeaders.add(VISIT_TYPE);
		biospecimenCsvHeaders.add(INVENTORY);
		biospecimenCsvHeaders.add(INVENTORY_DATE);
		biospecimenCsvHeaders.add(QUANTITY);
		biospecimenCsvHeaders.add(UNIT_NUMBER);
		biospecimenCsvHeaders.add(UNIT_OF_MEASURE);

		EXPORT_BIOSPECIMEN_CSV_HEADERS = Collections.unmodifiableList(biospecimenCsvHeaders);
	}

	public static final List<String> IMPORT_CSV_HEADERS;
	public static final List<String> EXPORT_CSV_HEADERS;
	public static final List<String> IMPORT_NTI_CSV_HEADERS;
	public static final List<String> EXPORT_NTI_CSV_HEADERS;

	static {
		ArrayList<String> csvHeaders = new ArrayList<String>();
		csvHeaders.add(NAME_READABLE);
		csvHeaders.add(TITLE_READABLE);
		csvHeaders.add(CATEGORY_READABLE);
		csvHeaders.add(VERSION_READABLE);
		csvHeaders.add(DEFINITION_READABLE);
		csvHeaders.add(SHORT_DESCRIPTION_READABLE);
		csvHeaders.add(TYPE_READABLE);
		csvHeaders.add(SIZE_READABLE);
		csvHeaders.add(RESTRICTIONS_READABLE);
		csvHeaders.add(MINIMUM_VALUE_READABLE);
		csvHeaders.add(MAXIMUM_VALUE_READABLE);
		csvHeaders.add(PERMISSIBLE_VALUES_READABLE);
		csvHeaders.add(PERMISSIBLE_VALUES_DESCRIPTION_READABLE);
		csvHeaders.add(PERMISSIBLE_VALUES_OUTPUT_CODES_READABLE);
		csvHeaders.add(ITEM_RESPONSE_OID_READABLE);
		csvHeaders.add(ELEMENT_OID_READABLE);
		csvHeaders.add(UNIT_OF_MEASURE_READABLE);
		csvHeaders.add(GUIDELINES_INSTRUCTIONS_READABLE);
		csvHeaders.add(NOTES_READABLE);
		csvHeaders.add(PREFERRED_QUESTION_TEXT_READABLE);
		csvHeaders.add(KEYWORD_READABLE);
		csvHeaders.add(REFERENCES_READABLE);
		/*
		 * csvHeaders.add(LOINC_READABLE); csvHeaders.add(LOINC_PV_READABLE); csvHeaders.add(SNOMED_READABLE);
		 * csvHeaders.add(SNOMED_PV_READABLE); csvHeaders.add(CADSR_READABLE); csvHeaders.add(CADSR_PV_READABLE);
		 * csvHeaders.add(CDISC_READABLE); csvHeaders.add(CDISC_PV_READABLE); csvHeaders.add(NINDS_READABLE);
		 * csvHeaders.add(NINDS_PV_READABLE);
		 */
		csvHeaders.add(POPULATION_READABLE);
		csvHeaders.add(DOMAIN_GENERAL);
		csvHeaders.add(DOMAIN_TBI);
		csvHeaders.add(DOMAIN_PD);
		csvHeaders.add(DOMAIN_FREDREICHS_ATAXIA);
		csvHeaders.add(DOMAIN_STROKE);
		csvHeaders.add(DOMAIN_AMYOTROPHIC_LATERAL_SCLEROSIS);
		csvHeaders.add(DOMAIN_HUNTINGTONS_DISEASE);
		csvHeaders.add(DOMAIN_MULTIPLE_SCLEROSIS);
		csvHeaders.add(DOMAIN_NEUROMUSCULAR_DISEASES);
		csvHeaders.add(DOMAIN_MYASTHENIA_GRAVIS);
		csvHeaders.add(DOMAIN_SPINAL_MUSCULAR_ATROPHY);
		csvHeaders.add(DOMAIN_DUCHENNE_MUSCULAR_DYSTROPHY_BECKER_MUSCULAR_DYSTROPHY);
		csvHeaders.add(DOMAIN_CONGENITAL_MUSCULAR_DYSTROPH);
		csvHeaders.add(DOMAIN_SPINAL_CORD_INJURY);
		csvHeaders.add(DOMAIN_HEADACHE);
		csvHeaders.add(DOMAIN_EPILEPSY);
		csvHeaders.add(CLASSIFICATION_GENERAL);
		csvHeaders.add(CLASSIFICATION_ACUTE_HOSPITALIZED);
		csvHeaders.add(CLASSIFICATION_CONCUSSION_MILD_TBI);
		csvHeaders.add(CLASSIFICATION_EPIDEMIOLOGY);
		csvHeaders.add(CLASSIFICATION_MODERATE_SEVERE_TBI);
		csvHeaders.add(CLASSIFICATION_PD);
		csvHeaders.add(CLASSIFICATION_FRIEDREICHS_ATAXIA);
		csvHeaders.add(CLASSIFICATION_STROKE);
		csvHeaders.add(CLASSIFICATION_AMYOTROPHIC_LATERAL_SCLEROSIS);
		csvHeaders.add(CLASSIFICATION_HUNTINGONS_DISEASE);
		csvHeaders.add(CLASSIFICATION_MULTIPLE_SCEROSIS);
		csvHeaders.add(CLASSIFICATION_NEUROMUSCULAR_DIEASES);
		csvHeaders.add(CLASSIFICATION_MYASTHENIA_GRAVIS);
		csvHeaders.add(CLASSIFICATION_SPINAL_MUSCULAR_ATROPHY);
		csvHeaders.add(CLASSIFICATION_DUCHENNE_MUSCULAR_DYSTROPHY);
		csvHeaders.add(CLASSIFICATION_CONGENITAL_MUSCULAR_DYSTROPHY);
		csvHeaders.add(CLASSIFICATION_SPINAL_CORD_INJURY);
		csvHeaders.add(CLASSIFICATION_HEADACHE);
		csvHeaders.add(CLASSIFICATION_EPILEPSY);
		csvHeaders.add(HISTORICAL_NOTES_READABLE);
		csvHeaders.add(LABEL_READABLE);
		csvHeaders.add(SEE_ALSO_READABLE);
		csvHeaders.add(SUBMITTING_ORGANIZATION_NAME_READABLE);
		csvHeaders.add(SUBMITTING_CONTACT_NAME_READABLE);
		csvHeaders.add(SUBMITTING_CONTACT_INFORMATION_READABLE);
		csvHeaders.add(EFFECTIVE_DATE_READABLE);
		csvHeaders.add(UNTIL_DATE_READABLE);
		csvHeaders.add(STEWARD_ORGANIZATION_NAME_READABLE);
		csvHeaders.add(STEWARD_CONTACT_NAME_READABLE);
		csvHeaders.add(STEWARD_CONTACT_INFORMATION_READABLE);
		csvHeaders.add(CREATION_DATE);
		csvHeaders.add(LAST_CHANGE_DATE);
		csvHeaders.add(ADMINISTRATIVE_STATUS);
		csvHeaders.add(CAT_OID_READABLE);
		csvHeaders.add(FORM_ITEM_OID_READABLE);
		
		IMPORT_CSV_HEADERS = Collections.unmodifiableList(csvHeaders);
		EXPORT_CSV_HEADERS = Collections.unmodifiableList(csvHeaders);
	}
	
	static{
		
		ArrayList<String> csvHeaders = new ArrayList<String>();
		csvHeaders.add(NAME_READABLE);
		csvHeaders.add(TITLE_READABLE);
		csvHeaders.add(CATEGORY_READABLE);
		csvHeaders.add(VERSION_READABLE);
		csvHeaders.add(DEFINITION_READABLE);
		csvHeaders.add(SHORT_DESCRIPTION_READABLE);
		csvHeaders.add(TYPE_READABLE);
		csvHeaders.add(SIZE_READABLE);
		csvHeaders.add(RESTRICTIONS_READABLE);
		csvHeaders.add(MINIMUM_VALUE_READABLE);
		csvHeaders.add(MAXIMUM_VALUE_READABLE);
		csvHeaders.add(PERMISSIBLE_VALUES_READABLE);
		csvHeaders.add(PERMISSIBLE_VALUES_DESCRIPTION_READABLE);
		csvHeaders.add(PERMISSIBLE_VALUES_OUTPUT_CODES_READABLE);
		csvHeaders.add(ITEM_RESPONSE_OID_READABLE);
		csvHeaders.add(ELEMENT_OID_READABLE);
		csvHeaders.add(UNIT_OF_MEASURE_READABLE);
		csvHeaders.add(GUIDELINES_INSTRUCTIONS_READABLE);
		csvHeaders.add(NOTES_READABLE);
		csvHeaders.add(PREFERRED_QUESTION_TEXT_READABLE);
		csvHeaders.add(KEYWORD_READABLE);
		csvHeaders.add(REFERENCES_READABLE);
		/*
		 * csvHeaders.add(LOINC_READABLE); csvHeaders.add(LOINC_PV_READABLE); csvHeaders.add(SNOMED_READABLE);
		 * csvHeaders.add(SNOMED_PV_READABLE); csvHeaders.add(CADSR_READABLE); csvHeaders.add(CADSR_PV_READABLE);
		 * csvHeaders.add(CDISC_READABLE); csvHeaders.add(CDISC_PV_READABLE); csvHeaders.add(NINDS_READABLE);
		 * csvHeaders.add(NINDS_PV_READABLE);
		 */
		csvHeaders.add(POPULATION_READABLE);
		csvHeaders.add(DOMAIN_TRAUMA);
		csvHeaders.add(CLASSIFICATION_NTRR_CORE);
		csvHeaders.add(CLASSIFICATION_OUTCOME_QOL);
		csvHeaders.add(CLASSIFICATION_REHAB);
		csvHeaders.add(CLASSIFICATION_INPATIENT);
		csvHeaders.add(CLASSIFICATION_PREHOSPITAL);
		csvHeaders.add(HISTORICAL_NOTES_READABLE);
		csvHeaders.add(LABEL_READABLE);
		csvHeaders.add(SEE_ALSO_READABLE);
		csvHeaders.add(SUBMITTING_ORGANIZATION_NAME_READABLE);
		csvHeaders.add(SUBMITTING_CONTACT_NAME_READABLE);
		csvHeaders.add(SUBMITTING_CONTACT_INFORMATION_READABLE);
		csvHeaders.add(EFFECTIVE_DATE_READABLE);
		csvHeaders.add(UNTIL_DATE_READABLE);
		csvHeaders.add(STEWARD_ORGANIZATION_NAME_READABLE);
		csvHeaders.add(STEWARD_CONTACT_NAME_READABLE);
		csvHeaders.add(STEWARD_CONTACT_INFORMATION_READABLE);
		csvHeaders.add(CREATION_DATE);
		csvHeaders.add(LAST_CHANGE_DATE);
		csvHeaders.add(ADMINISTRATIVE_STATUS);
		csvHeaders.add(CAT_OID_READABLE);
		csvHeaders.add(FORM_ITEM_OID_READABLE);
		
		IMPORT_NTI_CSV_HEADERS = Collections.unmodifiableList(csvHeaders);
		EXPORT_NTI_CSV_HEADERS = Collections.unmodifiableList(csvHeaders);
		
	}

	public static final String SCHEMA_MAPPING_BRICS_DE_SHORTNAME = "Variable Name";
	public static final String SCHEMA_MAPPING_SCHEMA_SYSTEM = "Schema System";
	public static final String SCHEMA_MAPPING_SCHEMA_DE_ID = "Schema ID";
	public static final String SCHEMA_MAPPING_BRICS_DE_PV = "BRICS DE PV";
	public static final String SCHEMA_MAPPING_SCHEMA_DE_NAME = "Schema DE Name";
	public static final String SCHEMA_MAPPING_SCHEMA_PV_VALUE = "Schema PV";
	public static final String SCHEMA_MAPPING_SCHEMA_PV_ID = "Schema PV ID";
	public static final int SCHEMA_DE_NAME_LIMIT = 50; // character limit of schema de name for schema pv mapping

	// ---------------- Schema PV errors messaging ------------------------------
	public static final String ERROR_SCHEMA_DE_NAME_MISSING =
			"The data element %s is missing a schema data element name for one of its mappings.";
	public static final String ERROR_SCHEMA_DE_CHARACTER_LIMIT =
			"The data element %s contains a value in the Schema DE Name column that exceeds the character limit of "
					+ SCHEMA_DE_NAME_LIMIT + " characters.";
	public static final String ERROR_MISSING_DE = "Could not find the data element with name %s.";
	public static final String ERROR_INVALID_PV =
			"The data element %s does not have a registered Permissible Value of %s.";
	public static final String ERROR_DUPLICATE_SCHEMA_ID_WITH_PV =
			"The data element %s with permissible value %s already has a mapped permissible value in %s with value \"%s\" and schema ID %s.";
	public static final String ERROR_DUPLICATE_SCHEMA_ID_WITHOUT_PV =
			"The data element %s with permissible value %s already has a mapped permissible value in %s with schema ID %s.";
	public static final String ERROR_INVALID_SCHEMA_SYSTEM = "The Schema System %s is not a supported schema system.";
	// --------------------------------------------------------------------------

	public static final List<String> EXPORT_SCHEMA_PV_CSV_HEADERS;
	static {
		ArrayList<String> csvHeaders = new ArrayList<String>();
		csvHeaders.add(SCHEMA_MAPPING_BRICS_DE_SHORTNAME);
		csvHeaders.add(SCHEMA_MAPPING_SCHEMA_SYSTEM);
		csvHeaders.add(SCHEMA_MAPPING_SCHEMA_DE_ID);
		csvHeaders.add(SCHEMA_MAPPING_BRICS_DE_PV);
		csvHeaders.add(SCHEMA_MAPPING_SCHEMA_DE_NAME);
		csvHeaders.add(SCHEMA_MAPPING_SCHEMA_PV_VALUE);
		csvHeaders.add(SCHEMA_MAPPING_SCHEMA_PV_ID);
		EXPORT_SCHEMA_PV_CSV_HEADERS = Collections.unmodifiableList(csvHeaders);
	}

	/* BTRIS Mapping */
	public static final String BTRIS_MAPPING_BRICS_DE_SHORTNAME = "Brics Data Element Short Name";
	public static final String BTRIS_MAPPING_BRICS_PV_VALUE = "Brics PV Value";
	public static final String BTRIS_MAPPING_OBSERVATION_NAME = "Btris Observation Name";
	public static final String BTRIS_MAPPING_RED_CONCEPT_CODE = "Btris Red Concept Code";
	public static final String BTRIS_MAPPING_SPECIMEN_TYPE = "Btris Specimen Type";
	public static final String BTRIS_MAPPING_TABLE = "Table in Btris";

	public static final String ERROR_BTRIS_OBSERVATION_NAME_MISSING =
			"The BTRIS Mapping is missing an observation name for one of its mappings.";
	public static final String ERROR_BTRIS_SPECIMEN_TYPE =
			"The BTRIS Mapping is missing a specimen type for one of its mappings.";
	public static final String ERROR_DUPLICATE_OBSERVATION_NAME =
			"The data element %s in BRICS already has a mapped BTRIS value \"%s\".";

	public static final List<String> EXPORT_BTRIS_MAPPING_CSV_HEADERS;
	static {
		ArrayList<String> csvHeaders = new ArrayList<String>();
		csvHeaders.add(BTRIS_MAPPING_BRICS_DE_SHORTNAME);
		csvHeaders.add(BTRIS_MAPPING_BRICS_PV_VALUE);
		csvHeaders.add(BTRIS_MAPPING_OBSERVATION_NAME);
		csvHeaders.add(BTRIS_MAPPING_RED_CONCEPT_CODE);
		csvHeaders.add(BTRIS_MAPPING_SPECIMEN_TYPE);
		csvHeaders.add(BTRIS_MAPPING_TABLE);
		EXPORT_BTRIS_MAPPING_CSV_HEADERS = Collections.unmodifiableList(csvHeaders);
	}

	/************************ Exception Messages *************************************/

	public static final String EXCEPTION_NULL_USER = "curUser cannot be null.";
	public static final String EXCEPTION_NULL_ACCOUNT_ID = "accountId cannot be null.";

	/************************ Account Roles ******************************************/

	public static final String ROLE_USER = "ROLE_USER";
	public static final String ROLE_ACCOUNT_ADMIN = "ROLE_ACCOUNT_ADMIN";
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_DICTIONARY = "ROLE_DICTIONARY";
	public static final String ROLE_DICTIONARY_ADMIN = "ROLE_DICTIONARY_ADMIN";
	public static final String ROLE_GUID = "ROLE_GUID";
	public static final String ROLE_GUID_ADMIN = "ROLE_GUID_ADMIN";
	public static final String ROLE_STUDY = "ROLE_STUDY";
	public static final String ROLE_STUDY_ADMIN = "ROLE_STUDY_ADMIN";
	public static final String ROLE_QUERY = "ROLE_QUERY";
	public static final String ROLE_QUERY_ADMIN = "ROLE_QUERY_ADMIN";

	public static final String STATUS_ACTIVE = "Active";
	public static final String STATUS_INACTIVE = "Inactive";
	public static final String STATUS_REQUESTED = "Requested";

	public static final String PERMISSION_OWNER = "OWNER";

	public static final String SFTP_NAME = "sftp";
	public static final String STRICT_HOST_KEY_CHECK = "StrictHostKeyChecking";
	public static final String NO = "no";
	public static final int RETRY_CHANNEL_CONNECTION=100;
	public static final int RETRY_SFTP_CLIENT_CONNECTION=20;
	public static final int THREAD_SLEEP_TIME_SFTP=3000;
	
	public static final int PASSWORD_MIN_LENGTH = 8;
	public static final int PASSWORD_MAX_LENGTH = 30;
	public static final String PASSWORD_DIGIT_PATTERN = "^.*(?=.*[0-9]).*$";
	public static final String PASSWORD_LOWER_PATTERN = "^.*(?=.*[a-z]).*$";
	public static final String PASSWORD_UPPER_PATTERN = "^.*(?=.*[A-Z]).*$";
	public static final String PASSWORD_SPECIAL_PATTERN = "^.(?=.*[`~!@#$%^&\\*()_\"{}=\\[\\]\'\"\\+-]).*$";
	public static final String NAMESPACE_ACCOUNTS = "accounts";
	public static final String ADMIN_NOTE = "adminNote";
	public static final String PHONE_NUMBER_PATTERN = "[\\s\\+0-9\\-\\(\\)xX]+";

	/************************* Permission Group **************************************/

	public static final String PUBLIC_STUDY = "Public Study";
	public static final String CLINICAL_TRIAL_PREFIX = "NCT";
	public static final String PUBLISHED_FORM_STRUCTURES = "Published Form Structures";
	public static final String PUBLIC_DATA_ELEMENTS = "Public Data Elements";
	public static final String PUBLISHED_META_STUDIES = "Published Meta Studies";
	public static final String SHARED_EFORMS = "Shared eForms";

	/************************* SQL ***************************************************/

	public static final String TABLE_SUBMISSION_JOIN = "submission_record_join";
	public static final String COLUMN_SUBMISSION_JOIN = "submission_record_join_id";
	public static final String MAIN = "main";

	/************************* FILE TYPES ***************************************************/

	public static final String FILE_TYPE_DICTIONARY = "Dictionary Documentation";
	public static final String FILE_TYPE_STUDY = "Study Documentation";
	public static final String FILE_TYPE_ACCOUNT = "Account Documentation";
	public static final String FILE_TYPE_META_STUDY_DOC = "Meta Study Documentation";
	public static final String FILE_TYPE_META_STUDY_DATA = "Meta Study Data";
	public static final String FILE_TYPE_SAVED_QUERY = "Saved Query";
	public static final String FILE_TYPE_RESULTS = "Results";
	public static final String FILE_TYPE_EFORM = "eForm Documentation";
	public static final String FILE_TYPE_EVENTLOG_DOC = "EventLog Documentation";
	public static final String FILE_TYPE_RESEARCHER_PICTURE = "Researcher Picture";
	public static final String FILE_TYPE_ELECTRONIC_SIGNATURE = "BRICS Electronic Signature";

	/************************* ROLE CHANGE MESSAGES ***************************************************/

	public static final String STRING_AND = " and";
	public static final String ACCOUNT_ROLE_GRANTED = " Account Role has been granted";
	public static final String ACCOUNT_ROLE_REQUESTED = " Account Role has been requested";
	public static final String ACCOUNT_ROLE_REVOKED = " Account Role has been revoked";
	public static final String ACCOUNT_ROLE_DENIED = " Account Role has been denied";
	public static final String ACCOUNT_DATE_CHANGED = " Expiration Date has changed to ";
	public static final String ACCOUNT_DATE_SET = " Expiration Date has been set to ";
	public static final String ACCOUNT_DATE_REMOVED = " Expiration Date has been removed ";

	/******************* Exception Messages *************************************/

	public static final String ADMIN_ACCESS_DENIED = "User does not have admin access to this entity.";
	public static final String WRITE_ACCESS_DENIED = "User does not have write access to this entity.";
	public static final String READ_ACCESS_DENIED = "User does not have read access to this entity.";
	public static final String MAIN_GROUP = "main";

	/************************************** Biosample Validation **************************************/

	public static final Integer MAX_BIOSAMPLE_LENGTH = 100;

	/*************************************** Log Output **********************************************/
	public static final String OVERWRITE_NOTICE = "Data Element Overwrite";
	public static final String CHANGES_MADE = "The following changes were made: ";

	/*************************** Date Time Formats **************************************/
	public final static DateTimeFormatter BRICS_DATE_FORMATTER;
	public final static int TIMEZONE_UTC_OFFSET = -4;
	// working with Bianca on defining these
	// public static String[] BRICS_DATE_FORMATS = { "dd-MMM-yy", "dd-MMM-yyyy", "dd/MMM/yy", "dd/MMM/yyyy", "dd/MM/yy",
	// "dd/MM/yyyy", "dd-MMM-yy HH:mm:ss", "dd-MMM-yyyy HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss",
	// "yyyy-MM-dd'T'HH:mm.S", "yyyy-MM-dd'T'HH:mm.S", "yyyy-MM-dd'T'HH:mm:ss.S", "yyyy-MM-dd'T'HH:mm:ss.SS",
	// "yyyy-MM-dd'T'HH:mm:ss'Z", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z", "yyyy-MM-dd'T'HH:mm:ss.SSSZZ" };
	public static final String DOMAIN_READABLE = "Domain";

	public static String[] BRICS_DATE_FORMATS = {"yyyy-MM-dd HH", "yyyy-MM-dd HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss.SSS",
			"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd", "dd-MMM-yy", "dd-MMM-yy HH", "dd-MMM-yy HH:mm",
			"dd-MMM-yy HH:mm:ss", "dd-MMM-yyyy hh:mm:ss a z", "dd/MMM/yy", "dd/MMM/yyyy hh:mm:ss a z",
			"dd-MMM-yyyy hh:mm:ss a", "dd/MMM/yyyy hh:mm:ss a", "dd-MMM-yyyy hh:mm a z", "dd/MMM/yyyy hh:mm a z",
			"dd-MMM-yyyy hh:mm a", "dd/MMM/yyyy hh:mm a", "dd-MMM-yyyy HH:mm:ss z", "dd/MMM/yyyy HH:mm:ss z",
			"dd-MMM-yyyy HH:mm:ss", "dd/MMM/yyyy HH:mm:ss", "dd-MMM-yyyy HH:mm z", "dd/MMM/yyyy HH:mm z",
			"dd-MMM-yyyy HH:mm", "dd/MMM/yyyy HH:mm", "dd-MMM-yyyy z", "dd/MMM/yyyy z", "dd-MMM-yyyy", "dd/MMM/yyyy",
			"yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssz", "yyyy-MM-dd'T'HH:mm:ssZ",
			"yyyy-MM-dd'T'HH:mm.S", "yyyy-MM-dd'T'HH:mm.S", "yyyy-MM-dd'T'HH:mm:ss.S", "yyyy-MM-dd'T'HH:mm:ss.SS",
			"yyyy-MM-dd'T'HH:mm:ss'Z", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z", "yyyy-MM-dd'T'HH:mm:ss.SSSZZ"};

	// build the brics formatter
	static {
		DateTimeParser[] parsers = new DateTimeParser[BRICS_DATE_FORMATS.length];

		for (int i = 0; i < BRICS_DATE_FORMATS.length; i++) {
			parsers[i] = DateTimeFormat.forPattern(BRICS_DATE_FORMATS[i]).getParser();
		}

		BRICS_DATE_FORMATTER = new DateTimeFormatterBuilder().append(null, parsers).toFormatter()
				.withZone(DateTimeZone.forOffsetHours(TIMEZONE_UTC_OFFSET));
	}

	public static final String DE_DISEASE_SPLIT_EXPRESSION = "\\.";

	public static final int DOMAIN_INDEX = 1;
	public static final int SUBDOMAIN_INDEX = 2;
	public static final int SUBGROUP_INDEX = 1;
	public static final int DISEASE_INDEX = 0;
	public static final int CLASSIFICATION_INDEX = 2;

	public static final String DOWNLOAD_QUEUE_NOTIFICATION_SUBJECT = "downloadTool.subject";
	public static final String DOWNLOAD_QUEUE_NOTIFICATION_BODY = "downloadTool.body";

	public static final String DOWNLOAD_QUEUE_MISSING_FILE_SUBJECT = "downloadMissingFile.subject";
	public static final String DOWNLOAD_QUEUE_MISSING_FILE_BODY = "downloadMissingFile.body";
	public static final String RDF_UPLOAD_FAILED_SUBJECT = "rdfUploadFailed.subject";
	public static final String RDF_UPLOAD_FAILED_BODY = "rdfUploadFailed.body";
	public static final String RDF_GEN_FAILED_SUBJECT = "rdfGenFailed.subject";
	public static final String RDF_GEN_FAILED_BODY = "rdfGenFailed.body";
	public static final String MAIL_RESOURCE_COMPLETE_DATASET_SUBMISSION_SUBJECT = "completeDatasetSubmission.subject";
	public static final String MAIL_RESOURCE_COMPLETE_DATASET_SUBMISSION_BODY = "completeDatasetSubmission.body";
	public static final String MAIL_RESOURCE_ERROR_LOAD_SUBMISSION_SUBJECT = "errorDuringLoadDataset.subject";
	public static final String MAIL_RESOURCE_ERROR_LOAD_SUBMISSION_BODY = "errorDuringLoadDataset.body";
	public static final String MAIL_RESOURCE_COMMON_FOOTER = "common.footer";

	public static final String META_NAME_SUFFIX = " COPY";
	
	//Saved Query Constants
	public static final String ZIP_EXTENSION=".zip";
	public static final String VERSION_ONE ="1.0";
	public static final String QUERY_TOOL = "Query Tool";
	
	// added by Ching-Heng for PRO project	
	public static final String CAT_T_SCORE="catTSCORE";
	public static final String CAT_SE="catStandardError";
	public static final String CAT_FINAL_T_SCORE="catFinalTSCORE";
	public static final String CAT_FINAL_SE="catFinalStandardError";
	public static final String POSITION="catQuestionPosition";
	public static final String ADAPTIVE="adaptive";
	public static final String AUTO_SCORING="autoScoring";
	public static final String SHORT_FORM="shortForm";
	public static final String ADAPTIVE_FULL="Adaptive Instrument";
	public static final String AUTO_SCORING_FULL="Auto-Scoring";
	public static final String SHORT_FORM_FULL="Short-Form";
	
	
	public static final int EXPIRATION_SOON_DAYS=30;
	public static final int EMAIL_REPORT_MAX_ROW = 15;
	

	/*************************** Account **************************************/
	
	public static final String ACCOUNT_RENEWAL_SUBJECT= "ACTION REQUIRED:  Updates needed for Continued Access to %s";
	
	// this is looking for five arguments to build the email body
	public static final String ACCOUNT_RENEWAL_BODY_FORMAT = "%s <br> %s <br> %s <br> %s <br> %s <br>";
	
	public static final String ACCOUNT_RENEWAL_INFO_FORMAT = "Hello, <br><br>"
			+ "You currently have access to these modules within %s with the following expiration dates:";
	
	public static final String ACCOUNT_RENEWAL_SYSTEM_MSG_FORMAT = "These modules are being reviewed for continued access by a System Administrator. "
			+ "Updates to your account may be required. Please review the information provided for continued %s Access below: <br>";
	
	public static final String ACCOUNT_RENEWAL_MSG_FORMAT = "%s <br>%s";
	
	public static final String ACCOUNT_RENEWAL_FOOTER_FORMAT = "Please log in to your account at %s and correct any issue(s) noted with your account "
			+ "and make any updates necessary for continued access to each module. Modules no longer required should be allowed to expire.  "
			+ "Updates and requests will be reviewed by the System Administrator.  <br>If you have any questions, please reach out to %s. <br><br> Thank you,<br>%s Operations at NIH/CIT";
	
	public static final String ACCOUNT_RENEWAL_TABLE_HEADER ="<tr><td>Role</td><td>Expiration Date</td></tr>";
	
	// this is looking for three arguments to put into a single html table row
	public static final String ACCOUNT_RENEWAL_ROW_FORMAT =
				"<tr><td>%s</td><td>%s</td></tr>";
	
	public static final String ACCOUNT_RENEWAL_COMMENT_DELIMITER = "/";
	
	public static final int META_STUDY_BACTH_SIZE = 100;
	
	public static final String USERNAME_PARAM = "abcd";
	public static final String PASSWORD_PARAM = "abcd";
	
	public static final String DUC_EXPIRATION_SUBJECT_PROPERTY = "ducExpiration.subject";
	public static final String DUC_EXPIRATION_MESSAGE_BODY_PROPERTY = "ducExpiration.body";
	public static final String DUC_EXPIRED_OPS_SUBJECT_PROPERTY = "ducExpiredOps.subject";
	public static final String DUC_EXPIRED_OPS_MESSAGE_BODY_PROPERTY = "ducExpiredOps.body";
	
	public static final String UNABLE_TO_DELETE_EFORM_BC_OF_COLLECTIONS = "This eform has data collected against it and cannot be deleted. If you have any questions please reach out to the operations team.";
	public static final String UNABLE_TO_DELETE_EFORM_BC_OF_EFORMS_IN_VT = "This eform is attached to one or more Visit Types and cannot be deleted. If you have any questions please reach out to the operations team.";
	
	/*************************** Visualization and Summary Constants **************************************/
	public static final String SUMMARY_DATA_GEN_FAILED_SUBJECT = "summaryDataGenFailed.subject";
	public static final String SUMMARY_DATA_GEN_FAILED_BODY = "summaryDataGenFailed.body";
	public static final String SUMMARYDATA_LOG_PATH = "/opt/apache-tomcat-portal/logs/summaryData.log";

}
