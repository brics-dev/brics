package gov.nih.tbi.query.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the results of a summary query request from Virtuoso.  This is a map of key/values which correlates with "data" and "count" results from Virtuoso.
 * 
 * @author Bill Puschmann
 *
 */
public class SummaryResult {
	
	private Map<String, String> results;
	
	private String jsonResults;
	
	public SummaryResult() {
		this.results = new LinkedHashMap<String, String>();
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

}
