package gov.nih.tbi.commons.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import gov.nih.tbi.account.model.AccountMessageTemplateType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountMessageTemplate;


public interface AccountMessageTemplateManager extends BaseManager {

	
	
	
	/**
	 * Saves the Account Message Template
	 * 
	 * @param template
	 * @return
	 */
	public AccountMessageTemplate saveAccountMessageTemplate(AccountMessageTemplate accountMessageTemplate);
	
	
	/**
	 * Gets list of Account Message Templates
	 * 
	 * @return
	 */
	public List<AccountMessageTemplate> getAccountMessageTemplateList();
	
	
	
	/**
	 * Gets list of Temporary Rejection Message Templates
	 * 
	 * @return
	 */
	public List<AccountMessageTemplate> getAccountTemporaryRejectionList();
	
	
	
	/**
	 * Gets list of Account Renewal Message Templates
	 * 
	 * @return
	 */
	public List<AccountMessageTemplate> getAccountRenewalList();
	
	
	
	
	/**
	 * Deletes an Account Message Template
	 * 
	 * @param id
	 */
	public void deleteAccountMessageTemplate(Long id);
	
	public AccountMessageTemplate getAccountMessageTemplate(Long id);
	
	public Map<Long,AccountMessageTemplate> getAccountMessageTemplateListByIds(List<Long> ids);
	
	public List<AccountMessageTemplate> getAccountMessageTemplateListByType(AccountMessageTemplateType accountMessageTemplateType);
	
	
	public void contactUser(Account currentAccount, List<AccountMessageTemplate> accountMsgs,String orgName,String orgUrl,String orgEmail);
	
	public List<AccountMessageTemplate> getgAccountMessageTemplateEmptyMsgListByType(AccountMessageTemplateType accountMessageTemplateType);
	
}
