package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.dao.DataElementSparqlDao;
import gov.nih.tbi.dictionary.dao.FormStructureDao;
import gov.nih.tbi.dictionary.dao.FormStructureSparqlDao;
import gov.nih.tbi.dictionary.dao.FormStructureSqlDao;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.MissingSemanticObjectException;
import gov.nih.tbi.dictionary.model.MissingStructuralObjectException;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;

@Transactional("dictionaryTransactionManager")
@Repository
public class FormStructureDaoImpl extends GenericDictDaoImpl<FormStructure, Long> implements FormStructureDao {

	static Logger logger = Logger.getLogger(FormStructureDaoImpl.class);

	@Autowired
	public FormStructureDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(FormStructure.class, sessionFactory);
		this.formStructureSqlDao = new FormStructureSqlDaoImpl(sessionFactory);
	}

	@Autowired
	DataElementDao dataElementDao;

	@Autowired
	DataElementSparqlDao dataElementSparqlDao;

	@Autowired
	FormStructureSqlDao formStructureSqlDao;

	@Autowired
	FormStructureSparqlDao formStructureSparqlDao;

	@Override
	public FormStructure getById(long id) {

		StructuralFormStructure fs = formStructureSqlDao.get(id);

		if (fs == null) {
			return null;
		}

		String shortName = fs.getShortName();
		String version = fs.getVersion();

		SemanticFormStructure rdfFs = formStructureSparqlDao.get(shortName, version);

		if (rdfFs == null) {
			return null;
		}

		Map<String, DataElement> dataElements = buildDataElementList(fs);

		return new FormStructure(fs, rdfFs, dataElements);
	}

	@Override
	public FormStructure get(String shortName, String version) {

		StructuralFormStructure fs = this.formStructureSqlDao.get(shortName, version);
		SemanticFormStructure rdfFs = formStructureSparqlDao.get(shortName, version);

		verifyCompositeFormStructure(rdfFs, fs);

		Map<String, DataElement> dataElements = buildDataElementList(fs);

		return new FormStructure(fs, rdfFs, dataElements);
	}

	@Override
	public FormStructure getLatestVersionByShortName(String shortName) {
		StructuralFormStructure fs = this.formStructureSqlDao.getLatestVersionByShortName(shortName);
		SemanticFormStructure rdfFs = formStructureSparqlDao.getLatest(shortName);

		if (fs == null && rdfFs == null) {
			return null;
		}

		Map<String, DataElement> dataElements = buildDataElementList(fs);

		verifyCompositeFormStructure(rdfFs, fs);

		return new FormStructure(fs, rdfFs, dataElements);
	}

	public Map<Long, FormStructure> getPublishedAndArchivedIntoMap() {
		Set<StatusType> fsStatuses = new HashSet<StatusType>();

		fsStatuses.add(StatusType.PUBLISHED);
		fsStatuses.add(StatusType.ARCHIVED);

		Set<DataElementStatus> deStatuses = new HashSet<DataElementStatus>();
		deStatuses.add(DataElementStatus.PUBLISHED);
		deStatuses.add(DataElementStatus.RETIRED);
		deStatuses.add(DataElementStatus.DEPRECATED);

		long startTime = System.currentTimeMillis();
		List<SemanticFormStructure> rdfFsList = this.formStructureSparqlDao.getByStatuses(fsStatuses);
		Map<String, SemanticFormStructure> rdfFsMap = new HashMap<String, SemanticFormStructure>();
		long endTime = System.currentTimeMillis();
		log.info("Finished RDF Get: " + (endTime - startTime) + "ms");

		for (SemanticFormStructure form : rdfFsList) {
			rdfFsMap.put(form.getShortNameAndVersion(), form);
		}

		startTime = System.currentTimeMillis();
		List<StructuralFormStructure> sqlFsList = this.formStructureSqlDao.listDataStructuresByStatuses(fsStatuses);
		endTime = System.currentTimeMillis();
		log.info("Finished SQL Get: " + (endTime - startTime) + "ms");

		Map<Long, StructuralFormStructure> sqlFsMap = new HashMap<Long, StructuralFormStructure>();
		for (StructuralFormStructure form : sqlFsList) {
			sqlFsMap.put(form.getId(), form);
		}

		startTime = System.currentTimeMillis();
		List<DataElement> des = dataElementDao.listByStatuses(deStatuses);
		Map<NameAndVersion, DataElement> deMap = new HashMap<NameAndVersion, DataElement>();
		endTime = System.currentTimeMillis();
		log.info("Finished DE Get: " + (endTime - startTime) + "ms");


		for (DataElement de : des) {
			deMap.put(new NameAndVersion(de.getName(), de.getVersion()), de);
		}

		startTime = System.currentTimeMillis();
		Map<Long, FormStructure> fsMap = combinePartsIntoMap(rdfFsMap, sqlFsMap, deMap);
		endTime = System.currentTimeMillis();
		log.info("Finished combine: " + (endTime - startTime) + "ms");
		return fsMap;
	}

	private Map<Long, FormStructure> combinePartsIntoMap(Map<String, SemanticFormStructure> rdfFsMap,
			Map<Long, StructuralFormStructure> sqlFsMap, Map<NameAndVersion, DataElement> deMap) {
		Map<Long, FormStructure> fsMap = new HashMap<Long, FormStructure>();
		for (Entry<Long, StructuralFormStructure> sqlEntry : sqlFsMap.entrySet()) {
			Long fsId = sqlEntry.getKey();
			StructuralFormStructure sqlFs = sqlEntry.getValue();
			String shortNameAndVersion = sqlFs.getShortNameAndVersion();
			SemanticFormStructure rdfFs = rdfFsMap.get(shortNameAndVersion);

			if (rdfFs != null) {
				Map<String, DataElement> dataElements = new HashMap<String, DataElement>();

				List<NameAndVersion> deNameVersionList = new ArrayList<NameAndVersion>();
				for (MapElement me : sqlFs.getDataElements()) {
					deNameVersionList.add(new NameAndVersion(me.getStructuralDataElement().getName(),
							me.getStructuralDataElement().getVersion()));
				}

				for (MapElement me : sqlFs.getDataElements()) {
					String name = me.getStructuralDataElement().getName();
					String version = me.getStructuralDataElement().getVersion();
					DataElement currentDe = deMap.get(new NameAndVersion(name, version));
					dataElements.put(name, currentDe);
				}

				fsMap.put(fsId, new FormStructure(sqlFs, rdfFs, dataElements));
			}
		}

		return fsMap;
	}

	@Override
	public Map<Long, FormStructure> getAllIntoMap() {

		Map<Long, StructuralFormStructure> sqlFsMap = this.formStructureSqlDao.getAllIntoMap();
		Map<String, SemanticFormStructure> rdfFsMap = formStructureSparqlDao.getAllIntoShortNameVersionMap();

		List<DataElement> des = dataElementDao.getAll();
		Map<NameAndVersion, DataElement> deMap = new HashMap<NameAndVersion, DataElement>();

		for (DataElement de : des) {
			deMap.put(new NameAndVersion(de.getName(), de.getVersion()), de);
		}

		return combinePartsIntoMap(rdfFsMap, sqlFsMap, deMap);
	}

	@Override
	public List<FormStructure> getAllSortedById(List<Long> dsIdList, PaginationData pageData) {

		List<StructuralFormStructure> sqlData = this.formStructureSqlDao.getAllSortedById(dsIdList, pageData);
		if (sqlData == null) {
			return null;
		}
		// if not null then create new form structure objects
		List<FormStructure> toReturn = new ArrayList<FormStructure>();
		for (StructuralFormStructure sqlObject : sqlData) {
			toReturn.add(new FormStructure(sqlObject));
		}
		return toReturn;
	}

	@Override
	public List<FormStructure> getAllById(List<Long> dsIdList) {

		List<StructuralFormStructure> sqlFsList = this.formStructureSqlDao.getAllById(dsIdList);
		return combineWithRDF(sqlFsList);
	}

	@Override
	public List<FormStructure> listDataStructuresByStatus(StatusType status) {

		List<StructuralFormStructure> sqlFsList = this.formStructureSqlDao.listDataStructuresByStatus(status);

		// If there are no SQL data structures of the given status, return an empty list.
		if (sqlFsList.isEmpty()) {
			return new ArrayList<FormStructure>();
		}
		return combineWithRDF(sqlFsList);
	}

	private List<FormStructure> combineWithRDF(List<StructuralFormStructure> sqlFsList) {

		if (sqlFsList == null) {
			return null;
		}

		if (sqlFsList.isEmpty()) {
			return new ArrayList<FormStructure>();
		}

		List<NameAndVersion> nameAndVersions = new ArrayList<NameAndVersion>();
		for (StructuralFormStructure fs : sqlFsList) {
			nameAndVersions.add(new NameAndVersion(fs.getShortName(), fs.getVersion()));
		}

		Map<String, SemanticFormStructure> shortNameVersionFsMap =
				formStructureSparqlDao.getShortNameAndVersionsMap(nameAndVersions);

		return combineSQLListWithRDFMap(sqlFsList, shortNameVersionFsMap);
	}

	private List<FormStructure> combineWithOnlyLatestRDF(List<StructuralFormStructure> sqlFsList) {

		if (sqlFsList == null) {
			return null;
		}

		if (sqlFsList.isEmpty()) {
			return new ArrayList<FormStructure>();
		}
		List<String> names = new ArrayList<String>();

		for (StructuralFormStructure fs : sqlFsList) {
			names.add(fs.getShortName());
		}
		long startTime = System.currentTimeMillis();
		List<SemanticFormStructure> latestSemanticStructures = formStructureSparqlDao.getLatestByNames(names);
		long endTime = System.currentTimeMillis();
		
		logger.info("Getting getting attached forms SPARQL time: " + (endTime - startTime) + "ms");
		
		return combineLatestSemanticWithSQL(latestSemanticStructures, sqlFsList);
	}

	private List<FormStructure> combineLatestSemanticWithSQL(List<SemanticFormStructure> latestSemanticStructures,
			List<StructuralFormStructure> sqlFsList) {
		List<FormStructure> formStructures = new ArrayList<FormStructure>();
		Map<NameAndVersion, StructuralFormStructure> sqlFsMap = new HashMap<NameAndVersion, StructuralFormStructure>();

		for (StructuralFormStructure sqlFs : sqlFsList) {
			sqlFsMap.put(new NameAndVersion(sqlFs.getShortName(), sqlFs.getVersion()), sqlFs);
		}

		for (SemanticFormStructure currentSemanticFs : latestSemanticStructures) {
			NameAndVersion currentNameAndVersion =
					new NameAndVersion(currentSemanticFs.getShortName(), currentSemanticFs.getVersion());
			StructuralFormStructure currentSqlFs = sqlFsMap.get(currentNameAndVersion);
			verifyCompositeFormStructure(currentSemanticFs, currentSqlFs);
			formStructures.add(new FormStructure(currentSqlFs, currentSemanticFs));
		}

		return formStructures;
	}

	private List<FormStructure> combineWithSQL(List<SemanticFormStructure> semanticList) {

		// Do not process empty and null lists
		if (semanticList == null) {
			return null;
		}
		if (semanticList.isEmpty()) {
			return new ArrayList<FormStructure>();
		}

		List<NameAndVersion> nameAndVersions = new ArrayList<NameAndVersion>();
		for (SemanticFormStructure fs : semanticList) {
			nameAndVersions.add(new NameAndVersion(fs.getShortName(), fs.getVersion()));
		}

		Map<String, StructuralFormStructure> shortNameVersionFsMap =
				formStructureSqlDao.getShortNameAndVersionsMap(nameAndVersions);

		return combineRDFListWithSQLMap(semanticList, shortNameVersionFsMap);

	}

	private List<FormStructure> combineRDFListWithSQLMap(List<SemanticFormStructure> semanticList,
			Map<String, StructuralFormStructure> shortNameVersionFsMap) {

		List<FormStructure> fsList = new ArrayList<FormStructure>();

		for (SemanticFormStructure semFS : semanticList) {
			String shortNameVersion = semFS.getShortNameAndVersion();
			StructuralFormStructure strucFS = shortNameVersionFsMap.get(shortNameVersion);

			verifyCompositeFormStructure(semFS, strucFS);
			fsList.add(new FormStructure(strucFS, semFS));
		}

		return fsList;
	}

	private List<FormStructure> combineSQLListWithRDFMap(List<StructuralFormStructure> sqlFsList,
			Map<String, SemanticFormStructure> shortNameVersionFsMap) {

		List<FormStructure> fsList = new ArrayList<FormStructure>();

		for (StructuralFormStructure sqlFs : sqlFsList) {
			String shortNameVersion = sqlFs.getShortNameAndVersion();
			SemanticFormStructure rdfFs = shortNameVersionFsMap.get(shortNameVersion);

			verifyCompositeFormStructure(rdfFs, sqlFs);
			fsList.add(new FormStructure(sqlFs, rdfFs));
		}

		return fsList;
	}

	@Override
	public List<FormStructure> listDataStructures(Set<Long> ids, PaginationData pageData) {

		List<StructuralFormStructure> sqlFsList = this.formStructureSqlDao.listDataStructures(ids, pageData);
		return combineWithRDF(sqlFsList);
	}

	@Override
	public List<String> getNamesByIds(List<Long> formStructureIds) {
		return this.formStructureSqlDao.getNamesByIds(formStructureIds);
	}

	@Override
	public List<FormStructure> getAll() {

		List<StructuralFormStructure> sqlFsList = this.formStructureSqlDao.getAll();
		Map<String, SemanticFormStructure> rdfFsMap = formStructureSparqlDao.getAllIntoShortNameVersionMap();

		return combineSQLListWithRDFMap(sqlFsList, rdfFsMap);
	}

	@Override
	public FormStructure get(Long id) {

		StructuralFormStructure sqlFs = this.formStructureSqlDao.get(id);

		if (sqlFs == null) {
			throw new MissingStructuralObjectException("Structural part missing from form structure with id: " + id);
		}

		SemanticFormStructure rdfFs = formStructureSparqlDao.get(sqlFs.getShortName(), sqlFs.getVersion());
		Map<String, DataElement> dataElements = buildDataElementList(sqlFs);

		verifyCompositeFormStructure(rdfFs, sqlFs);

		return new FormStructure(sqlFs, rdfFs, dataElements);
	}

	@Override
	public boolean exists(Long id) {

		return this.formStructureSqlDao.exists(id);// && this.formStructureRdfDao.exists(id);
	}

	@Override
	public FormStructure save(FormStructure object) {

		SemanticFormStructure semanticFormStructure = object.getFormStructureRDFObject();
		StructuralFormStructure structuralFormStructure = object.getFormStructureSqlObject();

		if (structuralFormStructure.getId() != null) {
			StructuralFormStructure oldFs = formStructureSqlDao.get(structuralFormStructure.getId());
			if (oldFs != null) {
				String oldName = oldFs.getShortName();
				String oldVersion = oldFs.getVersion();
				semanticFormStructure =
						formStructureSparqlDao.saveOverwrite(semanticFormStructure, oldName, oldVersion);
			} else {
				throw new NullPointerException(
						"Something went wrong, ID should be null for a form structure that doesn't exist alreadty in the database.");
			}
		} else {
			semanticFormStructure = formStructureSparqlDao.save(semanticFormStructure);
		}

		structuralFormStructure = this.formStructureSqlDao.save(object.getFormStructureSqlObject());

		if (structuralFormStructure != null && semanticFormStructure != null) {
			return new FormStructure(structuralFormStructure, semanticFormStructure);
		}

		return null;
	}

	@Override
	public void remove(Long id) {

		StructuralFormStructure sqlFs = formStructureSqlDao.get(id);

		if (sqlFs != null) {
			this.formStructureSparqlDao.remove(sqlFs.getShortName(), sqlFs.getVersion());
		}

		this.formStructureSqlDao.remove(id);
	}

	@Override
	public void removeAll(List<FormStructure> removeList) {

		for (FormStructure fs : removeList) {
			this.remove(fs.getId());
		}
	}

	@Override
	public List<FormStructure> listByStatus(Set<Long> ids, long[] statusList) {

		List<StructuralFormStructure> sqlFsList = this.formStructureSqlDao.listByStatus(ids, statusList);
		return combineWithRDF(sqlFsList);
	}

	@Override
	public List<FormStructure> findByShortName(String shortName) {

		List<StructuralFormStructure> sqlFsList = this.formStructureSqlDao.findByShortName(shortName);
		return combineWithRDF(sqlFsList);
	}

	@Override
	public Long getStatusCount(StatusType status) {

		return this.formStructureSqlDao.getStatusCount(status);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FormStructure> search(Map<FormStructureFacet, Set<String>> selectedFacets, Set<String> searchTerms,
			boolean exactMatch, PaginationData pageData, boolean includeDEList, boolean onlyOwned) {

		List<SemanticFormStructure> semanticList =
				formStructureSparqlDao.search(selectedFacets, searchTerms, exactMatch, pageData, onlyOwned);
		List<FormStructure> shortList = combineWithSQL(semanticList);
		List<FormStructure> fullList = new ArrayList<FormStructure>();

		// If the user wants the composite data elements with their form structures we need to build a new list and add
		// each one.
		if (includeDEList) {
			if (shortList != null && !shortList.isEmpty()) {
				for (FormStructure fs : shortList) {
					fullList.add(new FormStructure(fs.getFormStructureSqlObject(), fs.getFormStructureRDFObject(),
							buildDataElementList(fs.getFormStructureSqlObject())));
				}
			}
			return fullList;
		}
		return shortList;
	}

	private void verifyCompositeFormStructure(SemanticFormStructure semanticFS, StructuralFormStructure structuralFS) {

		if (semanticFS == null && structuralFS == null) {
			throw new MissingSemanticObjectException(
					"Semantic and Structural parts are both null! An unknown error has occured.");
		}
		if (semanticFS == null) {
			throw new MissingSemanticObjectException(
					"Semantic part missing from form structure: " + structuralFS.getShortNameAndVersion());
		}
		if (structuralFS == null) {
			throw new MissingStructuralObjectException(
					"Structural part missing from form structure: " + semanticFS.getShortNameAndVersion());
		}
		if (!structuralFS.getShortNameAndVersion().equals(semanticFS.getShortNameAndVersion())) {
			throw new MissingSemanticObjectException("Structural and Semantic parts do not match! Semantic: "
					+ semanticFS.getShortNameAndVersion() + ", Strutural: " + structuralFS.getShortNameAndVersion());
		}
	}

	private Map<String, DataElement> buildDataElementList(StructuralFormStructure fs) {

		if (fs == null) {
			throw new IllegalArgumentException("Argument fs cannot be null");
		}

		List<NameAndVersion> nameAndVersions = new ArrayList<NameAndVersion>();
		for (MapElement me : fs.getDataElements()) {
			String name = me.getStructuralDataElement().getName();
			String version = me.getStructuralDataElement().getVersion();

			nameAndVersions.add(new NameAndVersion(name, version));
		}

		Map<String, SemanticDataElement> semanticMap =
				dataElementSparqlDao.getBasicByNameAndVersionsMap(nameAndVersions);
		Map<String, DataElement> returnList = new HashMap<String, DataElement>();

		for (MapElement me : fs.getDataElements()) {
			StructuralDataElement structuralElement = me.getStructuralDataElement();
			String nameAndVersionString = structuralElement.getNameAndVersion();
			returnList.put(nameAndVersionString,
					new DataElement(semanticMap.get(nameAndVersionString), structuralElement));
		}

		return returnList;
	}

	/**
	 * @inheritDoc
	 */
	public List<FormStructure> getAttachedDataStructure(String deName, String deVersion) {
		long startTime = System.currentTimeMillis();
		List<StructuralFormStructure> structuralForms = formStructureSqlDao.getAttachedDataStructure(deName, deVersion);
		long endTime = System.currentTimeMillis();
		logger.info("Getting getting attached forms SQL time: " + (endTime - startTime) + "ms");
		return combineWithOnlyLatestRDF(structuralForms);
	}

	@Override
	public List<FormStructure> getAttachedDataStructure(String deName, String deVersion, boolean isPublicData) {

		List<StructuralFormStructure> structuralForms =
				formStructureSqlDao.getAttachedDataStructure(deName, deVersion, isPublicData);
		return combineWithOnlyLatestRDF(structuralForms);
	}
}
