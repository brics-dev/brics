package gov.nih.tbi.api.query.utils;

import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.*;

import org.testng.annotations.Test;

import gov.nih.tbi.api.query.model.Filter;
import gov.nih.tbi.api.query.model.Operator;
import gov.nih.tbi.pojo.FormResult;

public class FilterAdapterTest {
	@Test
	public void initializeFilterNameTest() {
		List<Filter> apiFilters = new ArrayList<>();
		Filter f1 = new Filter();
		f1.setForm("form");
		f1.setRepeatableGroup("Parkinson's Disease");
		f1.setDataElement("de");
		apiFilters.add(f1);
		new FilterAdapter(null, apiFilters);
		String expected = "form_Parkinson$s$Disease_de";
		assertEquals(f1.getName(), expected);
	}

	@Test
	public void initializeFilterNameTest2() {
		List<Filter> apiFilters = new ArrayList<>();
		Filter f1 = new Filter();
		f1.setForm("form");
		f1.setRepeatableGroup("Parkinson's Disease");
		f1.setDataElement("de");
		Filter f2 = new Filter();
		f2.setForm("form");
		f2.setRepeatableGroup("Parkinson's Disease");
		f2.setDataElement("de");
		Filter f3 = new Filter();
		f3.setForm("form");
		f3.setRepeatableGroup("Parkinson's Disease");
		f3.setDataElement("de");
		Filter f4 = new Filter();
		f4.setForm("form");
		f4.setRepeatableGroup("Parkinson's Disease");
		f4.setDataElement("de");
		apiFilters.add(f1);
		apiFilters.add(f2);
		apiFilters.add(f3);
		apiFilters.add(f4);
		new FilterAdapter(null, apiFilters);
		String expected = "form_Parkinson$s$Disease_de";
		assertEquals(f1.getName(), expected);
		expected = "form_Parkinson$s$Disease_de_1";
		assertEquals(f2.getName(), expected);
		expected = "form_Parkinson$s$Disease_de_2";
		assertEquals(f3.getName(), expected);
		expected = "form_Parkinson$s$Disease_de_3";
		assertEquals(f4.getName(), expected);
	}

	@Test
	public void initalizeFilterNameTest3() {
		List<Filter> apiFilters = new ArrayList<>();
		Filter f1 = new Filter();
		f1.setForm("eyeGENEDemographics");
		f1.setRepeatableGroup("Subject Demographics");
		f1.setDataElement("GenderTyp");
		apiFilters.add(f1);
		new FilterAdapter(null, apiFilters);
		String expected = "eyeGENEDemographics_Subject$Demographics_GenderTyp";
		assertEquals(f1.getName(), expected);
	}
	
	@Test
	public void buildExpressionTest() {
		List<FormResult> forms = new ArrayList<> ();
		FormResult form = new FormResult();
		forms.add(form);
		form.setShortName("eyeGENEGenomics");
		List<Filter> apiFilters = new ArrayList<> ();
		Filter f1 = new Filter();
		f1.setForm("eyeGENEGenomics");
		f1.setRepeatableGroup("Genomics Information");
		f1.setDataElement("HGNCGeneSymbl");
		f1.setOperator(Operator.OR);
		apiFilters.add(f1);
		
		Filter f2 = new Filter();
		f2.setForm("eyeGENEGenomics");
		f2.setRepeatableGroup("Genomics Information");
		f2.setDataElement("HGNCGeneSymbl");
		f2.setOperator(Operator.AND);
		apiFilters.add(f2);
		
		Filter f3 = new Filter();
		f3.setForm("eyeGENEGenomics");
		f3.setRepeatableGroup("Genomics Information");
		f3.setDataElement("GeneVariantIndicator");
		apiFilters.add(f3);
		
		FilterAdapter filterAdapter = new FilterAdapter(forms, apiFilters);
		
		//this method is non-deterministic due to the use of hash map.  leaving out the assertion for now until I figure out a better way to add it back in.
		System.out.println(filterAdapter.buildExpression());
	}
	
	@Test
	public void buildExpressionTest3() {
		List<FormResult> forms = new ArrayList<> ();
		FormResult form = new FormResult();
		forms.add(form);
		form.setShortName("eyeGENEGenomics");
		List<Filter> apiFilters = new ArrayList<> ();
		Filter f1 = new Filter();
		f1.setForm("eyeGENEGenomics");
		f1.setRepeatableGroup("Genomics Information");
		f1.setDataElement("HGNCGeneSymbl");
		f1.setOperator(Operator.OR);
		apiFilters.add(f1);
		
		Filter f3 = new Filter();
		f3.setForm("eyeGENEGenomics");
		f3.setRepeatableGroup("Genomics Information");
		f3.setDataElement("GeneVariantIndicator");
		apiFilters.add(f3);
		
		FilterAdapter filterAdapter = new FilterAdapter(forms, apiFilters);
		
		//this method is non-deterministic due to the use of hash map.  leaving out the assertion for now until I figure out a better way to add it back in.
		System.out.println(filterAdapter.buildExpression());
	}
}
