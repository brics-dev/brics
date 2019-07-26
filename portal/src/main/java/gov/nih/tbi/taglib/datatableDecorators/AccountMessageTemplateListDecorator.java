package gov.nih.tbi.taglib.datatableDecorators;


import gov.nih.tbi.account.model.hibernate.AccountMessageTemplate;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class AccountMessageTemplateListDecorator extends IdtDecorator {

	String type;


	public AccountMessageTemplateListDecorator(String type) {
		super();
		this.type = type;
	}
	
	
	
	public String getDecoratedCheckboxText() {
		AccountMessageTemplate accountMessageTemplate = (AccountMessageTemplate) this.getObject();
		
		return accountMessageTemplate.getCheckboxText();
	}
	
	
	public String getDecoratedMessage() {
		AccountMessageTemplate accountMessageTemplate = (AccountMessageTemplate) this.getObject();
		String message = accountMessageTemplate.getMessage();
		if(message != null && !message.equals("") ) {
			return message;
		}else {
			return "{Text Field in Workflow}";
		}
	
	}
	
	
	public String getDecoratedDefaultChecked() {
		AccountMessageTemplate accountMessageTemplate = (AccountMessageTemplate) this.getObject();
		
		if(accountMessageTemplate.getDefaultChecked() == Boolean.TRUE) {
			return "Yes";
		}else {
			return "No";
		}
	}
	
	
	public String getDecoratedActions() {
		AccountMessageTemplate accountMessageTemplate = (AccountMessageTemplate) this.getObject();
		
		//may want to add type to the edit link?
		String editLink = "<a href =\"javascript:editAccountMessageTemplate("+accountMessageTemplate.getId()+",'" + type + "')\"\">Edit</a>";
			
		String deleteLink = "<a href =\"javascript:deleteAccountMessageTemplate("+accountMessageTemplate.getId()+",'" + type + "')\"\">Delete</a>";
		String link = editLink + "&nbsp;" + deleteLink;
		return link;
	}
	
	

}
