package gov.nih.tbi.taglib.datatableDecorators;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.struts2.ServletActionContext;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.ordermanager.model.BioRepository;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;

public class OrderSampleListIdtDecorator extends IdtDecorator {
	BiospecimenItem sample;
	List<BiospecimenItem> biosampleOrderList;
	boolean isOrderEditable = false;
	String status = "";
	
	public OrderSampleListIdtDecorator(List<BiospecimenItem> biosampleOrderList){
		super();
		this.biosampleOrderList = biosampleOrderList;
	}
	
	public String initRow(Object obj, int viewIndexs) {
		String feedback = super.initRow(obj, viewIndexs);
		
		sample = (BiospecimenItem) this.getObject();
		
		isOrderEditable = (Boolean)ServletActionContext.getRequest().getAttribute("isOrderEditable");
		status = (String)ServletActionContext.getRequest().getAttribute("currentOrder.orderStatus.value");
		
		return feedback;
	}
	
	public String getBioRepositoryName() {
		BioRepository biorepo = sample.getBioRepository();
		if (biorepo != null) {
			return biorepo.getName();
		}
		else {
			return "<none>";
		}
	}
	
	public String getInventoryDate() {
		return BRICSTimeDateUtil.dateToDateString(sample.getInventoryDate());		
	}
	
	public String getAdminQuantityInputField() {
		String sampleId = String.valueOf(sample.getId());
		String numberOfAliquots = String.valueOf(sample.getNumberOfAliquots());
		if(status.equals("Created")) {
			return "<input type=\"text\" class=\"itemNum\" value=\""+numberOfAliquots+"\" size=\"5\" style=\"text-align:right\"  onkeyup=\"updateSampleQty(this,'hidden_"+sampleId+"')\" /><p hidden id=\"hidden_"+sampleId+"\">"+numberOfAliquots+"</p>";
		}else {
			return "<input type=\"text\" class=\"itemNum\" value=\""+numberOfAliquots+"\" size=\"5\" style=\"text-align:right\" onchange=\"launchReasonForChangeDialog(false)\" onkeyup=\"updateSampleQty(this,'hidden_"+sampleId+"')\" /><p hidden id=\"hidden_"+sampleId+"\">"+numberOfAliquots+"</p>";
		}
		
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
