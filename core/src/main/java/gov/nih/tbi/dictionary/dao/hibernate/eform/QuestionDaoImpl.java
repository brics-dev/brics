package gov.nih.tbi.dictionary.dao.hibernate.eform;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.eform.QuestionDao;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;

@Transactional("dictionaryTransactionManager")
@Repository
public class QuestionDaoImpl extends GenericDictDaoImpl<Question, Long> implements QuestionDao {

	@Autowired
	public QuestionDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {
		super(Question.class, sessionFactory);
	}


	public void deleteQuestions(List<Long> questionIds) {
		if (questionIds != null) {
			for (Long questionId : questionIds) {
				this.remove(questionId);
			}
		}

	}

	public ArrayList<Question> getByIdList(List<Long> questionsIds) {

		if (questionsIds != null && !questionsIds.isEmpty()) {

			CriteriaBuilder cb = getCriteriaBuilder();
			CriteriaQuery<Question> query = cb.createQuery(Question.class);
			Root<Question> root = query.from(Question.class);

			query.where(root.get("id").in(questionsIds)).distinct(true);
			root.fetch("questionDocument", JoinType.LEFT);

			ArrayList<Question> results = (ArrayList<Question>) createQuery(query).getResultList();
			return results;

		} else {
			return new ArrayList<Question>();
		}
	}

	public void batchInsetQuestions(List<Question> questionInsetList) {
		
		Session session = getSessionFactory().openSession();

		Transaction transaction = session.beginTransaction();

		for (int i = 0; i < questionInsetList.size(); i++) {
			session.save(questionInsetList.get(i));
			if (i % 30 == 0) {
				session.flush();
				session.clear();
			}
		}

		transaction.commit();
		session.close();
	}

	public ArrayList<BigInteger> getNextKey(Long batchSize) {

		String sqlSeqGenerator = "SELECT nextval('QUESTION_SEQ') FROM generate_series( 1," + batchSize + ")";
		NativeQuery<BigInteger> query = getSession().createNativeQuery(sqlSeqGenerator);

		return (ArrayList<BigInteger>) query.getResultList();
	}
	
}
