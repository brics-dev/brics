
package gov.nih.tbi.pojo.authentication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.proxy.Cas20ProxyRetriever;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.util.Assert;

/**
 * An extension of the ProviderManager provided by spring security. The goal of this provider is to
 * maintain the same Provider functionality with a few exceptions.
 * 
 * First, we want to use the deprecated default constructor, because rather than define our
 * authentication providers dynamically in the context xml files, we are going to create them after
 * this bean has been initialized by reading a properties file.
 * 
 * Second, the default case for this class is to loop through the provider list and attempt them one
 * by one. This will not work for the BRICS implementation because supplying the wrong provider to
 * the CAS server, will invalidate the CAS service ticket on the server side. This means the next
 * providers in the list will always fail even when we get to the right one. Therefore, we are going
 * to loop through the providers and make sure when it comes to the CAS providers, we never even
 * attempt authentication if the service properties don't match (which is the condition that would
 * invalidate the ticket on the CAS server).
 * 
 * 
 * @author Michael Valeiras
 * @see DefaultAuthenticationEventPublisher
 */
public class BricsProviderManager extends ProviderManager
{

    // ~ Static fields/initializers
    // =====================================================================================

    private static final Log logger = LogFactory.getLog(BricsProviderManager.class);

    // ~ Instance fields
    // ================================================================================================

	private AuthenticationEventPublisher eventPublisher; // In parent there is a private
															// NullEventPublisher class but since
															// our context sets a publisher this is
															// not required.
    private List<AuthenticationProvider> providers = Collections.emptyList();
    private AuthenticationManager parent;

	// Passed on to all providers
	private ProxyGrantingTicketStorage storage;
	private AuthenticationUserDetailsService<CasAssertionAuthenticationToken> authenticationUserDetailsService;

	// ~ Spring Property fields
	// ================================================================================================

	@Value("#{casSecurityProperties['cas.client.service']}")
	private String[] service;

	@Value("#{casSecurityProperties['cas.client.key']}")
	private String[] key;

	@Value("#{casSecurityProperties['cas.server.location']}")
	private String[] casUrl;

	@Value("#{casSecurityProperties['cas.client.proxyCallbackUrl']}")
	private String[] proxyCallbackUrl;


	public BricsProviderManager(List<AuthenticationProvider> providers) {
		super(providers);
	}
		
    // ~ Methods
    // ========================================================================================================

