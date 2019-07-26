package gov.nih.tbi.account.service.complex;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import gov.nih.tbi.account.model.AccountUserDetails;
import gov.nih.tbi.account.model.hibernate.Account;

public class BricsAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private static final String ESIGN_REDIRECT_URL = "/esign/esignAction!validateName.action";
	private static final String PORTAL_URL = "/portal";
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		
		AccountUserDetails accountUserDetails = (AccountUserDetails) authentication.getPrincipal();
		Account account = accountUserDetails.getAccount();
		
		if (account.getBricsESignature() == null) {
			logger.info("User " + account.getUserName() + " has no electronic signature captured, redirect to esign page.");
			String redirectUrl = response.encodeRedirectURL(PORTAL_URL + ESIGN_REDIRECT_URL);
			response.sendRedirect(redirectUrl);
		} else {
			super.onAuthenticationSuccess(request, response, authentication);
		}
	}

}
