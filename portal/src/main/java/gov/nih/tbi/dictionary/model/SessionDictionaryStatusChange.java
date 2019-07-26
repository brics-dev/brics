package gov.nih.tbi.dictionary.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DictionarySupportingDocumentation;

public class SessionDictionaryStatusChange implements Serializable{

	private static final long serialVersionUID = 3943907762412851310L;
	
	private StatusType newStatusType;
	
	private DataElementStatus newDEStatusType;
	
	private DataElementStatus currentDEStatusType;
	
	private String statusReason;
	
	private String attachedDEReason;
	
	private String approveReason;
	
	private Set<DataElement> attachedDEs;
	
	private List<DataElement> bulkDEs = new ArrayList<DataElement>();
	
	private boolean inDataElementPage;
	
	private Set<DictionarySupportingDocumentation> fSEventLogDocumentation = new HashSet<DictionarySupportingDocumentation>();
	
	private Set<DictionarySupportingDocumentation> dEEventLogDocumentation =new HashSet<DictionarySupportingDocumentation>();
	
	
	public void clear(){
		this.newStatusType=null;
		this.newDEStatusType=null;
		this.currentDEStatusType=null;
		this.statusReason=null;
		this.attachedDEReason=null;
		this.attachedDEs=null;
		this.approveReason=null;
		this.inDataElementPage=false;
		this.fSEventLogDocumentation=new HashSet<DictionarySupportingDocumentation>();		
		this.dEEventLogDocumentation=new HashSet<DictionarySupportingDocumentation>();
		this.bulkDEs=new ArrayList<DataElement>();
	}

	public StatusType getNewStatusType() {
		return newStatusType;
	}

	public void setNewStatusType(StatusType newStatusType) {
		this.newStatusType = newStatusType;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	public String getAttachedDEReason() {
		return attachedDEReason;
	}

	public void setAttachedDEReason(String attachedDEReason) {
		this.attachedDEReason = attachedDEReason;
	}

	public String getApproveReason() {
		return approveReason;
	}

	public void setApproveReason(String approveReason) {
		this.approveReason = approveReason;
	}

	public DataElementStatus getNewDEStatusType() {
		return newDEStatusType;
	}

	public void setNewDEStatusType(DataElementStatus newDEStatusType) {
		this.newDEStatusType = newDEStatusType;
	}

	public DataElementStatus getCurrentDEStatusType() {
		return currentDEStatusType;
	}

	public void setCurrentDEStatusType(DataElementStatus currentDEStatusType) {
		this.currentDEStatusType = currentDEStatusType;
	}

	public Set<DataElement> getAttachedDEs() {
		
		if(attachedDEs==null){
			attachedDEs = new HashSet <DataElement>();
		}
		return attachedDEs;
	}

	public void setAttachedDEs(Set<DataElement> attachedDEs) {
		this.attachedDEs = attachedDEs;
	}
	
	public void addAttahcedDEs(DataElement dataElement){
		getAttachedDEs().add(dataElement);
	}

	public boolean getInDataElementPage() {
		return inDataElementPage;
	}

	public void setInDataElementPage(boolean inDataElementPage) {
		this.inDataElementPage = inDataElementPage;
	}
	
	public List<DataElement> getBulkDEs() {
		return bulkDEs;
	}

	public void setBulkDEs(List<DataElement> bulkDEs) {
		this.bulkDEs = bulkDEs;
	}

	public Set<DictionarySupportingDocumentation> getfSEventLogDocumentation() {
		return fSEventLogDocumentation;
	}

	public void setfSEventLogDocumentation(Set<DictionarySupportingDocumentation> fSEventLogDocumentation) {
		this.fSEventLogDocumentation = fSEventLogDocumentation;
	}
	
	public Set<DictionarySupportingDocumentation> getDEEventLogDocumentation() {
		return dEEventLogDocumentation;
	}

	public void setdEEventLogDocumentation(Set<DictionarySupportingDocumentation> dEEventLogDocumentation) {
		this.dEEventLogDocumentation = dEEventLogDocumentation;
	}
	
	
		
}
