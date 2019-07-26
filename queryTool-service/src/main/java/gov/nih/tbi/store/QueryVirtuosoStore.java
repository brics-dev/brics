package gov.nih.tbi.store;

import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.VirtuosoGraph;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

/**
 * Virtuoso implementation for the Rhizomer metadata store.
 * 
 * @author: http://rhizomik.net/~roberto
 */

@Repository
public class QueryVirtuosoStore implements MetadataStore, Serializable {

	private static final long serialVersionUID = -5729315377459567235L;
	private VirtuosoGraph graph = null;

	private String graphURI = "";
	private String schema = "";
	private String ruleSet = "";
	private static final Logger log = LogManager.getLogger(QueryVirtuosoStore.class.getName());

	@Autowired
	ApplicationConstants constants;

	public QueryVirtuosoStore() {

		super();
	}

	@Autowired
	public QueryVirtuosoStore(@Qualifier(QueryToolConstants.RDF_CONNECTION) DataSource rdfConnection)
			throws SQLException {

		super();
		graph = new VirtuosoGraph("http://ninds.nih.gov:8080/allTriples.ttl", rdfConnection);


		graph.setReadFromAllGraphs(true);
	}

	@PostConstruct
	public void postConstruct() {
		graph.setFetchSize(constants.getVirtuosoFetchSize());
	}

	public String query(String queryString) {

		return query(queryString, "application/rdf+xml");
	}

	public String queryJSON(String queryString) {

		return query(queryString, "application/json");
	}

	public Model queryDescribe(Query query) {
		Model model = null;

		if (query.isDescribeType()) {
			if (log.isDebugEnabled() == true) {
				log.debug(query.toString());
			}

			VirtuosoQueryExecution qexec = VirtuosoQueryExecutionFactory.create(query, graph);
			model = qexec.execDescribe();
		}

		return model;
	}

	/**
	 * Perform input query and return output as RDF/XML or JSON (warning, just for SELECT queries)
	 * 
	 * @return java.lang.String
	 * @param queryString java.lang.String
	 * @param format java.lang.String
	 */
	public String query(String queryString, String format) {

		String response = "";

		VirtuosoQueryExecution qexec = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
			query.addGraphURI(graphURI);
			query.addGraphURI(schema);
			// if (!query.hasLimit())
			// query.setLimit(SPARQL_LIMIT);
			queryString = "DEFINE input:inference \"" + ruleSet + "\"\n" + query.toString();
			if (query.isDescribeType())
				queryString = "DEFINE sql:describe-mode \"CBDL\"\n" + queryString;

			/*
			 * if (queryString.indexOf("regex")>0) { queryString = queryString.replace("regex","bif:contains");
			 * queryString = queryString.replace(" '", " \"'"); queryString = queryString.replace("',", "'\",");
			 * queryString = queryString.replace(", 'i'", ""); }
			 */

			// log.log(Level.INFO, "VirtuosoStore.query: "+queryString);

			qexec = VirtuosoQueryExecutionFactory.create(queryString, graph);

			if (query.isSelectType()) {
				ResultSet results = qexec.execSelect();
				if (format.equals("application/json"))
					ResultSetFormatter.outputAsJSON(out, results);
				else
					ResultSetFormatter.outputAsRDF(out, "RDF/XML-ABBREV", results);
			} else if (query.isConstructType()) {
				Model results = qexec.execConstruct();
				results.write(out, "RDF/XML-ABBREV");
			} else if (query.isDescribeType()) {
				Model results = qexec.execDescribe();
				results.write(out, "RDF/XML-ABBREV");
			}
			out.flush();
			response = out.toString("UTF8");
		} catch (Exception e) {
			log.fatal("Exception in VirtuosoStore.query for: " + queryString, e);
			response = e.getMessage();
		} finally {
			if (qexec != null)
				qexec.close();
		}

