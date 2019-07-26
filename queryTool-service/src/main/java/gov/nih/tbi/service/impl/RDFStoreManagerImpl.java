package gov.nih.tbi.service.impl;

import gov.nih.tbi.service.RDFStoreManager;
import gov.nih.tbi.store.MetadataStore;
import gov.nih.tbi.store.QueryVirtuosoStore;

import java.io.Serializable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

@Repository
@Transactional
public class RDFStoreManagerImpl implements RDFStoreManager, Serializable {

	private static final long serialVersionUID = 2649662710994420605L;

	private static final Logger log = LogManager.getLogger(RDFStoreManagerImpl.class.getName());

	
	@Autowired
    protected QueryVirtuosoStore virtuosoStore;


	public void destroy() {}

	public ResultSet querySelect(Query query) {

		return virtuosoStore.querySelect(query, MetadataStore.REASONING);
	}

	public ResultSet querySelect(String query) {

		return virtuosoStore.querySelect(query, MetadataStore.REASONING);
	}

	public Model queryDescribe(Query query) {
		return virtuosoStore.queryDescribe(query);
	}

	public boolean queryAsk(Query query) {
		return virtuosoStore.queryAsk(query);
	}

	public void update(String query) {
		virtuosoStore.update(query);
	}

	public void storeModel(OntModel model) {
		virtuosoStore.storeModel(model);
	}
}
