package gov.nih.tbi.common.util;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

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
		String baseWsUrl = proformsBaseUrl + ProformsWsProvider.PROTOCOL_LIST_REL_PATH + studyPrefixId;
		WebClient client = WebClient.create(baseWsUrl);
		String proxyTicket = getProxyTicket(proformsBaseUrl);

		client.query(ProformsWsProvider.PROXY_TICKET_QUERY_PARAM, proxyTicket).accept(MediaType.APPLICATION_JSON);

		String strJsonArray = client.get(String.class);
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(strJsonArray);

		return element.getAsJsonArray();
	}
}
