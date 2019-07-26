package gov.nih.tbi.dictionary.dao.sparql.util;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.util.QueryConstructionUtil;
import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.commons.util.SearchDataElementQueryConstructionUtil;
import gov.nih.tbi.dictionary.model.BaseDictionaryFacet;
import gov.nih.tbi.dictionary.model.DateFacet;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.DiseaseFacet;
import gov.nih.tbi.dictionary.model.DiseaseFacetValue;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.StringFacet;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;

public class DataElementServiceQueryUtil extends SearchDataElementQueryConstructionUtil {

	private static Logger logger = Logger.getLogger(DataElementServiceQueryUtil.class);

	public static Query getDataElementSearchQuery(DictionarySearchFacets facets,
			Map<FacetType, Set<String>> searchLocationMap) {

		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		query.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDF.type.asNode(), RDFConstants.DATA_ELEMENT_NODE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				RDFConstants.SHORT_NAME_VARIABLE));

		// latest flag
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TYPE_NODE_N,
				RDFConstants.BASE_URI_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.BASE_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_LATEST_NODE_N,
				RDFConstants.URI_NODE));

		// add optional patterns
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N,
				RDFConstants.TITLE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_VERSION_NODE_N,
				RDFConstants.VERSION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_STATUS_NODE_N,
				RDFConstants.STATUS_VARIABLE));
		
		Set<FacetType> addedFacets = new HashSet<FacetType>();
		addedFacets.addAll(facets.getFacetMap().keySet());
		if (searchLocationMap != null && !searchLocationMap.isEmpty()) {
			addedFacets.addAll(searchLocationMap.keySet());
		}
		
		if (addedFacets.contains(FacetType.DATA_TYPE)) {
			body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_TYPE_NODE_N,
					RDFConstants.ELEMENT_TYPE_VARIABLE));
		}
		if (addedFacets.contains(FacetType.MODIFIED_DATE)) {
			body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_MODIFIED_DATE_N,
					RDFConstants.MODIFIED_DATE_VARIABLE));
		}
		if (addedFacets.contains(FacetType.DESCRIPTION)) {
			body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
					RDFConstants.PROPERTY_BRICS_DESCRIPTION_NODE_N, RDFConstants.DESCRIPTION_VARIABLE));
		}
		if (addedFacets.contains(FacetType.POPULATION)) {
			body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_POPULATION_NODE_N,
					RDFConstants.POPULATION_VARIABLE));
		}
		if (addedFacets.contains(FacetType.CATEGORY)) {
			body.addElement(getCategoryTriples());
		}
		if (addedFacets.contains(FacetType.LABELS)) {
			body.addElement(getLabelTriples());
		}
		if (addedFacets.contains(FacetType.KEYWORDS)) {
			body.addElement(getKeywordTriples());
		}
		if (addedFacets.contains(FacetType.EXTERNALID)) {
			body.addElement(getExternalIdTriples());
		}
		if (addedFacets.contains(FacetType.PERMISSIBLEVALUE)) {
			body.addElement(getPermissibleValuesTriples());
		}
		if (addedFacets.contains(FacetType.DISEASE)) {
			List<Triple> diseaseTriples = new ArrayList<Triple>();
			diseaseTriples.add(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_SUBDOMAIN_NODE_N,
					RDFConstants.SUB_DOMAIN_NODE_VARIABLE));
			diseaseTriples.add(Triple.create(RDFConstants.SUB_DOMAIN_NODE_VARIABLE,
					RDFConstants.PROPERTY_SUBDOMAIN_DISEASE_N, RDFConstants.DISEASE_VARIABLE));
			body.addElement(buildGroupOptionalPattern(diseaseTriples));
		}
		
		query = insertFacetFilters(query, facets, searchLocationMap);

		return query;
	}


	private static Query insertFacetFilters(Query query, DictionarySearchFacets facets,
			Map<FacetType, Set<String>> searchLocationMap) {

		ElementGroup body = (ElementGroup) query.getQueryPattern();

		for (Entry<FacetType, BaseDictionaryFacet> facetEntry : facets.getFacetMap().entrySet()) {
			FacetType facetType = facetEntry.getKey();
			BaseDictionaryFacet facet = facetEntry.getValue();

			if (facetType != null) {
				switch (facetType) {
					case MODIFIED_DATE:
						if (facet instanceof DateFacet) {
							Date date = ((DateFacet) facet).getDate();
							String dateStr = null;

							try {
								dateStr = BRICSTimeDateUtil.formatDateTime(date);
							} catch (DateParseException e) {
								e.printStackTrace();
								logger.error("Modified date facet being passed from the action is in the wrong format.");
							}

							body.addElementFilter(QueryConstructionUtil.greaterThanDate(new ExprVar(
									RDFConstants.MODIFIED_DATE_VARIABLE), dateStr));
						}
						break;
					case PERMISSIBLEVALUE:
						if (facet instanceof StringFacet) {
							List<String> permissibleValueValues = ((StringFacet) facet).getValues();
							if (permissibleValueValues != null && !permissibleValueValues.isEmpty())
								body.addElementFilter(isOneOfUri(
										RDFConstants.PERMISSIBLE_VALUE_DESCRIPTION_VARIABLE.getName(),
										permissibleValueValues));
						}
						break;
					case DISEASE:
						if (facet instanceof DiseaseFacet) {
							List<DiseaseFacetValue> values = ((DiseaseFacet) facet).getValues();
							if (!values.isEmpty()) {
								body.addElementFilter(buildDiseaseFilter(values));
							}
						}
						break;
					default:
						if (facet instanceof StringFacet) {
							List<String> values = ((StringFacet) facet).getValues();
							if (values != null && !values.isEmpty())
								body.addElementFilter(isOneOfUri(facetType.getName(), values));
						}
						break;
				}
			}
		}

		LinkedList<Expr> exprList = new LinkedList<Expr>();
		for (Entry<FacetType, Set<String>> searchLocationMapEntry : searchLocationMap.entrySet()) {
			FacetType facet = searchLocationMapEntry.getKey();
			Set<String> values = searchLocationMapEntry.getValue();

			exprList.add(multiRegexFilter(facet.getName(), values));
		}

		if (!exprList.isEmpty()) {
			body.addElementFilter(new ElementFilter(filterListToOrs(exprList)));
		}

		return query;
	}


}
