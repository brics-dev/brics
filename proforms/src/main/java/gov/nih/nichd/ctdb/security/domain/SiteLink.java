package gov.nih.nichd.ctdb.security.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * SiteLink DomainObject for the NICHD CTDB Application. This object represents
 * an external URL that can be accessed by a user on the system.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SiteLink extends CtdbDomainObject
{
	private static final long serialVersionUID = -6814618333199590961L;
	
	private String name;
    private String address;
    private String description;
    private SiteLinkType type = SiteLinkType.PROTOCOL;

    /**
     * Default Constructor for the SiteLink Domain Object
     */
    public SiteLink()
    {
        // default constructor
    }

    /**
     * Gets the link's name
     *
     * @return  String The link's name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the link's name
     *
     * @param   name The link's name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the link's web address. An example address would be:
     * http://www.yahoo.com
     *
     * @return  String The link's web address
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * Sets the link's web address. An example address would be:
     * http://www.yahoo.com
     *
     * @param   address The link's web address
     */
    public void setAddress(String address)
    {
        this.address = address;
    }

    /**
     * Gets the link's description
     *
     * @return  String The link's description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the link's description
     *
     * @param   description The link's description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * This method allows the transformation of a SiteLink into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException is thrown if there is an error during the XML tranformation
     * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
     */
    public Document toXML() throws TransformationException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in SiteLink.");
    }

	/**
	 * @return the type
	 */
	public SiteLinkType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(SiteLinkType type) {
		this.type = type;
	}
}