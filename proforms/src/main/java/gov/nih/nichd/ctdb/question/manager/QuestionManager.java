package gov.nih.nichd.ctdb.question.manager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.FileUploadException;
import gov.nih.nichd.ctdb.common.FileUploader;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.form.domain.CalculatedFormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.question.common.GroupResultControl;
import gov.nih.nichd.ctdb.question.dao.QuestionManagerDao;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.Group;
import gov.nih.nichd.ctdb.question.domain.ImageMapExportImport;
import gov.nih.nichd.ctdb.question.domain.ImageMapOption;
import gov.nih.nichd.ctdb.question.domain.ImageMapQuestion;
import gov.nih.nichd.ctdb.question.domain.ImageMapValuesExportImport;
import gov.nih.nichd.ctdb.question.domain.InstanceType;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionImage;
import gov.nih.nichd.ctdb.question.domain.VisualScale;


/**
 * QuestionManager is a business layer object which interacts with the QuestionManagerDao. The
 * role of the QuestionManager is to enforce business rule logic and delegate data layer manipulation
 * to the QuestionManagerDao.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionManager extends CtdbManager
{
    public static List<String> removeWords = new ArrayList<String>();

    /************************************************************************
     * CREATE QUESTION METHODS
     ***********************************************************************/
    
    /**
     * Creates a Question in the CTDB System.
     *
     * @param question The question to create
     * @throws DuplicateObjectException Thrown if the question already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    
    public void createQuestion(Question question) throws DuplicateObjectException, CtdbException
    {
    	createQuestion(question,false);
    }


    public void createQuestion(Question question,Boolean copyright) throws DuplicateObjectException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);

            // determine what type of question creating
            if( question.getInstanceType().equals(InstanceType.QUESTION)||
                    question.getInstanceType().equals(InstanceType.IMAGE_MAP_QUESTION)
                    || question.getInstanceType().equals(InstanceType.VISUAL_SCALE_QUESTION) )
            {
            	
                // creating base simple question
                dao.createQuestion(question,copyright);//modify by sunny
                
                // create answers
                List<Answer> answers = question.getAnswers();
                
                if ( answers.size() == 0 ) {
                    // no  answers, must have at least one, add default
                    answers.add(new Answer());
                }
                
                int answerOrderVal = 0;
                
                for ( Answer answer : answers )
                {
                    dao.createAnswer(question, answer, answerOrderVal++);
                }
                
                // associate with groups if any
                if ( question.getGroupsAssociatedWith() != null && !question.getGroupsAssociatedWith().isEmpty() )
                {
                    for (  Group group : question.getGroupsAssociatedWith() )
                    {
                        dao.associateQuestionGroup(question.getId(), group.getId());
                    }
                }
                
                
                if ( question.getInstanceType().equals(InstanceType.IMAGE_MAP_QUESTION) ) {
                    // update the image map fields
                    if( ((ImageMapQuestion) question).getMapHolder() != null && 
                    		((ImageMapQuestion) question).getMapHolder().getBufferedImage().size() > 0 ) {
                    	QuestionImage imageMap = ((ImageMapQuestion) question).getMapHolder();
                    	 List<String> fileTypes = CtdbConstants.IMAGE_FORMATS;

                    	 String newImageFileName = String.valueOf(question.getId()) + "_" + (String)imageMap.getNames().get(0);
                    	 ((ImageMapQuestion) question).setImageFileName(newImageFileName);
                    	 FileUploader.uploadForExportImport(imageMap.getBufferedImage().get(0), imageMap.getPath(),
                    			newImageFileName,fileTypes );
                   	}
                    
                    dao.createImageMapQuestion((ImageMapQuestion)question);
                    
                    for (Iterator<ImageMapOption> iter = ((ImageMapQuestion)question).getOptions().iterator(); iter.hasNext(); ) {
                        dao.createImageMapQuestionAnswer (((ImageMapQuestion)question).getImageMapId(), (ImageMapOption)iter.next());
                    }
                    
                    // In XML import mode, the file are encoded into bytearray within mapGraphric 
                    if( ((ImageMapQuestion) question).getMapHolder() != null && 
                    		((ImageMapQuestion) question).getMapHolder().getBufferedImage().size() > 0 ) {
                    	QuestionImage imageMap = ((ImageMapQuestion) question).getMapHolder();
                    	 List<String> fileTypes = CtdbConstants.IMAGE_FORMATS;

                    	FileUploader.uploadForExportImport(imageMap.getBufferedImage().get(0), imageMap.getPath(),
                    			String.valueOf(question.getId()) + "_" + (String)imageMap.getNames().get(0),fileTypes );
                   	}
                }
                else if (question.getInstanceType().equals(InstanceType.VISUAL_SCALE_QUESTION)) {
                    dao.createVisualScaleQuestion((VisualScale)question);
                }
            }
            else {
                throw new CtdbException("Unkown Question Instance Type. Question could not be created.");
            }

            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    
    public void createQuestionForExportImport(Question question,Boolean copyright) throws DuplicateObjectException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);

            QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);

            // determine what type of question creating
            if(question.getInstanceType().equals(InstanceType.QUESTION)||
                    question.getInstanceType().equals(InstanceType.IMAGE_MAP_QUESTION)
                    || question.getInstanceType().equals(InstanceType.VISUAL_SCALE_QUESTION) )
            {

                // creating base simple question
                dao.createQuestion(question,copyright);//modify by sunny

                // create answers
                List<Answer> answers = question.getAnswers();
                
                if (answers.size() == 0) {
                    // no  answers, must have at least one, add default
                    answers.add(new Answer());
                }
                
                int answerOrderVal = 0;
                
                for ( Answer answer : answers )
                {
                    dao.createAnswer(question, answer, answerOrderVal++);
                }
                
                // associate with groups if any
                if( question.getGroupsAssociatedWith() != null && !question.getGroupsAssociatedWith().isEmpty() )
                {
                    for(Group group : question.getGroupsAssociatedWith() )
                    {
                        dao.associateQuestionGroup(question.getId(), group.getId());
                    }
                }
            }
            else
            {
                throw new CtdbException("Unkown Question Instance Type. Question could not be created.");
            }
            
            this.commit(conn);
        }
        finally
        {
			this.rollback(conn);
			this.close(conn);
		}
	}
    
    public void createVisualScaleQuestionForExportImport(Question question,Boolean copyright) throws DuplicateObjectException, CtdbException{
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
			dao.createVisualScaleQuestion((VisualScale) question);
			commit(conn);
		}
		finally {
			rollback(conn);
			close(conn);
		}
    }
    
    
    public void createImageMapQuestionForExportImport(ImageMapExportImport iMap,Boolean copyright ) throws DuplicateObjectException, CtdbException{
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
			dao.createImageMapQuestionForImport(iMap);
			
			for ( Iterator<ImageMapValuesExportImport> iter = iMap.getiMapValues().iterator(); iter.hasNext(); ) {
				dao.createImageMapQuestionAnswerForExportImport(iMap.getImageMapId(), iter.next());
			}
			
			commit(conn);
		}
		finally {
			rollback(conn);
			close(conn);
		}
    }

    /**
     * Creates question images in the CTDB System.
     *
     * @param questionImage The question image domain object that contains the images to create
     * @throws CtdbException Thrown if any errors occur while processing
     * @throws FileUploadException
     */
    public void createQuestionImage(QuestionImage questionImage, String prefix) throws CtdbException, FileUploadException
    {
        Connection conn = null;
        List<String> fileswritten = new ArrayList<String>();
        
        try
        {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            QuestionManagerDao questionDao = QuestionManagerDao.getInstance(conn);
            int n = questionImage.getFiles().size();
            
            for(int i = 0; i < n; i++)
            {
            	questionDao.createQuestionImage(questionImage, prefix, i);
                List<String> fileTypes = CtdbConstants.IMAGE_FORMATS;

                fileswritten.add(FileUploader.upload((File) questionImage.getFiles().get(i), questionImage.getPath(), questionImage.getNames().get(i), fileTypes));
            }
            
            commit(conn);
        }
        catch( FileUploadException fue )
        {
            this.rollbackDeleteFiles(fileswritten);
            throw fue;
        }
        catch(CtdbException ce)
        {
            this.rollbackDeleteFiles(fileswritten);
            throw ce;
        }
        catch(Exception e)
        {
            this.rollbackDeleteFiles(fileswritten);
            throw new CtdbException("Unknown error occurred while creating question image: " + e.getMessage(), e);
        }
        finally
        {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    /**
     * Creates question images in the CTDB System.
     *
     * @param questionImage The question image domain object that contains the images to create
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void createQuestionImageForImport(QuestionImage questionImage, String prefix) throws CtdbException
    {
        Connection conn = null;
  
        try
        {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            QuestionManagerDao questionDao = QuestionManagerDao.getInstance(conn);
            int n = questionImage.getNames().size();
            
            for ( int i = 0; i < n; i++ )
            {
            	questionDao.createQuestionImageForImport(questionImage, prefix, i);
            }
            
            this.commit(conn);
        }
        finally
        {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /************************************************************************
     * UPDATE QUESTION METHODS
     ***********************************************************************/

    /**
     * Updates a Question in the CTDB System.
     *
     * @param question The question to update
     * @throws ObjectNotFoundException  Thrown if the question does not exist in the system
     * @throws DuplicateObjectException Thrown if the question already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     *                                  TODO ACCOUNT FOR QUESTION BEING ADMINISTERED BEFORE VERSIONING
     */
    public void updateQuestion(Question question)
            throws ObjectNotFoundException, DuplicateObjectException, CtdbException
    {
 		this.performUpdateQuestion(question, false);
	}

	/**
     * Updates a Question in the CTDB System with an option to force a version update
     *
     * @param question The question to update
     * @param versionFlag Option to force update or not
     * @throws ObjectNotFoundException  Thrown if the question does not exist in the system
     * @throws DuplicateObjectException Thrown if the question already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     *                                  TODO ACCOUNT FOR QUESTION BEING ADMINISTERED BEFORE VERSIONING
     */
 	public void updateQuestion(Question question, boolean versionFlag) throws ObjectNotFoundException, DuplicateObjectException, CtdbException
    {
		this.performUpdateQuestion(question, versionFlag);
	}

 	private void performUpdateQuestion(Question question, boolean versionFlag) throws ObjectNotFoundException, DuplicateObjectException, CtdbException
    {
 		Connection conn = null;
 		
        try
        {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);

            // determine what type of question creating
            if(question.getInstanceType().equals(InstanceType.QUESTION) ||
               question.getInstanceType().equals(InstanceType.IMAGE_MAP_QUESTION)
                    || question.getInstanceType().equals(InstanceType.VISUAL_SCALE_QUESTION)) //question.getInstanceType().equals(InstanceType.CALCULATED_QUESTION)
            {
                //int versionNum = dao.getQuestionVersion(question.getId());

                boolean editArchive = false;
                
                //if (versionFlag == false && question.getVersion().getVersionNumber() != versionNum) {
                    //editArchive = true;
                //}
                
                // creating base simple question
                dao.updateQuestion(question, versionFlag, editArchive);
                
                // create answers
                
                if(question.getAnswers() != null) {
                
	                dao.deleteAnswers(question.getId(), 1);
	                List<Answer> answers = question.getAnswers();
	                int answerOrderVal = 0;
	                
	                for ( Answer answer : answers )
	                {
	                    dao.createAnswer(question, answer, answerOrderVal++);
	                }
                }
                /*
                // associate with groups if any
                dao.deleteQuestionGroup(question.getId());
                
                if ( question.getGroupsAssociatedWith() != null && !question.getGroupsAssociatedWith().isEmpty() )
                {
                    for ( Group group : question.getGroupsAssociatedWith() )
                    {
                        dao.associateQuestionGroup(question.getId(), group.getId());
                    }
                }*/

                /* if ( question.getInstanceType().equals(InstanceType.IMAGE_MAP_QUESTION) ) {
                    // update the image map fields
                     if ( versionFlag ) {
                         dao.createImageMapQuestion((ImageMapQuestion)question);
                     }
                     else {
                        dao.updateImageMapQuestion ((ImageMapQuestion)question);
                     }

                    dao.deleteImageMapOptions (((ImageMapQuestion)question).getImageMapId());
                    
                    for ( Iterator<ImageMapOption> iter = ((ImageMapQuestion)question).getOptions().iterator(); iter.hasNext(); ) {
                        dao.createImageMapQuestionAnswer (((ImageMapQuestion)question).getImageMapId(), iter.next());
                    }
                }*/
                if ( question.getInstanceType().equals(InstanceType.VISUAL_SCALE_QUESTION) ) {
                     //if ( versionFlag ) {
                        // dao.createVisualScaleQuestion((VisualScale) question);
                     //}
                     //else {
                        dao.updateVisualScaleQuestion((VisualScale)question);
                     //}
                }
            }
            else
            {
                throw new CtdbException("Unkown Question Instance Type. Question could not be updated.");
            }

            this.commit(conn);
        }
        finally
        {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Retrieves all Answers for a Question from the system based on the unique identifiers.
     *
     * @param questionId The unique identifier of the Question to retrieve
     * @param questionVersion The question version for the answer
     * @return List of answer objects data object, empty list if no answers exist (This case
     *         should never occur)
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Answer> getAnswers(int questionId, int questionVersion) throws ObjectNotFoundException, CtdbException
    {
        Connection conn = null;
        
        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getAnswers(questionId, questionVersion);
        }
        finally
        {
            this.close(conn);
        }
    }

    public int getCurrentQuestionVersion (int questionId) throws ObjectNotFoundException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getQuestionVersion(questionId);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Updates question images in the CTDB System.
     *
     * @param questionImage The question image domain object that contains the images to update
     * @throws CtdbException Thrown if any errors occur while processing
     * @throws FileUploadException
     */
    public void updateQuestionImages(QuestionImage questionImage, String prefix) throws CtdbException, FileUploadException
    {
        Connection conn = null;
        List<String> fileswritten = new ArrayList<String>();
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            QuestionManagerDao questionDao = QuestionManagerDao.getInstance(conn);
            if(questionImage.getFiles()!=null && !questionImage.getFiles().isEmpty())
            {
	            int n = questionImage.getFiles().size();
	            
	            for ( int i = 0; i < n; i++ ) {
	            	questionDao.createQuestionImage(questionImage, prefix, i);
	
	                List<String> fileTypes = CtdbConstants.IMAGE_FORMATS;
	
	                fileswritten.add(FileUploader.uploadFile((File) questionImage.getFiles().get(i), 
	                		questionImage.getPath(), (String)questionImage.getNames().get(i), fileTypes));
	            }
            }

            // delete
            for ( String name : questionImage.getNamesToDelete() ) {
            	if(!CtdbConstants.DELETE_ALL_IMAGES.equals(name))
            	{
            		questionDao.deleteQuestionImage(questionImage.getId(), name);
            	}
            	else //delete all images of questionId
            	{
            		questionDao.deleteQuestionImages(questionImage.getId());
            	}
            }

            commit(conn);
        }
        catch (FileUploadException fue) {
            this.rollbackDeleteFiles(fileswritten);
            throw fue;
        }
        catch(CtdbException ce) {
            this.rollbackDeleteFiles(fileswritten);
            throw ce;
        }
        catch(Exception e) {
            this.rollbackDeleteFiles(fileswritten);
            throw new CtdbException("Unknown error occurred while creating question image: " + e.getMessage(), e);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    public void deleteQuestionImagesOnDB(QuestionImage questionImage, String prefix) throws CtdbException, FileUploadException
    {
        Connection conn = null;
        //List<String> fileswritten = new ArrayList<String>();
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            QuestionManagerDao questionDao = QuestionManagerDao.getInstance(conn);

            // delete
            for ( String name : questionImage.getNamesToDelete() ) {
            	if(!CtdbConstants.DELETE_ALL_IMAGES.equals(name))
            	{
            		questionDao.deleteQuestionImage(questionImage.getId(), name);
            	}
            	else //delete all images of questionId
            	{
            		questionDao.deleteQuestionImages(questionImage.getId());
            	}
            }

            commit(conn);
        }
/*        catch (FileUploadException fue) {
            this.rollbackDeleteFiles(fileswritten);
            throw fue;
        }*/
        catch(CtdbException ce) {
            //this.rollbackDeleteFiles(fileswritten);
            throw ce;
        }
        catch(Exception e) {
            //this.rollbackDeleteFiles(fileswritten);
            throw new CtdbException("Unknown error occurred while deleting question image: " + e.getMessage(), e);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    public void createQuestionImagesOnDB(QuestionImage questionImage, String prefix) throws CtdbException, FileUploadException
    {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            QuestionManagerDao questionDao = QuestionManagerDao.getInstance(conn);
            if(questionImage.getNames()!=null && !questionImage.getNames().isEmpty())
            {
	            int n = questionImage.getNames().size();	            
	            for ( int i = 0; i < n; i++ ) {
	            	questionDao.createQuestionImageForImport2(questionImage, prefix, i);
	
	            }
            }

            commit(conn);
        }
/*        catch (FileUploadException fue) {
            this.rollbackDeleteFiles(fileswritten);
            throw fue;
        }*/
        catch(CtdbException ce) {
//            this.rollbackDeleteFiles(fileswritten);
            throw ce;
        }
        catch(Exception e) {
//            this.rollbackDeleteFiles(fileswritten);
            throw new CtdbException("Unknown error occurred while creating question image: " + e.getMessage(), e);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    /**
     * Updates question images in the CTDB System.
     *
     * @param questionImage The question image domain object that contains the images to update
     * @throws FileUploadException
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void updateQuestionImagesForExportImport(QuestionImage questionImage, String prefix) throws FileUploadException, CtdbException
    {
        Connection conn = null;
        List<String> fileswritten = new ArrayList<String>();
        
        try
        {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            QuestionManagerDao questionDao = QuestionManagerDao.getInstance(conn);
            int n = questionImage.getNames().size();
            
            for ( int i = 0; i < n; i++ )
            {
            	questionDao.createQuestionImageForImport(questionImage, prefix, i);

                List<String> fileTypes = CtdbConstants.IMAGE_FORMATS;

                fileswritten.add(FileUploader.uploadForExportImport((BufferedImage) questionImage.getBufferedImage().get(i), 
                		questionImage.getPath(), (String) questionImage.getNames().get(i), fileTypes));
            }

            commit(conn);
        }
        catch(FileUploadException fue)
        {
            this.rollbackDeleteFiles(fileswritten);
            throw fue;
        }
        catch(CtdbException ce)
        {
            this.rollbackDeleteFiles(fileswritten);
            throw ce;
        }
        catch(Exception e)
        {
            this.rollbackDeleteFiles(fileswritten);
            throw new CtdbException("Unknown error occurred while creating question image: " + e.getMessage(), e);
        }
        finally
        {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /************************************************************************
     * GET QUESTION METHODS
     ***********************************************************************/

    /**
     * Retrieves a Question from the system based on the unique identifier.
     *
     * @param questionId The unique identifier of the Question to retrieve
     * @return Question data object
     * @throws ObjectNotFoundException Thrown if the question does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Question getQuestion(int questionId) throws ObjectNotFoundException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getQuestion(questionId);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves a Question from the system based on the unique identifier with the meta data only.
     *
     * @param questionId The unique identifier of the Question to retrieve
     * @return Question data object
     * @throws ObjectNotFoundException Thrown if the question does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Question getMiniQuestion(int questionId) throws ObjectNotFoundException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getMiniQuestion(questionId);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves a Question for the section with the meta data only.
     *
     * @param questionId The unique identifier of the Question to retrieve
     * @param sectionId the section that has the question attached
     * @return Question data object
     * @throws ObjectNotFoundException Thrown if the question does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Question getMiniSectionQuestion(int questionId, int sectionId)
            throws ObjectNotFoundException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getMiniSectionQuestion(questionId, sectionId);
        }
        finally
        {
            this.close(conn);
        }
    }

    public Question getQuestion(int questionId, int questionVersion) throws ObjectNotFoundException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getQuestion(questionId, questionVersion);
        }
        finally
        {
            this.close(conn);
        }
    }
    
    
    /**
     * gets the question id based on question name
     * 
     * question name is in syntax of formid_dataElementName
     * 
     */
    public int getQuestionId(String questionName) throws  CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getQuestionId(questionName);
        }
        finally
        {
            this.close(conn);
        }
    }

	public void removeSkipRule(int questionId, int questionVersion, int sectionId) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			QuestionManagerDao.getInstance(conn).removeSkipRule (questionId, questionVersion, sectionId);
			this.commit(conn);
		}
		finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
    
    public boolean isSkipRuleDependent (int sectionId, int questionId) throws CtdbException {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
            return dao.isSkipRuleDependent (sectionId,questionId) ;
        }
        finally
        {
            this.close(conn);
        }   	
    }

    /**
     * Uses Question id to lacate the latest version of the Question and Question version to check if the Question is the newest version.
     * @param question
     * @return true if question is the newest version and false if not
     * @throws CtdbException thrown if any errors occur while processing
     */
    public boolean isQuestionNewestVersion(Question question) throws CtdbException
    {
        Connection conn = null;
        int newestVersionNumber = Integer.MIN_VALUE;
        
        try
        {
            conn = CtdbManager.getConnection();
            newestVersionNumber = QuestionManagerDao.getInstance(conn).getQuestionVersion(question.getId());
        }
        finally
        {
            this.close(conn);
        }
        
        return !(question.getVersion().getVersionNumber() != newestVersionNumber);
    }

    /**
     * Checks if the question is used on any forms.  Uses ID and versionNumber of the
     * domain object to check to see if it is used.  If the versionNumber
     * in the domain object is not set (equals to Integer.MIN_VALUE), then the
     * current version of the question in the data base is used for the check.
     *
     * @param    question the question object to check
     * @return   if the question with the version has been used on any forms
     * @throws   CtdbException   thrown if any errors occur while processing
     */
    public boolean isAttachedToForm(Question question) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
            return dao.isAttachedToForm(question);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Checks if the question is administered.  Uses ID and versionNumber of the
     * domain object to check to see if it is administered.  If the versionNumber
     * in the domain object is not set (equals to Integer.MIN_VALUE), then the
     * current version of the question in the data base is used for the check.
     *
     * @param    question the question object to check
     * @return   if the question with the version has been administered
     * @throws   CtdbException   thrown if any errors occur while processing
     */
    public boolean isAdministered(Question question) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
            return dao.isAttachedToForm(question);
        }
        finally
        {
            this.close(conn);
        }
    }

    public boolean isAttachedAndAdministered(Question question) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection();
			QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
			return dao.isAdministered(question);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	public boolean isAdministeredInSection (Question question, int sectionId) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection();
			QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
			return dao.isAdministeredInSection(question, sectionId);
		}
		finally
		{
			this.close(conn);
		}
	}

    /**
     * Retrieves a Question from the system based on the unique identifier.
     * Only question meta data are retrieved.  This is light weight method
     * to get a question compared with the heavy weight method getQuestion().
     * @param questionId The unique identifier of the Question to retrieve
     * @return Question data object with only question meta data.
     * @throws ObjectNotFoundException Thrown if the question does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Question getQuestionMetaData(int questionId) throws ObjectNotFoundException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getQuestionMetaData(questionId);
        }
        finally
        {
            this.close(conn);
        }
    }
    
    /**
     * Gets a listing of questions that are not of textblock type. Then converts the results to a JSON array, 
     * which is formatted as an array of questions entries that are also stored as a JSON array.
     * 
     * @param excludeList - A list of question IDs to exclude from the returned JSON array
     * @return A string representation of the JSON arrays containing data to be displayed on a DisplayTable.
     * @throws CtdbException When a connection to the database could not be established
     * @throws SQLException	When a database connection occurs.
     */
    public String getQuestionLibraryAsJSON(List<Long> excludeList) throws CtdbException, SQLException
    {
    	String json = "[]";
    	Connection conn = getConnection();
    	
    	try
    	{
    		json = QuestionManagerDao.getInstance(conn).getQuestionLibraryAsJSON(excludeList);
    	}
    	finally
    	{
    		close(conn);
    	}
    	
    	return json;
    }

    /**
     * Retrieves all questions in the question library in the system. Only name and id
     * for a question are retrieved.
     *
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionIdNames() throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getQuestionIdNames();
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all questions in the question library in the system.
     *
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestions() throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getQuestions();
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all questions in the question library excluding the list of questions
     * passed in.  Only question name and id are retirived form a question.
     *
     * @param exclude List of questions to exclude
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
	public List<Question> getQuestionIdNamesExcluding(List<Question> exclude) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();

            return QuestionManagerDao.getInstance(conn).getQuestionIdNamesExcluding(exclude);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all questions in the question library excluding the list of questions
     * passed in.
     *
     * @param exclude List of questions to exclude
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionsExcluding(List<Question> exclude) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();

            return QuestionManagerDao.getInstance(conn).getQuestionsExcluding(exclude);
        }
        finally
        {
            this.close(conn);
        }
    }


    /**
     * Retrieves all questions in the question library excluding the list of questions
     * passed in.
     *
     * @param exclude List of questions to exclude
     * @param type The AnswerType of the questions to return
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionsExcluding(List<Question> exclude, AnswerType type, int formid) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();

            return QuestionManagerDao.getInstance(conn).getQuestionsExcluding(exclude, type, formid);
        }
        finally
        {
            this.close(conn);
        }
    }


    /**
     * This is a filter function for the getAllQuestionsExcluding function. It removes all duplicate questions; however,
     * it specifies no preference for which
     *
     * @param exclude
     * @param type
     * @param formid
     * @return
     * @throws CtdbException
     */
    public List<Question> getDistinctQuestionsExcluding(List<Question> exclude, AnswerType type, int formid) throws CtdbException {

        List<Question> list = this.getQuestionsExcluding(exclude, type, formid);
        HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
        
        for ( Iterator<Question> i = list.iterator(); i.hasNext(); ) {
            Question question = i.next();
            Integer qId = new Integer(question.getId());
            
            if ( hm.containsKey(qId) ) {
            	i.remove();
            }
            else {
                hm.put(qId, qId);
            }
        }

        return list;
    }

    /**
     * Retrieves all questions in the list passed in to include
     *
     * @param include List of questions to include
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionsIncluding(List<Question> include) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getQuestionsWithVersionsIncluding(include);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Get all questions attached to the sections on the form in the list passed in to include
     *
     * @param include  The list of questions to include
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException  Thrown if any errors occur while processing
     */
    public List<Question> getSectionQuestionsIncluding(List<Question> include) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getSectionQuestionsIncluding(include);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all questions in the list passed in to include
     *
     * @param include List of questions to include
     * @param type The AnswerType of the questions to return
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionsIncluding(List<Question> include, AnswerType type) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();

            return QuestionManagerDao.getInstance(conn).getQuestionsIncluding(include, type);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves the questions in the form based on the array of IDs
     *
     * @param form The Form to retrieve questions for
     * @param questionIds The array of IDs to bring back
     * @return The map of questiona in the form. The map will be empty if no
     *         questions exist in the form.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public Map<Integer, Question> getQuestionsFromForm(Form form, int[] questionIds) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();

            Map<Integer, Question> questionMap = new HashMap<Integer, Question>();
            QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);

            for( int idx = 0; idx < questionIds.length; idx++ )
            {
            	Question q = dao.getQuestionsInForm(form.getId(), questionIds[idx]);
            	questionMap.put(new Integer(q.getId()), q);
            }

            return questionMap;
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves the questions in the form based on the array of IDs
     *
     * @param form The Form to retrieve questions for
     * @param questionIds The array of IDs to bring back
     * @return The list of questions in the form. The list will be empty if no
     *         questions exist in the form.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionsInForm(Form form, int[] questionIds) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();

            List<Question> questions = new ArrayList<Question>();
            QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);

            for( int idx = 0; idx < questionIds.length; idx++ )
            {
                questions.add(dao.getQuestionsInForm(form.getId(), questionIds[idx]));
            }

            return questions;
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all versions of a question stored. A version consists
     * of modifications to the metadata about a question.
     *
     * @param questionId The unique identifier of the Question to retrieve
     * @return A list of all versions for a single question. The list
     *         will be ordered by versions such that index 0 will be the first
     *         question version. If the question does not exist, an empty
     *         list will be returned.
     * @throws CtdbException Thrown if any errors occur
     */
    public List<Question> getQuestionVersions(int questionId) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getQuestionVersions(questionId);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Return all questions at the protocol level assigned to the Form Section.
     *
     * @param section The form section to get questions for
     * @return The list of questions in the form section. The list will be empty if no
     *         questions exist in the form section.
     * @throws CtdbException Thrown if any errors occur
     */
    public List<Question> getQuestions(Section section) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getQuestions(section);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Return all questions at the protocol level assigned to the Form Section.
     * Only question meta data are retrieved.  This is light weight method
     * to get a question compared with the heavy weight method getQuestions().
     * @param section The form section to get questions for
     * @return The list of questions in the form section. The list will be empty if no
     *         questions exist in the form section.
     * @throws CtdbException Thrown if any errors occur
     */
    public List<Question> getMiniQuestions(Section section) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getMiniQuestions(section);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Return all questions at the protocol level assigned to the form section of a versioned form.
     *
     * @param section The form section to get questions for
     * @param formVersion The form version to get questions for
     * @return The list of questions in the form section. The list will be empty if no
     *         questions exist in the form section.
     * @throws CtdbException Thrown if any errors occur
     */
    public List<Question> getQuestions(Section section, int formVersion) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getQuestions(section, formVersion);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all images associated with a question.
     *
     * @param questionId The unique identifier of the Question to retrieve the images
     * @return A list of all image names.  If the question does not exist an empty
     *         list will be returned.
     * @throws CtdbException Thrown if any errors occur
     */
    public List<String> getQuestionImages(int questionId) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getQuestionImages(questionId);
        }
        finally
        {
            this.close(conn);
        }
    }

    /************************************************************************
     * QUESTION GROUP METHODS
     ***********************************************************************/

    /**
     * Creates a Question Group in the CTDB System.
     *
     * @param group The question group to create
     * @throws DuplicateObjectException Thrown if the question group already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void createGroup(Group group) throws DuplicateObjectException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            QuestionManagerDao.getInstance(conn).createGroup(group);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Updates a Question Group in the CTDB System.
     *
     * @param group The question group to update
     * @throws ObjectNotFoundException  Thrown if the question group does not exist in the system
     * @throws DuplicateObjectException Thrown if the question group already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void updateGroup(Group group)
            throws ObjectNotFoundException, DuplicateObjectException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            QuestionManagerDao.getInstance(conn).updateGroup(group);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves a Question Group from the system based on the unique identifier.
     *
     * @param groupId The unique identifier of the Question Group to retrieve
     * @return Group data object
     * @throws ObjectNotFoundException Thrown if the question group does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Group getGroup(int groupId) throws ObjectNotFoundException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getGroup(groupId);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all questions groups in the system.
     *
     * @return The list of question groups in the system. The list will be empty if no
     *         question groups exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Group> getGroups() throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getGroups();
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all questions groups in the system based on GroupResultControl object with searching and sorting.
     *
     * @param rc GroupResultControl object which determines searching and sorting of list
     * @return The list of question groups in the system. The list will be empty if no
     *         question groups exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Group> getGroups(GroupResultControl rc) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getGroups(rc);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all questions groups in the system excluding the list of groups
     * passed in.
     *
     * @param exclude List of groups to exclude
     * @return The list of question groups in the system. The list will be empty if no
     *         question groups exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Group> getGroupsExcluding(List<Group> exclude) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getGroupsExcluding(exclude);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all questions groups in the list to include
     *
     * @param include List of groups to include
     * @return The list of question groups in the system. The list will be empty if no
     *         question groups exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Group> getGroupsIncluding(List<Group> include) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getGroupsIncluding(include);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all images associated with a question group.
     *
     * @param groupId The unique identifier of the Group to retrieve
     * @return A list of all image names.  If the question does not exist an empty
     *         list will be returned.
     * @throws CtdbException Thrown if any errors occur
     */
    public List<String> getGroupImages(int groupId) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getGroupImages(groupId);
        }
        finally
        {
            this.close(conn);
        }
    }
    
    /**
     * Associates an array of question ids a question group..
     *
     * @param questionIds The array of question ids to be associated to the group
     * @param groupId    The group unique identifier to be associated to the question ids
     * @throws CtdbException Thrown if any errors occur
     */
	public void associateQuestionsToGroup(int groupId, String[] questionIds) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);

            for ( int i = 0; i < questionIds.length; i++ )
            {
            	dao.associateQuestionGroup(Integer.parseInt(questionIds[i]), groupId);
            }
            
            this.commit(conn);
        }
        finally
        {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    /**
     * Associates the group to the question.
     *
     * @param questionId The question unique identifier to associate the group to
     * @param groupId    The group unique identifier to be associated to the question
     * @throws CtdbException Thrown if any errors occur
     */
    public void associateQuestionGroup(int questionId, int groupId) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            QuestionManagerDao.getInstance(conn).associateQuestionGroup(questionId, groupId);
            this.commit(conn);
        }
        finally
        {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Retrieves all skip rule questions for a question in the section
     *
     * @param questionId  the question for the list of skip rule questions
     * @param sectionId  the section that the question attached to
     * @return The list of questions on the form. The list will be empty if no
     *         questions exist on the form.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getSkipQuestions(int questionId, int sectionId) throws CtdbException
    {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).getSkipQuestions(questionId, sectionId);
        }
        finally
        {
            this.close(conn);
        }
    }
    
    public String disableSkipRule(int formId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).disableSkipRule(formId) ;
        }
        finally
        {
            this.close(conn);
        }   	
    }

    /**
     * Retrieves all calculate questions for a question in the section
     *
     * @param cfqa the CalcFormQuesAttrs
     * @param sectionId   the section that the question attached to
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
     public List<Question> getCalculateQuestions(CalculatedFormQuestionAttributes cfqa, int sectionId) throws CtdbException
     {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();

            List<String> questionSectionIds = new ArrayList<String>();

            if ( cfqa.getCalculation() == null || cfqa.getCalculation().length() < 1 ) {
                return new ArrayList<Question>();
            }

            Pattern p = Pattern.compile("\\[(S_([0-9]+)_Q_([0-9]+))\\]");
            Matcher m = p.matcher(cfqa.getCalculation());
            
            while ( m.find() ) {
                String found = m.group(0);
                questionSectionIds.add(found);
            }

            return QuestionManagerDao.getInstance(conn).getCalculateQuestions(questionSectionIds, sectionId);
        }
        finally
        {
            this.close(conn);
        }
     }

    public boolean hasSkipRuleOnCurrentForm (Question q, String sectionId)throws CtdbException
	{
	    Connection conn = null;
	    
	    try {
	        conn = CtdbManager.getConnection();
	        return QuestionManagerDao.getInstance(conn).hasSkipRuleOnCurrentForm(q, sectionId);
	    }
	    finally
	    {
	        this.close(conn);
	    }
	}
    
    public boolean hasSkipRuleOnOtherForms (Question q, String sectionId)throws CtdbException
     {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).hasSkipRuleOnOtherForms(q, sectionId);
        }
        finally
        {
            this.close(conn);
        }
     }

    /**
     * This method rolls back the deleted image files when errors occur during the processing of creating and
     * updating the question images.
     *
     * @param files the list of files need to roll back
     * @throws CtdbException Thrown if any errors occur
     */
    private void rollbackDeleteFiles(List<String> files) throws CtdbException
    {
        try
        {
            int n = files.size();
            for(int i = 0; i < n; i++)
            {
                String name = files.get(i);
                File file = new File(name);
                if(!file.delete())
                {
                    throw new CtdbException("Unable to delete file: " + name);
                }
            }
        }
        catch(Exception e)
        {
            throw new CtdbException("Unknown error occurred in rollbackDeleteFiles: " + e.getMessage(), e);
        }
    }

    public boolean questionNameExists (String questionName) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return QuestionManagerDao.getInstance(conn).questionNameExists(questionName);
        }
        finally {
            this.close(conn);
        }
    }

    public String getImageMapSequence () throws CtdbException {
        return QuestionManagerDao.getInstance(CtdbManager.getConnection()).getImageMapSequence();
    }

    static {
        removeWords.add("a");
        removeWords.add("the");
        removeWords.add("of");
        removeWords.add("and");
        removeWords.add("for");
        removeWords.add("A");
        removeWords.add("THE");
        removeWords.add("OF");
        removeWords.add("AND");
        removeWords.add("FOR");
        removeWords.add("an");
        removeWords.add("AN");
    }
    
    /************************************************************************
     * DELETE QUESTION METHODS       added by Ching Heng
     ***********************************************************************/
    public boolean isAttached(int questionId, int questionVersion) throws CtdbException {
    	Connection conn = null;
    	
    	try{
    		conn = CtdbManager.getConnection();
    		return QuestionManagerDao.getInstance(conn).isAttached(questionId, questionVersion);
    	}
    	finally {
    		this.close(conn);
    	}
    }
    
    /**
     *  check if question is being used on another form
     * @param questionId
     * @param formid
     * @return
     * @throws CtdbException
     */
    public boolean isAttachedOnAnotherForm(int questionId, int formid) throws CtdbException {
    	Connection conn = null;
    	
    	try{
    		conn = CtdbManager.getConnection();
    		return QuestionManagerDao.getInstance(conn).isAttachedOnAnotherForm(questionId, formid);
    	}
    	finally {
    		this.close(conn);
    	}
    }
    
    
    
    
    public void deleteQuestions(int questionId, int questionVersion) throws CtdbException, SQLException {
    	Connection conn = null;
    	
    	try{
    		conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		dao.deleteQuestion(questionId, questionVersion);
    		commit(conn);
    	}
    	finally{
    		this.rollback(conn);
    		this.close(conn);
    	}
    }
    
    public void deleteCopiedQuestions(int questionId) throws CtdbException, SQLException {
    	Connection conn = null;
    	
    	try {
    		conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		dao.deleteCopiedQuestion(questionId);
    		commit(conn);
    	}
    	finally {
    		this.rollback(conn);
    		this.close(conn);
    	}
    }
    
   /* public int copyQuestion(int orgQuestionId) throws CtdbException, SQLException {
    	int newQuestionId = 0;
    	Connection conn = null;
    	
    	try {
    		conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		newQuestionId = dao.copyQuestion(orgQuestionId);
    		commit(conn);
    	}
    	finally {
    		this.rollback(conn);
    		this.close(conn);
    	}
    	
    	return newQuestionId;
    }*/
    
    //creates a new question based on an original question
    //the question name will be formId_dataElementName
    public int copyQuestionNewFormBuilder(int orgQuestionId, String newQuestionName) throws CtdbException, SQLException {
    	int newQuestionId = 0;
    	Connection conn = null;
    	
    	try {
    		conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		newQuestionId = dao.copyQuestionNewFormBuilder(orgQuestionId, newQuestionName);
    		commit(conn);
    	}
    	finally {
    		this.rollback(conn);
    		this.close(conn);
    	}
    	
    	return newQuestionId;
    }
    
    public int copyNonCopyRightQuestion(int orgQuestionId) throws CtdbException, SQLException {
    	int newQuestionId = 0;
    	Connection conn = null;
    	
    	try {
    		conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		newQuestionId = dao.copyNonCopyRightQuestion(orgQuestionId);
    		commit(conn);
    	}
    	finally {
    		this.rollback(conn);
    		this.close(conn);
    	}
    	
    	return newQuestionId;
    }   
    
    /*public void copyQuestionByfromId(int formId) throws CtdbException{
    	Connection conn = null;
    	
    	try{
    		conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		dao.copyQuestionByfromId(formId);
    		commit(conn);
    	}
    	finally {
    		this.rollback(conn);
    		this.close(conn);
    	}    	
    }*/
    
    public void updateCopiedQuestion(int formId,int orgQuestionId,int newQuestionId,int questionAttributeId) throws CtdbException {
    	Connection conn = null;
    	
    	try {
    		conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		dao.updateCopiedQuestion(formId,orgQuestionId,newQuestionId,questionAttributeId);
    		commit(conn);
    	}
    	finally {
    		this.rollback(conn);
    		this.close(conn);
    	}   	
    }
    	  
    
    /************************************************************************
     * get METHODS       added by Ching-Heng
     ***********************************************************************/
    public Set<Integer> getAttachedFormIds (int questionId, int questionVersion) throws CtdbException, SQLException {
    	Connection conn = null;
    	Set<Integer> attachedFormIds = new HashSet<Integer>();
    	
    	try {
    		conn = CtdbManager.getConnection();
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		attachedFormIds = dao.getAttachedForm(questionId, questionVersion);
    	}
    	finally {
    		this.close(conn);
    	}
    	
    	return attachedFormIds;
    }
    
    public Set<String> getCalDependentFormNames (int questionId) throws CtdbException, SQLException {
    	Connection conn = null;
    	Set<String> attachedFormNames = new HashSet<String>();
    	
    	try {
    		conn = CtdbManager.getConnection();
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		attachedFormNames = dao.getCalDependentFormNames(questionId);
    	}
    	finally {
    		this.close(conn);
    	}
    	
    	return attachedFormNames;
    }

    public String getImageMapFileNameByquestionId (int questionId, int questionVersion) throws CtdbException, SQLException {
    	Connection conn = null;
    	String imageMapFileName = "";
    	
    	try {
    		conn = CtdbManager.getConnection();
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		imageMapFileName = dao.getImageMapFileName(questionId, questionVersion);
    	}
    	finally {
    		this.close(conn);
    	}
    	
    	return imageMapFileName;
    }
    
    /**
     * Will return the ImageMap object for given questionId and questionVersion
     * @param questionId
     * @param questionVersion
     * @return
     * @throws CtdbException
     * @throws SQLException
     */
    public ImageMapExportImport getImageMapExport (int questionId, int questionVersion) throws CtdbException, SQLException {
    	Connection conn = null;
    	ImageMapExportImport iMap= new ImageMapExportImport();
    	
    	try {
    		conn = CtdbManager.getConnection();
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		iMap = dao.getImageMapExport(questionId, questionVersion);
    	}
    	finally {
    		this.close(conn);
    	}
    	
    	return iMap;
    }
    
    public List<String> getImageMapOptions(int questionId, int questionVersion) throws CtdbException, SQLException {
    	Connection conn = null;
    	List<String> imageMapOptions = new ArrayList<String>();
    	
    	try {
    		conn = CtdbManager.getConnection();
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		imageMapOptions = dao.getImageMapOptions(questionId, questionVersion);
    	}
    	finally {
    		this.close(conn);
    	}
    	
    	return imageMapOptions;
    }
    
    public List<ImageMapValuesExportImport> getImageMapValuesList(int questionId, int questionVersion) throws CtdbException {
    	Connection conn = null;
    	List<ImageMapValuesExportImport> iMapValuesList = new ArrayList<ImageMapValuesExportImport>();
    	
    	try {
    		conn = CtdbManager.getConnection();
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		iMapValuesList = dao.getImageMapValuesList(questionId, questionVersion);
    	}
    	finally {
    		this.close(conn);
    	}
    	
    	return iMapValuesList;
    }
    
    public String getVisualScaleInfo(int questionId, int questionVersion) throws CtdbException, SQLException {
    	Connection conn = null;
    	String VStexts = "";
    	
    	try {
    		conn = CtdbManager.getConnection();
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		VStexts = dao.getVisualScaleInfo(questionId, questionVersion);
    	}
    	finally {
    		this.close(conn);
    	}
    	
    	return VStexts;
    }
    	
    /**
     * Will return the VisualScale object for given questionId and questionVersion
     * @param questionId
     * @param questionVersion
     * @return
     * @throws CtdbException
     * @throws SQLException
     */
    public VisualScale getVisualScale(int questionId, int questionVersion) throws CtdbException, SQLException {
		Connection conn = null;
		VisualScale vs = new VisualScale();
		
		try {
			conn = CtdbManager.getConnection();
			QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
			vs = dao.getVisualScale(questionId, questionVersion);
		}
		finally {		
			this.close(conn);
		}
		
		return vs;
    }
    
    public List<String> getAssociatedGroupIds(int questionId) throws CtdbException {
    	Connection conn = null;
    	List<String> associatedGroupIds = new ArrayList<String>();
    	
    	try {
    		conn = CtdbManager.getConnection();
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		associatedGroupIds = dao.getAssociatedGroupIds(questionId);
    	}
    	finally {
    		this.close(conn);
    	}
    	
    	return associatedGroupIds;
    }
    
    public boolean isNameExist(String name) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();

            QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
            return dao.isNameExist(name);
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Gets the next ID of the question table.
     * 
     * @return The ID that would be set to a new question entry.
     * @throws CtdbException	When the next ID could not be determined.
     */
    public int getNextQuestionId() throws CtdbException {
    	Connection conn = CtdbManager.getConnection();
    	int nextId = -1;
    	
    	try {
    		QuestionManagerDao dao = QuestionManagerDao.getInstance(conn);
    		nextId = dao.getNextQuestionId();
    	}
    	finally {
    		this.close(conn);
    	}
    	
    	return nextId;
    }
}
