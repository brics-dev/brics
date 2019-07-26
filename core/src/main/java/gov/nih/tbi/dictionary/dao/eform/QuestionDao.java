package gov.nih.tbi.dictionary.dao.eform;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;

@Repository
public interface QuestionDao extends GenericDao<Question, Long> {
	public void deleteQuestions(List<Long> questionIds);
	
	public ArrayList<Question> getByIdList(List<Long> questionsIds);
	
	public void batchInsetQuestions(List<Question> questionInsetList);
	
	public ArrayList<BigInteger> getNextKey(Long batchSize);
}