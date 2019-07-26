package gov.nih.tbi.repository.portal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.jcraft.jsch.JSchException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.exceptions.DocumentationUploadException;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.portal.DocumentationUploadAction;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.EventLog;
import gov.nih.tbi.repository.model.hibernate.EventLogDocumentation;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class EventLogDocumentationAction extends BaseRepositoryAction implements DocumentationUploadAction<EventLog> {

	private static final long serialVersionUID = -3574800318954410789L;

	static Logger logger = Logger.getLogger(DatasetAction.class);

	@Autowired
	RepositoryManager repositoryManager;

	private String addDocSelect;
	private String supportingDocDescription;
	// Hidden field that maps to the uploaded file name for validation purpose
	private String uploadFileName;
	private File uploadSupportDoc;
	private String uploadSupportDocFileName;
	private boolean isEditingDoc = false;
	private String selectedDocumentName;
	private Set<EventLogDocumentation> supportDocList = new HashSet<EventLogDocumentation>();

	public String getSelectedDocumentName() {
		return selectedDocumentName;
	}

	public void setSelectedDocumentName(String selectedDocumentName) {
		this.selectedDocumentName = selectedDocumentName;
	}

	public String getAddDocSelect() {
		return addDocSelect;
	}

	public void setAddDocSelect(String addDocSelect) {
		this.addDocSelect = addDocSelect;
	}

	public String addDocDialog() {
		return PortalConstants.ACTION_ADD_DOCUMENTATION_DIALOG;
	}

	@Override
	public String getValidationActionName() {
		return "eventLogDocValidationAction";
	}

	public String getSupportingDocDescription() {
		return supportingDocDescription;
	}

	public void setSupportingDocDescription(String supportingDocDescription) {
		this.supportingDocDescription = supportingDocDescription;
	}

	public String getUploadFileName() {
		return uploadFileName;
	}

	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}

	public File getUploadSupportDoc() {
		return uploadSupportDoc;
	}

	public void setUploadSupportDoc(File uploadSupportDoc) {
		this.uploadSupportDoc = uploadSupportDoc;
	}

	public String getUploadSupportDocFileName() {
		return uploadSupportDocFileName;
	}

	public void setUploadSupportDocFileName(String uploadSupportDocFileName) {
		this.uploadSupportDocFileName = uploadSupportDocFileName;
	}

	public boolean getIsEditingDoc() {
		return isEditingDoc;
	}

	public void setIsEditingDoc(boolean isEditingDoc) {
		this.isEditingDoc = isEditingDoc;
	}

	public Set<EventLogDocumentation> getSupportDocList() {

		Set<EventLogDocumentation> supportDocListSession = getSessionSupportDocList().getSupportingDocumentation();

		return supportDocListSession;

	}

	public void setSupportDocList(Set<EventLogDocumentation> supportDocList) {
		this.supportDocList = supportDocList;
	}

	public String uploadDocumentation()
			throws SocketException, IOException, JSchException, DocumentationUploadException {

		selectedDocumentName = getSelectedDocumentFromParam();

		List<Dataset> datasets = getSessionDatasetList().getDatasets();
		Set<EventLogDocumentation> currentDocSet = getSessionSupportDocList().getSupportingDocumentation();

		EventLogDocumentation sd = null;

		if (isEditingDoc && selectedDocumentName != null) {

			for (EventLogDocumentation doc : currentDocSet) {
				if (doc.getName().equals(selectedDocumentName)) {
					sd = doc;
					break;
				}
			}

			if (sd == null) { // throw exception if the selected documentation
								// is not found in session.
				throw new DocumentationUploadException("The selected supporting documentation name "
						+ selectedDocumentName + " does not match any of the documents in session");
			}
		} else {

			sd = new EventLogDocumentation();
			sd.setDateCreated(new Date());
		}

		sd.setDescription(this.getSupportingDocDescription());

		if (!isEditingDoc || (isEditingDoc && uploadSupportDocFileName != null
				&& !uploadSupportDocFileName.equals(sd.getName()))) {
			UserFile userFile = repositoryManager.uploadFile(getUser().getId(), uploadSupportDoc,
					uploadSupportDocFileName, null, ServiceConstants.FILE_TYPE_EVENTLOG_DOC, new Date());
			sd.setUserFile(userFile);
		}

		// get supporting documents in session
		if (currentDocSet != null) {
			supportDocList.addAll(currentDocSet);
		}

		// add to supportDocList if the supporting document is created for the
		// first time
		if (!isEditingDoc)
			supportDocList.add(sd);

		getSessionDatasetList().setDatasets(datasets);
		getSessionSupportDocList().setSupportingDocumentation(supportDocList);

		selectedDocumentName = null;
		return PortalConstants.ACTION_REDIRECT_TO_DOCUMENTATION_REFRESH;
	}

	public String documentationRefresh() {

		/*
		 * Set <SupportingDocumentation> currentDocSet=
		 * getSessionSupportDocList().getSupportingDocumentation();
		 */
		return PortalConstants.ACTION_DOCUMENTATION_REFRESH;
	}

	public String editDocumentation() throws UnsupportedEncodingException {

		String docName = getSelectedDocumentFromParam();

		if (!StringUtils.isBlank(docName)) {

			docName = docName.replace("\\", "");
	
			Set<EventLogDocumentation> currentDocSet = getSessionSupportDocList().getSupportingDocumentation();

			for (EventLogDocumentation sd : currentDocSet) {
				if (sd.getName().equals(docName)) {
					this.setIsEditingDoc(true);
					this.setSupportingDocDescription(sd.getDescription());

					this.setAddDocSelect("file");
					this.setUploadFileName(sd.getUserFile().getName());
					this.setUploadSupportDocFileName(sd.getUserFile().getName());
				}
			}

			selectedDocumentName = docName;
			return PortalConstants.ACTION_ADD_DOCUMENTATION_DIALOG;

		}
		return null;
	}

	public String removeDocumentations() throws UnsupportedEncodingException {

		String docNames = getSelectedDocumentFromParam();

		Set<EventLogDocumentation> currentDocSet = getSessionSupportDocList().getSupportingDocumentation();

		if (!StringUtils.isBlank(docNames)) {
			String[] docNameArr = docNames.split(",");

			for (int i = 0; i < docNameArr.length; i++) {
				String docName = docNameArr[i].replace("\\", "");  // Remove backslash that may be added in the request
																	// call

				for (Iterator<EventLogDocumentation> it = currentDocSet.iterator(); it.hasNext();) {
					EventLogDocumentation sd = it.next();

					if (sd.getName().equals(docName)) {
						// if it's a new supporting documentation, we also
						// delete the user file associated with it.
						if (sd.getId() == null && sd.getUserFile() != null) {

							repositoryManager.removeUserFile(sd.getUserFile());
						}

						it.remove();
						logger.debug("Removed SupportingDocumentation " + docName);
						break;
					}
				}
			}
		}

		getSessionSupportDocList().setSupportingDocumentation(currentDocSet);

		return PortalConstants.ACTION_DOCUMENTATION_REFRESH;
	}

	@Override
	public String getSelectedDocumentFromParam() throws UnsupportedEncodingException {

		String selectedDocumentName = URLDecoder.decode(getRequest().getParameter(PortalConstants.SUPPORTING_DOC_NAME),
				"UTF-8");
		selectedDocumentName = selectedDocumentName.replaceAll("\\\\", "");

		return selectedDocumentName;
	}

	public StreamResult editDataDataTableSave() throws MalformedURLException, UnsupportedEncodingException {
		return validationSuccess();
	}

	public StreamResult validationSuccess() {
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	@Override
	public EventLog getSessionObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getActionName() {
		return null;
	}
	@Override
	public List<FileType> getSupportingDocTypes() {
		return null;
	}

	@Override
	public int getDocumentationLimit() {
		return PortalConstants.MAXIMUM_DOCUMENT_UPLOAD;
	}

}
