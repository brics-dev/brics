package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.StudyForm;

public interface StudyFormDao extends GenericDao<StudyForm, Long> {
	
	/**
	 * Retrieve a list of instances of a particular form/version, regardless of study.
	 * Useful for finding a list of studies containing a given form.
	 * 
	 * @param shortname form structure shortname
	 * @param version version number of the form structure
	 * @retur
	 */
	public List<StudyForm> getByShortNameVersion(String shortname, String version);
	
	/**
	 * Get all StudyForm entries for a given Study
	 * 
	 * @param studyId
	 * @return
	 */
	public List<StudyForm> getByStudy(Long studyId);
	
	/**
	 * Gets a single StudyForm 
	 * @param studyId
	 * @param shortName
	 * @param version
	 * @return
	 */
	public StudyForm getSingle(Long studyId, String shortName, String version);
}
