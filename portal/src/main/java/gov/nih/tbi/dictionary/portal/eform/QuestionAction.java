package gov.nih.tbi.dictionary.portal.eform;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xml.security.utils.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.service.BtrisMappingManager;
import gov.nih.tbi.commons.service.EformManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.formbuilder.SessionEform;
import gov.nih.tbi.dictionary.model.hibernate.BtrisMapping;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocument;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocumentPk;
import gov.nih.tbi.dictionary.portal.BaseEformAction;
import gov.nih.tbi.repository.model.hibernate.UserFile;


public class QuestionAction extends BaseEformAction {

	private static final long serialVersionUID = 6750429081049754146L;
	static Logger logger = Logger.getLogger(QuestionAction.class);

	@Autowired
	EformManager eFormManager;
	
	@Autowired
	SessionEform sessionEform;

	@Autowired
	BtrisMappingManager btrisMappingManager;

	/**
	 * Struts Constant used to set/get ActionMessages for this action from session.
	 */
	public static final String ACTION_MESSAGES_KEY = "QuestionImageAction_ActionMessages";

	private String questionId;
	private String imageFileName;
	private List<File> imageFile = new ArrayList<File>();
	private List<String> imageFileFileName = new ArrayList<String>();
	private int imageCount = 0;           // number of images to add
	private String[] namesToDelete = {};
	InputStream questionDocumentImage;
	private String imageNames;
	private List<QuestionDocument> questionDocumentList = new ArrayList<QuestionDocument>();
	
	//Refactor by Yogi to remove scriplets logic in JSP
	JSONObject graphicJSON = new JSONObject();
	
	private String fileJson;
	
	private String questionAttributeJson;

	public String getFileJson() {
		return fileJson;
	}

	public void setFileJson(String fileJson) {
		this.fileJson = fileJson;
	}

	public String getQuestionAttributeJson() {
		return questionAttributeJson;
	}

	public void setQuestionAttributeJson(String questionAttributeJson) {
		this.questionAttributeJson = questionAttributeJson;
	}

	/**
	 * Method to image upload landing page
	 * @return
	 * @throws Exception
	 */
	public String showQuestionImage() throws Exception {
		displayQuestionImageTable();
		return SUCCESS;
	}
	
	public String displayQuestionImageTable() {
		logger.info("questionId: "+this.questionId);
		if (this.questionId != null){
			Question docQuestion = eFormManager.getQuestion(Long.parseLong(this.questionId));
			
			List<QuestionDocument> quesDocList =  new ArrayList(docQuestion.getQuestionDocument());	
			this.setQuestionDocumentList(quesDocList);
		}
		return PortalConstants.ACTION_VIEW;
	}

	/**
	 * Method to render thumb nail image
	 * @return
	 * @throws Exception
	 */
	public String renderImage(){
		
		//TODO: Need permission check!!!!!!!!!!!!!!!
		this.getResponse().setContentType(MediaType.APPLICATION_JSON);
		Question documentQuestion = eFormManager.getQuestion(Long.parseLong(this.questionId));
		
		 QuestionDocument imageQuestionDocument = eFormManager.getQuestionDocument(documentQuestion, this.imageFileName);
		 Set<QuestionDocument> quesDocList =  documentQuestion.getQuestionDocument();
		 JSONArray imgJSONArr = new JSONArray();

		try {
				
			for(QuestionDocument qd : quesDocList){
				String fileName = qd.getQuestionDocumentPk().getFileName();
				byte[] questionDocumentBytes = repositoryManager.getFileByteArray(qd.getUserFile());
				String fileBytesEncoded = new String(Base64.encode(questionDocumentBytes));
				MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
				String mimeTypeOfFile = mimetypesFileTypeMap.getContentType(qd.getUserFile().getName());
				JSONObject imgJSONObj = new JSONObject();
				imgJSONObj.put("name", fileName);
				imgJSONObj.put("userFileId", qd.getUserFile().getId());
				imgJSONObj.put("source", "data:" + mimeTypeOfFile + ";base64," + fileBytesEncoded);
				imgJSONArr.put(imgJSONObj);

			}
			fileJson = imgJSONArr.toString();
			return SUCCESS;
		} catch (Exception e) {
			logger.error("There was an error transfering the image to the form builder", e);
			return PortalConstants.FAILURE;
		} 
	}
	
