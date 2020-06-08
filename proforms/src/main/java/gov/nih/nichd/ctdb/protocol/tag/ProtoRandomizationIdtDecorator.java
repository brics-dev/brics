package gov.nih.nichd.ctdb.protocol.tag;

import gov.nih.nichd.ctdb.protocol.domain.ProtocolRandomization;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class ProtoRandomizationIdtDecorator extends IdtDecorator {

	/**
	 * Default Constructor
	 */
	public ProtoRandomizationIdtDecorator() {
		super();
	}
	
	public long getSequence() {
		ProtocolRandomization protoRan = (ProtocolRandomization) getObject();
		return protoRan.getSequence();
	}
	
	public String getGroupName() {
		ProtocolRandomization protoRan = (ProtocolRandomization) getObject();
		return protoRan.getGroupName();
	}
	
	public String getGroupDescription() {
		ProtocolRandomization protoRan = (ProtocolRandomization) getObject();
		return protoRan.getGroupDescription();
	}

}
