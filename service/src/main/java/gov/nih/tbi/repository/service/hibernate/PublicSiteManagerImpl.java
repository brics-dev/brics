package gov.nih.tbi.repository.service.hibernate;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.jcraft.jsch.JSchException;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.service.PublicSiteManager;
import gov.nih.tbi.commons.service.QueryToolManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.repository.dao.DatafileEndpointInfoDao;
import gov.nih.tbi.repository.model.StudySubmittedForm;
import gov.nih.tbi.repository.model.StudySubmittedFormCache;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;

@Service
public class PublicSiteManagerImpl implements PublicSiteManager {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2463715895436584101L;
	private static Logger logger = Logger.getLogger(PublicSiteManagerImpl.class);
	private String orgName;
	private SftpClient sftpClient;
	
	@Autowired
	ModulesConstants modulesConstants;
	
	@Autowired
	DatafileEndpointInfoDao datafileEndpointInfoDao;
	
	@Autowired
	protected RepositoryManager repositoryManager;
	
	@Autowired
	private QueryToolManager queryToolManager;

	@Override
	public void buildFile() {
		orgName = modulesConstants.getModulesOrgName();
		
		String todaysDate = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
		String studyDataFileName = orgName + "_" +todaysDate + "_studyMetrics.json";

		String studyDataFile = buildStudyData();
		
		try {
			writeFileToPublic(studyDataFileName,studyDataFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private String buildStudyData(){
		
		JsonObject output = new JsonObject();
		
		output = this.performPublicSubmittedDataQuery();

		Gson gsonOutput = new GsonBuilder().setPrettyPrinting().create();
		return gsonOutput.toJson(output);
	}
	
	private void writeFileToPublic(String fileName, String data) throws Exception{
		
		try {
			String destinationPath = ServiceConstants.TBI_PUBLIC_SITE_FILE_PATH;
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
			logger.error("Could not write Public Site Study file");
			e.printStackTrace();
			throw new Exception("Failure in file processing", e);
		}
		catch(Exception e) {
			logger.error("The Public Site Study file could not be saved before transfer");
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
	
	private JsonObject performPublicSubmittedDataQuery() {
		String portalRootUrl = modulesConstants.getModulesPublicURL() + modulesConstants.getModulesPortalRoot();

		String requestUrl = portalRootUrl + "/ws/summaryData/studySubmittedData";

		JsonObject output = new JsonObject();
		try {
			logger.debug("Request URL: " + requestUrl);
			
			HttpClient httpClient = new HttpClient();
			GetMethod getMethod = new GetMethod(requestUrl);
			
			int responseCode = httpClient.executeMethod(getMethod);
			if (responseCode == 200) {
				InputStream input = getMethod.getResponseBodyAsStream();
				String response = null;
				byte[] buffer = IOUtils.toByteArray(input);			
				response = new String(buffer);
	
				JsonParser parser = new JsonParser();
				output = parser.parse(response).getAsJsonObject();
			} else {
				logger.error("Retrieving public submitted data service failed with reponse code "+responseCode);
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
			logger.error("error retrieving submitted data for public site");
		} 

		return output;

	}

}
