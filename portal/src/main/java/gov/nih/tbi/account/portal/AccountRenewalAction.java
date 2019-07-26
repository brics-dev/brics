package gov.nih.tbi.account.portal;

import java.util.ArrayList;
import org.apache.log4j.Logger;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.taglib.datatableDecorators.AccountRenewalListIdtDecorator;

public class AccountRenewalAction extends BaseAction {

	
	private static final long serialVersionUID = -693579168816692280L;
	
	private static Logger logger = Logger.getLogger(AccountRenewalAction.class);
	
	public String list(){
		
		return PortalConstants.ACTION_LIST ;
	}
	
	// url: http://fitbir-portal-local-cit.nih.gov:8080/portal/accountReviewer/accountRenewalAction!getAccountsForRenewal.action
	public String getAccountsForRenewal(){
		
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList <Account> outputList = new ArrayList<Account>(accountManager.getAccountsUpForRenewal());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new AccountRenewalListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

}
