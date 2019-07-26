package gov.nih.tbi.ws.cxf;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.jasig.cas.client.validation.Assertion;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.constants.ReportingConstants;
import gov.nih.tbi.reporting.authentication.AccountUserDetails;
import gov.nih.tbi.util.ReportingRestProviderUtils;
import gov.nih.tbi.ws.provider.RestReportingAccountProvider;

/**
 * A parent class for Rest web service implementations. Contains common methods
 * 
 * @author Abhishek Srivastava
 * 
 */
public class ReportingBaseRestService {

	private static Logger logger = Logger.getLogger(ReportingBaseRestService.class);

	@Autowired
	protected ApplicationConstants applicationConstants;

	protected static final String ANONYMOUS_USER_NAME = "anonymous";
	protected static final String ADMIN_USER_NAME = "administrator";

	/**
	 * Returns the account of the authenticated user who is making the web service call. If the web-service call is
	 * anonymous or if the there is a problem then the default ANONYMOUS account is returned.
	 * 
	 * If the web service call is anonymous, but the service does not have access to the account db (is not the
	 * AccountRestService), then an anonymous ws call will be made to the AccountRestService to
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException 
	 */
	protected Account getAuthenticatedAccount() throws UnsupportedEncodingException, UnauthorizedException {

		// If the web services are set to not be secured (local env only), then we are going to load in the
		// administrative user.
		// TODO: this makes it difficult to test other users in local
		if (!ApplicationConstants.isWebservicesSecured()) {
			logger.info("Web services are not secured by CAS in this instance (local env).");
			String accUrl = applicationConstants.getModulesAccountURL();

			RestReportingAccountProvider accountProvider = new RestReportingAccountProvider(accUrl, ReportingRestProviderUtils.getProxyTicket(accUrl));
			Account account = accountProvider.getUserAccountByUserName(ADMIN_USER_NAME, applicationConstants.getAccountWebServiceURL());
			logger.info("WS not handled in account module. Fetching with web service call. Username: "
					+ account.getUserName() + " and disease key: " + account.getDiseaseKey());
			return account;
		} else {
			logger.info("Web services are secured by CAS in this instance.");
			
			Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
			CasAuthenticationToken casAuth = (CasAuthenticationToken) auth;
			Account requestingAccount = ((AccountUserDetails) auth.getPrincipal()).getAccount();
			logger.info("This is the user accout information " + requestingAccount.getUserName() );
			logger.info("Is the user authenticated? " + casAuth.isAuthenticated());
			/*if(requestingAccount.getUserName().equalsIgnoreCase(ANONYMOUS_USER_NAME) || requestingAccount.getUserName().equalsIgnoreCase("guest")){
				throw new UnauthorizedException();
			}*/
			return requestingAccount;
		}

	}

	public static String decodeUrl(String url) throws UnsupportedEncodingException {

		if (url == null) {
			return null;
		}

		return URLDecoder.decode(url, "UTF-8");
	}
	
	protected String appendProxyTicket(String urlString, Assertion assertion) {
		String ticketArg = getProxyTicket(assertion);
		if (ticketArg != null && !ticketArg.isEmpty()) {
			urlString = urlString + "&" + ticketArg;
		}

		return urlString;
	}
	
	
	protected String getProxyTicket(Assertion assertion) {
		if (!ApplicationConstants.isWebservicesSecured()) {
			return ReportingConstants.EMPTY_STRING;
		}

		String url = applicationConstants.getModulesAccountURL()
				+ "/j_spring_cas_security_check";

		logger.info("GetProxyTicket - for Principal: "
				+ assertion.getPrincipal().getName());

		logger.info("Service URL: " + url);

		// Not really sure how this works, talk to Mike V
		String proxyTicket = assertion.getPrincipal().getProxyTicketFor(url);

		logger.info("Proxy Ticket = " + proxyTicket
				+ " for /j_spring_cas_security_check");

		return "ticket=" + proxyTicket;
	}

	protected boolean isSuccessfulResponse(int responseCode) {
		return responseCode >= 200 && responseCode < 300;
	}
	
	protected Response respondEmptyOk() {
		return Response.ok("{}", MediaType.APPLICATION_JSON).build();
	}
}
