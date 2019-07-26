package gov.nih.nichd.ctdb.attachments.action;

import gov.nih.nichd.ctdb.attachments.manager.AttachmentCategoryManager;
import gov.nih.nichd.ctdb.attachments.manager.AttachmentManager;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;


public class AttachmentCategoryListAction extends BaseAction {

	private static final long serialVersionUID = 4481967453184472741L;

	public String execute() throws Exception {
		
		AttachmentCategoryManager am = new AttachmentCategoryManager();
		
		//--from both PatientAction.java and AttachmentCategoryAction.java
		Protocol protocol = (Protocol)session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		int typeId = AttachmentManager.FILE_PATIENT;

        session.put("_attachments_categories", am.getAttachmentCategories(protocol.getId(), typeId));
        session.put("_categoryid", request.getParameter("categoryid"));
        
        return SUCCESS;
	}
}


