package gov.nih.nichd.ctdb.response.domain;

import java.sql.Timestamp;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.DomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * DataEntryAssArch DomainObject for the NICHD CTDB Application
 *
 * @author  Booz Allen Hamilton
 * @version 1.0
 */

public class DataEntryAssArch implements DomainObject
{
	private static final long serialVersionUID = 1883676605856845433L;
	
	private int dataEntryReAssignId = Integer.MIN_VALUE;
    private int dataEntryDraftId = Integer.MIN_VALUE;
    private int previousBy = Integer.MIN_VALUE;
    private int currentBy = Integer.MIN_VALUE;
    private int assignedBy = Integer.MIN_VALUE;
    private Timestamp assignedDate;
    private int dataEntryFlag;

    private String previousByName;
    private String currentByName;
    private String assignedByName;
    private String dataEntryFlagVerble;


    /**
     * Default Constructor for the DataEntryAssArch Domain Object
     */
    public DataEntryAssArch()
    {
        // default constructor
    }

    /**
     * Gets the DataEntryAssArch Object's ID dataentryreassignid
     *
     * @return dataentryreassignid
     */
    public int getId()
    {
        return dataEntryReAssignId;
    }

    /**
     * Sets the DataEntryAssArch Object's ID dataentryreassignid
     *
     * @param dataEntryReAssignId The domain object DataEntryAssArch ID dataentryreassignid
     */
    public void setId(int dataEntryReAssignId)
    {
        this.dataEntryReAssignId = dataEntryReAssignId;
    }

    /**
     * Gets the dataEntryDraftId
     *
     * @return dataEntryDraftId
     */
    public int getDataEntryDraftId()
    {
        return dataEntryDraftId;
    }

    /**
     * Sets the dataEntryDraftId
     *
     * @param dataEntryDraftId dataEntryDraftId
     */
    public void setDataEntryDraftId(int dataEntryDraftId)
    {
        this.dataEntryDraftId = dataEntryDraftId;
    }

    /**
     * Gets getPreviousBy id
     *
     * @return  The getPreviousBy ID
     */
    public int getPreviousBy()
    {
        return previousBy;
    }

    /**
     * Sets getPreviousBy ID
     *
     * @param previousBy ID
     */
    public void setPreviousBy(int previousBy)
    {
        this.previousBy = previousBy;
    }

    /**
     * Gets Current By user's ID of the Domain Object
     *
     * @return The domain object Current by user's ID
     */
    public int getCurrentBy()
    {
        return currentBy;
    }

    /**
     * Sets the Domain Object's Current By user's ID
     *
     * @param currentBy The domain object Current by user's ID
     */
    public void setCurrentBy(int currentBy)
    {
        this.currentBy = currentBy;
    }

    /**
     * Gets asgnedBy By user's ID of the Domain Object
     *
     * @return The domain object assignedBy user's ID
     */
    public int getAssignedBy()
    {
        return assignedBy;
    }

    /**
     * Sets the Domain Object's assigned By user's ID
     *
     * @param assignedBy The domain object assigned by user's ID
     */
    public void setAssignedBy(int assignedBy)
    {
        this.assignedBy = assignedBy;
    }

    /**
     * Gets Assigned date of the Domain Object
     *
     * @return assignedDate
     */
    public Timestamp getAssignedDate()
    {
        return assignedDate;
    }

    /**
     * Sets the Domain Object's Assigned Date
     *
     * @param assignedDate The domain object's Assigned date
     */
    public void setAssignedDate(Timestamp assignedDate)
    {
        this.assignedDate = assignedDate;
    }

    /**
     * Gets the dataEntryFlag of the DomainObject
     *
     * @return The dataEntryFlag
     */
    public int getDataEntryFlag()
    {
        return dataEntryFlag;
    }

    /**
     * Sets the dataEntryFlag of the DomainObject
     *
     * @param dataEntryFlag 
     */
    public void setDataEntryFlag(int dataEntryFlag)
    {
        this.dataEntryFlag = dataEntryFlag;
    }


    /**
     * Gets the previousByName of the DomainObject
     *
     * @return The previousByName
     */
    public String getPreviousByName()
    {
        return previousByName;
    }

    /**
     * Sets the previousByName of the DomainObject
     *
     * @param previousByName 
     */
    public void setPreviousByName(String previousByName)
    {
        this.previousByName = previousByName;
    }

    /**
     * Gets the currentByName of the DomainObject
     *
     * @return The currentByName
     */
    public String getCurrentByName()
    {
        return currentByName;
    }

    /**
     * Sets the currentByName of the DomainObject
     *
     * @param currentByName 
     */
    public void setCurrentByName(String currentByName)
    {
        this.currentByName = currentByName;
    }

    /**
     * Gets the assignedByName of the DomainObject
     *
     * @return The assignedByName
     */
    public String getAssignedByName()
    {
        return assignedByName;
    }

    /**
     * Sets the assignedByName of the DomainObject
     *
     * @param assignedByName 
     */
    public void setAssignedByName(String assignedByName)
    {
        this.assignedByName = assignedByName;
    }

    /**
     * Gets the dataEntryFlagVerble of the DomainObject
     *
     * @return The dataEntryFlagVerble
     */
    public String getDataEntryFlagVerble()
    {
        return dataEntryFlagVerble;
    }

    /**
     * Sets the dataEntryFlagVerble of the DomainObject
     *
     * @param dataEntryFlagVerble 
     */
    public void setDataEntryFlagVerble(String dataEntryFlagVerble)
    {
        this.dataEntryFlagVerble = dataEntryFlagVerble;
    }



    public Document toXML() throws TransformationException, UnsupportedOperationException
	{
    	throw new UnsupportedOperationException("No Implementation at this time for the method toXML()."); 
	}

}
