package gov.nih.tbi.service;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.service.model.DataCart;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBException;

/**
 * Contains the back end or business logic for saving, retrieving, and maintaining saved query data.
 * 
 * @author jeng
 */
public interface SavedQueryManager {


	/**
	 * Tests if the given query name is unique from with in the system. The test of uniqueness involves generating the
	 * underlining file name and path from the given query name. If the file exists in the file system, then the given
	 * query name is not unique.
	 * 
	 * @param queryName - The name of the query to test for existence.
	 * @return True if and only if the given query name is unique, and does not correspond to any saved query XML file
	 *         name.
	 * @throws UnsupportedEncodingException When a proxy ticket could not be generated for the underlining web service
	 *         call.
	 * @throws WebApplicationException When there is a HTTP error produced by the underlining web service call.
	 */
	public boolean isQueryNameUnique(String queryName) throws UnsupportedEncodingException, WebApplicationException;


	/**
	 * Saves any changed/new saved query data to the system. The provided entity map list will be updated with versions
	 * that were synced with the database.
	 * 
	 * @param dataCart - The data cart object that will be marshalled to XML for the saved query.
	 * @param savedQuery - The saved query object to persist to the saved query web service for storage.
	 * @param entityList - A list of permissions to be sent to the account web service to be saved.
	 * @return A saved query object that was synced with the database (i.e. All temp IDs have been replaced with the
	 *         real ones).
	 * @throws JAXBException When there was an error when marshalling the data cart to XML.
	 * @throws UnsupportedEncodingException When the proxy ticket could not be URL encoded.
	 * @throws WebApplicationException When any of the underlining web service calls produce a HTTP error response.
	 */
	public SavedQuery saveSavedQuery(DataCart dataCart, SavedQuery savedQuery, List<EntityMap> entityList)
			throws JAXBException, UnsupportedEncodingException, WebApplicationException;

	/**
	 * Removes the saved query using the query tool rest service See the SavedQueryRestService class.
	 *
	 * @param savedQueryId
	 * @param accountName
	 * @throws WebApplicationException When a the delete call to the web service was not successful.
	 * @throws UnsupportedEncodingException
	 */
	public void removeSavedQuery(Long savedQueryId) throws WebApplicationException, UnsupportedEncodingException;

	public List<SavedQuery> getSavedQueries(String accountName);


	public SavedQuery getSavedQueryById(Long queryId);


	/**
	 * Retrieves a list of savedQueries query using the query tool rest service See the QueryToolRestService.java class
	 * 
	 * @param qtDatacartManager
	 */
	public Map<Long, MetaStudy> getMetaStudies();
	
	/**
	 * links savedQuery to metaStudy using the query tool rest service See the QueryToolRestService.java 	class
	 * 
	 * @param qtDatacartManager
	 */
	public void linkSavedQueryMetaStudy(long metaStudyId,SavedQuery clonedSavedQuery);
	
	public boolean isQueryFileNameUniquePerMetaStudy(String fileName,long metaStudyId) throws UnsupportedEncodingException, WebApplicationException;
	
	public boolean isQuerySavedNameUniquePerMetaStudy(String queryName, long metaStudyId ) throws UnsupportedEncodingException;

	public SavedQuery getSavedQueryByNameAndMetaStudy(String queryName, long metaStudyId) throws UnsupportedEncodingException;
	
	public SavedQuery saveSavedQuery(DataCart dataCart, SavedQuery savedQuery) throws UnsupportedEncodingException, WebApplicationException;

}
