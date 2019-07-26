package gov.nih.tbi.dictionary.dao.sparql;

import gov.nih.tbi.commons.util.QueryConstructionUtil;
import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.dictionary.dao.DataElementSparqlDao;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.ExternalId;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataElementSparqlDaoTest {

	private DataElementSparqlDao dataElementDao;
	private ApplicationContext ctx;

	@BeforeMethod
	protected void setUp() {

		ctx = new ClassPathXmlApplicationContext("test-context.xml");
		dataElementDao = ctx.getBean("dataElementSparqlDao", DataElementSparqlDao.class);
	}

	/**
	 * This needs to be updated when semantic data element fields get modified
	 * 
	 * @param name
	 * @param collectionSize
	 * @return
	 */
	private SemanticDataElement createTestDataElement(String name, int collectionSize) {

		SemanticDataElement testDe = new SemanticDataElement();
		testDe.setName(name);
		testDe.setDescription("description");
		testDe.setShortDescription("shortDescription");
		testDe.setFormat("format");
		testDe.setGuidelines("guidelines");
		testDe.setTitle("title");
		testDe.setHistoricalNotes("historicalNotes");
		testDe.setNotes("notes");
		testDe.setReferences("references");

		// population
		Population population = new Population();
		population.setName("population");
		testDe.setPopulation(population);

		// disease element
		Set<SubDomainElement> subDomainElements = new HashSet<SubDomainElement>();

		for (int i = 0; i < collectionSize; i++) {
			SubDomainElement subDomainElement = new SubDomainElement();
			subDomainElements.add(subDomainElement);
			subDomainElement.setDisease(new Disease("disease" + i, true));
			subDomainElement.setSubDomain(new SubDomain(null, "subdomain" + i));
			subDomainElement.setDomain(new Domain(null, "domain" + i));
		}

		testDe.setSubDomainElementList(subDomainElements);

		// classification elements
		Set<ClassificationElement> classificationElements = new HashSet<ClassificationElement>();

		for (int i = 0; i < collectionSize; i++) {
			ClassificationElement ce = new ClassificationElement();
			ce.setClassification(new Classification("classification" + i, true, true));
			ce.setSubgroup(new Subgroup("subgroup" + i));
			classificationElements.add(ce);
		}

		testDe.setClassificationElementList(classificationElements);

		// External ID
		Set<ExternalId> externalIds = new HashSet<ExternalId>();
		Schema[] schemas = {new Schema("NINDS"), new Schema("LOINC"), new Schema("SNOMED"), new Schema("LAWL")};

		for (int i = 0; i < collectionSize; i++) {
			ExternalId ei = new ExternalId();
			ei.setValue("externalId" + i);
			ei.setSchema(schemas[i % (schemas.length)]);
			externalIds.add(ei);
		}

		testDe.setExternalIdSet(externalIds);

		// permissible values
		Set<ValueRange> permissibleValues = new HashSet<ValueRange>();

		for (int i = 0; i < collectionSize; i++) {
			ValueRange pv = new ValueRange("permissibleValue" + i, "permissibleValueDescription" + i);
			permissibleValues.add(pv);
		}

		testDe.setValueRangeList(permissibleValues);

		return testDe;
	}

	@Test
	public void testGetAll() {

		List<SemanticDataElement> dataElements = dataElementDao.getAll();
		Assert.assertTrue(!dataElements.isEmpty());
	}

	@Test
	public void testGetAllWithUntilDate() {

		List<SemanticDataElement> dataElements = dataElementDao.getAllWithUntilDate();

		for (SemanticDataElement de : dataElements) {
			Assert.assertNotNull(de.getUntilDate());
		}
	}
	
	@Test
	public void testGet() {

		SemanticDataElement dataElements =
				dataElementDao.get("http://ninds.nih.gov/dictionary/ibis/1.0/Element/DataElement/LabTestPrfmInd");
		// better way to test this?
	}

	@Test
	public void testSave() {

		final String deName = "testDe";
		final int collectionSize = 5;

		SemanticDataElement testDe = createTestDataElement(deName, collectionSize);
		testDe = dataElementDao.save(testDe);
		SemanticDataElement de = dataElementDao.get(testDe.getUri());

		Assert.assertTrue(de != null);
		Assert.assertEquals(de.getUri(), RDFConstants.ELEMENT_NS + deName);
		Assert.assertEquals(de.getTitle(), "title");
		Assert.assertEquals(de.getNotes(), "notes");
		Assert.assertEquals(de.getReferences(), "references");
		Assert.assertEquals(de.getDescription(), "description");
		Assert.assertEquals(de.getGuidelines(), "guidelines");
		Assert.assertEquals(de.getFormat(), "format");
		Assert.assertEquals(de.getShortDescription(), "shortDescription");
		Assert.assertEquals(de.getHistoricalNotes(), "historicalNotes");
		Assert.assertEquals(de.getPopulation().getName(), "population");
		Assert.assertEquals(de.getSubDomainElementList().size(), collectionSize);
		Assert.assertEquals(de.getClassificationElementList().size(), collectionSize);
		Assert.assertEquals(de.getExternalIdSet().size(), collectionSize);
		Assert.assertEquals(de.getValueRangeList().size(), collectionSize);
	}

	@Test
	public void testQueryUntilDate() {
		System.out.println(QueryConstructionUtil.getWithoutUntilDateQuery());
		System.out.println(QueryConstructionUtil.getWithUntilDateQuery());
	}
}
