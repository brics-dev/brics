package gov.nih.tbi.taglib.datatableDecorators;



import java.util.Set;

import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;
import gov.nih.tbi.dictionary.model.hibernate.DictionarySupportingDocumentation;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class DictionaryEventLogListIdtDecorator extends IdtDecorator {
	DictionaryEventLog eventLog;
	
	public String initRow(Object rowObj, int rowIndex) {
		String feedback = super.initRow(rowObj, rowIndex);
		
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

			descLine = desc;
			
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
    			 links+="<a class=tdLink href=\"" + link + "\">" + eventLogDoc.getName() + "</a>\r\n";
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
				
				changeMade = desc;
				
			}
		   return changeMade;
	   }

}
