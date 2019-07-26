package gov.nih.tbi.repository.dao.hibernate;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericRepoDaoImpl;
import gov.nih.tbi.repository.dao.SubmissionRecordJoinDao;
import gov.nih.tbi.repository.model.hibernate.SubmissionRecordJoin;

@Transactional("repositoryTransactionManager")
@Repository
public class SubmissionRecordJoinDaoImpl extends GenericRepoDaoImpl<SubmissionRecordJoin, Long>
		implements SubmissionRecordJoinDao {

	@Autowired
	public SubmissionRecordJoinDaoImpl(@Qualifier(CoreConstants.REPOS_FACTORY) SessionFactory sessionFactory) {

		super(SubmissionRecordJoin.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public List<SubmissionRecordJoin> getByDatasetId(Long datasetId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SubmissionRecordJoin> query = cb.createQuery(SubmissionRecordJoin.class);

		Root<SubmissionRecordJoin> root = query.from(SubmissionRecordJoin.class);
		query.where(cb.equal(root.get("datasetId"), datasetId));
		query.orderBy(cb.asc(root.get("id")));

		return createQuery(query).getResultList();
	}

	/**
	 * @inheritDoc
	 */
	public Long getNumRecordsInDataset(Long datasetId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);

		Root<SubmissionRecordJoin> root = query.from(SubmissionRecordJoin.class);
		query.select(cb.countDistinct(root.get("id")));
		query.where(cb.equal(root.get("datasetId"), datasetId));

		return getUniqueResult(query);
	}

	@Override
	public SubmissionRecordJoin save(SubmissionRecordJoin submissionRecordJoin) {
		return super.save(submissionRecordJoin);
	}

}
