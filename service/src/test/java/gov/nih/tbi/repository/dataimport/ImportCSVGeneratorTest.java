package gov.nih.tbi.repository.dataimport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

public class ImportCSVGeneratorTest {
	
	@InjectMocks
	private ImportCSVGenerator generator = new ImportCSVGenerator();

	@Test
	public void isInArrayTest() {
		DataImportDataElementData de1 = new DataImportDataElementData("Group1", "row1", "RG1", null, new ArrayList<Integer>(Arrays.asList(0, 1, 2)), false);
		DataImportDataElementData de2 = new DataImportDataElementData("Group2", "row2", "RG2", null, null, false);
		DataImportDataElementData de3 = new DataImportDataElementData("Group3", "row3", "RG3", null, null, false);
		DataImportDataElementData de4 = new DataImportDataElementData("Group4", "row4", "RG4", null, null, false);
		DataImportDataElementData de5 = new DataImportDataElementData("Group5", "row5", "RG5", null, new ArrayList<Integer>(Arrays.asList(0, 1, 2)), false);
		DataImportDataElementData de6 = new DataImportDataElementData("Group6", "row6", "RG6", null, null, false);
		
		List<DataImportDataElementData> deList = new ArrayList<>();
		
		deList.add(de1);
		deList.add(de2);
		deList.add(de3);
		deList.add(de4);
		deList.add(de5);
		deList.add(de6);
		
		assertEquals(generator.isInArray(deList, "Group1"), 0);
		assertNotEquals(generator.isInArray(deList, "Group3"), 0);
	}
	
	@Test
	public void returnIndexTest() {
		List<Integer> intList = Arrays.asList(0,1,2,5,6,7);
			
		assertEquals(generator.returnIndex(intList, "2"), 2);
		assertNotEquals(generator.returnIndex(intList, "2"), 3);
		assertEquals(generator.returnIndex(intList, "11"), -1);
	}
	
	@Test
	public void isUniqueRGNameTest() {
		
		DataImportRepeatableGroupData rg1 = new DataImportRepeatableGroupData("RG 1", "ONE", 1);
		DataImportRepeatableGroupData rg2 = new DataImportRepeatableGroupData("RG 2", "TWO", 2);
		DataImportRepeatableGroupData rg3 = new DataImportRepeatableGroupData("RG 3", "THREE", 3);
		DataImportRepeatableGroupData rg4 = new DataImportRepeatableGroupData("RG 4", "FOUR", 4);
		DataImportRepeatableGroupData rg5 = new DataImportRepeatableGroupData("RG 5", "FIVE", 5);
		DataImportRepeatableGroupData rg6 = new DataImportRepeatableGroupData("RG 6", "SIX", 6);
		DataImportRepeatableGroupData rg7 = new DataImportRepeatableGroupData("RG 7", "SEVEN", 7);
		
		List<DataImportRepeatableGroupData> rgList = new ArrayList<>();
		
		rgList.add(rg1);
		rgList.add(rg2);
		rgList.add(rg3);
		rgList.add(rg4);
		rgList.add(rg5);
		rgList.add(rg6);
		rgList.add(rg7);
		
		assertFalse(generator.isUniqueRGName(rgList, "RG 1"));
		assertTrue(generator.isUniqueRGName(rgList, "RG 8"));
				
	}

	@Test
	public void formattedListToStringTest() {
		List<String> list = new ArrayList<>();
		list.add("Test1");
		list.add("Test2");
		
		String result = generator.formattedListToString(list);
		
		assertEquals(result, "Test1, Test2");
		
	}
	@Test
	public void fourHoursTest() {
		String dateString = "2019-08-28 11:09:45";
		String result = generator.fixUTCDate(dateString);
		
		assertEquals(result, "2019-08-28 15:09:45");
		
	}
}
