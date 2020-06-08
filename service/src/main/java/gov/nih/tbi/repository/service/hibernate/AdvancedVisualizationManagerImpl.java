package gov.nih.tbi.repository.service.hibernate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.mail.MessagingException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;

import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.jcraft.jsch.JSchException;

import gov.nih.tbi.repository.dao.DatafileEndpointInfoDao;
import gov.nih.tbi.repository.dao.DatasetDao;
import gov.nih.tbi.repository.dao.VisualizationStudyDao;
import gov.nih.tbi.repository.model.StudySubmittedFormCache;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.SummaryDataCache;
import gov.nih.tbi.repository.model.hibernate.BasicStudySearch;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.Grant;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudyForm;
import gov.nih.tbi.repository.model.hibernate.StudySite;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.model.hibernate.VisualizationAccessData;
import gov.nih.tbi.repository.model.hibernate.VisualizationAccessRecord;
import gov.nih.tbi.repository.model.hibernate.VisualizationDataset;
import gov.nih.tbi.repository.model.hibernate.VisualizationStudy;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.common.util.ProformsWsProvider;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.hibernate.Address;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.AdvancedVisualizationManager;
import gov.nih.tbi.commons.service.QueryToolManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.commons.util.BRICSFilesUtils;
import gov.nih.tbi.query.dao.SummaryQueryDao;
import gov.nih.tbi.query.model.hibernate.SummaryQuery;
import gov.nih.tbi.repository.service.io.ExecClient;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;

