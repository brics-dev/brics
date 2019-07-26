package gov.nih.nichd.ctdb.security.domain;


/**
 * SystemRole DomainObject for the NICHD CTDB Application. The SystemRole
 * object is a sub-class of Role and sets default system roles for the system.
 * System Roles are assigned to user's for the purpose of system administration.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SystemRole extends Role
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5526909272986130603L;

	/**
     * Default Constructor for the SystemRole Domain Object
     */
    public SystemRole()
    {
        // default constructor
    }
}