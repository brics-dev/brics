package gov.nih.nichd.ctdb.security.common;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jasig.cas.client.util.CommonUtils;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.patient.util.Encryption;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.ws.HashMethods;

public class CasPostAuthFilter implements Filter {
	private static final Logger logger = Logger.getLogger(CasPostAuthFilter.class);
	public static final String LOCAL_TIMER_KEY = "localTimeoutRefreshLast";
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) 
		throws ServletException, IOException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		User currentLocalUser = (User) httpRequest.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
		String currentLocalUsername = null;
		
		// if we came from login, start (or restart) the local timeout timer
		if (httpRequest.getParameter("ticket") != null) {
			setLocalTimer(httpRequest);
		}
		
		// protection circuits for various strange failure conditions
		if (currentLocalUser != null) {
			currentLocalUsername = currentLocalUser.getUsername();
			logger.debug("user from session: currentLocalUsername: " + currentLocalUsername);
		
			if (Utils.isBlank(currentLocalUsername)) {
				// protect against "zombie proforms": the case where the local user object exists but is empty 
				// how THAT happens I have no idea, but it ends here!
				httpRequest.getSession().removeAttribute(CtdbConstants.USER_SESSION_KEY);
			}
			else {
				/*
				 * the case we're working with here is of a user logging out of CAS
				 * from outside ProFoRMS.  That causes CAS to have the user actually
				 * logged out OR logged in as another user.  That's BAD.
				 * So, to protect against it, we check the remote user (possibly the
				 * correct one from CAS if we have gone to the CAS login since coming
				 * here) and the referrer.  If the referrer is null, the user typed
				 * in the address and should be sent to the ProFoRMS home page anyway.
				 * If the user came from another domain, outside of ProFoRMS, we need
				 * to redirect to the CAS login page.  That redirection will then come
				 * straight back here if the user is actually logged in.
				 * HOWEVER (important), the remoteUser will have been reset to the
				 * correct CAS user after the redirect back.  So, we can sign the user
				 * in locally with their correct username.
				 */
				String remoteUser = httpRequest.getRemoteUser();							
				if (!remoteUser.equals(currentLocalUsername)) {
					logger.warn("logout different remote user:" + remoteUser);
					logout(httpRequest, httpResponse);
					redirectToLogin(httpRequest, httpResponse);
					return;
				}
			}
		}
		
		// normal log-in procedure
		if (httpRequest.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY) == null) {
			logger.info("Begin standard login...");
			try {
				User user = null;
				SecurityManager sm = new SecurityManager();
	
				String username = httpRequest.getUserPrincipal().getName();
				System.out.println("standard login username (from CAS): " + username);
			
				try {
					user = sm.ssoAuthenticate(username, httpRequest);
				}
				catch (AuthenticationFailedException e) {
					user = null;
					logger.info("Username not found in the usr table. Preping for new user creation.");
				}
				
				// Initialize the BRICS account web service client
				String restfulDomain = SysPropUtil.getProperty("webservice.usermgmt.url");
				String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.account.byusername.url")+"/" + username;
				SecuritySessionUtil ssu = new SecuritySessionUtil(httpRequest);
				String proxyTicket = ssu.getProxyTicket(restfulDomain);
				Client client = ClientBuilder.newClient();
				restfulUrl = ssu.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
				WebTarget wt = client.target(restfulUrl);
				Account account = wt.request(MediaType.TEXT_XML).get(Account.class);

				if (user == null) {
					logger.info("Begin new user creation...");
					
					// get the user's information
					if (account == null) {
						throw new AuthenticationFailedException("The user " + username + " does not have access to this module");
					}
					
					// check that the user has proforms access.
					if (!SecuritySessionUtil.validProformsRole(account)) {
						throw new AuthenticationFailedException("The user " + username + " does not have access to this module");
					}
					
					// save the user's information into the database
					User newUser = new User(account);
					
					logger.debug("New user has bricsId: " + newUser.getBricsUserId() + " and username: " + newUser.getUsername());
					
					sm.createUser(newUser);
					
					logger.debug("New user has usrid: " + newUser.getId());
					logger.debug("New user added: " + username);
					logger.info("Attempting new user authentication...");
					
					user = sm.ssoAuthenticate(username, httpRequest);
					
					logger.debug("User data after authentication:");
					logger.debug("username: " + user.getUsername() + " brics ID: " + user.getBricsUserId() + " usrid: " + user.getId());
				}
				else {
					logger.info("User exists in system, update info from BRICS account web service...");
					// get the user's information
					if (account == null) {
						throw new AuthenticationFailedException("The user " + username + " with ID " + user.getId() + " could not be found in BRICS");
					}
					if (!SecuritySessionUtil.validProformsRole(account)) {
						throw new AuthenticationFailedException("The user " + username + " does not have access to this module");
					}
					// refresh ALL user information on our end
					User updateUser = new User(account);
					
					logger.debug("User data from account web service:");
					logger.debug("username: " + user.getUsername() + " brics ID: " + user.getBricsUserId() + " usrid: " + user.getId());
					
					updateUser.setId(user.getId());
					
					logger.debug("User data after ID update:");
					logger.debug("username: " + user.getUsername() + " brics ID: " + user.getBricsUserId() + " usrid: " + user.getId());
					
					User.setCreateStudyPrivilege(account, user);
					sm.updateUser(updateUser);
					
					logger.debug("User data updated in db:");
					logger.debug("username: " + user.getUsername() + " brics ID: " + user.getBricsUserId() + " usrid: " + user.getId());
					
					// now that we've updated the user, set the updated information to the common variable
					user = sm.getUser(updateUser.getId());
		            sm.setRoles(user);
		            
		            logger.debug("User data after retrieve from db:");
		            logger.debug("username: " + user.getUsername() + " brics ID: " + user.getBricsUserId() + " usrid: " + user.getId());
				}
				
				SecuritySessionUtil.refreshUserProtocols(user, httpRequest);
				SecuritySessionUtil.setupUserBricsPermissions(account, httpRequest);
				httpRequest.getSession().setAttribute(SecuritySessionUtil.ACCOUNT_SESSION_KEY, account);
				httpRequest.getSession().setAttribute(CtdbConstants.USER_SESSION_KEY, user);
				
	            Cookie cookie = new Cookie(CtdbConstants.CTDB_USER_COOKIE, user.getUsername());
	            cookie.setSecure(true);
	            httpResponse.addCookie(cookie);
			}
			catch(Exception e) {
				logger.error("Failed login.  Redirecting to the SSO login failed page.", e);
	            
	            // Logout the user to invalidate session and cookie.
	            logout(httpRequest, httpResponse);
	            httpResponse.sendRedirect(httpRequest.getContextPath() + "/security/ssofailed.jsp");
	            return;
			}
		}
		// end the filter
		fc.doFilter(request, response);
	}
	
	/**
	 * Redirects the user to the CAS login page.  This doesn't necessarily mean
	 * that the user will see the login page because it could redirect back.
	 * 
	 * @param request the request so we can get the service information
	 * @param response the response so we can send the redirect
	 * @throws IOException thrown in case of a problem with the redirect
	 */
	public static void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("Redirecting to Login...");
	
		String service = SysPropUtil.getProperty("app.webroot");
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();
		if (queryString == null) {
			service = requestURL.toString();
		}
		else {
			service = requestURL.append('?').append(queryString).toString();
		}
		logger.debug("service string calculated in casPostAuthFilter: " + service);
		String url = CommonUtils.constructRedirectUrl(SysPropUtil.getProperty("webservice.cas.login.url"), "service", service, false, false);
		logger.debug("Login url calculated in casPostAuthFilter: " + url);
		response.sendRedirect(url);
	}
	
	/**
	 * Sets the local timeout metric first point.  Used by the CAS integration code to determine
	 * time elapsed since last CAS server hit.
	 * 
	 * @param request the request, so we can get the session
	 */
	public static void setLocalTimer(HttpServletRequest request) {
		request.getSession().setAttribute(CasPostAuthFilter.LOCAL_TIMER_KEY, new Long(System.currentTimeMillis()));
	}
	
	/**
	 * Gets the local timeout metric first point from the session.
	 * 
	 * @param request the request so we can get the time point from session
	 * @return Object 
	 */
	public static Long getLocalTimer(HttpServletRequest request) {
		return (Long) request.getSession().getAttribute(CasPostAuthFilter.LOCAL_TIMER_KEY);
	}

	@Override
	public void destroy() {
			
	}
	
	/**
	 * Logs the user out of the local system.  This removes all important
	 * session information that registers the user as logged in locally.
	 * 
	 * @param request the request object so we can manipulate the session
	 * @param response the response so we can clear the cookie
	 */
	private void logout(HttpServletRequest request, HttpServletResponse response) {
		logger.info("Request has invalid referrer or incorrectly stored local user.  Logging out.");
		request.getSession().removeAttribute(CtdbConstants.USER_SESSION_KEY);
		request.getSession().removeAttribute(SecuritySessionUtil.ACCOUNT_SESSION_KEY);
        Cookie delCookie = new Cookie(CtdbConstants.CTDB_USER_COOKIE, "");
        delCookie.setMaxAge(-1);
        delCookie.setSecure(true);
        response.addCookie(delCookie);
        request.getSession().invalidate();
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		logger.info("SSO Filter Active");
	}
	
	public void debugPasswordEncoding(byte[] passwd){
		if( passwd == null || passwd.length == 0 ) {
			return;
		}
		
		String pwdByteToString = passwd.toString();
		String pwdFromHashMathodsByteConvert = HashMethods.convertFromByte(passwd);
		String pwdFromHashMathodsClientHash = HashMethods.getClientHash( pwdFromHashMathodsByteConvert);
		
		logger.debug("CAS debug: pwdByteToString=" + pwdByteToString);
		logger.debug("CAS debug: pwdFromHashMathodsByteConvert=" + pwdFromHashMathodsByteConvert);
		logger.debug("CAS debug: pwdFromHashMathodsClientHash =" + pwdFromHashMathodsClientHash);
		
		try {
			String pwdEncrypted = Encryption.bytesToHex(Encryption.encrypt(pwdFromHashMathodsClientHash));
			logger.debug("CAS debug: pwdEncrypted=" + pwdEncrypted);
		}
	 	catch (Exception e){
	 		logger.error("Error while encoding password.", e);
	 	}
	}
	
}
