package gov.nih.tbi.dao.impl;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dao.DeSelectResultDao;
import gov.nih.tbi.pojo.DeSelectSearch;
import gov.nih.tbi.service.RDFStoreManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.query.ResultSet;

@Repository
@Transactional
public class DeSelectResultDaoImpl implements DeSelectResultDao, Serializable {

	private static final long serialVersionUID = 8145508181873288884L;
	
	public static List<String> columns = new ArrayList<String>();
	static {
		// used during sorting. These are the variable names used in the sparql query
		columns.add("de");
		columns.add("title");
		columns.add("varName");
		columns.add("classification");
	}

	@Autowired
	private RDFStoreManager rdfStoreManager;
	
	public ResultSet deSelectQuery(DeSelectSearch searchParameters) {
		StringBuffer sb = new StringBuffer();
		sb.append("select * WHERE {").append(QueryToolConstants.NL);
		sb.append("select DISTINCT ?de ?classification ?title ?varName WHERE {").append(QueryToolConstants.NL);
		sb.append(
				"?de <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://ninds.nih.gov/dictionary/ibis/1.0/Element>.")
				.append("?de <http://ninds.nih.gov/repository/fitbir/1.0/Study/facetedDE> ?someForm.")
				.append(QueryToolConstants.NL);

		addFilterClause(sb, searchParameters);

		sb.append("}").append(QueryToolConstants.NL);
		sb.append("ORDER BY " + searchParameters.getSortDirection() + "(lcase(str(?"
				+ columns.get(searchParameters.getSortColumnIndex()) + ")))");
		sb.append("}");
		
		sb.append("LIMIT " + searchParameters.getCountPerPage()).append(QueryToolConstants.NL);
		sb.append("OFFSET " + searchParameters.getPageOffset()).append(QueryToolConstants.NL);

		String query = sb.toString();
		
		ResultSet elements = rdfStoreManager.querySelect(query);
		
		return elements;
	}

	
	public ResultSet deSelectCountQuery(DeSelectSearch searchParameters) {

		StringBuffer sb = new StringBuffer();
		sb.append("select (count(distinct ?de) as ?count) WHERE {").append(
				QueryToolConstants.NL);
		sb.append(
				"?de <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://ninds.nih.gov/dictionary/ibis/1.0/Element>.")
				.append("?de <http://ninds.nih.gov/repository/fitbir/1.0/Study/facetedDE> ?someForm.")
				.append(QueryToolConstants.NL);

		this.addFilterClause(sb, searchParameters);

		sb.append("}").append(QueryToolConstants.NL);
		
		String query = sb.toString();
		ResultSet results = rdfStoreManager.querySelect(query);
		
		return results;
	}
	

