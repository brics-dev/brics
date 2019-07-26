package gov.nih.tbi.dictionary.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.commons.service.hibernate.StaticReferenceManagerImpl;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.model.hibernate.Alias;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseElement;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.DomainPair;
import gov.nih.tbi.dictionary.model.hibernate.ExternalId;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringType;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringUnit;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;
import gov.nih.tbi.dictionary.service.rulesengine.RulesEngine;
import gov.nih.tbi.dictionary.service.rulesengine.RulesEngineUtils;
import gov.nih.tbi.dictionary.service.rulesengine.model.InvalidOperationException;
import gov.nih.tbi.dictionary.service.rulesengine.model.RulesEngineException;
import gov.nih.tbi.repository.model.SubmissionType;

@ContextConfiguration({"/context.xml"})
public class RulesEngineTest extends AbstractJUnit4SpringContextTests {

	DataElement incoming;

	DataElement original;

	@Autowired
	DataElementDao dataElementDao;

	/*
	 * @Mock ValueRange valueRange;
	 */

	Population population = new Population();

	@Mock
	Disease disease;

	@Mock
	DomainPair domainPair;

	@Mock
	Domain domain;

	@Mock
	SubDomain subDomain;

	ExternalId externalId = new ExternalId();

	@Mock
	Alias alias;

	Category category = new Category();

	RepeatableGroup repeatableGroup;

	@Mock
	MapElement mapElement;

	@Mock
	FormStructure formStructureBase;

	@Mock
	FormStructure formStructureChanged;

	RulesEngine rulesEngine;

	StaticReferenceManager staticManager = new StaticReferenceManagerImpl();

	@Before
	public void setUp() throws JAXBException, IOException {

		MockitoAnnotations.initMocks(this);

		rulesEngine = new RulesEngine();
		// Setting Incoming

		// original = new DataElement();
	}

	/*
	 * This method will set up all the data related to the mocked data elements
	 */

	public void mockDataElements() {

		createDataElement();

	}

	/**
     * 
     */
	private DataElement createDataElement() {

		DataElement toRtn = new DataElement();
		// add mocked objects to value range list
		ValueRange vr = new ValueRange();
		vr.setValueRange("Test");
		vr.setDescription("Test for change");
		Set<ValueRange> valueRangeSet = new TreeSet<ValueRange>();
		valueRangeSet.add(vr);

		Set<SubDomainElement> subDomainElement = new HashSet<SubDomainElement>();
		SubDomainElement sde1 = new SubDomainElement();
		Disease diseaseSDE1 = new Disease();
		diseaseSDE1.setName("Headache");

		Domain domainSDE1 = new Domain();
		domainSDE1.setName("Safety Data");

		SubDomain subDomainSDE1 = new SubDomain();
		subDomainSDE1.setName("Cognitive");

		sde1.setDisease(diseaseSDE1);
		sde1.setDomain(domainSDE1);
		sde1.setSubDomain(subDomainSDE1);

		subDomainElement.add(sde1);

		// add mocked objects to classification element
		TreeSet<ClassificationElement> classificationElementSet = new TreeSet<ClassificationElement>();
		ClassificationElement classificationElement = new ClassificationElement();
		classificationElement.setId(1L);
		classificationElement.setDisease(diseaseSDE1);
		Classification classification = new Classification();
		classification.setId(1L);
		classification.setName("Core");
		classificationElement.setClassification(classification);
		Subgroup subgroup = new Subgroup("Epilepsy");
		classificationElement.setSubgroup(subgroup);
		classificationElementSet.add(classificationElement);

		// add mocked objects to disease element
		ArrayList<DiseaseElement> diseaseElementSet = new ArrayList<DiseaseElement>();
		DiseaseElement de = new DiseaseElement();
		de.setDisease(disease);
		domainPair.setDomain(domain);
		domainPair.setSubdomain(subDomain);
		Set<DomainPair> domainPairSet = new TreeSet<DomainPair>();
		domainPairSet.add(domainPair);
		de.setDomainList(domainPairSet);

		// add mocked objects to measuring unit

		MeasuringUnit measuringUnit = new MeasuringUnit();

		MeasuringType measuringType = new MeasuringType();
		measuringUnit.setMeasuringType(measuringType);

		// add mocked ojects to keyword element
		// KeywordElement keywordElement = new KeywordElement();
		//
		// Keyword keyword = new Keyword();
		// keyword.setKeyword("Keyword to your mama");
		// keyword.setCount(5L);
		//
		// Set<KeywordElement> keywordElementSet = new TreeSet<KeywordElement>();
		// keywordElement.setKeyword(keyword);
		// keywordElementSet.add(keywordElement);

		// add mocked objects to external id
		Set<ExternalId> externalIdSet = new TreeSet<ExternalId>();
		externalId.setSchema(new Schema("LOINC"));
		externalIdSet.add(externalId);

		// add mocked objects to alias
		Set<Alias> aliasListSet = new TreeSet<Alias>();
		aliasListSet.add(alias);

		Category category = new Category();

		category.setName("Unique Data Element");
		category.setShortName("UDE");
		category.setId(1L);

		// original = new DataElement();
		// populate both data elements with same mocked objects
		StructuralDataElement orgSDE = new StructuralDataElement();
		SemanticDataElement orgSemDE = new SemanticDataElement();
		toRtn = new DataElement(orgSDE, orgSemDE);
		toRtn.setType(DataType.ALPHANUMERIC);
		toRtn.setRestrictions(InputRestrictions.FREE_FORM);
		toRtn.setDescription("This is a test, dummy");
		toRtn.setStatus(DataElementStatus.DRAFT);
		toRtn.setValueRangeList(valueRangeSet);
		toRtn.setClassificationElementList(classificationElementSet);
		toRtn.setPopulation(population);
		toRtn.setSubDomainElementList(subDomainElement);
		toRtn.setMeasuringUnit(measuringUnit);
		// toRtn.setKeywordList(keywordElementSet);
		toRtn.setExternalIdSet(externalIdSet);
		toRtn.setAliasList(aliasListSet);
		toRtn.setCategory(category);
		toRtn.setTitle("Incoming Data Element");
		toRtn.setName("incoming");
		toRtn.setId(1L);
		toRtn.setSize(10);
		toRtn.setType(DataType.GUID);
		return toRtn;
	}

