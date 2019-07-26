package gov.nih.tbi.dictionary.portal;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLDecoder;
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
import gov.nih.tbi.dictionary.model.hibernate.DictionarySupportingDocumentation;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class FSEventLogDocumentationAction extends DataStructureAction
		implements DocumentationUploadAction<FormStructure> {

	private static final long serialVersionUID = 1982661056515618495L;
	
	private static final Logger logger = Logger.getLogger(FSEventLogDocumentationAction.class);

	// Supporting Documentation
	private String addDocSelect;
	private boolean isEditingDoc = false;
	private String supportingDocDescription;

	private String selectedDocumentName;

	private File uploadSupportDoc;
	private String uploadSupportDocContentType;

	private String uploadSupportDocFileName;
	// Hidden field that maps to the uploaded file name for validation purpose
	private String uploadFileName;
	private List<FileType> supportingDocTypes;

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

	public String getSupportingDocDescription() {
		return supportingDocDescription;
	}

	public void setSupportingDocDescription(String supportingDocDescription) {
		this.supportingDocDescription = supportingDocDescription;
	}

	public String getSelectedDocumentName() {
		return selectedDocumentName;
	}

	public void setSelectedDocumentName(String selectedDocumentName) {
		this.selectedDocumentName = selectedDocumentName;
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

	public void setSupportingDocTypes(List<FileType> supportingDocTypes) {
		this.supportingDocTypes = supportingDocTypes;
	}

	public String addDocDialog() {
		return PortalConstants.ACTION_ADD_DOCUMENTATION_DIALOG;
	}

	@Override
	public String getValidationActionName() {
		return PortalConstants.ACTION_FS_EVENT_LOG_VALIDATION;
	}
	
	public String getActionName() {
	    return ActionContext.getContext().getName();
	}

	public String uploadDocumentation()
			throws SocketException, IOException, JSchException, DocumentationUploadException {
		selectedDocumentName = getSelectedDocumentFromParam();
		
		boolean inDataElementPage = getSessionDictionaryStatusChange().getInDataElementPage();
	
		DictionarySupportingDocumentation supportingDocumentation = null;

		if (isEditingDoc && selectedDocumentName != null) {
			
			Set<DictionarySupportingDocumentation> currentDocumentations = null;
			if(inDataElementPage){
				currentDocumentations= getSessionDictionaryStatusChange().getDEEventLogDocumentation();
			}
			else{
				currentDocumentations= getSessionDictionaryStatusChange().getfSEventLogDocumentation();
			}
			

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
		}

		supportingDocumentation.setDescription(this.getSupportingDocDescription());

	
			if (!isEditingDoc || (isEditingDoc && uploadSupportDocFileName != null
					&& !uploadSupportDocFileName.equals(supportingDocumentation.getName()))) {

				UserFile userFile = repositoryManager.uploadFileDDT(getUser().getId(), uploadSupportDoc,
						uploadSupportDocFileName, null, ServiceConstants.FILE_TYPE_EVENTLOG_DOC, new Date());
				supportingDocumentation.setUserFile(userFile);
			}


			// add to supportDocList if the supporting document is created for the
			// first time
			if (!isEditingDoc){
				if(inDataElementPage){
					getSessionDictionaryStatusChange().getDEEventLogDocumentation().add(supportingDocumentation);
				}
				else{
					getSessionDictionaryStatusChange().getfSEventLogDocumentation().add(supportingDocumentation);
				}
			}
				
			
		selectedDocumentName = null;

		return PortalConstants.ACTION_REDIRECT_TO_DOCUMENTATION_REFRESH;
	}
	
	public String editDocumentation() throws UnsupportedEncodingException {

		String documentName = getSelectedDocumentFromParam();
		
		boolean inDataElementPage = getSessionDictionaryStatusChange().getInDataElementPage();

		if (!StringUtils.isBlank(documentName)) {
			documentName = documentName.replace("\\", "");
			
			Set<DictionarySupportingDocumentation> currentDocSet = null;
			
			if(inDataElementPage){
				currentDocSet= getSessionDictionaryStatusChange().getDEEventLogDocumentation();
			}
			else{
				currentDocSet= getSessionDictionaryStatusChange().getfSEventLogDocumentation();
			}

			for (DictionarySupportingDocumentation currentDocument : currentDocSet) {
				if (currentDocument.getName().equals(documentName)) {
					this.setIsEditingDoc(true);
					this.setSupportingDocDescription(currentDocument.getDescription());
					
					this.setAddDocSelect("file");
					this.setUploadFileName(currentDocument.getUserFile().getName());
					this.setUploadSupportDocFileName(currentDocument.getUserFile().getName());
			

					selectedDocumentName = documentName;
					return PortalConstants.ACTION_ADD_DOCUMENTATION_DIALOG;
				}
			}
		}

		return null;
	}
	
	public String removeDocumentations() throws UnsupportedEncodingException {

		String docNames = getSelectedDocumentFromParam();
		
		boolean inDataElementPage = getSessionDictionaryStatusChange().getInDataElementPage();
		
		Set<DictionarySupportingDocumentation> currentDocSet =null;
		
		if(inDataElementPage){
			currentDocSet= getSessionDictionaryStatusChange().getDEEventLogDocumentation();
		}
		else{
			currentDocSet= getSessionDictionaryStatusChange().getfSEventLogDocumentation();
		}

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

		return PortalConstants.ACTION_REDIRECT_TO_DOCUMENTATION_REFRESH;
	}
	
	public String documentationRefresh() {

		getEventLogSupportDocList();
		
		return PortalConstants.ACTION_DOCUMENTATION_REFRESH;
	}

	public int getDocumentationLimit() {
		 return PortalConstants.MAXIMUM_DOCUMENT_UPLOAD;
	}

	@Override
	public String getSelectedDocumentFromParam() throws UnsupportedEncodingException {
		String selectedDocumentName =
				URLDecoder.decode(getRequest().getParameter(PortalConstants.SUPPORTING_DOC_NAME), "UTF-8");
		selectedDocumentName = selectedDocumentName.replaceAll("\\[|\\]|\"", "");

		return selectedDocumentName;
	}

	@Override
	public List<FileType> getSupportingDocTypes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public FormStructure getSessionObject() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
