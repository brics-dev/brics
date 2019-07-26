package gov.nih.tbi.dictionary.dao.eform;

import org.springframework.stereotype.Repository;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestion;

@Repository
public interface CalculationQuestionDao extends GenericDao<CalculationQuestion, Long> { 

}
