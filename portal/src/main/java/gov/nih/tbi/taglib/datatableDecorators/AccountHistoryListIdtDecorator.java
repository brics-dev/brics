package gov.nih.tbi.taglib.datatableDecorators;

import java.util.Date;

import org.apache.struts2.ServletActionContext;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class AccountHistoryListIdtDecorator extends IdtDecorator {
	
	AccountHistory accountHistory;
	Account account;
	boolean inAccountAdmin;
	boolean inAccountReviewer;
	
	public AccountHistoryListIdtDecorator(Account account) {
		super();
		this.account = account;
	}
	
	public String initRow(Object obj, int viewIndex){
		String feedback = super.initRow(obj, viewIndex);
		
		if (obj instanceof AccountHistory){
			accountHistory = (AccountHistory) obj;
		}

		inAccountAdmin = (boolean) ServletActionContext.getRequest().getAttribute("inAccountAdmin");
		inAccountReviewer = (boolean) ServletActionContext.getRequest().getAttribute("inAccountReviewer");
		
		return feedback;
	}
	
	public String getActionType(){
		
		if (inAccountAdmin || inAccountReviewer || (accountHistory.getChangedByUser() != null
				&& accountHistory.getChangedByUser().equals(account.getUser()))) {
			return accountHistory.getActionTypeText(false);
		}
			
		return accountHistory.getActionTypeText(true);
	}
	
	public String getComment(){
		
		String comment = "";
		
		if(accountHistory.getComment()!=null){
			comment = accountHistory.getComment();;
		}
		
		return comment;
	}
	
	public Date getActionTime(){
		
		return accountHistory.getCreatedDate();
	}

	public String getActionDate(){
		
		return BRICSTimeDateUtil.dateToDateString(accountHistory.getCreatedDate());
	}
	
}
