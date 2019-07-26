package gov.nih.tbi.repository.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import gov.nih.tbi.repository.model.hibernate.EventLogDocumentation;

public class SessionSupportDocList implements Serializable {

	private static final long serialVersionUID = -5067462382575464758L;

	private Set <EventLogDocumentation> supportDocList;

	public void clear() {

		supportDocList = null;
	}

	public Set<EventLogDocumentation> getSupportingDocumentation() {

		if (supportDocList == null) {
			supportDocList = new HashSet<EventLogDocumentation>();
		}

		return supportDocList;
	}

	public void setSupportingDocumentation(Set<EventLogDocumentation> supportDocList) {

		this.supportDocList = supportDocList;
	}

}
