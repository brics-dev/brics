package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.account.model.AccountUserDetails;
/**
 * Decorator class to extend the capability of the JSP for UI display
 * 
 * @author khanaly
 *
 */
public class ActiveUserListDecorator extends Decorator {
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
