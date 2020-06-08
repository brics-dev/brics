package gov.nih.tbi.query.dao;

import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import gov.nih.tbi.MetadataStore;
import gov.nih.tbi.query.model.SummaryResult;
import gov.nih.tbi.query.model.hibernate.SummaryQuery;


/**
 * Interface for the SQL Database communication that contains the Summary Data Queries.
 * 
 * @author Bill Puschmann
 *
 */
public interface SummaryQueryDao {

	public SummaryQuery getSummaryByName(String shortname);
	
	public List<SummaryQuery> getAllStudySummaryQueriesShortnames(String instance);
	public List<SummaryQuery> getAllProgramSummaryQueriesShortnames(String instance);
	public List<SummaryQuery> getProgramSummaryQueriesShortnames(String instance);
	public List<SummaryQuery> getStudySummaryQueriesShortnames(String instance);
	public Integer getSubjectCountByStudy(String studyId);
	public SummaryResult getWithQuery(String queryString);

}