	/*
	 * This method will mock all data for both form structures
	 */

	public void mockFormStructures() {

		// mapElement.setDataElement(original);
		createFormStructure();

		setIncomingChangeFormStructure();

	}

	/**
     * 
     */
	private FormStructure createFormStructure() {

		FormStructure fs = new FormStructure();

		Set<RepeatableGroup> repeatableGroupSet = createRepeatableGroupSet();

		Set<DiseaseStructure> diseaseStructureSet = createDiseaseStructureSet();
		DiseaseStructure ds = new DiseaseStructure();
		Disease stroke = new Disease(17L, "Stroke", true, false);

		List<Disease> disease = new ArrayList<Disease>();
		disease.add(stroke);

		fs = new FormStructure();
		fs.setDiseases(disease);
		fs.setDescription("Description");
		fs.setTitle("Test");
		fs.setOrganization("NIH");
		fs.setRepeatableGroups(repeatableGroupSet);
		fs.setDiseaseList(diseaseStructureSet);
		fs.setStatus(StatusType.DRAFT);
		fs.setFileType(SubmissionType.CLINICAL);

		return fs;
	}

	/**
     * 
     */
	private void setIncomingChangeFormStructure() {

		Set<RepeatableGroup> repeatableGroupSet = createRepeatableGroupSet();

		Set<DiseaseStructure> diseaseStructureSet = createDiseaseStructureSet();

		List<Disease> disease = new ArrayList<Disease>();
		Disease stroke = new Disease(17L, "Stroke", true, false);
		disease.add(stroke);

		formStructureChanged = new FormStructure();
		formStructureChanged.setDiseases(disease);
		formStructureChanged.setDescription("Description");
		formStructureChanged.setTitle("Test");
		formStructureChanged.setRepeatableGroups(repeatableGroupSet);
		formStructureChanged.setDiseaseList(diseaseStructureSet);
		formStructureChanged.setStatus(StatusType.DRAFT);
		formStructureChanged.setFileType(SubmissionType.CLINICAL);
	}

	/**
	 * @return
	 */
	private Set<DiseaseStructure> createDiseaseStructureSet() {

		Disease disease = new Disease();
		disease.setId(12L);
		disease.setName("Stroke");
		DiseaseStructure ds = new DiseaseStructure();
		ds.setId(1L);
		ds.setDisease(disease);
		Set<DiseaseStructure> diseaseStructureSet = new TreeSet<DiseaseStructure>();
		diseaseStructureSet.add(ds);
		return diseaseStructureSet;
	}

