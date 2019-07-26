package gov.nih.tbi.commons.portal;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class RefererInterceptor implements Interceptor {
	private static final long serialVersionUID = 4302556516823514503L;
	private static final Logger logger = Logger.getLogger(RefererInterceptor.class);

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String intercept(ActionInvocation paramActionInvocation)
			throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		String referer = request.getHeader("referer");
		if (referer != null && !referer.equals("")) {
			String requestedUrl = request.getRequestURL().toString();

			referer = referer.replaceFirst("http://", "").replaceFirst("https://", "");
			requestedUrl = requestedUrl.replaceFirst("http://", "").replaceFirst("https://", "");
			requestedUrl = requestedUrl.split("/")[0];
			
			if (!referer.startsWith(requestedUrl) && !referer.contains("-local.cit.nih.gov")) {
				// kick the user out, there is a referer and it's not this site
				logger.warn("Referer kickout with referrer: " + referer + " should start with url: " + requestedUrl);
				return "refererError";
			}
		}
		
		return paramActionInvocation.invoke();
	}

}
