package gov.nih.nichd.ctdb.protocol.domain;

import gov.nih.nichd.ctdb.security.domain.SiteLink;

/**
 * ProtocolLink DomainObject for the NICHD CTDB Application. This object represents
 * an external URL that can be accessed by a user on the system within a Protocol.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ProtocolLink extends SiteLink
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 9161072728525227355L;
	private int protocolId;

    /**
     * Default Constructor for the ProtocolLink Domain Object
     */
    public ProtocolLink()
    {
        // default constructor
    }

    /**
     * Gets the link's protocolId
     *
     * @return  The link's protocolId
     */
    public int getProtocolId()
    {
        return protocolId;
    }

    /**
     * Sets the link's protocolId
     *
     * @param protocolId The link's protocolId
     */
    public void setProtocolId(int protocolId)
    {
        this.protocolId = protocolId;
    }

}
