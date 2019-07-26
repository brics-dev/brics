package gov.nih.tbi.dictionary.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationDisease;
import gov.nih.tbi.dictionary.model.hibernate.Disease;


public interface ClassificationDiseaseDao extends GenericDao<ClassificationDisease, Long>
{
    /**
     * Returns a list of classifications by disease
     * @param disease
     * @param isAdmin
     * @return
     */
    List<Classification> getByDisease(Disease disease, boolean isAdmin);

}
