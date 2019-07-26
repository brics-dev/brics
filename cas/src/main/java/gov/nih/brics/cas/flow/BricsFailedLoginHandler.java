package gov.nih.brics.cas.flow;

import java.util.Date;
import java.util.Map;

import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.webflow.execution.RequestContext;

import gov.nih.brics.cas.model.FailedAttemptInfo;

public class BricsFailedLoginHandler extends BricsLoginHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(BricsFailedLoginHandler.class);

	private final String ERROR_LOCKED = "errorLocked";
	private final String ERROR_TEMP_LOCKED = "errorTempLocked";
	private final String WARNING_ATTEMPTS = "warningAttempts";

	// ---------------------SQL---------------------
	private final String FAIL_INFO_SQL =
			"SELECT failed_interval_count, failed_consecutive_count, failed_interval_start from account where user_name = ?";

	// ------ Properties from bean config -----
	private int intervalFailThreshold;
	private int intervalFailSeconds;
	private int intervalFailLockSeconds;
	private int consecutiveFailThreshold;
	private int intervalFailMilliseconds;
	private int intervalFailLockMilliseconds;


	// ----- GETTERS/SETTERS -----
	public int getIntervalFailThreshold() {
		return intervalFailThreshold;
	}

	public void setIntervalFailThreshold(int intervalFailThreshold) {
		this.intervalFailThreshold = intervalFailThreshold;
	}

	public int getIntervalFailSeconds() {
		return intervalFailSeconds;
	}

	public void setIntervalFailSeconds(int intervalFailSeconds) {
		this.intervalFailSeconds = intervalFailSeconds;
		this.intervalFailMilliseconds = intervalFailSeconds * 1000;
	}

	public int getIntervalFailLockSeconds() {
		return intervalFailLockSeconds;
	}

	public void setIntervalFailLockSeconds(int intervalFailLockSeconds) {
		this.intervalFailLockSeconds = intervalFailLockSeconds;
		this.intervalFailLockMilliseconds = intervalFailLockSeconds * 1000;
	}

	public int getConsecutiveFailThreshold() {
		return consecutiveFailThreshold;
	}

	public void setConsecutiveFailThreshold(int consecutiveFailThreshold) {
		this.consecutiveFailThreshold = consecutiveFailThreshold;
	}

	public String submit(final RequestContext context, final UsernamePasswordCredential usernamePasswordCredential,
			final MessageContext messageContext) throws Exception {
		String username = getPrincipalNameTransformer().transform(usernamePasswordCredential.getUsername());

		FailedAttemptInfo failedAttemptInfo = null;

		try {
			failedAttemptInfo = initializeFailureState(username);
		} catch (IncorrectResultSizeDataAccessException e) {
			// this usually means the username just doesn't exist in the database. We will print stack trace just in
			// case
			// and return success to continue down the webflow.
			e.printStackTrace();
			return "success";
		}

		advanceFailureState(failedAttemptInfo, username);

		int failedConsecutiveCount = failedAttemptInfo.getFailedConsecutiveCount();
		int failedIntervalCount = failedAttemptInfo.getFailedIntervalCount();
		Date failedIntervalStart = failedAttemptInfo.getFailedIntervalStart();

		// if we are over the consecutive login limit
		if (failedConsecutiveCount >= consecutiveFailThreshold) {
			updateAccountLock(true, username);
			LOGGER.debug("Locking the account: " + username);
			return ERROR_LOCKED;
		} else if (withinFailureInterval(failedIntervalStart) && failedIntervalCount >= intervalFailThreshold) {
			long currentTimeMilliseconds = new Date().getTime();
			Date lockedUntilDate = new Date(currentTimeMilliseconds + intervalFailLockMilliseconds);
			updateAccountLockedUntil(lockedUntilDate, username);

			int minutesLocked = intervalFailLockSeconds / 60;

			LOGGER.debug("Account: " + username + " is locked for " + minutesLocked + " minutes!");
			context.getFlowScope().put("minutesLocked", minutesLocked);

			return ERROR_TEMP_LOCKED;
		} else {
			// add the message for the number of login attempts left
			int attemptsLeft = intervalFailThreshold - failedIntervalCount;
			messageContext
					.addMessage(new MessageBuilder().error().code("brics.attempts.warning").arg(attemptsLeft).build());
			LOGGER.debug("Warning the user that there are " + attemptsLeft + " log in attempts remaining.");
			return WARNING_ATTEMPTS;
		}
	}

	private boolean withinFailureInterval(Date failedIntervalStart) {
		long currentTimeMilliseconds = new Date().getTime();

		if (failedIntervalStart != null
				&& (currentTimeMilliseconds - failedIntervalStart.getTime()) < intervalFailMilliseconds) {
			return true;
		}

		return false;
	}

	private void advanceFailureState(FailedAttemptInfo failedAttemptInfo, String username) {
		int failedConsecutiveCount = failedAttemptInfo.getFailedConsecutiveCount();
		int failedIntervalCount = failedAttemptInfo.getFailedIntervalCount();
		Date failedIntervalStart = failedAttemptInfo.getFailedIntervalStart();

		failedConsecutiveCount++;
		failedAttemptInfo.setFailedConsecutiveCount(failedConsecutiveCount);
		updateConsecutiveCount(failedConsecutiveCount, username);

		// if we are within the previously set failure interval, increment the interval count, otherwise we want to
		// create a new interval start and set the count back to 0.
		if (withinFailureInterval(failedIntervalStart)) {
			failedIntervalCount++;
			failedAttemptInfo.setFailedIntervalCount(failedIntervalCount);
			updateIntervalCount(failedIntervalCount, username);
		} else {
			failedIntervalCount = 1;
			failedAttemptInfo.setFailedIntervalCount(failedIntervalCount);
			updateIntervalCount(failedIntervalCount, username);
			updateIntervalStart(new Date(), username);
		}
	}

	/**
	 * Initializes all the fields needed that come from the database
	 * 
	 * @param username
	 */
	private FailedAttemptInfo initializeFailureState(String username) {
		FailedAttemptInfo failedAttemptInfo = new FailedAttemptInfo();
		Map<String, Object> failureInfoMap = null;

		try {
			failureInfoMap = getJdbcTemplate().queryForMap(FAIL_INFO_SQL, username);
		} catch (IncorrectResultSizeDataAccessException e) {
			throw new IncorrectResultSizeDataAccessException(e.getExpectedSize(), e.getActualSize());
		}

		Object failedIntervalCountObj = failureInfoMap.get(FAILED_INTERVAL_COUNT_COLUMN);

		if (failedIntervalCountObj != null && failedIntervalCountObj instanceof Integer) {
			Integer failedIntervalCount = (Integer) failedIntervalCountObj;
			failedAttemptInfo.setFailedIntervalCount(failedIntervalCount);
		}

		Object failedConsecutiveCountObj = failureInfoMap.get(FAILED_CONSECUTIVE_COUNT_COLUMN);

		if (failedConsecutiveCountObj != null && failedConsecutiveCountObj instanceof Integer) {
			Integer failedConsecutiveCount = (Integer) failedConsecutiveCountObj;
			failedAttemptInfo.setFailedConsecutiveCount(failedConsecutiveCount);
		}

		Object failedIntervalStartObj = failureInfoMap.get(FAILED_INTERVAL_START_COLUMN);

		if (failedIntervalStartObj != null && failedIntervalStartObj instanceof Date) {
			Date failedIntervalStart = (Date) failedIntervalStartObj;
			failedAttemptInfo.setFailedIntervalStart(failedIntervalStart);
		}

		return failedAttemptInfo;
	}
}
