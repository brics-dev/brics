package gov.nih.tbi.constants;

public class ReportingPortalConstants {
	
	public final static int DUC_FILE=11;
	
	public final static String WHITESPACE = " ";
	public final static String NEW_LINE = "\n";
	public final static String FORWARD_SLASH = "/";
	public final static String BACKWARD_SLASH = "\\";
	/******************** Property Names *****************************/

	// public final static String ORG_NAME_ARG = "org";
	// public final static String ORG_PHONE_ARG = "orgphone";
	// public final static String ORG_EMAIL_ARG = "orgemail";
	// public final static String DEV_EMAIL_ARG = "devemail";

	/******************** Mail Messages *****************************/

	public final static String MAIL_RESOURCE_FORGOT_PASSWORD = "forgotPassword";
	public final static String MAIL_RESOURCE_FORGOT_PASSWORD_SUCCESS = "forgotPasswordSuccess";
	// public final static String MAIL_RESOURCE_CHANGE_PASSWORD = "changePassword";
	public final static String MAIL_RESOURCE_ACCEPTED_ACCOUNT = "acceptedAccount";
	public final static String MAIL_RESOURCE_REJECTED_ACCOUNT = "rejectedAccount";
	public final static String MAIL_RESOURCE_ACCEPTED_STUDY = "acceptedStudy";
	public final static String MAIL_RESOURCE_REJECTED_STUDY = "rejectedStudy";
	public final static String MAIL_RESOURCE_PENDING_ACCOUNT = "pendingAccount";
	public final static String MAIL_RESOURCE_USERNAME_RECOVERY = "userNameRecovery";
	public final static String MAIL_RESOURCE_ADMIN_ACCOUNT_CREATE = "adminAccountCreate";
	public final static String MAIL_RESOURCE_ORDER_CHANGES = "orderChanges";
	public final static String MAIL_RESOURCE_COMMON = "common";

	public final static String MAIL_RESOURCE_TO = ".to";
	public final static String MAIL_RESOURCE_SUBJECT = ".subject";
	public final static String MAIL_RESOURCE_HEADER = ".header";
	public final static String MAIL_RESOURCE_BODY = ".body";
	public final static String MAIL_RESOURCE_FOOTER = ".footer";
	public final static String MAIL_RESOURCE_PD = ".pd";

	public static final String DATASET_SHARE = "dataset.share";
	public static final String DATASET_ARCHIVE = "dataset.archive";
	public static final String MAIL_RESOURCE_ACCEPTED_DATASTRUCTURE = "acceptedDataStructure";
	public static final String MAIL_RESOURCE_REJECTED_DATASTRUCTURE = "rejectedDataStructure";
	public static final String MAIL_RESOURCE_ACCEPTED_DATASET = "acceptedDataset";
	public static final String MAIL_RESOURCE_REJECTED_DATASET = "rejectedDataset";
	public static final String MAIL_RESOURCE_ACCEPTED_DATAELEMENT = "acceptedDataElement";
	public static final String MAIL_RESOURCE_REJECTED_DATAELEMENT = "rejectedDataElement";
	public static final String MAIL_RESOURCE_ACCOUNT_REACTIVATION = "accountReactivation";
	public static final String MAIL_RESOURCE_CHANGE_STATUS_BULK_DATASETS= "changeStatusBulkDatasets";
	public static final String MAIL_RESOURCE_ACCEPTED_BULK_DATASETS = "acceptedBulkDatasets";
	public static final String MAIL_RESOURCE_REJECTED_BULK_DATASETS = "rejectedBulkDatasets";
	public static final String MAIL_RESOURCE_CHANGE_STATUS_DATASET = "changeStatusDataset";
	/******************** Data Structure *****************************/

	public final static String DATASTRUCTURE_ID = "dataStructureId";
	public final static String DATASTRUCTURE_NAME = "dataStructureName";
	public final static String VERSION = "version";
	public static final String VERSION_NEW = "1.0";
	public static final String REDIRECT = "redirect";
	public static final String DATABASE_NAME_REGEX = "[A-Za-z][A-Za-z0-9_]*";
	/******************** Data Structure/Element *****************************/

