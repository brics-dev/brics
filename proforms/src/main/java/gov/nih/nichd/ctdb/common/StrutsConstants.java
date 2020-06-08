package gov.nih.nichd.ctdb.common;

/**
 * This class will hold all constant values to be used
 * by the Struts Framework.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class StrutsConstants {

    /*****************************************
     * Action Forwarding CONSTANTS
     *
     * Constants to be used by Struts for
     * forwarding to a view
     *****************************************/
	
	public static final String FETCHFORM = "fetchForm";
	public static final String FETCHFORMPSR = "fetchFormPSR";
	public static final String FORMFETCHED = "formFetched";
	public static final String SAVE = "save";
	public static final String SAVEFORM = "saveForm";
	public static final String SAVEFORMPSR = "saveFormPSR";
	public static final String LOCKFORM = "lockForm";
	public static final String FORMPARAMS = "formParams";
	public static final String SYSTEMERROR = "SYSTEM ERROR";
	public static final String SAVEFORMFINALLOCKED = "saveFormFL";
	public static final String NEXTFORM = "nextForm";
	public static final String PREVIOUSFORM = "previousForm";
	public static final String JUMPTOFORM = "jumpToForm";
	public static final String LOCKANDEXIT = "lockAndExit";
	public static final String LOCKANDEXITCAT = "lockAndExitCat";
	public static final String VALIDATEMAINGROUP = "validateMainGroup";
	public static final String PROCESS_POPULATE_FORMNAMES_AJAX = "process_populateFormNamesAJAX";
	public static final String EDITFORM = "editForm";
	public static final String EDITFORMPSR = "editFormPSR";
	public static final String EDITUSER = "editUser";
	public static final String FORMNAME = "formName";
	public static final String FORMSTATUS = "formStatus";
	public static final String VISITDATE = "visitDate";
	public static final String INTERVAL = "interval";
	public static final String OTHER = "Other";
	public static final String FORMSININTERVAL = "formsInInterval";
	public static final String COMINGFROMAUTOSAVER = "comingFromAutoSaver";
	public static final String PATIENTGUID = "patientGuid";
	public static final String PATIENTDISPLAYLABEL ="patientDisplayLabel";
	public static final String MARKASCOMPLETESTATUSINACTION = "markAsCompletedStatusInAction";
	public static final String ATTACHFILES = "attachFiles";
	public static final String LOCKANDLOADNEXTFORM = "lockAndLoadNextForm";
	public static final String LOCKANDLOADPREVIOUSFORM = "lockAndLoadPreviousForm";
	public static final String FORMTYPESTRING="formTypeString";
	public static final String SUBJECTRECORDID ="patientRecordId";
	public static final String MEMO = "memo";
	public static final String DELETEONCANCELFLAG ="deleteOnCancelFlag";
	public static final String FORMNAMETOBESAVEDORLOCKED="formNameToBeSavedOrLocked";
	public static final String SUBJECT_VISIT_DATE = "pVisitDate";
	public static final String SUBJECT_INTERVAL_NAME ="pIntervName";
	//session variable to track which subject is collecting data
	public static final String SUBJECT_COLLECTING_DATA = "subjectCollecting";
	public static final String AUDITCOMMENTS = "auditComments";
	
	public static final String TOKEN = "token";
	
	public static final String HIDDENIDS = "hiddenSectionsQuestionsPVsElementIdsJSON";
	
    /**
     * Struts Constant used for Action Forwarding if the action failed
     */
    public static final String FAILURE = "failure";
    public static final String ATTACHMENTFAILURE = "attachmentFailure";

    /**
     * Struts Constant used for Action Forwarding if the action succeeded
     */
    public static final String SUCCESS = "success";
    
    /**
     * Struts Constant used for Action Forwarding if the action is canceled
     */
    public static final String CANCEL = "cancel";
    public static final String ACTION = "action";
    
    /**
     * Struts Constant used for Action Forwarding if the user doesn't select any protocol and click on data collection tab
     */
    public static final String SELECTPROTOCOL = "selectProtocol";

    /**
     * Struts Constant used for Action Forwarding if the action is to display a form
     */
    public static final String FORM = "form";
    public static final String CONTINUE = "continue";
    
    public static final String CREATEFORM = "createForm";
    public static final String CREATEEDITFORM = "createEditForm";

    public static final String HOME = "home";


    /**
     * Struts Constant used for Action Forwarding if the action is to display a form
     */
    public static final String NO_PROTOCOLS = "noprotocols";

    /**
     * Struts Constant used for Action Forwarding if the action threw an exception that must
     * be handled gracefully in the form of descriptive error messages to the user. This is
     * most commonly used by forms that have business logic errors, such as adding an object
     * that already exists in the system.
     */
    public static final String EXCEPTION = "exception";
    
    /**
     * Struts Constant used for Action Forwarding if the action threw an exception that must
     * be handled gracefully in the form of descriptive error messages to the user. This is
     * most commonly used by forms that have business logic errors, such as adding an object
     * that already exists in the system.
     */
    public static final String EXCEPTION2 = "exception2";

    /**
     * Struts Constant used for Action Forwarding if the action threw an exception that must
     * be handled gracefully in the form of descriptive error messages to the user. This is
     * used for exception after valide question delete.
     */
    public static final String EXCEPTIONQUESTIONDELETE = "exceptionquestiondelete";

    /**
     * Struts Constant used for Action Forwarding if the action is to lock data entry on a form
     */
    public static final String LOCK = "lock";
    
    /**
     * Struts Constant used for Action Forwarding if the action is to update the ordering of records in a table
     */
    public static final String UPDATE_ORDER = "updateOrder";
    
    /**
     * Struts constant used for Action methods that handles only Ajax request and need to send back html status codes 
     * for certain error conditions.
     */
    public static final String BAD_GATEWAY = "badGateway";
    
    /**
     * Struts constant used for Action methods that handles only Ajax request and need to send back HTML status codes
     * for certain error conditions.
     */
    public static final String BAD_REQUEST = "badRequest";
    
    /**
     * Struts constant used for Action methods that handles only Ajax requests and need to send back HTML status
     * codes for certain error conditions.
     */
    public static final String FORBIDDEN = "forbidden";

    /*****************************************
     * ActionMessages CONSTANTS
     *
     * Keys that map to ApplicationResources.properties
     * to display messages to the user.
     *****************************************/

    /**
     * Struts Constant used for ActionMessages if the action succeeded in adding an object
     */
    public static final String SUCCESS_ADD_KEY = "app.success.add";
    
    public static final String VALIDATE_REQUIRED_DE_WARNING = "app.validateRequiredDE.warning";

    public static final String SUCCESS_SWAP_KEY = "app.success.swap";
    
    
    public static final String CONTAINS_DEPRECATED_OR_RETIRED_DES = "app.containsDeprecatedOrRetiredDEs.warning";

    /**
     * Struts Constant used for ActionMessages if the action succeeded in adding an object
     */
    public static final String SUCCESS_ADD_MULTI_KEY = "app.success.add.multiple";

    /**
     * Struts Constant used for ActionMessages if the action succeeded in updating an object
     */
    public static final String SUCCESS_EDIT_KEY = "app.success.edit";
    

    /**
     * Struts Constant used for ActionMessages if the action succeeded in updating an object
     */
    public static final String SUCCESS_EDIT_MULTI_KEY = "app.success.edit.multiple";

    /**
     * Struts Constant used for ActionMessages if the action succeeded in deleting an object
     */
    public static final String SUCCESS_DELETE_KEY = "app.success.delete";
    
    /**
     * Struts Constant used for ActionMessages if the action succeeded in deleting multiple objects
     */
    public static final String SUCCESS_DELETE_MULTI_KEY = "app.success.delete.multiple";

    /**
     * Struts Constant used for ActionMessages if the action succeeded in deleting an object
     */
    public static final String SUCCESS_REORDER_KEY = "app.success.reorder";

    /**
     * Struts Constant used for ActionMessages if the action succeeded in uploading a file
     */
    public static final String SUCCESS_UPLOAD_KEY = "app.success.upload";

    /**
     * Struts Constant used for ActionMessages if the action succeeded in locking a form after data entry
     */
    public static final String SUCCESS_LOCK_KEY = "app.success.lock";
    
    /**
     * Struts Constant used for ActionMessages if the action succeeded in locking a form after data entry
     */
    public static final String SUCCESS_SAVE_KEY = "app.success.save";

    /**
     * Struts Constant used for ActionMessages if the action succeeded in reassign data entry
     */
    public static final String SUCCESS_REASSIGN_KEY = "app.success.reassign";

    /**
     * Struts Constant used for ActionMessages if the action succeeded in copying public form
     */
    public static final String SUCCESS_COPY_KEY = "app.success.copy";
	
    /**
     * Struts Constant used for ActionMessages if the action succeeded in search for database objects
     */
    public static final String SUCCESS_SEARCH_KEY = "app.success.search";
    
    

    public static final String GENERIC_MESSAGE = "message.generic";
    
    /*
     * -------------------------------
     * Error message definitions
     * -------------------------------
     */
    
    /**
     * Struts Constant used for ActionError if the action failed in adding/editing an object.
     * This key has no text and only excepts one String {0}. It is to be used to display
     * a user friendly error message to the screen.
     */
    public static final String ERROR_GENERIC = "errors.generic";
    
    /**
     * Struts constant used for when the action fails to load the page with nessiary data.
     */
    public static final String ERROR_PAGE_SETUP = "errors.page.setup";
    
    /**
     * Struts Constant used for ActionError if the action failed in creating an object
     * because the object exceeds a pre-defined maximum length.
     */
    public static final String ERROR_MAX_LENGTH = "errors.maxlength";
    
    /**
     * Struts Constant used for ActionError if the action failed in creating an object
     * because the one of the object's fields is required.
     */
    public static final String ERROR_FIELD_REQUIRED = "errors.required";
    
    /**
     * Struts Constant used for ActionError if the action failed in creating an object
     * because the one of the object's fields is invalid.
     */
    public static final String ERROR_FIELD_INVALID = "errors.invalid";
    
    /**
     * Struts Constant used for ActionError if the action failed in creating an object
     * because one of the object's fields contains invalid characters.
     */
    public static final String ERROR_CHARS_INVALID = "errors.invalid.chars";
    
    public static final String ERROR_CANNOTBE = "errors.cannotbe";
    
    /**
     * Struts Constant used for ActionError if the action failed in creating an object
     * because the object already exists in the system
     */
    public static final String ERROR_NOTFOUND = "errors.notfound";
    
    /**
     * Struts Constant used for ActionError if the action failed in creating an object
     * because the object already exists in the system
     */
    public static final String ERROR_DUPLICATE_EVENT = "errors.duplicate.eventkey";

    /**
     * Struts Constant used for ActionError if the action failed in creating an object
     * because the object already exists in the system
     */
    public static final String MORE_OBJECT_REQUIRED = "errors.needmoreobjects";

    /**
     * Struts Constant used for ActionError if the action failed in editing an object
     * because the object already exists in the system
     */
    public static final String ERROR_DUPLICATE = "errors.duplicate";
    
    /**
     * Struts constant used for action errors if the action failed in editing an object
     * because the version of the object already exists in its achive table.
     */
    public static final String ERROR_DUPLICATE_ARCHIVE = "errors.duplicate.archive";

    /**
     * Struts Constant used for ActionError if the action failed in editing an object, form,
     * because the object already exists in the system
     */
    public static final String ERROR_DUPLICATE_FORM = "errors.duplicate.form";
    
    public static final String ERROR_REQUIRED_DATA_ELEMENTS = "errors.required.data.elements";
    
    public static final String ERROR_REQUIRED_GUID_SUBJECT_ID = "errors.patientid.required";
    
    public static final String ERROR_REQUIRED_VISIT_TYPE= "errors.visittype.required";
    
    public static final String ERROR_REQUIRED_VISIT_DATE = "errors.patientvisitdate.required";

    public static final String ERROR_REQUIRED_CLLINICAL_POINT = "errors.patientvisit.clinicalPoint.required";

    public static final String ERROR_EXISTS_VISIT_DATE = "errors.visitdate.exists";

    /**
     * Struts Constant used for ActionError if the action failed in editing an object
     * because the object already exists in the system
     */
    public static final String ERROR_DUPLICATE_SECTION_QUESTION = "errors.duplicate.section.question";

    /**
     * Struts Constant used for ActionError if the action failed in editing an object
     * because the upload file name is of the wrong file type
     */
    public static final String ERROR_FILEUPLOAD_NAME = "errors.fileupload.name";

    /**
     * Struts Constant used for ActionError if the action failed in editing an object
     * because the file upload is unsuccessful
     */
    public static final String ERROR_FILEUPLOAD_NOTFOUND = "errors.fileupload.notfound";
    
    /**
     * Struts Constant used for ActionError if the action failed because the file size limit has been reached.
     */
    public static final String ERROR_FILEUPLOAD_MAX_SIZE = "errors.fileUpload.exceededLimit";

    /**
     * Struts Constant used for ActionError if the action failed in add/editing an object
     * because the enough number of users have started/completed data entry
     */
    public static final String ERROR_DATAENTRY_PATIENT = "errors.patient.dataentry";

    /**
     * Struts Constant used for ActionError if the action failed in add/editing an patient
     * because the patient has active data entry and someone is trying to inactivate them
     */
    public static final String ERROR_DATAENTRY_PATIENT_INACTIVATE = "errors.patient.dataentry.inactivate";

    /**
     * Struts Constant used for ActionError if the action failed in add/editing an object
     * because the enough number of users have started/completed data entry
     */
    public static final String ERROR_RESPONSE_ALREADYSTARTED = "errors.response.alreadystarted";
    
    public static final String ERROR_RESPONSE_NOTUSER = "errors.response.notuser";
    
    public static final String ERROR_RESPONSE_LOCKED = "errors.response.locked";

    public static final String ERROR_RESOURCE_NOT_AVAILABLE = "errors.resource.notavailable";

    public static final String ERROR_VISITDATE_MISMATCH = "errors.visitdate.mismatch";
    public static final String ERROR_ANSWER_REQUIRED_IN_SAVE ="errors.response.answer.required.save";
    public static final String ERROR_ANSWER_REQUIRED_IN_LOCK ="errors.response.answer.required.lock";
    
    public static final String ERROR_RESPONSE_LOADINGFORM = "errors.response.loadingform";
    public static final String ERROR_RESPONSE_LOADINGFORM_PERMISSION = "errors.response.loadingform.permission";

    /**
     * Struts Constant used for ActionError if the action failed in data entry lock
     * because the user does not check the lock box
     */
    public static final String ERROR_LOCK_REQUIRED = "errors.response.lock.required";

    /**
     * Struts contant used for aciton errrors if the user enters data outside
     * the normal range of values and does not chekc the acknowledge box
     */
    public static final String ERROR_ACCEPT_RANGE_REQUIRED = "errors.response.acceptrange.required";

    /**
     * Struts Constant used for ActionError with the DataEntrySetupAction
     * when there are no intervals associated with the protocol
     */
    public static final String ERROR_INTERVALS_EMPTY = "errors.response.intervalsempty";

    /**
     * Struts Constant used for ActionError with the DataEntrySetupAction
     * when there are no patients associated with the protocol
     */
    public static final String ERROR_PATIENTS_EMPTY = "errors.response.patientsempty";

    /**
     * Struts Constant used for ActionError with the EditAssignmentAction
     * when reassign the user who has started another data entry.
     */
    public static final String ERROR_DUPLICATE_DATAENTRY_USER = "errors.duplicate.dataentry.user";

    public static final String ERROR_ADD_SECTION = "errors.form.addsection";

    public static final String ERROR_FUTURE_VISITDATE = "errors.future.visitdate";

    public static final String ERROR_VISITDATE_REQUIRED = "errors.visitdate.required";
    
    public static final String ERROR_UNDELETABLE_REGULATIONS = "errors.ebinder.undeletableRegulations";
    
    public static final String ERROR_UNCHANGED_REGULATIONS = "errors.regulations.nochanges";
    
    public static final String ERROR_UNDELETABLE_SAMPLE_TYPES = "errors.sampletype.delete.failure";
    
    public static final String ERROR_SITELINK_DUPLICATE_URL = "errors.siteLink.url.duplicate";
    
    /**
     * Struts constant used for action errors if a deletion fails.
     */
    public static final String ERROR_DELETE = "errors.delete";
    
    
    public static final String ERROR_DELETE_QUESTIONATTACHEDONOTHERFORMS = "errors.delete.questionsAttachedOnOtherForms";
    
    public static final String ERROR_EDIT_LEGACY = "errors.edit.legacy";
    public static final String ERROR_EXPORT_LEGACY = "errors.export.legacy";
    public static final String ERROR_IMPORT_LEGACY = "errors.import.legacy";
    public static final String ERROR_SAVEAS_LEGACY = "errors.saveas.legacy";
    public static final String ERROR_COPY_LEGACY = "errors.copy.legacy";
    
    /**
     * Struts constant used for action errors when a change to the database fails and the failure can be
     * overcome by re-trying the change
     */
    public static final String ERROR_DATABASE_SAVE_RETRY = "errors.save.database.retry";
    
    /**
     * Struts constant used for action errors when a change to the database fails and it cannot be
     * fixed without some action by the system administrator.
     */
    public static final String ERROR_DATABASE_SAVE = "errors.save.database";
    
    /**
     * Struts constant used for action errors when a change to some data fails and the failure can be
     * overcome by re-trying the action
     */
    public static final String ERROR_DATA_SAVE_RETRY = "errors.save.data.retry";
    
    /**
     * Struts constant used for action errors when a change to some data fails and it cannot be
     * fixed without some action by the system administrator.
     */
    public static final String ERROR_DATA_SAVE = "errors.save.data";
    
    /**
     * Struts constant used for action errors when getting or generating some data from the system. 
     */
    public static final String ERROR_DATA_GET = "errors.get.data";
    
    /**
     * Struts constant used for action errors when retrieving data from the database fails and it
     * cannot be fixed with some action by the system administrator.
     */
    public static final String ERROR_DATABASE_GET = "errors.get.database";
    /**
     * Struts constant used for action errors when retrieving list of publised FS
     */
    public static final String ERROR_WS_FSLIST="errors.ws.visitType";
    
    public static final String ERROR_NOT_UNIQUE_SHORTNAME="errors.shortname.nounique";
    
    public static final String ERROR_SAVING_VISI_TYPE= "erors.save.visitType";
    
    public static final String ERROR_NOT_UNIQUE_VISITTYPE= "erors.visittype.nounique";
    
    /**
     * Struts constant used for action errors when trying to delete a visit type that is already
     * associated with a data collection.
     */
    public static final String ERROR_VISIT_TYPE_DATA_COLL_DELETE = "errors.delete.visitType.linkedWithDataColl";
    
    /**
     * Struts constant used for action errors when trying to delete multiple visit types that are already
     * associated with a data collection.
     */
    public static final String ERROR_VISIT_TYPE_DATA_COLL_DELETE_MULTI = "errors.delete.visitType.linkedWithDataColl.multiple";
    
    /**
     * Struts constant used for action errors when a list of BRICS studies can't be retrieved from the repository web service.
     */
    public static final String ERROR_BRICS_STUDY_LIST_GET = "errors.webservice.repository.study.list";
    
    /**
     * Struts constant used for action errors when a web service call fails.
     */
    public static final String ERROR_WEB_SERVICE_GET = "errors.webservice.get";
    
    /**
     * Struts constant used for action errors when a database error occurs for a report.
     */
    public static final String ERROR_REPORTS_DATABASE_GENERIC = "errors.reports.database.generic";
    
    /**
     * Struts constant used for action errors when a database error occurs while setting up an add or edit page.
     */
    public static final String ERROR_ADD_EDIT_PAGE_SETUP = "errors.addEdit.setup";
    
    /*****************************************
     * Action Lookup CONSTANTS
     *
     * Keys that are used to determine what
     * processing action an Action class should
     * perform. The action is set in the
     * corresponding ActionForm class for the Action
     *****************************************/

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to prepare the form for adding a new item.
     */
    public static final String ACTION_ADD_FORM = "add_form";
    
    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to prepare the form for deleting a item.
     */
    public static final String ACTION_DELETE_FORM = "delete_form";
    
    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to process the view of the form.
     */
    public static final String ACTION_VIEW_FORM = "view_form";
    
    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to prepare the form for adding a new item.
     */
    public static final String ACTION_CREATE_FORM = "create_form";
    
    public static final String ACTION_PROCESS_EDIT_FORMINFO = "process_edit_forminfo";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to prepare the form for editing an item.
     */
    public static final String ACTION_EDIT_FORM = "edit_form";
    public static final String ACTION_SPLIT = "split";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to process the form and add the item to the database.
     */
    public static final String ACTION_PROCESS_ADD = "process_add";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to process the form and update the item in the database.
     */
    public static final String ACTION_PROCESS_EDIT = "process_edit";
    
    /**
     * Struts constant used for form action lookup to tell the Action class to process a download request.
     */
    public static final String ACTION_PROCESS_DOWNLOAD = "process_download";
    
    /**
     * Struts constant used for form action lookup to tell the Action class to process an upload request.
     */
    public static final String ACTION_PROCESS_UPLOAD = "process_upload";
    
    /**
     * Struts Constant used for edit modes in certain JSPs.
     */
    public static final String ACTION_REDO_ADD = "redo_add_form";
    
    /**
     * Struts Constant used for form action lookup to determine the navigation.
     * This action lookup tells the Action class to process the form and add the item to the database and navigate to appropriate page.
     */
    public static final String ACTION_SAVE_NAVIGATE_HOME = "save_navigate_home";
    public static final String ACTION_SAVE_NAVIGATE_NEXT = "save_navigate_next";
    
    
    
    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to process the form and add the item to the database.
     */
    public static final String ACTION_PROCESS_SITE = "process_site";    

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to process the form and add the item to the database.
     */
    public static final String ACTION_PROCESS_DRUG_DEVICE = "process_drug_device";    
    

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to process the form and add the item to the database.
     */
    public static final String ACTION_PROCESS_INTERVAL = "process_interval";    


    public static final String ACTION_PROCESS_SPLIT = "process_split";
    

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to delete the item in the database.
     */
    public static final String ACTION_PROCESS_DELETE = "process_delete";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to save the item in the database.
     */
    public static final String ACTION_PROCESS_SAVE = "process_save";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to save an item as a new item.
     */
    public static final String ACTION_PROCESS_SAVEAS = "process_saveas";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to cancel the action.
     */
    public static final String ACTION_PROCESS_CANCEL = "process_cancel";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to start a process(several actions).
     */
    public static final String ACTION_PROCESS_START = "process_start";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to import a form.
     */
    public static final String ACTION_PROCESS_IMPORT = "process_import";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to export a form.
     */
    public static final String ACTION_PROCESS_EXPORT = "process_export";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to audit the change history.
     */
    public static final String ACTION_PROCESS_AUDIT = "process_audit";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to search items.
     */
    public static final String ACTION_PROCESS_SEARCH = "process_search";
    
    /**
     * Struts Constant used for from action lookup to determine processing.
     * This action lookup tells the Action class to process the adding of a site
     */
    public static final String ACTION_PROCESS_SITE_ADD = "process_site_add";
    
    /**
     * Struts Constant used for from action lookup to determine processing.
     * This action lookup tells the Action class to process the editing of a site
     */
    public static final String ACTION_PROCESS_SITE_EDIT = "process_site_edit";
    
    /**
     * Struts Constant used for from action lookup to determine processing.
     * This action lookup tells the Action class to process the adding of a device
     */
    public static final String ACTION_PROCESS_DEVICE_ADD = "process_device_add";
    
    /**
     * Struts Constant used for from action lookup to determine processing.
     * This action lookup tells the Action class to process the editing of a device
     */
    public static final String ACTION_PROCESS_DEVICE_EDIT = "process_device_edit";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to prepare the form for uploading question image.
     */
    public static final String ACTION_UPLOAD_QUESTIONIMAGE = "upload_questionimage";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to upload question image.
     */
    public static final String ACTION_PROCESS_UPLOAD_QUESTIONIMAGE = "process_upload_questionimage";

    /**
     * Struts Constant used for undo export action.
     * This action lookup tells the Action class to mark the form from checked out to inactive.
     */
    public static final String ACTION_UNDO_EXPORT = "undo_export";

    /**
     * Struts Constant used for view completed form action.
     * This action lookup tells the Action class to prepare a form with answers for viewing.
     */
    public static final String ACTION_VIEW_DRAFT_FORM = "view_draft_form";

    /**
     * Struts Constant used to view completed and discrepancy resolved form action.
     * This action lookup tells the Action class to prepare a form with answers for viewing.
     */
    public static final String ACTION_VIEW_RESOLVED_FORM = "view_resolved_form";

    /**
     * Struts Constant used for view completed form action.
     * This action lookup tells the Action class to prepare a form with answers for viewing.
     */
    public static final String ACTION_VIEW_DRAFT_FORM_WITHHEADER = "view_draft_form_withheader";

    /**
     * Struts Constant used for view completed form action.
     * This action lookup tells the Action class to prepare a form with answers for viewing.
     */
    public static final String ACTION_VIEW_CERTIFIED_FORM = "view_certified_form";

    /**
     * Struts Constant used for view completed form action.
     * This action lookup tells the Action class to prepare a form with answers for viewing.
     */
    public static final String ACTION_VIEW_CERTIFIED_FORM_WITHHEADER = "view_certified_form_withheader";

    /**
     * Struts Constant used for view completed form action.
     * This action lookup tells the Action class to prepare a form with answers for viewing.
     */
    public static final String ACTION_VIEW_FINAL_FORM = "view_final_form";

    /**
     * Struts Constant used for view completed form action.
     * This action lookup tells the Action class to prepare a form with answers for viewing.
     */
    public static final String ACTION_VIEW_FINAL_FORM_WITHHEADER = "view_final_form_withheader";

    /**
     * Struts Constant used for view print form action.
     * This action lookup tells the Action class to prepare a blank form for print.
     */
    public static final String ACTION_VIEW_PRINT_FORM = "view_print_form";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to lock (initial lock) the form after data entry is complete.
     */
    public static final String ACTION_PROCESS_LOCK = "process_lock";

    /**
     * Struts Constant used for form action lookup to determine processing.
     * Action for adding calculation rules to question
     */
    public static final String ACTION_CALCULATE = "calculations";
    public static final String ACTION_UPDATE_PARENT = "updateParent";
    
    /**
     * Struts Constant used for adding a binder section to an E-binder
     */
    public static final String ACTION_ADD_BINDER_SECTION = "add_bindersection";
    
    /**
     * Struts Constant used for deleting a binder section from an E-binder
     */
    public static final String ACTION_DELETE_BINDER_SECTION = "delete_section";
    
    /**
     * Struts Constant used for creating a new binder from the current protocol
     */
    public static final String ACTION_CREATE_DEFAULT_BINDER = "_aNb721";
    
    /** 
     * Struts Constant used for changing the order of records displayed on a table on a page.
     */
    public static final String ACTION_CHANGE_DISPLAY_ORDER = "change_display_order";
    
    /**
     * Struts Constant used for progressing the order of records that are displayed on a table on the page.
     */
    public static final String ACTION_PROCESS_CHANGE_ORDER = "action_process_order_change";
    
    public static final String ACTION_CLEAR_SESSION_VARIABLES = "action_clear_session_variables";
    
    public static final String ACTION_VIEW_RANDOMIZATION = "viewRandomization";

    /**
     * Struts Constant used for ActionMessages if the action succeeded in sending an email from CTDB application
     */
    public static final String SUCCESS_EMAIL_SEND = "email.success.send";

    public static final String IMPORTED_FORM_ANSWER_JS = "importFormAnswerJs";
    
    public static final String ERROR_STUDY_NUMBER_REQUIRED = "errors.study.number.required";
    public static final String ERROR_STUDY_NAME_REQUIRED = "errors.study.name.required";
    public static final String ERROR_STUDY_URL_REQUIRED = "errors.study.url.required";
    public static final String ERROR_STUDY_URL_INVALID = "errors.study.url";
    public static final String ERROR_URL_INVALID = "errors.url";
    
    public static final String ERROR_SUBJECTNUM_REQUIRED = "errors.protocol.subjectNumberStart";
    public static final String ERROR_SUBJECTNUM_INT = "errors.protocol.subjectNumberStartInt";
    
    public static final String ERROR_SITE_PI_REQUIRED = "errors.site.pi.required";
    public static final String ERROR_SITE_NAME_REQUIRED = "errors.site.name.required";
    public static final String ERROR_SITE_NAME_DUPLICATE = "errors.site.name.duplicate";
    public static final String ERROR_SITE_STUDY_ID_DUPLICATE = "errors.site.studyid.duplicate";
    public static final String ERROR_FDA_IND_REQUIRED = "errors.fda.ind.required";
    public static final String ERROR_FDA_IND_DUPLICATE = "errors.fda.ind.duplicate";
    public static final String ERROR_SITE_ADDRESS = "errors.site.address";
    
    public static final String ERROR_INTERVAL_NAME_REQUIRED = "errors.interval.name.required";
    public static final String ERROR_INTERVAL_DESCRIPTION_REQUIRED = "errors.interval.description.required";
    public static final String ERROR_INTERVAL_NAME_DUPLICATE = "errors.interval.name.duplicate";
    public static final String ERROR_PROJECT_NAME_REQUIRED = "errors.project.name.required";
    
    public static final String ERROR_SAMPLE_SOURCE_NAME_REQUIRED = "errors.sampleSource.name.required";

    
    public static final String EXPAND_INTERVAL_SECTION = "intervalInfoExpanded";
    
    /**
     * Struts Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to process the form and update the item in the database.
     */
    public static final String ERROR_LEFTNAV_PROTOCOL_REQUIRED = "errors.leftNav.protocol.required";
    public static final String LN_CURRENTSTUDY_VIEWSTUDY = "leftNavViewCurrentStudy_ViewStudy";
    
    /** added by Ching Heng
     * Struts Constant used for special String parsing symbol.
     */
    public static final String alienSymbol="�";
    
    /**
     *added by yogi
     *There will be only three in navigation in data collection one to go to data collections left nav link another to go to my collections left nav link and final to stay in same page on exception or success
     *This is inspired by convention over configuration(make things simpler)
     */
	public static final String DATA_COLLECTION = "dataCollection";
	public static final String MY_COLLECTIONS = "myCollections";
	public static final String STAY_IN_SAME_PAGE = "stayInSamePage";
	
	public static final String DATA_COLLECTION_PSR = "dataCollectionPSR";
	
	
	public static final String DUPLICATE_AND_OTHER_EXCEPTIONS = "response.errors.duplicate.data";
	
	public static final Float CONDITIONAL_CODE_FOR_DO_NOT_CALCULATE = 555f;
	
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

	public static final String SUBJECT_EXISTING_WARNING = "subject.existing.warning";
}