	/**
	 * @return
	 */
	private Set<RepeatableGroup> createRepeatableGroupSet() {

		RepeatableGroup repeatableGroup = new RepeatableGroup();
		MapElement orgMapElement = new MapElement(createDataElement().getStructuralObject());
		orgMapElement.setId(69L);

		LinkedHashSet<MapElement> mapElementSet = new LinkedHashSet<MapElement>();
		mapElementSet.add(orgMapElement);

		repeatableGroup.setName("Repeatable Group");
		repeatableGroup.setMapElements(mapElementSet);
		repeatableGroup.setThreshold(3);
		repeatableGroup.setPosition(2);
		repeatableGroup.setType(RepeatableType.EXACTLY);

		Set<RepeatableGroup> repeatableGroupSet = new TreeSet<RepeatableGroup>();
		repeatableGroupSet.add(repeatableGroup);
		return repeatableGroupSet;
	}

	public void organizeDataElementSeverityRecords(DataElement org, List<SeverityRecord> rulesEngineSeverityRecord) {

		if (!rulesEngineSeverityRecord.isEmpty()) {
			// StringBuilder changeString = new StringBuilder();
			// changeString.append(org.getName() + RulesEngineConstants.RULES_ENGINE_CHANGES);
			for (SeverityRecord sr : rulesEngineSeverityRecord) {
				// changeString.append("->\t" + formatFieldName(sr.getFieldName()) +
				// " was changed and it's severity is "
				// + sr.getSeverityLevel());
				System.out.println(sr.getFieldName() + "\n");

			}

		}
	}

	public String formatFieldName(String name) {

		String newFieldName = name;
		CharSequence cs = ".";
		if (name.contains(cs)) {
			newFieldName = name.substring(0, name.indexOf('.'));
		}
		int capLetters = detectCapitalLettersInFieldName(newFieldName);
		while (capLetters != -1) {
			newFieldName = addSpacesInFieldName(newFieldName, capLetters);
			capLetters = detectCapitalLettersInFieldName(newFieldName);
		}
		return newFieldName;

	}

	public int detectCapitalLettersInFieldName(String name) {

		for (int i = name.length() - 1; i >= 0; i--) {
			// This is to look for camelCase letters
			if (i != 0 && Character.isUpperCase(name.charAt(i)) && (name.charAt(i - 1) != ' ')) {
				return i;
			}
		}
		return -1;
	}

	public String addSpacesInFieldName(String name, int spaceLocal) {

		StringBuilder newFieldName = new StringBuilder();
		for (int i = name.length() - 1; i >= 0; i--) {
			if (i == spaceLocal) {
				newFieldName.append(name.subSequence(0, (i)));
				newFieldName.append(" ");
				newFieldName.append(name.subSequence(i, name.length()));
			}
		}
		return newFieldName.toString();
	}

	/*
	 * @Before public void incomingDEInstantiation() {
	 * 
	 * incoming = new DataElement(); incoming.setTitle("Incoming Data Element"); incoming.setName("incoming");
	 * incoming.setId(1L); incoming.setSize(10); incoming.setType(DataType.GUID);
	 * incoming.setRestrictions(InputRestrictions.FREE_FORM); incoming.setDescription("description");
	 * incoming.setShortDescription("short description"); incoming.setNotes("notes");
	 * incoming.setStatus(DataElementStatus.DRAFT); incoming.setValueRangeList(instantiateSameValueRangeList());
	 * incoming.setGuidelines("guidelines"); incoming.setHistoricalNotes("Historical Notes");
	 * incoming.setSuggestedQuestion("What it do?"); incoming.setReferences("References");
	 * incoming.setClassificationElementList();
	 * 
	 * 
	 * }
	 */

	/*
	 * @Before public Set<ValueRange> instantiateSameValueRangeList(){ Set<ValueRange> valueRangeList = new
	 * HashSet<ValueRange>(); ValueRange valueRange = new ValueRange(); valueRange.setValueRange("Value Range");
	 * valueRange.setDescription("Description"); valueRangeList.add(valueRange); return valueRangeList; }
	 * 
	 * @Before public Set<ClassificationElement> instantiateSameClassificationElementList(){ Set<ClassificationElement>
	 * classificationElementList = new HashSet<ClassificationElement>(); ClassificationElement classificationElement =
	 * new ClassificationElement(); classificationElement.setClassification(Classification.); }
	 */

