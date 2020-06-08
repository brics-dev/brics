package gov.nih.tbi.filter;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.exceptions.FilterParseException;
import gov.nih.tbi.filter.DelimitedMultiSelectFilter.DelimitedMultiSelectMode;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

import static org.mockito.Mockito.*;

public class FilterFactoryTest {

	@Test
	public void parseJsonChangeInDiagnosisTest() throws FilterParseException {
		JsonObject testJson = new JsonObject();
		testJson.add("filterJavaType", new JsonPrimitive(FilterType.CHANGE_IN_DIAGNOSIS.name()));
		JsonArray permissibleValuesJson = new JsonArray();
		permissibleValuesJson.add(new JsonPrimitive("Yes"));
		testJson.add("permissibleValues", permissibleValuesJson);
		testJson.add("name", new JsonPrimitive("testFilter"));
		testJson.add("logicBefore", new JsonPrimitive("test"));
		testJson.add("groupingBefore", new JsonPrimitive(0));
		testJson.add("groupingAfter", new JsonPrimitive(1));

		Filter testFilter = FilterFactory.parseJson(null, null, null, testJson);

		Filter expectedFilter = new ChangeInDiagnosisFilter("Yes", "testFilter", "test", 0, 1);


		assertEquals(testFilter, expectedFilter);
	}

	@Test
	public void parseJsonSingleSelectTest() throws FilterParseException {
		FormResult form = mock(FormResult.class);
		RepeatableGroup rg = mock(RepeatableGroup.class);
		DataElement testDe = mock(DataElement.class);
		when(testDe.getType()).thenReturn(DataType.ALPHANUMERIC);
		when(testDe.getInputRestrictions()).thenReturn(InputRestrictions.SINGLE);

		JsonObject testJson = new JsonObject();
		testJson.add("filterJavaType", new JsonPrimitive(FilterType.SINGLE_SELECT.name()));
		JsonArray permissibleValuesJson = new JsonArray();
		permissibleValuesJson.add(new JsonPrimitive("test1"));
		permissibleValuesJson.add(new JsonPrimitive("test2"));
		testJson.add("permissibleValues", permissibleValuesJson);
		testJson.add("freeFormValue", new JsonPrimitive("other"));
		testJson.add("name", new JsonPrimitive("testFilter"));
		testJson.add("logicBefore", new JsonPrimitive("test"));
		testJson.add("groupingBefore", new JsonPrimitive(0));
		testJson.add("groupingAfter", new JsonPrimitive(1));

		Filter testFilter = FilterFactory.parseJson(form, rg, testDe, testJson);
		List<String> permissibleValues = new ArrayList<>();
		permissibleValues.add("test1");
		permissibleValues.add("test2");
		Filter expectedFilter =
				new SingleSelectFilter(form, rg, testDe, permissibleValues, "other", "testFilter", "test", 0, 1);

		assertEquals(testFilter, expectedFilter);
	}

	@Test
	public void parseJsonRangedNumericTest() throws FilterParseException {
		FormResult form = mock(FormResult.class);
		RepeatableGroup rg = mock(RepeatableGroup.class);
		DataElement testDe = mock(DataElement.class);
		when(testDe.getType()).thenReturn(DataType.NUMERIC);

		JsonObject testJson = new JsonObject();
		testJson.add("filterJavaType", new JsonPrimitive(FilterType.RANGED_NUMERIC.name()));
		testJson.add("maximum", new JsonPrimitive("1.0"));
		testJson.add("minimum", new JsonPrimitive("0.5"));
		testJson.add("name", new JsonPrimitive("testFilter"));
		testJson.add("logicBefore", new JsonPrimitive("test"));
		testJson.add("groupingBefore", new JsonPrimitive(0));
		testJson.add("groupingAfter", new JsonPrimitive(1));

		Filter testFilter = FilterFactory.parseJson(form, rg, testDe, testJson);
		Filter expectedFilter = new RangedNumericFilter(form, rg, testDe, 1.0d, 0.5d, "testFilter", "test", 0, 1);

		assertEquals(testFilter, expectedFilter);
	}