	public final static String MAIN_GROUP_NAME = "Main";
	public final static String MAIN_GROUP = "Main Section";
	public final static String TBI = "Traumatic Brain Injury";
	public static final String REQUIRED = "required";
	public static final String PROHIBITED = "prohibited";

	/******************** Data Element ******************************/

	public final static String DATAELEMENT = "dataElement";
	public final static String DATAELEMENT_ID = "dataElementId";
	public final static String DATAELEMENT_NAMES = "dataElementNames";
	public final static int DE_DEFAULT_PAGESIZE = 10;
	public static final String DETAILS = "details";
	public static final String KEYWORDS = "keywords";
	public static final String PREVPAGE = "prevPage";
	public static final String DATAELEMENT_FILTER_DISEASE = "diseaseSelection";
	public static final String DATAELEMENT_FILTER_DOMAIN = "domainSelection";

	/******************** Map Element *******************************/

	public static final String POSITION = "position";
	public final static String MAPELEMENT = "mapElement";
	public final static String MAPELEMENT_ID = "mapElementId";
	public static final String GROUPELEMENT_ID = "groupElementId";
	public static final String REPEATABLEGROUP_ID = "repeatableGroupId";
	public static final String REQUIREDTYPE_ID = "requiredTypeId";
	public final static String MAPELEMENT_NAME = "mapElementName";
	public final static String MAPELEMENT_ROWID = "rowId";
	public final static String ATTACHMENTS = "attachments";

	/******************** Action Results ****************************/

