package gov.nih.tbi.repository.model;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.repository.model.alzped.StudyModelName;
import gov.nih.tbi.repository.model.alzped.StudyModelType;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.StudyTherapyType;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudyKeyword;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;

/**
 * Stores a study across a session
 * 
 * @author Michael Valeiras
 */
public class SessionStudy implements Serializable {

	private static final long serialVersionUID = -7207360995176465065L;

	/**********************************************************/
	private Study study;
	private List<EntityMap> entityMapList;
	private List<EntityMap> removedMapList;

	private List<AccessRecord> accessRecordList;
	private List<String> entityMapAuthNameList;

	// A list of keywords created this session.
	private Set<StudyKeyword> newKeywords;

	// A list of keywords that user added to the right panel
	private Set<StudyKeyword> keywordList = new HashSet<StudyKeyword>();
	
	private Integer newResearchManagement; 
	
	private Map<Long,ResearchManagement> researchMgmtMap = new HashMap<Long,ResearchManagement>();

	private String associatedPfProtocols;
	
	private Set<StudyTherapeuticAgent> therapeuticAgentSet;
	private Set<StudyTherapyType> therapyTypeSet;
	private Set<StudyTherapeuticTarget> therapeuticTargetSet;
	private Set<StudyModelType> modelTypeSet;
	private Set<StudyModelName> modelNameSet;

	/*********************************************************/

	public SessionStudy() {}

	public void clear() {
			
		newResearchManagement =-1;
		study = null;
		entityMapList = null;
		removedMapList = null;
		accessRecordList = null;
		entityMapAuthNameList = null;
		newKeywords = null;
		researchMgmtMap = new HashMap<Long,ResearchManagement>();
		keywordList.clear();
		associatedPfProtocols = "";
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public List<EntityMap> getEntityMapList() {
		return entityMapList;
	}

	public void setEntityMapList(List<EntityMap> entityMapList) {
		this.entityMapList = entityMapList;
	}

	public List<EntityMap> getRemovedMapList() {

		if (removedMapList == null) {
			removedMapList = new ArrayList<EntityMap>();
		}
		return removedMapList;
	}

	public List<AccessRecord> getAccessRecordList() {
		return accessRecordList;
	}

	public void setAccessRecordList(List<AccessRecord> accessRecordList) {
		this.accessRecordList = accessRecordList;
	}

	public List<String> getEntityMapAuthNameList() {
		if (entityMapAuthNameList == null) {
			entityMapAuthNameList = new ArrayList<String>();
		}

		return entityMapAuthNameList;
	}

	public void setEntityMapAuthNameList(List<String> entityMapAuthNameList) {
		this.entityMapAuthNameList = entityMapAuthNameList;
	}

	public void setRemovedMapList(List<EntityMap> removedMapList) {
		this.removedMapList = removedMapList;
	}

	public Set<StudyKeyword> getNewKeywords() {

		if (newKeywords == null) {
			newKeywords = new LinkedHashSet<StudyKeyword>();
		}
		return newKeywords;
	}

	public void setNewKeywords(Set<StudyKeyword> keywords) {
		this.newKeywords = keywords;
	}

	public Set<StudyKeyword> getKeywordList() {
		return keywordList;
	}
	
	public Integer getNewResearchManagement() {
		return newResearchManagement;
	}

	public void setNewResearchManagement(Integer newResearchManagement) {
		this.newResearchManagement = newResearchManagement;
	}
	
	public Map<Long, ResearchManagement> getResearchMgmtMap() {
		
		if(researchMgmtMap ==null){
			researchMgmtMap = new HashMap<Long,ResearchManagement>();
		}
		return researchMgmtMap;
	}

	public void setResearchMgmtMap(Map<Long, ResearchManagement> researchMgmtMap) {
		this.researchMgmtMap = researchMgmtMap;
	}
	
	public void addResearchMgmt(Long researchMgmtId,ResearchManagement researchMgmt) {
		getResearchMgmtMap().put(researchMgmtId, researchMgmt);
	}

	// Picks up the comma delimited list of attached keywords form the select box
	// on the keywordInterface page
	public void setKeywordList(String keywordString) {
		keywordList = new HashSet<StudyKeyword>();
				
		// If keywords is equal to empty, then do nothing
		if (!keywordString.equals("empty")) {
			// parse keyword string
			String[] keywordArray = keywordString.split(CoreConstants.COMMA);

			for (int i = 0; i < keywordArray.length; i++) {
				if (!keywordArray[i].trim().equals("")) {

					String[] keywordParts = keywordArray[i].trim().split(CoreConstants.UNDERSCORE, 2);

					StudyKeyword Keyword = new StudyKeyword();
					Keyword.setKeyword(keywordParts[1]);
					Keyword.setCount(Long.parseLong(keywordParts[0]));
					keywordList.add(Keyword);
				}
			}
		}
	}


	public void saveKeywords() {
		// get a list of current keywords to compare to session collections
		Set<StudyKeyword> savedKeywords = study.getKeywordSet();

		// if the study doesn't have any keywords they are all new
		// if the study has a new list compare the two to add new ones and remove non associated ones
		if (savedKeywords != null) {
			Set<StudyKeyword> returnSet = new HashSet<StudyKeyword>();

			// if the session keyword exists in the saved keyword list, use the saved keyword
			// if the session keyword does not exist in the saved keyword list, it is new and we should add it
			// this will also satisfy the case that a keyword is removed, it will not be included in the saved list
			for (StudyKeyword sessionKeyword : keywordList) {
				boolean isNew = true;
				for (StudyKeyword savedKeyword : savedKeywords) {
					if (savedKeyword.getKeyword().equalsIgnoreCase(sessionKeyword.getKeyword())) {
						isNew = false;
						returnSet.add(savedKeyword);
						break;
					}
				}
				if (isNew) {
					returnSet.add(sessionKeyword);
				}
			}

			study.setKeywordSet(returnSet);
		} else {
			study.setKeywordSet(keywordList);
		}
	}

	public String getAssociatedPFProtocols() {
		return this.associatedPfProtocols;
	}

	public void setAssociatedPFProtocols(String associatedPfProtocols) {
		this.associatedPfProtocols = associatedPfProtocols;
	}

	public Set<StudyTherapeuticAgent> getTherapeuticAgentSet() {
		return therapeuticAgentSet;
	}

	public void setTherapeuticAgentSet(Set<StudyTherapeuticAgent> therapeuticAgentSet) {
		this.therapeuticAgentSet = therapeuticAgentSet;
	}

	public Set<StudyTherapyType> getTherapyTypeSet() {
		return therapyTypeSet;
	}

	public void setTherapyTypeSet(Set<StudyTherapyType> therapyTypeSet) {
		this.therapyTypeSet = therapyTypeSet;
	}

	public Set<StudyTherapeuticTarget> getTherapeuticTargetSet() {
		return therapeuticTargetSet;
	}

	public void setTherapeuticTargetSet(Set<StudyTherapeuticTarget> therapeuticTargetSet) {
		this.therapeuticTargetSet = therapeuticTargetSet;
	}

	public Set<StudyModelType> getModelTypeSet() {
		return modelTypeSet;
	}

	public void setModelTypeSet(Set<StudyModelType> modelTypeSet) {
		this.modelTypeSet = modelTypeSet;
	}

	public Set<StudyModelName> getModelNameSet() {
		return modelNameSet;
	}

	public void setModelNameSet(Set<StudyModelName> modelNameSet) {
		this.modelNameSet = modelNameSet;
	}
	
	

}