	@Test
	public void parseJsonMultiSelectTest() throws FilterParseException {
		FormResult form = mock(FormResult.class);
		RepeatableGroup rg = mock(RepeatableGroup.class);
		DataElement testDe = mock(DataElement.class);
		when(testDe.getType()).thenReturn(DataType.ALPHANUMERIC);
		when(testDe.getInputRestrictions()).thenReturn(InputRestrictions.MULTIPLE);

		JsonObject testJson = new JsonObject();
		testJson.add("filterJavaType", new JsonPrimitive(FilterType.MULTI_SELECT.name()));
		JsonArray permissibleValuesJson = new JsonArray();
		permissibleValuesJson.add(new JsonPrimitive("test1"));
		permissibleValuesJson.add(new JsonPrimitive("test2"));
		testJson.add("permissibleValues", permissibleValuesJson);
		testJson.add("freeFormValue", new JsonPrimitive("other"));
		testJson.add("name", new JsonPrimitive("testFilter"));
		testJson.add("mode", new JsonPrimitive("inclusive"));
		testJson.add("logicBefore", new JsonPrimitive("test"));
		testJson.add("groupingBefore", new JsonPrimitive(0));
		testJson.add("groupingAfter", new JsonPrimitive(1));

		Filter testFilter = FilterFactory.parseJson(form, rg, testDe, testJson);
		List<String> permissibleValues = new ArrayList<>();
		permissibleValues.add("test1");
		permissibleValues.add("test2");
		Filter expectedFilter = new MultiSelectFilter(form, rg, testDe, permissibleValues, "other", "testFilter",
				FilterMode.INCLUSIVE, "test", 0, 1);

		assertEquals(testFilter, expectedFilter);
	}

	@Test
	public void parseJsonMultiSelectMultiDataTest() throws FilterParseException {
		FormResult form = mock(FormResult.class);
		RepeatableGroup rg = mock(RepeatableGroup.class);
		DataElement testDe = mock(DataElement.class);
		when(testDe.getType()).thenReturn(DataType.ALPHANUMERIC);
		when(testDe.getInputRestrictions()).thenReturn(InputRestrictions.MULTIPLE);

		JsonObject testJson = new JsonObject();
		testJson.add("filterJavaType", new JsonPrimitive(FilterType.MULTI_SELECT.name()));
		JsonArray permissibleValuesJson = new JsonArray();
		permissibleValuesJson.add(new JsonPrimitive("test1"));
		permissibleValuesJson.add(new JsonPrimitive("test2"));
		testJson.add("permissibleValues", permissibleValuesJson);
		testJson.add("freeFormValue", new JsonPrimitive("other"));
		testJson.add("name", new JsonPrimitive("testFilter"));
		testJson.add("mode", new JsonPrimitive("inclusive"));
		testJson.add("multiData", new JsonPrimitive(true));
		testJson.add("logicBefore", new JsonPrimitive("test"));
		testJson.add("groupingBefore", new JsonPrimitive(0));
		testJson.add("groupingAfter", new JsonPrimitive(1));

		Filter testFilter = FilterFactory.parseJson(form, rg, testDe, testJson);
		List<String> permissibleValues = new ArrayList<>();
		permissibleValues.add("test1");
		permissibleValues.add("test2");
		Filter expectedFilter = new MultiSelectFilter(form, rg, testDe, permissibleValues, "other", "testFilter",
				FilterMode.INCLUSIVE, true, "test", 0, 1);

		assertEquals(testFilter, expectedFilter);
	}

