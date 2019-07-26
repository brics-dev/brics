package gov.nih.tbi.metastudy.dao.hibernate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import gov.nih.tbi.commons.model.MetaStudyStatus;
import gov.nih.tbi.commons.model.ResearchManagementRole;
import gov.nih.tbi.metastudy.dao.MetaStudyDao;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.metastudy.model.hibernate.ResearchManagementMeta;
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

@Transactional("metaTransactionManager")
@Repository
public class MetaStudyDaoImpl extends GenericDaoImpl<MetaStudy, Long> implements MetaStudyDao {

	@Autowired
	public MetaStudyDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(MetaStudy.class, sessionFactory);
	}

	@Override
	public MetaStudy get(Long id) {

		if (id != null) {
			CriteriaBuilder cb = getCriteriaBuilder();
			CriteriaQuery<MetaStudy> query = cb.createQuery(MetaStudy.class);
			
			Root<MetaStudy> root = query.from(MetaStudy.class);
			query.where(cb.equal(root.get("id"), id));

			root.fetch("grantMetaSet", JoinType.LEFT);
			root.fetch("supportingDocumentationSet", JoinType.LEFT);
			root.fetch("metaStudyDataSet", JoinType.LEFT);
			root.fetch("metaStudyKeywords", JoinType.LEFT);
			root.fetch("metaStudyLabels", JoinType.LEFT);
			root.fetch("clinicalTrialMetaSet", JoinType.LEFT);
			root.fetch("researchMgmtMetaSet", JoinType.LEFT);
			root.fetch("therapeuticAgentSet",JoinType.LEFT);
			root.fetch("therapeuticTargetSet",JoinType.LEFT);
			root.fetch("therapyTypeSet",JoinType.LEFT);
			root.fetch("modelNameSet",JoinType.LEFT);
			root.fetch("modelTypeSet",JoinType.LEFT);

			MetaStudy metaStudy = getUniqueResult(query);
			return metaStudy;

		} else {
			return new MetaStudy();
		}
	}

	public boolean isTitleUnique(String title) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<MetaStudy> root = query.from(MetaStudy.class);

		query.where(cb.equal(cb.upper(root.get("title")), title.trim().toUpperCase()));
		query.select(cb.count(root));

		long count = createQuery(query).getSingleResult();
		return count == 0;
	}

	public MetaStudy getPublicMetaStudyById(Long metaStudyId) {

		if (metaStudyId != null) {
			CriteriaBuilder cb = getCriteriaBuilder();
			CriteriaQuery<MetaStudy> query = cb.createQuery(MetaStudy.class);
			Root<MetaStudy> root = query.from(MetaStudy.class);

			query.where(cb.equal(root.get("id"), metaStudyId)).distinct(true);
			root.fetch("researchMgmtMetaSet", JoinType.LEFT);

			MetaStudy metaStudy = getUniqueResult(query);
			return metaStudy;

		} else {
			return new MetaStudy();
		}
	}


	public List<MetaStudy> getMetaStudyListByIds(Set<Long> ids) {

		if (ids != null && !ids.isEmpty()) {
			CriteriaBuilder cb = getCriteriaBuilder();
			CriteriaQuery<MetaStudy> query = cb.createQuery(MetaStudy.class);
			Root<MetaStudy> root = query.from(MetaStudy.class);

			if (ids != null && !ids.isEmpty()) {
				query.where(root.get("id").in(ids));
			}

			List<MetaStudy> list = createQuery(query.distinct(true)).getResultList();
			return list;

		} else {
			return new ArrayList<MetaStudy>();
		}
	}

	public List<MetaStudy> getMetaStudyListFilterByStatus(Set<Long> ids, Set<MetaStudyStatus> status) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudy> query = cb.createQuery(MetaStudy.class);
		Root<MetaStudy> root = query.from(MetaStudy.class);

		Predicate predicate = cb.conjunction();

		if (ids != null && !ids.isEmpty()) {
			predicate = cb.and(predicate, root.get("id").in(ids));
		}

		if (status != null && !status.isEmpty()) {
			predicate = cb.and(predicate, root.get("status").in(status));
		}

		query.where(predicate).distinct(true);
		List<MetaStudy> list = createQuery(query).getResultList();
		return list;

	}

	public List<MetaStudy> metaStudyPublicSiteSearch() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudy> query = cb.createQuery(MetaStudy.class);
		Root<MetaStudy> root = query.from(MetaStudy.class);

		Join<MetaStudy, ResearchManagementMeta> rmJoin = root.join("researchMgmtMetaSet", JoinType.LEFT);
		query.where(cb.and(cb.equal(rmJoin.get("role"), ResearchManagementRole.PRIMARY_PRINCIPAL_INVESTIGATOR),
				cb.equal(root.get("status"), MetaStudyStatus.PUBLISHED)));
		query.orderBy(cb.desc(rmJoin.get("lastName")));

		List<MetaStudy> list = createQuery(query).getResultList();
		
		/*removed the non-primary-PI from research management meta set for public site 
		  since only primary PI is showing on meta study table in the public site
		 */
		for(MetaStudy metaStudy : list) {
			Set<ResearchManagementMeta> rmmList = metaStudy.getResearchMgmtMetaSet();
			for(Iterator<ResearchManagementMeta> rmmIterator = rmmList.iterator(); rmmIterator.hasNext();) {
				ResearchManagementMeta rmm = rmmIterator.next();
				if (!rmm.getRole().equals(ResearchManagementRole.PRIMARY_PRINCIPAL_INVESTIGATOR)) {
					rmmIterator.remove();
				}
			}
		}
		
		return list;
	}

	public MetaStudy getBasicMetaStudy(Long metaStudyId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudy> query = cb.createQuery(MetaStudy.class);
		Root<MetaStudy> root = query.from(MetaStudy.class);
		query.where(cb.equal(root.get("id"), metaStudyId)).distinct(true);

		MetaStudy metaStudy = getUniqueResult(query);
		return metaStudy;
	}


	@Override
	public List<TherapeuticAgent> getTherapeuticAgentsFromListOfStrings(List<String> agents) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<TherapeuticAgent> query = cb.createQuery(TherapeuticAgent.class);

		Root<TherapeuticAgent> root = query.from(TherapeuticAgent.class);
		Expression<String> exp = root.get("text");
		Predicate p = exp.in(agents);
		query.where(p);

		List<TherapeuticAgent> out = createQuery(query).getResultList();
		return out;
	}

	@Override
	public List<TherapeuticTarget> getTherapeuticTargetsFromListOfStrings(List<String> agents) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<TherapeuticTarget> query = cb.createQuery(TherapeuticTarget.class);

		Root<TherapeuticTarget> root = query.from(TherapeuticTarget.class);
		Expression<String> exp = root.get("text");
		Predicate p = exp.in(agents);
		query.where(p);

		List<TherapeuticTarget> out = createQuery(query).getResultList();
		return out;
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	public List<TherapeuticAgent> getMetaStudyTherapeuticAgents(String metaStudyId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyTherapeuticAgent> initialQuery = cb.createQuery(MetaStudyTherapeuticAgent.class);

		Root<MetaStudyTherapeuticAgent> initialRoot = initialQuery.from(MetaStudyTherapeuticAgent.class);
		initialQuery.where(cb.equal(initialRoot.get("metaStudyId"), metaStudyId));

		List<MetaStudyTherapeuticAgent> list = createQuery(initialQuery).getResultList();
		
		List<TherapeuticAgent> out = new ArrayList<>();
		
		for(MetaStudyTherapeuticAgent agent: list){
			out.add(agent.getTherapeuticAgent());
		}
		
		return out;
	
	}

	@Override
	public List<TherapeuticTarget> getMetaStudyTherapeuticTargets(String metaStudyId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyTherapeuticTarget> initialQuery = cb.createQuery(MetaStudyTherapeuticTarget.class);

		Root<MetaStudyTherapeuticTarget> initialRoot = initialQuery.from(MetaStudyTherapeuticTarget.class);
		initialQuery.where(cb.equal(initialRoot.get("metaStudyId"), metaStudyId));

		List<MetaStudyTherapeuticTarget> list = createQuery(initialQuery).getResultList();
		
		List<TherapeuticTarget> out = new ArrayList<>();
		
		for(MetaStudyTherapeuticTarget target: list){
			out.add(target.getTherapeuticTarget());
		}
		
		return out;
	}

	@Override
	public List<TherapyType> getMetaStudyTherapyTypes(String metaStudyId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyTherapyType> initialQuery = cb.createQuery(MetaStudyTherapyType.class);

		Root<MetaStudyTherapyType> initialRoot = initialQuery.from(MetaStudyTherapyType.class);
		initialQuery.where(cb.equal(initialRoot.get("metaStudyId"), metaStudyId));

		List<MetaStudyTherapyType> list = createQuery(initialQuery).getResultList();
		
		List<TherapyType> out = new ArrayList<>();
		
		for(MetaStudyTherapyType therapyType: list){
			out.add(therapyType.getTherapyType());
		}
		
		return out;
	
	}

	@Override
	public List<ModelName> getMetaStudyModelNames(String metaStudyId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyModelName> initialQuery = cb.createQuery(MetaStudyModelName.class);

		Root<MetaStudyModelName> initialRoot = initialQuery.from(MetaStudyModelName.class);
		initialQuery.where(cb.equal(initialRoot.get("metaStudyId"), metaStudyId));

		List<MetaStudyModelName> list = createQuery(initialQuery).getResultList();
		
		List<ModelName> out = new ArrayList<>();
		
		for(MetaStudyModelName modelName: list){
			out.add(modelName.getModelName());
		}
		
		return out;
	}

	@Override
	public List<ModelType> getMetaStudyModelTypes(String metaStudyId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyModelType> initialQuery = cb.createQuery(MetaStudyModelType.class);

		Root<MetaStudyModelType> initialRoot = initialQuery.from(MetaStudyModelType.class);
		initialQuery.where(cb.equal(initialRoot.get("metaStudyId"), metaStudyId));

		List<MetaStudyModelType> list = createQuery(initialQuery).getResultList();
		
		List<ModelType> out = new ArrayList<>();
		
		for(MetaStudyModelType modelType: list){
			out.add(modelType.getModelType());
		}
		
		return out;
	
	}

	@Override
	public List<MetaStudyModelType> getModelTypesForMetaStudy(String metaStudyId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyModelType> initialQuery = cb.createQuery(MetaStudyModelType.class);
		Root<MetaStudyModelType> initialRoot = initialQuery.from(MetaStudyModelType.class);
		initialQuery.where(cb.equal(initialRoot.get("metaStudyId"), metaStudyId));
		List<MetaStudyModelType> list = createQuery(initialQuery).getResultList();
		return list;
	}

	@Override
	public List<MetaStudyModelName> getModelNamesForMetaStudy(String metaStudyId) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyModelName> initialQuery = cb.createQuery(MetaStudyModelName.class);

		Root<MetaStudyModelName> initialRoot = initialQuery.from(MetaStudyModelName.class);
		initialQuery.where(cb.equal(initialRoot.get("metaStudyId"), metaStudyId));

		List<MetaStudyModelName> list = createQuery(initialQuery).getResultList();
		return list;
	}

	@Override
	public List<MetaStudyTherapyType> getTherapyTypesForMetaStudy(String metaStudyId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyTherapyType> initialQuery = cb.createQuery(MetaStudyTherapyType.class);

		Root<MetaStudyTherapyType> initialRoot = initialQuery.from(MetaStudyTherapyType.class);
		initialQuery.where(cb.equal(initialRoot.get("metaStudyId"), metaStudyId));

		List<MetaStudyTherapyType> list = createQuery(initialQuery).getResultList();
		return list;
	}

	@Override
	public List<MetaStudyTherapeuticTarget> getTherapeuticTargetsForMetaStudy(String metaStudyId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyTherapeuticTarget> initialQuery = cb.createQuery(MetaStudyTherapeuticTarget.class);

		Root<MetaStudyTherapeuticTarget> initialRoot = initialQuery.from(MetaStudyTherapeuticTarget.class);
		initialQuery.where(cb.equal(initialRoot.get("metaStudyId"), metaStudyId));

		List<MetaStudyTherapeuticTarget> list = createQuery(initialQuery).getResultList();
		return list;
	}

	@Override
	public List<MetaStudyTherapeuticAgent> getTherapeuticAgentsForMetaStudy(String metaStudyId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyTherapeuticAgent> initialQuery = cb.createQuery(MetaStudyTherapeuticAgent.class);

		Root<MetaStudyTherapeuticAgent> initialRoot = initialQuery.from(MetaStudyTherapeuticAgent.class);
		initialQuery.where(cb.equal(initialRoot.get("metaStudyId"), metaStudyId));

		List<MetaStudyTherapeuticAgent> list = createQuery(initialQuery).getResultList();
		return list;
	}

}
