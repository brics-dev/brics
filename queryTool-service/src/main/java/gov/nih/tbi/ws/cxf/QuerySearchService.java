package gov.nih.tbi.ws.cxf;

import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.FacetItem;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.semantic.model.QueryPermissions.FormResultPermission;
import gov.nih.tbi.semantic.model.QueryPermissions.StudyResultPermission;
import gov.nih.tbi.service.SearchManager;
import gov.nih.tbi.service.model.PermissionModel;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * This class contains web service methods for searching on Studies, Forms and Data Element tabs.
 * 
 * @author jim3
 */
@Path("/query/")
public class QuerySearchService extends QueryBaseRestService {

	private static final Logger logger = Logger.getLogger(QuerySearchService.class);

	@Autowired
	private SearchManager searchManager;

	@Autowired
	PermissionModel permissionModel;

	@Autowired
	ApplicationConstants constants;

	// TODO: check getAuthenticatedAccount() for all web service methods

	/**
	 * Web service that lists all studies that user has access to. Url:
	 * http://hostname:8080/query/service/query/studyResults/
	 * 
	 * @return json string of list of StudyResult objects.
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("studyResults/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getStudyResults() throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		logger.info("Retrieving all studyResults");
		List<StudyResult> studyResults = searchManager.getStudiesFromCache(permissionModel);

		JsonArray jsonArray = new JsonArray();
		for (StudyResult studyResult : studyResults) {
			jsonArray.add(studyResultToJson(studyResult));
		}

		return jsonArray.toString();
	}

	/**
	 * Web service that lists all forms that user has access to. Url:
	 * http://hostname:8080/query/service/query/formResults/
	 * 
	 * @return json string of list of FormResult objects.
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("formResults/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getFormResults() throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		logger.info("Retrieving all formResults");
		List<FormResult> formResults = searchManager.getFormsFromCache();

		JsonArray jsonArray = new JsonArray();
		for (FormResult formResult : formResults) {
			jsonArray.add(formResultToJson(formResult));
		}

		return jsonArray.toString();
	}

	/**
	 * Web service that lists all data elements and each of their count in the forms.
	 * 
	 * @return json string of list of Data Element facet items.
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("deFacetItems/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDeFacetItems() throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		logger.info("Retrieving Data Element list");
		List<FacetItem> deFacetItems = searchManager.getDeFacetItems();

		JsonArray jsonArray = new JsonArray();
		for (FacetItem deFacetItem : deFacetItems) {
			jsonArray.add(deFacetItem.toJson());
		}

		return jsonArray.toString();
	}

	/**
	 * http://hostname:8080/query/service/query/studies?textValue=...
	 * 
	 * @param textValue
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("studies")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchStudies(@QueryParam("textValue") String textValue)
			throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		logger.info("Searching studyResults with text " + textValue);

		List<StudyResult> studyResults = searchManager.searchStudies(textValue, permissionModel);

		JsonArray jsonArray = new JsonArray();
		for (StudyResult studyResult : studyResults) {
			jsonArray.add(studyResultToJson(studyResult));
		}

		return jsonArray.toString();
	}


	@GET
	@Path("forms")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchForms(@QueryParam("textValue") String textValue)
			throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		logger.info("Searching formResults with text " + textValue);

		List<FormResult> formResults = searchManager.searchForms(textValue);

		JsonArray jsonArray = new JsonArray();
		for (FormResult formResult : formResults) {
			jsonArray.add(formResultToJson(formResult));
		}

		return jsonArray.toString();
	}

	@GET
	@Path("deForms")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchDeForms(@QueryParam("textValue") String textValue,
			@QueryParam("selectedDeUris") List<String> deUris)
			throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		logger.info("Searching formResults with text " + textValue + " and selected data elements");

		List<FormResult> formResults = searchManager.searchDeForms(textValue, deUris);

		JsonArray jsonArray = new JsonArray();
		for (FormResult formResult : formResults) {
			jsonArray.add(formResultToJson(formResult));
		}

		return jsonArray.toString();
	}

	@GET
	@Path("formsByDeNames")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchFormsByDeNames(@QueryParam("deNames") List<String> deNames)
			throws UnauthorizedException, UnsupportedEncodingException {

		Multimap<String, FormResult> deToFormsMap = searchManager.searchFormsByDeNames(deNames);

		if (deToFormsMap.isEmpty()) {
			return Response.status(Status.NO_CONTENT).build();
		}

		JsonArray jsonResponse = new JsonArray();

		for (String deName : deToFormsMap.keySet()) {
			Collection<FormResult> fsList = deToFormsMap.get(deName);
			JsonObject jsonElement = new JsonObject();
			JsonArray formJsonArray = new JsonArray();
			for (FormResult currentForm : fsList) {
				if (currentForm != null) {
					formJsonArray.add(formResultToJson(currentForm));
				}
			}
			jsonElement.add(deName, formJsonArray);
			jsonResponse.add(jsonElement);
		}

		return Response.ok(jsonResponse.toString()).build();
	}

	/**
	 * Given a list of deNames, this method returns a json array of json properties where the key is the data element
	 * name as the value is the array of seeAlso associated with that data element.
	 * 
	 * @param deNames
	 * @return
	 */
	@GET
	@Path("seeAlso")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSeeAlso(@QueryParam("deNames") List<String> deNames) {
		Multimap<String, String> deToSeeAlsoMap = searchManager.getSeeAlso(deNames);

		if (deToSeeAlsoMap == null || deToSeeAlsoMap.isEmpty()) {
			return Response.status(Status.NO_CONTENT).build();
		}

		JsonArray jsonResponse = new JsonArray();

		for (String deName : deToSeeAlsoMap.keySet()) {
			Collection<String> seeAlsoList = deToSeeAlsoMap.get(deName);
			JsonObject jsonElement = new JsonObject();
			JsonArray seeAlsoJson = new JsonArray();
			for (String seeAlso : seeAlsoList) {
				seeAlsoJson.add(new JsonPrimitive(seeAlso));
			}
			jsonElement.add(deName, seeAlsoJson);
			jsonResponse.add(jsonElement);
		}

		return Response.ok(jsonResponse.toString()).build();
	}

