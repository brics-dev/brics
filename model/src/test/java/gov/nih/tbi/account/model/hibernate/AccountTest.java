package gov.nih.tbi.account.model.hibernate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.account.model.EmailReportType;



public class AccountTest {
	
	@Test
	public void testRolesWithStatus() {
		
		AccountRole expiredRole = new AccountRole();
		AccountRole expiringRole = new AccountRole();
		AccountRole inactiveRole = new AccountRole();
		AccountRole withdrawnRole = new AccountRole();
		AccountRole activeRole = new AccountRole();
		AccountRole expiredRole2 = new AccountRole();
		
		expiredRole.setRoleStatus(RoleStatus.EXPIRED);
		expiringRole.setRoleStatus(RoleStatus.EXPIRING_SOON);
		inactiveRole.setRoleStatus(RoleStatus.INACTIVE);
		withdrawnRole.setRoleStatus(RoleStatus.WITHDRAWN);
		activeRole.setRoleStatus(RoleStatus.ACTIVE);
		expiredRole2.setRoleStatus(RoleStatus.EXPIRED);
				
		Set<AccountRole> accountRolesList = new HashSet<AccountRole>();
		Set<AccountRole> accountRolesListDesired = new HashSet<AccountRole>();
		
		accountRolesList.add(expiredRole);
		accountRolesList.add(expiringRole);
		accountRolesList.add(inactiveRole);
		accountRolesList.add(withdrawnRole);
		accountRolesList.add(activeRole);
		accountRolesList.add(expiredRole2);
		
		accountRolesListDesired.add(expiredRole);
		accountRolesListDesired.add(expiringRole);
		accountRolesListDesired.add(expiredRole2);

		
		List<RoleStatus> statuses = Arrays.asList(RoleStatus.EXPIRED,RoleStatus.EXPIRING_SOON);
		
		Account acc = new Account();
		acc.setAccountRoleList(accountRolesList);
		acc.getRolesWithStatus(statuses);
		
		Assert.assertEquals(acc.getRolesWithStatus(statuses), accountRolesListDesired);
	}
	
	@Test
	public void testAccountRoleList() {
		
		AccountRole expiredRole = new AccountRole();
		AccountRole expiringRole = new AccountRole();
		String expectedString = "ROLE_ADMIN(Active - Expiring Soon), ROLE_USER(Expired)";
		String roleListDisplay;
		
		expiredRole.setRoleStatus(RoleStatus.EXPIRED);
		expiredRole.setRoleType(RoleType.ROLE_USER);
		expiringRole.setRoleStatus(RoleStatus.EXPIRING_SOON);
		expiringRole.setRoleType(RoleType.ROLE_ADMIN);
				
		Set<AccountRole> accountRolesList = new HashSet<AccountRole>();
		
		accountRolesList.add(expiredRole);
		accountRolesList.add(expiringRole);
		
		Account acc = new Account();
		acc.setAccountRoleList(accountRolesList);
		roleListDisplay = acc.getDisplayAccountRoleList();
		Assert.assertTrue(expectedString.equals(roleListDisplay));
	}
	
	@Test
	public void testExpirationDate() {
		
		AccountRole expiredRole = new AccountRole();
		AccountRole expiringRole = new AccountRole();
		AccountRole expiredRole2 = new AccountRole();
		
		expiredRole.setRoleStatus(RoleStatus.EXPIRED);
		expiringRole.setRoleStatus(RoleStatus.EXPIRING_SOON);
		expiredRole2.setRoleStatus(RoleStatus.EXPIRED);

		
		expiredRole.setExpirationDate(new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime());
		expiringRole.setExpirationDate(new GregorianCalendar(2020, Calendar.MARCH, 11).getTime());
		expiredRole2.setExpirationDate(new GregorianCalendar(2014, Calendar.MARCH, 11).getTime());
		
		Account acc = new Account();

		Set<AccountRole> expiringAccountRoles = new HashSet<AccountRole>();
		expiringAccountRoles.add(expiredRole);
		expiringAccountRoles.add(expiringRole);
		expiringAccountRoles.add(expiredRole2);
		
		acc.setAccountRoleList(expiringAccountRoles);

		String expirationDateExpected = BRICSTimeDateUtil.formatDate(new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime());
		String expirationDateActual = acc.getExpirationDate();
		
		Assert.assertEquals(expirationDateActual, expirationDateExpected);
	}
	
