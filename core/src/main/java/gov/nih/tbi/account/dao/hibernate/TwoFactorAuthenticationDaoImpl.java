package gov.nih.tbi.account.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.TwoFactorAuthenticationDao;
import gov.nih.tbi.account.model.hibernate.TwoFactorAuthentication;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;

@Transactional("metaTransactionManager")
@Repository
public class TwoFactorAuthenticationDaoImpl extends GenericDaoImpl<TwoFactorAuthentication, Long> implements TwoFactorAuthenticationDao {

	@Autowired
	public TwoFactorAuthenticationDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {
		super(TwoFactorAuthentication.class, sessionFactory);
	}
}
