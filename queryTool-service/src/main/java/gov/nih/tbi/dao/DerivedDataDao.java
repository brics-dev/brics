package gov.nih.tbi.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.query.model.DerivedDataKey;
import gov.nih.tbi.query.model.DerivedDataRow;
import gov.nih.tbi.query.model.RepeatableGroupDataElement;

public interface DerivedDataDao {
	public Map<DerivedDataKey, DerivedDataRow> getDerivedData(NameAndVersion formNameAndVersion,
			List<RepeatableGroupDataElement> repeatableGroupDataElements, Set<String> guids);
}
