package gov.nih.nichd.ctdb.security.tag;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import javax.servlet.jsp.JspException;

/**
 * SiteLinkDecorator enables a table to have a column with Action links (Edit/View/..). This
 * class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SiteLinkDecorator extends ActionDecorator
{
    /**
     * Default Constructor
     */
    public SiteLinkDecorator()
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
    public String getLinkCheckBoxes() throws JspException
    {
    	CtdbDomainObject domainObject = (CtdbDomainObject) this.getObject();
        int id = domainObject.getId();
        String htmlText;
        
        htmlText = "<input type=\"checkbox\" name=\"selectedSite\" value=\"" + id + "\" />";
        
        return htmlText;
    }
}
