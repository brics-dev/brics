package gov.nih.nichd.ctdb.protocol.form;

import gov.nih.nichd.ctdb.common.CtdbForm;

public class StudyEbinderForm extends CtdbForm
{
	private static final long serialVersionUID = -520107483031560282L;
	
	// Binder fields
	private String eBinderTree = "{}";
	private long studyID = Integer.MIN_VALUE;
	private int type = Integer.MIN_VALUE;
	private boolean binderValid = true;
	private String deleteType = "";
	
	// Attachment fields
	private int attachId = Integer.MIN_VALUE;
	private String attachName = "";
	private String attachDescription = "";
	private String attachAuthor = "";
	private int attachPubType = Integer.MIN_VALUE;
	private String attachUrl = "";
	private String attachPubMedId = "";
	//private FormFile attachFile = null;
	private String attachFileName = "";

	/**
	 * @return the eBinderTree
	 */
	public String geteBinderTree() {
		return eBinderTree;
	}

	/**
	 * @param eBinderTree the eBinderTree to set
	 */
	public void seteBinderTree(String eBinderTree) {
		this.eBinderTree = eBinderTree;
	}

	/**
	 * @return the studyID
	 */
	public long getStudyID() {
		return studyID;
	}

	/**
	 * @param studyID the studyID to set
	 */
	public void setStudyID(long studyID) {
		this.studyID = studyID;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the binderValid
	 */
	public boolean isBinderValid() {
		return binderValid;
	}

	/**
	 * @param binderValid the binderValid to set
	 */
	public void setBinderValid(boolean binderValid) {
		this.binderValid = binderValid;
	}

	/**
	 * @return the attachId
	 */
	public int getAttachId() {
		return attachId;
	}

	/**
	 * @param attachId the attachId to set
	 */
	public void setAttachId(int attachId) {
		this.attachId = attachId;
	}

	/**
	 * @return the attachName
	 */
	public String getAttachName() {
		return attachName;
	}

	/**
	 * @param attachName the attachName to set
	 */
	public void setAttachName(String attachName) {
		this.attachName = attachName;
	}

	/**
	 * @return the attachDescription
	 */
	public String getAttachDescription() {
		return attachDescription;
	}

	/**
	 * @param attachDescription the attachDescription to set
	 */
	public void setAttachDescription(String attachDescription) {
		this.attachDescription = attachDescription;
	}

	/**
	 * @return the attachAuthor
	 */
	public String getAttachAuthor() {
		return attachAuthor;
	}

	/**
	 * @param attachAuthor the attachAuthor to set
	 */
	public void setAttachAuthor(String attachAuthor) {
		this.attachAuthor = attachAuthor;
	}

	/**
	 * @return the attachPubType
	 */
	public int getAttachPubType() {
		return attachPubType;
	}

	/**
	 * @param attachPubType the attachPubType to set
	 */
	public void setAttachPubType(int attachPubType) {
		this.attachPubType = attachPubType;
	}

	/**
	 * @return the attachUrl
	 */
	public String getAttachUrl() {
		return attachUrl;
	}

	/**
	 * @param attachUrl the attachUrl to set
	 */
	public void setAttachUrl(String attachUrl) {
		this.attachUrl = attachUrl;
	}

	/**
	 * @return the attachPubMedId
	 */
	public String getAttachPubMedId() {
		return attachPubMedId;
	}

	/**
	 * @param attachPubMedId the attachPubMedId to set
	 */
	public void setAttachPubMedId(String attachPubMedId) {
		this.attachPubMedId = attachPubMedId;
	}

	/**
	 * @return the attachFile
	 */
//	public FormFile getAttachFile() {
//		return attachFile;
//	}
//
//	/**
//	 * @param attachFile the attachFile to set
//	 */
//	public void setAttachFile(FormFile attachFile) {
//		this.attachFile = attachFile;
//	}

	/**
	 * @return the attachFileName
	 */
	public String getAttachFileName() {
		return attachFileName;
	}

	/**
	 * @param attachFileName the attachFileName to set
	 */
	public void setAttachFileName(String attachFileName) {
		this.attachFileName = attachFileName;
	}

	/**
	 * @return the deleteType
	 */
	public String getDeleteType() {
		return deleteType;
	}

	/**
	 * @param deleteType the deleteType to set
	 */
	public void setDeleteType(String deleteType) {
		this.deleteType = deleteType;
	}
}
