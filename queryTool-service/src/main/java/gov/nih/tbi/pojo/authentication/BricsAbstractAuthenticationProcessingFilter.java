
package gov.nih.tbi.pojo.authentication;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * The AbstractAuthenticationProcessingFilter implemented in our version of Spring does not support a modified SuccessfulAuthentication call that allows a child class to manipulate the chian. Proxying requires this chain to continue filtering after authenticating the user therefore this the class had to be subsituted for this custom class. The class is identical to its replacement except for the addition of the following funciton:
 *
 *   protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
 *           FilterChain chain, Authentication authResult) throws IOException, ServletException
 *   {
 *       successfulAuthentication(request, response, authResult);
 *   }
 *
 *
 */
public abstract class BricsAbstractAuthenticationProcessingFilter extends GenericFilterBean implements
        ApplicationEventPublisherAware, MessageSourceAware
{

    // ~ Static fields/initializers
    // =====================================================================================

    /**
     * @deprecated Use the value in {@link WebAttributes} directly.
     */
    @Deprecated
    public static final String SPRING_SECURITY_LAST_EXCEPTION_KEY = WebAttributes.AUTHENTICATION_EXCEPTION;

    // ~ Instance fields
    // ================================================================================================

    protected ApplicationEventPublisher eventPublisher;
    protected AuthenticationDetailsSource authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private AuthenticationManager authenticationManager;
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    /*
     * Delay use of NullRememberMeServices until initialization so that namespace has a chance to inject
     * the RememberMeServices implementation into custom implementations.
     */
    private RememberMeServices rememberMeServices = null;

    /**
     * The URL destination that this filter intercepts and processes (usually something like
     * <code>/j_spring_security_check</code>)
     */
    private String filterProcessesUrl;

    private boolean continueChainBeforeSuccessfulAuthentication = false;

    private SessionAuthenticationStrategy sessionStrategy = new NullAuthenticatedSessionStrategy();

    private boolean allowSessionCreation = true;

    private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

    // ~ Constructors
    // ===================================================================================================

    /**
     * @param defaultFilterProcessesUrl
     *            the default value for <tt>filterProcessesUrl</tt>.
     */
    protected BricsAbstractAuthenticationProcessingFilter(String defaultFilterProcessesUrl)
    {

        this.filterProcessesUrl = defaultFilterProcessesUrl;
    }

    // ~ Methods
    // ========================================================================================================

    @Override
    public void afterPropertiesSet()
    {

        Assert.hasLength(filterProcessesUrl, "filterProcessesUrl must be specified");
        Assert.isTrue(UrlUtils.isValidRedirectUrl(filterProcessesUrl), filterProcessesUrl
                + " isn't a valid redirect URL");
        Assert.notNull(authenticationManager, "authenticationManager must be specified");

        if (rememberMeServices == null)
        {
            rememberMeServices = new NullRememberMeServices();
        }
    }

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException
    {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!requiresAuthentication(request, response))
        {
            chain.doFilter(request, response);

            return;
        }

        Authentication authResult;

        try
        {
            authResult = attemptAuthentication(request, response);
            if (authResult == null)
            {
                // return immediately as subclass has indicated that it hasn't completed authentication
                return;
            }
            sessionStrategy.onAuthentication(authResult, request, response);
        }
        catch (AuthenticationException failed)
        {
            // Authentication failed
            unsuccessfulAuthentication(request, response, failed);

            return;
        }

        // Authentication success
        if (continueChainBeforeSuccessfulAuthentication)
        {
            chain.doFilter(request, response);
        }

        successfulAuthentication(request, response, chain, authResult);
    }

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response)
    {

        String uri = request.getRequestURI();
        int pathParamIndex = uri.indexOf(';');
        
		logger.trace(
				"AUTH | Determine if request is a service ticket from CAS based on the path the CASFLITER listens on.");
		logger.trace("     | Path being listened on: " + filterProcessesUrl);
		logger.trace("     | URI of this request: " + request.getRequestURI());

        if (pathParamIndex > 0)
        {
            // strip everything after the first semi-colon
            uri = uri.substring(0, pathParamIndex);
        }      

        if ("".equals(request.getContextPath()))
        {
			logger.trace("     | Correct Path (w/o contextPath)?: " + uri.endsWith(filterProcessesUrl));
			return uri.endsWith(filterProcessesUrl);
        }
		logger.trace(
				"     | Correct Path (w/ contextPath): " + uri.endsWith(request.getContextPath() + filterProcessesUrl));
		return uri.endsWith(request.getContextPath() + filterProcessesUrl);
    }

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    public abstract Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException;

    /**
     * Default behavior for successful authentication.
     * <ol>
     * <li>Sets the successful <tt>Authentication</tt> object on the {@link SecurityContextHolder}</li>
     * <li>Invokes the configured {@link SessionAuthenticationStrategy} to handle any session-related behaviour (such as
     * creating a new session to protect against session-fixation attacks).</li>
     * <li>Informs the configured <tt>RememberMeServices</tt> of the successful login</li>
     * <li>Fires an {@link InteractiveAuthenticationSuccessEvent} via the configured <tt>ApplicationEventPublisher</tt></li>
     * <li>Delegates additional behavior to the {@link AuthenticationSuccessHandler}.</li>
     * </ol>
     * 
     * Subclasses can override this method to continue the {@link FilterChain} after successful authentication.
     * 
     * @param request
     * @param response
     * @param chain
     * @param authResult
     *            the object returned from the <tt>attemptAuthentication</tt> method.
     * @throws IOException
     * @throws ServletException
     */
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, Authentication authResult) throws IOException, ServletException
    {

        successfulAuthentication(request, response, authResult);
    }

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            Authentication authResult) throws IOException, ServletException
    {

		logger.debug("AUTH | DONE! Updating SecurityContextHolder to contain: " + authResult + "\n\n");

        SecurityContextHolder.getContext().setAuthentication(authResult);

        rememberMeServices.loginSuccess(request, response, authResult);

        // Fire event
        if (this.eventPublisher != null)
        {
            eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }

        successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException
    {

        SecurityContextHolder.clearContext();

        if (logger.isDebugEnabled())
        {
			logger.debug("AUTH | Authentication request failed: " + failed.toString());
			logger.debug("     | Updated SecurityContextHolder to contain null Authentication");
			logger.debug("     | Delegating to authentication failure handler" + failureHandler);
        }

        rememberMeServices.loginFail(request, response);

        failureHandler.onAuthenticationFailure(request, response, failed);
    }

    protected AuthenticationManager getAuthenticationManager()
    {

        return authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager)
    {

        this.authenticationManager = authenticationManager;
    }

    public String getFilterProcessesUrl()
    {

        return filterProcessesUrl;
    }

    public void setFilterProcessesUrl(String filterProcessesUrl)
    {

        this.filterProcessesUrl = filterProcessesUrl;
    }

    public RememberMeServices getRememberMeServices()
    {

        return rememberMeServices;
    }

    public void setRememberMeServices(RememberMeServices rememberMeServices)
    {

        this.rememberMeServices = rememberMeServices;
    }

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    public void setContinueChainBeforeSuccessfulAuthentication(boolean continueChainBeforeSuccessfulAuthentication)
    {

        this.continueChainBeforeSuccessfulAuthentication = continueChainBeforeSuccessfulAuthentication;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher)
    {

        this.eventPublisher = eventPublisher;
    }

    public void setAuthenticationDetailsSource(AuthenticationDetailsSource authenticationDetailsSource)
    {

        Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource required");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    public void setMessageSource(MessageSource messageSource)
    {

        this.messages = new MessageSourceAccessor(messageSource);
    }

    public AuthenticationDetailsSource getAuthenticationDetailsSource()
    {

        // Required due to SEC-310
        return authenticationDetailsSource;
    }

    protected boolean getAllowSessionCreation()
    {

        return allowSessionCreation;
    }

    public void setAllowSessionCreation(boolean allowSessionCreation)
    {

        this.allowSessionCreation = allowSessionCreation;
    }

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    public void setSessionAuthenticationStrategy(SessionAuthenticationStrategy sessionStrategy)
    {

        this.sessionStrategy = sessionStrategy;
    }

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler)
    {

        Assert.notNull(successHandler, "successHandler cannot be null");
        this.successHandler = successHandler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler)
    {

        Assert.notNull(failureHandler, "failureHandler cannot be null");
        this.failureHandler = failureHandler;
    }
}
