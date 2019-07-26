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

public class FormHomeDecorator extends ActionDecorator
{
    /**
     * Default Constructor
     */
    public FormHomeDecorator() {
        super();
    }

    
    
    /**
     * Retrieves the formId and use it as a link
     *
     * @return  HTML string displaying the formId on a Row
     */
    public String getFormId() throws JspException {
        Form form = (Form) this.getObject();
        int id = form.getId();
        return String.valueOf(id);
    }
    
    /**
     * Retrieves the formId and use it as a link
     *
     * @return  HTML string displaying the formId on a Row
     */
    public String getFormId2() throws JspException {
        Form form = (Form) this.getObject();
        int id = form.getId();
        return String.valueOf(id);
    }

    /**
     * Retrieves the intervalList as Administered for display
     *
     * @return  HTML string displaying the intervalList on a Row
     */
    public String getAdministered() {
        Form form = (Form) this.getObject();
    
        if (form.isAdministered()) {
        	return "Yes";
        } else {
        	return "No";
        }
    }
    
    
    public String getCalculationRule() {
    	  Form form = (Form) this.getObject();
          if(form.isHasCalculationRule())
              return "Yes";
          else
              return "No";
    }
    
    
    public String getSkipRule() {
  	  Form form = (Form) this.getObject();
        if(form.isHasSkipRule())
            return "Yes";
        else
            return "No";
    }
    
    
    public String getImageMap() {
    	Form form = (Form) this.getObject();
    	if (form.isHasImageMap()) {
    		return "Yes";
    	} else {
    		return "No";
    	}
      }

    /**
     * Retrieves the updatedDate in a proper format for display
     *
     * @return  HTML string displaying the updatedDate on a Row
     */
    public String getLastupdateddate() {
        Form form = (Form) this.getObject();
        Date date = form.getUpdatedDate();
        SimpleDateFormat localFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.dateformat"));

        return localFormat.format(date);
    }



	public String getNameVersion() throws JspException {
        Form form = (Form) this.getObject();
        int formId = form.getId();
        String nameVersion = form.getName();
        
		String url = this.getWebRoot() + "/form/viewFormDetail.action?source=popup&id=" + formId;
		String anchorTag = "<a href=\"Javascript:popupWindowWithMenu('"+url+" ');\">" + nameVersion + "</a>";

        return anchorTag;
    }

    public String getCtss()
    {
        Form form = (Form) this.getObject();
        if (form.isInCtss()) {
            return "Yes";
        } else {
            return "No";
        }
    }

      
    public String getAssociatedFormGroups() {

    	Form form = (Form) this.getObject();
    	String result = form.getFormGroupNames();

    	return result;
    	
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
        SimpleDateFormat dateFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.datetimeformat"));

        return dateFormat.format(date);
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
    	
    	text = "<input  type=\"checkbox\" name=\"formIdCheckbox\" id=\"" + id + "\"  />\n";
    	text +="<input name=\"numQuestions_"+id+"\" type=\"hidden\" value=\""+form.getNumQuestions() + "\"  />\n";
       	text +="<input name=\"statsQuestions_"+id+"\" type=\"hidden\" value=\""+ form.getStatus().getId() + "\"  />\n";
   	
    	return text;
    }
    
}
