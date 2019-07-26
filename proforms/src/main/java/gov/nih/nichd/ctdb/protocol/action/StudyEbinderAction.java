package gov.nih.nichd.ctdb.protocol.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.attachments.manager.AttachmentManager;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.BinderNotFoundException;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateArchiveObjectException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ServerFileSystemException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.ebinder.domain.Ebinder;
import gov.nih.nichd.ctdb.ebinder.manager.BinderManager;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;

public class StudyEbinderAction extends BaseAction {

	private static final long serialVersionUID = 8773220232317252866L;

	// Binder fields
	private String id = null;
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
	private File attachFile = null;
	private String attachFileFileName = "";     // Struts2 conventional file name for s:file mapped field.

	
	public String execute() throws Exception {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_EBINDER);
		
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		Ebinder binder = null;
		LookupManager luMan = new LookupManager();
		
		try {
			BinderManager bMan = new BinderManager();
			binder = bMan.getEbinder(p.getId(), BinderManager.TYPE_STUDY);
		} catch (CtdbException ce) {
			ce.printStackTrace();
			addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, Arrays.asList("the E-Binder")));

			binder = new Ebinder();  // Create an invalid binder
			this.setBinderValid(false);
			
		} catch (BinderNotFoundException bnfe) {
			binder = new Ebinder();  // Create the default binder
			binder.setStudyId(p.getId());
			binder.setType(luMan.getLookup(LookupType.EBINDER_TYPE, BinderManager.TYPE_STUDY));
			binder.setJsonTree(BinderManager.DEFAULT_TREE);
		}
				
		this.setId(binder.getId() + "");
		this.setStudyID(binder.getStudyId());
		this.seteBinderTree(binder.getJsonTree());
		this.setType(binder.getType().getId());
		
		return SUCCESS;
	}
	
	
	public String saveEbinder() throws Exception {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_EBINDER);
		
		this.clearErrorsAndMessages();
		
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		Ebinder binder = null;
		try {
			BinderManager bMan = new BinderManager();
			LookupManager luMan = new LookupManager();
			
			if (!Utils.isBlank(getId()) && Integer.parseInt(getId()) > 0) {
				binder = bMan.getEbinder(p.getId(), BinderManager.TYPE_STUDY);
				binder.setId(Integer.parseInt(getId()));
			} else {
				binder = new Ebinder();
			}
			
			binder.setStudyId(getStudyID());
			binder.setJsonTree(geteBinderTree());
			binder.setType(luMan.getLookup(LookupType.EBINDER_TYPE, getType()));
			
			bMan.saveEbinder(binder);
			this.setId(binder.getId() + "");
			this.setStudyID(binder.getStudyId());
			this.seteBinderTree(binder.getJsonTree());
			this.setType(binder.getType().getId());
			
			sendBinderInfoAsJSON(ServletActionContext.getResponse());
			
		} catch (CtdbException ce) {
			ce.printStackTrace();
			addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE_RETRY, Arrays.asList("E-Binder")));
		} catch (BinderNotFoundException bnfe) {
			bnfe.printStackTrace();
			addActionError(getText(StrutsConstants.ERROR_NOTFOUND, Arrays.asList("E-Binder")));
		} catch (JSONException je) {
			je.printStackTrace();
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE, Arrays.asList("E-Binder")));
		} catch (IOException ioe) {
			ioe.printStackTrace();
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE, Arrays.asList("E-Binder")));
		}
		
		if (this.hasActionErrors()) {
            return StrutsConstants.FAILURE;
		} else {
			return null;
		}
	}

	
	public String uploadFile() {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_EBINDER);
		
		if (!validateForm()) {
			return StrutsConstants.EXCEPTION;
		}
		
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		User user = getUser();
		
		Attachment file = null;
		try {
			AttachmentManager aMan = new AttachmentManager();
			LookupManager luMan = new LookupManager();
			
			if (this.getAttachId() > 0) {
				file = aMan.getAttachment(getAttachId(), p.getId(), 
					AttachmentManager.FILE_STUDY_EBINDER);
			} else {
				file = new Attachment();
				file.setCreatedBy(user.getId());
			}
			
			file.setId(getAttachId());
			file.setName(getAttachName());
			file.setDescription(getAttachDescription());
			file.setAuthors(getAttachAuthor());
			file.setPublicationType(luMan.getLookup(LookupType.PUBLICATION_TYPE, getAttachPubType()));
			file.setUrl(getAttachUrl());
			file.setPubMedId(getAttachPubMedId());
			file.setFileName(getAttachFileFileName());
			file.setAssociatedId(p.getId());
			file.setType(luMan.getLookup(LookupType.ATTACHMENT_TYPE, AttachmentManager.FILE_STUDY_EBINDER));
			file.setUpdatedBy(user.getId());
			
			// Check if the user uploaded a file
			if (getAttachFile() != null && getAttachFile().length() > 0) {
				file.setFileName(getAttachFileFileName());
				file.setAttachFile(getAttachFile());
			}
			
			if (this.getAttachId() > 0) {
				aMan.updateAttachment(file);
			} else {
				aMan.createAttachment(file);
			}
			
			this.setAttachId(file.getId());
			this.setAttachName(file.getName());
			this.setAttachDescription(file.getDescription());
			this.setAttachAuthor(file.getAuthors());
			this.setAttachPubType(file.getPublicationType().getId());
			this.setAttachUrl(file.getUrl());
			this.setAttachPubMedId(file.getPubMedId());
			this.setAttachFileFileName(file.getFileName());
			
		} catch (DuplicateObjectException doe) {
			doe.printStackTrace();
			addActionError(getText(StrutsConstants.ERROR_DUPLICATE, new String[]{"\"" + getAttachName() + "\" document"}));
		} catch (DuplicateArchiveObjectException daoe) {
			daoe.printStackTrace();
			addActionError(getText(StrutsConstants.ERROR_DUPLICATE_ARCHIVE, new String[]{"\"" + getAttachName() + "\" document"}));
		} catch (CtdbException ce) {
			ce.printStackTrace();
			addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, new String[]{"\"" + getAttachName() + "\" document"}));
		} catch (ServerFileSystemException sfse) {
			sfse.printStackTrace();
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE, new String[]{"\"" + getAttachName() + "\" document"}));
		} 

		if (this.hasActionErrors()) {
            return StrutsConstants.FAILURE;
		} else {
			return SUCCESS;
		}
	}


	public String deleteFile() throws IOException {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_EBINDER);
		
		HttpServletResponse response = ServletActionContext.getResponse();
		String message = "";
		
		if (!getDeleteType().equalsIgnoreCase("file")) {
			message = getText(StrutsConstants.ERROR_DELETE, new String[]{"\"" + getAttachName() + "\" document"});
			response.getWriter().write(message);
			response.flushBuffer();
			return null;
		}
		
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		try {
			AttachmentManager aMan = new AttachmentManager();
			aMan.deleteAttachment(getAttachId(), p.getId(), AttachmentManager.FILE_STUDY_EBINDER);
			message = getText(StrutsConstants.SUCCESS_DELETE_KEY, new String[]{"\"" + getAttachName() + "\" document"});
			
		} catch (CtdbException ce) {
			ce.printStackTrace();
			message = getText(StrutsConstants.ERROR_DELETE, new String[]{"\"" + getAttachName() + "\" document"});
		} catch (ServerFileSystemException sfse) {
			sfse.printStackTrace();
			message = getText(StrutsConstants.ERROR_DELETE, new String[]{"\"" + getAttachName() + "\" document"});
		} 

		if (!Utils.isBlank(message)) {
			response.getWriter().write(message);
			response.flushBuffer();
		}
		return null;
	}

	/**
	 * Sends out the binder form data to the response as a JSON string.
	 * 
	 * @param response - The HTML response object
	 * @throws IOException	When JSON data could not be sent through the response object.
	 * @throws JSONException	When the JSON object cannot be created.
	 */
	private void sendBinderInfoAsJSON(HttpServletResponse response) throws IOException, JSONException
	{
		JSONObject requestJson = new JSONObject();
		String jsonStr = "";
		PrintWriter pw = null;
		
		// Convert the form to JSON
		requestJson.put("id", getId());
		requestJson.put("studyID", getStudyID());
		requestJson.put("type", getType());
		requestJson.put("eBinderTree", geteBinderTree());
		jsonStr = requestJson.toString() != null ? requestJson.toString() : ""; 
		
		// Send out JSON in the response
		try {
			pw = response.getWriter();
			pw.append(jsonStr);
			pw.flush();
		} finally {
			pw.close();
		}
	}
	
	
	/**
	 * Performs validation checks on the file form data.
	 */
	private boolean validateForm() {
		this.clearErrorsAndMessages();
		
		// Check the "Title" field
		if (!Utils.isBlank(getAttachName())) {
			if (getAttachName().length() > 256) {
				addFieldError(getText("study.documents.title.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
						new String[]{getText("study.documents.title.display"), "256"}));
			}
		} else {
			addFieldError(getText("study.documents.title.display"), getText(StrutsConstants.ERROR_FIELD_REQUIRED, 
					new String[]{getText("study.documents.title.display"), "is"}));
		}
		
		// Check the file field
		if (Utils.isBlank(getAttachFileFileName()))  {
			if (getAttachFile() == null || getAttachFile().length() == 0) {
				addFieldError(getText("study.documents.upload.display"), getText(StrutsConstants.ERROR_FIELD_REQUIRED, 
						new String[]{getText("study.documents.upload.display"), "is"}));
			}
		}
		
		// Check the "Description" field
		if (!Utils.isBlank(getAttachDescription()) && getAttachDescription().length() > 4000) {
			addFieldError(getText("study.documents.description.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.documents.description.display"), "4000"}));
		}
		
		// Check the "Authors" field
		if (!Utils.isBlank(getAttachAuthor()) && getAttachAuthor().length() > 512) {
			addFieldError(getText("study.documents.authors.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.documents.authors.display"), "512"}));
		}
		
		// Check the "URL" field
		if (!Utils.isBlank(getAttachUrl())) {
			if (getAttachUrl().length() > 255) {
				addFieldError(getText("study.documents.url.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.documents.url.display"), "255"}));
			} else if (getAttachUrl().length() > 0) {
				try {
					URL url = new URL(getAttachUrl());
					URLConnection conn = url.openConnection();
					conn.connect();
				} catch (MalformedURLException mue) {
					addFieldError(getText("study.documents.url.display"), getText(
							StrutsConstants.ERROR_URL_INVALID, new String[]{getAttachUrl()}));
				} catch (IOException ie) {
					addFieldError(getText("study.documents.url.display"), getText(
							StrutsConstants.ERROR_URL_INVALID, new String[]{getAttachUrl()}));
				}
			}
		}
		
		// Check the "PubMed ID" field
		if (!Utils.isBlank(getAttachPubMedId()) && getAttachPubMedId().length() > 255) {
			addFieldError(getText("study.documents.pubMedID.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.documents.pubMedID.display"), "255"}));
		}

		return !hasFieldErrors();
	}

	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String geteBinderTree() {
		return eBinderTree;
	}
	public void seteBinderTree(String eBinderTree) {
		this.eBinderTree = eBinderTree;
	}

	public long getStudyID() {
		return studyID;
	}
	public void setStudyID(long studyID) {
		this.studyID = studyID;
	}

	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	public boolean isBinderValid() {
		return binderValid;
	}
	public void setBinderValid(boolean binderValid) {
		this.binderValid = binderValid;
	}

	public String getDeleteType() {
		return deleteType;
	}
	public void setDeleteType(String deleteType) {
		this.deleteType = deleteType;
	}

	public int getAttachId() {
		return attachId;
	}
	public void setAttachId(int attachId) {
		this.attachId = attachId;
	}

	public String getAttachName() {
		return attachName;
	}
	public void setAttachName(String attachName) {
		this.attachName = attachName;
	}

	public String getAttachDescription() {
		return attachDescription;
	}
	public void setAttachDescription(String attachDescription) {
		this.attachDescription = attachDescription;
	}

	public String getAttachAuthor() {
		return attachAuthor;
	}
	public void setAttachAuthor(String attachAuthor) {
		this.attachAuthor = attachAuthor;
	}

	public int getAttachPubType() {
		return attachPubType;
	}
	public void setAttachPubType(int attachPubType) {
		this.attachPubType = attachPubType;
	}

	public String getAttachUrl() {
		return attachUrl;
	}
	public void setAttachUrl(String attachUrl) {
		this.attachUrl = attachUrl;
	}

	public String getAttachPubMedId() {
		return attachPubMedId;
	}
	public void setAttachPubMedId(String attachPubMedId) {
		this.attachPubMedId = attachPubMedId;
	}

	public File getAttachFile() {
		return attachFile;
	}
	public void setAttachFile(File attachFile) {
		this.attachFile = attachFile;
	}

	public String getAttachFileFileName() {
		return attachFileFileName;
	}
	public void setAttachFileFileName(String attachFileFileName) {
		this.attachFileFileName = attachFileFileName;
	}
}
