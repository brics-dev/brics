package gov.nih.nichd.ctdb.form.common;

/**
 * This class will hold all constant values to be used
 * by the Form module.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class FormConstants
{

    /**
     * CTDB Form Constant used as the key to the question type list
     */
    public static final String QUESTIONTYPES = "questiontypes";

    /**
     * CTDB Form Constant used as the key to the group list
     */
    public static final String GROUPLIST = "grouplist";

    /**
     * CTDB Form Constant used as the key to the question list
     */
    public static final String QUESTIONLIST = "questionlist";

    /**
     * CTDB Form Constant used as the key to section id
     */
    public static final String SECTIONID = "sectionid";

    /**
     * CTDB Form Constant used as the key to section name
     */
    public static final String SECTIONNAME = "sectionname";

    /**
     * CTDB Form Constant used as the key to section list
     */
    public static final String SECTIONLIST = "sectionlist";

    /**
     * CTDB Form Constant used as the key to form id
     */
    public static final String FORMID = "formid";


    /**
     * CTDB Form Constant used as the key to retrieve form name
     */
    public static final String FORMNAME = "formname";
    
    /**
     * CTDB Form Constant used as the key to retrieve form status
     */
    public static final String FORMSTATUS = "formstatus";
    public static final String FORMTYPES = "formtype";

    /**
     * Struts Constant used for Action Forwarding if the action is to view a form detail
     */
    public static final String DETAIL = "detail";

    /**
     * Struts Constant used for Action Forwarding if the action is to view a form detail
     */
    public static final String FORMDETAIL = "formdetail";

    /**
     * CTDB Form Constant used as the key to retrieve the form status value in add/edit
     * form page
     */
    public static final String XFORMSTATUS = "xformstatus";

    /**
     * CTDB Form Constant used as the key for the status values displayed in the form search form.
     */
    public static final String FORMSEARCHSTATUS = "formsearchstatus";

    /**
     * CTDB Form Constant used as the key to retrieve the form list from the session or
     * request object
     */
    public static final String PROTOCOLFORMS = "protocolforms";
    public static final String PROTOCOLEFORMS = "protocolEforms";
    public static final String PUBLISHED_EFORM_MAP = "_published-eForm-map";

    /**
     * CTDB Form Constant used as the key to retrieve the public form list from the session or
     * request object
     */
    public static final String PUBLICFORMS = "publicforms";

    /**
     * CTDB Form Constant used as the key to retrieve the active form list from the session or
     * request object
     */
    public static final String ACTIVEFORMS = "activeforms";

    /**
     * CTDB Form Constant used as the key to retrieve the active form list from the session or
     * request object
     */
    public static final String FORMHOME = "formhome";
    
    
    /**
     * CTDB Form Constant used as the key to retrieve the active form list from the session or
     * request object
     */
    public static final String FORMINFOSAVED = "forminfoSaved";

    /**
     * CTDB Form Constant used as the key to retrieve the after question delete flag from the
     * request object
     */
    public static final String AFTERQUESTIONDELETE = "afterquestiondelete";

    /**
     * Form Constant used for ActionError if the action failed in creating an object
     * because the object already exists in the system
     */
    public static final String ERROR_NOTFOUND = "errors.notfound";

    /**
     * Form Constant used for ActionError if the skip rule question validation for
     * dependency fails
     */
    public static final String ERROR_SKIPRULE_DEPENDENCY = "errors.form.question.skiprule.dependency";

    /**
     * Form Constant used for ActionError if the skip rule question validation for
     * dependency fails when deleting a question
     */
    public static final String ERROR_SKIPRULE_DEPENDENCY_DELETE = "errors.form.question.skiprule.dependency.delete";

    /**
     * Form Constant used for ActionError if the calculated question validation for
     * dependency fails when deleting a section
     */
    public static final String ERROR_SECTION_SKIPRULE_DEPENDENCY_DELETE = "errors.form.question.section.skiprule.dependency.delete";
                                         

    public static final String ERROR_QUESTION_FORMVERSION_DELETE = "errors.form.question.delete.formversion";
    /**
     * Form Constant used for ActionError if the skip rule question validation for
     * dependency fails when deleting a row
     */
    public static final String ERROR_ROW_SKIPRULE_DEPENDENCY_DELETE = "errors.form.row.section.skiprule.dependency.delete";

    /**
     * Form Constant used for ActionError if the calculated question validation for
     * dependency fails
     */
    public static final String ERROR_CALCULATED_DEPENDENCY = "errors.form.question.calculated.dependency";

    /**
     * Form Constant used for ActionError if the calculated question validation for
     * dependency fails when deleting a question
     */
    public static final String ERROR_CALCULATED_DEPENDENCY_DELETE = "errors.form.question.calculated.dependency.delete";

    /**
     * Form Constant used for ActionError if the calculated question validation for
     * dependency fails when deleting a section
     */
    public static final String ERROR_SECTION_DEPENDENCY_DELETE = "errors.form.question.section.dependency.delete";

    /**
     * Form Constant used for ActionError if the action failed in creating an object
     * because the object already exists in the system
     */
    public static final String MORE_OBJECT_REQUIRED = "errors.needmoreobjects";

    /**
     * Form Constant used for ActionMessages if the action succeeded in search for database objects
     */
    public static final String SUCCESS_SEARCH_KEY = "app.success.search";

    /**
     * Form Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to save the item in the database.
     */
    public static final String ACTION_PROCESS_SAVE = "process_save";

    /**
     * Form Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to save an item as a new item.
     */
    public static final String ACTION_PROCESS_SAVEAS = "process_saveas";

    /**
     * Form Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to view the form details.
     */
    public static final String ACTION_SAVEAS_FORM = "save_as";
    
    /**
     * Form Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to process the form and add the item to the database.
     */
    public static final String ACTION_PROCESS_ADD_FORMINFO = "process_add_forminfo";

    /**
     * Form Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to copy the form.
     */
    public static final String ACTION_COPY_FORM = "copy_form";

    /**
     * Form Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to copy a form to a new form.
     */
    public static final String ACTION_PROCESS_COPY = "process_copy";

    /**
     * Form Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to save an item as a new item.
     */
    public static final String ACTION_VIEW_FORM = "view_form";

    /**
     * Form Constant used for form action lookup to determine processing.
     * This action lookup tells the Action class to export a form.
     */
    public static final String ACTION_PROCESS_EXPORT = "process_export";

    public static final String ACTION_SUCCESS_DELETE = "successdelete";
    public static final String ACTION_SUCCESS_EDIT = "success_edit";


    /**
     * Struts Constant used for ActionError if the action failed in editing an object
     * because it's not the same person importing the form
     */
    public static final String ERROR_FORMFILEUPLOAD_SAMEUSER = "errors.formfileupload.sameuser";

    /**
     * Struts Constant used for ActionError if the action failed in editing an object
     * because the file upload is unsuccessful
     */
    public static final String ERROR_FORMFILEUPLOAD = "errors.formfileupload";


    public static final String MAX_SECTIONS_IN_ROW_MESSAGE
            = "the maximum number of columns in this row has been reached";

    public static final String CURRENT_SECTION = "current_section";

    public static final String CURRENT_FORM = "current_form";

    public static final String RANGE_VALIDATION_JS_OBJS = "jsObjs";
    
    public static final String FORM_SECTION_PATTERN = "S_";
    
    public static final String FORM_QUESTION_PATTERN = "_Q_";

    public static int STATUS_INACTIVE = 1;

    public static int STATUS_CHECKEDOUT = 2;

    public static int STATUS_ACTIVE = 3;

    public static int STATUS_INPROGRESS = 4;

    public static int STATUS_EXTERNAL = 5;

    public static int ACCESS_PRIVATE = 0;

    public static int ACCESS_PUBLIC = 1;
}
