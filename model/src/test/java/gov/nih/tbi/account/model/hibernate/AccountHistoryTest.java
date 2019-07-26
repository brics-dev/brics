package gov.nih.tbi.account.model.hibernate;

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import gov.nih.tbi.account.model.AccountActionType;
import gov.nih.tbi.commons.model.hibernate.User;

public class AccountHistoryTest {
	
	@Test
	public void getActionTypeArgumentArrayTestSingle() {
		AccountHistory accountHistory = new AccountHistory();
		accountHistory.setActionTypeArguments("Trump, Donald");
		String expected[] = { "Trump, Donald" };
		Assert.assertEquals(accountHistory.getActionTypeArgumentArray(), expected);
	}
	
	@Test
	public void getActionTypeArgumentArrayTestMulti() {
		AccountHistory accountHistory = new AccountHistory();
		accountHistory.setActionTypeArguments("Trump, Donald | Pence, Mike| Comey, James ");
		String expected[] = { "Trump, Donald ", " Pence, Mike"," Comey, James " };
		Assert.assertEquals(accountHistory.getActionTypeArgumentArray(), expected);
	}
	
	@Test
	public void getRequestActionTextHappyTest() {
		User user = new User();
		user.setFirstName("JC");
		user.setLastName("Denton");
		AccountHistory accountHistory = new AccountHistory();
		accountHistory.setAccountActionType(AccountActionType.REQUEST);
		accountHistory.setActionTypeArguments("");
		accountHistory.setComment("Test comment");
		accountHistory.setCreatedDate(new Date());
		accountHistory.setChangedByUser(user);
		
		String accountHistoryTypeText = accountHistory.getActionTypeText(false);
		Assert.assertEquals(accountHistoryTypeText, "Account requested by Denton, JC");
	}
	
	@Test
	public void getRequestActionTextMaskTest() {
		User user = new User();
		user.setFirstName("JC");
		user.setLastName("Denton");
		AccountHistory accountHistory = new AccountHistory();
		accountHistory.setAccountActionType(AccountActionType.REQUEST);
		accountHistory.setActionTypeArguments("Denton, JC");
		accountHistory.setComment("Test comment");
		accountHistory.setCreatedDate(new Date());
		accountHistory.setChangedByUser(user);
		
		String accountHistoryTypeText = accountHistory.getActionTypeText(true);
		Assert.assertEquals(accountHistoryTypeText, "Account requested by System Admin");
	}
}
