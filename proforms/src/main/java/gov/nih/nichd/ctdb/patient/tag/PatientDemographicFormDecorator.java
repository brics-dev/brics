package gov.nih.nichd.ctdb.patient.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.response.domain.FormCollectionDisplay;

public class PatientDemographicFormDecorator extends ActionDecorator {
    public PatientDemographicFormDecorator()
    {
        super();
    }

    public String getCheckbox(){
    	int id =((FormCollectionDisplay) this.getObject()).getAdministeredFormId();
    return "<input type='checkbox' name='demographicFormId' value=\"" + id + "\" />";
    }

    public String getFormName() throws JspException {
    	FormCollectionDisplay fcd = (FormCollectionDisplay) this.getObject();
		String url = this.getWebRoot()+ "/response/viewForm.action?action=view_draft_form_withheader&source=response_home&id=" + fcd.getAdministeredFormId() +
				 "&userid="+ fcd.getDataEntryByUserId();
   		// return "<a class='anchorDemoForm' href=\"Javascript:popupWindow ('" + url+"'); value=\"" + url +"\">" + fcd.getFormName() + "</a>";
   		return "<a class='anchorDemoForm' href=\"" + url+"\" >" + fcd.getFormName() + "</a>";
}

}