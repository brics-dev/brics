package gov.nih.nichd.ctdb.protocol.action;

import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolRoleUser;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.protocol.tag.ProtocolUsersIdtDecorator;
import gov.nih.nichd.ctdb.security.domain.Role;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.site.domain.Site;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

/**
 * The Struts Action class responsible for adding and
 * editing study User information for ProFoRMS
 *
 * @author CIT
 */
public class ProtocolUsersAction extends BaseAction {

	private static final long serialVersionUID = -6046228981888875922L;
	private static final Logger logger = Logger.getLogger(ProtocolUsersAction.class);
	
	private String id = "";
	private int siteId = 0;
	private String userRolesAssignment;
	private List<ProtocolRoleUser> roleUsers;
	
	/**
     * Struts Constant used to set/get ActionMessages for this action from session.
     * Used for the redirect to the main listing page for this functionality.
     */
    public static final String ACTION_MESSAGES_KEY = "ProtocolUsersAction_ActionMessages";

    
    private void setupPage(Protocol protocol) throws CtdbException, NoRouteToHostException, 
    		WebApplicationException, UnknownHostException {
    	
        // request all Proforms accounts from BRICS
		SecuritySessionUtil ssu = new SecuritySessionUtil(request);
        List<Account> allProformsAccounts = ssu.accountRestWs("ROLE_PROFORMS");
        
        SecurityManager secMan = new SecurityManager();

         // translate them all into users
        List<User> allUsers = secMan.convertBricsUsers(allProformsAccounts);
        roleUsers = setupProtocolUsers(allUsers, protocol.getId());
        List<Role> roles = secMan.getSystemRoles();
            
        request.setAttribute("currentProtocol", protocol);
        request.setAttribute("roleUsers", roleUsers);
        request.setAttribute("roles", roles);
        request.setAttribute("study", protocol);
    }
    
    
    public String execute() throws Exception {
        buildLeftNav(LeftNavController.LEFTNAV_STUDY_ROLES);
        
		this.retrieveActionMessages(ACTION_MESSAGES_KEY);
		
        Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        if (protocol != null) {
            this.setId(protocol.getId() + "");
        } else {
        	throw new CtdbException("No study selected.");
        }
        
        try {
            this.setupPage(protocol);
        }
        catch (CtdbException ce) {
			logger.error("Database error occurred while setting up the Assign Roles page in the Study module.", ce);
            return StrutsConstants.FAILURE;
		}
        
        return SUCCESS;
    }
    
