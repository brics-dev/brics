package gov.nih.tbi.repository.dataimport;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.annotations.Test;

public class DataImportDataElementDataTest {

	@Test
	public void addStringToValuesTest() {
		DataImportDataElementData de = new DataImportDataElementData("test", "test", "test", Arrays.asList("Testing"), new ArrayList<Integer>(), false);
		de.addStringToValues(0, "testing2");
		
		assertEquals(de.getValue().get(0), "Testing;testing2");
	}
	
}
