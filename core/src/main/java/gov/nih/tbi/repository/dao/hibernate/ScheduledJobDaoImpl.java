package gov.nih.tbi.repository.dao.hibernate;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.JobStatus;
import gov.nih.tbi.commons.model.JobType;
import gov.nih.tbi.repository.dao.ScheduledJobDao;
import gov.nih.tbi.repository.model.hibernate.ScheduledJob;

/**
 * Created by amakar on 9/7/2016.
 */
@Transactional("metaTransactionManager")
@Repository
public class ScheduledJobDaoImpl extends GenericDaoImpl<ScheduledJob, Long> implements ScheduledJobDao {

	@Autowired
	public ScheduledJobDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(ScheduledJob.class, sessionFactory);
	}

	@Override
	public ScheduledJob get(Long id) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ScheduledJob> query = cb.createQuery(ScheduledJob.class);

		Root<ScheduledJob> root = query.from(ScheduledJob.class);
		query.where(cb.equal(root.get(CoreConstants.ID), id)).distinct(true);
		root.fetch("subTasks", JoinType.LEFT);

		return getUniqueResult(query);
	}


	public List<ScheduledJob> searchByTypeAndStatus(JobType type, JobStatus status) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ScheduledJob> query = cb.createQuery(ScheduledJob.class);

		Root<ScheduledJob> root = query.from(ScheduledJob.class);
		query.where(cb.and(
				cb.equal(root.get("typeStr"), type.getId()), cb.equal(root.get("statusInt"), status.getId())));
		query.distinct(true);
		root.fetch("subTasks", JoinType.LEFT);

		return createQuery(query).getResultList();
	}
}
