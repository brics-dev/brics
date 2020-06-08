package gov.nih.nichd.ctdb.security.common;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.util.CommonUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.tbi.account.model.AccountUserDetails;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.service.complex.BricsAbstractAuthenticationProcessingFilter;

public class ProformsCasAuthenticationFilter extends BricsAbstractAuthenticationProcessingFilter {

    /**
     * See Spring Security implementation of CasAuthenticationFilter for javadoc
     */
    public static final String CAS_STATEFUL_IDENTIFIER = "_cas_stateful_";

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    public static final String CAS_STATELESS_IDENTIFIER = "_cas_stateless_";

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    private String proxyReceptorUrl;

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    private ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    private String artifactParameter = ServiceProperties.DEFAULT_CAS_ARTIFACT_PARAMETER;
	
    // ~ Constructors
    // ===================================================================================================
    
	protected ProformsCasAuthenticationFilter() {
		super("/j_spring_cas_security_check");
	}
	
	// ~ Methods
    // ========================================================================================================
	
    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response)
            throws AuthenticationException, IOException {

		logger.debug("AUTH | Start!");
        // if the request is a proxy request process it and return null to indicate the request has been processed
        if (proxyReceptorRequest(request)) {
			logger.debug(
					"AUTH | Request is a PGT/IOU pair sent from CAS to proxy receptor URL. See CommonUtils debug info to see pair.");
            CommonUtils.readAndRespondToProxyReceptorRequest(request, response, this.proxyGrantingTicketStorage);
			logger.debug("     | PGT recieved: " + request.getParameter("pgtId"));
            return null;
        }

        final boolean serviceTicketRequest = serviceTicketRequest(request, response);
        String username = serviceTicketRequest ? CAS_STATEFUL_IDENTIFIER : CAS_STATELESS_IDENTIFIER;

        String password = obtainArtifact(request);

        if (password == null) {
			logger.error("AUTH | CAS service request did not contain a service ticket parameter. ");
            password = "";
        }
		logger.debug("AUTH | Created UsernamePasswordAuthenticationToken for validation by providers.");
		logger.debug("     | username: " + username);
		logger.debug("     | credentials (aka service ticket): " + password);

		final UsernamePasswordAuthenticationToken authRequest =
				new UsernamePasswordAuthenticationToken(username, password);
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    protected String obtainArtifact(HttpServletRequest request) {

        return request.getParameter(artifactParameter);
    }

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    @Override
    protected boolean requiresAuthentication(final HttpServletRequest request, final HttpServletResponse response) {
        final boolean serviceTicketRequest = serviceTicketRequest(request, response);
		final boolean prr = proxyReceptorRequest(request);
		final boolean ptr = proxyTicketRequest(serviceTicketRequest, request);
		final boolean result = serviceTicketRequest || prr || ptr;

		// debug all calls except the logging checks (which occur along with every page load)
		if (logger.isDebugEnabled() && !request.getRequestURI().endsWith("loginCheck.ajax")) {

			logger.debug("AUTH | NEW REQUEST. CHECK TYPES: " + request.getRequestURI());
			logger.debug("     | Domain: " + request.getLocalName());
			logger.debug(
					"     | Service Ticket Request (url is /j_spring_cas_security_check)?: " + serviceTicketRequest);
			logger.debug("     | Proxy Receptor Request (url is /j_spring_cas_proxy_callback)?: " + prr);
			logger.debug("     | PT Request (ST arg but url not /j_spring_cas_security_check)?: " + ptr);
			logger.debug("     |                                                               -------");
			logger.debug("     | Initiate CAS Authentication workflow? (any of the above true): " + result);
        }

        return result;
    }

    public final void setProxyReceptorUrl(final String proxyReceptorUrl) {

        this.proxyReceptorUrl = proxyReceptorUrl;
    }

    public final void setProxyGrantingTicketStorage(final ProxyGrantingTicketStorage proxyGrantingTicketStorage) {

        this.proxyGrantingTicketStorage = proxyGrantingTicketStorage;
    }

    public final void setServiceProperties(final ServiceProperties serviceProperties) {

        this.artifactParameter = serviceProperties.getArtifactParameter();
    }

    /**
     * Indicates if the request is eligible to process a service ticket. This method exists for readability.
     * 
     * @param request
     * @param response
     * @return
     */
    private boolean serviceTicketRequest(final HttpServletRequest request, final HttpServletResponse response) {

        boolean result = super.requiresAuthentication(request, response);
        return result;
    }

    /**
     * Indicates if the request is eligible to process a proxy ticket.
     * 
     * @param request
     * @return
     */
    private boolean proxyTicketRequest(final boolean serviceTicketRequest, final HttpServletRequest request) {

        if (serviceTicketRequest) {
            return false;
        }
        final boolean result = true && obtainArtifact(request) != null && !authenticated();
        return result;
    }

    /**
     * Determines if a user is already authenticated.
     * 
     * @return
     */
    private boolean authenticated() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    /**
     * Indicates if the request is eligible to be processed as the proxy receptor.
     * 
     * @param request
     * @return
     */
    private boolean proxyReceptorRequest(final HttpServletRequest request) {

        final String requestUri = request.getRequestURI();
        final boolean result = proxyReceptorConfigured() && requestUri.endsWith(this.proxyReceptorUrl);
        return result;
    }

    /**
     * Determines if the {@link CasAuthenticationFilter} is configured to handle the proxy receptor requests.
     * 
     * @return
     */
    private boolean proxyReceptorConfigured() {

        final boolean result = this.proxyGrantingTicketStorage != null && !CommonUtils.isEmpty(this.proxyReceptorUrl);
        return result;
    }

    /**
     * This overridden successfulAuthenticaiton contains a filter chain so the chain can continue after the successful
     * authentication of a proxy web service. NOTE: The class AbstractAuthenticationProcesssingFilter (this class'
     * parent) had to be overridden to include the parent of this function due to proxying not being supported in our
     * spring secuirty cas version.
     */
    @Override
    protected final void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, Authentication authResult) throws IOException, ServletException {
        AccountUserDetails usrDetails;
        
        if(authResult instanceof CasAuthenticationToken) {
            CasAuthenticationToken casAuthResult = (CasAuthenticationToken)authResult;
            usrDetails = (AccountUserDetails)casAuthResult.getUserDetails();
            String username = usrDetails.getUsername();
            
            // start the local timeout timer
            setLocalTimer(request);
            
            User user = null;
			SecurityManager sm = new SecurityManager();
			try {
				user = sm.ssoAuthenticate(username, request);
			}
			catch (AuthenticationFailedException e) {
				user = null;
				logger.info("Username not found in the usr table. Preping for new user creation.");
			}
			catch(CtdbException e) {
				user = null;
				logger.error("Unable to connect to proforms database. Erroring");
				throw new IOException("Unable to access proforms database", e);
			}
			
			// already have the account, so check if User exists
			Account account = usrDetails.getAccount();
			try {
				// verifies user details if user already in db, otherwise creates user in db
		    	if (user == null) {
					logger.info("Begin new user creation...");

					// don't need to check that the Account exists or has ProFoRMS access here.
					// Spring Security already did that
					// save the user's information into the database
					User newUser = new User(account);
					
					logger.debug("New user has bricsId: " + newUser.getBricsUserId() + " and username: " + newUser.getUsername());
					
					sm.createUser(newUser);
					
					logger.debug("New user has usrid: " + newUser.getId());
					logger.debug("New user added: " + username);
					logger.info("Attempting new user authentication...");
					
					user = sm.ssoAuthenticate(username, request);
					
					logger.debug("User data after authentication:");
					logger.debug("username: " + user.getUsername() + " brics ID: " + user.getBricsUserId() + " usrid: " + user.getId());
				}
				else {
					logger.info("User exists in system, update info from BRICS account web service...");
					// don't need to check that the Account exists or has ProFoRMS access here.
					// Spring Security already did that
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
	            
				SecuritySessionUtil.refreshUserProtocols(user, request);
				SecuritySessionUtil.setupUserBricsPermissions(account, request);
				request.getSession().setAttribute(SecuritySessionUtil.ACCOUNT_SESSION_KEY, account);
				request.getSession().setAttribute(CtdbConstants.USER_SESSION_KEY, user);
				
	            Cookie cookie = new Cookie(CtdbConstants.CTDB_USER_COOKIE, user.getUsername());
	            cookie.setSecure(true);
	            response.addCookie(cookie);
			}
			catch(Exception e) {
				logger.error("Failed login.  Redirecting to the SSO login failed page.", e);
	            
	            // Logout the user to invalidate session and cookie.
	            logout(request, response);
	            response.sendRedirect(request.getContextPath() + "/security/ssofailed.jsp");
	            return;
			}
        }

        boolean continueFilterChain = proxyTicketRequest(serviceTicketRequest(request, response), request);
        if (!continueFilterChain) {
            super.successfulAuthentication(request, response, chain, authResult);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Authentication success. Updating SecurityContextHolder to contain: " + authResult);
        }

        SecurityContextHolder.getContext().setAuthentication(authResult);

        // Fire event
        if (this.eventPublisher != null) {
            eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }

        chain.doFilter(request, response);
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
    
	/**
	 * Sets the local timeout metric first point.  Used by the CAS integration code to determine
	 * time elapsed since last CAS server hit.
	 * 
	 * @param request the request, so we can get the session
	 */
	public static void setLocalTimer(HttpServletRequest request) {
		request.getSession().setAttribute(CasPostAuthFilter.LOCAL_TIMER_KEY, new Long(System.currentTimeMillis()));
	}
}
