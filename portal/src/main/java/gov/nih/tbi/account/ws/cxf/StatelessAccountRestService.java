package gov.nih.tbi.account.ws.cxf;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AccountRestService;
import gov.nih.tbi.account.ws.model.UserLogin;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.ws.HashMethods;

@Component(value = "statelessAccountRestServiceBean")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Path("/")
public class StatelessAccountRestService extends AccountRestService {

	private static Logger logger = Logger.getLogger(StatelessAccountRestService.class);

	@Autowired
	private AccountManager accountManager;

	protected static final String ANONYMOUS_USER_NAME = "anonymous";
	protected static final String ADMIN_USER_NAME = "administrator";

	Account requestedAccount;

	/**
	 * Returns the account of a user who authenticating themselves using the credential supplied through the JNLP. If
	 * the credentials supplied are not correct, then null is returned.
	 * 
	 * @param userName
	 * @param password
	 * @return
	 * @throws Exception 
	 */
	protected Account getRequestedAccount(String userName, String password) {

		UserLogin user = new UserLogin(userName, HashMethods.getClientHash(userName),
				HashMethods.getClientHash(HashMethods.getServerHash(userName), password));
		
		if (user.getUserName().equals("anonymous")) {
			return null;
		}

		Account account = accountManager.getAccountByUserName(user.getUserName());

		logger.debug("getting requested account: " + account);

		if (account == null) {
			throw new RuntimeException("User with name " + user.getUserName() + " does not exist.");
		}

		String acctPassword = HashMethods.convertFromByte(account.getPassword());
		
		if ((HashMethods.validateClientHash(user.getHash1(), user.getUserName())) && (HashMethods
				.validateClientHash(user.getHash2(), HashMethods.getServerHash(user.getUserName()), acctPassword))) {
			return account;
		}

		throw new RuntimeException("Username and password does not match.");
	}

	protected Account getAuthenticatedAccount() {

		return requestedAccount;
	}

	/**************************************************************************************************************************************
	 * 
	 * 
	 * Webstart Rest Services
	 * 
	 * 
	 *************************************************************************************************************************************/

	@GET
	@Path("user/{username}")
	@Produces("text/xml")
	public Account getByUserName(@PathParam("username") String userToGet, @HeaderParam("userName") String userName,
			@HeaderParam("pass") String password) throws UnsupportedEncodingException {

		this.requestedAccount = getRequestedAccount(userName, password);
		return getByUserName(userToGet);
	}
}
