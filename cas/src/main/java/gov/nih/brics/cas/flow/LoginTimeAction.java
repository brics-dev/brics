
package gov.nih.brics.cas.flow;

import java.security.GeneralSecurityException;

import org.jasig.cas.adaptors.jdbc.AbstractJdbcUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageContext;
import org.springframework.dao.DataAccessException;
import org.springframework.webflow.execution.RequestContext;

/**
 * This action is part of the spring work flow defined in login-webflow.xml. After a user account
 * has been fully authenticated, it is called to write the current time to the user's account
 * object.
 * 
 * @author mvalei
 * 
 */
public class LoginTimeAction extends AbstractJdbcUsernamePasswordAuthenticationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoginTimeAction.class);

	public String submit(final RequestContext context, final UsernamePasswordCredential usernamePasswordCredential,
			final MessageContext messageContext) throws Exception {

		final String username = getPrincipalNameTransformer().transform(usernamePasswordCredential.getUsername());
		final String sql = "UPDATE account SET successful_login_date=now() WHERE user_name=?";

		int rows;
		try {
			rows = getJdbcTemplate().update(sql, username);
		} catch (DataAccessException e) {
			LOGGER.error("DataAccessException thrown while updating successful_login_date. Exception follows");
			e.printStackTrace();
			return "error";
		}

		// Determine if account is about to expire
		if (rows != 1) {
			LOGGER.error(
					"Incorrect number of rows effected when updating successful_login_date. Authentication denied");
			return "error";
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