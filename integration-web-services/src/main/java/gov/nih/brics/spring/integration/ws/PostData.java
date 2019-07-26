package gov.nih.brics.spring.integration.ws;

import java.io.File;

public class PostData {
	private String originalFile;
	private String newFile;
	private String originalPath;
	private String newPath;
	private File file;

	public String getOriginalFile() {
		return originalFile;
	}

	public void setOriginalFile(String originalFile) {
		this.originalFile = originalFile;
	}

	public String getNewFile() {
		return newFile;
	}

	public void setNewFile(String newFile) {
		this.newFile = newFile;
	}
	
	public String getOriginalPath() {
		return originalPath;
	}

	public void setOriginalPath(String originalPath) {
		originalPath = originalPath.replaceAll("\\s+","\\\\ ");
		this.originalPath = originalPath;
	}

	public String getNewPath() {
		return newPath;
	}

	public void setNewPath(String newPath) {
		this.newPath = newPath;
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return "{originalFile: " + originalFile + ", newFile: " + newFile + ", originalPath: " + originalPath
				+ ", newPath: " + newPath + "}";
	}
}