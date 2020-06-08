package gov.nih.tbi.repository.service.hibernate;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.commons.service.PublicSiteManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.repository.dao.DatafileEndpointInfoDao;
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

	@Override
	public void buildFile() {
		orgName = modulesConstants.getModulesOrgName();

		String todaysDate = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
		String studyDataFileName = orgName + "_" + todaysDate + "_studyMetrics.json";

		String studyDataFile = buildStudyData();

		writeFileToPublic(studyDataFileName, studyDataFile);
	}

	private String buildStudyData() {

		JsonObject output = new JsonObject();

		output = this.performPublicSubmittedDataQuery();

		Gson gsonOutput = new GsonBuilder().setPrettyPrinting().create();
		return gsonOutput.toJson(output);
	}

	private void writeFileToPublic(String fileName, String data) {

		try {
			String destinationPath = ServiceConstants.TBI_PUBLIC_SITE_FILE_PATH;
			initializeSftpClient();
			FileUtils.writeStringToFile(new File("/tmp/" + fileName), data, StandardCharsets.UTF_8);
			File reopenedFile = new File("/tmp/" + fileName);
			sftpClient.upload(reopenedFile, destinationPath, fileName);
		} catch (JSchException e) {
			logger.error("SFTP client could not be initialized", e);
		} catch (IOException e) {
			logger.error("Could not write Public Site Study file", e);
		} catch (SftpException e) {
			logger.error("Could not upload Public Site Study file", e);
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
				logger.error("Retrieving public submitted data service failed with reponse code " + responseCode);
			}
		} catch (IOException e) {
			logger.error("error retrieving submitted data for public site", e);
		}

		return output;

	}

}
