package gov.nih.brics.auth.security.jwt;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import gov.nih.brics.auth.configuration.BricsConfiguration;
import gov.nih.brics.auth.data.entity.AccountUserDetails;
import gov.nih.brics.auth.data.entity.UserToken;
import gov.nih.brics.auth.multitenant.TenantContext;
import gov.nih.brics.auth.util.BlackListCache;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

/**
 * Create and validate JSON Web Tokens (JWT) to represent our {@link User}
 * entities.
 * 
 */
@Component
public class TokenProvider {
	private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

	private static final String AUTHORITIES_KEY = "auth";
	public static final String CLAIM_ORG_ID = "org";
	public static final String CLAIM_USR_ID = "id";
	public static final String CLAIM_FULL_NAME = "fullName";
	public static final String CLAIM_TENANT = "tenant";
	
	@Autowired
	BlackListCache blackList;
	
	@Autowired
	BricsConfiguration config;

	/**
	 * Creates a valid JWT with all the claims
	 * 
	 * @param user
	 * @return String full JWT
	 */
	public String createToken(AccountUserDetails user) {
		String authorities = user.getAuthorities().stream().map(String::valueOf).collect(Collectors.joining(","));
		gov.nih.tbi.commons.model.hibernate.User usr = user.getAccount().getUser();
		Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_USR_ID, usr.getId());
		claims.put(CLAIM_FULL_NAME, usr.getFullName());
		claims.put(CLAIM_ORG_ID, user.getDiseaseId());
		claims.put(CLAIM_TENANT, TenantContext.getCurrentTenant());
		claims.put(AUTHORITIES_KEY, authorities);
		return buildToken(user.getUsername(), claims, getExpiration());
	}
	
	public String renewToken(String originalToken) {
		UserToken original = parseToken(originalToken);
		Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_USR_ID, original.getId());
		claims.put(CLAIM_FULL_NAME, original.getFullName());
		claims.put(CLAIM_ORG_ID, original.getOrgId());
		claims.put(CLAIM_TENANT, original.getTenant());
		claims.put(AUTHORITIES_KEY, original.getAuthorities().stream().map(String::valueOf).collect(Collectors.joining(",")));
		
		return buildToken(original.getUsername(), claims, getExpiration());
	}
	
	/**
	 * Computes the exiration date as now + tokenValidityInMilliseconds, stored in
	 * microservice.authentication.jwt.token-validity-in-milliseconds
	 * 
	 * @return Date expiration date for the token
	 */
	private Date getExpiration() {
		return new Date(Instant.now().toEpochMilli() + config.getTokenValidityInMilliseconds());
	}

	public Authentication getAuthentication(String token) {
		Claims claims = Jwts.parser().setSigningKey(config.getSecretKey()).parseClaimsJws(token).getBody();

		Collection<? extends GrantedAuthority> authorities = Arrays
				.stream(claims.get(AUTHORITIES_KEY).toString().split(",")).map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		User principal = new User(claims.getSubject(), "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	public boolean validateToken(String authToken) {
		try {
			Jwts.parser().setSigningKey(config.getSecretKey()).parseClaimsJws(authToken);
			return !blackList.contains(authToken);
		} catch (SignatureException e) {
			logger.info("Invalid JWT signature.");
			logger.trace("Invalid JWT signature trace: {}", e);
		} catch (MalformedJwtException e) {
			logger.info("Invalid JWT token.");
			logger.trace("Invalid JWT token trace: {}", e);
		} catch (ExpiredJwtException e) {
			logger.info("Expired JWT token.");
			logger.trace("Expired JWT token trace: {}", e);
		}
		return false;
	}
	
	/**
	 * Builds a JWT based on the incoming map of claims, subject, and expiration date. 
	 * 
	 * @param subject the subject definition (normally the principle's username)
	 * @param claims all claims we want to add to the JWT
	 * @param expiration Date at which the JWT will expire
	 * @return
	 */
	private String buildToken(String subject, Map<String, Object> claims, Date expiration) {
		return Jwts.builder()
				.setSubject(subject)
				.addClaims(claims)
				.setExpiration(expiration)
				.signWith(SignatureAlgorithm.HS512, config.getSecretKey())
				.compact();
	}

	/**
	 * Create the UserProfile based on the JSON Web Token (JWT) claims.
	 * 
	 * @param claims
	 *            the JWT claims
	 * @return the {@link UserToken}
	 */
	public UserToken parseToken(String token) {
		Claims claims = Jwts.parser().setSigningKey(config.getSecretKey()).parseClaimsJws(token).getBody();

		UserToken userToken = new UserToken();
		userToken.setId(claims.get(CLAIM_USR_ID, Long.class));
		userToken.setUsername(claims.getSubject());
		userToken.setFullName(claims.get(CLAIM_FULL_NAME, String.class));
		userToken.setOrgId(claims.get(CLAIM_ORG_ID, Long.class));
		userToken.setAuthorities(claims.get(AUTHORITIES_KEY, String.class));
		userToken.setTenant(claims.get(CLAIM_TENANT, String.class));
		userToken.setTokenExpiration(claims.getExpiration());

		return userToken;
	}
}
