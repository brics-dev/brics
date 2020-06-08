
package gov.nih.tbi.dictionary.dao.sparql;

import gov.nih.tbi.commons.dao.sparql.GenericSparqlDaoImpl;
import gov.nih.tbi.commons.util.QueryConstructionUtil;
import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.dictionary.dao.KeywordSparqlDao;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDFS;

@Repository
public class KeywordSparqlDaoImpl extends GenericSparqlDaoImpl<Keyword> implements KeywordSparqlDao {

	/**
	 * Returns a keyword object by the given name. This keyword object contains a name, uri, and count. Returns null if
	 * keyword does not exist
	 * 
	 * @param name
	 * @return
	 */
	public Keyword getByName(String name) {

		Query query = QueryConstructionUtil.getKeywordQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

		if (block == null) {
			block = new ElementTriplesBlock();
			body.addElement(block);
		}

		block.addTriple(
				Triple.create(RDFConstants.KEYWORD_VARIABLE, RDFS.label.asNode(), NodeFactory.createLiteral(name)));

		ResultSet results = querySelect(query);
		if (results.hasNext()) {
			return parseRow(results.next());
		}

		return null;
	}

	public Keyword parseRow(QuerySolution row) {
		String uri = rdfNodeToString(row.get(RDFConstants.KEYWORD_VARIABLE.toString()));
		String keywordName = rdfNodeToString(row.get(RDFConstants.VALUE_VARIABLE.toString()));
		String countString = rdfNodeToString(row.get(RDFConstants.COUNT_VARIABLE.toString()));
		if (keywordName == null || countString == null) {
			throw new NullPointerException("Keyword or keyword count is null.");
		}

		Long count = Long.valueOf(countString);

		Keyword keyword = new Keyword();
		keyword.setUri(uri);
		keyword.setKeyword(keywordName);
		keyword.setCount(count);

		return keyword;
	}

	/**
	 * Returns a list of keywords that contain the given string. Search is case insensitive.
	 * 
	 * @param searchKey
	 * @return
	 */
	public List<Keyword> search(String searchKey) {

		Query query = QueryConstructionUtil.getKeywordQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();

		if (searchKey != null && !searchKey.isEmpty()) {
			searchKey = searchKey.trim();
			body.addElementFilter(QueryConstructionUtil.regexFilter(RDFConstants.KEYWORD_VARIABLE, searchKey));
		}

		ResultSet results = querySelect(query);

		List<Keyword> keywords = new ArrayList<Keyword>();

		while (results.hasNext()) {
			keywords.add(parseRow(results.next()));
		}

		return keywords;
	}

	@Override
	public List<Keyword> getAll() {

		Query query = QueryConstructionUtil.getKeywordQuery();
		ResultSet results = querySelect(query);

		List<Keyword> keywords = new ArrayList<Keyword>();

		while (results.hasNext()) {
			Keyword keyword = parseRow(results.next());
			keywords.add(keyword);
		}

		return keywords;
	}

	@Override
	public Keyword get(String uri) {

		Query query = QueryConstructionUtil.getKeywordQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

		if (block == null) {
			block = new ElementTriplesBlock();
			body.addElement(block);
		}

		block.addTriple(Triple.create(RDFConstants.KEYWORD_VARIABLE, RDFS.isDefinedBy.asNode(),
				NodeFactory.createLiteral(uri)));

		ResultSet results = querySelect(query);
		if (results.hasNext()) {
			return parseRow(results.next());
		}

		return null;
	}

	public boolean exists(Keyword keyword) {

		if (keyword == null) {
			throw new NullPointerException("Why are you checking if a null data element exists?!");
		}

		if (keyword.getUri() == null) // no uri means that this is a new keyword, thus does not exist in db
		{
			return false;
		}

		Query existsQuery = QueryFactory.make();
		existsQuery.setQueryAskType();

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		existsQuery.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.KEYWORD_VARIABLE, RDFS.isDefinedBy.asNode(),
				NodeFactory.createURI(keyword.getUri())));

		return virtuosoStore.queryAsk(existsQuery);
	}

	@Override
	public Keyword save(Keyword keyword) {
		if (exists(keyword)) {
			delete(keyword);
		}

		UpdateDataInsert updateInsert = new UpdateDataInsert(QueryConstructionUtil.generateKeywordTriples(keyword));
		UpdateRequest request = UpdateFactory.create();
		request.add(updateInsert);
		virtuosoStore.update(request);
		return keyword;
	}

	private void delete(Keyword keyword) {

		String uri = keyword.getUri();

		String sparqlUpdate = "WITH <http://ninds.nih.gov:8080/allTriples.ttl> DELETE { <" + uri + "> ?p ?o } WHERE { <"
				+ uri + "> ?p ?o }";

		update(sparqlUpdate);
	}
}
