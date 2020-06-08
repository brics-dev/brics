package gov.nih.tbi.filter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.testng.Assert;

import com.google.gson.JsonObject;

import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.repository.model.InstancedRow;



public class DatasetFilterTest {
	public DatasetFilterTest() {}
	
	@Test
	public void testToJson1() {
		
		DatasetFilter datasetFilter = new DatasetFilter("datasetFilterName","FITBIR-DATA0006221",null,null,null);
		
		JsonObject datasetFilterJson = datasetFilter.toJson();
		
		Assert.assertEquals("datasetFilterName", datasetFilterJson.get("name").getAsString());
		Assert.assertEquals("FITBIR-DATA0006221", datasetFilterJson.get("freeFormValue").getAsString());
		Assert.assertEquals(FilterType.DATASET.name(), datasetFilterJson.get("filterType").getAsString());
	}
	
	@Test
	public void testToJson2() {
		
		DatasetFilter datasetFilter = new DatasetFilter("datasetFilterName","FITBIR-DATA0006221",null,null,null);
		
		JsonObject datasetFilterJson = datasetFilter.toJson();
		
		Assert.assertNotEquals("FITBIR-DATA0006221", datasetFilterJson.get("name").getAsString());
		Assert.assertNotEquals("datasetFilterName", datasetFilterJson.get("freeFormValue").getAsString());
		Assert.assertNotEquals(FilterType.DATE.name(), datasetFilterJson.get("filterType").getAsString());
		
	}
	
	@Test
	public void testEvaluate1() {
		
		DatasetFilter datasetFilter = new DatasetFilter("datasetFilterName","FITBIR-DATA0006221",null,null,null);
		
		InstancedRow row = mock(InstancedRow.class);
		when(row.getReadableDatasetId()).thenReturn("FITBIR-DATA0006221");
		
		assertTrue(datasetFilter.evaluate(row));
		
	}
	
	@Test
	public void testEvaluate2() {
		
		DatasetFilter datasetFilter = new DatasetFilter("datasetFilterName","FITBIR-DATA0006401",null,null,null);
		
		InstancedRow row = mock(InstancedRow.class);
		when(row.getReadableDatasetId()).thenReturn("FITBIR-DATA0006221");
		
		assertFalse(datasetFilter.evaluate(row));
		
	}
	
	@Test
	public void testEvaluate3() {
		
		DatasetFilter datasetFilter = new DatasetFilter("datasetFilterName","FITBIR-DATA0006401",null,null,null);
		
		InstancedRow row = mock(InstancedRow.class);
		when(row.getReadableDatasetId()).thenReturn(null);
		
		assertFalse(datasetFilter.evaluate(row));
	}
	
	@Test
	public void testEvaluate4() {
		
		DatasetFilter datasetFilter = new DatasetFilter("datasetFilterName","",null,null,null);
		
		InstancedRow row = mock(InstancedRow.class);
		when(row.getReadableDatasetId()).thenReturn("FITBIR-DATA0006221");
		
		assertTrue(datasetFilter.evaluate(row));
	}
	
	@Test
	public void testEvaluate5() {
		
		DatasetFilter datasetFilter = new DatasetFilter("datasetFilterName",null,null,null,null);
		
		InstancedRow row = mock(InstancedRow.class);
		when(row.getReadableDatasetId()).thenReturn("FITBIR-DATA0006221");
		
		assertTrue(datasetFilter.evaluate(row));
		
	}
	
	@Test
	public void testEvaluate6() {
		
        DatasetFilter datasetFilter = new DatasetFilter("datasetFilterName","FITBIR-DATA0006401;FITBIR-DATA0006221",null,null,null);
		
		InstancedRow row = mock(InstancedRow.class);
		when(row.getReadableDatasetId()).thenReturn("FITBIR-DATA0006221");
		
		assertTrue(datasetFilter.evaluate(row));
		
	}
	
	@Test
	public void testEvaluate7() {
		
        DatasetFilter datasetFilter = new DatasetFilter("datasetFilterName","FITBIR-DATA0006401;FITBIR-DATA0006221",null,null,null);
		
		InstancedRow row = mock(InstancedRow.class);
		when(row.getReadableDatasetId()).thenReturn("FITBIR-DATA0007378");
		
		assertFalse(datasetFilter.evaluate(row));
	}
	
	@Test
	public void testHashCode() {
		
		DatasetFilter datasetFilter1 = new DatasetFilter("datasetFilterName","FITBIR-DATA0006401",null,null,null);
		DatasetFilter datasetFilter2 = new DatasetFilter("datasetFilterName","FITBIR-DATA0006401",null,null,null);
		
		Assert.assertEquals(datasetFilter1.hashCode(), datasetFilter2.hashCode());
		
		datasetFilter1.setDelimitedValues("FITBIR-DATA0006221");
		Assert.assertNotEquals(datasetFilter1.hashCode(), datasetFilter2.hashCode());
			
	}
	
	@Test
	public void testEquals() {
		
		DatasetFilter datasetFilter1 = new DatasetFilter("datasetFilterName","FITBIR-DATA0006401",null,null,null);
		DatasetFilter datasetFilter2 = new DatasetFilter("datasetFilterName","FITBIR-DATA0006401",null,null,null);
		
		Assert.assertEquals(datasetFilter1, datasetFilter2);
		
		datasetFilter1.setDelimitedValues("FITBIR-DATA0006221");
		Assert.assertNotEquals(datasetFilter1, datasetFilter2);
		
	}

}
