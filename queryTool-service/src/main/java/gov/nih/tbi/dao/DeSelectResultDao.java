package gov.nih.tbi.dao;

import gov.nih.tbi.pojo.DeSelectSearch;
import gov.nih.tbi.pojo.QueryResult;

public interface DeSelectResultDao {

	public QueryResult deSelectQuery(DeSelectSearch searchParameters);
	
	public QueryResult deSelectCountQuery(DeSelectSearch searchParameters);
	
	public QueryResult getPopulationOptions();
	
	public QueryResult getDiseaseOption();
	
}
