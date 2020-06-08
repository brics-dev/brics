package gov.nih.brics.cas.flow;

import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.RequestContext;

import gov.nih.brics.cas.logging.SessionLogDbUtil;

public class BricsLogoutSessionLoggerAction extends BricsLoginHandler {
	
	private CookieGenerator tgtCookieGenerator;
	
	public String log(final RequestContext context) {
		
		// Try to get the TGT
		String tgtId = WebUtils.getTicketGrantingTicketId(context);
		if (StringUtils.isBlank(tgtId)) {
			Cookie cookie = org.springframework.web.util.WebUtils.getCookie(WebUtils.getHttpServletRequest(context), tgtCookieGenerator.getCookieName());

			if (cookie != null) {
				tgtId = cookie.getValue();
			}
		}

		// end the session in the session log db table
		if (StringUtils.isNotBlank(tgtId)) {
			SessionLogDbUtil.endSession(getJdbcTemplate(), tgtId);
		}

		return "success";
	}
	
	public void setTgtCookieGenerator(CookieGenerator generator) {
		this.tgtCookieGenerator = generator;
	}
}
