
package gov.nih.tbi.account.service.complex;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Service;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.model.AccountUserDetails;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.ws.AccountProvider;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.ws.HashMethods;

/**
 * This class is used by spring security to validate the user and identify what granted roles should be given Also this
 * now implements the post valid and invalid login attempts
 * 
 * @author Andrew Johnson
 * 
 */
@Service
public class AccountDetailService implements UserDetailsService, AuthenticationUserDetailsService, Serializable, AuthenticationSuccessHandler, AuthenticationFailureHandler {

	private static Logger logger = Logger.getLogger(AccountDetailService.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 2628156955029307389L;
	private static final String ROLE_UNSIGNED = "ROLE_UNSIGNED";
	private static final String ROLE_GUEST = "ROLE_GUEST";

	@Autowired
	protected AccountManager accountManager;

	@Autowired
	protected DictionaryToolManager dictionaryManager;

	/**
	 * AccountDao for audit logs
	 */
	@Autowired
	protected AccountDao accountDao;

	private static SavedRequestAwareAuthenticationSuccessHandler SUCCESS_HANDLER =
			new SavedRequestAwareAuthenticationSuccessHandler();

	@Autowired
	ModulesConstants modulesConstants;

	/**
	 * Method Used for AuthenticationUserDetailsService Loads UserDetails for Spring Security
	 * 
	 * @param auth
	 * @return
	 * @throws UsernameNotFoundException
	 */
	public UserDetails loadUserDetails(Authentication auth) throws UsernameNotFoundException {

		logger.debug("loadUserDetails: " + auth.getName());

		// TODO:FIXME This web service can't exist
		AccountProvider accountProvider = null;
		try {
			accountProvider = new AccountProvider(modulesConstants.getModulesAccountURL());
		} catch (MalformedURLException e) {

			e.printStackTrace();
		}
		Account acc = accountProvider.getByUserName(auth.getName());

		AccountUserDetails userDetails = null;

		if (acc != null) {
			userDetails = new AccountUserDetails(acc);
		} else {
			throw new UsernameNotFoundException(auth.getName());
		}

		return userDetails;
	}

	/**
	 * Method used for UserDetailsService Loads UserDetails for Spring Security
	 * 
	 * @param username
	 * @return
	 * @throws UsernameNotFoundException
	 * @throws DataAccessException
	 */
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		String orgName = modulesConstants.getModulesOrgName();
		Long diseaseKey = ServiceConstants.DEFAULT_PROVIDER;

		if ("PDBP".equals(orgName)) {
			diseaseKey = 5L;
		} else if ("FITBIR".equals(orgName)) {
			diseaseKey = 8L;
		} else if ("CNRM".equals(orgName)) {
			diseaseKey = 7L;
		}

		logger.debug("loadUserByUserName: " + username);

		AccountProvider accountProvider = null;
		try {
			accountProvider = new AccountProvider(modulesConstants.getModulesAccountURL(diseaseKey),
					modulesConstants.getAdministratorUsername(),
					HashMethods.getServerHash(modulesConstants.getAdministratorUsername(),
							modulesConstants.getSaltedAdministratorPassword(diseaseKey)));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Account acc = accountProvider.getByUserName(username);
		AccountUserDetails userDetails = null;

		if (acc != null) {
			acc.setDiseaseKey("-1");
			// XXX Form based login provides a dieaseId of -1L (The default disease) (unrelieable)
			userDetails = new AccountUserDetails(acc, ServiceConstants.DEFAULT_PROVIDER);
		} else {
			throw new UsernameNotFoundException(username);
		}

		return userDetails;
	}

	/**
	 * Method to load authorization when security is using CAS system.
	 * 
	 * @param auth
	 * @return
	 * @throws UsernameNotFoundException
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 */
	public UserDetails loadUserByAuthentication(Authentication auth)
			throws UsernameNotFoundException, DataAccessException, UnsupportedEncodingException {
		logger.debug("AUTH | Geting info from CAS request body (XML format). Values should never be null.");
		logger.debug("     | Username: " + auth.getName());

		// Get the disease attribute
		Long diseaseId = null;
		String diseaseString = (String) ((CasAssertionAuthenticationToken) auth).getAssertion().getPrincipal()
				.getAttributes().get("disease");
		if (diseaseString != null) {
			diseaseId = Long.valueOf(diseaseString);
		}
		logger.debug("     | Disease ID: " + (diseaseId == null ? "null" : diseaseId));

		// Use a web-service call to get account information if the current module does not support account
		Account acc = null;
		Boolean accountsActive = modulesConstants.getModulesAccountEnabled();
		if (accountsActive != null && accountsActive.equals(true)) {
			logger.debug(
					"AUTH | This instance of portal contains an account module. Acquiring account object via DB call...");
			acc = accountManager.getAccountByUserName(auth.getName());
		} else {
			logger.debug(
					"AUTH | This instance of portal does not contain an account module. Acquiring account object via WS call...");
			logger.debug("     | Location of account module: " + modulesConstants.getModulesAccountURL(diseaseId));
			// This is a manual call for what PortalUtils.getProxyTicket() does in the rest of the application.
			String accUrl = modulesConstants.getModulesAccountURL(diseaseId);
			String pt = ((CasAssertionAuthenticationToken) auth).getAssertion().getPrincipal().getProxyTicketFor(
					modulesConstants.getModulesAccountURL(diseaseId) + "portal/j_spring_cas_security_check");
			logger.debug("     | PT aquired for WS call to account module: " + pt);
			RestAccountProvider accountProvider = new RestAccountProvider(accUrl, pt);
			acc = accountProvider.getByUserName(auth.getName());
		}
		if (acc == null) {
			logger.error("AUTH | FAILED TO SUCESSFULLY ACQUIRE ACCOUNT OBJECT FROM USERNAME! TURN ON DEBUG!");
		}
		logger.debug("     | Successfully retrieved account with id " + acc.getId() + " from username.");

		if (diseaseString != null) {
			acc.setDiseaseKey(diseaseString);
		}
		AccountUserDetails userDetails = null;

		if (acc != null) {
			userDetails = new AccountUserDetails(acc, diseaseId);
		} else {
			throw new UsernameNotFoundException(auth.getName());
		}

		return userDetails;
	}

	/**
	 * Convenience method to convert AccountRoles into GrantedAuthorities
	 * 
	 * @param accountRoleList
	 * @return
	 */
	public static Collection<GrantedAuthority> getAuthorities(Account account) {

		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		
		if (account.getBricsESignature() == null) {
			authorities.add(new SimpleGrantedAuthority(ROLE_UNSIGNED));
			return authorities;
		}

		Date currentDate = new Date();

		for (AccountRole ar : account.getAccountRoleList()) {
			if (ar.getIsActive() && (ar.getExpirationDate() == null || currentDate.before(ar.getExpirationDate()))) {
				authorities.add(new SimpleGrantedAuthority(ar.getRoleType().getName()));
			}
		}

		return authorities;
	}

	/**
	 * Convenience method to convert AccountRoles into GrantedAuthorities
	 * 
	 * @param accountRoleList
	 * @return
	 */
	public static Collection<GrantedAuthority> getTwoFaAuthorities(Account account) {
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(ROLE_GUEST));
		return authorities;
	}

