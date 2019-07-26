package gov.nih.tbi.pojo;

import com.google.gson.JsonObject;

public class FacetItem {

	private String label;
	private String rdfURI;
	private Integer count;

	public JsonObject toJson() {
		JsonObject facetItemJson = new JsonObject();
		
		facetItemJson.addProperty("label", label);
		facetItemJson.addProperty("rdfURI", rdfURI);
		facetItemJson.addProperty("count", count);
		return facetItemJson;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getRdfURI() {
		return rdfURI;
	}

	public void setRdfURI(String rdfURI) {
		this.rdfURI = rdfURI;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

}
