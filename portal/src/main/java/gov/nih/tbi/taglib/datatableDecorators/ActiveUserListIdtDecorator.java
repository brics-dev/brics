package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.account.model.AccountUserDetails;
import gov.nih.tbi.idt.ws.IdtDecorator;
/**
 * Decorator class to extend the capability of the JSP for UI display
 * 
 * @author khanaly
 *
 */
public class ActiveUserListIdtDecorator extends IdtDecorator {
	/**
	 * Method to build the full name of the active user
	 * 
	 * @return
	 */
	public String getFullName() {
		AccountUserDetails accountUserDetails = (AccountUserDetails) this
				.getObject();
		return accountUserDetails.getAccount().getUser().getFirstName() + ' '
				+ accountUserDetails.getAccount().getUser().getLastName();

	}
	/**
	 * Method to build user email link
	 * @return
	 */
	public String getEmailLink() {
		AccountUserDetails accountUserDetails = (AccountUserDetails) this
				.getObject();
		return "<a href='mailto:"
				+ accountUserDetails.getAccount().getUser().getEmail() + "'>"
				+ accountUserDetails.getAccount().getUser().getEmail() + "</a>";
	}

}
