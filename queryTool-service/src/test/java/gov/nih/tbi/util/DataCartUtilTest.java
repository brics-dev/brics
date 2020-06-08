package gov.nih.tbi.util;


import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.service.model.DataCart;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DataCartUtilTest {

	/* Test for getting dataCart Saved Qeury Object to Json format */
	@Test
	public void getDataCartToSavedQueryJson() {

		SavedQuery sq = new SavedQuery();
		sq.setName("sq1");
		sq.setDescription("sq1 description");
		Date lastUpdated = new Date();
		sq.setLastUpdated(lastUpdated);
		sq.setDateCreated(BRICSTimeDateUtil.stringToDate("2019-07-30"));
		sq.setOutputCode("Permissible Value");
		DataCart dataCart = mock(DataCart.class);
		
		List<String> selectedFormUrisTest = new ArrayList<>(); 
		selectedFormUrisTest.add("form1");
		dataCart.setSelectedFormUris(selectedFormUrisTest);
		
		InstancedDataTable mockDataTable = new InstancedDataTable();
		mockDataTable.setFilterExpression("");
		when(dataCart.getInstancedDataTable()).thenReturn(mockDataTable);
		
		JsonObject output = DataCartUtil.getDataCartToSavedQueryJson(dataCart, sq);
		
		JsonObject expectedOutput = new JsonObject();
		expectedOutput.addProperty("name", "sq1");
		expectedOutput.addProperty("description", "sq1 description");
		expectedOutput.addProperty("lastUpdated", lastUpdated.toString());
		expectedOutput.addProperty("dateCreated", "Tue Jul 30 00:00:00 EDT 2019");
		expectedOutput.addProperty("outputCode", "Permissible Value");
		expectedOutput.addProperty("filterExpression", new String(""));
		JsonArray sForms = new JsonArray();
		expectedOutput.add("selectedFormURIList", sForms);
		expectedOutput.add("forms", new JsonArray());
		expectedOutput.add("studies", new JsonArray());
		

		Assert.assertEquals(output, expectedOutput);

	
	}

}
