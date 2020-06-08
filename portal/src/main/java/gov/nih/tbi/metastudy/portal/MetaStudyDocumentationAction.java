package gov.nih.tbi.metastudy.portal;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.SocketException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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
import gov.nih.tbi.commons.portal.DocumentationUploadAction;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.util.BRICSFilesUtils;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyDocumentation;
import gov.nih.tbi.repository.model.SupportingDocumentationInterface;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.taglib.datatableDecorators.SupportDocIdtListDecorator;

public class MetaStudyDocumentationAction extends BaseMetaStudyAction implements DocumentationUploadAction<MetaStudy> {

	private static final long serialVersionUID = -7802691340866721399L;

	private static Logger logger = Logger.getLogger(MetaStudyDocumentationAction.class);

	// Supporting Documentation
	private String addDocSelect;
	private boolean isEditingDoc = false;
	private FileType supportingDocType;
	private String supportingDocDescription;
	private String supportingDocTitle;
	private String url;

	private File uploadSupportDoc;
	private String uploadSupportDocContentType;

	private String uploadSupportDocFileName;
	// Hidden field that maps to the uploaded file name for validation purpose
	private String uploadFileName;
	private String selectedDocumentName;
	
	//From Action to JSP
	private String pubMedJson;
	//From JSP to action Publication data
	private String pubMedId;
	private String publicationDate;
	//private Author firstAuthor;
	//private Author lastAuthor;
	private Publication pubEntry;
	//Software data 
	private  String softwareName;
	private String version;
	
	
	

	

	public String getPubMedId() {
		return pubMedId;
	}

	public void setPubMedId(String pubMedId) {
		this.pubMedId = pubMedId;
	}

	public String getPubMedJson() {
		return pubMedJson;
	}

	public void setPubMedJson(String pubMedJson) {
		this.pubMedJson = pubMedJson;
	}

	public String getSelectedDocumentName() {
		return selectedDocumentName;
	}

	public void setSelectedDocumentName(String selectedDocumentName) {
		this.selectedDocumentName = selectedDocumentName;
	}

	public String addDocDialog() {
		return PortalConstants.ACTION_ADD_DOCUMENTATION_DIALOG;
	}

	public String getActionName() {
		return ActionContext.getContext().getName();
	}
	
	public String edit() {
		return PortalConstants.ACTION_EDIT;
	}

