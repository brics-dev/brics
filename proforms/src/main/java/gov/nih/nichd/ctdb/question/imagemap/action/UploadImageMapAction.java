package gov.nih.nichd.ctdb.question.imagemap.action;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.FileUploadException;
import gov.nih.nichd.ctdb.common.FileUploadInvalidTypeException;
import gov.nih.nichd.ctdb.common.FileUploader;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.question.common.QuestionConstants;
import gov.nih.nichd.ctdb.question.domain.ImageMapQuestion;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;


public class UploadImageMapAction extends BaseAction {

	private static final long serialVersionUID = 6927858728554862115L;
	
	private File imageFile = null;
	private String imageFileFileName = "";

    public String execute() throws Exception {
        try {
            QuestionManager qm = new QuestionManager();
            ImageMapQuestion imq = (ImageMapQuestion) (session.get(QuestionConstants.QUESTION_IN_PROGRESS));

            if (imageFile == null || Utils.isBlank(imageFileFileName)) {
                addActionError(getText("errors.questionwizard.fileupload.nofile", new String[]{""}));
                return StrutsConstants.EXCEPTION;
                
            } else {
                List<String> validFileTypes = new ArrayList<String>();
                validFileTypes.add("png");
                validFileTypes.add("gif");
                validFileTypes.add("jpg");
                validFileTypes.add("jpeg");
                
                String newName ="imageMap" + qm.getImageMapSequence() + 
                		imageFileFileName.substring(imageFileFileName.length()-4, imageFileFileName.length());
                
                // set image file save path in the question image domain object
                String questionImagePath = SysPropUtil.getProperty("filesystem.directory.questionimagepath");
                String path = request.getServletContext().getRealPath(questionImagePath);
                
                try {
                    FileUploader.uploadFile(imageFile, path, newName, validFileTypes);
                } catch (FileUploadInvalidTypeException e) {
                    addActionError(getText("errors.questionwizard.fileupload.filetypes", new String[]{""}));
                    return StrutsConstants.EXCEPTION;
                }

                imq.setImageFileName(newName);
                session.put(QuestionConstants.QUESTION_IN_PROGRESS, imq);
            }
        } catch (FileUploadException fue) {
            return StrutsConstants.FAILURE;
        } catch (CtdbException ce) {
            return StrutsConstants.FAILURE;
        }
        
        return SUCCESS;
    }

    
	public File getImageFile() {
		return imageFile;
	}

	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}

	public String getImageFileFileName() {
		return imageFileFileName;
	}

	public void setImageFileFileName(String imageFileFileName) {
		this.imageFileFileName = imageFileFileName;
	}
}