	@Test
	public void parseJsonMultiSelectMultiDataTest2() throws FilterParseException {
		FormResult form = mock(FormResult.class);
		RepeatableGroup rg = mock(RepeatableGroup.class);
		DataElement testDe = mock(DataElement.class);
		when(testDe.getType()).thenReturn(DataType.ALPHANUMERIC);
		when(testDe.getInputRestrictions()).thenReturn(InputRestrictions.MULTIPLE);

		JsonObject testJson = new JsonObject();
		testJson.add("filterJavaType", new JsonPrimitive(FilterType.MULTI_SELECT.name()));
		testJson.add("name", new JsonPrimitive("testFilter"));
		testJson.add("mode", new JsonPrimitive("inclusive"));
		testJson.add("multiData", new JsonPrimitive(true));
		testJson.add("logicBefore", new JsonPrimitive("test"));
		testJson.add("groupingBefore", new JsonPrimitive(0));
		testJson.add("groupingAfter", new JsonPrimitive(1));

		Filter testFilter = FilterFactory.parseJson(form, rg, testDe, testJson);
		Filter expectedFilter = new MultiSelectFilter(form, rg, testDe, null, null, "testFilter",
				FilterMode.INCLUSIVE, true, "test", 0, 1);

		assertEquals(testFilter, expectedFilter);
	}

	@Test
	public void parseJsonMultiSelectExactTest() throws FilterParseException {
		FormResult form = mock(FormResult.class);
		RepeatableGroup rg = mock(RepeatableGroup.class);
		DataElement testDe = mock(DataElement.class);
		when(testDe.getType()).thenReturn(DataType.ALPHANUMERIC);
		when(testDe.getInputRestrictions()).thenReturn(InputRestrictions.MULTIPLE);

		JsonObject testJson = new JsonObject();
		testJson.add("filterJavaType", new JsonPrimitive(FilterType.MULTI_SELECT.name()));
		JsonArray permissibleValuesJson = new JsonArray();
		permissibleValuesJson.add(new JsonPrimitive("test1"));
		permissibleValuesJson.add(new JsonPrimitive("test2"));
		testJson.add("permissibleValues", permissibleValuesJson);
		testJson.add("freeFormValue", new JsonPrimitive("other"));
		testJson.add("name", new JsonPrimitive("testFilter"));
		testJson.add("mode", new JsonPrimitive("exact"));
		testJson.add("multiData", new JsonPrimitive(true));
		testJson.add("logicBefore", new JsonPrimitive("test"));
		testJson.add("groupingBefore", new JsonPrimitive(0));
		testJson.add("groupingAfter", new JsonPrimitive(1));

		Filter testFilter = FilterFactory.parseJson(form, rg, testDe, testJson);
		List<String> permissibleValues = new ArrayList<>();
		permissibleValues.add("test1");
		permissibleValues.add("test2");
		Filter expectedFilter = new MultiSelectFilter(form, rg, testDe, permissibleValues, "other", "testFilter",
				FilterMode.EXACT, true, "test", 0, 1);

		assertEquals(testFilter, expectedFilter);
	}

	@Test
	public void parseJsonShowBlanksTest() throws FilterParseException {
		FormResult form = mock(FormResult.class);
		RepeatableGroup rg = mock(RepeatableGroup.class);
		DataElement testDe = mock(DataElement.class);
		when(testDe.getType()).thenReturn(DataType.ALPHANUMERIC);
		when(testDe.getInputRestrictions()).thenReturn(InputRestrictions.MULTIPLE);

		JsonObject testJson = new JsonObject();
		testJson.add("filterJavaType", new JsonPrimitive(FilterType.SHOW_BLANKS.name()));
		testJson.add("name", new JsonPrimitive("testFilter"));
		testJson.add("logicBefore", new JsonPrimitive("test"));
		testJson.add("groupingBefore", new JsonPrimitive(0));
		testJson.add("groupingAfter", new JsonPrimitive(1));

		Filter actualFilter = FilterFactory.parseJson(form, rg, testDe, testJson);
		Filter expectedFilter = new ShowBlanksFilter(form, rg, testDe, "testFilter", "test", 0, 1);

		assertEquals(actualFilter, expectedFilter);
	}

