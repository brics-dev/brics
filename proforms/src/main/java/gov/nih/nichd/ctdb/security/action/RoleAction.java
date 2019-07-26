package gov.nih.nichd.ctdb.security.action;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.security.common.SecurityConstants;
import gov.nih.nichd.ctdb.security.domain.Privilege;
import gov.nih.nichd.ctdb.security.domain.Role;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;

public class RoleAction extends BaseAction {

	private static final long serialVersionUID = 4982413334545861506L;
	private static final Logger logger = Logger.getLogger(RoleAction.class);
	
    public static final String ROLEACTION_MESSAGES_KEY = "RoleAction_ActionMessages";

    private int id = -1;    
	private String name = null;
    private String description = null;
    private String[] selectedPrivileges = null;

	public String showEditRole() {
   		buildLeftNav(LeftNavController.LEFTNAV_ADMIN_ROLES, new int[]{3, 8, 11, 15});
		
		try {
			SecurityManager sm = new SecurityManager();
			
			if ( id > 0 ) {
				Role role = sm.getSystemRole(id);
				name = role.getName();
				description = role.getDescription();
				
				// Set up selected privilege
                String[] privilegeIds = new String[role.getPrivList().size()];
                int idx = 0;
                
                for (Privilege priv : role.getPrivList()) {
                	privilegeIds[idx++] = Integer.toString(priv.getId());
                }
                
                selectedPrivileges = privilegeIds;
			}
			
			List<Privilege> allPreviledges = sm.getPrivileges();
			int portionRows = (allPreviledges.size() >> 1)+(allPreviledges.size() % 2);
			session.put(SecurityConstants.PRIVILEGE_SESSION_KEY, allPreviledges);
			session.put("offset", new Integer(portionRows));
		}
		catch (CtdbException ce) {
			logger.error("Database error on retrieving the " + name + " role.", ce);
			
            return StrutsConstants.FAILURE;
        }

		return SUCCESS;
	}
	
	public String showAddRole() {
   		buildLeftNav(LeftNavController.LEFTNAV_ADMIN_ROLES, new int[]{3, 8, 11, 15});
		
		try {
			SecurityManager sm = new SecurityManager();
			
			List<Privilege> allPreviledges = sm.getPrivileges();
			int portionRows = (allPreviledges.size() >> 1)+(allPreviledges.size() % 2);
			session.put(SecurityConstants.PRIVILEGE_SESSION_KEY, allPreviledges);
			session.put("offset", new Integer(portionRows));
			
		}
		catch (CtdbException ce) {
			logger.error("Database error on retrieving previledes for the " + name + " role.", ce);
			
            return StrutsConstants.FAILURE;
        }

		return SUCCESS;
	}

	public String saveRole() {
		buildLeftNav(LeftNavController.LEFTNAV_ADMIN_ROLES,
				new int[] {LeftNavController.LEFTNAV_SUBJECTS_MANAGE, LeftNavController.LEFTNAV_COLLECT,
						LeftNavController.LEFTNAV_FORM_HOME, LeftNavController.LEFTNAV_STUDY_HOME});
		
		User user = getUser();
        
        this.validateInput();
        if (this.hasFieldErrors()) {
        	return StrutsConstants.EXCEPTION;
        }
        
        Role role = new Role();
        
        if ( id > 0 ) {  // Edit Role if id is bound
        	role.setId(id);
        }
        else {  // Otherwise add role
            role.setCreatedBy(user.getId());
        }
        
        role.setName(this.getName());
        role.setDescription(this.getDescription());
        role.setUpdatedBy(user.getId());

        // process selected privilege list and wrap privilege ids to Privilege object
        String[] selectedPrivs = selectedPrivileges;
        logger.debug("selectedPriv length: " + selectedPrivs.length);
        List<Privilege> privList = new ArrayList<Privilege>();
        
        for ( int i = 0; i < selectedPrivs.length; i++ ) {
            Privilege priv = new Privilege();
            priv.setId(Integer.parseInt(selectedPrivs[i]));
            privList.add(priv);
        }
        
        role.setPrivList(privList);
        
		try {
			SecurityManager sm = new SecurityManager();
			
            if (role.getId() > 0) {  // Edit
           		sm.updateSystemRole(role);
          		addActionMessage(getText(StrutsConstants.SUCCESS_EDIT_KEY, 
          			new String[]{role.getName() + " " + getText("roles.message.display")}));
            }
            else {
           		sm.createSystemRole(role);
           		addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, 
           			new String[]{role.getName() + " " + getText("roles.message.display")}));
            }
            
            session.remove(SecurityConstants.PRIVILEGE_SESSION_KEY);
            session.put(ROLEACTION_MESSAGES_KEY, this.getActionMessages());
		}
		catch (DuplicateObjectException e) {
			logger.error("Duplicate role found in the system.", e);
    		addActionError(getText(StrutsConstants.ERROR_DUPLICATE, 
    			new String[]{role.getName() + " " + getText("roles.message.display")}));
        }
		catch (CtdbException ce) {
			logger.error("Database error occurred while saving the " + name + " role.", ce);
			addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, 
				new String[]{role.getName() + " " + getText("roles.message.display")}));
        }
		
		// Check if there were any action errors
		if ( hasActionErrors() ) {
			return StrutsConstants.EXCEPTION;
		}
		
		return SUCCESS;
	}
	
	
    private void validateInput() {
    	
    	String privileges = getText("role.privileges.display");
    	String roleName = getText("role.name.display");
    	String plural = getText("errors.plural");
    	String singular = getText("errors.singular");
    	String descriptionText = getText("role.description.display");
    	
    	// Test the role name.
    	if ( !Utils.isBlank(name) ) {
    		Pattern p = Pattern.compile("[^a-z0-9\\u0020\\u2000\\u200A]", Pattern.CASE_INSENSITIVE);
    		
    		// Check for any invalid characters
    		if ( p.matcher(name).find() ) {
    			addFieldError(roleName, getText("errors.invalid.chars", new String[]{roleName}));
    		}
    	}
    	else {
    		addFieldError(roleName, getText("errors.required", new String[]{roleName, singular}));
    	}
    	
    	// Test the role description.
    	if ( (!Utils.isBlank(description)) && (description.length() > 4000) ) {
    		addFieldError(descriptionText, getText("errors.maxlength", new String[]{descriptionText, singular}));
    	}
    	
    	// Test for any selected privilages.
    	if ( (selectedPrivileges == null) || (selectedPrivileges.length < 1) ) {
    		addFieldError(privileges, getText("errors.required", new String[]{privileges, plural}));
    	}
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
        if (name == null) {
            this.name = null;
        } else {
            this.name = name.trim();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) {
            this.description = null;
        } else {
            this.description = description.trim();
        }
    }

    public String[] getSelectedPrivileges() {
        return selectedPrivileges;
    }

    public void setSelectedPrivileges(String[] selectedPrivileges) {
        this.selectedPrivileges = selectedPrivileges;
    }

}
