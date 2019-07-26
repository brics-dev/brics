package gov.nih.tbi.metastudy.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyAccessRecord;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyDocumentation;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyKeyword;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyLabel;
import gov.nih.tbi.metastudy.model.hibernate.ResearchManagementMeta;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelName;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelType;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapyType;
import gov.nih.tbi.repository.model.alzped.StudyModelName;
import gov.nih.tbi.repository.model.alzped.StudyModelType;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.StudyTherapyType;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;

public class SessionMetaStudy implements Serializable {

	private static final long serialVersionUID = -7216476868979221175L;

	MetaStudy metaStudy;
	String selectedDataName;
	
	// A list of keywords existing in the DB
    private List<MetaStudyKeyword> currentKeywords;
    private List<MetaStudyLabel> currentLabels;
    
    // A list of keywords created this session.
    private Set<MetaStudyKeyword> newKeywords;
    private Set<MetaStudyLabel> newLabels;

	// Used to store permissions changes until they are committed (edit permissions page only)
	List<EntityMap> entityMapList;
	List<EntityMap> removedMapList;
	private List<String> entityMapAuthNameList;
	
	private Set<MetaStudyData> newMetaStudyData;
	private Set<MetaStudyDocumentation> newSupportingDocData;

	private List<MetaStudyAccessRecord> accessRecordList;
	
	private Integer newResearchManagement; 
	
	private Map<Long,ResearchManagementMeta> researchMgmtMap = new HashMap<Long,ResearchManagementMeta>();
	
	private Set<MetaStudyTherapeuticAgent> therapeuticAgentSet;
	private Set<MetaStudyTherapyType> therapyTypeSet;
	private Set<MetaStudyTherapeuticTarget> therapeuticTargetSet;
	private Set<MetaStudyModelType> modelTypeSet;
	private Set<MetaStudyModelName> modelNameSet;
	
	private PermissionType sessionMetaStudyUserPermissionType;
	private EntityMap sessionMetaStudyUserEntityMap;

    public SessionMetaStudy() {

        super();
    }
		
	public void clear() {
		
		this.newResearchManagement = -2;
		this.setMetaStudy(null);
		this.setSelectedDataName(null);
		this.setCurrentKeywords(null);
		this.setCurrentLabels(null);
		this.setNewKeywords(null);
		this.setNewLabels(null);
		this.setEntityMapList(null);
		this.setRemovedMapList(null);
		this.setEntityMapAuthNameList(null);
		this.setNewMetaStudyData(null);
		this.setNewSupportingDocData(null);
		this.setSessionMetaStudyUserPermissionType(null);
		this.setSessionMetaStudyUserEntityMap(null);
		this.researchMgmtMap = new HashMap<Long,ResearchManagementMeta>();
	}
	
	
	public EntityMap getSessionMetaStudyUserEntityMap() {
		return sessionMetaStudyUserEntityMap;
	}

	public void setSessionMetaStudyUserEntityMap(EntityMap sessionMetaStudyUserEntityMap) {
		this.sessionMetaStudyUserEntityMap = sessionMetaStudyUserEntityMap;
	}

	public PermissionType getSessionMetaStudyUserPermissionType() {
		return sessionMetaStudyUserPermissionType;
	}

	public void setSessionMetaStudyUserPermissionType(PermissionType sessionMetaStudyUserPermissionType) {
		this.sessionMetaStudyUserPermissionType = sessionMetaStudyUserPermissionType;
	}

	public String getSelectedDataName() {
		return selectedDataName;
	}

	public void setSelectedDataName(String selectedDataName) {
		this.selectedDataName = selectedDataName;
	}

	public MetaStudy getMetaStudy() {
		return metaStudy;
	}

	public void setMetaStudy(MetaStudy metaStudy) {
		this.metaStudy = metaStudy;
	}
	
	public List<MetaStudyAccessRecord> getAccessRecordList() {
		return accessRecordList;
	}

	public void setAccessRecordList(List<MetaStudyAccessRecord> recordList) {
		this.accessRecordList = recordList;
	}
	
    public Set<MetaStudyKeyword> getNewKeywords()
    {

        if (newKeywords == null)
        {
            newKeywords = new LinkedHashSet<MetaStudyKeyword>();
        }
        return newKeywords;
    }
    
    public void setNewKeywords(Set<MetaStudyKeyword> keywords)
    {
    	this.newKeywords = keywords;
    }
    public void addNewKeyword(MetaStudyKeyword keyword)
    {

        if (newKeywords == null)
        {
        	newKeywords = new LinkedHashSet<MetaStudyKeyword>();
        }
        newKeywords.add(keyword);
    }

    public Set<MetaStudyLabel> getNewLabels()
    {

        if (newLabels == null)
        {
            newLabels = new LinkedHashSet<MetaStudyLabel>();
        }
        return newLabels;
    }
    
