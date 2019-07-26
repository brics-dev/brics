package gov.nih.tbi.repository.service.hibernate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.jcraft.jsch.JSchException;

import gov.nih.tbi.repository.dao.DatafileEndpointInfoDao;
import gov.nih.tbi.repository.dao.DatasetDao;
import gov.nih.tbi.repository.dao.VisualizationStudyDao;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudyForm;
import gov.nih.tbi.repository.model.hibernate.StudySite;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.model.hibernate.VisualizationAccessRecord;
import gov.nih.tbi.repository.model.hibernate.VisualizationStudy;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.hibernate.Address;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.AdvancedVisualizationManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.repository.service.io.ExecClient;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
	
	@Autowired
    protected RepositoryManager repositoryManager;
	
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
			String dataAccessData = buildDataAccessData();
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
				}
				catch(Exception e) {
					logger.error("unable to copy file to updated name (to fitbir.json)");
					e.printStackTrace();
					// unable to copy file to updated name
				}
			}
			catch(Exception e) {
				logger.error("unable to copy file to server");
				e.printStackTrace();
				// unable to copy file to server
			}
		}
		catch(Exception e) {
			logger.error("major process failure");
			e.printStackTrace();
			// full process failure
		}
		
		logger.info("Advanced Visualization Build Service Complete");
		//return Response.ok().build();
	}
	
	/**
	 * Appends information about today's uploaded files to the history file for this year.  If
	 * this year's history file does not yet exist, the file is created and a header added to
	 * make the output pseudo-JSON (it is not quite JSON because it doesn't have the tailing
	 * closing brace.  That must be added by Alexandra in the viewer code.  We did that so we
	 * can just append here instead of removing then adding).
	 *   
	 * @param metaFileName file name of the "fitbir.json" file holding file metadata
	 * @param dataAccessFileName file name of the data access file
	 * @param studyIdsFileName file name of the compiled studyIds file
	 * @throws Exception on any major error
	 */
	private void updateHistoryFile(String metaFileName, String destinationPath, String dataAccessFileName, String studyIdsFileName) throws Exception {
		/*
		 * This process appends the file names passed in to the history file for this year.  Each year
		 * gets its own file so we may have to create the file if this year's file doesn't exist.
		 * ---------------
		 * create string to append
		 * check that this year's file exists
		 * if year's file does not exists
		 * 	create file with array prefix
		 * append today's object to the file
		 */
		logger.info("Updating History File");
		try {
			DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DRUPAL_DATAFILE_ENDPOINT_ID);
			ExecClient execClient = new ExecClient(info.getUserName(), info.getPassword(), info.getUrl(), info.getPort());
			Date todaysDate = new Date();
			String thisYear = (new SimpleDateFormat("yyyy")).format(todaysDate);
			String thisYearHistoryFilename = historyFileName + "_" + thisYear + ".json";
			String today = (new SimpleDateFormat("MM_dd")).format(todaysDate);
			
			String content = "\"" + today + "\": {"
					+ "\"meta\": \"" + metaFileName + "\","
					+ "\"dataaccess\": \"" + dataAccessFileName + "\","
					+ "\"studyIds\": \"" + studyIdsFileName + "\"}";
			
			/*
			 * TODO: We need to create this function to accurrately test existance of file on serve.
			 * 
			 * boolean doesFileExist = execClient.doesFileExist(destinationPath + thisYearHistoryFilename);
			if (!doesFileExist) {
				logger.info("History file for " + thisYear + " does not exist.  It will be created");
				String header = "{";
				execClient.appendToFile(destinationPath + thisYearHistoryFilename, header);
			}
			else {
				// if the file exists, it already has one entry
				content = "," + content;
			}
			*/
			
			content = "," + content;
			// we don't have to have an explicit create command because appendToFile
			// uses the >> format which creates the file if it doesn't exist
			execClient.appendToFile(destinationPath + "/" + thisYearHistoryFilename, content);
			execClient.disconnectSession();
		}
		catch(JSchException e) {
			logger.error("failure in updating history file");
			e.printStackTrace();
			throw new Exception("Failure in updating history file", e);
		}
		catch(Exception e) {
			logger.error("general failure in updating history file");
			e.printStackTrace();
			throw new Exception("General failure in updating history file", e);
		}
	}
	/**
	 * Runs alexandra's command in Node to produce her studyIds.json file based on our dataAccess file.
	 * 
	 * @param destinationPath folder where the process happens
	 * @param nodeScriptFileName script that should be run.  Exists inside the destinationPath
	 * @param dataAccessFileName data file that should be processed.  Exists inside the destinationPath
	 * @throws Exception when unable to create the client
	 */
	private void runNodeProcess(String destinationPath, String nodeScriptFileName, String dataAccessFileName) throws Exception {
		// node process is:
		// node <script_filename> <data_filename>
		// it outputs studyIds.json
		// that should be copied to studyIds_<date>.json as we do
		try {
			DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_PUBLIC_DRUPAL_DATAFILE_ENDPOINT_ID);
			ExecClient execClient = new ExecClient(info.getUserName(), info.getPassword(), info.getUrl(), info.getPort());
			String scriptPath = destinationPath + nodeScriptFileName;
			String dataFilePath = destinationPath  + dataAccessFileName;
			execClient.runNodeCommand(destinationPath, scriptPath + " " + dataFilePath);
			execClient.disconnectSession();
		}
		catch(JSchException e) {
			logger.error("");
			e.printStackTrace();
			throw new Exception("Failure in file processing", e);
		}
		catch(Exception e) {
			logger.error("The Advanced Viewer file could not be saved before transfer");
			throw new Exception("Failure in file processing", e);
		}
	}
	
	private void copyFileOnRemote(String folderPath, String sourceFileName, String destinationFileName) throws Exception {
		try {
			DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DRUPAL_DATAFILE_ENDPOINT_ID);
			ExecClient execClient = new ExecClient(info.getUserName(), info.getPassword(), info.getUrl(), info.getPort());
			String sourcePath = folderPath + sourceFileName;
			String destinationPath = folderPath + destinationFileName;
			
			execClient.copyOnRemote(sourcePath, destinationPath);
			execClient.disconnectSession();
		}
		catch(JSchException e) {
			logger.error("");
			e.printStackTrace();
			throw new Exception("Failure in file processing", e);
		}
		catch(Exception e) {
			logger.error("The Advanced Viewer file could not be saved before transfer");
			throw new Exception("Failure in file processing", e);
		}
		// no need to close.  execClient handles that for us
		
	}
	
	
	private void writeFileToPublic(String fileName, String data) throws Exception {
		try {
			String destinationPath = ServiceConstants.ADVANCED_VIEWER_UPLOAD_PATH;
			
			initializeSftpClient();
			FileUtils.writeStringToFile(new File("/tmp/" + fileName), data);
			File reopenedFile = new File("/tmp/" + fileName);
			sftpClient.upload(reopenedFile, destinationPath, fileName);
		}
		catch(JSchException e) {
			logger.error("SFTP client could not be initialized");
			e.printStackTrace();
			throw new Exception("Failure in file processing", e);
		}
		catch(IOException e) {
			logger.error("Could not write Advanced Viewer file");
			e.printStackTrace();
			throw new Exception("Failure in file processing", e);
		}
		catch(Exception e) {
			logger.error("The Advanced Viewer file could not be saved before transfer");
			throw new Exception("Failure in file processing", e);
		}
		finally {
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
	
	public String buildStudyData() {
		JsonObject output = new JsonObject();
		output.add("name", new JsonPrimitive("FITBIR"));
		output.add("link", new JsonPrimitive("https://fitbir.nih.gov"));
		
		JsonObject data = new JsonObject();
		
		// fill in from the summary data: protocol level ageRange, genderTyp, imgModalityTyp
		// we probably will have to populate the "type" parameter here to tell what type of
		// chart to show
		String[] charts = new String[] {
				"ageRange", 
				"genderTyp", 
				"imgModalityTyp",
				"eduYrCt",
				"injuryCause",
				"gcsTotalScore",
				"pGOSEScore",
				"tBITyp"};
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
		// and the URL for that is https://pdbp.ninds.nih.gov/portal/ws/summaryData/program?chartName=sampCollTyp
		
		// fill in with site at first-level and study at second
		
		// for each study
		// 	get site name and city
		// 	if site name and city match an entry that already exists, 
		//		add the new study to that site
		//	else
		//		add a new site to the siteToStudyMap and add the study to that entry
		//		Studies must include PI listings
		// 	
		//  study-level summary data must also be included in the study data ("data" sub - same format as summary data)
		
		
		List<VisualizationStudy> visualizationStudies = getAllStudiesVisualization();
		Map<Long, Set<SubmissionType>> studySubmissionTypes = getSubmissionTypes(visualizationStudies);
		
		JsonArray studiesJson = new JsonArray();
		// changing the data structure to be study-centric
		int studyCount = 1;
		for (VisualizationStudy visualizatoinStudy : visualizationStudies) {
			Study study = visualizatoinStudy.getStudy();
			String dataSharing = study.getStudyStatus().getName();
			
			if (dataSharing == null 
					|| dataSharing.equals("") 
					|| dataSharing.equals("Rejected") 
					|| dataSharing.equals("Requested")) {
				continue;
			}
			
			JsonObject studyJson = new JsonObject();
			studyJson.add("name", valueOrEmpty(study.getTitle()));
			studyJson.add("id", valueOrEmpty(study.getId().toString()));
			if (study.getFundingSource() != null) {
				studyJson.add("funding", valueOrEmpty(study.getFundingSource().getName()));
			}
			else {
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
			// for study in "requested" or "rejected" state, do not include the study in the data!
			// shared = Public
			// private = Private
			// no data = <does not have data>
			
			List<Dataset> studyDatasetSet =  datasetDao.getDatasetsByStudy(study);
			if (studyDatasetSet.isEmpty()) {
				dataSharing = "no data";
			}
			else {
				dataSharing = "private";
				for (Dataset studySite : studyDatasetSet) {
						if(studySite.getDatasetStatus().getName().equals("Shared")) {
 							dataSharing = "shared";
							break;
						} else if(studySite.getDatasetStatus().getName().equals("Private")) {
							dataSharing = "private";
							//Probably don't need this.
						}
					
				}
				
			}
			
			
			studyJson.add("totalDataSize", new JsonPrimitive(visualizatoinStudy.getTotalDataFileSize()));

			studyJson.add("dataSharing", valueOrEmpty(dataSharing));

			String studyUrl = generateStudyUrl(study);
			if (studyUrl != null && studyUrl != "" && !studyUrl.startsWith("http")) {
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
			// (because there's no actual data in the study), and doesn't duplicate code really.
			// So, I'm happy about that - but I still hate Hibernate doing stuff like this sometimes.
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
				}
				else {
					pi.add("image", new JsonPrimitive(domain + "sites/default/files/genericImage.jpg"));
				}
				pi.add("institute", valueOrEmpty(rmg.getOrgName()));
				piListJson.add(pi);
			}
			studyJson.add("PIs", piListJson);
			
			// -- Study instance data
			JsonObject studyData = new JsonObject();
			String[] studyCharts = {"ageRangeStudy", "genderTypStudy", "imgModalityTypStudy"};
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
			System.out.println("Completed build study data for study " + visualizatoinStudy.getStudy().getTitle()
					+ " ( " + studyCount + " of " + visualizationStudies.size() + ")");
			studyCount++;
		}
		output.add("children", studiesJson);
		
		Gson gsonOutput = new GsonBuilder().setPrettyPrinting().create();
		return gsonOutput.toJson(output);
	}
	
	private String buildDataAccessData() {
		JsonObject output = new JsonObject();
		
		List<VisualizationAccessRecord> recordList =
				repositoryManager.getAccessRecordsPerUser();
		JsonArray recordListJson = new JsonArray();
		int arNumber = 1;
		for (VisualizationAccessRecord ar : recordList) {
			JsonObject arJson = new JsonObject();
			
			Account arAccount = ar.getAccount();
			Dataset arDataset = ar.getDataset();

			arJson.add("userName", valueOrEmpty(arAccount.getUserName()));
			arJson.add("displayName", valueOrEmpty(arAccount.getDisplayName()));
			
			HashMap<Long, PermissionType> entityStudies =
					accountManager.listUserAccessMap(arAccount, EntityType.STUDY, PermissionType.WRITE, true);
			JsonArray entityTypesJson = new JsonArray();
			for (Entry<Long, PermissionType> entry : entityStudies.entrySet()) {
			    //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
				JsonObject entityTypeJson = new JsonObject();
				entityTypeJson.add("studyId",  valueOrEmpty(entry.getKey().toString()));
				entityTypeJson.add("permission",  valueOrEmpty(entry.getValue().name()));
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
			
			if(logger.isDebugEnabled()) {
				logger.debug("completed record object " + ar.getId() + "(" + arNumber + " of " + recordList.size() + ")");
			}
			
			arNumber++;
		}
		
		output.add("accessData",recordListJson);
		
		
		Gson gsonOutput = new GsonBuilder().setPrettyPrinting().create();
		return gsonOutput.toJson(output);
	}
	
	private Map<Long, Set<SubmissionType>> getSubmissionTypes(List<VisualizationStudy> vStudies) {
		Set<Long> ids = new HashSet<Long>();
		for (VisualizationStudy s : vStudies) {
			ids.add(s.getStudy().getId());
		}

		return repositoryManager.getStudySubmissionTypes(ids);
	}

	public List<VisualizationStudy> getAllStudiesVisualization() {
		///combine studies that don't have data with those that do
		List<Study> studies = visualizationStudyDao.getAllVisualization();
		List<VisualizationStudy> visualizationStudies = visualizationStudyDao.getAllVisualizationStudyData();
		List<VisualizationStudy> visualizationStudiesWithNoData = new ArrayList<VisualizationStudy>();
		boolean exists;
		
		
		for (Study s : studies) {
			exists = false;
			for (int i = 0; i < visualizationStudies.size(); i++) {
			
				VisualizationStudy vs = visualizationStudies.get(i);
				// Search the account role list for the given role type ID.
				//System.out.print("Study ID: " + s.getId() + " vs Viz Study ID: " + vs.getStudyId().longValue() + " /n/r");
				if(s.getId().equals(vs.getStudyId().longValue())) {
					vs.setStudy(s);
					exists = true;
					break;
				} 
			}
			
			if(exists == false) {
				VisualizationStudy newvs = new VisualizationStudy();
				newvs.setStudy(s);
				visualizationStudiesWithNoData.add(newvs);
			}
		}
		//combine lists
		visualizationStudies.addAll(visualizationStudiesWithNoData);
		
		return visualizationStudies;
	}
	
	/**
	 * Calculates the total dataset upload file size for the study.
	 * 
	 * @param study the study to analyze
	 * @return Long total file size
	 */
	private Long generateStudyDataSize(List<Dataset> datasetset) {
		//logger.info("Calculating upload file size for study " + study.getTitle() + "...");
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
		//logger.info("finished calculating upload file size for study " + study.getTitle());
		return studyTotalFileSize;
	}
	
	
	/**
	 * Provide a list of cities (array or file or whatever) If that city has not been located already (keep track of
	 * that in calling method) Search for the city in places.json (I have reformatted it for better search) If not found
	 * in places.json call Google Maps API to get location
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
		if (address.getState() != null && !address.getState().equals("")) {
			output += ", " + address.getState().getCode();
		}
		
		if (address.getCountry() != null && !address.getCountry().getName().equals("")) {
			output += ", " + address.getCountry().getName();
		}
		
		return output;
	}
	
	/**
	 * Attempts to find the geo location (in coordinates) of a city given the city name
	 * (and possibly its state or country).  First checks a local file then checks google maps API.
	 * @param address the city name and any other usable data into an address format (example: Atlanta, GA)
	 * @return 
	 */
	public JsonObject getLocation(String address) {
		JsonParser parser = new JsonParser();
		JsonObject placesJs = new JsonObject();
		JsonObject output = null;
		String encodedAddress = address;
		try {
			encodedAddress = URLEncoder.encode(address, "UTF-8");
		}
		catch(UnsupportedEncodingException e) {
			// should never have this, but...
			e.printStackTrace();
		}
		
		if (address.equals("")) {
			return output;
		}
		
		try {
			if (locations == null) {
				// TODO: change location to use actual file
				File placesFile = new File("C:/Users/jpark1/Desktop/temp/translate_mapTree_places/places_translated.json");
				if (placesFile.exists()) {
					locations = new FileReader(placesFile);
					placesJs = (JsonObject) parser.parse(locations);
				}
				else {
					placesJs = new JsonObject();
				}
			}
			if (placesJs.has(address)) {
				output = (JsonObject) placesJs.get(address);
			}
		}
		catch(FileNotFoundException e) {
			// no big deal, just won't be able to use the file
			logger.error("The location service was unable to load the local location file.  Falling back to using the Google Maps API");
		}
		
		if (output == null) {
			// if city not found in placesJs, look up in Google Maps API
			String apiKey = "AIzaSyDIeDd4SnFvsefA7GN-6xFbNA0HH-VS0aU";
			String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + encodedAddress + "&key=" + apiKey;
			
			GetMethod getMethod = new GetMethod(url);
			HttpClient client = new HttpClient();
			try {
				int responseCode = client.executeMethod(getMethod);
				if (responseCode == 200) {
					InputStream inputStream = getMethod.getResponseBodyAsStream();
					
			        String response = null;
			        
			        byte[] buffer = IOUtils.toByteArray(inputStream);
			        response = new String(buffer);
					
					try {
						JsonObject responseJson = parser.parse(response).getAsJsonObject();
						// get the location from the response JSON
						
						JsonArray resultsArray = (JsonArray) responseJson.get("results");
						if (resultsArray.size() > 0) {
							JsonObject resultObject = (JsonObject) resultsArray.get(0);
							JsonObject locationObject = (JsonObject) ((JsonObject)resultObject.get("geometry")).get("location");
							locationObject.add("place", valueOrEmpty(address));
							output = locationObject;
						}
						else {
							output = new JsonObject();
						}
					}
					catch(JsonSyntaxException jse) {
						// failed to convert to json object.  perhaps the response isn't json
						logger.error("The location service failed to obtain a JSON response from the Google Maps API");
					}
				}
				else {
					// response was something other than 200
					logger.error("The location service received response " + responseCode + " from the Google Maps API");
				}
			}
			catch(HttpException e) {
				logger.error("The location service encountered an HttpException while calling the Google Maps API");
				e.printStackTrace();
			}
			catch(IOException e) {
				logger.error("The location service encountered an IOException while calling the Google Maps API");
				e.printStackTrace();
			}
			finally {
				getMethod.releaseConnection();
			}
		}
		return output;
	}
	
	private String generateStudyUrl(Study study) {
		if (study.getStudyUrl() != null && !study.getStudyUrl().equals("")) {
			return study.getStudyUrl();
		}
		else {
			return "";
		}
	}
	
	/**
	 * Performs a request to get summary data for a particular Study.  This is the method that
	 * performs the work so it can accept a studyName of null to use the program-level query.
	 * 
	 * @param chartName[] the charts to request
	 * @param studyName the study name to filter by OR null to use program-level queries
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
			}
			catch (UnsupportedEncodingException e) {
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
		}
		catch(Exception e) {
			e.printStackTrace();
			logger.error("error retrieving summary data for study " + studyName);
		}
		if (client.getResponse().getStatus() != 200) {
			return new JsonObject();
		}
		else {
			JsonParser parser = new JsonParser();
			JsonObject output = parser.parse(response).getAsJsonObject();
			return output;
		}
	}
	
    
    /**
     * Performs a test and writes out a JsonPrimitive for the string value passed in.
     * If the value is null, the JsonPrimitive is given an empty string
     */
    private JsonPrimitive valueOrEmpty(String value) {
    	return valueOrEmpty(value, "");
    }
    
    /**
     * Performs a test and writes out a JsonPrimitive for the string value passed in.
     * If the value is null, the JsonPrimitive is given an empty string.
     * 
     * Provides a way to fill in an alternative value
     */
    private JsonPrimitive valueOrEmpty(String value, String alternative) {
    	if (value == null) {
    		value = alternative;
    	}
    	return new JsonPrimitive(value);
    }
    
	/**
	 * Performs a webservice request to get count summary data for a particular Study.  This is the method that
	 * performs the work so it can accept a studyId.
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
		}
		catch(Exception e) {
			e.printStackTrace();
			logger.error("error retrieving count summary data for study ID" + studyId);
		}
		if (client.getResponse().getStatus() != 200) {
			return new JsonObject();
		}
		else {
			JsonParser parser = new JsonParser();
			JsonObject output = parser.parse(response).getAsJsonObject();
			return output;
		}
	}
}
