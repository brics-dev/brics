package gov.nih.tbi.api.query.data.entity;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class UserTokenTest {
	@Test
	public void isExpiredTest() {
		UserToken userToken = new UserToken();
		Date expirationDate = Date.from(Instant.now().minus(Duration.ofDays(1))); // yesterday
		userToken.setTokenExpiration(expirationDate);
		assertTrue(userToken.isExpired());
	}

	@Test
	public void isExpiredTest2() {
		UserToken userToken = new UserToken();
		Date expirationDate = Date.from(Instant.now().plus(Duration.ofDays(1))); // tomorrow
		userToken.setTokenExpiration(expirationDate);
		assertFalse(userToken.isExpired());
	}
}
