package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.metastudy.model.hibernate.MetaStudyAccessRecord;

import org.apache.taglibs.display.Decorator;

public class MetaStudyAccessRecordsListDecorator extends Decorator {

	public String getSelectCheckbox() {
		
		MetaStudyAccessRecord msar = (MetaStudyAccessRecord) this.getObject();
		String msarName = null;
		
		if(msar.getMetaStudyData() != null){
			msarName = msar.getMetaStudyData().getName();
		} else {
			msarName = msar.getSupportingDocumentation().getName();
		}
		
		Long msarId = msar.getId();
		String output = "<input  type=\"checkbox\" name=\"docCheckbox\" value=\"" + msarId + "_" + msarName + "\" />\n";
       	return output;
	}
	
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
