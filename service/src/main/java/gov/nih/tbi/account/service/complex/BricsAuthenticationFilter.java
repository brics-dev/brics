
package gov.nih.tbi.account.service.complex;

import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AccountProvider;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.ws.HashMethods;

public class BricsAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "j_username";
	public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "j_password";

	@Autowired
	ModulesConstants modulesConstants;

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {

		String username = getPropertyFromRequest(request, SPRING_SECURITY_FORM_USERNAME_KEY);
		String password = getPropertyFromRequest(request, SPRING_SECURITY_FORM_PASSWORD_KEY);

		String orgName = modulesConstants.getModulesOrgName();
		Long diseaseKey = ServiceConstants.DEFAULT_PROVIDER; // for environments with only one disease, we can leave it
															 // as the default

		// This only matters for local. Essentially, on local, we don't have the disease key from CAS; thus, we will
		// need to derive it from the org name field in modules properties.
		if ("PDBP".equalsIgnoreCase(orgName)) {
			diseaseKey = 5L;
		} else if ("FITBIR".equalsIgnoreCase(orgName)) {
			diseaseKey = 8L;
		} else if ("CNRM".equalsIgnoreCase(orgName)) {
			diseaseKey = 7L;
		}

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

		Account account = accountProvider.getByUserName(username);

		if (account != null) {
			String salt = account.getSalt();
			return new UsernamePasswordAuthenticationToken(username, salt + password);
		}
		
		throw new BadCredentialsException("Bad credentials");
	}

	private String getPropertyFromRequest(HttpServletRequest request, String propertyName) {

		return request.getParameter(propertyName);
	}

}
