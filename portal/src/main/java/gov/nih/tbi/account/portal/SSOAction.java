
package gov.nih.tbi.account.portal;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.AccountDetailsForm;
import gov.nih.tbi.account.model.AccountPrivilegesForm;
import gov.nih.tbi.account.model.SiteMinderHeader;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.BasicAccount;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.Country;
import gov.nih.tbi.commons.model.hibernate.State;
import gov.nih.tbi.commons.model.hibernate.User;

/**
 * Action for SSO based on AccountAction and used for editAccountDetails.jsp
 * 
 * @author Nimesh Patel
 */
public class SSOAction extends BaseAccountAction {

	private static final long serialVersionUID = 1721607734875667152L;
	static Logger logger = Logger.getLogger(SSOAction.class);

	/******************************************************************/

	private static final String springSecurityAnonymousUser = "guest";

	private AccountDetailsForm accountDetailsForm;
	private AccountPrivilegesForm accountPrivilegesForm;

	private Account currentAccount;
	private String availability;
	private List<BasicAccount> accountList;

	private HttpServletRequest request;
	private HttpServletResponse response;
	private SiteMinderHeader headerInfo = null;
	private String redirectURL;

	/*******************************************************************/

	/**
	 * Called from editAccountDetails.jsp Gets header information and prepopulates form
	 */
	public String register() {

		getSessionAccountEdit().clearAll();
		currentAccount = new Account();

		// checks if user is trying to request an account
		currentAccount.setAccountStatus(AccountStatus.REQUESTED);
		currentAccount.setApplicationDate(new Date());

		if (request != null && request.getHeader(SiteMinderHeader.HEADER_SM_USER) != null) {
			setHeaderInfo(new SiteMinderHeader(request));

			// Will get all this from response header from SSO
			User user = new User();
			user.setFirstName(headerInfo.getUser_firstname());
			user.setLastName(headerInfo.getUser_lastname());
			user.setEmail(headerInfo.getUser_email());
			currentAccount.setUser(user);
			currentAccount.setUserName(headerInfo.getSm_user() + RandomStringUtils.randomNumeric(3));
			currentAccount.setEraId(headerInfo.getUser_upn());
			currentAccount.setAffiliatedInstitution(headerInfo.getUser_org());
			currentAccount.setPhone(headerInfo.getUser_telephone());
		}

		saveSession();

		return PortalConstants.ACTION_INPUT;
	}

	/**
	 * Called from login.jsp Checks to see if user was authenticated and redirects them appropriately
	 */
	public String ssoLogin() {

		this.clearActionErrors();

		String username = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		// Get user name
		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername();
		} else {
			username = principal.toString();
		}

