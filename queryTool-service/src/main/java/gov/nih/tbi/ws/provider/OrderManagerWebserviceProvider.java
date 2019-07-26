package gov.nih.tbi.ws.provider;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

import gov.nih.tbi.commons.util.BRICSStringUtils;

public class OrderManagerWebserviceProvider extends RestAuthenticationProvider {

	private static Logger log = Logger.getLogger(OrderManagerWebserviceProvider.class);
	
    /**
     * A constructor for a rest provider in which the serverLocation provided is the domain of the web service call
     * being made. In the case that any path information other than a domain (such as /portal), that information is
     * stripped.
     * 
     * A proxyTicket provided can only be used one time. If RestAccountProvider attempts to use the proxy ticket
     * multiple times, an exception is thrown. If a null or blank string is provided, then public calls may be made.
     * 
     * @param serverLocation
     * @param proxyTicket
     * @throws MalformedURLException
     */
    public OrderManagerWebserviceProvider(String serverLocation, String proxyTicket)
    {

        super(serverLocation, proxyTicket);
    }

	/**
	 * FOR QUERY TOOL USE ONLY. MUST PASS ApplicationConstants.isWebservicesSecured() INTO THE THIRD
	 * ARG.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @param wsSecure
	 */
	public OrderManagerWebserviceProvider(String serverLocation, String proxyTicket, boolean wsSecure) {
		super(serverLocation, proxyTicket, wsSecure);
	}

	public void addBiosampleToQueue(String formName, Map<String, String> biosampleRow , String path) throws UnsupportedEncodingException{
	
		
		  // generate parameters for the service invocation, add them to the baseURL for order manager
        
        StringBuilder sb = new StringBuilder();
        sb.append("formName=").append(formName);
       
        if (!biosampleRow.isEmpty()) {

               for (String key : biosampleRow.keySet()) {

                      // args for OM service are DE shortname = cellValue
                      String argName = key.substring(key.lastIndexOf('.') + 1, key.length());
                      String argValue = biosampleRow.get(key);
                      sb.append("&").append(argName).append("=").append(argValue);
               }
        }

        WebClient client = createWebClient(path);
        client.query("ticket", getEncodedTicket());
       
        log.info("Connecting to OrderManagerService at address: " + client.getCurrentURI().toString());
        Response response = client.accept(MediaType.TEXT_XML).post(sb.toString());
	}

	/**
	 * @throws UnsupportedEncodingException 
	 * Returns a list of Coriell IDs from the current user's biosample queue
	 * 
	 * @param assertion
	 * @return
	 * @throws  
	 */
	public Set<String> getBiosampleFromQueue(String path /* constants.getQueryOrderQueueURL() */) throws UnsupportedEncodingException {

		WebClient client = createWebClient(path);

		client.query("ticket", getEncodedTicket());

		String biosamplesString = client.accept(MediaType.TEXT_XML).get(String.class);

		return new HashSet<String>(BRICSStringUtils.delimitedStringToList(biosamplesString, ","));
	}

}