	/*
	 * @Test public void testRulesEngineCreation() {
	 * 
	 * System.out.println("Test Rules Engine");
	 * 
	 * Assert.assertTrue("Rules Engine successfully created", rulesEngine != null);
	 * 
	 * }
	 *//**
	 * Test the creation of the Date Element list of fields
	 */
	/*
	 * 
	 * @Test public void testRulesEngineDEFieldCreation() {
	 * 
	 * System.out.println("---------------------------------------------------");
	 * System.out.println("Test Rules Engine Data Element Field List Creation"); for (Field field :
	 * rulesEngine.listOfDataElementFields) { System.out.println(field.getName()); }
	 * 
	 * Assert.assertTrue("Rules Engine successfully created", rulesEngine.getListOfDataElementFields() != null); }
	 */
	/**
	 * Test the creation of the Form Structure list of fields
	 * 
	 * @throws RulesEngineException
	 * @throws InvalidOperationException
	 */
	/*
	 * @Test public void testRulesEngineFSFieldCreation() {
	 * 
	 * System.out.println("----------------------------------------------------");
	 * System.out.println("Test Rules Engine Form Structure Field List Creation");
	 * 
	 * for (Field field : rulesEngine.listOfFormStructureFields) { System.out.println(field.getName()); }
	 * Assert.assertTrue("Rules Engine successfully created", rulesEngine.getListOfFormStructureFields() != null); }
	 * 
	 * @Test public void testEvaluateDataElementChangeSeverityOrgNullDataElements() {
	 * 
	 * System.out.println("Evaluate Data Element Change Severity with Data Elements are null");
	 * 
	 * DataElement originalF = null; List<SeverityRecord> listOfSeverityRecords =
	 * rulesEngine.evaluateDataElementChangeSeverity(originalF, incoming); Assert.assertTrue(listOfSeverityRecords ==
	 * null); }
	 */
	/*
	 * @Test public void testDetermineChangesBetweenDataElementsFullRun() {
	 * 
	 * DataElement org = createDataElement();
	 * 
	 * List<SeverityRecord> listOfSeverityRecords = rulesEngine.evaluateDataElementChangeSeverity(org, org);
	 * Assert.assertTrue(listOfSeverityRecords.isEmpty()); }
	 */

	/*
	 * @Test public void testDetermineChangesBetweenFormStructureFullRun() throws InvalidOperationException,
	 * RulesEngineException {
	 * 
	 * FormStructure org = createFormStructure(); List<SeverityRecord> listOfSeverityRecords =
	 * rulesEngine.evaluateFormStructureChangeSeverity(org, org); Assert.assertTrue(listOfSeverityRecords.isEmpty()); }
	 */

	/**************************************************************************************************************
	 * 
	 * Test CompareTo and Equals Methods
	 * 
	 * @throws RulesEngineException
	 * @throws InvalidOperationException
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 * 
	 * 
	 **************************************************************************************************************/

	/*
	 * @Test public void testDataElementEqualStringFieldDescriptionField() throws InvalidOperationException,
	 * RulesEngineException {
	 * 
	 * DataElement org = createDataElement(); DataElement inc = createDataElement();
	 * 
	 * inc.setTitle("testDataElementEqualStringFieldDescriptionField");
	 * 
	 * List<SeverityRecord> listOfSeverityRecords = null; listOfSeverityRecords =
	 * rulesEngine.evaluateDataElementChangeSeverity(org, inc); if (!listOfSeverityRecords.isEmpty()) {
	 * organizeDataElementSeverityRecords(org, listOfSeverityRecords); if (listOfSeverityRecords.size() == 1) {
	 * SeverityRecord sr = listOfSeverityRecords.get(0); Assert.assertTrue(sr.getFieldName().equalsIgnoreCase("Title"));
	 * } } else { Assert.assertTrue(false);
	 * 
	 * } }
	 */
	// @Test
	// public void testDataElementEqualSuggestEdQuestionDescriptionField() throws InvalidOperationException,
	// RulesEngineException
	// {
	//
	// DataElement org = createDataElement();
	// DataElement inc = createDataElement();
	//
	// inc.setSuggestedQuestion("testDataElementEqualStringFieldDescriptionField");
	//
	// List<SeverityRecord> listOfSeverityRecords = null;
	// listOfSeverityRecords = rulesEngine.evaluateDataElementChangeSeverity(org, inc);
	// if (!listOfSeverityRecords.isEmpty())
	// {
	// organizeDataElementSeverityRecords(org, listOfSeverityRecords);
	// if (listOfSeverityRecords.size() == 1)
	// {
	// SeverityRecord sr = listOfSeverityRecords.get(0);
	// System.out.println(RulesEngineUtils.generateSeverityRecordString(sr));
	// Assert.assertTrue(sr.getFieldName().equalsIgnoreCase("SuggestedQuestion"));
	// }
	// }
	// else
	// {
	// Assert.assertTrue(false);
	//
	// }
	// }