	private void addFilterClause(StringBuffer sb, DeSelectSearch searchParameters) {
		
		// stores if we need to write "UNION" before each filter
		boolean union = false;
		
		// element type filters
		List<String> elementTypes = searchParameters.getElementTypes();
		if (elementTypes != null && !elementTypes.isEmpty()) {
			for (String elementType : elementTypes) {
				if (union) {
					sb.append(QueryToolConstants.NL).append(" UNION ").append(QueryToolConstants.NL);
				}
				sb.append("{ ?de " + keyToPredicate("category") + " \"" + elementType + "\". }");
				sb.append(QueryToolConstants.NL);
				union = true;
			}
		}

		// disease filters
		List<String> diseases = searchParameters.getDiseases();
		if (diseases != null && !diseases.isEmpty()) {
			for (String disease : diseases) {
				if (union) {
					sb.append(QueryToolConstants.NL).append(" UNION ").append(QueryToolConstants.NL);
				}
				sb.append("{ ?de " + keyToPredicate("disease") + " \"" + disease + "\". }");
				sb.append(QueryToolConstants.NL);
				union = true;
			}
		}

		// population filters
		List<String> populations = searchParameters.getPopulations();
		if (populations != null && !populations.isEmpty()) {
			for (String population : populations) {
				if (union) {
					sb.append(QueryToolConstants.NL).append(" UNION ").append(QueryToolConstants.NL);
				}
				sb.append("{ ?de " + keyToPredicate("population") + " \"" + population + "\". }");
				sb.append(QueryToolConstants.NL);
				union = true;
			}
		}

		// the lines that should always be in there - not UNION
		sb.append("OPTIONAL { ?de " + keyToPredicate("label") + " ?label. }").append(QueryToolConstants.NL);
		sb.append("OPTIONAL { ?de " + keyToPredicate("permissibleValue") + " ?permissibleValue. }").append(
				QueryToolConstants.NL);
		sb.append("OPTIONAL { ?de " + keyToPredicate("description") + " ?description. }").append(QueryToolConstants.NL);
		sb.append("OPTIONAL { ?de " + keyToPredicate("keyword") + " ?keyword. }").append(QueryToolConstants.NL);
		sb.append("OPTIONAL { ?de " + keyToPredicate("category") + " ?classification . }")
				.append(QueryToolConstants.NL);
		sb.append(" ?de " + keyToPredicate("title") + " ?title . ").append(QueryToolConstants.NL);
		sb.append("OPTIONAL { ?de " + keyToPredicate("varName") + " ?varName . }").append(QueryToolConstants.NL);

		// search location filters
		// I use this syntax so I can refrain from putting in the UNION for the first entry
		String searchPhrase = searchParameters.getSearchPhrase();
		List<String> searchLocations = searchParameters.getSearchLocations();
		if (searchPhrase != null && !searchPhrase.trim().equals("") && searchLocations != null && !searchLocations.isEmpty()) {
			sb.append("FILTER (").append(QueryToolConstants.NL);
			for (int i = 0; i < searchLocations.size(); i++) {
				String searchLocation = searchLocations.get(i);
				if (i != 0) {
					sb.append(QueryToolConstants.NL).append(" || ");
				}

				if (searchParameters.getWholeWordSearch() == "true") {
					// whole word match, case insensitive
					sb.append("regex(?" + searchLocation + ", \"" + searchPhrase + "\", \"i\")");
				} else {
					// partial word match
					sb.append("regex(?" + searchLocation + ", \"(.*)" + searchPhrase + "(.*)\", \"i\")");
				}
			}
			sb.append(")").append(QueryToolConstants.NL);
		}
	}
	
	
	/**
	 * Converts the given search parameter string to its predicate format
	 * 
	 * @param key the key to find
	 * @return String predicate string if found or empty string if not found
	 */
	private String keyToPredicate(String key) {
		if (key.equals("disease")) {
			return "element:elementDisease";
		} else if (key.equals("population")) {
			return "element:population";
		} else if (key.equals("keyword")) {
			return "element:keyword";
		} else if (key.equals("permissibleValue")) {
			return "element:permissibleValue";
		} else if (key.equals("title")) {
			return "element:title";
		} else if (key.equals("label")) {
			return "element:label";
		} else if (key.equals("varName")) {
			return "element:elementName";
		} else if (key.equals("description")) {
			return "element:description";
		} else if (key.equals("category")) {
			return "element:category";
		} else if (key.equals("externalId")) {
			return "element:externalId";
		}
		return "";
	}
	
	
	public ResultSet getPopulationOptions() {
		
		StringBuffer populationQuery = new StringBuffer();
		populationQuery.append("select DISTINCT ?population").append(QueryToolConstants.NL);
		populationQuery.append("WHERE {").append(QueryToolConstants.NL);
		populationQuery.append("?o element:population ?population.");
		populationQuery.append(QueryToolConstants.NL);
		populationQuery.append("}");

		ResultSet populationResults =
				rdfStoreManager.querySelect(populationQuery.toString());
		return populationResults;
	}
	
	public ResultSet getDiseaseOption() {
		
		StringBuffer diseasesQuery = new StringBuffer();
		diseasesQuery.append("select DISTINCT ?disease").append(QueryToolConstants.NL);
		diseasesQuery.append("WHERE {").append(QueryToolConstants.NL);
		diseasesQuery.append("?o element:elementDisease ?disease.").append(QueryToolConstants.NL);
		diseasesQuery.append("}");

		ResultSet diseaseResults =
				rdfStoreManager.querySelect(diseasesQuery.toString());
		return diseaseResults;
	}
	
}
