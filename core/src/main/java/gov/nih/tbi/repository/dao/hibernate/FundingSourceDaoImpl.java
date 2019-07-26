package gov.nih.tbi.repository.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.FundingSourceDao;
import gov.nih.tbi.repository.model.hibernate.FundingSource;

@Transactional("metaTransactionManager")
@Repository
public class FundingSourceDaoImpl extends GenericDaoImpl<FundingSource, Long> implements FundingSourceDao {

	@Autowired
	public FundingSourceDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(FundingSource.class, sessionFactory);
	}

}
