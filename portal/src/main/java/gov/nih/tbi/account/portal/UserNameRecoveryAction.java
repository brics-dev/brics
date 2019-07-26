
package gov.nih.tbi.account.portal;

import java.util.Arrays;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;

/**
 * This class is the action for the recovery of usernames
 * 
 * @author Francis Chen
 */
public class UserNameRecoveryAction extends BaseAccountAction {

	private static final long serialVersionUID = -8192295582491601587L;
	private static Logger logger = Logger.getLogger(UserNameRecoveryAction.class);

	private String email;


	/**
	 * Loads the form for the user to enter his email
	 * 
	 * @return "input"
	 */
	public String input() {

		return PortalConstants.ACTION_INPUT;
	}

	/**
	 * Sends the username in an email
	 * 
	 * @return "confirm"
	 * @throws MessagingException
	 */
	public String submit() throws MessagingException {

		Account account = accountManager.getAccountByEmail(email);

		if (account == null) {
			throw new RuntimeException(
					"Account with the email not found.  Something is wrong, struts validator should've caught this.");
		}

		boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();

		if (!isLocalEnv) {
			String bodyMsgText =
					this.getText(PortalConstants.MAIL_RESOURCE_USERNAME_RECOVERY + PortalConstants.MAIL_RESOURCE_BODY,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
									modulesConstants.getModulesOrgPhone(getDiseaseId()),
									account.getUser().getFullName(), account.getUserName()));

			sendAccountEmail(account, PortalConstants.MAIL_RESOURCE_USERNAME_RECOVERY, bodyMsgText, true);
		}

		return PortalConstants.ACTION_SUCCESS;
	}
	
	public String getEmail() {

		return email;
	}

	public void setEmail(String email) {

		this.email = email;
	}

}
