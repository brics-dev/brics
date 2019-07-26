package gov.nih.tbi.account.dao.hibernate;

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
import gov.nih.tbi.account.dao.AccountEmailReportSettingDao;
import gov.nih.tbi.account.model.EmailReportType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountEmailReportSetting;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;

@Transactional("metaTransactionManager")
@Repository
public class AccountEmailReportSettingDaoImpl extends GenericDaoImpl<AccountEmailReportSetting, Long> implements AccountEmailReportSettingDao {

	@Autowired
	public AccountEmailReportSettingDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {
		super(AccountEmailReportSetting.class, sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AccountEmailReportSetting> getByReportType(EmailReportType reportType) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<AccountEmailReportSetting> query = cb.createQuery(persistentClass);
		Root<AccountEmailReportSetting> root = query.from(persistentClass);

		query.where(cb.equal(root.get("reportType"), reportType));

		List<AccountEmailReportSetting> reportSettings = createQuery(query.distinct(true)).getResultList();

		return reportSettings;
	}
}
