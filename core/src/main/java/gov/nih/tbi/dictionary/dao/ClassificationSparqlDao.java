package gov.nih.tbi.dictionary.dao;

import java.util.Map;

import gov.nih.tbi.commons.dao.GenericSparqlDao;
import gov.nih.tbi.dictionary.model.hibernate.Classification;


public interface ClassificationSparqlDao extends GenericSparqlDao<Classification>
{
    /**
     * Returns a map of all the classifications in the database. Classification URI -> Classification object
     * 
     * @return
     */
    public Map<String, Classification> getClassificationMap();
    
    public boolean exists(Classification classification);
}
