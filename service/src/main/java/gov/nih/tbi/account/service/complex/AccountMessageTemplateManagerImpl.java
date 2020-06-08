package gov.nih.tbi.account.service.complex;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import gov.nih.tbi.account.dao.AccountMessageTemplateDao;
import gov.nih.tbi.account.model.AccountEmailReportFrequency;
import gov.nih.tbi.account.model.AccountMessageTemplateType;
import gov.nih.tbi.account.model.EmailReportType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountEmailReportSetting;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.account.model.hibernate.AccountMessageTemplate;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.service.AccountMessageTemplateManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.util.MailEngine;



@Service
@Scope("singleton")
public class AccountMessageTemplateManagerImpl extends BaseManagerImpl
		implements AccountMessageTemplateManager, Serializable {

	
	private static final long serialVersionUID = 7325665561470562270L;

	@Autowired
	AccountMessageTemplateDao accountMessageTemplateDao;
	
	@Autowired
	private MailEngine mailEngine;
	
	
	public AccountMessageTemplate saveAccountMessageTemplate(AccountMessageTemplate accountMessageTemplate) {
		accountMessageTemplate = accountMessageTemplateDao.save(accountMessageTemplate);
		return accountMessageTemplate;
	}



	/**
	 * 
	 */
	public List<AccountMessageTemplate> getAccountMessageTemplateList() {
		return accountMessageTemplateDao.getAll();
	}
	
	
	/**
	 * 
	 */
	public List<AccountMessageTemplate> getAccountTemporaryRejectionList() {
		List<AccountMessageTemplate> accountMessageTemplateList = accountMessageTemplateDao.getAll();
		
		Iterator<AccountMessageTemplate> iter = accountMessageTemplateList.iterator();
		while(iter.hasNext()) {
			AccountMessageTemplate template = (AccountMessageTemplate)iter.next();
			if(template.getAccountMessageTemplateType() != AccountMessageTemplateType.TEMPORARY_REJECTION) {
				iter.remove();
			}
			
		}

		return accountMessageTemplateList;
	}
	
	
	
	/**
	 * 
	 */
	public List<AccountMessageTemplate> getAccountRenewalList() {
		List<AccountMessageTemplate> accountMessageTemplateList = accountMessageTemplateDao.getAll();
		
		Iterator<AccountMessageTemplate> iter = accountMessageTemplateList.iterator();
		while(iter.hasNext()) {
			AccountMessageTemplate template = (AccountMessageTemplate)iter.next();
			if(template.getAccountMessageTemplateType() != AccountMessageTemplateType.ACCOUNT_RENEWAL) {
				iter.remove();
			}
			
		}
		
		return accountMessageTemplateList;
	}
	
	/**
	 * 
	 */
	public void deleteAccountMessageTemplate(Long id) {
		accountMessageTemplateDao.remove(id);
	}



	@Override
	public AccountMessageTemplate getAccountMessageTemplate(Long id) {
		return accountMessageTemplateDao.get(id);
	}



	@Override
	public List<AccountMessageTemplate> getAccountMessageTemplateListByType(AccountMessageTemplateType accountMessageTemplateType){
		return accountMessageTemplateDao.getAccountMessageTemplateListByType(accountMessageTemplateType);
	}



	@Override
	public void contactUser(Account currentAccount, List<AccountMessageTemplate> accountMsgs,String orgName,String orgUrl,String orgEmail) {
		if(orgName.contains("_")) {
			orgName = orgName.replaceAll("_", " ");
		}
		String accountRenewalSubject = String.format(ServiceConstants.ACCOUNT_RENEWAL_SUBJECT,orgName);
		String accountRenewalEmailBody = buildAccountRenewalEmail(currentAccount,accountMsgs,orgName,orgUrl,orgEmail);
		
		try {
			mailEngine.sendMail(accountRenewalSubject, accountRenewalEmailBody, null, currentAccount.getUser().getEmail());
			
		} catch (MessagingException e) {
			logger.error("The system failed to send the email to user:"+ currentAccount.getUser().getFullName()+ e);
		}

	}
	
	public String buildAccountRenewalEmail(Account currentAccount, List<AccountMessageTemplate> accountMsgs,String orgName,String orgUrl,String orgEmail){
		
		String info = String.format(ServiceConstants.ACCOUNT_RENEWAL_INFO_FORMAT,orgName);
		
		StringBuffer renewalMsgBuffer = new StringBuffer();
		for(AccountMessageTemplate accountMsg: accountMsgs ){
			
			String accountRenewalMsg = String.format(ServiceConstants.ACCOUNT_RENEWAL_MSG_FORMAT, accountMsg.getCheckboxText(),accountMsg.getMessage());
			renewalMsgBuffer.append(accountRenewalMsg);
			renewalMsgBuffer.append("<br><br>");
		}
		
		String adminMsg = String.format(ServiceConstants.ACCOUNT_RENEWAL_SYSTEM_MSG_FORMAT, orgName);
		String footer = String.format(ServiceConstants.ACCOUNT_RENEWAL_FOOTER_FORMAT, orgUrl,orgEmail,orgName);
		
		String bodyMsg = String.format(ServiceConstants.ACCOUNT_RENEWAL_BODY_FORMAT, info,buildAccountRenewalTable(currentAccount),adminMsg,
				renewalMsgBuffer.toString(),footer);
		
		return bodyMsg;
	}
	
	public String buildAccountRenewalTable(Account currentAccount){
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table border=\"1\">");
		buffer.append(ServiceConstants.ACCOUNT_RENEWAL_TABLE_HEADER);
		
		Set<AccountRole> accountRoles = currentAccount.getAccountRoleList();
		
		for(AccountRole role: accountRoles){
		
			String rowString  = String.format(ServiceConstants.ACCOUNT_RENEWAL_ROW_FORMAT, role.getRoleType().getTitle(),
					role.getExpirationDateISOFormat());
			
			buffer.append(rowString);
		}
		
		buffer.append("</table>");

		return buffer.toString();
	}



	@Override
	public Map<Long, AccountMessageTemplate> getAccountMessageTemplateListByIds(List<Long> ids) {
		return accountMessageTemplateDao.getAccountMessageTemplateListByIds(ids);
	}
	
	public List<AccountMessageTemplate> getgAccountMessageTemplateEmptyMsgListByType(AccountMessageTemplateType accountMessageTemplateType){
		return accountMessageTemplateDao.getgAccountMessageTemplateEmptyMsgListByType(accountMessageTemplateType);
	}
}