	// /**
	// * This should test wheter or not the multiField get works, which it should
	// *
	// * @throws InvalidOperationException
	// * @throws RulesEngineException
	// * @throws MalformedURLException
	// * @throws UnsupportedEncodingException
	// */
	// @Test
	// public void testDataElementEqualSubDomainElementList() throws InvalidOperationException, RulesEngineException,
	// MalformedURLException, UnsupportedEncodingException
	// {
	//
	// DataElement org = createDataElement();
	// DataElement inc = createDataElement();
	//
	// SubDomainElement sde1 = new SubDomainElement();
	// Disease diseaseSDE1 = new Disease();
	// diseaseSDE1.setName("Stroke");
	//
	// Domain domainSDE1 = new Domain();
	// domainSDE1.setName("Safety Data");
	//
	// SubDomain subDomainSDE1 = new SubDomain();
	// subDomainSDE1.setName("Cognitive");
	//
	// sde1.setDisease(diseaseSDE1);
	// sde1.setDomain(domainSDE1);
	// sde1.setSubDomain(subDomainSDE1);
	// org.getSubDomainElementList().add(sde1);
	//
	// List<SeverityRecord> listOfSeverityRecords = null;
	// listOfSeverityRecords = rulesEngine.evaluateDataElementChangeSeverity(org, inc);
	// if (!listOfSeverityRecords.isEmpty())
	// {
	// organizeDataElementSeverityRecords(org, listOfSeverityRecords);
	// if (listOfSeverityRecords.size() == 1)
	// {
	// SeverityRecord sr = listOfSeverityRecords.get(0);
	// Assert.assertTrue(sr.getFieldName().equalsIgnoreCase("SubDomainElementList"));
	// }
	// }
	// else
	// {
	// Assert.assertTrue(false);
	//
	// }
	// }

	// @Test
	// public void testDataElementEqualClassificationElementList() throws InvalidOperationException,
	// RulesEngineException
	// {
	//
	// DataElement org = createDataElement();
	// DataElement inc = createDataElement();
	//
	// ClassificationElement classificationElement = new ClassificationElement();
	// classificationElement.setId(2L);
	// Disease diseaseSDE1 = new Disease();
	// diseaseSDE1.setName("CNRM");
	// classificationElement.setDisease(diseaseSDE1);
	// Classification classification = new Classification();
	// classification.setId(1L);
	// classification.setName("Core");
	// classificationElement.setClassification(classification);
	// Subgroup subgroup = new Subgroup("Neuromuscular Diseases");
	// classificationElement.setSubgroup(subgroup);
	//
	// org.getClassificationElementList().add(classificationElement);
	//
	// List<SeverityRecord> listOfSeverityRecords = null;
	// listOfSeverityRecords = rulesEngine.evaluateDataElementChangeSeverity(org, inc);
	// if (!listOfSeverityRecords.isEmpty())
	// {
	// organizeDataElementSeverityRecords(org, listOfSeverityRecords);
	// if (listOfSeverityRecords.size() == 1)
	// {
	// SeverityRecord sr = listOfSeverityRecords.get(0);
	// Assert.assertTrue(sr.getFieldName().equalsIgnoreCase("ClassificationElementList"));
	// }
	// }
	// else
	// {
	// Assert.assertTrue(false);
	//
	// }
	// }

	// @Test
	// public void testDataElementEqualClassificationElementListAddedRemoved() throws InvalidOperationException,
	// RulesEngineException
	// {
	//
	// DataElement org = createDataElement();
	// DataElement inc = createDataElement();
	//
	// org.getClassificationElementList().clear();
	//
	// ClassificationElement classificationElement = new ClassificationElement();
	// classificationElement.setId(2L);
	// Disease diseaseSDE1 = new Disease();
	// diseaseSDE1.setName("CNRM");
	// classificationElement.setDisease(diseaseSDE1);
	// Classification classification = new Classification();
	// classification.setId(1L);
	// classification.setName("Core");
	// classificationElement.setClassification(classification);
	// Subgroup subgroup = new Subgroup("Epilepsy");
	// classificationElement.setSubgroup(subgroup);
	//
	// org.getClassificationElementList().add(classificationElement);
	//
	// List<SeverityRecord> listOfSeverityRecords = null;
	// listOfSeverityRecords = rulesEngine.evaluateDataElementChangeSeverity(org, inc);
	// if (!listOfSeverityRecords.isEmpty())
	// {
	// organizeDataElementSeverityRecords(org, listOfSeverityRecords);
	// if (listOfSeverityRecords.size() == 1)
	// {
	// SeverityRecord sr = listOfSeverityRecords.get(0);
	// Assert.assertTrue(sr.getFieldName().equalsIgnoreCase("ClassificationElementList"));
	// }
	// }
	// else
	// {
	// Assert.assertTrue(false);
	//
	// }
	// }

