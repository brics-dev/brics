package gov.nih.tbi.commons.util;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BRICSStringUtilsTest {

	@Test
	public void concatWithDelimiterTest() {
		final String DELIMITOR = ", ";
		List<String> stringList = new ArrayList<String>();
		stringList.add("Funtana");
		stringList.add("Tammy");
		stringList.add("Bugsit");

		final String EXPECTED_RESULT = "Funtana, Tammy, Bugsit";

		String result = BRICSStringUtils.concatWithDelimiter(stringList, DELIMITOR);
		Assert.assertEquals(result, EXPECTED_RESULT);
	}
	
	@Test
	public void concatWithDelimiterTest2() {
		final String DELIMITOR = ";";
		List<String> stringList = new ArrayList<String>();
		stringList.add("Funtana");
		stringList.add("Tammy");
		stringList.add("Bugsit");

		final String EXPECTED_RESULT = "Funtana;Tammy;Bugsit";

		String result = BRICSStringUtils.concatWithDelimiter(stringList, DELIMITOR);
		Assert.assertEquals(result, EXPECTED_RESULT);
	}


	@Test
	public void pathToFileNameTest() {
		final String PATH = "C:\\something\\something-else\\something1234\\testFile.csv";
		final String EXPECTED_RESULT = "testFile.csv";
		pathToFileNameTestHelper(PATH, EXPECTED_RESULT);
	}

	@Test
	public void pathToFileNameTest1() {
		final String PATH = "something-else\\something1234\\testFile.csv";
		final String EXPECTED_RESULT = "testFile.csv";
		pathToFileNameTestHelper(PATH, EXPECTED_RESULT);
	}

	@Test
	public void pathToFileNameTest2() {
		final String PATH = "/something/something-else/something1234/testFile.csv";
		final String EXPECTED_RESULT = "testFile.csv";
		pathToFileNameTestHelper(PATH, EXPECTED_RESULT);
	}


	@Test
	public void pathToFileNameTest3() {
		final String PATH = "something/something-else/something1234/testFile.csv";
		final String EXPECTED_RESULT = "testFile.csv";
		pathToFileNameTestHelper(PATH, EXPECTED_RESULT);
	}

	@Test
	public void delimitedStringToListTest() {
		final String STRING = "TOM1234,BILL,RYAN";
		final List<String> EXPECTED_RESULT = new ArrayList<String>();
		EXPECTED_RESULT.add("TOM1234");
		EXPECTED_RESULT.add("BILL");
		EXPECTED_RESULT.add("RYAN");
		Assert.assertEquals(BRICSStringUtils.delimitedStringToList(STRING, ","), EXPECTED_RESULT);
	}

	private void pathToFileNameTestHelper(final String PATH, final String EXPECTED_RESULT) {
		String result = BRICSStringUtils.pathToFileName(PATH);
		Assert.assertEquals(result, EXPECTED_RESULT);
	}

	@Test
	public void capitalizeFirstCharacterTest() {
		final String STRING = "johnny";
		final String EXPECTED_RESULT = "Johnny";
		Assert.assertEquals(BRICSStringUtils.capitalizeFirstCharacter(STRING), EXPECTED_RESULT);
	}
	
	@Test
	public void capitalizeFirstCharacterTest2() {
		final String STRING = "";
		final String EXPECTED_RESULT = "";
		Assert.assertEquals(BRICSStringUtils.capitalizeFirstCharacter(STRING), EXPECTED_RESULT);
	}
	
	@Test
	public void capitalizeFirstCharacterTest3() {
		final String STRING = "Johnny ";
		final String EXPECTED_RESULT = "Johnny ";
		Assert.assertEquals(BRICSStringUtils.capitalizeFirstCharacter(STRING), EXPECTED_RESULT);
	}
	
	@Test
	public void convertNameFormatTest1() {
		final String NAME = "John Smith";
		final String EXPECTED_RESULT = "Smith, John";
		Assert.assertEquals(BRICSStringUtils.convertNameFormat(NAME), EXPECTED_RESULT);
	}
	
	@Test
	public void convertNameFormatTest2() {
		final String NAME = "Mary Pat Green";
		final String EXPECTED_RESULT = "Green, Mary Pat";
		Assert.assertEquals(BRICSStringUtils.convertNameFormat(NAME), EXPECTED_RESULT);
	}
	
	@Test
	public void convertNameFormatTest3() {
		final String NAME = "Jason";
		final String EXPECTED_RESULT = "Jason";
		Assert.assertEquals(BRICSStringUtils.convertNameFormat(NAME), EXPECTED_RESULT);
	}
	
	@Test
	public void convertNameFormatTest4() {
		final String NAME = null;
		final String EXPECTED_RESULT = null;
		Assert.assertEquals(BRICSStringUtils.convertNameFormat(NAME), EXPECTED_RESULT);
	}	
}
