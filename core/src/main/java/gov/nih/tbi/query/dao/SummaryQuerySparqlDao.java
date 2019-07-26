package gov.nih.tbi.query.dao;

import java.util.List;

import com.hp.hpl.jena.query.ResultSet;

import gov.nih.tbi.commons.dao.GenericSparqlDao;
import gov.nih.tbi.query.model.SummaryResult;
import gov.nih.tbi.repository.model.hibernate.Study;

/**
 * Interface for the SPARQL communication that queries virtuoso and parses the results into a key/value map.
 * 
 * @author Bill Puschmann
 *
 */
public interface SummaryQuerySparqlDao extends GenericSparqlDao<SummaryResult> {
	public List<String> getAllStudyTitles();

	/**
	 * Gets a list of all studies in the SPARQL graph as Study objects with only title and ID filled in.
	 * 
	 * @return list of all studies (in very basic format)
	 */
	public List<Study> getAllStudies();

	/**
	 * Gets a Study with just title and id filled in by ID
	 * 
	 * @param id
	 * @return
	 */
	public Study getBasicStudyById(Long id);

	/**
	 * Gets a Study with just title and id filled in by title
	 * 
	 * @param title
	 * @return
	 */
	public Study getBasicStudyByTitle(String title);

	public SummaryResult getResultJson(String query);
}
