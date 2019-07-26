package gov.nih.tbi.dictionary.model.formbuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;

@Component
@Scope(value="session")
public class SessionEform implements Serializable{
	
	private static final long serialVersionUID = 5090898542732207354L;

	private Eform eform;
	private List<Long> quesitonList;
	private BasicEform basicEform;
	private String formMode;
	private Boolean attachFiles = false;
	private Boolean copyMode = false;

	// Used to store permissions changes until they are committed (edit permissions page only)
	List<EntityMap> entityMapList;
	List<EntityMap> removedMapList;
	private List<String> entityMapAuthNameList;
	private PermissionType sessionEformUserPermissionType;
	
	public void clear(){
		this.eform = null;
		this.quesitonList = null;
		this.basicEform = null;
		this.formMode = null;
		this.attachFiles = null;
		this.entityMapList = null;
		this.removedMapList = null;
		this.entityMapAuthNameList = null;
		this.sessionEformUserPermissionType = null;
		this.copyMode=false;
	}

	public Boolean getCopyMode() {
		return copyMode;
	}

	public void setCopyMode(Boolean copyMode) {
		this.copyMode = copyMode;
	}

	public boolean isAttachFiles() {
		return attachFiles;
	}

	public void setAttachFiles(boolean attachFiles) {
		this.attachFiles = attachFiles;
	}
	
	public Eform getEform(){
		return this.eform;
	}
	
	public void setEform(Eform eform){
		this.eform = eform;
	}
	public List<Long> getQuesitonList() {
		return quesitonList;
	}
	public void setQuesitonList(List<Long> quesitonList) {
		this.quesitonList = quesitonList;
	}
	public List<EntityMap> getEntityMapList() {
		return entityMapList;
	}
	public void setEntityMapList(List<EntityMap> entityMapList) {
		this.entityMapList = entityMapList;
	}
	public List<EntityMap> getRemovedMapList() {
		return removedMapList;
	}

	public void setRemovedMapList(List<EntityMap> removedMapList) {
		this.removedMapList = removedMapList;
	}
	public List<String> getEntityMapAuthNameList() {
		if (entityMapAuthNameList == null){
        	entityMapAuthNameList = new ArrayList<String>();
        }
        return entityMapAuthNameList;
	}
	public void setEntityMapAuthNameList(List<String> entityMapAuthNameList) {
		this.entityMapAuthNameList = entityMapAuthNameList;
	}

	public BasicEform getBasicEform() {
		return basicEform;
	}

	public void setBasicEform(BasicEform basicEform) {
		this.basicEform = basicEform;
	}
	
	public String getFormMode() {
		return formMode;
	}

	public void setFormMode(String formMode) {
		this.formMode = formMode;
	}

	public Boolean getAttachFiles() {
		return attachFiles;
	}

	public void setAttachFiles(Boolean attachFiles) {
		this.attachFiles = attachFiles;
	}
	
	public PermissionType getSessionEformUserPermissionType(){
		return this.sessionEformUserPermissionType;
	}
	
	public void setSessionEformUserPermissionType(PermissionType sessionEformUserPermissionType){
		this.sessionEformUserPermissionType = sessionEformUserPermissionType;
	}

}
