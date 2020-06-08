package gov.nih.tbi.ws.cxf;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
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

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.exceptions.FilterParseException;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.DataTableColumnWithUri;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.DataCartManager;
import gov.nih.tbi.service.model.DataCart;
import gov.nih.tbi.service.model.MetaDataCache;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.DataCartUtil;
import gov.nih.tbi.util.InstanceSpecificQueryModifier;

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

	@Autowired
	InstanceSpecificQueryModifier instanceSpecificQueryModifier;

	@Autowired
	MetaDataCache metaDataCache;

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
	public Response addToCart(@FormParam("formUri") String formUri, @FormParam("studyUri") String studyUri) {
		getAuthenticatedAccount();
		if (formUri == null || studyUri == null) {
			throw new BadRequestException("Invalid parameters passed in addToCart.");
		}

		FormResult formToAdd = metaDataCache.getFormResult(formUri);
		StudyResult studyToAdd = metaDataCache.getStudyResult(studyUri);

		if (formToAdd == null || studyToAdd == null) {
			throw new NotFoundException("No corresponding formResult and studyResult found in the cache.");
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
	public Response removeFromCart(@FormParam("formUri") String formUri, @FormParam("studyUri") String studyUri) {
		getAuthenticatedAccount();
		if (formUri == null || studyUri == null) {
			String msg = "Invalid parameters passed in removeFromCart: either formUri: " + formUri + " or studyUri: "
					+ studyUri + " is null";
			throw new BadRequestException(msg);
		}

		FormResult formToRemove = metaDataCache.getFormResult(formUri);
		StudyResult studyToRemove = metaDataCache.getStudyResult(studyUri);

		if (formToRemove == null || studyToRemove == null) {
			String msg = "No corresponding formResult or studyResult found in the cache.";
			throw new NotFoundException(msg);
		}

		dataCartManager.removeFromCart(dataCart, formToRemove, studyToRemove);

		log.info("Removed from cart - form: " + formToRemove.getShortName() + ", study: " + studyToRemove.getTitle());
		return respondEmptyOk();
	}

	@POST
	@Path("study/removeAll")
	@Produces(MediaType.TEXT_XML)
	public Response studyRemoveAll(@FormParam("studyUri") String studyUri) {
		getAuthenticatedAccount();
		if (studyUri == null) {
			String msg = "Invalid parameters passed in removeFromCart: studyUri: " + studyUri + " is null";
			throw new BadRequestException(msg);
		}

		StudyResult studyToRemove = metaDataCache.getStudyResult(studyUri);

		if (studyToRemove == null) {
			String msg = "No corresponding formResult or studyResult found in the cache.";
			throw new NotFoundException(msg);
		}

		dataCartManager.removeStudyFromCart(dataCart, studyToRemove);

		log.info("Removed from cart - study: " + studyToRemove.getTitle());
		return respondEmptyOk();
	}

	@POST
	@Path("form/removeAll")
	@Produces(MediaType.TEXT_XML)
	public Response formRemoveAll(@FormParam("formUri") String formUri) {
		getAuthenticatedAccount();
		if (formUri == null) {
			String msg = "Invalid parameters passed in removeFromCart: formUri: " + formUri + " is null";
			throw new BadRequestException(msg);
		}

		FormResult formToRemove = metaDataCache.getFormResult(formUri);

		if (formToRemove == null) {
			String msg = "No corresponding formResult or formResult found in the cache.";
			throw new NotFoundException(msg);
		}

		dataCartManager.removeFormFromCart(dataCart, formToRemove);

		log.info("Removed from cart - form: " + formToRemove.getShortName());
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
	public Response clearDataCart() {
		getAuthenticatedAccount();
		dataCart.reset();
		log.info("Data cart cleared.");

		return respondEmptyOk();
	}


	@GET
	@Path("form/permission")
	@Produces(MediaType.TEXT_XML)
	public boolean checkFormPermission(@QueryParam("formUri") String formUri) {
		getAuthenticatedAccount();
		if (ValUtil.isBlank(formUri)) {
			log.error("Invalid parameters passed in checkFormPermission.");
			return false;
		}

		FormResult form = metaDataCache.getFormResult(formUri);
		if (form == null) {
			log.error("No corresponding form found in the cache for " + formUri);
			return false;
		}

		long formId = form.getId();
		return (permissionModel.getFormResultPermissions() != null
				&& permissionModel.getFormResultPermissions().containsKey(Long.valueOf(formId).toString()));
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
	@POST
	@Path("runQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runQuery(@FormParam("formUris") List<String> formUris, @FormParam("offset") int offset,
			@FormParam("limit") int limit, @FormParam("sortColName") String sortColName,
			@FormParam("sortOrder") String sortOrder, @FormParam("filters") String filters,
			@FormParam("filterExpression") String filterExpression, @FormParam("doApplyFilter") Boolean doApplyFilter) {

		getAuthenticatedAccount();

		JsonObject resultJson = new JsonObject();

		if (formUris == null || formUris.isEmpty()) {
			throw new BadRequestException("Cannot run query without formUris!");
		}

		if (doApplyFilter == null) {
			throw new BadRequestException("Missing doApplyFilter");
		}

		synchronized (dataCart) {
			log.info("Run query for " + formUris.size() + " form(s).");

			dataCart.removeAllFilters();

			dataCart.clearSelectedForms();
			dataCart.setSelectedFormUris(formUris);

			List<FormResult> selectedForms = dataCartManager.loadSelectedFormDataElements(dataCart);
			dataCartManager.selectAllDataEements(dataCart);

			try {
				addFiltersToCart(filters);
			} catch (JsonSyntaxException e) {
				throw new BadRequestException("Invalid parameters passed in addToCart", e);
			} catch (FilterParseException e) {
				throw new BadRequestException("Invalid JSON data.  Probably missing some required properties.", e);
			}

			// insert info for whether or not the form has a guid column
			if (dataCart.getSelectedForms().size() > 1) {
				boolean showGuidDialog = false;
				JsonArray formInfo = new JsonArray();

				for (FormResult form : dataCart.getSelectedForms()) {
					JsonObject formJson = new JsonObject();
					formJson.addProperty("name", form.getShortName());
					boolean hasGuidColumn = false;
					for (RepeatableGroup rg : form.getRepeatableGroups()) {
						for (DataElement de : rg.getDataElements()) {
							if (DataType.GUID == de.getType()) {
								hasGuidColumn = true;
								break;
							}
						}
					}

					if (!hasGuidColumn) {
						showGuidDialog = true;
					}

					formJson.addProperty("hasGuidColumn", hasGuidColumn);
					formInfo.add(formJson);
				}


				if (showGuidDialog) {
					resultJson.add("formInfo", formInfo);
					return Response.ok(resultJson.toString()).build();
				}
			}

			if (selectedForms.isEmpty()) {
				return Response.status(Status.NO_CONTENT).build();
			}

			// if we are currently in PDBP
			if (QueryToolConstants.PDBP_ORG_NAME.equals(constants.getOrgName())) {
				boolean hasHighlightedGuid =
						dataCartManager.hasHighlightedGuid(selectedForms, permissionModel.getUserName());
				resultJson.addProperty("hasHighlightedGuid", hasHighlightedGuid);
			}

			dataCartManager.rebuildCodeMapping(dataCart, selectedForms);

			try {
				dataCartManager.generateInstancedDataTable(dataCart, offset, limit, sortColName, sortOrder,
						permissionModel.getUserName(), doApplyFilter, filterExpression);
			} catch (FilterEvaluatorException e) {
				throw new InternalServerErrorException(e);
			}

			instanceSpecificQueryModifier.modifyDataCart(dataCart);

			resultJson.addProperty("header", dataCartManager.getTableHeaderJson(dataCart));
			resultJson.addProperty("data", getTableDataJson());

			// insert info for whether or not the form has a guid column
			if (dataCart.getSelectedForms().size() > 1) {
				JsonArray formInfo = new JsonArray();

				for (FormResult form : dataCart.getSelectedForms()) {
					JsonObject formJson = new JsonObject();
					formJson.addProperty("name", form.getShortName());
					formJson.addProperty("hasGuidData", form.isHasGuidData());
					formInfo.add(formJson);
				}

				resultJson.add("formInfo", formInfo);
			}
		}

		return Response.ok(resultJson.toString()).build();
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
	public Response getDataWithPagination(@QueryParam("offset") int offset, @QueryParam("limit") int limit,
			@QueryParam("sortColName") String sortColName, @QueryParam("sortOrder") String sortOrder,
			@QueryParam("booleanExpression") String booleanExpression) {

		getAuthenticatedAccount();

		String displayOption = null;
		if (dataCart.getInstancedDataTable() != null) {
			displayOption = dataCart.getInstancedDataTable().getDisplayOption();
		}

		instanceSpecificQueryModifier.modifyDataCart(dataCart);

		try {
			dataCartManager.rebuildDataTableData(dataCart, offset, limit, sortColName, sortOrder,
					permissionModel.getUserName(), booleanExpression);
		} catch (FilterEvaluatorException e) {
			throw new InternalServerErrorException("Error occured while evaluating the filters", e);
		}

		dataCart.setDisplayOption(displayOption);

		return Response.ok(getTableDataJson()).build();
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
	public String changeDisplayOption(@QueryParam("displayOption") String displayOption) {
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
			@QueryParam("rgName") String rgName) {
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
			@QueryParam("rgFormUri") String rgFormUri, @QueryParam("rgName") String rgName) {
		getAuthenticatedAccount();
		dataCartManager.collapseRepeatableGroup(dataCart, rowUri, rgFormUri, rgName);
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
	public String getSelectedFormDetails() {
		getAuthenticatedAccount();

		synchronized (dataCart) {
			log.info("Retrieving data element details for all selected forms ... - " + dataCart.getSelectedFormUris());
			dataCartManager.loadSelectedFormDataElements(dataCart);
			return dataCartManager.getSelectedFormDetailsJson(dataCart);
		}
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
			@QueryParam("deName") String deName) {
		getAuthenticatedAccount();
		log.info("Loading the filter details for element: " + formUri + " " + rgName + " " + deName);

		DataElement de = DataCartUtil.getDataElement(dataCart, formUri, rgName, deName);
		if (de != null) {
			return de.toJsonDetails().toString();
		}

		return "Error getting filter details.";
	}

	private void addFiltersToCart(String filters) throws FilterParseException {
		// remove all filters so we can add them back
		dataCart.removeAllFilters();

		JsonParser parser = new JsonParser();
		// list of filter json objects
		JsonArray input = (JsonArray) parser.parse(filters);

		for (int i = 0; i < input.size(); i++) {
			JsonObject filterJson = (JsonObject) input.get(i);

			DataCartUtil.addFilterFromJson(dataCart, filterJson);
		}
	}

	/**
	 * Web service to be called when user clicks "Apply Filter" button. List of filters will be passed in as json array
	 * along with the pagination related parameters to refresh the data table.
	 * 
	 * Note: sortColName needs to be in the format as: "formNameAndVersion,rgName,deName"
	 * 
	 * @return JSON string of the data table.
	 * @throws FilterParseException
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@POST
	@Path("applyFiltersWithoutResults")
	public Response applyFilterWithoutResults(@FormParam("filters") String filters, @FormParam("offset") int offset,
			@FormParam("limit") int limit, @FormParam("sortColName") String sortColName,
			@FormParam("sortOrder") String sortOrder) throws FilterParseException {
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
	public Response applyFilters(@FormParam("filters") String filters, @FormParam("offset") int offset,
			@FormParam("limit") int limit, @FormParam("sortColName") String sortColName,
			@FormParam("sortOrder") String sortOrder, @FormParam("filterExpression") String filterExpression) {

		getAuthenticatedAccount();

		// remove all filters so we can add them back
		try {
			addFiltersToCart(filters);
		} catch (JsonSyntaxException e) {
			throw new BadRequestException("Invalid parameters passed in addToCart", e);
		} catch (FilterParseException e) {
			throw new BadRequestException("Invalid JSON data.  Probably missing some required properties.", e);
		}

		String displayOption = null;
		if (dataCart.getInstancedDataTable() != null) {
			displayOption = dataCart.getInstancedDataTable().getDisplayOption();
		}

		try {
			dataCartManager.generateInstancedDataTable(dataCart, offset, limit, sortColName, sortOrder,
					permissionModel.getUserName(), true, filterExpression);
		} catch (FilterEvaluatorException e) {
			throw new InternalServerErrorException("Error occured while evaluating the filters", e);
		}

		instanceSpecificQueryModifier.modifyDataCart(dataCart);

		dataCart.setDisplayOption(displayOption);

		return Response.ok(getTableDataJson()).build();
	}

	@POST
	@Path("element/deselect")
	public Response deselectDataElement(@FormParam("formUri") String formUri, @FormParam("rgName") String rgName,
			@FormParam("deName") String deName) {
		getAuthenticatedAccount();
		log.debug("deselecting the data element: " + formUri + " " + rgName + " " + deName);

		DataElement de = DataCartUtil.getDataElement(dataCart, formUri, rgName, deName);
		de.setSelected(false);

		return respondEmptyOk();
	}

	@POST
	@Path("element/select")
	public Response selectDataElement(@FormParam("formUri") String formUri, @FormParam("rgName") String rgName,
			@FormParam("deName") String deName) {
		getAuthenticatedAccount();

		if (log.isDebugEnabled()) {
			log.debug("selecting the data element: " + formUri + " " + rgName + " " + deName);
		}

		DataElement de = DataCartUtil.getDataElement(dataCart, formUri, rgName, deName);
		de.setSelected(true);

		return respondEmptyOk();
	}

	@GET
	@Path("reRunQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response reRunQuery() {
		getAuthenticatedAccount();

		JsonObject resultJson = new JsonObject();

		instanceSpecificQueryModifier.modifyDataCart(dataCart);

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


	/**
	 * 
	 */
	@GET
	@Path("setSelectedForms")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setSelectedForms(@QueryParam("formUris") List<String> formUris)
			throws UnauthorizedException, UnsupportedEncodingException {

		getAuthenticatedAccount();
		JsonObject resultJson = new JsonObject();

		if (formUris == null) {
			log.error("No form selected when calling runQuery.");
			resultJson.addProperty("error", "true");  // TODO
			throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).entity(resultJson)
					.type(MediaType.APPLICATION_JSON).build());
		}

		synchronized (dataCart) {
			dataCart.clearSelectedForms();
			if (!formUris.isEmpty()) {
				dataCart.setSelectedFormUris(formUris);
			}
			resultJson.addProperty("complete", "true");
			log.info("Done selecting forms: " + formUris);
			return Response.ok(resultJson.toString(), MediaType.APPLICATION_JSON).build();
		}
	}

}
