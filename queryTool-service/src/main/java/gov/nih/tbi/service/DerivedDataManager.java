package gov.nih.tbi.service;

import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.query.model.DerivedDataKey;
import gov.nih.tbi.query.model.DerivedDataRow;
import gov.nih.tbi.query.model.RepeatableGroupDataElement;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DerivedDataManager {

	/**
	 * Given form info, repeatable group and data element info, and a set of GUIDs to filter by, return data from the
	 * given form and data elements. The map returned is indexed by fields inside deriveDataKey
	 * 
	 * @param formNameAndVersion
	 * @param repeatableGroupDataElements
	 * @param guids
	 * @return
	 */
	public Map<DerivedDataKey, DerivedDataRow> getDerivedData(NameAndVersion formNameAndVersion,
			List<RepeatableGroupDataElement> repeatableGroupDataElements, Set<String> guids);
}
