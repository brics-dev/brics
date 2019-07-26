package gov.nih.tbi.guid.model;

import java.util.Base64;
import java.util.Calendar;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import gov.nih.tbi.guid.exception.InvalidJwtException;

/**
 * Represents a JWT from the GUID server. Really this is just a POJO with a little light logic (loading data in from a
 * string format, etc.).
 * 
 * This can represent BOTH Server JWT and User JWT
 * 
 * Please, don't try to verify the signature or header. They're not useful and you can't verify the signature here
 * anyway because you don't have access to the GUID server's certs.
 * 
 * @author jpark1
 *
 */
public class GuidJwt {
	public static final String CLAIM_EXPIRATION = "exp";
	public static final String CLAIM_SERVERNAME = "serverName";
	public static final String CLAIM_ISSUER = "iss";

	public static final String CLAIM_USERNAME = "user";
	public static final String CLAIM_RENEW_URL = "renew";
	public static final String CLAIM_IS_HOME_ADMIN = "isHomeAdmin";
	public static final String CLAIM_IS_ADMIN = "admin";
	public static final String CLAIM_ORGANIZATION = "org";
	public static final String CLAIM_FIRSTNAME = "fn";
	public static final String CLAIM_LASTNAME = "ln";
	public static final String CLAIM_EMAIL = "email";
	public static final String CLAIM_PERM_EXPIRATION = "permExp";
	public static final String CLAIM_STATUS = "status";

	public static final String SESSION_KEY = "guidJwt";
	public static final long EXPIRATION_BUFFER_MILLIS = 300000;

	private String fullJwtString;
	private JsonObject payload;

	private JsonParser parser;

	public GuidJwt() {
		parser = new JsonParser();
	}

	/**
	 * Initializes the class and loads a string JWT into this object and interprets it.
	 * 
	 * @param jwt the full string JWT (can handle headless if needed)
	 * @throws JsonSyntaxException If unable to translate the payload into JSON
	 * @throws InvalidJwtException if the JWT does not have a payload and signature
	 */
	public GuidJwt(String jwt) throws JsonSyntaxException, InvalidJwtException {
		this();
		load(jwt);
	}

	/**
	 * Loads a string JWT into this object and interprets it.
	 * 
	 * @param jwt the full string JWT (can handle headless if needed)
	 * @throws JsonSyntaxException If unable to translate the payload into JSON
	 * @throws InvalidJwtException if the JWT does not have a payload and signature
	 */
	public void load(String jwt) throws JsonSyntaxException, InvalidJwtException {
		fullJwtString = jwt;
		String[] jwtParts = jwt.split("\\.");
		
		// early escape
		if (jwtParts.length == 1) {
			throw new InvalidJwtException("The JWT is missing signature or may otherwise not be properly formed");
		}
		
		int payloadIndex = 1;
		if (jwtParts.length == 2) {
			// if the length is 2, we probably send a headless jwt. Use the default header and assume
			// index 0 is the payload
			payloadIndex = 0;
		}

		String decodedPayload = new String(Base64.getDecoder().decode(jwtParts[payloadIndex]));
		payload = parser.parse(decodedPayload).getAsJsonObject();
	}

	public Long getExpiration() {
		return Long.decode(getClaim(CLAIM_EXPIRATION));
	}

	public boolean isAlmostExpired() {
		Long expTimestamp = getExpiration();
		Calendar date = Calendar.getInstance();
		Long t = date.getTimeInMillis();
		return (expTimestamp - EXPIRATION_BUFFER_MILLIS) < t;
	}

	public boolean isExpired() {
		Long expTimestamp = getExpiration();
		Calendar date = Calendar.getInstance();
		Long t = date.getTimeInMillis();
		return expTimestamp < t;
	}

	public String getClaim(String claimName) {
		if (payload == null) {
			return null;
		}
		return payload.get(claimName).getAsString();
	}

	public String toString() {
		return fullJwtString;
	}
}
