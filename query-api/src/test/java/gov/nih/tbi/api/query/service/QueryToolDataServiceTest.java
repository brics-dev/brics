package gov.nih.tbi.api.query.service;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gov.nih.tbi.api.query.model.BasicFormStudy;
import gov.nih.tbi.api.query.model.BasicStudyForm;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.InstancedDataManager;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.impl.InstancedDataManagerImpl;
import gov.nih.tbi.service.impl.ResultManagerImpl;

public class QueryToolDataServiceTest {

	@Mock
	ResultManager resultManager = new ResultManagerImpl();

	@Mock
	InstancedDataManager instancedManager = new InstancedDataManagerImpl();

	@InjectMocks
	DataService dataService = new QueryToolDataService();
	
	Map<String, FormResult> formNameResultMap;
	
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		
		formNameResultMap = new HashMap<String, FormResult>();
		
		FormResult formResult1 = new FormResult();
		formResult1.setShortName("FS111219");
		List<StudyResult> studyResult1 = new ArrayList<StudyResult>();
		StudyResult studyResult11 = new StudyResult();
		studyResult11.setPrefixedId("FITBIR-STUDY0000428");
		studyResult1.add(studyResult11);
		StudyResult studyResult12 = new StudyResult();
		studyResult12.setPrefixedId("FITBIR-STUDY0000418");
		studyResult1.add(studyResult12);
		formResult1.setStudies(studyResult1);
		
		FormResult formResult2 = new FormResult();
		formResult2.setShortName("ADAPT_MEDS");
		List<StudyResult> studyResult2 = new ArrayList<StudyResult>();
		StudyResult studyResult21 = new StudyResult();
		studyResult21.setPrefixedId("FITBIR-STUDY0000428");
		studyResult2.add(studyResult21);
		StudyResult studyResult22 = new StudyResult();
		studyResult22.setPrefixedId("FITBIR-STUDY0000410");
		studyResult2.add(studyResult22);
		StudyResult studyResult23 = new StudyResult();
		studyResult23.setPrefixedId("FITBIR-STUDY0000416");
		studyResult2.add(studyResult23);
		formResult2.setStudies(studyResult2);
		
		FormResult formResult3 = new FormResult();
		formResult3.setShortName("SF36v2");
		List<StudyResult> studyResult3 = new ArrayList<StudyResult>();
		StudyResult studyResult31 = new StudyResult();
		studyResult31.setPrefixedId("FITBIR-STUDY0000418");
		studyResult3.add(studyResult31);
		StudyResult studyResult32 = new StudyResult();
		studyResult32.setPrefixedId("FITBIR-STUDY0000428");
		studyResult3.add(studyResult32);
		formResult3.setStudies(studyResult3);
		
		FormResult formResult4 = new FormResult();
		formResult4.setShortName("testNotIn");
		List<StudyResult> studyResult4 = new ArrayList<StudyResult>();
		StudyResult studyResult41 = new StudyResult();
		studyResult41.setPrefixedId("FITBIR-STUDY0000428");
		formResult4.setStudies(studyResult4);
		