	/**
	 * This function is called after the bean has fully initialized (because the parent class
	 * implements InitializingBean). The Spring docs state this this is where business logic that
	 * requires full init should be done and is the only time we are allowed to read the properties
	 * annotated by {@value}.
	 * 
	 * Therefore, this is where we will build the provider list as well as validate the properties
	 * file and the completed object.
	 */
    public void afterPropertiesSet() throws Exception
    {
		// Validate properties
		Assert.notNull(key, "property cas.client.key not found");
		Assert.notEmpty(key, "property cas.client.key cannot be empty");
		Assert.notNull(service, "property cas.client.service not found");
		Assert.notEmpty(service, "property cas.client.service cannot be empty");
		Assert.isTrue(key.length == service.length,
				"An incorrect size of cas.client.service has been provided (must match cas.client.key length).");
		Assert.notNull(casUrl, "property cas.server.location not found");
		Assert.notEmpty(casUrl, "property cas.server.location cannot be empty");
		Assert.isTrue(key.length == casUrl.length,
				"An incorrect size of cas.sever.location has been provided (must match cas.client.key length).");
		Assert.notNull(proxyCallbackUrl, "property cas.client.proxyCallbackUrl not found");
		Assert.notEmpty(proxyCallbackUrl, "property cas.client.proxyCallbackUrl cannot be empty");
		Assert.isTrue(key.length == proxyCallbackUrl.length,
				"An incorrect size of cas.client.proxyCallbackUrl has been provided (must match cas.client.key length).");

		logger.debug("AuthenticationManager initialization: " + key.length + " CAS instances to be registered.");

		// Logic for loading all the providers
		List<AuthenticationProvider> providerList = new ArrayList<AuthenticationProvider>();
		for (int i = 0; i < key.length; i++) {
			Cas20ProxyRetriever c20Retriever = new Cas20ProxyRetriever(casUrl[i], "UTF-8", null);
			Cas20ProxyTicketValidator c20Validator = new Cas20ProxyTicketValidator(casUrl[i]);
			c20Validator.setProxyGrantingTicketStorage(storage);
			c20Validator.setProxyCallbackUrl(proxyCallbackUrl[i]);
			c20Validator.setProxyRetriever(c20Retriever);
			c20Validator.setAcceptAnyProxy(true);

			ServiceProperties sp = new ServiceProperties();
			sp.setService(service[i]);
			sp.setSendRenew(false);
			sp.afterPropertiesSet(); // verify (since defining this bean in XML would call this
										// method)

			CasAuthenticationProvider provider = new BricsCasAuthenticationProvider();
			provider.setAuthenticationUserDetailsService(authenticationUserDetailsService);
			provider.setServiceProperties(sp);
			provider.setTicketValidator(c20Validator);
			provider.setKey(key[i]); // This used to be the same for every provider but it is
										// suppsed uniquely identify the provider.
			provider.afterPropertiesSet(); // verify

			providerList.add(provider);
		}
		providers = providerList;

		// This check is what the parent class validates
		if (parent == null && providers.isEmpty()) {
			throw new IllegalArgumentException(
					"A parent AuthenticationManager or a list " + "of AuthenticationProviders is required");
        }

		// Since we did not implement a private NullEventHandler we just want to make sure the
		// provider from the XML configuration was set (and not forgotten).
		Assert.notNull(eventPublisher,
				"BricsProviderManager.eventPublisher property cannot be null. Must be defined in context file");
		Assert.notNull(storage, "The ProxyGrantingTicketStorage supplied to cas providers cannot be null");
		Assert.notNull(authenticationUserDetailsService,
				"The AuthenticationUserDetailsService supplied to cas providers cannot be null");
    }

