package gov.nih.tbi.repository.ws;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;

import org.apache.log4j.Logger;

/**
 * Provides a lightweight implemenation of the client side classes needed to utilize a JaxWS Service.
 * 
 * @author Andrew Johnson
 *
 */
public class AccessionProvider
{
	
	/***************************************************************************************************/
	
	@SuppressWarnings( "unused" )
	private static Logger logger = Logger.getLogger( AccessionProvider.class );
	
	private static final String namespaceURI = "http://cxf.ws.repository.tbi.nih.gov/";
	private static final String serviceName = "AccessionWebServiceImplService";
	private static final String portName = "AccessionWebServiceImplPort";
	
	private static URL WSDL_LOCATION;
	
	/***************************************************************************************************/
	
	private Service service;
	
	/***************************************************************************************************/
	
	/**
	 * Constructor that takes the web service WSDL URL
	 * 
	 * @param accessionWsdlLocation
	 * @throws MalformedURLException
	 */
	public AccessionProvider(String accessionWsdlLocation) throws MalformedURLException
	{
		WSDL_LOCATION = new URL(AccessionProvider.class.getResource("."), accessionWsdlLocation);
		
		service = new AccessionService(WSDL_LOCATION, new QName(namespaceURI, serviceName));
	}
	
	/**
	 * Creates a new connection to consume the web service.  This should only need to be called once in a 
	 *   program.
	 * 
	 * @return
	 */
	@WebEndpoint(name = "dictionaryWebService")
    public AccessionWebService getAccessionWebService() {
        return service.getPort(new QName(namespaceURI, portName), AccessionWebService.class);
    }
	
	/***************************************************************************************************/
	
	/**
	 * Private class that extends service.  Service is part of the JaxWS reference implementation,
	 *   and allows the system to create a connection to a web service.
	 * 
	 * @author Andrew Johnson
	 *
	 */
	private class AccessionService extends Service
	{

		protected AccessionService( URL wsdlDocumentLocation, QName serviceName )
		{
			super( wsdlDocumentLocation, serviceName );
		}
		
	}

}
