package gov.nih.tbi.query.ws;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import gov.nih.tbi.account.ws.RestAuthenticationProvider;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.query.model.DerivedDataContainer;
import gov.nih.tbi.query.model.DerivedDataRequest;
import gov.nih.tbi.query.model.RepeatableGroupDataElement;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;

/**
 * Provides an implementation for the secure and public Rest service calls of the BRICS Query Tool module.
 * 
 * Developer Note: All the rest service call functions defined in this class must call
 * RestAuthenticationProvider.ticketValid() and RestAuthenticationProvider.cleanup() at the beginning and end of the
 * function respectively to guarantee validity of the proxy ticket. Finally, the user must get the proxyTicket argument
 * (i.e. ticket=PT-3284792379842) and append it to the web service url if the user needs to be authenticated. Any ws
 * calls without a ticket will be considered anon. The user can get this formated property with
 * RestAuthenticationProvider.getTicketProperty().
 * 
 * @author jim3
 *
 */
public class RestQueryToolProvider extends RestAuthenticationProvider {

	private static Logger logger = Logger.getLogger(RestQueryToolProvider.class);
	protected final String restServiceUrl = "query/service/";
	private int DERIVED_DATA_SERVICE_TIMEOUT = 0;

	public RestQueryToolProvider(String serverLocation, String proxyTicket) {
		super(serverLocation, proxyTicket);
	}

	public DerivedDataContainer getDerivedData(NameAndVersion formNameAndVersion,
			List<RepeatableGroupDataElement> repeatableGroupDataElements, Set<String> guids)
			throws UnsupportedEncodingException {
		ticketValid();

		// combine all arguments into a single request XML
		DerivedDataRequest requestPayload = new DerivedDataRequest();
		requestPayload.setFormNameAndVersion(formNameAndVersion);
		requestPayload.setRepeatableGroupDataElementRequests(repeatableGroupDataElements);
		requestPayload.setGuids(guids);

		String restUrl = serverUrl + restServiceUrl + "derived_data/search" + getTicketProperty();

		logger.info("Derived Data Webservice URL: " + restUrl);
		WebClient client = WebClient.create(restUrl);
		client.type("application/xml");
	    client.accept("application/xml");
		HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();
		
		//need to extend timeout because querying could be time consuming.
		conduit.getClient().setReceiveTimeout(DERIVED_DATA_SERVICE_TIMEOUT);
		DerivedDataContainer derivedDataContainer = null;
		try {
			// post request and get response
			derivedDataContainer = client.post(requestPayload, DerivedDataContainer.class);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cleanup();
		}

		return derivedDataContainer;
	}
}