		formNameResultMap.put(formResult1.getShortName(), formResult1);
		formNameResultMap.put(formResult2.getShortName(), formResult2);
		formNameResultMap.put(formResult3.getShortName(), formResult3);
		formNameResultMap.put(formResult4.getShortName(), formResult4);
	}
	
	
	@Test
	public void basicFormStudyToFormResultsTest() {
		List<BasicFormStudy> formStudies = new ArrayList<BasicFormStudy>();
		
		BasicFormStudy formStudy1 = new BasicFormStudy();
		formStudy1.setForm("FS111219");
		List<String> studies1 = new ArrayList<String>();
		studies1.add("FITBIR-STUDY0000428");
		studies1.add("FITBIR-STUDY0000418");
		formStudy1.setStudies(studies1);
		formStudies.add(formStudy1);
		
		BasicFormStudy formStudy2 = new BasicFormStudy();
		formStudy2.setForm("ADAPT_MEDS");
		List<String> studies2 = new ArrayList<String>();
		studies2.add("FITBIR-STUDY0000428");
		studies2.add("FITBIR-STUDY0000413");
		formStudy2.setStudies(studies2);
		formStudies.add(formStudy2);

		when(resultManager.getFormByShortName(anyString()))
				.thenAnswer(i -> formNameResultMap.get(i.getArguments()[0]));
		List<FormResult> formResults = dataService.basicFormStudyToFormResults(formStudies);
		
		assertEquals(formResults.size(), 2);
		
		boolean form1Exists = false, form2Exists = false;
		
		for (FormResult formResult : formResults) {
			if (formResult.getShortName().equals("FS111219")) {
				form1Exists = true;
				assertEquals(formResult.getStudies().size(), 2);
				
			} else if (formResult.getShortName().equals("ADAPT_MEDS")) {
				form2Exists = true;
				assertEquals(formResult.getStudies().size(), 1);
				assertEquals(formResult.getStudies().get(0).getPrefixedId(), "FITBIR-STUDY0000428");
			} 
		}
		
		assertTrue(form1Exists && form2Exists);
	}

	@Test
	public void basicStudyFormToFormResultsTest() {
		List<BasicStudyForm> studyForms = new ArrayList<BasicStudyForm>();
		
		BasicStudyForm studyForm1 = new BasicStudyForm();
		studyForm1.setStudy("FITBIR-STUDY0000428");
		List<String> forms1 = new ArrayList<String>();
		forms1.add("FS111219");
		forms1.add("ADAPT_MEDS");
		studyForm1.setForms(forms1);
		studyForms.add(studyForm1);
		
		BasicStudyForm studyForm2 = new BasicStudyForm();
		studyForm2.setStudy("FITBIR-STUDY0000418");
		List<String> forms2 = new ArrayList<String>();
		forms2.add("FS111219");
		forms2.add("SF36v2");
		studyForm2.setForms(forms2);
		studyForms.add(studyForm2);
		
		List<String> prefixIds = new ArrayList<String>();
		prefixIds.add(studyForm1.getStudy());
		prefixIds.add(studyForm2.getStudy());
		
		Multimap<String, FormResult> formResultMap = ArrayListMultimap.create();
		
		FormResult formResult1 = formNameResultMap.get("FS111219");
		formResultMap.put("FITBIR-STUDY0000418", formResult1);
		formResultMap.put("FITBIR-STUDY0000428", formResult1);
		
		FormResult formResult2 = formNameResultMap.get("ADAPT_MEDS");
		formResultMap.put("FITBIR-STUDY0000428", formResult2);
		
		FormResult formResult3 = formNameResultMap.get("SF36v2");
		formResultMap.put("FITBIR-STUDY0000418", formResult3);
		
		FormResult formResult4 = formNameResultMap.get("testNotIn");
		formResultMap.put("FITBIR-STUDY0000418", formResult4);

		when(resultManager.searchFormsByStudyPrefixedIds(prefixIds)).thenReturn(formResultMap);
		List<FormResult> formResults = dataService.basicStudyFormToFormResults(studyForms);
		
		assertEquals(formResults.size(), 3);
		
		boolean form1Exists = false, form2Exists = false, form3Exists = false, form4Exists = false;
		
		for (FormResult formResult : formResults) {
			if (formResult.getShortName().equals(formResult1.getShortName())) {
				form1Exists = true;
				assertEquals(formResult.getStudies().size(), 2);
				
			} else if (formResult.getShortName().equals(formResult2.getShortName())) {
				form2Exists = true;
				assertEquals(formResult.getStudies().size(), 1);
				assertEquals(formResult.getStudies().get(0).getPrefixedId(), "FITBIR-STUDY0000428");
				
			} else if (formResult.getShortName().equals(formResult3.getShortName())) {
				form3Exists = true;
				assertEquals(formResult.getStudies().size(), 1);
				assertEquals(formResult.getStudies().get(0).getPrefixedId(), "FITBIR-STUDY0000418");
				
			} else if (formResult.getShortName().equals(formResult4.getShortName())) {
				form4Exists = true;
			} 
		}
		
		assertTrue(form1Exists && form2Exists && form3Exists && !form4Exists);
	}
	
	
}
