package gov.nih.tbi.commons.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.query.model.hibernate.SavedQuery;

public class SavedQueryUtilTest {

  private static SavedQueryUtil savedQueryUtil;

@Test
  public void jsonToSavedQueryWithNoQuery() {
	  JsonObject jo = new JsonObject();
	  jo.addProperty("id", 1);
	  jo.addProperty("name", "sq1");
	  jo.addProperty("description", "sq1 description");
	  Date lastUpdated = new Date();
	  String lastUpdatedString = BRICSTimeDateUtil.dateToDateTimeString(new Date());
	  jo.addProperty("lastUpdated", lastUpdatedString);
	  jo.addProperty("dateCreated", "2019-07-30T00:00:00-04:00");
	  jo.addProperty("copyFlag", true);
	  jo.addProperty("outputCode", "Permissible Value");
	  SavedQuery sq  = SavedQueryUtil.jsonToSavedQueryWithNoQuery(jo);
	  //{"id":328,"name":"ryan_7_26_19","description":"created date test mode test","copyFlag":false,"lastUpdated":"2019-07-29T19:19:38-04:00","dateCreated":"2019-07-26T15:11:14-04:00","linkedUsers":[{"id":967,"userName":"andrea1","firstName":"Andrea1","lastName":"abc","email":"dcb@dcbstage.com","assignedPermission":{"entityMapId":2513821,"entityId":328,"permission":"Read","selected":false},"permissions":[{"entityMapId":-1,"entityId":-1,"permission":"Read","selected":true},{"entityMapId":-1,"entityId":-1,"permission":"Write","selected":false},{"entityMapId":-1,"entityId":-1,"permission":"Admin","selected":false},{"entityMapId":-1,"entityId":-1,"permission":"Owner","selected":false}],"disabled":false},{"id":2,"userName":"administrator","firstName":"Portal","lastName":"Admin","email":"bricsadmins@nih.gov","assignedPermission":{"entityMapId":2513822,"entityId":328,"permission":"Owner","selected":false},"permissions":[{"entityMapId":-1,"entityId":-1,"permission":"Read","selected":false},{"entityMapId":-1,"entityId":-1,"permission":"Write","selected":false},{"entityMapId":-1,"entityId":-1,"permission":"Admin","selected":false},{"entityMapId":-1,"entityId":-1,"permission":"Owner","selected":true}],"disabled":false}],"editMode":"edit","oldName":"ryan_7_26_19"}
		
		
		SavedQuery expectedSq = new SavedQuery();
		expectedSq.setId(1L);
		expectedSq.setName("sq1");
		expectedSq.setDescription("sq1 description");
		expectedSq.setLastUpdated(lastUpdated);
		expectedSq.setDateCreated(BRICSTimeDateUtil.stringToDate("2019-07-30T00:00:00-04:00"));
		expectedSq.setCopyFlag(true);
		expectedSq.setOutputCode("Permissible Value");
		Assert.assertEquals(sq, expectedSq);

		
  }
  
  @BeforeClass
  public static void beforeClass() {
	  savedQueryUtil = new SavedQueryUtil();
  }

  @Test
  public void savedQueryToJsonNoPermissions() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	  SavedQuery sq = new SavedQuery();
	  Date lastUpdated = new Date();
	  String lastUpdatedString = BRICSTimeDateUtil.dateToDateTimeString(new Date());
		sq.setId(1L);
		sq.setName("sq1");
		sq.setDescription("sq1 description");
		sq.setLastUpdated(lastUpdated);
		sq.setDateCreated(BRICSTimeDateUtil.stringToDate("2019-07-30T00:00:00-04:00"));
		sq.setCopyFlag(true);
	  
		Method method = SavedQueryUtil.class.getDeclaredMethod("savedQueryToJsonNoPermissions",  SavedQuery.class);
        method.setAccessible(true);
        JsonObject output = (JsonObject) method.invoke(savedQueryUtil, sq);
	  
	  JsonObject expectedOutput = new JsonObject();

	  expectedOutput.addProperty("id", 1);
	  expectedOutput.addProperty("name","sq1");
	  expectedOutput.addProperty("description", "sq1 description");
	  expectedOutput.addProperty("copyFlag", true);
	  expectedOutput.addProperty("lastUpdated", lastUpdatedString);
	  expectedOutput.addProperty("dateCreated", "2019-07-30T00:00:00-04:00");

		Assert.assertEquals(output, expectedOutput);
  }

 /* 
  * Removing this test for now. I'm pretty sure we are not using XML for saved queries anymore. We might need to do a clean up
  * @Test
  public void savedQueryXmlToJson() throws Exception {
	  SavedQuery sq = new SavedQuery();
	  Date lastUpdated = new Date();
	  String lastUpdatedString = BRICSTimeDateUtil.dateToDateTimeString(new Date());
		sq.setId(1L);
		sq.setName("sq1");
		sq.setDescription("sq1 description");
		sq.setLastUpdated(lastUpdated);
		sq.setDateCreated(BRICSTimeDateUtil.stringToDate("2019-07-30T00:00:00-04:00"));
		sq.setCopyFlag(true);
	  
		
        JsonObject output = SavedQueryUtil.savedQueryXmlToJson(sq);
	  
	  JsonObject expectedOutput = new JsonObject();

	  expectedOutput.addProperty("id", 1);
	  expectedOutput.addProperty("name","sq1");
	  expectedOutput.addProperty("description", "sq1 description");
	  expectedOutput.addProperty("copyFlag", true);
	  expectedOutput.addProperty("lastUpdated", lastUpdatedString);
	  expectedOutput.addProperty("dateCreated", "2019-07-30T00:00:00-04:00");

		Assert.assertEquals(output, expectedOutput);
  }*/
}
