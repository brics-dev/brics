package gov.nih.tbi.util;

import gov.nih.tbi.constants.ReportingConstants;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Static portal Methods
 * 
 * @author Abhishek
 * 
 */
public class ReportingRestProviderUtils {

	static Logger logger = Logger.getLogger(ReportingRestProviderUtils.class);

	/**
	 * Makes multiple calls to PortalUtils.getProxyTicket(...) to return the desired amount of tickets
	 * 
	 * @param serviceUrl
	 * @return
	 */
	public static String[] getMultipleProxyTickets(String serviceUrl, int numTickets) {

		String[] ticketArray = new String[numTickets];
		for (int i = 0; i < numTickets; i++) {
			ticketArray[i] = getProxyTicket(serviceUrl);
		}
		return ticketArray;
	}

	/**
	 * Creates and retrieves a proxy ticket that is good for one web service call. The user must specify a service that
	 * the ticket is for.
	 * 
	 * @param serviceUrl : The url that the web service call will be made to.
	 * @return A proxy ticket as a string parameter or a blank string if user is anon.
	 */
	public static String getProxyTicket(String serviceUrl) {

		// Strip trailing '/' from serviceUrl
		String url = serviceUrl + "/j_spring_cas_security_check";

		Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
		logger.debug("serviceUrl: " + serviceUrl);
		if (auth == null) {
			logger.debug("auth: null");
			return ReportingConstants.EMPTY_STRING;
		} else {
			logger.debug("auth: " + auth.toString());
		}
		// Case: User is not authenticated. Create an anon accountProvider.
		if (auth.getPrincipal() instanceof String && ((String) auth.getPrincipal()).equals("guest")) {
			logger.debug("User is unauthenticated (auth is a String value)");
			return ReportingConstants.EMPTY_STRING;
		}
		// Case: User is authenticated with form-based login. Send an unsecured webservice call.
		else if (auth instanceof UsernamePasswordAuthenticationToken) {
			logger.debug("User is authenticated with form based login.");
			return ReportingConstants.EMPTY_STRING;
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
				return ReportingConstants.EMPTY_STRING;
			}
		}

	}
	
	public static Assertion getAssertionFromSecurityContext(){
		Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
		if(auth == null){
			logger.debug("auth: null");
			return null;
		} else {
			CasAuthenticationToken casAuth = (CasAuthenticationToken) auth;
			return casAuth.getAssertion();
		}
	}
	
	public static String getOnlyProxyTicketByAssertion(String serviceUrl, Assertion assertion) {

		String url = serviceUrl + "/j_spring_cas_security_check";
		logger.debug("GetProxyTicket - for Principal: " + assertion.getPrincipal().getName());
		logger.debug("Service URL: " + url);

		String proxyTicket = assertion.getPrincipal().getProxyTicketFor(url);
		logger.debug("Proxy Ticket = " + proxyTicket + " for /j_spring_cas_security_check");

		return proxyTicket;
	}

	/**
	 * Strip away any path information after the domain of a url. The protocol (http or https) is preserved. Any port
	 * information is also retained.
	 * 
	 * @param url
	 * @return
	 */
	public static String stripToDomain(String url) {

		String[] brokenUrl = url.split("\\.", 2);

		// this check will prevent array out of bound errors
		if (brokenUrl.length < 2) {
			return url;
		}

		String[] splitDomain = brokenUrl[1].split("/");
		return brokenUrl[0] + "." + splitDomain[0] + "/";
	}

	/**
	 * Returns true if the string can be parsed into a double, false otherwise
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {

		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}
}
