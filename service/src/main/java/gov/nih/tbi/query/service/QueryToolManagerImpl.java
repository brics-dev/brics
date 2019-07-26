
package gov.nih.tbi.query.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.QueryToolManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.util.SavedQueryUtil;
import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.metastudy.dao.MetaStudyDao;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;
import gov.nih.tbi.query.dao.SavedQueryDao;
import gov.nih.tbi.query.dao.StudySubmittedFormsSparqlDao;
import gov.nih.tbi.query.dao.SummaryQueryDao;
import gov.nih.tbi.query.dao.SummaryQuerySparqlDao;
import gov.nih.tbi.query.model.SummaryResult;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.query.model.hibernate.SummaryQuery;
import gov.nih.tbi.repository.model.PublicSubmittedForm;
import gov.nih.tbi.repository.model.StudySubmittedForm;
import gov.nih.tbi.repository.model.hibernate.Study;


@Service
@Scope("singleton")
public class QueryToolManagerImpl implements QueryToolManager, Serializable
{

    private static final long serialVersionUID = 6998227314175280349L;
    private static final Logger log = Logger.getLogger(QueryToolManagerImpl.class);
	
    @Autowired
    SavedQueryDao savedQueryDao;
    
    @Autowired
    SummaryQueryDao summaryQueryDao;
    
    @Autowired
    SummaryQuerySparqlDao summaryQuerySparqlDao;
	
	@Autowired
    AccountManager accountManager;
	
	@Autowired
	MetaStudyDao metaStudyDao;
	
	@Autowired
	StudySubmittedFormsSparqlDao studySubmittedFormsSparqlDao;

    public List<String> getAllStudyTitles()
    {

        return summaryQuerySparqlDao.getAllStudyTitles();
    }
    
	/**
	 * this method will get a saved query by it's ID.
	 * 
	 */
    public SavedQuery getSavedQueryBySavedQueryName(Account account, Long savedQueryName) throws UserPermissionException
    {

        SavedQuery savedQuery = savedQueryDao.get(savedQueryName);

        if (savedQuery != null && !accountManager.getAccess(account, EntityType.STUDY, savedQuery.getId(), PermissionType.READ))
        {
            throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
        }

        return savedQuery;
    
    }
    
	/**
	 * {@inheritDoc}
	 */
	public SavedQuery saveDefinedQuery(SavedQuery query, Account account) throws IllegalArgumentException {
    	// if the query is a copy and the user is not an administrator, throw an error.
		if (query.getCopyFlag() != null && query.getCopyFlag() && !accountManager.hasRole(account, RoleType.ROLE_ADMIN))  {
    		throw new IllegalArgumentException("Copied Queries cannot be edited by non-admins");
		}

		// Set last updated to now.
		query.setLastUpdated(new Date());

		query = savedQueryDao.save(query);
		
		if (query.getCopyFlag() != null && query.getCopyFlag()) {
			accountManager.registerEntity(account, EntityType.SAVED_QUERY, query.getId(), PermissionType.OWNER);
		}
		
        return query;
    }

