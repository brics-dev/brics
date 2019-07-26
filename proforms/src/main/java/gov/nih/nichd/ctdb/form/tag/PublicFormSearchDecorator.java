package gov.nih.nichd.ctdb.form.tag;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * FormHomeDecorator enables a table to have a column with Action links (Edit/View/..). This
 * class works with the <code>display</code> tag library.
 *
 * @author  Booz Allen Hamilton
 * @version 1.0
 */

public class PublicFormSearchDecorator extends ActionDecorator
{
    /**
     * Default Constructor
     */
    public PublicFormSearchDecorator()
    {
        super();
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
        String root = this.getWebRoot();

        //return "&nbsp;<a href=\"" + root + "/form/form.do?action=view_form&id=" + id + "\">" + id + "</a>&nbsp;&nbsp;&nbsp;";
        
        return String.valueOf(id);
    }
    
    
    
    /**
     * retrieves the checkbox and adds an onclick method to it to uncheck the others
     * 
     * @return
     */
    public String getFormIdCheckbox() {
    	String text = "";
    	Form form = (Form) this.getObject();
    	int id = form.getId();
    	
    	text = "<input name=\"formIdCheckbox\" type=\"checkbox\" id=\"" + id + "\"  onclick=\"enableDisableButtons(" + id + ")\"/>";
    	//text = "<html:multibox property=\"formIdCheckbox\" value=\"\" ></html:multibox>";
    	
    	return text;
    }


    /**
     * Retrieves the updatedDate in a proper format for display
     *
     * @return  HTML string displaying the updatedDate on a Row
     */
    public String getLastupdateddate()
    {
        Form form = (Form) this.getObject();
        Date date = form.getUpdatedDate();
        SimpleDateFormat localFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.dateformat"));

        return localFormat.format(date);
    }

    /**
     * Retrieves the updatedDate in a proper format for display
     *
     * @return  HTML string displaying the updatedDate on a Row
     */
    public String getUpdateddatetime()
    {
        Form form = (Form) this.getObject();
        Date date = form.getUpdatedDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.dateformat"));

        return dateFormat.format(date);
    }

    /**
     * Retrieves the user actions allowed to be done on a Row of data.
     *
     * @return  HTML string displaying the actions that can be made on a Row
     */
    public String getActions() throws JspException
    {
        Form form = (Form) this.getObject();

        int id = form.getId();
        String root = this.getWebRoot();

        StringBuffer actionText = new StringBuffer();

        if(this.checkPrivilege("addeditforms"))
        {
            actionText.append("&nbsp;<a href=\"" + root + "/form/form.do?action=copy_form&id=" + id + "\">copy to current protocol</a>&nbsp;");
        }
        return actionText.toString();
    }
}
