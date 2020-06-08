package gov.nih.tbi.api.query.data.repository.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Repository;

import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.repository.model.hibernate.Study;

@Repository
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DatasetRepositoryImpl implements DatasetRepoCustom {

	@PersistenceContext
    private EntityManager entityManager;
	
	/**
	 * {@inheritDoc}
	 */
	public List<Dataset> getByStatuses(Set<DatasetStatus> statuses) {
		// define the indexes of the projected properties
		final int DATASET_ID = 0;
		final int STUDY_ID = 1;
		final int STUDY_TITLE = 2;
		final int DATA_STRUCTURE_ID = 3;

		if (statuses == null || statuses.isEmpty()) {
			return new ArrayList<Dataset>();
		}

		// here we need to turn a set of DatasetStatuses into an array of dataset status IDs
		List<Long> datasetStatuses = new ArrayList<Long>();

		for (DatasetStatus status : statuses) {
			datasetStatuses.add(status.getId());
		}

		final String HQL =
				"SELECT ds.id, s.id, s.title, dsd.dataStructureId from Dataset ds INNER JOIN ds.study s INNER JOIN ds.datasetDataStructure dsd WHERE ds.datasetStatus IN (:dsStatuses)";

		Query query = entityManager.createQuery(HQL);
		query.setParameter("dsStatuses", statuses);
		
		List<Object[]> objectList = query.getResultList();

		Map<Long, Dataset> datasetMap = new HashMap<Long, Dataset>();

		for (Object[] object : objectList) {
			Long datasetId = (Long) object[DATASET_ID];

			Long studyId = (Long) object[STUDY_ID];
			String studyTitle = (String) object[STUDY_TITLE];
			Long dataStructureId = (Long) object[DATA_STRUCTURE_ID];

			if (!datasetMap.containsKey(datasetId)) {
				Dataset newDataset = new Dataset();
				newDataset.setId(datasetId);
				Study newStudy = new Study();
				newStudy.setId(studyId);
				newStudy.setTitle(studyTitle);
				newDataset.setStudy(newStudy);
				datasetMap.put(datasetId, newDataset);
			}

			Dataset currentDataset = datasetMap.get(datasetId);
			DatasetDataStructure newDatasetDataStructure = new DatasetDataStructure();
			newDatasetDataStructure.setDataset(currentDataset);
			newDatasetDataStructure.setDataStructureId(dataStructureId);
			currentDataset.getDatasetDataStructure().add(newDatasetDataStructure);
		}

		return new ArrayList<Dataset>(datasetMap.values());
	}
	
	@Override
	public List<Long> getDatasetIdsByStudyIds(Set<Long> studyIds, Set<DatasetStatus> statuses) {
		if (studyIds == null || studyIds.isEmpty() || statuses == null || statuses.isEmpty()) {
			return new ArrayList<Long>();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<Dataset> root = query.from(Dataset.class);
		
		query.where(cb.and(root.join("study", JoinType.LEFT).get("id").in(studyIds),
				root.get("datasetStatus").in(statuses)));
		query.select(root.get("id")).distinct(true);
		
		return entityManager.createQuery(query).getResultList();
	}

	@Override
	public List<Dataset> getByIds(Set<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<Dataset>();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(Dataset.class);
		
		query.where(root.get("id").in(ids)).distinct(true);
		root.fetch("datasetDataStructure", JoinType.LEFT);
		
		return entityManager.createQuery(query).getResultList();
	}

}