	// @Test
	// public void testDataElementEqualPermissibleValueField() throws InvalidOperationException, RulesEngineException
	// {
	//
	// DataElement org = createDataElement();
	// DataElement inc = createDataElement();
	// ValueRange vr = new ValueRange();
	// vr.setValueRange("print");
	// vr.setDescription("Test for change");
	//
	// ValueRange ibroke = new ValueRange();
	// vr.setValueRange("print");
	// vr.setDescription("difference");
	//
	// org.getValueRangeList().add(vr);
	// inc.getValueRangeList().add(ibroke);
	// List<SeverityRecord> listOfSeverityRecords = null;
	// listOfSeverityRecords = rulesEngine.evaluateDataElementChangeSeverity(org, inc);
	// if (!listOfSeverityRecords.isEmpty())
	// {
	// organizeDataElementSeverityRecords(org, listOfSeverityRecords);
	// if (listOfSeverityRecords.size() == 1)
	// {
	// SeverityRecord sr = listOfSeverityRecords.get(0);
	// Assert.assertTrue(sr.getFieldName().equalsIgnoreCase("ValueRangeList"));
	// }
	// }
	// else
	// {
	// Assert.assertTrue(false);
	//
	// }
	// }
	//
	// @Test
	// public void testDataElementEqualPermissibleValueDescription() throws InvalidOperationException,
	// RulesEngineException
	// {
	//
	// DataElement org = createDataElement();
	// DataElement inc = createDataElement();
	//
	// Boolean once = false;
	// for (ValueRange vr : inc.getValueRangeList())
	// {
	// if (!once)
	// {
	// vr.setDescription("IMMACHANGEYOU");
	// }
	// }
	// List<SeverityRecord> listOfSeverityRecords = null;
	// listOfSeverityRecords = rulesEngine.evaluateDataElementChangeSeverity(org, inc);
	// if (!listOfSeverityRecords.isEmpty())
	// {
	// organizeDataElementSeverityRecords(org, listOfSeverityRecords);
	// if (listOfSeverityRecords.size() == 1)
	// {
	// SeverityRecord sr = listOfSeverityRecords.get(0);
	// System.out.println(RulesEngineUtils.generateSeverityRecordString(sr));
	//
	// Assert.assertTrue(sr.getFieldName().equalsIgnoreCase("ValueRangeList"));
	// }
	// }
	// else
	// {
	// Assert.assertTrue(false);
	//
	// }
	// }

	@Test
	public void testDetermineChangesBetweenFormStructureRepeatableGroup() throws InvalidOperationException,
			RulesEngineException {

		FormStructure org = createFormStructure();
		FormStructure inc = createFormStructure();

		inc.getRepeatableGroupByName("Repeatable Group").setThreshold(10);
		inc.getRepeatableGroupByName("Repeatable Group").setType(RepeatableType.LESSTHAN);

		List<SeverityRecord> listOfSeverityRecords = rulesEngine.evaluateFormStructureChangeSeverity(org, inc);
		if (!listOfSeverityRecords.isEmpty()) {

			if (listOfSeverityRecords.size() == 1) {
				SeverityRecord sr = listOfSeverityRecords.get(0);
				System.out.println(sr.getFieldName());
				System.out.println(RulesEngineUtils.generateSeverityRecordString(sr));
				Assert.assertTrue(sr.getFieldName().equals("Type/Threshold"));
			}
		} else {
			Assert.assertTrue(false);

		}
		Assert.assertTrue(listOfSeverityRecords.isEmpty());
	}

