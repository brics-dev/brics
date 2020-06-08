package gov.nih.tbi.api.query.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.api.query.data.entity.UserToken;
import gov.nih.tbi.api.query.model.FormStructure;
import gov.nih.tbi.api.query.model.StudyForm;
import gov.nih.tbi.api.query.security.jwt.EurekaTokenProvider;
import gov.nih.tbi.api.query.service.AccountService;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.cache.QueryMetaDataCache;
import gov.nih.tbi.service.impl.ResultManagerImpl;
import gov.nih.tbi.service.model.MetaDataCache;

public class FormStructureControllerTest {
	@Mock
	private EurekaTokenProvider tokenProvider;

	@Mock
	private AccountService accountService;

	@Mock
	ResultManager resultManager = new ResultManagerImpl();

	@InjectMocks
	FormStructureController formStructureController;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);

		String authJwt = "abc";
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		when(SecurityContextHolder.getContext().getAuthentication().getCredentials()).thenReturn(authJwt);
		when(tokenProvider.parseToken(anyString())).thenReturn(token());
		when(accountService.findByUserName(anyString())).thenReturn(validAccount());
	}

	@Test
	public void getFormByStudyPrefixedIdTest() {
		MetaDataCache metaDataCache = new QueryMetaDataCache();
		List<String> ids = new ArrayList<>();
		ids.add("FITBIR-STUDY0000001");
		FormResult testFormResult = new FormResult();
		testFormResult.setId(100L);
		testFormResult.setShortName("testForm");
		testFormResult.setTitle("test form");
		testFormResult.setUri("testForm");
		testFormResult.setVersion("1.0");
		List<RepeatableGroup> rgs = new ArrayList<>();
		RepeatableGroup rg1 = new RepeatableGroup();
		rg1.setName("rg1");
		rg1.setThreshold(1);
		rg1.setType("Exactly");
		rg1.setPosition("1");
		rgs.add(rg1);

		DataElement rg1de1 = new DataElement();
		rg1de1.setName("rg1de1");
		rg1de1.setId(100L);
		rg1de1.setInputRestrictions(InputRestrictions.FREE_FORM);
		rg1de1.setType(DataType.ALPHANUMERIC);
		rg1de1.setDescription("This is a data element");
		rg1.addDataElement(metaDataCache, rg1de1);

		testFormResult.setRepeatableGroups(rgs);

		Multimap<String, FormResult> formResults = ArrayListMultimap.create();
		formResults.put("FITBIR-STUDY0000001", testFormResult);

		when(resultManager.searchFormsByStudyPrefixedIds(ids)).thenReturn(formResults);
		List<StudyForm> studyForms = formStructureController.getFormByStudyPrefixedId(ids).getBody();
		FormStructure actualForm = studyForms.get(0).getForms().get(0);

		assertEquals(studyForms.get(0).getStudyId(), "FITBIR-STUDY0000001");
		assertEquals(actualForm.getId(), Long.valueOf(100L));
		assertEquals(actualForm.getShortName(), "testForm");
		assertEquals(actualForm.getVersion(), "1.0");
		assertEquals(actualForm.getTitle(), "test form");
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

	public Account validAccount() {
		Account account = new Account();

		User user = new User();
		user.setFirstName("first");
		user.setLastName("last");
		user.setMiddleName("middle");
		user.setId(2L);

		Set<AccountRole> roles = new HashSet<AccountRole>();
		roles.add(new AccountRole(account, RoleType.ROLE_QUERY, RoleStatus.ACTIVE, new Date()));
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
