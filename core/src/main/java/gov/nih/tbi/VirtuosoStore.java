package gov.nih.tbi;

import gov.nih.tbi.commons.util.RDFConstants;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Virtuoso implementation for the Rhizomer metadata store.
 * 
 * @author: http://rhizomik.net/~roberto
 */

@Repository
public class VirtuosoStore implements MetadataStore, Serializable {

	private static final long serialVersionUID = -5729315377459567235L;
	private VirtuosoGraph graph = null;
	private Model dictionaryModel = null;
	private String graphURI = "";
	private String schema = "";
	private String ruleSet = "";
	private static final Logger log = LogManager.getLogger(VirtuosoStore.class.getName());

	public VirtuosoStore() {

	}

	@Autowired
	public VirtuosoStore(@Qualifier(CoreConstants.RDF_CONNECTION) DataSource rdfConnection) throws SQLException {

		super();
		graph = new VirtuosoGraph("http://ninds.nih.gov:8080/allTriples.ttl", rdfConnection);


		graph.setReadFromAllGraphs(true);
	}

	public String getGraphURI() {

		return graphURI;
	}

	public void setGraphURI(String graphURI) {

		this.graphURI = graphURI;
	}

	public String getSchema() {

		return schema;
	}

	public void setSchema(String schema) {

		this.schema = schema;
	}

	public void recacheDictionary() {

		if (dictionaryModel != null) {
			dictionaryModel.close();
		}

		initDictionaryCache();
	}

	/**
	 * Initializes the dictionaryModel which is an in memory object used to cache dictionary triples
	 */
	@Deprecated
	private void initDictionaryCache() {

		log.info("Caching dictionary triples...");
		String constructString = CoreConstants.DICTIONARY_CONSTRUCT;
		Query query = QueryFactory.create(constructString, Syntax.syntaxARQ);
		VirtuosoQueryExecution qe = VirtuosoQueryExecutionFactory.create(query, graph);
		dictionaryModel = ModelFactory.createMemModelMaker().createDefaultModel().add(qe.execConstruct());

		// if (log.isDebugEnabled() == true)
		// {
		StmtIterator iterator = dictionaryModel.listStatements();

		while (iterator.hasNext()) {
			log.debug(iterator.next());
		}
		// }
	}

	public void init(String db_url, String db_user, String db_pass) {

		graph = new VirtuosoGraph("http://ninds.nih.gov:8080/allTriples.ttl", db_url, db_user, db_pass);

		graph.setReadFromAllGraphs(true);
		initDictionaryCache();
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
		initDictionaryCache();
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

		graph.close();
	}

	public String query(String queryString) {

		return query(queryString, "application/rdf+xml");
	}

