package gov.nih.tbi.commons.service;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.dictionary.ws.RestDictionaryProvider;
import gov.nih.tbi.repository.model.DataFile;
import gov.nih.tbi.repository.model.hibernate.Dataset;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DataLoader {

	public DataFile storeDataFile(
			Account account, 
			Dataset dataset, 
			DataFile dataFile, RestDictionaryProvider restDictionaryProvider)
            throws SQLException, MalformedURLException;
	
	/**
	 * Retrieves a map of data based on a SQL query string.
	 * 
	 * @param queryString a SQL query
	 * @return a list of maps representing key/value pairs
	 */
	public List<Map<String, Object>> retrieveObjects(String queryString);
}
