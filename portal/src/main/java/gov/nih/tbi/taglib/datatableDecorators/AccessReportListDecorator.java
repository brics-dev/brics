package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.repository.model.SessionStudy;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;

public class AccessReportListDecorator extends Decorator {
	AccessRecord accessRecord;
	boolean inAdmin;
	
	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);
		
		if (obj instanceof AccessRecord) {
			accessRecord = (AccessRecord) obj;
		}
		
		inAdmin = (boolean) this.getPageContext().findAttribute("inAdmin");
		
		return feedback;
	}
	
	public String getNameLink() {
		BasicDataset ds = accessRecord.getDataset();
		if (this.getPageContext().getRequest().getParameter("exportType") != null) {
			return ds.getName();
		}
		else {
			if (inAdmin) {
				return "<a href=\"javascript:viewDataset('" + ds.getPrefixedId() + "', 'true')\">" + ds.getName() + "</a>";
			} else {
				return "<a href=\"javascript:viewDataset('" + ds.getPrefixedId() + "', 'false')\">" + ds.getName() + "</a>";
			}
		}
	}
	
	public String getUserLink() {
		Account account = accessRecord.getAccount();
		if (this.getPageContext().getRequest().getParameter("exportType") != null) {
			return account.getUserName();
		}
		else {
			return "<a href=\"javascript:viewAccount(" + String.valueOf(account.getId()) + ")\" >" + account.getDisplayName() + "</a>";
		}
	}

}
