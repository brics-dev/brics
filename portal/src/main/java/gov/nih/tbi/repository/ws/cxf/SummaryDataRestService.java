package gov.nih.tbi.repository.ws.cxf;

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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.QueryToolManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.query.model.SummaryResult;
import gov.nih.tbi.repository.model.StudySubmittedForm;
import gov.nih.tbi.repository.model.StudySubmittedFormCache;
import gov.nih.tbi.repository.model.SummaryDataCache;
import gov.nih.tbi.repository.model.hibernate.BasicStudySearch;

public class SummaryDataRestService extends AbstractRestService {
	private static Logger logger = Logger.getLogger(SummaryDataRestService.class);

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

	UriInfo uriInfo;

	@GET
	@Path("program")
	@Produces(MediaType.APPLICATION_JSON)
	public String getProgramDataChart(@QueryParam("chartName") List<String> chartNames)
			throws UnsupportedEncodingException {
		logger.debug("Webservice getProgramDataCart");
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager);
		JsonObject jsonOutput = sdCache.getProgramMultiple(chartNames);

		jsonOutput.addProperty("dateUpdated", BRICSTimeDateUtil.formatDate(sdCache.getUpdateDate()));

		return jsonOutput.toString();
	}
	
	@GET
	@Path("sparql/program")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSparqlProgramDataChart(@QueryParam("chartName") List<String> chartNames)
			throws UnsupportedEncodingException {
		logger.debug("Webservice getProgramDataCart");
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager);
		JsonObject jsonOutput = sdCache.getSparqlProgramMultiple(chartNames);

		jsonOutput.addProperty("dateUpdated", BRICSTimeDateUtil.formatDate(sdCache.getUpdateDate()));

		return jsonOutput.toString();
	}
	
	@GET
	@Path("sparql/questionNumber/{number}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSparqlQuestionNumberResponse(@PathParam("number") Long number,@QueryParam("questionName") String questionName)
			throws UnsupportedEncodingException {
		logger.debug("Webservice sparql question number");
		SummaryResult responseResult = queryToolManager.getSparqlSummaryData(questionName,number);
		//NOTE: if this returns null should we return an empty json object?
		LinkedHashMap<String, String> singleResponse =
				(LinkedHashMap<String, String>) responseResult.getResults();
		JsonArray singleResponseJsonArray = new JsonArray();
		if(singleResponse.isEmpty() && !responseResult.getJsonResults().isEmpty()) {
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
	public String getSparqlQuestionTextResponse(@PathParam("text") String text,@QueryParam("questionName") String questionName)
			throws UnsupportedEncodingException {
		logger.debug("Webservice sparql question text");
		SummaryResult responseResult = queryToolManager.getSparqlSummaryData(questionName,text);
		//NOTE: if this returns null should we return an empty json object?
		LinkedHashMap<String, String> singleResponse =
				(LinkedHashMap<String, String>) responseResult.getResults();
		JsonArray singleResponseJsonArray = new JsonArray();
		if(singleResponse.isEmpty() && !responseResult.getJsonResults().isEmpty()) {
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
			@QueryParam("chartName") List<String> chartNames)
			throws UnsupportedEncodingException {
		studyId = URLDecoder.decode(studyId, "UTF-8");
		logger.debug("Webservice getStudyDataChart with studyId: " + studyId);
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager);

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
			@QueryParam("chartName") List<String> chartNames)
			throws UnsupportedEncodingException {
		studyId = URLDecoder.decode(studyId, "UTF-8");
		logger.debug("Webservice getStudyDataChart with studyId: " + studyId);
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager);

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
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager);
		sdCache.clearCache(queryToolManager);
		
		logger.info("SummaryData cache cleared.");
		return Response.ok().build();
	}
	
	@GET
	@Path("countSummary/{studyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCountSummaryData(@PathParam("studyId") String studyId)
			throws UnsupportedEncodingException {
		logger.debug("Webservice getCountSummaryData with study ID "+studyId);
		JsonObject jsonOutput = new JsonObject();
		StudySubmittedFormCache studySubmittedFormCache = StudySubmittedFormCache.getInstance(repositoryManager,queryToolManager);
		Integer totalNumRec = studySubmittedFormCache.getRowCountByStudy(studyId);
		jsonOutput.addProperty("totalRecords", String.valueOf(totalNumRec));
		
		Integer totalNumForm = studySubmittedFormCache.getFormCountByStudy(studyId);
		jsonOutput.addProperty("totalFormWithData", String.valueOf(totalNumForm));
		
		String lastSubmitDateStr = studySubmittedFormCache.getLastSubmitDateByStudy(studyId);
		String lastSubmitDateFormated = "";
		try {
			if(lastSubmitDateStr != null && !lastSubmitDateStr.equals("")){
				Date lastSubmitDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(lastSubmitDateStr);	
				lastSubmitDateFormated = new SimpleDateFormat("yyyy-MM-dd").format(lastSubmitDate);
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
	public String submittedDataSearch(){
		logger.debug("PUBLIC REST WS CALL: studySubmittedData");
		
		JsonObject jsonOutput = new JsonObject();
		
		StudySubmittedFormCache studySubmittedFormCache = StudySubmittedFormCache.getInstance(repositoryManager,queryToolManager);
		JsonArray commonlyUsedFsJson =studySubmittedFormCache.getAllPublicSubmittedFormsJson();
		System.out.println("Complete get all public submitted forms.");
		JsonArray studySubmittedDataJson = repositoryManager.getPublicSiteSubmittedData(studySubmittedFormCache);
		System.out.println("Complete get public site submitted data.");
		
		Collection<StudySubmittedForm> studyCollection = queryToolManager.getAllStudySubmittedForms().values();
		
		List <StudySubmittedForm> studyLists = new ArrayList<StudySubmittedForm>(studyCollection);		
		String totalNumberOfRecords = studySubmittedFormCache.getTotalRecordCount(studyLists);
		
		List<BasicStudySearch> basicStudyList = repositoryManager.getPublicSiteSearchBasicStudies();
		String totalNumberofSharedStudies = studySubmittedFormCache.getSharedStudiesCount(basicStudyList);	
		
		jsonOutput.add("totalNumberOfRecords", new JsonPrimitive(totalNumberOfRecords));
		jsonOutput.add("totalNumberOfSharedStudies", new JsonPrimitive(totalNumberofSharedStudies));
		jsonOutput.add("commonlyUsedFs", commonlyUsedFsJson);
		jsonOutput.add("studySubmittedData", studySubmittedDataJson);

		return jsonOutput.toString();
	}

}
