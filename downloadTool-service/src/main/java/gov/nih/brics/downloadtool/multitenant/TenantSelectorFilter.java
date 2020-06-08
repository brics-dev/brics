package gov.nih.brics.downloadtool.multitenant;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import gov.nih.brics.downloadtool.configuration.BricsConfiguration;
import gov.nih.brics.downloadtool.data.entity.UserToken;
import gov.nih.brics.downloadtool.security.jwt.ClaimsOnlyTokenProvider;
import gov.nih.brics.downloadtool.service.RepositoryServiceImpl;

@Component
public class TenantSelectorFilter extends GenericFilterBean {
	
	@Autowired
	private BricsConfiguration config;
	
	@Autowired
	ClaimsOnlyTokenProvider tokenProvider;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
		
		// get the tenant name from EITHER the JWT (for service-to-service calls) or the tenant ID (for external calls)
		// first try tenantID, then try JWT 
		String tenantId = httpServletRequest.getHeader("X-TenantID");
		if (tenantId == null) {
			UserToken jwt = getUserToken();
			if (jwt != null) {
				tenantId = jwt.getTenant();
			}
		}
		
		// check if either of them failed, if not, set the tenant and continue on
		if (tenantId != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Setting tenant to %s", tenantId));
			}
			TenantContext.setCurrentTenant(tenantId);
			config.load(tenantId);
			
			filterChain.doFilter(servletRequest, servletResponse);
		}
		else {
			logger.warn("Tenant ID was not specified in header or JWT.  Sending 400 back to the user");
			httpServletResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		}
	}
	
	private UserToken getUserToken() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return null;
		}
		return tokenProvider.parseToken((String) auth.getCredentials());
	}
	
	
	
}
