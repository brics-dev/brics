package gov.nih.nichd.ctdb.workspace.domain;

import org.json.JSONArray;

public class ViewDataGraph {

	private String eformName;
	private String dename;
	private JSONArray intervalsJSON;
	private JSONArray scoresJSON;
	
	public String getEformName() {
		return eformName;
	}
	public void setEformName(String eformName) {
		this.eformName = eformName;
	}
	public JSONArray getIntervalsJSON() {
		return intervalsJSON;
	}
	public void setIntervalsJSON(JSONArray intervalsJSON) {
		this.intervalsJSON = intervalsJSON;
	}
	public JSONArray getScoresJSON() {
		return scoresJSON;
	}
	public void setScoresJSON(JSONArray scoresJSON) {
		this.scoresJSON = scoresJSON;
	}
	public String getDename() {
		return dename;
	}
	public void setDename(String dename) {
		this.dename = dename;
	}
	
}
