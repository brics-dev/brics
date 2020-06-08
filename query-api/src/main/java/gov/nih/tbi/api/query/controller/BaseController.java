package gov.nih.tbi.api.query.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.nih.tbi.api.query.data.entity.UserToken;
import gov.nih.tbi.api.query.security.jwt.EurekaTokenProvider;
import gov.nih.tbi.api.query.service.AccountService;
import gov.nih.tbi.service.model.PermissionModel;

public class BaseController {
	
	@Autowired
	EurekaTokenProvider tokenProvider;
	
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
	
	public PermissionModel getPermissionModel() {
		return accountService.getPermissionModel(getUserToken());
	}
	
}
