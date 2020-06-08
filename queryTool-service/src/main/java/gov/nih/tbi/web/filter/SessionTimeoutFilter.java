package gov.nih.tbi.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.filter.GenericFilterBean;

/**
 * This servlet filter gets applied to all of our webservices. When the session has been destroyed, this filter will
 * intercept the request and return a json that tells our ajax calls to redirect the user to the logout page.
 * 
 * @author Francis Chen
 *
 */
public class SessionTimeoutFilter extends GenericFilterBean {

	private static final String UNAUTHORIZED_RESPONSE = "{ \"status\": 401 }";
	private static final String UNAUTHORIZED_RESPONSE_CONTENT_TYPE = "application/json";

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpSession session = httpRequest.getSession(false);

		if (session != null || (httpRequest.getRequestURI().startsWith("/query/service/stateless")
				|| httpRequest.getRequestURI().startsWith("/query/service/derived_data")
				|| httpRequest.getRequestURI().startsWith("/query/service/query/knowledgeGraphInfo"))) {
			chain.doFilter(request, response);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Session inactive, sending user to logout page...");
			}

			HttpServletResponse httpResponse = (HttpServletResponse) response;

			try {
				httpResponse.setStatus(HttpServletResponse.SC_OK);
				httpResponse.setContentType(UNAUTHORIZED_RESPONSE_CONTENT_TYPE);
				httpResponse.getWriter().write(UNAUTHORIZED_RESPONSE);
			} finally {
				httpResponse.getWriter().flush();
				httpResponse.getWriter().close();
			}
		}
	}

}
