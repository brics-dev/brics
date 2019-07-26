package gov.nih.tbi.dictionary.dao.sparql;

import gov.nih.tbi.MetadataStore;
import gov.nih.tbi.VirtuosoStore;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.dictionary.dao.sparql.util.FormStructureServiceQueryUtil;
import gov.nih.tbi.dictionary.model.FormStructureFacet;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_StrLowerCase;
import com.hp.hpl.jena.sparql.expr.ExprVar;

@Transactional
@Repository
public class FormStructureServiceSparqlDao {

    @Autowired
    protected VirtuosoStore virtuosoStore;

    public ResultSet search(Map<FormStructureFacet, Set<String>> facetMap, PaginationData pageData) {
		
		Query query = FormStructureServiceQueryUtil.getFormStructureSearchQuery(facetMap);
		
		query.addResultVar(RDFConstants.URI_VARIABLE_NAME);
		query.addResultVar(RDFConstants.SHORT_NAME_VARIABLE);
		query.addResultVar(RDFConstants.VERSION_VARIABLE);
		query.addResultVar(RDFConstants.TITLE_VARIABLE);
		query.addResultVar(RDFConstants.STATUS_VARIABLE);

		if (pageData != null && pageData.getSort() != null) {
			query.addOrderBy(new E_StrLowerCase(new E_Str(new ExprVar(pageData.getSort()))),
					pageData.getAscending() ? Query.ORDER_ASCENDING : Query.ORDER_DESCENDING);
		}

		if (pageData != null && pageData.getPage() != null && pageData.getPageSize() != null) {
			query.setLimit(pageData.getPageSize());
			query.setOffset(pageData.getPageSize() * (pageData.getPage() - 1));
		}

		ResultSet rs = virtuosoStore.querySelect(query, MetadataStore.REASONING, false);
		
		return rs;
	}
}
