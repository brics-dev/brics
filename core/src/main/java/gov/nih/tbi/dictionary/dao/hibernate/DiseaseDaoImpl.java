package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.List;

import javax.persistence.TypedQuery;
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
import gov.nih.tbi.dictionary.dao.DiseaseDao;
import gov.nih.tbi.dictionary.model.hibernate.Disease;

@Transactional("dictionaryTransactionManager")
@Repository
public class DiseaseDaoImpl extends GenericDictDaoImpl<Disease, Long> implements DiseaseDao {

	@Autowired
	public DiseaseDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(Disease.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<Disease> getAll() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Disease> query = cb.createQuery(Disease.class);

		Root<Disease> root = query.from(Disease.class);
		query.where(cb.equal(root.get("isActive"), Boolean.TRUE));
		query.orderBy(cb.asc(root.get("name")));

		TypedQuery<Disease> q = createQuery(query.distinct(true));
		return q.getResultList();
	}

	/**
	 * @inheritDoc
	 */
	public String getPrefix(Long diseaseId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);

		Root<Disease> root = query.from(Disease.class);
		query.select(root.get("prefix"));
		query.where(cb.equal(root.get("id"), diseaseId));

		return getUniqueResult(query);
	}
	
}
