package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.dao.FormStructureSqlDao;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;

@Transactional("dictionaryTransactionManager")
@Repository
public class FormStructureSqlDaoImpl extends GenericDictDaoImpl<StructuralFormStructure, Long> implements FormStructureSqlDao {

	@Autowired
	public FormStructureSqlDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(StructuralFormStructure.class, sessionFactory);
	}

	public StructuralFormStructure get(String shortName, String version) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralFormStructure> query = cb.createQuery(StructuralFormStructure.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		query.where(cb.and(
				cb.like(cb.upper(root.get(FormStructure.SHORT_NAME)), shortName.toUpperCase()),
				cb.like(root.get(FormStructure.VERSION), version))).distinct(true);
		root.fetch("supportingDocumentationSet", JoinType.LEFT);
		root.fetch("repeatableGroups", JoinType.LEFT);
		root.fetch("diseaseList", JoinType.LEFT);

		StructuralFormStructure returnDataStructure = getUniqueResult(query);
		getLazyLoadedChildren(returnDataStructure);

		return returnDataStructure;
	}

	/**
	 * @inheritDoc
	 */
	public List<String> getNamesByIds(List<Long> formStructureIds) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		query.select(root.get("shortName"));
		query.where(root.get("id").in(formStructureIds)).distinct(true);
		List<String> nameList = createQuery(query).getResultList();

		return nameList;
	}

	public StructuralFormStructure getLatestVersionByShortName(String shortName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralFormStructure> query = cb.createQuery(StructuralFormStructure.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		query.where(cb.equal(root.get(FormStructure.SHORT_NAME), shortName));
		query.orderBy(cb.desc(root.get(FormStructure.ID))).distinct(true);

		List<StructuralFormStructure> listOfDataStructures = createQuery(query).getResultList();
		// Ordered by version (highest version first)
		StructuralFormStructure returnDataStructure = null;

		// if the results are empty we need to return a null form structure
		if (!listOfDataStructures.isEmpty()) {
			returnDataStructure = listOfDataStructures.get(0);
		}

		if (returnDataStructure != null) {
			Long id = returnDataStructure.getId();
			return get(id);
		} else {
			return null;
		}
	}

	/**
	 * @inheritDoc
	 */
	public List<StructuralFormStructure> getAllById(List<Long> dsIdList) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralFormStructure> query = cb.createQuery(StructuralFormStructure.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		query.where(root.get("id").in(dsIdList)).distinct(true);
		root.fetch("supportingDocumentationSet", JoinType.LEFT);
		root.fetch("repeatableGroups", JoinType.LEFT);
		root.fetch("diseaseList", JoinType.LEFT);

		List<StructuralFormStructure> listOfDataStructures = createQuery(query).getResultList();
		for (StructuralFormStructure ds : listOfDataStructures) {
			getLazyLoadedChildren(ds);
		}

		return listOfDataStructures;
	}

	public List<StructuralFormStructure> listDataStructuresByStatuses(Set<StatusType> statuses) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralFormStructure> query = cb.createQuery(StructuralFormStructure.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		query.where(root.get("status").in(statuses)).distinct(true);
		root.fetch("repeatableGroups", JoinType.LEFT);
		root.fetch("diseaseList", JoinType.LEFT);

		List<StructuralFormStructure> listOfDataStructures = createQuery(query).getResultList();
		return listOfDataStructures;
	}

	public List<StructuralFormStructure> listDataStructuresByStatus(StatusType status) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralFormStructure> query = cb.createQuery(StructuralFormStructure.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		query.where(cb.equal(root.get("status"), status)).distinct(true);

		List<StructuralFormStructure> listOfDataStructures = createQuery(query).getResultList();
		for (StructuralFormStructure ds : listOfDataStructures) {
			getLazyLoadedChildren(ds);
		}

		return listOfDataStructures;
	}

	@Override
	public List<StructuralFormStructure> listDataStructures(Set<Long> ids, PaginationData pageData) {

		if (ids != null && ids.isEmpty()) {
			return new ArrayList<StructuralFormStructure>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralFormStructure> query = cb.createQuery(StructuralFormStructure.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		// TODO re-check: subquery is meaningless and unnecessary, I removed from the code
		// The detachedCriteria holds all the filters and restrictions the user wants on the query.
		query.where(root.get(FormStructure.ID).in(ids));

		if (pageData != null) {
			// get the total number of search results (excluding pagination)
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			countQuery.select(cb.countDistinct(root.get(FormStructure.ID)));
			countQuery.where(root.get(FormStructure.ID).in(ids));

			long totalCount = createQuery(countQuery).getSingleResult();
			pageData.setNumSearchResults((int) totalCount);

			// Add Sorting
			if (pageData.getSort() != null) {
				if (pageData.getAscending()) {
					query.orderBy(cb.asc(root.get(pageData.getSort())));
				} else {
					query.orderBy(cb.desc(root.get(pageData.getSort())));
				}
			}
		}

		TypedQuery<StructuralFormStructure> q = createQuery(query.distinct(true));

		// Add Pagination
		if (pageData != null && pageData.getPage() != null) {
			q.setMaxResults(pageData.getPageSize()).setFirstResult(pageData.getPageSize() * (pageData.getPage() - 1));
		}

		List<StructuralFormStructure> list = q.getResultList();
		for (StructuralFormStructure ds : list) {
			getLazyLoadedChildren(ds);
		}
		return list;
	}

	
	@Override
	public List<StructuralFormStructure> getAllSortedById(List<Long> dsIdList, PaginationData pageData) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralFormStructure> query = cb.createQuery(StructuralFormStructure.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		// TODO re-check: subquery is meaningless and unnecessary, I removed from the code
		// The detachedCriteria holds all the filters and restrictions the user wants on the query.
		query.where(root.get(FormStructure.ID).in(dsIdList));

		// If there is a pageData object, then we want to get all the page information and
		// add the page/sort information to our selectCriteria
		if (pageData != null) {
			// Add Sorting
			if (pageData.getSort() != null) {
				if (pageData.getAscending()) {
					query.orderBy(cb.asc(root.get(pageData.getSort())));
				} else {
					query.orderBy(cb.desc(root.get(pageData.getSort())));
				}
			}
		}
		
		List<StructuralFormStructure> list = createQuery(query.distinct(true)).getResultList();
		for (StructuralFormStructure ds : list) {
			getLazyLoadedChildren(ds);
		}

		return list;
	}

	/**
	 * @inheritDoc
	 */
	public Map<Long, StructuralFormStructure> getAllIntoMap() {

		Map<Long, StructuralFormStructure> fsMap = new HashMap<Long, StructuralFormStructure>();
		List<StructuralFormStructure> fsList = this.getAll();

		if (fsList != null) {
			for (StructuralFormStructure fs : fsList) {
				fsMap.put(fs.getId(), fs);
			}
		}

		return fsMap;
	}

	/*
	 * override the generic DAO call so we can lazy load all children associated with DE
	 */
	@Override
	public StructuralFormStructure get(Long id) {

		StructuralFormStructure entity = getSession().find(this.persistentClass, id);

		if (entity == null) {
			throw new ObjectRetrievalFailureException(this.persistentClass, id);
		}
		entity.getSupportingDocumentationSet().size();
		entity.getRepeatableGroups().size();
		entity.getDiseaseList().size();
		getLazyLoadedChildren((entity));

		return entity;
	}

	/*
	 * override the generic DAO call so we can lazy load all children associated with DE
	 */
	@Override
	public List<StructuralFormStructure> getAll() {

		List<StructuralFormStructure> toReturn = this.getAll();

		for (StructuralFormStructure entity : toReturn) {
			entity.getSupportingDocumentationSet().size();
			entity.getRepeatableGroups().size();
			entity.getDiseaseList().size();
			getLazyLoadedChildren(entity);
		}

		return toReturn;
	}

	public List<StructuralFormStructure> getAllNoChildren() {

		List<StructuralFormStructure> toReturn = this.getAll();

		return toReturn;
	}

	/*
	 * This method will make a DB call to return disease list and classification children associated with the DE passed
	 */
	public void getLazyLoadedChildren(StructuralFormStructure dataStructure) {

		if (dataStructure != null) {
			for (RepeatableGroup rg : dataStructure.getRepeatableGroups()) {
				rg.getMapElements().size();
				for (MapElement me : rg.getMapElements()) {
					if (me.getStructuralDataElement() != null) {
						me.getStructuralDataElement().getValueRangeList().size();
						me.getStructuralDataElement().getAliasList().size();
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<StructuralFormStructure> listByStatus(Set<Long> ids, long[] statusList) {

		Set<Enum<StatusType>> statusSet = new HashSet<Enum<StatusType>>();

		// If the user does not have permission to view any data structures, then do not make a query
		if (ids.isEmpty()) {
			return new ArrayList<StructuralFormStructure>();
		}

		// puts all the status described in statusList into statusSet
		for (long statusId : statusList) {
			StatusType newStatus = StatusType.statusOf(statusId);
			if (!statusSet.contains(newStatus)) {
				statusSet.add(newStatus);
			}
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralFormStructure> query = cb.createQuery(StructuralFormStructure.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);
		
		query.where(cb.and(root.get(FormStructure.ID).in(ids), root.get(FormStructure.STATUS).in(statusSet)));
		query.orderBy(cb.asc(root.get(FormStructure.TITLE)));

		List<StructuralFormStructure> finalList = createQuery(query).getResultList();
		return finalList;
	}

	/**
	 * {@inheritDoc}
	 */
	public Long getStatusCount(StatusType status) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);
		
		if (status != null) {
			query.where(cb.equal(root.get("status"), status));
		}
		query.select(cb.countDistinct(root.get("id")));

		long count = createQuery(query).getSingleResult();
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<StructuralFormStructure> findByShortName(String shortName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralFormStructure> query = cb.createQuery(StructuralFormStructure.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);
		
		query.where(cb.like(cb.upper(root.get(FormStructure.SHORT_NAME)), shortName.toUpperCase()));
		root.fetch("supportingDocumentationSet", JoinType.LEFT);
		root.fetch("repeatableGroups", JoinType.LEFT);
		root.fetch("diseaseList", JoinType.LEFT);

		return createQuery(query).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getAllShortNameById(Set<Long> dsIdList) {

		if (dsIdList == null || dsIdList.isEmpty()) {
			return new ArrayList<String>();
		}

		// This function does not take version into account. It will only return short names and duplicates if two of
		// the ids are for different versions of the same form structure.
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);
		query.where(cb.and(root.get("id").in(dsIdList)));
		
		return createQuery(query).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<NameAndVersion> getAllDraftAndAPById(Set<Long> dsIdList) {

		List<NameAndVersion> nameAndVersionList = new ArrayList<NameAndVersion>();
		if (dsIdList == null || dsIdList.isEmpty()) {
			return nameAndVersionList;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		query.where(cb.and(root.get("id").in(dsIdList), 
				cb.notEqual(root.get("status"), StatusType.PUBLISHED),
				cb.notEqual(root.get("status"), StatusType.ARCHIVED),
				cb.notEqual(root.get("status"), StatusType.SHARED_DRAFT)));
		query.multiselect(root.get(FormStructure.SHORT_NAME), root.get(CoreConstants.VERSION)).distinct(true);

		List<Object[]> rows = createQuery(query).getResultList();
		for (Object[] row : rows) {
			NameAndVersion nameAndVersion = new NameAndVersion((String) row[0], (String) row[1]);
			nameAndVersionList.add(nameAndVersion);
		}

		return nameAndVersionList;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<NameAndVersion> getAllShortNameAndVersionById(Set<Long> dsIdList) {

		List<NameAndVersion> nameAndVersionList = new ArrayList<NameAndVersion>();
		if (dsIdList == null || dsIdList.isEmpty()) {
			return nameAndVersionList;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		query.where(root.get("id").in(dsIdList));
		query.multiselect(root.get(FormStructure.SHORT_NAME), root.get(CoreConstants.VERSION)).distinct(true);

		List<Object[]> rows = createQuery(query).getResultList();
		for (Object[] row : rows) {
			NameAndVersion nameAndVersion = new NameAndVersion((String) row[0], (String) row[1]);
			nameAndVersionList.add(nameAndVersion);
		}

		return nameAndVersionList;
	}

	/**
	 * @inheritDoc
	 */
	public List<String> getAllPublishedAndSharedDraftById(Set<Long> dsIdList) {

		if (dsIdList == null || dsIdList.isEmpty()) {
			return new ArrayList<String>();
		}

		// This function does not take version into account. It will only return short names and duplicates if two of
		// the ids are for different versions of the same form structure.
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		query.where(cb.and(root.get("id").in(dsIdList), cb.or(
				cb.equal(root.get("status"), StatusType.PUBLISHED),
				cb.equal(root.get("status"), StatusType.SHARED_DRAFT))));
		query.select(root.get(FormStructure.SHORT_NAME)).distinct(true);
		
		return createQuery(query).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<StructuralFormStructure> getByShortNameAndVersions(List<NameAndVersion> nameAndVersions) {

		if (nameAndVersions == null) {
			return null;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralFormStructure> query = cb.createQuery(StructuralFormStructure.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		Predicate disjunction = cb.disjunction();
		for (NameAndVersion nav : nameAndVersions) {
			Predicate predicate = cb.and(cb.equal(root.get(FormStructure.SHORT_NAME), nav.getName()),
					cb.equal(root.get(CoreConstants.VERSION), nav.getVersion()));
			disjunction = cb.or(disjunction, predicate);
		}

		query.where(disjunction).distinct(true);
		root.fetch("supportingDocumentationSet", JoinType.LEFT);
		root.fetch("repeatableGroups", JoinType.LEFT);
		root.fetch("diseaseList", JoinType.LEFT);
		
		List<StructuralFormStructure> list = createQuery(query).getResultList();
		for (StructuralFormStructure ds : list) {
			getLazyLoadedChildren(ds);
		}

		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, StructuralFormStructure> getShortNameAndVersionsMap(List<NameAndVersion> nameAndVersions) {

		List<StructuralFormStructure> list = getByShortNameAndVersions(nameAndVersions);
		Map<String, StructuralFormStructure> shortNameAndVersionsMap = new HashMap<String, StructuralFormStructure>();
		for (StructuralFormStructure fs : list) {
			shortNameAndVersionsMap.put(fs.getShortNameAndVersion(), fs);
		}
		return shortNameAndVersionsMap;
	}

	/**
	 * @inheritDoc
	 */
	public List<StructuralFormStructure> getAttachedDataStructure(String deName, String deVersion) {
		final String HQL =
				"SELECT fs.id, fs.shortName, fs.version, fs.title, fs.status FROM StructuralFormStructure fs " + 
				"INNER JOIN fs.repeatableGroups rg INNER JOIN rg.mapElements me INNER JOIN me.dataElement de " + 
				"WHERE de.name = :deName and de.version = :deVersion";
		
		// define the indexes of the projected properties
		final int ID_INDEX = 0;
		final int SHORT_NAME_INDEX = 1;
		final int VERSION_INDEX = 2;
		final int TITLE_INDEX = 3;
		final int STATUS_INDEX = 4;

		Query query = getSession().createQuery(HQL);
		query.setParameter("deName", deName);
		query.setParameter("deVersion", deVersion);

		List<Object[]> objectList = query.getResultList();

		List<StructuralFormStructure> formStructures = new ArrayList<StructuralFormStructure>();

		for (Object[] object : objectList) {
			StructuralFormStructure newFormStructure = new StructuralFormStructure();
			newFormStructure.setId((Long) object[ID_INDEX]);
			newFormStructure.setShortName((String) object[SHORT_NAME_INDEX]);
			newFormStructure.setVersion((String) object[VERSION_INDEX]);
			newFormStructure.setTitle((String) object[TITLE_INDEX]);
			newFormStructure.setStatus((StatusType) object[STATUS_INDEX]);
			formStructures.add(newFormStructure);
		}

		return formStructures;
	}

	/**
	 * adds a filter if the boolean isPublicData is set to true. This will only return the publicly viewable FSs
	 */
	public List<StructuralFormStructure> getAttachedDataStructure(String deName, String deVersion,
			boolean ispublicData) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralFormStructure> query = cb.createQuery(StructuralFormStructure.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		Subquery<Long> maxQuery = query.subquery(Long.class);
		Root<StructuralFormStructure> subroot = maxQuery.from(StructuralFormStructure.class);
		Join<StructuralFormStructure, RepeatableGroup> subRgJoin = subroot.join("repeatableGroups");
		Join<RepeatableGroup, MapElement> subMeJoin = subRgJoin.join("mapElements");
		Join<MapElement, StructuralDataElement> subDeJoin = subMeJoin.join("dataElement");
		
		maxQuery.select(subroot.get("id"));
		maxQuery.where(cb.and(
				cb.equal(subDeJoin.get("name"), deName), cb.equal(subDeJoin.get("version"), deVersion),
				cb.equal(subroot.get("shortName"), root.get("shortName"))));

		Join<StructuralFormStructure, RepeatableGroup> rgJoin = root.join("repeatableGroups");
		Join<RepeatableGroup, MapElement> meJoin = rgJoin.join("mapElements");
		Join<MapElement, StructuralDataElement> deJoin = meJoin.join("dataElement");
		query.where(cb.and(
				cb.equal(deJoin.get("name"), deName), cb.equal(deJoin.get("version"), deVersion),
				cb.equal(root.get("id"), maxQuery))).distinct(true);
		root.fetch("supportingDocumentationSet", JoinType.LEFT);
		
		List<StructuralFormStructure> list = createQuery(query).getResultList();
		for (StructuralFormStructure ds : list) {
			getLazyLoadedChildren(ds);
		}

		return list;
	}
	
	@Override
	public StructuralFormStructure getOriginalFormStructureByName(String name){
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralFormStructure> query = cb.createQuery(StructuralFormStructure.class);
		Root<StructuralFormStructure> root = query.from(StructuralFormStructure.class);

		query.where(cb.and(
				cb.equal(root.get(FormStructure.SHORT_NAME), name), cb.equal(root.get(CoreConstants.VERSION), "1.0")));
		
		return getUniqueResult(query);
	}
}
