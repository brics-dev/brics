package gov.nih.nichd.ctdb.protocol.util;

import java.util.Comparator;

import gov.nih.nichd.ctdb.protocol.domain.PrepopDataElement;

public class PrePopDataElementComparator
		implements
			Comparator<PrepopDataElement> {

	public PrePopDataElementComparator() {
	}

	@Override
	public int compare(PrepopDataElement prepopDE1,
			PrepopDataElement prepopDE2) {

		if (prepopDE1 != null && prepopDE2 != null) {
			return Integer.compare(prepopDE1.getId(), prepopDE2.getId());
		} else {
			return -1;
		}
	}

}
