package gov.nih.tbi.taglib.datatableDecorators;


import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.OrderStatus;

public class OrderListDecorator extends Decorator {
	
	BiospecimenOrder order;
	Boolean isUserAdmin;
	
	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);
		
		isUserAdmin = (Boolean) this.getPageContext().findAttribute("isAdmin");
		
		if (obj instanceof BiospecimenOrder) {
			order = (BiospecimenOrder)obj;
		}
		else {
			order = null;
		}
		return feedback;
	}

	public String getOrderCheckbox() {
		OrderStatus orderStatus = order.getOrderStatus();
		String disabled = " disabled=\"disabled\"";
		if (isUserAdmin || orderStatus == OrderStatus.CREATED || orderStatus == OrderStatus.CANCELLED || orderStatus == OrderStatus.PERSISTED) {
			disabled = "";
		}
		
		return "<input type=\"checkbox\"" + disabled + " id=\""+ order.getId() +"\" value=\""+ order.getId() +"\" />";
	}
	
	public String getOrderTitle(){
		return order.getOrderTitle();
	}
	
	
	public String getOrderIdLink() {
		return "<a href=\"javascript:void(0);\" onClick=\"$().chooseOrder(" + String.valueOf(order.getId()) + "," + String.valueOf(order.getUser().getId()) + ")\">" + String.valueOf(order.getId()) + "</a>";
	}
	
	public String getAdminSelectInput() {
		return "<input type=\"checkbox\" id=\"" + order.getId() + "\" value=\"" + order.getId() + "\" />";
	}
	
	public String getSubmitterName() {
		return order.getUser().getFirstName() + " " + order.getUser().getLastName();
	}
	
	public String getAdminTitle() {
		return "<a href=\"javascript:void(0);\" onClick=\"$().chooseOrder(" + order.getId() + "," + order.getUser().getId() + ")\">" + order.getId() + "</a>";
	}
}
