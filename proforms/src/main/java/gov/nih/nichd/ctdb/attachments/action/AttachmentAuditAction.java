package gov.nih.nichd.ctdb.attachments.action;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.attachments.manager.AttachmentManager;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.util.Utils;


/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Feb 5, 2007
 * Time: 2:06:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class AttachmentAuditAction extends BaseAction { 

	private static final long serialVersionUID = 3655663180109218966L;
	private static final Logger logger = Logger.getLogger(AttachmentAuditAction.class);
	
    private String id;

	public String execute() throws Exception {

        try {
            if ( !Utils.isBlank(getId()) ) {
            	AttachmentManager am = new AttachmentManager();
                request.setAttribute("_attachmentsAudit", am.getAttachmentAudit(Integer.parseInt(getId())));
            }
            else {
            	logger.error("Invalid Id passed to Attachment Audit Action");
            	throw new Exception("Invalid Id passed to Attachment Audit Action");
            }
        }
        catch (CtdbException ce) {
        	logger.error("Database error occurred while getting the attachment audit info.", ce);
        	
        	return StrutsConstants.FAILURE;
        }
        
        return SUCCESS;
    }
	
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}


}
