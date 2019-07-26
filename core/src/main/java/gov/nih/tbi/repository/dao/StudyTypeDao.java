package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.StudyType;

public interface StudyTypeDao extends GenericDao<StudyType, Long> {
	
	
	
    /**
     * Gets the file types that have a certain classification
     * 
     * @param classification
     * @return
     */
	public StudyType getStudyTypeFromName(String name);
	
    /**
     * Gets the file types that have a certain classification
     * 
     * @param classification
     * @return
     */
	public StudyType getStudyTypeById(Long id);
	
}
