package gov.nih.tbi.dictionary.dao.hibernate.eform;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.eform.QuestionDocumentDao;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocument;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocumentPk;

@Transactional("dictionaryTransactionManager")
@Repository
public class QuestionDocumentDaoImpl extends GenericDictDaoImpl<QuestionDocument, Long> implements QuestionDocumentDao {

	@Autowired
	public QuestionDocumentDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {
		super(QuestionDocument.class, sessionFactory);
	}

	/**
	 * get the list of documents associated to and eform based off the question id
	 */
	public List<QuestionDocument> getQuestionDocumentsByQuestionId(Question documentId) {

		if (documentId != null) {

			CriteriaBuilder cb = getCriteriaBuilder();
			CriteriaQuery<QuestionDocument> query = cb.createQuery(QuestionDocument.class);
			Root<QuestionDocument> root = query.from(QuestionDocument.class);

			query.where(cb.equal(root.get("questionDocumentPk").get("question"), documentId)).distinct(true);
			return createQuery(query).getResultList();

		} else {
			return null;
		}
	}

	public void deleteQuestionImages(List<QuestionDocument> documents) {

		if (documents != null && !documents.isEmpty()) {
			// loop over every document and delete it
			for (QuestionDocument qd : documents) {
				getSession().delete(qd);
			}
		}
	}

	public QuestionDocument getQuestionDocument(QuestionDocumentPk documentId) {
		if (documentId.getQuestion().getId() != null && documentId.getFileName() != null
				&& !documentId.getFileName().trim().equals("")) {
			CriteriaBuilder cb = getCriteriaBuilder();
			CriteriaQuery<QuestionDocument> query = cb.createQuery(QuestionDocument.class);
			Root<QuestionDocument> root = query.from(QuestionDocument.class);

			query.where(cb.equal(root.get("questionDocumentPk"), documentId)).distinct(true);
			return getUniqueResult(query);
		}
		return null;
	}

}
