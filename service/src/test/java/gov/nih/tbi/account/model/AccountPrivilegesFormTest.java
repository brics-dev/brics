package gov.nih.tbi.account.model;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;

public class AccountPrivilegesFormTest {

	@BeforeMethod
	public void init() {}

	@Test
	public void setAccountRoleListTest() {
		Account account = new Account();
		Set<AccountRole> roleSet = new HashSet<>();

		AccountRole dictionaryRole = new AccountRole();
		dictionaryRole.setRoleType(RoleType.ROLE_DICTIONARY);
		dictionaryRole.setRoleStatus(RoleStatus.EXPIRED);
		roleSet.add(dictionaryRole);

		AccountRole adminRole = new AccountRole();
		adminRole.setRoleType(RoleType.ROLE_ADMIN);
		adminRole.setRoleStatus(RoleStatus.ACTIVE);
		roleSet.add(adminRole);

		AccountRole guidRole = new AccountRole();
		guidRole.setRoleType(RoleType.ROLE_GUID);
		guidRole.setRoleStatus(RoleStatus.EXPIRING_SOON);
		roleSet.add(guidRole);


		account.setAccountRoleList(roleSet);
		account.setUserName("testUser");
		AccountPrivilegesForm accountPrivilegesForm = new AccountPrivilegesForm(account);

		assertTrue(accountPrivilegesForm.getAccountRoleList().contains(dictionaryRole));
		assertTrue(accountPrivilegesForm.getAccountRoleList().contains(adminRole));

		Date currentDate = new Date();
		Date newExpirationDate = new Date(currentDate.getTime() + TimeUnit.MILLISECONDS.convert(15, TimeUnit.DAYS));
		DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("MM/dd/yyyy");
		String newExpirationDateString = dateFormatter.print(newExpirationDate.getTime());
		accountPrivilegesForm.setAccountRoleExpiration(RoleType.ROLE_DICTIONARY.getId(), newExpirationDateString);
		accountPrivilegesForm.setAccountRoleExpiration(RoleType.ROLE_GUID.getId(), newExpirationDateString);
		accountPrivilegesForm.setAccountRoleExpiration(RoleType.ROLE_ADMIN.getId(), newExpirationDateString);
		accountPrivilegesForm.setAccountRoleExpiration(RoleType.ROLE_METASTUDY.getId(), newExpirationDateString);

		for (AccountRole accountRole : accountPrivilegesForm.getAccountRoleList()) {
			assertEquals(accountRole.getRoleStatus(), RoleStatus.EXPIRING_SOON);
			assertEquals(dateFormatter.print(accountRole.getExpirationDate().getTime()), newExpirationDateString);
		}

		boolean exists = false;
		for (AccountRole accountRole : accountPrivilegesForm.getAccountRoleList()) {
			if (RoleType.ROLE_METASTUDY == accountRole.getRoleType()
					&& RoleStatus.EXPIRING_SOON == accountRole.getRoleStatus()) {
				exists = true;
			}
		}

		assertTrue(exists);

		newExpirationDate = new Date(currentDate.getTime() - TimeUnit.MILLISECONDS.convert(5, TimeUnit.DAYS));
		newExpirationDateString = dateFormatter.print(newExpirationDate.getTime());
		accountPrivilegesForm.setAccountRoleExpiration(RoleType.ROLE_DICTIONARY.getId(), newExpirationDateString);
		accountPrivilegesForm.setAccountRoleExpiration(RoleType.ROLE_GUID.getId(), newExpirationDateString);
		accountPrivilegesForm.setAccountRoleExpiration(RoleType.ROLE_ADMIN.getId(), newExpirationDateString);
		accountPrivilegesForm.setAccountRoleExpiration(RoleType.ROLE_METASTUDY.getId(), newExpirationDateString);

		for (AccountRole accountRole : accountPrivilegesForm.getAccountRoleList()) {
			assertEquals(accountRole.getRoleStatus(), RoleStatus.EXPIRED);
		}

		newExpirationDate = new Date(currentDate.getTime() + TimeUnit.MILLISECONDS.convert(35, TimeUnit.DAYS));
		newExpirationDateString = dateFormatter.print(newExpirationDate.getTime());
		accountPrivilegesForm.setAccountRoleExpiration(RoleType.ROLE_DICTIONARY.getId(), newExpirationDateString);
		accountPrivilegesForm.setAccountRoleExpiration(RoleType.ROLE_GUID.getId(), newExpirationDateString);
		accountPrivilegesForm.setAccountRoleExpiration(RoleType.ROLE_ADMIN.getId(), newExpirationDateString);
		accountPrivilegesForm.setAccountRoleExpiration(RoleType.ROLE_METASTUDY.getId(), newExpirationDateString);
		
		for (AccountRole accountRole : accountPrivilegesForm.getAccountRoleList()) {
			assertEquals(accountRole.getRoleStatus(), RoleStatus.ACTIVE);
			assertEquals(dateFormatter.print(accountRole.getExpirationDate().getTime()), newExpirationDateString);
		}

	}
}
