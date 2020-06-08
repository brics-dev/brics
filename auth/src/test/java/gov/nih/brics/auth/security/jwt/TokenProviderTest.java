package gov.nih.brics.auth.security.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import gov.nih.brics.auth.configuration.BricsConfiguration;
import gov.nih.brics.auth.data.entity.AccountUserDetails;
import gov.nih.brics.auth.security.jwt.TokenProvider;
import gov.nih.brics.auth.util.BlackListCache;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.hibernate.User;

public class TokenProviderTest {
	
	public static final int tokenValidityInMilliseconds = 1800000;
	public static final String SECRET_KEY = "ABCD";
	
	@Mock 
	private BlackListCache blackList;
	
	@Mock
	private BricsConfiguration config;
	
	@InjectMocks
	private TokenProvider tokenProvider = new TokenProvider();
	
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	/**
	 * Creates a JWT with the given claims.  Decodes the JWT to verify the contents of the
	 * token.
	 */
	@Test
	public void testJwtCreation() {
		//ReflectionTestUtils.setField(tokenProvider, "secretKey", SECRET_KEY);
		//ReflectionTestUtils.setField(tokenProvider, "tokenValidityInMilliseconds", tokenValidityInMilliseconds);
		
		JsonParser jsonParser = JsonParserFactory.getJsonParser();
		AccountUserDetails details = getValidUserDetails();
		
		Date expiration1 = new Date((new Date()).getTime() + tokenValidityInMilliseconds);
		
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		when(config.getSecretKey()).thenReturn(SECRET_KEY);
		when(config.getTokenValidityInMilliseconds()).thenReturn(new Long(tokenValidityInMilliseconds));
		//when(details.getAuthorities()).thenReturn(authorities);
		
		String jwt = tokenProvider.createToken(details);
		String[] split = jwt.split("\\.");
		String body = split[1];
		String decodedBody = new String(Base64.getDecoder().decode(body));
		Map<String, Object> map = jsonParser.parseMap(decodedBody);
		
		assertThat(map.get(TokenProvider.CLAIM_FULL_NAME).toString()).isEqualTo("Testee, Tester T");
		assertThat(map.get(TokenProvider.CLAIM_ORG_ID)).isEqualTo(5);
		assertThat(map.get(TokenProvider.CLAIM_USR_ID)).isEqualTo(1);
		assertThat(map.get("sub").toString()).isEqualTo("test");
		// can only check that the date is ahead of now and less than 30 minutes from now
		
		Instant instant = Instant.ofEpochSecond((Integer) map.get("exp"));
		Date tokenDate = Date.from(instant);
		
		// test that the expiration 1 and token dates are less than 5 seconds apart
		assertThat(Math.abs(expiration1.getTime() - tokenDate.getTime())).isLessThan(5000L);
	}
	
	public AccountUserDetails getValidUserDetails() {
		User user = new User();
		user.setId(1L);
		user.setFirstName("tester");
		user.setMiddleName("T");
		user.setLastName("testee");
		user.setEmail("tester.T.testee@test.test");
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, 1);
		
		Account account = new Account();
		account.setId(1L);
		account.setUserName("test");
		account.setPassword("test2".getBytes());
		account.setUser(user);
		account.setAccountStatus(AccountStatus.ACTIVE);
		account.setPasswordExpirationDate(cal.getTime());
		account.setIsActive(true);
		account.setIsLocked(false);
		
		AccountUserDetails details = new AccountUserDetails(account, 5L, config);
		//ReflectionTestUtils.setField(details, "accountExpirationDays", 5);
		
		return details;
	}
}
