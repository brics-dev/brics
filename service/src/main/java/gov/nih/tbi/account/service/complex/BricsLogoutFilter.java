
package gov.nih.tbi.account.service.complex;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * A version of LogoutFilter from the spring security framework that takes multiple logoutSuccessURLs as an argument and
 * then picks on when logging out based on the host
 * 
 */
public class BricsLogoutFilter extends GenericFilterBean
{

    // ~ Instance fields
    // ================================================================================================

    private String filterProcessesUrl = "/logout";
    private List<LogoutHandler> handlers;
    private LinkedHashMap<String, String> logoutSuccessUrls;
    private SimpleUrlLogoutSuccessHandler logoutSuccessHandler;

	@Value("#{casSecurityProperties['cas.client.key']}")
	private String[] key;

	@Value("#{casSecurityProperties['cas.server.location']}")
	private String[] casUrl;

    // ~ Constructors
    // ===================================================================================================

    public BricsLogoutFilter(LinkedHashMap<String, String> logoutSuccessUrls, LogoutHandler... handlers)
    {

        Assert.notEmpty(handlers, "LogoutHandlers are required");
        this.handlers = Arrays.asList(handlers);
        SimpleUrlLogoutSuccessHandler urlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
		this.logoutSuccessUrls = logoutSuccessUrls;
        logoutSuccessHandler = urlLogoutSuccessHandler;
    }

	public BricsLogoutFilter(LogoutHandler... handlers) {
		Assert.notEmpty(handlers, "LogoutHandlers are required");
		this.handlers = Arrays.asList(handlers);
		SimpleUrlLogoutSuccessHandler urlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
		logoutSuccessHandler = urlLogoutSuccessHandler;

	}

    // ~ Methods
    // ========================================================================================================

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException
    {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (requiresLogout(request, response))
        {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (logger.isDebugEnabled())
            {
                logger.debug("Logging out user '" + auth + "' and transferring to logout destination");
            }

            for (LogoutHandler handler : handlers)
            {
                handler.logout(request, response, auth);
            }

            // This is where we set the success handler's url based on the host
            // key format = <requiredString>-<antiString>
            // After the requiredString is identified, you need to make sure that the antiString is not identified.
            for (String s : logoutSuccessUrls.keySet())
            {
                String[] sArray = s.split("-");
                if (request.getHeader("host") != null && request.getHeader("host").contains(sArray[0]))
                {
                    if (sArray.length <= 1 || !request.getHeader("host").contains(sArray[1]))
                    {
                        logoutSuccessHandler.setDefaultTargetUrl(logoutSuccessUrls.get(s));
                        break;
                    }
                }
            }
            logoutSuccessHandler.onLogoutSuccess(request, response, auth);

            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Allow subclasses to modify when a logout should take place.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * 
     * @return <code>true</code> if logout should occur, <code>false</code> otherwise
     */
    protected boolean requiresLogout(HttpServletRequest request, HttpServletResponse response)
    {

        String uri = request.getRequestURI();
        int pathParamIndex = uri.indexOf(';');

        if (pathParamIndex > 0)
        {
            // strip everything from the first semi-colon
            uri = uri.substring(0, pathParamIndex);
        }

        int queryParamIndex = uri.indexOf('?');

        if (queryParamIndex > 0)
        {
            // strip everything from the first question mark
            uri = uri.substring(0, queryParamIndex);
        }

        if ("".equals(request.getContextPath()))
        {
            return uri.endsWith(filterProcessesUrl);
        }

        return uri.endsWith(request.getContextPath() + filterProcessesUrl);
    }

    public void setFilterProcessesUrl(String filterProcessesUrl)
    {

        Assert.isTrue(UrlUtils.isValidRedirectUrl(filterProcessesUrl), filterProcessesUrl + " isn't a valid value for"
                + " 'filterProcessesUrl'");
        this.filterProcessesUrl = filterProcessesUrl;
    }

    protected String getFilterProcessesUrl()
    {

        return filterProcessesUrl;
    }

	/**
	 * Set logout locations
	 */
	public void afterPropertiesSet() throws ServletException {

		// Assertions
		Assert.notNull(key, "property cas.client.key not found");
		Assert.notEmpty(key, "property cas.client.key cannot be empty");
		Assert.notNull(casUrl, "property cas.server.location not found");
		Assert.notEmpty(casUrl, "property cas.server.location cannot be empty");
		Assert.isTrue(key.length == casUrl.length,
				"An incorrect size of cas.sever.location has been provided (must match cas.client.key length).");

		logoutSuccessUrls = new LinkedHashMap<String, String>();
		for (int i = 0; i < key.length; i++) {
			logoutSuccessUrls.put(key[i], casUrl[i] + "/logout");
		}

		initFilterBean();
	}
}