	// @Test
	// public void testAdditionFormStructureRepeatableGroup() throws InvalidOperationException, RulesEngineException
	// {
	//
	// FormStructure org = createFormStructure();
	// FormStructure inc = createFormStructure();
	//
	// RepeatableGroup repeatableGroup = new RepeatableGroup();
	// MapElement orgMapElement = new MapElement(createDataElement().getStructuralObject());
	// orgMapElement.setId(69L);
	//
	// Set<MapElement> mapElementSet = new TreeSet<MapElement>();
	// mapElementSet.add(orgMapElement);
	//
	// repeatableGroup.setName("An Added Repeatable Group");
	// repeatableGroup.setMapElements(mapElementSet);
	// repeatableGroup.setThreshold(3);
	// repeatableGroup.setPosition(2);
	// repeatableGroup.setType(RepeatableType.EXACTLY);
	//
	// inc.getRepeatableGroups().add(repeatableGroup);
	//
	// List<SeverityRecord> listOfSeverityRecords = rulesEngine.evaluateFormStructureChangeSeverity(org, inc);
	// if (!listOfSeverityRecords.isEmpty())
	// {
	// for (SeverityRecord sr : listOfSeverityRecords)
	// {
	// System.out.println(sr.getFieldName());
	//
	// System.out.println(RulesEngineUtils.generateSeverityRecordString(sr));
	// }
	// if (listOfSeverityRecords.size() == 1)
	// {
	// SeverityRecord sr = listOfSeverityRecords.get(0);
	// System.out.println(sr.getFieldName());
	// System.out.println(RulesEngineUtils.generateSeverityRecordString(sr));
	// Assert.assertTrue(sr.getFieldName().equals("Type/Threshold"));
	// }
	// }
	// else
	// {
	// Assert.assertTrue(false);
	//
	// }
	// Assert.assertTrue(listOfSeverityRecords.isEmpty());
	// }

	// @Test
	// public void testDataElementEqualIntegerFieldSizeField() throws InvalidOperationException, RulesEngineException
	// {
	//
	// DataElement org = createDataElement();
	// DataElement inc = createDataElement();
	//
	// inc.setSize(100);
	//
	// List<SeverityRecord> listOfSeverityRecords = null;
	// listOfSeverityRecords = rulesEngine.evaluateDataElementChangeSeverity(org, inc);
	// if (!listOfSeverityRecords.isEmpty())
	// {
	//
	// if (listOfSeverityRecords.size() == 1)
	// {
	// SeverityRecord sr = listOfSeverityRecords.get(0);
	// System.out.println(RulesEngineUtils.generateSeverityRecordString(sr));
	// // Assert.assertTrue(sr.getFieldName().equalsIgnoreCase("Size"));
	// Assert.assertTrue(true);
	// }
	// }
	// else
	// {
	// Assert.assertTrue(false);
	//
	// }
	// }
	//
	// @Test
	// public void testDataElementEqualCategoryField() throws InvalidOperationException, RulesEngineException
	// {
	//
	// DataElement org = createDataElement();
	// DataElement inc = createDataElement();
	//
	// Category category = new Category();
	//
	// category.setName("Common Data Element");
	// category.setShortName("CDE");
	// category.setId(2L);
	// inc.setCategory(category);
	//
	// List<SeverityRecord> listOfSeverityRecords = null;
	// listOfSeverityRecords = rulesEngine.evaluateDataElementChangeSeverity(org, inc);
	// if (!listOfSeverityRecords.isEmpty())
	// {
	// // organizeDataElementSeverityRecords(org, listOfSeverityRecords);
	//
	// if (listOfSeverityRecords.size() == 1)
	// {
	// SeverityRecord sr = listOfSeverityRecords.get(0);
	// System.out.println(RulesEngineUtils.generateSeverityRecordString(sr));
	// // System.out.println(sr.getFieldName());
	// // Assert.assertTrue(sr.getFieldName().equalsIgnoreCase("Category"));
	// Assert.assertTrue(true);
	//
	// }
	// }
	// else
	// {
	// Assert.assertTrue(false);
	//
	// }
	// }
	//
	// @Test
	// public void testDataElementEqualDataTypeField() throws InvalidOperationException, RulesEngineException
	// {
	//
	// DataElement org = createDataElement();
	// DataElement inc = createDataElement();
	//
	// inc.setType(DataType.BIOSAMPLE);
	//
	// List<SeverityRecord> listOfSeverityRecords = null;
	// listOfSeverityRecords = rulesEngine.evaluateDataElementChangeSeverity(org, inc);
	// if (!listOfSeverityRecords.isEmpty())
	// {
	// // organizeDataElementSeverityRecords(org, listOfSeverityRecords);
	//
	// if (listOfSeverityRecords.size() == 1)
	// {
	// SeverityRecord sr = listOfSeverityRecords.get(0);
	// System.out.println(RulesEngineUtils.generateSeverityRecordString(sr));
	//
	// // System.out.println(sr.getFieldName());
	// Assert.assertTrue(true);
	//
	// // Assert.assertTrue(sr.getFieldName().equalsIgnoreCase("Type"));
	// }
	// }
	// else
	// {
	// Assert.assertTrue(false);
	//
	// }
	// }

