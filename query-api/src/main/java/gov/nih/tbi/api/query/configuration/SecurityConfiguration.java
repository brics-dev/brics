package gov.nih.tbi.api.query.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import gov.nih.tbi.api.query.security.jwt.EurekaTokenProvider;
import gov.nih.tbi.api.query.security.jwt.JWTFilter;
import gov.nih.tbi.api.query.security.jwt.JwtAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private EurekaTokenProvider tokenProvider;

	@Autowired
	private JwtAuthenticationEntryPoint unauthorizedHandler;
	
	private DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
        defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchy());
        defaultWebSecurityExpressionHandler.setDefaultRolePrefix("");
        return defaultWebSecurityExpressionHandler;
    }
	
	@Bean
	public RoleHierarchy roleHierarchy() {
		RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
		roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER " + 
				"ROLE_ADMIN > ROLE_DICTIONARY_ADMIN " + 
				"ROLE_ADMIN > ROLE_ACCOUNT_ADMIN " + 
				"ROLE_ADMIN > ROLE_STUDY_ADMIN " + 
				"ROLE_ADMIN > ROLE_GUID_ADMIN " + 
				"ROLE_ADMIN > ROLE_ORDER_ADMIN " + 
				"ROLE_ADMIN > ROLE_QUERY_ADMIN " + 
				"ROLE_ADMIN > ROLE_PROFORMS_ADMIN " + 
				"ROLE_ADMIN > ROLE_METASTUDY_ADMIN " + 
				"ROLE_ACCOUNT_ADMIN >  ROLE_USER " + 
				"ROLE_USER > ROLE_UNSIGNED " + 
				"ROLE_UNSIGNED > ROLE_GUEST " + 
				"ROLE_DICTIONARY_ADMIN > ROLE_DICTIONARY " + 
				"ROLE_DICTIONARY_ADMIN > ROLE_DICTIONARY_EFORM " + 
				"ROLE_STUDY_ADMIN > ROLE_STUDY " + 
				"ROLE_GUID_ADMIN > ROLE_GUID " + 
				"ROLE_QUERY_ADMIN > ROLE_QUERY " + 
				"ROLE_PROFORMS_ADMIN > ROLE_PROFORMS " + 
				"ROLE_METASTUDY_ADMIN > ROLE_METASTUDY");
		return roleHierarchy;
	}
	
	@Bean
    public RoleHierarchyVoter roleVoter() {
        return new RoleHierarchyVoter(roleHierarchy());
    }

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//@formatter:off
		http
			.csrf().disable()
			.exceptionHandling()
				.authenticationEntryPoint(unauthorizedHandler)
		.and()
			.headers()
			.frameOptions().disable()
		.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
			.authorizeRequests()
			.expressionHandler(webSecurityExpressionHandler())
			.antMatchers("/health").permitAll()
			.antMatchers("/swagger-ui.html").permitAll()
			.antMatchers("/webjars/springfox-swagger-ui/**").permitAll()
			.antMatchers("/swagger-resources/**").permitAll()
			.antMatchers("/service-instances/**").permitAll()
			.antMatchers("/v2/api-docs").permitAll()
			.antMatchers("/**").hasAuthority("ROLE_USER")
		.and()
			.addFilterBefore(new JWTFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);
		//@formatter:on
	}
}
