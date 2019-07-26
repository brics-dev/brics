package gov.nih.tbi.commons.util;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.dictionary.model.FormStructureFacet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_StrLowerCase;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.vocabulary.RDF;

public class SearchFormStructureQueryConstructionUtil extends QueryConstructionUtil {

	// list of hard coded FS search locations. These strings should be the variable names of the data to be searched on
	// when a search key is entered.
	private static List<String> FS_SEARCH_LOCATIONS;

	static {
		ArrayList<String> searchLocations = new ArrayList<String>();
		searchLocations.add(RDFConstants.TITLE_VARIABLE.getName());
		searchLocations.add(RDFConstants.DESCRIPTION_VARIABLE.getName());
		searchLocations.add(RDFConstants.SHORT_NAME_VARIABLE.getName());
		searchLocations.add(RDFConstants.CREATED_BY_VARIABLE.getName());

		FS_SEARCH_LOCATIONS = Collections.unmodifiableList(searchLocations);
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
	public static Query getBasicFormStructureSearchQuery(Map<FormStructureFacet, Set<String>> facetMap,
			Set<String> searchTerms, boolean exactMatch, boolean onlyOwned) {

		Query basicFormStructureQuery = QueryFactory.make();
		basicFormStructureQuery.setQuerySelectType();
		basicFormStructureQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		basicFormStructureQuery.setQueryPattern(body);
		// block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFS.Nodes.subClassOf,
		// RDFConstants.FORM_STRUCTURE_NODE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDF.type.asNode(), RDFConstants.FORM_STRUCTURE_NODE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				RDFConstants.SHORT_NAME_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_VERSION_NODE_N,
				RDFConstants.VERSION_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_DISEASE_NODE_N,
				RDFConstants.DISEASE_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TYPE_NODE_N,
				RDFConstants.BASE_URI_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.BASE_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_LATEST_NODE_N,
				RDFConstants.URI_NODE));



		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_STANDARDIZATION_NODE_N,
				RDFConstants.STANDARDIZATION_VARIABLE));

		// adding variables to be selected
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_BRICS_DESCRIPTION_NODE_N, RDFConstants.DESCRIPTION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N,
				RDFConstants.TITLE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_ORGANIZATION_NODE_N,
				RDFConstants.ORGANIZATION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_MODIFIED_DATE_NODE_N,
				RDFConstants.MODIFIED_DATE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_STATUS_NODE_N,
				RDFConstants.STATUS_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_FS_SUBMISSION_TYPE_NODE_N, RDFConstants.SUBMISSION_TYPE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_DATE_CREATED_NODE_N,
				RDFConstants.DATE_CREATED_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_CREATED_BY_NODE_N,
				RDFConstants.CREATED_BY_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_FS_IS_COPYRIGHTED_NODE_N, RDFConstants.IS_COPYRIGHTED_VARIABLE));

		for (Entry<FormStructureFacet, Set<String>> facetEntry : facetMap.entrySet()) {
			FormStructureFacet facet = facetEntry.getKey();
			Set<String> values = facetEntry.getValue();

			if (facet != null && values != null) {
				if (FormStructureFacet.URI.equals(facet)) {
					LinkedList<Expr> listOr = new LinkedList<Expr>();

					if (values.size() > 0) {
						// If there are is a list to filter on then add it to the OR list
						listOr.add(isOneOfUri(facet.getName(), values).getExpr());
						// If onlyOwned is true, that means the list of values includes everything we have permission
						// for. If onlyOwned is not selected that means that public entities are not included.
						if (!onlyOwned) {
							listOr.add(new E_Equals(new E_Str(new ExprVar(RDFConstants.STATUS_VARIABLE)),
									new NodeValueString("Published")));
							listOr.add(new E_Equals(new E_Str(new ExprVar(RDFConstants.STATUS_VARIABLE)),
									new NodeValueString("Shared Draft")));
							listOr.add(new E_Equals(new E_Str(new ExprVar(RDFConstants.STATUS_VARIABLE)),
									new NodeValueString("Archived")));
						}

						body.addElementFilter(new ElementFilter(filterListToOrs(listOr)));
					} else if (onlyOwned) { // if is owned is selected and no uri is owned by the current user
						values.add(CoreConstants.EMPTY_STRING); // add an empty string to the filter so it will return 0
																 // results.
						listOr.add(isOneOfUri(facet.getName(), values).getExpr());
						body.addElementFilter(new ElementFilter(filterListToOrs(listOr)));
					} else {
						listOr.add(new E_Equals(new E_Str(new ExprVar(RDFConstants.STATUS_VARIABLE)),
								new NodeValueString("Published")));
						listOr.add(new E_Equals(new E_Str(new ExprVar(RDFConstants.STATUS_VARIABLE)),
								new NodeValueString("Shared Draft")));
						listOr.add(new E_Equals(new E_Str(new ExprVar(RDFConstants.STATUS_VARIABLE)),
								new NodeValueString("Archived")));

						body.addElementFilter(new ElementFilter(filterListToOrs(listOr)));
					}
				} else if (FormStructureFacet.REQUIRED.equals(facet)) {

					List<String> requiredList = new ArrayList<String>(values);

					if (requiredList.size() == 1) {
						Node currentRequiredNode = NodeFactory.createURI(requiredList.get(0));
						block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_REQUIRED_NODE_N,
								currentRequiredNode));
					} else if (requiredList.size() > 1) {
						ElementUnion union = new ElementUnion();
						body.addElement(union);
						for (String requiredValue : values) {
							Node currentRequiredNode = NodeFactory.createURI(requiredValue);
							ElementTriplesBlock currentBlock = new ElementTriplesBlock();
							union.addElement(currentBlock);
							currentBlock.addTriple(Triple.create(RDFConstants.URI_NODE,
									RDFConstants.PROPERTY_FS_REQUIRED_NODE_N, currentRequiredNode));
						}
					}
				} else if (facet.equals(FormStructureFacet.DISEASE)) {
					LinkedList<Expr> exprList = new LinkedList<Expr>();
					exprList.add(multiRegexFilter(facet.getName(), values));
					body.addElement(new ElementFilter(filterListToOrs(exprList)));
				} else {
					body.addElement(isOneOfUri(facet.getName(), values));
				}
			}
		}

		if (searchTerms != null && !searchTerms.isEmpty()) {
			LinkedList<Expr> exprList = new LinkedList<Expr>();

			if (exactMatch) {
				for (String searchLocationVariable : FS_SEARCH_LOCATIONS) {
					Set<String> boundaryValues = new HashSet<String>();
					for (String value : searchTerms) {
						boundaryValues.add("\b" + value + "\b");
					}

					exprList.add(multiRegexFilter(searchLocationVariable, boundaryValues));
				}
			} else {
				for (String searchLocationVariable : FS_SEARCH_LOCATIONS) {
					if (searchTerms != null && !searchTerms.isEmpty()) {
						searchTerms = QueryConstructionUtil.processSearchTerms(searchTerms);
						exprList.add(multiRegexFilter(searchLocationVariable, searchTerms));
					}
				}
			}

			if (!exprList.isEmpty()) {
				body.addElementFilter(new ElementFilter(filterListToOrs(exprList)));
			}
		}

		return basicFormStructureQuery;
	}

	/**
	 * Returns query used to count the number of search results sans pagination
	 * 
	 * @param facetMap
	 * @param searchTerms
	 * @param pageData
	 * @return
	 */
	public static Query buildFormStructureSearchCountQuery(Map<FormStructureFacet, Set<String>> facetMap,
			Set<String> searchTerms, boolean exactMatch, boolean onlyOwned) {

		Query query = getBasicFormStructureSearchQuery(facetMap, searchTerms, exactMatch, onlyOwned);
		query.addResultVar(RDFConstants.COUNT_VARIABLE, new ExprAggregator(RDFConstants.COUNT_VARIABLE,
				AggregatorFactory.createCountExpr(true, new ExprVar(RDFConstants.URI_VARIABLE_NAME))));
		// query.addResultVar(RDFConstants.SHORT_NAME_VARIABLE);

		return query;
	}

	public static Query buildFormStructureSearchQuery(Map<FormStructureFacet, Set<String>> facetMap,
			Set<String> searchTerms, boolean exactMatch, PaginationData pageData, boolean onlyOwned) {

		Query query = getBasicFormStructureSearchQuery(facetMap, searchTerms, exactMatch, onlyOwned);

		query.addResultVar(RDFConstants.URI_VARIABLE_NAME);
		query.addResultVar(RDFConstants.SHORT_NAME_VARIABLE);
		query.addResultVar(RDFConstants.VERSION_VARIABLE);
		query.addResultVar(RDFConstants.TITLE_VARIABLE);
		query.addResultVar(RDFConstants.STATUS_VARIABLE);
		query.addResultVar(RDFConstants.SUBMISSION_TYPE_VARIABLE);
		query.addResultVar(RDFConstants.MODIFIED_DATE_VARIABLE);
		query.addResultVar(RDFConstants.DESCRIPTION_VARIABLE);
		query.addResultVar(RDFConstants.IS_COPYRIGHTED_VARIABLE);

		if (pageData != null && pageData.getSort() != null) {
			if (RDFConstants.MODIFIED_DATE_VARIABLE.getName().equals(pageData.getSort())) {
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
}
