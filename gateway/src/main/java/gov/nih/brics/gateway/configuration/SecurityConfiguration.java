package gov.nih.brics.gateway.configuration;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import gov.nih.brics.gateway.security.AuthenticatingTokenProvider;
import gov.nih.brics.gateway.security.JWTFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private AuthenticatingTokenProvider tokenProvider;
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowCredentials(true);
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//@formatter:off
		http
			.cors()
		.and()
			// CSRF protection is unnecessary with JWTs in the Authorization header
			.csrf().disable()
			.headers()
			.frameOptions().disable()
		.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
			.authorizeRequests()
			.anyRequest()
			.authenticated()
		.and()
			.addFilterBefore(new JWTFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);
		//@formatter:on
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web
			.ignoring()
			.antMatchers("/authentication/user/login")
			.antMatchers("/authentication/user/bricslogin")
			.antMatchers("/static/**")
			//Query Swagger documentations
			.antMatchers("/query-api/swagger-ui.html")
			.antMatchers("/query-api/v2/api-docs")
			.antMatchers("/query-api/swagger-resources/**")
			.antMatchers("/query-api/webjars/springfox-swagger-ui/**")
			//Authentication Swagger documentations
			.antMatchers("/authentication/swagger-ui.html")
			.antMatchers("/authentication/v2/api-docs")
			.antMatchers("/authentication/swagger-resources/**")
			.antMatchers("/authentication/webjars/springfox-swagger-ui/**")
			//File Service
			.antMatchers(HttpMethod.GET, "/filerepository/files/**");
	}
}
