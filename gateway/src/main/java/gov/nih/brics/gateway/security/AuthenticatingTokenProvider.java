package gov.nih.brics.gateway.security;

import org.springframework.security.core.Authentication;

public interface AuthenticatingTokenProvider extends TokenProvider {
	public Authentication getAuthentication(String token);
}
