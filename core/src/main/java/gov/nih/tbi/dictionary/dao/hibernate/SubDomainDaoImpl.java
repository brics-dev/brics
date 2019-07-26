
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
import gov.nih.tbi.dictionary.dao.SubDomainDao;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;

@Transactional("dictionaryTransactionManager")
@Repository
public class SubDomainDaoImpl extends GenericDictDaoImpl<SubDomain, Long> implements SubDomainDao {

	@Autowired
	public SubDomainDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(SubDomain.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<SubDomain> getAll() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SubDomain> query = cb.createQuery(SubDomain.class);
		Root<SubDomain> root = query.from(SubDomain.class);

		query.where(cb.equal(root.get("isActive"), Boolean.TRUE));
		query.orderBy(cb.asc(root.get("name"))).distinct(true);

		List<SubDomain> outList = createQuery(query).getResultList();
		return outList;
	}
}
