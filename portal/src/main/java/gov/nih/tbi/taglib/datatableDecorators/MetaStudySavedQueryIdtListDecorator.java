package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.query.model.hibernate.SavedQuery;


public class MetaStudySavedQueryIdtListDecorator extends IdtDecorator {
	
	public String getDataNameLink() {
	
		SavedQuery sq = (SavedQuery) this.getObject();
				
		return "<a class=idtLink href=\"javascript:viewSavedQuery(\'" + sq.getId() + "\')\">" + sq.getName() + "</a>";
	}
}
