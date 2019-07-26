
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
 * This action is part of the spring work flow defined in login-webflow.xml. If the user account is
 * expired, the submit function returns "error" which causes the user to be taken to a page where
 * they can set their password. Returning "success" allows for the user to get a service ticket as
 * normally.
 * 
 * @author mvalei
 * 
 */
public class ExpiredPasswordAction extends AbstractJdbcUsernamePasswordAuthenticationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpiredAccountAction.class);

	public String submit(final RequestContext context, final UsernamePasswordCredential usernamePasswordCredential,
			final MessageContext messageContext) throws Exception {


		final String username = getPrincipalNameTransformer().transform(usernamePasswordCredential.getUsername());
		final String sql = "SELECT password_expiration_date FROM account WHERE user_name=?";

		Date goBadDate = null;
		try {
			goBadDate = getJdbcTemplate().queryForObject(sql, Timestamp.class, username);
		} catch (IncorrectResultSizeDataAccessException e) {
			throw new IncorrectResultSizeDataAccessException(e.getExpectedSize(), e.getActualSize());
		}

		// Determine if account is about to expire
		if (goBadDate == null) {
			LOGGER.debug("User: " + username + " does not have an expiration date.");
			return "success";
		}
		LOGGER.debug("User: " + username + ", Password expires: " + goBadDate.toString());
		Date currentDate = new Date();

		if (currentDate.after(goBadDate)) {
			// Account is expired
			return "error";
		}

		return "success";
	}


	@Override
	protected HandlerResult authenticateUsernamePasswordInternal(UsernamePasswordCredential transformedCredential)
			throws GeneralSecurityException, PreventedException {
		// TODO Auto-generated method stub
		return null;
	}

}