
package gov.nih.tbi.account.service.complex;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

/**
 * 
 * @author mvalei
 * 
 */
public class BricsAuthenticationEntryPoint implements AuthenticationEntryPoint, InitializingBean
{

	private static Logger logger = Logger.getLogger(BricsAuthenticationEntryPoint.class);

    private LinkedHashMap<String, AuthenticationEntryPoint> entryPoints;
    private AuthenticationEntryPoint defaultEntryPoint;

	@Value("#{casSecurityProperties['cas.client.service']}")
	private String[] service;

	@Value("#{casSecurityProperties['cas.server.location']}")
	private String[] casUrl;

	@Value("#{casSecurityProperties['cas.client.key']}")
	private String[] key;


	public BricsAuthenticationEntryPoint() {

	}
	/**
	 * This can be used to define all the entryPoints in context XML, instead of the default
	 * constructor which will create entry points from a properties file.
	 * 
	 * @param entryPoints
	 */
    public BricsAuthenticationEntryPoint(LinkedHashMap<String, AuthenticationEntryPoint> entryPoints)
    {

        this.entryPoints = entryPoints;
    }

    /**
     * EntryPoint which is used when no RequestMatcher returned true
     */
    public void setDefaultEntryPoint(AuthenticationEntryPoint defaultEntryPoint)
    {

        this.defaultEntryPoint = defaultEntryPoint;
    }

    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException
    {
		logger.debug("AUTH | Initiating CAS Auth redirect.");
        // key format = <requiredString>-<antiString>
        // After the requiredString is identified, you need to make sure that the antiString is not identified.
        for (String s : entryPoints.keySet())
        {
            String[] sArray = s.split("-");
			if (request.getServerName() != null && request.getServerName().contains(sArray[0]))
            {
				if (sArray.length <= 1 || !request.getServerName().contains(sArray[1]))
                {
					AuthenticationEntryPoint ep = entryPoints.get(s);
					debugCheck(ep, s, "host");
					ep.commence(request, response, authException);
                    return;
                }
            }
        }

        // No EntryPoint matched, use defaultEntryPoint
		logger.error("    | NO MATCHING ENTRY POINT FOUND. THIS SHOULD NOT OCCUR! Please check cas.properties");
        defaultEntryPoint.commence(request, response, authException);
    }

    public void afterPropertiesSet() throws Exception
    {


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

		entryPoints = new LinkedHashMap<String, AuthenticationEntryPoint>();
		for (int i = 0; i < key.length; i++) {
			ServiceProperties sp = new ServiceProperties();
			sp.setService(service[i]);
			sp.setSendRenew(false);
			sp.afterPropertiesSet(); // verify (since defining this bean in XML would call this
										// method)
			CasAuthenticationEntryPoint ep = new CasAuthenticationEntryPoint();
			ep.setLoginUrl(casUrl[i] + "/login");
			ep.setServiceProperties(sp);
			ep.afterPropertiesSet(); // verify (since defining this bean in XML would call this
										// method)


			entryPoints.put(key[i], ep);
		}


        Assert.notEmpty(entryPoints, "entryPoints must be specified");
        Assert.notNull(defaultEntryPoint, "defaultEntryPoint must be specified");
    }

	private void debugCheck(AuthenticationEntryPoint ep, String key, String h) {
		if (logger.isDebugEnabled()) {
			if (ep instanceof CasAuthenticationEntryPoint) {
				String loginUrl = ((CasAuthenticationEntryPoint) ep).getLoginUrl();
				logger.debug("     | Redirect : " + loginUrl + " due to \"" + key + "\" found in request header: " + h);
			} else {
				logger.error("AuthenticationEntryPoint used is of known type. This should not happen!");
			}
		}
	}

}
