package gov.nih.brics.auth.security;

import java.util.Collection;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import gov.nih.brics.auth.configuration.BricsConfiguration;
import gov.nih.brics.auth.data.entity.AccountUserDetails;
import gov.nih.brics.auth.service.AccountService;
import gov.nih.brics.auth.service.AccountServiceImpl;
import gov.nih.tbi.account.model.hibernate.Account;

@Component
public class BricsAuthenticationProvider implements AuthenticationProvider {
	private static final Logger logger = LogManager.getLogger();
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private BricsConfiguration config;
	
	/**
	 * Authenticate the credentials stored the the passed in Authentication object.
	 * 
	 * @param authentication - The UsernamePasswordAuthenticationToken object passed in from the authentication filter.
	 * @return The final authenticated UsernamePasswordAuthenticationToken object or null to pass on to the next
	 *         provider.
	 * @throws AuthenticationException When the user's account or credentials fail validation checks.
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		// Validate the given auth object.
		if (!(authentication.getPrincipal() instanceof String)
				|| !(authentication.getCredentials() instanceof String)) {
			throw new AuthenticationServiceException("Both principal and credentials will need to be strings.");
		}

		AccountUserDetails userDets = null;
		Account loginAccount = null;
		String username = (String) authentication.getPrincipal();
		String plainTxtPwd = (String) authentication.getCredentials();

		logger.info("Authenticating login attempt for " + username + ".");

		try {
			loginAccount = accountService.findByUserName(username);

			// Check if the login account exists.
			if (loginAccount == null) {
				logger.error("No account could be found for username: " + username);
				throw new UsernameNotFoundException("Username or password is invalid");
			}

			userDets = new AccountUserDetails(loginAccount, accountService.getDiseaseKey(), config);

			// Run account validation tests.
			accountService.validateAccount(loginAccount, userDets, plainTxtPwd);
		} catch (DataAccessException e) {
			// Logging error to the tomcat log rather than
			logger.error("Error occured while retrieving the account from the database.", e);
			throw new AuthenticationServiceException("Error occured while retrieving the account from the database.");
		}

		logger.info("User login authentication is successful: " + username);

		// Construct data for the final trusted Authentication object.
		String pwdHash = Hex.encodeHexString(loginAccount.getPassword());
		//Collection<GrantedAuthority> authorities = userDets.getAuthorities();
		Collection<GrantedAuthority> authorities = AccountServiceImpl.getAuthorities(loginAccount);

		return new UsernamePasswordAuthenticationToken(userDets, pwdHash, authorities);
	}
	
	/**
	 * Ensure that only the UsernamePasswordAuthenticationToken objects are supported.
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
