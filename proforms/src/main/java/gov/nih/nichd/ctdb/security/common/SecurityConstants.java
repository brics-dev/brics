package gov.nih.nichd.ctdb.security.common;

/**
 * This class will hold all constant values to be used
 * by the Security Sub Application.
 *
 * @version 1.0
 */
public class SecurityConstants
{

    /**
     *  Security Constant used as the key/id for the role object in request
     */
    public static final String ROLE_REQUEST_KEY = "role";

    /**
     *  Security Constant used as the key/id for the user object in request
     */
    public static final String USERS_REQUEST_KEY = "users";

    /**
     *  Security Constant used as the key/id for the privilege object in request
     */
    public static final String PRIVILEGE_SESSION_KEY = "privileges";

	public final static String PI_SCHEDULER_PRIV = "pischeduler";
	public final static String SCHEDULER_PRIV = "scheduler";
	public final static String ADD_EDIT_AUDITOR_COMMENTS_PRIV = "addeditauditorcomments";
	public final static String RESPOND_TO_AUDIT_COMMENTS_PRIV = "respondtoauditcomments";
	public final static String VIEW_PROTOCOL_RANDOMIZATION = "viewrandomization";
	public final static String VIEW_CONFIGURE_EFORMS_PRIV = "viewConfigureEform";
	public final static String EDIT_CONFIGURE_EFORMS_PRIV = "editConfigureEform";

	public final static String[] SCHEDULER_PRIV_ARR =
			{SecurityConstants.SCHEDULER_PRIV, SecurityConstants.PI_SCHEDULER_PRIV};
	public final static String[] AUDITOR_COMMENTS_PRIV_ARR =
			{SecurityConstants.ADD_EDIT_AUDITOR_COMMENTS_PRIV, SecurityConstants.RESPOND_TO_AUDIT_COMMENTS_PRIV};
	public final static String[] CONFIGURE_EFORMS_PRIV_ARR = 
		{SecurityConstants.VIEW_CONFIGURE_EFORMS_PRIV,SecurityConstants.EDIT_CONFIGURE_EFORMS_PRIV};
}
