package gov.nih.nichd.ctdb.protocol.action;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.attachments.manager.AttachmentManager;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateArchiveObjectException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.ServerFileSystemException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.protocol.common.ProtocolConstants;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;

public class StudyDocumentAction extends BaseAction {

	private static final long serialVersionUID = -4850974202093740097L;
	private static final Logger log = Logger.getLogger(StudyDocumentAction.class);
    private static final String DOCUMENTACTION_MESSAGES_KEY = "DocumentAction_ActionMessages";
    private static final String MAX_FILE_SIZE = "250MB";

    private String id;
	private int studyId = Integer.MIN_VALUE;
	private String title = "";
	private String authors = "";
	private String description = "";
	private int publicationType = 0;
	private String url = "";
	private String pubmedId = "";
	private File fileUpload = null;
	private String fileUploadFileName = "";
	private String idsToDelete = "";
	private int attachmentType = 0;

	private void setupPage() throws CtdbException {
		
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_DOCUMENTS);
		
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		this.setStudyId(p.getId());
		
		if (session.get(ProtocolConstants.PUBLICATION_TYPE_LIST) == null) {
			LookupManager luMan = new LookupManager();
			session.put(ProtocolConstants.PUBLICATION_TYPE_LIST, 
					luMan.getLookups(LookupType.PUBLICATION_TYPE));
		}
		
