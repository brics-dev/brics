
package gov.nih.tbi.commons.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;

import com.google.common.collect.Multimap;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.query.model.SummaryResult;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.query.model.hibernate.SummaryQuery;
import gov.nih.tbi.repository.model.PublicSubmittedForm;
import gov.nih.tbi.repository.model.StudySubmittedForm;
import gov.nih.tbi.repository.model.hibernate.Study;

public interface QueryToolManager {
	 public SavedQuery getSavedQueryBySavedQueryId(Account account, Long savedQueryId) throws UserPermissionException;
	
	 /**
	  * This method returns a list of SavedQuery without populating their XML.
	  * 
	  * @param ids - SavedQuery ids
	  * @return - a list of SavedQuery objects without populating their XML
	  */
	 public List<SavedQuery> getSavedQueriesByIds( Set<Long> ids);
	 
	 
	/**
	 * Returns all SavedQuery instances. This method is mainly used by ROLE_ADMIN user who has full access to all saved
	 * queries.
	 * 
	 * @return all SavedQuery instances.
	 */
	public List<SavedQuery> getAllSavedQueries();

	/**
	 * Saves the saved query to the database.
	 * 
	 * @param query - The saved query object to save.
	 * @param account - The account of the user doing the saving.
	 * @return The updated SavedQuery object that is synced with the database.
	 * @throws IllegalArgumentException When the given account does not have the permission to save a query.
	 */
	public SavedQuery saveDefinedQuery(SavedQuery query, Account account) throws IllegalArgumentException;

	/**
	 * Removes the saved query and any associated entity maps from the database.
	 * 
	 * @param savedQueryId - The ID of the saved query to be removed.
	 * @throws HibernateException When there is an error removing the saved query and/or its entity maps from the
	 *         database.
	 */
	public void removeSavedQuery(Long savedQueryId);
	 
	public boolean isSavedQueryNameUnique(String savedQueryName);
	 
	 /**
	  * This method requests summary data based on the shortname of the summary query.
	  * 
	  * @param shortname
	  * @return
	  */
	 public SummaryQuery getSummaryByName(String shortname);
	
	 public SummaryResult getSummaryData(String shortname, SummaryQuery query);
	 
	 /**
	  * This method requests summary data based on the shortname of the summary query.  It passes in variables
	  * that must be substituted in the query.
	  * 
	  * @param shortname
	  * @param site
	  * @param study
	  * @param number
	  * @param text
	  * @return
	  */
	 public SummaryResult getSummaryData(String shortname, SummaryQuery query, String site, String study, Long number, String text);
	 
	 public SummaryResult getSparqlSummaryData(String shortname);
	 
	 public SummaryResult getSparqlSummaryData(String shortname, Long number);
	 
	 public SummaryResult getSparqlSummaryData(String shortname, String number);
	 
	 public SummaryResult getSparqlSummaryData(String shortname, String site, String study, Long number, String text);
	 
	 /**
	  * Gets all study titles from the query RDF graph
	  * 
	  * @return List of string titles of all studies
	  */
	 public List<String> getAllStudyTitles();
	 
	 public List<SummaryQuery> getAllStudySummaryQueriesShortnames(String instance);
	 public List<SummaryQuery> getAllProgramSummaryQueriesShortnames(String instance);   
	 
	 public List<SavedQuery> searchSavedQuery(Set<Long> savedQueryIdsList, String savedQueryName, 
			 String savedQueryDescription, Date startDateRange, Date endDateRange, boolean includeCopies);
	 
	 public void migrateAllSavedQueries();
	  
	 public boolean isSavedQueryUniquePerMetaStudy(String savedQueryName,Long metaStudyId);

	 public boolean isSavedQueryFileNameUniquePerMetaStudy(String savedQueryFileName, Long metaStudyId);
	 
	 public boolean isQueryLinkedToMetaStudy(Long savedQueryId);
	 
	 public SavedQuery getSavedQueryByNameAndMetaStudy(String savedQueryName,Long metaStudyId);
	 
	 public List<StudySubmittedForm> getStudySubmittedForms(String studyTitle);
	 
	 public Multimap<Long, StudySubmittedForm> getAllStudySubmittedForms();
	 
	 public List<PublicSubmittedForm> getAllPublicSubmittedForms();
	 
	 public Integer getRowCountByStudy(String studyId);
	 
	 public Integer getSubjectCountByStudy(String studyId);
	 
	 public Integer getFormCountByStudy(String studyId);
	 
	 public String getLastSubmitDateByStudy(String studyId);
	 
	/**
	 * Gets a list of all studies in the SPARQL graph with only title and ID filled in
	 * 
	 * @return
	 */
	public List<Study> getAllStudies();
	
	
	/**
	 * Gets a Study with just title and id filled in by title
	 * 
	 * @param title
	 * @return
	 */
	public Study getBasicStudyByTitle(String title);

	
	/**
	 * Gets a Study with just title and id filled in by ID
	 * 
	 * @param id
	 * @return
	 */
	public Study getBasicStudyById(Long id);

	
	
	
}
