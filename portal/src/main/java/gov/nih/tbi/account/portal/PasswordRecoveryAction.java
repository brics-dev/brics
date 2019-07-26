
package gov.nih.tbi.account.portal;

import java.util.Arrays;
import java.util.Date;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.AccountActionType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.commons.model.RoleType;


/**
 * Defines actions for password recovery
 * 
 * @author mvalei
 */
public class PasswordRecoveryAction extends BaseAccountAction {

	private static final long serialVersionUID = 4779911634509793677L;
	private static Logger logger = Logger.getLogger(PasswordRecoveryAction.class);

	/******************************************************************************************************/


	String token;
	String casToken;
	String newPassword;
	String confirmPassword;

	// userOrEmail is the input from the enter your username or email form on the initial password recov. page
	private String userOrEmail;
	
	private AccountHistory accountHistory;
	
    public static final String  PASSWORD_RESET_REQUESTED_BY_USER ="Password reset requested by User";
	
	public static final String  PASSWORD_RESET_REQUESTED_BY_ADMIN ="Password reset requested by Admin";


	/******************************************************************************************************/

	/**
	 * Directs the user to the opening password recovery page.
	 * 
	 * @return
	 */
	public String input() {

		return PortalConstants.ACTION_INPUT;
	}

	/**
	 * Creates and sends the hash after verifying the account based on the form input 'userOrEmail'
	 * 
	 * @return
	 * @throws MessagingException
	 */
	public String submit() throws MessagingException {

		Account account = accountManager.getAccountByEmail(userOrEmail);
		if (account == null) {
			account = accountManager.getAccountByUserName(userOrEmail);
		}
		if (account == null) {
			// Validation should prevent this block from executing
			logger.warn("Could not retrieve user by username or email.");
			return PortalConstants.ACTION_ERROR;
		}

		token = accountManager.addRecoveryTokenForAccount(account);
		logger.info("PASSWORD RECOVERY GRANTED FOR: " + account.getUserName());
		logger.info("TOKEN: " + token);

		// send email to user
		String url = getRequest().getRequestURL().toString();
		url = (url.split(getRequest().getRequestURI()))[0];

		if (ModulesConstants.getWsSecured()) {
			String messageFormat =
					this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
									modulesConstants.getModulesOrgPhone(getDiseaseId()), "{0}", "{1}"))
							+ this.getText(
									PortalConstants.MAIL_RESOURCE_FORGOT_PASSWORD
											+ PortalConstants.MAIL_RESOURCE_BODY,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgPhone(getDiseaseId()), "{0}", "{1}"))
							+ this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgPhone(getDiseaseId()), "{0}", "{1}"));

			String subjectFormat =
					this.getText(PortalConstants.MAIL_RESOURCE_FORGOT_PASSWORD + PortalConstants.MAIL_RESOURCE_SUBJECT,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId())));

			String fullURL = url + PortalConstants.FORWARD_SLASH + getPortalRoot() + PortalConstants.FORWARD_SLASH;

			accountManager.sendPasswordRecoveryEmail(account, token, fullURL, subjectFormat, messageFormat);
		} else {
			// prints out the url that would have been presented in the password reovery email.
			logger.warn("An email message is not being sent because the system things its in a local enviornment.");
			logger.info(PortalConstants.FORWARD_SLASH + getPortalRoot() + PortalConstants.FORWARD_SLASH
					+ CoreConstants.RECOVERY_EMAIL_URI + token);
		}
		if(getIsAccountAdmin()) {
			
			recordPasswordResetByUserOrAdmin(account,PASSWORD_RESET_REQUESTED_BY_ADMIN,AccountActionType.PASSWORD_RESET_REQUESTED_BY_ADMIN,getAccount());
			
		} else {
			
		    recordPasswordResetByUserOrAdmin(account,PASSWORD_RESET_REQUESTED_BY_USER,AccountActionType.PASSWORD_RESET_REQUEST_BY_USER,account);
		} 
	
		return PortalConstants.ACTION_SUCCESS;
	}
	

	/**
	 * The goal of this method is to add a record in account history 
	 * when the User requests a password reset from the CAS login page or 
	 * when an Admin requests a password reset through the accounts module.
	 * **/
	
	public void  recordPasswordResetByUserOrAdmin(Account account, String commnet, AccountActionType accountActionType, Account requestedAccount){
        
	     //Record password reset by User or Admin in account history
		  accountHistory  = new AccountHistory(account,accountActionType,PortalConstants.EMPTY_STRING,
				  commnet, new Date(),requestedAccount.getUser());
		  
		  account.addAccountHistory(accountHistory);
		
		  //Save changes to the database
		  accountManager.saveAccount(account);
	
	  }
	
	/**
	 * this method will check if the user is an Admin user
	 */
	
	public boolean getIsAccountAdmin() {

		return (accountManager.hasRole(getAccount(), RoleType.ROLE_ADMIN)
				|| accountManager.hasRole(getAccount(), RoleType.ROLE_ACCOUNT_ADMIN));

	}

	/**
	 * Action sent out in email link. Directs the user to the change password page.
	 * 
	 * @return String
	 */
	public String recover() {

		setToken(getRequest().getParameter(PortalConstants.TOKEN));
		setCasToken(getRequest().getParameter(PortalConstants.CAS_TOKEN));
		if (getCasToken() == null) {
			logger.error("Bad argument " + PortalConstants.CAS_TOKEN + " in request passwordRecovery!recover");
			return PortalConstants.ACTION_ERROR;
		}

		return PortalConstants.ACTION_INPUT;
	}

	/**
	 * Action for changing the password through final password recovery page
	 * 
	 * @return
	 * @throws MessagingException
	 */
	public String update() throws MessagingException {

		setCasToken(getRequest().getParameter(PortalConstants.CAS_TOKEN));
		if (!getRequest().getRequestURI().startsWith(
				"/" + getPortalRoot() + "/publicAccounts/submitpasswordRecoveryValidationAction!update.action")) {
			logger.error("Request did not go through validation. URI was:" + getRequest().getRequestURI());
			return PortalConstants.ACTION_ERROR;
		}

		// Get the user's account based on the userOrEmail field
		Account account = accountManager.getAccountByEmail(userOrEmail);
		if (account == null) {
			account = accountManager.getAccountByUserName(userOrEmail);
		}
		if (account == null) {
			logger.warn("Could not retreve user by username or email.");
			return PortalConstants.ACTION_ERROR;
		}

		account.setRecoveryDate(null);

		// undo all locks on this account
		account.setIsLocked(false);
		account.setLockedUntil(null);
		
		accountManager.changePassword(account, newPassword);

		// Send password change success email
		String bodyMsgText =
				this.getText(PortalConstants.MAIL_RESOURCE_FORGOT_PASSWORD_SUCCESS + PortalConstants.MAIL_RESOURCE_BODY,
						Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
								modulesConstants.getModulesOrgPhone(getDiseaseId()), "{0}", "{1}"));

		sendAccountEmail(account, PortalConstants.MAIL_RESOURCE_FORGOT_PASSWORD_SUCCESS, bodyMsgText, true);

		return PortalConstants.ACTION_SUCCESS;
	}

	
	/******************************************************************************************************/

	public void setToken(String token) {

		this.token = token;
	}

	public String getToken() {

		return token;
	}

	public void setNewPassword(String newPassword) {

		this.newPassword = newPassword;
	}

	public String getNewPassword() {

		return newPassword;
	}

	public void setConfirmPassword(String confirmPassword) {

		this.confirmPassword = confirmPassword;
	}

	public String getConfirmPassword() {

		return confirmPassword;
	}

	public String getUserOrEmail() {

		return userOrEmail;
	}

	public void setUserOrEmail(String userOrEmail) {

		if (userOrEmail == null) {
			userOrEmail = "";
		}

		this.userOrEmail = userOrEmail;
	}

	public String getCasToken() {

		return casToken;
	}

	public void setCasToken(String casToken) {

		this.casToken = casToken;
	}

}
