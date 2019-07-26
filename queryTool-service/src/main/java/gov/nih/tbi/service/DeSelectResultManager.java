package gov.nih.tbi.service;

import gov.nih.tbi.pojo.DeSelectSearch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface DeSelectResultManager {

	public JsonObject getDeSelectFilterOptions();

	
	public JsonArray searchDeSelect(DeSelectSearch searchParameters);
	
	
	/**
	 * Counts the results of buildSearch() WITHOUT pagination. This is the same query but without the order, limits etc.
	 * 
	 * @param searchParameters
	 * @return
	 */
	public int countQueryElements(DeSelectSearch searchParameters);

}
