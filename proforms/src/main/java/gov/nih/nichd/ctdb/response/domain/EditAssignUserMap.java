package gov.nih.nichd.ctdb.response.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.DomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * EditAssignUserMap DomainObject for the NICHD CTDB Application
 *
 * @author  Booz Allen Hamilton
 * @version 1.0
 */

public class EditAssignUserMap implements DomainObject
{
	private static final long serialVersionUID = -8092674691553517572L;
	
	private int userId = Integer.MIN_VALUE;
    private String userName;

    /**
     * Default Constructor for the EditAssignUserMap Domain Object
     */
    public EditAssignUserMap()
    {
        // default constructor
    }

    /**
     * Parametized Constructor for the EditAssignUserMap Domain Object
     */
    public EditAssignUserMap(int userId, String userName)
    {
        this.userId = userId;
		this.userName = userName;
    }

    /**
     * Gets the EditAssignUserMap user id
     *
     * @return user id
     */
    public int getId()
    {
        return userId;
    }

    /**
     * Sets the userId 
     *
     * @param userId 
     */
    public void setId(int userId)
    {
        this.userId = userId;
    }

    /**
     * Gets the user Name
     *
     * @return The user Name
     */
    public String getName()
    {
        return userName;
    }

    /**
     * Sets the user Name
     *
     * @param userName
     */
    public void setName(String userName)
    {
        this.userName = userName;
    }

    public Document toXML() throws TransformationException
	{
    	throw new UnsupportedOperationException("toXML() is not supported in EditAssignUserMap.");
	}
}
