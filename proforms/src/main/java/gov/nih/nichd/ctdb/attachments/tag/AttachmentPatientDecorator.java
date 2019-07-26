package gov.nih.nichd.ctdb.attachments.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.attachments.manager.AttachmentManager;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class AttachmentPatientDecorator extends IdtDecorator {

    public String getPatientAttachmentName() throws JspException {
    	Attachment a = (Attachment) this.getObject();
    	String subjectName = a.getName();
    	
    	if ( a.getId() > 0 ) {
    		subjectName = "<a href=\"" + this.getWebRoot() + "/attachments/download.action?typeId=" + AttachmentManager.FILE_PATIENT + "&associatedId=" + a.getAssociatedId()
   				  + "&id=" + a.getId() + "\">" + a.getName() + "</a>";
    	}
    	
    	return subjectName;
    }
    
    public String getCategoryName() throws JspException {
    	Attachment a = (Attachment) this.getObject();
    	String categoryName = a.getCategory().getName();
    	
    	return categoryName;
    }
}

