package gov.nih.tbi.account.ws.cxf;

import java.io.UnsupportedEncodingException;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.ws.cxf.StatelessRestProvider;

public class StatelessAccountRestProvider extends StatelessRestProvider {
	private static Logger logger = Logger.getLogger(StatelessAccountRestProvider.class);

	private final String ACCOUNT_SERVICE_URL = "portal/ws/stateless/account/";

	public StatelessAccountRestProvider(String serverUrl, String userName, String password) {
		super(serverUrl, userName, password);
	}

	public Account getAccountByUserName(String userToGet) {

		if (this.getUserName() == null) {
			return null;
		}

		String webString = null;

		try {
			webString = this.getServerUrl() + ACCOUNT_SERVICE_URL + "user/" + encodeUrlParam(userToGet);
			logger.info("Account service url: " + webString);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, this.getUserName());
		client.header(PASS, this.getPassword());
		Account account = (Account) client.accept("text/xml").get(Account.class);
		return account;
	}
}
