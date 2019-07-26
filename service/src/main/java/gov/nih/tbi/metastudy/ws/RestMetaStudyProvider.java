package gov.nih.tbi.metastudy.ws;

import java.net.MalformedURLException;

import gov.nih.tbi.account.ws.RestAuthenticationProvider;

import org.apache.log4j.Logger;

/**
 * Provides an implementation for the secure and public Rest service calls of the BRICS Meta Study module.
 * 
 * Developer Note: All the rest service call functions defined in this class must call
 * RestAuthenticationProvider.ticketValid() and RestAuthenticationProvider.cleanup() at the beginning and end of the
 * function respectively to guarantee validity of the proxy ticket. Finally, the user must get the proxyTicket argument
 * (i.e. ticket=PT-3284792379842) and append it to the web service url if the user needs to be authenticated. Any ws
 * calls without a ticket will be considered anon. The user can get this formated property with
 * RestAuthenticationProvider.getTicketProperty().
 * 
 * @author jim3
 *
 */
public class RestMetaStudyProvider extends RestAuthenticationProvider {

	private static Logger logger = Logger.getLogger(RestMetaStudyProvider.class);
	protected final String restServiceUrl = "portal/ws/metastudy/metastudy";

	
    /**
     * A constructor for a rest provider in which the serverLocation provided is the domain of the web service call
     * being made. In the case that any path information other than a domain (such as /portal), that information is
     * stripped.
     * 
     * A proxyTicket provided can only be used one time. If RestMetaStudyProvider attempts to use the proxy ticket
     * multiple times, an exception is thrown. If a null or blank string is provided, then public calls may be made.
     * 
     * @param serverLocation
     * @param proxyTicket
     * @throws MalformedURLException
     */
	public RestMetaStudyProvider(String serverLocation, String proxyTicket) {
		super(serverLocation, proxyTicket);
	}

	
	/**
	 * FOR QUERY TOOL USE ONLY. MUST PASS ApplicationConstants.isWebservicesSecured() INTO THE THIRD
	 * ARG.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @param wsSecure
	 */
	public RestMetaStudyProvider(String serverLocation, String proxyTicket, boolean wsSecure) {
		super(serverLocation, proxyTicket, wsSecure);
	}
}
