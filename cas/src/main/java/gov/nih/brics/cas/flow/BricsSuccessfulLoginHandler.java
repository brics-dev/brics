package gov.nih.brics.cas.flow;

import java.util.List;

import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.RequestContext;

import gov.nih.brics.cas.logging.SessionLogDbUtil;
import gov.nih.brics.cas.model.SessionLog;

public class BricsSuccessfulLoginHandler extends BricsLoginHandler {
	
	/**
	 * Handles the login. In this case, updates the consecutive login count (if needed) and
	 * performs all login operations for the account session audit log
	 * 
	 * @param context request context (from spring)
	 * @param usernamePasswordCredential the login credentials. Provides the user's username
	 * @param messageContext message context 
	 * @return message to pass up to the webflow (for next step logic)
	 * @throws Exception not actually used in this case but part of the interface
	 */
	public String submit(final RequestContext context, final UsernamePasswordCredential usernamePasswordCredential,
			final MessageContext messageContext) throws Exception {
		String username = getPrincipalNameTransformer().transform(usernamePasswordCredential.getUsername());

		// reset the consecutive failure count back to 0 upon successful login
		updateConsecutiveCount(0, username);
		
		String loginTicket = WebUtils.getTicketGrantingTicketId(context);
		
		manageSessionLog(loginTicket, username);

		return "success";
	}
	
	/**
	 * Handles closing any still-open user sessions before opening a new one, then opens the
	 * new one.
	 * 
	 * @param tgt Ticket Granting Ticket for this session
	 * @param username user's username who is performing the operation
	 */
	private void manageSessionLog(String tgt, String username) {
		// get any existing sessions for this user that are not closed, and close them
		List<SessionLog> userOpenSessions = SessionLogDbUtil.getUserOpenSessions(getJdbcTemplate(), username);
		for (SessionLog log : userOpenSessions) {
			SessionLogDbUtil.endSession(getJdbcTemplate(), log.getTgt());
		}
		// start session in session log database table
		SessionLogDbUtil.startSession(getJdbcTemplate(), tgt, username);
	}
}
