
package gov.nih.tbi.pojo.authentication;

import org.apache.log4j.Logger;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.cas.authentication.NullStatelessTicketCache;
import org.springframework.security.cas.authentication.StatelessTicketCache;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;

/**
 * An {@link AuthenticationProvider} implementation that integrates with JA-SIG Central Authentication Service (CAS).
 * <p>
 * This <code>AuthenticationProvider</code> is capable of validating {@link UsernamePasswordAuthenticationToken}
 * requests which contain a <code>principal</code> name equal to either
 * {@link CasAuthenticationFilter#CAS_STATEFUL_IDENTIFIER} or {@link CasAuthenticationFilter#CAS_STATELESS_IDENTIFIER}.
 * It can also validate a previously created {@link CasAuthenticationToken}.
 * 
 * @author Ben Alex
 * @author Scott Battaglia
 */
public class BricsCasAuthenticationProvider extends CasAuthenticationProvider
{

	private static Logger logger = Logger.getLogger(BricsCasAuthenticationProvider.class);

    // ~ Instance fields
    // ================================================================================================

	private AuthenticationUserDetailsService<CasAssertionAuthenticationToken> authenticationUserDetailsService;

    private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private StatelessTicketCache statelessTicketCache = new NullStatelessTicketCache();
    private String key;
    private TicketValidator ticketValidator;
    private ServiceProperties serviceProperties;

    // ~ Methods
    // ========================================================================================================