    public void setNewLabels(Set<MetaStudyLabel> labels)
    {
    	this.newLabels = labels;
    }

    public void addNewLabel(MetaStudyLabel label)
    {

        if (newLabels == null)
        {
            newLabels = new LinkedHashSet<MetaStudyLabel>();
        }
        newLabels.add(label);
    }

    public List<MetaStudyKeyword> getCurrentKeywords()
    {

        if (currentKeywords == null)
        {
            currentKeywords = new ArrayList<MetaStudyKeyword>();
        }

        return currentKeywords;
    }
    
    public void setCurrentKeywords(List<MetaStudyKeyword> currentKeywords)
    {

        this.currentKeywords = currentKeywords;
    }

    public List<MetaStudyLabel> getCurrentLabels()
    {

        if (currentLabels == null)
        {
            currentLabels = new ArrayList<MetaStudyLabel>();
        }
        return currentLabels;
    }

    public void setCurrentLabels(List<MetaStudyLabel> currentLabels)
    {

        this.currentLabels = currentLabels;
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
		return entityMapAuthNameList;
	}

	public void setEntityMapAuthNameList(List<String> entityMapAuthNameList) {
		this.entityMapAuthNameList = entityMapAuthNameList;
	}
	
    public Set<MetaStudyData> getNewMetaStudyData()
    {

        if (newMetaStudyData == null)
        {
        	newMetaStudyData = new LinkedHashSet<MetaStudyData>();
        }
        return newMetaStudyData;
    }
    
    public void setNewMetaStudyData(Set<MetaStudyData> metaStudyData)
    {
    	this.newMetaStudyData = metaStudyData;
    }

    public void addNewMetaStudyData(MetaStudyData metaStudyData)
    {

        if (newMetaStudyData == null)
        {
        	newMetaStudyData = new LinkedHashSet<MetaStudyData>();
        }
        newMetaStudyData.add(metaStudyData);
    }
    
    public Set<MetaStudyDocumentation> getNewSupportingDocData()
    {

        if (newSupportingDocData == null)
        {
        	newSupportingDocData = new LinkedHashSet<MetaStudyDocumentation>();
        }
        return newSupportingDocData;
    }
    
    public void setNewSupportingDocData(Set<MetaStudyDocumentation> newSupportingDocData)
    {
    	this.newSupportingDocData = newSupportingDocData;
    }

    public void addNewSupportingDocData(MetaStudyDocumentation newSupportingDocData)
    {

        if (this.newSupportingDocData == null)
        {
        	this.newSupportingDocData = new LinkedHashSet<MetaStudyDocumentation>();
        }
        this.newSupportingDocData.add(newSupportingDocData);
    }
    
    public Integer getNewResearchManagement() {
		return newResearchManagement;
	}

	public void setNewResearchManagement(Integer newResearchManagement) {
		this.newResearchManagement = newResearchManagement;
	}
	
	public Map<Long, ResearchManagementMeta> getResearchMgmtMap() {
		
		if(researchMgmtMap ==null){
			researchMgmtMap = new HashMap<Long,ResearchManagementMeta>();
		}
		return researchMgmtMap;
	}

	public void setResearchMgmtMap(Map<Long, ResearchManagementMeta> researchMgmtMap) {
		this.researchMgmtMap = researchMgmtMap;
	}
	
	public void addResearchMgmt(Long researchMgmtId,ResearchManagementMeta researchMgmt) {
		getResearchMgmtMap().put(researchMgmtId, researchMgmt);
	}

	public Set<MetaStudyTherapeuticAgent> getTherapeuticAgentSet() {
		return therapeuticAgentSet;
	}

	public void setTherapeuticAgentSet(Set<MetaStudyTherapeuticAgent> therapeuticAgentSet) {
		this.therapeuticAgentSet = therapeuticAgentSet;
	}

	public Set<MetaStudyTherapyType> getTherapyTypeSet() {
		return therapyTypeSet;
	}

	public void setTherapyTypeSet(Set<MetaStudyTherapyType> therapyTypeSet) {
		this.therapyTypeSet = therapyTypeSet;
	}

	public Set<MetaStudyTherapeuticTarget> getTherapeuticTargetSet() {
		return therapeuticTargetSet;
	}

	public void setTherapeuticTargetSet(Set<MetaStudyTherapeuticTarget> therapeuticTargetSet) {
		this.therapeuticTargetSet = therapeuticTargetSet;
	}

	public Set<MetaStudyModelType> getModelTypeSet() {
		return modelTypeSet;
	}

	public void setModelTypeSet(Set<MetaStudyModelType> modelTypeSet) {
		this.modelTypeSet = modelTypeSet;
	}

	public Set<MetaStudyModelName> getModelNameSet() {
		return modelNameSet;
	}

	public void setModelNameSet(Set<MetaStudyModelName> modelNameSet) {
		this.modelNameSet = modelNameSet;
	}

}
