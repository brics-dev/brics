package gov.nih.tbi.repository.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.repository.dao.BasicDatasetDao;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;

@Transactional("metaTransactionManager")
@Repository
public class BasicDatasetDaoImpl extends GenericDaoImpl<BasicDataset, Long> implements BasicDatasetDao {

	@Autowired
	public BasicDatasetDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {
		super(BasicDataset.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public List<BasicDataset> getByIds(Set<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<BasicDataset>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BasicDataset> query = cb.createQuery(BasicDataset.class);
		Root<BasicDataset> root = query.from(BasicDataset.class);

		query.where(root.get("id").in(ids)).distinct(true);
		return createQuery(query).getResultList();
	}

	/**
	 * @inheritDoc
	 */
	public List<Long> getPrivateSharedDatasetIds() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<BasicDataset> root = query.from(BasicDataset.class);

		query.where(cb.or(cb.equal(root.get("datasetStatus"), DatasetStatus.PRIVATE),
				cb.equal(root.get("datasetStatus"), DatasetStatus.SHARED))).distinct(true);
		query.select(root.get("id"));
		return createQuery(query).getResultList();
	}

	/**
	 * @inheritDoc
	 */
	public List<BasicDataset> getPrivateSharedDatasets() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BasicDataset> query = cb.createQuery(BasicDataset.class);
		Root<BasicDataset> root = query.from(BasicDataset.class);

		query.where(cb.or(cb.equal(root.get("datasetStatus"), DatasetStatus.PRIVATE),
				cb.equal(root.get("datasetStatus"), DatasetStatus.SHARED))).distinct(true);
		return createQuery(query).getResultList();
	}

	public Long getDatasetIdByDatasetFileId(Long datasetFileId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<BasicDataset> root = query.from(BasicDataset.class);

		query.where(cb.equal(root.join("datasetFileSet", JoinType.LEFT).get("id"), datasetFileId)).distinct(true);
		query.select(root.get("id"));
		return getUniqueResult(query);
	}
}
