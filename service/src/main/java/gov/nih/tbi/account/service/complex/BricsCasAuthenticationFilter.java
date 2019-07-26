
package gov.nih.tbi.account.service.complex;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.AccountUserDetails;

/**
 * The version of Spring Security we currently imlement does not allow for proxy suppport. This class had to be modified
 * to more closely resemble the Spring 3.1 implementation. Changes allow for this filter to process on any request that
 * contains a ticket (proxy or service) and it allows for the optional redirection after authentication for proxy
 * tickets.
 * 
 */
public class BricsCasAuthenticationFilter extends BricsAbstractAuthenticationProcessingFilter
{
    @Autowired
    ModulesConstants modulesConstants;

    // ~ Static fields/initializers
    // =====================================================================================

    /**
     * See Spring Security implementation of CasAuthenticationFilter for javadoc
     */
    public static final String CAS_STATEFUL_IDENTIFIER = "_cas_stateful_";

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    public static final String CAS_STATELESS_IDENTIFIER = "_cas_stateless_";

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    private String proxyReceptorUrl;

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    private ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    private String artifactParameter = ServiceProperties.DEFAULT_CAS_ARTIFACT_PARAMETER;

    // ~ Constructors
    // ===================================================================================================

    public BricsCasAuthenticationFilter()
    {

        super("/j_spring_cas_security_check");
    }

    // ~ Methods
    // ========================================================================================================

    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response)
            throws AuthenticationException, IOException
    {

		logger.debug("AUTH | Start!");
        // if the request is a proxy request process it and return null to indicate the request has been processed
        if (proxyReceptorRequest(request))
        {
			logger.debug(
					"AUTH | Request is a PGT/IOU pair sent from CAS to proxy receptor URL. See CommonUtils debug info to see pair.");
            CommonUtils.readAndRespondToProxyReceptorRequest(request, response, this.proxyGrantingTicketStorage);
			logger.debug("     | PGT recieved: " + request.getParameter("pgtId"));
            return null;
        }

        final boolean serviceTicketRequest = serviceTicketRequest(request, response);
        String username = serviceTicketRequest ? CAS_STATEFUL_IDENTIFIER : CAS_STATELESS_IDENTIFIER;

        String password = obtainArtifact(request);

        if (password == null)
        {
			logger.error("AUTH | CAS service request did not contain a service ticket parameter. ");
            password = "";
        }
		logger.debug("AUTH | Created UsernamePasswordAuthenticationToken for validation by providers.");
		logger.debug("     | username: " + username);
		logger.debug("     | credentials (aka service ticket): " + password);

		final UsernamePasswordAuthenticationToken authRequest =
				new UsernamePasswordAuthenticationToken(username, password);
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    protected String obtainArtifact(HttpServletRequest request)
    {

        return request.getParameter(artifactParameter);
    }

    /**
     * See Spring implementation of AbstractAuthenticationProcessingFilter for javadoc
     */
    @Override
    protected boolean requiresAuthentication(final HttpServletRequest request, final HttpServletResponse response)
    {
        final boolean serviceTicketRequest = serviceTicketRequest(request, response);
		final boolean prr = proxyReceptorRequest(request);
		final boolean ptr = proxyTicketRequest(serviceTicketRequest, request);
		final boolean result = serviceTicketRequest || prr || ptr;

		// debug all calls except the logging checks (which occur along with every page load)
		if (logger.isDebugEnabled() && !request.getRequestURI().endsWith("loginCheck.ajax"))
        {

			logger.debug("AUTH | NEW REQUEST. CHECK TYPES: " + request.getRequestURI());
			logger.debug("     | Domain: " + request.getLocalName());
			logger.debug(
					"     | Service Ticket Request (url is /j_spring_cas_security_check)?: " + serviceTicketRequest);
			logger.debug("     | Proxy Receptor Request (url is /j_spring_cas_proxy_callback)?: " + prr);
			logger.debug("     | PT Request (ST arg but url not /j_spring_cas_security_check)?: " + ptr);
			logger.debug("     |                                                               -------");
			logger.debug("     | Initiate CAS Authentication workflow? (any of the above true): " + result);
        }

        return result;
    }

    public final void setProxyReceptorUrl(final String proxyReceptorUrl)
    {

        this.proxyReceptorUrl = proxyReceptorUrl;
    }

    public final void setProxyGrantingTicketStorage(final ProxyGrantingTicketStorage proxyGrantingTicketStorage)
    {

        this.proxyGrantingTicketStorage = proxyGrantingTicketStorage;
    }

    public final void setServiceProperties(final ServiceProperties serviceProperties)
    {

        this.artifactParameter = serviceProperties.getArtifactParameter();
    }

    /**
     * Indicates if the request is elgible to process a service ticket. This method exists for readability.
     * 
     * @param request
     * @param response
     * @return
     */
    private boolean serviceTicketRequest(final HttpServletRequest request, final HttpServletResponse response)
    {

        boolean result = super.requiresAuthentication(request, response);
        return result;
    }

    /**
     * Indicates if the request is elgible to process a proxy ticket.
     * 
     * @param request
     * @return
     */
    private boolean proxyTicketRequest(final boolean serviceTicketRequest, final HttpServletRequest request)
    {

        if (serviceTicketRequest)
        {
            return false;
        }
        final boolean result = true && obtainArtifact(request) != null && !authenticated();
        return result;
    }

    /**
     * Determines if a user is already authenticated.
     * 
     * @return
     */
    private boolean authenticated()
    {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    /**
     * Indicates if the request is eligible to be processed as the proxy receptor.
     * 
     * @param request
     * @return
     */
    private boolean proxyReceptorRequest(final HttpServletRequest request)
    {

        final String requestUri = request.getRequestURI();
        final boolean result = proxyReceptorConfigured() && requestUri.endsWith(this.proxyReceptorUrl);
        return result;
    }

    /**
     * Determines if the {@link CasAuthenticationFilter} is configured to handle the proxy receptor requests.
     * 
     * @return
     */
    private boolean proxyReceptorConfigured()
    {

        final boolean result = this.proxyGrantingTicketStorage != null && !CommonUtils.isEmpty(this.proxyReceptorUrl);
        return result;
    }

    /**
     * This overridden successfulAuthenticaiton contains a filter chain so the chain can continue after the successful
     * authentication of a proxy web service. NOTE: The class AbstractAuthenticationProcesssingFilter (this class'
     * parent) had to be overridden to include the parent of this function due to proxying not being supported in out
     * spring secuirty cas version.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, Authentication authResult) throws IOException, ServletException
    {
        AccountUserDetails usrDetails;

        //this if might be unnecessary
        if(authResult instanceof CasAuthenticationToken) {
            CasAuthenticationToken casAuthResult = (CasAuthenticationToken)authResult;
            usrDetails = (AccountUserDetails)casAuthResult.getUserDetails();
            Long diseaseId = usrDetails.getDiseaseId();
            String styleID = this.modulesConstants.getStylingKey(diseaseId);

            request.getSession().setAttribute(ModulesConstants.SESSION_STYLE_KEY, styleID);
        }

        boolean continueFilterChain = proxyTicketRequest(serviceTicketRequest(request, response), request);
        if (!continueFilterChain)
        {
            super.successfulAuthentication(request, response, chain, authResult);
            return;
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Authentication success. Updating SecurityContextHolder to contain: " + authResult);
        }

        SecurityContextHolder.getContext().setAuthentication(authResult);

        // Fire event
        if (this.eventPublisher != null)
        {
            eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }


        chain.doFilter(request, response);
    }
}
