package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.idt.ws.IdtDecorator;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringEscapeUtils;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;
import gov.nih.tbi.dictionary.portal.BaseDataElementSearchAction;

public class DataElementSearchIdtDecorator extends IdtDecorator {
	
	SemanticDataElement semanticDataElement;
		
	BaseDataElementSearchAction baseDataElementSearchAction;
	
	public StringBuilder getTitle() {
		
		StringBuilder sb = new StringBuilder();
		
		semanticDataElement  = (SemanticDataElement) this.getObject();
		
		String title = StringEscapeUtils.escapeJava(semanticDataElement.getTitle());
		String desc = StringEscapeUtils.escapeJava(semanticDataElement.getDescription());
		
		sb.append(title);
		
		return sb;		 
		
	}
	
	public String getCategoryType() {
		String categoryType = "UNKNOWN";
		// None of these fields should ever be null. This is only put in for early RDF graph development.
		if (semanticDataElement.getCategory() != null) {
			categoryType = (semanticDataElement.getCategory().getName().equals("Unique Data Element")) ? "UDE" : "CDE";
		}
		
		return categoryType;
			
	}
	
	public String getStatus() {
				
		return semanticDataElement.getStatus().getName();
			
	}
	
	public String getDate() {
		
		String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(semanticDataElement.getModifiedDate());
		
		return modifiedDate;
			
	} 
	
	public String getShortName() {
		
		return semanticDataElement.getName();
	}
	
	public String getCategory() {
		
		String categoryType = "UNKNOWN";
		// None of these fields should ever be null. This is only put in for early RDF graph development.
		if (semanticDataElement.getCategory() != null) {
			categoryType = (semanticDataElement.getCategory().getName().equals("Unique Data Element")) ? "UDE" : "CDE";
		}
		
		return categoryType;
	}
		
	
}