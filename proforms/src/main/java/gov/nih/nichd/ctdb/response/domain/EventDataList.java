package gov.nih.nichd.ctdb.response.domain;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;


/**
 * EventDataList Domain Object for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class EventDataList extends CtdbDomainObject
{
	private static final long serialVersionUID = -3530948811368914630L;
	
	private List eventDatas = new ArrayList();

    /**
     * Default Constructor for the EventDataList Domain Object
     */
    public EventDataList()
    {
        // default constructor
    }

    /**
     * Gets the event data list
     *
     * @return The event data list
     */
    public List getEventDatas()
    {
        return eventDatas;
    }

    /**
     * Sets the event data list
     *
     * @param eventDatas The event data list
     */
    public void setEventDatas(List eventDatas)
    {
        this.eventDatas = eventDatas;
    }

    /**
     * This method allows the transformation of a EventDataList into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException is thrown if there is an error during the XML tranformation
     * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
     */
    public Document toXML() throws TransformationException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in EventDataList.");
    }
}
