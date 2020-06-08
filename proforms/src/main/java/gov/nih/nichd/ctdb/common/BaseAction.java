package gov.nih.nichd.ctdb.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.opensymphony.xwork2.ActionSupport;

import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.security.common.UserNotFoundException;
import gov.nih.nichd.ctdb.security.domain.Role;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.nichd.ctdb.site.manager.SiteManager;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.account.model.AccountUserDetails;
import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.account.model.hibernate.Account;

/**
 * Struts2 action base class
 *
 * @author jim3
 * @version 1.1
 */
public class BaseAction extends ActionSupport implements ServletRequestAware, SessionAware {
	private static final long serialVersionUID = -4532238839935385785L;
	
	protected HttpServletRequest request = null;
	protected Map<String, Object> session = null;
	
	private String deploymentVersion;
	private String buildID;
	private String deploymentID;
	
	@Autowired
	protected SessionAccount sessionAccount;

	@Autowired
	@Qualifier("ProformsConstants")
	protected ModulesConstants constants;
	
	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}
 
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

    protected void buildLeftNav(int highlightIndex) {
    	buildLeftNav(highlightIndex, null);
    }
    
    protected void buildLeftNav(int highlightIndex, int[] disableIndices) {
    	try {
			LeftNavController controller = new LeftNavController(request, getUser());
    		controller.setHighlightedLink(highlightIndex);
    	
    		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
    		if (protocol == null) {
    			controller.setDisabledLinks(disableIndices);
    		}
    		controller.save();
    	} catch(Exception e) {
    		System.out.println("Couldn't create left navigation bar.");
    		e.printStackTrace();
   		}
    }
    
    protected void disableLinksForProtocolCloseout(int highlightIndex, int[] disableIndices){
    	try {
			LeftNavController controller = new LeftNavController(request, getUser());
    		controller.setHighlightedLink(highlightIndex);
    	
   			controller.setDisabledLinks(disableIndices);
    		controller.save();
    	} catch(Exception e) {
    		e.printStackTrace();
   		}
    }
    
    /**
     * This method should be called when previous action redirects to the current action 
     * with confirmation message passed via ActionMessages stored in the session.  
     * It first retrieves ActionMessages with the given key, adds them to the current
     * action, and deletes the old one from the session.  It functions like the 
     * MessageStoreInterceptor in the flash mode.
     * 
     * @param msgKey - Key of the action messages stored in the session
     */
    protected void retrieveActionMessages(String msgKey) {
   		@SuppressWarnings("unchecked")
		Collection<String> actionMessages = (Collection<String>) session.get(msgKey);
   		
   		if (actionMessages != null && !actionMessages.isEmpty()) {
   	    	this.clearMessages();
   	    	
   			for (String msg : actionMessages) {
   				this.addActionMessage(msg);
   			}
   		}
   		
   		session.remove(msgKey);
    }
	
    protected void retrieveActionErrors(String errorMsgKey) {
   		@SuppressWarnings("unchecked")
		Collection<String> actionErrors = (Collection<String>) session.get(errorMsgKey);
   		
   		if ( actionErrors != null && !actionErrors.isEmpty() ) {
   	    	this.clearErrors();
   	    	
   			for (String error : actionErrors) {
   				this.addActionError(error);
   			}
   		}
   		
   		session.remove(errorMsgKey);
    }

    protected List<String> getOptionsFromMessageResources(String key, String delimiter) {
		List<String> options = new ArrayList<String>();
		
		String optionString = getText(key);
		StringTokenizer st = new StringTokenizer(optionString, delimiter);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			options.add(token);
		}

		return options;
	}

	protected List<String> getOptionsFromMessageResources(String key) {
		return getOptionsFromMessageResources(key, " ,\t");
	}
	
	/**
	 * Generates a unique negative ID for use with newly created POJOs that have not been
	 * persisted to the database yet.
	 * 
	 * @return	A negative integer that can be used as an ID.
	 */
	protected final int getNegativeUniqueId() {
		Integer negativeId = (Integer) session.get(CtdbConstants.UNIQUE_ID);
		
		// Check if the session variable was set
		if ( negativeId != null ) {
			int id = negativeId.intValue() - 1;
			
			// Check for integer wrap around scenario.
			if ( id >= 0 ) {
				id = -1;
			}
			
			negativeId = Integer.valueOf(id);
		}
		else {
			negativeId = Integer.valueOf(-1);
		}
		
		// Save the current negative ID to the session
		session.put(CtdbConstants.UNIQUE_ID, negativeId);
		
		return negativeId.intValue();
	}
	

	/**
	 * This is be preferred way of getting the User object rather than getting it from session via the
	 * {@link CtdbConstants#USER_SESSION_KEY} key. The User object is generated from the {@link SessionAccount} object
	 * that is provided buy Spring Security.
	 * 
	 * @return The ProFoRMS User object that is generated from the SessionAccount object from Spring Security.
	 */
	public final User getUser() {
		// this is no longer stored directly in the session but is managed here and in SessionAccount
		// The first call to account of the session, the account needs to be taken from spring security and added to the
		// session
		if (sessionAccount == null || sessionAccount.getAccount() == null
				|| sessionAccount.getAccount().getUserName().equals(CtdbConstants.ANONYMOUS_USER_NAME)) {
			Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
			Account account = null;
			if (auth == null) {
				// fail fast here in case the security context holder fails
				return null;
			}
 			account = ((AccountUserDetails) auth.getPrincipal()).getAccount();
			sessionAccount.setAccount(account);
		}
		
		try {
			SecurityManager sm = new SecurityManager();
			User user = sm.getUser(sessionAccount.getAccount().getUserName());
			
			Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			
			if(protocol == null) {
				return user;
			} else {
				Role role = sm.getUserRole(user.getId(), protocol.getId());
				if(role != null) {
					List<Role> roleList = new ArrayList<Role>();
					roleList.add(role);
					user.setRoleList(roleList);
				}
				return user;
			}
		}
		catch(Exception e) {
			return null;
		}
	}

	public String getDeploymentVersion() throws IOException {

		if (deploymentVersion == null) {
			ServletContext aContext = request.getSession().getServletContext();
			InputStream fis = aContext.getResourceAsStream("/META-INF/MANIFEST.MF");
			try {
				Properties p = new Properties();
				p.load(fis);
				deploymentVersion = p.getProperty("Implementation-Build");

			} finally {
				fis.close();
			}
		}

		return deploymentVersion;
	}

	public String getBuildID() throws IOException{
		if (this.buildID == null) {
			ServletContext aContext = request.getSession().getServletContext();
			InputStream fis = aContext.getResourceAsStream("/META-INF/MANIFEST.MF");
			try {
				Properties p = new Properties();
				p.load(fis);
				this.buildID = p.getProperty("Implementation-Build");
			} finally {
				fis.close();
			}	
		}		
		return this.buildID;
	}

	public String getDeploymentID() throws IOException{
		if (this.deploymentID == null) {
			ServletContext aContext = request.getSession().getServletContext();
			InputStream fis = aContext.getResourceAsStream("/META-INF/MANIFEST.MF");
			try {
				Properties p = new Properties();
				p.load(fis);
				this.deploymentID = p.getProperty("Repo-Id");
			} finally {
				fis.close();
			}	
		}
		return this.deploymentID.substring(0, 11);
	}
	
	public List<Integer> getUserAssignedSites() throws UserNotFoundException, CtdbException {
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);

		int protocolId = protocol.getId();
		SecurityManager sm = new SecurityManager();
		SiteManager siteManager = new SiteManager();
		
		User user = sm.getUser(sessionAccount.getAccount().getUserName());
		int userId = user.getId();
		boolean isUsrAdmin = user.isSysAdmin();
			
		List<Integer> siteIds = new ArrayList<Integer>();
		
		if(!isUsrAdmin) {
			
			Role role = sm.getUserRole(user.getId(), protocol.getId());
			int roleId = role.getId();
		
			siteIds = siteManager.getSiteIdsFromProtocolusrrole(protocolId, roleId, userId);

		}
		return siteIds;
	}
	
	public Boolean isUserSiteAssigned() throws UserNotFoundException, CtdbException {
		User user = getUser();
		boolean isUsrAdmin = user.isSysAdmin();
		boolean isUserSiteAssigned = false;
			
		List<Integer> siteIds = new ArrayList<Integer>();
		
		if(!isUsrAdmin) {
			
			siteIds = getUserAssignedSites();
			if(siteIds.size() != 0) {
				isUserSiteAssigned =true;
			}
			
		}
		return isUserSiteAssigned;
	}
	
	public Boolean isUserSiteCheckNeeded() throws UserNotFoundException, CtdbException, NullPointerException {
		User user = getUser();
		boolean isUsrAdmin = user.isSysAdmin();
		boolean isUserSiteAssigned = isUserSiteAssigned();
		boolean isUserSiteCheckNeeded = true;
		Boolean siteRestrictProp = Boolean.parseBoolean(SysPropUtil.getProperty(CtdbConstants.USER_WITH_SITE_LIMIT));
		
		if(isUsrAdmin || !isUserSiteAssigned || (siteRestrictProp != null && !siteRestrictProp)) {
			 isUserSiteCheckNeeded = false;		
		}
		return isUserSiteCheckNeeded;
	}
	
}
