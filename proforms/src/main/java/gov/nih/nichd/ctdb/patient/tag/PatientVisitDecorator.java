package gov.nih.nichd.ctdb.patient.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.patient.domain.PatientVisit;

/**
 * PatientHomeDecorator enables a table to have a column with Action links (Edit/View/..). This
 *  class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientVisitDecorator extends ActionDecorator
{

    /**
     * Default Constructor
     */
    public PatientVisitDecorator()
    {
        super();
    }

    /**
     * Returns the patient's names
     **/
 
     public String getCheckbox() {
    	 if (this.getPageContext().getRequest().getParameter("exportType") != null) {
    		 return "";
    	 }
    	 else {
    		 int id =((CtdbDomainObject) this.getObject()).getId();
    		 return "<input type='checkbox' name='selectedIds' value=\"" + id + "\" />";
    	 }
    }

     public String getIdDisplay() throws JspException {
         PatientVisit pv = (PatientVisit) this.getObject();
         if (this.getPageContext().getRequest().getParameter("exportType") != null) {
        	 return String.valueOf(pv.getPatientId());
         }
         else {
        	 return "<a href=\"" + this.getWebRoot() + "/patient/viewPatient.action?highlightLeftNav=patientHome.action&subjectId=" + pv.getPatientId() + "\">" + pv.getPatientId() + "</a>";
         }
    }

     public String getGuidDisplay () throws JspException {
         PatientVisit pv = (PatientVisit) this.getObject();
         if (this.getPageContext().getRequest().getParameter("exportType") != null) {
        	 return pv.getGuid();
         }
         else {
        	 return "<a href=\"" + this.getWebRoot() + "/patient/viewPatient.action?highlightLeftNav=patientHome.action&subjectId=" + pv.getPatientId() + "\">" + pv.getGuid() + "</a>";
         }
     }
     
     //TODO: pv.getId() or pv.getPatientId() ???
     public String getMrn () throws JspException {
         PatientVisit pv = (PatientVisit) this.getObject();
         if (this.getPageContext().getRequest().getParameter("exportType") != null) {
        	 String mrn = pv.getMrn();
        	 return mrn == null ? "" : mrn;
         }
         else {
        	 return "<a href=\"" + this.getWebRoot() + "/patient/viewPatient.action?highlightLeftNav=patientHome.action&id=" + pv.getId() + "\">" + (pv.getMrn()==null?"":pv.getMrn()) + "</a>";
         }
    }

    public String getTokenLink() throws JspException {
    	PatientVisit pv = (PatientVisit) this.getObject();
    	String token = pv.getToken();
    	String tokenLink = "";
    	if (token == null) {
    		tokenLink = " ";
    	}
    	else if (this.getPageContext().getRequest().getParameter("exportType") != null) {
    		tokenLink = this.getWebRoot() + "/selfreporting/list?token=" + pv.getToken();
    	}
    	else {
    		tokenLink = "<a href=\"javascript:showTokenLink('" + pv.getGuid() + "','" + this.getWebRoot() + "/selfreporting/list?token=" + pv.getToken() + "')\">" + pv.getToken() + "</a>";
    	}
    	return tokenLink;
    }
}

