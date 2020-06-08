package gov.nih.tbi.taglib.datatableDecorators;

import java.util.Set;

import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.hibernate.EventLog;
import gov.nih.tbi.repository.model.hibernate.EventLogDocumentation;

public class EventLogListIdtDecorator extends IdtDecorator {

	EventLog eventLog;

	public String initRow(Object obj, int viewIndex) {
		String feedback = super.initRow(obj, viewIndex);

		if (obj instanceof EventLog) {
			eventLog = (EventLog) obj;
		}
		return feedback;
	}

	public String getActionTaken() {
		String discription = null;
		for (EventType eventtype : EventType.values()) {
			if (eventLog.getTypeStr().trim().equals(eventtype.getId())) {
				discription = eventtype.getDescription();
			}
		}

		return discription;
	}

	public String getDocNameLink() {
		Set<EventLogDocumentation> eventLogDocs = eventLog.getSupportingDocumentationSet();
		String links = "";

		// If user file has been saved, creates a file downloading link
		for (EventLogDocumentation eventLogDoc : eventLogDocs) {
			if (eventLogDoc.getId() != null && eventLogDoc.getUserFile().getId() != null) {
				String link = "fileDownloadAction!download.action?fileId=" + eventLogDoc.getUserFile().getId();
				links += "<a href=\"" + link + "\">" + eventLogDoc.getName() + "</a>\r\n";
			} else {
				links += eventLogDoc.getName();
			}
		}
		
		return links;
	}

}
