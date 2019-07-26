package gov.nih.nichd.ctdb.security.common;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.authentication.logout.LogoutHandler;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.tbi.account.service.complex.BricsLogoutFilter;

/**
 * Subclasses BricsLogoutFilter in order to handle proforms logout on spring logout
 * then handle brics normal logout.
 * 
 * @author Joshua Park(jospark)
 *
 */
public class ProformsLogoutFilter extends BricsLogoutFilter {
	
	public ProformsLogoutFilter(LinkedHashMap<String, String> logoutSuccessUrls, LogoutHandler... handlers) {
		super(logoutSuccessUrls, handlers);
    }
	
	public ProformsLogoutFilter(LogoutHandler... handlers) {
		super(handlers);
	}

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
    		ServletException
    {
    	HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        if (requiresLogout(request, response)) {
        	logout(request, response);
        }
        
        super.doFilter(request, response, chain);
    }
    
    private void logout(HttpServletRequest request, HttpServletResponse response) {
		logger.info("Request has invalid referrer or incorrectly stored local user.  Logging out.");
		request.getSession().removeAttribute(CtdbConstants.USER_SESSION_KEY);
		request.getSession().removeAttribute(SecuritySessionUtil.ACCOUNT_SESSION_KEY);
        Cookie delCookie = new Cookie(CtdbConstants.CTDB_USER_COOKIE, "");
        delCookie.setMaxAge(-1);
        delCookie.setSecure(true);
        response.addCookie(delCookie);
        request.getSession().invalidate();
    }
}
