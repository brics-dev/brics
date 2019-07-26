package gov.nih.tbi.account.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.nih.tbi.account.model.hibernate.AccountMessageTemplate;

public class SessionAccountMessageTemplates implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 6176570699176013285L;

	private List<AccountMessageTemplate> accountTemporaryRejectionList;
	
	private List<AccountMessageTemplate> accountRenewalList;
	
	private AccountMessageTemplate accountMessageTemplate;
	
	
	public SessionAccountMessageTemplates() {
		super();
		accountTemporaryRejectionList = new ArrayList<AccountMessageTemplate>();
		accountRenewalList = new ArrayList<AccountMessageTemplate>();
		accountMessageTemplate = new AccountMessageTemplate();
	}


	
	
	
	public List<AccountMessageTemplate> getAccountTemporaryRejectionList() {
		return accountTemporaryRejectionList;
	}



	public void setAccountTemporaryRejectionList(List<AccountMessageTemplate> accountTemporaryRejectionList) {
		this.accountTemporaryRejectionList = accountTemporaryRejectionList;
	}



	public List<AccountMessageTemplate> getAccountRenewalList() {
		return accountRenewalList;
	}



	public void setAccountRenewalList(List<AccountMessageTemplate> accountRenewalList) {
		this.accountRenewalList = accountRenewalList;
	}

	

	public AccountMessageTemplate getAccountMessageTemplate() {
		return accountMessageTemplate;
	}


	public void setAccountMessageTemplate(AccountMessageTemplate accountMessageTemplate) {
		this.accountMessageTemplate = accountMessageTemplate;
	}


	public void clearAll() {
		
		accountTemporaryRejectionList = new ArrayList<AccountMessageTemplate>();
		accountRenewalList = new ArrayList<AccountMessageTemplate>();
		accountMessageTemplate = null;
	}
	
	public void clearCurrentAccountMessageTemplate(){
		accountMessageTemplate = null;
	}

}
