package gov.nih.tbi.repository.dao.hibernate;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.BasicStudyDao;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.BasicStudySearch;

@Transactional("metaTransactionManager")
@Repository
public class BasicStudyDaoImpl extends GenericDaoImpl<BasicStudy, Long> implements BasicStudyDao {
	@Autowired
	public BasicStudyDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(BasicStudy.class, sessionFactory);
	}
	
	public BasicStudy getBasicStudyByPrefixId(String prefixId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BasicStudy> query = cb.createQuery(BasicStudy.class);
		Root<BasicStudy> root = query.from(BasicStudy.class);

		query.where(cb.equal(root.get("prefixedId"), prefixId)).distinct(true);
		return getUniqueResult(query);
	}
	
	/**
	 * need to look at readable vs performance. break up into 3 queries. one for the meta, one for private/shared, one for submission type
	 * maybe a view????
	 */
	public List<BasicStudySearch> getPublicSiteSearchBasicStudies(){

		String publicSiteAggregateSearch = "select s.id as id, s.title as title, s.abstract as \"abstractText\", fs.name as \"fundingSource\", rm.last_name || ', ' || rm.first_name as \"principleName\", rm.org_name as institution, "
				+ "(select count(id) from dataset where study_id = s.id and dataset_status_id = 0) as \"privateDatasetCount\", "
				+ "(select count(id) from dataset where study_id = s.id and dataset_status_id = 1) as \"sharedDatasetCount\", "
				+ "(select count(dataset_id) from dataset_submissiontype where dataset_id in (select id from dataset where study_id = s.id and dataset_status_id in (0,1)) and submissiontype_id = 0) as \"clinicalDataCount\", "
				+ "(select count(dataset_id) from dataset_submissiontype where dataset_id in (select id from dataset where study_id = s.id and dataset_status_id in (0,1)) and submissiontype_id = 1) as \"genomicDataCount\", "
				+ "(select count(dataset_id) from dataset_submissiontype where dataset_id in (select id from dataset where study_id = s.id and dataset_status_id in (0,1)) and submissiontype_id = 2) as \"imagingDataCount\", "
				+ "s.study_status_id as \"studyStatus\" "
				+ "from study s "
				+ "left outer join research_management rm on s.id = rm.study_id "
				+ "left join funding_source fs on s.funding_source_id = fs.id "
				+ "where rm.role_id = 0 "
				//+ "and s.study_status_id = 0 "
				+ "order by rm.last_name";
		
		Query query = getSession().createNativeQuery(publicSiteAggregateSearch);
		((NativeQueryImpl) query).setResultTransformer(Transformers.aliasToBean(BasicStudySearch.class));
		
		List<BasicStudySearch> results = query.getResultList();
		return results;
	}
}
