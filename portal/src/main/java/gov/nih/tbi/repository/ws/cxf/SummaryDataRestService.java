package gov.nih.tbi.repository.ws.cxf;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.common.util.ProformsWsProvider;
import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.AdvancedVisualizationManager;
import gov.nih.tbi.commons.service.QueryToolManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.query.model.SummaryResult;
import gov.nih.tbi.repository.model.StudySubmittedForm;
import gov.nih.tbi.repository.model.StudySubmittedFormCache;
import gov.nih.tbi.repository.model.SummaryDataCache;
import gov.nih.tbi.repository.model.hibernate.BasicStudySearch;

public class SummaryDataRestService extends AbstractRestService {
	private static Logger logger = Logger.getLogger(SummaryDataRestService.class);

	@Autowired
	protected AdvancedVisualizationManager advancedVisualizationManager;

	@Autowired
	protected RepositoryManager repositoryManager;

	@Autowired
	private AccountManager accountManager;

	@Autowired
	protected SessionAccount sessionAccount;

	@Autowired
	protected ModulesConstants modulesConstants;

	@Resource
	private WebServiceContext wsContext;

	@Autowired
	private QueryToolManager queryToolManager;
	
	@Autowired
	private ProformsWsProvider proformsWsProvider;

	UriInfo uriInfo;

	

	@GET
	@Path("full")
	@Produces(MediaType.APPLICATION_JSON)
	public void getSummaryData() throws UnsupportedEncodingException {
		logger.debug("Webservice getSummaryData");
		advancedVisualizationManager.buildSummaryData();
	}

	@GET
	@Path("program")
	@Produces(MediaType.APPLICATION_JSON)
	public String getProgramDataChart(@QueryParam("chartName") List<String> chartNames)
			throws JsonParseException, WebApplicationException, IOException {
		logger.debug("Webservice getProgramDataCart");
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager, proformsWsProvider, modulesConstants);
		JsonObject jsonOutput = sdCache.getProgramMultiple(chartNames);

		jsonOutput.addProperty("dateUpdated", BRICSTimeDateUtil.formatDate(sdCache.getUpdateDate()));

