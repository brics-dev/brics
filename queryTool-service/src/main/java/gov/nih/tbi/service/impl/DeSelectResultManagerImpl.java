package gov.nih.tbi.service.impl;

import gov.nih.tbi.dao.DeSelectResultDao;
import gov.nih.tbi.pojo.DeSelectSearch;
import gov.nih.tbi.service.DeSelectResultManager;

import java.io.Serializable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

@Component
@Scope("application")
public class DeSelectResultManagerImpl implements DeSelectResultManager, Serializable {

	private static final long serialVersionUID = -6666557346223774677L;
	private static final Logger log = LogManager.getLogger(DeSelectResultManagerImpl.class);

	@Autowired
	private DeSelectResultDao deSelectResultDao;
	
	/** 
	 * This method Calls the DAO and returns JsonArray for population and disease filter options.
	 * @return JsonArray data of population and disease filter options
	 */
	public JsonObject getDeSelectFilterOptions() {
		JsonObject output = new JsonObject();
		JsonArray populations = new JsonArray();
		JsonArray diseases = new JsonArray();

		ResultSet populationResults = deSelectResultDao.getPopulationOptions();
		while (populationResults.hasNext()) {
			QuerySolution row = populationResults.next();
			RDFNode populationNode = row.get("population");
			if (populationNode != null) {
				populations.add(new JsonPrimitive(populationNode.asLiteral().getString()));
			}
		}

		ResultSet diseaseResults = deSelectResultDao.getDiseaseOption();
		while (diseaseResults.hasNext()) {
			QuerySolution row = diseaseResults.next();
			RDFNode diseaseNode = row.get("disease");
			if (diseaseNode != null) {
				diseases.add(new JsonPrimitive(diseaseNode.asLiteral().getString()));
			}
		}

		output.add("populations", populations);
		output.add("diseases", diseases);
		return output;
	}

	
	public JsonArray searchDeSelect(DeSelectSearch searchParameters) {
		log.debug("--- beginning data element selected query ---");
		
		ResultSet elements = deSelectResultDao.deSelectQuery(searchParameters);

		JsonArray output = new JsonArray();
		while (elements.hasNext()) {
			QuerySolution element = elements.next();
			JsonArray rowJson = new JsonArray();
			RDFNode deUrl = element.get("de");
			RDFNode classification = element.get("classification");
			RDFNode title = element.get("title");
			RDFNode varName = element.get("varName");

			// check for the required fields
			if (deUrl != null) {
				String checkbox = "<input type=\"checkbox\" value=\"" + deUrl.toString() + "\"></input>";
				String titleOut = (title != null) ? title.asLiteral().toString() : "";
				String varNameOut = (varName != null) ? varName.asLiteral().toString() : "";
				String classificationOut = (classification != null) ? classification.asLiteral().toString() : "";
				
				// for datatables, these have to be in an array, not a map
				rowJson.add(new JsonPrimitive(checkbox));
				rowJson.add(new JsonPrimitive(titleOut));
				rowJson.add(new JsonPrimitive(varNameOut));
				rowJson.add(new JsonPrimitive(classificationOut));

				output.add(rowJson);
			}
		}

		return output;
	}
	
	
	/**
	 * Counts the results of buildSearch() WITHOUT pagination. This is the same query but without the order, limits etc.
	 * 
	 * @param searchParameters
	 * @return
	 */
	public int countQueryElements(DeSelectSearch searchParameters) {
		log.debug("--- beginning data element count query ---");

		ResultSet results = deSelectResultDao.deSelectCountQuery(searchParameters);

		int output = 0;
		if (results.hasNext()) {
			QuerySolution first = results.next();
			String count = first.get("count").asLiteral().toString();
			String[] countParts = count.split("\\^\\^");
			output = Integer.parseInt(countParts[0]);
		}
		
		return output;
	}
}
