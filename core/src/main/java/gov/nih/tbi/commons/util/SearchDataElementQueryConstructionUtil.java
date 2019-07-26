package gov.nih.tbi.commons.util;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.exceptions.SemanticSearchException;
import gov.nih.tbi.dictionary.model.BaseDictionaryFacet;
import gov.nih.tbi.dictionary.model.ClassificationFacet;
import gov.nih.tbi.dictionary.model.ClassificationFacetValue;
import gov.nih.tbi.dictionary.model.DateFacet;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.DiseaseFacet;
import gov.nih.tbi.dictionary.model.DiseaseFacetValue;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.StringFacet;

import java.util.ArrayList;
import java.util.Collection;
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
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_StrLowerCase;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This class will construct queries to be used by data element and form structure searches
 * 
 * @author Francis Chen
 */
public class SearchDataElementQueryConstructionUtil extends QueryConstructionUtil {

	static Logger logger = Logger.getLogger(SearchDataElementQueryConstructionUtil.class);

	public static Query getDetailedDataElementSearchQuery(DictionarySearchFacets facets,
			Map<FacetType, Set<String>> searchLocationMap, boolean exactMatch, PaginationData pageData,
			boolean onlyOwned) {

		Query query = getBaseDataElementSearchQuery(facets, searchLocationMap);
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_SHORT_DESCRIPTION_NODE_N, RDFConstants.SHORT_DESCRIPTION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_FORMAT,
				RDFConstants.FORMAT_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_NOTES_NODE_N,
				RDFConstants.NOTES_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_REFERENCES_N,
				RDFConstants.REFERENCES_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_DE_HISTORICAL_NOTES_NODE_N, RDFConstants.HISTORICAL_NOTES_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_GUIDELINES_NODE_N, RDFConstants.GUIDELINES_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_SUBMITTING_CONTACT_INFO_NODE_N,
				RDFConstants.SUBMITTING_CONTACT_INFO_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_SUBMITTING_CONTACT_NAME_NODE_N,
				RDFConstants.SUBMITTING_CONTACT_NAME_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_SUBMITTING_ORG_NAME_NODE_N, RDFConstants.SUBMITTING_ORG_NAME_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_STEWARD_CONTACT_INFO_NODE_N, RDFConstants.STEWARD_CONTACT_INFO_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_STEWARD_CONTACT_NAME_NODE_N, RDFConstants.STEWARD_CONTACT_NAME_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_STEWARD_ORG_NAME_NODE_N, RDFConstants.STEWARD_ORG_NAME_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_EFFECTIVE_DATE_NODE_N, RDFConstants.EFFECTIVE_DATE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_UNTIL_DATE_NODE_N, RDFConstants.UNTIL_DATE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_SEE_ALSO_NODE_N,
				RDFConstants.SEE_ALSO_VARIABLE));

		query.addResultVar(RDFConstants.URI_VARIABLE_NAME);
		query.addResultVar(RDFConstants.SHORT_NAME_VARIABLE);
		query.addResultVar(RDFConstants.TITLE_VARIABLE);
		query.addResultVar(RDFConstants.STATUS_VARIABLE);
		query.addResultVar(RDFConstants.DATE_CREATED_VARIABLE);
		query.addResultVar(RDFConstants.MODIFIED_DATE_VARIABLE);
		query.addResultVar(RDFConstants.CATEGORY_TITLE_VARIABLE);
		query.addResultVar(RDFConstants.VERSION_VARIABLE);
		query.addResultVar(RDFConstants.SHORT_DESCRIPTION_VARIABLE);
		query.addResultVar(RDFConstants.FORMAT_VARIABLE);
		query.addResultVar(RDFConstants.NOTES_VARIABLE);
		query.addResultVar(RDFConstants.REFERENCES_VARIABLE);
		query.addResultVar(RDFConstants.HISTORICAL_NOTES_VARIABLE);
		query.addResultVar(RDFConstants.GUIDELINES_VARIABLE);
		query.addResultVar(RDFConstants.SUBMITTING_CONTACT_INFO_VARIABLE);
		query.addResultVar(RDFConstants.SUBMITTING_CONTACT_NAME_VARIABLE);
		query.addResultVar(RDFConstants.SUBMITTING_ORG_NAME_VARIABLE);
		query.addResultVar(RDFConstants.STEWARD_CONTACT_INFO_VARIABLE);
		query.addResultVar(RDFConstants.STEWARD_CONTACT_NAME_VARIABLE);
		query.addResultVar(RDFConstants.STEWARD_ORG_NAME_VARIABLE);
		query.addResultVar(RDFConstants.EFFECTIVE_DATE_VARIABLE);
		query.addResultVar(RDFConstants.UNTIL_DATE_VARIABLE);
		query.addResultVar(RDFConstants.SEE_ALSO_VARIABLE);
		query.addResultVar(RDFConstants.DESCRIPTION_VARIABLE);
		query.addResultVar(RDFConstants.POPULATION_VARIABLE);

		// add triples for nested list objects
		body.addElement(getLabelTriples());
		body.addElement(getSubdomainTriples());
		body.addElement(getClassificationTriples());
		body.addElement(getKeywordTriples());
		body.addElement(getExternalIdTriples());
		body.addElement(getPermissibleValuesTriples());
		body.addElement(getCategoryTriples());

		// insert facet filters
		query = insertFacetFilters(query, facets, searchLocationMap, exactMatch, onlyOwned);

		// insert pagination and sorting if necessary
		if (pageData != null && pageData.getSort() != null) {
			if (RDFConstants.MODIFIED_DATE_VARIABLE.getName().equals(pageData.getSort())
					|| RDFConstants.DATE_CREATED_VARIABLE.getName().equals(pageData.getSort())) {
				query.addOrderBy(new ExprVar(pageData.getSort()),
						pageData.getAscending() ? Query.ORDER_ASCENDING : Query.ORDER_DESCENDING);
			} else {
				query.addOrderBy(new E_StrLowerCase(new E_Str(new ExprVar(pageData.getSort()))),
						pageData.getAscending() ? Query.ORDER_ASCENDING : Query.ORDER_DESCENDING);
			}
		}

		if (pageData != null && pageData.getPage() != null && pageData.getPageSize() != null) {
			query.setLimit(pageData.getPageSize());
			query.setOffset(pageData.getPageSize() * (pageData.getPage() - 1));
		}

		return query;
	}

	/**
	 * Returns query with only the patterns, filters, offset, limit, and order clauses added. Methods that call this
	 * will have to add things into the select clause for the query to work correctly.
	 * 
	 * @param modifiedDate
	 * @param facetMap
	 * @param searchLocationMap
	 * @return
	 */
	private static Query getBaseDataElementSearchQuery(DictionarySearchFacets facets,
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

		// add latest only pattern
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TYPE_NODE_N,
				RDFConstants.BASE_URI_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.BASE_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_LATEST_NODE_N,
				RDFConstants.URI_NODE));

		// add optional patterns
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_TYPE_NODE_N,
				RDFConstants.ELEMENT_TYPE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_DATE_CREATED_N,
				RDFConstants.DATE_CREATED_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_MODIFIED_DATE_N,
				RDFConstants.MODIFIED_DATE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_BRICS_DESCRIPTION_NODE_N, RDFConstants.DESCRIPTION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N,
				RDFConstants.TITLE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_POPULATION_NODE_N,
				RDFConstants.POPULATION_VARIABLE));

		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_STATUS_NODE_N,
				RDFConstants.STATUS_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_VERSION_NODE_N,
				RDFConstants.VERSION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_CREATED_BY_N,
				RDFConstants.CREATED_BY_VARIABLE));

		return query;
	}

	public static Query getBasicDataElementSearchQuery(DictionarySearchFacets facets,
			Map<FacetType, Set<String>> searchLocationMap, boolean exactMatch, boolean onlyOwned) {

		Query query = getBaseDataElementSearchQuery(facets, searchLocationMap);
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		body.addElement(getCategoryTriples());

		Set<FacetType> addedFacets = new HashSet<FacetType>(facets.getFacetMap().keySet());

		if (addedFacets != null) {
			addedFacets.addAll(searchLocationMap.keySet());
		}

		// the following triples are only necessary when it's respective facet has been added
		if (addedFacets != null && !addedFacets.isEmpty()) {
			if (addedFacets.contains(FacetType.LABELS))
				body.addElement(getLabelTriples());
			if (addedFacets.contains(FacetType.DISEASE))
				body.addElement(getSubdomainTriples());
			if (addedFacets.contains(FacetType.CLASSIFICATION))
				body.addElement(getClassificationTriples());
			if (addedFacets.contains(FacetType.KEYWORDS))
				body.addElement(getKeywordTriples());
			if (addedFacets.contains(FacetType.EXTERNALID))
				body.addElement(getExternalIdTriples());
			if (addedFacets.contains(FacetType.PERMISSIBLEVALUE))
				body.addElement(getPermissibleValuesTriples());
		}

		query = insertFacetFilters(query, facets, searchLocationMap, exactMatch, onlyOwned);

		return query;
	}

	public static ElementOptional getLabelTriples() {

		List<Triple> labelTriples = new ArrayList<Triple>();
		labelTriples.add(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_LABELS_N,
				RDFConstants.LABEL_NODE_VARIABLE));
		labelTriples.add(Triple.create(RDFConstants.LABEL_NODE_VARIABLE, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N,
				RDFConstants.LABEL_VARIABLE));

		return buildGroupOptionalPattern(labelTriples);
	}

	private static ElementOptional getSubdomainTriples() {

		List<Triple> subdomainTriples = new ArrayList<Triple>();
		subdomainTriples.add(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_SUBDOMAIN_NODE_N,
				RDFConstants.SUB_DOMAIN_NODE_VARIABLE));
		subdomainTriples.add(Triple.create(RDFConstants.SUB_DOMAIN_NODE_VARIABLE,
				RDFConstants.PROPERTY_SUBDOMAIN_DISEASE_N, RDFConstants.DISEASE_VARIABLE));
		subdomainTriples.add(Triple.create(RDFConstants.SUB_DOMAIN_NODE_VARIABLE,
				RDFConstants.PROPERTY_SUBDOMAIN_DOMAIN_N, RDFConstants.DOMAIN_VARIABLE));
		subdomainTriples.add(Triple.create(RDFConstants.SUB_DOMAIN_NODE_VARIABLE,
				RDFConstants.PROPERTY_SUBDOMAIN_SUBDOMAIN_N, RDFConstants.SUB_DOMAIN_VARIABLE));
		return buildGroupOptionalPattern(subdomainTriples);
	}

	private static ElementOptional getClassificationTriples() {

		List<Triple> classificationTriples = new ArrayList<Triple>();
		classificationTriples.add(Triple.create(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_CLASSIFICATION_ELEMENT_N, RDFConstants.CLASSIFICATION_NODE_VARIABLE));
		classificationTriples.add(Triple.create(RDFConstants.CLASSIFICATION_NODE_VARIABLE,
				RDFConstants.PROPERTY_CLASSIFICATION_DISEASE_N, RDFConstants.DISEASE_VARIABLE));
		classificationTriples.add(Triple.create(RDFConstants.CLASSIFICATION_NODE_VARIABLE,
				RDFConstants.PROPERTY_CLASSIFICATION_SUBGROUP_N, RDFConstants.SUBGROUP_VARIABLE));
		classificationTriples.add(Triple.create(RDFConstants.CLASSIFICATION_NODE_VARIABLE,
				RDFConstants.PROPERTY_CLASSIFICATION_VALUE_N, RDFConstants.CLASSIFICATION_VARIABLE));
		return buildGroupOptionalPattern(classificationTriples);
	}

	public static ElementOptional getKeywordTriples() {

		List<Triple> keywordTriples = new ArrayList<Triple>();
		keywordTriples.add(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_KEYWORDS_N,
				RDFConstants.KEYWORD_NODE_VARIABLE));
		keywordTriples.add(Triple.create(RDFConstants.KEYWORD_NODE_VARIABLE, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N,
				RDFConstants.KEYWORD_VARIABLE));

		return buildGroupOptionalPattern(keywordTriples);
	}

	public static ElementOptional getExternalIdTriples() {

		List<Triple> externalIdTriples = new ArrayList<Triple>();

		externalIdTriples.add(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_EXTERNAL_ID,
				RDFConstants.EXTERNAL_ID_NODE_VARIABLE));
		externalIdTriples.add(Triple.create(RDFConstants.EXTERNAL_ID_NODE_VARIABLE,
				RDFConstants.PROPERTY_EXTERNAL_ID_VALUE, RDFConstants.EXTERNAL_ID_VARIABLE));
		return buildGroupOptionalPattern(externalIdTriples);
	}

	/**
	 * Returns the optional triples block for permissible values
	 * 
	 * @return
	 */
	public static ElementOptional getPermissibleValuesTriples() {

		List<Triple> permissibleValueTriples = new ArrayList<Triple>();

		permissibleValueTriples.add(Triple.create(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_PERMISSIBLE_VALUE_N, RDFConstants.PERMISSIBLE_VALUE_NODE_VARIABLE));
		permissibleValueTriples.add(Triple.create(RDFConstants.PERMISSIBLE_VALUE_NODE_VARIABLE, RDFS.label.asNode(),
				RDFConstants.PERMISSIBLE_VALUE_VARIABLE));
		permissibleValueTriples.add(Triple.create(RDFConstants.PERMISSIBLE_VALUE_NODE_VARIABLE,
				RDFConstants.PROPERTY_BRICS_DESCRIPTION_NODE_N, RDFConstants.PERMISSIBLE_VALUE_DESCRIPTION_VARIABLE));

		return buildGroupOptionalPattern(permissibleValueTriples);
	}

	public static ElementOptional getCategoryTriples() {

		Collection<Triple> categorySet = new HashSet<Triple>();
		categorySet.add(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_CATEGORY_NODE_N,
				RDFConstants.CATEGORY_URI_VARIABLE));
		categorySet.add(Triple.create(RDFConstants.CATEGORY_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N,
				RDFConstants.CATEGORY_TITLE_VARIABLE));
		categorySet.add(Triple.create(RDFConstants.CATEGORY_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				RDFConstants.CATEGORY_SHORT_VARIABLE));
		return buildGroupOptionalPattern(categorySet);
	}

	private static Query insertFacetFilters(Query query, DictionarySearchFacets facets,
			Map<FacetType, Set<String>> searchLocationMap, boolean exactMatch, boolean onlyOwned) {

		ElementGroup body = (ElementGroup) query.getQueryPattern();

		for (Entry<FacetType, BaseDictionaryFacet> facetEntry : facets.getFacetMap().entrySet()) {
			FacetType facetType = facetEntry.getKey();
			BaseDictionaryFacet facet = facetEntry.getValue();

			if (facetType != null) {
				switch (facetType) {
					case PERMISSIONS:
						if (facet instanceof StringFacet) {
							String variable = facet.getType().getName();
							List<String> permissionValues = ((StringFacet) facet).getValues();

							// if (values != null && !values.isEmpty())
							// body.addElement(QueryConstructionUtil.buildValuesSubQuery(RDFConstants.SHORT_NAME_VARIABLE,
							// values));

							LinkedList<Expr> listOr = new LinkedList<Expr>();

							// check to see if any values are contained in the SHORT_NAME list - don't include this part
							// of
							// the
							// filter if there aren't any. The "published" DEs will still be included.
							if (permissionValues.size() > 0)// && values.size() < 1000)
							{
							    
							    List<List<String>> permissionsValuesList = subList(permissionValues, 4000);
                                                            for(List<String> pvList:permissionsValuesList){
                                                                
                                                                listOr.add(isOneOfUri(variable, pvList).getExpr());
                                                            }
							        

								if (!onlyOwned) {
									listOr.add(new E_Equals(new E_Str(new ExprVar(RDFConstants.STATUS_VARIABLE)),
											new NodeValueString("Published")));
									listOr.add(new E_Equals(new E_Str(new ExprVar(RDFConstants.STATUS_VARIABLE)),
											new NodeValueString("Awaiting Publication")));
								}
																    
								body.addElementFilter(new ElementFilter(filterListToOrs(listOr)));
								
							} else if (onlyOwned) // if we only want owned and permission list is empty
							{
								permissionValues.add(CoreConstants.EMPTY_STRING); // add an empty string to the filter
																					 // so it will
								// return 0
								// results.
								listOr.add(isOneOfUri(variable, permissionValues).getExpr());
								body.addElementFilter(new ElementFilter(filterListToOrs(listOr)));
							} else {
								listOr.add(new E_Equals(new E_Str(new ExprVar(RDFConstants.STATUS_VARIABLE)),
										new NodeValueString("Published")));
								
								listOr.add(new E_Equals(new E_Str(new ExprVar(RDFConstants.STATUS_VARIABLE)),
										new NodeValueString("Awaiting Publication")));
								
								if(listOr.size() > 5000) {
									listOr = (LinkedList<Expr>) listOr.subList(0, 5000);
								}
								
								body.addElementFilter(new ElementFilter(filterListToOrs(listOr)));
							}
						}
						break;
					case MODIFIED_DATE:
						if (facet instanceof DateFacet) {
							Date date = ((DateFacet) facet).getDate();

							String dateStr = null;

							try {
								dateStr = BRICSTimeDateUtil.formatDateTime(date);
							} catch (DateParseException e) {
								e.printStackTrace();
								logger.error(
										"Modified date facet being passed from the action is in the wrong format.");
							}

							body.addElementFilter(QueryConstructionUtil
									.greaterThanDate(new ExprVar(RDFConstants.MODIFIED_DATE_VARIABLE), dateStr));
						}
						break;
					case PERMISSIBLEVALUE:
						if (facet instanceof StringFacet) {
							List<String> permissibleValueValues = ((StringFacet) facet).getValues();
							if (permissibleValueValues != null && !permissibleValueValues.isEmpty())
								body.addElementFilter(
										isOneOfUri(RDFConstants.PERMISSIBLE_VALUE_DESCRIPTION_VARIABLE.getName(),
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
					case CLASSIFICATION:
						if (facet instanceof ClassificationFacet) {
							List<ClassificationFacetValue> values = ((ClassificationFacet) facet).getValues();
							if (!values.isEmpty()) {
								body.addElementFilter(buildClassificationFilter(values));
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
		if (exactMatch) {
			for (Entry<FacetType, Set<String>> searchLocationMapEntry : searchLocationMap.entrySet()) {
				FacetType facet = searchLocationMapEntry.getKey();
				Set<String> boundaryValues = new HashSet<String>();
				if (searchLocationMapEntry.getValue() != null && !searchLocationMapEntry.getValue().isEmpty()) {
					for (String value : searchLocationMapEntry.getValue()) {
						boundaryValues.add("\b" + value + "\b");
					}
				}

				exprList.add(multiRegexFilter(facet.getName(), boundaryValues));
			}
		} else {
			for (Entry<FacetType, Set<String>> searchLocationMapEntry : searchLocationMap.entrySet()) {
				FacetType facet = searchLocationMapEntry.getKey();

				if (searchLocationMapEntry.getValue() != null && !searchLocationMapEntry.getValue().isEmpty()) {
					Set<String> searchTerms = QueryConstructionUtil.processSearchTerms(searchLocationMapEntry.getValue());
					exprList.add(multiRegexFilter(facet.getName(), searchTerms));
				}
			}
		}

		if (!exprList.isEmpty()) {
			body.addElementFilter(new ElementFilter(filterListToOrs(exprList)));
		}

		return query;
	}

	public static final Query buildSearchDataElementQuery(DictionarySearchFacets facets,
			Map<FacetType, Set<String>> searchLocationMap, boolean exactMatch, PaginationData pageData,
			boolean onlyOwned) {

		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.setDistinct(true);

		Query subQuery = getBasicDataElementSearchQuery(facets, searchLocationMap, exactMatch, onlyOwned);

		subQuery.addResultVar(RDFConstants.URI_VARIABLE_NAME);
		subQuery.addResultVar(RDFConstants.SHORT_NAME_VARIABLE);
		subQuery.addResultVar(RDFConstants.TITLE_VARIABLE);
		subQuery.addResultVar(RDFConstants.STATUS_VARIABLE);
		subQuery.addResultVar(RDFConstants.DATE_CREATED_VARIABLE);
		subQuery.addResultVar(RDFConstants.MODIFIED_DATE_VARIABLE);
		subQuery.addResultVar(RDFConstants.CATEGORY_TITLE_VARIABLE);
		subQuery.addResultVar(RDFConstants.VERSION_VARIABLE);

		query.addResultVar(RDFConstants.URI_VARIABLE_NAME);
		query.addResultVar(RDFConstants.SHORT_NAME_VARIABLE);
		query.addResultVar(RDFConstants.TITLE_VARIABLE);
		query.addResultVar(RDFConstants.STATUS_VARIABLE);
		query.addResultVar(RDFConstants.DATE_CREATED_VARIABLE);
		query.addResultVar(RDFConstants.MODIFIED_DATE_VARIABLE);
		query.addResultVar(RDFConstants.CATEGORY_TITLE_VARIABLE);
		query.addResultVar(RDFConstants.VERSION_VARIABLE);

		if (pageData != null && pageData.getSort() != null) {
			if (RDFConstants.MODIFIED_DATE_VARIABLE.getName().equals(pageData.getSort())
					|| RDFConstants.DATE_CREATED_VARIABLE.getName().equals(pageData.getSort())) {
				subQuery.addOrderBy(new ExprVar(pageData.getSort()),
						pageData.getAscending() ? Query.ORDER_ASCENDING : Query.ORDER_DESCENDING);
			} else {
				subQuery.addOrderBy(new E_StrLowerCase(new E_Str(new ExprVar(pageData.getSort()))),
						pageData.getAscending() ? Query.ORDER_ASCENDING : Query.ORDER_DESCENDING);
			}
		}

		if (pageData != null && pageData.getPage() != null && pageData.getPageSize() != null) {
			query.setLimit(pageData.getPageSize());
			query.setOffset(pageData.getPageSize() * (pageData.getPage() - 1));
		}

		query.setQueryPattern(new ElementSubQuery(subQuery));

		return query;
	}

	/**
	 * Returns the query used to count the number of data element search results sans pagination
	 * 
	 * @param modifiedDate
	 * @param facetMap
	 * @param searchLocationMap
	 * @return
	 */
	public static final Query buildDataElementCountQuery(DictionarySearchFacets facets,
			Map<FacetType, Set<String>> searchLocationMap, boolean exactMatch, boolean onlyOwned) {

		// get the entire query without the select part filled out
		Query query = getBasicDataElementSearchQuery(facets, searchLocationMap, exactMatch, onlyOwned);

		query.addResultVar(RDFConstants.COUNT_VARIABLE, new ExprAggregator(RDFConstants.COUNT_VARIABLE,
				AggregatorFactory.createCountExpr(true, new ExprVar(RDFConstants.URI_VARIABLE_NAME))));
		// query.addResultVar(RDFConstants.SHORT_NAME_VARIABLE);
		return query;
	}

	private static ElementFilter buildClassificationFilter(List<ClassificationFacetValue> values) {

		return new ElementFilter(classificationFilterHelper(new LinkedList<ClassificationFacetValue>(values)));
	}

	private static Expr classificationFilterHelper(LinkedList<ClassificationFacetValue> values) {

		if (values.isEmpty()) {
			return null;
		} else if (values.size() == 1) {
			ClassificationFacetValue nextValue = values.pop();
			return classificationFacetToExpression(nextValue);
		} else {
			ClassificationFacetValue nextValue = values.pop();
			return new E_LogicalOr(classificationFacetToExpression(nextValue), classificationFilterHelper(values));
		}
	}

	private static Expr classificationFacetToExpression(ClassificationFacetValue value) {

		String classification = value.getClassification();
		String subgroup = value.getSubgroup();

		E_Equals classificationEquals =
				QueryConstructionUtil.buildEqualsExpression(RDFConstants.CLASSIFICATION_VARIABLE, classification);
		E_Equals subgroupEquals = QueryConstructionUtil.buildEqualsExpression(RDFConstants.SUBGROUP_VARIABLE, subgroup);

		if (subgroupEquals != null && classificationEquals != null) {
			return new E_LogicalAnd(subgroupEquals, classificationEquals);
		} else if (subgroupEquals != null && classificationEquals == null) {
			return subgroupEquals;
		} else {
			throw new SemanticSearchException(
					"Cannot create filter when subgroup or classification for facet is null!");
		}
	}

	public static ElementFilter buildDiseaseFilter(List<DiseaseFacetValue> values) {

		return new ElementFilter(diseaseFilterHelper(new LinkedList<DiseaseFacetValue>(values)));
	}

	private static Expr diseaseFilterHelper(LinkedList<DiseaseFacetValue> values) {

		if (values.isEmpty()) {
			return null;
		} else if (values.size() == 1) {
			DiseaseFacetValue nextValue = values.pop();
			return diseaseFacetToExpression(nextValue);
		} else {
			DiseaseFacetValue nextValue = values.pop();
			return new E_LogicalOr(diseaseFacetToExpression(nextValue), diseaseFilterHelper(values));
		}
	}

	private static Expr diseaseFacetToExpression(DiseaseFacetValue value) {

		String disease = value.getDisease();
		String domain = value.getDomain();
		String subdomain = value.getSubdomain();

		E_Equals diseaseEquals = QueryConstructionUtil.buildEqualsExpression(RDFConstants.DISEASE_VARIABLE, disease);
		E_Equals domainEquals = QueryConstructionUtil.buildEqualsExpression(RDFConstants.DOMAIN_VARIABLE, domain);
		E_Equals subdomainEquals =
				QueryConstructionUtil.buildEqualsExpression(RDFConstants.SUB_DOMAIN_VARIABLE, subdomain);

		if (diseaseEquals != null && domainEquals != null && subdomainEquals != null) {

			return new E_LogicalAnd(diseaseEquals, new E_LogicalAnd(domainEquals, subdomainEquals));
		} else if (diseaseEquals != null && domainEquals != null) {
			return new E_LogicalAnd(diseaseEquals, domainEquals);
		} else if (diseaseEquals != null) {
			return diseaseEquals;
		} else {
			return null;
		}
	}
	
	private static <T> List<List<T>> subList(List<T> list, final int L) {
	    List<List<T>> parts = new ArrayList<List<T>>();
	    final int N = list.size();
	    for (int i = 0; i < N; i += L) {
	        parts.add(new ArrayList<T>(
	            list.subList(i, Math.min(N, i + L)))
	        );
	    }
	    return parts;
	}

}
