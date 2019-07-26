
package gov.nih.tbi.commons.reporting;

import javax.servlet.http.HttpSession;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.TokenInterceptor;
import org.apache.struts2.util.TokenHelper;

import com.opensymphony.xwork2.ActionInvocation;

/**
 * This is the exception interceptor that automatically logs any exception it intercepts.
 * 
 * @author Francis Chen
 * @author Raymond Anthony Trombadore
 */
public class TokenSessionInterceptor extends TokenInterceptor
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 3833298475043L;
	private static final Logger log = LogManager.getLogger(TokenSessionInterceptor.class.getName());
	/**
	 * 
	 */
	 protected String doIntercept(ActionInvocation invocation) throws Exception {
		
		 if (log.isDebugEnabled()) {
			 log.debug("Intercepting invocation to check for valid transaction token.");
		 }
 
		 HttpSession session = ServletActionContext.getRequest().getSession(true);

		 synchronized (session) {
		 
			 if (!TokenHelper.validToken()) {
				 return handleInvalidToken(invocation);
			 }
			 return handleValidToken(invocation);
		 	}
		 }
}
