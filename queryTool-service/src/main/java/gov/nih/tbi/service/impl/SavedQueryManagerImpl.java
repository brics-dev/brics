package gov.nih.tbi.service.impl;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.service.SavedQueryManager;
import gov.nih.tbi.service.model.DataCart;
import gov.nih.tbi.util.DataCartUtil;
import gov.nih.tbi.util.QueryRestProviderUtils;
import gov.nih.tbi.ws.provider.RestQueryAccountProvider;
import gov.nih.tbi.ws.provider.RestSavedQueryProvider;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

/**
 * Contains the back end or business logic for saving, retrieving, and maintaining saved query data.
 * 
 * @author jeng
 */
@Component
@Scope("application")
public class SavedQueryManagerImpl implements SavedQueryManager, Serializable {
	private static final long serialVersionUID = 1303242203793393205L;
	private static final Logger log = Logger.getLogger(SavedQueryManagerImpl.class);

	@Autowired
	ApplicationConstants constants;

	/**
	 * {@inheritDoc}
	 */
	public boolean isQueryNameUnique(String queryName) {
		RestSavedQueryProvider restSavedQueryProvider = new RestSavedQueryProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));
		return restSavedQueryProvider.isQueryNameUnique(queryName, constants.getSavedQueryNameUniqueWebServiceURL());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public SavedQuery saveSavedQuery(DataCart dataCart, SavedQuery savedQuery, List<EntityMap> entityList) {

		// Set last updated to now.
		savedQuery.setLastUpdated(new Date());
		
		log.info("Saving data cart to JSON...");

		JsonObject dataCartJson = DataCartUtil.getDataCartToSavedQueryJson(dataCart, savedQuery);
		savedQuery.setQueryData(dataCartJson.toString());

		log.info("Calling web service to save the saved query meta data to the database...");

		// Persists changes to the save query web service for storage.
		RestSavedQueryProvider sqWsProvider = new RestSavedQueryProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		SavedQuery syncedSavedQuery =
				sqWsProvider.saveSavedQuery(savedQuery, constants.getSavedQueryCreateSavedQueryWebServiceURL());

		log.info("Sending the related entity maps (permissions) to a web service to be saved to the database...");

		// Update the listed entities with the ID of the SavedQuery object from the web service.
		for (EntityMap em : entityList) {
			em.setEntityId(syncedSavedQuery.getId());
		}

		// Persists permission changes to the account web service.
		RestQueryAccountProvider accountProvider = new RestQueryAccountProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		List<EntityMap> syncedPermissions =
				accountProvider.saveEntityList(entityList, constants.updateSavedQueryPermissionListWebServiceURL());

		// Update the entity map list.
		entityList.clear();
		entityList.addAll(syncedPermissions);

		return syncedSavedQuery;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeSavedQuery(Long savedQueryId) {
		RestSavedQueryProvider restSavedQueryProvider = new RestSavedQueryProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		restSavedQueryProvider.removeSavedQuery(savedQueryId, constants.getSavedQueryRemoveSavedQueryWebServiceURL());
	}

	/**
	 * Gets a SavedQueryList from the Rest Service. NOTE: a SavedQueryList does not have query details. It is an
	 * abbreviated object.
	 * 
	 * @param accountName username of the user whose saved queries should be retrieved
	 */
	public List<SavedQuery> getSavedQueries(String accountName) {

		List<SavedQuery> savedQueries = null;
		RestSavedQueryProvider restSavedQueryProvider = new RestSavedQueryProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		String url = constants.getUserSavedQueryListWebServiceURL();
		savedQueries = restSavedQueryProvider.getSavedQueries(accountName, url);

		return savedQueries;
	}


	public SavedQuery getSavedQueryById(Long queryId) {
		RestSavedQueryProvider restSavedQueryProvider = new RestSavedQueryProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		return restSavedQueryProvider.getSavedQueryById(queryId, constants.getSavedQueryGetWebServiceURL());
	}


	/**
	 * Retrieves a list of savedQueries query using the query tool rest service See the QueryToolRestService.java class
	 * 
	 * @param qtDatacartManager
	 */
	public Map<Long, MetaStudy> getMetaStudies() {
		RestSavedQueryProvider restSavedQueryProvider = new RestSavedQueryProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		return restSavedQueryProvider.getMetaStudies(constants.getUserMetaStudyListWebServiceURL());
	}

	/**
	 * links savedQuery to metaStudy using the query tool rest service See the QueryToolRestService.java class
	 * 
	 * @param qtDatacartManager
	 */
	public void linkSavedQueryMetaStudy(long metaStudyId, SavedQuery clonedSavedQuery) {
		RestSavedQueryProvider restSavedQueryProvider = new RestSavedQueryProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		try {
			restSavedQueryProvider.linkSavedQueryMetaStudy(metaStudyId, clonedSavedQuery.getId(),
					constants.linkSavedQueryMetaStudyServiceURL());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isQueryFileNameUniquePerMetaStudy(String fileName, long metaStudyId) {

		RestSavedQueryProvider restSavedQueryProvider = new RestSavedQueryProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));
		return restSavedQueryProvider.isFileNameUniquePerMetaStudy(fileName, metaStudyId,
				constants.getSavedQueryFileNameUniqueWebServiceURL());
	}

	@Override
	public SavedQuery getSavedQueryByNameAndMetaStudy(String queryName, long metaStudyId) {

		RestSavedQueryProvider restSavedQueryProvider = new RestSavedQueryProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));
		return restSavedQueryProvider.getSavedQueryByNameAndMetaStudy(queryName, metaStudyId,
				constants.getSavedQueryByNameAndMetaStudyPath());
	}


	@Override
	public boolean isQuerySavedNameUniquePerMetaStudy(String queryName, long metaStudyId) {

		RestSavedQueryProvider restSavedQueryProvider = new RestSavedQueryProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));
		return restSavedQueryProvider.isSavedQueryUniquePerMetaStudy(queryName, metaStudyId,
				constants.getSavedQueryNameUniquePerMetaStudyPath());
	}

	@Override
	public SavedQuery saveSavedQuery(DataCart dataCart, SavedQuery savedQuery) {

		log.info("Saving data cart to JSON...");

		JsonObject dataCartJson = DataCartUtil.getDataCartToSavedQueryJson(dataCart, savedQuery);
		savedQuery.setQueryData(dataCartJson.toString());

		log.info("Calling web service to save the saved query meta data to the database...");

		// Persists changes to the save query web service for storage.
		RestSavedQueryProvider sqWsProvider = new RestSavedQueryProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		SavedQuery syncedSavedQuery =
				sqWsProvider.saveSavedQuery(savedQuery, constants.getSavedQueryCreateSavedQueryWebServiceURL());

		return syncedSavedQuery;
	}

	public boolean isQueryLinkedToMetaStudy(long savedQueryId) {

		RestSavedQueryProvider restSavedQueryProvider = new RestSavedQueryProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));
		return restSavedQueryProvider.isQueryLinkedToMetaStudy(savedQueryId, constants.getQueryLinkedToMetaStudyPath());
	}
	
}
