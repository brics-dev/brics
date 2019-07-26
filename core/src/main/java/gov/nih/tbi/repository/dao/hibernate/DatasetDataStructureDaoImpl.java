
package gov.nih.tbi.repository.dao.hibernate;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
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
import gov.nih.tbi.repository.dao.DatasetDataStructureDao;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;

@Transactional("metaTransactionManager")
@Repository
public class DatasetDataStructureDaoImpl extends GenericDaoImpl<DatasetDataStructure, Long> implements DatasetDataStructureDao {

	@Autowired
	public DatasetDataStructureDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(DatasetDataStructure.class, sessionFactory);
	}

	public Boolean isFormInAnyDataset(Long formId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DatasetDataStructure> query = cb.createQuery(DatasetDataStructure.class);
		Root<DatasetDataStructure> root = query.from(DatasetDataStructure.class);
		Join<DatasetDataStructure, Dataset> dsJoin = root.join("dataset", JoinType.INNER);

		query.where(cb.and(cb.equal(root.get("dataStructureId"), formId),
				dsJoin.get("datasetStatus").in(DatasetStatus.PRIVATE, DatasetStatus.SHARED))).distinct(true);

		List<DatasetDataStructure> results = createQuery(query).getResultList();

		if (results == null || results.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public List<DatasetDataStructure> getByDatasetId(Long id) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DatasetDataStructure> query = cb.createQuery(DatasetDataStructure.class);
		Root<DatasetDataStructure> root = query.from(DatasetDataStructure.class);

		query.where(cb.equal(root.join("dataset", JoinType.LEFT).get("id"), id)).distinct(true);
		return createQuery(query).getResultList();
	}

	public List<DatasetDataStructure> getByFormStructureId(Long id) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DatasetDataStructure> query = cb.createQuery(DatasetDataStructure.class);
		Root<DatasetDataStructure> root = query.from(DatasetDataStructure.class);

		query.where(cb.equal(root.get("dataStructureId"), id)).distinct(true);
		return createQuery(query).getResultList();
	}
}
