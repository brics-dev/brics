package gov.nih.tbi.repository.portal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jcraft.jsch.JSchException;
import com.opensymphony.xwork2.ActionContext;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.exceptions.DocumentationUploadException;
import gov.nih.tbi.commons.model.Publication;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.util.BRICSFilesUtils;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.repository.model.SupportingDocumentationInterface;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudySupportingDocumentation;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.taglib.datatableDecorators.SupportDocIdtListDecorator;

public class StudyDocumentationAction extends BaseRepositoryAction {

	private static final long serialVersionUID = -8074828278796736278L;
	private static Logger logger = Logger.getLogger(StudyDocumentationAction.class);

	private boolean isEditingDoc = false;
	private String addDocSelect;   // Add Url or File
	private FileType supportingDocType;
	private String supportingDocDescription;
	private String supportingDocTitle;
	private String url;
	private Long id;

	private Publication pubEntry;
	private String pubMedJson;

	// Doc File Upload
	private File uploadSupportDoc;
	private String uploadSupportDocContentType;
	private String uploadSupportDocFileName;

	// Hidden field that maps to the uploaded support doc file name for validation purpose
	private String uploadFileName;
	private String selectedDocumentName;

	private String softwareName;
	private String version;

	// Admin File Upload
	private File adminUpload;
	private String adminUploadContentType;
	private String adminUploadFileName;
	
	@Override
	public void validate(){
		/*Retain the upload file when validation fails*/
		if (hasFieldErrors()) {
			logger.debug("validate() has field errors: "+getFieldErrors().size());
			getSessionUploadFile().clear();
			if (uploadSupportDoc != null) {
				try {
					getSessionUploadFile().setUploadFile(BRICSFilesUtils.copyFile(uploadSupportDoc));
				} catch (IOException e) {
					logger.error("Failed to read upload file to byte array in validation.", e);
				}
			}
        } 
	}
	/**
	 * Uploads the supporting documentation, creates a UserFile if applicable.
	 * 
	 * @return
	 * @throws JSchException
	 * @throws IOException
	 * @throws SocketException
	 * @throws DocumentationUploadException
	 */
	public StreamResult uploadDocumentation() throws SocketException, IOException, JSchException,
			DocumentationUploadException {

		selectedDocumentName = getSelectedDocumentFromParam();
		Study currentStudy = getSessionStudy().getStudy();
		StudySupportingDocumentation sd = null;
	
		if (isEditingDoc && selectedDocumentName != null) {
			Set<StudySupportingDocumentation> currentDocSet = currentStudy.getSupportingDocumentationSet();

			for (StudySupportingDocumentation doc : currentDocSet) {
				if (doc.getName().equals(selectedDocumentName)) {
					sd = doc;
					break;
				}
			}

			if (sd == null) { // throw exception if the selected documentation is not found in session.
				throw new DocumentationUploadException("The selected supporting documentation name "
						+ selectedDocumentName + " does not match any of the documents in session");
			}
		} else {
			sd = new StudySupportingDocumentation();
			sd.setDateCreated(new Date());
		}
		
		sd.setFileType(supportingDocType);
		if(supportingDocDescription!=null){
			sd.setDescription(supportingDocDescription);
		}
		if(supportingDocTitle!=null){
			sd.setTitle(supportingDocTitle);
		}
		
		if (PortalConstants.FILETYPE_PUBLICATION.equals(supportingDocType.getName()) && pubEntry != null
				&& !StringUtils.isEmpty(pubEntry.getTitle())) {
			Publication pub = new Publication(pubEntry);
			sd.setPublication(pub);
			sd.setTitle(pubEntry.getTitle());
			sd.setDescription(pubEntry.getDescription());
			pubEntry = new Publication();

		} else if (PortalConstants.FILETYPE_SOFTWARE.equals(supportingDocType.getName())) {
			sd.setVersion(version);
		}
		
		if (getSessionUploadFile().getUploadFile() != null && uploadSupportDoc == null) {
			uploadSupportDoc = getSessionUploadFile().getUploadFile();
		}

		if (StringUtils.isBlank(getUrl())) {
			// Create new user file only if it's new doc or edited doc's file name changed
			if (!isEditingDoc
					|| (isEditingDoc && uploadSupportDocFileName != null && !uploadSupportDocFileName.equals(sd
							.getName()))) {
				if (uploadSupportDocFileName == null && uploadFileName !=null){
					uploadSupportDocFileName = uploadFileName;
				}
				UserFile userFile =
						repositoryManager.uploadFile(getUser().getId(), uploadSupportDoc, uploadSupportDocFileName,
								null, ServiceConstants.FILE_TYPE_STUDY, new Date());				
				sd.setUserFile(userFile);
			}

		} else {
			sd.setUrl(this.getUrl());
		}

		if (!isEditingDoc) {
			currentStudy.getSupportingDocumentationSet().add(sd);
		}
		getSessionStudy().setStudy(currentStudy);

		// Clear uploaded file.
		getSessionUploadFile().clear();
		uploadSupportDoc = null;
		selectedDocumentName = null;

		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}
	
	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/study/studyDocumentationAction!getUploadDocumentation.action
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