@Service
public class AdvancedVisualizationManagerImpl implements AdvancedVisualizationManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4744592627027919548L;
	private static Logger logger = Logger.getLogger(AdvancedVisualizationManagerImpl.class);
	private FileReader locations = null;
	private String domain;
	private String orgName;
	private SftpClient sftpClient;

	public final String historyFileName = "history";
	public final String nodeScriptFileName = "generateStudyIds.js";
	
	private File logFile;

	@Autowired
	protected RepositoryManager repositoryManager;

	@Autowired
	private QueryToolManager queryToolManager;

	@Autowired
	DatafileEndpointInfoDao datafileEndpointInfoDao;

	@Autowired
	ModulesConstants modulesConstants;

	@Autowired
	AccountManager accountManager;

	@Autowired
	VisualizationStudyDao visualizationStudyDao;

	@Autowired
	DatasetDao datasetDao;

	@Autowired
	SummaryQueryDao summaryQueryDao;
	
	@Autowired
    private MailEngine mailEngine;

    @Autowired
    protected MessageSource messageSource;
    
    @Autowired
	private  ProformsWsProvider proformsWsProvider;
    
    
    private PrintStream logStream;
	private boolean hasError;
	
	private static final String LOG_START_FORMAT = "----------  Summary Data Gen Started at %s ----------\n";
	private static final String LOG_SUCCESS_FORMAT = "----------  Summary Data Gen ended Successfully at %s ----------\n";
	private static final String LOG_FAIL_FORMAT = "----------  Summary Data Gen ended with Errors at %s ----------\n";

	public void buildSummaryData() {
		logFile = initializeLogPrinter();
		domain = modulesConstants.getModulesAccountURL();
		orgName = modulesConstants.getModulesOrgName();

		String todaysDate = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
		String studyDataFileName = orgName + "_" + todaysDate + ".json";
		String baseStudyDataFileName = orgName + ".json";
		String destinationPath = ServiceConstants.ADVANCED_VIEWER_UPLOAD_PATH;
		try {
			JsonObject dataAccessData = buildDataAccessData();
			logStream.println("Advanced Visualization Build Data Access Data Complete");
			JsonObject studyData = buildStudySummaryData();
			logStream.println("Advanced Visualization Build Study Data Complete");
			JsonArray jsonStudies = studyData.getAsJsonArray("studies");
			
			// Below will add Data Access info into Study Data
			for (int i = 0; i < jsonStudies.size(); i++) {
				JsonObject study = jsonStudies.get(i).getAsJsonObject();
				String studyId = study.get("id").getAsString();
				JsonObject studyObj = dataAccessData.getAsJsonObject(studyId);
				if (studyObj != null) {
					study.add("downloads", studyObj.getAsJsonObject("downloads"));
					study.add("studyDownloads", studyObj.getAsJsonObject("studyDownloads"));
				}

			}

			studyData.add("userList", dataAccessData.getAsJsonArray("userList"));

			Gson gsonOutput = new GsonBuilder().setPrettyPrinting().create();
			String studyDataString = gsonOutput.toJson(studyData);
			
			try {
				writeFileToPublic(studyDataFileName, studyDataString);

				try {

					copyFileOnRemote(destinationPath, studyDataFileName, baseStudyDataFileName);
					updateHistoryFile(studyDataFileName, destinationPath);
				} catch (Exception e) {
					logger.error("unable to copy file to updated name (to fitbir.json)");
					e.printStackTrace(logStream);
					// unable to copy file to updated name
					hasError = true;
				} finally {
					handleFailureNotification(hasError,logFile);
				}
			} catch (Exception e) {
				logger.error("unable to copy file to server");
				e.printStackTrace(logStream);
				hasError = true;
				// unable to copy file to server
			} finally {
				handleFailureNotification(hasError,logFile);
			}
		} catch (Exception e) {
			logger.error("major process failure");
			e.printStackTrace();
			// full process failure
			hasError = true;
		} finally {
			handleFailureNotification(hasError,logFile);
			if(!hasError) {
				//gen ran successfully
				String logEndString = String.format(LOG_SUCCESS_FORMAT, BRICSTimeDateUtil.getCurrentReadableTimeString());
				logStream.println(logEndString);
				logStream.close();
				
			}
		}

		logger.info("Advanced Visualization Build Service Complete");
		// return Response.ok().build();
	}

	public void buildFile() {
		domain = modulesConstants.getModulesAccountURL();
		orgName = modulesConstants.getModulesOrgName();

		String todaysDate = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
		String studyDataFileName = orgName + "_" + todaysDate + ".json";
		String baseStudyDataFileName = orgName + ".json";
		String dataAccessFileName = orgName + "_dataAccess_" + todaysDate + ".json";
		String baseDataAccessFileName = orgName + "_dataAccess.json";
		String studyIdsFileName = "studyIds_" + todaysDate + ".json";
		String baseStudyIdsFileName = "studyIds.json";
		String destinationPath = ServiceConstants.ADVANCED_VIEWER_UPLOAD_PATH;
		try {
			String dataAccessData = buildTestDataAccessData();
			logger.info("Advanced Visualization Build Data Access Data Complete");
			String studyData = buildStudyData();
			logger.info("Advanced Visualization Build Study Data Complete");

			try {
				writeFileToPublic(studyDataFileName, studyData);
				writeFileToPublic(dataAccessFileName, dataAccessData);

				try {

					copyFileOnRemote(destinationPath, studyDataFileName, baseStudyDataFileName);
					copyFileOnRemote(destinationPath, dataAccessFileName, baseDataAccessFileName);

					runNodeProcess(destinationPath, nodeScriptFileName, dataAccessFileName);
					copyFileOnRemote(destinationPath, baseStudyIdsFileName, studyIdsFileName);

					updateHistoryFile(studyDataFileName, destinationPath, dataAccessFileName, studyIdsFileName);
				} catch (Exception e) {
					logger.error("unable to copy file to updated name (to fitbir.json)");
					e.printStackTrace();
					// unable to copy file to updated name
				}
			} catch (Exception e) {
				logger.error("unable to copy file to server");
				e.printStackTrace();
				// unable to copy file to server
			}
		} catch (Exception e) {
			logger.error("major process failure");
			e.printStackTrace();
			// full process failure
		}

		logger.info("Advanced Visualization Build Service Complete");
		// return Response.ok().build();
	}

	/**
	 * Appends information about today's uploaded files to the history file for this
	 * year. If this year's history file does not yet exist, the file is created and
	 * a header added to make the output pseudo-JSON (it is not quite JSON because
	 * it doesn't have the tailing closing brace. That must be added by Alexandra in
	 * the viewer code. We did that so we can just append here instead of removing
	 * then adding).
	 * 
	 * @param metaFileName       file name of the "fitbir.json" file holding file
	 *                           metadata
	 * @param dataAccessFileName file name of the data access file
	 * @param studyIdsFileName   file name of the compiled studyIds file
	 * @throws Exception on any major error
	 */
	private void updateHistoryFile(String metaFileName, String destinationPath, String dataAccessFileName,
			String studyIdsFileName) throws Exception {
		/*
		 * This process appends the file names passed in to the history file for this
		 * year. Each year gets its own file so we may have to create the file if this
		 * year's file doesn't exist. --------------- create string to append check that
		 * this year's file exists if year's file does not exists create file with array
		 * prefix append today's object to the file
		 */
		logger.info("Updating History File");
		try {
			DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DRUPAL_DATAFILE_ENDPOINT_ID);
			ExecClient execClient = new ExecClient(info.getUserName(), info.getPassword(), info.getUrl(),
					info.getPort());
			Date todaysDate = new Date();
			String thisYear = (new SimpleDateFormat("yyyy")).format(todaysDate);
			String thisYearHistoryFilename = historyFileName + "_" + thisYear + ".json";
			String today = (new SimpleDateFormat("MM_dd")).format(todaysDate);

			String content = "\"" + today + "\": {" + "\"meta\": \"" + metaFileName + "\"," + "\"dataaccess\": \""
					+ dataAccessFileName + "\"," + "\"studyIds\": \"" + studyIdsFileName + "\"}";

			/*
			 * TODO: We need to create this function to accurrately test existance of file
			 * on serve.
			 * 
			 * boolean doesFileExist = execClient.doesFileExist(destinationPath +
			 * thisYearHistoryFilename); if (!doesFileExist) {
			 * logger.info("History file for " + thisYear +
			 * " does not exist.  It will be created"); String header = "{";
			 * execClient.appendToFile(destinationPath + thisYearHistoryFilename, header); }
			 * else { // if the file exists, it already has one entry content = "," +
			 * content; }
			 */

			content = "," + content;
			// we don't have to have an explicit create command because appendToFile
			// uses the >> format which creates the file if it doesn't exist
			execClient.appendToFile(destinationPath + "/" + thisYearHistoryFilename, content);
			execClient.disconnectSession();
		} catch (JSchException e) {
			logger.error("failure in updating history file");
			e.printStackTrace();
			throw new Exception("Failure in updating history file", e);
		} catch (Exception e) {
			logger.error("general failure in updating history file");
			e.printStackTrace();
			throw new Exception("General failure in updating history file", e);
		}
	}

	/**
	 * Appends information about today's uploaded summary data file to the history
	 * file for this year. If this year's history file does not yet exist, the file
	 * is created and a header added to make the output pseudo-JSON (it is not quite
	 * JSON because it doesn't have the tailing closing brace. That must be added by
	 * Alexandra in the viewer code. We did that so we can just append here instead
	 * of removing then adding).
	 * 
	 * @param metaFileName       file name of the "fitbir.json" file holding file
	 *                           metadata
	 * @param dataAccessFileName file name of the data access file
	 * @param studyIdsFileName   file name of the compiled studyIds file
	 * @throws Exception on any major error
	 */
	private void updateHistoryFile(String metaFileName, String destinationPath) throws Exception {
		/*
		 * This process appends the file names passed in to the history file for this
		 * year. Each year gets its own file so we may have to create the file if this
		 * year's file doesn't exist. --------------- create string to append check that
		 * this year's file exists if year's file does not exists create file with array
		 * prefix append today's object to the file
		 */
		logger.info("Updating History File");
		try {
			DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DRUPAL_DATAFILE_ENDPOINT_ID);
			ExecClient execClient = new ExecClient(info.getUserName(), info.getPassword(), info.getUrl(),
					info.getPort());
			Date todaysDate = new Date();
			String thisYear = (new SimpleDateFormat("yyyy")).format(todaysDate);
			String thisYearHistoryFilename = historyFileName + "_" + thisYear + ".json";
			String today = (new SimpleDateFormat("MM_dd")).format(todaysDate);

			String content = "\"" + today + "\": {" + "\"meta\": \"" + metaFileName + "\" }";

			/*
			 * TODO: We need to create this function to accurrately test existance of file
			 * on serve.
			 * 
			 * boolean doesFileExist = execClient.doesFileExist(destinationPath +
			 * thisYearHistoryFilename); if (!doesFileExist) {
			 * logger.info("History file for " + thisYear +
			 * " does not exist.  It will be created"); String header = "{";
			 * execClient.appendToFile(destinationPath + thisYearHistoryFilename, header); }
			 * else { // if the file exists, it already has one entry content = "," +
			 * content; }
			 */

			content = "," + content;
			// we don't have to have an explicit create command because appendToFile
			// uses the >> format which creates the file if it doesn't exist
			execClient.appendToFile(destinationPath + "/" + thisYearHistoryFilename, content);
			execClient.disconnectSession();
		} catch (JSchException e) {
			logger.error("failure in updating history file");
			e.printStackTrace();
			throw new Exception("Failure in updating history file", e);
		} catch (Exception e) {
			logger.error("general failure in updating history file");
			e.printStackTrace();
			throw new Exception("General failure in updating history file", e);
		}
	}

	/**
	 * Runs alexandra's command in Node to produce her studyIds.json file based on
	 * our dataAccess file.
	 * 
	 * @param destinationPath    folder where the process happens
	 * @param nodeScriptFileName script that should be run. Exists inside the
	 *                           destinationPath
	 * @param dataAccessFileName data file that should be processed. Exists inside
	 *                           the destinationPath
	 * @throws Exception when unable to create the client
	 */
	private void runNodeProcess(String destinationPath, String nodeScriptFileName, String dataAccessFileName)
			throws Exception {
		// node process is:
		// node <script_filename> <data_filename>
		// it outputs studyIds.json
		// that should be copied to studyIds_<date>.json as we do
		try {
			DatafileEndpointInfo info = datafileEndpointInfoDao
					.get(ServiceConstants.TBI_PUBLIC_DRUPAL_DATAFILE_ENDPOINT_ID);
			ExecClient execClient = new ExecClient(info.getUserName(), info.getPassword(), info.getUrl(),
					info.getPort());
			String scriptPath = destinationPath + nodeScriptFileName;
			String dataFilePath = destinationPath + dataAccessFileName;
			execClient.runNodeCommand(destinationPath, scriptPath + " " + dataFilePath);
			execClient.disconnectSession();
		} catch (JSchException e) {
			logger.error("");
			e.printStackTrace();
			throw new Exception("Failure in file processing", e);
		} catch (Exception e) {
			logger.error("The Advanced Viewer file could not be saved before transfer");
			throw new Exception("Failure in file processing", e);
		}
	}

	private void copyFileOnRemote(String folderPath, String sourceFileName, String destinationFileName)
			throws Exception {
		try {
			DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DRUPAL_DATAFILE_ENDPOINT_ID);
			ExecClient execClient = new ExecClient(info.getUserName(), info.getPassword(), info.getUrl(),
					info.getPort());
			String sourcePath = folderPath + sourceFileName;
			String destinationPath = folderPath + destinationFileName;

			execClient.copyOnRemote(sourcePath, destinationPath);
			execClient.disconnectSession();
		} catch (JSchException e) {
			logger.error("");
			e.printStackTrace();
			throw new Exception("Failure in file processing", e);
		} catch (Exception e) {
			logger.error("The Advanced Viewer file could not be saved before transfer");
			throw new Exception("Failure in file processing", e);
		}
		// no need to close. execClient handles that for us

	}

	private void writeFileToPublic(String fileName, String data) throws Exception {
		try {
			String destinationPath = ServiceConstants.ADVANCED_VIEWER_UPLOAD_PATH;

			initializeSftpClient();
			FileUtils.writeStringToFile(new File("/tmp/" + fileName), data);
			File reopenedFile = new File("/tmp/" + fileName);
			sftpClient.upload(reopenedFile, destinationPath, fileName);
		} catch (JSchException e) {
			logger.error("SFTP client could not be initialized");
			e.printStackTrace();
			throw new Exception("Failure in file processing", e);
		} catch (IOException e) {
			logger.error("Could not write Advanced Viewer file");
			e.printStackTrace();
			throw new Exception("Failure in file processing", e);
		} catch (Exception e) {
			logger.error("The Advanced Viewer file could not be saved before transfer");
			throw new Exception("Failure in file processing", e);
		} finally {
			closeClient();
		}
	}

	private void initializeSftpClient() throws JSchException {
		if (sftpClient == null) {
			DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DRUPAL_DATAFILE_ENDPOINT_ID);
			sftpClient = SftpClientManager.getClient(info);
		}
	}

	private void closeClient() {
		SftpClientManager.closeClient(sftpClient);
		sftpClient = null;
	}

	public JsonObject buildStudySummaryData() {
		JsonObject output = new JsonObject();
		output.addProperty("dateCreated", BRICSTimeDateUtil.getCurrentReadableTimeString());
		output.add("name", new JsonPrimitive(orgName));
		output.add("link", new JsonPrimitive(domain));

		// Let's get all the studies we are working with

		// for each study
		// get site name and city
		// if site name and city match an entry that already exists,
		// add the new study to that site
		// else
		// add a new site to the siteToStudyMap and add the study to that entry
		// Studies must include PI listings
		//
		// study-level summary data must also be included in the study data ("data" sub
		// - same format as summary data)

		// This is a list of studies that have both submitted data and not submitted
		// data
		// NOTE: Should we get studies without data?????
		Map<Long, VisualizationStudy> visualizationStudies = getAllStudiesVisualization();
		logStream.println("The total amount of viz studies is: " + visualizationStudies.size() + " : " + BRICSTimeDateUtil.getCurrentReadableTimeString());
		output.add("totalNumberOfStudies", new JsonPrimitive(visualizationStudies.size()));

		// NOTE: Should we get submission types when we get each study? that would mean
		// a call to postgress for every study. do we just have one call for all
		// studies?
		Map<Long, Set<SubmissionType>> studySubmissionTypes = getSubmissionTypes(visualizationStudies);

		StudySubmittedFormCache studySubmittedFormCache = StudySubmittedFormCache.getInstance(repositoryManager,
				queryToolManager);

		Map<Long, BasicStudySearch> basicStudyList = repositoryManager.getPublicSiteSearchBasicStudiesMap();
		logStream.println("The total amount of basic studies is: " + basicStudyList.size());
		// NOTE: This indicates that a study is considered a shared study if it has a
		// shared dataset
		String totalNumberofSharedStudies = studySubmittedFormCache.getSharedStudiesCount(basicStudyList);
		logStream.println("The total amount of shared studies is: " + totalNumberofSharedStudies);
		output.add("totalNumberOfSharedStudies", new JsonPrimitive(totalNumberofSharedStudies));

		//empty the data cache
		emptyCache();
		JsonObject data = new JsonObject();

		JsonObject summaryResults = this.getProgramSummaryGraphs();
		for (Entry<String, JsonElement> entry : summaryResults.entrySet()) {
			data.add(entry.getKey(), entry.getValue());
		}
		output.add("data", data);

		// a summary graph contains data like this:
		// {"sampCollTyp":[{"DNA":"1457"},{"Serum":"2372"},{"Plasma":"2669"},{"CSF":"690"},{"Blood":"3358"},{"RNA":"3894"}],"dateUpdated":"2017-03-03"}
		// and the URL for that is
		// https://pdbp.ninds.nih.gov/portal/ws/summaryData/program?chartName=sampCollTyp

		// fill in with site at first-level and study at second

		List<String> studyChartNames = this.getStudySummaryGraphNames();
		JsonArray studiesJson = new JsonArray();
		// changing the data structure to be study-centric
		int studyCount = 1;
		int totalRecordsSum = 0;
		int allStudiesRecordSum = 0;
		logStream.println("Begin getting study data " + ":" + BRICSTimeDateUtil.getCurrentReadableTimeString());
		
		for (Entry<Long, VisualizationStudy> entry : visualizationStudies.entrySet()) {
			VisualizationStudy visualizationStudy = entry.getValue();
			Study study = visualizationStudy.getStudy();
			String dataSharing = study.getStudyStatus().getName();

			// TODO: remove studies that are not currently sharing data?
			// TODO: do we want private data?
			if (dataSharing == null || dataSharing.equals("") || dataSharing.equals("Rejected")
					|| dataSharing.equals("Requested") || dataSharing.equals("Private")) {

				Integer totalNumRec = studySubmittedFormCache.getRowCountByStudy(study.getId().toString());
				allStudiesRecordSum += totalNumRec;
				continue;
			}

			JsonObject studyJson = new JsonObject();
			studyJson.add("title", valueOrEmpty(study.getTitle()));
			studyJson.add("id", valueOrEmpty(study.getId().toString()));
			if (study.getFundingSource() != null) {
				studyJson.add("funding", valueOrEmpty(study.getFundingSource().getName()));
			} else {
				studyJson.add("funding", valueOrEmpty("unknown"));
			}
			studyJson.add("visibility", valueOrEmpty(dataSharing));
			
			String dataSharingStatus = getStudySharingStatus(study);
			studyJson.add("dataSharing", valueOrEmpty(dataSharingStatus));

			String studyId = String.valueOf(study.getId());
			JsonObject countSummaryResult = this.performStudyCountSummaryQuery(studyId);
			if(dataSharingStatus == "shared") {
				totalRecordsSum += countSummaryResult.get("totalRecords").getAsInt();
			}
			allStudiesRecordSum += countSummaryResult.get("totalRecords").getAsInt();
			studyJson.add("recordCount", countSummaryResult.get("totalRecords"));
			studyJson.add("totalRecords", countSummaryResult.get("totalRecords"));
			studyJson.add("formWithDataCount", countSummaryResult.get("totalFormWithData"));
			studyJson.add("totalFormWithData", countSummaryResult.get("totalFormWithData"));
			studyJson.add("lastSubmitDate", countSummaryResult.get("lastSubmitDate"));

			String grantEndDate = study.getEndDate();
			studyJson.add("grantEndDate", valueOrEmpty(grantEndDate));

			// data sharing should be "shared", "private", or "no data"
			// for study in "requested" or "rejected" state, do not include the study in the
			// data!
			// shared = Public
			// private = Private
			// no data = <does not have data>
			studyJson.add("totalDataSize", new JsonPrimitive(visualizationStudy.getTotalDataFileSize()));

			String studyUrl = generateStudyUrl(study);
			if (studyUrl != null && studyUrl.equals("") && !studyUrl.startsWith("http")) {
				studyUrl = "http://" + studyUrl;
			}
			studyJson.add("externalLink", valueOrEmpty(studyUrl));
			String studyLocalUrl = domain + "study_profile/" + study.getId().toString();
			studyJson.add("link", new JsonPrimitive(studyLocalUrl));

			if (study.getGraphicFile() != null) {
				studyJson.add("studyImage",
						new JsonPrimitive(domain + "portal/ws/public/study/" + study.getId().toString() + "/graphic"));
			} else {
				studyJson.add("studyImage", new JsonPrimitive(""));
			}
			studyJson.add("abstract", valueOrEmpty(study.getAbstractText()));
			studyJson.add("aims", valueOrEmpty(study.getGoals()));

			BasicStudySearch basicStudyInfo = basicStudyList.get(study.getId());
			studyJson.add("principleName", valueOrEmpty(basicStudyInfo.getPrincipleName()));
			studyJson.add("fundingSource", valueOrEmpty(basicStudyInfo.getFundingSource()));

			String grantId = getStudyGrantIdList(study);
			studyJson.addProperty("grantId", grantId);

			// slowish
			Integer numbOfSubject = studySubmittedFormCache
					.getSubjectCountByStudy(String.valueOf(basicStudyInfo.getId()));
			studyJson.addProperty("subjectCount", numbOfSubject);
			studyJson.addProperty("privateDatasetCount", basicStudyInfo.getPrivateDatasetCount());
			studyJson.addProperty("sharedDatasetCount", basicStudyInfo.getSharedDatasetCount());

			JsonArray studyDataTypes = getStudyDataTypes(study, studySubmissionTypes);
			studyJson.add("dataType", studyDataTypes);
			// -- Studies
			JsonArray studySitesJson = getStudySites(study);
			studyJson.add("sites", studySitesJson);
			// -- PIs
			JsonArray piListJson = getStudyPis(study);
			studyJson.add("PIs", piListJson);
			// -- Study instance data
			JsonObject studyData = getStudySummaryGraphData(studyChartNames, study);
			// -- Forms
			JsonArray forms = getStudyJsonForms(study);
			studyData.add("forms", forms);
			studyJson.add("data", studyData);
			studiesJson.add(studyJson);
			logStream.println("Completed build study data for study " + visualizationStudy.getStudy().getTitle()
					+ " ( " + studyCount + " of " + visualizationStudies.size() + ")" + ":" + BRICSTimeDateUtil.getCurrentReadableTimeString());
			studyCount++;
		}
		
		logStream.println("End getting study data " + ":" + BRICSTimeDateUtil.getCurrentReadableTimeString());

		output.add("totalNumberOfRecords", new JsonPrimitive(allStudiesRecordSum));
		output.add("totalNumberOfSharedRecords", new JsonPrimitive(totalRecordsSum));
		output.add("studies", studiesJson);
		JsonArray commonlyUsedFsJson = studySubmittedFormCache.getAllPublicSubmittedFormsJson();
		output.add("commonlyUsedFs", commonlyUsedFsJson);
		
		return output;
		// Gson gsonOutput = new GsonBuilder().setPrettyPrinting().create();
		// return gsonOutput.toJson(output);
	}

	public JsonArray getStudyDataTypes(Study study, Map<Long, Set<SubmissionType>> studySubmissionTypes) {
		JsonArray studyDataTypes = new JsonArray();
		// This annoys me to no end. We can't actually insert the submissionTypes into
		// the study - because Hibernate. So, because getIsClinical, etc. rely on that
		// we have to insert it somehow. How do we do it? We create a proxy study that's
		// not controlled by hibernate! This works, isn't a terrible memory drain
		// (because there's no actual data in the study), and doesn't duplicate code
		// really.
		// So, I'm happy about that - but I still hate Hibernate doing stuff like this
		// sometimes.
		Study tempStudy = new Study();
		tempStudy.setSubTypes(studySubmissionTypes.get(study.getId()));

		if (tempStudy.getIsClinical())
			studyDataTypes.add(new JsonPrimitive("clinical"));
		if (tempStudy.getIsGenomic())
			studyDataTypes.add(new JsonPrimitive("genomics"));
		if (tempStudy.getIsImaging())
			studyDataTypes.add(new JsonPrimitive("imaging"));

		return studyDataTypes;
	}

	public JsonArray getStudyJsonForms(Study study) {
		JsonArray forms = new JsonArray();
		for (StudyForm form : study.getStudyForms()) {
			forms.add(valueOrEmpty(form.getTitle()));
		}
		return forms;
	}

	public JsonObject getStudySummaryGraphData(List<String> studyChartNames, Study study) {
		JsonObject studyData = new JsonObject();
		JsonObject studySummaryData = this.getStudySummaryGraphs(studyChartNames, study.getId().toString());
		for (Entry<String, JsonElement> studyEntry : studySummaryData.entrySet()) {

			String studyKey = "";
			switch (studyEntry.getKey()) {
				case "ageRangeStudy":
					studyKey = "ageRange";
					break;
				case "genderTypStudy":
					studyKey = "genderTyp";
					break;
				case "imgModalityTypStudy":
					studyKey = "imgModalityTyp";
					break;
				default:
					studyKey = studyEntry.getKey();
					break;
			}

			studyData.add(studyKey, studyEntry.getValue());
		}
		studyData.add("dateUpdated", studySummaryData.get("dateUpdated"));
		return studyData;
	}

	public JsonArray getStudyPis(Study study) {
		JsonArray piListJson = new JsonArray();
		for (ResearchManagement rmg : study.getResearchMgmtSet()) {
			JsonObject pi = new JsonObject();
			pi.add("name", valueOrEmpty(rmg.getFullName()));
			pi.add("role", valueOrEmpty(String.valueOf(rmg.getRole().getName())));
			if (rmg.getPictureFile() != null) {
				pi.add("image", new JsonPrimitive(domain + "portal/ws/public/study/researcher/"
						+ study.getId().toString() + "/" + rmg.getId().toString() + "/image"));
			} else {
				pi.add("image", new JsonPrimitive(domain + "sites/default/files/genericImage.jpg"));
			}
			pi.add("institute", valueOrEmpty(rmg.getOrgName()));
			piListJson.add(pi);
		}

		return piListJson;
	}

	public JsonArray getStudySites(Study study) {
		Set<StudySite> studySites = study.getStudySiteSet();
		JsonArray studySitesJson = new JsonArray();
		for (StudySite studySite : studySites) {
			JsonObject studySiteJson = new JsonObject();
			studySiteJson.add("institute", valueOrEmpty(studySite.getSiteName()));
			studySiteJson.add("location", getLocation(addressToString(studySite)));
			studySiteJson.add("primary", new JsonPrimitive(studySite.isPrimary()));

			studySitesJson.add(studySiteJson);
		}
		return studySitesJson;
	}

	public String getStudyGrantIdList(Study study) {
		Set<Grant> grantSet = new HashSet<Grant>(study.getGrantSet());
		List<String> grantIdList = new ArrayList<String>();
		for (Grant grant : grantSet) {
			grantIdList.add(grant.getGrantId());
		}
		String grantId = grantIdList.size() == 0 ? "" : StringUtils.join(grantIdList);
		if (grantId.length() > 2) {// remove square blankets
			grantId = grantId.substring(1, grantId.length() - 1);
		}

		return grantId;
	}

	public String getStudySharingStatus(Study study) {
		String dataSharing = "";
		List<VisualizationDataset> studyDatasetSet = datasetDao.getVisualizationStudyDatasetByStudy(study);
		if (studyDatasetSet.isEmpty()) {
			dataSharing = "no data";
		} else {
			dataSharing = "private";
			for (VisualizationDataset studySite : studyDatasetSet) {
				if (studySite.getDatasetStatusId().equals(DatasetStatus.SHARED.getId().intValue())) {
					dataSharing = "shared";
					break;
				} else if (studySite.getDatasetStatusId().equals(DatasetStatus.PRIVATE.getId().intValue())) {
					dataSharing = "private";
					// Probably don't need this.
				}

			}

		}
		return dataSharing;
	}

	public String buildStudyData() {
		JsonObject output = new JsonObject();
		output.add("name", new JsonPrimitive("FITBIR"));
		output.add("link", new JsonPrimitive("https://fitbir.nih.gov"));

		JsonObject data = new JsonObject();

		// fill in from the summary data: protocol level ageRange, genderTyp,
		// imgModalityTyp
		// we probably will have to populate the "type" parameter here to tell what type
		// of
		// chart to show
		String[] charts = new String[] { "ageRange", "genderTyp", "imgModalityTyp", "eduYrCt", "injuryCause",
				"gcsTotalScore", "pGOSEScore", "tBITyp" };
		JsonObject summaryResults = this.performSummaryDataQuery(charts, null);
		data.add("ageRange", summaryResults.get("ageRange"));
		data.add("genderTyp", summaryResults.get("genderTyp"));
		data.add("imgModalityTyp", summaryResults.get("imgModalityTyp"));
		data.add("eduYrCt", summaryResults.get("eduYrCt"));
		data.add("injuryCause", summaryResults.get("injuryCause"));
		data.add("gcsTotalScore", summaryResults.get("gcsTotalScore"));
		data.add("pGOSEScore", summaryResults.get("pGOSEScore"));
		data.add("tBITyp", summaryResults.get("tBITyp"));
		data.add("dateUpdated", summaryResults.get("dateUpdated"));
		output.add("data", data);

		// a summary graph contains data like this:
		// {"sampCollTyp":[{"DNA":"1457"},{"Serum":"2372"},{"Plasma":"2669"},{"CSF":"690"},{"Blood":"3358"},{"RNA":"3894"}],"dateUpdated":"2017-03-03"}
		// and the URL for that is
		// https://pdbp.ninds.nih.gov/portal/ws/summaryData/program?chartName=sampCollTyp

		// fill in with site at first-level and study at second

		// for each study
		// get site name and city
		// if site name and city match an entry that already exists,
		// add the new study to that site
		// else
		// add a new site to the siteToStudyMap and add the study to that entry
		// Studies must include PI listings
		//
		// study-level summary data must also be included in the study data ("data" sub
		// - same format as summary data)

		Map<Long, VisualizationStudy> visualizationStudies = getAllStudiesVisualization();
		Map<Long, Set<SubmissionType>> studySubmissionTypes = getSubmissionTypes(visualizationStudies);

		JsonArray studiesJson = new JsonArray();
		// changing the data structure to be study-centric
		int studyCount = 1;

		for (Entry<Long, VisualizationStudy> entry : visualizationStudies.entrySet()) {
			VisualizationStudy visualizationStudy = entry.getValue();
			Study study = visualizationStudy.getStudy();
			String dataSharing = study.getStudyStatus().getName();

			if (dataSharing == null || dataSharing.equals("") || dataSharing.equals("Rejected")
					|| dataSharing.equals("Requested")) {
				continue;
			}

			JsonObject studyJson = new JsonObject();
			studyJson.add("name", valueOrEmpty(study.getTitle()));
			studyJson.add("id", valueOrEmpty(study.getId().toString()));
			if (study.getFundingSource() != null) {
				studyJson.add("funding", valueOrEmpty(study.getFundingSource().getName()));
			} else {
				studyJson.add("funding", valueOrEmpty("unknown"));
			}
			studyJson.add("visibility", valueOrEmpty(dataSharing));

			String studyId = String.valueOf(study.getId());
			JsonObject countSummaryResult = this.performCountSummaryQuery(studyId);
			studyJson.add("totalRecords", countSummaryResult.get("totalRecords"));
			studyJson.add("totalFormWithData", countSummaryResult.get("totalFormWithData"));
			studyJson.add("lastSubmitDate", countSummaryResult.get("lastSubmitDate"));

			String grantEndDate = study.getEndDate();
			studyJson.add("grantEndDate", valueOrEmpty(grantEndDate));

			// data sharing should be "shared", "private", or "no data"
			// for study in "requested" or "rejected" state, do not include the study in the
			// data!
			// shared = Public
			// private = Private
			// no data = <does not have data>

			List<Dataset> studyDatasetSet = datasetDao.getDatasetsByStudy(study);
			if (studyDatasetSet.isEmpty()) {
				dataSharing = "no data";
			} else {
				dataSharing = "private";
				for (Dataset studySite : studyDatasetSet) {
					if (studySite.getDatasetStatus().getName().equals("Shared")) {
						dataSharing = "shared";
						break;
					} else if (studySite.getDatasetStatus().getName().equals("Private")) {
						dataSharing = "private";
						// Probably don't need this.
					}

				}

			}

			studyJson.add("totalDataSize", new JsonPrimitive(visualizationStudy.getTotalDataFileSize()));

			studyJson.add("dataSharing", valueOrEmpty(dataSharing));

			String studyUrl = generateStudyUrl(study);
			if (studyUrl != null && studyUrl.equals("") && !studyUrl.startsWith("http")) {
				studyUrl = "http://" + studyUrl;
			}
			studyJson.add("externalLink", valueOrEmpty(studyUrl));
			String studyLocalUrl = domain + "study_profile/" + study.getId().toString();
			studyJson.add("link", new JsonPrimitive(studyLocalUrl));
			studyJson.add("abstract", valueOrEmpty(study.getAbstractText()));
			studyJson.add("aims", valueOrEmpty(study.getGoals()));

			JsonArray studyDataTypes = new JsonArray();

			// This annoys me to no end. We can't actually insert the submissionTypes into
			// the study - because Hibernate. So, because getIsClinical, etc. rely on that
			// we have to insert it somehow. How do we do it? We create a proxy study that's
			// not controlled by hibernate! This works, isn't a terrible memory drain
			// (because there's no actual data in the study), and doesn't duplicate code
			// really.
			// So, I'm happy about that - but I still hate Hibernate doing stuff like this
			// sometimes.
			Study tempStudy = new Study();
			tempStudy.setSubTypes(studySubmissionTypes.get(study.getId()));

			if (tempStudy.getIsClinical())
				studyDataTypes.add(new JsonPrimitive("clinical"));
			if (tempStudy.getIsGenomic())
				studyDataTypes.add(new JsonPrimitive("genomics"));
			if (tempStudy.getIsImaging())
				studyDataTypes.add(new JsonPrimitive("imaging"));
			studyJson.add("dataType", studyDataTypes);

			// -- Studies
			Set<StudySite> studySites = study.getStudySiteSet();
			JsonArray studySitesJson = new JsonArray();
			for (StudySite studySite : studySites) {
				JsonObject studySiteJson = new JsonObject();
				studySiteJson.add("institute", valueOrEmpty(studySite.getSiteName()));
				studySiteJson.add("location", getLocation(addressToString(studySite)));
				studySiteJson.add("primary", new JsonPrimitive(studySite.isPrimary()));

				studySitesJson.add(studySiteJson);
			}
			studyJson.add("sites", studySitesJson);

			// -- PIs
			JsonArray piListJson = new JsonArray();
			for (ResearchManagement rmg : study.getResearchMgmtSet()) {
				JsonObject pi = new JsonObject();
				pi.add("name", valueOrEmpty(rmg.getFullName()));
				pi.add("role", valueOrEmpty(String.valueOf(rmg.getRole().getName())));
				if (rmg.getPictureFile() != null) {
					pi.add("image", new JsonPrimitive(domain + "portal/ws/public/study/researcher/"
							+ study.getId().toString() + "/" + rmg.getId().toString() + "/image"));
				} else {
					pi.add("image", new JsonPrimitive(domain + "sites/default/files/genericImage.jpg"));
				}
				pi.add("institute", valueOrEmpty(rmg.getOrgName()));
				piListJson.add(pi);
			}
			studyJson.add("PIs", piListJson);

			// -- Study instance data
			JsonObject studyData = new JsonObject();
			String[] studyCharts = { "ageRangeStudy", "genderTypStudy", "imgModalityTypStudy" };
			JsonObject studySummaryData = this.performSummaryDataQuery(studyCharts, study.getId().toString());
			studyData.add("ageRange", studySummaryData.get("ageRangeStudy"));
			studyData.add("genderTyp", studySummaryData.get("genderTypStudy"));
			studyData.add("imgModalityTyp", studySummaryData.get("imgModalityTypStudy"));
			studyData.add("dateUpdated", studySummaryData.get("dateUpdated"));
			JsonArray forms = new JsonArray();
			for (StudyForm form : study.getStudyForms()) {
				forms.add(valueOrEmpty(form.getTitle()));
			}
			studyData.add("forms", forms);
			studyJson.add("data", studyData);

			studiesJson.add(studyJson);
			System.out.println("Completed build study data for study " + visualizationStudy.getStudy().getTitle()
					+ " ( " + studyCount + " of " + visualizationStudies.size() + ")");
			studyCount++;
		}
		output.add("children", studiesJson);

		Gson gsonOutput = new GsonBuilder().setPrettyPrinting().create();
		return gsonOutput.toJson(output);
	}

	public JsonObject buildDataAccessData() {
		JsonObject output = new JsonObject();
		JsonArray usersList = new JsonArray();

		List<VisualizationAccessData> recordList = repositoryManager.getAllAccessRecordsPerUser();
		
		logStream.println("Advanced Visualization Build Data Access Data start"+ ":" + BRICSTimeDateUtil.getCurrentReadableTimeString());

		ArrayList<String> stationsUsers = new ArrayList<String>();
		int arNumber = 1;
		for (VisualizationAccessData ar : recordList) {

			JsonObject userListJson = new JsonObject();
			Integer arAccountId = ar.getAccountId();
			Integer arDatasetId = ar.getDatasetId();

			logger.debug("Access record Ar" + arAccountId + " " + arDatasetId+ ":" + BRICSTimeDateUtil.getCurrentReadableTimeString());

			userListJson.add("userName", valueOrEmpty(ar.getUserName()));
			userListJson.add("displayName", valueOrEmpty(ar.getDisplayName()));

			HashMap<Long, PermissionType> entityStudies = accountManager.visualizationListUserAccessMap(
					arAccountId.longValue(), EntityType.STUDY, PermissionType.WRITE, true);
			logger.debug("Access Map finished " + entityStudies+ ":" + BRICSTimeDateUtil.getCurrentReadableTimeString());
			JsonArray entityTypesJson = new JsonArray();
			for (Entry<Long, PermissionType> entry : entityStudies.entrySet()) {
				JsonObject entityTypeJson = new JsonObject();
				entityTypeJson.add("studyId", valueOrEmpty(entry.getKey().toString()));
				entityTypeJson.add("permission", valueOrEmpty(entry.getValue().name()));
				entityTypesJson.add(entityTypeJson);
			}

			boolean external = false;
			if (entityTypesJson.size() > 0 && entityTypesJson != null) {
				external = true;
			}
			userListJson.addProperty("external", external);
			userListJson.add("userStudies", entityTypesJson);

			// we don't want to get duplicate users
			String stationUser = userListJson.get("userName").toString();
			if (!(stationsUsers.contains(stationUser))) {
				stationsUsers.add(stationUser);
				usersList.add(userListJson);
			} else {
				// we don't want to do anything in this case

			}

			BigDecimal count = repositoryManager.getTotalDatasetFilesSize(arDatasetId.longValue());

			logger.debug("getTotalDatasetFilesSize finished " + count+ ":" + BRICSTimeDateUtil.getCurrentReadableTimeString());

			double totalFileSizeCount = count.doubleValue();

			JsonObject studyJson = new JsonObject();
			studyJson.add("studyId", valueOrEmpty(ar.getStudyId().toString()));
			studyJson.add("title", valueOrEmpty(ar.getStudyTitle()));
			JsonArray submissionTypesJson = new JsonArray();

			HashSet<SubmissionType> SubmissionTypes = repositoryManager
					.getDatasetWithSubmissionTypes(arDatasetId.longValue());

			logger.debug("SubmissionTypes finished " + SubmissionTypes+ ":" + BRICSTimeDateUtil.getCurrentReadableTimeString());

			for (SubmissionType st : SubmissionTypes) {
				submissionTypesJson.add(new JsonPrimitive(st.name().toLowerCase()));
			}
			studyJson.add("dataType", submissionTypesJson);
			studyJson.add("downloads", new JsonObject());
			studyJson.add("studyDownloads", new JsonObject());
			if (!output.has(ar.getStudyId().toString())) {
				output.add(ar.getStudyId().toString(), studyJson);
			}

			String key = null;
			StringBuilder sb = new StringBuilder();
			if (submissionTypesJson.size() == 1) {
				key = submissionTypesJson.get(0).toString();
			} else {
				// I dunno if there is a method within JsonArray where we can just join all
				// elements
				// as far as i know it doesn't exist so i had to do this :(
				for (int i = 0; i < submissionTypesJson.size(); i++) {
					sb.append(submissionTypesJson.get(i));

					// if not the last item
					if (i != submissionTypesJson.size() - 1) {
						sb.append(",");
					}
				}

				key = sb.toString();
			}

			JsonObject obj = new JsonObject();
			
			//JsonObject studyobj = new JsonObject();
			JsonObject jsonChildObject = output.getAsJsonObject(ar.getStudyId().toString());

			int splitCount = entityTypesJson.size();
			int userIndex = stationsUsers.indexOf(stationUser);

			double totalCount = 0;
			double studyAmount = 0;

			for (Map.Entry<String, JsonElement> entry : jsonChildObject.entrySet()) {
				// for a particular user, what types did they download.
				if (entry.getKey().equals("downloads")) {
					JsonObject downloadJson =  entry.getValue().getAsJsonObject();
					if (key != null) {
						if (!entry.getValue().getAsJsonObject().has(Integer.toString(userIndex))) {
							obj.addProperty(key.replaceAll("\"", ""), totalFileSizeCount);
							downloadJson.add(Integer.toString(userIndex), obj);

						} else {
							for (Map.Entry<String, JsonElement> subentry : entry.getValue().getAsJsonObject()
									.getAsJsonObject(Integer.toString(userIndex)).entrySet()) {
								if (subentry.getKey().equalsIgnoreCase(key.replaceAll("\"", ""))) {
									totalCount = totalFileSizeCount + subentry.getValue().getAsDouble();
									obj.addProperty(key.replaceAll("\"", ""), totalCount);

								} else {

									obj.addProperty(key.replaceAll("\"", ""), totalFileSizeCount);

								}

							}
							
							downloadJson.add(Integer.toString(userIndex), obj);

						}
						
						studyJson.add("downloads", downloadJson);
						output.add(ar.getStudyId().toString(), studyJson);
					}
				}
				// second aggregation - how much data studies download from each other.
				// a user is associated with one or more studies - split data between the
				// studies
				// users with no studies already accounted for in .downloads above.
				if (entry.getKey().equals("studyDownloads")) {
					JsonObject studyobj = entry.getValue().getAsJsonObject();
					if (entityTypesJson.size() > 0) {
						for (int i = 0; i < entityTypesJson.size(); i++) {
							String entityVal = entityTypesJson.get(i).getAsJsonObject().get("studyId").toString();
							if (entry.getValue().getAsJsonObject().has(entityVal.replaceAll("\"", ""))) {
								studyAmount = studyobj.get(entityVal.replaceAll("\"", "")).getAsDouble();
								studyAmount += totalFileSizeCount / splitCount;
							} else {
								studyAmount = totalFileSizeCount / splitCount;
							}
							
							studyobj.addProperty(entityTypesJson.get(i).getAsJsonObject().get("studyId").toString()
									.replaceAll("\"", ""), studyAmount);
							
							
						}
						
						
					}
					studyJson.add("studyDownloads", studyobj);
					output.add(ar.getStudyId().toString(), studyJson);

				}

			}

			if (logger.isDebugEnabled()) {
				logger.debug(
						"completed record object " + ar.getId() + "(" + arNumber + " of " + recordList.size() + ")" + ":" + BRICSTimeDateUtil.getCurrentReadableTimeString());
			}
			
			if(arNumber % 10 == 0) {
				logStream.println("completed record object " + ar.getId() + "(" + arNumber + " of " + recordList.size() + ")" + ":" + BRICSTimeDateUtil.getCurrentReadableTimeString());
			}
			arNumber++;
		}

		output.add("userList", usersList);
		return output;
	}

	public String buildTestDataAccessData() {
		JsonObject output = new JsonObject();

		List<VisualizationAccessRecord> recordList = repositoryManager.getAccessRecordsPerUser();
		JsonArray recordListJson = new JsonArray();
		int arNumber = 1;
		for (VisualizationAccessRecord ar : recordList) {
			JsonObject arJson = new JsonObject();

			Account arAccount = ar.getAccount();
			Dataset arDataset = ar.getDataset();

			arJson.add("userName", valueOrEmpty(arAccount.getUserName()));
			arJson.add("displayName", valueOrEmpty(arAccount.getDisplayName()));

			HashMap<Long, PermissionType> entityStudies = accountManager.listUserAccessMap(arAccount, EntityType.STUDY,
					PermissionType.WRITE, true);
			JsonArray entityTypesJson = new JsonArray();
			for (Entry<Long, PermissionType> entry : entityStudies.entrySet()) {
				// System.out.println("Key = " + entry.getKey() + ", Value = " +
				// entry.getValue());
				JsonObject entityTypeJson = new JsonObject();
				entityTypeJson.add("studyId", valueOrEmpty(entry.getKey().toString()));
				entityTypeJson.add("permission", valueOrEmpty(entry.getValue().name()));
				entityTypesJson.add(entityTypeJson);
			}
			arJson.add("userStudies", entityTypesJson);

			double totalFileSizeCount = 0;

			List<DatasetFile> dsFiles = repositoryManager.getDatasetFiles(arDataset);
			for (DatasetFile dsf : dsFiles) {
				totalFileSizeCount += dsf.getUserFile().getSize();
			}

			arJson.addProperty("amount", totalFileSizeCount);
			JsonObject arStudyJson = new JsonObject();
			arStudyJson.add("studyId", valueOrEmpty(arDataset.getStudy().getId().toString()));
			arStudyJson.add("title", valueOrEmpty(arDataset.getStudy().getTitle()));
			JsonArray submissionTypesJson = new JsonArray();
			for (SubmissionType st : arDataset.getSubmissionTypes()) {
				submissionTypesJson.add(new JsonPrimitive(st.name().toLowerCase()));
			}

			arStudyJson.add("dataType", submissionTypesJson);
			arJson.add("study", arStudyJson);
			recordListJson.add(arJson);

			if (logger.isDebugEnabled()) {
				logger.debug(
						"completed record object " + ar.getId() + "(" + arNumber + " of " + recordList.size() + ")");
			}

			arNumber++;
		}

		output.add("accessData", recordListJson);

		Gson gsonOutput = new GsonBuilder().setPrettyPrinting().create();
		return gsonOutput.toJson(output);
	}

	public Map<Long, Set<SubmissionType>> getSubmissionTypes(Map<Long, VisualizationStudy> vStudies) {
		Set<Long> ids = new HashSet<Long>();
		for (Entry<Long, VisualizationStudy> s : vStudies.entrySet()) {
			ids.add(s.getKey());
		}
		logger.info("The getSubmissionTypes is done: ");
		return repositoryManager.getStudySubmissionTypes(ids);
	}

	public Map<Long, VisualizationStudy> getAllStudiesVisualization() {
		/// get all available studies from postgres
		List<Study> studies = visualizationStudyDao.getAllVisualization();
		// get all studies with data from virtuoso
		Map<Long, VisualizationStudy> tempVisualizationStudies = visualizationStudyDao.getAllVisualizationStudyData();
		//There might be a scenario where the study may not exist in tempVisualizationStudies, but exists in studies in that case don't want to send that over.
		Map<Long, VisualizationStudy> visualizationStudies = new HashMap<Long, VisualizationStudy>();
		
		for (Study s : studies) {
			VisualizationStudy vs = tempVisualizationStudies.get(s.getId());
			if (vs != null) {
				vs.setStudy(s);
				visualizationStudies.put(s.getId(), vs);
			} else {
				VisualizationStudy newvs = new VisualizationStudy();
				newvs.setStudy(s);
				visualizationStudies.put(s.getId(), newvs);
			}
			
		}
		logger.info("The amount of visualization studies is: " + studies.size());

		return visualizationStudies;
	}

	/**
	 * Calculates the total dataset upload file size for the study.
	 * 
	 * @param study the study to analyze
	 * @return Long total file size
	 */
	private Long generateStudyDataSize(List<Dataset> datasetset) {
		// logger.info("Calculating upload file size for study " + study.getTitle() +
		// "...");
		Long studyTotalFileSize = 0L;
		Iterator<Dataset> datasetIterator = datasetset.iterator();
		while (datasetIterator.hasNext()) {
			Dataset dataset = datasetIterator.next();

			Long datasetTotalFileSize = 0L;
			Iterator<DatasetFile> filesetIterator = dataset.getDatasetFileSet().iterator();
			while (filesetIterator.hasNext()) {
				DatasetFile datasetFile = filesetIterator.next();
				UserFile file = datasetFile.getUserFile();
				datasetTotalFileSize += file.getSize();
			}

			studyTotalFileSize += datasetTotalFileSize;
		}
		// logger.info("finished calculating upload file size for study " +
		// study.getTitle());
		return studyTotalFileSize;
	}

	/**
	 * Provide a list of cities (array or file or whatever) If that city has not
	 * been located already (keep track of that in calling method) Search for the
	 * city in places.json (I have reformatted it for better search) If not found in
	 * places.json call Google Maps API to get location
	 * 
	 * If still not found (or error), use the site name.
	 * 
	 * @param StudySite site the site upon which to generate the address string
	 * @return String address string to use in google api
	 */
	private String addressToString(StudySite site) {
		Address address = site.getAddress();
		String output = "";
		// if city doesn't exist, I have no clue where we are
		if (address == null || address.getCity() == null || address.getCity().equals("")) {
			return site.getSiteName();
		}

		output += address.getCity();
		if (address.getState() != null && !address.getState().getName().equals("")) {
			output += ", " + address.getState().getCode();
		}

		if (address.getCountry() != null && !address.getCountry().getName().equals("")) {
			output += ", " + address.getCountry().getName();
		}

		return output;
	}

	/**
	 * Attempts to find the geo location (in coordinates) of a city given the city
	 * name (and possibly its state or country). First checks a local file then
	 * checks google maps API.
	 * 
	 * @param address the city name and any other usable data into an address format
	 *                (example: Atlanta, GA)
	 * @return
	 */
	public JsonObject getLocation(String address) {
		JsonParser parser = new JsonParser();
		JsonObject placesJs = new JsonObject();
		JsonObject output = null;
		String encodedAddress = address;
		try {
			encodedAddress = URLEncoder.encode(address, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should never have this, but...
			e.printStackTrace();
		}

		if (address.equals("")) {
			return output;
		}

		try {
			if (locations == null) {
				// TODO: change location to use actual file
				File placesFile = new File(
						"C:/Users/jpark1/Desktop/temp/translate_mapTree_places/places_translated.json");
				if (placesFile.exists()) {
					locations = new FileReader(placesFile);
					placesJs = (JsonObject) parser.parse(locations);
				} else {
					placesJs = new JsonObject();
				}
			}
			if (placesJs.has(address)) {
				output = (JsonObject) placesJs.get(address);
			}
		} catch (FileNotFoundException e) {
			// no big deal, just won't be able to use the file
			logger.error(
					"The location service was unable to load the local location file.  Falling back to using the Google Maps API");
		}

		if (output == null) {
			// if city not found in placesJs, look up in Google Maps API
			String apiKey = "AIzaSyDIeDd4SnFvsefA7GN-6xFbNA0HH-VS0aU";
			String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + encodedAddress + "&key="
					+ apiKey;

			GetMethod getMethod = new GetMethod(url);
			HttpClient client = new HttpClient();
			try {
				int responseCode = client.executeMethod(getMethod);
				if (responseCode == 200) {
					InputStream inputStream = getMethod.getResponseBodyAsStream();

					String response = null;
					byte[] buffer = null;
					try {
						buffer = IOUtils.toByteArray(inputStream);
						response = new String(buffer);
					}finally {
						inputStream.close();
					}
					
					try {
						JsonObject responseJson = parser.parse(response).getAsJsonObject();
						// get the location from the response JSON

						JsonArray resultsArray = (JsonArray) responseJson.get("results");
						if (resultsArray.size() > 0) {
							JsonObject resultObject = (JsonObject) resultsArray.get(0);
							JsonObject locationObject = (JsonObject) ((JsonObject) resultObject.get("geometry"))
									.get("location");
							locationObject.add("place", valueOrEmpty(address));
							output = locationObject;
						} else {
							output = new JsonObject();
						}
					} catch (JsonSyntaxException jse) {
						// failed to convert to json object. perhaps the response isn't json
						logger.error("The location service failed to obtain a JSON response from the Google Maps API");
					}
				} else {
					// response was something other than 200
					logger.error(
							"The location service received response " + responseCode + " from the Google Maps API");
				}
			} catch (HttpException e) {
				logger.error("The location service encountered an HttpException while calling the Google Maps API");
				e.printStackTrace();
			} catch (IOException e) {
				logger.error("The location service encountered an IOException while calling the Google Maps API");
				e.printStackTrace();
			} finally {
				getMethod.releaseConnection();
			}
		}
		return output;
	}

	public String generateStudyUrl(Study study) {
		if (study.getStudyUrl() != null && !study.getStudyUrl().equals("")) {
			return study.getStudyUrl();
		} else {
			return "";
		}
	}

	/**
	 * Performs a request to get summary data for a set of graphs for the Program
	 * level.
	 * 
	 */
	public JsonObject getProgramSummaryGraphs() {
		logger.debug("begin getting program summary graphs");

		orgName = modulesConstants.getModulesOrgName();
		List<SummaryQuery> summaryQueries = summaryQueryDao.getProgramSummaryQueriesShortnames(orgName);
		// create list of chartNames
		List<String> chartNames = new ArrayList<String>();
		for (SummaryQuery s : summaryQueries) {
			chartNames.add(s.getShortname());
		}
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager, proformsWsProvider, modulesConstants);
		JsonObject jsonOutput = null;
		try {
			jsonOutput = sdCache.getProgramMultiple(chartNames);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WebApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonOutput;

	}

	/**
	 * Performs a request to get summary data graph names for a particular Study.
	 * This is the method that performs the work so it can accept a studyName . This
	 * is used in conjuction wtih getStudySummaryGraphs(). We don't want to call the
	 * database multiple times for this list.
	 * 
	 * 
	 */
	public List<String> getStudySummaryGraphNames() {
		logger.debug("begin getting program summary graphs names");

		orgName = modulesConstants.getModulesOrgName();
		List<SummaryQuery> summaryQueries = summaryQueryDao.getStudySummaryQueriesShortnames(orgName);
		// create list of chartNames
		List<String> chartNames = new ArrayList<String>();
		for (SummaryQuery s : summaryQueries) {
			chartNames.add(s.getShortname());
		}
		return chartNames;
	}

	/**
	 * Performs a request to get summary data graph names for a particular Study.
	 * This is the method that performs the work so it can accept a studyName .
	 * 
	 * 
	 * @param studyName the study name to filter by OR null to use program-level
	 *                  queries
	 */

	private JsonObject getStudySummaryGraphs(List<String> chartNames, String studyId) {
		logger.debug("begin getting program summary graphs");
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager, proformsWsProvider, modulesConstants);
		JsonObject jsonOutput = new JsonObject();
		if (StringUtils.isNumeric(studyId)) {
			Long sId = Long.decode(studyId);
			jsonOutput = sdCache.getStudyMultiple(chartNames, sId);
		} else {
			jsonOutput = sdCache.getStudyMultiple(chartNames, studyId);
		}
		
		
		jsonOutput.addProperty("dateUpdated", BRICSTimeDateUtil.formatDate(sdCache.getUpdateDate()));

		return jsonOutput;

	}

	/**
	 * Performs a request to get summary data for a particular Study. This is the
	 * method that performs the work so it can accept a studyName of null to use the
	 * program-level query.
	 * 
	 * @param chartName[] the charts to request
	 * @param studyName   the study name to filter by OR null to use program-level
	 *                    queries
	 */
	private JsonObject performSummaryDataQuery(String[] chartNames, String studyName) {
		String portalRootUrl = modulesConstants.getModulesPublicURL() + modulesConstants.getModulesPortalRoot();
		String chartNameQuery = "";
		for (int i = 0; i < chartNames.length; i++) {
			if (i != 0) {
				chartNameQuery += "&";
			}
			chartNameQuery += "chartName=" + chartNames[i];
		}
		String requestUrl = portalRootUrl + "/ws/summaryData/program?" + chartNameQuery;

		if (studyName != null) {
			String encodedStudyName = studyName;
			try {
				encodedStudyName = URLEncoder.encode(studyName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// do nothing, we have already fallen back to the strict studyName
			}
			requestUrl = portalRootUrl + "/ws/summaryData/study/" + encodedStudyName + "?" + chartNameQuery;
		}

		WebClient client = WebClient.create(requestUrl);
		WebClient.getConfig(client).getHttpConduit().getClient().setReceiveTimeout(6000000);
		String response = "[]";

		try {
			logger.debug("Request URL: " + requestUrl);
			response = client.accept("application/json").get(String.class);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error retrieving summary data for study " + studyName);
		}
		if (client.getResponse().getStatus() != 200) {
			return new JsonObject();
		} else {
			JsonParser parser = new JsonParser();
			JsonObject output = parser.parse(response).getAsJsonObject();
			return output;
		}
	}

	/**
	 * Performs a test and writes out a JsonPrimitive for the string value passed
	 * in. If the value is null, the JsonPrimitive is given an empty string
	 */
	public JsonPrimitive valueOrEmpty(String value) {
		return valueOrEmpty(value, "");
	}

	/**
	 * Performs a test and writes out a JsonPrimitive for the string value passed
	 * in. If the value is null, the JsonPrimitive is given an empty string.
	 * 
	 * Provides a way to fill in an alternative value
	 */
	public JsonPrimitive valueOrEmpty(String value, String alternative) {
		if (value == null) {
			value = alternative;
		}
		return new JsonPrimitive(value);
	}

	/**
	 * Performs a webservice request to get count summary data for a particular
	 * Study. This is the method that performs the work so it can accept a studyId.
	 * 
	 * @param studyId the study name to filter
	 */
	private JsonObject performCountSummaryQuery(String studyId) {
		String portalRootUrl = modulesConstants.getModulesPublicURL() + modulesConstants.getModulesPortalRoot();

		String requestUrl = portalRootUrl + "/ws/summaryData/countSummary/" + studyId;

		WebClient client = WebClient.create(requestUrl);
		WebClient.getConfig(client).getHttpConduit().getClient().setReceiveTimeout(180000);

		String response = "[]";

		try {
			logger.debug("Request URL: " + requestUrl);
			response = client.accept("application/json").get(String.class);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error retrieving count summary data for study ID" + studyId);
		}
		if (client.getResponse().getStatus() != 200) {
			return new JsonObject();
		} else {
			JsonParser parser = new JsonParser();
			JsonObject output = parser.parse(response).getAsJsonObject();
			return output;
		}
	}

	/**
	 * This methods works to get counts summary data for a particular Study. This is
	 * the method that performs the work so it can accept a studyId.
	 * 
	 * @param studyId the study name to filter
	 */
	public JsonObject performStudyCountSummaryQuery(String studyId) {

		logger.debug("getCountSummaryData with study ID " + studyId);
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
				Date lastSubmitDate = new SimpleDateFormat(ServiceConstants.UNIVERSAL_FORMAT_DATE).parse(lastSubmitDateStr);
				lastSubmitDateFormated = new SimpleDateFormat(ServiceConstants.DEFAULT_FORMAT_DATE).format(lastSubmitDate);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jsonOutput.addProperty("lastSubmitDate", lastSubmitDateFormated);

		return jsonOutput;
	}
	
	
	/**
	 * Send an email to infrastructure to inform if advanced visualization fails
	 */
	private void sendFailureEmail(File logFile) {
		// here, we are reading the log file and adding it into the email to be sent
		// to infrastructure.
		String LogContent = null;

		try {
			LogContent = BRICSFilesUtils.readFile(logFile, StandardCharsets.UTF_8);
			LogContent = LogContent.replace("\n", "<br>");
		} catch (IOException e) {
			LogContent = "Error occurred while trying to read rdfgen.log content to email";
			logger.error(LogContent, e);
		} finally {
			// we still want to send the email even if reading the log file caused an error
			String emailAddress = modulesConstants.getModulesInfraEmail();
			String mailSubject = messageSource.getMessage(ServiceConstants.SUMMARY_DATA_GEN_FAILED_SUBJECT, null, null);

			Object[] bodyArgs = new Object[] {modulesConstants.getModulesAccountURL(), LogContent};
			String mailBody = messageSource.getMessage(ServiceConstants.SUMMARY_DATA_GEN_FAILED_BODY, bodyArgs, null);

			try {
				mailEngine.sendMail(mailSubject, mailBody, null, emailAddress);
			} catch (MessagingException e) {
				logger.error("Error occured while trying to email infrastructure about failed Summary Data and Visulzation generation", e);
			}
		}
	}
	
	
	private void handleFailureNotification(boolean hasError, File logFile) {
		String logEndString = null;
		if (hasError) { 
			logger.info("Error occurred while running Summary Data Generator, sending failure email to infrastructure...");
			logEndString = String.format(LOG_FAIL_FORMAT, BRICSTimeDateUtil.getCurrentReadableTimeString());
			sendFailureEmail(logFile);
			logStream.println(logEndString);
			logStream.close();
		} 
		
		

	}
	
	private File initializeLogPrinter() {
		String filePath = ServiceConstants.SUMMARYDATA_LOG_PATH;
		File logFile = new File(filePath);

		try {
			if (!logFile.exists()) {
				if (logFile.getParentFile() != null) {
					logFile.getParentFile().mkdirs();
				}
				logFile.createNewFile();
			}

			logStream = new PrintStream(logFile);
			String logStartString = String.format(LOG_START_FORMAT, BRICSTimeDateUtil.getCurrentReadableTimeString());
			logStream.println(logStartString);
			return logFile;
		} catch (FileNotFoundException e) {
			logger.error("Could not find the log file", e);
		} catch (IOException e) {
			logger.error("Error occurred when attempting to create log file", e);
		}

		return null;
	}
	
	private void emptyCache() {
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager, proformsWsProvider, modulesConstants);
		try {
			sdCache.emptyCache(queryToolManager, proformsWsProvider, modulesConstants);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new InternalServerErrorException(e);
		}
		logger.info("SummaryData cache cleared by Advanced Visualization." + " : " + BRICSTimeDateUtil.getCurrentReadableTimeString());
	}
}
