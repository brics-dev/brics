package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue(value="E")
public class EventLogDocumentation extends SupportingDocumentation implements Serializable {
	
	private static final long serialVersionUID = 5622704520312389253L;
	
	@ManyToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinColumn(name = "event_log_id")
    private EventLog eventLog;
	
	public EventLogDocumentation() {
	}
	
	public EventLog getEventLog() {
		return eventLog;
	}

	public void setEventLog(EventLog eventLog) {
		this.eventLog = eventLog;
	}

}
