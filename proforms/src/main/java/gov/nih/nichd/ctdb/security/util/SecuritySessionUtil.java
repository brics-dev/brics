package gov.nih.nichd.ctdb.security.util;

import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.account.model.AccountRestServiceModel.AccountsWrapper;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.ws.HashMethods;

public class SecuritySessionUtil {
	
	private static final Logger log = Logger.getLogger(SecuritySessionUtil.class);
	public static final String ACCOUNT_SESSION_KEY = "session_brics_account";
	public static final String BRICS_PERMISSION_GUID = "isAllowedGuid";
	public static final String BRICS_PERMISSION_DDT = "isAllowedDictionary";
	public static final String BRICS_PERMISSION_REPO = "isAllowedRepo";
	public static final String BRICS_PERMISSION_QUERY = "isAllowedQuery";
	public static final String BRICS_PERMISSION_METASTUDY = "isAllowedMetaStudy";
	public static final String BRICS_PERMISSION_REPORTING = "isAllowedReporting";
	
	
	private Protocol selectedProtocol = null;
	private List<Protocol> protocols = null;
	private User user = null;
	private boolean protocolSwitched = false;
	private HttpServletRequest request = null;
	
	/**
	 * Backup constructor if necessary to support non-session loading.
	 * SecuritySessionUtil(request) should be used if possible!
	 */
	public SecuritySessionUtil() {
		protocols = new ArrayList<Protocol>();
	}
	
	/**
	 * Default constructor.
	 * 
	 * @param request - The HTTP request object.
	 */
	public SecuritySessionUtil(HttpServletRequest request) {
		this.request = request;
		try {
			user = (User) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
	
			// Try to get list of studies for the user.
			if (user != null) {
				protocols = SecuritySessionUtil.refreshUserProtocols(user, request);
			}
			else {
				protocols = new ArrayList<Protocol>();
			}
		}
		catch(Exception e) {
			protocols = new ArrayList<Protocol>();
		}
	}
	
	/**
	 * Gets BRICS Account information about a given User.  NOTE: BRICS User ID must be provided in the User object.
	 * @param request HttpRequest of the current session
	 * @param usr User (with brics user id) to retrieve information about
	 * @return Account BRICS account
	 * @throws UnknownHostException
	 * @throws CtdbException
	 * @throws NoRouteToHostException
	 */
	public static Account getBricsAccountInformation(HttpServletRequest request, User usr) throws UnknownHostException, CtdbException, NoRouteToHostException {
		String restfulDomain = SysPropUtil.getProperty("webservice.usermgmt.url");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.account.ws.url")+"/" + usr.getBricsUserId();
		SecuritySessionUtil ssu = new SecuritySessionUtil(request);
		String proxyTicket = ssu.getProxyTicket(restfulDomain);
		Client client = ClientBuilder.newClient();
		restfulUrl= ssu.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
		WebTarget wt = client.target(restfulUrl);
		Account bricsAccount = wt.request(MediaType.TEXT_XML).get(Account.class);
		return bricsAccount;
	}
	
	/**
	 * Method to call BRICS account ws to get the Account object for password verification with Proforms User password
	 * @param request
	 * @return either brics and proforms password matches or not(true/false)
	 * @throws CtdbException 
	 * @throws NoRouteToHostException 
	 */
	public static String getBricsAccountPassword(HttpServletRequest request, User usr) throws UnknownHostException, CtdbException, NoRouteToHostException {
		Account bricsAccount = SecuritySessionUtil.getBricsAccountInformation(request, usr);
		return HashMethods.convertFromByte(bricsAccount.getPassword());
	}
	
	/**
	 * Method to get BRICS account password from Account object
	 * @param request
	 * @return either brics and proforms password matches or not(true/false)
	 */
	public static String getBricsAccountPassword(Account bricsAccount) {
		return HashMethods.convertFromByte(bricsAccount.getPassword());
	}
	
	public static String getBricsAccountSalt(Account bricsAccount) {
		return bricsAccount.getSalt();
	}
	
	/**
	 * Gets the list of user protocols. This method ONLY gets the list.
	 * It does NOT change the session.
	 * 
	 * @param user - the user whose protocols this method gets
	 * @return a list of protocols
	 */
	private static List<Protocol> getUserProtocols(User user) {
		List<Protocol> protocols = null;

		try {
			ProtocolManager protoMan = new ProtocolManager();

			if (user.isSysAdmin()) {
				protocols = protoMan.getProtocols();
			}
			else {
				protocols = protoMan.getUserProtocols(user.getId());
			}
		}
		catch (Exception e) {
			log.error("Could not get a list of studies for the user.", e);
		}

		return protocols != null ? protocols : new ArrayList<Protocol>();
	}
	
