
package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.Collections;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.DomainSubDomainDao;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.DomainSubDomain;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;

@Transactional("dictionaryTransactionManager")
@Repository
public class DomainSubDomainDaoImpl extends GenericDictDaoImpl<DomainSubDomain, Long> implements DomainSubDomainDao {

	@Autowired
	public DomainSubDomainDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(DomainSubDomain.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<Domain> getDomains(SubDomain subDomain) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Domain> query = cb.createQuery(Domain.class);

		Root<DomainSubDomain> root = query.from(DomainSubDomain.class);
		query.select(root.get("domain")).distinct(true);
		query.where(cb.equal(root.join("subDomain").get("id"), subDomain.getId()));

		TypedQuery<Domain> q = createQuery(query);
		return q.getResultList();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<SubDomain> getSubDomains(Domain domain, Disease disease) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SubDomain> query = cb.createQuery(SubDomain.class);

		Root<DomainSubDomain> root = query.from(DomainSubDomain.class);
		query.select(root.get("subDomain")).distinct(true);
		Predicate predicate = cb.conjunction();

		if (domain != null) {
			predicate = cb.and(predicate, cb.equal(root.join("domain").get("id"), domain.getId()));
		}

		if (disease != null) {
			predicate = cb.and(predicate, cb.equal(root.join("disease").get("id"), disease.getId()));
		}

		query.where(predicate);
		List<SubDomain> list = createQuery(query).getResultList();
		Collections.sort(list);
		return list;
	}

	@Override
	public boolean legalPair(Disease disease, Domain domain, SubDomain subDomain) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);

		Root<DomainSubDomain> root = query.from(DomainSubDomain.class);
		query.select(cb.countDistinct(root.get("id")));
		query.where(cb.and(
				cb.equal(root.join("disease").get("id"), disease.getId()),
				cb.equal(root.join("domain").get("id"), domain.getId()),
				cb.equal(root.join("subDomain").get("id"), subDomain.getId())));
		
		long count = getUniqueResult(query);
		return count > 0;
	}

	/**
	 * @inheritDoc
	 */
	public List<Domain> getByDisease(Disease disease) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Domain> query = cb.createQuery(Domain.class);

		Root<DomainSubDomain> root = query.from(DomainSubDomain.class);
		query.select(root.get("domain")).distinct(true);
		query.where(cb.equal(root.join("disease").get("id"), disease.getId()));

		List<Domain> list = createQuery(query).getResultList();
		Collections.sort(list);

		return list;
	}
}
