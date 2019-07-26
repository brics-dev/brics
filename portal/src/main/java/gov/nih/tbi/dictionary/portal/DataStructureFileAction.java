package gov.nih.tbi.dictionary.portal;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jcraft.jsch.JSchException;
import com.opensymphony.xwork2.ActionContext;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.exceptions.DocumentationUploadException;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.portal.DocumentationUploadAction;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.util.BRICSFilesUtils;
import gov.nih.tbi.dictionary.model.hibernate.DictionarySupportingDocumentation;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.repository.model.SupportingDocumentationInterface;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.taglib.datatableDecorators.SupportDocIdtListDecorator;

/**
 * Handles the documentation stuff for the data structure lightbox.
 * 
 * @author Andrew Johnson
 * 
 */
public class DataStructureFileAction extends DataStructureAction implements DocumentationUploadAction<FormStructure> {
	private static final long serialVersionUID = 7882044470114875735L;
	private static final Logger logger = Logger.getLogger(DataStructureFileAction.class);
	private final int DOCUMENTATION_LIMIT = 10;

	// Supporting Documentation
	private String addDocSelect;
	private boolean isEditingDoc = false;
	private FileType supportingDocType;
	private String supportingDocDescription;
	private String url;

	private String selectedDocumentName;

	private File uploadSupportDoc;
	private String uploadSupportDocContentType;

	private String uploadSupportDocFileName;
	// Hidden field that maps to the uploaded file name for validation purpose
	private String uploadFileName;
	private List<FileType> supportingDocTypes;

	public String getSelectedDocumentName() {
		return selectedDocumentName;
	}

	public void setSelectedDocumentName(String selectedDocumentName) {
		this.selectedDocumentName = selectedDocumentName;
	}

	public String addDocDialog() {
		return PortalConstants.ACTION_ADD_DOCUMENTATION_DIALOG;
	}

	public String getValidationActionName() {
		return PortalConstants.FORM_STRUCTURE_FILE_VALIDATON_ACTION;
	}

	public String getActionName() {
		return ActionContext.getContext().getName();
	}