	public static synchronized void setupUserBricsPermissions(Account user, HttpServletRequest request) {
		Set<AccountRole> accountRoles = Collections.emptySet();
		if (user != null) {
			accountRoles = user.getAccountRoleList();
		}
		
		HttpSession session = request.getSession();
		
		// Initialize the BRICS permissions.
		session.setAttribute(BRICS_PERMISSION_GUID, false);
		session.setAttribute(BRICS_PERMISSION_DDT, false);
		session.setAttribute(BRICS_PERMISSION_REPO, false);
		session.setAttribute(BRICS_PERMISSION_QUERY, false);
		session.setAttribute(BRICS_PERMISSION_METASTUDY, false);
		session.setAttribute(BRICS_PERMISSION_REPORTING, false);
		
		for (AccountRole role : accountRoles) {
			// Check if the role is still valid.
			if ( role.getIsActive() && !role.isExpired() ) {
				RoleType roleType = role.getRoleType();
				
				// Check for the sys admin role.
				if (roleType == RoleType.ROLE_ADMIN) {
					session.setAttribute(BRICS_PERMISSION_GUID, true);
					session.setAttribute(BRICS_PERMISSION_DDT, true);
					session.setAttribute(BRICS_PERMISSION_REPO, true);
					session.setAttribute(BRICS_PERMISSION_QUERY, true);
					session.setAttribute(BRICS_PERMISSION_METASTUDY, true);
					session.setAttribute(BRICS_PERMISSION_REPORTING, true);
					
					return;
				}
				
				// Check for the other roles.
				switch (roleType) {
					case ROLE_GUID_ADMIN:
						session.setAttribute(BRICS_PERMISSION_GUID, true);
						break;
					case ROLE_GUID:
						session.setAttribute(BRICS_PERMISSION_GUID, true);
						break;
					case ROLE_DICTIONARY_ADMIN:
						session.setAttribute(BRICS_PERMISSION_DDT, true);
						break;
					case ROLE_DICTIONARY:
						session.setAttribute(BRICS_PERMISSION_DDT, true);
						break;
					case ROLE_REPOSITORY_ADMIN:
						session.setAttribute(BRICS_PERMISSION_REPO, true);
						break;
					case ROLE_STUDY:
						session.setAttribute(BRICS_PERMISSION_REPO, true);
						break;
					case ROLE_QUERY_ADMIN:
						session.setAttribute(BRICS_PERMISSION_QUERY, true);
						break;
					case ROLE_QUERY:
						session.setAttribute(BRICS_PERMISSION_QUERY, true);
						break;
					case ROLE_METASTUDY_ADMIN:
						session.setAttribute(BRICS_PERMISSION_METASTUDY, true);
						break;
					case ROLE_METASTUDY:
						session.setAttribute(BRICS_PERMISSION_METASTUDY, true);
						break;
					case ROLE_REPORTING_ADMIN:
						session.setAttribute(BRICS_PERMISSION_REPORTING, true);
						break;
					default:
						break;
				}
			}
		}
	}
    
    /**
     * Gets and stores the list of protocols mapped to a user regardless of
     * the status of the session. This effectively refreshes the protocol list.
     * 
     * @param user the user whose protocols should be retrieved
     * @param request the current request (holds the session)
     * @return a list of protocols which this user is mapped to
     */
    public static synchronized List<Protocol> refreshUserProtocols(User user, HttpServletRequest request) {
		List<Protocol> prots = getUserProtocols(user);
		request.getSession().setAttribute(CtdbConstants.USER_PROTOCOL_LIST, prots);
		
		return prots;
    }
    
    /**
     * Converts a string input to the sha-256 hashed version of that string
     * 
     * @param input the input string
     * @return byte[] the hashed sha-256 version of the string
     */
	public static byte[] sha256(String input) { 
		try {
			MessageDigest cript = MessageDigest.getInstance("SHA-256");
			cript.reset();
			cript.update(input.getBytes());
			return cript.digest();
		}
		catch (NoSuchAlgorithmException e) {
			return new byte[0];
		}
	}
	
