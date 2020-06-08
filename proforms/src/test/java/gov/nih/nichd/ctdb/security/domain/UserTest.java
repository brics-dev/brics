package gov.nih.nichd.ctdb.security.domain;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.ws.HashMethods;

public class UserTest {
	@Test
	public void testProformsAdminRoleExpiringSoon() {
		// +++++++++++++++++++++++++++++++++++++++++++++++
		// ++++++++++ PerformpPre-testing setup ++++++++++
		// +++++++++++++++++++++++++++++++++++++++++++++++

		// Create an account with a "ROLE_PROFORMS_ADMIN" role that is expiring soon.
		Account acct = new Account();
		Set<AccountRole> roles = new HashSet<AccountRole>();
		AccountRole role = new AccountRole();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);

		role.setAccount(acct);
		role.setExpirationDate(calendar.getTime());
		role.setRoleStatus(RoleStatus.EXPIRING_SOON);
		role.setRoleType(RoleType.ROLE_PROFORMS_ADMIN);

		roles.add(role);
		acct.setAccountRoleList(roles);

		// Create the BRICS User object.
		gov.nih.tbi.commons.model.hibernate.User bricsUser = new gov.nih.tbi.commons.model.hibernate.User();

		bricsUser.setFirstName("John");
		bricsUser.setLastName("Doe");
		bricsUser.setId(Long.valueOf(-1));
		bricsUser.setEmail("jdoe@company.bogus");

		acct.setUser(bricsUser);

		// Populate the rest of the account fields.
		acct.setId(Long.valueOf(-1));
		acct.setUserName("jdoe");

		// Generate a fake password hash.
		String salt = HashMethods.getPasswordSalt();
		acct.setPassword(DigestUtils.sha256(salt + "thePassord"));
		acct.setSalt(salt);

		// +++++++++++++++++++++++++++++++++++++
		// ++++++++++ Begin the test. ++++++++++
		// +++++++++++++++++++++++++++++++++++++

