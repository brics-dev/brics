package gov.nih.tbi.repository.model;

import java.io.File;
import java.io.Serializable;

public class SessionUploadFile implements Serializable {
	
	private static final long serialVersionUID = -7207360995176465065L;
	private File uploadFile;
	private String uploadFileFileName;
	private String uploadFileContentType;
	
	public SessionUploadFile() {}

	public void clear() {
		if (uploadFile != null) {
			uploadFile.delete();
			uploadFile = null;
		}

		uploadFileFileName = null;
		uploadFileContentType = null;
	}
	
	public File getUploadFile() {
		return uploadFile;
	}
	
	public void setUploadFile(File uploadFile) {
		if ((this.uploadFile != null) && this.uploadFile.exists()) {
			this.uploadFile.delete();
			this.uploadFile = null;
		}

		this.uploadFile = uploadFile;
	}

	public String getUploadFileFileName() {
		return uploadFileFileName;
	}

	public void setUploadFileFileName(String uploadFileFileName) {
		this.uploadFileFileName = uploadFileFileName;
	}

	public String getUploadFileContentType() {
		return uploadFileContentType;
	}

	public void setUploadFileContentType(String uploadFileContentType) {
		this.uploadFileContentType = uploadFileContentType;
	}

}
