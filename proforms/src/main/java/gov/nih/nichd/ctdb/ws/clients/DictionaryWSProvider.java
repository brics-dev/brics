package gov.nih.nichd.ctdb.ws.clients;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import gov.nih.nichd.ctdb.common.CasProxyTicketException;
import gov.nih.nichd.ctdb.security.util.BaseWsProvider;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.dictionary.model.EformRestServiceModel.BasicEformList;
import gov.nih.tbi.dictionary.model.EformRestServiceModel.DataElementNameList;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;

public class DictionaryWSProvider extends BaseWsProvider {
	private static final Logger logger = Logger.getLogger(DictionaryWSProvider.class);

	public DictionaryWSProvider() {}
	/**
	 * Default constructor.
	 * 
	 * @param request - The HTTP request object, which is used to generate the proxy ticket.
	 */
	public DictionaryWSProvider(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * Method to get the published map of BasicEform so that they can be associated to Visit Type to start data
	 * collection.
	 * 
	 * @return A map of published BasicEform objects from the Dictionary, with the key being the eForm short name.
	 * @throws CasProxyTicketException When there is an error when generating a proxy ticket for the Dictionary request.
	 * @throws WebApplicationException When there is an error when getting a list of eForms from the Dictionary web
	 *         service.
	 */
	public Map<String, BasicEform> getPublishedEformMapFromRestWs()
			throws CasProxyTicketException, WebApplicationException {
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.published.eform.url");

		try {
			SecuritySessionUtil ssu = new SecuritySessionUtil(request);
			String proxyTicket = ssu.getProxyTicket(restfulDomain);

			if (proxyTicket != null) {
				restfulUrl = ssu.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
			}
		}
		catch (Exception e) {
			throw new CasProxyTicketException(
					"Couldn't get the proxy ticket for the Dictionary WS call to get a list of eForms.", e);
		}

		logger.info("Getting eForms with the following URL: " + restfulUrl);
		
		Client client = ClientBuilder.newClient();
		WebTarget wt = client.target(restfulUrl);
		BasicEformList basicEformList = wt.request(MediaType.APPLICATION_XML).get(BasicEformList.class);

		// Create the eForm map from the returned list, with the key being each eForm's short name.
		List<BasicEform> eFormList = basicEformList.getList();
		Map<String, BasicEform> eFromMap = new HashMap<String, BasicEform>(eFormList.size());
		String disablePromis = SysPropUtil.getProperty("disable.promis");
		for (BasicEform bf : eFormList) {
			if(Boolean.valueOf(disablePromis)) {
				if(!bf.getIsCAT()) {
					eFromMap.put(bf.getShortName(), bf);
				}
			}else {
				eFromMap.put(bf.getShortName(), bf);
			}	
		}

		return eFromMap;
	}

	/**
	 * Retrieves a list of data element short names for the given JSON array of eForm short names. The relationship between data element
	 * short names and eForms will not be preserved in the return list, nor is it needed for this type of request.
	 * 
	 * @param eFormShortNames - A JSON array containing the eForm short names used to search for data element names.
	 * @return A list of data element names from the Dictionary web service, which will be used for pre-population.
	 * @throws CasProxyTicketException When there is an error when generating the proxy ticket.
	 * @throws WebApplicationException When there is an error when getting a list of data element names from the Dictionary web service.
	 */
	public List<String> getDeNamesByEformShortNameJsArray(JSONArray eFormShortNames)
			throws CasProxyTicketException, WebApplicationException {
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.eform.ddt.url") + "/dataElementNameList/byEformShortNameJsonArray";

		try {
			SecuritySessionUtil ssu = new SecuritySessionUtil(request);
			String proxyTicket = ssu.getProxyTicket(restfulDomain);

			if (proxyTicket != null) {
				restfulUrl = ssu.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
			}
		}
		catch (Exception e) {
			throw new CasProxyTicketException("Couldn't get the proxy ticket for the Dictionary WS call to get a list of eForms.", e);
		}

		Client client = ClientBuilder.newClient();
		WebTarget wt = client.target(restfulUrl);
		Entity<String> entity = Entity.entity(eFormShortNames.toString(), MediaType.APPLICATION_JSON);
		DataElementNameList dataElemNameList = wt.request(MediaType.APPLICATION_XML).post(entity, DataElementNameList.class);

		return dataElemNameList.getList();
	}
	
	/**
	 * Retrieves a list of data element short names for the given eForm short name.
	 * 
	 * @param eFormShortName - The eForm short name used to query the data element short names.
	 * @return A list of data element short names that are associated with the given eForm short name.
	 * @throws CasProxyTicketException When there is an error when generating the proxy ticket.
	 * @throws WebApplicationException When there is an error when getting a list of data element names from the Dictionary web service.
	 */
	public List<String> getDeNamesByEformShortName(String eFormShortName) 
			throws CasProxyTicketException, WebApplicationException {
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.eform.ddt.url") + 
				"/dataElementNameList/byEformShortName/" + eFormShortName;
		
		try {
			SecuritySessionUtil ssu = new SecuritySessionUtil(request);
			String proxyTicket = ssu.getProxyTicket(restfulDomain);

			if (proxyTicket != null) {
				restfulUrl = ssu.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
			}
		}
		catch (Exception e) {
			throw new CasProxyTicketException("Couldn't get the proxy ticket for the Dictionary WS call to get a list of eForms.", e);
		}
		
		Client client = ClientBuilder.newClient();
		WebTarget wt = client.target(restfulUrl);
		DataElementNameList dataElemNameList = wt.request(MediaType.APPLICATION_XML).get(DataElementNameList.class);
		
		return dataElemNameList.getList();
	}
	
	/**
	 * Method to get the Data Element by given short name.
	 * 
	 * @param dataElementName - The data element short name to query for.
	 * @return The DataElement object that matches the given short name, or null if none can be found.
	 * @throws CasProxyTicketException If there an error generating the proxy ticket.
	 * @throws WebApplicationException If an error response is received by the web service call.
	 */
	public DataElement getDataElementByName(String dataElementName) throws CasProxyTicketException, WebApplicationException {
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.ddt.de.url") + "/" + dataElementName;
		SecuritySessionUtil ssu = new SecuritySessionUtil(request);
		String proxyTicket;
		Client client = ClientBuilder.newClient();
		
		try {
			proxyTicket = ssu.getProxyTicket(restfulDomain);
			
			if ( proxyTicket != null ) {
				restfulUrl = ssu.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
			}
		}
		catch (Exception e) {
			throw new CasProxyTicketException("Error with the proxy ticket.", e);
		}
		
		logger.info("DictionaryWsClient.java->getDataElementByName() restfulUrl: " + restfulUrl);
		
		DataElement dataElement = client.target(restfulUrl).request(MediaType.APPLICATION_XML, MediaType.TEXT_XML).get(DataElement.class);

		return dataElement;
	}

	public Map<String, BasicEform> getPublishedEformListFromWs()
			throws CasProxyTicketException, WebApplicationException {
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.published.eform.url");

		try {
			String proxyTicket = getProxyTicket(restfulDomain);

			if (proxyTicket != null) {
				restfulUrl = compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
			}
		} catch (Exception e) {
			throw new CasProxyTicketException(
					"Couldn't get the proxy ticket for the Dictionary WS call to get a list of eForms.", e);
		}

		logger.info("Getting eForms with the following URL: " + restfulUrl);

		Client client = ClientBuilder.newClient();
		WebTarget wt = client.target(restfulUrl);
		BasicEformList basicEformList = wt.request(MediaType.APPLICATION_XML).get(BasicEformList.class);

		// Create the eForm map from the returned list, with the key being each eForm's short name.
		List<BasicEform> eFormList = basicEformList.getList();
		Map<String, BasicEform> eFromMap = new HashMap<String, BasicEform>(eFormList.size());
		String disablePromis = SysPropUtil.getProperty("disable.promis");
		for (BasicEform bf : eFormList) {
			if (Boolean.valueOf(disablePromis)) {
				if (!bf.getIsCAT()) {
					eFromMap.put(bf.getShortName(), bf);
				}
			} else {
				eFromMap.put(bf.getShortName(), bf);
			}
		}

		return eFromMap;
	}

}
