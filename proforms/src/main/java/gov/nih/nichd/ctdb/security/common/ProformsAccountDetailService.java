package gov.nih.nichd.ctdb.security.common;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import gov.nih.nichd.ctdb.common.ModulesConstants;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.account.model.AccountUserDetails;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AccountProvider;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.ws.HashMethods;

public class ProformsAccountDetailService implements UserDetailsService, AuthenticationUserDetailsService, Serializable, AuthenticationSuccessHandler, AuthenticationFailureHandler {

	private static final long serialVersionUID = -3640201886626098274L;
	private static final Logger logger = Logger.getLogger(ProformsAccountDetailService.class);
	
	private static SavedRequestAwareAuthenticationSuccessHandler SUCCESS_HANDLER =
			new SavedRequestAwareAuthenticationSuccessHandler();
	
	@Autowired
	ModulesConstants modulesConstants;
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		String redirectUrl = ServiceConstants.LOGIN_FAILURE_DEFAULT;

		// sets information for the login-error page to display when the account is unlocked
		// else
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

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		logger.info(authentication.getName() + " has successfully logged in.");
		SUCCESS_HANDLER.onAuthenticationSuccess(request, response, authentication);
		
	}
	
	public UserDetails loadUserByAuthentication(Authentication auth) throws UsernameNotFoundException, DataAccessException, UnsupportedEncodingException {
		return loadUserDetails(auth);
	}

	@Override
	public UserDetails loadUserDetails(Authentication token) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		logger.debug("loadUserDetails: " + token.getName());
		
		AccountUserDetails userDetails = null;
		
		try {
			Account account = accountByAuthentication(token);
			if (account != null) {
				userDetails = new AccountUserDetails(account, getDiseaseId(token));
			}
			else {
				throw new UsernameNotFoundException(token.getName());
			}
		}
		catch(Exception e) {
			logger.error("Unable to perform web request to Account webservice", e);
			return null;
		}
		return userDetails;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		logger.debug("loadUserDetails by Username: " + username);
		AccountUserDetails userDetails = null;
		
		try {
			Account account = accountByUsername(username);
			if (account != null) {
				userDetails = new AccountUserDetails(account);
			}
			else {
				throw new UsernameNotFoundException(username);
			}
		}
		catch(Exception e) {
			logger.error("Unable to perform web request to Account webservice", e);
			return null;
		}
		return userDetails;
	}
	
	private Account accountByAuthentication(Authentication auth) throws NoRouteToHostException, UnknownHostException, RuntimeException {
		String username = auth.getName();
		String restfulDomain = SysPropUtil.getProperty("webservice.usermgmt.url");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.account.byusername.url")+"/" + username;
		SecuritySessionUtil ssu = new SecuritySessionUtil();
		String pt = ((CasAssertionAuthenticationToken) auth).getAssertion().getPrincipal().getProxyTicketFor(
				modulesConstants.getModulesAccountURL() + "portal/j_spring_cas_security_check");
		Client client = ClientBuilder.newClient();
		restfulUrl = ssu.compileProxiedWebserviceUrl(restfulUrl, pt);
		WebTarget wt = client.target(restfulUrl);
		Account account = wt.request(MediaType.TEXT_XML).get(Account.class);
		return account;
	}
	
	private Account accountByUsername(String username) {
		logger.debug("loadUserByUserName: " + username);
		
		AccountProvider accountProvider = null;
		try {
			accountProvider = new AccountProvider(modulesConstants.getModulesAccountURL(),
					modulesConstants.getAdministratorUsername(),
					HashMethods.getServerHash(modulesConstants.getAdministratorUsername(),
							modulesConstants.getSaltedAdministratorPassword()));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Account acc = accountProvider.getByUserName(username);
		return acc;
	}
	
	private Long getDiseaseId(Authentication token) {
		Long diseaseId = null;
		String diseaseString = (String) ((CasAssertionAuthenticationToken) token).getAssertion().getPrincipal()
				.getAttributes().get("disease");
		if (diseaseString != null) {
			diseaseId = Long.valueOf(diseaseString);
		}
		return diseaseId;
	}

}