		return response;
	}

	/**
	 * Processes the raw query string. The scopes, defined in MetadataStore, are: - INSTANCES (if just to query instance
	 * data) - SCHEMAS (if just to query schemas and ontologies) - REASONING (instance plus schemas plus the reasoning
	 * provided by the store)
	 */
	public Query processSelectQuery(Query query) {

		// TODO: make this dynamic
		query.addGraphURI("http://ninds.nih.gov:8080/allTriples.ttl");

		if (log.isDebugEnabled() == true) {
			log.debug(query.toString());
		}

		return query;
	}

	/**
	 * Perform input SPARQL SELECT query and return result as ResultSet The scopes, defined in MetadataStore, are: -
	 * INSTANCES (if just to query instance data) - SCHEMAS (if just to query schemas and ontologies) - REASONING
	 * (instance plus schemas plus the reasoning provided by the store)
	 */
	public ResultSet querySelect(String queryString, int scope) {
		if (queryString.startsWith("PREFIX") == false) {
			queryString = QueryToolConstants.PREFIXES + queryString;
		}

		if (log.isDebugEnabled() == true) {
			log.debug(queryString);
		}

		Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

		return querySelect(query, scope);
	}

	public ResultSet querySelect(Query query, int scope) {
		ResultSet results = null;

		try {
			log.debug("Querying Virtuoso...");
			VirtuosoQueryExecution qexec = null;
			query = processSelectQuery(query);

			long startTime = System.currentTimeMillis();

			qexec = VirtuosoQueryExecutionFactory.create(query, graph);
			results = qexec.execSelect();

			long endTime = System.currentTimeMillis();

			if (log.isDebugEnabled() == true) {
				log.debug("Total time: " + (endTime - startTime) + "ms");
			}
		} finally {
			// graph.close();
		}

		return results;
	}

	public boolean queryAsk(Query query) {
		boolean askResult;
		VirtuosoQueryExecution qexec = null;

		try {
			query.addGraphURI("http://ninds.nih.gov:8080/allTriples.ttl");
			log.debug(query.toString());
			qexec = VirtuosoQueryExecutionFactory.create(query.toString(), graph);
			askResult = qexec.execAsk();
		} finally {
			qexec.close();
			// graph.close();
		}

		return askResult;
	}

	/**
	 * Perform input SPARQL ASK query and return result as boolean
	 * 
	 * @return boolean
	 * @param queryString java.lang.String
	 */
	public boolean queryAsk(String queryString) {
		Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
		return queryAsk(query);
	}

	/**
	 * Store the input metadata. TODO: If it is a class, property,... store into the schema graph instead of in the
	 * instance one
	 * 
	 * @return java.lang.String
	 * @param metadata java.io.InputStream
	 */
	public String store(InputStream metadata, String contentType) {

		String response = "";

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			String format = "RDF/XML"; // Default

			if (contentType.indexOf("application/n-triples") >= 0)
				format = "N-TRIPLE";
			else if (contentType.indexOf("application/n3") >= 0)
				format = "N3";

			try {
				Model temp = ModelFactory.createDefaultModel();
				temp.read(metadata, "", format);
				graph.getTransactionHandler().begin();
				graph.getBulkUpdateHandler().add(temp.getGraph());
				graph.getTransactionHandler().commit();

				temp.write(out, "RDF/XML-ABBREV");
				out.close();
				response = out.toString("UTF8");
			} catch (Exception e) {
				log.fatal("Exception in JenaStore.store", e);
				response = e.toString();
			}
		} finally {
			// graph.close();
		}

		return response;
	}

	/**
	 * Store the metadata at URL.
	 * 
	 * @return java.lang.String
	 * @param metadataURL java.net.URL
	 */
	public String store(URL metadataURL) {

		String response = "";

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			try {
				Model temp = ModelFactory.createDefaultModel();
				temp.read(metadataURL.toString());
				graph.getTransactionHandler().begin();
				graph.getBulkUpdateHandler().add(temp.getGraph());
				graph.getTransactionHandler().commit();

				temp.write(out, "RDF/XML-ABBREV");
				out.close();
				response = out.toString("UTF8");
			} catch (Exception e) {
				log.fatal("Exception in JenaStore.store for: " + metadataURL, e);
				response = e.toString();
			}
		} finally {
			// graph.close();
		}

		return response;
	}

	/**
	 * Remove all available metadata for the input URI, i.e. the Concise Bounded Description for the URI resource
	 */
	public void remove(java.net.URI uri) {

		try {
			Query query = QueryFactory.create("DESCRIBE <" + uri + ">");
			query.addGraphURI(graphURI);
			String queryString = "DEFINE sql:describe-mode \"CBD\"\n" + query.toString();
			VirtuosoQueryExecution qexec = VirtuosoQueryExecutionFactory.create(queryString, graph);
			Model remove = qexec.execDescribe();

			graph.getTransactionHandler().begin();
			graph.getBulkUpdateHandler().delete(remove.getGraph());
			graph.getTransactionHandler().commit();
		} finally {
			// graph.close();
		}
	}

	public void update(String ur) {
		VirtuosoUpdateRequest vurRequest = VirtuosoUpdateFactory.create(ur, graph);
		vurRequest.exec();
		// graph.close();
	}

	/**
	 * Remove the input metadata from the store. TODO: Remove triples also from schema graph, check if it is class,
	 * property,...?
	 * 
	 * @return java.lang.String
	 * @param metadata java.io.InputStream
	 */
	public void remove(InputStream metadata, String contentType) {

		try {
			String metadataFormat = "RDF/XML"; // Default

			if (contentType.equalsIgnoreCase("application/n-triples"))
				metadataFormat = "N-TRIPLE";
			else if (contentType.equalsIgnoreCase("application/n3"))
				metadataFormat = "N3";

			Model remove = ModelFactory.createDefaultModel();
			remove.read(metadata, "", metadataFormat);

			graph.getTransactionHandler().begin();
			graph.getBulkUpdateHandler().delete(remove.getGraph());
			graph.getTransactionHandler().commit();
		} finally {
			// graph.close();
		}
	}

	public void storeModel(OntModel model) {
		graph.getTransactionHandler().begin();
		graph.getBulkUpdateHandler().add(model.getGraph());

		graph.getTransactionHandler().commit();

		// graph.close();
		model.close();
	}

	public void init(String db_url, String db_user, String db_pass) {

		graph = new VirtuosoGraph("http://ninds.nih.gov:8080/allTriples.ttl", db_url, db_user, db_pass);

		graph.setReadFromAllGraphs(true);
	}

	public void init(String db_url, String db_user, String db_pass, String db_graph, String db_schema)
			throws SQLException {

		graphURI = db_graph;
		// If schema for reasoning explicitly stated in web.xml, otherwise build from db_graph
		if (db_schema != null)
			schema = db_schema;
		else
			schema = graphURI + (graphURI.endsWith("/") ? "" : "/") + "schema/";
		ruleSet = schema + "rules/";
		graph = new VirtuosoGraph(db_graph, db_url, db_user, db_pass);
		// Add or recalculate inference for the graph
		// NOTE: to grant the "rhizomer" Virtuoso user rights to execute the rdfs_rule_set, execute
		// the following command from Virtuoso iSQL: grant execute on rdfs_rule_set to "rhizomer"
		String sqlStatement = "DB.DBA.RDFS_RULE_SET('" + ruleSet + "', '" + schema + "')";
		graph.getConnection().prepareCall(sqlStatement).execute();
		// sqlStatement = "set result_timeout = 0";
		// graph.getConnection().prepareCall(sqlStatement).execute();
	}

	public void init(Properties props) throws Exception {

		if (props.getProperty("db_url") == null)
			throw new Exception("Missing parameter for VirtuosoStore init: db_url");
		else if (props.getProperty("db_user") == null)
			throw new Exception("Missing parameter for VirtuosoStore init: db_user");
		else if (props.getProperty("db_pass") == null)
			throw new Exception("Missing parameter for VirtuosoStore init: db_pass");
		else if (props.getProperty("db_graph") == null)
			throw new Exception("Missing parameter for VirtuosoStore init: db_graph");
		else {
			init(props.getProperty("db_url"), props.getProperty("db_user"), props.getProperty("db_pass"),
					props.getProperty("db_graph"), props.getProperty("db_schema"));
		}
	}

	protected void finalize() throws Throwable {

		// graph.close();
	}
}
