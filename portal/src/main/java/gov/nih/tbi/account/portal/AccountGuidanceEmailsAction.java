package gov.nih.tbi.account.portal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.log4j.Logger;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.AccountMessageTemplateType;
import gov.nih.tbi.account.model.SessionAccountMessageTemplates;
import gov.nih.tbi.account.model.hibernate.AccountMessageTemplate;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.service.AccountMessageTemplateManager;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.taglib.datatableDecorators.AccountMessageTemplateListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.AccountRoleListIdtDecorator;

public class AccountGuidanceEmailsAction extends BaseAction {

	
	private static final long serialVersionUID = 1104126130743671394L;
	private static Logger logger = Logger.getLogger(AccountGuidanceEmailsAction.class);

	@Autowired
	protected AccountMessageTemplateManager accountMessageTemplateManager;
	
	@Autowired
	protected SessionAccountMessageTemplates sessionAccountGuidanceEmails;
	
	private AccountMessageTemplate currentAccountMessageTemplate;
	private String checkboxText;
	private String message;
	private Boolean defaultChecked;
	private String typeid;
	private String accountTemplateId;
	
	
	public String getTypeid() {
		return typeid;
	}



	public void setTypeid(String typeid) {
		this.typeid = typeid;
	}


	/**
	 * ajax call to save new email template and refresh the lists
	 * @return
	 */
	public String saveAccountGuidanceEmailTemplate() {
		
		
		AccountMessageTemplate currentAccountMessageTemplate =getSessionAccountGuidanceEmails().getAccountMessageTemplate();
		
		if(currentAccountMessageTemplate==null){
			currentAccountMessageTemplate = new AccountMessageTemplate();
		}
		
	
		long accountMessageId = Long.parseLong(getAccountTemplateId());
		
		if(accountMessageId != 0) {
			currentAccountMessageTemplate.setId(accountMessageId);
		}
		currentAccountMessageTemplate.setCheckboxText(getCheckboxText());
		currentAccountMessageTemplate.setMessage(getMessage());
		
		AccountMessageTemplateType type = null;
		if(getTypeid().equals(AccountMessageTemplateType.TEMPORARY_REJECTION.name())) {
			type = AccountMessageTemplateType.TEMPORARY_REJECTION;
		}
		else if(getTypeid().equals(AccountMessageTemplateType.ACCOUNT_RENEWAL.name())) {
			type = AccountMessageTemplateType.ACCOUNT_RENEWAL;
		}
		else{
			throw new RuntimeException("Unsupported type id: "+ getTypeid()+".There is no accountMessageTemplateType available with passed type id");
		}
		
		currentAccountMessageTemplate.setAccountMessageTemplateType(type);
		currentAccountMessageTemplate.setDefaultChecked(getDefaultChecked()); 
		
		accountMessageTemplateManager.saveAccountMessageTemplate(currentAccountMessageTemplate);
		

		//get the new lists
		view();
		
		return null;   
	}
	
	
	
	public String deleteAccountMessageTemplate() {
		
		
		long accountMessageId = Long.parseLong(getAccountTemplateId());
		accountMessageTemplateManager.deleteAccountMessageTemplate(accountMessageId);
		
		//get the new lists
		view();
		
		return null;  
	}
	
	

	/**
	 * gets the lists of temporary rejection and account renewals and puts them in session
	 * @return
	 */
	public String view() {
		getSessionAccountGuidanceEmails().clearAll();
		
		List<AccountMessageTemplate> accountTemporaryRejectionList = accountMessageTemplateManager.getAccountTemporaryRejectionList();
		List<AccountMessageTemplate> accountRenewalList = accountMessageTemplateManager.getAccountRenewalList();

		getSessionAccountGuidanceEmails().setAccountTemporaryRejectionList(accountTemporaryRejectionList);
		getSessionAccountGuidanceEmails().setAccountRenewalList(accountRenewalList);
		
		

		return SUCCESS; 
	}
	
	public String edit() {
		
		getSessionAccountGuidanceEmails().clearCurrentAccountMessageTemplate();
		
		currentAccountMessageTemplate = getAccountMessageTemplateFromRequest();
		getSessionAccountGuidanceEmails().setAccountMessageTemplate(currentAccountMessageTemplate);
		
	
		return PortalConstants.ACTION_EDIT;
	}
	
	
	
	
	public SessionAccountMessageTemplates getSessionAccountGuidanceEmails() {
		if(sessionAccountGuidanceEmails == null) {
			sessionAccountGuidanceEmails = new SessionAccountMessageTemplates();
		}
		return sessionAccountGuidanceEmails;
	}
	
	
	
	
	public String getTemporaryRejectionList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<AccountMessageTemplate> accountTemporaryRejectionList = new ArrayList<AccountMessageTemplate>(getAccountTemporaryRejectionList());
			idt.setList(accountTemporaryRejectionList);
			idt.setTotalRecordCount(accountTemporaryRejectionList.size());
			idt.setFilteredRecordCount(accountTemporaryRejectionList.size());
			idt.decorate(new AccountMessageTemplateListDecorator(AccountMessageTemplateType.TEMPORARY_REJECTION.name()));
			idt.output();
		} catch (InvalidColumnException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	private List<AccountMessageTemplate> getAccountTemporaryRejectionList () {
		
		return getSessionAccountGuidanceEmails().getAccountTemporaryRejectionList();
	}
	
	
	
	
	public String getAccountRenewalList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<AccountMessageTemplate> accountRenewalList = new ArrayList<AccountMessageTemplate>(getAccountAccountRenewalList());
			idt.setList(accountRenewalList);
			idt.setTotalRecordCount(accountRenewalList.size());
			idt.setFilteredRecordCount(accountRenewalList.size());
			idt.decorate(new AccountMessageTemplateListDecorator(AccountMessageTemplateType.ACCOUNT_RENEWAL.name()));
			idt.output();
		} catch (InvalidColumnException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	private List<AccountMessageTemplate> getAccountAccountRenewalList () {
		
		return getSessionAccountGuidanceEmails().getAccountRenewalList();
	}
	
	
	
	private AccountMessageTemplate getAccountMessageTemplateFromRequest(){
		
		String idStr = getAccountTemplateId();
		
		if(!StringUtils.isBlank(idStr)){
			try{
				long accountMessageId = Long.parseLong(idStr);
				return accountMessageTemplateManager.getAccountMessageTemplate(accountMessageId);
			} catch (NumberFormatException ne){
				logger.error("AccountMessageTemplateId passed in is not a valid number "+idStr,ne);
			}
		}
		
		return null;
	}



	public String getCheckboxText() {
		return checkboxText;
	}



	public void setCheckboxText(String checkboxText) {
		this.checkboxText = checkboxText;
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	public Boolean getDefaultChecked() {
		return defaultChecked;
	}



	public void setDefaultChecked(Boolean defaultChecked) {
		this.defaultChecked = defaultChecked;
	}



	public String getAccountTemplateId() {
		return accountTemplateId;
	}



	public void setAccountTemplateId(String accountTemplateId) {
		this.accountTemplateId = accountTemplateId;
	}



	public AccountMessageTemplate getCurrentAccountMessageTemplate() {
		return currentAccountMessageTemplate;
	}



	public void setCurrentAccountMessageTemplate(AccountMessageTemplate currentAccountMessageTemplate) {
		this.currentAccountMessageTemplate = currentAccountMessageTemplate;
	}
	
}