	@Override
	public SavedQuery getSavedQueryBySavedQueryId(Account account, Long savedQueryId) throws UserPermissionException {

		SavedQuery savedQuery = savedQueryDao.get(savedQueryId);

		if (savedQuery != null
				&& !accountManager.getAccess(account, EntityType.SAVED_QUERY, savedQuery.getId(), PermissionType.READ)) {
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}
		return savedQuery;
	}
	
	
	public List<SavedQuery> getSavedQueriesByIds( Set<Long> ids) {
		
		// Convert the list of BasicStudies returned into a list of Study objects		
        List<SavedQuery> savedQueries = savedQueryDao.getSavedQueryInfoByIds(ids);
		return savedQueries;
		
	}
	
	
	/**
	 * Returns all SavedQuery instances. This method is mainly used by ROLE_ADMIN user who has full access
	 * to all saved queries.
	 */
	public List<SavedQuery> getAllSavedQueries() {
		
		return savedQueryDao.getAllSavedQueries();
	}

	 
    /**
     * Returns true if the given savedQueryName does not exist in the database, otherwise false.
     * 
     * @param savedQueryName
     * @return true if the given savedQueryName does not exist in the database, otherwise false.
     */
	public boolean isSavedQueryNameUnique(String savedQueryName) {

		return savedQueryDao.isSavedQueryNameUnique(savedQueryName);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeSavedQuery(Long savedQueryId) {
		accountManager.unregisterEntity(EntityType.SAVED_QUERY, savedQueryId);
		savedQueryDao.remove(savedQueryId);
	 }

	 /**
	  * Requests the results of a summary query.
	  * 
	  * @param shortname The query being requested
	 * @return A Summary Result for the query or null.
	  */
	@Override
	public SummaryResult getSummaryData(String shortname) {
		return getSummaryData(shortname, null, null);
	}

	/**
	 * Requests the results of a summary query, including site and study variables
	 * 
	 * @param shortname The query being requested
	 * @param site The phrase to be used as "SITE" in the query.
	 * @param study The phrase to be used as "STUDY" in the query.
	 * @return A Summary Result for the query or null.
	 */
	@Override
	public SummaryResult getSummaryData(String shortname, String site,
			String study) {

		SummaryQuery query = summaryQueryDao.getSummaryByName(shortname);
		
		if (query == null) {
			return null;
		}
		
		if (query.getRequiresSite()) {
			if (site == null || site.isEmpty()) { 
				System.out.println("query failed gracefully because site is missing");
				return null; 
			}
			query.setQuery(modifyQuery(query.getQuery(), "SITE", site));
		}
		if (query.getRequiresStudy()) {
			if (study == null || study.isEmpty()) { 
				System.out.println("query failed because study is missing");
				return null; 
			}
			query.setQuery(modifyQuery(query.getQuery(), "STUDY", study));
		}

		SummaryResult summaryResult = summaryQuerySparqlDao.get(query.getQuery());
		
		return summaryResult;
	}
	
	/**
	  * Requests the results of a summary query for sparql json format.
	  * 
	  * @param shortname The query being requested
	 * @return A Summary Result for the query or null.
	  */
	@Override
	public SummaryResult getSparqlSummaryData(String shortname) {
		return getSparqlSummaryData(shortname, null, null,null,null);
	}
	
	/**
	  * Requests the results of a summary query for sparql json format.
	  * 
	  * @param shortname The query being requested
	 * @return A Summary Result for the query or null.
	  */
	@Override
	public SummaryResult getSparqlSummaryData(String shortname, Long number) {
		return getSparqlSummaryData(shortname, null, null,number, null);
	}
	
	/**
	  * Requests the results of a summary query for sparql json format.
	  * 
	  * @param shortname The query being requested
	 * @return A Summary Result for the query or null.
	  */
	@Override
	public SummaryResult getSparqlSummaryData(String shortname, String text) {
		return getSparqlSummaryData(shortname, null, null,null, text);
	}
	
	/**
	 * Requests the results of a summary query in sparql json format, including site and study variables
	 * 
	 * @param shortname The query being requested
	 * @param site The phrase to be used as "SITE" in the query.
	 * @param study The phrase to be used as "STUDY" in the query.
	 * @return A Summary Result for the query or null in sparql json format.
	 */
	@Override
	public SummaryResult getSparqlSummaryData(String shortname, String site,
			String study, Long number, String text) {

		SummaryQuery query = summaryQueryDao.getSummaryByName(shortname);
		
		if (query == null) {
			return null;
		}
		
		if (query.getRequiresSite()) {
			if (site == null || site.isEmpty()) { 
				System.out.println("query failed gracefully because site is missing");
				return null; 
			}
			query.setQuery(modifyQuery(query.getQuery(), "SITE", site));
		}
		if (query.getRequiresStudy()) {
			if (study == null || study.isEmpty()) { 
				System.out.println("query failed because study is missing");
				return null; 
			}
			query.setQuery(modifyQuery(query.getQuery(), "STUDY", study));
		}
		
		if (query.getRequiresNumber()) {
			if (number == null) { 
				System.out.println("query failed because study is missing");
				return null; 
			}
			
			query.setQuery(modifyQuery(query.getQuery(), "NUMBERVAR", number.toString()));
		}
		
		if (query.getRequiresText()) {
			if (text == null || text.isEmpty()) { 
				System.out.println("query failed because study is missing");
				return null; 
			}
			query.setQuery(modifyQuery(query.getQuery(), "TEXTVAR", text));
		}

		SummaryResult summaryResult = summaryQuerySparqlDao.getResultJson(query.getQuery());
		
		return summaryResult;
	}
	
	public List<SummaryQuery> getAllStudySummaryQueriesShortnames() {
		return summaryQueryDao.getAllStudySummaryQueriesShortnames();
	}
	
	public List<SummaryQuery> getAllProgramSummaryQueriesShortnames() {
		return summaryQueryDao.getAllProgramSummaryQueriesShortnames();
	}
	
	public List<SavedQuery> searchSavedQuery(Set<Long> savedQueryIdsList, String savedQueryName, 
			String savedQueryDescription, Date startDateRange, Date endDateRange, boolean includeCopies){
		return savedQueryDao.searchSavedQuery(savedQueryIdsList, savedQueryName, savedQueryDescription, startDateRange, endDateRange, includeCopies);
	}
	
	
	/**
	 * Helper Method that substitutes one phrase for another
	 * 
	 * @param query The query
	 * @param placeholder The phrase being substituted
	 * @param value The new value for the phrase
	 * @return The new query
	 */
	private String modifyQuery(String query, String placeholder, String value) {
        query = query.replaceAll(placeholder, value);
        return query;
	}
    
	/**
	 * This method loops through all saved queries, populates its xml data, calls util method to convert it to json 
	 * format, and saves the query with json data back into database.
	 */
	public void migrateAllSavedQueries() {
		
		List<SavedQuery> allSavedQueries = savedQueryDao.getAllSavedQueries();
		log.info("Retrieved all saved queries, total count: " + allSavedQueries.size());
	
		int count = 0;
		for (SavedQuery sq : allSavedQueries) {
			log.info("Start migrating saved query: " + sq.getName()); 
			
			// Getting savedQuery with xml populated
			SavedQuery sqFull = savedQueryDao.get(sq.getId()); 
			
			if (ValUtil.isBlank(sqFull.getXml())) {
				log.info("Empty xml found in saved query: " + sq.getName()); 
				continue;
			}
			
			JsonObject json = null;
			try {
				json = SavedQueryUtil.savedQueryXmlToJson(sqFull);
			} catch (Exception e) {
				log.error("Error occured when migrating SavedQuery " + sq.getName());
				e.printStackTrace();
			}
			
			if (json != null) {
				sqFull.setQueryData(json.toString());
				
				savedQueryDao.save(sqFull);
				
				log.info("Finished migrating saved query: " + sq.getName()); 
				count++;
			}
			
		}
		
		log.info("Total saved queries migrated: " + count); 
	}
	
	@Override
	public boolean isSavedQueryFileNameUniquePerMetaStudy(String savedQueryFileName,Long metaStudyId) {

		MetaStudy metaStudy =  metaStudyDao.get(metaStudyId);
		Set<MetaStudyData> metaStudyDatasets =new HashSet<MetaStudyData>();
		
		if(metaStudy.getMetaStudyDataSet()!=null){
			metaStudyDatasets= metaStudy.getMetaStudyDataSet();
		}
			
		for( MetaStudyData metaStudydata:metaStudyDatasets){			
			if(metaStudydata.getUserFile()!=null && (metaStudydata.getUserFile().getName()!=null)){	
				String fileName = StringUtils.removeEnd(metaStudydata.getUserFile().getName(), ServiceConstants.ZIP_EXTENSION);
				if(savedQueryFileName.equalsIgnoreCase(fileName)){
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public boolean isSavedQueryUniquePerMetaStudy(String savedQueryName,Long metaStudyId) {

		MetaStudy metaStudy =  metaStudyDao.get(metaStudyId);
		Set<MetaStudyData> metaStudyDatasets =new HashSet<MetaStudyData>();
		
		if(metaStudy.getMetaStudyDataSet()!=null){
			metaStudyDatasets= metaStudy.getMetaStudyDataSet();
		}
		
		for( MetaStudyData metaStudydata:metaStudyDatasets){
			if(metaStudydata.getSavedQuery()!=null){
				if(savedQueryName.equalsIgnoreCase(metaStudydata.getSavedQuery().getName())){
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public SavedQuery getSavedQueryByNameAndMetaStudy(String savedQueryName,Long metaStudyId) {
		
		SavedQuery savedQuery = null;
		
		MetaStudy metaStudy =  metaStudyDao.get(metaStudyId);
		Set<MetaStudyData> metaStudyDatasets =new HashSet<MetaStudyData>();
		
		if(metaStudy.getMetaStudyDataSet()!=null){
			metaStudyDatasets= metaStudy.getMetaStudyDataSet();
		}
			
		for( MetaStudyData metaStudydata:metaStudyDatasets){
			if(metaStudydata.getSavedQuery()!=null){
				if(savedQueryName.equalsIgnoreCase(metaStudydata.getSavedQuery().getName())){
					savedQuery = metaStudydata.getSavedQuery();
				}
			}
		}
		
		return savedQuery;
	}
	
	@Override
	public List<StudySubmittedForm> getStudySubmittedForms(String studyTitle) {
	
		List <StudySubmittedForm> studySubmittedForms = studySubmittedFormsSparqlDao.get(studyTitle);
		
		return studySubmittedForms;
	}
	
	@Override
	public Multimap<String, StudySubmittedForm> getAllStudySubmittedForms() {
		
		Multimap<String, StudySubmittedForm> allStudySubmittedForms = studySubmittedFormsSparqlDao.getAllStudySubmittedForms();
		
		return allStudySubmittedForms;
	}
	
	@Override
	public List<PublicSubmittedForm> getAllPublicSubmittedForms(){
		
		List<PublicSubmittedForm> publicSubmittedFormList = studySubmittedFormsSparqlDao.getAllPublicSubmittedForms();
		
		return publicSubmittedFormList;
	}
	
	@Override
	public Integer getRowCountByStudy(String studyId){
		return studySubmittedFormsSparqlDao.getRowCountByStudy(studyId);
	}
	
	@Override
	public Integer getSubjectCountByStudy(String studyId){
		return studySubmittedFormsSparqlDao.getSubjectCountByStudy(studyId);
	}
	
	@Override
	public Integer getFormCountByStudy(String studyId){
		return studySubmittedFormsSparqlDao.getFormCountByStudy(studyId);
	}
	
	@Override
	public String getLastSubmitDateByStudy(String studyId){
		return studySubmittedFormsSparqlDao.getLastSubmitDateByStudy(studyId);		
	}
	
	
	public List<Study> getAllStudies() {
		return summaryQuerySparqlDao.getAllStudies();
    }
	
	public Study getBasicStudyById(Long id) {
		return summaryQuerySparqlDao.getBasicStudyById(id);
	}

	public Study getBasicStudyByTitle(String title) {
		return summaryQuerySparqlDao.getBasicStudyByTitle(title);
	}
	
}
