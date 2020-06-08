package gov.nih.nichd.ctdb.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * This class will hold all constant values to be used by the CTDB Application across modules.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class CtdbConstants {
	// From value indication whether it's single or double entry data collection corresponds to column---->dataentryflag
	// in form table
	public static final int SINGLE_ENTRY_FORM = 1;
	public static final int MAX_COL_Q_IN_SECTION = 12;

	// Form Range Operator Types
	public static final int FORM_ISEQUAL_RANGEOPERATOR = 1;
	public static final int FORM_ISLESSTHAN_RANGEOPERATOR = 2;
	public static final int FORM_ISGREATERTHAN_RANGEOPERATOR = 3;
	public static final int FORM_INBETWEEN_RANGEOPERATOR = 4;

	// From status in use inactive 1 active 3 in progress 4
	public static final int FORM_STATUS_ACTIVE = 3;
	public static final int FORM_STATUS_INPROGRESS = 4;
	public static final int FORM_STATUS_INACTIVE = 1;
	// From Type
	public static final int FORM_TYPE_SUBJECT = 10;
	public static final int FORM_TYPE_SAMPLE = 11;
	public static final int FORM_TYPE_STUDY = 12;
	public static final int FORM_TYPE_PROJECT = 13;
	public static final int FORM_TYPE_ADMIN = 14;
	public static final int FORM_TYPE_OTHER = 15;

	public static final String FORM_TYPE_SUBJECT_STRING = "Subject";

	// Question Type
	public static final String QUESTION_VISUAL_SCALE_TYPE = "Visual Scale";
	public static final String QUESTION_IMAGE_MAP_TYPE = "Image Map";
	public static final String QUESTION_SELECT_TYPE = "Select";
	public static final String QUESTION_RADIO_TYPE = "Radio";
	public static final String QUESTION_MULTI_SELECT_TYPE = "Multi-Select";
	public static final String QUESTION_CHECKBOX_TYPE = "Checkbox";
	public static final String QUESTION_CALCULATED_TYPE = "Calculated";
	public static final String QUESTION_TEXTAREA_TYPE = "Textarea";
	public static final String QUESTION_TEXTBOX_TYPE = "Textbox";
	// Data Collection Status Added by yogi 5/3/2013 to be stored in dataentrydraft table coll_status column
	public static final String DATACOLLECTION_STATUS_COMPLETED = "Completed";
	public static final String DATACOLLECTION_STATUS_COMPLETED_R = "Completed(Required)";
	public static final String DATACOLLECTION_STATUS_COMPLETED_O = "Completed(Optional)";
	public static final String DATACOLLECTION_STATUS_LOCKED = "Locked";
	public static final String DATACOLLECTION_STATUS_LOCKED_R = "Locked(Required)";
	public static final String DATACOLLECTION_STATUS_LOCKED_O = "Locked(Optional)";
	public static final String DATACOLLECTION_STATUS_FINALLOCKED = "Final Lock";
	public static final String DATACOLLECTION_STATUS_INPROGRESS = "In Progress";
	public static final String DATACOLLECTION_STATUS_REASSIGNED = "Reassigned";
	public static final String DATACOLLECTION_STATUS_STARTED = "Started";
	public static final String DATACOLLECTION_STATUS_INPROGRESS_R = "In Progress(Required)";
	public static final String DATACOLLECTION_STATUS_INPROGRESS_O = "In Progress(Optional)";
	public static final String DATACOLLECTION_STATUS_NOTSTARTED = "Not Started";
	public static final String DATACOLLECTION_STATUS_NOTSTARTED_R = "Not Started(Required)";
	public static final String DATACOLLECTION_STATUS_NOTSTARTED_O = "Not Started(Optional)";
	public static final String DATACOLLECTION_STATUS_CERTIFIED = "Certified"; // will are not using it in ibis->proforms
																				 // just put it since we have old code
																				 // floating around with it

	// colors used by data collection status, to be consistent with the colors in dataCollection.css from line 229 to
	// line 239
	public static final String DATACOLLECTION_STATUS_LOCKED_GREEN = "#80FF7F";
	public static final String DATACOLLECTION_STATUS_INPROGRESS_RED = "#FF7F7F";
	public static final String DATACOLLECTION_STATUS_COMPLETED_YELLOW = "#FFFF7F";
	public static final String DATACOLLECTION_STATUS_NOTSTARTED_WHITE = "#CDC9C9";

	// audit comment status
	public static final String AUDITCOMMENT_STATUS_COMPLETED = "C";
	public static final String AUDITCOMMENT_STATUS_LOCKED = "L";
	public static final String AUDITCOMMENT_STATUS_INPROGRESS = "I";

	// Data Collection Action string variables
	public static final String DATACOLLECTION_FORMPATIENT = "formPatient";
	public static final String DATACOLLECTION_MODE = "mode";
	public static final String DATACOLLECTION_AUDIT = "audit";
	public static final String DATACOLLECTION_SQ_SHOW = "sqshow";
	public static final String DATACOLLECTION_SID = "sId";
	public static final String DATACOLLECTION_QID = "qId";
	public static final String DATACOLLECTION_EDITMODE = "editMode";
	public static final String DATACOLLECTION_PATIENTMODE = "patient";

	public static final String YES = "Y";
	public static final String NO = "N";

	public static final String REQUIRED = "Required";
	public static final String OPTIONAL = "Optional";
	public static final String FILE_EXIST = "fileExist";
	public static final String ASSOCFILEQUESTIONIDS = "assocFileQuestionIds";
	public static final String VISIT_TYPE_OPTIONS = "intervalOptions";
	public static final String VISIT_TYPE_SCHEDULER_LIST_KEY = "intervalSchedulerStatus";
	public static final String DATACOLLECTION_DISPLAY_VISITDATE_WARNING = "displayVisitDateWarning";


	public static final String FORM_STATUS_ACTIVE_SHORTNAME = "Active";
	public static final String FORM_STATUS_INPROGRESS_SHORTNAME = "In Progress";
	public static final String FORM_STATUS_INACTIVE_SHORTNAMe = "Inactive";

	/**
	 * CTDB Constant used to encrypt/decrypt strings
	 */
	public static final String CRYPTO_PHRASE = "ODHFOISHDFISD09UW0348092U39JF829HJ982HF89H2398FHIOH";
	public static final String ENCRYPTION_KEY = "413EF868BE4575014DDDDDA00C22940F59A6C46147710F6B";

	/**
	 * Global Application Names
	 */
	public static final String GLOBAL_APPNAME_PDBP = "pdbp";
	public static final String GLOBAL_APPNAME_FITBIR = "fitbir";
	public static final String GLOBAL_APPNAME_CNRM = "cnrm";
	public static final String GLOBAL_APPNAME_CISTAR = "cistar";
	public static final String GLOBAL_APPNAME_NEI = "nei";
	public static final String GLOBAL_APPNAME_EYEGENE = "eyegene";
	public static final String GLOBAL_APPNAME_NTI = "nti";
	public static final String GLOBAL_APPNAME_CDRNS = "cdrns";


	public static final String LOCAL_CIT_NIH_GOV = "local.cit.nih.gov";
	/**
	 * Global URL Environments on DEV
	 */

	public static final String URL_HOST_FITBIR_DEV = "https://fitbir-dev.cit.nih.gov";
	public static final String URL_HOST_PDBBP_DEV = "https://pdbp-dev.cit.nih.gov";

	/**
	 * Global URL Environments on Stage
	 */

	public static final String URL_HOST_FITBIR_STAGE = "https://fitbir-stage.cit.nih.gov";
	public static final String URL_HOST_PDBP_STAGE = "https://pdbp-stage.cit.nih.gov";
	public static final String URL_HOST_CISTAR_STAGE = "https://cistar-stage.cit.nih.gov";
	public static final String URL_HOST_NEI_STAGE = "https://bricsnei-stage.cit.nih.gov";
	public static final String URL_HOST_CDRNS_STAGE = "https://cdrns-stage.cit.nih.gov";


	/**
	 * Global URL Environments on UAT
	 */
	public static final String URL_HOST_FITBIR_UAT = "https://fitbir-uat.cit.nih.gov";
	public static final String URL_HOST_PDBP_UAT = "https://pdbp-uat.cit.nih.gov";


	/**
	 * Global constant for initializing JSON array strings to an empty array.
	 */
	public static final String EMPTY_JSON_ARRAY_STR = "[]";

	/**
	 * Global constant for initializing JSON object strings to an empty object.
	 */
	public static final String EMPTY_JSON_OBJECT_STR = "{}";

	/**
	 * CTDB Constant used to store user cookie
	 */
	public static final String CTDB_USER_COOKIE = "CtdbUserCookie";

	/**
	 * CTDB constant used to specify the path to the user file root directory
	 */
	public static final String FILE_STORAGE_DIR = "filesystem.storageRoot";

	/**
	 * CTDB constant used to store the success messages in session to be used across action redirects.
	 */
	public static final String SUCCESS_MSG_SESSION_KEY = "_success-msg-key";

	/**
	 * CTDB Constant used as the key/id for the user object in session
	 * 
	 * @deprecated Use the {@link BaseAction#getUser()} method instead. Or implement a similar method in a related
	 *             parent class.
	 */
	public static final String USER_SESSION_KEY = "user";

	/**
	 * CTDB Constant used to identify the username of the anonymous user in Spring Security.
	 */
	public static final String ANONYMOUS_USER_NAME = "anonymous";

	/**
	 * CTDB Constant used as the key/id for the GUID JWT object in session.
	 */
	public static final String GUID_JWT_SESSION_KEY = "guid-jwt-key";

	/**
	 * CTDB Constant used as the key/id for the current protocol object in session
	 */
	public static final String CURRENT_PROTOCOL_SESSION_KEY = "protocol";

	/**
	 * CTDB Constant used as the key/id for the current project list object in session
	 */
	public static final String PROTOCOL_PROJECT_LIST = "_projectHome_projects";

	/**
	 * CTDB Constant used as the key/id for the list of study sites associated with the study object in session
	 */
	public static final String UNIQUE_ID = "__uniqueId";

	/**
	 * CTDB Constant used as the key/id for the list of study sites associated with the study object in session
	 */
	public static final String CURRENT_PROTOCOL_STUDY_SITES_SESSION_KEY = "__studySites";

	/**
	 * CTDB Constant used as the key/id for the list of study sites associated with the study object in session
	 */
	public static final String SITE_HASH_MAP = "__SiteHaspMap";

	/**
	 * CTDB Constant used as the key/id for the list of study sites associated with the study object in session
	 */
	public static final String HASH_MAP_KEY = "(|~!@#$%^&*|)";

	/**
	 * CTDB Constant used as the key/id for the list of study sites associated with the study object in session
	 */
	public static final String DRUG_DEVICE_HASH_MAP = "__DrugDeviceHaspMap";

	/**
	 * CTDB Constant used as the key/id for the hash map of intervals associated with the study object in session
	 */
	public static final String INTERVAL_HASH_MAP = "__intervalHaspMap";

	/**
	 * Constant used as the key/id to indicate whether or not the system should accept PII data.
	 */
	public static final String CAN_USE_PII_KEY = "guid_with_non_pii";

	/**
	 * CTDB Constant used as the attachment list associated with a specific patient in the session
	 */

	public static final String ATTACHMENT_TYPE_ATTR = "_attachments_type";
	public static final String PATIENT_ATTACHMENTS_SESSION_KEY = "_attachments";
	public static final String ACTION_PROCESS_ATTACHMENT = "process_attachment";
	public static final String ACTION_PROCESS_PATIENTVISIT = "process_patientvist";
	public static final String ATTACHMENT_HASH_MAP = "_attachmentHashMap";
	public static final String ATTACHED_FILE = "_attached_file";
	public static final String PATIENT_VISIT_DATE_LIST_KEY = "_patientVisits";
	public static final String PATIENT_LIST_KEY = "_patientList";
	public static final String PRE_POP_DE_CACHE_KEY = "_pre-pop-data-elem-cache";
	public static final String FORM_XML_IMPORT = "_form_xml_import";
	public static final String QUESTIONS_XML_IMPORT = "_questions_xml_import";
	public static final String AFORM_CACHE_KEY = "acKey";

	/**
	 * CTDB Constant used as the key to retrieve the id parameter (used for viewing/editing an object)
	 */
	public static final String ID_REQUEST_ATTR = "id";
	public static final String VERSION_REQUEST_ATTR = "version";
	public static final String SQ_ID_REQUEST_ATTR = "sqid";
	/**
	 * CTDB Constant used as the key to retrieve the id parameter (used for selecting form for data collection)
	 */
	public static final String FORM_ID_REQUEST_ATTR = "formId";
	public static final String FORM_TYPE_REQUEST_ATTR = "formType";
	public static final String AFORM_ID_REQUEST_ATTR = "aformId";
	public static final String PATIENT_ID_REQUEST_ATTR = "patientId";
	public static final String IS_CAT = "isCat";
	public static final String MEASUREMENT_TYPE = "measurementType";
	public static final String FORM_DS_NAME_REQUEST_ATTR = "formDSName";
	/**
	 * CTDB Constant used as the key to retrieve the uses tab attribute from the request. This is used to determine if
	 * the JSPs uses tabs to display data.
	 */
	public static final String USES_TABS = "jspUsesTabs";
	/**
	 * CTDB Form Constant used as the key to retrieve the protocol id from the session object
	 */
	public static final String ID_PROTOCOL = "id_protocol";

	/**
	 * CTDB Form Constant used as the key to retrieve the protocol name from the session object
	 */
	public static final String NAME_PROTOCOL = "name_protocol";

	/**
	 * The request attribute key for the left navigation tree
	 */
	public static final String NAVIGATION_LEFTNAV_KEY = "leftnav_session";
	/**
	 * Navigation constant which defines that the "active" main link is "my workspace"
	 */
	public static final String NAVIGATION_SELECTED_WOKSPACE = "workspace";
	/**
	 * Navigation constant which defines that the "active" main link is "manage patients"
	 */
	public static final String NAVIGATION_SELECTED_MANAGEPATIENTS = "manage_patients";
	/**
	 * Navigation constant which defines that the "active" main link is "collect data"
	 */
	public static final String NAVIGATION_SELECTED_COLLECTDATA = "collect_data";
	/**
	 * Navigation constant which defines that the "active" main link is "manage forms"
	 */
	public static final String NAVIGATION_SELECTED_FORMS = "forms";
	/**
	 * Navigation constant which defines that the "active" main link is "manage studies"
	 */
	public static final String NAVIGATION_SELECTED_STUDIES = "manage_studies";
	/**
	 * Navigation constant which defines that the "active" main link is "report/query"
	 */
	public static final String NAVIGATION_SELECTED_QUERY = "report_query";

	public static final String USER_PROTOCOL_LIST = "user_protocol_list";

	/*****************************************
	 * XSL CONSTANTS
	 *
	 * Constants to be used by the system for XSL parameters
	 *****************************************/

	/**
	 * CTDB Constant XSL Parameter Map initialized using a static initializer
	 */
	public static HashMap<String, String> GLOBAL_XSL_PARAMETER_MAP = null;


	/********************************************
	 * Version Function Constants
	 */

	/**
	 * There are 26 Letters in the English Alphabet. This is required for version conversions.
	 */
	public static final int NUM_LETTERS_IN_ALPHABET = 26;

	/**
	 * Evaluating Character.getNumericValue("A") gives 10, subtracting 9 gives 1
	 */
	public static final int CHAR_TO_INT_OFFSET = 9;

	/**
	 * The Regular Expresion Needed to ensure a string at least length 1 consisting of only capital letters
	 */
	public static final String CAP_LETTERS_REGEX = "[A-Z]+";

	/**
	 * The tag used to designate the name for a textbox holding the version for a question
	 */
	public static final String QUESTION_VERSION_TEXTBOX_TAG = "question_";

	public static final String ED_PATIENTID_SESSION = "patient";

	public static final String EVENT_EDIT_PROCESS = "eventeditprocess";

	public static final String EVENT_PROTOCOL = "eventprotocol";

	/**
	 * The tag used to designate the name for "Other" option and code in checkbox, select, multi-select and radio
	 * question
	 */
	public static final String OTHER_OPTION = "Other, please specify | null | ";
	public static final String OTHER_OPTION_DISPLAY = "Other, please specify";

	/**
	 * constants used for pre-population values
	 */
	public static final String PREPOPULATION_NONE = "none";
	public static final String PREPOPULATION_PRIMARYSITENAME = "primarySiteName";
	public static final String PREPOPULATION_VISITTYPE = "visitType";
	public static final String PREPOPULATION_VISITDATE = "visitDate";
	public static final String PREPOPULATION_GUID = "guid";

	/**
	 * constant for record soft delete flag value in the database
	 */
	public static final boolean DATABASE_DELETE_FLAG_TRUE = true;
	public static final int DATABASE_DELETE_FLAG_TRUE_INT = 1;

	/**
	 * constant for appending record (unique constraint) soft delete flag value in the database
	 */
	public static final String DATEFORMAT_FOR_SOFT_DELETED_RECORD = "yyyy-MM-dd HH:mm:ss.ms";

	/**
	 * constant for HTTP Header Name
	 */
	public static final String HTTP_HEADER_REFERER = "referer";

	public static final String SECURITY_ACCESS_DENIED = "accessdenied";

	public static final String AD_ADAPTER_GSS = "ad_adapter_gss";

	public static final int LOGIN_METHOD_NONE = 0;
	public static final int LOGIN_METHOD_SSO = 1;
	public static final int LOGIN_METHOD_LDAP = 2;

	public static final int PROTOCOL_TYPE_STANDARD = 13;/// S-Screening - Protocol Type (Check with Tsega)
	public static final int PROTOCOL_TYPE_NATURAL_HISTORY = 1;
	public static final int PROTOCOL_TYPE_NATURAL_HISTORY_1 = 1;
	public static final int PROTOCOL_TYPE_NATURAL_HISTORY_2 = 2;
	public static final int PROTOCOL_TYPE_NATURAL_HISTORY_3 = 3;
	public static final int PROTOCOL_TYPE_NATURAL_HISTORY_4 = 4;

	public static final int PATIENT_DISPLAY_ID = 1;
	// Not used currenty
	public static final int PATIENT_DISPLAY_NAME = 2;
	// Not used currently
	public static final int PATIENT_DISPLAY_SUBJECT = 3;
	public static final int PATIENT_DISPLAY_GUID = 4;
	public static final int PATIENT_DISPLAY_MRN = 5;

	public static final String PATIENTID_DISPLAY = "Subject ID";
	public static final String PATIENTNAME_DISPLAY = "Subject Name";
	public static final String PATIENTSUBJECT_DISPLAY = "Subject Number";
	public static final String SUBJECT_TITLE_DISPLAY = "Subject";
	public static final String SUBJECT_GUID_DISPLAY = "GUID";
	public static final String SUBJECT_MRN_DISPLAY = "MRN";
	public static final String SUBJECT_VISIT_DATE = "pVisitDate";
	public static final String SUBJECT_INTERVAL_NAME = "intervalName";

	public static final String PATIENTLASTNAME = "lastName";
	public static final String PATIENTFIRSTNAME = "First Name";

	public static final String PROTOCOL_CLOSED_SESSION_KEY = "protocolclosedout";
	public static final int DEFAULT_BINDER_INSTANCE = -10;


	public static final int CLINICAL_TRIAL_5 = 5;
	public static final int CLINICAL_TRIAL_6 = 6;
	public static final int CLINICAL_TRIAL_7 = 7;
	public static final int CLINICAL_TRIAL_8 = 8;
	public static final int CLINICAL_TRIAL_9 = 9;
	public static final int CLINICAL_TRIAL_10 = 10;
	public static final int CLINICAL_TRIAL_11 = 11;

	public static final String PDBP = "pdbp";
	public static final String FITBIR = "fitbir";
	public static final String CNRM = "cnrm";

	static {
		CtdbConstants.GLOBAL_XSL_PARAMETER_MAP = new HashMap<String, String>();

		String webroot = SysPropUtil.getProperty("app.webroot");
		CtdbConstants.GLOBAL_XSL_PARAMETER_MAP.put("webroot", webroot);

		String imageRoot = SysPropUtil.getProperty("app.imageroot");
		CtdbConstants.GLOBAL_XSL_PARAMETER_MAP.put("imageroot", imageRoot);

		String cssStyleSheet = SysPropUtil.getProperty("app.stylesheet");
		CtdbConstants.GLOBAL_XSL_PARAMETER_MAP.put("cssstylesheet", cssStyleSheet);

		String title = ResourceBundle.getBundle("ApplicationResources").getString("app.title");
		CtdbConstants.GLOBAL_XSL_PARAMETER_MAP.put("title", title);

		String dictionaryWsRoot = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		CtdbConstants.GLOBAL_XSL_PARAMETER_MAP.put("dictionaryWsRoot", dictionaryWsRoot);
	}

	/**
	 * constants for the left navigation header links
	 */
	public static final String WORKSPACE_HEADER_LINK_NAME = "ProFoRMS Home";
	public static final String SUBJECTS_HEADER_LINK_NAME = "Manage Subjects";
	public static final String DATA_HEADER_LINK_NAME = "Collect Data";
	public static final String FORMS_HEADER_LINK_NAME = "Manage Forms";
	public static final String STUDY_HEADER_LINK_NAME = "Manage Protocol";
	public static final String REPORT_HEADER_LINK_NAME = "Report & Query";
	public static final String ADMIN_HEADER_LINK_NAME = "Site Administration";

	/**
	 * constant for accepting file formats of images
	 */
	public static final List<String> IMAGE_FORMATS =
			Collections.unmodifiableList(Arrays.asList("jpg", "gif", "jpeg", "png"));
	public static final String DELETE_ALL_IMAGES = "Delete All Images";
	public static final String GIF = "gif";
	public static final String JPG = "jpg";
	public static final String JPEG = "jpeg";
	public static final String PNG = "png";
	public static final String FILE_EXTENSION_NOT_SUPPORTED_ERROR_TEXT = "Please upload file type GIF, JPG, JPEG, PNG.";


	// My collections page search & collection status
	public static final String SEARCH_STATUS_MyCollection = "status";
	public static final String COLLECTION_STATUS_LOCKED = "Locked";
	public static final String COLLECTION_STATUS_INPROGRESS = "In Progress";
	public static final String COLLECTION_STATUS_COMPLETED = "Completed";
	// Data Collection serach parms
	public static final String DATA_COLLECTION_EFORMS_NAME = "eformName";
	public static final String DATA_COLLECTION_FORM_STATUS = "formStatus";
	public static final String DATA_COLLECTION_FORM_LAST_UPDATED = "formLastUpd";
	public static final String DATA_COLLECTION_VISIT_DATE = "visitDate";
	public static final String DATA_COLLECTION_SUBJECT_GUID = "subjectGuid";
	/* Constant for predefined visit types */
	// public static final List<String> PREDEFINED_VISIT_TYPE_LIST = Collections.unmodifiableList(Arrays.asList("6
	// months", "12 months","18 months","24 months","30 months","36 months","42 months","48 months","54 months","60
	// months","Baseline","Screening"));
	public static final String isSelfReporting = "isSelfReporting";

	public static final String PSR_SCHEDULED_VISIT_DATE = "psrScheduledVisitDate";
	public static final String VISIT_DATE_DATA_ELEMENT = "VisitDate";
	public static final String VISIT_TYPE_DATA_ELEMENT = "VisitType";
	public static final String VISIT_TYP_PDBP_DATA_ELEMENT ="VisitTypPDBP";
	public static final String GUID_DATA_ELEMENT = "GUID";
	public static final String SITE_NAME_DATA_ELEMENT = "SiteName";
	public static final String AE_START_DATE_DATA_ELEMENT = "AdvrsEvntStartDateTime"; //"AdverseEventStartDateTime";
	public static final String AE_END_DATE_DATA_ELEMENT   = "AdverseEvntEndDateTime"; //"AdverseEventEndDateTime";
	
	

	public static final String EFORM_SECTIONLIST = "eformSectionList";
	public static final String EFORM_NAME = "eformName";
	public static final String EFORM_CALC_QUESTIONS_LIST = "eformCalcQuestionsList";
	public static final String EFORM_CALC_DEPENDENT_QUESTIONS_LIST = "eformCalcDependentQuestionsList";
	public static final String EFORM_SKIP_QUESTIONS_LIST = "eformSkipQuestionsList";
	public static final String EFORM_SKIP_DEPENDENT_QUESTIONS_LIST = "eformSkipDependentQuestionsList";
	
	public static final String PROMIS_MAIN_SECTION = "Main";
	public static final String PROMIS_REQ_SECTION = "Required Fields";
	public static final String PROMIS_FA_SECTION = "Form Administration";
	public static final String PROMIS_AED_SECTION = "Adverse Event Description";
	public static final String PROMIS_AE_SECTION = "Adverse Event";

	public static final String PROMIS_API_URL = "healthMeasurement.api.url";
	public static final String PROMIS_API_TOKEN = "healthMeasurement.api.token";

	public static final String AE_START_DATE_QUESTION = "Start Date";
	public static final String AE_END_DATE_QUESTION = "End Date";
	public static final String VISIT_DATE_QUESTION = "Visit date";
	public static final String QUESTION_NO_ANSWER = "None";

	//Added for Populating default value for disabled questions due to skip rule
	public static final String EXPORT_DEFAULT_SKIP_DISABLE_VALUE = "BLANK-SKIPPED";
	
	public static final int PSRHEADER_MAX_LENGTH = 1000;
	
	public static final String IS_EFORM_CONFIGURED = "isEformConfigured";
	
	public static final String REPORT_PARAM = "report";
	public static final String PROTOCOL_REPORT = "protocol";
	public static final String DETAILED_PROTOCOL_REPORT = "detailedProtocol";
	
	public static final String GUID_ERR_TEXT = "GUID entered in form does not match the GUID the collection was started on. If there appears to be an error in prepopulation, please delete this collection and clear your browser cache.";
	public static final String VTYPE_ERR_TEXT = "Visit Type entered in form does not match the Visit Type the collection was started on. If there appears to be an error in prepopulation, please delete this collection and clear your browser cache.";
	
	/*CISTAR-637: user associated to a site will have limit access to subject info */
	public static final String USER_WITH_SITE_LIMIT = "user_with_site_limit";
	
	/* Btris Constants*/
	
	public static final String BTRIS_DATA_ELEMENT_UNIT = "_unit";
	public static final String BTRIS_DATA_ELEMENT_COMMENT = "_comment";
	public static final String BTRIS_DATA_ELEMENT_RANGE = "_range";
	public static final String BTRIS_DATA_ELEMENT_DATE = "_date";
	
	public static final int DATA_COLLECTION_MAX_CHARS_LEFT_NAV_EFORM = 20;
	
	}
