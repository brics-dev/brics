package gov.nih.tbi.commons;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.StructuralFormStructureList;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.repository.model.SubmissionTicket;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Study;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;


public class WebstartRestProvider {

	/***************************************************************************************************/

	private static Logger logger = Logger.getLogger(WebstartRestProvider.class);
	protected final String repoRestServiceUrl = "portal/ws/webstart/repository/";
	protected final String dictRestServiceUrl = "portal/ws/webstart/dictionary/";

	protected final String USER_NAME = "REPLACED";
	protected final String PASS = "REPLACED";
	protected final String LOCAL_PATH = "localPath";
	protected final String SERVER_PATH = "serverPath";

	protected final int MAX_DELETE_LENGTH = 10000;

	protected final int RECEIVE_TIMEOUT_MS = 0;
	protected final int CONNECTION_TIMEOUT_MS = 0;
	HttpClient client;

	private String serverUrl;
	private String userName;
	private String password;

	/***************************************************************************************************/

	public WebstartRestProvider(String serverUrl, String userName, String password) {

		this.client = new HttpClient();
		this.serverUrl = serverUrl;
		this.userName = userName;
		this.password = password;
	}

	private WebClient buildWebClient(String url) {
		WebClient client = WebClient.create(url);
		HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();
		HTTPClientPolicy policy = new HTTPClientPolicy();
		policy.setReceiveTimeout(RECEIVE_TIMEOUT_MS);
		policy.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
		conduit.setClient(policy);

		return client;
	}

	/**
	 * Retrieves the Studies a user has permission to see.
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public Set<Study> getStudies() throws UnsupportedEncodingException {

		String webString = serverUrl + repoRestServiceUrl + "Study/getStudies";

		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		ArrayList<Study> studies = (ArrayList<Study>) client.accept("text/xml").getCollection(Study.class);
		Set<Study> study = new HashSet<Study>();
		study.addAll(studies);
		return study;
	}

	/**
	 * Retrieves the Datasets that belong to a particular study
	 * 
	 * @param studyName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public Set<Dataset> getDatasets(String studyName) throws UnsupportedEncodingException {

		if (studyName == null) {
			return null;
		}

		String webString = serverUrl + repoRestServiceUrl + "Dataset/getDatasets?study=" + encodeUrlParam(studyName);
		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		ArrayList<Dataset> datasets = (ArrayList<Dataset>) client.accept("text/xml").getCollection(Dataset.class);
		Set<Dataset> dataset = new HashSet<Dataset>();
		dataset.addAll(datasets);
		return dataset;
	}

	@SuppressWarnings("unchecked")
	public boolean getDatasetExistStatus(String studyName, String datasetName) throws UnsupportedEncodingException {

		if (studyName == null || datasetName == null) {
			return false;
		}

		String webString = serverUrl + repoRestServiceUrl + "Dataset/getDatasets?study=" + encodeUrlParam(studyName);
		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		ArrayList<Dataset> datasets = (ArrayList<Dataset>) client.accept("text/xml").getCollection(Dataset.class);
		Set<Dataset> dataset = new HashSet<Dataset>();
		dataset.addAll(datasets);
		return true;
	}

	/**
	 * Retrieves a list of datasets that are currently being uploaded
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public List<Dataset> getUploadingDataset() throws UnsupportedEncodingException {

		String webString = serverUrl + repoRestServiceUrl + "Dataset/getUploadingDatasets";

		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		List<Dataset> dataset = (List<Dataset>) client.accept("text/xml").getCollection(Dataset.class);
		return dataset;
	}

	/**
	 * Given a dataset Id, A set of dataset files belonging to the dataset are returned.
	 * 
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public Set<DatasetFile> getDatasetFiles(Long id) throws UnsupportedEncodingException {

		String webString = serverUrl + repoRestServiceUrl + "dataset/" + id + "/datasetFiles";

		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		ArrayList<DatasetFile> datasetFiles =
				(ArrayList<DatasetFile>) client.accept("text/xml").getCollection(DatasetFile.class);
		Set<DatasetFile> datasetFile = new TreeSet<DatasetFile>();
		datasetFile.addAll(datasetFiles);
		return datasetFile;
	}

	/**
	 * Used in the Upload Process, given a dataset Id, a dataset is set to complete
	 * 
	 * @param id
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws WebstartRestProviderException
	 */
	public void setDatasetFileToComplete(Long id) throws MalformedURLException, UnsupportedEncodingException,
			InternalServerErrorException, WebstartRestProviderException {

		String webString = serverUrl + repoRestServiceUrl + "DatasetFile/setToComplete?id=" + id;

		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);

