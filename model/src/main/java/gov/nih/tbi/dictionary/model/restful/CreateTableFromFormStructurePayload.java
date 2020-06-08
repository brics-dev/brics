package gov.nih.tbi.dictionary.model.restful;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.hibernate.DictionarySupportingDocumentation;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;

@XmlRootElement(name = "CreateTableFromFormStructure")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateTableFromFormStructurePayload {
	
	StructuralFormStructure formStructure;
	String statusChangeComment;
	String approveReason;
	Set<DictionarySupportingDocumentation> docList;
	StatusType statusType;
	Long diseaseId;
	Long userId;
	
	
	public CreateTableFromFormStructurePayload() {
		super();
	}
	public CreateTableFromFormStructurePayload(StructuralFormStructure formStructure,String statusChangeComment,String approveReason,
			Set<DictionarySupportingDocumentation> docList, StatusType statusType, Long diseaseId, Long userId) {
		this.formStructure = formStructure;
		this.statusChangeComment = statusChangeComment;
		this.approveReason = approveReason;
		this.docList = docList;
		this.statusType = statusType;
		this.diseaseId = diseaseId;
		this.userId = userId;
	}
	public StructuralFormStructure getFormStructure() {
		return formStructure;
	}
	public void setFormStructure(StructuralFormStructure formStructure) {
		this.formStructure = formStructure;
	}
	public String getApproveReason() {
		return approveReason;
	}
	public void setApproveReason(String approveReason) {
		this.approveReason = approveReason;
	}
	public String getStatusChangeComment() {
		return statusChangeComment;
	}
	public void setStatusChangeComment(String statusChangeComment) {
		this.statusChangeComment = statusChangeComment;
	}
	public Set<DictionarySupportingDocumentation> getDocList() {
		return docList;
	}
	public void setDocList(Set<DictionarySupportingDocumentation> docList) {
		this.docList = docList;
	}
	public StatusType getStatusType() {
		return statusType;
	}
	public void setStatusType(StatusType statusType) {
		this.statusType = statusType;
	}
	public Long getDiseaseId() {
		return diseaseId;
	}
	public void setDiseaseId(Long diseaseId) {
		this.diseaseId = diseaseId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