	@Test
	public void parseJsonFreeFormTest() throws FilterParseException {
		FormResult form = mock(FormResult.class);
		RepeatableGroup rg = mock(RepeatableGroup.class);
		DataElement testDe = mock(DataElement.class);
		when(testDe.getType()).thenReturn(DataType.ALPHANUMERIC);
		when(testDe.getInputRestrictions()).thenReturn(InputRestrictions.MULTIPLE);

		JsonObject testJson = new JsonObject();
		testJson.add("filterJavaType", new JsonPrimitive(FilterType.FREE_FORM.name()));
		testJson.add("freeFormValue", new JsonPrimitive("bla bla bla"));
		testJson.add("name", new JsonPrimitive("testFilter"));
		testJson.add("logicBefore", new JsonPrimitive("test"));
		testJson.add("groupingBefore", new JsonPrimitive(0));
		testJson.add("groupingAfter", new JsonPrimitive(1));
		testJson.add("mode", new JsonPrimitive("exact"));
		Filter testFilter = FilterFactory.parseJson(form, rg, testDe, testJson);
		Filter expectedFilter = new FreeFormFilter(form, rg, testDe, "bla bla bla", "testFilter", "test", 0, 1,
				FilterMode.EXACT);

		assertEquals(testFilter, expectedFilter);
	}

	@Test
	public void parseJsonDateTest() throws FilterParseException {
		FormResult form = mock(FormResult.class);
		RepeatableGroup rg = mock(RepeatableGroup.class);
		DataElement testDe = mock(DataElement.class);
		when(testDe.getType()).thenReturn(DataType.DATE);

		JsonObject testJson = new JsonObject();
		testJson.add("filterJavaType", new JsonPrimitive(FilterType.DATE.name()));
		testJson.add("dateMin", new JsonPrimitive("08/05/19"));
		testJson.add("dateMax", new JsonPrimitive("08/06/19"));
		testJson.add("name", new JsonPrimitive("testFilter"));
		testJson.add("logicBefore", new JsonPrimitive("test"));
		testJson.add("groupingBefore", new JsonPrimitive(0));
		testJson.add("groupingAfter", new JsonPrimitive(1));

		Filter testFilter = FilterFactory.parseJson(form, rg, testDe, testJson);
		Filter expectedFilter = new DateFilter(form, rg, testDe, BRICSTimeDateUtil.parseTwoDigitSlashDate("08/06/19"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("08/05/19"), "testFilter", "test", 0, 1);

		assertEquals(testFilter, expectedFilter);
	}

	@Test
	public void parseDelimitedMultiSelectTest() throws FilterParseException {
		FormResult form = mock(FormResult.class);
		RepeatableGroup rg = mock(RepeatableGroup.class);
		DataElement testDe = mock(DataElement.class);
		when(testDe.getType()).thenReturn(DataType.BIOSAMPLE);

		JsonObject testJson = new JsonObject();
		testJson.add("filterJavaType", new JsonPrimitive(FilterType.DELIMITED_MULTI_SELECT.name()));
		testJson.add("freeFormValue", new JsonPrimitive("123;567"));
		testJson.add("name", new JsonPrimitive("testFilter"));
		testJson.add("logicBefore", new JsonPrimitive("test"));
		testJson.add("groupingBefore", new JsonPrimitive(0));
		testJson.add("groupingAfter", new JsonPrimitive(1));
		testJson.add("mode", new JsonPrimitive("exact"));
		
		Filter testFilter = FilterFactory.parseJson(form, rg, testDe, testJson);
		Filter expectedFilter = new DelimitedMultiSelectFilter(form, rg, testDe, "123;567", "testFilter", "test", 0, 1,
				DelimitedMultiSelectMode.EXACT);

		assertEquals(testFilter, expectedFilter);
	}
}
