package gov.nih.tbi.api.query.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.api.query.data.entity.UserToken;
import gov.nih.tbi.api.query.exception.ApiEntityNotFoundException;
import gov.nih.tbi.api.query.model.FormStudy;
import gov.nih.tbi.api.query.model.Study;
import gov.nih.tbi.api.query.model.Study.StatusEnum;
import gov.nih.tbi.api.query.security.jwt.EurekaTokenProvider;
import gov.nih.tbi.api.query.service.AccountService;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.exceptions.ResultSetTranslationException;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.impl.ResultManagerImpl;

public class StudyControllerTest {

	@Mock
	private EurekaTokenProvider tokenProvider;

	@Mock
	private AccountService accountService;

	@Mock
	ResultManager resultManager = new ResultManagerImpl();

	@InjectMocks
	StudyController studyController;

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
	public void studyByPrefixedIdTest() {
		List<String> ids = new ArrayList<>();
		ids.add("FITBIR-STUDY0000100");
		StudyResult testStudyResult = new StudyResult();
		testStudyResult.setUri("uri");
		testStudyResult.setId(100L);
		testStudyResult.setPrefixedId("FITBIR-STUDY0000100");
		testStudyResult.setStatus("Public");
		testStudyResult.setTitle("testStudy");
		testStudyResult.setAbstractText("abstract");
		testStudyResult.setPi("pi");
		List<StudyResult> studyResults = new ArrayList<>();
		studyResults.add(testStudyResult);
		when(resultManager.getStudyByPrefixedIds(ids)).thenReturn(studyResults);
		List<String> prefixedIds = new ArrayList<>();
		prefixedIds.add("FITBIR-STUDY0000100");
		List<Study> actualStudies = studyController.getStudies(prefixedIds).getBody();
		Study actualStudy = actualStudies.get(0);
		assertEquals(actualStudy.getId(), "FITBIR-STUDY0000100");
		assertEquals(actualStudy.getStatus(), StatusEnum.PUBLIC);
		assertEquals(actualStudy.getTitle(), "testStudy");
		assertEquals(actualStudy.getAbstract(), "abstract");
		assertEquals(actualStudy.getPi(), "pi");
	}

	@Test(expectedExceptions = ApiEntityNotFoundException.class)
	public void studyByPrefixedIdTestNotFound() {
		List<String> ids = new ArrayList<>();
		ids.add("100");
		when(resultManager.getStudyByPrefixedIds(ids)).thenReturn(new ArrayList<StudyResult>());
		studyController.getStudies(ids);
	}

	@Test
	public void getAllStudiesTest() throws ResultSetTranslationException {

		List<StudyResult> studyResults = new ArrayList<>();
		StudyResult testStudyResult = new StudyResult();
		testStudyResult.setUri("uri");
		testStudyResult.setId(100L);
		testStudyResult.setStatus("Public");
		testStudyResult.setTitle("testStudy");
		testStudyResult.setAbstractText("abstract");
		testStudyResult.setPi("pi");
		studyResults.add(testStudyResult);

		StudyResult testStudyResult2 = new StudyResult();
		testStudyResult2.setUri("uri");
		testStudyResult2.setId(101L);
		testStudyResult2.setStatus("Public");
		testStudyResult2.setTitle("testStudy2");
		testStudyResult2.setAbstractText("abstract");
		testStudyResult2.setPi("pi");
		studyResults.add(testStudyResult2);

		when(resultManager.runStudyQueryForCaching()).thenReturn(studyResults);
		List<Study> actualStudies = studyController.getStudies(new ArrayList<String>()).getBody();
		assertEquals(actualStudies.get(0).getTitle(), "testStudy");
		assertEquals(actualStudies.get(1).getTitle(), "testStudy2");
	}

	@Test
	public void getStudyByFormNameTest() {
		List<StudyResult> studyResults = new ArrayList<>();
		StudyResult testStudyResult = new StudyResult();
		testStudyResult.setUri("uri");
		testStudyResult.setId(100L);
		testStudyResult.setStatus("Public");
		testStudyResult.setTitle("testStudy1");
		testStudyResult.setAbstractText("abstract");
		testStudyResult.setPi("pi");
		FormResult testForm1 = new FormResult();
		testForm1.setShortName("form1");
		List<FormResult> testForms1 = new ArrayList<>();
		testForms1.add(testForm1);
		testStudyResult.setForms(testForms1);
		studyResults.add(testStudyResult);

		StudyResult testStudyResult2 = new StudyResult();
		testStudyResult2.setUri("uri");
		testStudyResult2.setId(101L);
		testStudyResult2.setStatus("Public");
		testStudyResult2.setTitle("testStudy2");
		testStudyResult2.setAbstractText("abstract");
		testStudyResult2.setPi("pi");
		FormResult testForm2 = new FormResult();
		testForm2.setShortName("form2");
		List<FormResult> testForms2 = new ArrayList<>();
		testForms2.add(testForm2);
		testStudyResult2.setForms(testForms2);
		studyResults.add(testStudyResult2);

		List<String> formNames = new ArrayList<>();
		formNames.add("form1");
		formNames.add("form2");
		when(resultManager.searchStudyByFormNames(formNames)).thenReturn(studyResults);

		List<FormStudy> formStudies = studyController.getStudyByFormName(formNames).getBody();

		List<String> forms = formStudies.stream().map(f -> f.getForm()).collect(Collectors.toList());
		assertTrue(forms.contains("form1"));
		assertTrue(forms.contains("form2"));

		for (FormStudy formStudy : formStudies) {
			if ("form1".equals(formStudy.getForm())) {
				assertEquals("testStudy1", formStudy.getStudies().get(0).getTitle());
			}

			if ("form2".equals(formStudy.getForm())) {
				assertEquals("testStudy2", formStudy.getStudies().get(0).getTitle());
			}
		}
	}

	@Test(expectedExceptions = ApiEntityNotFoundException.class)
	public void getStudyByFormNameNotFoundTest() {
		List<StudyResult> studyResults = new ArrayList<>();
		List<String> formNames = new ArrayList<>();
		formNames.add("form1");
		formNames.add("form2");
		when(resultManager.searchStudyByFormNames(formNames)).thenReturn(studyResults);
		studyController.getStudyByFormName(formNames);
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
