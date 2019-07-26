package gov.nih.tbi.query.dao;

import java.util.List;

import gov.nih.tbi.query.model.hibernate.SummaryQuery;


/**
 * Interface for the SQL Database communication that contains the Summary Data Queries.
 * 
 * @author Bill Puschmann
 *
 */
public interface SummaryQueryDao {

	public SummaryQuery getSummaryByName(String shortname);
	
	public List<SummaryQuery> getAllStudySummaryQueriesShortnames();
	public List<SummaryQuery> getAllProgramSummaryQueriesShortnames();

}