		// Check to see if user is logged in, if not redirect them back to login.jsp
		if (username.equals(springSecurityAnonymousUser)) {
			logger.debug("Redirecting " + springSecurityAnonymousUser + " to Login");
			this.addActionError(this.getText("errors.sso"));
			return PortalConstants.ACTION_ERROR;
		} else {

			// Redirect to URL user was tyring to go to if it exists, if not go to home
			SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
			String savedURL = null;

			if (savedRequest != null) {
				savedURL = savedRequest.getRedirectUrl();
			}

			if (savedURL != null) {
				redirectURL = savedURL;
			} else {
				redirectURL = "../";
			}

			logger.debug("WILL REDIRECT TO - : " + redirectURL);
			return PortalConstants.ACTION_REDIRECT;
		}

	}

	/**
	 * Used to show header information
	 */
	public String requestInfo() {

		if (request != null && request.getHeader(SiteMinderHeader.HEADER_SM_USER) != null) {
			setHeaderInfo(new SiteMinderHeader(request));
		}
		return PortalConstants.ACTION_VIEW;
	}

	/**
	 * Save account form data into session.
	 */
	public void saveSession() {

		if (currentAccount == null) {
			currentAccount = getSessionAccountEdit().getAccount();
		}

		accountDetailsForm = new AccountDetailsForm(currentAccount);
		accountPrivilegesForm = new AccountPrivilegesForm(currentAccount);

		getSessionAccountEdit().setAccount(currentAccount);
	}

	/************************ Getters/Setters *****************************/

	public void setServletRequest(HttpServletRequest request) {

		this.request = request;
	}

	public HttpServletRequest getServletRequest() {

		return request;
	}

	public void setServletResponse(HttpServletResponse response) {

		this.response = response;
	}

	public HttpServletResponse getServletResponse() {

		return response;
	}

	/**
	 * @return the headerInfo
	 */
	public SiteMinderHeader getHeaderInfo() {

		return headerInfo;
	}

	/**
	 * @param headerInfo the headerInfo to set
	 */
	public void setHeaderInfo(SiteMinderHeader headerInfo) {

		this.headerInfo = headerInfo;
	}

	public AccountDetailsForm getAccountDetailsForm() {

		return accountDetailsForm;
	}

	public boolean getIsAccountActive() {

		return (getSessionAccountEdit().getAccount().getAccountStatus() == AccountStatus.ACTIVE);
	}

	public boolean getIsAccountRejected() {

		return (getSessionAccountEdit().getAccount().getAccountStatus() == AccountStatus.DENIED
				|| getSessionAccountEdit().getAccount().getAccountStatus() == AccountStatus.INACTIVE);
	}

	public Account getCurrentAccount() {

		return getSessionAccountEdit().getAccount();
	}

	public void setCurrentAccount(Account currentAccount) {

		this.currentAccount = currentAccount;
	}

	public AccountPrivilegesForm getAccountPrivilegesForm() {

		return accountPrivilegesForm;
	}

	public void setAccountPrivilegesForm(AccountPrivilegesForm accountPrivilegesForm) {

		this.accountPrivilegesForm = accountPrivilegesForm;
	}

	public void setAccountDetailsForm(AccountDetailsForm accountDetailsForm) {

		this.accountDetailsForm = accountDetailsForm;
	}

	public boolean getEnforceStaticFields() {

		return false;
	}

	public boolean getIsRequest() {

		if (getSessionAccountEdit().getAccount().getId() == null) {
			return true;
		} else {
			return false;
		}
	}

	public List<State> getStateList() {

		List<State> stateList = staticManager.getStateList();
		return stateList;
	}

	public List<Country> getCountryList() {

		return staticManager.getCountryList();
	}

	/**
	 * Returns true is namespace is 'accounts' or 'publicAccounts'
	 */
	public boolean getInAccounts() {

		return (PortalConstants.NAMESPACE_ACCOUNTS.equals(getNameSpace())
				|| PortalConstants.NAMESPACE_PUBLICACCOUNTS.equals(getNameSpace()));
	}

	/**
	 * @return true if in namespace /sso or jsp/sso
	 */
	public boolean getInSSO() {

		return (PortalConstants.NAMESPACE_SSO.equals(getNameSpace())
				|| PortalConstants.NAMESPACE_JSP_SSO.equals(getNameSpace()));
	}

	/**
	 * Returns true if namespace is 'admin'
	 */
	public boolean getInAdmin() {

		return PortalConstants.NAMESPACE_ADMIN.equals(getNameSpace());
	}

	public RoleType[] getRoleList() {

		return RoleType.values();
	}

	public RoleStatus[] getRoleStatus() {

		return RoleStatus.values();
	}

	public Long getAccountId() {

		return getSessionAccountEdit().getAccount().getId();
	}

	public String getAvailability() {

		return availability;
	}

	public void setAvailability(String availability) {

		this.availability = availability;
	}

	public Long getUserId() {

		return getSessionAccountEdit().getAccount().getUserId();
	}

	public List<BasicAccount> getAccountList() {

		return accountList;
	}

	public void setAccountList(List<BasicAccount> accountList) {

		this.accountList = accountList;
	}

	public String getRedirectURL() {

		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {

		this.redirectURL = redirectURL;
	}

	/**
	 * Builds a list of affiliated institutions to be used by the JQUERY auto-complete field.
	 * 
	 * @return - a list of affiliated institutions in the following format: "[ \"X\", \"Y\", ... \"N\" ]"
	 */
	public String getAffiliatedInstitutionList() {

		List<String> affiliatedInstitutions = accountManager.getAffiliatedInstitutions();
		StringBuilder builder = new StringBuilder("[ ");

		for (int i = 0; i < affiliatedInstitutions.size(); i++) {
			String currentAffiliatedInstitution = affiliatedInstitutions.get(i);
			if (i != 0) {
				builder.append(PortalConstants.COMMA + PortalConstants.WHITESPACE);
			}
			builder.append(PortalConstants.QUOTE);
			builder.append(currentAffiliatedInstitution);
			builder.append(PortalConstants.QUOTE);
		}

		builder.append(" ]");

		return builder.toString();
	}

}
