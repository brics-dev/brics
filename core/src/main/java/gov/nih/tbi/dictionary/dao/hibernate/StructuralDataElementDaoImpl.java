package gov.nih.tbi.dictionary.dao.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.dictionary.dao.StructuralDataElementDao;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.SchemaPv;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

/**
 * Hibernate implementation of the Data Element Dao.
 * 
 * @author Andrew Johnson
 * @author Francis Chen
 * @author Michael Valeiras
 */

@Transactional("dictionaryTransactionManager")
@Repository
public class StructuralDataElementDaoImpl<T, PK extends Serializable> extends GenericDictDaoImpl<StructuralDataElement, Long> implements StructuralDataElementDao {

	static Logger logger = Logger.getLogger(StructuralDataElementDaoImpl.class);

	@Autowired
	public StructuralDataElementDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(StructuralDataElement.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public StructuralDataElement getLatestByName(String dataElementName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralDataElement> query = cb.createQuery(StructuralDataElement.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.where(cb.like(cb.upper(root.get(CoreConstants.NAME)), dataElementName.toUpperCase()));
		query.orderBy(cb.desc(root.get(DataElement.ID)));
		root.fetch("supportingDocumentationSet", JoinType.LEFT);

		StructuralDataElement de = null;

		try {
			de = createQuery(query).setMaxResults(1).getSingleResult();
		} catch (NoResultException ex) {
		}

		if (de != null) {
			getLazyLoadedChildren(de);
		}

		return de;
	}

	/**
	 * @inheritDoc
	 */
	public List<StructuralDataElement> findByShortName(String dataElementName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralDataElement> query = cb.createQuery(StructuralDataElement.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.where(cb.like(cb.upper(root.get(CoreConstants.NAME)), dataElementName.toUpperCase()));

		List<StructuralDataElement> des = createQuery(query.distinct(true)).getResultList();
		for (StructuralDataElement de : des) {
			getLazyLoadedChildren(de);
		}
		return des;
	}

	/**
	 * @inheritDoc
	 */
	public StructuralDataElement getByNameAndVersion(String dataElementName, String version) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralDataElement> query = cb.createQuery(StructuralDataElement.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.where(cb.and(cb.like(root.get(CoreConstants.NAME), dataElementName),
				cb.equal(root.get(CoreConstants.VERSION), version)));

		root.fetch("supportingDocumentationSet", JoinType.LEFT);
		StructuralDataElement de = getUniqueResult(query);

		if (de != null) {
			getLazyLoadedChildren(de);
		}
		return de;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<StructuralDataElement> getByNameAndVersions(List<NameAndVersion> nameAndVersions) {

		Map<String, StructuralDataElement> deMap = getByNameAndVersionsMap(nameAndVersions);
		return deMap != null ? new ArrayList<StructuralDataElement>(deMap.values()) : null;
	}

	/**
	 * {@inheritDoc} Represents the following SQL query select * from data_element as a join ( select element_name,
	 * max(version) as ver from data_element group by element_name ) as b on a.element_name = b.element_name where
	 * a.version = b.ver;
	 * 
	 */
	public List<StructuralDataElement> getLatestByNameList(Set<String> dataElementNames) {

		// This search cannot be done through straight criteria, but should be optimized by HQL
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralDataElement> query = cb.createQuery(StructuralDataElement.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.where(root.get(CoreConstants.NAME).in(dataElementNames)).distinct(true);
		List<StructuralDataElement> fullList = createQuery(query).getResultList();

		// Filter out all the non-latest versions
		Map<String, StructuralDataElement> latestDEs = new HashMap<String, StructuralDataElement>();
		for (StructuralDataElement de : fullList) {
			StructuralDataElement fromMap = latestDEs.get(de.getName());
			String[] latestDEVersion = new String[2];
			if (fromMap != null) {
				latestDEVersion = fromMap.getVersion().split("\\.");
			}
			String[] currDEVerion = de.getVersion().split("\\.");
			if (fromMap == null || Integer.valueOf(latestDEVersion[0]) < Integer.valueOf(currDEVerion[0])
					|| (Integer.valueOf(latestDEVersion[0]).equals(Integer.valueOf(currDEVerion[0]))
							&& Integer.valueOf(latestDEVersion[1]) < Integer.valueOf(currDEVerion[1]))) {
				latestDEs.put(de.getName(), de);
			}
		}
		List<StructuralDataElement> dataElements = new ArrayList<StructuralDataElement>(latestDEs.values());

		for (StructuralDataElement de : dataElements) {
			getLazyLoadedChildren(de);
		}

		return dataElements;
	}

	/**
	 * @inheritDoc
	 */
	public Map<String, StructuralDataElement> getByNameAndVersionsMap(List<NameAndVersion> nameAndVersions) {

		Map<String, StructuralDataElement> output = new HashMap<String, StructuralDataElement>();
		if (nameAndVersions == null || nameAndVersions.isEmpty()) {
			return output;
		}

		// split the list
		List<List<NameAndVersion>> chunkedList = splitList(nameAndVersions);

		log.debug("Splitting NameAndVersions list (size=" + nameAndVersions.size() + ") into " + chunkedList.size()
				+ " item(s)");
		List<StructuralDataElement> fullList = new ArrayList<StructuralDataElement>();

		String hql = "";
		String forHqlNameVersion = "";

		for (List<NameAndVersion> smallerList : chunkedList) {
			int counter = 0;
			for (NameAndVersion nameAndVersion : smallerList) {
				counter++;
				String name = nameAndVersion.getName();
				String version = nameAndVersion.getVersion();

				forHqlNameVersion += "('" + name + "','" + version + "')";
				if (counter != smallerList.size()) {
					forHqlNameVersion += ",";
				}
			}
			hql = "from StructuralDataElement sd where (sd.name, sd.version) in (" + forHqlNameVersion + ")";
			TypedQuery<StructuralDataElement> q = getSession().createQuery(hql);

			fullList.addAll(q.getResultList());
			forHqlNameVersion = "";
		}

		for (StructuralDataElement de : fullList) {
			if (de != null) {
				getLazyLoadedChildren(de);
			}
			output.put(de.getNameAndVersion(), de);
		}

		return output;
	}

	/**
	 * In certain circumstances, the query to PostgreSQL was too large to handle all the disjunctions, so this splits
	 * the list apart.
	 * 
	 * @param originalList
	 * @return
	 */
	private List<List<NameAndVersion>> splitList(List<NameAndVersion> originalList) {

		List<List<NameAndVersion>> chunkedList = new ArrayList<List<NameAndVersion>>();
		int n = 1000;

		/**
		 * Loop stolen from http://stackoverflow.com/questions/13678387/how-to-split-array-list-into-equal-parts
		 */
		for (int i = 0; i < originalList.size(); i += n) {
			List<NameAndVersion> chunk = originalList.subList(i, Math.min(originalList.size(), i + n));
			chunkedList.add(chunk);
		}
		return chunkedList;
	}


	/**
	 * {@inheritDoc}
	 */
	public List<StructuralDataElement> getByIdList(List<Long> ids) {

		if (ids == null || ids.isEmpty()) {
			return new ArrayList<StructuralDataElement>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralDataElement> query = cb.createQuery(StructuralDataElement.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.where(root.get("id").in(ids)).distinct(true);

		List<StructuralDataElement> dataElements = createQuery(query).getResultList();
		for (StructuralDataElement de : dataElements) {
			getLazyLoadedChildren(de);
		}

		return dataElements;
	}

	/**
	 * @inheritDoc
	 */
	public Long getStatusCount(DataElementStatus status, Category category) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);
		Predicate predicate = cb.conjunction();

		if (status != null) {
			predicate = cb.and(predicate, cb.equal(root.get("status"), status));
		}
		if (category != null) {
			predicate = cb.and(predicate, cb.equal(root.get("category"), category));
		}

		query.where(predicate);
		query.select(cb.countDistinct(root.get(CoreConstants.ID)));
		long count = createQuery(query).getSingleResult();
		return count;
	}

	/**
	 * @inheritDoc
	 */
	public List<StructuralDataElement> listByStatus(DataElementStatus status) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralDataElement> query = cb.createQuery(StructuralDataElement.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.where(cb.equal(root.get("status"), status)).distinct(true);

		List<StructuralDataElement> des = createQuery(query).getResultList();
		for (StructuralDataElement de : des) {
			getLazyLoadedChildren(de);
		}

		return des;
	}

	public List<StructuralDataElement> listByStatuses(Set<DataElementStatus> statuses) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralDataElement> query = cb.createQuery(StructuralDataElement.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.where(root.get("status").in(statuses)).distinct(true);

		List<StructuralDataElement> des = createQuery(query).getResultList();
		for (StructuralDataElement de : des) {
			getLazyLoadedChildren(de);
		}

		return des;
	}


	/**
	 * @inheritDoc
	 */
	public Map<Long, StructuralDataElement> getByMapElementIds(Set<Long> ids) {

		Map<Long, StructuralDataElement> dataElementMap = new HashMap<Long, StructuralDataElement>();

		if (ids == null || ids.isEmpty()) {
			return dataElementMap;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MapElement> query = cb.createQuery(MapElement.class);
		Root<MapElement> root = query.from(MapElement.class);

		query.where(root.get("id").in(ids));
		List<MapElement> mes = createQuery(query).getResultList();

		for (MapElement me : mes) {
			dataElementMap.put(me.getId(), me.getStructuralDataElement());
			getLazyLoadedChildren(me.getStructuralDataElement());
		}

		return dataElementMap;
	}

	public StructuralDataElement getByMapElementId(Long id) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MapElement> query = cb.createQuery(MapElement.class);
		Root<MapElement> root = query.from(MapElement.class);

		query.where(cb.equal(root.get("id"), id));
		MapElement me = getUniqueResult(query);

		if (me != null && me.getStructuralDataElement() != null) {
			getLazyLoadedChildren(me.getStructuralDataElement());
			return me.getStructuralDataElement();
		} else {
			return null;
		}
	}

	/*
	 * override the generic DAO call so we can lazy load all children associated with DE
	 */
	@Override
	public StructuralDataElement get(Long id) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralDataElement> query = cb.createQuery(StructuralDataElement.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.where(cb.equal(root.get("id"), id)).distinct(true);
		root.fetch("supportingDocumentationSet", JoinType.LEFT);

		StructuralDataElement de = getUniqueResult(query);
		if (de != null) {
			getLazyLoadedChildren(de);
		}

		return de;
	}

	/*
	 * override the generic DAO call so we can lazy load all children associated with DE
	 */
	@Override
	public List<StructuralDataElement> getAll() {

		List<StructuralDataElement> toReturn = this.getAll();

		for (StructuralDataElement element : toReturn) {
			getLazyLoadedChildren(element);
		}

		return toReturn;
	}

	/*
	 * This method will make a DB call to return disease list and classification children associated with the DE passed
	 */
	private void getLazyLoadedChildren(StructuralDataElement dataElement) {

		if (dataElement != null) {
			dataElement.getValueRangeList().size();
			dataElement.getAliasList().size();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public List<NameAndVersion> getAllDraftAndArchivedById(Set<Long> dsIdList) {

		if (dsIdList == null || dsIdList.isEmpty()) {
			return new ArrayList<NameAndVersion>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.where(cb.and(root.get("id").in(dsIdList), cb.notEqual(root.get("status"), DataElementStatus.PUBLISHED),
				cb.notEqual(root.get("status"), DataElementStatus.AWAITING)));
		query.multiselect(root.get(CoreConstants.NAME), root.get(CoreConstants.VERSION)).distinct(true);

		List<Object[]> rows = createQuery(query).getResultList();


		List<NameAndVersion> nameAndVersionList = new ArrayList<NameAndVersion>();

		for (Object[] row : rows) {
			NameAndVersion nameAndVersion = new NameAndVersion((String) row[0], (String) row[1]);
			nameAndVersionList.add(nameAndVersion);
		}

		return nameAndVersionList;

	}

	public List<NameAndVersion> getAllDraftAndArchivedByIdMax(Set<Long> dsIdList) {

		List<NameAndVersion> fullList = new ArrayList<>();

		if (dsIdList.size() < 32000) {
			return getAllDraftAndArchivedById(dsIdList);
		}

		Iterable<List<Long>> splitSets = Iterables.partition(dsIdList, 32000);
		List<Set<Long>> listOfSets = new ArrayList<>();
		for (List<Long> list : splitSets) {
			Set<Long> tempSet = new HashSet<>();
			tempSet.addAll(list);
			listOfSets.add(tempSet);
		}

		for (Set<Long> set : listOfSets) {
			fullList.addAll(getAllDraftAndArchivedById(set));
		}

		return fullList;
	}

	/**
	 * @inheritDoc
	 */
	public List<NameAndVersion> getNameVersionByIdList(Set<Long> dsIdList) {

		if (dsIdList == null || dsIdList.isEmpty()) {
			return new ArrayList<NameAndVersion>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.where(root.get("id").in(dsIdList));
		query.multiselect(root.get(CoreConstants.NAME), root.get(CoreConstants.VERSION)).distinct(true);

		List<Object[]> rows = createQuery(query).getResultList();
		List<NameAndVersion> nameAndVersionList = new ArrayList<NameAndVersion>();

		for (Object[] row : rows) {
			NameAndVersion nameAndVersion = new NameAndVersion((String) row[0], (String) row[1]);
			nameAndVersionList.add(nameAndVersion);
		}

		return nameAndVersionList;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getNextMajorDataElementVersion(String shortName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Double> query = cb.createQuery(Double.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.select(cb.max(root.get(CoreConstants.VERSION).as(Double.class)));
		query.where(cb.equal(root.get(CoreConstants.NAME), shortName));

		String highVersion = getUniqueResult(query).toString();
		String majorVersion = highVersion.substring(0, highVersion.indexOf("."));
		int versionInt = Integer.parseInt(majorVersion);
		versionInt = versionInt + 1;
		String newVersion = String.valueOf(versionInt).concat(".0");

		// Plus one for new version.
		return newVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNextMinorDataElementVersion(String shortName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Double> query = cb.createQuery(Double.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.select(cb.max(root.get(CoreConstants.VERSION).as(Double.class)));
		query.where(cb.equal(root.get(CoreConstants.NAME), shortName));

		String highVersion = getUniqueResult(query).toString();
		String minorVersion = highVersion.substring(highVersion.indexOf(".") + 1, highVersion.length());
		int versionInt = Integer.parseInt(minorVersion);
		versionInt = versionInt + 1;
		String newVersion = highVersion.substring(0, highVersion.indexOf(".") + 1).concat(String.valueOf(versionInt));

		// Plus one for new version.
		return newVersion;
	}


	/**
	 * This method returns a map of data element name to map of permissible values for the given data element name set.
	 * 
	 * @param deNames - Set of data element names
	 * @return a map of data element name to map of permissible value to permissible value object.
	 */
	public Map<String, Map<String, ValueRange>> getDEValueRangeMap(Set<String> deNames) {

		if (deNames == null || deNames.isEmpty()) {
			return null;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		Join<StructuralDataElement, ValueRange> vrJoin = root.join("valueRangeList", JoinType.LEFT);
		Join<ValueRange, SchemaPv> spJoin = vrJoin.join("schemaPvs", JoinType.LEFT);
		Join<SchemaPv, Schema> sJoin = spJoin.join("schema", JoinType.LEFT);

		List<Selection<?>> selections = new ArrayList<>();
		selections.add(root.get("name"));
		selections.add(vrJoin.get("valueRange"));
		selections.add(vrJoin.get("outputCode"));
		selections.add(vrJoin.get("description"));
		selections.add(sJoin.get("name"));
		selections.add(spJoin.get("id"));
		selections.add(spJoin.get("permissibleValue"));
		selections.add(spJoin.get("schemaDeId"));
		query.multiselect(selections).distinct(true);

		// Add latest version criteria
		Subquery<Long> subquery = query.subquery(Long.class);
		Root<StructuralDataElement> subroot = subquery.from(StructuralDataElement.class);
		subquery.select(cb.max(subroot.get("id")));
		subquery.where(cb.equal(subroot.get("name"), root.get("name")));
		query.where(cb.and(root.get("name").in(deNames), cb.equal(root.get("id"), subquery)));

		// List<Object[]> rows = createQuery(query).getResultList();
		TypedQuery<Object[]> q = createQuery(query);
		List<Object[]> rows = q.getResultList();
		Map<String, Map<String, ValueRange>> dePVMap = new HashMap<String, Map<String, ValueRange>>();

		for (Object[] row : rows) {
			String deName = (String) row[0];

			if (!dePVMap.containsKey(deName)) {
				Map<String, ValueRange> pvMap = new HashMap<String, ValueRange>();
				dePVMap.put(deName, pvMap);
			}

			ValueRange vr = null;
			String pv = (String) row[1];

			if (dePVMap.get(deName).containsKey(pv)) {
				vr = dePVMap.get(deName).get(pv);
			} else {
				vr = new ValueRange();
				vr.setValueRange(pv);
				vr.setOutputCode((Integer) row[2]);
				vr.setDescription((String) row[3]);
			}

			SchemaPv sp = new SchemaPv();
			sp.setSchema(new Schema());
			sp.getSchema().setName((String) row[4]);
			sp.setId((Long) row[5]);
			sp.setPermissibleValue((String) row[6]);
			sp.setSchemaDeId((String) row[7]);
			vr.getSchemaPvs().add(sp);

			// add the permissible value description as a schema PV
			// this is for use in query tool so we can map data in query tool to its applicable
			// permissible value description.
			SchemaPv descriptionSchema = new SchemaPv();
			descriptionSchema.setSchema(new Schema());
			descriptionSchema.getSchema().setName("Permissible Value Description");
			descriptionSchema.setPermissibleValue((String) row[3]);
			vr.getSchemaPvs().add(descriptionSchema);

			dePVMap.get(deName).put(pv, vr);
		}

		return dePVMap;
	}

	@Override
	public List<String> getDataElementNamesByType(DataType dataType) {

		if (dataType == null) {
			return new ArrayList<String>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.select(root.get(CoreConstants.NAME)).distinct(true);
		query.where(cb.equal(root.get("type"), dataType)).distinct(true);

		return createQuery(query).getResultList();
	}

	public List<StructuralDataElement> getAllByName(String name) {

		List<StructuralDataElement> result = this.findByShortName(name);
		return result;
	}

	@Override
	public StructuralDataElement getOriginalDataElementByName(String name) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StructuralDataElement> query = cb.createQuery(StructuralDataElement.class);
		Root<StructuralDataElement> root = query.from(StructuralDataElement.class);

		query.where(
				cb.and(cb.equal(root.get(CoreConstants.NAME), name), cb.equal(root.get(CoreConstants.VERSION), "1.0")));

		StructuralDataElement dataElement = getUniqueResult(query);
		return dataElement;
	}
}
