package gov.nih.tbi.repository.rdf;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import gov.nih.tbi.repository.model.GenericTable;
import gov.nih.tbi.repository.model.GenericTableRow;

public class MdsUpdrsXResourceBuilderTest {
	@Test
	public void buildModelTest() {
		MdsUpdrsXResourceBuilder builder = new MdsUpdrsXResourceBuilder();
		GenericTable table = new GenericTable("Required Fields");
		GenericTableRow row1 = new GenericTableRow();
		row1.addCell("guid", "TBI123");
		GenericTableRow row2 = new GenericTableRow();
		row2.addCell("guid", "TBI456");
		table.addRow(row1);
		table.addRow(row2);

		builder.putTableResult("Required Fields", table);

		Model actual = builder.buildModel();
		assertTrue(actual.contains(
				ResourceFactory.createResource("http://ninds.nih.gov/repository/fitbir/1.0/Guid/TBI123"),
				ResourceFactory.createProperty("http://ninds.nih.gov/repository/fitbir/1.0/Guid/mdsUpdrsX"),
				ResourceFactory.createPlainLiteral("true")));
		assertTrue(actual.contains(
				ResourceFactory.createResource("http://ninds.nih.gov/repository/fitbir/1.0/Guid/TBI456"),
				ResourceFactory.createProperty("http://ninds.nih.gov/repository/fitbir/1.0/Guid/mdsUpdrsX"),
				ResourceFactory.createPlainLiteral("true")));
	}
}
