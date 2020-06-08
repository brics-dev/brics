package gov.nih.tbi.account.service.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.service.ServiceConstants;

/**
 * Utility class for account module.
 * 
 * @author jim3
 */
public class AccountServiceUtil {

	/**
	 * Calculates the role status based on the given expiration date. the logic is as following:
	 * 1. If it's in the past, returns Expired
	 * 2. If it's in the future but within 30 days, returns Active - Expiring Soon
	 * 3. Otherwise, returns Active. 
	 *  
	 * @param expirationDate - expiration date
	 * @return Role status enum
	 */
	public static RoleStatus getRoleStatusFromExpDate(Date expirationDate) {

		Date currentDate = new Date();
		long currentTime = currentDate.getTime();
		long expirationTime = expirationDate.getTime();

		// expiration date is in the past, the role should be expired now.
		if (expirationTime < currentTime) {
			return RoleStatus.EXPIRED;
			
		} else { 
			// not yet expired, now we need to find out if the role should be active or expiring soon
			long diff = expirationTime - currentTime;
			int days = Math.round(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
			if (days >= 0 && days <= ServiceConstants.EXPIRATION_SOON_DAYS) {
				return RoleStatus.EXPIRING_SOON;
			} else {
				return RoleStatus.ACTIVE;
			}
		}
	}
	
}
