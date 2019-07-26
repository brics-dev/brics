package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.log4j.Logger;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class AccountReportingListIdtDecorator extends IdtDecorator {
	EntityMap permission;

	static Logger logger = Logger.getLogger(AccountReportingListIdtDecorator.class);

	public String initRow(Object obj, int rowIndex) {
		String feedback = super.initRow(obj, rowIndex);

		return feedback;
	}


	
	public String getFirstname() {
		Account account = (Account) this.getObject();
		String output = "";
		if(account.getUser() != null) {
			output += account.getUser().getFirstName();
		}
		return output;
	}
	

	public String getLastname() {
		Account account = (Account) this.getObject();
		String output = "";
		if(account.getUser() != null) {
			output += account.getUser().getLastName();
		}
		return output;
	}

	public String getAccountStatus() {
		Account account = (Account) this.getObject();
		String output = "";
		if(account.getAccountStatus() != null) {
			output += account.getAccountStatus().getName();
		}
		return output;
	}

	public String getAccountRole() {
		Account account = (Account) this.getObject();
		String output = "";
		
		StringBuffer roleBuffer = new StringBuffer();
		int count = 0;
		
		for(AccountRole role : account.getAccountRoleList()) {
			count++;
			if(count % 4 == 0) {
				roleBuffer.append("<br></br>");
			}else {
				roleBuffer.append(role.getRoleType().name()).append("(").append(role.getRoleStatus().name()).append(")").append(",");
			}	
		}
		output += roleBuffer.toString();
		return output;
	}
	
	
}
