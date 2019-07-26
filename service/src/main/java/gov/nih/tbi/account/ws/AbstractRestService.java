
package gov.nih.tbi.account.ws;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.nih.tbi.ModulesConstants;
//import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.AccountUserDetails;
import gov.nih.tbi.account.model.hibernate.Account;
//import gov.nih.tbi.account.ws.RestAccountProvider;
//import gov.nih.tbi.account.ws.cxf.AccountRestService;
import gov.nih.tbi.commons.service.AccountManager;

/**
 * A parent class for Rest web service implementations. Contains common methods
 * 
 * @author mvalei
 * 
 */
public class AbstractRestService {

	private static Logger logger = Logger.getLogger(AbstractRestService.class);

	@Autowired
	protected ModulesConstants modulesConstants;

	@Autowired
	protected AccountManager accountManager;

	protected static final String ANONYMOUS_USER_NAME = "anonymous";
	protected static final String ADMIN_USER_NAME = "administrator";

	/**
	 * Returns the account of the authenticated user who is making the web
	 * service call. If the web-service call is anonymous or if the there is a
	 * problem then the default ANONYMOUS account is returned.
	 * 
	 * If the web service call is anonymous, but the service does not have
	 * access to the account db (is not the AccountRestService), then an
	 * anonymous ws call will be made to the AccountRestService to
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	protected Account getAuthenticatedAccount() throws UnsupportedEncodingException {

		// If the web services are set to not be secured (local env only), then
		// we are going to load in the
		// administrative user.
		// TODO: this makes it difficult to test other users in local
		if (!ModulesConstants.getWsSecured()) {
			logger.debug("Web services are not secured by CAS in this instance (local env).");
			Boolean accountsActive = modulesConstants.getModulesAccountEnabled();
			if (accountsActive != null && accountsActive.equals(true)) {
				Account account = accountManager.getAccountByUserName(ADMIN_USER_NAME);
				account.setDiseaseKey(getDiseaseId().toString());
				logger.debug("WS handled in account module. Fetching user: " + account.getUserName()
						+ " with disease key: " + account.getDiseaseKey());
				return account;
			} else {
				String accUrl = modulesConstants.getModulesAccountURL();
				RestAccountProvider accountProvider = new RestAccountProvider(accUrl, null);
				Account account = accountProvider.getByUserName(ADMIN_USER_NAME);
				account.setDiseaseKey(getDiseaseId().toString());
				logger.debug("WS not handled in account modeule. Fetching with web service call. Username: "
						+ account.getUserName() + " and disease key: " + account.getDiseaseKey());
				return account;
			}
		}
		logger.debug("Web services are secured by CAS in this instance.");
		Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
		// Case where no user is logged in (published data element and fs
		// search)
		if (auth.getPrincipal() instanceof String && ((String) auth.getPrincipal()).equals("guest")) {
			// If restService is account, then get the anonymous account, else,
			// make a ws call to account to get the
			// anonymous account.
			if (AccountRestService.class.isAssignableFrom(this.getClass())) {
				Account account = accountManager.getAccountByUserName(ANONYMOUS_USER_NAME);
				logger.debug("WS handled in account module. Fetching user: " + account.getUserName()
						+ " with disease key: " + account.getDiseaseKey());
				return account;
			} else {
				String accUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
				RestAccountProvider accountProvider = new RestAccountProvider(accUrl, null);
				Account account = accountProvider.getByUserName(ANONYMOUS_USER_NAME);
				if (account.getDiseaseKey() == null || account.getDiseaseKey().equals(StringUtils.EMPTY)) {
					logger.debug("Disease key is null or blank. Setting manually.");
					account.setDiseaseKey(getDiseaseId().toString());
				}
				logger.debug("WS not handled in account modeule. Fetching with web service call. Username: "
						+ account.getUserName() + " and disease key: " + account.getDiseaseKey());
				return account;
			}
		} else {
			Account account = ((AccountUserDetails) auth.getPrincipal()).getAccount();
			logger.debug("CAS authenticated user is " + account.getUserName() + " with disease key: "
					+ account.getDiseaseKey());
			return account;
		}

	}

	/**
	 * For authenticated web service calls, returns the disease of the current
	 * user. WARNING: Unauthenticated web service calls will return -1 (default)
	 * disease type!
	 * 
	 * @return
	 */
	protected Long getDiseaseId() {

		Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
		if (auth == null) {
			logger.debug("auth token is null. This should only occur in local enviornments.");
		} else {
			logger.debug("Disease fetching from inside a web-service: " + auth.toString());
		}
		Long diseaseId = null;
		if (auth == null || (auth.getPrincipal() instanceof String && ((String) auth.getPrincipal()).equals("guest"))) {
			logger.debug("User is not authenticated. Using default.");
			diseaseId = -1L;
		} else {
			diseaseId = ((AccountUserDetails) auth.getPrincipal()).getDiseaseId();
		}
		logger.debug("Disease key is: " + diseaseId);
		return diseaseId;
	}

	protected String encodeByteArrayToBase64(byte[] data, String fileName) {
		String fileBytesEncoded = new String(Base64.getEncoder().encode(data));
		MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
		String mimeTypeOfFile = mimetypesFileTypeMap.getContentType(fileName);
		JSONObject imageJsonWrapper = new JSONObject();
		imageJsonWrapper.put("msgType", fileBytesEncoded);
		return "data:image/" + mimeTypeOfFile + ";base64," + fileBytesEncoded;
	}

	public static String decodeUrl(String url) throws UnsupportedEncodingException {

		if (url == null) {
			return null;
		}

		return URLDecoder.decode(url, "UTF-8");
	}
}