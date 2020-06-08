package gov.nih.tbi.account.service.util;

import static org.testng.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.service.ServiceConstants;

public class AccountServiceUtilTest {

	@BeforeMethod
	public void init() {}

	@Test
	public void getRoleStatusFromExpDateTest() {
		
		// expiration date is yesterday, roleStatus should be expired
		Calendar cal1 = Calendar.getInstance();
		cal1.add(Calendar.DATE, -1);
		Date expiredDate = cal1.getTime();
		assertEquals(AccountServiceUtil.getRoleStatusFromExpDate(expiredDate), RoleStatus.EXPIRED);
		
		// expiration date is 30 days away, roleStatus should be Expiring Soon
		Calendar cal2 = Calendar.getInstance();
		cal2.add(Calendar.DATE, ServiceConstants.EXPIRATION_SOON_DAYS);
		Date expiringSoonDate = cal2.getTime();
		assertEquals(AccountServiceUtil.getRoleStatusFromExpDate(expiringSoonDate), RoleStatus.EXPIRING_SOON);
		
		// expiration date is 31 days away, should return active
		Calendar cal3 = Calendar.getInstance();
		cal3.add(Calendar.DATE, ServiceConstants.EXPIRATION_SOON_DAYS + 1);
		Date activeDate = cal3.getTime();
		assertEquals(AccountServiceUtil.getRoleStatusFromExpDate(activeDate), RoleStatus.ACTIVE);
	}
	
}
