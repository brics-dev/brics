package gov.nih.tbi.metastudy.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.MetaStudyStatus;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelName;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelType;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapyType;
import gov.nih.tbi.repository.model.alzped.ModelName;
import gov.nih.tbi.repository.model.alzped.ModelType;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.TherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.TherapyType;

import java.util.List;
import java.util.Set;

public interface MetaStudyDao extends GenericDao<MetaStudy, Long> {

	public List<MetaStudy> getMetaStudyListByIds(Set<Long> ids);
	
	public List<MetaStudy> getMetaStudyListFilterByStatus(Set<Long> ids, Set<MetaStudyStatus> status);
	
	public boolean isTitleUnique(String title);
	
	public List<MetaStudy> metaStudyPublicSiteSearch();
	
	public MetaStudy getPublicMetaStudyById(Long metaStudyId);
	
	public MetaStudy getBasicMetaStudy(Long metaStudyId);
	
    public List<TherapeuticAgent> getTherapeuticAgentsFromListOfStrings(List<String> agents);
	
	public List<TherapeuticTarget> getTherapeuticTargetsFromListOfStrings(List<String> agents);
	
	public List<TherapyType> getTherapyTypesFromListOfStrings(List<String> agents);
	
	public List<ModelName> getModelNamesFromListOfStrings(List<String> agents);
	
	public List<ModelType> getModelTypesFromListOfStrings(List<String> agents);
	
    public List<TherapeuticAgent> getMetaStudyTherapeuticAgents(String metaStudyId);
	
	public List<TherapeuticTarget> getMetaStudyTherapeuticTargets(String metaStudyId);
	
	public List<TherapyType> getMetaStudyTherapyTypes(String metaStudyId);
	
	public List<ModelName> getMetaStudyModelNames(String metaStudyId);
	
	public List<ModelType> getMetaStudyModelTypes(String metaStudyId);
	
	public List<MetaStudyModelType> getModelTypesForMetaStudy(String metaStudyId);
	
	public List<MetaStudyModelName> getModelNamesForMetaStudy(String metaStudyId);
	
	public List<MetaStudyTherapyType> getTherapyTypesForMetaStudy(String metaStudyId);
	
	public List<MetaStudyTherapeuticTarget> getTherapeuticTargetsForMetaStudy(String metaStudyId);
	
	public List<MetaStudyTherapeuticAgent> getTherapeuticAgentsForMetaStudy(String metaStudyId);
}
