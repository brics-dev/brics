package gov.nih.tbi.guid.ws;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.guid.model.GuidSearchResult;

@Controller
public class GuidWebserviceProvider {

	@Autowired
	private ModulesConstants modulesConstants;

	private static Logger logger = Logger.getLogger(GuidWebserviceProvider.class);

	public List<GuidSearchResult> searchGuids(String jwt, PaginationData pageData, String searchText, String showType,
			boolean hideDuplicate, boolean showAll, List<String> searchColumns, boolean export, boolean inAdmin) {

		String url = "guid/search";
		WebClient client = this.buildGuidClient(url, jwt);

		String sortOrder = (pageData.getAscending()) ? "asc" : "desc";

		JsonObject requestJson = new JsonObject();
		requestJson.add("page", new JsonPrimitive(pageData.getPage()));
		requestJson.add("pageLen", new JsonPrimitive(pageData.getPageSize()));
		requestJson.add("searchText", new JsonPrimitive(searchText));
		requestJson.add("sortColumn", new JsonPrimitive(pageData.getSort()));
		requestJson.add("sortOrder", new JsonPrimitive(sortOrder));
		// options at the moment: "guids", "PseudoGUIDs",
		// "convertedPseudoguids", "unconvertedPseudoguids"
		requestJson.add("type", new JsonPrimitive(showType));
		requestJson.add("hideDuplicate", new JsonPrimitive(hideDuplicate));
		requestJson.add("showAll", new JsonPrimitive(showAll));
		requestJson.add("export", new JsonPrimitive(export));
		requestJson.add("inAdmin", new JsonPrimitive(inAdmin));
		JsonArray searchCols = new JsonArray();
		// column name options:
		// guid, type, organization, fullName, linked, dateCreated
		// but that's not what the datatable sends!
		for (String column : searchColumns) {
			searchCols.add(new JsonPrimitive(column));
		}
		requestJson.add("searchCols", searchCols);
		Response response = client.post(requestJson.toString());

		List<GuidSearchResult> output = new ArrayList<GuidSearchResult>();
		if (response.getStatus() != 200) {
			logger.error("search GUID webservice call failed with code: " + response.getStatus());
			return output;
		}

		JsonParser parser = new JsonParser();
		String responseData = response.readEntity(String.class);
		JsonObject responseObject = (JsonObject) parser.parse(responseData);

		pageData.setNumSearchResults(getTotalCount(jwt, inAdmin));
		if (responseObject.has("recordsFiltered")) {
			pageData.setNumFilteredResults(responseObject.get("recordsFiltered").getAsInt());
		}

		JsonArray dataArray = responseObject.getAsJsonArray("data");
		int arraySize = dataArray.size();
		for (int i = 0; i < arraySize; i++) {
			JsonObject responseElement = (JsonObject) dataArray.get(i);
			GuidSearchResult guidResult = jsonToResult(responseElement, parser);
			output.add(guidResult);
		}

		return output;
	}

	private int getTotalCount(String jwt, boolean inAdmin) {
		String isAdmin = (inAdmin) ? "true" : "false";
		String url = "guid/count?inAdmin=" + isAdmin;
		WebClient countClient = this.buildGuidClient(url, jwt);
		Response countResponse = countClient.get();
		if (countResponse.getStatus() != 200) {
			logger.error("Getting the total number of GUIDs failed with error code " + countResponse.getStatus());
			return 0;
		}
		return countResponse.readEntity(Integer.class);
	}

	private GuidSearchResult jsonToResult(JsonObject input, JsonParser parser) {
		GuidSearchResult output = new GuidSearchResult();
		output.setGuid(input.get("guid").getAsString());
		output.setType(input.get("type").getAsString());
		output.setServerShortName(input.get("serverShortName").getAsString());
		if (!input.get("organization").isJsonNull()) {
			output.setOrganization(input.get("organization").getAsString());
		}
		if (!input.get("fullName").isJsonNull()) {
			output.setFullName(input.get("fullName").getAsString());
		}
		output.setDateCreated(input.get("date").getAsString());

		if (!input.get("linked").isJsonNull()) {
			JsonArray linkedArray = (JsonArray) parser.parse(input.get("linked").getAsString());
			int linkedSize = linkedArray.size();
			List<String> outputLinked = new ArrayList<String>();
			for (int i = 0; i < linkedSize; i++) {
				outputLinked.add(linkedArray.get(i).getAsString());
			}
			output.setLinked(outputLinked);
		}

		if (!input.get("detailsFlag").isJsonNull()) {
			output.setDetailsFlag(input.get("detailsFlag").getAsBoolean());
		}
		
		if (!input.get("cohort").isJsonNull()) {
			output.setCohort(input.get("cohort").getAsString());
		}

		return output;
	}

