package gov.nih.nichd.ctdb.protocol.util;

import java.util.Comparator;

import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;

public class BasicEformTitleComparator implements Comparator<BasicEform> {
	public BasicEformTitleComparator() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(BasicEform be1, BasicEform be2) {
		if (be1.getTitle() != null && be2.getTitle() != null)

		{ return be1.getTitle().compareToIgnoreCase(be2.getTitle()); }
			else

		{ 
			return -1;
			}
		
		
	}

}
