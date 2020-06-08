package gov.nih.tbi.util;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.repository.model.DataTableColumn;
import org.testng.Assert;
import org.testng.annotations.Test;

public class InstancedRecordComparatorTest {
	@Test
	public void compareAuxDecimal() {
		String value1 = "1.25";
		String value2 = "2.15";

		InstancedRecordComparator comparator =
				new InstancedRecordComparator(new DataTableColumn("form", "rg", "de"), QueryToolConstants.ASCENDING);
		int actual = comparator.compareAux(value1, value2);

		Assert.assertTrue(actual < 0);
	}

	@Test
	public void compareAuxDecimal2() {
		String value1 = "1.25";
		String value2 = "1.25";

		InstancedRecordComparator comparator =
				new InstancedRecordComparator(new DataTableColumn("form", "rg", "de"), QueryToolConstants.ASCENDING);
		int actual = comparator.compareAux(value1, value2);

		Assert.assertTrue(actual == 0);
	}

	@Test
	public void compareAuxDecimal3() {
		String value1 = "2.35";
		String value2 = "2.15";

		InstancedRecordComparator comparator =
				new InstancedRecordComparator(new DataTableColumn("form", "rg", "de"), QueryToolConstants.ASCENDING);
		int actual = comparator.compareAux(value1, value2);

		Assert.assertTrue(actual > 0);
	}

	@Test
	public void compareAuxAlpha() {
		String value1 = "ABC";
		String value2 = "CDE";

		InstancedRecordComparator comparator =
				new InstancedRecordComparator(new DataTableColumn("form", "rg", "de"), QueryToolConstants.ASCENDING);
		int actual = comparator.compareAux(value1, value2);
		Assert.assertTrue(actual < 0);
	}

	@Test
	public void compareAuxAlpha2() {
		String value1 = "EFG";
		String value2 = "CDE";

		InstancedRecordComparator comparator =
				new InstancedRecordComparator(new DataTableColumn("form", "rg", "de"), QueryToolConstants.ASCENDING);
		int actual = comparator.compareAux(value1, value2);
		Assert.assertTrue(actual > 0);
	}

	@Test
	public void compareAuxAlpha3() {
		String value1 = "ABC";
		String value2 = "ABC";

		InstancedRecordComparator comparator =
				new InstancedRecordComparator(new DataTableColumn("form", "rg", "de"), QueryToolConstants.ASCENDING);
		int actual = comparator.compareAux(value1, value2);

		Assert.assertTrue(actual == 0);
	}

	@Test
	public void compareAuxGuid() {
		String value1 = "PDBB001EMM";
		String value2 = "PDKW000LTV";

		InstancedRecordComparator comparator =
				new InstancedRecordComparator(new DataTableColumn("form", "rg", "de"), QueryToolConstants.ASCENDING);
		int actual = comparator.compareAux(value1, value2);

		Assert.assertTrue(actual < 0);
	}

	@Test
	public void compareAuxGuid2() {
		String value1 = "PDKW000LTV";
		String value2 = "PDKW000LTV";

		InstancedRecordComparator comparator =
				new InstancedRecordComparator(new DataTableColumn("form", "rg", "de"), QueryToolConstants.ASCENDING);
		int actual = comparator.compareAux(value1, value2);

		Assert.assertTrue(actual == 0);
	}

	@Test
	public void compareAuxGuid3() {
		String value1 = "PDKW000LTV";
		String value2 = "PDBB001EMM";

		InstancedRecordComparator comparator =
				new InstancedRecordComparator(new DataTableColumn("form", "rg", "de"), QueryToolConstants.ASCENDING);
		int actual = comparator.compareAux(value1, value2);

		Assert.assertTrue(actual > 0);
	}
}
