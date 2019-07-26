package gov.nih.tbi.portal;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/*
 * This interceptor logs action and user account information to the actionlog
 */
public class ActionLogInterceptor implements Interceptor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1274726438L;
	private static Logger logger = Logger.getLogger(ActionLogInterceptor.class);


	public String intercept(ActionInvocation invocation) throws Exception {

		List<String> nonLoggedMethodNames = new ArrayList<String>();
		nonLoggedMethodNames.add("loginCheck");

		String method = invocation.getProxy().getMethod();
		String action_2 = invocation.getInvocationContext().getName().toString();

		if (!nonLoggedMethodNames.contains(method)) {
			logger.warn("Struts Action: " + action_2 + ", method: " + method);
		}
		return invocation.invoke();
	}

	public void destroy() {}

	public void init() {}


}
