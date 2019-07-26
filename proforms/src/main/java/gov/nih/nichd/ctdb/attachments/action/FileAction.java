package gov.nih.nichd.ctdb.attachments.action;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.attachments.manager.AttachmentManager;
import gov.nih.nichd.ctdb.attachments.util.AttachmentIOUtil;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.ServerFileSystemException;
import gov.nih.nichd.ctdb.common.StrutsConstants;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Dec 18, 2006
 * Time: 1:43:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileAction extends BaseAction {

	private static final long serialVersionUID = -2054733573564839254L;
	private static Logger logger = Logger.getLogger(FileAction.class);
	
	private String id;
	private String associatedId;
	private String typeId;

	public static final String ACTION_MESSAGES_KEY = "attachmentAction_messageskey";

	public String downloadFile() throws Exception {
    	// If hiding the attachment left navigation is needed
        if (session.get("_attachments_hideNav") != null) {
            request.setAttribute("hideNav", "true");
        }
        
        try {
            AttachmentManager am = new AttachmentManager();
            
			int attId = Integer.parseInt(getId());
			int associatedId = Integer.parseInt(getAssociatedId());
			int storageTypeId = Integer.parseInt(getTypeId());
			Attachment file = am.getAttachment(attId, associatedId, storageTypeId);
			File sysFile = am.getFileFromSystem(file);
			
           	AttachmentIOUtil attUtil = new AttachmentIOUtil();
			attUtil.sendToBrowser(sysFile, ServletActionContext.getResponse());
			
		}
        catch (ObjectNotFoundException obfe) {
			logger.error("Could not find that attachment database record or file in the file system.", obfe);
			addActionError("Could not find the attachment in the database or the file system.  Please contact the system adminstrator");
		}
        catch (CtdbException ce) {
			logger.error("Database error occurred while downloading a file.", ce);
			addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[]{"data for the attachment"}));
		}
        catch (ServerFileSystemException sfse) {
			logger.error("Could not access the file stored on the server's file system.", sfse);
			addActionError("Could not access the document from the system.");
		}
        catch (IOException | SecurityException | NumberFormatException e) {
			logger.error("An error occured while downloading the file.", e);
			addActionError("An error occured while downloading the file.  Please try again.");
		}
        
        if (this.hasActionErrors()) {
        	return StrutsConstants.FAILURE;
        }
        
        return null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAssociatedId() {
		return associatedId;
	}

	public void setAssociatedId(String associatedId) {
		this.associatedId = associatedId;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}
	
}