package gov.nih.tbi.taglib.datatableDecorators;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.text.SimpleDateFormat;

import javax.servlet.jsp.PageContext;

import org.apache.struts2.ServletActionContext;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class DataElementListIdtDecorator extends IdtDecorator {
	
	DataElement dataElement;
	boolean isAdmin;
	
	public String initRow(Object rowObj, int rowIndex) {
		String feedback = super.initRow(rowObj,rowIndex);
		
		isAdmin = (boolean) ServletActionContext.getRequest().getAttribute("inAdmin");
		
		if (obj instanceof DataElement) {
			dataElement = (DataElement) obj;
		}
		
		return feedback;
	}

	public String getTitleLink() {
	
		String title = BRICSStringUtils.formatStringForJson(stringToEscape(dataElement.getTitle()));
		String output = "";
		if(isAdmin)
			output = "<a href=\"/portal/dictionaryAdmin/dataElementAction!view.action?dataElementName=" + getVariableName() + "\">";
		else
			output = "<a href=\"/portal/dictionary/dataElementAction!view.action?dataElementName=" + getVariableName() + "\">";
		output += title;
		output += "</a>";
		return output;
	}
	
	public String getTitleViewLink() {
		
		String title = BRICSStringUtils.formatStringForJson(stringToEscape(dataElement.getTitle()));
		String output = "<a class=\"lightbox\" href=\"/portal/dictionary/dataElementAction!viewDetails.ajax?statusChange=true&dataElementName="+getVariableName()+"\">"+title+"</a>";	
		
		return output;	
	}
	public String getVariableName(){
		
		return BRICSStringUtils.formatStringForJson(dataElement.getName());
	}
	
	public String getType(){
		
		String categoryType = null;
		
		if (dataElement.getCategory() != null) {
		categoryType = (dataElement.getCategory().getName().equals("Unique Data Element")) ? "UDE" : "CDE";
		} 
		
		return categoryType;
	}
	
	public String getModifiedDate() {
		
		SimpleDateFormat df = new SimpleDateFormat(ModelConstants.ISO_DATE_FORMAT);

        return dataElement.getModifiedDate() != null ? df.format(dataElement.getModifiedDate()) : df.format(dataElement.getDateCreated());
	}
	
	public String getStatus() {
		
		return dataElement.getStatus().getName();
	}
	
	/*
	 * escape HTML for display
	 */
	private String stringToEscape(String title){
		title = escapeHtml(title);
		return title;
	}

}
