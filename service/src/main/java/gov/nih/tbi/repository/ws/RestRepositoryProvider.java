
package gov.nih.tbi.repository.ws;

import gov.nih.tbi.account.ws.RestAuthenticationProvider;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.UpdateFormStructureReferencePayload;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

/**
 * Provides an implementation for the secure and public Rest service calls of the BRICS repository module.
 * 
 * Developer Note: All the rest service call functions defined in this class must call
 * RestAuthenticationProvider.ticketValid() and RestAuthenticationProvider.cleanup() at the beginning and end of the
 * function respectively to guarantee validity of the proxy ticket. Finally, the user must get the proxyTicket argument
 * (i.e. ticket=PT-3284792379842) and append it to the web service url if the user needs to be authenticated. Any ws
 * calls without a ticket will be considered anon. The user can get this formated property with
 * RestAuthenticationProvider.getTicketProperty().
 * 
 * @author Michael Valeiras
 * 
 */
public class RestRepositoryProvider extends RestAuthenticationProvider {

	/***************************************************************************************************/

	private static Logger logger = Logger.getLogger(RestRepositoryProvider.class);
	protected final String restServiceUrl = "portal/ws/repository/repository/";

	/***************************************************************************************************/

	/**
	 * A constructor for a rest provider in which the serverLocation provided is the domain of the web service call
	 * being made. In the case that any path information other than a domain (such as /portal), that information is
	 * stripped.
	 * 
	 * A proxyTicket provided can only be used one time. If RestAccountProvider attempts to use the proxy ticket
	 * multiple times, an exception is thrown. If a null or blank string is provided, then public calls may be made.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @throws MalformedURLException
	 */
	public RestRepositoryProvider(String serverLocation, String proxyTicket) {

		super(serverLocation, proxyTicket);
	}

	/***************************************************************************************************/

	public Study getStudyByStudyId(String studyId) throws UnsupportedEncodingException {

		ticketValid();

		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "Study/get/" + studyId + "?" + getTicketProperty());
		Study study = client.accept("text/xml").get(Study.class);

		cleanup();

