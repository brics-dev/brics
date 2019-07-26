package gov.nih.tbi.repository.model;

import gov.nih.tbi.commons.model.Publication;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public interface SupportingDocumentationInterface {
	public Long getId();
	public String getName();
	public UserFile getUserFile();
	public String getUrl();
	public String getDescription();
	public void setId(Long id);
	public void setUserFile(UserFile userFile);
	public void setUrl(String url);
	public void setDescription(String description);
	public FileType getFileType();
	public void setFileType(FileType fileType);
	public Publication getPublication();
	public String getTitle();
	public void setTitle(String title);

}
