package gov.nih.tbi.metastudy.dao.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.metastudy.dao.MetaStudyAccessRecordDao;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyAccessRecord;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyDocumentation;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.repository.model.hibernate.UserFile;

@Transactional("metaTransactionManager")
@Repository
public class MetaStudyAccessRecordDaoImpl extends GenericDaoImpl<MetaStudyAccessRecord, Long> implements MetaStudyAccessRecordDao {

	@Autowired
	public MetaStudyAccessRecordDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(MetaStudyAccessRecord.class, sessionFactory);
	}

	@Override
	public List<MetaStudyAccessRecord> searchAccessRecords(Long metaStudyId, String searchText, Date queueDate,
			Date oldestDate, PaginationData pageData) {
		
		List<Long> ids = getIDsOfMetaStudyAccessRecord(metaStudyId);
		
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<MetaStudyAccessRecord>();
		}
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyAccessRecord> query = cb.createQuery(MetaStudyAccessRecord.class);
		Root<MetaStudyAccessRecord> root = query.from(MetaStudyAccessRecord.class);
		
		Join<MetaStudyAccessRecord, MetaStudy> msJoin = root.join("metaStudy", JoinType.LEFT);
		Join<MetaStudyAccessRecord, MetaStudyData> msdJoin = root.join("metaStudyData", JoinType.LEFT);
		Join<MetaStudyAccessRecord, Account> aJoin = root.join("account", JoinType.LEFT);
		Join<MetaStudyAccessRecord, MetaStudyDocumentation> sdJoin = root.join("supportingDocumentation", JoinType.LEFT);
		Join<MetaStudyDocumentation, UserFile> sufJoin = sdJoin.join("userFile", JoinType.LEFT);
		Join<MetaStudyData, UserFile> dufJoin = msdJoin.join("userFile", JoinType.LEFT);
		Join<MetaStudyData, SavedQuery> dsqJoin = msdJoin.join("savedQuery", JoinType.LEFT);
			
		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(cb.equal(msJoin.get("id"), metaStudyId));
		predicates.add(root.get("id").in(ids));
		
		if (!StringUtils.isEmpty(searchText)) {
			Predicate searchClause = cb.disjunction();
			searchText = "%" + searchText.toUpperCase() + "%";
			
			searchClause = cb.or(searchClause, cb.like(cb.upper(aJoin.get("userName")), searchText));
			searchClause = cb.or(searchClause, cb.like(cb.upper(dufJoin.get("name")), searchText));
			searchClause = cb.or(searchClause, cb.like(cb.upper(dsqJoin.get("name")), searchText));
			searchClause = cb.or(searchClause, cb.like(cb.upper(sdJoin.get("url")), searchText));
			searchClause = cb.or(searchClause, cb.like(cb.upper(sufJoin.get("name")), searchText));
			searchClause = cb.or(searchClause, cb.like(cb.upper(msJoin.get("doi")), searchText));

			if (queueDate != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(queueDate);
				c.add(Calendar.DATE, 1);
				Date dt = (Date) c.getTime();
				searchClause = cb.or(searchClause, cb.between(root.get("dateCreated"), queueDate, dt));
			}

			predicates.add(searchClause);
		}

		if (oldestDate != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.get("dateCreated"), oldestDate));
		}

		Predicate[] predicateArr = predicates.toArray(new Predicate[predicates.size()]);
		query.where(cb.and(predicateArr));
		
		// Add Sorting
		if (pageData.getSort() != null) {
			Path<Object> orderPath = root.get(pageData.getSort());
			query.orderBy(pageData.getAscending() ? cb.asc(orderPath) : cb.desc(orderPath));
			
		} else {
			query.orderBy(cb.asc(root.get("dateCreated")));
		}
		
		TypedQuery<MetaStudyAccessRecord> q = createQuery(query);

		if (pageData != null) {
			if (searchText != null || oldestDate != null) {
				q.setMaxResults(ids.size());   // no need to run another query to get the count
			} else {
				q.setMaxResults(pageData.getPageSize());
			}
			q.setFirstResult((pageData.getPageSize() + 1) * (pageData.getPage() - 1));
		}

		List<MetaStudyAccessRecord> list = q.getResultList();
		return list;
	}
	
	public List<Long> getIDsOfMetaStudyAccessRecord(Long metaStudyId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<MetaStudyAccessRecord> root = query.from(MetaStudyAccessRecord.class);

		query.where(cb.equal(root.join("metaStudy", JoinType.LEFT).get("id"), metaStudyId));
		query.select(root.get("id"));
		
		TypedQuery<Long> q = createQuery(query);
	    return q.getResultList();
	}
    
	@Override
	public int countAccessRecords(Long metaStudyId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<MetaStudyAccessRecord> root = query.from(MetaStudyAccessRecord.class);

		query.where(cb.equal(root.join("metaStudy", JoinType.LEFT).get("id"), metaStudyId));
		query.select(cb.count(root));
		
		Long returnValue = getUniqueResult(query);
		return returnValue.intValue();
	}
	

	public List<MetaStudyAccessRecord> getAccessRecordByMetaStudyId(Long metaStudyId) {

		if (metaStudyId != null) {
			CriteriaBuilder cb = getCriteriaBuilder();
			CriteriaQuery<MetaStudyAccessRecord> query = cb.createQuery(MetaStudyAccessRecord.class);
			Root<MetaStudyAccessRecord> root = query.from(MetaStudyAccessRecord.class);

			query.where(cb.equal(root.join("metaStudy", JoinType.LEFT).get("id"), metaStudyId));
		//	root.join("supportingDocumentation", JoinType.LEFT).fetch("metaStudy", JoinType.LEFT);
			return createQuery(query.distinct(true)).getResultList();

		} else {
			return new ArrayList<MetaStudyAccessRecord>();
		}
	}
}