	public final static String ACTION_INPUT = "input";
	public final static String ACTION_EDIT = "edit";
	public final static String ACTION_EDIT_COPY = "editCopy";
	public final static String ACTION_CREATE = "create";
	public final static String ACTION_VIEW = "view";
	public final static String EXCEPTION = "exception";
	public final static String ACTION_BASIC_VIEW = "basicView";
	public final static String ACTION_VIEW_ACCOUNT = "viewAccount";
	public final static String ACTION_ADMIN_VIEW = "adminView";
	public final static String ACTION_REDIRECT = "redirect";
	public final static String ACTION_ELEMENT = "element";
	public final static String ACTION_HEADER = "header";
	public final static String ACTION_LIST = "list";
	public final static String HOW_TO_REPORT = "howToReporting";
	public final static String ACTION_LIST_ASSOCAITED_EFORMS= "listAssociatedEforms";
	public final static String ACTION_FILTER = "filter";
	public final static String ACTION_FILTERPUBLISHED = "filterPublished";
	public final static String ACTION_ERROR = "error";
	public static final String ACTION_DISPLAY = "display";
	public static final String ACTION_ELEMENTS = "elements";
	public static final String ACTION_SEARCH = "search";
	public static final String ACTION_CURRENT = "current";
	public static final String ACTION_IMPORT_DATA_ELEMENT = "importDataElement";
	public static final String ACTION_ADMIN_IMPORT_DATA_ELEMENT = "adminImportDataElement";
	public static final String ACTION_EXPORT = "export";
	public static final String ACTION_REDIRECT_EDIT_MAP_ELEMENT = "redirectEditMapElement";
	public static final String ACTION_REDIRECT_EDIT_DATA_ELEMENT = "redirectEditDataElement";
	public static final String ACTION_REDIRECT_CREATE_MAP_ELEMENT = "redirectCreateMapElement";
	public static final String ACTION_REDIRECT_CREATE_DATA_ELEMENT = "redirectCreateDataElement";
	public static final String ACTION_VIEW_DETAILS = "viewDetails";
	public static final String ACTION_VIEW_TABLE_DETAILS = "viewTableDetails";
	public static final String ACTION_VIEW_BINARY_DETAILS = "viewBinaryDetails";
	public static final String ACTION_LIGHTBOX = "lightbox";
	public static final String ACTION_PASSWORD = "password";
	public static final String ACTION_CREATE_PRIVILEGES = "createPrivileges";
	public static final String ACTION_CHANGE_STATUS = "changeStatus";
	public static final String ACTION_TYPE_GRAPH = "typeGraph";
	public static final String ACTION_COMMON_GRAPH = "commonGraph";
	public static final String ACTION_LOAD = "load";
	public static final String ACTION_IFRAME = "iframe";
	public static final String ACTION_SUBDOMAIN = "subDomain";
	public static final String ACTION_UPDATE_INPUT = "updateInput";
	public static final String ACTION_SUBGROUP = "subgroup";
	public static final String ACTION_ADVANCED = "advanced";
	public static final String ACTION_ACCESS_RECORD = "accessRecord";
	public static final String ACTION_VIEW_PSEUDOGUID = "viewPseudoGuid";
	public static final String ACTION_JSON_RESPONSE = "jsonResponse";
	public static final String SUCCESS_ADD_KEY = "app.success.add";
	public static final String SUCCESS_EDIT_KEY = "app.success.edit";
	public static final String ERROR_FILEUPLOAD_NOTFOUND = "errors.fileupload.notfound";
	/******************** Account ***********************************/
	public static final String ONE = "1";
	public static final Long ANONYMOUS_ID = 1L;
	public static final String ACTION_SUBMIT_REQUEST = "submitRequest";
	public static final String ACTION_REQUEST_SUCCESS = "requestSuccess";
	public final static String ACTION_RECOVER = "recover";
	public final static String ACTION_PROMPT = "prompt";
	public final static String ACTION_CONFIRM = "confirm";
	public final static String ACTION_ADDKEYWORD = "addKeyword";
	public final static String ACTION_ADDLABEL = "addLabel";
	public final static String ACTION_CURRENT_USERS = "currentUsers";
	public static final String ACTION_EDITDETAILS = "editDetails";
	public static final String ACTION_EDITVALUERANGE = "editValueRange";
	public static final String ACTION_EDITSTANDARDDETAILS = "editStandardDetails";
	public static final String ACTION_EDITKEYWORDS = "editKeywords";
	public static final String ACTION_EDITATTACHMENTS = "editAttachments";
	public static final String ACTION_ERROR_REDIRECT = "errorRedirect";
	public final static String ACTION_ADDLIGHTBOX = "addLightbox";
	public final static String ACTION_GROUPLIGHTBOX = "groupLightbox";
	public final static String ACTION_VIEW_PRIVILEGES = "viewPrivileges";
	public final static String ACTION_REFRESH = "refresh";
	public final static String ACTION_HOMEPAGE = "homepage";
	public final static String ACTION_IN = "in";
	public final static String ACTION_OUT = "out";
	public static final String ACTION_DOWNLOAD = "download";
	public static final String ID = "id";
	public static final String USER_ID = "userId";
	public static final String USER = "user";
	public static final String ADMIN = "admin";
	public static final String USERNAME_AVAILABLE = "Username is available";
	public static final String ILLEGAL_USERNAME = "Username is illegal";
	public static final String USERNAME_NOT_AVAILABLE = "Username is not available";
	public static final String SOURCE = "source";
	public static final String REQUEST = "request";
	public static final String NAME_SPACE = "nameSpace";
	public static final String IS_REQUEST = "isRequest";
	public static final String ACTION_GRANT = "grant";
	public static final String ACTION_CLINICAL_TRIAL = "clinicalTrial";
	public static final String ACTION_DOCUMENTATION = "documentation";
	public static final String ACTION_EDIT_DOCUMENTATION = "editDocumentation";
	public static final String ACTION_EDIT_DATA = "editData";
	public static final String ACTION_DATASET = "dataset";
	public static final String ACTION_PERMISSIONS = "permissions";
	public static final String ACTION_SUPPORTING_DOC_TABLE = "supportingDocumentationTable";
	public static final String ACTION_CLINICAL_TRIAL_DETAILS = "clinicalTrialDetails";
	public static final String ACTION_DATASET_TABLE = "datasetTable";
	public static final String ACTION_INVALID = "invalid";
	public static final String ACTION_EXPIRATION_DATE = "expirationDate";
	public static final String ACTION_REACTIVATE_LIGHTBOX = "reactivateLightbox";
	public static final String ACTION_PUBLICATION = "publication";
	public static final String ACTION_REVIEW = "review";
	public static final String ACTION_SUCCESS = "success";
	public static final String ACTION_INNER = "inner";
	public static final String ACTION_IMPORT = "import";
	public static final String ACTION_DSIMPORT = "dsImport";
	public static final String ACTION_IMPORT_FORM_STRUCTURE = "importFormStructure";
	public static final String ACTION_IMPORT_EFORM = "importEform";
	public static final String ACTION_IMPORT_DATA_ELEMENT_SCHEMA = "importDataElementSchema";
	public static final String ACTION_CLASSIFICATION = "classification";
	public static final String ACTION_DOMAIN = "domain";
	public static final String ACTION_SPONSOR_INFO_TABLE = "sponsorInfoTable";
	public static final String ACTION_EDIT_RESEARCH_MANAGEMENT = "editResearchMgmt";
	public static final String ACTION_EDIT_STUDY_SITE = "editStudySite";

