package gov.nih.tbi.account.dao;

import java.util.List;

import gov.nih.tbi.account.model.EmailReportType;
import gov.nih.tbi.account.model.hibernate.AccountEmailReportSetting;
import gov.nih.tbi.commons.dao.GenericDao;

public interface AccountEmailReportSettingDao extends GenericDao<AccountEmailReportSetting, Long> {
	/**
	 * Return all of the report settings with the given report type
	 * 
	 * @param reportType
	 * @return
	 */
	public List<AccountEmailReportSetting> getByReportType(EmailReportType reportType);
}
