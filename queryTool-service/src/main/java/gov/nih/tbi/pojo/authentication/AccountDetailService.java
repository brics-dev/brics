package gov.nih.tbi.pojo.authentication;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.util.QueryRestProviderUtils;
import gov.nih.tbi.ws.provider.RestQueryAccountProvider;

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
	private static final long serialVersionUID = 2628156955029307389L;

	private static final String ROLE_UNSIGNED = "ROLE_UNSIGNED";

	private static SavedRequestAwareAuthenticationSuccessHandler SUCCESS_HANDLER =
			new SavedRequestAwareAuthenticationSuccessHandler();

	@Autowired
	ApplicationConstants constants;

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
		RestQueryAccountProvider accountProvider = new RestQueryAccountProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		Account acc = null;
		acc = accountProvider.getUserAccountByUserName(auth.getName(), constants.getAccountWebServiceURL());

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

		logger.debug("loadUserByUserName: " + username);

		RestQueryAccountProvider accountProvider = new RestQueryAccountProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		Account acc = null;
		acc = accountProvider.getUserAccountByUserName(username, constants.getAccountWebServiceURL());
		AccountUserDetails userDetails = null;

		if (acc != null) {
			acc.setDiseaseKey("-1");
			// XXX Form based login provides a dieaseId of -1L (The default disease) (unrelieable)
			userDetails = new AccountUserDetails(acc, -1l);
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

		logger.debug("AUTH | Query does not contain an account module. Acquiring account object via WS call...");
		logger.debug("     | Location of account module: " + constants.getModulesQTURL(diseaseId));
		// This is a manual call for what PortalUtils.getProxyTicket() does in the rest of the application.
		String accUrl = constants.getModulesAccountURL(diseaseId);
		String pt = ((CasAssertionAuthenticationToken) auth).getAssertion().getPrincipal()
				.getProxyTicketFor(constants.getModulesAccountURL(diseaseId) + "/j_spring_cas_security_check");

		logger.debug("     | Auth: " + auth);
		logger.debug("     | PT aquired for WS call to account module: " + pt);

		RestQueryAccountProvider accountProvider = new RestQueryAccountProvider(accUrl, pt);
		logger.debug("     | Constructured a provider to get the user's account : " + accUrl + " " + pt);
		acc = accountProvider.getUserAccountByUserName(auth.getName(), constants.getAccountWebServiceURL());

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
	 * Method used by the spring security background to clear failed login attempts
	 */
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		logger.info(authentication.getName() + " has successfully logged in.");

		// forward the page onto the original url
		SUCCESS_HANDLER.onAuthenticationSuccess(request, response, authentication);
	}

	/**
	 * Method used by the spring security to update the failed login attempts table, or help inform the user when they
	 * can login next.
	 */
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		/*
		 * AuthenticationException.getAuthentication() is deprecated to avoid potential leaking of sensitive information
		 * // Got a null pointer here so added check if (exception != null && exception.getAuthentication() != null &&
		 * exception.getAuthentication().getName() != null) {
		 * logger.debug("Someone failed to log into the system with the username: " +
		 * exception.getAuthentication().getName()); logger.debug("exception type was: " + exception.getClass()); }
		 * 
		 * // Make updates to failed attempts table if the credentials were wrong if (exception instanceof
		 * BadCredentialsException) { // Don't log a blank username/password combination if
		 * (!(QueryToolConstants.EMPTY_STRING.equals(exception.getAuthentication().getName()) &&
		 * QueryToolConstants.EMPTY_STRING .equals((String) exception.getAuthentication().getCredentials()))) { } }
		 */

		String redirectUrl = QueryToolConstants.LOGIN_FAILURE_DEFAULT;

		// sets information for the login-error page to display when the account is unlocked
		// else
		if (exception instanceof LockedException) {
			// Account account = accountDao.getByUserName(exception.getAuthentication().getName());
			// request.getSession().setAttribute(ServiceConstants.UNLOCK_DATE_PARAM, account.getUnlockDate());
		} else if (exception instanceof CredentialsExpiredException) {
			redirectUrl = QueryToolConstants.LOGIN_FAILURE_EXPIRED;
		}

		SimpleUrlAuthenticationFailureHandler FAILURE_HANDLER = new SimpleUrlAuthenticationFailureHandler(redirectUrl);

		// forwards the user onto the login error page.
		FAILURE_HANDLER.onAuthenticationFailure(request, response, exception);
	}

}
