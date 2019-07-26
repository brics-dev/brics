package gov.nih.nichd.ctdb.workspace.domain;

import org.json.JSONArray;

public class EformsSelected {
	
	private String name;
	private String shortName;
	private JSONArray deJson;
	
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public JSONArray getDeJson() {
		return deJson;
	}
	public void setDeJson(JSONArray deJson) {
		this.deJson = deJson;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	

}
