package gov.nih.tbi.dictionary.dao.hibernate.eform;

import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.eform.CountQuestionDao;
import gov.nih.tbi.dictionary.model.hibernate.eform.CountQuestion;


@Transactional("dictionaryTransactionManager")
@Repository
public class CountQuestionDaoImpl extends GenericDictDaoImpl<CountQuestion, Long> implements CountQuestionDao {

	public CountQuestionDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {
		super(CountQuestion.class, sessionFactory);
	}

}