	/**
	 * Get guids and their associated pseudoguids in key:[values] of guid:[pseudoguids] format. calls
	 * 
	 * @param guids
	 * @param baseUrl
	 * @return
	 */
	public List<String> getPseudoGuidMapping(String guid, String jwt) {

		String url = "pseudo_guids/by_guid/" + guid;
		WebClient client = this.buildGuidClient(url, jwt);
		Response r = client.get(Response.class);

		List<String> output = new ArrayList<String>();

		if (r.getStatus() != 200) {
			logger.error("getPseudoGuidMapping web service call failed status code: " + r.getStatus());
			return output;
		}

		JsonParser parser = new JsonParser();
		String responseData = r.readEntity(String.class);
		JsonArray jsonArr = (JsonArray) parser.parse(responseData);

		for (int i = 0; i < jsonArr.size(); i++) {
			String pseudoGuid = jsonArr.get(i).getAsString();
			output.add(pseudoGuid);
		}

		return output;
	}

	/**
	 * Get pseudoGuid and their associated guid. calls /guid/by_pseudoguid/
	 * 
	 * @param pseudoguid
	 * @param baseUrl
	 * @param jwt
	 * @return
	 */
	public List<String> getGuidMapping(String pseudoGuid, String jwt) {

		String url = "guid/by_pseudoguid/" + pseudoGuid;
		WebClient client = this.buildGuidClient(url, jwt);
		Response r = client.get(Response.class);

		List<String> output = new ArrayList<String>();

		if (r.getStatus() != 200) {
			logger.error("getGuidMapping web service call failed status code: " + r.getStatus());
			return output;
		}

		JsonParser parser = new JsonParser();
		String responseData = r.readEntity(String.class);
		JsonArray jsonArr = (JsonArray) parser.parse(responseData);

		if (jsonArr.size() > 0) {
			output.add(jsonArr.get(0).getAsString());
		}

		return output;
	}

	public String getGuidDetails(String guid, String jwt) {

		String url = "guid/details/" + guid;
		String output = "{}";

		WebClient client = this.buildGuidClient(url, jwt);
		Response r = client.post("");

		if (r.getStatus() != 200) {
			logger.error("getGuidDetails webservice call failed with status " + r.getStatus());
			return output;
		}

		return r.readEntity(String.class);
	}

	/**
	 * Returns a list of "TRUE" or "FALSE" strings that matches with the order of the input guid list to indicate if
	 * each guid exists or not
	 * 
	 * @param guidList
	 * @param jwt
	 * @return
	 */
	public List<String> validateGuids(List<String> guidList, String jwt) {

		String url = "valid/guids";

		JsonArray guidListJson = new JsonArray();
		for (String guid : guidList) {
			guidListJson.add(new JsonPrimitive(guid));
		}

		List<String> output = new ArrayList<String>();

		logger.info("attempting to call GUID validation webservice at " + url);
		WebClient client = this.buildGuidClient(url, jwt);
		Response r = client.post(guidListJson.toString());

		if (r.getStatus() != 200) {
			logger.error("Validate Guids web service call failed status code: " + r.getStatus());
			return output;
		}

		JsonParser parser = new JsonParser();
		String respData = r.readEntity(String.class);
		JsonObject respJson = parser.parse(respData).getAsJsonObject();

		for (String guid : guidList) {
			String isValid = "ERROR";

			if (respJson.has(guid) && !respJson.get(guid).isJsonNull()) {
				isValid = respJson.get(guid).getAsString().toUpperCase();
			}
			output.add(isValid);
		}

		return output;
	}

	/**
	 * Builds a WebClient for the GUID Centralized Webservice that includes the Authorization header with the provided
	 * JWT.
	 * 
	 * @param url the URL to call
	 * @param jwt the JWT to add to the call
	 * @return basic WebClient that should be expanded to call the GUID services
	 */
	private WebClient buildGuidClient(String url, String jwt) {
		String guidServiceUrl = modulesConstants.getModulesGTServiceURL() + url;
		logger.info("Calling GUID web service at url " + guidServiceUrl);

		WebClient client = WebClient.create(guidServiceUrl);
		client.type(MediaType.APPLICATION_JSON);
		client.header("Authorization", "JWT " + jwt);
		return client;
	}
}
