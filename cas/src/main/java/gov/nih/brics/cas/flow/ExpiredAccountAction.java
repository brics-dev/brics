
package gov.nih.brics.cas.flow;

import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.Date;

import org.jasig.cas.adaptors.jdbc.AbstractJdbcUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageContext;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.webflow.execution.RequestContext;

/**
 * Part of the spring workflow (similar to ExpiredPasswordAction). Checks verify that the user
 * account is not expired.
 * 
 * @author mvalei
 * 
 */
public class ExpiredAccountAction extends AbstractJdbcUsernamePasswordAuthenticationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpiredAccountAction.class);

	public String submit(final RequestContext context, final UsernamePasswordCredential usernamePasswordCredential,
			final MessageContext messageContext) throws Exception {

		final String username = getPrincipalNameTransformer().transform(usernamePasswordCredential.getUsername());
		final String sqlExpired =
				"SELECT account_role.expiration_date FROM account_role INNER JOIN account ON account_role.account_id=account.id WHERE account.user_name=? AND role_type_id=0;";

		Date goBadDate = null;
		try {
			goBadDate = getJdbcTemplate().queryForObject(sqlExpired, Timestamp.class, username);
		} catch (IncorrectResultSizeDataAccessException e) {
			throw new IncorrectResultSizeDataAccessException(e.getExpectedSize(), e.getActualSize());
		}

		// Determine if account's user privilege is expired.
		if (goBadDate == null) {
			LOGGER.debug("There is no expiration date. You are good to go.");

		} else {
			LOGGER.debug("Username: " + username + ". Expiration Date: " + goBadDate.toString());
			Date currentDate = new Date();

			if (currentDate.after(goBadDate)) {
				// Account is expired
				LOGGER.debug("Account " + username + " is expired");
				return "errorExpired";
			}
		}
		
		//if the account status is 3 that means it's requested and they shouldn't have access to the system.
		final String sqlAccountActive = "SELECT account.account_status_id FROM account WHERE account.user_name=?;";

		Long accountStatus = null;
		try {
			accountStatus = getJdbcTemplate().queryForObject(sqlAccountActive, Long.class, username);

		} catch (IncorrectResultSizeDataAccessException e) {
			throw new IncorrectResultSizeDataAccessException(e.getExpectedSize(), e.getActualSize());
		}
		
		if (accountStatus == null) {
			// Account status is requested
			return "errorInactive";
		}
		
		// Make sure the account status is 0
		final String sqlRequested =
				"SELECT account_role.role_status_id FROM account_role INNER JOIN account ON account_role.account_id=account.id WHERE account.user_name=? AND role_type_id=0 ;";

		Long roleStatus = null;
		try {
			roleStatus = getJdbcTemplate().queryForObject(sqlRequested, Long.class, username);

		} catch (IncorrectResultSizeDataAccessException e) {
			throw new IncorrectResultSizeDataAccessException(e.getExpectedSize(), e.getActualSize());
		}

		// If null then there is a problem
		if (roleStatus == null) {
			LOGGER.error("The status of " + username + " user role is null.");
			return "errorInactive";
		}

		LOGGER.debug("User role for " + username + " is status " + roleStatus.toString());

		if (!roleStatus.equals(0L)) {
			// User role is inactive or requested
			LOGGER.error("The status of " + username + " user role not active.");
			return "errorInactive";
		}

		return "success";
	}

	/**
	 * This class is not used, and should never be called. This function exists so that this Action
	 * can extend a class with the exact jdbc support we need.
	 */
	@Override
	protected HandlerResult authenticateUsernamePasswordInternal(UsernamePasswordCredential transformedCredential)
			throws GeneralSecurityException, PreventedException {
		// TODO Auto-generated method stub
		return null;
	}

}