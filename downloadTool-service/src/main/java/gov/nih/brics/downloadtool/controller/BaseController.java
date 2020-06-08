package gov.nih.brics.downloadtool.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.nih.brics.downloadtool.data.entity.UserToken;
import gov.nih.brics.downloadtool.security.jwt.ClaimsOnlyTokenProvider;
import gov.nih.brics.downloadtool.service.AccountService;
import gov.nih.tbi.account.model.hibernate.Account;

public class BaseController {
	
	@Autowired
	ClaimsOnlyTokenProvider tokenProvider;
	
	@Autowired
	AccountService accountService;
	
	/**
	 * Get current requesting user's access token in UserToken form
	 * 
	 * @return UserToken
	 */
	public UserToken getUserToken() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String token = (String) auth.getCredentials();
		return tokenProvider.parseToken(token);
	}
	
	/**
	 * Get current requesting user's Account
	 * 
	 * @return
	 */
	public Account getAccount() {
		UserToken token = getUserToken();
		return accountService.findByUserName(token.getUsername());
	}
}
