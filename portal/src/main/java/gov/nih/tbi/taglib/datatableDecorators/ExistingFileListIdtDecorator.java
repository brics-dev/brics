package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class ExistingFileListIdtDecorator extends IdtDecorator {
	
	
	private boolean inAccountReviewer;
	private boolean isOnlyAccountReviewer;
	
	public ExistingFileListIdtDecorator(boolean inAccountReviewer, boolean isOnlyAccountReviewer) {
		super();
		this.inAccountReviewer = inAccountReviewer;
		this.isOnlyAccountReviewer = isOnlyAccountReviewer;
	}
	
	
	
	public String getFileName() {	
		UserFile userFile = (UserFile) this.getObject();
		return "<a href=\"fileDownloadAction!download.action?fileId=" + userFile.getId() + "\">" + userFile.getName() + "</a>";
	}
	
	public String getFileType() {
		UserFile userFile = (UserFile) this.getObject();
		if(inAccountReviewer || isOnlyAccountReviewer) {
			return userFile.getDescription();
		}else {
			return userFile.getDescription() + "&nbsp;&nbsp;&nbsp;" +  "<a href=\"javascript:void(0);\" onclick=\"changeFileTypeLightbox(" + userFile.getId() + ",'"   + userFile.getDescription()   +   "')\">" + "Change File Type" + "</a>";
		}	
	}
		
	
	public String getDateUploaded() {
		UserFile userFile = (UserFile) this.getObject();
		if(inAccountReviewer || isOnlyAccountReviewer) {
			return userFile.getUploadDateString();
		}else {
			return userFile.getUploadDateString() + "&nbsp;&nbsp;&nbsp;" +  "<a href=\"javascript:void(0);\" onclick=\"deleteUserFileLightbox(" + userFile.getId() + ",'"   + userFile.getName()   +   "')\">" + "Delete" + "</a>";
		}
	}

	
	
}
