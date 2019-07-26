
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
import gov.nih.tbi.ordermanager.dao.BioRepositoryFileTypeDao;
import gov.nih.tbi.ordermanager.model.BioRepositoryFileType;

@Transactional("metaTransactionManager")
@Repository
public class BioRepositoryFileTypeDaoImpl extends GenericDaoImpl<BioRepositoryFileType, Long> implements BioRepositoryFileTypeDao {

	@Autowired
	public BioRepositoryFileTypeDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(BioRepositoryFileType.class, sessionFactory);
	}

	@Override
	public BioRepositoryFileType get(String fileType) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BioRepositoryFileType> query = cb.createQuery(BioRepositoryFileType.class);
		Root<BioRepositoryFileType> root = query.from(BioRepositoryFileType.class);

		query.where(cb.equal(root.get("name"), fileType)).distinct(true);
		return getUniqueResult(query);
	}

}
