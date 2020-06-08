package gov.nih.brics.auth.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.brics.auth.configuration.BricsConfiguration;
import gov.nih.brics.auth.data.entity.AccountUserDetails;
import gov.nih.brics.auth.data.repository.AccountRepository;
import gov.nih.brics.auth.util.HashMethods;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.AccountStatus;

@Service
public class AccountServiceImpl implements AccountService {
	
	private static final Logger logger = LogManager.getLogger();
	
	private Duration tempLockoutDuration = null;
	private static final String ROLE_UNSIGNED = "ROLE_UNSIGNED";
	
	@Autowired
	AccountRepository accountRepository;
	
	@Autowired
	BricsConfiguration config;
	
	@PostConstruct
	public void init() {
		tempLockoutDuration = Duration.ofMinutes(config.getTempLockoutMinutes());
	}
	
	@Transactional(readOnly = true)
	public Account findByUserName(String username) {
		return accountRepository.findByUserName(username).orElse(null);
	}
	
	@Transactional(readOnly = true)
	public Account findByUserId(Long id) {
		return accountRepository.findById(id).orElse(null);
	}
	
	/**
	 * Run a variety of validation tests on the account of the user logging in.
	 * 
	 * @param account - The account object of the user logging to be verified.
	 * @param userDetails - The user details object used for is built in validation methods.
	 * @param plainTxtPwd - The plain text password that was entered by the user.
	 * @throws AuthenticationException When any of the validation tests fails.
	 */
	@Override
	public void validateAccount(Account account, AccountUserDetails userDetails, String plainTxtPwd) {
		// Check if the account is disabled.
		if (isDisabled(account) || !userDetails.isEnabled()) {
			logger.error("The account is disabled for " + account.getUserName());
			throw new DisabledException("The account is disabled");
		}

		// Check if the account is locked.
		if (account.getIsCurrentLocked() || !userDetails.isAccountNonLocked()) {
			logger.error("The account is locked for " + account.getUserName());
			throw new LockedException("The account is locked");
		}

		// Check if the account has expired.
		if (!userDetails.isAccountNonExpired()) {
			logger.error("The account has expired for " + account.getUserName());
			throw new AccountExpiredException("The account has expired");
		}

		// Check if the user's credentials have expired.
		if (!userDetails.isCredentialsNonExpired()) {
			logger.error("The password has expired for " + account.getUserName());
			throw new CredentialsExpiredException("The password has expired");
		}

		// Check if the given password matches with the hashed version from the database.
		if (checkPasswordIsInvalid(account, plainTxtPwd)) {
			logger.error("The password is invalid for " + account.getUserName());

			// Record the failed login attempt.
			recordAndCheckInvalidLoginAttempt(account);

			// Create the error message to the user.
			String msg = null;

			if (!account.getIsCurrentLocked()) {
				if (account.getFailedConsecutiveCount() < config.getMaxTotalFailCount() - 1) {
					int attemptsRemaining =
							config.getMaxIntervalFailCount() - account.getFailedIntervalCount();
					msg = "The given credentials are invalid. You have " + attemptsRemaining + " attempt(s) remaining";
				} else {
					msg = "The given credentials are invalid. You have 1 more attempt before your account is permanently locked";
				}
			} else if ((account.getIsLocked() == null) || !account.getIsLocked()) {
				msg = "Your account is locked for the next " + config.getTempLockoutMinutes() + " minutes";
			} else {
				msg = "Your account is locked. Please reset your password to unlock your account";
			}

			throw new BadCredentialsException(msg);
		}
	}
	
	/**
	 * Run a username/password check against credentials coming from the BRICS application.
	 * The user should already be authenticated in BRICS at this point, and BRICS hashes the password here
	 * so we do not need to check locked, expired, etc.
	 * 
	 * This is marked as deprecated because it should not be used except in the one case where the user
	 * has to log in with an already-created CAS session.
	 * 
	 * @param account
	 * @param password
	 */
	@Deprecated
	public void validateAccountFromBRICS(Account account, String password) {
		if (checkPasswordIsInvalidFromBRICS(account, password)) {
			throw new BadCredentialsException("The password is invalid");
		}
	}
	
