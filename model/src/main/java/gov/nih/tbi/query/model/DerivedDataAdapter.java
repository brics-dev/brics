package gov.nih.tbi.query.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DerivedDataAdapter extends XmlAdapter<DerivedDataJaxbElements, HashMap<DerivedDataKey, DerivedDataRow>> {

	@Override
	public HashMap<DerivedDataKey, DerivedDataRow> unmarshal(DerivedDataJaxbElements elements) throws Exception {
		List<DerivedDataJaxbElement> elementList = elements.getDerivedDataList();

		HashMap<DerivedDataKey, DerivedDataRow> derivedDataMap = new HashMap<DerivedDataKey, DerivedDataRow>();

		for (DerivedDataJaxbElement element : elementList) {
			String guid = element.getGuid();
			String visitType = element.getVisitType();
			List<String> keys = element.getKeys();
			List<String> values = element.getValues();

			DerivedDataKey derivedKey = new DerivedDataKey(guid, visitType);
			DerivedDataRow derivedRow = new DerivedDataRow();

			for (int i = 0; i < keys.size(); i++) {
				String currentKey = keys.get(i);
				String currentValue = values.get(i);
				derivedRow.put(currentKey, currentValue);
			}

			derivedDataMap.put(derivedKey, derivedRow);
		}

		return derivedDataMap;
	}

	@Override
	public DerivedDataJaxbElements marshal(HashMap<DerivedDataKey, DerivedDataRow> derivedDataMap) throws Exception {
		DerivedDataJaxbElements elements = new DerivedDataJaxbElements();
		List<DerivedDataJaxbElement> elementList = new ArrayList<DerivedDataJaxbElement>();
		elements.setDerivedDataList(elementList);

		for (Entry<DerivedDataKey, DerivedDataRow> derivedDataEntry : derivedDataMap.entrySet()) {
			DerivedDataKey derivedKey = derivedDataEntry.getKey();
			DerivedDataRow derivedRow = derivedDataEntry.getValue();

			String guid = derivedKey.getGuid();
			String visitType = derivedKey.getVisitType();

			DerivedDataJaxbElement element = new DerivedDataJaxbElement();
			element.setGuid(guid);
			element.setVisitType(visitType);

			for (Entry<String, String> rowEntry : derivedRow.getRow().entrySet()) {
				String currentKey = rowEntry.getKey();
				String currentValue = rowEntry.getValue();

				element.getKeys().add(currentKey);
				element.getValues().add(currentValue);
			}

			elementList.add(element);
		}

		return elements;
	}
}
