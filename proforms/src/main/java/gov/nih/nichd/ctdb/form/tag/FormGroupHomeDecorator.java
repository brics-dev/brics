package gov.nih.nichd.ctdb.form.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormGroup;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: May 23, 2005
 * Time: 12:35:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class FormGroupHomeDecorator extends ActionDecorator  {
	
    /**
     * retrieves the checkbox and adds an onclick method to it to uncheck the others
     * 
     * @return
     */
    public String getFormGroupIdCheckbox() {
    	String text = "";
    	FormGroup domainObject = (FormGroup) this.getObject();
        int id = domainObject.getId();
    	
    	text = "<input type=\"checkbox\" name=\"selectedGroupIds\" value=\"" + id + "\"/>";
    	
    	return text;
    }
    
    /**
     * retrieves the checkbox and adds an onclick method to it to uncheck the others
     * 
     * @return
     */
    public String getFormIdsToAttach() {
    	String text = "";
    	Form form = (Form) this.getObject();
    	int id = form.getId();
   
    	text = "<input name=\"formIdsToAttach\" type=\"checkbox\" value=\"" + id + "\" />";
    	
    	return text;
    }
    
    /**
     * Retrieves the formId and use it as a link
     *
     * @return  HTML string displaying the formId on a Row
     */
    public String getFormId() throws JspException
    {
        Form form = (Form) this.getObject();
        int id = form.getId();
        return String.valueOf(id);
    }
	
    /**
     * Retrieves the intervalList as Administered for display
     *
     * @return  HTML string displaying the intervalList on a Row
     */
    public String getAdministered()
    {
        Form form = (Form) this.getObject();
        if (form.isAdministered())
            return "Yes";
        else
            return "No";
    }
}
