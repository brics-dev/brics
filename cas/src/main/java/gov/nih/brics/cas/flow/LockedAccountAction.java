package gov.nih.brics.cas.flow;

import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;

import org.jasig.cas.adaptors.jdbc.AbstractJdbcUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageContext;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.webflow.execution.RequestContext;

import gov.nih.brics.cas.model.LockInfo;

public class LockedAccountAction extends AbstractJdbcUsernamePasswordAuthenticationHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(LockedAccountAction.class);

	// ---------------------SQL---------------------
	private final String LOCK_INFO_SQL =
			"SELECT is_locked, locked_until, failed_interval_count, failed_consecutive_count, failed_interval_start from account where user_name = ?";
	private final String IS_LOCKED_COLUMN = "is_locked";
	private final String LOCKED_UNTIL_COLUMN = "locked_until";
	// --------------------END SQL--------------------

	private final String ERROR_LOCKED = "errorLocked";
	private final String ERROR_TEMP_LOCKED = "errorTempLocked";

	/**
	 * This method checks if the current user's account is locked. Go to an error locked page if it is locked.
	 * 
	 * @param context
	 * @param usernamePasswordCredential
	 * @param messageContext
	 * @return
	 * @throws Exception
	 */
	public String submit(final RequestContext context, final UsernamePasswordCredential usernamePasswordCredential,
			final MessageContext messageContext) throws Exception {
		String username = getPrincipalNameTransformer().transform(usernamePasswordCredential.getUsername());
		LockInfo accountLockInfo = null;

		try {
			accountLockInfo = initializeLockInfo(username);
		} catch (IncorrectResultSizeDataAccessException e) {
			// this usually means the username just doesn't exist in the database. We will printstack trace just in case
			// and return success to continue down the webflow.
			e.printStackTrace();
			return "success";
		}

		boolean isLocked = accountLockInfo.isLocked();
		Date lockedUntil = accountLockInfo.getLockedUntil();

		if (isLocked) {
			LOGGER.debug("Account: " + username + " is locked!");
			return ERROR_LOCKED;
		}

		Date currentDate = new Date();

		if (lockedUntil != null && currentDate.before(lockedUntil)) {
			long minutesLocked = (lockedUntil.getTime() - currentDate.getTime()) / 60000;
			LOGGER.debug("Account: " + username + " is locked for " + minutesLocked + " minutes!");
			context.getFlowScope().put("minutesLocked", minutesLocked);
			return ERROR_TEMP_LOCKED;
		}

		return "success";
	}

	/**
	 * Initializes all the fields needed from the database
	 * 
	 * @param username
	 */
	private LockInfo initializeLockInfo(String username) throws IncorrectResultSizeDataAccessException {
		LockInfo accountLockInfo = new LockInfo();
		Map<String, Object> lockInfoMap = null;

		try {
			lockInfoMap = getJdbcTemplate().queryForMap(LOCK_INFO_SQL, username);
		} catch (IncorrectResultSizeDataAccessException e) {
			throw new IncorrectResultSizeDataAccessException(e.getExpectedSize(), e.getActualSize());
		}

		Object isLockedObj = lockInfoMap.get(IS_LOCKED_COLUMN);

		if (isLockedObj != null && isLockedObj instanceof Boolean) {
			Boolean isLocked = (Boolean) isLockedObj;
			accountLockInfo.setLocked(isLocked);
		} else {
			accountLockInfo.setLocked(false);
		}

		Object lockedUntilObj = lockInfoMap.get(LOCKED_UNTIL_COLUMN);

		if (lockedUntilObj != null && lockedUntilObj instanceof Date) {
			Date lockedUntil = (Date) lockedUntilObj;
			accountLockInfo.setLockedUntil(lockedUntil);
		}

		return accountLockInfo;
	}

	/**
	 * This class is not used, and should never be called. This function exists so that this Action can extend a class
	 * with the exact jdbc support we need.
	 */
	@Override
	protected HandlerResult authenticateUsernamePasswordInternal(UsernamePasswordCredential arg0)
			throws GeneralSecurityException, PreventedException {
		return null;
	}
}
