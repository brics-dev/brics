package gov.nih.nichd.ctdb.protocol.tag;

import gov.nih.nichd.ctdb.protocol.domain.Procedure;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class ProtocolProcedureIdtDecorator extends IdtDecorator {
	
	public ProtocolProcedureIdtDecorator() {
		super();
	}
	
	public int getProcedureId() {
		Procedure proc = (Procedure) getObject();
		return proc.getId();
	}	
	
	public String getProcedureTypeName() {
		Procedure proc = (Procedure) getObject();
		return proc.getProcedureType().getName();
	}
	
	public String getProcedureName() {
		Procedure proc = (Procedure) getObject();
		return proc.getName();
	}
	
	public String getProcedureIsNew() {
		Procedure proc = (Procedure) getObject();
		return String.valueOf(proc.getIsNew());
	}

}
