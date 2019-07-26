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
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DictionarySupportingDocumentation;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.repository.model.SupportingDocumentationInterface;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.taglib.datatableDecorators.SupportDocIdtListDecorator;

/**
 * Handles the documentation stuff for data element lightbox stuff.
 * 
 * @author Andrew Johnson
 * 
 */
public class DataElementFileAction extends DataElementAction implements DocumentationUploadAction<DataElement> {
	private static final long serialVersionUID = -8482828206785636276L;

	private static final Logger logger = Logger.getLogger(DataElementFileAction.class);

	// Supporting Documentation
	private String addDocSelect;
	private boolean isEditingDoc = false;
	private FileType supportingDocType;
	private List<FileType> supportingDocTypes;
	private String supportingDocDescription;
	private String url;

	private final int DOCUMENTATION_LIMIT = 10;

	private String selectedDocumentName;

	private File uploadSupportDoc;
	private String uploadSupportDocContentType;

	private String uploadSupportDocFileName;
	// Hidden field that maps to the uploaded file name for validation purpose
	private String uploadFileName;

	public String getSelectedDocumentName() {
		return selectedDocumentName;
	}

	public void setSelectedDocumentName(String selectedDocumentName) {
		this.selectedDocumentName = selectedDocumentName;
	}

	public String addDocDialog() {
		return PortalConstants.ACTION_ADD_DOCUMENTATION_DIALOG;
	}

	/**
	 * @inheritDoc
	 */
	public String getValidationActionName() {
		return PortalConstants.DATA_ELEMENT_FILE_VALIDATION_ACTION;
	}

	/**
	 * @inheritDoc
	 */
	public String getActionName() {
		return ActionContext.getContext().getName();
	}

	public String getDataType() {
		return PortalConstants.DATAELEMENT;
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

	public DataElement getSessionObject() {
		return getSessionDataElement().getDataElement();
	}

	public String uploadDocumentation() throws SocketException, IOException, JSchException,
			DocumentationUploadException {

		selectedDocumentName = getSelectedDocumentFromParam();
		logger.debug(selectedDocumentName);
		DataElement currentDataElement = getSessionDataElement().getDataElement();
		DictionarySupportingDocumentation supportingDocumentation = null;

		if (isEditingDoc && selectedDocumentName != null) {
			Set<DictionarySupportingDocumentation> currentDocumentations = currentDataElement.getSupportingDocumentationSet();

			for (DictionarySupportingDocumentation currentDocumentation : currentDocumentations) {
				if (currentDocumentation.getName().equals(selectedDocumentName)) {
					supportingDocumentation = currentDocumentation;
					break;
				}
			}

			if (supportingDocumentation == null) {
				throw new DocumentationUploadException("The selected supporting documentation name "
						+ selectedDocumentName + " does not match any of the documents in session");
			}
		} else {
			supportingDocumentation = new DictionarySupportingDocumentation();
			supportingDocumentation.setDateCreated(new Date());
			currentDataElement.addSupportingDocumentation(supportingDocumentation);
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

		getSessionDataElement().setDataElement(currentDataElement);

		// Clear uploaded file.
		getSessionUploadFile().clear();
		uploadSupportDoc = null;
		selectedDocumentName = null;

		return PortalConstants.ACTION_REDIRECT_TO_DOCUMENTATION_REFRESH;
	}
	
	// url: http://fitbir-portal-local.cit.nih.gov:8081/portal/dictionary/dataElementFileAction!getUploadDocumentation.action
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

		setSelectedDocumentName(documentName);
		if (!StringUtils.isBlank(documentName)) {
			documentName = documentName.replace("\\", "");
			Set<DictionarySupportingDocumentation> currentDocSet =
					getSessionDataElement().getDataElement().getSupportingDocumentationSet();

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
				getSessionDataElement().getDataElement().getSupportingDocumentationSet();

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

		return PortalConstants.ACTION_EDIT_DOCUMENTATION;
	}

	@Override
	public void validate(){
		if (hasFieldErrors()) {
			logger.debug("DataElementFileAction validate() has field errors: "+getFieldErrors().size());
			getSessionUploadFile().clear();
			if (uploadSupportDoc != null) {
				try {
					getSessionUploadFile().setUploadFile(BRICSFilesUtils.copyFile(uploadSupportDoc));
					getSessionUploadFile().setUploadFileFileName(uploadSupportDocFileName);
					getSessionUploadFile().setUploadFileContentType(uploadSupportDocContentType);
				} catch (IOException e) {
					logger.error("Failed to read upload file to byte array in validation.", e);
				}
			}
        } 
	}
	
	public List<DictionarySupportingDocumentation> getSupportDocList() {

		List<DictionarySupportingDocumentation> supportDocList = new ArrayList<DictionarySupportingDocumentation>();
		supportDocList.addAll(getSessionDataElement().getDataElement().getSupportingDocumentationSet());

		return supportDocList;
	}

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
}