		try {
			Response response = client.accept("text/xml").get(Response.class);

			if (response != null && response.getStatus() != 200) {
				throw new WebstartRestProviderException();
			}
		} catch (Exception e) {
			throw new WebstartRestProviderException();
		}
	}

	/**
	 * Cancel an ongoing upload process, delete a dataset that has been partially submitted into a BRICS study
	 */
	public void deleteDataset(Long id) throws MalformedURLException, UnsupportedEncodingException,
			InternalServerErrorException, WebstartRestProviderException {

		String webString = serverUrl + repoRestServiceUrl + "DatasetFile/setToComplete?id=" + id;

		WebClient client = WebClient.create(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		try {
			Response response = client.accept("text/xml").get(Response.class);

			if (response != null && response.getStatus() != 200) {
				throw new WebstartRestProviderException();
			}
		} catch (Exception e) {
			throw new WebstartRestProviderException();
		}
	}

	/**
	 * Given a datasetFile id, a BasisStudy belonging to the datasetFile is returned.
	 * 
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public BasicStudy getStudyFromDatasetFile(Long id) throws UnsupportedEncodingException {

		String webString = serverUrl + repoRestServiceUrl + "Study/getFromDataset?id=" + id;

		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		BasicStudy study = client.accept("text/xml").get(BasicStudy.class);


		return study;
	}

	/**
	 * Given the name of a studyName and the name of a dataset that resides in the study, the dataset set is deleted
	 * from the study
	 * 
	 * @param studyName
	 * @param datasetName
	 * @throws HttpException
	 * @throws IOException
	 */
	public void deleteDatasetByName(String studyName, String datasetName) throws HttpException, IOException {

		String url = serverUrl + repoRestServiceUrl + "Study/" + encodeUrlParam(studyName) + "/deleteByName?dataset="
				+ encodeUrlParam(datasetName);
		DeleteMethod deleteMethod = new DeleteMethod(url);
		deleteMethod.addRequestHeader(USER_NAME, userName);
		deleteMethod.addRequestHeader(PASS, password);
		try {
			getClient().executeMethod(deleteMethod);
		} finally {
			deleteMethod.releaseConnection();
		}

	}

	/**
	 * Given the name of a studyName and the name of a dataset that resides in the study, the dataset set is deleted
	 * from the study
	 * 
	 * @param studyName
	 * @param datasetName
	 * @throws HttpException
	 * @throws IOException
	 */
	public void deleteDatasetCascadeByName(String studyName, String datasetName) throws HttpException, IOException {
		String url = serverUrl + repoRestServiceUrl + "Study/" + encodeUrlParam(studyName)
				+ "/deleteCascadeByName?dataset=" + encodeUrlParam(datasetName);
		System.out.println("deleteDatasetCascasdeByName: url:" + url);
		DeleteMethod deleteMethod = new DeleteMethod(url);
		deleteMethod.addRequestHeader(USER_NAME, userName);
		deleteMethod.addRequestHeader(PASS, password);
		try {
			getClient().executeMethod(deleteMethod);
		} finally {
			deleteMethod.releaseConnection();
		}

	}

	/**
	 * Used in the upload process, given the names of a study and a dataset that resides in it, the number of Datasets
	 * 
	 * @param studyName
	 * @param datasetName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getNumOfCompletedFiles(String datasetName) throws UnsupportedEncodingException {

		String webString =
				serverUrl + repoRestServiceUrl + "upload/completedFiles?dataset=" + encodeUrlParam(datasetName);

		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		String completedFiles = client.accept("text/xml").get(String.class);
		return completedFiles;
	}

	/**
	 * Returns a list of download packages owned by the current user
	 * 
	 * @return
	 */
	public List<DownloadPackage> getDownloadPackage() {
		String webString = serverUrl + repoRestServiceUrl + "download/DownloadPackage";
		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		List<DownloadPackage> downloadPackages =
				(List<DownloadPackage>) client.accept("text/xml").getCollection(DownloadPackage.class);
		return downloadPackages;
	}

	/**
	 * Delete all the downloadables with the given IDs Calls the download/deleteDownloadables web service with a list of
	 * id parameter
	 * 
	 * We need to chunk the list of selected IDs because tomcat has a limit to the parameter in the POST body. The
	 * default for this limit is 10,000. Since this code is client side, there is no way for us to get this limit
	 * dynamically. The constant MAX_DELETE_LENGTH contains the default max parameter count for tomcat.
	 * 
	 * This was changed from a DELETE request into a POST because the number of arguments being passed is too large.
	 * 
	 * @param selectedIds
	 * @throws Exception
	 * @throws HttpException
	 */
	public void deleteDownloadables(List<Long> selectedIds) throws Exception {
		if (selectedIds.isEmpty()) {
			return;
		}

		List<List<Long>> selectedIdLists = splitList(selectedIds, MAX_DELETE_LENGTH);

		for (List<Long> selectedList : selectedIdLists) {

			DataOutputStream writer = null;
			HttpURLConnection connection = null;
			StringBuilder paramBuilder = new StringBuilder();

			for (Long id : selectedList) {
				paramBuilder.append("id=").append(id).append("&");
			}

			paramBuilder.replace(paramBuilder.length() - 1, paramBuilder.length(), ModelConstants.EMPTY_STRING);

			byte[] postData = paramBuilder.toString().getBytes(Charset.forName("UTF-8"));
			int postDataLength = postData.length;

			try {

				URL url = new URL(serverUrl + repoRestServiceUrl + "download/deleteDownloadables");

				if (url.getProtocol().equals("https") == true) {
					connection = (HttpsURLConnection) url.openConnection();
				} else {
					connection = (HttpURLConnection) url.openConnection();
				}


				connection.addRequestProperty(USER_NAME, userName);
				connection.addRequestProperty(PASS, password);
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
				connection.addRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
				connection.addRequestProperty("charset", "utf-8");
				connection.addRequestProperty("Content-Length", Integer.toString(postDataLength));
				connection.connect();

				writer = new DataOutputStream(connection.getOutputStream());
				writer.write(postData);

				if (connection.getResponseCode() != 200) {
					System.out.println("Content: " + connection.getContent());
					System.out.println("HTTP toString: " + connection.toString());
					System.out.println("Error Stream: " + connection.getErrorStream() != null ? connection
							.getErrorStream().toString() : "Nothing");
					throw new HttpException(
							"Response code: " + connection.getResponseCode() + "\nContent: " + connection.getContent());
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			} finally {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				connection.disconnect();
			}
		}
	}

	/**
	 * Delete the download packages with the given list of IDs
	 * 
	 * @param selectedIds
	 * @throws HttpException
	 * @throws IOException
	 */
	public void deleteDownloadPackages(List<Long> selectedIds) throws HttpException, IOException {
		if (selectedIds.isEmpty()) {
			return;
		}

		// The list of selected Id's for removal is split to avoid reaching the max length of the http request
		// which will result in client's request being rejected by REST. PS-905.
		List<List<Long>> selectedIdLists = splitList(selectedIds, MAX_DELETE_LENGTH);

		StringBuilder urlList = new StringBuilder();
		for (List<Long> selectedList : selectedIdLists) {

			for (Long l : selectedList) {
				urlList.append("id=" + l + "&"); // += "id=" + em.getId() + "&";
			}
			final DefaultHttpClient httpClient = new DefaultHttpClient();

			String url = serverUrl + repoRestServiceUrl + "download/deleteDownloadPackages?"
					+ urlList.substring(0, urlList.length() - 1);

			HttpDelete httpDelete = new HttpDelete(url);
			httpDelete.addHeader(USER_NAME, userName);
			httpDelete.addHeader(PASS, password);

			DeleteMethod deleteMethod = new DeleteMethod(url);
			deleteMethod.addRequestHeader(USER_NAME, userName);
			deleteMethod.addRequestHeader(PASS, password);
			try {
				httpClient.execute(httpDelete);
				// client.ex

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				deleteMethod.releaseConnection();
			}
		}
	}

	/**
	 * UserDownloadQueue objects are deleted from a users Download Queue using a list of UserDownloadQueue ids,
	 * 
	 * @param selectedIds
	 * @throws HttpException
	 * @throws IOException
	 */
	@Deprecated
	public void deleteFromDownloadQueue(List<Long> selectedIds) throws HttpException, IOException {
		if (selectedIds.isEmpty()) {
			return;
		}

		// The list of selected Id's for removal is split to avoid reaching the max length of the http request
		// which will result in client's request being rejected by REST. PS-905.
		List<List<Long>> selectedIdLists = splitList(selectedIds, MAX_DELETE_LENGTH);

		StringBuilder urlList = new StringBuilder();
		for (List<Long> selectedList : selectedIdLists) {

			for (Long l : selectedList) {
				urlList.append("id=" + l + "&"); // += "id=" + em.getId() + "&";
			}
			final DefaultHttpClient httpClient = new DefaultHttpClient();

			String url = serverUrl + repoRestServiceUrl + "download/deleteFromDownloadQueue?"
					+ urlList.substring(0, urlList.length() - 1);

			HttpDelete httpDelete = new HttpDelete(url);
			httpDelete.addHeader(USER_NAME, userName);
			httpDelete.addHeader(PASS, password);

			DeleteMethod deleteMethod = new DeleteMethod(url);
			deleteMethod.addRequestHeader(USER_NAME, userName);
			deleteMethod.addRequestHeader(PASS, password);
			try {
				httpClient.execute(httpDelete);
				// client.ex

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				deleteMethod.releaseConnection();
			}
		}

	}

	/**
	 * Wrapper method to obscure username hashing
	 * 
	 * @param submissionTicket
	 * @param localPath
	 * @param serverPath
	 * @param studyName
	 * @param datasetName
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	// Used by Upload MGR
	public Dataset processSubmissionTicket(SubmissionTicket submissionTicket, String localPath, String serverPath,
			String studyName, String datasetName) throws IOException, JAXBException {

		Response response = null;

		submissionTicket.setDatasetName(datasetName);
		submissionTicket.setLocalPath(localPath);
		submissionTicket.setServerPath(serverPath);
		submissionTicket.setStudyName(studyName);

		String webString = serverUrl + repoRestServiceUrl + "dataset/processSubmissionTicket/";

		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		
		try {
			response = client.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML + "; charset=utf-8").post(submissionTicket);
		} catch (Exception e) {
			e.printStackTrace();

		}
		/* If the post method returns OK */
		if (response.getStatus() == 200) {
			return getDatasetByName(studyName, datasetName);
		}
		return null;

	}

	/**
	 * Given a study name and dataset name, a dataset is retrieved
	 * 
	 * @param studyName
	 * @param datasetName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Dataset getDatasetByName(String studyName, String datasetName) throws UnsupportedEncodingException {

		String webString = serverUrl + repoRestServiceUrl + "dataset/" + encodeUrlParam(encodeUrlParam(studyName)) + "/"
				+ encodeUrlParam(encodeUrlParam(datasetName));
		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		Dataset dataset = client.accept("text/xml").get(Dataset.class);
		return dataset;
	}

	/**************************************************************************************************************************************
	 * 
	 * 
	 * Dictionary Webstart Rest Services
	 * 
	 * 
	 *************************************************************************************************************************************/

	/**
	 * Convenience Pass through method
	 * 
	 * @param connection
	 * @param structureNames
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static List<StructuralFormStructure> getDataDictionary(WebstartRestProvider connection,
			String[] structureNames)

	{

		List<StructuralFormStructure> dataStructureRequestList = new ArrayList<StructuralFormStructure>();

		List<String> dsNames = new ArrayList<String>();

		for (String name : structureNames) {
			// name = name.toLowerCase();

			// XXX: break apart short name and create temp list of structures

			try {
				dsNames.add(name);

			} catch (NumberFormatException ex) {
				logger.debug(
						"Tried to get a data structure by composite shortname that has no version.  Likely this is because it is not actually a short name.");
				ex.printStackTrace();
			} catch (StringIndexOutOfBoundsException ex) {
				logger.debug(
						"Tried to get a data structure by composite shortname that is not long enough to have a version.  Likely this is because it is not actually a short name.");
				ex.printStackTrace();
			}
		}

		try {
			return connection.getDataStructuresDetails(dsNames);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns a list dataStructures after passing a list of AbstractData as xml.
	 * 
	 * @param dataStructureList
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public List<StructuralFormStructure> getDataStructuresDetails(List<String> dataStructureList)
			throws UnsupportedEncodingException {

		String webString = serverUrl + dictRestServiceUrl + "formStructure/details?";
		StringBuilder urlList = new StringBuilder();
		if (dataStructureList == null) {
			return null;
		}

		for (String l : dataStructureList) {
			urlList.append("dsName=" + encodeUrlParam(l) + "&"); // += "id=" + em.getId() + "&";
		}

		webString = webString + urlList.substring(0, urlList.length() - 1);
		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		StructuralFormStructureList dsl =
				(StructuralFormStructureList) client.accept("text/xml").get(StructuralFormStructureList.class);
		return dsl.getList();
	}

	/**
	 * Encodes the parameter of the URL with escape characters
	 * 
	 * @param param - the parameter to encode
	 * @return Properly encoded parameter (UTF-8)
	 * @throws UnsupportedEncodingException
	 */
	public static String encodeUrlParam(String param) throws UnsupportedEncodingException {

		if (param == null || ServiceConstants.EMPTY_STRING.equals(param)) {
			return param;
		}

		String encoded = URLEncoder.encode(param, "UTF-8");
		return encoded;
	}

	public HttpClient getClient() {

		return client;
	}

	public void setClient(HttpClient client) {

		this.client = client;
	}

	/**
	 * Sends reports to the server
	 * 
	 * @param string
	 */
	public void sendDownloadReport(String report) {

		String webString = serverUrl + dictRestServiceUrl + "download/logger";
		WebClient client = buildWebClient(webString);
		client.post(report);

		// TODO Auto-generated method stub

	}

	/**
	 * This is a helper method to split a list into a list of lists, each of a given length.
	 * 
	 * @param List<T> The list to be split
	 * @param int Max size of each divided list
	 * @return List<List<T>>
	 */
	private static <T> List<List<T>> splitList(List<T> list, int length) {

		List<List<T>> output = new ArrayList<List<T>>();
		int N = list.size();

		for (int i = 0; i < N; i += length) {
			List<T> group = list.subList(i, Math.min(N, i + length));
			output.add(new ArrayList<T>(group));
		}

		return output;

	}

	public DataElement getDataElementByName(String portalDomain, String name) throws UnsupportedEncodingException {

		if (name == null || name.isEmpty()) {
			return null;
		}

		String webString = serverUrl + dictRestServiceUrl + "DataElement/name/" + encodeUrlParam(name)
				+ "?portalDomain=" + encodeUrlParam(portalDomain);
		WebClient client = buildWebClient(webString);
		client.header(USER_NAME, userName);
		client.header(PASS, password);
		DataElement dataElement = (DataElement) client.accept("application/xml").get(DataElement.class);
		return dataElement;
	}
}
