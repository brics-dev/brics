package gov.nih.tbi.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.testng.annotations.Test;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.exceptions.FilterEvaluationException;
import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;

public class FilterEvaluatorTest {

	@Test(expectedExceptions = NullArgumentException.class)
	public void nullExpressionTest() throws FilterEvaluatorException {
		FormResult form = mock(FormResult.class);
		List<FormResult> forms = new ArrayList<>();
		forms.add(form);
		new FilterEvaluator(null, forms);
	}

	@Test
	public void simpleTest() throws FilterEvaluatorException {
		String testExpression = "f1 && f2 && f3 && f4";

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		DataElement nameDe = mock(DataElement.class);
		when(nameDe.getName()).thenReturn("name");
		when(nameDe.getType()).thenReturn(DataType.ALPHANUMERIC);

		DataElement dateDe = mock(DataElement.class);
		when(dateDe.getName()).thenReturn("date");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d, "f1", null, null, null);
		Filter f2 = new RangedNumericFilter(form, rg, ageDe, 30d, 20d, "f2", null, null, null);
		Filter f3 = new FreeFormFilter(form, rg, nameDe, "Doge", "f3", null, null, null, FilterMode.EXACT);
		Filter f4 = new DateFilter(form, rg, dateDe, BRICSTimeDateUtil.parseTwoDigitSlashDate("07/15/19"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("07/10/19"), "f4", null, null, null);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);
		filters.add(f2);
		filters.add(f3);
		filters.add(f4);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(testExpression, forms);

		InstancedRecord record = mock(InstancedRecord.class);

		List<InstancedRow> rows = new ArrayList<>();
		InstancedRow row = mock(InstancedRow.class);
		rows.add(row);

		when(record.getSelectedRows()).thenReturn(rows);