	@Test
	public void testRemoveEmailReportSettings() {
		
		Account acc = new Account();
		AccountEmailReportSetting test1 = new AccountEmailReportSetting();
		AccountEmailReportSetting test2 = new AccountEmailReportSetting();
		AccountEmailReportSetting test3 = new AccountEmailReportSetting();
		
		test1.setReportType(EmailReportType.ACCOUNT_RENEWAL);
		test2.setReportType(EmailReportType.ACCOUNT_RENEWAL);
		test3.setReportType(EmailReportType.ACCOUNT_RENEWAL);

		Set<AccountEmailReportSetting> accountEmailReportSettings = new HashSet<AccountEmailReportSetting>();
		accountEmailReportSettings.add(test1);
		accountEmailReportSettings.add(test2);
		accountEmailReportSettings.add(test3);

		acc.setAccountEmailReportSettings(accountEmailReportSettings);
		acc.removeAccountEmailReportSetting(EmailReportType.ACCOUNT_RENEWAL);
		
		Assert.assertEquals(acc.getAccountEmailReportSettings(),new HashSet<AccountEmailReportSetting>());
		
	}
	
	@Test
	public void testRemoveEmailReportSettingsLeftover() {
		
		Account acc = new Account();
		AccountEmailReportSetting test1 = new AccountEmailReportSetting();
		AccountEmailReportSetting test2 = new AccountEmailReportSetting();
		AccountEmailReportSetting test3 = new AccountEmailReportSetting();
		
		test1.setReportType(EmailReportType.ACCOUNT_RENEWAL);
		test2.setReportType(EmailReportType.ACCOUNT_RENEWAL);
		test3.setReportType(EmailReportType.ACCOUNT_REQUEST);

		Set<AccountEmailReportSetting> accountEmailReportSettings = new HashSet<AccountEmailReportSetting>();
		Set<AccountEmailReportSetting> expectedEmailReportSettings = new HashSet<AccountEmailReportSetting>();
		accountEmailReportSettings.add(test1);
		accountEmailReportSettings.add(test2);
		accountEmailReportSettings.add(test3);
		expectedEmailReportSettings.add(test3);

		acc.setAccountEmailReportSettings(accountEmailReportSettings);
		acc.removeAccountEmailReportSetting(EmailReportType.ACCOUNT_RENEWAL);
		
		Assert.assertEquals(acc.getAccountEmailReportSettings(),expectedEmailReportSettings);
		
	}
	
	@Test
	public void testUpdateEmailReportSettingsAdd() {
		
		Account acc = new Account();
		Account acc2 = new Account();
		AccountEmailReportSetting test1 = new AccountEmailReportSetting();
		AccountEmailReportSetting test2 = new AccountEmailReportSetting();
		AccountEmailReportSetting test3 = new AccountEmailReportSetting();
		
		test1.setReportType(EmailReportType.ACCOUNT_RENEWAL);
		test2.setReportType(EmailReportType.ACCOUNT_RENEWAL);
		test3.setReportType(EmailReportType.ACCOUNT_REQUEST);

		Set<AccountEmailReportSetting> accountEmailReportSettings = new HashSet<AccountEmailReportSetting>();
		accountEmailReportSettings.add(test1);
		
		Set<AccountEmailReportSetting> accountEmailReportSettings2 = new HashSet<AccountEmailReportSetting>();
		accountEmailReportSettings2.add(test1);
		accountEmailReportSettings2.add(test3);

		acc.setAccountEmailReportSettings(accountEmailReportSettings);
		acc2.setAccountEmailReportSettings(accountEmailReportSettings2);

		acc.updateAccountEmailReportSetting(test3);
		
		Assert.assertTrue(acc.getAccountEmailReportSettings().equals(acc2.getAccountEmailReportSettings()));
		
	}
	
	@Test
	public void testUpdateEmailReportSettingsTrim() {
		
		Account acc = new Account();
		Account acc2 = new Account();
		AccountEmailReportSetting test1 = new AccountEmailReportSetting();
		AccountEmailReportSetting test2 = new AccountEmailReportSetting();
		AccountEmailReportSetting test3 = new AccountEmailReportSetting();
		
		test1.setReportType(EmailReportType.ACCOUNT_RENEWAL);
		test2.setReportType(EmailReportType.ACCOUNT_RENEWAL);
		test3.setReportType(EmailReportType.ACCOUNT_REQUEST);

		Set<AccountEmailReportSetting> accountEmailReportSettings = new HashSet<AccountEmailReportSetting>();
		accountEmailReportSettings.add(test1);
		accountEmailReportSettings.add(test3);
		
		Set<AccountEmailReportSetting> accountEmailReportSettings2 = new HashSet<AccountEmailReportSetting>();
		accountEmailReportSettings2.add(test1);
		accountEmailReportSettings2.add(test1);
		accountEmailReportSettings2.add(test2);
		accountEmailReportSettings2.add(test3);

		acc.setAccountEmailReportSettings(accountEmailReportSettings);
		acc2.setAccountEmailReportSettings(accountEmailReportSettings2);

		acc.updateAccountEmailReportSetting(test3);
		
		Assert.assertTrue(acc.getAccountEmailReportSettings().equals(acc2.getAccountEmailReportSettings()));
		
	}

}
