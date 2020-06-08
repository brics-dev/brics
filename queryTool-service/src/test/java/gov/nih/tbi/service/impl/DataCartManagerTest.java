package gov.nih.tbi.service.impl;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.service.DataCartManager;
import gov.nih.tbi.service.cache.QueryMetaDataCache;
import gov.nih.tbi.service.model.DataCart;
import gov.nih.tbi.service.model.MetaDataCache;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

public class DataCartManagerTest {

	@Test
	public void addToCartTest() {

		DataCart dataCart = new DataCart();
		StudyResult testStudy = new StudyResult();
		testStudy.setUri("study1");
		List<FormResult> testForms = new ArrayList<>();
		FormResult form1 = new FormResult();
		form1.setUri("form1");
		form1.setRepeatableGroups(new ArrayList<RepeatableGroup>());
		testForms.add(form1);
		FormResult form2 = new FormResult();
		form2.setUri("form2");
		form2.setRepeatableGroups(new ArrayList<RepeatableGroup>());
		testForms.add(form2);

		DataCartManager dataCartManager = new DataCartManagerImpl();

		dataCartManager.addToCart(dataCart, form1, testStudy);
		dataCartManager.addToCart(dataCart, form2, testStudy);
		assertTrue(dataCart.getFormsInCart().values().contains(form1));
		assertTrue(dataCart.getFormsInCart().values().contains(form2));
		for (FormResult form : dataCart.getFormsInCart().values()) {
			assertTrue(form.getStudies().contains(testStudy));
		}
	}

	@Test
	public void removeStudyFromCartTest() {

		DataCart dataCart = new DataCart();
		StudyResult testStudy = new StudyResult();
		testStudy.setUri("study1");
		List<FormResult> testForms = new ArrayList<>();
		FormResult form1 = new FormResult();
		form1.setUri("form1");
		form1.setRepeatableGroups(new ArrayList<RepeatableGroup>());
		testForms.add(form1);
		FormResult form2 = new FormResult();
		form2.setUri("form2");
		form2.setRepeatableGroups(new ArrayList<RepeatableGroup>());
		testForms.add(form2);

		DataCartManager dataCartManager = new DataCartManagerImpl();

		dataCartManager.addToCart(dataCart, form1, testStudy);
		dataCartManager.addToCart(dataCart, form2, testStudy);

		dataCartManager.removeStudyFromCart(dataCart, testStudy);

		assertTrue(dataCart.getFormsInCart().isEmpty());
	}

	@Test
	public void removeFormFromCartTest() {

		DataCart dataCart = new DataCart();
		StudyResult testStudy = new StudyResult();
		testStudy.setUri("study1");
		List<FormResult> testForms = new ArrayList<>();
		FormResult form1 = new FormResult();
		form1.setUri("form1");
		form1.setRepeatableGroups(new ArrayList<RepeatableGroup>());
		testForms.add(form1);
		FormResult form2 = new FormResult();
		form2.setUri("form2");
		form2.setRepeatableGroups(new ArrayList<RepeatableGroup>());
		testForms.add(form2);

		DataCartManager dataCartManager = new DataCartManagerImpl();

		dataCartManager.addToCart(dataCart, form1, testStudy);
		dataCartManager.addToCart(dataCart, form2, testStudy);

		FormResult formToRemove = dataCart.getFormsInCart().get("form1");
		dataCartManager.removeFormFromCart(dataCart, formToRemove);

		assertFalse(dataCart.getFormsInCart().values().contains(formToRemove));

		formToRemove = dataCart.getFormsInCart().get("form2");
		dataCartManager.removeFormFromCart(dataCart, formToRemove);

		assertFalse(dataCart.getFormsInCart().values().contains(formToRemove));
	}

	@Test
	public void selectAllDataElementsTest() {
		MetaDataCache metaDataCache = new QueryMetaDataCache();
		FormResult form1 = new FormResult();
		List<RepeatableGroup> rgList = new ArrayList<>();
		RepeatableGroup rg1 = new RepeatableGroup();
		DataElement de1 = new DataElement();
		de1.setSelected(false);
		rg1.addDataElement(metaDataCache, de1);
		DataElement de2 = new DataElement();
		de2.setSelected(false);
		rg1.addDataElement(metaDataCache, de2);
		RepeatableGroup rg2 = new RepeatableGroup();
		DataElement de3 = new DataElement();
		de3.setSelected(false);
		rg2.addDataElement(metaDataCache, de3);
		rgList.add(rg1);
		rgList.add(rg2);
		form1.setRepeatableGroups(rgList);

		DataCart dataCart = new DataCart();
		Map<String, FormResult> formsInCart = new HashMap<>();
		formsInCart.put("form1", form1);
		dataCart.setFormsInCart(formsInCart);
		DataCartManager dataCartManager = new DataCartManagerImpl();
		dataCartManager.selectAllDataEements(dataCart);

		for (FormResult form : dataCart.getFormsInCart().values()) {
			for (RepeatableGroup rg : form.getRepeatableGroups()) {
				for (DataElement de : rg.getDataElements()) {
					assertTrue(de.isSelected());
				}
			}
		}
	}
}
