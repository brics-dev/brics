package gov.nih.brics.auth.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import gov.nih.brics.auth.multitenant.TenantSelectorFilter;
import gov.nih.brics.auth.security.jwt.JWTFilter;
import gov.nih.brics.auth.security.jwt.JwtAuthenticationEntryPoint;
import gov.nih.brics.auth.security.jwt.TokenProvider;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private TokenProvider tokenProvider;

	@Autowired
	private JwtAuthenticationEntryPoint unauthorizedHandler;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//@formatter:off
		http
			.exceptionHandling()
				.authenticationEntryPoint(unauthorizedHandler)
		.and()
			.csrf()
				.ignoringAntMatchers("/user/login", "/user/bricslogin")
		.and()
			.headers()
			.frameOptions().disable()
		.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
			.authorizeRequests()
			.antMatchers("/health").permitAll()
			.antMatchers("/swagger-ui.html").permitAll()
			.antMatchers("/webjars/springfox-swagger-ui/**").permitAll()
			.antMatchers("/swagger-resources/**").permitAll()
			.antMatchers("/service-instances/**").permitAll()
			.antMatchers("/v2/api-docs").permitAll()
			.antMatchers("/user/login").permitAll()
			.antMatchers("/user/bricslogin").permitAll()
			.antMatchers("/actuator/info").permitAll()
			.antMatchers("/**").authenticated()
		.and()
			.addFilterBefore(new JWTFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);
		//@formatter:on
	}
}
