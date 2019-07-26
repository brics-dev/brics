package gov.nih.nichd.ctdb.security.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.security.common.CasPostAuthFilter;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

public class CasLoginRedirectAction  {
	private static final long serialVersionUID = 7527213020046920853L;

	public String execute(HttpServletRequest request) throws Exception {
		
		// check the timeout.  If we are greater than the local timeout, redirect!
		Long currentTime = System.currentTimeMillis();
		Long localTimeoutRefreshLast = CasPostAuthFilter.getLocalTimer(request);
		
		if (localTimeoutRefreshLast != null) {
			Long localTimeoutThreshold = Long.decode(SysPropUtil.getProperty(
					"application.integration.cas.localtimeout"));
			
			// note internal timer is in millis, so to compare, we have to convert to minutes
			Long elapsedTime = currentTime - localTimeoutRefreshLast;
			elapsedTime = elapsedTime/60000;
			
			// check to see if the elapsed time is greater than the local timeout threshold
			if (elapsedTime > localTimeoutThreshold) {
				CasPostAuthFilter.setLocalTimer(request);
				CasPostAuthFilter.redirectToLogin(request, ServletActionContext.getResponse());
			}
			
		} else {
			// since the local timer is null, we can assume the user has just come from login
			// that means we need to set the local timer
			CasPostAuthFilter.setLocalTimer(request);
		}
	
		return StrutsConstants.SUCCESS;
	}
}
