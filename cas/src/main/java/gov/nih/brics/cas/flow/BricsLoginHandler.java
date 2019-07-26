package gov.nih.brics.cas.flow;

import java.security.GeneralSecurityException;
import java.util.Date;

import org.jasig.cas.adaptors.jdbc.AbstractJdbcUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class BricsLoginHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(BricsLoginHandler.class);

	protected final String FAILED_INTERVAL_COUNT_COLUMN = "failed_interval_count";
	protected final String FAILED_CONSECUTIVE_COUNT_COLUMN = "failed_consecutive_count";
	protected final String FAILED_INTERVAL_START_COLUMN = "failed_interval_start";
	protected final String IS_LOCKED_COLUMN = "is_locked";
	protected final String LOCKED_UNTIL_COLUMN = "locked_until";

	private void updateAccountByUsername(String columnName, Object value, String username) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Setting column: " + columnName + " to " + value + " for " + username);
		}

		String sql_template = "UPDATE account SET %s=? WHERE user_name=?";

		String sql = String.format(sql_template, columnName);
		try {
			getJdbcTemplate().update(sql, value, username);
		} catch (DataAccessException e) {
			LOGGER.error("DataAccessException thrown while updating " + columnName + ". Exception follows");
			e.printStackTrace();
		}
	}
	
	/**
	 * Update the account locked_until column with the given username and date
	 * @param lockedUntil
	 * @param username
	 */
	protected void updateAccountLockedUntil(Date lockedUntil, String username) {
		updateAccountByUsername(LOCKED_UNTIL_COLUMN, lockedUntil, username);
	}

	/**
	 * Update the account is_locked column with the given username and boolean
	 * @param lockedUntil
	 * @param username
	 */
	protected void updateAccountLock(boolean isLocked, String username) {
		updateAccountByUsername(IS_LOCKED_COLUMN, isLocked, username);
	}

	/**
	 * Update the account failed_interval_start column with the given username and date
	 * @param lockedUntil
	 * @param username
	 */
	protected void updateIntervalStart(Date intervalStart, String username) {
		updateAccountByUsername(FAILED_INTERVAL_START_COLUMN, intervalStart, username);
	}

	/**
	 * Update the account failed_interval_count column with the given username and count integer
	 * @param lockedUntil
	 * @param username
	 */
	protected void updateIntervalCount(int intervalCount, String username) {
		updateAccountByUsername(FAILED_INTERVAL_COUNT_COLUMN, intervalCount, username);

	}

	/**
	 * Update the account failed_consecutive_count column with the given username and count integer
	 * @param lockedUntil
	 * @param username
	 */
	protected void updateConsecutiveCount(int consecutiveCount, String username) {
		updateAccountByUsername(FAILED_CONSECUTIVE_COUNT_COLUMN, consecutiveCount, username);
	}

	/**
	 * This class is not used, and should never be called. This function exists so that this Action
	 * can extend a class with the exact jdbc support we need.
	 */
	@Override
	protected HandlerResult authenticateUsernamePasswordInternal(UsernamePasswordCredential arg0)
			throws GeneralSecurityException, PreventedException {
		// TODO Auto-generated method stub
		return null;
	}

}
