package gov.nih.tbi.repository.util;

import gov.nih.tbi.repository.model.AbstractStudy;

import java.util.Comparator;

public class PiComparator implements Comparator<AbstractStudy> {
	public PiComparator() {
		super();
	}

	@Override
	public int compare(AbstractStudy o1, AbstractStudy o2) {
		if (o1 == o2) {
			return 0;
		} else if (o1.getPrincipalInvestigator() == null) {
			return -1;
		} else if (o2.getPrincipalInvestigator() == null) {
			return 1;
		} else {
			return o1.getPrincipalInvestigator().compareToIgnoreCase(o2.getPrincipalInvestigator());
		}
	}

}

