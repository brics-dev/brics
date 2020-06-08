package gov.nih.tbi.repository.dao.hibernate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.account.dao.SessionLogDao;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.repository.dao.DatasetDao;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.model.hibernate.VisualizationDataset;
import gov.nih.tbi.repository.model.hibernate.VisualizationStudy;
import gov.nih.tbi.repository.model.hibernate.VisualizationAccessRecord;

@Transactional("metaTransactionManager")
@Repository
public class DatasetDaoImpl extends GenericDaoImpl<Dataset, Long> implements DatasetDao {

	private static Logger logger = Logger.getLogger(SessionLogDao.class);

	@Autowired
	public DatasetDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(Dataset.class, sessionFactory);
	}

	public List<Long> getDatasetIdsByStudyIds(Set<Long> studyIds, Set<DatasetStatus> statuses) {
		if (studyIds == null || studyIds.isEmpty() || statuses == null || statuses.isEmpty()) {
			return new ArrayList<Long>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(cb.and(root.join("study", JoinType.LEFT).get("id").in(studyIds),
				root.get("datasetStatus").in(statuses)));
		query.select(root.get("id")).distinct(true);

		return createQuery(query).getResultList();
	}

	public List<Long> getDatasetIdsByStudyIds(Set<Long> studyIds) {
		if (studyIds == null || studyIds.isEmpty()) {
			return new ArrayList<Long>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(cb.and(root.join("study", JoinType.LEFT).get("id").in(studyIds)));
		query.select(root.get("id")).distinct(true);

		return createQuery(query).getResultList();
	}

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

		// here we need to turn a set of DatasetStatuses into an array of dataset status
		// IDs
		List<Long> datasetStatuses = new ArrayList<Long>();

		for (DatasetStatus status : statuses) {
			datasetStatuses.add(status.getId());
		}

		final String HQL = "SELECT ds.id, s.id, s.title, dsd.dataStructureId from Dataset ds INNER JOIN ds.study s INNER JOIN ds.datasetDataStructure dsd WHERE ds.datasetStatus IN (:dsStatuses)";

		Query query = getSessionFactory().getCurrentSession().createQuery(HQL);
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
	public List<Dataset> getByIds(Set<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<Dataset>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(root.get("id").in(ids)).distinct(true);
		root.fetch("datasetDataStructure", JoinType.LEFT);

		return createQuery(query).getResultList();
	}

	@Override
	public Dataset getDatasetWithChildren(Long id) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(cb.equal(root.get("id"), id)).distinct(true);
		root.fetch("datasetFileSet", JoinType.LEFT);
		root.fetch("datasetDataStructure", JoinType.LEFT);
		root.fetch("datasetSubject", JoinType.LEFT);

		return getUniqueResult(query);
	}

	public Dataset getDatasetExcludingDatasetFiles(Long id) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(cb.equal(root.get("id"), id)).distinct(true);
		root.fetch("datasetDataStructure", JoinType.LEFT);
		root.fetch("datasetSubject", JoinType.LEFT);

		return getUniqueResult(query);
	}

	/**
	 * @inheritDoc
	 */
	public List<Dataset> search(Set<Long> ids, String key, List<String> searchColumns, DatasetStatus currentStatus,
			DatasetStatus requestStatus, PaginationData pageData) {

		// If the user does not have permission to view any dataset, then do not make a
		// query
		if (ids != null && ids.isEmpty()) {
			return new ArrayList<Dataset>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		// Subquery for search restrictions (rather then the pagination restrictions).
		Subquery<Long> subquery = query.subquery(Long.class);
		Root<Dataset> subRoot = subquery.from(persistentClass);

		List<Predicate> predList = new ArrayList<Predicate>();

		// Add the filter based on the ids list
		if (ids != null) {
			predList.add(subRoot.get("id").in(ids));
		}

		// Add the search term
		if (!StringUtils.isEmpty(key) && !searchColumns.isEmpty()) {
			Predicate keyPredicate = cb.disjunction();
			Predicate namePredicate = cb.disjunction();
			DatasetStatus status = DatasetStatus.getByName(key);
			Date queueDate = getParsedDate(key);

			key = CoreConstants.WILDCARD + key.toUpperCase() + CoreConstants.WILDCARD;

			for (String searchColumn : searchColumns) {
				switch (searchColumn) {
					case "nameLink":
						keyPredicate = cb.or(keyPredicate, cb.like(cb.upper(subRoot.get("name")), key));
						break;
					case "studyLink":
						keyPredicate = cb.or(keyPredicate,
								cb.like(cb.upper(subRoot.join("study", JoinType.LEFT).get("title")), key));
						break;
					case "submitterFullName":
						if (key.contains(",")) {
							String strArray[] = key.trim().split("\\, ");
							String lastName = strArray[0].replace("%", "");
							String firstName = strArray[1].replace("%", "");
							namePredicate = cb.and(
									cb.equal(cb.upper(subRoot.join("submitter", JoinType.LEFT).get("firstName")),
											firstName),
									cb.equal(cb.upper(subRoot.join("submitter", JoinType.LEFT).get("lastName")),
											lastName));
							keyPredicate = cb.or(keyPredicate, namePredicate);
						} else {
							keyPredicate = cb.or(keyPredicate,
									cb.like(cb.upper(subRoot.join("submitter", JoinType.LEFT).get("firstName")), key));
							keyPredicate = cb.or(keyPredicate,
									cb.like(cb.upper(subRoot.join("submitter", JoinType.LEFT).get("lastName")), key));
						}
						break;
					case "submitDate":
						if (queueDate != null) {
							Calendar c = Calendar.getInstance();
							c.setTime(queueDate);
							c.add(Calendar.DATE, 1);
							Date dt = c.getTime();
							keyPredicate = cb.or(keyPredicate, cb.between(subRoot.get("submitDate"), queueDate, dt));
						}
						break;
					case "status":
						if (status != null) {
							keyPredicate = cb.or(keyPredicate, cb.equal(subRoot.get("datasetStatus"), status));
						}
						break;
					default:
						// This should never happen.
						logger.error("Attempting to search by an  invalid column.");
						break;
				}
			}

			predList.add(keyPredicate);
		}

		// Filter the search by the user status (if value is null then do not filter)
		if (currentStatus != null) {
			predList.add(cb.equal(subRoot.get("datasetStatus"), currentStatus));
		}

		if (requestStatus != null) {
			predList.add(cb.equal(subRoot.get("datasetRequestStatus"), requestStatus));
		}

		Predicate subPredicate = cb.and(predList.toArray(new Predicate[predList.size()]));

		// This projection alters the criteria to return a list of distinct ids instead
		// of complete records
		subquery.select(subRoot.get("id")).distinct(true);
		subquery.where(subPredicate);

		Join<Dataset, Study> stdyJoin = root.join("study", JoinType.LEFT);
		Join<Dataset, User> subJoin = root.join("submitter", JoinType.LEFT);

		// The select criteria limits the results to a single page.
		query.where(root.get("id").in(subquery));

		// Pagination block
		if (pageData != null) {
			// get the count of distinct IDs only to compute the total results before
			// pagination
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<Dataset> countRoot = countQuery.from(Dataset.class);
			countQuery.select(cb.countDistinct(countRoot.get("id")));
			countQuery.where(countRoot.get("id").in(subquery));

			long count = getUniqueResult(countQuery);
			pageData.setNumFilteredResults((int) count);

			// Add Sorting
			String sortKey = pageData.getSort();
			if (sortKey != null && !sortKey.equals("null")) {

				if (sortKey.equals("submitterFullName")) {
					Expression<String> sortExp1 = subJoin.get("lastName");
					Expression<String> sortExp2 = subJoin.get("firstName");
					query.orderBy(pageData.getAscending() ? cb.asc(sortExp1) : cb.desc(sortExp1),
							pageData.getAscending() ? cb.asc(sortExp2) : cb.desc(sortExp2));

				} else {
					Expression<String> sortExp = root.get("submitDate");
					if (sortKey.equals("nameLink")) {
						sortExp = root.get("prefixedId");
					} else if (sortKey.equals("studyLink")) {
						sortExp = stdyJoin.get("title");
					} else if (sortKey.equals("status")) {
						sortExp = root.get("datasetStatus");
					}

					query.orderBy(pageData.getAscending() ? cb.asc(sortExp) : cb.desc(sortExp));
				}
			} else {
				query.orderBy(cb.desc(root.get("submitDate")));
			}
		}

		TypedQuery<Dataset> q = createQuery(query);

		// Add Pagination
		if (pageData != null && pageData.getPage() != null) {
			q.setMaxResults(pageData.getPageSize()).setFirstResult(pageData.getPageSize() * (pageData.getPage() - 1));
		}

		List<Dataset> list = q.getResultList();
		// System.out.println(q.unwrap(org.hibernate.Query.class).getQueryString());
		return list;

	}

	private Date getParsedDate(String dateString) {
		SimpleDateFormat dateForm;

		Date date = null;
		for (String dateFormat : ModelConstants.UNIVERSAL_DATE_FORMATS) {
			try {
				dateForm = new SimpleDateFormat(dateFormat);
				dateForm.setLenient(false);
				date = dateForm.parse(dateString);
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				int year = cal.get(Calendar.YEAR);

				if (year < 1000) {
					return null;
				}

				if (date != null) {
					break;
				}
			} catch (ParseException e) {
				// this is just a failing date format
				logger.warn("searchDatasets is not an ISO date.");
			}
		}

		return date;
	}

	/**
	 * @inheritDoc
	 */
	public List<Dataset> search(Set<Long> ids, String key, DatasetStatus datasetStatus, boolean requested,
			PaginationData pageData) {

		// If the user does not have permission to view any dataset, then do not make a
		// query
		if (ids != null && ids.isEmpty()) {
			return new ArrayList<Dataset>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		// Subquery for search restrictions (rather then the pagination restrictions).
		Subquery<Long> subquery = query.subquery(Long.class);
		Root<Dataset> subRoot = subquery.from(persistentClass);

		List<Predicate> subPredicates = new ArrayList<Predicate>();

		// Add the filter based on the ids list
		if (ids != null) {
			subPredicates.add(subRoot.get("id").in(ids));
		}

		// Add the search term
		if (key != null) {
			key = CoreConstants.WILDCARD + key + CoreConstants.WILDCARD;
			Predicate keyPredicate = cb.or(cb.like(cb.upper(subRoot.get("name")), key),
					cb.like(cb.upper(subRoot.join("study", JoinType.LEFT).get("title")), key),
					cb.like(cb.upper(subRoot.join("submitter", JoinType.LEFT).get("lastName")), key),
					cb.like(cb.upper(subRoot.join("submitter", JoinType.LEFT).get("firstName")), key));

			subPredicates.add(keyPredicate);
		}

		// Filter the search by the user status (if value is null then do not filter)
		if (datasetStatus != null) {
			if (requested) {
				subPredicates.add(cb.equal(subRoot.get("datasetRequestStatus"), datasetStatus));
			} else {
				subPredicates.add(cb.equal(subRoot.get("datasetStatus"), datasetStatus));
			}
		}

		Predicate subPredicate = cb.and(subPredicates.toArray(new Predicate[subPredicates.size()]));

		// This projection alters the criteria to return a list of distinct ids instead
		// of complete records
		subquery.select(subRoot.get("id")).distinct(true);
		subquery.where(subPredicate);

		Join<Dataset, Study> stdyJoin = root.join("study", JoinType.LEFT);
		Join<Dataset, User> subJoin = root.join("submitter", JoinType.LEFT);

		// The select criteria limits the results to a single page.
		query.where(root.get("id").in(subquery));

		// Pagination block
		if (pageData != null) {
			// get the count of distinct IDs only to compute the total results before
			// pagination
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<Dataset> countRoot = countQuery.from(Dataset.class);
			countQuery.select(cb.countDistinct(countRoot.get("id")));
			countQuery.where(countRoot.get("id").in(subquery));

			long count = getUniqueResult(countQuery);
			pageData.setNumSearchResults((int) count);

			// Add Sorting
			pageData.setSort("studyLink");
			pageData.setAscending(true);

			String sortKey = pageData.getSort();
			if (sortKey != null && !sortKey.equals("null")) {
				Expression<String> sortExp = root.get("submitDate");
				if (sortKey.equals("nameLink")) {
					sortExp = root.get("prefixedId");
				} else if (sortKey.equals("studyLink")) {
					sortExp = stdyJoin.get("title");
				} else if (sortKey.equals("submitter")) {
					sortExp = subJoin.get("fullName");
				} else if (sortKey.equals("status")) {
					sortExp = root.get("datasetStatus");
				}

				query.orderBy(pageData.getAscending() ? cb.asc(sortExp) : cb.desc(sortExp));

			} else {
				query.orderBy(cb.desc(root.get("submitDate")));
			}
		}

		TypedQuery<Dataset> q = createQuery(query);

		// Add Pagination
		if (pageData != null && pageData.getPage() != null) {
			q.setMaxResults(pageData.getPageSize()).setFirstResult(pageData.getPageSize() * (pageData.getPage() - 1));
		}

		List<Dataset> list = q.getResultList();
		return list;
	}

	public int countAll() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<BasicDataset> root = query.from(BasicDataset.class);

		query.select(cb.countDistinct(root));
		Long returnValue = getUniqueResult(query);
		return returnValue.intValue();
	}

	public List<Dataset> getDatasetsByStudy(Study study) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(cb.equal(root.get("study"), study)).distinct(true);
		// root.fetch("datasetFileSet", JoinType.LEFT);

		List<Dataset> datasets = createQuery(query).getResultList();
		return datasets;
	}

	@Override
	public List<VisualizationDataset> getVisualizationStudyDatasetByStudy(Study study) {

		String hql = "select d.id, d.dataset_status_id as \"datasetStatusId\", d.study_id as \"studyId\" from dataset d where study_id = "
				+ study.getId() + ";";
		Query query = getSession().createNativeQuery(hql);
		((NativeQueryImpl) query).setResultTransformer(Transformers.aliasToBean(VisualizationDataset.class));
		List<VisualizationDataset> vdList = query.getResultList();

		return vdList;
	}

	/**
	 * @inheritDoc
	 */
	public List<Dataset> getUploadingDataset(User user) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(cb.and(cb.equal(root.get("submitter"), user),
				cb.equal(root.get("datasetStatus"), DatasetStatus.UPLOADING))).distinct(true);
		root.fetch("datasetFileSet", JoinType.LEFT);
		root.fetch("datasetDataStructure", JoinType.LEFT);

		List<Dataset> datasets = createQuery(query).getResultList();
		return datasets;
	}

	/**
	 * @inheritDoc
	 */
	public Long getStatusCount(DatasetStatus status, boolean requested) {

		if (status == null) {
			return null;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<Dataset> root = query.from(persistentClass);

		if (requested) {
			query.where(cb.equal(root.get("datasetRequestStatus"), status));
		} else {
			query.where(cb.equal(root.get("datasetStatus"), status));
		}

		query.select(cb.countDistinct(root.get("id")));
		long count = createQuery(query).getSingleResult();
		return count;
	}

	/**
	 * @inheritDoc
	 */
	public Dataset getDatasetByName(String studyName, String datasetName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(cb.and(cb.like(cb.upper(root.get(CoreConstants.NAME)), datasetName.toUpperCase()),
				cb.equal(root.join("study").get("title"), studyName))).distinct(true);

		root.fetch("datasetFileSet", JoinType.LEFT);
		root.fetch("datasetDataStructure", JoinType.LEFT);
		root.fetch("datasetSubject", JoinType.LEFT);

		Dataset data = getUniqueResult(query);
		return data;
	}

	/**
	 * @inheritDoc
	 */
	public Dataset getByPrefixedId(String datasetId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(cb.equal(root.get("prefixedId"), datasetId)).distinct(true);
		root.fetch("datasetFileSet", JoinType.LEFT);
		root.fetch("datasetDataStructure", JoinType.LEFT);
		root.fetch("datasetSubject", JoinType.LEFT);

		Dataset dataset = getUniqueResult(query);
		return dataset;
	}

	/**
	 * @inheritDoc
	 */
	public Dataset getByPrefixedIdWithStudyInfo(String datasetId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(cb.equal(root.get("prefixedId"), datasetId)).distinct(true);
		root.fetch("datasetFileSet", JoinType.LEFT);
		root.fetch("datasetDataStructure", JoinType.LEFT);

		Dataset dataset = getUniqueResult(query);
		if (dataset != null && dataset.getStudy() != null) {
			dataset.getStudy().getStudySiteSet().size();
			dataset.getStudy().getClinicalTrialSet().size();
			dataset.getStudy().getGrantSet().size();
			dataset.getStudy().getSponsorInfoSet().size();
		}

		return dataset;
	}

	/**
	 * @inheritDoc
	 */
	public Dataset getByPrefixedIdWithoutSubjects(String datasetId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(cb.equal(root.get("prefixedId"), datasetId)).distinct(true);
		root.fetch("datasetFileSet", JoinType.LEFT);
		root.fetch("datasetDataStructure", JoinType.LEFT);

		Dataset dataset = getUniqueResult(query);
		return dataset;
	}

	public String getDatasetFilePath(String studyPrefixedId, String datasetName, String fileName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Dataset> root = query.from(persistentClass);

		Join<Dataset, DatasetFile> dfJoin = root.join("datasetFileSet");
		Join<DatasetFile, UserFile> ufJoin = dfJoin.join("userFile");
		query.where(cb.and(cb.equal(root.get(CoreConstants.NAME), datasetName),
				cb.equal(root.join("study").get("prefixedId"), studyPrefixedId),
				cb.equal(ufJoin.get("name"), fileName)));
		query.select(ufJoin.get("path")).distinct(true);

		return getUniqueResult(query);
	}

	public List<Dataset> getByStatusesAndDate(Date date, DatasetStatus datasetStatus) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(
				cb.and(cb.equal(root.get("datasetStatus"), datasetStatus), cb.lessThan(root.get("submitDate"), date)))
				.distinct(true);

		List<Dataset> datasets = createQuery(query).getResultList();
		return datasets;
	}

	public Dataset getDatasetWithFiles(Long id) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(cb.equal(root.get("id"), id)).distinct(true);
		root.fetch("datasetFileSet", JoinType.LEFT);

		return getUniqueResult(query);

	}

	public Dataset getDataset(Long id) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Dataset> query = cb.createQuery(Dataset.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(cb.equal(root.get("id"), id)).distinct(true);

		return getUniqueResult(query);
	}

	public int updateDatasetStatus(DatasetStatus status, Long datasetId) {
		Query query = getSessionFactory().getCurrentSession()
				.createQuery("update Dataset set datasetStatus = :status" + " where id = :id");
		query.setParameter("status", status);
		query.setParameter("id", datasetId);
		int result = query.executeUpdate();
		return result;

	}

	public List<Long> getDatasetIdsByStatus(Set<DatasetStatus> statuses) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<Dataset> root = query.from(persistentClass);

		query.where(root.get("datasetStatus").in(statuses));
		query.select(root.get("id")).distinct(true);

		return createQuery(query).getResultList();
	}

	public List<Dataset> getDatasetByStatuses(Set<DatasetStatus> statuses) {
		if (statuses == null || statuses.isEmpty()) {
			return new ArrayList<Dataset>();
		}
		Query query = getSessionFactory().getCurrentSession()
				.createQuery("FROM Dataset ds WHERE ds.datasetStatus IN (?1)");
		query.setParameter(1, statuses);
		List<Dataset> datasets = query.getResultList();
		return datasets;
	}

	public BigInteger getDatasetUserFiles(Long userFileId) {
		try {
			Query query = getSessionFactory().getCurrentSession().createSQLQuery(
					"select count(*) from Dataset ds, dataset_file dsf, user_file uf where ds.id = dsf.dataset_id and dsf.user_file_id = uf.id \r\n"
							+ "and dataset_status_id in (0,1,2,3) and dsf.user_file_id = :userFileId");
			query.setParameter("userFileId", userFileId);
			BigInteger count = (BigInteger) query.getSingleResult();
			return count;
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return null;
	}

	@Override
	public List<Integer> getSubmissionTypeseByDatasetId(Long datasetId) {
		String hql = "select dst.submissiontype_id as \"submissionTypes\" from dataset_submissiontype dst "
				+ "where (dst.dataset_id = ?);";

		NativeQuery query = getSession().createNativeQuery(hql);
		query.setParameter(1, datasetId);
		// ((NativeQueryImpl)
		// query).setResultTransformer(Transformers.aliasToBean(SubmissionType.class));
		List<Integer> list = (List<Integer>) query.getResultList();
		return list;
	}

}