	public static final String ACTION_SUPPORTING_DOCUMENTATION_TABLE = "documentationTable";
	public static final String ACTION_META_STUDY_DATA_TABLE = "metaStudyDataTable";
	public static final String ACTION_META_STUDY_EDIT = "editMetaStudy";
	public static final String ACTION_ADD_DOCUMENTATION_DIALOG = "addDocumentationDialog";
	public static final String ACTION_SELECT_SAVED_QUERY_DIALOG = "selectSavedQueryDialog";
	public static final String ACTION_ADD_SAVED_QUERY_DIALOG = "addSavedQueryDialog";
	public static final String ACTION_ADD_DATA_FILE_DIALOG = "addDataFileDialog";
	public static final String META_STUDY_KEYWORD = "metastudyKeyword";
	public static final String ACTION_REDIRECT_TO_DOCUMENTATION_REFRESH = "editDocumentDataTableSave";
	public static final String ACTION_REDIRECT_TO_DATA = "editDataDataTableSave";
	public static final String ACTION_REDIRECT_TO_EDIT_DATA = "redirectToEditData";
	public static final String ACTION_REDIRECT_TO_VIEW = "redirectToView";
	public static final String ACTION_DOCUMENTATION_REFRESH = "documentationRefresh";


	/******************** GUID ***********************************/

	public static final String ACTION_LANDING = "landing";
	public static final String ACTION_LAUNCH = "launch";

	/******************** Value Range *******************************/

	public final static String VALUERANGE = "valueRange";
	public final static String VALUERANGE_DESCRIPTION = "description";
	public final static String VALUERANGE_NORMAL = "normal";
	public final static String VALUERANGE_ACCEPTABLEVALUES = "acceptableValues";

	/******************** Keyword Interface **************************/

	public final static String KEYWORD = "keyword";

	/******************** Password Recovery **************************/

	public final static String TOKEN = "token";
	public final static String CAS_TOKEN = "casToken";

	/******************** Form Type **********************************/

	public final static String FORMTYPE_EDIT = "edit";
	public final static String FORMTYPE_CREATE = "create";

	/******************** Commons *************************************/

	public final static String SEARCH_WILDCARD = "%";
	public final static String COMMA = ",";
	public static final Object EMPTY_STRING = "";

	/******************** Documentation *******************************/

	public static final String DOCUMENTATION_FILE = "file";
	public static final String DOCUMENTATION_URL = "url";
	public static final String DOCUMENTATION_NONE = "none";

	/******************** File Action Names *******************************/

