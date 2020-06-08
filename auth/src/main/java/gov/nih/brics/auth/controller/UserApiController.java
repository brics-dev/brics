package gov.nih.brics.auth.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import gov.nih.brics.auth.configuration.BricsConfiguration;
import gov.nih.brics.auth.data.entity.AccountUserDetails;
import gov.nih.brics.auth.security.jwt.TokenProvider;
import gov.nih.brics.auth.service.AccountService;
import gov.nih.brics.auth.util.BlackListCache;
import gov.nih.brics.auth.util.SessionTracker;
import gov.nih.tbi.account.model.hibernate.Account;

@RestController
public class UserApiController implements UserApi {
	
	private static final Logger logger = LogManager.getLogger();
	
	@Autowired
	TokenProvider tokenProvider;
	
	@Autowired
	BlackListCache blackList;
	
	@Autowired
	AccountService accountService;
	
	@Autowired
	BricsConfiguration config;
	
	@Autowired
	SessionTracker sessionTracker;

	@Override
	public ResponseEntity<String> login(String username, String password) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Log in from credentials with username %s", username));
		}
		Account account = accountService.findByUserName(username);
		if (account == null) {
			throw new BadCredentialsException("The given credentials are invalid");
		}
		AccountUserDetails aud = new AccountUserDetails(account, accountService.getDiseaseKey(), config);
		
		// throws a runtime exception if the account fails
		accountService.validateAccount(account, aud, password);
		String token = tokenProvider.createToken(aud);
		sessionTracker.startSession(token);
		if (logger.isDebugEnabled()) {
			logger.debug("Log in successful with username %s", username);
		}
		return ResponseEntity.ok(token);
	}
	
	@Override
	public ResponseEntity<String> bricsLogin(String username, String password) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Log in from BRICS back-end with username %s", username));
		}
		Account account = accountService.findByUserName(username);
		if (account == null) {
			throw new BadCredentialsException("The given credentials are invalid");
		}
		AccountUserDetails aud = new AccountUserDetails(account, accountService.getDiseaseKey(), config);
		
		// throws a runtime exception if the account fails
		accountService.validateAccountFromBRICS(account, password);
		String token = tokenProvider.createToken(aud);
		sessionTracker.startSession(token);
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Log in from BRICS back-end successful with username %s", username));
		}
		return ResponseEntity.ok(token);
	}

	@Override
	public ResponseEntity<Void> logout() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String token = (String) auth.getCredentials();
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Log out JWT %s", token));
		}
		
		// add the current token to the blacklist
		blackList.add(token, tokenProvider.parseToken(token).getTokenExpiration());
		sessionTracker.endSessionNow(token);
		return ResponseEntity.ok().build();
	}

	@Override
	public ResponseEntity<String> renew() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String token = (String) auth.getCredentials();
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Renew JWT %s", token));
		}
		
		// blacklist the old token
		blackList.add(token, tokenProvider.parseToken(token).getTokenExpiration());
		
		String newToken = tokenProvider.renewToken(token);
		sessionTracker.updateSession(newToken);
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("JWT %s renewed to %s", token, newToken));
		}
		return ResponseEntity.ok(newToken);
	}

	@Override
	public ResponseEntity<Void> verify() {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("JWT verified successfully: %s", (String)SecurityContextHolder.getContext().getAuthentication().getCredentials()));
		}
		return ResponseEntity.ok().build();
	}

}
