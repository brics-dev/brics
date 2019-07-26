
package gov.nih.tbi.commons.service.hibernate;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.dao.CountryDao;
import gov.nih.tbi.account.dao.StateDao;
import gov.nih.tbi.commons.dao.FileTypeDao;
import gov.nih.tbi.commons.model.FileClassification;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.Country;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.model.hibernate.State;
import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.dictionary.dao.CategoryDao;
import gov.nih.tbi.dictionary.dao.ClassificationDao;
import gov.nih.tbi.dictionary.dao.ClassificationDiseaseDao;
import gov.nih.tbi.dictionary.dao.DiseaseDao;
import gov.nih.tbi.dictionary.dao.DomainDao;
import gov.nih.tbi.dictionary.dao.DomainSubDomainDao;
import gov.nih.tbi.dictionary.dao.MeasuringTypeDao;
import gov.nih.tbi.dictionary.dao.MeasuringUnitDao;
import gov.nih.tbi.dictionary.dao.PopulationDao;
import gov.nih.tbi.dictionary.dao.SchemaDao;
import gov.nih.tbi.dictionary.dao.SubDomainDao;
import gov.nih.tbi.dictionary.dao.SubgroupDao;
import gov.nih.tbi.dictionary.dao.SubgroupDiseaseDao;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringType;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringUnit;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.ws.RestDictionaryProvider;
import gov.nih.tbi.repository.dao.FundingSourceDao;
import gov.nih.tbi.repository.dao.StudyTypeDao;
import gov.nih.tbi.repository.model.hibernate.FundingSource;
import gov.nih.tbi.repository.model.hibernate.StudyType;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * This is the implementation of StaticReferenceManager
 * 
 * @author Francis Chen
 */
@Service
@Scope("singleton")
public class StaticReferenceManagerImpl implements StaticReferenceManager {

	static Logger logger = Logger.getLogger(StaticReferenceManagerImpl.class);

	@Autowired
	ClassificationDao classificationDao;

	@Autowired
	SubgroupDao subgroupDao;

	@Autowired
	PopulationDao populationDao;

	@Autowired
	CategoryDao categoryDao;

	@Autowired
	DiseaseDao diseaseDao;

	@Autowired
	DomainDao domainDao;

	@Autowired
	SubDomainDao subDomainDao;

	@Autowired
	MeasuringUnitDao measuringUnitDao;

	@Autowired
	MeasuringTypeDao measuringTypeDao;

	@Autowired
	StateDao stateDao;

	@Autowired
	CountryDao countryDao;

	@Autowired
	FundingSourceDao fundingSourceDao;
	
	@Autowired
	StudyTypeDao studyTypeDao;

	@Autowired
	FileTypeDao fileTypeDao;

	@Autowired
	ClassificationDiseaseDao classificationDiseaseDao;

	@Autowired
	SubgroupDiseaseDao subgroupDiseaseDao;

	@Autowired
	DomainSubDomainDao domainSubDomainDao;

	@Autowired
	ModulesConstants modulesConstants;

	@Autowired
	DictionaryToolManager dictionaryToolManager;

	@Autowired
	RoleHierarchy roleHierarchy;

	@Autowired
	SchemaDao schemaDao;

	private static List<Classification> userClassificationList;
	private static List<Classification> fullClassificationList;
	private static List<Subgroup> subgroupList;
	private static List<Population> populationList;
	private static List<Category> categoryList;
	private static List<Disease> diseaseList;
	private static List<Domain> domainList;
	private static List<SubDomain> subDomainList;

	private static List<MeasuringUnit> measurementUnitList;
	private static List<MeasuringType> measurementTypeList;

	private static List<State> stateList;
	private static List<Country> countryList;
	private static List<FileType> adminFileTypeList;
	private static List<FileType> supportingDocumentationTypeList;
	private static List<FundingSource> fundingSourceList;
	private static List<StudyType> studyTypeList;

	private static List<FileType> metaStudySupportingDocFileTypeList;
	private static List<FileType> metaStudyDataFileTypeList;

	private static Map<RoleType, Set<RoleType>> rolesThatImplyMap;

	private RestDictionaryProvider anonDictionaryProvider;
	private static Map<Disease, List<Classification>> diseaseClassificationAdminList =
			new LinkedHashMap<Disease, List<Classification>>();
	private static Map<Disease, List<Classification>> diseaseClassificationUserList =
			new LinkedHashMap<Disease, List<Classification>>();
	private static Map<Disease, List<Subgroup>> diseaseSubgroupList = new LinkedHashMap<Disease, List<Subgroup>>();
	private static Map<Disease, List<Domain>> diseaseDomainList = new LinkedHashMap<Disease, List<Domain>>();
	private static Map<Disease, Map<Domain, List<SubDomain>>> diseaseSubDomainList =
			new LinkedHashMap<Disease, Map<Domain, List<SubDomain>>>();