	/**
	 * Method to render thumb nail image
	 * @return
	 * @throws Exception
	 */
	public String renderSingleImage(){
		
		//TODO: Need permission check!!!!!!!!!!!!!!!
		this.getResponse().setContentType(MediaType.APPLICATION_JSON);
		Question documentQuestion = eFormManager.getQuestion(Long.parseLong(this.questionId));
		
		QuestionDocument imageQuestionDocument = eFormManager.getQuestionDocument(documentQuestion, this.imageFileName);
		JSONArray imgJSONArr = new JSONArray();
		 
		try {
			imageQuestionDocument.getUserFile().setName(imageQuestionDocument.getQuestionDocumentPk().getFileName());
			byte[] questionDocumentBytes = repositoryManager.getFileByteArray(imageQuestionDocument.getUserFile());
			String fileBytesEncoded = new String(Base64.encode(questionDocumentBytes));
			MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
			String mimeTypeOfFile = mimetypesFileTypeMap.getContentType(imageQuestionDocument.getQuestionDocumentPk().getFileName());
			imgJSONArr.put( "data:" + mimeTypeOfFile + ";base64," + fileBytesEncoded);
			fileJson = imgJSONArr.toString();
			return SUCCESS;
		} catch (Exception e) {
			logger.error("There was an error transfering the image to the form builder", e);
			return PortalConstants.FAILURE;
		} 
	}

	/**
	 * Method to upload file and close the active editor and save image as thumbnail
	 * @return
	 * @throws Exception
	 */
	public String saveQuestionImage() throws Exception {
		try {
			// first we need to get the Question
			Question question = eFormManager.getQuestion(Long.parseLong(getQuestionId()));
			List<QuestionDocument> quesDocList = new ArrayList<QuestionDocument>();
			List<String>  imageFileFileNameList = this.getImageFileFileName();
			String[] fileNameArray = imageFileFileNameList.toArray(new String[0]);
			
			boolean isFileExtensionValid = false;
			boolean isFileSizeValid = false;
		
			for(String fileName : imageFileFileNameList){
				String fileExtesionFromJsp= FilenameUtils.getExtension(fileName);
				if (PortalConstants.QUESTION_DOCUMENT_TYPES.contains(fileExtesionFromJsp.toLowerCase()))
				{
					isFileExtensionValid =true;
				}
					
			}
			
			
			String filePath = ServiceConstants.DDT_EFORM_FILE_PATH + getQuestionId() + ServiceConstants.FILE_SEPARATER;
			Set<String> uploadedFiles = new HashSet<String>();
			
			int nameCounter = 0;
			for (File file : imageFile) {
				
				Long fileSizeInMB =file.length() / (1024 * 1024);				
				if (fileSizeInMB < PortalConstants.PERMISSIBLE_UPLOAD_FILESIZE) {	
					isFileSizeValid = true;
				}
				
				if(!isFileExtensionValid && !isFileSizeValid) {
					 graphicJSON.put("graphicNames",PortalConstants.UNSUPPORTED_FILE_EXTENSION+PortalConstants.UNSUPPORTED_FILE_SIZE);
					 return SUCCESS;
				}else if(isFileExtensionValid && !isFileSizeValid) {
					 graphicJSON.put("graphicNames",PortalConstants.UNSUPPORTED_FILE_SIZE);
					 return SUCCESS;
				}else if(!isFileExtensionValid && isFileSizeValid) {
					 graphicJSON.put("graphicNames",PortalConstants.UNSUPPORTED_FILE_EXTENSION);
					 return SUCCESS;
				}
				
					QuestionDocument quesDoc = new QuestionDocument();
					QuestionDocumentPk compositeId = new QuestionDocumentPk();
					compositeId.setFileName(fileNameArray[nameCounter]);
					if (!uploadedFiles.contains(compositeId.getFileName())) {
						UserFile userFile = repositoryManager.uploadFileDDTWithPath(getUser().getId(), file, compositeId.getFileName(),
								null, ServiceConstants.FILE_TYPE_EFORM, new Date(), filePath);
	
						quesDoc.setUserFile(userFile);
						compositeId.setQuestion(question);
						quesDoc.setQuestionDocumentPk(compositeId);
						quesDocList.add(quesDoc);
					}
					uploadedFiles.add(compositeId.getFileName());
					nameCounter++;
			}

			eFormManager.saveQuestionDocument(quesDocList);

			List<QuestionDocument> questionDocuments = eFormManager
					.getQuestionDocuments(question);
		
			
			List<String> names = new ArrayList<String>();
			// pass them back to the front end
			for (QuestionDocument qd : questionDocuments) {
				names.add(qd.getUserFile().getName());
			}
			graphicJSON.put("graphicNames",names.toArray());	
		

		} catch (Exception ce) {
			return PortalConstants.FAILURE;
		}
		return SUCCESS;
	
	}

/**
 * Method to delete graphics from question
 * @return
 * @throws Exception
 */
	public String deleteQuestionImage() throws Exception {

		String questionId = getRequest().getParameter("qId");
		String namesToDelete = getRequest().getParameter("namesToDelete");


		try {
			// get the question
			Question question = eFormManager.getQuestion(Long.parseLong(questionId));

			List<String> filesToDelete = Arrays.asList(namesToDelete.split(","));
			List<QuestionDocument> documentsToDelete = new ArrayList<QuestionDocument>();
			// Get document objects to delete
			if (question.getQuestionDocument() != null) {
				for (String name : filesToDelete) {				
					for (QuestionDocument qd : question.getQuestionDocument()) {
						if (StringUtils.equals(qd.getQuestionDocumentPk().getFileName(), name)) {
							documentsToDelete.add(qd);
						}
					}
				}
				eFormManager.deleteQuestionImages(documentsToDelete);
			}

			fileJson= namesToDelete;

		} catch (Exception ce) {
			ce.printStackTrace();
			//return PortalConstants.FAILURE;
		}

		
		return SUCCESS;
	}

