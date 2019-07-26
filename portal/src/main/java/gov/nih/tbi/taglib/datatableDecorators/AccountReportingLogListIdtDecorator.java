package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.struts2.ServletActionContext;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountReportingLog;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class AccountReportingLogListIdtDecorator extends IdtDecorator {
	
	AccountReportingLog accountReportingLog;
	Account account;
	
	boolean inAccountAdmin;
	boolean inAccountReviewer;
	

	public String initRow(Object obj, int viewIndex){
		String feedback = super.initRow(obj, viewIndex);
		
		if (obj instanceof AccountReportingLog){
			accountReportingLog = (AccountReportingLog) obj;
		
		}

		inAccountAdmin = (boolean) ServletActionContext.getRequest().getAttribute("inAccountAdmin");
		inAccountReviewer = (boolean) ServletActionContext.getRequest().getAttribute("inAccountReviewer");
		
		return feedback;
	}
	
	public String getUserName() {
		
		return accountReportingLog.getAccount().getUserName();
	}
	
	public String getName() {
		return accountReportingLog.getAccount().getUser().getFullName();
		
	}
	
	public String getEmail() {
		return accountReportingLog.getAccount().getUser().getEmail();
	}
	
	public String getRequestSubmitDate() {
		return BRICSTimeDateUtil.dateToDateString(accountReportingLog.getSubmitedDate());
	}
	
	public String getAccountType() {
		return accountReportingLog.getRequestedAccountType().getName();
	}
	
	public String getRequestType() {
		
		return accountReportingLog.getAccountReportType().getName();
	}
	

}