		String docName = getSelectedDocumentFromParam();

		if (!StringUtils.isBlank(docName)) {
			docName = docName.replace("\\", "");
			Set<StudySupportingDocumentation> currentDocSet = getSessionStudy().getStudy().getSupportingDocumentationSet();

			for (StudySupportingDocumentation sd : currentDocSet) {
				if (sd.getName().equals(docName)) {
					this.setIsEditingDoc(true);
					this.setSupportingDocType(sd.getFileType().getId());
					this.setSupportingDocDescription(sd.getDescription());
					this.setSupportingDocTitle(sd.getTitle());

					if (!StringUtils.isBlank(sd.getUrl())) {
						this.setAddDocSelect("url");
						this.setUrl(sd.getUrl());
					} else {
						this.setAddDocSelect("file");
						this.setUploadFileName(sd.getUserFile().getName());
						this.setUploadSupportDocFileName(sd.getUserFile().getName());

					}

					if (PortalConstants.FILETYPE_PUBLICATION.equals(supportingDocType.getName())) {
						this.setPubEntry(sd.getPublication());
					} else if (PortalConstants.FILETYPE_SOFTWARE.equals(supportingDocType.getName())) {
						this.setSoftwareName(sd.getSoftwareName());
						this.setVersion(sd.getVersion());
					}

					selectedDocumentName = docName;
					return PortalConstants.ACTION_ADD_DOCUMENTATION_DIALOG;
				}
			}
		}

