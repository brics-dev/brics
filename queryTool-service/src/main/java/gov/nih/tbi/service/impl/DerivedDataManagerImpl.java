package gov.nih.tbi.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.dao.DerivedDataDao;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.query.model.DerivedDataKey;
import gov.nih.tbi.query.model.DerivedDataRow;
import gov.nih.tbi.query.model.RepeatableGroupDataElement;
import gov.nih.tbi.service.DerivedDataManager;

@Component
@Scope("application")
public class DerivedDataManagerImpl implements DerivedDataManager {

	@Autowired
	private DerivedDataDao derivedDataDao;

	/**
	 * @inheritDoc
	 */
	public Map<DerivedDataKey, DerivedDataRow> getDerivedData(NameAndVersion formNameAndVersion,
			List<RepeatableGroupDataElement> repeatableGroupDataElements, Set<String> guids) {
		return derivedDataDao.getDerivedData(formNameAndVersion, repeatableGroupDataElements, guids);
	}
}
