package gov.nih.nichd.ctdb.security.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;

/**
 * SiteLinkDecorator enables a table to have a column with Action links (Edit/View/..). This
 * class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SiteLinkIdtDecorator extends ActionIdtDecorator
{
    /**
     * Default Constructor
     */
    public SiteLinkIdtDecorator()
    {
        // default constructor
        super();
    }
    
    /**
     * Retrieves the checkbox for a row of data
     * 
     * @return	HTML string displaying the checkbox for a row
     * @throws JspException	Checkbox creation failed
     */
    public int getLinkId() throws JspException
    {
    	CtdbDomainObject domainObject = (CtdbDomainObject) this.getObject();
        int id = domainObject.getId();
        
        return id;
    }
}
