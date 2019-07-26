
package gov.nih.tbi.account.ws;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.ResourceConnectionException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;

import org.apache.commons.lang3.StringUtils;

/**
 * Provides
 * 
 * @author Michael Valeiras
 * 
 */
public class RestAuthenticationProvider
{

    /***************************************************************************************************/

    private static Logger logger = Logger.getLogger(RestAuthenticationProvider.class);

    private final String TICKET_PROPERTY = "ticket=";

    protected String serverUrl = null;
    private boolean ticketUsed = false;
    private String proxyTicket = null;
    private String ticketProp = null;
	private boolean wsSecure;

    /***************************************************************************************************/

    HttpClient client = new HttpClient();


    /***************************************************************************************************/

    /**
     * A constructor
     * 
     * @param serverLocation
     * @param proxyTicket
     * @throws MalformedURLException
     */
    public RestAuthenticationProvider(String serverLocation, String proxyTicket)
    {
		this(serverLocation, proxyTicket, ModulesConstants.getWsSecured());
	}

    /**
	 * A constructor that should ONLY BE USED IN THE QT. In the query tool, the user can supply if
	 * the web-services should be secured form application constants. Pulling the property from
	 * ModulesConstants will not work if the application is not controlled with spring.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @param wsSecure
	 */
	public RestAuthenticationProvider(String serverLocation, String proxyTicket, boolean wsSecure) {
        serverUrl = stripToDomain(serverLocation);
        if (ServiceConstants.EMPTY_STRING.equals(proxyTicket))
        {
            proxyTicket = null;
        }
        this.proxyTicket = proxyTicket;
		this.wsSecure = wsSecure;
    }

    /*************************************** HELPERS *************************************************/
    // THIS FUNCITONS MUST BE CALLED BEFORE AND AFTER EVERY WEB SERVICE CALL. EVEN PUBLIC ONES!

    protected void cleanup()
    {

        if (proxyTicket != null)
        {
            ticketUsed = true;
        }
    }

    protected void ticketValid()
    {

        if (proxyTicket == null || ticketUsed == false)
        {
            return;
        }
        throw new IllegalArgumentException("The proxy ticket for this restful web service call has already been used.");
    }

    protected String getTicketProperty() throws UnsupportedEncodingException
    {

		if (!wsSecure)
        {
            return ServiceConstants.EMPTY_STRING;
        }
        if (proxyTicket != null)
        {
            return TICKET_PROPERTY + URLEncoder.encode(proxyTicket, "UTF-8");
        }
        return ServiceConstants.EMPTY_STRING;
    }

    /**
     * Strip away any path information after the domain of a url
     * 
     * @param url
     * @return
     */
    private static String stripToDomain(String url)
    {

        String[] brokenUrl = url.split("\\.", 2);

        // this check will prevent array out of bound errors
        if (brokenUrl.length < 2)
        {
            return url;
        }

        String[] splitDomain = brokenUrl[1].split("/");
        return brokenUrl[0] + "." + splitDomain[0] + "/";
    }

    protected Boolean isProxyTicketNull()
    {

        if (proxyTicket != null)
        {
            return false;
        }
        return true;
    }

    /**
     * Encodes the parameter of the URL with escape characters
     * 
     * @param param
     *            - the parameter to encode
     * @return Properly encoded parameter (UTF-8)
     * @throws UnsupportedEncodingException
     */
    public static String encodeUrlParam(String param) throws UnsupportedEncodingException
    {

        if (param == null || ServiceConstants.EMPTY_STRING.equals(param))
        {
            return param;
        }

        String encoded = URLEncoder.encode(param, "UTF-8");
        return encoded;
    }

    /**
     * Builds a list of parameters from a list of variables and their arguments. Will also encode the parameters in
     * UTF-8.
     * 
     * @param variables
     * @param parameters
     * @return A list of parameters like...?variable1=parameter1&variable2=parameter2...&variableN=parameterN
     * @throws UnsupportedEncodingException
     */
    public static String buildParameters(String[] variables, String[] parameters, String ticketProperty)
            throws UnsupportedEncodingException
    {

        StringBuilder builder = new StringBuilder();

        if (variables.length != parameters.length)
        {
            throw new RuntimeException("Number of variables and parameters must be the same!");
        }

        // counts the number of parameters
        int parameterCount = 0;

        for (int i = 0; i < variables.length; i++)
        {
            if (parameters[i] == null || ServiceConstants.EMPTY_STRING.equals(parameters[i]))
            {
                continue;
            }

            if (parameterCount == 0)
            {
                builder.append("?");
            }
            else
            // if more than one parameter already exists
            {
                builder.append("&");
            }

            // variable=parameter
            builder.append(variables[i]).append("=").append(encodeUrlParam(parameters[i]));
            parameterCount++;
        }

        if (ticketProperty != null && !ServiceConstants.EMPTY_STRING.equals(ticketProperty))
        {
            if (parameterCount == 0)
            {
                builder.append("?");
            }
            else
            {
                builder.append("&");
            }

            builder.append(ticketProperty);
        }

        return builder.toString();
    }

    public HttpClient getClient()
    {

        return client;
    }

    public void setClient(HttpClient client)
    {

        this.client = client;
    }
}
