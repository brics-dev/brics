 package gov.nih.tbi.repository.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.ResearchManagementRole;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.repository.dao.StudyDao;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelName;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelType;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapyType;
import gov.nih.tbi.repository.model.alzped.ModelName;
import gov.nih.tbi.repository.model.alzped.ModelType;
import gov.nih.tbi.repository.model.alzped.StudyModelName;
import gov.nih.tbi.repository.model.alzped.StudyModelType;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.StudyTherapyType;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.TherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.TherapyType;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;

@Transactional("metaTransactionManager")
@Repository
public class StudyDaoImpl extends GenericDaoImpl<Study, Long> implements StudyDao {

	@Autowired
	public StudyDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {
		super(Study.class, sessionFactory);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Study getPublicStudySearchById(Long studyId) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Date> submitDateQuery = cb.createQuery(Date.class);
		Root<Dataset> dsRoot = submitDateQuery.from(Dataset.class);
		
		submitDateQuery.select(cb.greatest(dsRoot.get("submitDate").as(Date.class)));
		submitDateQuery.where(cb.equal(dsRoot.join("study").get("id"), studyId));
		Date maxSubmitDate = getUniqueResult(submitDateQuery);
		
		CriteriaQuery<Study> query = cb.createQuery(Study.class);
		
		Root<Study> root = query.from(Study.class);
		Predicate predicate = cb.equal(root.get("id"), studyId);
		
		Join<Study, ResearchManagement> rmFetch = (Join) root.fetch("researchMgmtSet", JoinType.LEFT);
		predicate =
				cb.and(predicate, cb.equal(rmFetch.get("role"), ResearchManagementRole.PRIMARY_PRINCIPAL_INVESTIGATOR));
		
		root.fetch("studyForms", JoinType.LEFT);
		root.fetch("dataSubmissionDocument", JoinType.LEFT);
		Join<Study, Dataset> dsFetch = (Join) root.fetch("datasetSet", JoinType.LEFT);
		
		if (maxSubmitDate != null) {
			predicate = cb.and(predicate, cb.equal(dsFetch.get("submitDate"), maxSubmitDate));
		} else {
			predicate = cb.and(predicate, cb.isNull(dsFetch.get("submitDate")));
		}
		
		query.where(predicate);
		return getUniqueResult(query);
	}
	
	/**
	 * @inheritDoc
	 */
	public Study getStudyWithDatasetsByName(String studyTitle) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Study> query = cb.createQuery(Study.class);
		
		Root<Study> root = query.from(Study.class);
		root.fetch("datasetSet", JoinType.LEFT);
		query.where(cb.equal(root.get("title"), studyTitle));
		
