package gov.nih.nichd.ctdb.response.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;

import javax.servlet.jsp.JspException;

public class EditAssignmentDecorator extends ActionDecorator {


	public EditAssignmentDecorator() {
		super();
	}
	
	
	/**
     * Retrieves the formId and use it as a link
     *
     * @return  HTML string displaying the formId on a Row
     */
    public String getSubject() throws JspException
    {
        AdministeredForm aform = (AdministeredForm) this.getObject();
        
        return aform.getPatient().getLabel();
    }
    
    public String getGuid() throws JspException {
		AdministeredForm domainObject = (AdministeredForm) this.getObject();
		return domainObject.getPatient().getLabel();
	}
	
	public String getNihRecordNo() throws JspException{
		AdministeredForm domainObject = (AdministeredForm) this.getObject();
		return domainObject.getPatient().getLabel();
	}
	
	public String getSubjectNo() throws JspException{
		AdministeredForm domainObject = (AdministeredForm) this.getObject();
		return domainObject.getPatient().getLabel();
	}
	
	public String getSubjectName() throws JspException{
		AdministeredForm domainObject = (AdministeredForm) this.getObject();
		return domainObject.getPatient().getLabel();
	}
    
    /**
     * Retrieves the formId and use it as a link
     *
     * @return  HTML string displaying the formId on a Row
     */
    public String getVisitDate() throws JspException
    {
        AdministeredForm aform = (AdministeredForm) this.getObject();
        
        return aform.getpVisitDate();
    }
    
    
    /**
     * Retrieves the formId and use it as a link
     *
     * @return  HTML string displaying the formId on a Row
     */
    public String getVisitType() throws JspException
    {
        AdministeredForm aform = (AdministeredForm) this.getObject();
        
        return aform.getInterval().getName();
    }
    
    
    
    /**
     * Retrieves the formId and use it as a link
     *
     * @return  HTML string displaying the formId on a Row
     */
    public String getFormName() throws JspException
    {
        AdministeredForm aform = (AdministeredForm) this.getObject();
        String name = aform.getForm().getName();
		int formId = aform.getForm().getId();
		String root = this.getWebRoot();

		String anchorTag = "<a href=\"Javascript:popupWindowWithMenu ('" + root
				+ "/form/viewFormDetail.action?source=popup&id=" + formId + "');\">"
				+ name + "</a>";

		return anchorTag;
    }
    
    /**
     * Retrieves the formId and use it as a link
     *
     * @return  HTML string displaying the formId on a Row
     */
    public String getUser1() throws JspException
    {
        AdministeredForm aform = (AdministeredForm) this.getObject();
        return aform.getUser1LastNameFirstNameDisplay();
    }
    
    
    /**
     * Retrieves the formId and use it as a link
     *
     * @return  HTML string displaying the formId on a Row
     */
    public String getStatus1() throws JspException
    {
        AdministeredForm aform = (AdministeredForm) this.getObject();
        
        return aform.getEntryOneStatus();
    }
    
    
    /**
     * Retrieves the formId and use it as a link
     *
     * @return  HTML string displaying the formId on a Row
     */
    public String getUser2() throws JspException
    {
        AdministeredForm aform = (AdministeredForm) this.getObject();
        return aform.getUser2LastNameFirstNameDisplay();
    }
    
    
    /**
     * Retrieves the formId and use it as a link
     *
     * @return  HTML string displaying the formId on a Row
     */
    public String getStatus2() throws JspException
    {
        AdministeredForm aform = (AdministeredForm) this.getObject();
        
        return aform.getEntryTwoStatus();
    }
    
	
}
