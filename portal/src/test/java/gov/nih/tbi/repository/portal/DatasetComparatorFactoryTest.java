package gov.nih.tbi.repository.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DatasetComparatorFactoryTest {

	@Test
	public void testPrefixedId() throws InvalidColumnException {
		Dataset one = new Dataset();
		one.setPrefixedId("TEST-DATASET00001");

		Dataset two = new Dataset();
		two.setPrefixedId("TEST-DATASET00002");

		Dataset three = new Dataset();
		three.setPrefixedId("TEST-DATASET00003");

		List<Dataset> testList = new ArrayList<Dataset>();
		testList.add(three);
		testList.add(one);
		testList.add(two);

		Comparator<Dataset> comparator = DatasetComparatorFactory.getComparator("prefixId");

		Collections.sort(testList, comparator);

		Assert.assertEquals(testList.get(0), one);
		Assert.assertEquals(testList.get(1), two);
		Assert.assertEquals(testList.get(2), three);
	}

	@Test
	public void testName() throws InvalidColumnException {
		Dataset one = new Dataset();
		one.setName("a");

		Dataset two = new Dataset();
		two.setName("b");

		Dataset three = new Dataset();
		three.setName("c");

		List<Dataset> testList = new ArrayList<Dataset>();
		testList.add(three);
		testList.add(one);
		testList.add(two);

		Comparator<Dataset> comparator = DatasetComparatorFactory.getComparator("name");

		Collections.sort(testList, comparator);

		Assert.assertEquals(testList.get(0), one);
		Assert.assertEquals(testList.get(1), two);
		Assert.assertEquals(testList.get(2), three);
	}

	@Test
	public void testType() throws InvalidColumnException {
		Dataset one = new Dataset();
		Set<SubmissionType> type1 = new HashSet<>();
		type1.add(SubmissionType.ACCOUNT_FILE);
		one.setSubmissionTypes(type1);

		Dataset two = new Dataset();
		Set<SubmissionType> type2 = new HashSet<>();
		type2.add(SubmissionType.CLINICAL);
		two.setSubmissionTypes(type2);

		Dataset three = new Dataset();
		Set<SubmissionType> type3 = new HashSet<>();
		type3.add(SubmissionType.DATA_CART_FILE);
		three.setSubmissionTypes(type3);

		List<Dataset> testList = new ArrayList<Dataset>();
		testList.add(three);
		testList.add(one);
		testList.add(two);

		Comparator<Dataset> comparator = DatasetComparatorFactory.getComparator("type");

		Collections.sort(testList, comparator);

		Assert.assertEquals(testList.get(0), one);
		Assert.assertEquals(testList.get(1), two);
		Assert.assertEquals(testList.get(2), three);
	}

	@Test
	public void testStatus() throws InvalidColumnException {
		Dataset one = new Dataset();
		one.setDatasetStatus(DatasetStatus.ARCHIVED);

		Dataset two = new Dataset();
		two.setDatasetStatus(DatasetStatus.DELETED);

		Dataset three = new Dataset();
		three.setDatasetStatus(DatasetStatus.SHARED);

		List<Dataset> testList = new ArrayList<Dataset>();
		testList.add(three);
		testList.add(one);
		testList.add(two);

		Comparator<Dataset> comparator = DatasetComparatorFactory.getComparator("status");

		Collections.sort(testList, comparator);

		Assert.assertEquals(testList.get(0), one);
		Assert.assertEquals(testList.get(1), two);
		Assert.assertEquals(testList.get(2), three);
	}

	@Test
	public void testRecordCount() {
		Dataset one = new Dataset();
		one.setRecordCount(0L);

		Dataset two = new Dataset();
		two.setRecordCount(1L);

		Dataset three = new Dataset();
		three.setRecordCount(2L);

		List<Dataset> testList = new ArrayList<Dataset>();
		testList.add(three);
		testList.add(one);
		testList.add(two);

		Comparator<Dataset> comparator = null;
		try {
			comparator = DatasetComparatorFactory.getComparator("recordCount");
		} catch (InvalidColumnException e) {
			e.printStackTrace();
		}

		Collections.sort(testList, comparator);

		Assert.assertEquals(testList.get(0), one);
		Assert.assertEquals(testList.get(1), two);
		Assert.assertEquals(testList.get(2), three);
	}

	@Test
	public void testRecordCountNull() throws InvalidColumnException {
		Dataset one = new Dataset();
		one.setRecordCount(0L);

		Dataset two = new Dataset();
		two.setRecordCount(1L);

		Dataset three = new Dataset();
		three.setRecordCount(2L);

		Dataset four = new Dataset();

		List<Dataset> testList = new ArrayList<Dataset>();
		testList.add(three);
		testList.add(one);
		testList.add(two);
		testList.add(four);

		Comparator<Dataset> comparator = DatasetComparatorFactory.getComparator("recordCount");

		Collections.sort(testList, comparator);

		Assert.assertEquals(testList.get(0), four);
		Assert.assertEquals(testList.get(1), one);
		Assert.assertEquals(testList.get(2), two);
		Assert.assertEquals(testList.get(3), three);
	}

	@Test
	public void testSubmitDate() throws InvalidColumnException {
		Dataset one = new Dataset();
		one.setSubmitDate(BRICSTimeDateUtil.parseTwoDigitSlashDate("05/14/89"));

		Dataset two = new Dataset();
		two.setSubmitDate(BRICSTimeDateUtil.parseTwoDigitSlashDate("05/17/89"));

		Dataset three = new Dataset();
		three.setSubmitDate(BRICSTimeDateUtil.parseTwoDigitSlashDate("05/19/89"));

		List<Dataset> testList = new ArrayList<Dataset>();
		testList.add(three);
		testList.add(one);
		testList.add(two);

		Comparator<Dataset> comparator = DatasetComparatorFactory.getComparator("submitDate");

		Collections.sort(testList, comparator);

		Assert.assertEquals(testList.get(0), one);
		Assert.assertEquals(testList.get(1), two);
		Assert.assertEquals(testList.get(2), three);
	}

	@Test
	public void testSubmitDateNull() throws InvalidColumnException {
		Dataset one = new Dataset();
		one.setSubmitDate(BRICSTimeDateUtil.parseTwoDigitSlashDate("05/14/89"));

		Dataset two = new Dataset();
		two.setSubmitDate(BRICSTimeDateUtil.parseTwoDigitSlashDate("05/17/89"));

		Dataset three = new Dataset();
		three.setSubmitDate(BRICSTimeDateUtil.parseTwoDigitSlashDate("05/19/89"));

		Dataset four = new Dataset();

		List<Dataset> testList = new ArrayList<Dataset>();
		testList.add(three);
		testList.add(one);
		testList.add(two);
		testList.add(four);

		Comparator<Dataset> comparator = DatasetComparatorFactory.getComparator("submitDate");

		Collections.sort(testList, comparator);

		Assert.assertEquals(testList.get(0), four);
		Assert.assertEquals(testList.get(1), one);
		Assert.assertEquals(testList.get(2), two);
		Assert.assertEquals(testList.get(3), three);
	}
}