	/*
	 * @Test public void testDataElementEqualInputRestrictionField() {
	 * 
	 * DataElement org = createDataElement(); DataElement inc = createDataElement();
	 * 
	 * inc.setRestrictions(InputRestrictions.SINGLE);
	 * 
	 * List<SeverityRecord> listOfSeverityRecords = null; listOfSeverityRecords =
	 * rulesEngine.evaluateDataElementChangeSeverity(org, inc); if (!listOfSeverityRecords.isEmpty()) { if
	 * (listOfSeverityRecords.size() == 1) { SeverityRecord sr = listOfSeverityRecords.get(0);
	 * Assert.assertTrue(sr.getFieldName().equalsIgnoreCase("Restrictions")); } } else { Assert.assertTrue(false);
	 * 
	 * } }
	 */
	/*
	 * @Test public void testDataElementEqualValueRangeFieldAdded() {
	 * 
	 * DataElement org = createDataElement(); DataElement inc = createDataElement();
	 * 
	 * ValueRange vr = new ValueRange(); vr.setValueRange("Added"); vr.setDescription("Added");
	 * inc.getValueRangeList().add(vr);
	 * 
	 * List<SeverityRecord> listOfSeverityRecords = null; listOfSeverityRecords =
	 * rulesEngine.evaluateDataElementChangeSeverity(org, inc); if (!listOfSeverityRecords.isEmpty()) { if
	 * (listOfSeverityRecords.size() == 1) { SeverityRecord sr = listOfSeverityRecords.get(0);
	 * Assert.assertTrue(sr.getFieldName().equalsIgnoreCase("ValueRangeList")); } } else { Assert.assertTrue(false);
	 * 
	 * } }
	 */
	/*
	 * @Test public void testJAXBRulesEngineFactor() throws JAXBException {
	 * 
	 * JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class); Unmarshaller unmarshaller =
	 * jaxbContext.createUnmarshaller(); File xmlFile = new
	 * File("C:\\Users\\mgree1\\Documents\\New_Dictionary\\Rules_Engine_Design\\Rules.xml");
	 * JAXBElement<RulesEngineRules> unmarshalledObject = (JAXBElement<RulesEngineRules>) unmarshaller
	 * .unmarshal(xmlFile);
	 * 
	 * RulesEngineRules rer = unmarshalledObject.getValue(); DictionaryObjectRules DEObjRules =
	 * rer.getDataElementRules(); DictionaryObjectRules FSObjRules = rer.getFormStructureRules();
	 * 
	 * List<FieldRule> deFieldRules = DEObjRules.getFieldRules();
	 * 
	 * }
	 */

	/*
	 * incoming.getValueRangeList().clear(); ValueRange vr = new ValueRange(); vr.setValueRange("Different");
	 * vr.setDescription("Test for change"); incoming.getValueRangeList().add(vr); List<SeverityRecord>
	 * listOfSeverityRecords = rulesEngine.evaluateDataElementChangeSeverity(original, incoming);
	 * Assert.assertTrue(!listOfSeverityRecords.isEmpty());
	 */

	/*
	 * @Test public void testViewFieldsInDEFS() throws IllegalArgumentException, IllegalAccessException {
	 * 
	 * for (Field field : rulesEngine.listOfDataElementFields) { System.out.println(field.getName()); Object obj =
	 * field.get(incoming); if (obj != null) { System.out.println("Object: " + obj.getClass());
	 * 
	 * } else { System.out.println("Null Field: " + field.getName()); } } }
	 * 
	 * 
	 * 
	 * 
	 * @Test public void testDetermineSeverityOfDataElementChangeMade() {
	 * 
	 * Assert.fail("Not yet implemented"); }
	 * 
	 * @Test public void testEvaluateFormStructureChangeSeverity() {
	 * 
	 * Assert.fail("Not yet implemented"); }
	 * 
	 * @Test public void testDetermineChangesBetweenFormStructures() {
	 * 
	 * Assert.fail("Not yet implemented"); }
	 * 
	 * @Test public void testDetermineSeverityOfFormStructureChangeMade() {
	 * 
	 * Assert.fail("Not yet implemented"); }
	 * 
	 * @Test public void testCompareTo() {
	 * 
	 * Assert.fail("Not yet implemented"); }
	 * 
	 * @Test public void testCompareToClassificationElement() {
	 * 
	 * Assert.fail("Not yet implemented"); }
	 * 
	 * @Test public void testCompareToSet() {
	 * 
	 * Assert.fail("Not yet implemented"); }
	 * 
	 * @Test public void testCompareToDiseaseElement() {
	 * 
	 * Assert.fail("Not yet implemented"); }
	 * 
	 * @Test public void testCompareToKeywordElement() {
	 * 
	 * Assert.fail("Not yet implemented"); }
	 */

}
