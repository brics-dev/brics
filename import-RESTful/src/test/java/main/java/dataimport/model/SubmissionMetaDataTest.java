package main.java.dataimport.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;

public class SubmissionMetaDataTest {

	@Test
	public void constructorTest() {
		Date expectedLockDate = new Date();

		SubmissionMetaData expected =
				new SubmissionMetaData("userEmail", "datasetName", "studyPrefixedId", "submissionLocation",
						"submissionFileLocation", 1L, BRICSTimeDateUtil.repositoryDateToString(expectedLockDate), 0L);

		List<String> metaDataLine = new ArrayList<>();
		metaDataLine.add("userEmail");
		metaDataLine.add("datasetName");
		metaDataLine.add("studyPrefixedId");
		metaDataLine.add("submissionLocation");
		metaDataLine.add("submissionFileLocation");
		metaDataLine.add("1");
		metaDataLine.add(BRICSTimeDateUtil.repositoryDateToString(expectedLockDate));
		metaDataLine.add("0");

		SubmissionMetaData test = new SubmissionMetaData(metaDataLine);
		Assert.assertEquals(expected, test);
	}

	@Test
	public void constructorTest2() {
		Date expectedLockDate = new Date();

		SubmissionMetaData expected = new SubmissionMetaData("userEmail", "datasetName", "studyPrefixedId",
				"submissionLocation", "submissionFileLocation", null,
				BRICSTimeDateUtil.repositoryDateToString(expectedLockDate), null);

		List<String> metaDataLine = new ArrayList<>();
		metaDataLine.add("userEmail");
		metaDataLine.add("datasetName");
		metaDataLine.add("studyPrefixedId");
		metaDataLine.add("submissionLocation");
		metaDataLine.add("submissionFileLocation");
		metaDataLine.add("null");
		metaDataLine.add(BRICSTimeDateUtil.repositoryDateToString(expectedLockDate));
		metaDataLine.add("null");

		SubmissionMetaData test = new SubmissionMetaData(metaDataLine);
		Assert.assertEquals(expected, test);
	}
	
	@Test
	public void constructorTest3() {
		SubmissionMetaData expected = new SubmissionMetaData("userEmail", "datasetName", "studyPrefixedId",
				"submissionLocation", "submissionFileLocation", null, null, null);

		List<String> metaDataLine = new ArrayList<>();
		metaDataLine.add("userEmail");
		metaDataLine.add("datasetName");
		metaDataLine.add("studyPrefixedId");
		metaDataLine.add("submissionLocation");
		metaDataLine.add("submissionFileLocation");

		SubmissionMetaData test = new SubmissionMetaData(metaDataLine);
		Assert.assertEquals(expected, test);
	}

	@Test
	public void subjectIsSubmittedTest() {
		Date expectedLockDate = new Date();

		List<String> metaDataLine = new ArrayList<>();
		metaDataLine.add("userEmail");
		metaDataLine.add("datasetName");
		metaDataLine.add("studyPrefixedId");
		metaDataLine.add("submissionLocation");
		metaDataLine.add("submissionFileLocation");
		metaDataLine.add("1");
		metaDataLine.add(BRICSTimeDateUtil.repositoryDateToString(expectedLockDate));
		metaDataLine.add("-1");

		SubmissionMetaData test = new SubmissionMetaData(metaDataLine);
		Assert.assertTrue(test.isSubjectSubmitted());
	}
	
	@Test
	public void subjectIsSubmittedTest2() {
		Date expectedLockDate = new Date();

		List<String> metaDataLine = new ArrayList<>();
		metaDataLine.add("userEmail");
		metaDataLine.add("datasetName");
		metaDataLine.add("studyPrefixedId");
		metaDataLine.add("submissionLocation");
		metaDataLine.add("submissionFileLocation");
		metaDataLine.add("1");
		metaDataLine.add(BRICSTimeDateUtil.repositoryDateToString(expectedLockDate));
		metaDataLine.add("1");

		SubmissionMetaData test = new SubmissionMetaData(metaDataLine);
		Assert.assertFalse(test.isSubjectSubmitted());
	}
}