		NonRepeatingCellValue ageCellValue = new NonRepeatingCellValue(DataType.NUMERIC, "30");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), ageDe.getName())).thenReturn(ageCellValue);

		NonRepeatingCellValue nameCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "Doge");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), nameDe.getName())).thenReturn(nameCellValue);

		NonRepeatingCellValue dateCellValue = new NonRepeatingCellValue(DataType.DATE, "2019-07-12T00:00:00Z");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), dateDe.getName())).thenReturn(dateCellValue);

		boolean result = evaluator.evaluate(record);
		assertTrue(result);
	}

	@Test
	public void specialCharacterTest() throws FilterEvaluatorException {
		String testExpression = "form_repeatable$group_dataelement";

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		DataElement nameDe = mock(DataElement.class);
		when(nameDe.getName()).thenReturn("name");

		DataElement dateDe = mock(DataElement.class);
		when(dateDe.getName()).thenReturn("date");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d, "form_repeatable$group_dataelement", null, null,
				null);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(testExpression, forms);

		InstancedRecord record = mock(InstancedRecord.class);

		List<InstancedRow> rows = new ArrayList<>();
		InstancedRow row = mock(InstancedRow.class);
		rows.add(row);

		when(record.getSelectedRows()).thenReturn(rows);

		NonRepeatingCellValue ageCellValue = new NonRepeatingCellValue(DataType.NUMERIC, "30");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), ageDe.getName())).thenReturn(ageCellValue);

		boolean result = evaluator.evaluate(record);
		assertTrue(result);
	}

	@Test
	public void singleQuoteEscapeTest() throws FilterEvaluatorException {
		String testExpression = "(FamilyHistory_Alzheimer's$disease_FamHistMedclCondTyp_0)";

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		DataElement nameDe = mock(DataElement.class);
		when(nameDe.getName()).thenReturn("name");

		DataElement dateDe = mock(DataElement.class);
		when(dateDe.getName()).thenReturn("date");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d,
				"FamilyHistory_Alzheimer's$disease_FamHistMedclCondTyp_0", null, null, null);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(testExpression, forms);

		InstancedRecord record = mock(InstancedRecord.class);

		List<InstancedRow> rows = new ArrayList<>();
		InstancedRow row = mock(InstancedRow.class);
		rows.add(row);

		when(record.getSelectedRows()).thenReturn(rows);

		NonRepeatingCellValue ageCellValue = new NonRepeatingCellValue(DataType.NUMERIC, "30");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), ageDe.getName())).thenReturn(ageCellValue);

		boolean result = evaluator.evaluate(record);
		assertTrue(result);
	}

	@Test
	public void simpleNotTest() throws FilterEvaluatorException {
		String testExpression = "f1 && f2 && f3 && !f4";

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		DataElement nameDe = mock(DataElement.class);
		when(nameDe.getName()).thenReturn("name");
		when(nameDe.getType()).thenReturn(DataType.ALPHANUMERIC);

		DataElement dateDe = mock(DataElement.class);
		when(dateDe.getName()).thenReturn("date");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d, "f1", null, null, null);
		Filter f2 = new RangedNumericFilter(form, rg, ageDe, 30d, 20d, "f2", null, null, null);
		Filter f3 = new FreeFormFilter(form, rg, nameDe, "Doge", "f3", null, null, null, FilterMode.EXACT);
		Filter f4 = new DateFilter(form, rg, dateDe, BRICSTimeDateUtil.parseTwoDigitSlashDate("07/15/19"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("07/10/19"), "f4", null, null, null);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);
		filters.add(f2);
		filters.add(f3);
		filters.add(f4);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(testExpression, forms);

		InstancedRecord record = mock(InstancedRecord.class);

		List<InstancedRow> rows = new ArrayList<>();
		InstancedRow row = mock(InstancedRow.class);
		rows.add(row);

		when(record.getSelectedRows()).thenReturn(rows);

		NonRepeatingCellValue ageCellValue = new NonRepeatingCellValue(DataType.NUMERIC, "30");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), ageDe.getName())).thenReturn(ageCellValue);

		NonRepeatingCellValue nameCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "Doge");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), nameDe.getName())).thenReturn(nameCellValue);

		NonRepeatingCellValue dateCellValue = new NonRepeatingCellValue(DataType.DATE, "2019-07-12T00:00:00Z");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), dateDe.getName())).thenReturn(dateCellValue);

		boolean result = evaluator.evaluate(record);
		assertFalse(result);
	}

	@Test
	public void precedenceTest() throws FilterEvaluatorException {
		String testExpression = "((f1 && f2) || f3) || f4";

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		DataElement nameDe = mock(DataElement.class);
		when(nameDe.getName()).thenReturn("name");
		when(nameDe.getType()).thenReturn(DataType.ALPHANUMERIC);

		DataElement dateDe = mock(DataElement.class);
		when(dateDe.getName()).thenReturn("date");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d, "f1", null, null, null);
		Filter f2 = new RangedNumericFilter(form, rg, ageDe, 30d, 20d, "f2", null, null, null);
		Filter f3 = new FreeFormFilter(form, rg, nameDe, "Doge", "f3", null, null, null, FilterMode.EXACT);
		Filter f4 = new DateFilter(form, rg, dateDe, BRICSTimeDateUtil.parseTwoDigitSlashDate("07/15/19"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("07/10/19"), "f4", null, null, null);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);
		filters.add(f2);
		filters.add(f3);
		filters.add(f4);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(testExpression, forms);

		InstancedRecord record = mock(InstancedRecord.class);

		List<InstancedRow> rows = new ArrayList<>();
		InstancedRow row = mock(InstancedRow.class);
		rows.add(row);

		when(record.getSelectedRows()).thenReturn(rows);

		NonRepeatingCellValue ageCellValue = new NonRepeatingCellValue(DataType.NUMERIC, "40");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), ageDe.getName())).thenReturn(ageCellValue);

		NonRepeatingCellValue nameCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "cat");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), nameDe.getName())).thenReturn(nameCellValue);

		NonRepeatingCellValue dateCellValue = new NonRepeatingCellValue(DataType.DATE, "2019-07-12T00:00:00Z");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), dateDe.getName())).thenReturn(dateCellValue);

		boolean result = evaluator.evaluate(record);
		assertTrue(result);
	}

	@Test(expectedExceptions = FilterEvaluationException.class)
	public void undefinedVariableTest() throws FilterEvaluatorException {
		String testExpression = "((f1 && f2) || f3) || f4";

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		DataElement nameDe = mock(DataElement.class);
		when(nameDe.getName()).thenReturn("name");
		when(nameDe.getType()).thenReturn(DataType.ALPHANUMERIC);

		DataElement dateDe = mock(DataElement.class);
		when(dateDe.getName()).thenReturn("date");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d, "f1", null, null, null);
		Filter f2 = new RangedNumericFilter(form, rg, ageDe, 30d, 20d, "f2", null, null, null);
		Filter f3 = new FreeFormFilter(form, rg, nameDe, "Doge", "f3", null, null, null, FilterMode.EXACT);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);
		filters.add(f2);
		filters.add(f3);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(testExpression, forms);

		InstancedRecord record = mock(InstancedRecord.class);

		List<InstancedRow> rows = new ArrayList<>();
		InstancedRow row = mock(InstancedRow.class);
		rows.add(row);

		when(record.getSelectedRows()).thenReturn(rows);

		NonRepeatingCellValue ageCellValue = new NonRepeatingCellValue(DataType.NUMERIC, "40");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), ageDe.getName())).thenReturn(ageCellValue);

		NonRepeatingCellValue nameCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "cat");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), nameDe.getName())).thenReturn(nameCellValue);

		NonRepeatingCellValue dateCellValue = new NonRepeatingCellValue(DataType.DATE, "2019-07-12T00:00:00Z");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), dateDe.getName())).thenReturn(dateCellValue);

		evaluator.evaluate(record);
	}

	@Test
	public void getSubExpressionTest() throws FilterEvaluatorException {

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		DataElement nameDe = mock(DataElement.class);
		when(nameDe.getName()).thenReturn("name");

		DataElement dateDe = mock(DataElement.class);
		when(dateDe.getName()).thenReturn("date");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d, "f1", null, null, null);
		Filter f2 = new RangedNumericFilter(form, rg, ageDe, 30d, 20d, "f2", null, null, null);
		Filter f3 = new FreeFormFilter(form, rg, nameDe, "Doge", "f3", null, null, null, FilterMode.INCLUSIVE);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);
		filters.add(f2);
		filters.add(f3);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(
				"(eyeGENEGenomics_Genomics$Information2_Pathogenic) && (eyeGENEGenomics_Genomics$Information_HGNCGeneSymbl_0 || eyeGENEGenomics_Genomics$Information_HGNCGeneSymbl_1) && (eyeGENEGenomics_Genomics$Information2_Pathogenic)",
				forms);

		String expected =
				"(eyeGENEGenomics_Genomics$Information_HGNCGeneSymbl_0 || eyeGENEGenomics_Genomics$Information_HGNCGeneSymbl_1)";
		assertEquals(evaluator.getSubExpression("eyeGENEGenomics", "Genomics Information"), expected);
	}

	@Test
	public void getSubExpressionTest2() throws FilterEvaluatorException {

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		DataElement nameDe = mock(DataElement.class);
		when(nameDe.getName()).thenReturn("name");

		DataElement dateDe = mock(DataElement.class);
		when(dateDe.getName()).thenReturn("date");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d, "f1", null, null, null);
		Filter f2 = new RangedNumericFilter(form, rg, ageDe, 30d, 20d, "f2", null, null, null);
		Filter f3 = new FreeFormFilter(form, rg, nameDe, "Doge", "f3", null, null, null, FilterMode.INCLUSIVE);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);
		filters.add(f2);
		filters.add(f3);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(
				"(!(eyeGENEGenomics_Genomics$Information2_Pathogenic)) && (eyeGENEGenomics_Genomics$Information_HGNCGeneSymbl_0 || eyeGENEGenomics_Genomics$Information_HGNCGeneSymbl_1) && (eyeGENEGenomics_Genomics$Information2_Pathogenic)",
				forms);

		String expected =
				"(eyeGENEGenomics_Genomics$Information_HGNCGeneSymbl_0 || eyeGENEGenomics_Genomics$Information_HGNCGeneSymbl_1)";
		assertEquals(evaluator.getSubExpression("eyeGENEGenomics", "Genomics Information"), expected);
	}

	@Test
	public void getSubExpressionTest3() throws FilterEvaluatorException {

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		DataElement nameDe = mock(DataElement.class);
		when(nameDe.getName()).thenReturn("name");

		DataElement dateDe = mock(DataElement.class);
		when(dateDe.getName()).thenReturn("date");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d, "f1", null, null, null);
		Filter f2 = new RangedNumericFilter(form, rg, ageDe, 30d, 20d, "f2", null, null, null);
		Filter f3 = new FreeFormFilter(form, rg, nameDe, "Doge", "f3", null, null, null, FilterMode.INCLUSIVE);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);
		filters.add(f2);
		filters.add(f3);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(
				"(!(eyeGENEGenomics_Genomics$Information_Pathogenic)) && (eyeGENEGenomics_Genomics$Information2_HGNCGeneSymbl_0 || eyeGENEGenomics_Genomics$Information2_HGNCGeneSymbl_1) && (eyeGENEGenomics_Genomics$Information2_Pathogenic)",
				forms);

		String expected = "(!(eyeGENEGenomics_Genomics$Information_Pathogenic))";
		assertEquals(evaluator.getSubExpression("eyeGENEGenomics", "Genomics Information"), expected);
	}

	@Test
	public void getSubExpressionTest4() throws FilterEvaluatorException {

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		DataElement nameDe = mock(DataElement.class);
		when(nameDe.getName()).thenReturn("name");

		DataElement dateDe = mock(DataElement.class);
		when(dateDe.getName()).thenReturn("date");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d, "f1", null, null, null);
		Filter f2 = new RangedNumericFilter(form, rg, ageDe, 30d, 20d, "f2", null, null, null);
		Filter f3 = new FreeFormFilter(form, rg, nameDe, "Doge", "f3", null, null, null, FilterMode.INCLUSIVE);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);
		filters.add(f2);
		filters.add(f3);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(
				"((eyeGENEGenomics_Genomics$Information_HGNCGeneSymbl_0 || eyeGENEGenomics_Genomics$Information_HGNCGeneSymbl_1))&&((eyeGENEGenomics_Genomics$Information_GeneVariantIndicator_0))",
				forms);

		System.out.println("Result: " + evaluator.getSubExpression("eyeGENEGenomics", "Genomics Information"));
		// String expected = "!(eyeGENEGenomics_Genomics$Information_Pathogenic)";
		// assertEquals(evaluator.getSubExpression("Genomics Information"), expected);
	}

	@Test
	public void getSubExpressionTest5() throws FilterEvaluatorException {

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d, "f1", null, null, null);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(
				"(PriorAndConcomitantMeds_Parkinson$s$Disease$Medications_MedctnPriorConcomPD_0)", forms);

		String expected = "(PriorAndConcomitantMeds_Parkinson$s$Disease$Medications_MedctnPriorConcomPD_0)";
		assertEquals(evaluator.getSubExpression("PriorAndConcomitantMeds", "Parkinson's Disease Medications"),
				expected);
	}

	@Test
	public void getSubExpressionTest6() throws FilterEvaluatorException {

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d, "f1", null, null, null);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(
				"(PROWL_LASIK_PreOp2_Lasik$Questionnaire_LasikDIFCNDRVScl_0)&&(PROWL_LASIK_PreOp2_Lasik$Questionnaire_LasikDAILYACTScl_0)&&(PROWL_LASIK_PostOp_Lasik$Questionnaire_LasikVSNCLRYScl_0)",
				forms);

		String expected = "(PROWL_LASIK_PostOp_Lasik$Questionnaire_LasikVSNCLRYScl_0)";
		assertEquals(evaluator.getSubExpression("PROWL_LASIK_PostOp", "Lasik Questionnaire"), expected);
	}
	
	@Test
	public void getSubExpressionTest7() throws FilterEvaluatorException {

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement ageDe = mock(DataElement.class);
		when(ageDe.getName()).thenReturn("ageYrs");

		Filter f1 = new RangedNumericFilter(form, rg, ageDe, 50d, 20d, "f1", null, null, null);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(
				"(PROWL_LASIK_PreOp2_Lasik$Questionnaire_LasikDIFCNDRVScl_0)&&(PROWL_LASIK_PostOp_Lasik$Questionnaire_LasikVSNCLRYScl_0)&&(PROWL_LASIK_PreOp2_Lasik$Questionnaire_LasikDAILYACTScl_0)",
				forms);

		String expected = "(PROWL_LASIK_PreOp2_Lasik$Questionnaire_LasikDIFCNDRVScl_0)&&(PROWL_LASIK_PreOp2_Lasik$Questionnaire_LasikDAILYACTScl_0)";
		assertEquals(evaluator.getSubExpression("PROWL_LASIK_PreOp2", "Lasik Questionnaire"), expected);
	}
}
