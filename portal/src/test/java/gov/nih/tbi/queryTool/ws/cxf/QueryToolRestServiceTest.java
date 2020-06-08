package gov.nih.tbi.queryTool.ws.cxf;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

public class QueryToolRestServiceTest {

	@Test
	public void getFileExtensionTest() {
		QueryToolRestService service = new QueryToolRestService();
		String actual = service.getFileExtension("easy_test-123.csv");
		String expected = ".csv";
		assertEquals(actual, expected);
	}

	@Test
	public void getFileExtensionTest2() {
		QueryToolRestService service = new QueryToolRestService();
		String actual =
				service.getFileExtension("Non-admin_Bio-Demo-MDS-MoCA-Neuro ageyrs test_data_element_details.csv");
		String expected = ".csv";
		assertEquals(actual, expected);
	}

	@Test
	public void getFileExtensionTest3() {
		QueryToolRestService service = new QueryToolRestService();
		String actual = service.getFileExtension("Non-admin_Bio-Demo-MDS-MoCA-Neuro ageyrs test_data_element_details");
		String expected = null;
		assertEquals(actual, expected);
	}
}
