package gov.nih.tbi.dictionary.dao.hibernate;

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
import gov.nih.tbi.dictionary.dao.FormLabelDao;
import gov.nih.tbi.dictionary.model.hibernate.FormLabel;


@Transactional("dictionaryTransactionManager")
@Repository
public class FormLabelDaoImpl extends GenericDictDaoImpl<FormLabel, Long> implements FormLabelDao {

	@Autowired
	public FormLabelDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(FormLabel.class, sessionFactory);
	}
	
	@Override
	public List<FormLabel> getAllFormLabels() {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<FormLabel> query = cb.createQuery(FormLabel.class);
		Root<FormLabel> root = query.from(FormLabel.class);
		query.orderBy(cb.asc(root.get("label")));
		
		return createQuery(query).getResultList();
	}
	
	@Override
	public boolean isFormLabelUnique(String formLabel) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<FormLabel> root = query.from(FormLabel.class);

		query.where(cb.equal(cb.upper(root.get("label")), formLabel.toUpperCase()));
		query.select(cb.countDistinct(root));
		long count = createQuery(query).getSingleResult();
		return count == 0;
	}
}
