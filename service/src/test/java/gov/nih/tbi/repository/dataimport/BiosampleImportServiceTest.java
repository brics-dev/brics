package gov.nih.tbi.repository.dataimport;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;


public class BiosampleImportServiceTest {
	
	private BiosampleImportService service = new BiosampleImportService();
	
	@Test
	public void formattedListToStringTest() {
		
		List<String> testList = new ArrayList<>();
		testList.add("Test1");
		testList.add("Test2");
		
		assertEquals(service.formattedListToString(testList), "Test1, Test2");
		
		
	}
	
	@Test
	public void parseFileToPojoTest() {
		
		String s = "1,AJPD,CSF,Baseline,PDBP-STUDY00001,GUID,200,ul,4,Y,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2019-12-17 07:13:33";
		List<BiosampleCSVData> parseList = service.parseFileToPojo(s);
		
		List<BiosampleCSVData> constructorList = new ArrayList<>();
		
		BiosampleCSVData record = new BiosampleCSVData("1", "AJPD", "CSF","Baseline", "PDBP-STUDY00001", "GUID", 200, "ul", 4, "Y",0,"0",0,0,0,0,"0","0","0",0,"0","0","0",0,"2019-12-17 07:13:33");
		
		constructorList.add(record);
		
		assertEquals(parseList, constructorList);
	}
	
	
	@Test
	public void updateBiosampleRecordsTestUpdateRepositoryName() {
		
		BiosampleCSVData recordNormal = new BiosampleCSVData("1", "AJPD", "CSF","Baseline", "PDBP-STUDY00001", "GUID", 200, "ul", 4, "Y",0,"0",0,0,0,0,"0","0","0",0,"0","0","0",0,"2019-12-17 07:13:33");


		BiosampleCSVData recordNINDS = new BiosampleCSVData("1", "NINDS", "CSF","Baseline", "PDBP-STUDY00001", "GUID", 200, "ul", 4, "Y",0,"0",0,0,0,0,"0","0","0",0,"0","0","0",0,"2019-12-17 07:13:33");
		
		
		List<BiosampleCSVData> fullList = new ArrayList<>();
		
		fullList.add(recordNormal);
		

		List<BiosampleCSVData> updatedList = service.updateBiosampleRecords(fullList);
		
		assertEquals(updatedList.get(0).getRepositoryName(), recordNINDS.getRepositoryName());
		
	}

}
