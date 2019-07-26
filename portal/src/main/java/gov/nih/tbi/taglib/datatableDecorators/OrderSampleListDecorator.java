package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.ordermanager.model.BioRepository;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;

public class OrderSampleListDecorator extends Decorator {
	BiospecimenItem sample;
	boolean isOrderEditable = false;
	String status = "";
	
	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);
		
		sample = (BiospecimenItem) this.getObject();
		
		isOrderEditable = (Boolean)this.getPageContext().findAttribute("isOrderEditable");
		status = (String)this.getPageContext().findAttribute("currentOrder.orderStatus.value");
		
		return feedback;
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
	
	public String getQuantityInputField() {
		String sampleId = String.valueOf(sample.getId());
		String numberOfAliquots = String.valueOf(sample.getNumberOfAliquots());
		if (isOrderEditable) {
			if(status.equals("Created")) {
				return "<input type=\"text\" class=\"itemNum\" value=\""+numberOfAliquots+"\" size=\"5\" style=\"text-align:right\"  onkeyup=\"updateSampleQty(this,'hidden_"+sampleId+"')\" />";
			}else {
				return "<input type=\"text\" class=\"itemNum\" value=\""+numberOfAliquots+"\" size=\"5\" style=\"text-align:right\" onchange=\"launchReasonForChangeDialog(false)\" onkeyup=\"updateSampleQty(this,'hidden_"+sampleId+"')\" />";
			}
			
		}
		else {
			return numberOfAliquots;
		}
	}
	
	public String getAdminQuantityInputField() {
		String sampleId = String.valueOf(sample.getId());
		String numberOfAliquots = String.valueOf(sample.getNumberOfAliquots());
		if(status.equals("Created")) {
			return "<input type=\"text\" class=\"itemNum\" value=\""+numberOfAliquots+"\" size=\"5\" style=\"text-align:right\"  onkeyup=\"updateSampleQty(this,'hidden_"+sampleId+"')\" />";
		}else {
			return "<input type=\"text\" class=\"itemNum\" value=\""+numberOfAliquots+"\" size=\"5\" style=\"text-align:right\" onchange=\"launchReasonForChangeDialog(false)\" onkeyup=\"updateSampleQty(this,'hidden_"+sampleId+"')\" />";
		}
		
	}
	
	public String getSelectCheckbox() {
		String sampleId = String.valueOf(sample.getId());
		return "<input type=\"checkbox\" id=\"" + sampleId + "\" />";
	}
	
	public String getDiagnosis() {
		String diagnosis = "";
		if(sample.getNeuroDiagnosis()!=null){
			diagnosis = sample.getNeuroDiagnosis();
		}
		
		return diagnosis;
	}
	
	public String getCaseControl() {
		String caseControl ="";
		
		if(sample.getCaseControl()!=null){
			caseControl =sample.getCaseControl();
		}
		
		return caseControl;
	}
	public String getAgeYrs() {
		String ageYrs = "";
		if(sample.getAgeYrs()!=null){
			ageYrs = sample.getAgeYrs();
		}
		
		return ageYrs;
	}
}
