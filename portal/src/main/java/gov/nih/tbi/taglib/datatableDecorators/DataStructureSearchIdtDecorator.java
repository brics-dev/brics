package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.idt.ws.IdtDecorator;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringEscapeUtils;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;

public class DataStructureSearchIdtDecorator extends IdtDecorator {
	
	SemanticFormStructure semanticFormStructure;
	
	public StringBuilder getTitle() {
		
		StringBuilder sb = new StringBuilder();
		
		semanticFormStructure  = (SemanticFormStructure) this.getObject();
		
		String title = StringEscapeUtils.escapeJava(semanticFormStructure.getTitle());
		String desc = StringEscapeUtils.escapeJava(semanticFormStructure.getDescription());
		
		sb.append(semanticFormStructure.getShortName()).append("|").append(title).append("|").append(desc != null ? desc : PortalConstants.EMPTY_STRING);
		
		return sb;		 
		
	}
	
	public String getStatus() {
				
		return semanticFormStructure.getStatus().getType();
			
	}
	
	public String getDate() {
		
		String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(semanticFormStructure.getModifiedDate());
		
		return modifiedDate;
			
	} 
		
	
}