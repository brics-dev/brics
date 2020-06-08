package gov.nih.nichd.ctdb.security.util;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.nih.tbi.commons.service.ServiceConstants;


public class BaseWsProvider {
	private static final Logger logger = Logger.getLogger(BaseWsProvider.class);
	private static final String CAS_SECURITY_CHECK_PATH_SEG = "j_spring_cas_security_check";

	public static final String PROXY_TICKET_QUERY_PARAM = "ticket";

	protected HttpServletRequest request;

	public BaseWsProvider() {}

	public BaseWsProvider(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	protected String getProxyTicket(String serviceUrl) {

		// Strip trailing '/' from serviceUrl
		String url = this.stripToDomain(serviceUrl) + "portal/j_spring_cas_security_check";

		Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
		logger.debug("serviceUrl: " + serviceUrl);
		if (auth == null) {
			logger.debug("auth: null");
			return ServiceConstants.EMPTY_STRING;
		} else {
			logger.debug("auth: " + auth.toString());
		}
		// Case: User is not authenticated. Create an anon accountProvider.
		if (auth.getPrincipal() instanceof String && ((String) auth.getPrincipal()).equals("guest")) {
			logger.debug("User is unauthenticated (auth is a String value)");
			return ServiceConstants.EMPTY_STRING;
		}
		// Case: User is authenticated with form-based login. Send an unsecured webservice call.
		else if (auth instanceof UsernamePasswordAuthenticationToken) {
			logger.debug("User is authenticated with form based login.");
			return ServiceConstants.EMPTY_STRING;
		}
		// Case: The user is authenticated
		else {
			if (auth instanceof CasAuthenticationToken) {
				CasAuthenticationToken casAuth = (CasAuthenticationToken) auth;
				// casAuth.getAssertion().getPrincipal().getName() to get a username from Spring Security

				logger.debug("Authenticating User: " + casAuth.getAssertion().getPrincipal().getName());
				logger.debug("Provided Service: " + url);
				logger.debug("Contacting CAS server...");
				logger.debug(Arrays.toString(Thread.currentThread().getStackTrace()));
				String token = casAuth.getAssertion().getPrincipal().getProxyTicketFor(url);
				logger.debug("Token: " + token);
				return token;
			} else {
				logger.error("auth was not of type CasAuthenticationToken");
				logger.error("auth: " + auth.toString());
				if (auth instanceof CasAssertionAuthenticationToken) {
					logger.error("auth was of type CasAssertionAuthenticationToken");
					CasAssertionAuthenticationToken casAuth = (CasAssertionAuthenticationToken) auth;
					logger.error("assertion: " + casAuth.getAssertion().toString());
					logger.error("assertion.principal: " + casAuth.getAssertion().getPrincipal().toString());
					logger.error("assertion.principal.name: " + casAuth.getAssertion().getPrincipal().getName());
				}
				return ServiceConstants.EMPTY_STRING;
			}
		}

	}

	protected String stripToDomain(String url) {

		String[] brokenUrl = url.split("\\.", 2);

		// this check will prevent array out of bound errors
		if (brokenUrl.length < 2) {
			return url;
		}

		String[] splitDomain = brokenUrl[1].split("/");
		return brokenUrl[0] + "." + splitDomain[0] + "/";
	}

	protected String compileProxiedWebserviceUrl(String baseUrl, String ticket) {
		String finalUrl = baseUrl;
		if (baseUrl.contains("?")) {
			finalUrl += "&ticket=" + ticket;
		} else {
			finalUrl += "?ticket=" + ticket;
		}
		return finalUrl;
	}
}
