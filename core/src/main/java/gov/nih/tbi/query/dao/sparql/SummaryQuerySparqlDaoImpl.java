package gov.nih.tbi.query.dao.sparql;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import gov.nih.tbi.MetadataStore;
import gov.nih.tbi.commons.dao.sparql.GenericSparqlDaoImpl;
import gov.nih.tbi.query.dao.SummaryQuerySparqlDao;
import gov.nih.tbi.query.model.SummaryResult;
import gov.nih.tbi.repository.model.hibernate.Study;

/**
 * SPARQL communication that queries virtuoso and parses the results into a key/value map.
 * @author Bill Puschmann
 *
 */
@Repository
public class SummaryQuerySparqlDaoImpl extends
		GenericSparqlDaoImpl<SummaryResult> implements SummaryQuerySparqlDao {

	@Override
	public List<SummaryResult> getAll() {
		// TODO  Throw Exception
		return null;
	}

	/**
	 * Calls virtuoso with the given query and parses the results into a key/value map of variables and counts (based on the convention of the SPARQL queries).
	 * 
	 * These queries should only be returning two variables: "value" and "count".  Anything else will be ignored.
	 */
	@Override
	public SummaryResult get(String query) {
		SummaryResult summaryResult = new SummaryResult();
		Map<String, String> summaryResultsMap = summaryResult.getResults();

        ResultSet resultset = virtuosoStore.querySelect(query, MetadataStore.REASONING);

        String strVar = resultset.getResultVars().get(0);
        String countVar = resultset.getResultVars().get(1);
  
        while (resultset.hasNext()) {
            QuerySolution row = resultset.next();

            if (!row.contains(strVar)) {
                continue;
            }

            String category = row.getLiteral(strVar).toString();
            String count = row.getLiteral(countVar).toString();
            
            summaryResultsMap.put(category, count);
            
        }
		return summaryResult;
	}
	
	/**
	 * Calls virtuoso with the given query and parses the results into a key/value map of variables and counts (based on the convention of the SPARQL queries).
	 * 
	 * These queries should only be returning two variables: "value" and "count".  Anything else will be ignored.
	 * @return 
	 */
	@Override
	public SummaryResult getResultJson(String query) {
		SummaryResult summaryResult = new SummaryResult();
		ResultSet resultset = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		String resultJson = "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ResultSetFormatter.outputAsJSON(ps, resultset);
        try {
			resultJson = new String(baos.toByteArray(), "UTF-8");
			summaryResult.setJsonResults("["+resultJson+"]");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
	
		return summaryResult;
	}

	/**
	 * This is not intended to be implemented.
	 */
	@Override
	public SummaryResult save(SummaryResult object) {
    	throw new AccessControlException("Summary Query SPARQL DAO is read only");
	}


	public List<String> getAllStudyTitles() {
		List<String> titles = new ArrayList<String>();
		StringBuffer qb = new StringBuffer();
		qb.append("select ?title from <http://ninds.nih.gov:8080/allTriples.ttl> where {");
		qb.append(
				"?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://ninds.nih.gov/repository/fitbir/1.0/Study>.");
		qb.append("?s <http://ninds.nih.gov/repository/fitbir/1.0/Study/title> ?title.");
		qb.append("}");
		String query = qb.toString();

		ResultSet resultSet = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		while (resultSet.hasNext()) {
			QuerySolution row = resultSet.next();
			RDFNode titleNode = row.get("title");
			if (titleNode != null) {
				titles.add(titleNode.asLiteral().toString());
			}
		}
		return titles;
	}

	public List<Study> getAllStudies() {
		List<Study> output = new ArrayList<Study>();
		StringBuffer qb = new StringBuffer();
		qb.append("select ?title from <http://ninds.nih.gov:8080/allTriples.ttl> where {");
		qb.append("?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://ninds.nih.gov/repository/fitbir/1.0/Study>.");
		qb.append("?s <http://ninds.nih.gov/repository/fitbir/1.0/Study/title> ?title.");
		qb.append("?s <http://ninds.nih.gov/repository/fitbir/1.0/Study/studyId> ?studyId.");
		qb.append("}");
		String query = qb.toString();
		
		ResultSet resultSet = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		while (resultSet.hasNext()) {
			Study tempStudy = new Study();
			QuerySolution row = resultSet.next();
			RDFNode titleNode = row.get("title");
			RDFNode idNode = row.get("studyId");
			if (titleNode != null) {
				tempStudy.setTitle(titleNode.asLiteral().toString());
			}
			if (idNode != null) {
				tempStudy.setId(idNode.asLiteral().getLong());
			}
			output.add(tempStudy);

		}
		return output;
	}

	public Study getBasicStudyById(Long id) {
		Study output = null;
		StringBuffer qb = new StringBuffer();
		qb.append("select ?title where {");
		qb.append("?s <http://ninds.nih.gov/repository/fitbir/1.0/Study/studyId> \"" + id.toString() + "\".");
		qb.append("?s <http://ninds.nih.gov/repository/fitbir/1.0/Study/title> ?title.");
		qb.append("} limit 1");
		String query = qb.toString();

		ResultSet resultSet = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		while (resultSet.hasNext()) {
			QuerySolution row = resultSet.next();
			RDFNode titleNode = row.get("title");
			if (titleNode != null) {
				output = new Study();
				output.setTitle(titleNode.asLiteral().toString());
				output.setId(id);
				break;
			}
		}
		return output;
	}

	public Study getBasicStudyByTitle(String title) {
		Study output = null;
		StringBuffer qb = new StringBuffer();
		qb.append("select ?studyId where {");
		qb.append("?s <http://ninds.nih.gov/repository/fitbir/1.0/Study/title> \"" + title + "\".");
		qb.append("?s <http://ninds.nih.gov/repository/fitbir/1.0/Study/studyId> ?studyId.");
		qb.append("} limit 1");
		String query = qb.toString();

		ResultSet resultSet = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		while (resultSet.hasNext()) {
			QuerySolution row = resultSet.next();
			RDFNode idNode = row.get("studyId");
			if (idNode != null) {
				output = new Study();
				output.setTitle(title);
				output.setId(idNode.asLiteral().getLong());
				break;
			}
		}
		return output;
	}
	
	/**
	 * Calls virtuoso with the given query and parses the results into a key/value map of variables and counts (based on the convention of the SPARQL queries).
	 * 
	 * These queries should only be returning two variables: "value" and "count".  Anything else will be ignored.
	 */

	@Override
	public SummaryResult getResultsMapping(String query) {
		SummaryResult summaryResult = new SummaryResult();
		
		Multimap<String, String> summaryResultsMap = summaryResult.getResultsMultiMap();

        ResultSet resultset = virtuosoStore.querySelect(query, MetadataStore.REASONING);

        String strVar = resultset.getResultVars().get(0);
        String countVar = resultset.getResultVars().get(1);
        
       
  
        while (resultset.hasNext()) {
            QuerySolution row = resultset.next();

            if (!row.contains(strVar)) {
                continue;
            }
            
            Literal categoryVal = row.getLiteral(strVar);
            Literal countVal = row.getLiteral(countVar);
            String category = String.valueOf(categoryVal);
            String count = String.valueOf(countVal);

           
            summaryResultsMap.put(category, count);
            
        }
		return summaryResult;
	}
}
