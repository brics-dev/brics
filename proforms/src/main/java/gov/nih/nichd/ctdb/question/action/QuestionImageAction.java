package gov.nih.nichd.ctdb.question.action;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.FileUploadInvalidTypeException;
import gov.nih.nichd.ctdb.common.FileUploadNotFoundException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.question.domain.QuestionImage;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;


/**
 * The Struts Action class responsible for uploading and deleting image file for  
 * questions and question groups for the nichd ctdb
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionImageAction extends BaseAction {

	private static final long serialVersionUID = -6372535554321148934L;

	/**
     * Struts Constant used to set/get ActionMessages for this action from session.
     */
    public static final String ACTION_MESSAGES_KEY = "QuestionImageAction_ActionMessages";

    private String questionId = null;
    private List<File> imageFile = new ArrayList<File>();
    private List<String> imageFileFileName = new ArrayList<String>();
    private int imageCount = 0;           // number of images to add
    private String[] namesToDelete = {};

    
    public String execute() throws Exception {
        try {
            QuestionManager qm = new QuestionManager();
            
        	if (!Utils.isBlank(questionId)) {
        		
        		List<String> imageNames = qm.getQuestionImages(Integer.parseInt(questionId));
        		request.setAttribute("imageNames", imageNames);
        		
                if (imageNames.isEmpty()) {
                	session.put("noGraphics", "true");
                } else {
                	session.put("noGraphics", "false");
                }
            }
		} catch (CtdbException ce) {
			return StrutsConstants.FAILURE;
		}
        
		return SUCCESS;
	}
        	
    //small "save" button.
    public String saveQuestionImage() throws Exception {
        try {
            QuestionManager qm = new QuestionManager();
            
            QuestionImage questionImage = new QuestionImage();
            questionImage.setId(Integer.parseInt(getQuestionId()));
            questionImage.setFiles(getImageFile());
            questionImage.setNames(getImageFileFileName());
            questionImage.setNamesToDelete(Arrays.asList(getNamesToDelete()));
            
            try {
            	// set image file save path in the question image domain object
            	String questionImagePath = SysPropUtil.getProperty("filesystem.directory.questionimagepath");
            	questionImage.setPath(request.getServletContext().getRealPath(questionImagePath));
            	
            	String questionImagePrefix = getText("app.questionimageprefix");
            	qm.updateQuestionImages(questionImage, questionImagePrefix);
            	
            	List<String> names = qm.getQuestionImages(questionImage.getId());
            	session.put("questionImageNames", names);
            	
            	if (!Utils.isBlank(questionId)) {
            		addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, new String[]{"Question Images"}));
            	} else {
            		addActionMessage(getText(StrutsConstants.SUCCESS_EDIT_KEY, new String[]{"Question Images"}));
            	}

            } catch (FileUploadInvalidTypeException e) {
            	this.execute();//To refresh the Graphic Tab to show question images        		
            	addActionError(getText("errors.question.fileupload.filetypes", new String[]{""}));
            	return StrutsConstants.EXCEPTION;
            } catch (FileUploadNotFoundException e) {
            	this.execute();//To refresh the Graphic Tab to show question images
            	addActionError(getText(StrutsConstants.ERROR_FILEUPLOAD_NOTFOUND, new String[]{""}));
            	return StrutsConstants.EXCEPTION;
            }
		} catch (CtdbException ce) {
			return StrutsConstants.FAILURE;
		}
        
        session.put(QuestionImageAction.ACTION_MESSAGES_KEY, getActionMessages());
        return SUCCESS;
    }

    //happen on deleting question signal.
	public String deleteQuestionImage() throws Exception {

		String questionId = request.getParameter("qId");
		String namesToDelete = request.getParameter("namesToDelete");
		try {
			QuestionManager qm = new QuestionManager();

			QuestionImage questionImage = new QuestionImage();
			questionImage.setId(Integer.parseInt(questionId));
			questionImage.setNamesToDelete(Arrays.asList(namesToDelete));
			String questionImagePrefix = getText("app.questionimageprefix");
			qm.deleteQuestionImagesOnDB(questionImage, questionImagePrefix);

			List<String> names = qm.getQuestionImages(questionImage.getId());
			session.put("questionImageNames", names);

			if (!Utils.isBlank(questionId)) {
				addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, new String[] {"Question Images"}));
			} else {
				addActionMessage(getText(StrutsConstants.SUCCESS_EDIT_KEY, new String[] {"Question Images"}));
			}
		} catch (CtdbException ce) {
			return StrutsConstants.FAILURE;
		}

		session.put(QuestionImageAction.ACTION_MESSAGES_KEY, getActionMessages());
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

}

