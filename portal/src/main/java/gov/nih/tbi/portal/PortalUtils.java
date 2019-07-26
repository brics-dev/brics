
package gov.nih.tbi.portal;

import gov.nih.tbi.account.model.AccountUserDetails;
import gov.nih.tbi.commons.service.ServiceConstants;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Static portal Methods
 * 
 * @author Michael Valeiras
 * 
 */
public class PortalUtils
{

    static Logger logger = Logger.getLogger(PortalUtils.class);

    /**
     * Makes multiple calls to PortalUtils.getProxyTicket(...) to return the desired amount of tickets
     * 
     * @param serviceUrl
     * @return
     */
    public static String[] getMultipleProxyTickets(String serviceUrl, int numTickets)
    {

        String[] ticketArray = new String[numTickets];
        for (int i = 0; i < numTickets; i++)
        {
            ticketArray[i] = getProxyTicket(serviceUrl);
        }
        return ticketArray;
    }

    /**
     * Creates and retrieves a proxy ticket that is good for one web service call. The user must specify a service that
     * the ticket is for.
     * 
     * @param serviceUrl
     *            : The url that the web service call will be made to.
     * @return A proxy ticket as a string parameter or a blank string if user is anon.
     */
    public static String getProxyTicket(String serviceUrl)
    {

        // Strip trailing '/' from serviceUrl
        String url = stripToDomain(serviceUrl) + "portal/j_spring_cas_security_check";

        Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
        logger.debug("serviceUrl: " + serviceUrl);
        if (auth == null)
        {
            logger.debug("auth: null");
            return ServiceConstants.EMPTY_STRING;
        }
        else
        {
            logger.debug("auth: " + auth.toString());
        }
        // Case: User is not authenticated. Create an anon accountProvider.
        if (auth.getPrincipal() instanceof String && ((String) auth.getPrincipal()).equals("guest"))
        {
            logger.debug("User is unauthenticated (auth is a String value)");
            return ServiceConstants.EMPTY_STRING;
        }
        // Case: User is authenticated with form-based login. Send an unsecured webservice call.
        else
            if (auth instanceof UsernamePasswordAuthenticationToken)
            {
                logger.debug("User is authenticated with form based login.");
                return ServiceConstants.EMPTY_STRING;
            }
            // Case: The user is authenticated
            else
            {
                if (auth instanceof CasAuthenticationToken)
                {
                    CasAuthenticationToken casAuth = (CasAuthenticationToken) auth;
                    // casAuth.getAssertion().getPrincipal().getName() to get a username from Spring Security

                    logger.debug("Authenticating User: " + casAuth.getAssertion().getPrincipal().getName());
                    logger.debug("Provided Service: " + url);
                    logger.debug("Contacting CAS server...");
                    logger.debug(Arrays.toString(Thread.currentThread().getStackTrace()));
                    String token = casAuth.getAssertion().getPrincipal().getProxyTicketFor(url);
                    logger.debug("Token: " + token);
                    return token;
                }
                else
                {
                    logger.error("auth was not of type CasAuthenticationToken");
                    logger.error("auth: " + auth.toString());
                    if (auth instanceof CasAssertionAuthenticationToken)
                    {
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

    /**
     * Returns the diseaseId from the security context. Should provide the same result as the BaseAction but does not
     * store the result in session. (Used when user is stateless and there is no sessions ie. RestAccountService).
     * 
     * @return
     */
    public static Long getDiseaseId()
    {

        Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
        if (auth.getPrincipal() instanceof String && ((String) auth.getPrincipal()).equals("guest"))
        {
            return -1L;
        }
        else
        {
            return ((AccountUserDetails) auth.getPrincipal()).getDiseaseId();
        }
    }

    /**
	 * Strip away any path information after the domain of a url. The protocol (http or https) is
	 * preserved. Any port information is also retained.
	 * 
	 * @param url
	 * @return
	 */
	public static String stripToDomain(String url)
    {

        String[] brokenUrl = url.split("\\.", 2);

        // this check will prevent array out of bound errors
        if (brokenUrl.length < 2)
        {
            return url;
        }

        String[] splitDomain = brokenUrl[1].split("/");
        return brokenUrl[0] + "." + splitDomain[0] + "/";
    }


}
