package gov.nih.tbi;

import gov.nih.tbi.commons.util.QueryConstructionUtil;
import gov.nih.tbi.commons.util.RDFConstants;

import org.testng.annotations.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ProductionSupportUtils {
	@Test
	public void addPermissibleValue() {
		String[] valueRange = {"Enlisted"};
		String[] description = {"Enlisted"};
		String dataElementName = "MilRnkCat";

		for (int i = 0; i < valueRange.length; i++) {
			String sqlQuery =
					"INSERT INTO value_range (id, value_range, description, data_element_id) VALUES (nextval('value_range_seq'), '";
			String currentValueRange = valueRange[i];
			String currentDescription = description[i];
			sqlQuery += currentValueRange;
			sqlQuery += "', '";
			sqlQuery += currentDescription;
			sqlQuery += "', (select id from data_element where element_name = '";
			sqlQuery += dataElementName;
			sqlQuery += "' order by version desc limit 1));";

			System.out.println(sqlQuery);
		}

		ElementTriplesBlock whereBlock = new ElementTriplesBlock();

		whereBlock.addTriple(Triple.create(RDFConstants.URI_NODE, RDF.type.asNode(), RDFConstants.DATA_ELEMENT_NODE));
		whereBlock.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				NodeFactory.createLiteral(dataElementName)));
		whereBlock.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TYPE_NODE_N,
				RDFConstants.BASE_URI_VARIABLE));
		whereBlock.addTriple(Triple.create(RDFConstants.BASE_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_LATEST_NODE_N,
				RDFConstants.DATA_ELEMENT_VARIABLE));

		ElementTriplesBlock insertBlock = new ElementTriplesBlock();

		for (int i = 0; i < valueRange.length; i++) {
			String currentValueRange = valueRange[i];
			String currentDescription = description[i];
			Node pvNode = QueryConstructionUtil.createPermissibleValueUri(dataElementName, currentValueRange);
			insertBlock.addTriple(Triple.create(RDFConstants.DATA_ELEMENT_VARIABLE,
					RDFConstants.PROPERTY_ELEMENT_PERMISSIBLE_VALUE_N, pvNode));
			insertBlock.addTriple(Triple.create(pvNode, RDF.type.asNode(),
					NodeFactory.createURI(RDFConstants.PERMISSIBLE_VALUE)));
			insertBlock.addTriple(Triple.create(pvNode, RDFS.label.asNode(),
					NodeFactory.createLiteral(currentValueRange)));
			insertBlock.addTriple(Triple.create(pvNode,
					NodeFactory.createURI(RDFConstants.PROPERTY_BRICS_DESCRIPTION_N),
					NodeFactory.createLiteral(currentDescription)));
		}

		String sparqlQuery =
				"SPARQL\nINSERT INTO <http://ninds.nih.gov:8080/allTriples.ttl> { \n" + insertBlock + "}\nWHERE{\n"
						+ whereBlock + "}";
		System.out.println(sparqlQuery);
	}
}
