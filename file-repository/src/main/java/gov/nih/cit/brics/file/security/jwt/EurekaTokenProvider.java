package gov.nih.cit.brics.file.security.jwt;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.nih.cit.brics.file.data.entity.UserToken;
import gov.nih.cit.brics.file.exception.JWTRenewalException;

/**
 * Parses an incoming JWT (but does not validate the signature) into a UserToken
 * 
 */
@Component
public class EurekaTokenProvider {
	private static final Logger logger = LoggerFactory.getLogger(EurekaTokenProvider.class);
	private static final String AUTH_SERVICE_NAME = "authentication";

	public static final String AUTHORITIES_KEY = "auth";
	public static final String CLAIM_ORG_ID = "org";
	public static final String CLAIM_USR_ID = "id";
	public static final String CLAIM_FULL_NAME = "fullName";
	public static final String CLAIM_EXPIRATION = "exp";
	public static final String CLAIM_USERNAME = "sub";
	public static final String CLAIM_TENANT = "tenant";
	public static final String VERIFY_TOKEN_URL_FORMAT = "http://%s/user/verify";
	public static final String RENEW_TOKEN_URL_FORMAT = "http://%s/user/renew";

	@Autowired
	RestTemplate restTemplate;

	public EurekaTokenProvider() {
		System.out.println("++++++ Starting up token provider.");
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

		List<GrantedAuthority> authorities = Arrays
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
	public synchronized UserToken parseToken(JsonObject body) {
		// Check if the JSON body object is null.
		if (body == null) {
			return null;
		}

		UserToken userToken = new UserToken();

		userToken.setId(body.get(CLAIM_USR_ID).getAsLong());
		userToken.setOrgId(body.get(CLAIM_ORG_ID).getAsLong());
		userToken.setUsername(body.get(CLAIM_USERNAME).getAsString());
		userToken.setFullName(body.get(CLAIM_FULL_NAME).getAsString());
		userToken.setAuthorities(body.get(AUTHORITIES_KEY).getAsString());
		userToken.setTenant(body.get(CLAIM_TENANT).getAsString());

		// Convert the expiration date string into a LocalDateTime object.
		LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(body.get(CLAIM_EXPIRATION).getAsLong(), 0,
				OffsetDateTime.now().getOffset());

		userToken.setTokenExpiration(localDateTime);

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
	
	/**
	 * Validate the given JWT with the auth service.
	 * 
	 * @param jwt - The JWT string to validate.
	 * @return True if and only if the given JWT is valid.
	 */
	public boolean validateToken(String jwt) {
		String url = String.format(VERIFY_TOKEN_URL_FORMAT, AUTH_SERVICE_NAME);
		HttpEntity<Void> request = buildJwtRequest(jwt);

		if (logger.isDebugEnabled()) {
			logger.debug("Calling " + url + " with " + request);
		}

		ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.GET, request, Void.class);
		return HttpStatus.OK == response.getStatusCode();
	}

	/**
	 * Renews the given JWT by calling the renewal endpoint of the auth service.
	 * 
	 * @param jwt - The old, but still valid, JWT to be used the request a new one.
	 * @return The new JWT, with an updated expiration date.
	 * @throws JWTRenewalException When the call to the renewal endpoint of the auth service fails.
	 */
	public String renewToken(String jwt) throws JWTRenewalException {
		String newJwt = null;
		String url = String.format(RENEW_TOKEN_URL_FORMAT, AUTH_SERVICE_NAME);
		HttpEntity<Void> request = buildJwtRequest(jwt);

		logger.info("Renewing the following JWT: {}.", jwt);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

		if (response.getStatusCode() == HttpStatus.OK) {
			newJwt = response.getBody();
		} else {
			throw new JWTRenewalException(String.format("Failed to renew the JWT: %s.", jwt));
		}

		return newJwt;
	}

	private HttpEntity<Void> buildJwtRequest(String jwt) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(JWTFilter.AUTHORIZATION_HEADER, JWTFilter.BEARER_HEADER + jwt);
		return new HttpEntity<Void>(null, headers);
	}
	
}
