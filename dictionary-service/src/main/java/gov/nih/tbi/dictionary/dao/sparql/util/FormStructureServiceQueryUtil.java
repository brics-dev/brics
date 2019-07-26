package gov.nih.tbi.dictionary.dao.sparql.util;

import gov.nih.tbi.commons.util.QueryConstructionUtil;
import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.dictionary.model.FormStructureFacet;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;

public class FormStructureServiceQueryUtil extends QueryConstructionUtil {

	public static Query getFormStructureSearchQuery(Map<FormStructureFacet, Set<String>> facetMap) {
		
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		query.setQueryPattern(body);
		
		Set<FormStructureFacet> facets = facetMap.keySet();
		
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDF.type.asNode(), RDFConstants.FORM_STRUCTURE_NODE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				RDFConstants.SHORT_NAME_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_VERSION_NODE_N,
				RDFConstants.VERSION_VARIABLE));
		
		if (facets.contains(FormStructureFacet.DISEASE)) {
			block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_DISEASE_NODE_N,
					RDFConstants.DISEASE_VARIABLE));
		}
		
		// latest flag
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TYPE_NODE_N,
				RDFConstants.BASE_URI_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.BASE_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_LATEST_NODE_N,
				RDFConstants.URI_NODE));   

		// adding variables to be selected
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N,
				RDFConstants.TITLE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_STATUS_NODE_N,
				RDFConstants.STATUS_VARIABLE));
		
		if (facets.contains(FormStructureFacet.SUBMISSION_TYPE)) {
			body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
					RDFConstants.PROPERTY_FS_SUBMISSION_TYPE_NODE_N, RDFConstants.SUBMISSION_TYPE_VARIABLE));
		}

		for (Entry<FormStructureFacet, Set<String>> facetEntry : facetMap.entrySet()) {
			FormStructureFacet facet = facetEntry.getKey();
			Set<String> values = facetEntry.getValue();

			if (facet != null && values != null) {
				if (FormStructureFacet.STATUS.equals(facet) ||
						FormStructureFacet.SUBMISSION_TYPE.equals(facet) ||
						FormStructureFacet.DISEASE.equals(facet)) {
					body.addElement(isOneOfUri(facet.getName(), values));
					
				} else if (FormStructureFacet.SHORT_NAME.equals(facet) || 
						FormStructureFacet.TITLE.equals(facet)) {
					LinkedList<Expr> exprList = new LinkedList<Expr>();
					exprList.add(multiRegexFilter(facet.getName(), values));
					body.addElementFilter(new ElementFilter(filterListToOrs(exprList)));
				}
			}
		}

		return query;
	}
}