	@Override
	public void validate(){
		if (hasFieldErrors()) {
			logger.debug("validate() has field errors: " + getFieldErrors().size());
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
	
	public String getSelectedDocumentFromParam() throws UnsupportedEncodingException {

		String selectedDocumentName =
				URLDecoder.decode(getRequest().getParameter(PortalConstants.SUPPORTING_DOC_NAME), "UTF-8");
		selectedDocumentName = selectedDocumentName.replaceAll("\\\\", "");

		return selectedDocumentName;
	}

	public String uploadDocumentation() throws SocketException, IOException, JSchException,
			DocumentationUploadException {
		selectedDocumentName = getSelectedDocumentFromParam();
		MetaStudy currentMetaStudy = getSessionMetaStudy().getMetaStudy();
		MetaStudyDocumentation sd = null;

		if (isEditingDoc && selectedDocumentName != null) {
			Set<MetaStudyDocumentation> currentDocSet = currentMetaStudy.getSupportingDocumentationSet();

			for (MetaStudyDocumentation doc : currentDocSet) {
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
			sd = new MetaStudyDocumentation();
			sd.setDateCreated(new Date());
			 
			if (PortalConstants.FILETYPE_PUBLICATION.equals(supportingDocType.getName()) && pubEntry != null
					&& !StringUtils.isEmpty(pubEntry.getTitle())) {
				Publication pub = new Publication(pubEntry);
				sd.setPublication(pub);

				sd.setDescription(pubEntry.getDescription());
				pubEntry = new Publication();

			} else if (PortalConstants.FILETYPE_SOFTWARE.equals(supportingDocType.getName())) {
				sd.setSoftwareName(softwareName);
				sd.setVersion(version);
			}
			
			currentMetaStudy.addSupportingDocumentationSet(sd);
		}
		
		if(supportingDocDescription!=null){
			sd.setDescription(this.getSupportingDocDescription());
		}
		if(supportingDocTitle!=null) {
			sd.setTitle(this.getSupportingDocTitle());
		}
		sd.setFileType(this.getSupportingDocType());
		
		if (isEditingDoc && PortalConstants.FILETYPE_PUBLICATION.equals(sd.getFileType().getName())) {
			sd.setPublication(pubEntry);
		}
		
		if(softwareName!=null){
			sd.setSoftwareName(this.getSoftwareName());
		}
		if(version!=null){
			sd.setVersion(this.getVersion());
		}
	
		getSessionMetaStudy().addNewSupportingDocData(sd);

		if (getSessionUploadFile().getUploadFile() != null && uploadSupportDoc == null) {
			uploadSupportDoc = getSessionUploadFile().getUploadFile();
			uploadSupportDocFileName = getSessionUploadFile().getUploadFileFileName();
			uploadSupportDocContentType = getSessionUploadFile().getUploadFileContentType();
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
						metaStudyManager.uploadFile(getUser().getId(), uploadSupportDoc, uploadSupportDocFileName,
								null, ServiceConstants.FILE_TYPE_META_STUDY_DOC, new Date());
				sd.setUserFile(userFile);
			}
		} else {
			sd.setUrl(this.getUrl());
		}
		
		getSessionMetaStudy().setMetaStudy(currentMetaStudy);
		
		// Clear uploaded file.
		getSessionUploadFile().clear();
		uploadSupportDoc = null;
		selectedDocumentName = null;
		
		Writer rw = getResponse().getWriter();
		
		rw.write("success");
		rw.flush();
		rw.close();

//		return PortalConstants.ACTION_REDIRECT_TO_DOCUMENTATION_REFRESH;
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8081/portal/metastudy/metaStudyDocAction!getUploadDocumentation.action
	public String getUploadDocumentation() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<SupportingDocumentationInterface> sdList = 
					new ArrayList<SupportingDocumentationInterface>(getSessionMetaStudy().getMetaStudy().getSupportingDocumentationSet());
			
			idt.setList(sdList);
			idt.decorate(new SupportDocIdtListDecorator(getSessionMetaStudy().getMetaStudy()));
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
			Set<MetaStudyDocumentation> currentDocSet =
					getSessionMetaStudy().getMetaStudy().getSupportingDocumentationSet();

			for (MetaStudyDocumentation sd : currentDocSet) {
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
						
						if (PortalConstants.FILETYPE_PUBLICATION.equals(supportingDocType.getName())) {
							this.setPubEntry(sd.getPublication());
						} else if (PortalConstants.FILETYPE_SOFTWARE.equals(supportingDocType.getName())) {
							this.setSoftwareName(sd.getSoftwareName());
							this.setVersion(sd.getVersion());
						}
					}

					selectedDocumentName = docName;				
					return PortalConstants.ACTION_ADD_DOCUMENTATION_DIALOG;
				}
			}
		}

