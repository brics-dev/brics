package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.struts2.ServletActionContext;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;

public class AccessReportIdtListDecorator extends IdtDecorator {
	AccessRecord accessRecord;
	boolean inAdmin;

	public String initRow(Object obj, int viewIndex) {
		String feedback = super.initRow(obj, viewIndex);

		if (obj instanceof AccessRecord) {
			accessRecord = (AccessRecord) obj;
		}

		inAdmin = (boolean) ServletActionContext.getRequest().getAttribute("inAdmin");

		return feedback;
	}

	public String getNameLink() {
		BasicDataset ds = accessRecord.getDataset();
		if (ServletActionContext.getRequest().getParameter("exportType") != null) {
			return ds.getName();
		} else {
			if (inAdmin) {
				return "<a href=\"javascript:viewDataset('" + ds.getPrefixedId() + "', 'true')\">" + ds.getName()
						+ "</a>";
			} else {
				return "<a href=\"javascript:viewDataset('" + ds.getPrefixedId() + "', 'false')\">" + ds.getName()
						+ "</a>";
			}
		}
	}

	public String getUserLink() {
		Account account = accessRecord.getAccount();
		if (ServletActionContext.getRequest().getParameter("exportType") != null) {
			return account.getUserName();
		} else {
			return "<a href=\"javascript:viewAccount(" + String.valueOf(account.getId()) + ")\" >"
					+ account.getDisplayName() + "</a>";
		}
	}
}
