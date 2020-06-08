package gov.nih.tbi.api.query.security.jwt;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.testng.annotations.Test;

public class JWTFilterTest {
	@Test
	public void resolveTokenTest() {
		JWTFilter jwtFilter = new JWTFilter(null);
		HttpServletRequest testRequest = mock(HttpServletRequest.class);
		when(testRequest.getHeader(JWTFilter.AUTHORIZATION_HEADER)).thenReturn("Bearer fakeToken123");
		String actualToken = jwtFilter.resolveToken(testRequest);
		String expectedToken = "fakeToken123";
		assertEquals(actualToken, expectedToken);
	}

	@Test
	public void resolveTokenTest2() {
		JWTFilter jwtFilter = new JWTFilter(null);
		HttpServletRequest testRequest = mock(HttpServletRequest.class);
		when(testRequest.getHeader(JWTFilter.AUTHORIZATION_HEADER)).thenReturn("garbage stuff");
		String actualToken = jwtFilter.resolveToken(testRequest);
		String expectedToken = null;
		assertEquals(actualToken, expectedToken);
	}

	@Test
	public void resolveTokenTest3() {
		JWTFilter jwtFilter = new JWTFilter(null);
		HttpServletRequest testRequest = mock(HttpServletRequest.class);
		when(testRequest.getHeader(JWTFilter.AUTHORIZATION_HEADER)).thenReturn(null);
		String actualToken = jwtFilter.resolveToken(testRequest);
		String expectedToken = null;
		assertEquals(actualToken, expectedToken);
	}
}