	public String uploadDocumentation() throws SocketException, IOException, JSchException,
			DocumentationUploadException {
		selectedDocumentName = getSelectedDocumentFromParam();
		FormStructure currentDataStructure = getSessionDataStructure().getDataStructure();
		DictionarySupportingDocumentation supportingDocumentation = null;

		if (isEditingDoc && selectedDocumentName != null) {
			Set<DictionarySupportingDocumentation> currentDocumentations = currentDataStructure.getSupportingDocumentationSet();

			for (DictionarySupportingDocumentation currentDocumentation : currentDocumentations) {
				if (currentDocumentation.getName().equals(selectedDocumentName)) {
					supportingDocumentation = currentDocumentation;
					break;
				}
			}

			if (supportingDocumentation == null) {
				throw new DocumentationUploadException(
						"The selected supporting documentation name does not match any of the documents in session: "
								+ selectedDocumentName);
			}
		} else {
			supportingDocumentation = new DictionarySupportingDocumentation();
			supportingDocumentation.setDateCreated(new Date());
			currentDataStructure.addSupportingDocumentation(supportingDocumentation);
		}

		supportingDocumentation.setDescription(this.getSupportingDocDescription());
		supportingDocumentation.setFileType(this.getSupportingDocType());

		if(getSessionUploadFile().getUploadFile() != null && uploadSupportDoc == null){
			uploadSupportDoc = getSessionUploadFile().getUploadFile();
			uploadSupportDocFileName = getSessionUploadFile().getUploadFileFileName();
			uploadSupportDocContentType = getSessionUploadFile().getUploadFileContentType();
		}
		
		if (StringUtils.isBlank(getUrl())) {
			// Create new user file only if it's new doc or edited doc's file name changed
			if (!isEditingDoc
					|| (isEditingDoc && uploadSupportDocFileName != null && !uploadSupportDocFileName
							.equals(supportingDocumentation.getName()))) {

				UserFile userFile =
						repositoryManager.uploadFileDDT(getUser().getId(), uploadSupportDoc, uploadSupportDocFileName,
								null, ServiceConstants.FILE_TYPE_META_STUDY_DOC, new Date());
				supportingDocumentation.setUserFile(userFile);
			}

		} else {
			supportingDocumentation.setUrl(this.getUrl());
		}

		getSessionDataStructure().setDataStructure(currentDataStructure);

		// Clear uploaded file.
		getSessionUploadFile().clear();
		uploadSupportDoc = null;
		selectedDocumentName = null;

		return PortalConstants.ACTION_REDIRECT_TO_DOCUMENTATION_REFRESH;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8081/portal/dictionary/dataStructureFileAction!getUploadDocumentation.action
	public String getUploadDocumentation() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<SupportingDocumentationInterface> sdList =
					new ArrayList<SupportingDocumentationInterface>(getSessionObject().getSupportingDocumentationSet());
			idt.setList(sdList);
			idt.decorate(new SupportDocIdtListDecorator());
			idt.setTotalRecordCount(sdList.size());
			idt.setFilteredRecordCount(sdList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}


	public String editDocumentation() throws UnsupportedEncodingException {

		String documentName = getSelectedDocumentFromParam();

		if (!StringUtils.isBlank(documentName)) {
			documentName = documentName.replace("\\", "");
			Set<DictionarySupportingDocumentation> currentDocSet =
					getSessionDataStructure().getDataStructure().getSupportingDocumentationSet();

			for (DictionarySupportingDocumentation currentDocument : currentDocSet) {
				if (currentDocument.getName().equals(documentName)) {
					this.setIsEditingDoc(true);
					this.setSupportingDocType(currentDocument.getFileType().getId());
					this.setSupportingDocDescription(currentDocument.getDescription());

					if (!StringUtils.isBlank(currentDocument.getUrl())) {
						this.setAddDocSelect("url");
						this.setUrl(currentDocument.getUrl());
					} else {
						this.setAddDocSelect("file");
						this.setUploadFileName(currentDocument.getUserFile().getName());
						this.setUploadSupportDocFileName(currentDocument.getUserFile().getName());
					}

					selectedDocumentName = documentName;
					return PortalConstants.ACTION_ADD_DOCUMENTATION_DIALOG;
				}
			}
		}

		return null;
	}

	public String removeDocumentations() throws UnsupportedEncodingException {

		String docNames = getSelectedDocumentFromParam();

		Set<DictionarySupportingDocumentation> currentDocSet =
				getSessionDataStructure().getDataStructure().getSupportingDocumentationSet();

		if (!StringUtils.isBlank(docNames)) {
			String[] docNameArr = docNames.split(",");

			for (int i = 0; i < docNameArr.length; i++) {
				String docName = docNameArr[i].replace("\\", "");  // Remove backslash that may be added in the request
																	// call

				for (Iterator<DictionarySupportingDocumentation> it = currentDocSet.iterator(); it.hasNext();) {
					DictionarySupportingDocumentation supportingDocumentation = it.next();

					if (supportingDocumentation.getName().equals(docName)) {
						// if it's a new supporting documentation, we also delete the user file associated with it.
						if (supportingDocumentation.getId() == null && supportingDocumentation.getUserFile() != null) {
							repositoryManager.removeUserFile(supportingDocumentation.getUserFile());
						}

						it.remove();
						logger.debug("Removed SupportingDocumentation " + docName);
						break;
					}
				}
			}
		}

		return PortalConstants.ACTION_DOCUMENTATION;
	}

	@Override
	public void validate(){
		if (hasFieldErrors()) {
			logger.debug("DataStructureFileAction validate() has field errors: "+getFieldErrors().size());
			getSessionUploadFile().clear();
			if (uploadSupportDoc != null) {
				try {
					getSessionUploadFile().setUploadFile(BRICSFilesUtils.copyFile(uploadSupportDoc));
				} catch (IOException e) {
					logger.error("Failed to read upload file to byte array in validation.");
					e.printStackTrace();
				}

				getSessionUploadFile().setUploadFileFileName(uploadSupportDocFileName);
				getSessionUploadFile().setUploadFileContentType(uploadSupportDocContentType);
			}
        } 
	}
	public List<DictionarySupportingDocumentation> getSupportDocList() {

		List<DictionarySupportingDocumentation> supportDocList = new ArrayList<DictionarySupportingDocumentation>();
		supportDocList.addAll(getSessionDataStructure().getDataStructure().getSupportingDocumentationSet());

		return supportDocList;
	}

	/**
	 * Return an empty list for the file type. This should hide the file type drop. Anytime we actually need to add file
	 * types, just have this method return a list.
	 */
	public List<FileType> getSupportingDocTypes() {
		if (this.supportingDocTypes == null) {
			this.supportingDocTypes = new ArrayList<FileType> ();
			this.supportingDocTypes.add(staticManager.getAdminFileTypeByName(ServiceConstants.FILE_TYPE_DICTIONARY));
		}
		
		return this.supportingDocTypes;
	}

	public String getAddDocSelect() {
		return addDocSelect;
	}

	public void setAddDocSelect(String addDocSelect) {
		this.addDocSelect = addDocSelect;
	}

	public boolean getIsEditingDoc() {
		return isEditingDoc;
	}

	public void setIsEditingDoc(boolean isEditingDoc) {
		this.isEditingDoc = isEditingDoc;
	}

	public FileType getSupportingDocType() {
		return supportingDocType;
	}

	public void setSupportingDocType(Long supportingDocType) {
		if (supportingDocType != null) {
			for (FileType type : this.getSupportingDocTypes()) {
				if (type.getId().equals(supportingDocType)) {
					this.supportingDocType = type;
					break;
				}
			}
		}
	}

	public String getSupportingDocDescription() {
		return supportingDocDescription;
	}

	public void setSupportingDocDescription(String supportingDocDescription) {
		this.supportingDocDescription = supportingDocDescription;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public File getUploadSupportDoc() {
		return uploadSupportDoc;
	}

	public void setUploadSupportDoc(File uploadSupportDoc) {
		this.uploadSupportDoc = uploadSupportDoc;
	}

	public String getUploadSupportDocContentType() {
		return uploadSupportDocContentType;
	}

	public void setUploadSupportDocContentType(String uploadSupportDocContentType) {
		this.uploadSupportDocContentType = uploadSupportDocContentType;
	}

	public String getUploadSupportDocFileName() {
		return uploadSupportDocFileName;
	}

	public void setUploadSupportDocFileName(String uploadSupportDocFileName) {
		this.uploadSupportDocFileName = uploadSupportDocFileName;
	}

	public String getUploadFileName() {
		return uploadFileName;
	}

	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}

	public String documentationRefresh() {
		return PortalConstants.ACTION_EDIT_DOCUMENTATION;
	}

	/**
	 * @inheritDoc
	 */
	public FormStructure getSessionObject() {
		return getSessionDataStructure().getDataStructure();
	}

	/**
	 * @inheritDoc
	 */
	public int getDocumentationLimit() {
		return DOCUMENTATION_LIMIT;
	}

	/**
	 * @inheritDoc
	 */
	public String getSelectedDocumentFromParam() throws UnsupportedEncodingException {
		String selectedDocumentName =
				URLDecoder.decode(getRequest().getParameter(PortalConstants.SUPPORTING_DOC_NAME), "UTF-8");
		selectedDocumentName = selectedDocumentName.replaceAll("\\\\", "");

		return selectedDocumentName;
	}
}