	private static Map<String, Schema> schemasMap;

	protected RestDictionaryProvider getDictionaryProvider() throws MalformedURLException {

		if (anonDictionaryProvider == null) {
			anonDictionaryProvider = new RestDictionaryProvider(modulesConstants.getModulesDDTURL(), null);
		}
		return anonDictionaryProvider;
	}

	public Map<String, Schema> getSchemasMap() {
		if (schemasMap == null) {
			schemasMap = new HashMap<String, Schema>();
			List<Schema> schemaList = schemaDao.getAll();
			for (Schema schema : schemaList) {
				schemasMap.put(schema.getName(), schema);
			}
		}

		return schemasMap;
	}
	
	public Schema getSchemaByName(String name) {
		return this.getSchemasMap().get(name);
	}

	/**
	 * @inheritDoc
	 */
	public List<State> getStateList() {

		if (stateList == null) {
			stateList = stateDao.getAll();
		}

		return stateList;
	}

	/**
	 * @inheritDoc
	 */
	public List<Country> getCountryList() {

		if (countryList == null) {
			countryList = countryDao.getAll();
		}

		return countryList;
	}

	/**
	 * Returns a static list of all funding sources.
	 * 
	 * @return
	 */
	public List<FundingSource> getFundingSourceList() {
		
		if (fundingSourceList == null) {
			fundingSourceList = fundingSourceDao.getAll();
		}

		return fundingSourceList;

	}

	/**
	 * Returns a static list of all study types.
	 * 
	 * @return
	 */
	public List<StudyType> getStudyTypeList() {
		
		if (studyTypeList == null) {
			studyTypeList = studyTypeDao.getAll();
		}

		return studyTypeList;
	}

	/**
	 * @inheritDoc
	 */
	public List<Classification> getClassificationList(boolean admin) {

		List<Classification> classificationList;
		if (admin) {
			if (fullClassificationList == null) {
				fullClassificationList = classificationDao.getAll();
			}
			classificationList = fullClassificationList;
		} else {
			if (userClassificationList == null) {
				userClassificationList = classificationDao.getUserList();
			}
			classificationList = userClassificationList;
		}

		return classificationList;
	}

