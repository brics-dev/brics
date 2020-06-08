package gov.nih.tbi.repository.dao.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
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
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.repository.dao.VisualizationAccessRecordDao;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.VisualizationAccessData;
import gov.nih.tbi.repository.model.hibernate.VisualizationAccessRecord;
import gov.nih.tbi.repository.model.hibernate.VisualizationStudy;

@Transactional("metaTransactionManager")
@Repository
public class VisualizationAccessRecordDaoImpl extends GenericDaoImpl<VisualizationAccessRecord, Long> implements VisualizationAccessRecordDao {
	static Logger logger = Logger.getLogger(VisualizationAccessRecordDaoImpl.class);

	@Autowired
	public VisualizationAccessRecordDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(VisualizationAccessRecord.class, sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<VisualizationAccessRecord> getAll(Long studyId, Date startDate, Date endDate, PaginationData pageData) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<VisualizationAccessRecord> query = cb.createQuery(VisualizationAccessRecord.class);

		Root<VisualizationAccessRecord> root = query.from(VisualizationAccessRecord.class);
		Join<VisualizationAccessRecord, Dataset> datasetJoin = root.join("dataset", JoinType.LEFT);
		//Join<Dataset, DatasetFile> fileJoin = datasetJoin.join("datasetFileSet", JoinType.LEFT);
		Join<Dataset, Study> studyJoin = datasetJoin.join("study", JoinType.LEFT);
		Join<VisualizationAccessRecord, Account> accountJoin = root.join("account", JoinType.LEFT);

		Predicate predicate = cb.conjunction();

		if (startDate != null) {
			predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("queueDate"), startDate));
		}

		if (endDate != null) {
			predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("queueDate"), endDate));
		}

		predicate = cb.and(predicate, cb.or(cb.isNull(accountJoin.get("hideAccessRecords")),
				cb.equal(accountJoin.get("hideAccessRecords"), false)));

		if (studyId > 0) {
			predicate = cb.and(predicate, cb.equal(studyJoin.get("id"), studyId));
		}

		query.where(predicate).orderBy(cb.desc(root.get("queueDate"))).distinct(true);
		TypedQuery<VisualizationAccessRecord> q = createQuery(query);

		// Pagination block
		if (pageData != null) {
			if (pageData.getPage() != null && pageData.getPageSize() != null) {
				q.setMaxResults(pageData.getPageSize())
						.setFirstResult(pageData.getPageSize() * (pageData.getPage() - 1));
			}
		}

		List<VisualizationAccessRecord> list = q.getResultList();
		
		// Lazy fetch after the select
		for (VisualizationAccessRecord var : list) {
			if (var.getAccount() != null) {
				var.getAccount().getAccountRoleList().size();
				var.getAccount().getPermissionGroupMemberList().size();
			}
			
			if (var.getDataset() != null) {
				var.getDataset().getDatasetFileSet().size();
			}
		}
		
		return list;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<VisualizationAccessData> getAllAccessRecords() {
			String hql = "SELECT ar.id, s.id as \"studyId\", s.title as \"studyTitle\", d.id as \"datasetId\", a.id as \"accountId\", a.user_name as \"userName\", u.first_name as \"firstName\", u.last_name as \"lastName\" FROM access_record ar " 
					+ "left join dataset d on d.id = ar.dataset_id "
					+ "left join study s on d.study_id = s.id " 
					+ "left join account a on a.id = ar.account_id "
					+ "left join tbi_user u on u.id = a.id "
					+ "where (a.hide_access_records = false or a.hide_access_records is null) "
					+ "ORDER BY a.id;";
	
		Query query = getSession().createNativeQuery(hql);
		((NativeQueryImpl) query).setResultTransformer(Transformers.aliasToBean(VisualizationAccessData.class));
		List<VisualizationAccessData> list = query.getResultList();

		return list;
		

	}



}