	/**
	 * Method used by the spring security background to clear failed login attempts
	 */
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		logger.info(authentication.getName() + " has successfully logged in.");

		/**
		 * Check to see if auditing is enabled. By default it is off, if the value doesn't exist, it is off. The value
		 * is set in modules.properties = modules.account.audit.enabled
		 */
		if (modulesConstants.getModulesAccountAuditEnabled()) {
			Date loginDate = new Date();
			AccountUserDetails accountUserDetails = (AccountUserDetails) authentication.getPrincipal();
			Account a = accountUserDetails.getAccount();
			accountDao.auditSuccessfulLogin(a.getId(), loginDate);
		}

		// Make updates to account
		// {
		// Account account = accountDao.getByUserName(authentication.getName());
		//
		// account.setLastSuccessfulLogin(new Date());
		// account.setUnlockDate(null);
		//
		// accountDao.save(account);
		//
		// // wipe out the failed login table
		// failedLoginAttemptDao.removeAll(account);
		// }

		// forward the page onto the original url
		SUCCESS_HANDLER.onAuthenticationSuccess(request, response, authentication);
	}

	/**
	 * Method used by the spring security to update the failed login attempts table, or help inform the user when they
	 * can login next.
	 */
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		String redirectUrl = ServiceConstants.LOGIN_FAILURE_DEFAULT;

		// sets information for the login-error page to display when the account is unlocked else
		if (exception instanceof LockedException) {
			// Account account = accountDao.getByUserName(exception.getAuthentication().getName());
			// request.getSession().setAttribute(ServiceConstants.UNLOCK_DATE_PARAM, account.getUnlockDate());
		} else if (exception instanceof CredentialsExpiredException) {
			redirectUrl = ServiceConstants.LOGIN_FAILURE_EXPIRED;
		}

		SimpleUrlAuthenticationFailureHandler FAILURE_HANDLER = new SimpleUrlAuthenticationFailureHandler(redirectUrl);

		// forwards the user onto the login error page.
		FAILURE_HANDLER.onAuthenticationFailure(request, response, exception);
	}

}