		return null;
	}
	
	public String viewDocumentation() throws UnsupportedEncodingException {
		String returnName = "ViewPublication";
		String docName = getSelectedDocumentFromParam();

		if (!StringUtils.isBlank(docName)) {
			docName = docName.replace("\\", "");
			Set<StudySupportingDocumentation> currentDocSet = getSessionStudy().getStudy().getSupportingDocumentationSet();

			for (StudySupportingDocumentation sd : currentDocSet) {
				if (sd.getName().equals(docName)) {
					this.setSupportingDocType(sd.getFileType().getId());
					this.setSupportingDocDescription(sd.getDescription());
					this.setSupportingDocTitle(sd.getTitle());

					this.setAddDocSelect("file");
					if(sd.getUserFile()!=null){
					this.setUploadFileName(sd.getUserFile().getName());
					this.setUploadSupportDocFileName(sd.getUserFile().getName());
					}

					if (PortalConstants.FILETYPE_PUBLICATION.equals(supportingDocType.getName())) {
						this.setPubEntry(sd.getPublication());
						returnName = "ViewPublication";
					} else if (PortalConstants.FILETYPE_SOFTWARE.equals(supportingDocType.getName())) {
						this.setSoftwareName(sd.getSoftwareName());
						this.setVersion(sd.getVersion());
						returnName = "ViewSoftware";
					}

					selectedDocumentName = docName;
					return returnName;
				}
			}
		}

		return null;
	}

	/**
	 * Removes the documentation from study
	 * 
	 * @return
	 */
	public String removeDocumentations() throws UnsupportedEncodingException {

		String docNames = getSelectedDocumentFromParam();

		Set<StudySupportingDocumentation> currentDocSet = getSessionStudy().getStudy().getSupportingDocumentationSet();
		
		if (!StringUtils.isBlank(docNames)) {
			String[] docNameArr = docNames.split(",");

			for (int i = 0; i < docNameArr.length; i++) {
				String docName = docNameArr[i].replace("\\", "");  // Remove backslash that may be added in the request
																	// call

				for (Iterator<StudySupportingDocumentation> it = currentDocSet.iterator(); it.hasNext();) {
					StudySupportingDocumentation sd = it.next();
					
					 String testName = sd.getName();

					if (testName.equals(docName)) {
						// if it's a new supporting documentation, we also delete the user file associated with it
						if (sd.getId() == null && sd.getUserFile() != null) {
							repositoryManager.removeUserFile(sd.getUserFile());
						}

						it.remove();
						logger.debug("Removed StudySupportingDocumentation " + docName);
						break;
					}
				}
			}
		}
		
		return PortalConstants.ACTION_SUPPORTING_DOC_TABLE;
	}

	public String addDocDialog() {
		return PortalConstants.ACTION_ADD_DOCUMENTATION_DIALOG;
	}


	public String getPubMedInfo() {

		String pubMedId = getRequest().getParameter("pubMedId");
		JSONObject pubMedJSONObj = new JSONObject();

		if (StringUtils.isEmpty(pubMedId)) {
			pubMedJSONObj.put("error", "Pubmed ID cannot be null.");
			pubMedJson = pubMedJSONObj.toString();
			logger.error("PubMed ID is empty.");
			return "pubmed";
		}

		String pubMedWsUrl = PortalConstants.pubmedWsUrl + pubMedId;

		try {
			Document doc = Jsoup.connect(pubMedWsUrl).get();

			Elements error = doc.select("error");
			String errorText = error.html();
			if (!StringUtils.isEmpty(errorText)) {
				pubMedJSONObj.put("format", errorText);
				pubMedJson = pubMedJSONObj.toString();
				return "pubmed";
			}

			// First Author, PubMed returns in the format of LastName FIMI (First Name Initial and Middle Name Initial)
			Elements firstAuth = doc.select("Item[Name=Author]:nth-child(1");
			String firstAuthStr = firstAuth.html();
			if (!StringUtils.isEmpty(firstAuthStr)) {
				String[] faArr = firstAuthStr.split(" ");
				if (faArr.length > 0) {
					pubMedJSONObj.put("faLastName", faArr[0]);

					if (faArr.length > 1) {
						String faFiMi = faArr[1];
						pubMedJSONObj.put("faFirstName", faFiMi.substring(0, 1));

						if (faFiMi.length() == 2) {
							pubMedJSONObj.put("faMiddleName", faFiMi.substring(1));
						}
					}
				}
			}

			// Last Author
			Elements lastAuthor = doc.select("Item[Name=LastAuthor]");
			String lastAuthStr = lastAuthor.html().trim();
			if (!StringUtils.isEmpty(lastAuthStr)) {
				String[] laArr = lastAuthStr.split(" ");
				if (laArr.length > 0) {
					pubMedJSONObj.put("laLastName", laArr[0]);

					if (laArr.length > 1) {
						String laFiMi = laArr[1];
						pubMedJSONObj.put("laFirstName", laFiMi.substring(0, 1));

						if (laFiMi.length() == 2) {
							pubMedJSONObj.put("laMiddleName", laFiMi.substring(1));
						}
					}
				}
			}

			// Title
			Elements title = doc.select("Item[Name=Title]");
			String titleText = title.html();
			if (!StringUtils.isEmpty(titleText)) {
				pubMedJSONObj.put("title", titleText);
			}

			// Publication Date
			Elements histElements = doc.select("Item[Name=History]");
			Elements pubDate = histElements.select("Item[Name=pubMed]");
			String pubDateStr = pubDate.html();
			if (!StringUtils.isEmpty(pubDateStr)) {
				String[] dateArr = pubDateStr.split(" ");  // remove timestamp if exists
				pubMedJSONObj.put("publicationDate", dateArr[0].replace("/", "-"));  // change the format to yyyy-MM-dd
			}
			
			
			// Publication Abstract
			String pubMedAbstractWsUrl = PortalConstants.pubmedAbstractWsUrl + pubMedId;

			String summaryStr = new String(Jsoup.connect(pubMedAbstractWsUrl).get().toString().getBytes(StandardCharsets.ISO_8859_1));
			summaryStr = summaryStr.replace("&lt;", "<").replace("&gt;", ">");
			Document summary = Jsoup.parse(summaryStr);
			
			Elements abstractEles = summary.select("AbstractText");
			String abstractStr = "";
			for (Element abstractEle : abstractEles) {
				if(!StringUtils.isEmpty(abstractEle.attr("Label"))){					
					abstractStr += abstractEle.attr("Label") + ": \n"+abstractEle.html() +"\n\n";
				} else {
					abstractStr += abstractEle.html();
				}
			}
			
			logger.debug("abstractStr: "+abstractStr);
			if(abstractStr.length() > PortalConstants.PUBMED_ABSTRACT_MAX_CHARACTERS){
				abstractStr = abstractStr.substring(0, PortalConstants.PUBMED_ABSTRACT_MAX_CHARACTERS-1) + "...";				
			}
			if (StringUtils.isEmpty(abstractStr)) {
				errorText = "Error in retrieving abstract for Pubmed# "+pubMedId;
				pubMedJSONObj.put("format", errorText);
				pubMedJson = pubMedJSONObj.toString();
				return "pubmed";
			} else {
				pubMedJSONObj.put("abstract", abstractStr);
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		pubMedJson = pubMedJSONObj.toString();
		return "pubmed";
	}


	public String updateAdminFile() {

		if (adminUploadFileName != null) {
			try {
				UserFile adminFile =
						repositoryManager.uploadFile(getUser().getId(), adminUpload, adminUploadFileName, null,
								ServiceConstants.FILE_TYPE_STUDY, new Date());
				getSessionStudy().getStudy().setDataSubmissionDocument(adminFile);
			} catch (JSchException | IOException e) {
				e.printStackTrace();
			}
		}
		return PortalConstants.ACTION_INPUT;
	}

	/**
	 * Ajax call to this will display the supporting documentation table
	 * 
	 * @return
	 */
	public String displaySupportingDocumentation() {
		return PortalConstants.ACTION_SUPPORTING_DOC_TABLE;
	}

	public String getSelectedDocumentFromParam() throws UnsupportedEncodingException {

		String selectedDocumentName =
				URLDecoder.decode(getRequest().getParameter(PortalConstants.SUPPORTING_DOC_NAME), "UTF-8");
		selectedDocumentName = selectedDocumentName.replaceAll("\\\\", "");

		return selectedDocumentName;
	}

	/**
	 * @inheritDoc
	 */
	public Study getSessionObject() {
		return getSessionStudy().getStudy();
	}

	public List<FileType> getSupportingDocTypes() {
		return staticManager.getSupportingDocumentationTypeList();
	}

	public boolean getIsEditingDoc() {
		return isEditingDoc;
	}

	public void setIsEditingDoc(boolean isEditingDoc) {
		this.isEditingDoc = isEditingDoc;
	}

	public String getAddDocSelect() {
		return addDocSelect;
	}

	public void setAddDocSelect(String addDocSelect) {
		this.addDocSelect = addDocSelect;
	}

	public FileType getSupportingDocType() {
		return supportingDocType;
	}

	public void setSupportingDocType(Long supportingDocTypeId) {
		if (supportingDocTypeId != null) {
			for (FileType type : this.getSupportingDocTypes()) {
				if (type.getId().equals(supportingDocTypeId)) {
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
	
	public String getSupportingDocTitle() {
		return supportingDocTitle;
	}

	public void setSupportingDocTitle(String supportingDocTitle) {
		this.supportingDocTitle = supportingDocTitle;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public Publication getPubEntry() {
		return pubEntry;
	}

	public void setPubEntry(Publication pubEntry) {
		this.pubEntry = pubEntry;
	}

	public String getPubMedJson() {
		return pubMedJson;
	}

	public void setPubMedJson(String pubMedJson) {
		this.pubMedJson = pubMedJson;
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

	public String getSelectedDocumentName() {
		return selectedDocumentName;
	}

	public void setSelectedDocumentName(String selectedDocumentName) {
		this.selectedDocumentName = selectedDocumentName;
	}

	public String getSoftwareName() {
		return softwareName;
	}

	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getActionName() {
		return ActionContext.getContext().getName();
	}

	public String getValidationActionName() {
		return "studyDocValidationAction";
	}
	
	public String documentationRefresh() {
		return PortalConstants.ACTION_DOCUMENTATION;
	}

	public File getAdminUpload() {
		return adminUpload;
	}

	public void setAdminUpload(File adminUpload) {
		this.adminUpload = adminUpload;
	}

	public String getAdminUploadContentType() {
		return adminUploadContentType;
	}

	public void setAdminUploadContentType(String adminUploadContentType) {
		this.adminUploadContentType = adminUploadContentType;
	}

	public String getAdminUploadFileName() {
		return adminUploadFileName;
	}

	public void setAdminUploadFileName(String adminUploadFileName) {
		this.adminUploadFileName = adminUploadFileName;
	}

	public int getDocumentationLimit() {
		return -1;
	}

}
