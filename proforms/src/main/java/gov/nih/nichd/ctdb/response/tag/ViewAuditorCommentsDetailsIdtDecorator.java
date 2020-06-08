package gov.nih.nichd.ctdb.response.tag;

import java.text.SimpleDateFormat;

import javax.servlet.jsp.JspException;

import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.nichd.ctdb.response.domain.ViewAuditorComment;
import gov.nih.nichd.ctdb.security.common.SecurityConstants;
import gov.nih.nichd.ctdb.security.domain.Privilege;
import gov.nih.nichd.ctdb.security.domain.User;

public class ViewAuditorCommentsDetailsIdtDecorator extends ActionIdtDecorator {
    /**
     * Default Constructor
     */
    public ViewAuditorCommentsDetailsIdtDecorator()    {
        super();
    }
    
    /**
     * Retrieves response's answer edit date
     *
     * @return  HTML string displaying the answer edit date.  If date is null,
     *			return empty string.
     */
    public String getEditDate() {
    	ViewAuditorComment  vac = (ViewAuditorComment) this.getObject();
    	SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    	if (vac.getEditDate() != null) {
         return df.format(vac.getEditDate());
     } else {
         return "N/A";
     }
       
 
    }
    
    public String getQuesText() throws JspException	{
    	ViewAuditorComment  vac = (ViewAuditorComment) this.getObject();

        String quesText = vac.getQuestionText();
        int sId=vac.getSectionId();
        int qId=vac.getQuestionId();
        String sId_qId = "S_" + sId + "_Q_" + qId;
        int selected_Form_Ids=vac.getAdministeredformId();
        
        User currentUser = (User) ServletActionContext.getRequest().getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
        Privilege rspdAdtCmmtPrivlege = new Privilege(SecurityConstants.RESPOND_TO_AUDIT_COMMENTS_PRIV);
        
        if(quesText!=null && !quesText.trim().isEmpty()) {
        	String quesTextTm = quesText.trim();
        	//link to edit form page if this eform is not CAT form && current user is sysAdmin or has respond to auditor comments privilege
        	if (!vac.getIsCat() && currentUser != null 
        			&& (currentUser.isSysAdmin() || currentUser.hasPrivilege(rspdAdtCmmtPrivlege)) ) {
        		quesText = "<a href=\"" + this.getWebRoot()
				+ "/response/dataCollection.action?action=editForm&mode=formPatient&aformId="
				+ selected_Form_Ids + "&editUser=1&sqshow=true&sId=" + sId + "&qId=" + qId + "#" + sId_qId + "\">"
				+ quesTextTm + "</a>";
        	} else {
        		//link to audit comment page if this eform is a CAT form or current user is not sysAdmin and does not have respond to auditor comments privilege
        		quesText = "<a href=\"" + this.getWebRoot()
					+ "/response/dataCollection.action?action=auditComments&mode=formPatient&aformId="
					+ selected_Form_Ids + "&audit=true&sqshow=true&sId=" + sId + "&qId=" + qId + "#" + sId_qId +"\">"
					+ quesTextTm + "</a>";
        	}
        }
        return quesText;
    }
    
    public String getAudStatus() throws JspException {
    	ViewAuditorComment  vac = (ViewAuditorComment) this.getObject();

        String audstatus = vac.getAuditStatus();
        int sId=vac.getSectionId();
        int qId=vac.getQuestionId();
        int selected_Form_Ids=vac.getAdministeredformId();
        String sId_qId = "S_" + sId + "_Q_" + qId;
        String collectionStatus = "";
        
        User currentUser = (User) ServletActionContext.getRequest().getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
        Privilege rspdAdtCmmtPrivlege = new Privilege(SecurityConstants.RESPOND_TO_AUDIT_COMMENTS_PRIV);
        
        if(audstatus!=null && !audstatus.trim().isEmpty()) {
        	String audStatusTm = audstatus.trim();
        	
    		if(CtdbConstants.AUDITCOMMENT_STATUS_COMPLETED.equalsIgnoreCase(audStatusTm)) {
    			collectionStatus = CtdbConstants.DATACOLLECTION_STATUS_COMPLETED;
    		} else if(CtdbConstants.AUDITCOMMENT_STATUS_LOCKED.equalsIgnoreCase(audStatusTm)) {
    			collectionStatus = CtdbConstants.DATACOLLECTION_STATUS_LOCKED;
       		} else if(CtdbConstants.AUDITCOMMENT_STATUS_INPROGRESS.equalsIgnoreCase(audStatusTm)) {
    			collectionStatus = CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS;
    		}
        	//link to edit form page if this eform is not CAT form && current user is sysAdmin or has respond to auditor comments privilege
        	if (!vac.getIsCat() && currentUser != null 
        			&& (currentUser.isSysAdmin() || currentUser.hasPrivilege(rspdAdtCmmtPrivlege)) ) {
        		audstatus = "<a href=\"" + this.getWebRoot()
				+ "/response/dataCollection.action?action=editForm&mode=formPatient&aformId="
				+ selected_Form_Ids + "&editUser=1&sqshow=true&sId=" + sId + "&qId=" + qId + "#" + sId_qId + "\">"
				+ collectionStatus + "</a>";
        	} else { 
        		//link to audit comment page if this eform is a CAT form or current user is not sysAdmin and does not have respond to auditor comments privilege
				audstatus = "<a href=\"" + this.getWebRoot()
				+ "/response/dataCollection.action?action=auditComments&mode=formPatient&aformId="
				+ selected_Form_Ids + "&audit=true&sqshow=true&sId=" + sId + "&qId=" + qId + "#" + sId_qId + "\">"
				+ collectionStatus + "</a>";        	}
        }
        return audstatus;
    }
    

}
