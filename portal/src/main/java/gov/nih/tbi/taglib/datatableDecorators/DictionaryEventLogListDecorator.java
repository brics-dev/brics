package gov.nih.tbi.taglib.datatableDecorators;



import java.util.Set;

import org.apache.taglibs.display.Decorator;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;
import gov.nih.tbi.dictionary.model.hibernate.DictionarySupportingDocumentation;

public class DictionaryEventLogListDecorator extends Decorator {
	DictionaryEventLog eventLog;
	
	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);
		
	     eventLog = (DictionaryEventLog) this.getObject();
		return feedback;
	}
	
	public String getUser(){
		
		String fullName="";
		
		if(eventLog.getUser()!=null){
			fullName = eventLog.getUser().getFullName();
		}
		
		return fullName;
	}
	
	public String getActionTaken(){
		String discription = "";
		
		if(eventLog.getEventTypeStr()!=null){
			discription = eventLog.getEventTypeStr();
		}
    	for(EventType eventtype: EventType.values()){
    		if(discription.trim().equals(eventtype.getId())){
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
    	 Set <DictionarySupportingDocumentation> eventLogDocs = eventLog.getSupportingDocumentationSet();
    	 
    	 String links="";
    	 
    	// If user file has been saved, creates a file downloading link
    	 for(DictionarySupportingDocumentation eventLogDoc:eventLogDocs){
    		 if(eventLogDoc.getId()!=null && eventLogDoc.getUserFile().getId()!=null){
    			 String link = "fileDownloadAction!download.action?fileId=" + eventLogDoc.getUserFile().getId();
    			 links+="<a href=\"" + link + "\">" + eventLogDoc.getName() + "</a>\r\n";
    		 }else{
    			 links+= eventLogDoc.getName();
    		 }   		
    	 }
    	 return links;
			
	}
	   
	   public String getChangeMade(){
		   
		   String changeMade = "";
		   
		   if(eventLog.getMinorMajorDesc()!=null){
				String desc = eventLog.getMinorMajorDesc();
						
				if (desc.length() > PortalConstants.ELLIPSIS_CHARACTER_COUNT) {
					changeMade = "<span class=\"descBeginning\">";
					changeMade += desc.substring(0, PortalConstants.ELLIPSIS_CHARACTER_COUNT + 1);
					changeMade +=
							" <a href=\"javascript:;\" class=\"ellipsisExpandCollapse\" onclick=\"ellipsisExpandCollapse(this)\">...</a></span>";
					changeMade += "<span class=\"descAll\">";
					changeMade += desc;
					changeMade +=
							" <a href=\"javascript:;\" class=\"ellipsisExpandCollapse\" onclick=\"ellipsisExpandCollapse(this)\">collapse</a></span>";
				} else {
					changeMade = desc;
				}
				
			}
		   return changeMade;
	   }

}
