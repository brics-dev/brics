package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;
import gov.nih.tbi.query.model.hibernate.SavedQuery;

import org.apache.taglibs.display.Decorator;

public class MetaStudySavedQueryListDecorator extends Decorator {

	public String getSelectRadio() {
		
		SavedQuery sq = (SavedQuery) this.getObject();
		
		String output = "<input  type=\"radio\" name=\"savedQueryRadio\" value=\"" + sq.getId() + "\" />\n";
       	return output;
	}
	
	public String getDataNameLink() {
	
		SavedQuery sq = (SavedQuery) this.getObject();
				
		return "<a href=\"javascript:viewSavedQuery(\'" + sq.getId() + "\')\">" + sq.getName() + "</a>";
	}
}