	@GET
	@Path("knowledgeGraphInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getKnowledgeGraphInfo(@QueryParam("deNames") List<String> deNames) {
		Multimap<String, String> deToSeeAlsoMap = searchManager.getSeeAlso(deNames);
		Multimap<String, FormResult> deToFormsMap = searchManager.searchFormsByDeNames(deNames);
		Map<String, String> deToDeTitleMap = searchManager.getDeTitles(deNames);

		if (deToSeeAlsoMap == null || deToSeeAlsoMap.isEmpty()) {
			return Response.status(Status.NO_CONTENT).build();
		}

		JsonArray jsonResponse = new JsonArray();

		for (String deName : deNames) {
			JsonObject jsonElement = new JsonObject();
			jsonElement.addProperty("elementName", deName);

			String title = deToDeTitleMap.get(deName);
			if (title != null) {
				jsonElement.addProperty("elementTitle", deToDeTitleMap.get(deName));
			}

			jsonElement.addProperty("dictionaryLink",
					String.format(QueryToolConstants.DE_LINK_FROMAT, constants.getModulesDDTURL(), deName));


			Collection<String> seeAlsoList = deToSeeAlsoMap.get(deName);

			if (seeAlsoList != null) {
				JsonArray seeAlsoArray = new JsonArray();
				for (String seeAlso : seeAlsoList) {
					seeAlsoArray.add(new JsonPrimitive(seeAlso));
				}
				jsonElement.add("seeAlso", seeAlsoArray);
			}

			Collection<FormResult> fsList = deToFormsMap.get(deName);
			if (fsList != null) {
				JsonArray formJsonArray = new JsonArray();
				for (FormResult currentForm : fsList) {
					if (currentForm != null) {
						formJsonArray.add(formResultToJsonKnowledgeGraph(currentForm));
					}
				}
				jsonElement.add("formStructures", formJsonArray);
			}

			jsonResponse.add(jsonElement);
		}

		return Response.ok(jsonResponse.toString()).build();
	}

	private JsonObject studyResultToJson(StudyResult studyResult) {
		JsonObject studyJson = new JsonObject();

		studyJson.addProperty("title", studyResult.getTitle());
		studyJson.addProperty("shortName", studyResult.getTitle());
		studyJson.addProperty("uri", studyResult.getUri());
		studyJson.addProperty("pi", studyResult.getPi());
		studyJson.addProperty("studyId", studyResult.getId());

		int availFormCount = 0;
		StudyResultPermission srp = permissionModel.getStudyResultPermissions().get(studyResult.getUri());

		JsonArray formsJson = new JsonArray();
		for (FormResult form : studyResult.getForms()) {
			JsonObject formJson = new JsonObject();

			formJson.addProperty("uri", form.getUri());
			if (srp != null && srp.getFormIds() != null && srp.getFormIds().contains(form.getId())) {
				formJson.addProperty("isAvailable", "true");
				availFormCount++;
			} else {
				formJson.addProperty("isAvailable", "false");
			}

			formsJson.add(formJson);
		}

		studyJson.add("forms", formsJson);
		studyJson.addProperty("isAvailable", (availFormCount > 0));

		return studyJson;
	}

	private JsonObject formResultToJson(FormResult formResult) {
		JsonObject formJson = new JsonObject();

		formJson.addProperty("title", formResult.getTitle());
		formJson.addProperty("shortName", formResult.getShortName());
		formJson.addProperty("version", formResult.getVersion());
		formJson.addProperty("uri", formResult.getUri());
		formJson.addProperty("dictionaryLink", String.format(QueryToolConstants.FS_LINK_FORMAT,
				constants.getModulesDDTURL(), formResult.getShortName()));

		JsonArray studiesJson = new JsonArray();
		for (StudyResult study : formResult.getStudies()) {
			studiesJson.add(new JsonPrimitive(study.getUri()));
		}

		formJson.add("studiesMapped", studiesJson);

		Map<String, FormResultPermission> frps = permissionModel.getFormResultPermissions();
		formJson.addProperty("isAvailable", frps.containsKey(formResult.getShortName()));

		return formJson;
	}


	private JsonObject formResultToJsonKnowledgeGraph(FormResult formResult) {
		JsonObject formJson = new JsonObject();

		formJson.addProperty("title", formResult.getTitle());
		formJson.addProperty("shortName", formResult.getShortName());
		formJson.addProperty("version", formResult.getVersion());
		formJson.addProperty("dictionaryLink", String.format(QueryToolConstants.FS_LINK_FORMAT,
				constants.getModulesDDTURL(), formResult.getShortName()));

		return formJson;
	}

}
