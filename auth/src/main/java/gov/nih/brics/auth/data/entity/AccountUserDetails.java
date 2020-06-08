package gov.nih.brics.auth.data.entity;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import gov.nih.brics.auth.configuration.BricsConfiguration;
import gov.nih.brics.auth.service.AccountServiceImpl;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;

public class AccountUserDetails implements UserDetails {
	private static final long serialVersionUID = 2805511370340453429L;
	public static final String HEXES = "0123456789abcdef";

	private BricsConfiguration config;
	
	private Account account;
	private String salt;
	private Long diseaseId;

	public AccountUserDetails(Account account, Long diseaseId, BricsConfiguration config) {
		this.config = config;
		this.account = account;
		this.diseaseId = diseaseId;
		this.salt = account.getSalt();
	}

	/**
	 * Constructor requires an Account and a diseaseId to identify where the user is registered
	 * 
	 * @param account
	 * @param diseaseId
	 * @deprecated
	 */
	@Deprecated
	public AccountUserDetails(Account account, Long diseaseId) {

		// fall back to using a default config
		this.config = new BricsConfiguration();
		this.config.setAccountExpirationDays(120);
		this.account = account;
		this.diseaseId = diseaseId;
		this.salt = account.getSalt();
	}

	/**
	 * Consructor takes an account but leaves the diseaseId null
	 * 
	 * @param account
	 * @deprecated
	 */
	@Deprecated
	public AccountUserDetails(Account account) {

		this.account = account;
		this.diseaseId = null;
		this.salt = account.getSalt();
	}

	public Account getAccount() {

		return account;
	}

	public Long getDiseaseId() {

		return diseaseId;
	}

	/**
	 * Returns the roles an account has in the form that Spring Security is expecting
	 */
	public Collection<GrantedAuthority> getAuthorities() {

		return AccountServiceImpl.getAuthorities(account);
	}

	/**
	 * This method returns a HEX representation of the bit array that is stored in the database
	 */
	public String getPassword() {

		String temp = null;

		byte[] raw = account.getPassword();
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
		}

		temp = hex.toString();

		return temp;
	}

	/**
	 * Returns the username of the account
	 */
	public String getUsername() {

		return account.getUserName();
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {

		this.salt = salt;
	}

	/**
	 * Checks to see if the user has logged in in the last 365 days, and if they have a valid 'USER_ROLE'
	 */
	public boolean isAccountNonExpired() {

		AccountRole adminRole = null;

		// A user must have an active "USER_ROLE" or "ADMIN_ROLE"
		{
			AccountRole userRole = null;

			for (AccountRole ar : account.getAccountRoleList()) {
				// Check for USER_ROLE
				if (RoleType.ROLE_USER.equals(ar.getRoleType())) {
					userRole = ar;

					// Exit loop if both are set
					if (adminRole != null) {
						break;
					}
				}

				// CHECK FOR ADMIN_ROLE
				if (RoleType.ROLE_ADMIN.equals(ar.getRoleType())) {
					adminRole = ar;

					// Exit loop if both are set
					if (userRole != null) {
						break;
					}
				}
			}

			// A user must have logged in within the last ## of days (unless they are an admin)
			if (account.getLastSuccessfulLogin() != null && (adminRole == null
					|| !adminRole.getIsActive() || adminRole.isExpired())) {
				Calendar cal = Calendar.getInstance();

				cal.setTime(account.getLastSuccessfulLogin());

				// add ## number of days
				cal.add(Calendar.DAY_OF_YEAR, config.getAccountExpirationDays());

				// return false if current date is after the modified date
				if ((new Date()).after(cal.getTime())) {
					return false;
				}
			}

			// If the user is really an admin ignore
			if (adminRole == null || !adminRole.getIsActive() || adminRole.isExpired()) {
				// If the user doesn't have a user role, that is active and not expired then return false
				if (userRole == null || (!userRole.getIsActive()
						&& !RoleStatus.PENDING.equals(userRole.getRoleStatus())) || userRole.isExpired()) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Checks to see if there is an unlock date and if that date is in the past
	 * 
	 * @return true if the account is not locked
	 */
	public boolean isAccountNonLocked() {

		if (account.getUnlockDate() != null && (new Date()).before(account.getUnlockDate())) {
			return false;
		}

		return true;
	}

	/**
	 * Checks to see if the user's password is expired
	 * 
	 * @return true if the password is not expired
	 */
	public boolean isCredentialsNonExpired() {

		if (account.getPasswordExpirationDate() != null) {
			if (account.getPasswordExpirationDate().before(new Date())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * This method is used by spring security to determine if the account should be allowed to log in.
	 */
	public boolean isEnabled() {
		if (AccountStatus.ACTIVE.equals(account.getAccountStatus()) && account.getIsActive() != null
				&& account.getIsActive()) {
			return true;
		} else if (AccountStatus.REQUESTED.equals(account.getAccountStatus())) {
			return true;
		} else if (AccountStatus.PENDING.equals(account.getAccountStatus())) {
			return true;
		} else if (AccountStatus.CHANGE_REQUESTED.equals(account.getAccountStatus())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccountUserDetails other = (AccountUserDetails) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} 
		else if (!account.equals(other.account)) {
			return false;
		}
		return true;
	}

}
