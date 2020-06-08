package gov.nih.tbi.service;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;

import gov.nih.tbi.pojo.QueryResult;

public interface RDFStoreManager {

	public void destroy();

	public QueryResult querySelect(Query query);

	public QueryResult querySelect(String query);

	public Model queryDescribe(Query query);
	
	public boolean queryAsk(Query query);

	public void update(String query);

	public void storeModel(OntModel model);
}
