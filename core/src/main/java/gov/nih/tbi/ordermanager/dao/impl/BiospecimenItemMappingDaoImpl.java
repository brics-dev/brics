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
import gov.nih.tbi.ordermanager.dao.BiospecimenItemMappingDao;
import gov.nih.tbi.ordermanager.model.BiospecimenItemMapping;


@Transactional("metaTransactionManager")
@Repository
public class BiospecimenItemMappingDaoImpl extends GenericDaoImpl<BiospecimenItemMapping, Long> implements BiospecimenItemMappingDao {

	@Autowired
	public BiospecimenItemMappingDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(BiospecimenItemMapping.class, sessionFactory);
	}

	public BiospecimenItemMapping findByFormName(String formName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BiospecimenItemMapping> query = cb.createQuery(BiospecimenItemMapping.class);
		Root<BiospecimenItemMapping> root = query.from(BiospecimenItemMapping.class);

		query.where(cb.equal(root.get("formName"), formName)).distinct(true);
		return getUniqueResult(query);
	}
}
