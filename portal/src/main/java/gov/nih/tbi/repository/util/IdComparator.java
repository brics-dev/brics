package gov.nih.tbi.repository.util;

import gov.nih.tbi.repository.model.AbstractStudy;

import java.util.Comparator;

public class IdComparator implements Comparator<AbstractStudy> {
	public IdComparator() {
		super();
	}

	@Override
	public int compare(AbstractStudy o1, AbstractStudy o2) {
		if (o1 == o2) {
			return 0;
		} else if (o1.getPrefixedId() == null) {
			return -1;
		} else if (o2.getPrefixedId() == null) {
			return 1;
		} else {
			return o1.getPrefixedId().compareToIgnoreCase(o2.getPrefixedId());
		}
	}

}

