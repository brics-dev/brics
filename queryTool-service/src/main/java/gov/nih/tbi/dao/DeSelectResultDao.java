package gov.nih.tbi.dao;

import gov.nih.tbi.pojo.DeSelectSearch;

import com.hp.hpl.jena.query.ResultSet;

public interface DeSelectResultDao {

	public ResultSet deSelectQuery(DeSelectSearch searchParameters);
	
	public ResultSet deSelectCountQuery(DeSelectSearch searchParameters);
	
	public ResultSet getPopulationOptions();
	
	public ResultSet getDiseaseOption();
	
}
