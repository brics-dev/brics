package gov.nih.brics.cas.logging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import gov.nih.brics.cas.flow.BricsLoginHandler;
import gov.nih.brics.cas.model.LimitedUser;
import gov.nih.brics.cas.model.SessionLog;

/**
 * Performs DAO/Manager like operations for the SessionLog in CAS. Because CAS doesn't have
 * the DAO/Manager separation in other files and there's very little logic, I chose to
 * combine them here.
 * 
 * @author Joshua Park(jospark)
 *
 */
public class SessionLogDbUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BricsLoginHandler.class);
	private static final String COLUMN_TGT = "tgt";
	private static final String COLUMN_USERNAME = "username";
	private static final String COLUMN_TIME_IN = "time_in";
	private static final String COLUMN_TIME_OUT = "time_out";
	private static final String COLUMN_USER_FULLNAME = "full_name";
	private static final String COLUMN_USER_EMAIL = "email";
	
	private static final String SQL_END_SESSION = "UPDATE session_log SET time_out = ? WHERE tgt = ? AND time_out IS NULL";
	private static final String SQL_START_SESSION = "INSERT INTO session_log (tgt, username, full_name, account_id, email, time_in, time_out) VALUES (?, ?, ?, ?, ?, ?, NULL)";
	private static final String SQL_USER_SESSIONS = "SELECT * FROM session_log WHERE username=? AND time_out IS NULL ORDER BY time_in";
	private static final String SQL_TGT_SESSION = "SELECT * FROM session_log WHERE tgt=? ORDER BY time_in DESC LIMIT 1";
	private static final String SQL_USER_DETAILS = "SELECT a.user_name as user_name, tu.first_name as first_name, tu.last_name as last_name, tu.email as email, a.id as account_id FROM account a LEFT JOIN tbi_user tu ON a.user_id = tu.id WHERE a.user_name = ?";
	private static final String SQL_DELETE_TWO_FA = "DELETE FROM two_factor_authentication WHERE account_id = (SELECT account_id FROM session_log WHERE tgt = ? and time_out IS NULL)";
	
	/**
	 * Get a session for a particular TGT. This allows calling code to verify a session exists
	 * or any other management process without modifying it. Note that this method gets the
	 * MOST RECENT use of the provided Ticket Granting Ticket (because TGTs can potentially be
	 * reused, this ensures only one result is obtained).
	 * 
	 * @param template JdbcTemplate that should be read from
	 * @param tgt the Ticket Granting Ticket to find
	 * @return SessionLog object populated with the session information if found, otherwise null
	 */
	public static final SessionLog getSession(JdbcTemplate template, String tgt) {
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("finding session log for TGT " + tgt);
		}
		
		// initialize log here, will instantiate if we have results
		SessionLog log = null;
		
		// get the LATEST log for the given TGT. It's possible TGTs duplicate (they're random)
		try {
			List<Map<String, Object>> rows = template.queryForList(SQL_TGT_SESSION, tgt);
			log = new SessionLog();
			
			for (Map<String, Object> row : rows) {
				log = mapSessionLogParams(row);
			}
		}
		catch(DataAccessException e) {
			// only log, fall through to return null
			LOGGER.error("DataAccessException thrown while obtaining session log for TGT " + tgt, e);
		}
		return log;
	}
	
	/**
	 * Searches for any open sessions for a particular user. This allows higher logic to end those
	 * before starting a new one. An open session is one that has an empty timeOut parameter.
	 * 
	 * @param template JdbcTemplate that should be read from
	 * @param username username on which to search
	 * @return List of SessionLogs matching the criteria or empty list if none found
	 */
	public static final List<SessionLog> getUserOpenSessions(JdbcTemplate template, String username) {
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("finding session log for username " + username);
		}
		List<SessionLog> logs = new ArrayList<SessionLog>();
		
		try {
			List<Map<String, Object>> rows = template.queryForList(SQL_USER_SESSIONS, username);
			for (Map<String, Object> row : rows) {
				logs.add(mapSessionLogParams(row));
			}
		}
		catch(DataAccessException e) {
			// log, fall through to return empty set. There's no better handling for this
			// until we get a notification system of some sort
			LOGGER.error("DataAccessException thrown while obtaining session log for username " + username, e);
		}

		return logs;
	}
	
	/**
	 * Get user details in order to fill in this information in the session log
	 * 
	 * @param template JdbcTemplate that should be read from
	 * @param username on which to search
	 * @return LimitedUser user details or null if not found or other error
	 */
	public static final LimitedUser getUserDetails(JdbcTemplate template, String username) {
		
		LimitedUser user = null;
		try {
			user = template.queryForObject(SQL_USER_DETAILS, new Object[] {username}, new LimitedUserRowMapper());
		}
		catch(DataAccessException e) {
			// log then fall through to return null (error condition)
			// this will catch the case where no rows are returned as well
			LOGGER.error("DataAccessException thrown (could be caused by not finding the row) while obtaining details for username " + username, e);
		}
		
		// DEBUG FOR CRIT-10266
		if (user.getId() == null) {
			System.out.println("FAILED TO FIND ACCOUNT ID: " + user.toString() + ". Searched by Username: " + username);
			
		}
		
		return user;
	}
	
	/**
	 * Inserts a beginning session log row into the database. This consists of a TGT, username,
	 * and starting time along with default values for the other fields. 0 is an error condition.
	 * 
	 * @param template JdbcTemplate that should be written to
	 * @param tgt the Ticket Granting Ticket to write
	 * @param username to write
	 * @return number of rows affected. 0 is an error condition.
	 */
	public static final int startSession(JdbcTemplate template, String tgt, String username) {
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("inserting session into database for username " + username);
		}
		int output = 0;
		
		try {
			// get user details, use those to fill in the insert
			LimitedUser user = getUserDetails(template, username);
			if (user == null) {
				// this is really super rare because we KNOW the username is in the db (because we wouldn't get here otherwise)
				// but just in case...
				return 0;
			}
			
			output = template.update(SQL_START_SESSION, tgt, username, user.getFullName(), user.getId(), user.getEmail(), new Date());
			// output should be 1 here.  A failure condition is an output of 0
		}
		catch(DataAccessException e) {
			// log, fall through to return 0. There's no better handling for this
			// until we get a notification system of some sort
			LOGGER.error("failed to start session for username " + username, e);
		}
		
		return output;
	}
	
	/**
	 * Finds the record matching the given TGT and update it with the correct ending time (now).
	 * 
	 * @param template JDBC template that should be written to
	 * @param tgt the Ticket Granting Ticket to end
	 * @return number of affected rows. 0 is an error condition
	 */
	public static final int endSession(JdbcTemplate template, String tgt) {
		SessionLogDbUtil.deleteTwoFa(template, tgt);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ending session in database for TGT " + tgt);
		}
		int output = 0;
		try {
			output = template.update(SQL_END_SESSION, new Date(), tgt);
			// output should be 1 here.  A failure condition is an output of 0
		}
		catch(DataAccessException e) {
			// log, fall through to return 0. There's no better handling for this
			// until we get a notification system of some sort
			LOGGER.error("failed to end session for TGT " + tgt, e);
		}
		return output;
	}
	
	/**
	 * Converts the JdbcTemplate result Map into a SessionLog
	 * 
	 * @param row input result Map
	 * @return SessionLog object filled with the db table's properties
	 */
	private static SessionLog mapSessionLogParams(Map<String, Object> row) {
		SessionLog log = new SessionLog();
		log.setTgt((String) row.get(COLUMN_TGT));
		log.setUsername((String) row.get(COLUMN_USERNAME));
		log.setFullName((String) row.get(COLUMN_USER_FULLNAME));
		log.setEmail((String) row.get(COLUMN_USER_EMAIL));
		 // TODO: the column here is a timestamp. make sure it converts properly
		log.setTimeIn((Date) row.get(COLUMN_TIME_IN));
		log.setTimeOut((Date) row.get(COLUMN_TIME_OUT));
		return log;
	}
	
	/**
	 * Finds account id with the given TGT and delete records associated to this account in two_fa_authentication table.
	 * 
	 * @param template JDBC template that should be written to
	 * @param tgt the Ticket Granting Ticket to end
	 * @return number of affected rows. 0 is an error condition
	 */
	public static final int deleteTwoFa(JdbcTemplate template, String tgt) {
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("delete two fa record for the account with tgt in session log when session ends" + tgt);
		}
		int output = 0;
		try {
			output = template.update(SQL_DELETE_TWO_FA, tgt);
			// output should be 1 here.  A failure condition is an output of 0
		}
		catch(DataAccessException e) {
			// log, fall through to return 0. There's no better handling for this
			// until we get a notification system of some sort
			LOGGER.error("failed to delete two factor for the acccount with TGT " + tgt, e);
		}
		return output;
	}
}