		return study;
	}


	public void updateFormStructureReferences(UpdateFormStructureReferencePayload payload)
			throws UnsupportedEncodingException, JAXBException {
		ticketValid();

		HttpClient httpClient = new HttpClient();

		String url =
				serverUrl + restServiceUrl + "dataset/update_form_structure_references" + "?" + getTicketProperty();
		PostMethod postMethod = new PostMethod(url);

		final StringWriter stringWriter = new StringWriter();
		JAXBContext jaxbContext = JAXBContext.newInstance(UpdateFormStructureReferencePayload.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

		jaxbMarshaller.marshal(payload, stringWriter);

		postMethod.setRequestEntity(new StringRequestEntity(stringWriter.toString(), "application/xml", "UTF-8"));

		try {
			httpClient.executeMethod(postMethod);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cleanup();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Study> listStudiesByUserAndPermission(String username, PermissionType permission)
			throws UnsupportedEncodingException {

		ticketValid();

		String webString = serverUrl + restServiceUrl + "Study/list/";
		if (username != null) {
			webString += username;
		}
		if (permission != null) {
			webString += "?permission=" + permission.toString();
			if (!getTicketProperty().isEmpty()) {
				webString += "&" + getTicketProperty();
			}
		} else {
			if (!getTicketProperty().isEmpty()) {
				webString += "?" + getTicketProperty();
			}
		}

		WebClient client = WebClient.create(webString);
		List<Study> studies = (List<Study>) client.accept("text/xml").getCollection(Study.class);

		cleanup();

		return studies;
	}

	/**
	 * Retrieves the Studies a user has permission to see.
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public Set<Study> getStudies() throws UnsupportedEncodingException {

		ticketValid();
		String webString = serverUrl + restServiceUrl + "Study/getStudies";
		if (!getTicketProperty().isEmpty()) {
			webString += "&" + getTicketProperty();
		}

		WebClient client = WebClient.create(encodeUrlParam(webString));
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

		ticketValid();
		String webString = serverUrl + restServiceUrl + "Dataset/getDatasets?study=" + "studyName";
		if (studyName == null) {
			return null;
		}
		if (!getTicketProperty().isEmpty()) {
			webString += "&" + getTicketProperty();
		}

		WebClient client = WebClient.create(encodeUrlParam(webString));
		ArrayList<Dataset> datasets = (ArrayList<Dataset>) client.accept("text/xml").getCollection(Dataset.class);
		Set<Dataset> dataset = new HashSet<Dataset>();
		dataset.addAll(datasets);
		return dataset;
	}

	/**
	 * Retrieves a list of datasets that are currently being uploaded
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public List<Dataset> getUploadingDataset() throws UnsupportedEncodingException {

		ticketValid();
		String webString = serverUrl + restServiceUrl + "Dataset/getUploadingDatasets";
		if (!getTicketProperty().isEmpty()) {
			webString += "&" + getTicketProperty();
		}

		WebClient client = WebClient.create(encodeUrlParam(webString));
		List<Dataset> studies = (List<Dataset>) client.accept("text/xml").getCollection(Dataset.class);
		return studies;
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

		ticketValid();
		String webString = serverUrl + restServiceUrl + "Dataset/" + id + "/getDatasetFiles";
		if (!getTicketProperty().isEmpty()) {
			webString += "&" + getTicketProperty();
		}

		WebClient client = WebClient.create(encodeUrlParam(webString));
		ArrayList<DatasetFile> datasetFiles =
				(ArrayList<DatasetFile>) client.accept("text/xml").getCollection(DatasetFile.class);
		Set<DatasetFile> datasetFile = new HashSet<DatasetFile>();
		datasetFile.addAll(datasetFiles);
		return datasetFile;
	}

	/**
	 * Used in the Upload Process, given a dataset Id, a dataset is set to complete
	 * 
	 * @param id
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public void setDatasetFileToComplete(Long id) throws MalformedURLException, UnsupportedEncodingException {

		ticketValid();
		String webString = serverUrl + restServiceUrl + "DatasetFile/setToComplete?id=" + id;
		if (!getTicketProperty().isEmpty()) {
			webString += "&" + getTicketProperty();
		}

		WebClient client = WebClient.create(encodeUrlParam(webString));
		client.accept("text/xml");
	}

	/**
	 * Given a datasetFile id, a BasisStudy belonging to the datasetFile is returned.
	 * 
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public BasicStudy getStudyFromDatasetFile(Long id) throws UnsupportedEncodingException {

		ticketValid();
		String webString = serverUrl + restServiceUrl + "Study/getFromDataset?id=" + id;
		if (!getTicketProperty().isEmpty()) {
			webString += "&" + getTicketProperty();
		}

		WebClient client = WebClient.create(encodeUrlParam(webString));
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

		ticketValid();
		String url = serverUrl + restServiceUrl + "Study/" + studyName + "/deleteByName?dataset=" + datasetName + "&"
				+ getTicketProperty();
		DeleteMethod deleteMethod = new DeleteMethod(encodeUrlParam(url));

		try {
			getClient().executeMethod(deleteMethod);

		} finally {
			deleteMethod.releaseConnection();
		}

		cleanup();
	}

	/**
	 * Used in the upload process, given the names of a study and a dataset that resides in it, the number of Datasets
	 * 
	 * @param studyName
	 * @param datasetName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getNumOfCompletedFiles(String studyName, String datasetName) throws UnsupportedEncodingException {

		ticketValid();
		String webString =
				serverUrl + restServiceUrl + "Upload/getCompletedFiles?study=" + studyName + "&dataset=" + datasetName;
		if (!getTicketProperty().isEmpty()) {
			webString += "&" + getTicketProperty();
		}

		WebClient client = WebClient.create(encodeUrlParam(webString));
		String completedFiles = client.accept("text/xml").get(String.class);
		return completedFiles;
	}

	/**
	 * Creates a table in the database using a Data Structure. The Data Structure is a sent over by converting the
	 * object to xml and sending it in the body of the POST request
	 * 
	 * @param datastructure
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean createTableFromDataStructure(StructuralFormStructure datastructure) throws SQLException, Exception {

		ticketValid();
		PostMethod postMethod;
		Boolean archived;
		final StringWriter stringWriter = new StringWriter();
		JAXBContext jaxbContext = JAXBContext.newInstance(StructuralFormStructure.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

		jaxbMarshaller.marshal(datastructure, stringWriter);
		StringBuilder postUrl = new StringBuilder();
		postUrl.append(serverUrl).append(restServiceUrl).append("DataStructure/createTable");
		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {
			postUrl.append("?").append(getTicketProperty());
		}

		postMethod = new PostMethod(postUrl.toString());
		postMethod.setRequestEntity(new StringRequestEntity(stringWriter.toString(), "application/xml", "UTF-8"));

		try {
			getClient().executeMethod(postMethod);
			String newString = postMethod.getResponseBodyAsString();
			byte[] responseBody = postMethod.getResponseBody();
			archived = new Boolean(new String(responseBody));
		} finally {

		}

		return archived;

	}

	/**
	 * UserDownloadQueue objects are deleted from a users Download Queue using a list of UserDownloadQueue ids,
	 * 
	 * @param selectedIds
	 * @throws HttpException
	 * @throws IOException
	 */
	public void deleteFromDownloadQueue(List<Long> selectedIds) throws HttpException, IOException {

		ticketValid();
		StringBuilder urlList = new StringBuilder();

		for (Long l : selectedIds) {
			urlList.append("id=" + l + "&"); // += "id=" + em.getId() + "&";
		}

		String url = serverUrl + restServiceUrl + "Download/deleteFromDownloadQueue"
				+ urlList.substring(0, urlList.length() - 1) + "&" + getTicketProperty();

		DeleteMethod deleteMethod = new DeleteMethod(encodeUrlParam(url));

		try {
			getClient().executeMethod(deleteMethod);

		} finally {
			deleteMethod.releaseConnection();
		}
		cleanup();
	}

	/**
	 * Given a fileType, all the files belonging to that user currently of that particular type are returned
	 * 
	 * @param fileType
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public List<UserFile> getAvailableUserFiles(String fileType) throws UnsupportedEncodingException {

		ticketValid();
		String webString = serverUrl + restServiceUrl + "UserFiles/getAvalibleFiles?fileType=" + fileType;
		if (!getTicketProperty().isEmpty()) {
			webString += "&" + getTicketProperty();
		}

		WebClient client = WebClient.create(encodeUrlParam(webString));
		List<UserFile> studies = (List<UserFile>) client.accept("text/xml").getCollection(UserFile.class);
		return studies;
	}

	/**
	 * Given an id number, a corresponding UserFile object is returned
	 * 
	 * @param fileId
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public UserFile getFileById(Long fileId) throws UnsupportedEncodingException {

		ticketValid();
		String webString = serverUrl + restServiceUrl + "UserFiles/getFileById?fileId=" + fileId;
		if (!getTicketProperty().isEmpty()) {
			webString += "&" + getTicketProperty();
		}

		WebClient client = WebClient.create(encodeUrlParam(webString));
		UserFile uf = client.accept("text/xml").get(UserFile.class);
		return uf;
	}

	/**
	 * Given a id, a Data Structure can be set to the archived status.
	 * 
	 * @param dataStructureId
	 * @param isArchived
	 * @return
	 * @throws IOException
	 */
	public boolean setDataStoreInfoArchive(Long dataStructureId, boolean isArchived) throws IOException {

		ticketValid();
		PostMethod postMethod;
		Boolean archived;
		StringBuilder postUrl = new StringBuilder();
		postUrl.append(serverUrl).append(restServiceUrl).append("DataStore/DataStructure/").append(dataStructureId)
				.append("/archive?isArchived=").append(isArchived);
		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {
			postUrl.append("&").append(getTicketProperty());
		}

		postMethod = new PostMethod(encodeUrlParam(postUrl.toString()));
		try {
			getClient().executeMethod(postMethod);
			byte[] responseBody = postMethod.getResponseBody();
			archived = new Boolean(new String(responseBody));
		} finally {
			postMethod.releaseConnection();
		}
		cleanup();
		return archived;
	}

	public void sendDownloadReport(String report) {

		logger.info("This is the report string generated by the Repo rest provider" + report);
		// TODO Auto-generated method stub
	}

	public void clearHibernateCache()  {
		URIBuilder uriBuilder = null;
		StringBuilder url = new StringBuilder();
		url.append(serverUrl).append(restServiceUrl).append("hibernate/clearCache");
		
		DeleteMethod deleteMethod = null;
		
		try {
			uriBuilder = new URIBuilder(url.toString());
			logger.info(uriBuilder.toString());
			deleteMethod = new DeleteMethod(uriBuilder.toString());
			getClient().executeMethod(deleteMethod);
			String responseStatus = deleteMethod.getStatusText();
			logger.info("Response Status: " + responseStatus);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			deleteMethod.releaseConnection();
		}
	}
}
