package gov.nih.ninds.cit.cas;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class ProformsUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {
	
	private static Logger logger = Logger.getLogger(ProformsUserDetailsService.class);
	@Override
	public UserDetails loadUserDetails(CasAssertionAuthenticationToken token)
			throws UsernameNotFoundException {
		String username = token.getName();
		StringBuilder message = new StringBuilder();
		message.append("\n=============================");
		message.append("\nUsername :" + username);
		message.append("\n=============================");
		logger.info(message);
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		return new User(username, "", authorities);

	}

}
