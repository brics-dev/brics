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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.DataSource;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.ResearchManagementRole;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.repository.dao.StudyDao;
import gov.nih.tbi.repository.dao.VisualizationStudyDao;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.VisualizationStudy;

@Transactional("metaTransactionManager")
@Repository
public class VisualizationStudyDaoImpl extends GenericDaoImpl<VisualizationStudy, Long> implements VisualizationStudyDao {

	@Autowired
	public VisualizationStudyDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(VisualizationStudy.class, sessionFactory);
	}

	@Override
	public List<Study> getAllVisualization() {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Study> query = cb.createQuery(Study.class);
		Predicate predicate = cb.conjunction();
		
		Root<Study> root = query.from(Study.class);
		root.fetch("sponsorInfoSet", JoinType.LEFT);
		root.fetch("studySiteSet", JoinType.LEFT);
		root.fetch("grantSet", JoinType.LEFT);
		root.fetch("studyForms", JoinType.LEFT);
		root.fetch("researchMgmtSet", JoinType.LEFT).fetch("pictureFile", JoinType.LEFT);
		
		//predicate = cb.and(predicate, cb.equal(root.get("studyStatus"), 0));
		//if we want to add teh above predicate this must be included below where(predicate)
		//query.where(predicate);
		return createQuery(query.distinct(true)).getResultList();
	}
	
	@Override
	public Map<Long, VisualizationStudy> getAllVisualizationStudyData() {
		
		String hql = "SELECT ds.study_id as \"studyId\", SUM(uf.user_file_size) as \"totalDataFileSize\" FROM dataset ds " 
					+ "join study s on s.id = ds.study_id "
					+ "join dataset_file dsf on ds.id = dsf.dataset_id " 
					+ "join user_file uf on dsf.user_file_id = uf.id "
					//+ "where s.study_status_id = 0 "
					+ "GROUP BY ds.study_id "
					+ "ORDER BY ds.study_id;";

		Query query = getSession().createNativeQuery(hql);
		((NativeQueryImpl) query).setResultTransformer(Transformers.aliasToBean(VisualizationStudy.class));
		List<VisualizationStudy> vsList = query.getResultList();
		
		Map<Long, VisualizationStudy> VisualizationStudyMap = new HashMap<Long, VisualizationStudy>();
		
		for (VisualizationStudy obj : vsList) {
			VisualizationStudyMap.put(obj.getStudyId().longValue(), obj);
		}
		
		return VisualizationStudyMap;
	}
		
}
