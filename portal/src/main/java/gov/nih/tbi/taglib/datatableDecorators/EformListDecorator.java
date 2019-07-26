package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.common.util.DictionaryUtils;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.taglibs.display.Decorator;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class EformListDecorator extends Decorator {
	
	BasicEform eform;
	boolean isAdmin;
	
	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);
		
		isAdmin = (boolean) this.getPageContext().findAttribute("inAdmin");
		
		if (obj instanceof Eform) {
			eform = (BasicEform) obj;
		}
		
		return feedback;
	}

	public String getTitleLink() {
		BasicEform eform = (BasicEform) this.getObject();
		

		String title = stringToEscape(eform.getTitle());
		String output = "";
		if(isAdmin)
			output = "<a href=\"/portal/dictionaryAdmin/eFormAction!view.action?eformId=" + eform.getId() + "\">";
		else
			output = "<a href=\"/portal/dictionary/eFormAction!view.action?eformId=" + eform.getId() + "\">";
		output += title;
		output += "</a>";
		return output;
	}
	
	public String getTitleAdminLink() {
		BasicEform eform = (BasicEform) this.getObject();
		

		String title = stringToEscape(eform.getTitle());
		String output = "<a href=\"/portal/dictionaryAdmin/eFormAction!view.action?eformId=" + eform.getId() + "\">";
		output += title;
		output += "</a>";
		return output;
	}
	
	public String getStatus() {
		BasicEform eform = (BasicEform) this.getObject();
		return eform.getStatus().getType();
	}
	
	public String getModifiedDate() {
		BasicEform eform = (BasicEform) this.getObject();
		SimpleDateFormat df = new SimpleDateFormat(ModelConstants.ISO_DATE_FORMAT);

        return df.format(DictionaryUtils.getMostCurrentDate(eform.getAllDates()));
	}
	
	public String getFormStructureTitle(){
		BasicEform eform = (BasicEform) this.getObject();
		//will need to consider making a request for the latest to get the title
		return stringToEscape(eform.getFormStructureTitle());
	}
	
	/*
	 * escape HTML for display
	 */
	private String stringToEscape(String title){
		title = escapeHtml(title);
		return title;
	}
}