	public void recordAndCheckInvalidLoginAttempt(Account account) {
		// Double check if the account is already locked.
		if (account.getIsCurrentLocked()) {
			return;
		}

		int totalAttempts = account.getFailedConsecutiveCount() == null ? 1 : account.getFailedConsecutiveCount() + 1;
		int numIntervalAttempts = account.getFailedIntervalCount() == null ? 1 : account.getFailedIntervalCount() + 1;

		// First check if the max allowed login attempts have been reached.
		if (totalAttempts < config.getMaxTotalFailCount()) {
			// Set the start of the failed login attempt interval, if there is none already set.
			if (account.getFailedIntervalStart() == null) {
				account.setFailedIntervalStart(new Date());
			}

			Instant now = Instant.now();
			Instant loginAttemptStart = account.getFailedIntervalStart().toInstant();

			// Check if this failed attempt is with in the lockout period.
			if (ChronoUnit.MINUTES.between(loginAttemptStart, now) <= config.getTempLockoutIntervalWindow()) {
				// Check if the max consecutive login attempts have been reached.
				if (numIntervalAttempts >= config.getMaxIntervalFailCount()) {
					// Set the temporary locked date
					Instant tempLockoutTime = now.plus(tempLockoutDuration);

					account.setLockedUntil(Date.from(tempLockoutTime));
					numIntervalAttempts = 0;
					account.setFailedIntervalStart(null);
				}
			} else {
				// This attempt is outside of the check interval. Reset initial date and counter.
				account.setFailedIntervalStart(Date.from(now));
				numIntervalAttempts = 1;
			}
		} else {
			// Set the permanent lock flag for the account.
			account.setIsLocked(Boolean.TRUE);
		}

		// Update failed attempt counters on the Account object.
		account.setFailedConsecutiveCount(Integer.valueOf(totalAttempts));
		account.setFailedIntervalCount(Integer.valueOf(numIntervalAttempts));

		// Persist account changes to the database.
		accountRepository.save(account);
	}
	
	/**
	 * Determine if this account is either disabled or inactive.
	 * 
	 * @return True if and only if this account is currently disabled or inactive.
	 */
	public boolean isDisabled(Account account) {
		// Note: If the account is in the requested state it should override the active check.
		return !(account.getIsActive() || account.getAccountStatus() == AccountStatus.REQUESTED || account.getAccountStatus() == AccountStatus.PENDING);
	}
	
	/**
	 * Get the disease key for the current instance.
	 * 
	 * @return
	 */
	public Long getDiseaseKey() {
		Long diseaseKey = config.getDefaultDisease();
		String orgName = config.getOrgName();

		// Try to determine the disease key.
		if ("pdbp".equalsIgnoreCase(orgName)) {
			diseaseKey = Long.valueOf(5L);
		} else if ("fitbir".equalsIgnoreCase(orgName)) {
			diseaseKey = Long.valueOf(8L);
		} else if ("cnrm".equalsIgnoreCase(orgName)) {
			diseaseKey = Long.valueOf(7L);
		}

		return diseaseKey;
	}
	
	public boolean checkPasswordIsInvalid(Account account, String password) {
		if (account == null || account.getPassword() == null) {
			return true;
		}

		// Check if provided password is not the same as current password
		return !Arrays.equals(account.getPassword(), hashPassword(account.getSalt() + password));
	}
	
	public boolean checkPasswordIsInvalidFromBRICS(Account account, String password) {
		if (account == null || account.getPassword() == null) {
			return true;
		}
		
		String accountPassword = HashMethods.getServerHash(account.getUserName(), HashMethods.convertFromByte(account.getPassword()));
		return !password.equals(accountPassword);
	}
	
	public byte[] hashPassword(String password) {

		// Way to complicated method to fill a Byte array with the encrypted password
		byte[] raw = null;
		try {
			raw = MessageDigest.getInstance("SHA-256").digest(password.getBytes());
			byte[] passwordArray = new byte[raw.length];
			for (int i = 0; i < raw.length; i++) {
				passwordArray[i] = raw[i];
			}
			return passwordArray;
		} catch (NoSuchAlgorithmException e) {
			logger.error("There was an exception in the AccountService hashPassword(): " + e.getLocalizedMessage(), e);
		}
		return raw;
	}
	
	/**
	 * Convenience method to convert AccountRoles into GrantedAuthorities
	 * 
	 * @param accountRoleList
	 * @return
	 */
	public static Collection<GrantedAuthority> getAuthorities(Account account) {

		Set<GrantedAuthority> authorities = new HashSet<>();
		
		if (account.getBricsESignature() == null) {
			authorities.add(new SimpleGrantedAuthority(ROLE_UNSIGNED));
			return authorities;
		}

		Date currentDate = new Date();

		for (AccountRole ar : account.getAccountRoleList()) {
			if (ar.getIsActive() && (ar.getExpirationDate() == null || currentDate.before(ar.getExpirationDate()))) {
				authorities.add(new SimpleGrantedAuthority(ar.getRoleType().getName()));
			}
		}

		return authorities;
	}
}
