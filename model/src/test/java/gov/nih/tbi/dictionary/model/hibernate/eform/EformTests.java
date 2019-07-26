package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.testng.annotations.Test;

import gov.nih.tbi.dictionary.model.EformRestServiceModel.EformList;
import junit.framework.Assert;

public class EformTests {
	private static final String EFORM_DATA_FILE_ROOT = "src/test/resources/eform/";

	@Test
	public void eformSkipRuleLoadTest() throws Exception {
		File eformXml = new File(EFORM_DATA_FILE_ROOT + "simple-skip-rules.xml");

		Assert.assertTrue("Could not find the test file.", eformXml.exists());

		JAXBContext jc = JAXBContext.newInstance(EformList.class);
		Unmarshaller um = jc.createUnmarshaller();
		EformList eformList = (EformList) um.unmarshal(eformXml);
		Eform eform = eformList.getList().get(0);

		Assert.assertNotNull("Nothing was unmarshalled back from the XML file.", eform);
	}

	@Test
	public void eformCalcRuleLoadTest() throws Exception {
		File eformXml = new File(EFORM_DATA_FILE_ROOT + "simple-calc-rules.xml");

		Assert.assertTrue("Could not find the test file.", eformXml.exists());

		JAXBContext jc = JAXBContext.newInstance(EformList.class);
		Unmarshaller um = jc.createUnmarshaller();
		EformList eformList = (EformList) um.unmarshal(eformXml);
		Eform eform = eformList.getList().get(0);

		Assert.assertNotNull("Nothing was unmarshalled back from the XML file.", eform);
	}

}