	public String queryJSON(String queryString) {

		return query(queryString, "application/json");
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
			Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
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

	public void update(UpdateRequest request) {

		if (graph == null || request == null) {
			log.error("No graph loaded");
			return;
		}

		if (log.isDebugEnabled() == true) {
			log.debug(request.toString());
		}

		UpdateAction.execute(request, graph);
	}

	/**
	 * Executes select query using the dictionary triples cache
	 * 
	 * @param queryString
	 * @param scope
	 * @return
	 */
	public ResultSet querySelectDictionary(String queryString, int scope) {

		if (log.isDebugEnabled() == true) {
			log.debug("Querying Cache...");
		}

		ResultSet results = new ResultSetMem();
		QueryExecution qexec = null;
		Query query = processSelectQuery(queryString, scope, true);

		if (queryString == null) {
			return results; // return empty result
		}

		long startTime = System.currentTimeMillis();

		qexec = QueryExecutionFactory.create(query, dictionaryModel);

		results = qexec.execSelect();

		long endTime = System.currentTimeMillis();

		if (log.isDebugEnabled() == true) {
			log.debug("Total time: " + (endTime - startTime) + "ms");
		}

		return results;
	}

	/**
	 * Processes the raw query string. The scopes, defined in MetadataStore, are: - INSTANCES (if just to query instance
	 * data) - SCHEMAS (if just to query schemas and ontologies) - REASONING (instance plus schemas plus the reasoning
	 * provided by the store)
	 */
	public Query processSelectQuery(String queryString, int scope, boolean isCache) {

		if (queryString.startsWith("PREFIX") == false) {
			// queryString = "SELECT * WHERE {{ " +queryString + "}}";
			queryString = RDFConstants.PREFIXES + queryString;
		}

		Query query = null;

		try {
			query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
		} catch (QueryParseException e) {
			log.info("Error while querying the following: \n" + queryString);
			log.fatal(e.toString());
			e.printStackTrace();
			return null; // Return empty query
		}

		if (!query.isSelectType()) {
			return null;
		}

		// NP: Added multiple graph support
		// if (!isCache && !graphURI.isEmpty() && !schema.isEmpty())
		// {
		// if (scope == MetadataStore.INSTANCES || scope == MetadataStore.REASONING)
		// {
		// query.addGraphURI(graphURI);
		// }
		// if (scope == MetadataStore.SCHEMAS || scope == MetadataStore.REASONING)
		// {
		// query.addGraphURI(schema);
		// }
		// }

		return query;
	}

	/**
	 * Perform input SPARQL SELECT query and return result as ResultSet The scopes, defined in MetadataStore, are: -
	 * INSTANCES (if just to query instance data) - SCHEMAS (if just to query schemas and ontologies) - REASONING
	 * (instance plus schemas plus the reasoning provided by the store)
	 */
	public ResultSet querySelect(String queryString, int scope) {

		log.debug("Querying Virtuoso...");

		ResultSet results = new ResultSetMem();
		VirtuosoQueryExecution qexec = null;
		Query query = processSelectQuery(queryString, scope, false);

		if (queryString == null) {
			return results; // return empty result
		}

		if (log.isDebugEnabled() == true) {
			log.debug(query.toString());
		}

		long startTime = System.currentTimeMillis();

		qexec = VirtuosoQueryExecutionFactory.create(query, graph);
		results = qexec.execSelect();

		long endTime = System.currentTimeMillis();

		if (log.isDebugEnabled() == true) {
			log.debug("Total time: " + (endTime - startTime) + "ms");
		}

		return results;
	}

	public String update(String ur) {

		VirtuosoUpdateRequest vurRequest = VirtuosoUpdateFactory.create(ur, graph);
		vurRequest.exec();

		return null;
	}

	public void storeModel(OntModel model) {

		graph.getTransactionHandler().begin();
		graph.getBulkUpdateHandler().add(model.getGraph());

		graph.getTransactionHandler().commit();

	}

	public ResultSet querySelect(Query query, int scope, boolean largeQuerySupport) {
		if (largeQuerySupport) {
			Query mainQuery = QueryFactory.make();
			mainQuery.setQuerySelectType();

			for (String resultVar : query.getResultVars()) {
				mainQuery.addResultVar(resultVar);
			}

			ElementGroup body = new ElementGroup();
			mainQuery.setQueryPattern(body);
			body.addElement(new ElementSubQuery(query));
			return querySelect(mainQuery);
		} else {
			return querySelect(query);
		}
	}


	public ResultSet querySelect(Query query) {
		ResultSet results = null;

		if (log.isDebugEnabled() == true) {
			log.debug("Querying Virtuoso...");
		}

		if (log.isTraceEnabled()) {
			log.trace(query.toString());
		}

		long startTime = System.currentTimeMillis();

		VirtuosoQueryExecution qexec = VirtuosoQueryExecutionFactory.create(query, graph);
		results = qexec.execSelect();

		long endTime = System.currentTimeMillis();

		if (log.isDebugEnabled() == true) {
			log.debug("Total time: " + (endTime - startTime) + "ms");
		}

		return results;
	}


	public boolean queryAsk(Query query) {

		boolean result = false;
		// TODO: remove this when we want multi-graph support
		// query.addGraphURI(graphURI);
		// query.addGraphURI(schema);

		VirtuosoQueryExecution qexec = VirtuosoQueryExecutionFactory.create(query.toString(), graph);

		if (query.isAskType())
			result = qexec.execAsk();

		if (log.isDebugEnabled() == true) {
			log.debug(query.toString());
		}

		return result;
	}

	/**
	 * Perform input SPARQL ASK query and return result as boolean
	 * 
	 * @return boolean
	 * @param queryString java.lang.String
	 */
	public boolean queryAsk(String queryString) {

		boolean result = false;
		VirtuosoQueryExecution qexec = null;
		Query query = null;

		try {
			query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		} catch (QueryParseException e) {
			log.fatal(e.toString());
			return result;
		}

		// query.addGraphURI(graphURI);
		// query.addGraphURI(schema);
		queryString = "DEFINE input:inference \"" + ruleSet + "\"\n" + query.toString();
		// log.log(Level.INFO, "VirtuosoStore.query: "+query.toString());

		qexec = VirtuosoQueryExecutionFactory.create(query.toString(), graph);

		if (query.isAskType())
			result = qexec.execAsk();

		return result;
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

		return response;
	}

	/**
	 * Remove all available metadata for the input URI, i.e. the Concise Bounded Description for the URI resource
	 */
	public void remove(java.net.URI uri) {

		Query query = QueryFactory.create("DESCRIBE <" + uri + ">");
		query.addGraphURI(graphURI);
		String queryString = "DEFINE sql:describe-mode \"CBD\"\n" + query.toString();
		VirtuosoQueryExecution qexec = VirtuosoQueryExecutionFactory.create(queryString, graph);
		Model remove = qexec.execDescribe();

		graph.getTransactionHandler().begin();
		graph.getBulkUpdateHandler().delete(remove.getGraph());
		graph.getTransactionHandler().commit();
	}

	/**
	 * Remove the input metadata from the store. TODO: Remove triples also from schema graph, check if it is class,
	 * property,...?
	 * 
	 * @return java.lang.String
	 * @param metadata java.io.InputStream
	 */
	public void remove(InputStream metadata, String contentType) {

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
	}

	public void close() {

		graph.close();
	}

}