 // url: http://fitbir-portal-local.cit.nih.gov:8080/proforms/protocol/getProtocolRoleUser.action
 	public String getProtocolRoleUser() throws Exception {
 		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
 		try {
 			IdtInterface idt = new Struts2IdtInterface();
 			setupPage(protocol);
 			ArrayList<ProtocolRoleUser> outputList = new ArrayList<ProtocolRoleUser>(getRoleUsers());
 			idt.setList(outputList);
 			idt.setTotalRecordCount(outputList.size());
 			idt.setFilteredRecordCount(outputList.size());
 			idt.decorate(new ProtocolUsersIdtDecorator());
 			idt.output();
 		} catch (InvalidColumnException e) {
 			logger.error("invalid column: " + e);
 			e.printStackTrace();
 		}
 		return null;
 	}	    

     
    public String saveAssignment() throws Exception {
        buildLeftNav(LeftNavController.LEFTNAV_STUDY_ROLES);
        
        this.clearErrorsAndMessages();
        
        Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        if (protocol != null) {
            this.setId(protocol.getId() + "");
        } else {
        	throw new CtdbException("No study selected.");
        }
        
        try {
        	if ( !Utils.isBlank(getUserRolesAssignment()) ) {
        		JSONArray input = new JSONArray(getUserRolesAssignment());
        		JSONObject inputObj = null;

        		for (int i = 0; i < input.length(); i++) {
        			inputObj = (JSONObject) input.get(i);
       			
        			String userName = "";
        			int roleId = 0;
        			int siteId = 0;

        			if (inputObj.has("roleId")) {
        				roleId = Integer.parseInt(inputObj.getString("roleId"));
        			}
        			if (inputObj.has("userName")) {
        				userName = inputObj.getString("userName");
        			}
        			if (inputObj.has("siteId")) {
        				siteId = inputObj.getInt("siteId");
        			}
       			
        			// enforce required data
        			if (userName.length() == 0) {
        				addActionError("The user name was not set when trying to change roles");
        				throw new CtdbException("The user name was not set when trying to change roles");
        			}
       			
        			SecurityManager secMan = new SecurityManager();
        			// does the user exist locally?
        			User doesExistUser = null;
        			try {
        				doesExistUser = secMan.getUser(userName);
        			}
        			catch (Exception e) {
        				addActionError("The user ID was not found when trying to change roles");
        				throw new CtdbException("The user ID was not found when trying to change roles: "
        						+ e.getMessage(), e);
        			}
       			
        			// save the user's role information (or remove it)
        			ProtocolManager protoMan = new ProtocolManager();
        			
        			try {
        				protoMan.associateProtocolUser(protocol.getId(), siteId, roleId, doesExistUser.getId());
        			}
        			catch (Exception e) {
        				addActionError(getText(StrutsConstants.ERROR_DUPLICATE, Arrays.asList("study users")));
        			}
        		}
        	}
        	else {
        		addActionError(getText(StrutsConstants.ERROR_FIELD_REQUIRED, new String[]{"study", "roles"}));
        	}
       		
        	this.setupPage(protocol);
       		
       	}
        catch ( NumberFormatException nfe ) {
        	logger.error("Could not convert a string to an integer.", nfe);
        	return StrutsConstants.FAILURE;
        }
        catch (JSONException je) {
        	logger.error("An error occurred while reading data from the JSON array.", je);
       		return StrutsConstants.FAILURE;
       	}
        catch (CtdbException ce) {
       		logger.error("A database error occurred while getting data for the current study.", ce);
       		return StrutsConstants.FAILURE;
        }
        
        // Check for any action errors
        if ( !hasActionErrors() ) {
        	addActionMessage(getText(StrutsConstants.SUCCESS_ADD_MULTI_KEY, Arrays.asList("study users")));
        	session.put(ACTION_MESSAGES_KEY, getActionMessages());
        	return SUCCESS;
        }
        else {
        	return StrutsConstants.EXCEPTION;
        }
    }

    
    private List<ProtocolRoleUser> setupProtocolUsers(List<User> allUsers, int protocolId) throws CtdbException {
    	
    	SecurityManager secMan = new SecurityManager();
    	
    	List<ProtocolRoleUser> users = new ArrayList<ProtocolRoleUser>(allUsers.size());
    	ProtocolRoleUser protocolUser;
    	Role userRole;
    	Site userSite;
    	
    	for (User user : allUsers) {
    		protocolUser = new ProtocolRoleUser();
    		protocolUser.setUser(user);
    		userRole = secMan.getUserRole(user.getId(), protocolId);
    		userSite = secMan.getUserProtocolSite(user.getId(), protocolId);
    		protocolUser.setRole(userRole);
    		protocolUser.setSite(userSite);
    		users.add(protocolUser);
    	}
    	
    	// order the users in order of role
    	Collections.sort(users);
    	return users;
    }
    

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getSiteId() {
		return siteId;
	}

	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}

	public String getUserRolesAssignment() {
		return userRolesAssignment;
	}

	public void setUserRolesAssignment(String userRolesAssignment) {
		this.userRolesAssignment = userRolesAssignment;
	}
	
	public List<ProtocolRoleUser> getRoleUsers() {
		return roleUsers;
	}

	public void setRoleUsers(List<ProtocolRoleUser> roleUsers) {
		this.roleUsers = roleUsers;
	}	
    
}