		return null;
	}
	
	
	public String pubMedWS(){
		//String url = "https://www.ncbi.nlm.nih.gov/pmc/utils/oa/oa.fcgi?id=PMC13901";
		JSONArray pubMedJSONArr = new JSONArray();
		JSONObject pubMedJSONObj = new JSONObject();
		if(pubMedId==null|| pubMedId.isEmpty()){
			pubMedJSONObj.put("error", "Pubmed  ID cannot be null");
			pubMedJSONArr.put(pubMedJSONObj);
			//throw new NullPointerException("Pubmed  ID cannot be null");
			pubMedJson =pubMedJSONArr.toString();
			return "pubmed";
		
		}
		String pubMedWS = PortalConstants.pubmedWsUrl+pubMedId;
		try {
			Document doc =Jsoup.connect(pubMedWS).get();
			Elements error = doc.select("error");
			Elements title = doc.select("Item[Name=Title]");
			//Elements pubDate = doc.select("Item[Name=PubDate]");
			//Elements firstAuth = doc.select("Item[Name=AuthorList]:first-child");
//			Elements firstAuth = doc.select("Item[Name=Author]:nth-child(1");
//			Elements lastAuthor = doc.select("Item[Name=LastAuthor]");
			//Elements firstAuthName = firstAuth.getElementsByAttribute("Author");
//			String firstAuthStr = firstAuth.html();
//			String lastAuthStr = lastAuthor.html();
//			
//			 StringTokenizer stokFirst = firstMiddleLastNameSplitter(firstAuthStr);
//			 StringTokenizer stokLast = firstMiddleLastNameSplitter(lastAuthStr);
//			 String faFirstName = stokFirst.nextToken();
//			 String laFirstName = stokLast.nextToken();
//			  StringBuilder faMiddleName = new StringBuilder();
//			  StringBuilder laMiddleName = new StringBuilder();
//			    String faLastName = stokFirst.nextToken();
//			    String laLastName = stokLast.nextToken();
//			    while (stokFirst.hasMoreTokens())
//			    {
//			    	faMiddleName.append(faLastName + " ");
//			    	faLastName = stokFirst.nextToken();
//			    }
//			    while (stokLast.hasMoreTokens())
//			    {
//			    	laMiddleName.append(laLastName + " ");
//			    	laLastName = stokLast.nextToken();
//			    }
			 
//			 if(faFirstName!=null && !faFirstName.isEmpty()){
//				 pubMedJSONObj.put("faFirstName", faFirstName);
//					pubMedJSONArr.put(pubMedJSONObj);
//			 }
//			 if(laFirstName!=null && !laFirstName.isEmpty()){
//				 pubMedJSONObj.put("laFirstName", laFirstName);
//					pubMedJSONArr.put(pubMedJSONObj);
//			 }
//			 
//			 if(faMiddleName!=null && faMiddleName.length()<0){
//				 pubMedJSONObj.put("faMiddleName", faMiddleName);
//					pubMedJSONArr.put(pubMedJSONObj);
//			 }
//			 
//			 if(laMiddleName!=null && laMiddleName.length()<0){
//				 pubMedJSONObj.put("laMiddleName", laMiddleName);
//					pubMedJSONArr.put(pubMedJSONObj);
//			 }
//			 
//			 if(faLastName!=null && !faLastName.isEmpty()){
//				 pubMedJSONObj.put("faLastName", faLastName);
//					pubMedJSONArr.put(pubMedJSONObj);
//			 }
//			 if(laLastName!=null && !laLastName.isEmpty()){
//				 pubMedJSONObj.put("laLastName", laLastName);
//					pubMedJSONArr.put(pubMedJSONObj);
//			 }
			 
			String errorText = error.html();
			String titleText= title.html();
			//String publicationDate= pubDate.html();
			if(titleText!=null&&!titleText.isEmpty()){
				pubMedJSONObj.put("title", titleText);
				pubMedJSONArr.put(pubMedJSONObj);
			}
			
//			if(publicationDate!=null&&!publicationDate.isEmpty()){
//				pubMedJSONObj.put("publicationDate", publicationDate);
//				pubMedJSONArr.put(pubMedJSONObj);
//			}
			
			// Publication Date
			Elements histElements = doc.select("Item[Name=History]");
			Elements pubDate = histElements.select("Item[Name=pubMed]");
			String pubDateStr = pubDate.html();
			if (!StringUtils.isEmpty(pubDateStr)) {
				String[] dateArr = pubDateStr.split(" ");  // remove timestamp if exists
				pubMedJSONObj.put("publicationDate", dateArr[0].replace("/", "-"));  // change the format to yyyy-MM-dd
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
			if (!StringUtils.isEmpty(abstractStr)) {
				pubMedJSONObj.put("abstract", abstractStr);
			}
			
			if(errorText!=null && !errorText.isEmpty() || StringUtils.isEmpty(abstractStr)){
				String abstractErrTxt ="Error in retrieving abstract for Pubmed# "+pubMedId;
				if(StringUtils.isEmpty(abstractStr) && (errorText==null || errorText.isEmpty())){
					errorText = abstractErrTxt;
				} else {
					errorText += " | "+ abstractErrTxt;
				}
				pubMedJSONObj.put("format", errorText);
				pubMedJSONArr.put(pubMedJSONObj);
				pubMedJson =pubMedJSONArr.toString();
				return "pubmed";
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		pubMedJson =pubMedJSONArr.toString();
		return "pubmed";
		
		
	}
	
	
	
	
	
//	
//	private StringTokenizer firstMiddleLastNameSplitter(String fullName){
//		 StringTokenizer stok = new StringTokenizer(fullName);
//		 return stok;
//	}

	public String removeDocumentations() throws UnsupportedEncodingException {

		String docNames = getSelectedDocumentFromParam();

		Set<MetaStudyDocumentation> currentDocSet =
				getSessionMetaStudy().getMetaStudy().getSupportingDocumentationSet();
		Set<MetaStudyDocumentation> newDocSet = new HashSet<MetaStudyDocumentation>(currentDocSet);

		if (!StringUtils.isBlank(docNames)) {
			String[] docNameArr = docNames.split(",");

			for (int i = 0; i < docNameArr.length; i++) {
				String docName = docNameArr[i].replace("\\", "");  // Remove backslash that may be added in the request
																	// call
				Iterator<MetaStudyDocumentation> it = newDocSet.iterator();
				while (it.hasNext()) {
					MetaStudyDocumentation sd = it.next();
					String testName = sd.getName();
					if (testName.equals(docName)) {
						// if it's a new supporting documentation, we also delete the user file associated with it.
						if (sd.getId() == null && sd.getUserFile() != null) {
							metaStudyManager.removeUserFile(sd.getUserFile());
						}
						it.remove();
						logger.debug("Removed SupportingDocumentation " + docName);
						break;
					}
				}
			}
		}
		getSessionMetaStudy().getMetaStudy().setSupportingDocumentationSet(newDocSet);

	return PortalConstants.ACTION_DOCUMENTATION;

	}

	public List<MetaStudyDocumentation> getSupportDocList() {

		List<MetaStudyDocumentation> supportDocList = new ArrayList<MetaStudyDocumentation>();
		supportDocList.addAll(sessionMetaStudy.getMetaStudy().getSupportingDocumentationSet());

		return supportDocList;
	}
	
	public String viewDocumentation() throws UnsupportedEncodingException {
		String returnName = "ViewPublication";
		String docName = getSelectedDocumentFromParam();

		if (!StringUtils.isBlank(docName)) {
			docName = docName.replace("\\", "");
			List<MetaStudyDocumentation> currentDocSet = getSupportDocList();

			for (SupportingDocumentation sd : currentDocSet) {
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

	public List<FileType> getSupportingDocTypes() {
		return staticManager.getMetaStudySupportingDocFileTypeList();
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
		
	public String getSupportingDocTitle() {
		return supportingDocTitle;
	}

	public void setSupportingDocTitle(String supportingDocTitle) {
		this.supportingDocTitle = supportingDocTitle;
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

	/**
	 * @inheritDoc
	 */
	public MetaStudy getSessionObject() {
		return getSessionMetaStudy().getMetaStudy();
	}

	/**
	 * @inheritDoc
	 */
	public int getDocumentationLimit() {
		return -1;
	}

	/**
	 * @inheritDoc
	 */
	public String getValidationActionName() {
		return "metaStudyDocValidationAction";
	}
	
	public String documentationRefresh() {
		return PortalConstants.ACTION_DOCUMENTATION;
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

	public String getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(String publicationDate) {
		this.publicationDate = publicationDate;
	}

//	public Author getFirstAuthor() {
//		return firstAuthor;
//	}
//
//	public void setFirstAuthor(Author firstAuthor) {
//		this.firstAuthor = firstAuthor;
//	}
//
//	public Author getLastAuthor() {
//		return lastAuthor;
//	}
//
//	public void setLastAuthor(Author lastAuthor) {
//		this.lastAuthor = lastAuthor;
//	}

	public Publication getPubEntry() {
		return pubEntry;
	}

	public void setPubEntry(Publication pubEntry) {
		this.pubEntry = pubEntry;
	}


}
