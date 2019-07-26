package gov.nih.tbi.account.dao;


import java.util.List;
import java.util.Map;

import gov.nih.tbi.account.model.AccountMessageTemplateType;
import gov.nih.tbi.account.model.hibernate.AccountMessageTemplate;
import gov.nih.tbi.commons.dao.GenericDao;

public interface AccountMessageTemplateDao extends GenericDao<AccountMessageTemplate, Long> {
	
	public List<AccountMessageTemplate> getAccountMessageTemplateListByType(AccountMessageTemplateType accountMessageTemplateType);
	
	Map<Long, AccountMessageTemplate> getAccountMessageTemplateListByIds(List<Long> ids);
	
	public List<AccountMessageTemplate> getgAccountMessageTemplateEmptyMsgListByType(AccountMessageTemplateType accountMessageTemplateType);

}
