package gov.nih.tbi.pojo;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import gov.nih.tbi.service.cache.QueryMetaDataCache;
import gov.nih.tbi.service.model.MetaDataCache;

public class RepeatableGroupTest {

	@Test
	public void getIndexOfDataElementTest() {
		MetaDataCache metaDataCache = new QueryMetaDataCache();
		RepeatableGroup rg = new RepeatableGroup();

		DataElement de1 = new DataElement();
		de1.setUri("de1");
		de1.setName("de1");
		rg.addDataElement(metaDataCache, de1);

		DataElement de2 = new DataElement();
		de2.setUri("de2");
		de2.setName("de2");
		rg.addDataElement(metaDataCache, de2);

		DataElement de3 = new DataElement();
		de3.setUri("de3");
		de3.setName("de3");
		rg.addDataElement(metaDataCache, de3);

		DataElement de4 = new DataElement();
		de4.setUri("de4");
		de4.setName("de4");
		rg.addDataElement(metaDataCache, de4);

		assertEquals(rg.getIndexOfDataElement(de3), 2);
	}

	@Test
	public void getIndexOfDataElementTest2() {
		MetaDataCache metaDataCache = new QueryMetaDataCache();
		RepeatableGroup rg = new RepeatableGroup();

		DataElement de1 = new DataElement();
		de1.setUri("de1");
		de1.setName("de1");
		rg.addDataElement(metaDataCache, de1);

		DataElement de2 = new DataElement();
		de2.setUri("de2");
		de2.setName("de2");
		rg.addDataElement(metaDataCache, de2);

		DataElement de3 = new DataElement();
		de3.setUri("de3");
		de3.setName("de3");
		rg.addDataElement(metaDataCache, de3);

		DataElement de4 = new DataElement();
		de4.setUri("de4");
		de4.setName("de4");
		rg.addDataElement(metaDataCache, de4);

		assertEquals(rg.getIndexOfDataElement(de1), 0);
	}
}
