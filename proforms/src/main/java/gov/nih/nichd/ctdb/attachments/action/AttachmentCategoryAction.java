package gov.nih.nichd.ctdb.attachments.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.attachments.domain.AttachmentCategory;
import gov.nih.nichd.ctdb.attachments.manager.AttachmentCategoryManager;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.InvalidRemovalException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jan 24, 2007
 * Time: 9:35:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class AttachmentCategoryAction extends BaseAction {

	private static final long serialVersionUID = -28959189525993668L;
	private static final Logger logger = Logger.getLogger(AttachmentCategoryAction.class);
	
    private int id = -1;
	private String name = "";
    private String description = "";
    private int protocolId = -1;
    private int type = Integer.MIN_VALUE;

    public static final String ACTION_MESSAGES_KEY = "attachmentCategoryAction_messageskey";

	public String execute() throws Exception {
        int attachmentType = Integer.MIN_VALUE;
        if (!Utils.isBlank(request.getParameter("_typeId"))) {
        	attachmentType = Integer.parseInt(request.getParameter("_typeId"));
            setType(attachmentType);
        } else {
        	attachmentType = getType();
        }
        
        if (attachmentType < 0) {
            addActionError("The attachment type must be provided when creating a new attachment category.");
            return StrutsConstants.FAILURE;
        }
    	   
        Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        setProtocolId(protocol.getId());
           
        try {
        	AttachmentCategoryManager am = new AttachmentCategoryManager();
        	LookupManager lookUp = new LookupManager();
        	List<CtdbLookup> attTypes = lookUp.getLookups(LookupType.ATTACHMENT_TYPE);
            
            boolean isEditingForm = id > 0;
        	if (!isEditingForm) {
         	   if (!typeValidated(attTypes, attachmentType)) {
         		  addActionError("The attachment type provided has not defined yet in the reference database table");
         		  return StrutsConstants.EXCEPTION;
        	   }
        	} else {
        		AttachmentCategory ac = am.getAttachmentCategory(id);
                this.setName(ac.getName());
        		this.setDescription(ac.getDescription());
        		this.setProtocolId(ac.getProtocolId());
        		this.setType(ac.getType().getId());
        	}
        	
        	request.setAttribute("_attachmentCategories", am.getAttachmentCategories(protocol.getId(), getType()));
        	
        }
        catch (CtdbException ce) {
        	logger.error("Database error occurred while setting up the page.", ce);
            return StrutsConstants.FAILURE;
        }
        
        return SUCCESS;
	}
	
	public String saveAttachmentCategory() {
		String strutsResult = BaseAction.SUCCESS;
		User user = (User) session.get(CtdbConstants.USER_SESSION_KEY); 
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		AttachmentCategory ac = new AttachmentCategory();
		ac.setName(getName().trim());
		ac.setDescription(getDescription());
		ac.setProtocolId(getProtocolId());
		ac.setType(new CtdbLookup(getType()));
		ac.updateUpdatedByInfo(user);
		
		boolean isEditingForm = id > 0;
        AttachmentCategoryManager am = new AttachmentCategoryManager();
		
		try {
			if (isEditingForm) {  // Update
				ac.setId(id);
				am.updateAttachmentCategory(ac);
				addActionMessage(getText(StrutsConstants.SUCCESS_EDIT_KEY, new String[]{getText("attachmentcategory.singular.display")}));
			}
			else {  // Create
				ac.updateCreatedByInfo(user);
				am.createAttachmentCategory(ac);
				addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, new String[]{getText("attachmentcategory.singular.display")}));
			}
		}
		catch (DuplicateObjectException doe) {
			addActionError(getText(StrutsConstants.ERROR_DUPLICATE, new String[]{ac.getName() + " " +
				getText("attachmentcategory.singular.display")}));
			logger.error("Duplicate attachment category found while saving.", doe);
		}
		catch (CtdbException ce) {
			addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, new String[]{ac.getName() + " " +
					getText("attachmentcategory.singular.display")}));
			logger.error("A database error occured while saving an attachment category.", ce);
		}
		
		// Check for any error messages and reset the from if there aren't any.
    	if ( hasActionErrors() ) {
    		strutsResult = BaseAction.ERROR;
    	}
    	else {
    		this.resetForm();
    	}
		
		// Re-populate the attachment category table
		try {
			request.setAttribute("_attachmentCategories", am.getAttachmentCategories(protocol.getId(), getType()));
		}
		catch (CtdbException ce) {
			logger.error("Could not get a list of attachment categories.", ce);
			addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[]{getText("attachmentcategory.plural.display")}));
			request.setAttribute("_attachmentCategories", new ArrayList<AttachmentCategory>());
		}
        
        return strutsResult;
	}
	
	
	public String deleteAttachmentCategory() {
        Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		String[] delIds = request.getParameterValues("selectedIds");
		List<Long> deletedCategories = new ArrayList<Long>();
		String strutsResult = BaseAction.SUCCESS;
		AttachmentCategoryManager am = new AttachmentCategoryManager();
		
		try {
    		am.deleteAttachmentCategories(delIds, deletedCategories);
    		
    		// Check if the deletion was successful
    		if ( !deletedCategories.isEmpty() ) {
    			if ( delIds.length == 1 ) {
    				addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY, new String[]{getText("attachmentcategory.singular.display")}));
    			}
    			else {
    				addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_MULTI_KEY, new String[]{"The " + getText("attachmentcategory.plural.display")}));
    			}
    		}
        }
		catch (InvalidRemovalException ire) {
			logger.error("Foreign key violation occurred while deleting an attachment category.", ire);
          	addActionError(getText("errors.attchCategory.delete.linkedCollection"));
		}
		catch (CtdbException ce) {
			logger.error("A database error occurred while deleting an attachment category.", ce);
			addActionError(getText(StrutsConstants.ERROR_DELETE, new String[]{getText("attachmentcategory.singular.display")}));
        }
		
		// Check for any error messages.
    	if ( hasActionErrors() ) {
    		strutsResult = BaseAction.ERROR;
    		
    		// Log any successful deletes
          	if ( deletedCategories.size() == 1 ) {
          		addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_MULTI_KEY, new String[]{"1 " + getText("attachmentcategory.singular.display")}));
          	}
          	else if ( !deletedCategories.isEmpty() ) {
          		int numDeleted = delIds.length - deletedCategories.size();
          		addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_MULTI_KEY, new String[]{numDeleted + " " + 
          			getText("attachmentcategory.plural.display")}));
          	}
    	}
		
		// Re-populate the attachment category table
		try {
			request.setAttribute("_attachmentCategories", am.getAttachmentCategories(protocol.getId(), getType()));
		}
		catch (CtdbException ce) {
            logger.error("Could not get a list of attachment categories.", ce);
            addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[]{getText("attachmentcategory.plural.display")}));
            request.setAttribute("_attachmentCategories", new ArrayList<AttachmentCategory>());
        }
    	
		return strutsResult;
	}
	
	private boolean typeValidated(List<CtdbLookup> attTypes, int id) {
		if (attTypes == null || attTypes.size() == 0){
			return false;
		}
		
		for (CtdbLookup lu : attTypes) {
			if (lu.getId() == id) {
				return true;
			}
		}
		return false;
	}

	private void resetForm() {
		id = -1;
    	name = "";
    	description = "";
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(int protocolId) {
		this.protocolId = protocolId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