		return jsonOutput.toString();
	}
	
	@GET
	@Path("getByDate/{tableName}/{selectedDate}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDataByDate(@PathParam("tableName") String tableName,
			@PathParam("selectedDate") String selectedDate) {
		logger.debug("REST WS CALL: getSubmittedDataByDaterange ( " + selectedDate + " ) ");

		if (!modulesConstants.getIsRepositoryPublic()) {
			String msg = "PUBLIC REST WS CALL: getSubmittedDataByDaterange. The service is forbidden on this instance.";
			logger.error(msg + "Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}

		if (selectedDate != null) {

			String orgName = modulesConstants.getModulesOrgName();
			JsonArray dataJson = new JsonArray();

			SimpleDateFormat formatter = new SimpleDateFormat(ServiceConstants.DEFAULT_FORMAT_DATE);
			try {
				Date sdate = formatter.parse(selectedDate);
				String newJsonFormatDate = modulesConstants.getSummaryNewJsonDate();
				Date changedFormatDate = formatter.parse(newJsonFormatDate);
				Boolean useNewJsonFormat = false;

				if (sdate.after(changedFormatDate)) {
					useNewJsonFormat = true;
				}
				String submittedDateFileName = orgName + "_" + formatter.format(sdate) + "_studyMetrics.json";
				String filePath = ServiceConstants.TBI_PUBLIC_SITE_FILE_PATH;
				if (useNewJsonFormat) {
					submittedDateFileName = orgName + "_" + formatter.format(sdate) + ".json";
					filePath = ServiceConstants.ADVANCED_VIEWER_UPLOAD_PATH;
				}

				try {
					InputStream fis = repositoryManager.getSftpInputStream(
							ServiceConstants.TBI_DRUPAL_DATAFILE_ENDPOINT_ID, filePath, submittedDateFileName);
					String jsonStr = "";
					try {
						jsonStr = IOUtils.toString(fis, "utf-8");
					} finally {
						fis.close();
					}

					JsonParser jsonParser = new JsonParser();
					JsonArray selectedDateJsonArr = new JsonArray();
					if (tableName.equalsIgnoreCase("submittedData")) {
						if (useNewJsonFormat) {
							selectedDateJsonArr = jsonParser.parse(jsonStr).getAsJsonObject().get("studies")
									.getAsJsonArray();
						} else {
							selectedDateJsonArr = jsonParser.parse(jsonStr).getAsJsonObject().get("studySubmittedData")
									.getAsJsonArray();
						}
					} else if (tableName.equalsIgnoreCase("commonlyUsedFS")) {
						selectedDateJsonArr = jsonParser.parse(jsonStr).getAsJsonObject().get("commonlyUsedFs")
								.getAsJsonArray();
					}

					dataJson.addAll(selectedDateJsonArr);

				} catch (JSchException | SftpException e1) {
					logger.error("PUBLIC REST WS CALL: getSubmittedDataByDaterange. Failed to get data from sftp file");
					e1.printStackTrace();
				} catch (IOException e2) {
					logger.error(
							"PUBLIC REST WS CALL: getSubmittedDataByDaterange. Failed to convert inputStream from sftp file into string");
					e2.printStackTrace();
				}

			} catch (ParseException e) {
				logger.error("PUBLIC REST WS CALL: getSubmittedDataByDaterange. Failed to parse string to date");
				e.printStackTrace();
			}

			return dataJson.toString();

		} else {
			String msg = "PUBLIC REST WS CALL: getSubmittedDataByDaterange. Didn't complete because missing selected date range. selected date = "
					+ selectedDate;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
	}

	

	@GET
	@Path("sparql/program")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSparqlProgramDataChart(@QueryParam("chartName") List<String> chartNames)
			throws UnsupportedEncodingException {
		logger.debug("Webservice getProgramDataCart");
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager, proformsWsProvider, modulesConstants);
		JsonObject jsonOutput = sdCache.getSparqlProgramMultiple(chartNames);

		jsonOutput.addProperty("dateUpdated", BRICSTimeDateUtil.formatDate(sdCache.getUpdateDate()));

		return jsonOutput.toString();
	}

	@GET
	@Path("sparql/questionNumber/{number}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSparqlQuestionNumberResponse(@PathParam("number") Long number,
			@QueryParam("questionName") String questionName) throws UnsupportedEncodingException {
		logger.debug("Webservice sparql question number");
		SummaryResult responseResult = queryToolManager.getSparqlSummaryData(questionName, number);
		// NOTE: if this returns null should we return an empty json object?
		LinkedHashMap<String, String> singleResponse = (LinkedHashMap<String, String>) responseResult.getResults();
		JsonArray singleResponseJsonArray = new JsonArray();
		if (singleResponse.isEmpty() && !responseResult.getJsonResults().isEmpty()) {
			JsonParser parser = new JsonParser();
			JsonElement Element = parser.parse(responseResult.getJsonResults());
			singleResponseJsonArray = Element.getAsJsonArray();
		}
		JsonObject dataJson = new JsonObject();
		dataJson.add("response", singleResponseJsonArray);
		return dataJson.toString();
	}

	@GET
	@Path("sparql/questionText/{text}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSparqlQuestionTextResponse(@PathParam("text") String text,
			@QueryParam("questionName") String questionName) throws UnsupportedEncodingException {
		logger.debug("Webservice sparql question text");
		SummaryResult responseResult = queryToolManager.getSparqlSummaryData(questionName, text);
		// NOTE: if this returns null should we return an empty json object?
		LinkedHashMap<String, String> singleResponse = (LinkedHashMap<String, String>) responseResult.getResults();
		JsonArray singleResponseJsonArray = new JsonArray();
		if (singleResponse.isEmpty() && !responseResult.getJsonResults().isEmpty()) {
			JsonParser parser = new JsonParser();
			JsonElement Element = parser.parse(responseResult.getJsonResults());
			singleResponseJsonArray = Element.getAsJsonArray();
		}
		JsonObject dataJson = new JsonObject();
		dataJson.add("response", singleResponseJsonArray);
		return dataJson.toString();
	}

	@GET
	@Path("study/{studyId}")
	@Produces("application/json")
	public String getStudyDataChart(@PathParam("studyId") String studyId,
			@QueryParam("chartName") List<String> chartNames) throws UnsupportedEncodingException {
		studyId = URLDecoder.decode(studyId, "UTF-8");
		logger.debug("Webservice getStudyDataChart with studyId: " + studyId);
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager, proformsWsProvider, modulesConstants);

		JsonObject jsonOutput = new JsonObject();
		if (StringUtils.isNumeric(studyId)) {
			Long sId = Long.decode(studyId);
			jsonOutput = sdCache.getStudyMultiple(chartNames, sId);
		} else {
			jsonOutput = sdCache.getStudyMultiple(chartNames, studyId);
		}

		jsonOutput.addProperty("dateUpdated", BRICSTimeDateUtil.formatDate(sdCache.getUpdateDate()));

		return jsonOutput.toString();
	}

	@GET
	@Path("sparql/study/{studyId}")
	@Produces("application/json")
	public String getSparqlStudyDataChart(@PathParam("studyId") String studyId,
			@QueryParam("chartName") List<String> chartNames) throws UnsupportedEncodingException {
		studyId = URLDecoder.decode(studyId, "UTF-8");
		logger.debug("Webservice getStudyDataChart with studyId: " + studyId);
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager, proformsWsProvider, modulesConstants);

		JsonObject jsonOutput = new JsonObject();
		if (StringUtils.isNumeric(studyId)) {
			Long sId = Long.decode(studyId);
			jsonOutput = sdCache.getSparqlStudyMultiple(chartNames, sId);
		} else {
			jsonOutput = sdCache.getSparqlStudyMultiple(chartNames, studyId);
		}

		jsonOutput.addProperty("dateUpdated", BRICSTimeDateUtil.formatDate(sdCache.getUpdateDate()));

		return jsonOutput.toString();
	}

	@DELETE
	@Path("resetCache")
	public Response resetCache() throws UnsupportedEncodingException {
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager, proformsWsProvider, modulesConstants);
		try {
			sdCache.resetCache(queryToolManager, proformsWsProvider, modulesConstants);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new InternalServerErrorException(e);
		}

		logger.info("SummaryData cache cleared.");
		return Response.ok().build();
	}

	@GET
	@Path("countSummary/{studyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCountSummaryData(@PathParam("studyId") String studyId) throws UnsupportedEncodingException {
		logger.debug("Webservice getCountSummaryData with study ID " + studyId);
		JsonObject jsonOutput = new JsonObject();
		StudySubmittedFormCache studySubmittedFormCache = StudySubmittedFormCache.getInstance(repositoryManager,
				queryToolManager);
		Integer totalNumRec = studySubmittedFormCache.getRowCountByStudy(studyId);
		jsonOutput.addProperty("totalRecords", String.valueOf(totalNumRec));

		Integer totalNumForm = studySubmittedFormCache.getFormCountByStudy(studyId);
		jsonOutput.addProperty("totalFormWithData", String.valueOf(totalNumForm));

		String lastSubmitDateStr = studySubmittedFormCache.getLastSubmitDateByStudy(studyId);
		String lastSubmitDateFormated = "";
		try {
			if (lastSubmitDateStr != null && !lastSubmitDateStr.equals("")) {
				Date lastSubmitDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(lastSubmitDateStr);
				lastSubmitDateFormated = new SimpleDateFormat(ServiceConstants.DEFAULT_FORMAT_DATE)
						.format(lastSubmitDate);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jsonOutput.addProperty("lastSubmitDate", lastSubmitDateFormated);

		return jsonOutput.toString();
	}

	@GET
	@Path("studySubmittedData")
	@Produces(MediaType.APPLICATION_JSON)
	public String submittedDataSearch() {
		logger.debug("PUBLIC REST WS CALL: studySubmittedData");

		JsonObject jsonOutput = new JsonObject();

		StudySubmittedFormCache studySubmittedFormCache = StudySubmittedFormCache.getInstance(repositoryManager,
				queryToolManager);
		JsonArray commonlyUsedFsJson = studySubmittedFormCache.getAllPublicSubmittedFormsJson();
		System.out.println("Complete get all public submitted forms.");
		JsonArray studySubmittedDataJson = repositoryManager.getPublicSiteSubmittedData(studySubmittedFormCache);
		System.out.println("Complete get public site submitted data.");

		Collection<StudySubmittedForm> studyCollection = queryToolManager.getAllStudySubmittedForms().values();

		List<StudySubmittedForm> studyLists = new ArrayList<StudySubmittedForm>(studyCollection);
		String totalNumberOfRecords = studySubmittedFormCache.getTotalRecordCount(studyLists);

		List<BasicStudySearch> basicStudyList = repositoryManager.getPublicSiteSearchBasicStudies();
		String totalNumberofSharedStudies = studySubmittedFormCache.getSharedStudiesCount(basicStudyList);

		jsonOutput.add("totalNumberOfRecords", new JsonPrimitive(totalNumberOfRecords));
		jsonOutput.add("totalNumberOfSharedStudies", new JsonPrimitive(totalNumberofSharedStudies));
		jsonOutput.add("commonlyUsedFs", commonlyUsedFsJson);
		jsonOutput.add("studySubmittedData", studySubmittedDataJson);

		return jsonOutput.toString();
	}

	

	@GET
	@Path("getTotalNumbersByDate/{selectedDate}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTotalNumbersByDate(@PathParam("selectedDate") String selectedDate) {
		logger.debug("REST WS CALL: getTotalNumbersByDaterange ( " + selectedDate + " ) ");

		if (!modulesConstants.getIsRepositoryPublic()) {
			String msg = "PUBLIC REST WS CALL: getTotalNumbersByDaterange. The service is forbidden on this instance.";
			logger.error(msg + "Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}

		if (selectedDate != null) {
			String orgName = modulesConstants.getModulesOrgName();
			JsonObject dataJson = new JsonObject();

			SimpleDateFormat formatter = new SimpleDateFormat(ServiceConstants.DEFAULT_FORMAT_DATE);

			try {
				
				Date sdate = formatter.parse(selectedDate);
				String newJsonFormatDate = modulesConstants.getSummaryNewJsonDate();
				Date changedFormatDate = formatter.parse(newJsonFormatDate);
				Boolean useNewJsonFormat = false;
				

				if (sdate.after(changedFormatDate)) {
					useNewJsonFormat = true;
				}
				String submittedDateFileName = orgName + "_" + formatter.format(sdate) + "_studyMetrics.json";
				String filePath = ServiceConstants.TBI_PUBLIC_SITE_FILE_PATH;
				if (useNewJsonFormat) {
					submittedDateFileName = orgName + "_" + formatter.format(sdate) + ".json";
					filePath = ServiceConstants.ADVANCED_VIEWER_UPLOAD_PATH;
				}

				try {
					InputStream fis = repositoryManager.getSftpInputStream(
							ServiceConstants.TBI_DRUPAL_DATAFILE_ENDPOINT_ID, filePath, submittedDateFileName);
					String jsonStr = "";
					try {
						jsonStr = IOUtils.toString(fis, "utf-8");
					} finally {
						fis.close();
					}

					JsonParser jsonParser = new JsonParser();
					dataJson.add("totalNumberOfStudies", (useNewJsonFormat) ? jsonParser.parse(jsonStr).getAsJsonObject().get("totalNumberOfStudies") : new JsonPrimitive("N/A"));
					dataJson.add("totalNumberOfRecords", jsonParser.parse(jsonStr).getAsJsonObject().get("totalNumberOfRecords"));
					dataJson.add("totalNumberOfSharedStudies", jsonParser.parse(jsonStr).getAsJsonObject().get("totalNumberOfSharedStudies"));
					dataJson.add("totalNumberOfSharedRecords", (useNewJsonFormat) ? jsonParser.parse(jsonStr).getAsJsonObject().get("totalNumberOfSharedRecords") : new JsonPrimitive("N/A"));

				} catch (JSchException | SftpException e1) {
					logger.error("PUBLIC REST WS CALL: getTotalNumbersByDaterange. Failed to get data from sftp file");
					e1.printStackTrace();
				} catch (IOException e2) {
					logger.error(
							"PUBLIC REST WS CALL: getTotalNumbersByDaterange. Failed to convert inputStream from sftp file into string");
					e2.printStackTrace();
				}

			} catch (ParseException e) {
				logger.error("PUBLIC REST WS CALL: getTotalNumbersByDaterange. Failed to parse string to date");
				e.printStackTrace();
			}

			return dataJson.toString();

		} else {
			String msg = "PUBLIC REST WS CALL: getTotalNumbersByDaterange. Didn't complete because missing selected date range. selected date = "
					+ selectedDate;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
	}

}
