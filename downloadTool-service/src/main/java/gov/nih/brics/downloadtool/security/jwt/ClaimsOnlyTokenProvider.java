package gov.nih.brics.downloadtool.security.jwt;

import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.nih.brics.downloadtool.data.entity.UserToken;

/**
 * Parses an incoming JWT (but does not validate the signature) into a UserToken
 * 
 */
@Component
public class ClaimsOnlyTokenProvider {
	public static final String AUTHORITIES_KEY = "auth";
	public static final String CLAIM_ORG_ID = "org";
	public static final String CLAIM_USR_ID = "id";
	public static final String CLAIM_FULL_NAME = "fullName";
	public static final String CLAIM_EXPIRATION = "exp";
	public static final String CLAIM_USERNAME = "sub";
	public static final String CLAIM_TENANT = "tenant";

	public ClaimsOnlyTokenProvider() {
		// empty constructor
	}

	/**
	 * Constructs a Spring Authentication from the JWT access token.
	 * Stores a User object in the principal, 
	 * the string token in the credentials
	 * and the authorities from the token
	 * 
	 * @param token user's JWT
	 * @return
	 */
	public Authentication getAuthentication(String token) {
		UserToken claims = parseToken(token);

		Collection<? extends GrantedAuthority> authorities = Arrays
				.stream(claims.getAuthoritiesArray()).map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		User principal = new User(claims.getUsername(), "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}
	
	/**
	 * Parse a JsonObject (from JWT Body) into a UserToken.
	 * This is the inverse of TokenProvider.createToken() in the auth service
	 * 
	 * @param body JsonObject representing the body of the incoming JWT
	 * @return UserToken
	 */
	public UserToken parseToken(JsonObject body) {
		UserToken userToken = new UserToken();
		userToken.setId(body.get(CLAIM_USR_ID).getAsLong());
		userToken.setOrgId(body.get(CLAIM_ORG_ID).getAsLong());
		Instant instant = Instant.ofEpochSecond(body.get(CLAIM_EXPIRATION).getAsLong());
		userToken.setTokenExpiration(Date.from(instant));
		userToken.setUsername(body.get(CLAIM_USERNAME).getAsString());
		userToken.setFullName(body.get(CLAIM_FULL_NAME).getAsString());
		userToken.setAuthorities(body.get(AUTHORITIES_KEY).getAsString());
		userToken.setTenant(body.get(CLAIM_TENANT).getAsString());
		return userToken;
	}
	
	/**
	 * Parse a string JWT token into a UserToken.
	 * This is the inverse of TokenProvider.createToken() in the auth service
	 * 
	 * @param token representing the incoming JWT
	 * @return UserToken
	 */
	public UserToken parseToken(String token) {
		return parseToken(getTokenBody(token));
	}
	
	/**
	 * Splits out the body from an incoming signed JWT string into a JsonObject
	 * 
	 * @param token token to separate
	 * @return 
	 */
	protected JsonObject getTokenBody(String token) {
		String[] bodyParts = token.split("\\.");
		if (bodyParts.length != 3) {
			return null;
		}
		String body = bodyParts[1];
		String jsonBody = new String(Base64.getDecoder().decode(body));
		return JsonParser.parseString(jsonBody).getAsJsonObject(); 
		
	}
	
	
}
