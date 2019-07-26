package gov.nih.nichd.ctdb.response.common;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will hold all constant values to be used
 * by the Response Sub Application.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ResponseConstants
{

    /**
     *  Response Constant used as the key/id for the Form object in session
     */
    public static final String FORM_SESSION_KEY = "currentform";

    /**
     *  Response Constant used as the key/id for the AdministeredForm object in request
     */
    public static final String AFORM_SESSION_KEY = "administeredform";

    /**
     *  Response Constant used as the key/id for the AdministeredForm object
     *  that represents the current locked form in the Response Data module
     */
    public static final String LOCKEDFORM_SESSION_KEY = "currentlockedform";

    /**
     *  Response Constant used as the key/id for the dataentry header info
     */
    public static final String DATAENTRYHEADER_SESSION_KEY = "dataentryheader";
    
    /**
     *  Response Constant used as the key/id for the dataentry header info
     */
    public static final String DATAENTRYHEADER_SESSION_KEY2 = "dataentryheader2";

    /**
     *  Response Constant used as the key/id for the printed form header info
     */
    public static final String FORMPRINTHEADER_REQUEST_KEY = "formprintheader";

    /**
     *  Response Constant used as the key/id for the AdministeredForms in progress in request
     */
    public static final String AFORMSINPROGRESS_REQUEST_KEY = "aformsinprogress";

    /**
     *  Response Constant used as the key/id for the response home page table data with certified (initial lock, final certified, or final locked forms)
     */
    public static final String CFORMS_REQUEST_KEY = "cforms";

    /**
     *  Response Constant used as the key/id for the data trnafer error messages.
     */
    public static final String DATA_TRANSFER_ERROR = "response.error.datatransfer";

    /**
     *  Response Constant used as the key/id for the data trnafer success messages.
     */
    public static final String DATA_TRANSFER_SUCCESS = "response.success.datatransfer";

    /**
     *  Response Constant used as a Struts forward key word refering to the home page for the module.
     */
    public static final String RESPONSE_HOME = "home";

    /**
     *  Response Constant used as a Struts key/id for the list of administered form objects.
     */
    public static final String LIST_ADMINISTEREDFORM = "aformList";
    
    /**
     *  Response Constant used as a Struts key/id for the list of non subject administered form objects.
     */
    public static final String LIST_ADMINISTEREDFORM_NP = "npAdminFormsList";

    /**
     *  Response Constant used as a Struts key/id for the list of protocol forms
     */
    public static final String RESPONSE_PROTOCOL_FORMS_REQUEST_KEY = "protocolforms";

    /**
     *  Response Constant used as a Struts key/id for the list of intervals
     */
    public static final String RESPONSE_PROTOCOL_INTERVALS_REQUEST_KEY = "protocolintervals";

    /**
     *  Response Constant used as a Struts key/id for the list of events
     */
    public static final String RESPONSE_EVENTS_REQUEST_KEY = "events";

    /**
     *  Response Constant used as a Struts key/id for the list of errors generated during
     *  data file upload.
     */
    public static final String ERROR_DETAILS = "errordetails";

    /**
     * Struts Constant used as a Struts key/id for resolve discrepancy message.
     */
    public static final String SUCCESS_RESOLVE_KEY = "app.success.resolve";

    /**
     * Struts Constant to define action type
     */
    public static final String ACTION_PROCESS_CERTIFY = "process_certify";

    /**
     * Struts Constant to define action forward type
     */
    public static final String CERTIFY = "certify";

    /**
     * Struts Constant used for ActionError if the action failed
     * because the user does not check the check box
     */
    public static final String ERROR_CHECKBOX_REQUIRED = "errors.response.checkbox.required";
    
    /**
     * String constant for calculated questions.
     */
    public static final String NOCALCULATION = "Calculation condition not met.";

    /**
     * String constant for calculated questions.
     */
    public static final String CHANGECALCULATION = "Calculation condition changed.";

    public static final String ORIG_AFORM_SESSION_KEY = "OriginalAdministeredForm";

    public static final String ALLOW_EDIT = "allowEdit";

    public static final String AFORM_ATTACHMENTS = "AformAttachments";

    public static final String ON_MOBILE_DEVICE = "onMobileDevice";

    public static final Set<String> allowableImageTypes = ConcurrentHashMap.newKeySet();
    static {
        allowableImageTypes.add(".gif");
        allowableImageTypes.add(".jpg");
        allowableImageTypes.add(".jpeg");
        allowableImageTypes.add(".png");
    }
}
