package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * XmlAdapter for passing map of value range objects from web service
 */
public class DEValueRangeMapAdapter extends XmlAdapter<DEValueRangeMapAdapter.MapOfMapType, Map<String, Map<String, ValueRange>>> {

	protected static class ValueRangeMapEntryType {

		@XmlAttribute
		public String value;

		@XmlElement(nillable = true)
		public ValueRange valueRange;
	}


	protected static class DEMapEntryType {

		@XmlAttribute
		public String deName;

		@XmlElement
		public List<ValueRangeMapEntryType> valueRangeMaps = new ArrayList<ValueRangeMapEntryType>();
	}


	protected static class MapOfMapType {

		@XmlElement
		public List<DEMapEntryType> valueRangeMapList = new ArrayList<DEMapEntryType>();
	}


	@Override
	public Map<String, Map<String, ValueRange>> unmarshal(MapOfMapType v) throws Exception {

		Map<String, Map<String, ValueRange>> mainMap = new HashMap<String, Map<String, ValueRange>>();

		for (DEMapEntryType myMapEntryType : v.valueRangeMapList) {

			Map<String, ValueRange> valueRangeMap = new HashMap<String, ValueRange>();
			for (ValueRangeMapEntryType valueRangeMapType : myMapEntryType.valueRangeMaps) {
				valueRangeMap.put(valueRangeMapType.value, valueRangeMapType.valueRange);
			}
			mainMap.put(myMapEntryType.deName, valueRangeMap);
		}

		return mainMap;
	}


	@Override
	public MapOfMapType marshal(Map<String, Map<String, ValueRange>> v) throws Exception {
		MapOfMapType myMapType = new MapOfMapType();

		List<DEMapEntryType> entry = new ArrayList<DEMapEntryType>();

		for (String deName : v.keySet()) {
			DEMapEntryType myMapEntryType = new DEMapEntryType();
			Map<String, ValueRange> valueRangeMap = v.get(deName);

			List<ValueRangeMapEntryType> valueRangeList = new ArrayList<ValueRangeMapEntryType>();

			for (String value : valueRangeMap.keySet()) {

				ValueRangeMapEntryType valueRangeMapType = new ValueRangeMapEntryType();
				valueRangeMapType.value = value;
				valueRangeMapType.valueRange = valueRangeMap.get(value);
				valueRangeList.add(valueRangeMapType);
			}

			myMapEntryType.deName = deName;
			myMapEntryType.valueRangeMaps = valueRangeList;
			entry.add(myMapEntryType);
		}

		myMapType.valueRangeMapList = entry;
		return myMapType;
	}

}
