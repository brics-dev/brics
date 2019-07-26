
package gov.nih.tbi.ordermanager.dao.impl;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.ordermanager.dao.BioRepositoryDao;
import gov.nih.tbi.ordermanager.model.BioRepository;

@Transactional("metaTransactionManager")
@Repository
public class BioRepositoryDaoImpl extends GenericDaoImpl<BioRepository, Long> implements BioRepositoryDao {

	@Autowired
	public BioRepositoryDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(BioRepository.class, sessionFactory);
	}

	public BioRepository findByName(String name) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BioRepository> query = cb.createQuery(BioRepository.class);
		Root<BioRepository> root = query.from(BioRepository.class);

		query.where(cb.like(cb.upper(root.get("name")), name.toUpperCase()));
		return getUniqueResult(query);
	}
}
