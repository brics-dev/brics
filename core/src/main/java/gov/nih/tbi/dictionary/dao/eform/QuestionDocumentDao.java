package gov.nih.tbi.dictionary.dao.eform;

import java.util.List;

import org.springframework.stereotype.Repository;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocument;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocumentPk;

@Repository
public interface QuestionDocumentDao extends GenericDao<QuestionDocument, Long> { 

	public List<QuestionDocument> getQuestionDocumentsByQuestionId(Question questionId);
	
	public void deleteQuestionImages(List<QuestionDocument> documents);
	
	public QuestionDocument getQuestionDocument(QuestionDocumentPk documentId);
}
