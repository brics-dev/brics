package gov.nih.nichd.ctdb.response.domain;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.TransformationException;


/**
 * EventData Domain Object for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class EventData extends CtdbDomainObject
{
	private static final long serialVersionUID = 4134838755112489268L;
	
	private int eventId = Integer.MIN_VALUE;
    private int eventTypeId = Integer.MIN_VALUE;
    private String name;
    private int dataValueId = Integer.MIN_VALUE;
    private List dataValues = new ArrayList();
    private CtdbLookup dataType = null;

    /**
     * Default Constructor for the EventData Domain Object
     */
    public EventData()
    {
        // default constructor
    }

    /**
     * Gets the event ID
     *
     * @return The event ID
     */
    public int getEventId()
    {
        return eventId;
    }

    /**
     * Sets the event ID
     *
     * @param eventId The event ID
     */
    public void setEventId(int eventId)
    {
        this.eventId = eventId;
    }

    /**
     * Gets the event type ID
     *
     * @return The event type ID
     */
    public int getEventTypeId()
    {
        return eventTypeId;
    }

    /**
     * Sets the event type ID
     *
     * @param eventTypeId The event form ID
     */
    public void setEventTypeId(int eventTypeId)
    {
        this.eventTypeId = eventTypeId;
    }

    /**
     * Gets the name of data field.
     *
     * @return the data field name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of data field.
     *
     * @param name the data field name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the data value ID for the data field
     *
     * @return The data value ID
     */
    public int getDataValueId()
    {
        return dataValueId;
    }

    /**
     * Sets the data value ID for the data field
     *
     * @param dataValueId The data value ID
     */
    public void setDataValueId(int dataValueId)
    {
        this.dataValueId = dataValueId;
    }

    /**
     * Gets values for the data field.
     *
     * @return data value list
     */
    public List getDataValues()
    {
        return dataValues;
    }

    /**
     * Sets values for the data field.
     *
     * @param dataValues data value list
     */
    public void setDataValues(List dataValues)
    {
        this.dataValues = dataValues;
    }

    /** Getter for property dataType.
     * @return Value of property dataType.
     */
    public CtdbLookup getDataType()
    {
        return dataType;
    }
    
    /** Setter for property dataType.
     * @param dataType New value of property dataType.
     */
    public void setDataType(CtdbLookup dataType)
    {
        this.dataType = dataType;
    }
    
    /**
     * This method allows the transformation of a EventData into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException is thrown if there is an error during the XML tranformation
     * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
     */
    public Document toXML() throws TransformationException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in EventData.");
    }
}
