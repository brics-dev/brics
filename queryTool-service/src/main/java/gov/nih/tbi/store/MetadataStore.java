package gov.nih.tbi.store;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import gov.nih.tbi.pojo.QueryResult;

/**
 * Generic interface used to operate with different metadata stores.
 * 
 * @author: http://rhizomik.net/~roberto
 */
public interface MetadataStore {

	public static int INSTANCES = 1;
	public static int SCHEMAS = 2;
	public static int REASONING = 3;

	/**
	 * Abstract method for querying a metadata store.
	 */
	String query(String query);

	/**
	 * Abstract method for querying a metadata store and getting JSON instead of RDF/XML.
	 */
	String queryJSON(String query);

	/**
	 * Abstract method for querying and retrieving Jena ResultSet, just for SPARQL select and for a defined query scope.
	 * The scopes, defined in MetadataStore, are: - INSTANCES (if just to query instance data) - SCHEMAS (if just to
	 * query schemas and ontologies) - REASONING (instance plus schemas plus the reasoning provided by the store)
	 */
	QueryResult querySelect(String query, int scope);

	QueryResult querySelect(Query query, int scope);

	// /**
	// * Abstract method for querying the in-memory dictionary triples.
	// * @param query
	// * @param scope
	// * @return
	// */
	// @Deprecated
	// ResultSet querySelectDictionary(String query, int scope);

	/**
	 * Abstract method for performing a SPARQL ASK query an retrieve a boolean.
	 */
	boolean queryAsk(String query);
	
	/**
	 * Abstract method for performing a SPARQL ASK query an retrieve a boolean.
	 */
	boolean queryAsk(Query query);

	/**
	 * Abstract method for storing input metadata in a store.
	 */
	String store(InputStream metadata, String contentType);

	/**
	 * Abstract method for storing the content of a URL in a metadata store.
	 */
	String store(URL metadataUrl);

	/**
	 * Abstract method for removing input metadata from a store.
	 */
	void remove(InputStream metadata, String contentType);

	/**
	 * Abstract method for removing the metadata coresponding to the Concise Bounded Description for the resource from a
	 * metadata store.
	 */
	void remove(URI resource);

	public Model queryDescribe(Query query);

	void update(String query);

	void storeModel(OntModel model);
}
