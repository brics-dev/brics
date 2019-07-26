package gov.nih.nichd.ctdb.security.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.security.domain.SiteLink;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
/**
 * SiteLinkAction will handle all CRUD functionality for a SiteLink object.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SiteLinkAction extends BaseAction {

	private static final long serialVersionUID = 4818205912362800378L;

	public static final String ACTION_MESSAGES_KEY = "SiteLinkAction_ActionMessages";

    private String id = null;    
	private String name = null;
    private String description = null;
    private String address = "http://";

    public String execute() {
		this.buildLeftNav(LeftNavController.LEFTNAV_ADMIN_URLS,
				new int[] {LeftNavController.LEFTNAV_SUBJECTS_MANAGE, LeftNavController.LEFTNAV_COLLECT,
						LeftNavController.LEFTNAV_FORM_HOME, LeftNavController.LEFTNAV_STUDY_HOME});
    	return SUCCESS;
    }
    
    public String showEditSiteLink() {
		this.buildLeftNav(LeftNavController.LEFTNAV_ADMIN_URLS,
				new int[] {LeftNavController.LEFTNAV_SUBJECTS_MANAGE, LeftNavController.LEFTNAV_COLLECT,
						LeftNavController.LEFTNAV_FORM_HOME, LeftNavController.LEFTNAV_STUDY_HOME});
        
        try {
            SecurityManager sm = new SecurityManager();
        	
			if (id != null && Integer.parseInt(id) > 0) {
				SiteLink siteLink = sm.getSiteLink(Integer.parseInt(id));
				this.setName(siteLink.getName());
				this.setDescription(siteLink.getDescription());
				this.setAddress(siteLink.getAddress());
			}
        } catch (ObjectNotFoundException one) {
            return StrutsConstants.FAILURE;
        } catch (CtdbException ce) {
            return StrutsConstants.FAILURE;
        }
    	return SUCCESS;
    }
    
    public String saveSiteLink() throws Exception {

		User currentUser = getUser();
		this.buildLeftNav(LeftNavController.LEFTNAV_ADMIN_URLS,
				new int[] {LeftNavController.LEFTNAV_SUBJECTS_MANAGE, LeftNavController.LEFTNAV_COLLECT,
						LeftNavController.LEFTNAV_FORM_HOME, LeftNavController.LEFTNAV_STUDY_HOME});

        try {
            this.validateInput();
            if (this.hasFieldErrors()) {
            	return StrutsConstants.EXCEPTION;
            }
            
            SecurityManager sm = new SecurityManager();
            boolean isEdit = (getId() != null && getId().length() > 0);
            
            SiteLink siteLink = new SiteLink();
            if (isEdit) {  
            	siteLink.setId(Integer.parseInt(getId()));
            } else {  // Otherwise add role
            	siteLink.setCreatedBy(currentUser.getId());
            }
            siteLink.setName(getName());
            siteLink.setDescription(getDescription());
            siteLink.setAddress(getAddress());
            siteLink.setUpdatedBy(currentUser.getId());
            
            if (isEdit) {
            	sm.updateSiteLink(siteLink);
            	addActionMessage(getText(StrutsConstants.SUCCESS_EDIT_KEY, new String[]{"Site URL " + name}));
            } else {
            	sm.createSiteLink(siteLink);
            	addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, new String[]{"Site URL " + name}));
            }
            
            session.put(ACTION_MESSAGES_KEY, this.getActionMessages());
            
        } catch (DuplicateObjectException e) {
        	addActionError(getText(StrutsConstants.ERROR_SITELINK_DUPLICATE_URL, new String[]{getName()}));
        	return StrutsConstants.EXCEPTION;
        } catch (CtdbException ce) {
            return StrutsConstants.FAILURE;
        }
        
        return SUCCESS;
    }
    
    
    /**
     * Validates the input coming in from the form for normal add/edit ops.
     * 
     * I hate having to do this process here since struts has its own method
     * but the struts validator doesn't allow processing to come back to the
     * action and therefore doesn't allow the left nav to be rendered.
     * 
     * @return string error message on error, otherwise empty string
     */
    private void validateInput() {
    	
    	String singular = getText("errors.singular");
    	String errorUrl = getText("sitelink.address.display");
    	String errorName = getText("sitelink.name.display");
    	String errorDesc = getText("sitelink.description.display");
    	
    	if (name == null || name.isEmpty()) {
    		addFieldError(getText("sitelink.name.display"), getText("errors.required", new String[]{errorName, singular}));
    	}
    	
    	if (!Utils.isBlank(address)) {
    		try {
    			URL url = new URL(address);
				URLConnection conn = url.openConnection();
				conn.connect();
    		}catch (MalformedURLException e) {
    			addFieldError(getText("sitelink.address.display"), getText("errors.url", new String[]{errorUrl}));
    		}catch (Exception e) {
    			addFieldError(getText("sitelink.address.display"), getText("errors.invalid", new String[]{errorUrl}));
    		}
    		
    	}
    	
    	
    	if (description != null && description.length() > 4000) {
    		addFieldError(getText("sitelink.description.display"), getText("errors.maxlength", new String[]{errorDesc, singular}));
    	}
    }


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            this.name = null;
        } else {
            this.name = name.trim();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description)
    {
        if (description == null) {
            this.description = null;
        } else {
            this.description = description.trim();
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (address == null) {
            this.address = null;
        } else {
            this.address = address.trim();
        }
    }

}