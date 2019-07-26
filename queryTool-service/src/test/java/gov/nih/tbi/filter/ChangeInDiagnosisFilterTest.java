package gov.nih.tbi.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRow;

public class ChangeInDiagnosisFilterTest {
	public ChangeInDiagnosisFilterTest() {}

	@Test
	public void testFactory() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		Filter f1 = FilterFactory.createChangeInDiagnosisFilter("test");
		ChangeInDiagnosisFilter f2 = new ChangeInDiagnosisFilter("test");

		Assert.assertEquals(f1, f2);
	}

	@Test
	public void testEquals() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		ChangeInDiagnosisFilter f1 = FilterFactory.createChangeInDiagnosisFilter("test");
		ChangeInDiagnosisFilter f2 = FilterFactory.createChangeInDiagnosisFilter("test");
		Assert.assertEquals(f1, f2);

		f2.setValue("test123");

		Assert.assertNotEquals(f1, f2);
	}

	@Test
	public void testHashCode() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		ChangeInDiagnosisFilter f1 = FilterFactory.createChangeInDiagnosisFilter("test");
		ChangeInDiagnosisFilter f2 = FilterFactory.createChangeInDiagnosisFilter("test");
		Assert.assertEquals(f1.hashCode(), f2.hashCode());

		f2.setValue("test123");

		Assert.assertNotEquals(f1.hashCode(), f2.hashCode());
	}

	@Test
	public void testToJson() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		ChangeInDiagnosisFilter f = FilterFactory.createChangeInDiagnosisFilter("test");
		JsonObject j = f.toJson();
		Assert.assertEquals("test", j.get("freeFormValue").getAsString());
		Assert.assertEquals(false, j.get("blank").getAsBoolean());
		Assert.assertEquals(FilterType.CHANGE_IN_DIAGNOSIS.name(), j.get("filterType").getAsString());
	}

	@Test
	public void testEval1() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		ChangeInDiagnosisFilter f = FilterFactory.createChangeInDiagnosisFilter("yes");
		InstancedRow row = mock(InstancedRow.class);
		when(row.isDoHighlight()).thenReturn(true);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}
	
	@Test
	public void testEval2() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		ChangeInDiagnosisFilter f = FilterFactory.createChangeInDiagnosisFilter("no");
		InstancedRow row = mock(InstancedRow.class);
		when(row.isDoHighlight()).thenReturn(true);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}
	
	@Test
	public void testEval3() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		ChangeInDiagnosisFilter f = FilterFactory.createChangeInDiagnosisFilter("");
		InstancedRow row = mock(InstancedRow.class);
		when(row.isDoHighlight()).thenReturn(true);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}
}