		return getUniqueResult(query);
	}
	
	/**
	 * @inheritDoc
	 */
	public Study getStudyByName(String name) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Study> query = cb.createQuery(Study.class);
		
		Root<Study> root = query.from(Study.class);
		query.where(cb.equal(root.get("title"), name));
		
		return getUniqueResult(query);
	}

	public BasicStudy getBasicStudyByName(String name) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BasicStudy> query = cb.createQuery(BasicStudy.class);
		
		Root<BasicStudy> root = query.from(BasicStudy.class);
		query.where(cb.equal(root.get("title"), name));
		
		return getUniqueResult(query);
	}

	/**
	 * @inheritDoc
	 */
	public Long getStatusCount(StudyStatus status) {

		if (status == null) {
			return null;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		
		Root<Study> root = query.from(Study.class);
		query.select(cb.countDistinct(root.get("id")));
		query.where(cb.equal(root.get("studyStatus"), status));
		
		return getUniqueResult(query);
	}

	/**
	 * @inheritDoc
	 */
	public List<Study> getByIds(Set<Long> ids) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Study> query = cb.createQuery(Study.class);
		Root<Study> root = query.from(Study.class);
		
		if (ids != null && !ids.isEmpty()) {
			query.where(root.get("id").in(ids));
		}

		query.orderBy(cb.asc(root.get("id")));
		List<Study> studies = createQuery(query).getResultList();
		
		// Performance fix, removed the fetching of researchManagementSet and dataSet, move
		// the submissionTypes calculation logic into a separate query
		Map<Long, Set<SubmissionType>> studySubTypeMap = getStudySumissionTypes(ids);
		
		for (Study study : studies) {
			long studyId = study.getId();
			Set<SubmissionType> subTypes = studySubTypeMap.get(studyId);
			study.setSubTypes(subTypes);
		}
		return studies;
	}

	// Returns a map of study id to a set of its submission types.
	public Map<Long, Set<SubmissionType>> getStudySumissionTypes(Set<Long> ids) {
		
		String statusStr = DatasetStatus.PRIVATE.getId() + ", " + DatasetStatus.SHARED.getId();
		
		// Use exists in the query to avoid looping through the entire dataset records of the study
		StringBuilder sb = new StringBuilder();
		sb.append("select s.id, ");
		sb.append(" exists (select 1 from dataset d, dataset_submissiontype ds ");
		sb.append(" 	where d.study_id = s.id and d.id = ds.dataset_id ");
		sb.append(" 	and d.dataset_status_id in (" + statusStr + ") ");
		sb.append(" 	and ds.submissiontype_id = " + SubmissionType.CLINICAL.getId() + ") as is_clinical, ");
		sb.append(" exists (select 1 from dataset d, dataset_submissiontype ds ");
		sb.append("  	where d.study_id = s.id and d.id = ds.dataset_id ");
		sb.append(" 	and d.dataset_status_id in (" + statusStr + ") ");
		sb.append("  	and ds.submissiontype_id = " + SubmissionType.GENOMICS.getId() + ") as is_genomics, ");
		sb.append(" exists (select 1 from dataset d, dataset_submissiontype ds ");
		sb.append("  	where d.study_id = s.id and d.id = ds.dataset_id ");
		sb.append(" 	and d.dataset_status_id in (" + statusStr + ") ");
		sb.append("	    and ds.submissiontype_id = " + SubmissionType.IMAGING.getId() + ") as is_imaging ");
		sb.append(" from study s ");
		
		if (ids != null && !ids.isEmpty()) {
			sb.append(" where s.id in (");
			
			for (long id : ids) {
				sb.append(id + ",");
			}
			
			sb.deleteCharAt(sb.length() - 1);
			
			sb.append(")");
		}
		
		String sql = sb.toString();
		log.debug("Get studysubmission type query " + sql);
		
		Query query = getSession().createNativeQuery(sql);
		@SuppressWarnings("unchecked")
		List<Object[]> results = query.getResultList();

		Map<Long, Set<SubmissionType>> studySubTypeMap = new HashMap<Long, Set<SubmissionType>>();
		
		for (Object[] obj : results) {
			Set<SubmissionType> typeSet = new HashSet<SubmissionType>();
			
			long studyId = ((Integer) obj[0]).longValue();
			if  ((Boolean) obj[1]) {
				typeSet.add(SubmissionType.CLINICAL);
			}
			if  ((Boolean) obj[2]) {
				typeSet.add(SubmissionType.GENOMICS);
			}
			if  ((Boolean) obj[3]) {
				typeSet.add(SubmissionType.IMAGING);
			}
			studySubTypeMap.put(studyId, typeSet);
		}
		
		return studySubTypeMap;
	}
	
	@Override
	public Study getStudyWithChildren(Long id) {
		if (id != null) {
			CriteriaBuilder cb = getCriteriaBuilder();
			CriteriaQuery<Study> query = cb.createQuery(Study.class);
			
			Root<Study> root = query.from(Study.class);
			query.where(cb.equal(root.get("id"), id));
			root.fetch("sponsorInfoSet", JoinType.LEFT);
			root.fetch("researchMgmtSet", JoinType.LEFT);
			root.fetch("studySiteSet", JoinType.LEFT);
			root.fetch("clinicalTrialSet", JoinType.LEFT);
			root.fetch("grantSet", JoinType.LEFT);
			root.fetch("keywordSet", JoinType.LEFT);
			root.fetch("studyForms", JoinType.LEFT);
			root.fetch("supportingDocumentationSet", JoinType.LEFT);
			root.fetch("dataSubmissionDocument", JoinType.LEFT);
			root.fetch("datasetSet", JoinType.LEFT);
			root.fetch("graphicFile",JoinType.LEFT);

			Study study = getUniqueResult(query);
			return study;

		} else {
			return new Study();
		}
	}

	@Override
	public Study getStudyWithOutDatasets(Long id) {

		if (id != null) {
			CriteriaBuilder cb = getCriteriaBuilder();
			CriteriaQuery<Study> query = cb.createQuery(Study.class);
			
			Root<Study> root = query.from(Study.class);
			query.where(cb.equal(root.get("id"), id));
			root.fetch("sponsorInfoSet", JoinType.LEFT);
			root.fetch("researchMgmtSet", JoinType.LEFT);
			root.fetch("studySiteSet", JoinType.LEFT);
			root.fetch("clinicalTrialSet", JoinType.LEFT);
			root.fetch("grantSet", JoinType.LEFT);
			root.fetch("keywordSet", JoinType.LEFT);
			root.fetch("studyForms", JoinType.LEFT);
			root.fetch("supportingDocumentationSet", JoinType.LEFT);
			root.fetch("dataSubmissionDocument", JoinType.LEFT);
			
			Study study = getUniqueResult(query);
			return study;

			

		} else {
			return new Study();
		}
    }

	@Override
	public Study getLazy(Long id) {
		return super.get(id);
    } 
    
    	@Override
	public Study getByPrefixedIdExcludingDataset(String studyId) {
    		CriteriaBuilder cb = getCriteriaBuilder();
    		CriteriaQuery<Study> query = cb.createQuery(Study.class);
    		
    		Root<Study> root = query.from(Study.class);
    		query.where(cb.equal(root.get("prefixedId"), studyId));
    		root.fetch("sponsorInfoSet", JoinType.LEFT);
    		root.fetch("researchMgmtSet", JoinType.LEFT);
    		root.fetch("studySiteSet", JoinType.LEFT);
    		root.fetch("clinicalTrialSet", JoinType.LEFT);
    		root.fetch("grantSet", JoinType.LEFT);
    		root.fetch("keywordSet", JoinType.LEFT);
    		root.fetch("studyForms", JoinType.LEFT);
    		root.fetch("supportingDocumentationSet", JoinType.LEFT);
    		root.fetch("dataSubmissionDocument", JoinType.LEFT);
    		root.fetch("graphicFile",JoinType.LEFT);

    		Study study = getUniqueResult(query.distinct(true));
    		return study;
    		
	}    
	/**
	 * @inheritDoc
	 */
	public Study getByPrefixedId(String studyId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Study> query = cb.createQuery(Study.class);
		
		Root<Study> root = query.from(Study.class);
		query.where(cb.equal(root.get("prefixedId"), studyId));
		root.fetch("sponsorInfoSet", JoinType.LEFT);
		root.fetch("researchMgmtSet", JoinType.LEFT);
		root.fetch("studySiteSet", JoinType.LEFT);
		root.fetch("clinicalTrialSet", JoinType.LEFT);
		root.fetch("grantSet", JoinType.LEFT);
		root.fetch("keywordSet", JoinType.LEFT);
		root.fetch("studyForms", JoinType.LEFT);
		root.fetch("supportingDocumentationSet", JoinType.LEFT);
		root.fetch("dataSubmissionDocument", JoinType.LEFT);
		root.fetch("datasetSet", JoinType.LEFT);

		Study study = getUniqueResult(query.distinct(true));
		return study;
	}

	public Long getStudyIdByDatasetFileId(Long datasetFileId) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		
		Root<Study> root = query.from(Study.class);
		Join<Study, Dataset> datasetJoin = root.join("dataset", JoinType.LEFT);
		Join<Dataset, DatasetFile> dsFileJoin = datasetJoin.join("datasetFileSet", JoinType.LEFT);
		query.where(cb.equal(dsFileJoin.get("id"), datasetFileId));
		query.select(root.get("id"));
		
		return getUniqueResult(query);
	}
	
	@Override
	public List<Study> getAllStudiesWithKeyword() {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Study> query = cb.createQuery(Study.class);

		Root<Study> root = query.from(Study.class);
		root.fetch("studySiteSet", JoinType.LEFT);
		root.fetch("researchMgmtSet", JoinType.LEFT);
		root.fetch("keywordSet", JoinType.LEFT);

		return createQuery(query.distinct(true)).getResultList();
	}
	
	public List<Study> getAllForRDFGen() {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Study> query = cb.createQuery(Study.class);
		
		Root<Study> root = query.from(Study.class);
		root.fetch("clinicalTrialSet", JoinType.LEFT);
		root.fetch("grantSet", JoinType.LEFT);
		root.fetch("datasetSet", JoinType.LEFT);

		return createQuery(query.distinct(true)).getResultList();
	}

	@Override
	public Study getStudySites(long studyId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Study> query = cb.createQuery(Study.class);
		
		Root<Study> root = query.from(Study.class);
		root.fetch("studySiteSet", JoinType.LEFT);
		query.where(cb.equal(root.get("id"), studyId)).distinct(true);
		
		return getUniqueResult(query);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public List<Study> getStudiesWithPrivateSharedDatasets() {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Study> query = cb.createQuery(Study.class);
		
		Root<Study> root = query.from(Study.class);
		root.fetch("researchMgmtSet", JoinType.LEFT);
		
		// This is an ugly but working join with fetch
		Join<Study, Dataset> datasetFetch = (Join)root.fetch("datasetSet", JoinType.LEFT);

		query.where(cb.or(cb.equal(datasetFetch.get("datasetStatus"), DatasetStatus.PRIVATE),
				cb.equal(datasetFetch.get("datasetStatus"), DatasetStatus.SHARED)));
		
		return createQuery(query.distinct(true)).getResultList();
	}

	public boolean isStudyPublic(Long studyId) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		
		Root<Study> root = query.from(Study.class);
		query.select(cb.count(root));
		query.where(cb.and(cb.equal(root.get("id"), studyId), cb.equal(root.get("studyStatus"), StudyStatus.PUBLIC)));
		
		return getUniqueResult(query) > 0;
	}
	
	@Override
	public Study getPublicSubmittedDataStudyById(Long studyId, StudyStatus studyStatus) {	
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Study> query = cb.createQuery(Study.class);
		
		Root<Study> root = query.from(Study.class);
		query.where(cb.and(cb.equal(root.get("id"), studyId), cb.equal(root.get("studyStatus"), studyStatus)));
		root.fetch("grantSet", JoinType.LEFT);
		query.distinct(true);
		
		return getUniqueResult(query);
	}
	
	public Study getStudyGraphicFileById(Integer studyId){
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Study> sQuery = cb.createQuery(Study.class);
		Root<Study> sRoot = sQuery.from(Study.class);
		sRoot.fetch("graphicFile", JoinType.LEFT);
		sQuery.select(sRoot);
		sQuery.where(cb.equal(sRoot.get("id"), studyId)).distinct(true);
		
		Study study = getUniqueResult(sQuery);
		return study;
	}
	
	public List<TherapeuticAgent> getAllTherapeuticAgents(){
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<TherapeuticAgent> query = cb.createQuery(TherapeuticAgent.class);

		Root<TherapeuticAgent> root = query.from(TherapeuticAgent.class);
		query.select(root);

		List<TherapeuticAgent> out = createQuery(query).getResultList();
		return out;
		
	}
		
	public List<TherapyType> getAllTherapyTypes(){
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<TherapyType> query = cb.createQuery(TherapyType.class);

		Root<TherapyType> root = query.from(TherapyType.class);
		query.select(root);

		List<TherapyType> out = createQuery(query).getResultList();
		return out;
		
	}
		
	public List<TherapeuticTarget> getAllTherapeuticTargets(){
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<TherapeuticTarget> query = cb.createQuery(TherapeuticTarget.class);

		Root<TherapeuticTarget> root = query.from(TherapeuticTarget.class);
		query.select(root);

		List<TherapeuticTarget> out = createQuery(query).getResultList();
		return out;
		
	}
		
	public List<ModelName> getAllModelNames(){
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ModelName> query = cb.createQuery(ModelName.class);

		Root<ModelName> root = query.from(ModelName.class);
		query.select(root);

		List<ModelName> out = createQuery(query).getResultList();
		return out;
		
	}
	
	public List<ModelType> getAllModelTypes(){
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ModelType> query = cb.createQuery(ModelType.class);

		Root<ModelType> root = query.from(ModelType.class);
		query.select(root);

		List<ModelType> out = createQuery(query).getResultList();
		return out;
		
	}
	
	public List<TherapeuticAgent> getTherapeuticAgentsFromListOfStrings(List<String> agents){
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<TherapeuticAgent> query = cb.createQuery(TherapeuticAgent.class);

		Root<TherapeuticAgent> root = query.from(TherapeuticAgent.class);
		Expression<String> exp = root.get("text");
		Predicate p = exp.in(agents);
		query.where(p);

		List<TherapeuticAgent> out = createQuery(query).getResultList();
		return out;
	}
	
	public List<TherapeuticTarget> getTherapeuticTargetsFromListOfStrings(List<String> agents){
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<TherapeuticTarget> query = cb.createQuery(TherapeuticTarget.class);

		Root<TherapeuticTarget> root = query.from(TherapeuticTarget.class);
		Expression<String> exp = root.get("text");
		Predicate p = exp.in(agents);
		query.where(p);

		List<TherapeuticTarget> out = createQuery(query).getResultList();
		return out;
	}

	public List<TherapyType> getTherapyTypesFromListOfStrings(List<String> agents) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<TherapyType> query = cb.createQuery(TherapyType.class);

		Root<TherapyType> root = query.from(TherapyType.class);
		Expression<String> exp = root.get("text");
		Predicate p = exp.in(agents);
		query.where(p);

		List<TherapyType> out = createQuery(query).getResultList();
		return out;
	}

	public List<ModelName> getModelNamesFromListOfStrings(List<String> agents) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ModelName> query = cb.createQuery(ModelName.class);

		Root<ModelName> root = query.from(ModelName.class);
		Expression<String> exp = root.get("text");
		Predicate p = exp.in(agents);
		query.where(p);

		List<ModelName> out = createQuery(query).getResultList();
		return out;
	}

	public List<ModelType> getModelTypesFromListOfStrings(List<String> agents) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ModelType> query = cb.createQuery(ModelType.class);

		Root<ModelType> root = query.from(ModelType.class);
		Expression<String> exp = root.get("text");
		Predicate p = exp.in(agents);
		query.where(p);

		List<ModelType> out = createQuery(query).getResultList();
		return out;
	}
	
	public List<TherapeuticAgent> getStudyTherapeuticAgents(String studyId){
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyTherapeuticAgent> initialQuery = cb.createQuery(StudyTherapeuticAgent.class);

		Root<StudyTherapeuticAgent> initialRoot = initialQuery.from(StudyTherapeuticAgent.class);
		initialQuery.where(cb.equal(initialRoot.get("studyId"), studyId));

		List<StudyTherapeuticAgent> list = createQuery(initialQuery).getResultList();
		
		List<TherapeuticAgent> out = new ArrayList<>();
		
		for(StudyTherapeuticAgent agent: list){
			out.add(agent.getTherapeuticAgent());
		}
		
		return out;
	
	}
	
	
	public List<StudyTherapeuticAgent> getTherapeuticAgentsForStudy(String studyId){
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyTherapeuticAgent> initialQuery = cb.createQuery(StudyTherapeuticAgent.class);

		Root<StudyTherapeuticAgent> initialRoot = initialQuery.from(StudyTherapeuticAgent.class);
		initialQuery.where(cb.equal(initialRoot.get("studyId"), studyId));

		List<StudyTherapeuticAgent> list = createQuery(initialQuery).getResultList();
		return list;
	
	}
	
	
	public List<TherapeuticTarget> getStudyTherapeuticTargets(String studyId){
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyTherapeuticTarget> initialQuery = cb.createQuery(StudyTherapeuticTarget.class);

		Root<StudyTherapeuticTarget> initialRoot = initialQuery.from(StudyTherapeuticTarget.class);
		initialQuery.where(cb.equal(initialRoot.get("studyId"), studyId));

		List<StudyTherapeuticTarget> list = createQuery(initialQuery).getResultList();
		
		List<TherapeuticTarget> out = new ArrayList<>();
		
		for(StudyTherapeuticTarget target: list){
			out.add(target.getTherapeuticTarget());
		}
		
		return out;
	
	}
	
	public List<StudyTherapeuticTarget> getTherapeuticTargetsForStudy(String studyId){
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyTherapeuticTarget> initialQuery = cb.createQuery(StudyTherapeuticTarget.class);

		Root<StudyTherapeuticTarget> initialRoot = initialQuery.from(StudyTherapeuticTarget.class);
		initialQuery.where(cb.equal(initialRoot.get("studyId"), studyId));

		List<StudyTherapeuticTarget> list = createQuery(initialQuery).getResultList();
		return list;
	}
	
	public List<TherapyType> getStudyTherapyTypes(String studyId){
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyTherapyType> initialQuery = cb.createQuery(StudyTherapyType.class);

		Root<StudyTherapyType> initialRoot = initialQuery.from(StudyTherapyType.class);
		initialQuery.where(cb.equal(initialRoot.get("studyId"), studyId));

		List<StudyTherapyType> list = createQuery(initialQuery).getResultList();
		
		List<TherapyType> out = new ArrayList<>();
		
		for(StudyTherapyType therapyType: list){
			out.add(therapyType.getTherapyType());
		}
		
		return out;
	
	}
	
	public List<StudyTherapyType> getTherapyTypesForStudy(String studyId){
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyTherapyType> initialQuery = cb.createQuery(StudyTherapyType.class);

		Root<StudyTherapyType> initialRoot = initialQuery.from(StudyTherapyType.class);
		initialQuery.where(cb.equal(initialRoot.get("studyId"), studyId));

		List<StudyTherapyType> list = createQuery(initialQuery).getResultList();
		return list;
	
	}
	
	public List<ModelName> getStudyModelNames(String studyId){
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyModelName> initialQuery = cb.createQuery(StudyModelName.class);

		Root<StudyModelName> initialRoot = initialQuery.from(StudyModelName.class);
		initialQuery.where(cb.equal(initialRoot.get("studyId"), studyId));

		List<StudyModelName> list = createQuery(initialQuery).getResultList();
		
		List<ModelName> out = new ArrayList<>();
		
		for(StudyModelName modelName: list){
			out.add(modelName.getModelName());
		}
		
		return out;
	
	}
	
	
	public List<StudyModelName> getModelNamesForStudy(String studyId){
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyModelName> initialQuery = cb.createQuery(StudyModelName.class);

		Root<StudyModelName> initialRoot = initialQuery.from(StudyModelName.class);
		initialQuery.where(cb.equal(initialRoot.get("studyId"), studyId));

		List<StudyModelName> list = createQuery(initialQuery).getResultList();
		return list;
	}
	
	public List<ModelType> getStudyModelTypes(String studyId){
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyModelType> initialQuery = cb.createQuery(StudyModelType.class);

		Root<StudyModelType> initialRoot = initialQuery.from(StudyModelType.class);
		initialQuery.where(cb.equal(initialRoot.get("studyId"), studyId));

		List<StudyModelType> list = createQuery(initialQuery).getResultList();
		
		List<ModelType> out = new ArrayList<>();
		
		for(StudyModelType modelType: list){
			out.add(modelType.getModelType());
		}
		
		return out;
	
	}
	
	
	public List<StudyModelType> getModelTypesForStudy(String studyId){
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyModelType> initialQuery = cb.createQuery(StudyModelType.class);
		Root<StudyModelType> initialRoot = initialQuery.from(StudyModelType.class);
		initialQuery.where(cb.equal(initialRoot.get("studyId"), studyId));
		List<StudyModelType> list = createQuery(initialQuery).getResultList();
		return list;
	
	}
			
}
