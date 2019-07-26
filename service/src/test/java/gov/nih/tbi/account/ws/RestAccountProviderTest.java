
package gov.nih.tbi.account.ws;

import java.io.UnsupportedEncodingException;

import org.testng.Assert;
import org.testng.annotations.Test;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.RestAccountProvider;

public class RestAccountProviderTest {

	@Test
	public void accountGetterTest() {
		String accountUrl = "http://fitbir-portal-local.cit.nih.gov:8080/portal/ws";
        RestAccountProvider restProvider = new RestAccountProvider(accountUrl, null);

		try {
            Account acc = restProvider.getAccount();
			Assert.assertFalse((acc.getUserName() == null) || acc.getUserName().isEmpty(),
					"The account username is empty.");
        }
		catch (UnsupportedEncodingException e) {
			Assert.fail("Couldn't get account.", e);
        }
	}

}
