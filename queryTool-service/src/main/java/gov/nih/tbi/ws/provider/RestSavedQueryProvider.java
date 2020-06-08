package gov.nih.tbi.ws.provider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import gov.nih.tbi.dictionary.model.QueryToolRestServiceModel.MetaStudyList;
import gov.nih.tbi.dictionary.model.QueryToolRestServiceModel.SavedQueryList;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.query.model.hibernate.SavedQuery;

public class RestSavedQueryProvider extends RestAuthenticationProvider {

	private static final Logger log = LogManager.getLogger(RestSavedQueryProvider.class);

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
	 */
	public RestSavedQueryProvider(String serverLocation, String proxyTicket) {

		super(serverLocation, proxyTicket);
	}

	/**
	 * FOR QUERY TOOL USE ONLY. MUST PASS ApplicationConstants.isWebservicesSecured() INTO THE THIRD ARG.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @param wsSecure
	 */
	public RestSavedQueryProvider(String serverLocation, String proxyTicket, boolean wsSecure) {
		super(serverLocation, proxyTicket, wsSecure);
	}


	/**
	 * Tests if the given query name is unique from with in the system. The test of uniqueness involves generating the
	 * underlining file name and path from the given query name. If the file exists in the file system, then the given
	 * query name is not unique.
	 * 
	 * @param queryName - The name of the query to test for existence.
	 * @param assertion
	 * @return True if and only if the given query name is unique, and does not correspond to any saved query XML file
	 *         name.
	 */
	public boolean isQueryNameUnique(String queryName,
			String path /* constants.getSavedQueryNameUniqueWebServiceURL() */) {

		// check if name exists for saved query, if it exists we will return false
		WebClient client;
		try {
			client = createWebClient(path + "/" + URLEncoder.encode(queryName, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new InternalServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Error occured while encoding saved query name").build());
		}

		client.query("ticket", getEncodedTicket(), "UTF-8");
		log.debug("isQueryNameUnique url: " + client.getCurrentURI().toString());

		Boolean isNameUnique = client.accept(MediaType.TEXT_PLAIN).get(Boolean.class);

		return isNameUnique.booleanValue();
	}


	/**
	 * Saves the query using the query tool rest service. See the QueryToolRestService.java class
	 * 
	 * @param savedQuery - The SavedQuery object to send to the web service.
	 */
	public SavedQuery saveSavedQuery(SavedQuery savedQuery,
			String path /* constants.getSavedQueryCreateSavedQueryWebServiceURL() */) {
		WebClient client = createWebClient(path);
		log.info("Saving the query named " + savedQuery.getName());

		// Add the proxy ticket to the URL.
		client.query("ticket", getEncodedTicket(), "UTF-8");
		log.debug("Save query URI: " + client.getCurrentURI().toString());

		SavedQuery newSavedQuery = client.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML)
				.post(savedQuery, SavedQuery.class);

		return newSavedQuery;
	}


	/**
	 * Removes the saved query using the query tool rest service See the SavedQueryRestService class.
	 *
	 * @param savedQueryId
	 * @param accountName
	 */
	public int removeSavedQuery(Long savedQueryId,
			String path /* constants.getSavedQueryRemoveSavedQueryWebServiceURL() */) {
		WebClient client = createWebClient(path + "/" + savedQueryId);

		client.query("ticket", getEncodedTicket());

		Response response = client.get();

		return response.getStatus();
	}


	/**
	 * Retrieves a map of savedQueries.
	 * 
	 * @param accountName
	 * @param path
	 * @return
	 */
	public List<SavedQuery> getSavedQueries(String accountName,
			String path /* constants.getUserSavedQueryListWebServiceURL() */) {

		WebClient client = createWebClient(path + "/" + accountName);

		// sets the list of saved queries to only return non copied saved queries
		client.query("isCopy", false);
		client.query("ticket", getEncodedTicket());

		SavedQueryList dsl = client.accept(MediaType.APPLICATION_XML).get(SavedQueryList.class);

		log.debug("1dsl list: " + dsl.getList().toString());

		return dsl.getList();
	}


	public SavedQuery getSavedQueryById(Long queryId, String path /* constants.getSavedQueryGetWebServiceURL() */) {
		WebClient client = createWebClient(path + "/" + queryId);

		client.query("ticket", getEncodedTicket());
		log.debug("getSavedQueryById url: " + client.getCurrentURI().toString());

		SavedQuery sq = client.accept(MediaType.APPLICATION_XML).get(SavedQuery.class);

		return sq;
	}


	/**
	 * Retrieves a map of MetaStudies for the login account holder.
	 * 
	 * @param path
	 * @return
	 */
	public Map<Long, MetaStudy> getMetaStudies(String path /* constants.getUserMetaStudyListWebServiceURL() */) {
		WebClient client = createWebClient(path);

		client.query("ticket", getEncodedTicket());
		log.debug("Get meta study URL: " + client.getCurrentURI().toString());

		MetaStudyList msl = client.accept(MediaType.TEXT_XML).get(MetaStudyList.class);

		log.debug("msl list: " + msl.getList().toString());

		Map<Long, MetaStudy> metaStudiesMap = new HashMap<Long, MetaStudy>();

		for (MetaStudy ms : msl.getList()) {
			metaStudiesMap.put(ms.getId(), ms);
		}

		return metaStudiesMap;
	}


	/**
	 * Links a single saved query to a metaStudy.
	 * 
	 * @param metaStudyId
	 * @param savedQueryId
	 * @param path
	 */
	public void linkSavedQueryMetaStudy(Long metaStudyId, Long savedQueryId,
			String path /* constants.linkSavedQueryMetaStudyServiceURL() */)
			throws UnsupportedEncodingException, WebApplicationException {

		WebClient client = createWebClient(path + "/" + savedQueryId + "/" + metaStudyId);

		client.query("ticket", getEncodedTicket());
		log.debug("Saved query/Meta study link URL: " + client.getCurrentURI().toString());

		client.get();
	}


	public boolean isFileNameUniquePerMetaStudy(String fileName, long metaStudyId,
			String path /* constants.getSavedQueryFileNameUniqueWebServiceURL() */) {

		WebClient client;

		try {
			client = createWebClient(path + "/" + URLEncoder.encode(fileName, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new InternalServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Error occured while encoding fileName name").build());
		}

		client.query("ticket", getEncodedTicket(), "UTF-8");
		log.debug("isFileNameUnique url: " + client.getCurrentURI().toString());

		client.query("metaStudyId", metaStudyId);

		Boolean isNameUnique = client.accept(MediaType.TEXT_PLAIN).get(Boolean.class);

		return isNameUnique.booleanValue();
	}

	public boolean isSavedQueryUniquePerMetaStudy(String queryName, long metaStudyId,
			String path /* constants.getSavedQueryNameUniquePerMetaStudyPath() */) {

		WebClient client;
		try {
			client = createWebClient(path + "/" + URLEncoder.encode(queryName, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new InternalServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Error occured while encoding saved query name").build());
		}

		client.query("ticket", getEncodedTicket(), "UTF-8");
		log.debug("isQueryNameUnique url: " + client.getCurrentURI().toString());

		client.query("metaStudyId", metaStudyId);

		Boolean isNameUnique = client.accept(MediaType.TEXT_PLAIN).get(Boolean.class);

		return isNameUnique.booleanValue();
	}
	
	public boolean isQueryLinkedToMetaStudy(long savedQueryId, String path) {

		WebClient client = createWebClient(path + "/" + savedQueryId);
		client.query("ticket", getEncodedTicket());
		log.debug("isQueryLinkedToMetaStudy link URL: " + client.getCurrentURI().toString());

		Boolean isLinked = client.accept(MediaType.TEXT_PLAIN).get(Boolean.class);
		return isLinked.booleanValue();
	}

	public SavedQuery getSavedQueryByNameAndMetaStudy(String queryName, long metaStudyId,
			String path /* constants.getSavedQueryByNameAndMetaStudyPath() */) {

		WebClient client;
		try {
			client = createWebClient(path + "/" + URLEncoder.encode(queryName, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new InternalServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Error occured while encoding saved query name").build());
		}

		client.query("ticket", getEncodedTicket(), "UTF-8");
		log.debug("getSavedQueryByNameAndMetaStudy url: " + client.getCurrentURI().toString());

		client.query("metaStudyId", metaStudyId);

		SavedQuery savedQ = client.accept(MediaType.APPLICATION_XML).get(SavedQuery.class);

		return savedQ;
	}



}
