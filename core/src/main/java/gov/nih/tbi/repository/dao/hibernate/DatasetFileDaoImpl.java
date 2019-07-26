package gov.nih.tbi.repository.dao.hibernate;

import java.math.BigInteger;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.DatasetFileStatus;
import gov.nih.tbi.repository.dao.DatasetFileDao;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.SubmissionType;
import javax.persistence.criteria.JoinType;

@Transactional("metaTransactionManager")
@Repository
public class DatasetFileDaoImpl extends GenericDaoImpl<DatasetFile, Long> implements DatasetFileDao {

	@Autowired
	public DatasetFileDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(DatasetFile.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public DatasetFile getByDatasetIdAndFileName(Long datasetId, String fileName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DatasetFile> query = cb.createQuery(DatasetFile.class);
		Root<DatasetFile> root = query.from(DatasetFile.class);

		query.where(cb.and(cb.equal(root.join("dataset").get("id"), datasetId),
				cb.like(cb.upper(root.get("localLocation")), CoreConstants.WILDCARD + fileName.toUpperCase())))
				.distinct(true);
		return getUniqueResult(query);
	}

	/**
	 * @inheritDoc
	 */
	public List<DatasetFile> getByDatasetId(Long datasetId) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DatasetFile> query = cb.createQuery(DatasetFile.class);
		Root<DatasetFile> root = query.from(DatasetFile.class);
		query.where(cb.equal(root.get("dataset"), datasetId));

		Query<DatasetFile> q = createQuery(query);

		return q.getResultList();
	}

	// TBD
	@Override
	public BigInteger getDatasetPendingFileCount(Long datasetId) {
		String sqlString = "select COUNT(*) from dataset_file where dataset_id = ? and dataset_file_status = ? ";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		idQuery.setParameter(1, datasetId);
		idQuery.setParameter(2, DatasetFileStatus.PENDING.getId());
		BigInteger count = (BigInteger) idQuery.getSingleResult();
		return count;
	}
}
