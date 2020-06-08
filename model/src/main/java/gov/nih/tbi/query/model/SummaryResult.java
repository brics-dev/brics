package gov.nih.tbi.query.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Holds the results of a summary query request from Virtuoso.  This is a map of key/values which correlates with "data" and "count" results from Virtuoso.
 * 
 * @author Bill Puschmann
 *
 */
public class SummaryResult {
	
	private Map<String, String> results;
	
	private Multimap<String, String> resultsMultiMap;
	
	private String jsonResults;
	
	public SummaryResult() {
		this.results = new LinkedHashMap<String, String>();
		this.resultsMultiMap = ArrayListMultimap.create();
		this.jsonResults = null;
	}

	public SummaryResult(Map<String, String> results) {

		if (this.results == null) {
			this.results = new LinkedHashMap<String, String>();
		} else {
			this.results.clear();
		}

		this.results.putAll(results);
	}


	public Map<String, String> getResults() {
		return results;
	}
	public void setResults(Map<String, String> results) {
		this.results = results;
	}

	public String getJsonResults() {
		return jsonResults;
	}

	public void setJsonResults(String jsonResults) {
		this.jsonResults = jsonResults;
	}

	public Multimap<String, String> getResultsMultiMap() {
		return resultsMultiMap;
	}

	public void setResultsMultiMap(Multimap<String, String> resultsMultiMap) {
		this.resultsMultiMap = resultsMultiMap;
	}

}