		AttachmentManager am = new AttachmentManager();
		request.setAttribute(ProtocolConstants.PUBLICATION_LIST, 
				am.getAttachments(AttachmentManager.FILE_STUDY, p.getId()));
	}
	
	public String execute() throws Exception {
		String strutsResult = BaseAction.SUCCESS;
		this.retrieveActionMessages(StudyDocumentAction.DOCUMENTACTION_MESSAGES_KEY);
				
		try {
			this.setupPage();
			AttachmentManager am = new AttachmentManager();

			if (!Utils.isBlank(getId())) {
				Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
				int fileId = Integer.parseInt(getId());
				Attachment file = am.getAttachment(fileId, p.getId(), AttachmentManager.FILE_STUDY);
				
				this.setStudyId(file.getAssociatedId());
				this.setTitle(file.getName());
				this.setAuthors(file.getAuthors());
				this.setDescription(file.getDescription());
				this.setPublicationType(file.getPublicationType().getId());
				this.setUrl(file.getUrl());
				this.setPubmedId(file.getPubMedId());
				this.setFileUpload(file.getAttachFile());
				this.setFileUploadFileName(file.getFileName());
				this.setAttachmentType(AttachmentManager.FILE_STUDY);
			}
		}
		catch (ObjectNotFoundException onfe) {
			log.error("Could not find attachment with ID " + getId(), onfe);
			addActionError(getText(StrutsConstants.ERROR_NOTFOUND, new String[]{"publication"}));
		}
		catch (CtdbException ce) {
			log.error("Could not retrieve attachment from the database.", ce);
			addActionError("An error occured while trying to retrieve the document. Please try your edit again. " +
					"If the error still persists, please contact the system administrator.");
		}
		catch (NumberFormatException nfe) {
			log.error("An invalid attachment ID (" + getId() + ") given.", nfe);
			addActionError("There is an issue with document table on the page. Please contact the system administrator.");
		}
		
		if (this.hasActionErrors()) {
            strutsResult = StrutsConstants.EXCEPTION;
		}
	
		return strutsResult;
	}
	
	
	public String saveDocument() {
		String strutsResult = BaseAction.SUCCESS;
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		User u = getUser();
				
		Attachment attachment = new Attachment();
		try {
			LookupManager luMan = new LookupManager();
			AttachmentManager am = new AttachmentManager();
			
			if (!validateForm(am, luMan, p)) {
				this.setupPage();
				return StrutsConstants.EXCEPTION;
			}

			if (!Utils.isBlank(getId())) {
				attachment = am.getAttachment(Integer.parseInt(getId()), p.getId(), AttachmentManager.FILE_STUDY);
			}
			
			attachment.setType(luMan.getLookup(LookupType.ATTACHMENT_TYPE, AttachmentManager.FILE_STUDY));
			attachment.setAssociatedId(this.getStudyId());
			attachment.setName(this.getTitle());
			attachment.setAuthors(this.getAuthors());
			attachment.setDescription(this.getDescription());
			attachment.setPublicationType(new CtdbLookup(this.getPublicationType()));
			attachment.setUrl(this.getUrl());
			attachment.setPubMedId(this.getPubmedId());
			attachment.setUpdatedBy(u.getId());
			
			if (this.getFileUpload() != null) {
				attachment.setFileName(this.getFileUploadFileName());
				attachment.setAttachFile(this.getFileUpload());
			} 
			
			if (!Utils.isBlank(getId())) {
				am.updateAttachment(attachment, p.getId());
				addActionMessage(getText(StrutsConstants.SUCCESS_EDIT_KEY, new String[]{"\"" + attachment.getName() + "\" document"}));
			}
			else {
				attachment.setCreatedBy(u.getId());
				am.createAttachment(attachment, p.getId());
				addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, new String[]{"\"" + attachment.getName() + "\" document"}));
			}
			
			this.setupPage();
			session.put(DOCUMENTACTION_MESSAGES_KEY, this.getActionMessages());
			
		}
		catch (DuplicateObjectException doe) {
			log.error("Existing attachment record found in the database.", doe);
			addActionError(getText(StrutsConstants.ERROR_DUPLICATE, new String[]{"\"" + getTitle() + "\" document"}));
		}
		catch (DuplicateArchiveObjectException daoe) {
			log.error("An archived attachment record already exists for this version of the record.", daoe);
			addActionError(getText(StrutsConstants.ERROR_DUPLICATE_ARCHIVE, new String[]{"\"" + getTitle() + "\" document"}));
		}
		catch (CtdbException ce) {
			log.error("Could not update or add an attachment record in the database.", ce);
			addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, new String[]{"\"" + getTitle() + "\" document"}));
		}
		catch (ServerFileSystemException sfse) {
			log.error("Could not save the attachment data to the server's file system.", sfse);
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE, new String[]{"\"" + getTitle() + "\" document"}));
		} 
		
		if (this.hasActionErrors()) {
			strutsResult = StrutsConstants.EXCEPTION;
		}
		
		return strutsResult;
	}

	
	public String deleteDocument() {
		String strutsResult = BaseAction.SUCCESS;
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		List<Long> delIdList = Utils.convertStrToLongArray(idsToDelete);
		
		try {
			AttachmentManager am = new AttachmentManager();
			
			List<String> errorAttachmentList = new ArrayList<String>();
			List<Attachment> deletedAttachList = am.deleteAttachments(
					delIdList, p.getId(), AttachmentManager.FILE_STUDY, errorAttachmentList);
			
			if (!deletedAttachList.isEmpty()) {
				addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_MULTI_KEY, new String[]{
						"The " + getStringifiedNames(deletedAttachList) + " document(s)"}));
			}
			
			if (!errorAttachmentList.isEmpty()) {
				addActionError(getText(StrutsConstants.ERROR_DELETE, new String[]{Utils.convertListToString(errorAttachmentList) + " document(s)"}));
			}
			
		}
		catch (CtdbException ce) {
			log.error("Could not delete the document(s) from the database.", ce);
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE, new String[]{"document deletion changes"}));
		}
		catch (NumberFormatException nfe) {
			log.error("Invalid list of document IDs was given to this action class.", nfe);
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE, new String[]{"document deletion changes"}));
		} 
		
		try {
			this.setupPage();
		}
		catch (CtdbException ce) {
			log.error("Could not delete the document(s) from the database.", ce);
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE, new String[]{"document deletion changes"}));
			return StrutsConstants.FAILURE;
		}
		
		if (this.hasActionErrors()) {
            strutsResult = StrutsConstants.EXCEPTION;
		}
		
		return strutsResult;
	}


	/**
	 * Performs validation checks on the form data.
	 * 
	 * @param am - The attachment manger used for validation tests that require access to the database.
	 * @param luMan - The lookup manager to get the correct attachment type for certain validation tests.
	 * @param p - The current study object that is needed by some validation tests.
	 * @return True if and only if all the form data passes all of the validation checks.
	 * @throws CtdbException When there are any database errors.
	 * @throws NumberFormatException	When
	 */
	private boolean validateForm(AttachmentManager am, LookupManager luMan, Protocol p) throws CtdbException, NumberFormatException {
		this.clearErrorsAndMessages();

		// Check the "Title" field
		if ( !Utils.isBlank(getTitle()) ) {
			if ( getTitle().length() > 256 ) {
				addFieldError(getText("study.documents.title.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
						new String[]{getText("study.documents.title.display"), "256"}));
			}
			
			CtdbLookup attachType = luMan.getLookup(LookupType.ATTACHMENT_TYPE, AttachmentManager.FILE_STUDY);
			
			if ( !Utils.isBlank(getId()) ) {
				// Perform uniqueness validation on an existing document's title.
				Attachment dbDoc = am.getAttachment(Long.parseLong(getId()), p.getId(), attachType.getId());
				
				// If the document title changed, then check for uniqueness.
				if ( !dbDoc.getName().equals(getTitle()) && !am.isAttachmentNameUnique(getTitle(), attachType.getId(), p.getId()) ) {
					addFieldError(getText("study.documents.title.display"), getText(StrutsConstants.ERROR_DUPLICATE, 
							new String[]{getText("study.documents.title.display").toLowerCase(request.getLocale()) + " \"" + getTitle() + "\""}));
				}
			}
			else {
				// Check uniqueness for a brand new document's title
				if ( !am.isAttachmentNameUnique(getTitle(), attachType.getId(), p.getId()) ) {
					addFieldError(getText("study.documents.title.display"), getText(StrutsConstants.ERROR_DUPLICATE, 
							new String[]{getText("study.documents.title.display").toLowerCase(request.getLocale()) + " \"" + getTitle() + "\""}));
				}
			}
		}
		else {
			addFieldError(getText("study.documents.title.display"), getText(StrutsConstants.ERROR_FIELD_REQUIRED, 
					new String[]{getText("study.documents.title.display"), "is"}));
		}
		
		// Check the "Description" field
		if (!Utils.isBlank(getDescription())) {
			if (getDescription().length() > 4000) {
				addFieldError(getText("study.documents.description.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
						new String[]{getText("study.documents.description.display"), "4000"}));
			}
		} else {
			addFieldError(getText("study.documents.description.display"), getText(StrutsConstants.ERROR_FIELD_REQUIRED, 
					new String[]{getText("study.documents.description.display"), "is"}));
		}
		
		// Check the "Authors" field
		if (!Utils.isBlank(getAuthors()) && getAuthors().length() > 512) {
			addFieldError(getText("study.documents.authors.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.documents.authors.display"), "512"}));
		}
		
		// Check the "URL" field
		if (!Utils.isBlank(getUrl())) {
			if (getUrl().length() > 255) {
				addFieldError(getText("study.documents.url.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.documents.url.display"), "255"}));
			} else {
				try {
					URL url = new URL(getUrl());
					URLConnection conn = url.openConnection();
					conn.connect();
				} catch (MalformedURLException mue) {
					addFieldError(getText("study.documents.url.display"), getText(
							StrutsConstants.ERROR_URL_INVALID, new String[]{getUrl()}));
				} catch (IOException ie) {
					addFieldError(getText("study.documents.url.display"), getText(
							StrutsConstants.ERROR_URL_INVALID, new String[]{getUrl()}));
				}
			}
		}
		
		// Check the "PubMed ID" field
		if (!Utils.isBlank(getPubmedId()) && getPubmedId().length() > 255) {
			addFieldError(getText("study.documents.pubMedID.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.documents.pubMedID.display"), "255"}));
		}
		
		return !hasFieldErrors();
	}
	
	/**
	 * Helper method to extract the attachment names from the specified list, and creates a grammatically correct listing
	 * of those names.
	 * 
	 * @param attachmentList - The list of attachments used to extract the names from
	 * @return	A listing of attachment names in a grammatically correct (English) format.
	 */
	private String getStringifiedNames(List<Attachment> attachmentList) {
		List<String> nameList = new ArrayList<String>(attachmentList.size());
		
		for (Attachment a : attachmentList) {
			nameList.add(a.getName());
		}
		
		return Utils.convertListToString(nameList);
	}
	

	@Override  
	public void validate() {
		List<String> uploadErrors = (List<String>)getActionErrors();
		if (getFieldErrors().get("upload") != null) {
			uploadErrors.addAll((List<String>)getFieldErrors().get("upload"));
		}
	   
		for (String err : uploadErrors) {
			if (err.contains("allowed size limit") || 
				err.contains("Max size allowed is")) {
				clearErrorsAndMessages();
				addActionError(getText(StrutsConstants.ERROR_FILEUPLOAD_MAX_SIZE, Arrays.asList(MAX_FILE_SIZE)));
				break;
			}
		}
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public int getStudyId() {
		return studyId;
	}
	public void setStudyId(int studyId) {
		this.studyId = studyId;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthors() {
		return authors;
	}
	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public int getPublicationType() {
		return publicationType;
	}
	public void setPublicationType(int publicationType) {
		this.publicationType = publicationType;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getPubmedId() {
		return pubmedId;
	}
	public void setPubmedId(String pubmedId) {
		this.pubmedId = pubmedId;
	}

	public File getFileUpload() {
		return fileUpload;
	}
	public void setFileUpload(File fileUpload) {
		this.fileUpload = fileUpload;
	}

	public String getFileUploadFileName() {
		return fileUploadFileName;
	}
	public void setFileUploadFileName(String fileUploadFileName) {
		this.fileUploadFileName = fileUploadFileName;
	}

	public String getIdsToDelete() {
		return idsToDelete;
	}
	public void setIdsToDelete(String idsToDelete) {
		this.idsToDelete = idsToDelete;
	}

	/**
	 * @return the attachmentType
	 */
	public int getAttachmentType() {
		return attachmentType;
	}

	/**
	 * @param attachmentType the attachmentType to set
	 */
	public void setAttachmentType(int attachmentType) {
		this.attachmentType = attachmentType;
	}

}