	public static final String DATA_ELEMENT_FILE_ACTION = "dataElementFileAction";
	public static final String DATA_STRUCTURE_FILE_ACTION = "dataStructureFileAction";

	public static final String FILE_ID_PARAM = "fileId";
	public static final String FILE_UPLOAD_TYPE_PARAM = "file_upload_type";
	public static final String USER_FILE_ID_PARAM = "user_file_id";
	public static final String URL_PARAM = "new_url";

	public static final String DOC_TYPE_URL = "URL Document";
	public static final String DOC_TYPE_NEW_FILE = "New File Document";
	public static final String DOC_TYPE_OLD_FILE = "Reuse Existing";
	public static final String DOC_TYPE_NONE = "None";

	public static final int FILE_UPLOAD_MAX = 5242879;

	/******************** Current Object Names *******************************/

	public static final String CURRENT_DATA_ELEMENT_NAME = "currentDataElement";
	public static final String CURRENT_DATA_STRUCTURE_NAME = "currentDataStructure";
	public static final String ACCOUNT_ID = "accountId";
	public static final String FILE_ID = "fileId";

	/******************** Repository *****************************************/

	public static final String DATASTORE_ID = "dataStoreId";
	public static final String PRIVATE = "Private";
	public static final String PUBLIC = "Public";
	public static final String REPOSITORY = "repository";
	public static final String STATUS_CHANGE = "statusChange";
	public static final String STATUS_CHANGE_LISTS = "statusChangeLists";
	public static final String DATASET_REQUEST_APPROVE = "Approve Request";
	public static final String DATASET_REQUEST_REJECT = "Reject Request";
	public static final int MAXIMUM_DOCUMENT_UPLOAD = 10;
	public static final String ACCESSRECORD_YES = "YES";
	public static final String ACCESSRECORD_NO ="NO";
	public static final String MANAGE_DATASET="manageDataset";
	
	
	/******************** Dictionary *****************************************/
	
	public static final String ACTION_STATUS_CHANGE = "statusChange";
	public static final String ATTACHED_DE_STATUS_CHANGE = "attachedDEStatusChange";
	public static final String ACTION_EVENT_LOG_TABLE = "eventLogTable";
	public static final String ACTION_SUPPORT_DOC_TABLE = "supportDocTable";
	public static final String LIST_ATTACHED_DES = "listAttachedDEs";
	public static final String ACTION_FS_EVENT_LOG_DOC="fsEventLogDocumentationAction";
	public static final String ACTION_FS_EVENT_LOG_VALIDATION="fsEventLogDocValidationAction";
	public static final String ACTION_DE_EVENT_LOG_DOC="dEEventLogDocumentationAction";
	public static final String ACTION_DE_EVENT_LOG_VALIDATION="dEEventLogDocValidationAction";
	public static final String ACTION_BULK_STATUS_CHANGE ="bulkStatusChange";
	public static final String LIST_BULK_DES = "listBulkDEs";

	/******************** Name Spaces ****************************************/

	public static final String NAMESPACE_ACCOUNTS = "accounts";
	public static final String NAMESPACE_PUBLICACCOUNTS = "publicAccounts";
	public static final String NAMESPACE_SSO = "sso";
	public static final String NAMESPACE_JSP_SSO = "jsp/sso";
	public static final String NAMESPACE_ADMIN = "admin";
	public static final String NAMESPACE_ACCOUNT_ADMIN = "accountAdmin";
	public static final String NAMESPACE_STUDYADMIN = "studyAdmin";
	public static final String NAMESPACE_STUDY = "study";
	public static final String NAMESPACE_DICTIONARYADMIN = "dictionaryAdmin";
	public static final String NAMESPACE_PUBLICDICTIONARY = "publicData";
	public static final String NAMESPACE_GUID_ADMIN = "guidAdmin";
	public static final String ADMIN_EDIT = "adminEdit";
	public static final String ADMIN_SUBMIT = "adminSubmit";
	public static final String ADMIN_SUBMIT_EDIT = "adminSubmitEdit";
	public static final String DATE_FORMAT = "MM/dd/yyyy";

