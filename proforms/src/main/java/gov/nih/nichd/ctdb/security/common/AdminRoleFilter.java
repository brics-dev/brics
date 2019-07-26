package gov.nih.nichd.ctdb.security.common;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;

public class AdminRoleFilter implements Filter {

	/**
	 * This method checks if the current user from CAS session has role ADMIN or
	 * PROFORMS_ADMIN. The filter will continue is one of the admin roles are found
	 * and throw an exception otherwise.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc)
			throws ServletException, IOException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		User currentLocalUser = (User) httpRequest.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);

		try {
			if (currentLocalUser == null) {
				throw new AuthenticationFailedException("The user is not logged in and should not have access.");
			}

			String username = currentLocalUser.getUsername();

			String restfulDomain = SysPropUtil.getProperty("webservice.usermgmt.url");
			String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.account.byusername.url") + "/"
					+ username;
			SecuritySessionUtil ssu = new SecuritySessionUtil(httpRequest);
			String proxyTicket = ssu.getProxyTicket(restfulDomain);
			Client client = ClientBuilder.newClient();
			restfulUrl = ssu.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
			WebTarget wt = client.target(restfulUrl);
			Account account = wt.request(MediaType.TEXT_XML).get(Account.class);

			boolean hasAccess = false;
			for (AccountRole role : account.getAccountRoleList()) {
				if (role.getIsActive()) {
					switch (role.getRoleType()) {
					case ROLE_PROFORMS_ADMIN:
					case ROLE_ADMIN:
						hasAccess = true;
					default:
						break;
					}
				}
			}

			if (hasAccess) {
				fc.doFilter(request, response);
			} else {
				throw new AuthenticationFailedException("The user is not an admin and should not have access.");
			}
		} catch (AuthenticationFailedException e) {
			e.printStackTrace();
			httpResponse.sendRedirect(httpRequest.getContextPath() + "/common/accessDenied.jsp");
		}
	}

	public void destroy() {
		// dont need to do anything here
	}

	public void init(FilterConfig arg0) throws ServletException {
		// don't need to do anything here
	}
}
