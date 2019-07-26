package gov.nih.tbi.ws.provider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import gov.nih.tbi.dictionary.model.QueryToolRestServiceModel.MetaStudyList;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.repository.model.hibernate.UserFile;


public class RestMetaStudyProvider extends RestAuthenticationProvider {

	private static final Logger log = LogManager.getLogger(RestMetaStudyProvider.class);

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
	public RestMetaStudyProvider(String serverLocation, String proxyTicket) {

		super(serverLocation, proxyTicket);
	}

	/**
	 * FOR QUERY TOOL USE ONLY. MUST PASS ApplicationConstants.isWebservicesSecured() INTO THE THIRD ARG.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @param wsSecure
	 */
	public RestMetaStudyProvider(String serverLocation, String proxyTicket, boolean wsSecure) {
		super(serverLocation, proxyTicket, wsSecure);
	}

	/**
	 * Retrieves a map of MetaStudies for the login account holder
	 * 
	 * @param qtDatacartManager
	 */
	public Map<Long, MetaStudy> getMetaStudies(String path /* constants.getUserMetaStudyListWebServiceURL() */)
			throws UnsupportedEncodingException {

		WebClient client = createWebClient(path);

		client.query("ticket", getEncodedTicket(), "UTF-8");

		String ac = client.getCurrentURI().toString();
		createWebClient(ac);
		log.debug("unsecure url  " + ac);

		MetaStudyList msl = client.accept("text/xml").get(MetaStudyList.class);

		if (msl == null) {
			log.error("msl object returned null.");
		} else if (msl.getList() == null) {
			log.debug("msl list object returned null.");
		} else {
			log.debug("msl list: " + msl.getList().toString());
		}

		Map<Long, MetaStudy> metaStudiesMap = new HashMap<Long, MetaStudy>();

		if (msl != null && msl.getList() != null) {
			for (MetaStudy ms : msl.getList()) {
				metaStudiesMap.put(ms.getId(), ms);
			}
		}

		return metaStudiesMap;
	}


	/**
	 * Links a single saved query to a metaStudy
	 * 
	 * @param
	 */
	public void linkSavedQueryMetaStudy(Long metaStudyId, Long savedQueryId, String path /*constants.linkSavedQueryMetaStudyServiceURL()*/)
			throws UnsupportedEncodingException {

		WebClient client = createWebClient(path + "/" + savedQueryId + "/" + metaStudyId);

		client.query("ticket", getEncodedTicket(), "UTF-8");

		String ac = client.getCurrentURI().toString();
		createWebClient(ac);
		log.debug("unsecure url  " + ac);
		WebClient response = client.accept("text/xml");

		log.info("generateTriplanarImages web service call returns status code " + response.get().getStatus());

		if (response.get().getStatus() != 200) {
			log.error("generateTriplanarImages web service call failed status code " + response.get().getStatus());
		}

	}

	public boolean isMetaStudyDataFileNameUnique(String metaStudyDataFileName, long metaStudyId, String path /*constants.uploadQTDownloadPackageMetaStudyServiceURL()*/)
			throws UnsupportedEncodingException {
		WebClient client = createWebClient(path + "/" + metaStudyId + "/isDataFileNameUnique");

		client.query("fileName", metaStudyDataFileName);
		client.query("ticket", getEncodedTicket(), "UTF-8");

		String ac = client.getCurrentURI().toString();
		WebClient.create(ac);
		log.debug("unsecure url  " + ac);

		Response response = client.get();

		if (response.getStatus() != 200) {
			log.error("User is trying to add a file to a meta study and the file name is not unique. "
					+ response.getStatus() + " metaStudyId " + metaStudyId + " file name: " + metaStudyDataFileName);
			return false;
		}
		return true;
	}

	/**
	 * Sends the given download package from the query tool rest provider See the MetaStudyRestService.java class
	 * 
	 * @throws UnsupportedEncodingException
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 **/
	public void sendDownloadPackageToMetaStudy(UserFile instanceDataFile, Long metaStudyId, String path /*constants.uploadQTDownloadPackageMetaStudyServiceURL()*/)
			throws UnsupportedEncodingException {

		WebClient client = createWebClient(path + "/" + metaStudyId + "/metaStudyData");
		client.query("fileDescription", instanceDataFile.getDescription());
		client.query("fileName", instanceDataFile.getName());
		client.query("filePath", instanceDataFile.getPath());
		client.query("fileSize", Long.toString(instanceDataFile.getSize()));

		client.query("ticket", getEncodedTicket(), "UTF-8");

		String ac = client.getCurrentURI().toString();
		createWebClient(ac);
		log.info("unsecure url  " + ac);

		Response response = client.post(null);

		log.info("Sending Instance Data to the meta study module " + response.getStatus());

		if (response.getStatus() != 200) {
			log.error("Sending Instance Data to the meta study module failed status code " + response.getStatus());
		}
	}
}
