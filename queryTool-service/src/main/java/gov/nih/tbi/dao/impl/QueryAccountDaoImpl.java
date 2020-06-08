package gov.nih.tbi.dao.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDFS;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dao.QueryAccountDao;
import gov.nih.tbi.pojo.QueryResult;
import gov.nih.tbi.semantic.model.AccountRDF;
import gov.nih.tbi.semantic.model.DatasetRDF;
import gov.nih.tbi.semantic.model.FormStructureRDF;
import gov.nih.tbi.service.RDFStoreManager;
import gov.nih.tbi.util.InstancedDataUtil;

@Repository
@Transactional
public class QueryAccountDaoImpl implements QueryAccountDao, Serializable {

	private static final long serialVersionUID = 6999452529415924840L;
	private static final Logger log = LogManager.getLogger(QueryAccountDaoImpl.class);

	@Autowired
	RDFStoreManager rdfStoreManager;

	/**
	 * {@inheritDoc}
	 */
	public Map<Long, String> getFormIdNameMap() {

		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.addResultVar(QueryToolConstants.FS_ID_VAR);
		query.addResultVar(QueryToolConstants.FORM_NAME_VAR);
		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);
		ElementTriplesBlock triples = new ElementTriplesBlock();
		body.addElement(triples);
		triples.addTriple(Triple.create(QueryToolConstants.FORM_VAR, FormStructureRDF.PROPERTY_ID.asNode(),
				QueryToolConstants.FS_ID_VAR));
		triples.addTriple(Triple.create(QueryToolConstants.FORM_VAR, FormStructureRDF.PROPERTY_SHORT_NAME.asNode(),
				QueryToolConstants.FORM_NAME_VAR));
		QueryResult rs = rdfStoreManager.querySelect(query);

		Map<Long, String> formIdNameMap = new HashMap<>();

		for(QuerySolution row:rs.getQueryData()) {
			String shortName =
					InstancedDataUtil.trimRdfType(row.get(QueryToolConstants.FORM_NAME_VAR.getName()).toString());
			String idString = InstancedDataUtil.trimRdfType(row.get(QueryToolConstants.FS_ID_VAR.getName()).toString());
			Long id = Long.valueOf(idString);

			formIdNameMap.put(id, shortName);
		}

		return formIdNameMap;
	}

	/**
	 * Creates a new node representing the current user and link the node to all of the datasets the user has access to.
	 * 
	 * @param username
	 */
	public void addGraphAccount(String username, List<Long> datasetIds) {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		Resource resource = AccountRDF.createResource(username);
		model.add(resource, RDFS.subClassOf, AccountRDF.RESOURCE_ACCOUNT);
		model.add(resource, AccountRDF.PROPERTY_USERNAME, ResourceFactory.createPlainLiteral(username));

		log.debug("Adding user node: <" + resource.getURI() + ">");

		// Modified RDF gen to use dataset IDs to generate dataset URIs to make this operation faster.
		if (datasetIds != null) {
			for (Long datasetId : datasetIds) {
				Resource datasetResource = DatasetRDF.createDatasetResource(datasetId.toString());
				model.add(resource, AccountRDF.PROPERTY_DATASET, datasetResource);
			}
		}

		rdfStoreManager.storeModel(model);
	}

	/**
	 * Removes the node associated to the current user and all the links from said node
	 * 
	 * @param username
	 */
	public void removeGraphAccount(String username) {
		String uri = AccountRDF.createResource(username).getURI();

		String sparqlUpdate = "WITH <http://ninds.nih.gov:8080/allTriples.ttl> DELETE { <" + uri + "> ?p ?o } WHERE { <"
				+ uri + "> ?p ?o }";

		log.debug("Deleting user node: <" + uri + ">");
		log.debug(sparqlUpdate);
		rdfStoreManager.update(sparqlUpdate);
	}

}
