package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyAccessRecord;

public class MetaStudyAccessRecordsIdtListDecorator extends IdtDecorator {
	
	public String getDoi(){
		return "";
	}
	
	public String getFileName(){
		MetaStudyAccessRecord msar = (MetaStudyAccessRecord) this.getObject();
		String fileName = null;
		
		if(msar.getMetaStudyData() != null){
			fileName = msar.getMetaStudyData().getName();
		} else {
			fileName = msar.getSupportingDocumentation().getName();
		}
		
		return fileName;
	}	
}
