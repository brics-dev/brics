package gov.nih.tbi.repository.util;


import gov.nih.tbi.repository.model.AbstractStudy;

import java.util.Comparator;

import com.opensymphony.xwork2.inject.Inject;

public class TitleComparator implements Comparator<AbstractStudy> {
	public TitleComparator() {
		super();
	}

	@Override
	public int compare(AbstractStudy o1, AbstractStudy o2) {
		if (o1 == o2) {
			return 0;
		} else if (o1.getTitle() == null) {
			return -1;
		} else if (o2.getTitle() == null) {
			return 1;
		} else {
			return o1.getTitle().compareToIgnoreCase(o2.getTitle());
		}
	}

}
