package gov.nih.tbi.dictionary.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import gov.nih.tbi.dictionary.model.hibernate.*;
import gov.nih.tbi.dictionary.model.DEValueRangeMapAdapter;
import gov.nih.tbi.dictionary.model.DEValueRangeMapAdapter.DEMapEntryType;
import gov.nih.tbi.dictionary.model.DEValueRangeMapAdapter.MapOfMapType;
import gov.nih.tbi.dictionary.model.DEValueRangeMapAdapter.ValueRangeMapEntryType;

public class DEValueRangeMapAdapterTest{
	
	@Test
	public void marshalTest() throws Exception{
		
		MapOfMapType myMapType = new MapOfMapType();
		List<DEMapEntryType> entry = new ArrayList<DEMapEntryType>();
		ValueRange theRange = new ValueRange();

		Map<String, Map<String,ValueRange>> test = new HashMap<String, Map<String,ValueRange>>();
		test.put("teststring", new HashMap<String,ValueRange>());
		test.get("teststring").put("teststring2",theRange);
		
		DEValueRangeMapAdapter v = new DEValueRangeMapAdapter();
		List<ValueRangeMapEntryType> valueRangeList = new ArrayList<ValueRangeMapEntryType>();
		
		ValueRangeMapEntryType valueRangeMapType = new ValueRangeMapEntryType();
		valueRangeMapType.value = "teststring2";
		valueRangeMapType.valueRange = theRange;
		
		DEMapEntryType myMapEntryType = new DEMapEntryType();
		myMapEntryType.deName = "teststring";
		valueRangeList.add(valueRangeMapType);
		myMapEntryType.valueRangeMaps = valueRangeList;
		
		entry.add(myMapEntryType);
		myMapType.valueRangeMapList = entry;
		
		//check if DEName is equal, in this case "teststring"
		Assert.assertEquals(myMapType.valueRangeMapList.get(0).deName,
				(v.marshal(test).valueRangeMapList.get(0).deName));
		//check if ValueRangeMaps value is equal, in this case "teststring2"
		Assert.assertEquals(myMapType.valueRangeMapList.get(0).valueRangeMaps.get(0).value,
				(v.marshal(test).valueRangeMapList.get(0).valueRangeMaps.get(0).value));
		//check if ValueRangeMaps valueRange is equal, in this case empty ValueRange
		Assert.assertEquals(myMapType.valueRangeMapList.get(0).valueRangeMaps.get(0).valueRange,
				(v.marshal(test).valueRangeMapList.get(0).valueRangeMaps.get(0).valueRange));
		
		
	}
}