package gov.nih.brics.auth.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import gov.nih.brics.downloadtool.controller.DownloadQueueController;
import gov.nih.brics.downloadtool.data.entity.UserToken;
import gov.nih.brics.downloadtool.exception.UnauthorizedActionException;
import gov.nih.brics.downloadtool.model.DownloadToolPackage;
import gov.nih.brics.downloadtool.model.DownloadToolPackageDownloadables;
import gov.nih.brics.downloadtool.model.DownloadToolPackageUserFile;
import gov.nih.brics.downloadtool.security.jwt.ClaimsOnlyTokenProvider;
import gov.nih.brics.downloadtool.model.DownloadToolPackage.OriginEnum;
import gov.nih.brics.downloadtool.service.AccountService;
import gov.nih.brics.downloadtool.service.RepositoryService;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.User;

public class DownloadQueueControllerTest {
	
	@Mock
	private RepositoryService repositoryService;
	
	@Mock
	private ClaimsOnlyTokenProvider tokenProvider;
	
	@Mock
	private AccountService accountService;
	
	@InjectMocks
	private DownloadQueueController controller;
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGetAllWithResults() {
		List<DownloadToolPackage> packages = new ArrayList<>();
		packages.add(createDownloadPackage());

		String authJwt = "abc";
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication().getCredentials()).thenReturn(authJwt);
        when(tokenProvider.parseToken(anyString())).thenReturn(token());
		when(accountService.findByUserName(anyString())).thenReturn(validAccount());
		when(repositoryService.getDownloadPackageByUser(any(Account.class))).thenReturn(packages);
		
		ResponseEntity<List<DownloadToolPackage>> response = controller.getAll();
		assertThat(response.getBody()).isEqualTo(packages);
	}
	
	@Test
	public void testGetAllNoResults() {
		List<DownloadToolPackage> packages = new ArrayList<>();
		// note: packages is empty

		String authJwt = "abc";
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication().getCredentials()).thenReturn(authJwt);
        when(tokenProvider.parseToken(anyString())).thenReturn(token());
		when(accountService.findByUserName(anyString())).thenReturn(validAccount());
		when(repositoryService.getDownloadPackageByUser(any(Account.class))).thenReturn(packages);
		
		
		ResponseEntity<List<DownloadToolPackage>> response = controller.getAll();
		assertTrue(response.getBody().size() == 0);
	}
	
	@Test
	public void removeFromQueueSuccess() {
		List<Long> downloadableIds = new ArrayList<>();
		downloadableIds.add(2L);

		String authJwt = "abc";
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication().getCredentials()).thenReturn(authJwt);
        when(tokenProvider.parseToken(anyString())).thenReturn(token());
		when(accountService.findByUserName(anyString())).thenReturn(validAccount());
		when(repositoryService.doesUserOwnThese(any(Account.class), anyList())).thenReturn(true);
		
		ResponseEntity<Void> response = controller.removeFromQueue(downloadableIds);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void removeFromQueueNoId() {
		List<Long> downloadableIds = new ArrayList<>();
		
		ResponseEntity<Void> response = controller.removeFromQueue(downloadableIds);
		
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
	}
	
	@Test
	public void removeFromQueueNotOwned() throws UnauthorizedActionException {
		List<Long> downloadableIds = new ArrayList<>();
		downloadableIds.add(1L); // an unathorized id
		
		String authJwt = "abc";
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication().getCredentials()).thenReturn(authJwt);
        when(tokenProvider.parseToken(anyString())).thenReturn(token());
		when(accountService.findByUserName(anyString())).thenReturn(validAccount());
		doThrow(new UnauthorizedActionException()).when(repositoryService).removeFromQueue(any(Account.class), anyList());
		
		ResponseEntity<Void> response = controller.removeFromQueue(downloadableIds);
		
		assertTrue(response.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
	}
	
	private UserToken token() {
		UserToken output = new UserToken();
		output.setAuthorities("ADMINISTRATOR");
		output.setFullName("full name");
		output.setId(1L);
		output.setOrgId(2L);
		output.setTokenExpiration(new Date());
		output.setUsername("administrator");
		return output;
	}
	
	private DownloadToolPackage createDownloadPackage() {
		DownloadToolPackage output = new DownloadToolPackage();
		List<DownloadToolPackageDownloadables> downloadables = new ArrayList<>();
		downloadables.add(createDownloadable());
		
		Date now = new Date();
		output.setDateAdded(now.getTime());
		output.setDownloadables(downloadables);
		output.setId(3L);
		output.setName("some download package");
		output.setOrigin(OriginEnum.QUERY_TOOL);
		return output;
	}
	
	private DownloadToolPackageDownloadables createDownloadable() {
		DownloadToolPackageDownloadables downloadable = new DownloadToolPackageDownloadables();
		downloadable.setId(2L);
		downloadable.setType("type");
		downloadable.setUserFile(createUserFile());
		return downloadable;
	}
	
	private DownloadToolPackageUserFile createUserFile() {
		DownloadToolPackageUserFile userfile = new DownloadToolPackageUserFile();
		userfile.setDescription("userfile description");
		userfile.setId(1L);
		userfile.setName("userfile.txt");
		userfile.setPath("/files");
		return userfile;
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
		account.setAffiliatedInstitution("PDBP");
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