    public void afterPropertiesSet() throws Exception
    {

        Assert.notNull(this.authenticationUserDetailsService, "An authenticationUserDetailsService must be set");
        Assert.notNull(this.ticketValidator, "A ticketValidator must be set");
        Assert.notNull(this.statelessTicketCache, "A statelessTicketCache must be set");
        Assert.hasText(this.key,
                "A Key is required so CasAuthenticationProvider can identify tokens it previously authenticated");
        Assert.notNull(this.messages, "A message source must be set");
        Assert.notNull(this.serviceProperties, "serviceProperties is a required field.");
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {

        if (!supports(authentication.getClass()))
        {
            return null;
        }

        if (authentication instanceof UsernamePasswordAuthenticationToken
                && (!CasAuthenticationFilter.CAS_STATEFUL_IDENTIFIER.equals(authentication.getPrincipal().toString()) && !CasAuthenticationFilter.CAS_STATELESS_IDENTIFIER
                        .equals(authentication.getPrincipal().toString())))
        {
            // UsernamePasswordAuthenticationToken not CAS related
			logger.debug("     | UsernamePasswordAuthenticationToken not CAS related (username not recognized).");
            return null;
        }

        // If an existing CasAuthenticationToken, just check we created it
        if (authentication instanceof CasAuthenticationToken)
        {
            if (this.key.hashCode() == ((CasAuthenticationToken) authentication).getKeyHash())
            {
				logger.debug(
						"     | Provider confirms it created this CasAuthenticationToken. Returned to manager unchanged.");
                return authentication;
            }
            else
            {
                throw new BadCredentialsException(messages.getMessage("CasAuthenticationProvider.incorrectKey",
                        "The presented CasAuthenticationToken does not contain the expected key"));
            }
        }

        // Ensure credentials are presented
        if ((authentication.getCredentials() == null) || "".equals(authentication.getCredentials()))
        {
            throw new BadCredentialsException(messages.getMessage("CasAuthenticationProvider.noServiceTicket",
                    "Failed to provide a CAS service ticket to validate"));
        }

        boolean stateless = false;

        if (authentication instanceof UsernamePasswordAuthenticationToken
                && CasAuthenticationFilter.CAS_STATELESS_IDENTIFIER.equals(authentication.getPrincipal()))
        {
            stateless = true;
        }

        CasAuthenticationToken result = null;

		logger.debug("     | Provider determines ticket is stateless: " + stateless);
        if (stateless)
        {
            // Try to obtain from cache
            result = statelessTicketCache.getByTicketId(authentication.getCredentials().toString());
			logger.debug(
					(result == null) ? "     | Unable to load Token through the stateless ticket cache." : "     | Token loaded through stateless ticket cache.");
        }

        if (result == null)
        {
            result = this.authenticateNow(authentication);
            result.setDetails(authentication.getDetails());
			logger.debug(
					(result == null) ? "     | Provider unable to validate ticket" : "     | Ticket successfully validated. User object creatd.");
        }

        if (stateless)
        {
            // Add to cache
            statelessTicketCache.putTicketInCache(result);
        }

        return result;
    }

    private CasAuthenticationToken authenticateNow(final Authentication authentication) throws AuthenticationException
    {

        try
        {
			logger.debug("     | Asking CAS if the successful authentication received is legitimate. "
					+ "See serviceValidate/proxyValidate URL of CAS");
            final Assertion assertion = this.ticketValidator.validate(authentication.getCredentials().toString(),
                    serviceProperties.getService());
            final UserDetails userDetails = loadUserByAssertion(assertion);
            userDetailsChecker.check(userDetails);
            return new CasAuthenticationToken(this.key, userDetails, authentication.getCredentials(),
                    userDetails.getAuthorities(), userDetails, assertion);
        }
        catch (final TicketValidationException e)
        {
			logger.error(
					"     | CAS denied supplying the ST specified. Most likely the wrong instance of CAS was asked.");
            throw new BadCredentialsException(e.getMessage(), e);
        }
    }

    /**
     * Template method for retrieving the UserDetails based on the assertion. Default is to call configured
     * userDetailsService and pass the username. Deployers can override this method and retrieve the user based on any
     * criteria they desire.
     * 
     * @param assertion
     *            The CAS Assertion.
     * @return the UserDetails.
     */
    protected UserDetails loadUserByAssertion(final Assertion assertion)
    {

        final CasAssertionAuthenticationToken token = new CasAssertionAuthenticationToken(assertion, "");
        return this.authenticationUserDetailsService.loadUserDetails(token);
    }

    @Deprecated
    /**
     * @deprecated as of 3.0.  Use the {@link org.springframework.security.cas.authentication.CasAuthenticationProvider#setAuthenticationUserDetailsService(org.springframework.security.core.userdetails.AuthenticationUserDetailsService)} instead.
     */
    public void setUserDetailsService(final UserDetailsService userDetailsService)
    {

        this.authenticationUserDetailsService = new UserDetailsByNameServiceWrapper(userDetailsService);
    }

    public void setAuthenticationUserDetailsService(
            final AuthenticationUserDetailsService authenticationUserDetailsService)
    {

        this.authenticationUserDetailsService = authenticationUserDetailsService;
    }

    public void setServiceProperties(final ServiceProperties serviceProperties)
    {

        this.serviceProperties = serviceProperties;
    }

    public ServiceProperties getServiceProperties()
    {

        return this.serviceProperties;
    }

    protected String getKey()
    {

        return key;
    }

    public void setKey(String key)
    {

        this.key = key;
    }

    public StatelessTicketCache getStatelessTicketCache()
    {

        return statelessTicketCache;
    }

    public void setMessageSource(final MessageSource messageSource)
    {

        this.messages = new MessageSourceAccessor(messageSource);
    }

    public void setStatelessTicketCache(final StatelessTicketCache statelessTicketCache)
    {

        this.statelessTicketCache = statelessTicketCache;
    }

    public void setTicketValidator(final TicketValidator ticketValidator)
    {

        this.ticketValidator = ticketValidator;
    }

    public TicketValidator getTicketValidator()
    {

        return this.ticketValidator;
    }

    public boolean supports(final Class<? extends Object> authentication)
    {

        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication))
                || (CasAuthenticationToken.class.isAssignableFrom(authentication))
                || (CasAssertionAuthenticationToken.class.isAssignableFrom(authentication));
    }

	/**
	 * Every service ticket has a suffix (defined on the cas server's properties file) that needs to
	 * match the key of this provider in order for the provider to attempt Authentication.
	 * 
	 * @param st - The service ticket (needs to end with this.getKey())
	 * @return
	 */
	public boolean supportsThisCasInstance(String st) {

		return st.endsWith(getKey());
	}
}
