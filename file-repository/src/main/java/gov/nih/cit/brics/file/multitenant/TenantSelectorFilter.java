package gov.nih.cit.brics.file.multitenant;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import gov.nih.cit.brics.file.data.entity.UserToken;
import gov.nih.cit.brics.file.exception.InvalidJWTException;
import gov.nih.cit.brics.file.security.jwt.EurekaTokenProvider;
import gov.nih.cit.brics.file.util.FileRepositoryConstants;

@Component
public class TenantSelectorFilter extends GenericFilterBean {
	private static final Logger logger = LoggerFactory.getLogger(TenantSelectorFilter.class);
	private static final String TENANT_HEADER_NAME = "X-TenantID";
	
	@Autowired
	private FileRepositoryConstants config;
	
	@Autowired
	EurekaTokenProvider tokenProvider;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		
		// get the tenant name from EITHER the JWT (for service-to-service calls) or the tenant ID (for external calls)
		// first try tenantID, then try JWT 
		String tenantId = httpServletRequest.getHeader(TENANT_HEADER_NAME);
		if (tenantId == null) {
			UserToken jwt = getUserToken();
			if (jwt != null) {
				tenantId = jwt.getTenant();
			} else {
				throw new InvalidJWTException("The JWT doesn't contain a tenant name.");
			}
		}
		
		// Set the tenant name and properties.
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Setting tenant to %s.", tenantId));
		}

		TenantContext.setCurrentTenant(tenantId);
		config.load(tenantId);

		filterChain.doFilter(servletRequest, servletResponse);
	}
	
	private UserToken getUserToken() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return null;
		}
		return tokenProvider.parseToken((String) auth.getCredentials());
	}

}
