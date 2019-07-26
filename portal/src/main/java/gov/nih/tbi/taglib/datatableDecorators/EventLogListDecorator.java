package gov.nih.tbi.taglib.datatableDecorators;



import java.util.Set;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.repository.model.hibernate.EventLog;
import gov.nih.tbi.repository.model.hibernate.EventLogDocumentation;


public class EventLogListDecorator extends Decorator {
	EventLog eventLog;
	
	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);
		
	     eventLog = (EventLog) this.getObject();
		return feedback;
	}
	
	public String getUser(){
		return eventLog.getUser().getFullName();
	}
	
	public String getActionTaken(){
		String discription = null;
    	for(EventType eventtype: EventType.values()){
    		if(eventLog.getTypeStr().trim().equals(eventtype.getId())){
    			discription=  eventtype.getDescription();
    		}
    	}
    	
    	return discription;
	}
	
	public String getComment(){
		String descLine = "";
		
		if(eventLog.getComment()!=null){
			String desc = eventLog.getComment();
					
			if (desc.length() > PortalConstants.ELLIPSIS_CHARACTER_COUNT) {
				descLine = "<span class=\"descBeginning\">";
				descLine += desc.substring(0, PortalConstants.ELLIPSIS_CHARACTER_COUNT + 1);
				descLine +=
						" <a href=\"javascript:;\" class=\"ellipsisExpandCollapse\" onclick=\"ellipsisExpandCollapse(this)\">...</a></span>";
				descLine += "<span class=\"descAll\">";
				descLine += desc;
				descLine +=
						" <a href=\"javascript:;\" class=\"ellipsisExpandCollapse\" onclick=\"ellipsisExpandCollapse(this)\">collapse</a></span>";
			} else {
				descLine = desc;
			}
			
		}
		
		return descLine;
	}	
	
     public String getDocNameLink() {
    	 Set <EventLogDocumentation> eventLogDocs = eventLog.getSupportingDocumentationSet();
    	 
    	 String links="";
    	 
    	// If user file has been saved, creates a file downloading link
    	 for(EventLogDocumentation eventLogDoc:eventLogDocs){
    		 if(eventLogDoc.getId()!=null && eventLogDoc.getUserFile().getId()!=null){
    			 String link = "fileDownloadAction!download.action?fileId=" + eventLogDoc.getUserFile().getId();
    			 links+="<a href=\"" + link + "\">" + eventLogDoc.getName() + "</a>\r\n";
    		 }else{
    			 links+= eventLogDoc.getName();
    		 }   		
    	 }
    	 return links;
			
	}
}
