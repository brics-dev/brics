package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.ordermanager.model.BioRepository;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;

public class OrderQueueListIdtDecorator extends IdtDecorator {
	BiospecimenItem sample;
	
	public String initRow(Object obj, int viewIndex) {
		String feedback = super.initRow(obj, viewIndex);
		
		sample = (BiospecimenItem) this.getObject();
		
		return feedback;
	}
	
	
	public String getQueueQuantityInputField() {
		String sampleId = String.valueOf(sample.getId());
		return "<input type=\"text\" class=\"itemNum\" value=\"1\" size=\"5\" style=\"text-align:right\" id=\"queueInput_"+sampleId+"\" onchange=\"changeQty(" + sampleId + ", this)\" />"
				+"<p hidden id=\"hidden_queueInput_"+sampleId+"\"></p>";
	}
	
	public String getBioRepName() {
		BioRepository biorepo = sample.getBioRepository();
		if (biorepo != null) {
			return biorepo.getName();
		}
		else {
			return "<none>";
		}
	}
	
	public String getSelectCheckbox() {
		String sampleId = String.valueOf(sample.getId());
		return "<input type=\"checkbox\" id=\"" + sampleId + "\" />";
	}
	
	public String getInventoryValue(){
		String sampleId = String.valueOf(sample.getId());
		String inventory ="";
		
		if(sample.getInventory()!=null){
			inventory = String.valueOf(sample.getInventory());
		}
		
		return "<span id=\"inventory_"+sampleId+"\">"+inventory+"</span>";
		
	}
}
