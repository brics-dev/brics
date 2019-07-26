package gov.nih.tbi.taglib.datatableDecorators;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class AccountRenewalListIdtDecorator extends IdtDecorator {
	
	Account account;
	
	public String initRow(Object obj,int viewIndex){
		
		String feedback = super.initRow(obj, viewIndex);
		
		if(obj instanceof Account){
			account = (Account)obj;
		}
		return feedback;
	}
	
	public String getUserName(){
		
		String accountUserName = account.getUserName();

		return "<a href='/portal/accountReviewer/viewAccountRenewal!viewAccountRenewal.action?accountId="
					+ account.getId() + "'>" + accountUserName + "</a>";
	}

	public String getExpiringStatus(){
		
		Set<AccountRole> accountRoleList = account.getAccountRoleList();
		
		for(AccountRole accountRole:accountRoleList ){
			if(accountRole.getRoleStatus()==RoleStatus.EXPIRED || accountRole.getRoleStatus()==RoleStatus.EXPIRING_SOON){
				return accountRole.getRoleStatus().getName();
			}
		}
		return "";
	}
	
	public String getExpirationDate() {
		return account.getExpirationDate();
	}
	
	public String getLastUpdated() {
		
		if(account.getLatestAccountHistory()!=null){
			Date lastUpdatedDate = account.getLatestAccountHistory().getCreatedDate();
			
			return BRICSTimeDateUtil.formatDate(lastUpdatedDate);
		}
		
		if(account.getLastUpdatedDate()!=null){
			return BRICSTimeDateUtil.formatDate(account.getLastUpdatedDate());
		}
		
		return "";		
	}
	
	public String getLastAction() {
		
		if(account.getLatestAccountHistory()!=null){
			return account.getLatestAccountHistory().getActionTypeText(false);
		}
		
		return "";
	}
	
	public boolean getIsExpired() {
		
		Set<AccountRole> accountRoleList = account.getAccountRoleList();
		
		for(AccountRole accountRole:accountRoleList ){
			if(accountRole.getRoleStatus()==RoleStatus.EXPIRED){
				return true;
			}
		}
		return false;
	}
	
	public boolean getIsActiveExpringSoon() {
		
		Set<AccountRole> accountRoleList = account.getAccountRoleList();
		
		for(AccountRole accountRole:accountRoleList ){
			if(accountRole.getRoleStatus()==RoleStatus.EXPIRING_SOON){
				return true;
			}
		}
		return false;
	}
}
