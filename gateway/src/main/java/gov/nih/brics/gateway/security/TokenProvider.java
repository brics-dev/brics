package gov.nih.brics.gateway.security;

public interface TokenProvider {
	public String getToken(String username, String password);
	public boolean validateToken(String token);
}