	/**
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public List<Classification> getClassificationList(Disease disease, boolean isAdmin)
			throws MalformedURLException, UnsupportedEncodingException {

		// Set which list we are looking at
		Map<Disease, List<Classification>> list;
		if (isAdmin) {
			list = diseaseClassificationAdminList;
		} else {
			list = diseaseClassificationUserList;
		}

		if (list.get(disease) == null) {
			try {
				list.put(disease, classificationDiseaseDao.getByDisease(disease, isAdmin));
			} catch (Exception e) {
				list.put(disease, getDictionaryProvider().getClassificationList(disease, isAdmin));
			}

		}

		return list.get(disease);
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public List<Population> getPopulationList() throws MalformedURLException, UnsupportedEncodingException {

		if (populationList == null) {
			try {
				populationList = populationDao.getAll();
			} catch (InvalidDataAccessResourceUsageException e) {
				logger.error(
						"Dictionary refrence tables not found. Fetching list with web service - " + e.getMessage());

				populationList = getDictionaryProvider().getPopulationList();
			}
		}

		return populationList;
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public List<Category> getCategoryList() throws MalformedURLException, UnsupportedEncodingException {

		if (categoryList == null) {
			try {
				categoryList = categoryDao.getAll();
			} catch (InvalidDataAccessResourceUsageException e) {
				logger.error(
						"Dictionary refrence tables not found. Fetching list with web service - " + e.getMessage());

				categoryList = getDictionaryProvider().getCategoryList();
			}
		}

		return categoryList;
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public List<Subgroup> getSubgroupList() throws MalformedURLException, UnsupportedEncodingException {

		if (subgroupList == null) {
			try {
				subgroupList = subgroupDao.getAll();
			} catch (InvalidDataAccessResourceUsageException e) {
				logger.error(
						"Dictionary refrence tables not found. Fetching list with web service - " + e.getMessage());

				subgroupList = getDictionaryProvider().getSubgroupList();
			}
		}

		return subgroupList;
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * 
	 */
	public List<Subgroup> getSubgroupsByDisease(Disease disease)
			throws MalformedURLException, UnsupportedEncodingException {

		if (diseaseSubgroupList.get(disease) == null) {
			try {
				diseaseSubgroupList.put(disease, subgroupDiseaseDao.getByDisease(disease));
			} catch (Exception e) {
				diseaseSubgroupList.put(disease, getDictionaryProvider().getSubgroupsByDisease(disease));
			}
		}

		return diseaseSubgroupList.get(disease);
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public List<Disease> getDiseaseList() throws MalformedURLException, UnsupportedEncodingException {

		if (diseaseList == null) {
			try {
				diseaseList = diseaseDao.getAll();
			} catch (SQLGrammarException e) {
				logger.error(
						"Dictionary refrence tables not found. Fetching list with web service - " + e.getMessage());

				diseaseList = getDictionaryProvider().getDiseaseList();
			}
		}

		return diseaseList;
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public List<Domain> getDomainList() throws MalformedURLException, UnsupportedEncodingException {

		if (domainList == null) {
			try {
				domainList = domainDao.getAll();
			} catch (Exception e) {
				logger.error(
						"Dictionary refrence tables not found. Fetching list with web service - " + e.getMessage());

				domainList = getDictionaryProvider().getDomainList();
			}
		}

		return domainList;
	}

	/**
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 * 
	 */
	public List<Domain> getDomainsByDisease(Disease disease)
			throws MalformedURLException, UnsupportedEncodingException {

		if (diseaseDomainList.get(disease) == null) {
			try {
				diseaseDomainList.put(disease, domainSubDomainDao.getByDisease(disease));
			} catch (Exception e) {
				diseaseDomainList.put(disease, getDictionaryProvider().getDomainsByDisease(disease));
			}

		}

		return diseaseDomainList.get(disease);
	}

	/**
	 * @inheritDoc
	 */
	public List<Domain> getDomainsByDiseaseId(Long id) throws MalformedURLException, UnsupportedEncodingException {

		return getDomainsByDisease(getDiseaseById(id));
	}

	/**
	 * @inheritDoc
	 */
	public List<SubDomain> getSubDomainList() {

		if (subDomainList == null) {
			subDomainList = subDomainDao.getAll();
		}

		return subDomainList;
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public List<SubDomain> getSubDomainsList(Domain domain, Disease disease)
			throws MalformedURLException, UnsupportedEncodingException {

		if (diseaseSubDomainList.get(disease) == null) {
			diseaseSubDomainList.put(disease, new LinkedHashMap<Domain, List<SubDomain>>());
		}
		Map<Domain, List<SubDomain>> domainSubDomainList = diseaseSubDomainList.get(disease);

		if (domainSubDomainList.get(domain) == null) {
			try {
				domainSubDomainList.put(domain, domainSubDomainDao.getSubDomains(domain, disease));
			} catch (Exception e) {
				domainSubDomainList.put(domain, getDictionaryProvider().getSubDomainsList(domain, disease));
			}

		}

		return diseaseSubDomainList.get(disease).get(domain);
	}

	public List<SubDomain> getSubDomainsByDiseaseAndDomainId(Long diseaseId, Long domainId)
			throws MalformedURLException, UnsupportedEncodingException {

		return getSubDomainsList(getDomainById(domainId), getDiseaseById(diseaseId));
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public Disease getDiseaseByName(String name) throws MalformedURLException, UnsupportedEncodingException {

		for (Disease disease : getDiseaseList()) {
			if (disease.getName().equals(name)) {
				return disease;
			}
		}
		return null;
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public Disease getDiseaseById(Long id) throws MalformedURLException, UnsupportedEncodingException {

		for (Disease disease : getDiseaseList()) {
			if (disease.getId().equals(id)) {
				return disease;
			}
		}
		return null;
	}

	@Override
	public Disease getDiseaseBySubgroup(Subgroup group) {

		return subgroupDiseaseDao.getDiseaseBySubGroup(group);
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public Domain getDomainByName(String name) throws MalformedURLException, UnsupportedEncodingException {

		for (Domain domain : getDomainList()) {
			if (domain.getName().equals(name)) {
				return domain;
			}
		}
		return null;
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public Domain getDomainById(Long id) throws MalformedURLException, UnsupportedEncodingException {

		for (Domain domain : getDomainList()) {
			if (domain.getId().equals(id)) {
				return domain;
			}
		}
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public SubDomain getSubDomainByName(String name) {

		for (SubDomain subDomain : getSubDomainList()) {
			if (subDomain.getName().equals(name)) {
				return subDomain;
			}
		}
		return null;
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public Population getPopulationByName(String name) throws MalformedURLException, UnsupportedEncodingException {

		for (Population population : getPopulationList()) {
			if (population.getName().equals(name)) {
				return population;
			}
		}
		return null;
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	public Category getCategoryByName(String name) throws MalformedURLException, UnsupportedEncodingException {

		for (Category category : getCategoryList()) {
			if (category.getName().equals(name)) {
				return category;
			}
		}
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Category getCategoryById(Long id) throws MalformedURLException, UnsupportedEncodingException {

		for (Category category : getCategoryList()) {
			if (category.getId().equals(id)) {
				return category;
			}
		}
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public List<MeasuringType> getMeasuringTypeList() {

		if (measurementTypeList == null) {
			measurementTypeList = measuringTypeDao.getAll();
		}

		return measurementTypeList;
	}

	/**
	 * @inheritDoc
	 */
	public List<MeasuringUnit> getMeasuringUnitList() {

		if (measurementUnitList == null) {
			measurementUnitList = measuringUnitDao.getAll();
		}

		return measurementUnitList;
	}

	/**
	 * @inheritDoc
	 */
	public MeasuringUnit getMeasuringUnitByName(String name) {

		for (MeasuringUnit mu : getMeasuringUnitList()) {
			if (mu.getName().equalsIgnoreCase(name)) {
				return mu;
			}
		}
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public List<FileType> getAdminFileTypeList() {

		if (adminFileTypeList == null) {
			adminFileTypeList = fileTypeDao.getFileTypeByClassification(FileClassification.ADMIN);
		}

		return adminFileTypeList;
	}

	/**
	 * @inheritDoc
	 */
	public FileType getAdminFileTypeByName(String name) {

		for (FileType adminFile : getAdminFileTypeList()) {
			if (adminFile.getName().equals(name)) {
				return adminFile;
			}
		}
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public List<FileType> getSupportingDocumentationTypeList() {

		if (supportingDocumentationTypeList == null) {
			supportingDocumentationTypeList =
					fileTypeDao.getFileTypeByClassification(FileClassification.SUPPORTING_DOCUMENT);
		}

		return supportingDocumentationTypeList;
	}

	/**
	 * @inheritDoc
	 */
	public List<FileType> getMetaStudySupportingDocFileTypeList() {

		if (metaStudySupportingDocFileTypeList == null) {
			metaStudySupportingDocFileTypeList =
					fileTypeDao.getFileTypeByClassification(FileClassification.META_STUDY_SUPPORTING_DOCUMENT);
		}

		return metaStudySupportingDocFileTypeList;
	}

	/**
	 * @inheritDoc
	 */
	public List<FileType> getMetaStudyDataFileTypeList() {

		if (metaStudyDataFileTypeList == null) {
			metaStudyDataFileTypeList = fileTypeDao.getFileTypeByClassification(FileClassification.META_STUDY_DATA);
		}

		return metaStudyDataFileTypeList;
	}

	@Override
	public Classification getClassificationByName(String name) {

		for (Classification classification : getClassificationList(true)) {
			if (classification.getName().equalsIgnoreCase(name)) {
				return classification;
			}
		}
		return null;
	}

	@Override
	public Subgroup getSubgroupByName(String name) throws MalformedURLException, UnsupportedEncodingException {

		for (Subgroup subgroup : getSubgroupList()) {
			if (subgroup.getSubgroupName().equals(name)) {
				return subgroup;
			}
		}
		return null;
	}

	@Override
	public Set<RoleType> rolesThatImplyRole(RoleType key) {

		// If the map has not been created yet, we fetch the complete list of users to be the keys in the map.
		if (rolesThatImplyMap == null) {
			RoleType[] allRoles = RoleType.values();
			rolesThatImplyMap = new HashMap<RoleType, Set<RoleType>>();

			for (int i = 0; i < allRoles.length; i++) {
				rolesThatImplyMap.put(allRoles[i], null);
			}
		}

		// If the map does not have a value for the key role then create one.
		if (rolesThatImplyMap.get(key) == null) {
			Set<RoleType> rolesThatImplyKey = new HashSet<RoleType>();
			RoleType[] allRoles = RoleType.values();

			for (int i = 0; i < allRoles.length; i++) {
				Collection<GrantedAuthority> roleAsCollection = new HashSet<GrantedAuthority>();
				roleAsCollection.add(new SimpleGrantedAuthority(allRoles[i].getName()));
				Collection<? extends GrantedAuthority> grantedAuths =
						roleHierarchy.getReachableGrantedAuthorities(roleAsCollection);
				if (grantedAuths.contains(new SimpleGrantedAuthority(key.getName()))) {
					rolesThatImplyKey.add(allRoles[i]);
				}

			}
			rolesThatImplyMap.put(key, rolesThatImplyKey);
		}

		return rolesThatImplyMap.get(key);

	}

}
