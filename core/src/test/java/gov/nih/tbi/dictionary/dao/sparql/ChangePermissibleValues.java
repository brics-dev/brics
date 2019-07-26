package gov.nih.tbi.dictionary.dao.sparql;

import java.util.Set;

import gov.nih.tbi.dictionary.dao.DataElementSparqlDao;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChangePermissibleValues {
	private DataElementSparqlDao dataElementSparqlDao;
	private ApplicationContext ctx;

	@BeforeMethod
	protected void setUp() {

		ctx = new ClassPathXmlApplicationContext("test-context.xml");
		dataElementSparqlDao = ctx.getBean("dataElementSparqlDao", DataElementSparqlDao.class);

	}

	@Test
	public void updatePermissibleValues() {
		SemanticDataElement de = dataElementSparqlDao.getByNameAndVersion("ImgFMRITaskTyp", "1.0");

		Set<ValueRange> vrs = de.getValueRangeList();
		
		ValueRange newVr = new ValueRange();
		newVr.setValueRange("Retinopathy");
		newVr.setDescription("Retinopathy");
		vrs.add(newVr);
		
		dataElementSparqlDao.save(de);
	}
	//
	// private void updatePermissibleValue(String name, String valueRange, String description, Integer outputCode) {
	// SemanticDataElement de = dataElementDao.getByNameAndVersion("CurntAcademicYr", "1.2");
	// ValueRange vr = new ValueRange();
	// vr.setValueRange(valueRange);
	// vr.setDescription(description);
	// vr.setOutputCode(outputCode);
	// de.addValueRange(vr);
	// dataElementDao.save(de);
	// }
	//
	// @Test
	// public void addPermissibleValuesCsv() {
	// File csvFile = new File("C:\\Users\\fchen\\Desktop\\pv.csv");
	// BufferedReader reader = null;
	//
	// try {
	// reader = new BufferedReader(new FileReader(csvFile));
	// String line = null;
	//
	// while ((line = reader.readLine()) != null) {
	// String[] parsedLine = line.split(",");
	// String value = parsedLine[0];
	// String description = parsedLine[1];
	// String outputCode = parsedLine[2];
	//
	// updatePermissibleValue(value, description, description, Integer.valueOf(outputCode));
	// System.out.println(value + ", " + description + ", " + outputCode);
	// }
	// } catch (IOException e) {
	//
	// }
	// }

}
