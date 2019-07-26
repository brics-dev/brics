package gov.nih.nichd.ctdb.security.common;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

/**
 * This is a modified version of UserDetailsByNameServiceWrapper user for CAS authentication. Form based authentication
 * will still use UserDetailsByNameServiceWrapper and will be insecure. Passes the complete Authentication token to the
 * AccountDetailService which extracts the username AND the (hashed) password for a secure account web services call.
 * 
 * @author Michael Valeiras
 * Modified for ProFoRMS by Joshua Park
 */
public class ProformsUserDetailsByAuthServiceWrapper implements AuthenticationUserDetailsService, InitializingBean
{

    private UserDetailsService userDetailsService = null;

    /**
     * Constructs an empty wrapper for compatibility with Spring Security 2.0.x's method of using a setter.
     */
    public ProformsUserDetailsByAuthServiceWrapper()
    {

        // constructor for backwards compatibility with 2.0
    }

    /**
     * Constructs a new wrapper using the supplied
     * {@link org.springframework.security.core.userdetails.UserDetailsService} as the service to delegate to.
     * 
     * @param userDetailsService
     *            the UserDetailsService to delegate to.
     */
    public ProformsUserDetailsByAuthServiceWrapper(final UserDetailsService userDetailsService)
    {

        Assert.notNull(userDetailsService, "userDetailsService cannot be null.");
        this.userDetailsService = userDetailsService;
    }

    /**
     * Check whether all required properties have been set.
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {

        Assert.notNull(this.userDetailsService, "UserDetailsService must be set");
    }

    /**
     * Get the UserDetails object from the wrapped UserDetailsService implementation
     */
    public UserDetails loadUserDetails(Authentication authentication) throws UsernameNotFoundException,
            DataAccessException
    {

        try
        {
            return ((ProformsAccountDetailService) userDetailsService).loadUserByAuthentication(authentication);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new UsernameNotFoundException("Web service encoding error", e);
        }
    }

    /**
     * Set the wrapped UserDetailsService implementation
     * 
     * @param aUserDetailsService
     *            The wrapped UserDetailsService to set
     */
    public void setUserDetailsService(UserDetailsService aUserDetailsService)
    {

        this.userDetailsService = aUserDetailsService;
    }
}
