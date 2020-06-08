package gov.nih.brics.auth.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.nih.brics.auth.configuration.BricsConfiguration;
import gov.nih.brics.auth.controller.UserApiController;
import gov.nih.brics.auth.data.entity.AccountUserDetails;
import gov.nih.brics.auth.data.entity.UserToken;
import gov.nih.brics.auth.security.jwt.TokenProvider;
import gov.nih.brics.auth.service.AccountService;
import gov.nih.brics.auth.util.BlackListCache;
import gov.nih.brics.auth.util.SessionTracker;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.User;

/**
 * 
 * Tests the user controller.  Specifically makes sure that the contract (in swagger)
 * is upheld and that as many outside or corner conditions are validated.
 * 
 * @author Joshua Park
 *
 */
public class UserApiControllerTest {
	
	@Mock
	private TokenProvider tokenProvider;
	
	@Mock
	private BlackListCache blackList;
	
	@Mock
	private AccountService accountService;
	
	@Mock
	private SessionTracker sessionTracker;
	
	@InjectMocks
	private UserApiController controller = new UserApiController();
	
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testLoginSuccess() {
		String username = "user";
		String password = "pass";
		String expectedToken = "abc.def.ghi";
		Account account = validAccount();
		BricsConfiguration config = mock(BricsConfiguration.class);
		
		when(accountService.getDiseaseKey()).thenReturn(5L);
		AccountUserDetails aud = new AccountUserDetails(account, accountService.getDiseaseKey(), config);
		
		when(accountService.findByUserName(anyString())).thenReturn(account);
		// technically the same thing as just not adding it but here for completeness
		doNothing().when(accountService).validateAccount(account, aud, password);
		
		when(tokenProvider.createToken(any(AccountUserDetails.class))).thenReturn(expectedToken);
		
		ResponseEntity<String> response = controller.login(username, password);
		
		assertThat(response.getBody()).isEqualTo(expectedToken);
	}
	
	/**
	 * Given bad credentials, the validation should throw an exception of type
	 * BadCredentialsException.
	 */
	@Test(expectedExceptions=BadCredentialsException.class)
	public void testBadCredentialsLogin() {
		String username = "user";
		String password = "pass";
		Account account = validAccount();
		
		BricsConfiguration config = mock(BricsConfiguration.class);
		
		when(accountService.getDiseaseKey()).thenReturn(5L);
		AccountUserDetails aud = new AccountUserDetails(account, accountService.getDiseaseKey(), config);
		
		when(accountService.findByUserName(anyString())).thenReturn(account);
		doThrow(new BadCredentialsException("The given credentials are invalid"))
				.when(accountService).validateAccount(account, aud, password);
		
		// throwing the exception is equivalent to a 401
		controller.login(username, password);
	}
	
	/**
	 * Given a bad username, the method should throw a BadCredentialException. That
	 * is equivalent to a 401 response
	 */
	@Test(expectedExceptions=BadCredentialsException.class)
	public void testBadUsernameLogin() {
		String username = "userbad";
		String password = "pass";

		when(accountService.findByUserName(anyString())).thenReturn(null);
		
		// throwing the exception is equivalent to a 401
		controller.login(username, password);
	}
	
	/**
	 * If the account is locked, the account validation should fail with a
	 * LockedException.  Reminder, we mock the service here.  Other tests actually
	 * test the validation.
	 */
	@Test(expectedExceptions=LockedException.class)
	public void testLockedAccountLogin() {
		String username = "user";
		String password = "pass";
		Account account = validAccount();
		account.setIsLocked(true);
		BricsConfiguration config = mock(BricsConfiguration.class);
		
		when(accountService.getDiseaseKey()).thenReturn(5L);
		AccountUserDetails aud = new AccountUserDetails(account, accountService.getDiseaseKey(), config);
		
		when(accountService.findByUserName(anyString())).thenReturn(account);
		doThrow(new LockedException("The account is locked"))
				.when(accountService).validateAccount(account, aud, password);
		
		// throwing the exception is equivalent to a 401
		controller.login(username, password);
	}
	
	/**
	 * If the account is disabled, the account validation should fail with a
	 * DisabledException.  That's equivalent of a 401 response
	 */
	@Test(expectedExceptions=DisabledException.class)
	public void testDisabledAccountLogin() {
		String username = "user";
		String password = "pass";
		Account account = validAccount();
		account.setIsActive(false);
		BricsConfiguration config = mock(BricsConfiguration.class);
		
		when(accountService.getDiseaseKey()).thenReturn(5L);
		AccountUserDetails aud = new AccountUserDetails(account, accountService.getDiseaseKey(), config);
		
		when(accountService.findByUserName(anyString())).thenReturn(account);
		doThrow(new DisabledException("The account is disabled"))
				.when(accountService).validateAccount(account, aud, password);
		
		// throwing the exception is equivalent to a 401
		controller.login(username, password);
	}
	
	
	/**
	 * If the account has expired, the account validation should fail with a
	 * AccountExpiredException. That's equivalent to a 401 response
	 */
	// TODO: write this test
	
	/**
	 * if the credentials have expired, the account validation should fail with a
	 * CredentialsExpiredException.
	 */
	// TODO: write this test
	
	/**
	 * If the user fails login 5 times, the account should be temporarily locked.
	 * That means we should get a BadCredentialsException 5 times with messages
	 * including "The given credentials are invalid. You have X attempt(s) remaining"
	 * The message should change for the #1 attempt.  We should then get an account
	 * locked LockedException.
	 */
	@Test
	public void testAccountLockoutAfter5Incorrect() {
		// TODO: write this test
		assertThat(true);
	}
	
	/**
	 * Ensures that, after calling logout once with a jwt, we get back a 200 response
	 */
	@Test
	public void testLogoutSuccess() {
		String authJwt = "abc";
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getCredentials()).thenReturn(authJwt);
        UserToken ut = validUserToken();
        when(tokenProvider.parseToken(authJwt)).thenReturn(ut);
        
        ResponseEntity<Void> response = controller.logout();
        verify(blackList).add(authJwt, ut.getTokenExpiration());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	/**
	 * Note: all tests for "invalid JWT" scenarios like expired, invalid,
	 * blacklisted, etc. are tested in the TokenProviderTest
	 */

	
	public UserToken validUserToken() {
		UserToken ut = new UserToken();
		ut.setFullName("name, full");
		ut.setAuthorities("ROLE_DICTIONARY,ROLE_ADMIN");
		ut.setId(1L);
		ut.setOrgId(5L);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, 1);
		
		ut.setTokenExpiration(cal.getTime());
		ut.setUsername("user");
		return ut;
	}
	
	public Account validAccount() {
		Account account = new Account();
		
		User user = new User();
		user.setFirstName("first");
		user.setLastName("last");
		user.setMiddleName("middle");
		user.setId(2L);
		
		Set<AccountRole> roles = new HashSet<AccountRole>();
		roles.add(new AccountRole(account, RoleType.ROLE_DICTIONARY, RoleStatus.ACTIVE, new Date()));
		roles.add(new AccountRole(account, RoleType.ROLE_ADMIN, RoleStatus.ACTIVE, new Date()));
		
		account.setUserName("user");
		account.setAffiliatedInstitution("pdbp");
		account.setId(1L);
		account.setUser(user);
		account.setAccountRoleList(roles);
		account.setAccountStatus(AccountStatus.ACTIVE);
		account.setIsActive(true);
		account.setIsLocked(false);
		account.setSalt("abc");
		
		return account;
	}
	
}
