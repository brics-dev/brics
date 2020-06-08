package gov.nih.tbi.account.service.complex;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.service.AccountManager;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class AccountManagerTest {

	@InjectMocks
	private AccountManager accountManager = new AccountManagerImpl();

	@Mock
	private AccountDao accountDao;


	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	public void updateAccountPrivilegesExpirationTestTemplate(Date expirationDate, RoleStatus expectedStatus) {
		List<Account> accounts = new ArrayList<>();

		Account expiringSoonAccount = new Account();
		accounts.add(expiringSoonAccount);
		expiringSoonAccount.setUserName("testUser");

		AccountRole accountRole = new AccountRole();
		accountRole.setExpirationDate(expirationDate);
		accountRole.setRoleStatus(RoleStatus.ACTIVE);
		accountRole.setRoleType(RoleType.ROLE_DICTIONARY);
		Set<AccountRole> roleSet = new HashSet<AccountRole>();
		roleSet.add(accountRole);

		expiringSoonAccount.setAccountRoleList(roleSet);

		when(accountDao.getAllActive()).thenReturn(accounts);

		accountManager.updateAccountPrivilegesExpiration();

		Iterator<AccountRole> roleIterator = expiringSoonAccount.getAccountRoleList().iterator();
		AccountRole testRole = null;
		if (roleIterator.hasNext()) { // there should only be one role here
			testRole = roleIterator.next();
		}

		assertEquals(testRole.getRoleStatus(), expectedStatus);
	}

	@Test
	public void updateAccountPrivilegesExpirationTestExpiringSoon() {
		Date currentDate = new Date();

		updateAccountPrivilegesExpirationTestTemplate(
				new Date(currentDate.getTime() + Math.round(TimeUnit.MILLISECONDS.convert(29, TimeUnit.DAYS))),
				RoleStatus.EXPIRING_SOON);
	}

	@Test
	public void updateAccountPrivilegesExpirationTestActive() {
		Date currentDate = new Date();

		// Expiration date is 32 days into the future - should remain active
		updateAccountPrivilegesExpirationTestTemplate(
				new Date(currentDate.getTime() + TimeUnit.MILLISECONDS.convert(32, TimeUnit.DAYS)), RoleStatus.ACTIVE);
	}

	@Test
	public void updateAccountPrivilegeExpirationTestExpired() {
		Date currentDate = new Date();

		// Expiration date is 1 day into the past - should be expired
		updateAccountPrivilegesExpirationTestTemplate(
				new Date(currentDate.getTime() - Math.round(TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))),
				RoleStatus.EXPIRED);
	}
}
