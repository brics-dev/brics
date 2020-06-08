package gov.nih.nichd.ctdb.security.action;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.security.common.CasPostAuthFilter;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.account.ws.RestAccountProvider;

public class CasLoginRedirectAction extends BaseAction {
	private static final long serialVersionUID = 7527213020046920853L;
	private static final Logger logger = Logger.getLogger(CasLoginRedirectAction.class);

	// public String execute(HttpServletRequest request) throws Exception {
	public String execute() throws Exception {
		
		// check the timeout.  If we are greater than the local timeout, redirect!
		Long currentTime = Long.parseLong(request.getParameter("unique"));
		// Long currentTime = System.currentTimeMillis();
		Long localTimeoutRefreshLast = CasPostAuthFilter.getLocalTimer(request);
		// logger.info("localTimeoutRefreshLast: " + localTimeoutRefreshLast / 60000);
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
			} else {
				CasPostAuthFilter.setLocalTimer(request);
				keepAliveInAccount();
			}
			
		} else {
			// since the local timer is null, we can assume the user has just come from login
			// that means we need to set the local timer
			CasPostAuthFilter.setLocalTimer(request);
			keepAliveInAccount();
		}
	
		return StrutsConstants.SUCCESS;
	}

	private void keepAliveInAccount() {
		String accountUrl = SysPropUtil.getProperty("webservice.usermgmt.url");

		try {
			SecuritySessionUtil ssu = new SecuritySessionUtil(request);
			String proxyTicket = ssu.getProxyTicket(accountUrl);
			RestAccountProvider restProvider = new RestAccountProvider(accountUrl, proxyTicket);
			restProvider.keepSessionAlive();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
