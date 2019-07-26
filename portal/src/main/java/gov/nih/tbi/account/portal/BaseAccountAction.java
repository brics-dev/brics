
package gov.nih.tbi.account.portal;

import java.util.Arrays;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.account.model.SessionAccountEdit;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.service.StaticReferenceManager;

public class BaseAccountAction extends BaseAction {
	protected static final Logger accountsLog = Logger.getLogger("accountsLogger");
	private static final long serialVersionUID = -3999842245732220149L;

	/***************************************************/

	@Autowired
	protected StaticReferenceManager staticManager;

	@Autowired
	protected SessionAccountEdit sessionAccountEdit;

	/***************************************************/

	public SessionAccount getSessionAccount() {

		if (sessionAccount == null) {
			sessionAccount = new SessionAccount();
		}

		return sessionAccount;
	}

	public SessionAccountEdit getSessionAccountEdit() {

		if (sessionAccountEdit == null) {
			sessionAccountEdit = new SessionAccountEdit();
		}

		return sessionAccountEdit;
	}

	public void sendAccountEmail(Account account, String subjectMsg, String bodyMsgText, boolean includeHeaderFooter)
			throws MessagingException {

		String subject = this.getText(subjectMsg + PortalConstants.MAIL_RESOURCE_SUBJECT,
				Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId())));

		if (includeHeaderFooter) {
			StringBuilder bodyTextBuilder = new StringBuilder();
			bodyTextBuilder
					.append(this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
									modulesConstants.getModulesOrgPhone(getDiseaseId()),
									account.getUser().getFullName(), getSessionAccountEdit().getReason())));
			bodyTextBuilder.append(bodyMsgText);

			bodyTextBuilder
					.append(this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
									modulesConstants.getModulesOrgPhone(getDiseaseId()),
									account.getUser().getFullName(), getSessionAccountEdit().getReason())));

			bodyMsgText = bodyTextBuilder.toString();
		}

		accountManager.sendEmail(account, subject, bodyMsgText);
	}

}
