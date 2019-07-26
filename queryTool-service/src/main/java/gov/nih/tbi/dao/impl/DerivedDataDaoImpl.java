package gov.nih.tbi.dao.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nih.tbi.commons.util.SparqlConstructionUtil;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dao.DerivedDataDao;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.query.model.DerivedDataKey;
import gov.nih.tbi.query.model.DerivedDataRow;
import gov.nih.tbi.query.model.RepeatableGroupDataElement;
import gov.nih.tbi.semantic.model.DataElementRDF;
import gov.nih.tbi.semantic.model.FormStructureRDF;
import gov.nih.tbi.semantic.model.RepeatableGroupRDF;
import gov.nih.tbi.service.RDFStoreManager;
import gov.nih.tbi.util.InstancedDataUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This class is used to query Virtuoso for derived data. This is primarily called from the derivedDataManager
 * 
 * @author fchen
 *
 */
@Repository
@Transactional
public class DerivedDataDaoImpl implements DerivedDataDao, Serializable {

	private static final long serialVersionUID = -4918595899211380075L;

	@Autowired
	private RDFStoreManager rdfStoreManager;

	/**
	 * Given form info, repeatable group and data element info, and a set of GUIDs to filter by, return data from the
	 * given form and data elements. The map returned is indexed by fields inside deriveDataKey
	 * 
	 * @param formNameAndVersion
	 * @param repeatableGroupDataElements
	 * @param guids
	 * @return
	 */
	public Map<DerivedDataKey, DerivedDataRow> getDerivedData(NameAndVersion formNameAndVersion,
			List<RepeatableGroupDataElement> repeatableGroupDataElements, Set<String> guids) {

		// get the query to retrieve derived data
		Query query = getDerivedDataQuery(formNameAndVersion, repeatableGroupDataElements, guids);

		ResultSet resultSet = rdfStoreManager.querySelect(query);

		Map<DerivedDataKey, DerivedDataRow> derivedDataMap = new HashMap<DerivedDataKey, DerivedDataRow>();

		while (resultSet.hasNext()) {
			QuerySolution row = resultSet.next();

			DerivedDataRow currentDataRow = new DerivedDataRow();
			DerivedDataKey currentDataKey = new DerivedDataKey();

			for (String currentVar : resultSet.getResultVars()) {
				String currentValue = InstancedDataUtil.rdfNodeToString(row.get(currentVar));
				if (currentValue == null) {
					currentValue = QueryToolConstants.EMPTY_STRING;
				}

				// if current row is one of the keys, we want to set the value in addition adding it to the
				// derivedDataRow object
				if (QueryToolConstants.GUID_VAR.getName().equals(currentVar)) {
					if (!guids.contains(currentValue) && !guids.isEmpty()) {  // we will use the code to filter out
																			  // guids
																			  // instead. turns out filtering by a
																			  // large
																			  // list (>1000) is very very slow in
																			  // sparql.
						continue;
					}
					currentDataKey.setGuid(currentValue);
				} else if (QueryToolConstants.VISIT_TYPE_VAR.getName().equals(currentVar)) {
					currentDataKey.setVisitType(currentValue);
				}

				// add the value into the row map
				currentDataRow.getRow().put(currentVar, currentValue);
			}

			derivedDataMap.put(currentDataKey, currentDataRow);
		}

		return derivedDataMap;
	}

	/**
	 * Adds the triples to retrieve data for a data element.
	 * 
	 * @param block - Triples block to append to
	 * @param repeatableGroup - current repeatable group
	 * @param dataElement - current data element
	 * @param variableNameCounter - the counter to append to variable names
	 * @return
	 */
	private ElementTriplesBlock insertRepeatableGroupDataElementTriples(ElementTriplesBlock block,
			String repeatableGroup, String dataElement, int variableNameCounter) {
		// construct the necessary variables
		Var currentRgInstanceVar = Var.alloc("rgInstance" + variableNameCounter);
		Var currentRgVar = Var.alloc("rg" + variableNameCounter);
		Var currentDeVar = Var.alloc("de" + variableNameCounter);
		Var currentDeValueVar = Var.alloc(dataElement);

		// add triples for repeatable group
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.HAS_INSTANCED_REPEATABLE_GROUP,
				currentRgInstanceVar));
		block.addTriple(Triple.create(currentRgInstanceVar, RDF.type.asNode(), currentRgVar));
		block.addTriple(Triple.create(currentRgVar, RepeatableGroupRDF.PROPERTY_NAME.asNode(),
				NodeFactory.createLiteral(repeatableGroup)));

		// add triples for data element
		block.addTriple(Triple.create(currentRgInstanceVar, currentDeVar, currentDeValueVar));
		block.addTriple(Triple.create(currentDeVar, DataElementRDF.PROPERTY_ELEMENT_NAME.asNode(),
				NodeFactory.createLiteral(dataElement)));

		return block;
	}

	/**
	 * Returns the query to retrieve derived data
	 * 
	 * @param formNameAndVersion
	 * @param repeatableGroupDataElements
	 * @param guids
	 * @return
	 */
	private Query getDerivedDataQuery(NameAndVersion formNameAndVersion,
			List<RepeatableGroupDataElement> repeatableGroupDataElements, Set<String> guids) {

		// query creation boilerplate
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.setDistinct(true);

		Node formNode = FormStructureRDF.createFormResource(formNameAndVersion.getName()).asNode();
		// GUID triples
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, RDF.type.asNode(), formNode));
		block.addTriple(
				Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.ROW_GUID, QueryToolConstants.GUID_N_VAR));
		block.addTriple(Triple.create(QueryToolConstants.GUID_N_VAR, RDFS.label.asNode(), QueryToolConstants.GUID_VAR));

		// Visit Type triples

		List<Triple> visitTypeTriples = new ArrayList<Triple>();
		visitTypeTriples.add(Triple.create(QueryToolConstants.ROW_VAR,
				QueryToolConstants.HAS_INSTANCED_REPEATABLE_GROUP, QueryToolConstants.VISIT_TYPE_RG_INSTANCE_VAR));
		visitTypeTriples.add(Triple.create(QueryToolConstants.VISIT_TYPE_RG_INSTANCE_VAR,
				QueryToolConstants.VISIT_TYPE_URI, QueryToolConstants.VISIT_TYPE_VAR));
		body.addElement(SparqlConstructionUtil.buildGroupOptionalPattern(visitTypeTriples));

		// Add GUID and visit type variables to select
		query.addResultVar(QueryToolConstants.GUID_VAR);
		query.addResultVar(QueryToolConstants.VISIT_TYPE_VAR);


		// add triples for data elements from the repeatableGroupDataElements set
		int variableNameCounter = 0;
		for (RepeatableGroupDataElement repeatableGroupDataElement : repeatableGroupDataElements) {
			String rgName = repeatableGroupDataElement.getRepeatableGroupName();
			String deName = repeatableGroupDataElement.getDataElementName();

			block = insertRepeatableGroupDataElementTriples(block, rgName, deName, variableNameCounter);
			query.addResultVar(deName);
			variableNameCounter++;
		}

		// add GUIDs filter
		// if (!guids.isEmpty()) {
		// body.addElement(SparqlConstructionUtil.buildValuesSubQuery(Var.alloc("guid"), guids));
		// }

		return query;
	}
}
