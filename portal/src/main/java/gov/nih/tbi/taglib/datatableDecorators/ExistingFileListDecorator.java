package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.repository.model.hibernate.UserFile;

public class ExistingFileListDecorator extends Decorator {
	
	public String getFileName() {
		
		UserFile userFile = (UserFile) this.getObject();
		
		return "<a href=\"fileDownloadAction!download.action?fileId=" + userFile.getId() + "\">" + userFile.getName() + "</a>";
		 
		
	}
	
	public String getFileType() {
		UserFile userFile = (UserFile) this.getObject();
		return userFile.getDescription();
			
	}
		
	
	public String getDateUploaded() {
		UserFile userFile = (UserFile) this.getObject();
		return userFile.getUploadDateString();
		
	}

	
	
}
