package gov.nih.brics.gateway.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

public class JWTFilter extends GenericFilterBean {
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String BEARER_HEADER = "Bearer ";

	private AuthenticatingTokenProvider tokenProvider;

	public JWTFilter(AuthenticatingTokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		String jwt = resolveToken(httpServletRequest);

		if (StringUtils.hasText(jwt) && this.tokenProvider.validateToken(jwt)) {
			logger.debug("user passed auth service validation");
			// put auth in the context holder to not cause errors in pass-through responses
			Authentication authentication = this.tokenProvider.getAuthentication(jwt);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(servletRequest, servletResponse);
		} else {
			HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
			logger.error("Responding with unauthorized error");
			httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	protected String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_HEADER)) {
			return bearerToken.substring(7, bearerToken.length());
		}
		
		return null;
	}
}