	/**
	 * Validates that the account passed has the role proforms or role
	 * proforms_admin and is still an active role.
	 * 
	 * @param account the account to check
	 * @return boolean true if role is valid, otherwise false
	 */
	public static boolean validProformsRole(Account account) {
		Set<AccountRole> accountRoles = account.getAccountRoleList();
		Set<AccountRole> removeRoles = new HashSet<AccountRole>();
		// this is to check against BRICS messing up the role types in their 
		// RoleType enum.  It has happened.
		for (AccountRole prospectiveRole : accountRoles) {
			if (prospectiveRole.getRoleType() == null) {
				removeRoles.add(prospectiveRole);
			}
		}
		
		// just in case, see above
		accountRoles.removeAll(removeRoles);
		
		Iterator<AccountRole> iter = accountRoles.iterator();
		while(iter.hasNext()) {
			AccountRole role = iter.next();
			if ((role.getRoleType().equals(RoleType.ROLE_PROFORMS) || role.getRoleType().equals(RoleType.ROLE_PROFORMS_ADMIN) || role.getRoleType().equals(RoleType.ROLE_ADMIN)) 
					&& role.getIsActive() && !role.isExpired()) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Get accountList by role using RESTful
	 */
    public List<Account> accountRestWs(String role) 
    		throws CtdbException, UnknownHostException, NoRouteToHostException, WebApplicationException, RuntimeException {
    	String restfulDomain = SysPropUtil.getProperty("webservice.usermgmt.url");
    	String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.role.url") + role;
    	String proxyTicket = this.getProxyTicket(restfulDomain);
    	
    	if(proxyTicket != null) {
    		restfulUrl = this.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
    	}

    	Client client = ClientBuilder.newClient();
    	WebTarget wt = client.target(restfulUrl);
    	AccountsWrapper allProformsAccounts = wt.request(MediaType.TEXT_XML).get(AccountsWrapper.class);
    	
    	return allProformsAccounts.getAccountList();

    }
	
	/**
	 * Handles switching the protocol to the one specified in request (GET),
	 * session, or default (only one in the list).  This sets the local
	 * properties protocol, protocols, protocolSwitched, and user.
	 * 
	 * @return Protocol selected Protocol or null if none exists
	 * @throws CtdbException if problems in retrieving from the database
	 */
	public Protocol switchProtocol() throws CtdbException {
		/*
		 *  I'm using an "escape fast" format here.  I know many people don't
		 *  like it because it's slightly less readable but here it makes sense
		 *  to leave this function as soon as possible. 
		 */
		if (user == null) {
			user = (User) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
		}
		
		if (protocols == null) {
			protocols = SecuritySessionUtil.refreshUserProtocols(user, request);
		}
		
		ProtocolManager protoMan = new ProtocolManager();
		Protocol sessionProtocol = getProtocolFromSession(request);
		
		// is the protocol changed in the parameters (IE: GET)
		String id = request.getParameter(CtdbConstants.ID_REQUEST_ATTR);
		if (id != null) {
			// a request has been made to change the protocol
			int protocolId = Integer.parseInt(id);
			if (protocolId == 0) {
				// we are switching to "all"
				selectedProtocol = null;
			}
			else {
				// we are switching to an actual protocol
				selectedProtocol = protoMan.getProtocol(protocolId);
			}
			setProtocolInSession(request, selectedProtocol);
			protocolSwitched = true;
			return selectedProtocol;
		}
		
		// we are not requesting a change to the protocol, so see if one is set
		if (sessionProtocol == null) {
			// how many protocols does this user have?  If only one, use that
			if (protocols.size() == 1) {
				selectedProtocol = protocols.get(0);
				setProtocolInSession(request, selectedProtocol);
				protocolSwitched = true;
				return selectedProtocol;
			}
		}
		else {
			// we are not requesting a change and have one set, so just return it
			// but first make sure the protocol is complete (apparently sometimes it's not?)
			if (sessionProtocol.getDescription() == null) {
				sessionProtocol = protoMan.getProtocol(sessionProtocol.getId());
				setProtocolInSession(request, sessionProtocol);
			}
			selectedProtocol = sessionProtocol;
			return sessionProtocol;
		}
		return null;
	}
	
	/**
	 * Gets the proxy ticket for a proxied web service.  Append the resulting
	 * ticket to your request URL with ?ticket=<this ticket>
	 * 
	 * @param urlBase the domain to call WITH FOLLOWING SLASH
	 * @return proxy ticket string
	 */
	public String getProxyTicket(String domain) throws UnknownHostException,NoRouteToHostException,RuntimeException{
		Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
		Assertion assertion = ((CasAuthenticationToken) auth).getAssertion();
		String url = domain + "portal/j_spring_cas_security_check";
		log.debug("requesting proxy ticket from: " + url);
		String proxyTicket = assertion.getPrincipal().getProxyTicketFor(url);
		log.debug("proxy ticket is: " + proxyTicket);
		return proxyTicket;
	}
	
	/**
	 * Compiles a given webservice call URL into one with a proxy ticket
	 * 
	 * @param baseUrl the original webservice URL
	 * @param ticket the pre-requested ticket to apply to this URL
	 * @return compiled string URL
	 */
	public String compileProxiedWebserviceUrl(String baseUrl, String ticket) {
		String finalUrl = baseUrl;
		if (baseUrl.contains("?")) {
			finalUrl += "&ticket=" + ticket;
		}
		else {
			finalUrl += "?ticket=" + ticket;
		}
		return finalUrl;
	}

	public Protocol getProtocolFromSession(HttpServletRequest request) {
		return (Protocol)request.getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	}
	
	public void setProtocolInSession(HttpServletRequest request, Protocol protocol) {
		request.getSession().setAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY, protocol);
	}
	
	public Protocol getSelectedProtocol() {
		return selectedProtocol;
	}
	
	public User getUser() {
		return user;
	}
	
	public List<Protocol> getProtocols() {
		return protocols;
	}
	
	public boolean switchedProtocol() {
		return protocolSwitched;
	}
}