		try {
			User proformsUser = new User(acct);
			Assert.assertTrue(proformsUser.isSysAdmin(),
					"The 'sysAdmin' is should be set to 'true' when the 'ROLE_PROFORMS_ADMIN' role is expiring soon.");
		} catch (UnsupportedEncodingException e) {
			Assert.fail("Couldn't encode the password to a hex string.", e);
		}
	}

	@Test
	public void testProformsAdminRoleIsActive() {
		// +++++++++++++++++++++++++++++++++++++++++++++++
		// ++++++++++ PerformpPre-testing setup ++++++++++
		// +++++++++++++++++++++++++++++++++++++++++++++++

		// Create an account with a "ROLE_PROFORMS_ADMIN" role that is active.
		Account acct = new Account();
		Set<AccountRole> roles = new HashSet<AccountRole>();
		AccountRole role = new AccountRole();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);

		role.setAccount(acct);
		role.setExpirationDate(calendar.getTime());
		role.setRoleStatus(RoleStatus.ACTIVE);
		role.setRoleType(RoleType.ROLE_PROFORMS_ADMIN);

		roles.add(role);
		acct.setAccountRoleList(roles);

		// Create the BRICS User object.
		gov.nih.tbi.commons.model.hibernate.User bricsUser = new gov.nih.tbi.commons.model.hibernate.User();

		bricsUser.setFirstName("John");
		bricsUser.setLastName("Doe");
		bricsUser.setId(Long.valueOf(-1));
		bricsUser.setEmail("jdoe@company.bogus");

		acct.setUser(bricsUser);

		// Populate the rest of the account fields.
		acct.setId(Long.valueOf(-1));
		acct.setUserName("jdoe");

		// Generate a fake password hash.
		String salt = HashMethods.getPasswordSalt();
		acct.setPassword(DigestUtils.sha256(salt + "thePassord"));
		acct.setSalt(salt);

		// +++++++++++++++++++++++++++++++++++++
		// ++++++++++ Begin the test. ++++++++++
		// +++++++++++++++++++++++++++++++++++++

		try {
			User proformsUser = new User(acct);
			Assert.assertTrue(proformsUser.isSysAdmin(),
					"The 'sysAdmin' should be set to 'true' when the 'ROLE_PROFORMS_ADMIN' role is active.");
		} catch (UnsupportedEncodingException e) {
			Assert.fail("Couldn't encode the password to a hex string.", e);
		}
	}

	@Test
	public void testProformsAdminRoleExpired() {
		// +++++++++++++++++++++++++++++++++++++++++++++++
		// ++++++++++ PerformpPre-testing setup ++++++++++
		// +++++++++++++++++++++++++++++++++++++++++++++++

		// Create an account with a "ROLE_PROFORMS_ADMIN" role that has expired.
		Account acct = new Account();
		Set<AccountRole> roles = new HashSet<AccountRole>();
		AccountRole role = new AccountRole();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -1);

		role.setAccount(acct);
		role.setExpirationDate(calendar.getTime());
		role.setRoleStatus(RoleStatus.EXPIRED);
		role.setRoleType(RoleType.ROLE_PROFORMS_ADMIN);

		roles.add(role);
		acct.setAccountRoleList(roles);

		// Create the BRICS User object.
		gov.nih.tbi.commons.model.hibernate.User bricsUser = new gov.nih.tbi.commons.model.hibernate.User();

		bricsUser.setFirstName("John");
		bricsUser.setLastName("Doe");
		bricsUser.setId(Long.valueOf(-1));
		bricsUser.setEmail("jdoe@company.bogus");

		acct.setUser(bricsUser);

		// Populate the rest of the account fields.
		acct.setId(Long.valueOf(-1));
		acct.setUserName("jdoe");

		// Generate a fake password hash.
		String salt = HashMethods.getPasswordSalt();
		acct.setPassword(DigestUtils.sha256(salt + "thePassord"));
		acct.setSalt(salt);

		// +++++++++++++++++++++++++++++++++++++
		// ++++++++++ Begin the test. ++++++++++
		// +++++++++++++++++++++++++++++++++++++

		try {
			User proformsUser = new User(acct);
			Assert.assertFalse(proformsUser.isSysAdmin(),
					"The 'sysAdmin' field should be set to 'false' when the 'ROLE_PROFORMS_ADMIN' role has expired.");
		} catch (UnsupportedEncodingException e) {
			Assert.fail("Couldn't encode the password to a hex string.", e);
		}
	}

	@Test
	public void testStudyRoleIsActive() {
		// +++++++++++++++++++++++++++++++++++++++++++++++
		// ++++++++++ PerformpPre-testing setup ++++++++++
		// +++++++++++++++++++++++++++++++++++++++++++++++

		// Create an account with a "ROLE_STUDY" role that is active.
		Account acct = new Account();
		Set<AccountRole> roles = new HashSet<AccountRole>();
		AccountRole role = new AccountRole();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);

		role.setAccount(acct);
		role.setExpirationDate(calendar.getTime());
		role.setRoleStatus(RoleStatus.ACTIVE);
		role.setRoleType(RoleType.ROLE_STUDY);

		roles.add(role);
		acct.setAccountRoleList(roles);

		// Create the BRICS User object.
		gov.nih.tbi.commons.model.hibernate.User bricsUser = new gov.nih.tbi.commons.model.hibernate.User();

		bricsUser.setFirstName("John");
		bricsUser.setLastName("Doe");
		bricsUser.setId(Long.valueOf(-1));
		bricsUser.setEmail("jdoe@company.bogus");

		acct.setUser(bricsUser);

		// Populate the rest of the account fields.
		acct.setId(Long.valueOf(-1));
		acct.setUserName("jdoe");

		// Generate a fake password hash.
		String salt = HashMethods.getPasswordSalt();
		acct.setPassword(DigestUtils.sha256(salt + "thePassord"));
		acct.setSalt(salt);

		// +++++++++++++++++++++++++++++++++++++
		// ++++++++++ Begin the test. ++++++++++
		// +++++++++++++++++++++++++++++++++++++

		try {
			User proformsUser = new User(acct);
			Assert.assertTrue(proformsUser.isCreateStudy(),
					"The 'createStudy' field should be set to 'true' when the 'ROLE_STUDY' status is active.");
		} catch (UnsupportedEncodingException e) {
			Assert.fail("Couldn't encode the password to a hex string.", e);
		}
	}

	@Test
	public void testStudyRoleExpiringSoon() {
		// +++++++++++++++++++++++++++++++++++++++++++++++
		// ++++++++++ PerformpPre-testing setup ++++++++++
		// +++++++++++++++++++++++++++++++++++++++++++++++

		// Create an account with a "ROLE_STUDY" role that is expiring soon.
		Account acct = new Account();
		Set<AccountRole> roles = new HashSet<AccountRole>();
		AccountRole role = new AccountRole();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);

		role.setAccount(acct);
		role.setExpirationDate(calendar.getTime());
		role.setRoleStatus(RoleStatus.EXPIRING_SOON);
		role.setRoleType(RoleType.ROLE_STUDY);

		roles.add(role);
		acct.setAccountRoleList(roles);

		// Create the BRICS User object.
		gov.nih.tbi.commons.model.hibernate.User bricsUser = new gov.nih.tbi.commons.model.hibernate.User();

		bricsUser.setFirstName("John");
		bricsUser.setLastName("Doe");
		bricsUser.setId(Long.valueOf(-1));
		bricsUser.setEmail("jdoe@company.bogus");

		acct.setUser(bricsUser);

		// Populate the rest of the account fields.
		acct.setId(Long.valueOf(-1));
		acct.setUserName("jdoe");

		// Generate a fake password hash.
		String salt = HashMethods.getPasswordSalt();
		acct.setPassword(DigestUtils.sha256(salt + "thePassord"));
		acct.setSalt(salt);

		// +++++++++++++++++++++++++++++++++++++
		// ++++++++++ Begin the test. ++++++++++
		// +++++++++++++++++++++++++++++++++++++

		try {
			User proformsUser = new User(acct);
			Assert.assertTrue(proformsUser.isCreateStudy(),
					"The 'createStudy' field should be set to 'true' when the 'ROLE_STUDY' status is expiring soon.");
		} catch (UnsupportedEncodingException e) {
			Assert.fail("Couldn't encode the password to a hex string.", e);
		}
	}

	@Test
	public void testStudyRoleExpired() {
		// +++++++++++++++++++++++++++++++++++++++++++++++
		// ++++++++++ PerformpPre-testing setup ++++++++++
		// +++++++++++++++++++++++++++++++++++++++++++++++

		// Create an account with a "ROLE_STUDY" role that has expired.
		Account acct = new Account();
		Set<AccountRole> roles = new HashSet<AccountRole>();
		AccountRole role = new AccountRole();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -1);

		role.setAccount(acct);
		role.setExpirationDate(calendar.getTime());
		role.setRoleStatus(RoleStatus.EXPIRED);
		role.setRoleType(RoleType.ROLE_STUDY);

		roles.add(role);
		acct.setAccountRoleList(roles);

		// Create the BRICS User object.
		gov.nih.tbi.commons.model.hibernate.User bricsUser = new gov.nih.tbi.commons.model.hibernate.User();

		bricsUser.setFirstName("John");
		bricsUser.setLastName("Doe");
		bricsUser.setId(Long.valueOf(-1));
		bricsUser.setEmail("jdoe@company.bogus");

		acct.setUser(bricsUser);

		// Populate the rest of the account fields.
		acct.setId(Long.valueOf(-1));
		acct.setUserName("jdoe");

		// Generate a fake password hash.
		String salt = HashMethods.getPasswordSalt();
		acct.setPassword(DigestUtils.sha256(salt + "thePassord"));
		acct.setSalt(salt);

		// +++++++++++++++++++++++++++++++++++++
		// ++++++++++ Begin the test. ++++++++++
		// +++++++++++++++++++++++++++++++++++++

		try {
			User proformsUser = new User(acct);
			Assert.assertFalse(proformsUser.isCreateStudy(),
					"The 'createStudy' field should be set to 'false' when the 'ROLE_STUDY' status has expired.");
		} catch (UnsupportedEncodingException e) {
			Assert.fail("Couldn't encode the password to a hex string.", e);
		}
	}
}
