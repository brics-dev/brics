package gov.nih.brics.cas.flow;

import javax.servlet.http.Cookie;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.RequestContext;

import gov.nih.brics.cas.logging.SessionLogDbUtil;

public class BricsLogoutSessionLoggerAction extends BricsLoginHandler {
	
	private CookieGenerator tgtCookieGenerator;
	
	public String log(final RequestContext context) {
		
		// the below do not work to get the TGT
		String tgtId = WebUtils.getTicketGrantingTicketId(context);
		if (tgtId == null || tgtId.equals("")) {
			Cookie cookie = org.springframework.web.util.WebUtils.getCookie(WebUtils.getHttpServletRequest(context), tgtCookieGenerator.getCookieName());
			tgtId = cookie.getValue();
		}
		
		// end the session in the session log db table
		SessionLogDbUtil.endSession(getJdbcTemplate(), tgtId);

		return "success";
	}
	
	public void setTgtCookieGenerator(CookieGenerator generator) {
		this.tgtCookieGenerator = generator;
	}
}
