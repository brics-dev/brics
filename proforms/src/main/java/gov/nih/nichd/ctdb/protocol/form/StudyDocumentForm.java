package gov.nih.nichd.ctdb.protocol.form;

import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.common.StrutsConstants;

public class StudyDocumentForm extends CtdbForm
{
	private static final long serialVersionUID = -8888790041484049072L;
	
	private int studyId = Integer.MIN_VALUE;
	private String title = "";
	private String authors = "";
	private String description = "";
	private int publicationType = 0;
	//private FormFile uploadedFile = null;
	private String url = "";
	private String pubmedId = "";
	private String fileName = "";
	private String changeMode = StrutsConstants.ACTION_ADD_FORM;
	
	/**
	 * @return the studyId
	 */
	public int getStudyId() {
		return studyId;
	}
	
	/**
	 * @param studyId the studyId to set
	 */
	public void setStudyId(int studyId) {
		this.studyId = studyId;
	}
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the authors
	 */
	public String getAuthors() {
		return authors;
	}

	/**
	 * @param authors the authors to set
	 */
	public void setAuthors(String authors) {
		this.authors = authors;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the publicationType
	 */
	public int getPublicationType() {
		return publicationType;
	}

	/**
	 * @param publicationType the publicationType to set
	 */
	public void setPublicationType(int publicationType) {
		this.publicationType = publicationType;
	}

	/**
	 * @return the uploadedFile
	 */
//	public FormFile getUploadedFile() {
//		return uploadedFile;
//	}
//
//	/**
//	 * @param uploadedFile the uploadedFile to set
//	 */
//	public void setUploadedFile(FormFile uploadedFile) {
//		this.uploadedFile = uploadedFile;
//	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the pubmedId
	 */
	public String getPubmedId() {
		return pubmedId;
	}

	/**
	 * @param pubmedId the pubmedId to set
	 */
	public void setPubmedId(String pubmedId) {
		this.pubmedId = pubmedId;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the changeMode
	 */
	public String getChangeMode() {
		return changeMode;
	}

	/**
	 * @param changeMode the changeMode to set
	 */
	public void setChangeMode(String changeMode) {
		this.changeMode = changeMode;
	}
}
