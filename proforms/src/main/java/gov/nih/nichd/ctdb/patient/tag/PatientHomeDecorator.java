package gov.nih.nichd.ctdb.patient.tag;

import java.util.Iterator;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientProtocol;
import gov.nih.nichd.ctdb.patient.form.PatientSearchForm;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;

/**
 * PatientHomeDecorator enables a table to have a column with Action links (Edit/View/..). This
 *  class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientHomeDecorator extends ActionDecorator 
{
/*    private Patient patient; //(Patient) this.getObject();
    private Protocol protocol; // = (Protocol) this.getPageContext().getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
*/
	
    /**
     * Default Constructor
     */
    public PatientHomeDecorator() {
        super();
    }
    
    public String getIdDisplay () throws JspException {
        Patient patient = (Patient) this.getObject();
        
        String subjIdString = patient.getSubjectId();
        
        if(subjIdString.startsWith("UNKBRICS")) {
        	subjIdString = "";
        }
        
        if (this.getPageContext().getRequest().getParameter("exportType") != null) {
        	return subjIdString;
        }
        else
        {
        	return "<a href=\"" + this.getWebRoot() + "/patient/viewPatient.action?subjectId=" + patient.getSubjectId()+ "\">" + subjIdString + "</a>";
        }
    }

    public String getGuidDisplay () throws JspException {
        Patient patient = (Patient) this.getObject();
        if (this.getPageContext().getRequest().getParameter("exportType") != null) {
        	return patient.getGuid();
        }
        else
        {
        	return "<a href=\"" + this.getWebRoot() + "/patient/viewPatient.action?subjectId=" + patient.getSubjectId() + "\">" + patient.getGuid() + "</a>";
        }
    }

    public String getLastName()
    {
        Patient patient = (Patient) this.getObject();
        return patient.getLastName();
    }

    public String getFirstName()
    {
        return ((Patient) this.getObject()).getFirstName();
    }

    public String getPatientProtocolStatus()
    {
    	 String message = "";
    	 Patient patient = (Patient) this.getObject();
    	 //Protocol protocol = (Protocol) this.getPageContext().getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
    	 int i = 1;
    	 for (Iterator iter = patient.getProtocols().iterator(); iter.hasNext();) {
    		 PatientProtocol pp = (PatientProtocol) iter.next();
    		 if (pp.getId() <=0) { // no protocol associate with current subject, i.e. no study specific value
    			 continue;
    		 }
    		 // System.out.println("Same with current Protocol ID:"+ pp.getId() +  " status:" + pp.isActive());
    		 if (pp.isActive()){
    			 message ="Active";
    		 }
    		 else{
    			 message ="Inactive";
    		 }
    	 }
    	 return message;
    }
    
    public String getPatientGroup() {
    	String message = "";
    	Patient patient = (Patient) this.getObject();
    	Protocol protocol = (Protocol) this.getPageContext().getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
    	int i = 1;
    	for (Iterator iter = patient.getProtocols().iterator(); iter.hasNext();) {
    		PatientProtocol pp = (PatientProtocol) iter.next();
    		i++;
    		if (pp.getId() == protocol.getId()) {
   				message =  pp.getGroupName();
   				break;
    		}
    	}
    	return message;
    }
    

     public String getCheckbox()
    {
    	 	//int id =((CtdbDomainObject) this.getObject()).getId();
    	 	Patient patient = (Patient) this.getObject();
    	 	String nrn = patient.getSubjectId();
         if (this.getPageContext().getRequest().getParameter("exportType") != null) {
         	return "";
         }
         else
         {
         //return "<input type='checkbox' name='patientId' value=\"" + ((Patient) this.getObject()).getNihRecordNumber() + "\">";
        	 return "<input type='checkbox' name='selectedIds' value=\"" + nrn + "\" />";
         }
    }

    
    /**
     * Returns a string of all protocol numbers/names associated with the patient. In the following
     * format:<br>
     * <li> protocol number (protocol name)
     *
     * @return  The string representation of all protocol numbers/names associated with the patient.
     */

    public String getProtocols() throws JspException {
    	Patient patient = (Patient) this.getObject();
        Protocol proto = (Protocol) this.getPageContext().getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
    
    	String result = "";
    	for(Iterator iterator = patient.getProtocols().iterator(); iterator.hasNext();) {
    		PatientProtocol pp = (PatientProtocol) iterator.next();
    		if(pp !=null && pp.getProtocolNumber() != null){
    			result  += pp.getProtocolNumber();
    		}
            if(iterator.hasNext())
            {
                result += ", ";
            }
    	}
    	return result;
    }


    /**
     * Returns edit and audit action links for a patient
     *
     * @return  The action links
     */
    public String getActions() throws JspException
    {
        String actionStr = "";
        CtdbDomainObject domainObject = (CtdbDomainObject) this.getObject();
        int id = domainObject.getId();
        String root = this.getWebRoot();
        if(this.checkPrivilege("addeditpatients"))
        {
            actionStr += "&nbsp;<a href=\"" + root + "/patient/showEditPatient.action?patientId=" + id + "\">edit</a>&nbsp;&nbsp;&nbsp;";
        }
        if(this.checkPrivilege("viewaudittrails"))
        {
            actionStr += "<a href=\"Javascript:popupWindow ('" + root + "/patient/patientAudit.action?id=" + id + "');\">view&nbsp;audit</a>";
        }

        PatientSearchForm form = (PatientSearchForm) this.getPageContext().getRequest().getAttribute("patientSearchForm");

        if(this.checkPrivilege("addeditpatients") && form.getInProtocol().equalsIgnoreCase("no"))
        {
            actionStr += "&nbsp;&nbsp;<a href=\"" + root + "/patient/showEditPatient.action?action=add_to_protocol&patientId=" + id + "\">associate&nbsp;to&nbsp;protocol</a>";
        }
        if (this.checkPrivilege("manageAttachments")) {
            actionStr += "&nbsp;&nbsp;<a href=\""+root +"/attachments/attachmentHome.do?highlightLeftNav=patientHome.do&hideNav=false&typeId=2&associatedId="+id+"\">attachments</a> ";
        }

         if (this.checkPrivilege("createquery")) {
            actionStr += ("&nbsp;<a href=\""+root+"/qa/qaQuery.do?action=add_form&_associatedId="+id+"&_subId=0&_ocId=9\">query</a>&nbsp;");
        }

        return actionStr;
    }

    public String getAdminFormActions () throws JspException {
    	Patient patient = (Patient) this.getObject();
        String actions = "";
        int id = patient.getId();
        String root = this.getWebRoot();
        actions += "<a href='"+ root + "/response/dataEntrySetup.do?action=add_form&selectedPatient="+id;
        actions += "' title='Select Patient' class='tableCell'>select patient</a>&nbsp;&nbsp;&nbsp;";

        actions += "<a href='"+root+"/patient/viewPatient.action?id="+id+"' class='tableCell' title='View Details'>";
        actions += "view details</a>";

        return actions;
    }

    public String getFormatValidated () throws JspException {
    	Patient patient = (Patient) this.getObject();
    	Protocol protocol = (Protocol) this.getPageContext().getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
    	 String result = "";
    	for(Iterator iterator = patient.getProtocols().iterator(); iterator.hasNext();) {
    		PatientProtocol pp = (PatientProtocol) iterator.next();
    		if(pp !=null && pp.getId() == protocol.getId() && pp.isValidated()){
                return "<img src=\"" + this.getWebRoot()  + "/images/checkMark.png\" alt='validated' width='15' height='15' />";
            }
    	}
    	return result;
    }


}