	public String showQuestionBtrisMapping() throws Exception {
		String questionId = getRequest().getParameter("questionId");
		try {
			// get the question
			Question question = eFormManager.getQuestion(Long.parseLong(questionId));

			String dataElementName = question.getName().split("_", 2)[1];
			BtrisMapping btrisMapping = btrisMappingManager.getBtrisMappingByDEName(dataElementName);
			JSONObject qaJsonObj = new JSONObject();
			if (btrisMapping != null) {
				qaJsonObj.put("hasBtrisMapping", true);
				BtrisMapping associatedBM = question.getBtrisMapping();
				if (associatedBM != null) {
					qaJsonObj.put("isGettingBtrisVal", true);
				} else {
					qaJsonObj.put("isGettingBtrisVal", false);
				}
				qaJsonObj.put("btrisObservationName", btrisMapping.getBtrisObservationName());
				qaJsonObj.put("btrisRedCode", btrisMapping.getBtrisRedCode());
				qaJsonObj.put("btrisSpecimenType", btrisMapping.getBtrisSpecimenType());
			} else {
				qaJsonObj.put("hasBtrisMapping", false);
			}
			this.questionAttributeJson = qaJsonObj.toString();
		} catch (Exception ce) {
			ce.printStackTrace();
			return PortalConstants.FAILURE;
		}
		return SUCCESS;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public List<File> getImageFile() {
		return imageFile;
	}

	public void setImageFile(List<File> imageFile) {
		this.imageFile = imageFile;
	}

	public List<String> getImageFileFileName() {
		return imageFileFileName;
	}

	public void setImageFileFileName(List<String> imageFileFileName) {
		this.imageFileFileName = imageFileFileName;
	}

	public int getImageCount() {
		return imageCount;
	}

	public void setImageCount(int imageCount) {
		this.imageCount = imageCount;
	}

	public String[] getNamesToDelete() {
		return namesToDelete;
	}

	public void setNamesToDelete(String[] namesToDelete) {
		this.namesToDelete = namesToDelete;
	}
	
	public JSONObject getGraphicJSON() {
		return graphicJSON;
	}

	public void setGraphicJSON(JSONObject graphicJSON) {
		this.graphicJSON = graphicJSON;
	}
	
	public void setImageFileName(String imageFileName){
		this.imageFileName = imageFileName;
	}
	
	public String getImageFileName(){
		return this.imageFileName;
	}
	
	public InputStream getQuestionDocumentImage(){
		return this.questionDocumentImage;
	}
	
	public void setQuestionDocumentImage(InputStream questionDocumentImage){
		this.questionDocumentImage = questionDocumentImage;
	}
	
	public void setSessionEform(SessionEform sessionEform){
		this.sessionEform = sessionEform;
	}
	
	public SessionEform getSessionEform(){
		return this.sessionEform;
	}
	
	public void setImageNames(String imageNames){
		this.imageNames = imageNames;
	}
	
	public String getImageNames(){
		return this.imageNames;
	}
	
	public List<QuestionDocument> getQuestionDocumentList() {
		return this.questionDocumentList;
	}

	public void setQuestionDocumentList(List<QuestionDocument> questionDocumentList) {
		this.questionDocumentList = questionDocumentList;
	}
}
