package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.taglib.datatableDecorators.DatasetIdtListDecorator;

import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DatasetIdtDecoratorTest {
	
	@Test
	public void testGetNameLinkInAdmin() {
		DatasetIdtListDecorator dec = new DatasetIdtListDecorator();
		BasicDataset dataset = new BasicDataset();
		dataset.setName("Test_Dataset_Name_Long_Form_ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		dataset.setPrefixedId("TEST_123");
		dec.dataset = dataset;
		dec.inAdmin = true;
		Assert.assertEquals(dec.getNameLink(),"<span title=\"Test_Dataset_Name_Long_Form_ABCDEFGHIJKLMNOPQRSTUVWXYZ\"><a href=\"/portal/studyAdmin/datasetAction!view.action?prefixedId=TEST_123\">Test_Dataset_Name_Long_Form_ABCDEFGHIJKLMNOPQRSTU...</a></span>");		
	}
	
	@Test
	public void testGetNameLinkNotInAdmin() {
		DatasetIdtListDecorator dec = new DatasetIdtListDecorator();
		BasicDataset dataset = new BasicDataset();
		dataset.setName("Test_Dataset_Name_Long_Form_ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		dataset.setPrefixedId("TEST_123");
		dec.dataset = dataset;
		dec.inAdmin = false;
		Assert.assertEquals(dec.getNameLink(),"<span title=\"Test_Dataset_Name_Long_Form_ABCDEFGHIJKLMNOPQRSTUVWXYZ\"><a href=\"javascript:viewDataset('TEST_123', 'false')\">Test_Dataset_Name_Long_Form_ABCDEFGHIJKLMNOPQRSTU...</a></span>");		
	}
}