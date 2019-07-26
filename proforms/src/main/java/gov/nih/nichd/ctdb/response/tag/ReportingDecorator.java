package gov.nih.nichd.ctdb.response.tag;

import java.util.Date;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.response.form.DataCollectionLandingForm;
import gov.nih.nichd.ctdb.response.form.ReportingForm;

public class ReportingDecorator extends ActionDecorator{
	
	public ReportingDecorator()
	{
		super();
	}
	
	/**
	 * Method to get userName for the data discrepancy in data collection process
	 * @return
	 * @throws JspException
	 */
	 public String getUserName() throws JspException{
		 ReportingForm form = (ReportingForm) this.getObject();
		 return form.getUserName();
	 }
	 
	 public String getIntervalName() throws JspException{
		 ReportingForm form = (ReportingForm) this.getObject();
		 return form.getIntervalName();
	 }
	 
	 public String getUpdateDate() throws JspException{
		 ReportingForm form = (ReportingForm) this.getObject();
		 return form.getUpdateDate();
	 }
	 
	 public String getPatientNo() throws JspException{
		 ReportingForm form = (ReportingForm) this.getObject();
		 return form.getPatientNo();
	 
	 }
	 
	/* public String getCfvFilledFormsName() throws JspException{
		 ReportingForm form = (ReportingForm) this.getObject();
		 String root = this.getWebRoot();
    	 String fName = form.getCfvFilledFormsName();
    	 int fId = form.getCfvFormId();
    	   String anchorTag = "<a href=\"Javascript:popupWindow ('" + root + "/form/form.do?action=view_form&id=" + fId + "');\">" + fName + "</a>";
    	   	return anchorTag;
	 }*/
	 
	

}
