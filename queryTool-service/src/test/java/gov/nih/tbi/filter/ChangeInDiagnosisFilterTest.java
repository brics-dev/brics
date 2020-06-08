package gov.nih.tbi.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.repository.model.InstancedRow;

public class ChangeInDiagnosisFilterTest {
	public ChangeInDiagnosisFilterTest() {}

	@Test
	public void testEquals() {
		ChangeInDiagnosisFilter f1 = new ChangeInDiagnosisFilter("test", "f1", null, null, null);
		ChangeInDiagnosisFilter f2 = new ChangeInDiagnosisFilter("test", "f1", null, null, null);
		Assert.assertEquals(f1, f2);

		f2.setValue("test123");

		Assert.assertNotEquals(f1, f2);
	}

	@Test
	public void testHashCode() {
		ChangeInDiagnosisFilter f1 = new ChangeInDiagnosisFilter("test", "f1", null, null, null);
		ChangeInDiagnosisFilter f2 = new ChangeInDiagnosisFilter("test", "f1", null, null, null);
		Assert.assertEquals(f1.hashCode(), f2.hashCode());

		f2.setValue("test123");

		Assert.assertNotEquals(f1.hashCode(), f2.hashCode());
	}

	@Test
	public void testToJson() {
		ChangeInDiagnosisFilter f = new ChangeInDiagnosisFilter("test", "f", null, null, null);
		JsonObject j = f.toJson();
		Assert.assertEquals("test", j.get("freeFormValue").getAsString());
		Assert.assertEquals(FilterType.CHANGE_IN_DIAGNOSIS.name(), j.get("filterType").getAsString());
	}

	@Test
	public void testEval1() {
		ChangeInDiagnosisFilter f = new ChangeInDiagnosisFilter("yes", "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		when(row.isDoHighlight()).thenReturn(true);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testEval2() {
		ChangeInDiagnosisFilter f = new ChangeInDiagnosisFilter("no", "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		when(row.isDoHighlight()).thenReturn(true);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void testEval3() {
		ChangeInDiagnosisFilter f = new ChangeInDiagnosisFilter("", "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		when(row.isDoHighlight()).thenReturn(true);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testYesToString() {
		ChangeInDiagnosisFilter f = new ChangeInDiagnosisFilter("Yes", "f", null, null, null);
		String actualString = f.toString();
		String expectedString = "(f = 'Yes')";
		assertEquals(actualString, expectedString);
	}

	@Test
	public void testNoToString() {
		ChangeInDiagnosisFilter f = new ChangeInDiagnosisFilter("No", "f", null, null, null);
		String actualString = f.toString();
		String expectedString = "(f = 'No')";
		assertEquals(actualString, expectedString);
	}

	@Test
	public void testEmptyToString() {
		ChangeInDiagnosisFilter f = new ChangeInDiagnosisFilter(null, "f", null, null, null);
		String actualString = f.toString();
		String expectedString = "";
		assertEquals(actualString, expectedString);
	}
}
