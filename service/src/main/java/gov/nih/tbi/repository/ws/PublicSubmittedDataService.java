package gov.nih.tbi.repository.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.service.QueryToolManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.repository.model.PublicSubmittedForm;
import gov.nih.tbi.repository.model.StudySubmittedForm;
import gov.nih.tbi.repository.model.StudySubmittedFormCache;
import gov.nih.tbi.repository.model.hibernate.BasicStudySearch;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;
import gov.nih.tbi.repository.model.hibernate.UserFile;

@Path("/submittedData/")
public class PublicSubmittedDataService extends AbstractRestService {
	private static Logger logger = Logger.getLogger(PublicSubmittedDataService.class);
	
	@Autowired
	protected RepositoryManager repositoryManager;
	
	@Autowired
	private QueryToolManager queryToolManager;
	
	@GET   	
	@Path("getByDate/{tableName}/{selectedDate}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDataByDate(@PathParam("tableName") String tableName, @PathParam("selectedDate") String selectedDate){
		logger.debug("REST WS CALL: getSubmittedDataByDaterange ( " + selectedDate  +" ) ");

		if(!modulesConstants.getIsRepositoryPublic()){
			String msg = "PUBLIC REST WS CALL: getSubmittedDataByDaterange. The service is forbidden on this instance.";
			logger.error(msg + "Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		if(selectedDate != null ){
			String orgName = modulesConstants.getModulesOrgName();
			JsonArray dataJson = new JsonArray();
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");	
			try {
				Date sdate = formatter.parse(selectedDate);

				String submittedDateFileName = orgName + "_" +formatter.format(sdate) + "_studyMetrics.json";
				try {
					InputStream fis = repositoryManager.getSftpInputStream(ServiceConstants.TBI_DRUPAL_DATAFILE_ENDPOINT_ID,
											ServiceConstants.TBI_PUBLIC_SITE_FILE_PATH, submittedDateFileName);
					String jsonStr = IOUtils.toString(fis, "utf-8"); 
					JsonParser jsonParser = new JsonParser();
					JsonArray selectedDateJsonArr = new JsonArray();
					if(tableName.equalsIgnoreCase("submittedData")){
						selectedDateJsonArr = jsonParser.parse(jsonStr).getAsJsonObject().get("studySubmittedData").getAsJsonArray();
					} else if (tableName.equalsIgnoreCase("commonlyUsedFS")){
						selectedDateJsonArr = jsonParser.parse(jsonStr).getAsJsonObject().get("commonlyUsedFs").getAsJsonArray();
					}

					dataJson.addAll(selectedDateJsonArr);
					
				} catch (JSchException | SftpException e1) {
					logger.error("PUBLIC REST WS CALL: getSubmittedDataByDaterange. Failed to get data from sftp file");
					e1.printStackTrace();
				} catch (IOException e2) {
					logger.error("PUBLIC REST WS CALL: getSubmittedDataByDaterange. Failed to convert inputStream from sftp file into string");
					e2.printStackTrace();
				}

			} catch (ParseException e) {
				logger.error("PUBLIC REST WS CALL: getSubmittedDataByDaterange. Failed to parse string to date");
				e.printStackTrace();
			}

			return Response.ok().entity(dataJson).build();

		} else {
			String msg = "PUBLIC REST WS CALL: getSubmittedDataByDaterange. Didn't complete because missing selected date range. selected date = " + selectedDate;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
	}

	@GET   	
	@Path("getTotalNumbersByDate/{selectedDate}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTotalNumbersByDate( @PathParam("selectedDate") String selectedDate){
		logger.debug("REST WS CALL: getTotalNumbersByDaterange ( " + selectedDate  +" ) ");

		if(!modulesConstants.getIsRepositoryPublic()){
			String msg = "PUBLIC REST WS CALL: getTotalNumbersByDaterange. The service is forbidden on this instance.";
			logger.error(msg + "Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		if(selectedDate != null ){
			String orgName = modulesConstants.getModulesOrgName();
			JsonObject dataJson = new JsonObject();
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");	
			try {
				Date sdate = formatter.parse(selectedDate);

				String submittedDateFileName = orgName + "_" +formatter.format(sdate) + "_studyMetrics.json";
				try {
					InputStream fis = repositoryManager.getSftpInputStream(ServiceConstants.TBI_DRUPAL_DATAFILE_ENDPOINT_ID,
											ServiceConstants.TBI_PUBLIC_SITE_FILE_PATH, submittedDateFileName);
					String jsonStr = IOUtils.toString(fis, "utf-8"); 
					JsonParser jsonParser = new JsonParser();

					dataJson.add("totalNumberOfRecords", jsonParser.parse(jsonStr).getAsJsonObject().get("totalNumberOfRecords"));
					dataJson.add("totalNumberOfSharedStudies", jsonParser.parse(jsonStr).getAsJsonObject().get("totalNumberOfSharedStudies"));
					
				} catch (JSchException | SftpException e1) {
					logger.error("PUBLIC REST WS CALL: getTotalNumbersByDaterange. Failed to get data from sftp file");
					e1.printStackTrace();
				} catch (IOException e2) {
					logger.error("PUBLIC REST WS CALL: getTotalNumbersByDaterange. Failed to convert inputStream from sftp file into string");
					e2.printStackTrace();
				}

			} catch (ParseException e) {
				logger.error("PUBLIC REST WS CALL: getTotalNumbersByDaterange. Failed to parse string to date");
				e.printStackTrace();
			}

			return Response.ok().entity(dataJson).build();

		} else {
			String msg = "PUBLIC REST WS CALL: getTotalNumbersByDaterange. Didn't complete because missing selected date range. selected date = " + selectedDate;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
	}
	
}