	/******************** Field Names *****************************************/

	public static final String SESSION_CLINICAL_LIST = "sessionClinicalList";
	public static final String IS_CREATE = "isCreate";
	public static final String SESSION_GRANT_LIST = "sessionGrantList";
	public static final String STUDY_ID = "studyId";
	public static final String URL_DOCUMENTATION_RADIO = "urlDocumentationRadio";
	public static final String DATASET_ID = "datasetId";
	public static final String PREFIXED_ID = "prefixedId";
	public static final String VIEW = "view";
	public static final String NULL = "null";
	public static final String DICTIONARY_ADMIN = "dictionaryAdmin";
	// public static final String PORTAL_ROOT = "portalRoot";
	public static final String PERMISSION_GROUP_ID = "permissionGroupId";
	public static final String QUOTE = "\"";
	public static final String ACCESS_DENIED = null;
	public static final String SELECT = "select";
	public static final String DATE = "date";
	public static final String TEXT = "text";
	public static final String FILE = "file";
	public static final String DATA_ELEMENT_NAME = "dataElementName";
	public static final String DATA_ELEMENT_STATUS_CHANGE= "statusChange";
	public static final String PUBLIC_AREA = "publicArea";
	public static final String QUERY_AREA = "queryArea";
	public static final String ORDER_ERROR_KEY = "errors.orderManager";

	public static final String DE_DISEASE_SPLIT_EXPRESSION = "\\.";

	public static final String FORMAT = "format";
	public static final String REDCAP = "redcap";

	public static final String META_STUDY_ID = "metaStudyId";
	public static final String META_STUDY_EDIT_BOOLEAN = "isMetaStudyEdit";
	public static final String SUPPORTING_DOC_NAME = "supportingDocName";
	public static final String META_STUDY_DATA_NAME = "metaStudyDataName";
	public static final String COPY_OF = "Copy Of ";
	public static final int ELLIPSIS_CHARACTER_COUNT = 100;
	public static final String DATA_ELEMENT_FILE_VALIDATION_ACTION = "dataElementFileValidationAction";
	public static final String FORM_STRUCTURE_FILE_VALIDATON_ACTION = "dataStructureFileValidationAction";
	public static final String META_STUDY_DATA_VALIDATION_ACTION = "metaStudyDataValidationAction";

	// eForm specific constants
	public static final String EFROM_NAME = "name";
	public static final String EFROM_SHORT_NAME_TEMP = " ";
	public static final String EFROM_DESCRIPTION = "description";
	public static final String EFROM_QUESTIONID = "questionId";
	public static final String EFROM_SECTIONID = "sectionId";
	public static final String EFROM_SECTION_PATTERN = "S_";
	public static final String EFORM_QUESTION_PATTERN ="_Q_";
	public static final String EFROM_ROW = "row";
	public static final String EFROM_COLUMN = "col";
	public static final String EFROM_MINUS_ONE = "-1"; 
	public static final String FAILURE = "failure";
	//Support image format for graphcis in eForm
	public static final String GIF= "gif";
	public static final String JPG= "jpg";
	public static final String JPEG= "jpeg";
	public static final String PNG= "png";
	public static final String  UNSUPPORTED_FILE_EXTENSION = "unsupported_file_extension";
	
	public static final String ACCOUNT_ESIGNATURE_DUC_CUTOFFDATE = "2015-11-14";
	
	/******************** DOI *****************************************/
	public static final String DOI_RESOLVER_BASE_URL = "https://doi.org/";

	//PubMed WS
	public static final String pubmedWsUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=";
	public static final String FILETYPE_PUBLICATION = "Publication";
	public static final String FILETYPE_SOFTWARE = "Software";
	public static final String BRICS_SYSTEM_GENERATED = "BRICS System Generated";
	
	// Grant search
	public static final String FEDERAL_REPORTER_SEARCH_URL = "https://federalreporter.nih.gov/projects/search";
}
