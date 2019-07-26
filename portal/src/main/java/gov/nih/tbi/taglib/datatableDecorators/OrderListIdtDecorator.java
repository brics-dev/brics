package gov.nih.tbi.taglib.datatableDecorators;


import java.util.List;
import org.apache.struts2.ServletActionContext;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;

public class OrderListIdtDecorator extends IdtDecorator{
	
	BiospecimenOrder order;
	List<BiospecimenOrder> biospecimenOrderList;
	Boolean isUserAdmin;
	
	public OrderListIdtDecorator(List<BiospecimenOrder> biospecimenOrderList){
		super();
		this.biospecimenOrderList = biospecimenOrderList;
	}
	
	public String initRow(Object obj, int viewIndex) {
		String feedback = super.initRow(obj, viewIndex);
		
		isUserAdmin = (Boolean) ServletActionContext.getRequest().getAttribute("isAdmin");
		
		if (obj instanceof BiospecimenOrder) {
			order = (BiospecimenOrder)obj;
		}
		else {
			order = null;
		}
		return feedback;
	}
	
	public String getAdminTitle() {
		return "<a href=\"javascript:void(0);\" onClick=\"$().chooseOrder(" + order.getId() + "," + order.getUser().getId() + ")\">" + order.getId() + "</a>";
	}
	
	public String getOrderTitle(){
		return order.getOrderTitle();
	}

	public String getOrderStatus(){
		return order.getOrderStatus().getValue();
	}
	
	public String getDateCreated() {
		return BRICSTimeDateUtil.dateToDateString(order.getDateCreated());
	}
	
	public String getDateSubmitted() {
		return BRICSTimeDateUtil.dateToDateString(order.getDateSubmitted());
	}
	
	public String getSubmitterName() {
		return order.getUser().getFirstName() + " " + order.getUser().getLastName();
	}
	


}
