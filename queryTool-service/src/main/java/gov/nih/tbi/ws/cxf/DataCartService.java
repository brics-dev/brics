package gov.nih.tbi.ws.cxf;

import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.exceptions.DataCartException;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.DataTableColumnWithUri;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.DataCartManager;
import gov.nih.tbi.service.model.DataCart;
import gov.nih.tbi.service.model.MetaDataCache;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.DataCartUtil;
import gov.nih.tbi.util.InstancedDataUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

@Path("/dataCart/")
public class DataCartService extends QueryBaseRestService {

	private static final Logger log = Logger.getLogger(DataCartService.class);

	@Autowired
	DataCart dataCart;

	@Autowired
	DataCartManager dataCartManager;

	@Autowired
	PermissionModel permissionModel;

	@Autowired
	ApplicationConstants constants;

	/**
	 * POST method to be called when adding a form to the data cart. Example Url:
	 * http://hostname:8080/query/service/dataCart/addToCart
	 * 
	 * @param formUri - uri of the selected form
	 * @param studyUri - uri of the study that the selected form is associated with.
	 * @return Response of OK status if the form is added successfully
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@POST
	@Path("form/add")
	@Produces(MediaType.TEXT_XML)
	public Response addToCart(@FormParam("formUri") String formUri, @FormParam("studyUri") String studyUri)
			throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		if (formUri == null || studyUri == null) {
			String msg = "Invalid parameters passed in addToCart.";
			log.error(msg);
			Response errResponse = Response.status(Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errResponse);
		}

		FormResult formToAdd = MetaDataCache.getFormResult(formUri);
		StudyResult studyToAdd = MetaDataCache.getStudyResult(studyUri);

		if (formToAdd == null || studyToAdd == null) {
			String msg = "No corresponding formResult and studyResult found in the cache.";
			log.error(msg);
			Response errResponse = Response.status(Status.NOT_FOUND).entity(msg).build();
			throw new NotFoundException(errResponse);
		}

		dataCartManager.addToCart(dataCart, formToAdd, studyToAdd);

		log.info("Added to cart - form: " + formToAdd.getShortName() + ", study: " + studyToAdd.getTitle());
		return respondEmptyOk();
	}

	/**
	 * POST method to be called when removing a form from the data cart. Example Url:
	 * http://hostname:8080/query/service/dataCart/form/remove
	 * 
	 * @param formUri - uri of the selected form
	 * @param studyUri - uri of the study that the selected form is associated with.
	 * @return Response of OK status if the form is removed successfully
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@POST
	@Path("form/remove")
	@Produces(MediaType.TEXT_XML)
	public Response removeFromCart(@FormParam("formUri") String formUri, @FormParam("studyUri") String studyUri)
			throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		if (formUri == null || studyUri == null) {
			String msg = "Invalid parameters passed in removeFromCart: either formUri: " + formUri + " or studyUri: "
					+ studyUri + " is null";
			log.error(msg);
			Response errResponse = Response.status(Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errResponse);
		}

		FormResult formToRemove = MetaDataCache.getFormResult(formUri);
		StudyResult studyToRemove = MetaDataCache.getStudyResult(studyUri);

		if (formToRemove == null || studyToRemove == null) {
			String msg = "No corresponding formResult or studyResult found in the cache.";
			log.error(msg);
			Response errResponse = Response.status(Status.NOT_FOUND).entity(msg).build();
			throw new NotFoundException(errResponse);
		}

		dataCartManager.removeFromCart(dataCart, formToRemove, studyToRemove);

		log.info("Removed from cart - form: " + formToRemove.getShortName() + ", study: " + studyToRemove.getTitle());
		return respondEmptyOk();
	}

	/**
	 * Web service method that clears the data cart in the server session.
	 * 
	 * @return Response of OK status
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@POST
	@Path("clear")
	public Response clearDataCart() throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		dataCart.reset();
		log.info("Data cart cleared.");

		return respondEmptyOk();
	}


	@GET
	@Path("form/permission")
	@Produces(MediaType.TEXT_XML)
	public boolean checkFormPermission(@QueryParam("formUri") String formUri)
			throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		if (ValUtil.isBlank(formUri)) {
			log.error("Invalid parameters passed in checkFormPermission.");
			return false;
		}

		FormResult form = MetaDataCache.getFormResult(formUri);
		if (form == null) {
			log.error("No corresponding form found in the cache for " + formUri);
			return false;
		}

		long formId = form.getId();
		return (permissionModel.getFormResultPermissions() != null
				&& permissionModel.getFormResultPermissions().containsKey(formId));
	}

	@GET
	@Path("columnsWithNoData")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getColumnsWithNoData() {
		Set<DataTableColumnWithUri> columnsWithNoData =
				dataCartManager.getColumnsWithNoData(dataCart, permissionModel.getUserName());

		// nothing to build so no content
		if (columnsWithNoData == null) {
			return Response.noContent().build();
		}

		JsonArray resultJson = new JsonArray();

		for (DataTableColumnWithUri columnWithNoData : columnsWithNoData) {
			JsonObject currentJsonObject = new JsonObject();
			resultJson.add(currentJsonObject);
			currentJsonObject.addProperty("formUri", columnWithNoData.getFormUri());
			currentJsonObject.addProperty("formName", columnWithNoData.getForm());
			currentJsonObject.addProperty("rgUri", columnWithNoData.getRepeatableGroupUri());
			currentJsonObject.addProperty("rgName", columnWithNoData.getRepeatableGroup());
			currentJsonObject.addProperty("deUri", columnWithNoData.getDataElementUri());
			currentJsonObject.addProperty("deName", columnWithNoData.getDataElement());
		}

		if (log.isDebugEnabled()) {
			log.debug("JSON RESPONSE: " + resultJson.toString());
		}

		return Response.ok(resultJson.toString(), MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Web service method to be called when user selects a single form or joins multiple forms. Example Url:
	 * http://hostname:8080/query/service/dataCart/runQuery?formUris=...&formUris=...
	 * 
	 * @param formUris - formUris of the selected forms
	 * @param sortColName - in the format of formNameAndVersion,repeatbableGroupName,dataElementName
	 * @return - json string of the serialized InstancedDataTable
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("runQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public String runQuery(@QueryParam("formUris") List<String> formUris, @QueryParam("offset") int offset,
			@QueryParam("limit") int limit, @QueryParam("sortColName") String sortColName,
			@QueryParam("sortOrder") String sortOrder) throws UnauthorizedException, UnsupportedEncodingException {

		getAuthenticatedAccount();
		JsonObject resultJson = new JsonObject();

		if (formUris == null || formUris.isEmpty()) {
			log.error("No form selected when calling runQuery.");
			resultJson.addProperty("error", "true");  // TODO
			return resultJson.toString();
		}

		log.info("Run query for " + formUris.size() + " form(s).");

		// we don't want to remove filters for joined query
		if (formUris.size() < 2) {
			dataCart.removeAllFilters();
		}

		dataCart.clearSelectedForms();
		dataCart.setSelectedFormUris(formUris);

		List<FormResult> selectedForms = dataCartManager.loadSelectedFormDataElements(dataCart);


		if (selectedForms.size() > 1) {
			// When joining forms and if any form doesn't have GUID element, returns error
			if (!InstancedDataUtil.formsHaveGuid(selectedForms)) {
				resultJson.addProperty("allHaveGuid", "false");
				return resultJson.toString();
			}
		}


		// if we are currently in PDBP
		if (QueryToolConstants.PDBP_ORG_NAME.equals(constants.getOrgName())) {
			boolean hasHighlightedGuid =
					dataCartManager.hasHighlightedGuid(selectedForms, permissionModel.getUserName());
			resultJson.addProperty("hasHighlightedGuid", hasHighlightedGuid);
		}

		dataCartManager.rebuildCodeMapping(dataCart, selectedForms);
		dataCartManager.generateInstancedDataTable(dataCart, offset, limit, sortColName, sortOrder,
				permissionModel.getUserName(), false);

		if (selectedForms.size() > 1 && !dataCartManager.hasMatchingGuid(dataCart)) {
			resultJson.addProperty("hasMatchingGuid", "false");
			return resultJson.toString();
		}

		resultJson.addProperty("header", dataCartManager.getTableHeaderJson(dataCart));
		resultJson.addProperty("data", getTableDataJson());

		return resultJson.toString();
	}


	private String getTableDataJson() {
		return dataCartManager.getTableDataJson(dataCart);
	}


	/**
	 * This web service method is for navigation through InstancedDataTable.
	 * 
	 * @param offset
	 * @param limit
	 * @param sortColName - in the format of formNameAndVersion,repeatbableGroupName,dataElementName
	 * @param sortOrder
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("dataWithPagination")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDataWithPagination(@QueryParam("offset") int offset, @QueryParam("limit") int limit,
			@QueryParam("sortColName") String sortColName, @QueryParam("sortOrder") String sortOrder)
			throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();

		String displayOption = null;
		if (dataCart.getInstancedDataTable() != null) {
			displayOption = dataCart.getInstancedDataTable().getDisplayOption();
		}

		dataCartManager.rebuildDataTableData(dataCart, offset, limit, sortColName, sortOrder,
				permissionModel.getUserName());
		dataCart.setDisplayOption(displayOption);

		return getTableDataJson();
	}

	/**
	 * When switching display option, data table was not rebuilt Example Url:
	 * http://hostname:8080/query/service/dataCart/changeDisplayOption?displayOption=CDISC
	 * 
	 * @param displayOption
	 * @return json string of the serialized InstancedDataTable
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("changeDisplayOption")
	@Produces(MediaType.APPLICATION_JSON)
	public String changeDisplayOption(@QueryParam("displayOption") String displayOption)
			throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		log.info("Switching data table display option to " + displayOption);

		dataCart.setDisplayOption(displayOption);
		if (dataCart.getSelectedFormUris().isEmpty()) {
			return new JsonObject().toString();
		} else {
			return getTableDataJson();
		}

	}


	/**
	 * Example Url:
	 * http://hostname:8080/query/service/dataCart/expandRepeatableGroup?rowUri=...&rgFormUri=...&rgName=...
	 * 
	 * @param displayOption
	 * @return json string of the serialized InstancedDataTable
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("expandRepeatableGroup")
	@Produces(MediaType.APPLICATION_JSON)
	public String expandRepeatableGroup(@QueryParam("rowUri") String rowUri, @QueryParam("rgFormUri") String rgFormUri,
			@QueryParam("rgName") String rgName) throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		log.info("Expanding repeatable group: " + rgName);

		dataCartManager.expandRepeatableGroup(dataCart, rowUri, rgFormUri, rgName, permissionModel.getUserName());
		return getTableDataJson();
	}

	/**
	 * Example Url:
	 * http://hostname:8080/query/service/dataCart/collapseRepeatableGroup?rowUri=...&rgFormUri=...&rgName=...
	 * 
	 * @param displayOption
	 * @return json string of the serialized InstancedDataTable
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("collapseRepeatableGroup")
	@Produces(MediaType.APPLICATION_JSON)
	public String collapseRepeatableGroup(@QueryParam("rowUri") String rowUri,
			@QueryParam("rgFormUri") String rgFormUri, @QueryParam("rgName") String rgName)
			throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		dataCartManager.collapseRepeatableGroup(dataCart, rowUri, rgFormUri, rgName);
		return getTableDataJson();
	}

	/**
	 * Example Url:
	 * http://hostname:8080/query/service/dataCart/refreshRepeatableGroup?rowUri=...&rgFormUri=...&rgName=...
	 * 
	 * @param displayOption
	 * @return json string of the serialized InstancedDataTable
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("refreshRepeatableGroup")
	@Produces(MediaType.APPLICATION_JSON)
	public String refreshRepeatableGroup(@QueryParam("rowUri") String rowUri, @QueryParam("rgFormUri") String rgFormUri,
			@QueryParam("rgName") String rgName) throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		dataCartManager.refreshRepeatableGroup(dataCart, rowUri, rgFormUri, rgName, permissionModel.getUserName());
		return getTableDataJson();
	}


	/**
	 * Example Url: http://hostname:8080/query/service/dataCart/selectedFormDetails
	 * 
	 * @return Json string of all data element filters of the selected form(s).
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("selectedFormDetails")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSelectedFormDetails() throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		log.info("Retrieving data element details for all selected forms ...");
		return dataCartManager.getSelectedFormDetailsJson(dataCart);
	}


	/**
	 * Web service method that loads the details for the given data element so that user can add a filter on it. Url:
	 * http://hostname:8080/query/service/dataCart/deFilterDetails?formUri=...&rgName=...&deName=...
	 * 
	 * @param formUri - uri of the form
	 * @param rgName - repeatable group name
	 * @param deName - data element shortname
	 * @return Json string of the data element details (please refer to DataElement.toJsonDetails())
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("deFilterDetails")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDeFilterDetails(@QueryParam("formUri") String formUri, @QueryParam("rgName") String rgName,
			@QueryParam("deName") String deName) throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		log.info("Loading the filter details for element: " + formUri + " " + rgName + " " + deName);

		try {
			DataElement de = DataCartUtil.getDataElement(dataCart, formUri, rgName, deName);
			if (de != null) {
				return de.toJsonDetails().toString();
			}

		} catch (DataCartException e) {
			log.error("Exception occured when getting filter details " + e.getMessage());
		}

		return "Error getting filter details.";
	}

	private void addFiltersToCart(String filters) {
		// remove all filters so we can add them back
		dataCart.removeAllFilters();

		JsonParser parser = new JsonParser();
		try {
			// list of filter json objects
			JsonArray input = (JsonArray) parser.parse(filters);

			for (int i = 0; i < input.size(); i++) {
				JsonObject filterJson = (JsonObject) input.get(i);
				DataCartUtil.addFilterFromJson(dataCart, filterJson);
			}
		} catch (JsonSyntaxException e) {
			throw e;
		} catch (DataCartException e) {
			throw e;
		}
	}

	/**
	 * Web service to be called when user clicks "Apply Filter" button. List of filters will be passed in as json array
	 * along with the pagination related parameters to refresh the data table.
	 * 
	 * Note: sortColName needs to be in the format as: "formNameAndVersion,rgName,deName"
	 * 
	 * @return JSON string of the data table.
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@POST
	@Path("applyFiltersWithoutResults")
	public Response applyFilterWithoutResults(@FormParam("filters") String filters, @FormParam("offset") int offset,
			@FormParam("limit") int limit, @FormParam("sortColName") String sortColName,
			@FormParam("sortOrder") String sortOrder) throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		addFiltersToCart(filters);
		return respondEmptyOk();
	}

	/**
	 * Web service to be called when user clicks "Apply Filter" button. List of filters will be passed in as json array
	 * along with the pagination related parameters to refresh the data table.
	 * 
	 * Note: sortColName needs to be in the format as: "formNameAndVersion,rgName,deName"
	 * 
	 * @return JSON string of the data table.
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@POST
	@Path("applyFilters")
	@Produces(MediaType.APPLICATION_JSON)
	public String applyFilters(@FormParam("filters") String filters, @FormParam("offset") int offset,
			@FormParam("limit") int limit, @FormParam("sortColName") String sortColName,
			@FormParam("sortOrder") String sortOrder) throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		// remove all filters so we can add them back
		try {
			addFiltersToCart(filters);
		} catch (JsonSyntaxException e) {
			log.error("Invalid parameters passed in addToCart.", e);
			return "bad json format";
		} catch (DataCartException e) {
			log.error("Exception occured when adding filter ", e);
			return "Exception occured when adding filter " + e.getMessage();
		}

		String displayOption = null;
		if (dataCart.getInstancedDataTable() != null) {
			displayOption = dataCart.getInstancedDataTable().getDisplayOption();
		}

		dataCartManager.generateInstancedDataTable(dataCart, offset, limit, sortColName, sortOrder,
				permissionModel.getUserName(), true);
		dataCart.setDisplayOption(displayOption);

		return getTableDataJson();
	}

	@POST
	@Path("element/deselect")
	public Response deselectDataElement(@FormParam("formUri") String formUri, @FormParam("rgName") String rgName,
			@FormParam("deName") String deName) throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		log.info("deselecting the data element: " + formUri + " " + rgName + " " + deName);

		try {
			DataElement de = DataCartUtil.getDataElement(dataCart, formUri, rgName, deName);
			de.setSelected(false);

		} catch (DataCartException e) {
			log.error("Exception occured when deselecting data element " + e.getMessage());
		}

		return respondEmptyOk();
	}

	@POST
	@Path("element/select")
	public Response selectDataElement(@FormParam("formUri") String formUri, @FormParam("rgName") String rgName,
			@FormParam("deName") String deName) throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();

		if (log.isDebugEnabled()) {
			log.debug("selecting the data element: " + formUri + " " + rgName + " " + deName);
		}

		try {
			DataElement de = DataCartUtil.getDataElement(dataCart, formUri, rgName, deName);
			de.setSelected(true);

		} catch (DataCartException e) {
			e.printStackTrace();
			log.error("Exception occured when selecting data element " + e.getMessage());
		}

		return respondEmptyOk();
	}

	@GET
	@Path("reRunQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response reRunQuery() throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		JsonObject resultJson = new JsonObject();

		resultJson.addProperty("header", dataCartManager.getTableHeaderJson(dataCart));
		resultJson.addProperty("data", getTableDataJson());

		return Response.ok(resultJson.toString(), MediaType.APPLICATION_JSON).build();
	}


	@GET
	@Path("schemas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSchemaOptions() {
		if (dataCart.getFormsInCart().isEmpty()) {
			return Response.ok(new JsonArray().toString(), MediaType.APPLICATION_JSON).build();
		}

		List<String> formNames = new ArrayList<String>();

		for (FormResult form : dataCart.getSelectedForms()) {
			formNames.add(form.getShortName());
		}

		List<Schema> schemas = dataCartManager.getSchemaOptionsByFormNames(formNames);
		JsonArray jsonArray = new JsonArray();

		for (Schema schema : schemas) {
			JsonPrimitive currentSchema = new JsonPrimitive(schema.getName());
			if (!"NINDS".equals(schema.getName())) { // PS-3429 (Remove NINDS option from PV drop down)
				jsonArray.add(currentSchema);
			}
		}

		return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).build();
	}


	@GET
	@Path("deDetailPage")
	@Produces(MediaType.TEXT_HTML)
	public String getDeDetailPage(@QueryParam("deName") String deName) {

		String deDetailPage = dataCartManager.getDeDetailPage(deName);
		return deDetailPage;
	}

	// @GET
	// @Path("executeRScript")
	// public byte[] executeRScript() throws CSVGenerationException {
	//
	// InstancedDataTable instancedDataTable = dataCart.getInstancedDataTable();
	// // byte[] dataBytes = CSVGenerator.generateCSV(instancedDataTable);
	//
	// // Use for flattened csv format
	// byte[] dataBytes = CSVGenerator.generateFlattenedCSV(instancedDataTable);
	//
	// return dataBytes;
	// /*
	// * //Populate jaxb request object with script and data Strings RboxRequest request = new RboxRequest();
	// * request.setScript(scriptString); request.setDataBytes(new String(dataBytes));
	// *
	// * System.out.println("inside service call with script: " + scriptString + " and data: " + new
	// * String(dataBytes));
	// *
	// * //Execute client post request and retrieve jaxb response object from server WebClient client =
	// * WebClient.create("http://localhost:8082/rbox/v1/script/process"); RboxResponse response =
	// * client.post(request, RboxResponse.class);
	// *
	// * return Response.ok(response).build(); // InstancedDataTable dataTable = dataCartManager.get //
	// * dataCartManager.generateInstancedDataTable(dataCart, 0, 20, "", "asc", permissionModel.getUserName());
	// */
	// }
}
