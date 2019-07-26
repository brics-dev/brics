package gov.nih.tbi.dictionary.dao.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.util.DataElementFilter;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.dao.DataElementSparqlDao;
import gov.nih.tbi.dictionary.dao.StructuralDataElementDao;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.DiscrepantDataException;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.MissingDataException;
import gov.nih.tbi.dictionary.model.MissingSemanticObjectException;
import gov.nih.tbi.dictionary.model.MissingStructuralObjectException;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;

/**
 * Hibernate implementation of the Data Element Dao.
 * 
 * @author Andrew Johnson
 * @author Francis Chen
 * @author Michael Valeiras
 */

@Repository
@Transactional("dictionaryTransactionManager")
public class DataElementDaoImpl<T, PK extends Serializable> extends GenericDictDaoImpl<DataElement, Long> implements DataElementDao {

	static Logger logger = Logger.getLogger(DataElementDaoImpl.class);

	@Autowired
	DataElementSparqlDao dataElementSparqlDao;

	@Autowired
	StructuralDataElementDao structuralDao;

	@Autowired
	public DataElementDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(DataElement.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public DataElement getLatestByName(String dataElementName) {

		String dataElementNameSQL =
				dataElementName.replace(CoreConstants.UNDERSCORE, CoreConstants.SQL_ESCAPED_UNDERSCORE);
		SemanticDataElement semanticElement = dataElementSparqlDao.getLatestByName(dataElementName);

		StructuralDataElement structuralElement = null;
		// using the version from the semantic data because it contains a flag for the latest data element
		if (semanticElement != null) {
			if (semanticElement.getVersion() != null) {
				structuralElement = structuralDao.getByNameAndVersion(dataElementNameSQL, semanticElement.getVersion());
			}
		} else {
			// This will be null but its neccessary for the lower logic
			structuralElement = structuralDao.getLatestByName(dataElementName);
		}
		// MG: It's very important for the Data Element Import functionality to have this conditional. By returning
		// null, this method lets the import know that the importing Data Element is not overwriting a preexisting Data
		// Element. Please notify Matt Green if needs to be removed.
		if (structuralElement == null && semanticElement == null) {
			return null;
		}
		verifyCompositeDataElement(semanticElement, structuralElement);

		return new DataElement(structuralElement, semanticElement);
	}
	
	/**
	 * @inheritDoc
	 */
	public DataElement getLatestByNameCaseInsensitive(String dataElementName) {

		SemanticDataElement semanticElement = dataElementSparqlDao.getLatestByNameInsens(dataElementName);
		
		StructuralDataElement structuralElement = null;
		// using the version from the semantic data because it contains a flag for the latest data element
		if (semanticElement != null) {
			if (semanticElement.getVersion() != null) {
				structuralElement = structuralDao.getByNameAndVersion(semanticElement.getName(), semanticElement.getVersion());
			}
		} else {
			// This will be null but its neccessary for the lower logic
			structuralElement = structuralDao.getLatestByName(dataElementName);
		}
		
		// MG: It's very important for the Data Element Import functionality to have this conditional. By returning
		// null, this method lets the import know that the importing Data Element is not overwriting a preexisting Data
		// Element. Please notify Matt Green if needs to be removed.
		if (structuralElement == null && semanticElement == null) {
			return null;
		}
		verifyCompositeDataElement(semanticElement, structuralElement);

		return new DataElement(structuralElement, semanticElement);
	}


	public List<DataElement> getByNameAndVersions(List<NameAndVersion> nameAndVersions) {

		List<StructuralDataElement> structuralDe = structuralDao.getByNameAndVersions(nameAndVersions);
		return combineIntoComposite(structuralDe);
	}

	/**
	 * @inheritDoc
	 */
	public DataElement getByNameAndVersion(String dataElementName, String version) {

		String dataElementNameSQL =
				dataElementName.replace(CoreConstants.UNDERSCORE, CoreConstants.SQL_ESCAPED_UNDERSCORE);
		StructuralDataElement structuralElement = structuralDao.getByNameAndVersion(dataElementNameSQL, version);
		SemanticDataElement semanticElement = dataElementSparqlDao.getByNameAndVersion(dataElementName, version);

		verifyCompositeDataElement(semanticElement, structuralElement);

		return new DataElement(semanticElement, structuralElement);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DataElement> getLatestByNameList(Set<String> dataElementNames) {

		List<StructuralDataElement> structuralList = structuralDao.getLatestByNameList(dataElementNames);
		Map<String, SemanticDataElement> nameSemanticDeMap = dataElementSparqlDao.getLatestByNameList(dataElementNames);
		return combineIntoComposite(structuralList, nameSemanticDeMap);
	}

	/**
	 * This method takes a list of names and gets the latest version back by the semantic graph adds them to the name
	 * and version list. and makes a dao call for that data element.
	 */
	public List<DataElement> getBasicLatestByNameList(Set<String> dataElementNames) {

		// get the latest data element flagged by the graph
		Map<String, SemanticDataElement> nameSemanticDeMap =
				dataElementSparqlDao.getBasicLatestByNameList(dataElementNames);
		List<NameAndVersion> SemanticDataElementList = new ArrayList<NameAndVersion>();

		// a null check but it is prolly unneeded
		if (nameSemanticDeMap != null && !nameSemanticDeMap.isEmpty()) {
			// get the name and version and add it to the list
			for (Map.Entry<String, SemanticDataElement> dataElement : nameSemanticDeMap.entrySet()) {
				SemanticDataElementList.add(new NameAndVersion(dataElement.getValue().getName(), dataElement.getValue()
						.getVersion()));
			}
		} else {
			return new ArrayList<DataElement>();
		}
		// dao call for the name and version of the DE
		List<StructuralDataElement> structuralList = structuralDao.getByNameAndVersions(SemanticDataElementList);

		return combineIntoComposite(structuralList, nameSemanticDeMap);
	}

	public Map<String, DataElement> getLatestByNameListIntoMap(Set<String> dataElementNames) {

		List<DataElement> deList = getLatestByNameList(dataElementNames);
		Map<String, DataElement> deMap = new HashMap<String, DataElement>();
		if (deList != null) {
			for (DataElement de : deList) {
				deMap.put(de.getName(), de);
			}
		}

		return deMap;
	}

	/**
	 * @inheritDoc
	 */
	public List<DataElement> search(Set<Long> ids, Category category, DataElementStatus status, String searchKey,
			PaginationData pageData, DataElementFilter dataElementFilter) {

		// List<SemanticDataElement> sDEList = dataElementSparqlDao.search(selectedFacets, searchKeywords, modifiedDate,
		// pageData)
		return new ArrayList<DataElement>();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DataElement> getByIdList(List<Long> ids) {

		List<StructuralDataElement> structuralList = structuralDao.getByIdList(ids);
		return combineIntoComposite(structuralList);
	}

	/**
	 * @inheritDoc
	 */
	public Long getStatusCount(DataElementStatus status, Category category) {

		return structuralDao.getStatusCount(status, category);
	}

	/**
	 * @inheritDoc
	 */
	public List<DataElement> listByStatus(DataElementStatus status) {

		List<StructuralDataElement> structuralList = structuralDao.listByStatus(status);
		return combineIntoComposite(structuralList);
	}


	@Override
	public List<DataElement> listByStatuses(Set<DataElementStatus> statuses) {
		List<StructuralDataElement> structuralList = structuralDao.listByStatuses(statuses);
		Map<String, SemanticDataElement> semanticMap = dataElementSparqlDao.listByStatuses(statuses);
		return combineIntoComposite(structuralList, semanticMap);
	}

	/**
	 * @inheritDoc
	 */
	public List<DataElement> listLatestByStatus(DataElementStatus status) {

		/**
		 * This calls the SQL twice. This is bad. However, it's required for situations where multiple versions of
		 * elements are published.
		 */
		List<StructuralDataElement> structuralList = structuralDao.listByStatus(status);
		Set<String> deNames = new HashSet<String>();

		for (StructuralDataElement sde : structuralList) {
			deNames.add(sde.getName());
		}

		return getLatestByNameList(deNames);
	}

	/**
	 * @inheritDoc
	 */
	public Map<Long, DataElement> getByMapElementIds(Set<Long> ids) {

		Map<Long, DataElement> dataElementMap = new HashMap<Long, DataElement>();
		Map<Long, StructuralDataElement> structuralMap = structuralDao.getByMapElementIds(ids);
		List<DataElement> compositeDes = combineIntoComposite(structuralMap.values());

		Map<String, DataElement> nameToCompositeDeMap = new HashMap<String, DataElement>();

		for (DataElement compositeDe : compositeDes) {
			nameToCompositeDeMap.put(compositeDe.getName(), compositeDe);
		}

		for (Entry<Long, StructuralDataElement> mapElementStructuralEntry : structuralMap.entrySet()) {
			Long mapElementIdLong = mapElementStructuralEntry.getKey();
			StructuralDataElement stucturalDe = mapElementStructuralEntry.getValue();
			DataElement compositeDe = nameToCompositeDeMap.get(stucturalDe.getName());

			if (compositeDe != null) {
				dataElementMap.put(mapElementIdLong, compositeDe);
			}
		}

		return dataElementMap;
	}

	public DataElement getByMapElementId(Long id) {

		StructuralDataElement structuralElement = structuralDao.getByMapElementId(id);
		return combineIntoComposite(structuralElement);
	}

	/**
	 * override the generic DAO call so we can lazy load all children associated with DE
	 */
	public DataElement get(Long id) {

		StructuralDataElement structuralElement = structuralDao.get(id);
		return combineIntoComposite(structuralElement);
	}

	/*
	 * override the generic DAO call so we can lazy load all children associated with DE
	 */
	@Override
	public List<DataElement> getAll() {

		List<StructuralDataElement> structuralList = structuralDao.getAll();
		Map<String, SemanticDataElement> nameDeMap = dataElementSparqlDao.getAllInNameMap();
		return combineIntoComposite(structuralList, nameDeMap);
	}

	/**
	 * Uses the name of the data element to query for the semantic data element, then uses both objects to construct the
	 * composite object.
	 * 
	 * @param structuralDes
	 * @return
	 */
	public DataElement combineIntoComposite(StructuralDataElement structuralDe) {

		SemanticDataElement semanticDe =
				dataElementSparqlDao.getByNameAndVersion(structuralDe.getName(), structuralDe.getVersion());
		return new DataElement(semanticDe, structuralDe);
	}

	/**
	 * Uses the names of the data elements in the list to query for the list of semantic data elements, then uses both
	 * lists to construct a list of composite objects.
	 * 
	 * @param structuralDes
	 * @return
	 */
	public List<DataElement> combineIntoComposite(Collection<StructuralDataElement> structuralDes) {

		List<NameAndVersion> deNames = new ArrayList<NameAndVersion>();

		if (structuralDes == null || structuralDes.isEmpty()) {
			return new ArrayList<DataElement>();
		}

		for (StructuralDataElement structuralDe : structuralDes) {
			deNames.add(new NameAndVersion(structuralDe.getName(), structuralDe.getVersion()));
		}

		Map<String, SemanticDataElement> nameToSemanticDeMap = dataElementSparqlDao.getByNameAndVersionsMap(deNames);

		return combineIntoComposite(structuralDes, nameToSemanticDeMap);
	}

	/**
	 * Uses a list of structural objects and a map of data element names to semantic objects to construct a list of the
	 * resulting composite objects.
	 * 
	 * @param structuralDes
	 * @param nameToSemanticDeMap
	 * @return
	 */
	public List<DataElement> combineIntoComposite(Collection<StructuralDataElement> structuralDes,
			Map<String, SemanticDataElement> nameToSemanticDeMap) {

		List<DataElement> dataElements = new ArrayList<DataElement>();
		for (StructuralDataElement structuralDe : structuralDes) {
			SemanticDataElement semanticDe = nameToSemanticDeMap.get(structuralDe.getNameAndVersion());

			verifyCompositeDataElement(semanticDe, structuralDe);
			dataElements.add(new DataElement(semanticDe, structuralDe));
		}

		return dataElements;
	}

	public DataElement save(DataElement dataElement) {

		StructuralDataElement structuralDataElement = dataElement.getStructuralObject();
		SemanticDataElement semanticDataElement = dataElement.getSemanticObject();

		if (structuralDataElement.getId() != null) {
			StructuralDataElement oldElement = structuralDao.get(structuralDataElement.getId());
			if (oldElement != null) {
				String oldName = oldElement.getName();
				String oldVersion = oldElement.getVersion();
				semanticDataElement = dataElementSparqlDao.saveOverwrite(semanticDataElement, oldName, oldVersion);
			} else {
				throw new NullPointerException(
						"Something went wrong, ID should be null for a data element that doesn't exist alreadty in the database.");
			}
		} else
		// means this is a create
		{
			semanticDataElement = dataElementSparqlDao.save(semanticDataElement);
		}

		structuralDataElement = structuralDao.save(structuralDataElement);

		return new DataElement(semanticDataElement, structuralDataElement);
	}

	/**
	 * The remove method that will be responsible for Deleting a Data Elements including Structural and Semantic Data
	 * Elements
	 */
	@Override
	public void remove(DataElement dataElement) {

		StructuralDataElement structuralDataElement = dataElement.getStructuralObject();
		SemanticDataElement semanticDataElement = dataElement.getSemanticObject();

		structuralDao.remove(structuralDataElement.getId());
		dataElementSparqlDao.remove(semanticDataElement);
		// /Delete Structural and Semantic DEs here

	}

	private void verifyCompositeDataElement(SemanticDataElement semanticDE, StructuralDataElement structDE) {

		if (semanticDE == null && structDE == null) {
			String msg = "Semantic and Structural parts are both null! An unknown error has occured.";

			throw new MissingDataException(msg);
		}

		if (semanticDE == null) {

			String structName = structDE.getName();
			String msg = "Semantic part missing from data element: " + structName;

			throw new MissingSemanticObjectException(msg);
		}

		if (structDE == null) {

			String semanticName = semanticDE.getName();
			String msg = "Structural part missing from data element: " + semanticName;

			throw new MissingStructuralObjectException(msg);
		}

		String structName = structDE.getName();
		String semanticName = semanticDE.getName();

		if (!structName.equals(semanticName)) {

			String msg = "Structural and Semantic parts do not match! Semantic: "
					+ semanticName + ", Structural: " + structName;

			throw new DiscrepantDataException(msg);
		}
	}

	/**
	 * @inheritDoc
	 */
	public List<DataElement> searchDetailed(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchKeywords,
			boolean exactMatch, PaginationData pageData, boolean onlyOwned) {

		List<SemanticDataElement> semanticDataElements =
				dataElementSparqlDao.searchDetailed(facets, searchKeywords, exactMatch, pageData, onlyOwned);

		return combineWithSQL(semanticDataElements);
	}


	/**
	 * @inheritDoc
	 */
	public List<DataElement> searchCount(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchKeywords,
			boolean exactMatch, PaginationData pageData, boolean onlyOwned) {

		List<SemanticDataElement> semanticDataElements =
				dataElementSparqlDao.searchDetailed(facets, searchKeywords, exactMatch, pageData, onlyOwned);

		return combineWithSQL(semanticDataElements);
	}


	private List<DataElement> combineWithSQL(List<SemanticDataElement> semanticList) {

		// Do not process empty and null lists
		if (semanticList == null) {
			return null;
		}
		if (semanticList.isEmpty()) {
			return new ArrayList<DataElement>();
		}

		List<NameAndVersion> nameAndVersions = new ArrayList<NameAndVersion>();
		for (SemanticDataElement de : semanticList) {
			nameAndVersions.add(new NameAndVersion(de.getName(), de.getVersion()));
		}

		Map<String, StructuralDataElement> shortNameVersionFsMap = new HashMap<String, StructuralDataElement>();

		// we were running into memories issues when trying to get all of the data elements with name and version
		// chunking this up so we are only getting a subset of it at a time and putting it into the hashmap
		int startIndex = 0;
		int endIndex = Math.min(CoreConstants.DATA_ELEMENT_SEARCH_CHUNK_SIZE, nameAndVersions.size());

		while (startIndex < nameAndVersions.size()) {
			shortNameVersionFsMap.putAll(structuralDao.getByNameAndVersionsMap(nameAndVersions.subList(startIndex,
					endIndex)));
			startIndex += CoreConstants.DATA_ELEMENT_SEARCH_CHUNK_SIZE;
			endIndex = Math.min(endIndex += CoreConstants.DATA_ELEMENT_SEARCH_CHUNK_SIZE, nameAndVersions.size());
		}

		return combineRDFListWithSQLMap(semanticList, shortNameVersionFsMap);

	}

	private List<DataElement> combineRDFListWithSQLMap(List<SemanticDataElement> semanticList,
			Map<String, StructuralDataElement> shortNameVersionDEMap) {

		List<DataElement> deList = new ArrayList<DataElement>();

		for (SemanticDataElement semDE : semanticList) {
			String nameAndVersion = semDE.getShortNameAndVersion();
			StructuralDataElement strucDe = shortNameVersionDEMap.get(nameAndVersion);

			verifyCompositeDataElement(semDE, strucDe);
			deList.add(new DataElement(strucDe, semDE));
		}

		return deList;
	}

	/**
	 * @inheritDoc
	 */
	public List<DataElement> getAllByName(String name) {

		List<StructuralDataElement> structuralList = structuralDao.findByShortName(name);
		return combineIntoComposite(structuralList);
	}


	public List<DataElement> getDataElementsForStatusUpdate() {

		List<SemanticDataElement> semanticDataElements = dataElementSparqlDao.getAllWithUntilDate();
		semanticDataElements.addAll(dataElementSparqlDao.getAllWithoutUntilDate());

		return combineWithSQL(semanticDataElements);
	}
	

	@Override
	public Set<Long> getDEIdsFormListOfFSIds(Set<Long> fSIds) {
		if (fSIds.isEmpty() || fSIds == null) {
			return new HashSet<Long>();
		}
		
		String hql = "select de.id from StructuralFormStructure ds "
					+"JOIN ds.repeatableGroups rg "
	                +"JOIN rg.mapElements me "
	                +"JOIN me.dataElement de "
	                +"where ds.id in (:ids) "
	                +"and de.status.id != :statusIdPub "
	                +"and de.status.id != :statusIdAP";
		
		Query query = getSession().createQuery(hql);
		query.setParameter("ids", fSIds);
		query.setParameter("statusIdPub", DataElementStatus.PUBLISHED.getId());
		query.setParameter("statusIdAP", DataElementStatus.AWAITING.getId());
		
		List<Long> list = query.getResultList();	
		Set<Long> dataElementIds = new HashSet<Long>(list);
			
		return dataElementIds;
	}
	
	public String getDEShortNameFromVirtuosoIgnoreCases(String deName) {
		String result = null;
		String deNameInVirtuoso = dataElementSparqlDao.getSemeticDEShortNameByNameIgnoreCases(deName);
		List<StructuralDataElement> structureDEList = structuralDao.getAllByName(deName);
		if(deNameInVirtuoso != null && !deNameInVirtuoso.isEmpty()
				&& structureDEList != null && structureDEList.size() > 0) {
			result = deNameInVirtuoso;
		} 
		return result;
	}

}
