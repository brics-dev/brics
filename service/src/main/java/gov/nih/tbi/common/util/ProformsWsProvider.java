package gov.nih.tbi.common.util;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import gov.nih.tbi.query.model.SummaryResult;

/**
 * Handles all web service calls to any ProFoRMS web service. All authenticated web service calls (nearly all of them)
 * will need a proxy ticket generated for each call. Proxy tickets can be generated with the
 * {@link #getProxyTicket(String)} method.
 * 
 * The is class is using the "prototype" scope, which should create a new object for each request. This is intended to
 * prevent cases were multiple users make requests at the same time and get each others results, which would be likely
 * if this class was in the default singleton, or even application, scope. In other words, do not change the scope of
 * this class unless there is a fairly heavy performance/memory penalty in keeping it in the prototype scope.
 * 
 * @author joseng
 *
 */
@Service
@Scope(value = "prototype")
public class ProformsWsProvider extends BaseWsProvider {
	private static final String PROTOCOL_LIST_REL_PATH = "ws/protocol/getProtocolList/";
	private static final String ARE_ANY_COLLECTION_REL_PATH = "ws/public/datacollection/areAnyDataCollections/";
	private static final String ARE_ANY_EFORMS_IN_VISITTYPE_REL_PATH = "ws/public/datacollection/areThereEformsAttachedToAnyVisitType/";
	private static final String SUMMARY_DATA_REL_PATH = "ws/public/summaryData/program/";

	public ProformsWsProvider() {}

	/**
	 * Gets a JSON array of protocols from ProFoRMS that are releated to the given study prefix ID.
	 * 
	 * @param proformsBaseUrl - The base URL for proforms.
	 * @param studyPrefixId
	 * @return
	 * @throws JsonParseException
	 * @throws WebApplicationException
	 * @throws IOException
	 */
	public JsonArray getStudyAssoPfProtocols(String proformsBaseUrl, String studyPrefixId)
			throws JsonParseException, WebApplicationException, IOException {
		StringBuffer baseWsUrl = new StringBuffer();
		baseWsUrl.append(proformsBaseUrl);
		baseWsUrl.append(ProformsWsProvider.PROTOCOL_LIST_REL_PATH );
		baseWsUrl.append(encodeUrlParam(studyPrefixId));
		System.out.println("Getting associated Protocols with URL: " + baseWsUrl.toString());
		WebClient client = WebClient.create(baseWsUrl.toString());
		String proxyTicket = getProxyTicket(proformsBaseUrl);

		client.query(ProformsWsProvider.PROXY_TICKET_QUERY_PARAM, proxyTicket).accept(MediaType.APPLICATION_JSON);

		String strJsonArray = client.get(String.class);
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(strJsonArray);

		return element.getAsJsonArray();
	}
	
	/**
	 * Gets SummaryResult from ProFoRMS that are related to the given chartName this is for public site
	 * 
	 * @param proformsBaseUrl - The base URL for proforms.
	 * @param chartName
	 * @return SummaryResult
	 * @throws JsonParseException
	 * @throws WebApplicationException
	 * @throws IOException
	 */
	public SummaryResult getSummaryData(String proformsBaseUrl, String chartName)
			throws JsonParseException, WebApplicationException, IOException {
		String baseWsUrl = proformsBaseUrl +  ProformsWsProvider.SUMMARY_DATA_REL_PATH + encodeUrlParam(chartName);
		System.out.println("Getting associated SummaryData with URL: " + baseWsUrl);
		WebClient client = WebClient.create(baseWsUrl);
		client.accept(MediaType.APPLICATION_JSON);

		String strJsonArray = client.get(String.class);
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(strJsonArray);
		JsonArray result = element.getAsJsonArray();
		SummaryResult summaryResult = new SummaryResult();
		Map<String, String> summaryResultsMap = summaryResult.getResults();
		//convert JsonArray to summaryResult
		for (int i=0; i < result.size(); i++) {
			JsonObject obj = result.get(i).getAsJsonObject();
			String count = obj.get("count").getAsString();
			String category =  obj.get("category").getAsString();
			summaryResultsMap.put(category, count);
		}
		
		
		
		return summaryResult;
	}	
	
	
	public boolean areThereDataCollections(String proformsBaseUrl, String eformShortName)
			throws JsonParseException, WebApplicationException, IOException {
		String baseWsUrl = proformsBaseUrl + ProformsWsProvider.ARE_ANY_COLLECTION_REL_PATH + encodeUrlParam(eformShortName);
		WebClient client = WebClient.create(baseWsUrl);
		String proxyTicket = getProxyTicket(proformsBaseUrl);

		client.query(ProformsWsProvider.PROXY_TICKET_QUERY_PARAM, proxyTicket).accept(MediaType.APPLICATION_JSON);

		String areAnyCollections = client.get(String.class);

		return Boolean.parseBoolean(areAnyCollections); 
	}
	
	
	public boolean areThereEformsAttachedToAnyVisitType(String proformsBaseUrl, String eformShortName)
			throws JsonParseException, WebApplicationException, IOException {
		String baseWsUrl = proformsBaseUrl + ProformsWsProvider.ARE_ANY_EFORMS_IN_VISITTYPE_REL_PATH + encodeUrlParam(eformShortName);
		WebClient client = WebClient.create(baseWsUrl);
		String proxyTicket = getProxyTicket(proformsBaseUrl);

		client.query(ProformsWsProvider.PROXY_TICKET_QUERY_PARAM, proxyTicket).accept(MediaType.APPLICATION_JSON);

		String areAnyEformsInVT = client.get(String.class);

		return Boolean.parseBoolean(areAnyEformsInVT); 
	}

}