    /**
	 * This overrides the normal auth procedure. This is where we will additionally check
	 */
	public Authentication authenticate(Authentication authentication) throws AuthenticationException 
    {

        Class<? extends Authentication> toTest = authentication.getClass();
        AuthenticationException lastException = null;
        Authentication result = null;

		// Large DEBUG Block
        if (logger.isDebugEnabled()) {
			logger.debug("AUTH | Authentication Object Information: ");
			logger.debug("     | Class: " + authentication.getClass().getSimpleName());
			if (authentication instanceof UsernamePasswordAuthenticationToken) {
				logger.debug("     | Username: "
 + ((UsernamePasswordAuthenticationToken) authentication).getPrincipal());
				logger.debug("     | Password: "
 + ((UsernamePasswordAuthenticationToken) authentication).getCredentials());
			}
			if (authentication instanceof CasAuthenticationToken) {
				logger.debug("     | Represents a SUCCESSFUL Cas Authentication.");
				logger.debug("     | Principal: " + ((CasAuthenticationToken) authentication).getPrincipal().toString());
				logger.debug("     | Credentials: " + ((CasAuthenticationToken) authentication).getCredentials().toString());
				logger.debug("     | KeyHash (Unique to the provider that created this Authentication): "
						+ ((CasAuthenticationToken) authentication).getKeyHash());
				logger.debug("     | Assertion ValidUntilDate: "
						+ ((CasAuthenticationToken) authentication).getAssertion().getValidUntilDate().toString());
			}
			if (authentication instanceof CasAssertionAuthenticationToken) {
				logger.debug("     | Temporary authentication object needed to load the user details service.");
				logger.debug("     | Principal: "
						+ ((CasAssertionAuthenticationToken) authentication).getPrincipal().toString());
				logger.debug("     | Credentials: "
						+ ((CasAssertionAuthenticationToken) authentication).getCredentials().toString());
			}
        }

		logger.debug("AUTH | Looping through " + key.length + " providers.");
		int i = 0;
        for (AuthenticationProvider provider : getProviders())
        {
			i++;
			logger.debug("AUTH | Provider Number " + i + ". Class: " + provider.getClass().getSimpleName());
			if (provider instanceof BricsCasAuthenticationProvider) {
				String key = ((BricsCasAuthenticationProvider) provider).getKey();
				logger.debug("     | Key: " + key);
				logger.debug("     | KeyHash: " + key.hashCode());

				if (authentication instanceof UsernamePasswordAuthenticationToken) {
					UsernamePasswordAuthenticationToken bricsAuth =
							(UsernamePasswordAuthenticationToken) authentication;
					if (!((BricsCasAuthenticationProvider) provider)
							.supportsThisCasInstance(bricsAuth.getCredentials().toString())) {
						logger.debug("     | Provider with key \"" + key
								+ "\" did not attempt to verify ST because they ST does not have a suffix of \"" + key
								+ "\".");
						continue;
					}
				}
			}

            if (!provider.supports(toTest))
            {
				logger.debug("     | Provider " + i + " does not support this Authentication class.");
                continue;
            }

            try
            {
                result = provider.authenticate(authentication);

                if (result != null)
                {
                    copyDetails(authentication, result);
                    break;
                }
            }
            catch (AccountStatusException e)
            {
                // SEC-546: Avoid polling additional providers if auth failure is due to invalid account status
                eventPublisher.publishAuthenticationFailure(e, authentication);
                throw e;
            }
            catch (AuthenticationException e)
            {
                lastException = e;
            }
        }

        if (result == null && parent != null)
        {
            // Allow the parent to try.
            try
            {
                result = parent.authenticate(authentication);
            }
            catch (ProviderNotFoundException e)
            {
                // ignore as we will throw below if no other exception occurred prior to calling parent and the parent
                // may throw ProviderNotFound even though a provider in the child already handled the request
            }
            catch (AuthenticationException e)
            {
                lastException = e;
            }
        }

        if (result != null)
        {
			if (isEraseCredentialsAfterAuthentication() && (result instanceof CredentialsContainer))
            {
                // Authentication is complete. Remove credentials and other secret data from authentication
                ((CredentialsContainer) result).eraseCredentials();
            }

            eventPublisher.publishAuthenticationSuccess(result);
            return result;
        }

        // Parent was null, or didn't authenticate (or throw an exception).

        if (lastException == null)
        {
            lastException = new ProviderNotFoundException(messages.getMessage("ProviderManager.providerNotFound",
                    new Object[] { toTest.getName() }, "No AuthenticationProvider found for {0}"));
        }

        eventPublisher.publishAuthenticationFailure(lastException, authentication);

        throw lastException;
    }

    /**
	 * A private class used in authenticate that unfortunately needs to be implemented again in this
	 * class.
	 * 
	 * @param source source authentication
	 * @param dest the destination authentication object
	 */
    private void copyDetails(Authentication source, Authentication dest)
    {

        if ((dest instanceof AbstractAuthenticationToken) && (dest.getDetails() == null))
        {
            AbstractAuthenticationToken token = (AbstractAuthenticationToken) dest;

            token.setDetails(source.getDetails());
        }
    }

    public List<AuthenticationProvider> getProviders()
    {

        return providers;
    }

    public void setParent(AuthenticationManager parent)
    {

        this.parent = parent;
    }

    public void setAuthenticationEventPublisher(AuthenticationEventPublisher eventPublisher)
    {

        Assert.notNull(eventPublisher, "AuthenticationEventPublisher cannot be null");
        this.eventPublisher = eventPublisher;
    }

	public void setProxyGrantingTicketStorage(ProxyGrantingTicketStorage storage) {
		this.storage = storage;
	}

	public void setAuthenticationUserDetailsService(
			final AuthenticationUserDetailsService<CasAssertionAuthenticationToken> authenticationUserDetailsService) {
		this.authenticationUserDetailsService = authenticationUserDetailsService;
	}

}
