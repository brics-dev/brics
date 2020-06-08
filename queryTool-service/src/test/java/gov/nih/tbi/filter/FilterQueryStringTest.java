package gov.nih.tbi.filter;

import org.testng.annotations.Test;

import gov.nih.tbi.exceptions.FilterQueryStringException;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class FilterQueryStringTest {

	@Test
	public void generateStringTest() throws FilterQueryStringException {
		FormResult form1 = mock(FormResult.class);
		when(form1.getShortName()).thenReturn("form1");
		RepeatableGroup rg1 = mock(RepeatableGroup.class);
		when(rg1.getName()).thenReturn("rg1");
		DataElement de1 = mock(DataElement.class);
		when(de1.getName()).thenReturn("de1");
		FormResult form2 = mock(FormResult.class);
		when(form2.getShortName()).thenReturn("form2");
		RepeatableGroup rg2 = mock(RepeatableGroup.class);
		when(rg2.getName()).thenReturn("rg2");
		DataElement de2 = mock(DataElement.class);
		when(de2.getName()).thenReturn("de2");

		List<Filter> filterList1 = new ArrayList<>();
		Filter form1Filter1 =
				new FreeFormFilter(form1, rg1, de1, "doge", "form1Filter1", null, null, null, FilterMode.EXACT);
		Filter form1Filter2 =
				new FreeFormFilter(form1, rg1, de1, "wow", "form1Filter2", null, null, null, FilterMode.EXACT);
		filterList1.add(form1Filter1);
		filterList1.add(form1Filter2);
		when(form1.getFilters()).thenReturn(filterList1);

		List<Filter> filterList2 = new ArrayList<>();
		List<String> multiSelectValues = new ArrayList<>();
		multiSelectValues.add("wow");
		multiSelectValues.add("such filter");
		multiSelectValues.add("very string");
		Filter form2Filter1 = new MultiSelectFilter(form2, rg2, de2, multiSelectValues, "much select", "form2Filter1",
				FilterMode.EXACT, null, null, null);
		Filter form2Filter2 =
				new FreeFormFilter(form1, rg2, de2, "wow", "form2Filter2", null, null, null, FilterMode.EXACT);
		filterList1.add(form2Filter1);
		filterList1.add(form2Filter2);
		when(form2.getFilters()).thenReturn(filterList2);

		String filterExpression = "((form1Filter1 || form1Filter2) && form2Filter1) || !(form2Filter2)";

		List<FormResult> forms = new ArrayList<>();
		forms.add(form1);
		forms.add(form2);

		FilterQueryStringFactory factory = new FilterQueryStringFactory(filterExpression, forms);
		String actualString = factory.generateString();
		String expected =
				"(((form1.rg1.de1 = 'doge') OR (form1.rg1.de1 = 'wow')) AND (form2.rg2.de2 = 'wow' AND form2.rg2.de2 = 'such filter' AND form2.rg2.de2 = 'very string' AND form2.rg2.de2 = 'much select')) OR NOT((form1.rg2.de2 = 'wow'))";
		assertEquals(actualString, expected);
	}

	@Test
	public void generateStringDollarSignTest() throws FilterQueryStringException {
		FormResult form1 = mock(FormResult.class);
		when(form1.getShortName()).thenReturn("form1");
		RepeatableGroup rg1 = mock(RepeatableGroup.class);
		when(rg1.getName()).thenReturn("rg1");
		DataElement de1 = mock(DataElement.class);
		when(de1.getName()).thenReturn("de1");

		List<Filter> filterList1 = new ArrayList<>();
		Filter form1Filter1 =
				new FreeFormFilter(form1, rg1, de1, "doge", "form1$Filter1", null, null, null, FilterMode.EXACT);
		filterList1.add(form1Filter1);
		when(form1.getFilters()).thenReturn(filterList1);

		String filterExpression = "(form1$Filter1)";

		List<FormResult> forms = new ArrayList<>();
		forms.add(form1);

		FilterQueryStringFactory factory = new FilterQueryStringFactory(filterExpression, forms);
		String actualString = factory.generateString();
		String expected = "((form1.rg1.de1 = 'doge'))";
		assertEquals(actualString, expected);
	}
}
