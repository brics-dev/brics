package gov.nih.tbi.dictionary.dao.sparql;

import gov.nih.tbi.MetadataStore;
import gov.nih.tbi.VirtuosoStore;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.dictionary.dao.sparql.util.DataElementServiceQueryUtil;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.FacetType;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_StrLowerCase;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;

public class DataElementServiceSparqlDao {

	@Autowired
	protected VirtuosoStore virtuosoStore;

	public ResultSet search(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchLocMap,
			PaginationData pageData) {

		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.setDistinct(true);

		Query subQuery = DataElementServiceQueryUtil.getDataElementSearchQuery(facets, searchLocMap);

		subQuery.addResultVar(RDFConstants.URI_VARIABLE_NAME);
		subQuery.addResultVar(RDFConstants.SHORT_NAME_VARIABLE);
		subQuery.addResultVar(RDFConstants.VERSION_VARIABLE);
		subQuery.addResultVar(RDFConstants.TITLE_VARIABLE);
		subQuery.addResultVar(RDFConstants.STATUS_VARIABLE);

		query.addResultVar(RDFConstants.URI_VARIABLE_NAME);
		query.addResultVar(RDFConstants.SHORT_NAME_VARIABLE);
		query.addResultVar(RDFConstants.VERSION_VARIABLE);
		query.addResultVar(RDFConstants.TITLE_VARIABLE);
		query.addResultVar(RDFConstants.STATUS_VARIABLE);

		if (pageData != null && pageData.getSort() != null) {
			subQuery.addOrderBy(new E_StrLowerCase(new E_Str(new ExprVar(pageData.getSort()))),
					pageData.getAscending() ? Query.ORDER_ASCENDING : Query.ORDER_DESCENDING);
		}

		if (pageData != null && pageData.getPage() != null && pageData.getPageSize() != null) {
			subQuery.setLimit(pageData.getPageSize());
			subQuery.setOffset(pageData.getPageSize() * (pageData.getPage() - 1));
		}

		query.setQueryPattern(new ElementSubQuery(subQuery));

		System.out.println(query.toString());
		ResultSet rs = virtuosoStore.querySelect(query, MetadataStore.REASONING, false);

		return rs;
	}
}
