package gov.nih.tbi.dao.impl;

import gov.nih.tbi.dao.QueryAccountDao;
import gov.nih.tbi.semantic.model.AccountRDF;
import gov.nih.tbi.semantic.model.DatasetRDF;
import gov.nih.tbi.service.RDFStoreManager;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

@Repository
@Transactional
public class QueryAccountDaoImpl implements QueryAccountDao, Serializable {

	private static final long serialVersionUID = 6999452529415924840L;
	private static final Logger log = LogManager.getLogger(QueryAccountDaoImpl.class);

	@Autowired
	RDFStoreManager rdfStoreManager;
	
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

		String sparqlUpdate =
				"WITH <http://ninds.nih.gov:8080/allTriples.ttl> DELETE { <" + uri + "> ?p ?o } WHERE { <" + uri
						+ "> ?p ?o }";

		log.debug("Deleting user node: <" + uri + ">");
		log.debug(sparqlUpdate);
		rdfStoreManager.update(sparqlUpdate);
	}
	
}
