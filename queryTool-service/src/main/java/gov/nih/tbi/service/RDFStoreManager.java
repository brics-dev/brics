package gov.nih.tbi.service;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public interface RDFStoreManager {

	public void destroy();

	public ResultSet querySelect(Query query);

	public ResultSet querySelect(String query);

	public Model queryDescribe(Query query);
	
	public boolean queryAsk(Query query);

	public void update(String query);

	public void storeModel(OntModel model);
}
