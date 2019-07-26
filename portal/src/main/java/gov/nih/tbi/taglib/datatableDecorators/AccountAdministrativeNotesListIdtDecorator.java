package gov.nih.tbi.taglib.datatableDecorators;
import java.text.SimpleDateFormat;

import org.apache.struts2.ServletActionContext;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountAdministrativeNote;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class AccountAdministrativeNotesListIdtDecorator extends IdtDecorator {
	
	AccountAdministrativeNote accountAdministrativeNote;
	Account account;
	
	boolean inAccountAdmin;
	boolean inAccountReviewer;
	
	
	public AccountAdministrativeNotesListIdtDecorator(Account account){
		super();
		this.account = account;
	}
	
	public String initRow(Object obj, int viewIndex){
		String feedback = super.initRow(obj, viewIndex);
		
		if (obj instanceof AccountAdministrativeNote){
			accountAdministrativeNote = (AccountAdministrativeNote) obj;
		
		}

		inAccountAdmin = (boolean) ServletActionContext.getRequest().getAttribute("inAccountAdmin");
		inAccountReviewer = (boolean) ServletActionContext.getRequest().getAttribute("inAccountReviewer");
		
		return feedback;
	}
	
	public String getAccountName(){
		return accountAdministrativeNote.getAdminAccount().getDisplayName();
		
	}
	
	public String getDate(){

        return BRICSTimeDateUtil.dateToDateString(accountAdministrativeNote.getDate());
	}
	
}
	