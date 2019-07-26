package gov.nih.nichd.ctdb.protocol.tag;

import java.util.Date;

import gov.nih.nichd.ctdb.protocol.domain.MilesStone;
import gov.nih.tbi.idt.ws.IdtDecorator;

/**
 * 
 * @author kollas2
 *
 */
public class ProtocolMilesStoneIdtDecorator extends IdtDecorator {
	
	public ProtocolMilesStoneIdtDecorator() {
		super();
	}
	
	public int getId() {
		MilesStone milesStone = (MilesStone) getObject();
		return milesStone.getId();
	}	
	
	public String getName() {
		MilesStone milesStone = (MilesStone) getObject();
		return milesStone.getName();
	}
	
	public Date getMilesStoneDate() {
		MilesStone milesStone = (MilesStone) getObject();
		return milesStone.getMilesStoneDate();
	}
	
}
