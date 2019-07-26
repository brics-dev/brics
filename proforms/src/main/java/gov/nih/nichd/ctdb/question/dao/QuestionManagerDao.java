package gov.nih.nichd.ctdb.question.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.postgresql.util.PSQLException;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.question.common.GroupResultControl;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.Group;
import gov.nih.nichd.ctdb.question.domain.ImageMapExportImport;
import gov.nih.nichd.ctdb.question.domain.ImageMapOption;
import gov.nih.nichd.ctdb.question.domain.ImageMapQuestion;
import gov.nih.nichd.ctdb.question.domain.ImageMapValuesExportImport;
import gov.nih.nichd.ctdb.question.domain.InstanceType;
import gov.nih.nichd.ctdb.question.domain.PatientCalendarQuestion;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionImage;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleOperatorType;
import gov.nih.nichd.ctdb.question.domain.VisualScale;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * QuestionManagerDao interacts with the Data Layer for the QuestionManager.
 * The only job of the DAO is to manipulate the data layer.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionManagerDao extends CtdbDao {
    /**
     * Private Constructor to hide the instance
     * creation implementation of the QuestionManagerDao object
     * in memory. This will provide a flexible architecture
     * to use a different pattern in the future without
     * refactoring the QuestionManager.
     */
    private QuestionManagerDao() {

    }

    /**
     * Method to retrieve the instance of the QuestionManagerDao.
     *
     * @return QuestionManagerDao data object
     */
    public static synchronized QuestionManagerDao getInstance() {
        return new QuestionManagerDao();
    }

    /**
     * Method to retrieve the instance of the QuestionManagerDao. This method
     * accepts a Database Connection to be used internally by the DAO. All
     * transaction management will be handled at the BusinessManager level.
     *
     * @param conn Database connection to be used within this data object
     * @return QuestionManagerDao data object
     */
    public static synchronized QuestionManagerDao getInstance(Connection conn) {
        QuestionManagerDao dao = new QuestionManagerDao();
        dao.setConnection(conn);
        return dao;
    }

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
    
    public void createQuestion(Question question) throws DuplicateObjectException, CtdbException {
    	createQuestion(question,false);
    }
    	  
    
    public void createQuestion(Question question, Boolean copyright) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;
        
        try {
            StringBuffer sql = new StringBuffer(100);
            sql.append("insert into question( ");
            sql.append(this.getBaseInsertColumns());
            sql.append(") values( ");
            sql.append(this.getBaseInsertValues());
            sql.append(")");

            stmt = this.conn.prepareStatement(sql.toString());

           if ( question.getId() <= 0 ) {
                // question has no current ID, assign new one
                question.setId(this.getNextSequenceValue(conn, "question_seq"));
            }
           
            int colIndx = this.setBaseInsertData(question, stmt, 1);
            
            if ( copyright ) {
            	stmt.setLong(colIndx++, 1);
            }
            else {
            	stmt.setLong(colIndx++, 0);
            }
            
            stmt.setString(colIndx++,question.getDescriptionUp());
            stmt.setString(colIndx++,question.getDescriptionDown());
            stmt.setBoolean(colIndx++, question.isIncludeOtherOption());
            stmt.setString(colIndx++, question.getHtmltext());
            question.setVersion(new Version(1));
            stmt.executeUpdate();
            
            if(question.getImageHolder() !=null && !question.getImageHolder().getNames().isEmpty()){
            	// Question ID must be set in order to save images to question image table
            	question.getImageHolder().setId( question.getId());
            }
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateObjectException("A question with the name " + question.getName() + " already exists in the system: " + e.getMessage(), e);
            }
            else {
                throw new CtdbException("Unable to create new question with name " + question.getName() + " : " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateObjectException("A question with the name " + question.getName() + " already exists in the system: " + e.getMessage(), e);
            }
            else {
                throw new CtdbException("Unable to create new question: " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
        }
    }
    
    public void createImageMapQuestion(ImageMapQuestion question) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(100);
            sql.append("insert into imagemap ");
            sql.append(" (imagemapid, questionid, questionversion, resolutionsid, imagefilename, height, width, displaygrid )");
            sql.append(" values ");
            sql.append(" (DEFAULT, ?, ?, ?, ?, ?, ?, ?)");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, question.getId());
            stmt.setInt(2, question.getVersion().getVersionNumber());
            stmt.setLong(3, question.getGridResolution());
            stmt.setString(4, question.getImageFileName());
            stmt.setInt(5, (int) question.getHeightInt());
            stmt.setInt(6, (int) question.getWidthInt());
            stmt.setBoolean(7, question.isShowGrid());
            stmt.executeUpdate();
            question.setImageMapId(this.getInsertId(conn, "imagemap_seq"));
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to create new image map question: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }
    
    public void createImageMapQuestionForImport(ImageMapExportImport iMap) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(100);
            sql.append("insert into imagemap ");
            sql.append(" (imagemapid, questionid, questionversion, resolutionsid, imagefilename, height, width, displaygrid )");
            sql.append(" values ");
            sql.append(" (DEFAULT, ?, ?, ?, ?, ?, ?, ?)");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, iMap.getId());
            stmt.setInt(2, iMap.getVersion());
            stmt.setLong(3, iMap.getGridResolution());
            stmt.setString(4, iMap.getImageFileName());
            stmt.setInt(5,  iMap.getHeight());
            stmt.setInt(6, iMap.getWidth());
            stmt.setBoolean(7, iMap.isShowGrid());
            stmt.executeUpdate();
            iMap.setImageMapId(this.getInsertId(conn, "imagemap_seq"));
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to create new image map question: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    public void createVisualScaleQuestion(VisualScale question) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(100);
            sql.append("insert into visualscale ");
            sql.append(" ( questionid, questionversion, startrange, endrange, widthmm ");
            sql.append ("  , lefttext, righttext, centertext, showhandle ) ");
            sql.append(" values ");
            sql.append(" (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt = this.conn.prepareStatement(sql.toString());

            stmt.setLong(1, question.getId());
            stmt.setInt(2, question.getVersion().getVersionNumber());
            stmt.setInt(3, question.getRangeStart());
            stmt.setInt(4, question.getRangeEnd());
            stmt.setInt(5, question.getWidth());
            stmt.setString(6, question.getLeftText());
            stmt.setString(7, question.getRightText());
            stmt.setString(8, question.getCenterText());
            stmt.setBoolean(9, question.isShowHandle());
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to create new visual scale question: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    public void updateVisualScaleQuestion(VisualScale question) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;
        
        try {
            StringBuffer sql = new StringBuffer(100);
            sql.append("update visualscale ");
            sql.append("set startrange = ?, endrange = ?, widthmm = ?, lefttext = ?, righttext = ?, ");
            sql.append( "centertext = ?, showhandle = ? ");
            sql.append("where questionid = ? and questionversion = ? ");
            stmt = this.conn.prepareStatement(sql.toString());

            stmt.setInt(1, question.getRangeStart());
            stmt.setInt(2, question.getRangeEnd());
            stmt.setInt(3, question.getWidth());
            stmt.setString(4, question.getLeftText());
            stmt.setString(5, question.getRightText());
            stmt.setString(6, question.getCenterText());
            stmt.setBoolean(7, question.isShowHandle());

            stmt.setLong(8, question.getId());
            stmt.setInt(9, question.getVersion().getVersionNumber());
            if (stmt.executeUpdate() < 1) {
                // failed, must have not had any row
                this.createVisualScaleQuestion(question);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update visual scale question: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Deletes the answers for the question.  This is alwasy done for the most recent question.
     *
     * @param questionId The question unique identifier
     * @throws CtdbException Thrown if any errors occur
     */
    public void deleteAnswers(int questionId, int questionVersion) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "delete FROM questionanswer where questionid = ? and questionversion = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, questionId);
            stmt.setInt(2, questionVersion);
            
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to delete answers for question " + questionId + " : " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Creates the answer for the question.
     *
     * @param question The Question object to create the answer for
     * @param answer   The Answer object to create
     * @param orderVal The answer order value
     * @throws DuplicateObjectException Thrown if the question answer already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur
     */
    public void createAnswer(Question question, Answer answer, int orderVal)
            throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {

            StringBuffer sql = new StringBuffer(200);
            sql.append("insert into questionanswer(questionid, questionversion, answerid, display, codevalue, orderval, score, submittedvalue)");
            sql.append("values(?, ?, DEFAULT, ?, ?, ?, ?, ?) ");

            stmt = this.conn.prepareStatement(sql.toString());

            stmt.setLong(1, question.getId());

            stmt.setInt(2, 1);
            stmt.setString(3, answer.getDisplay());
            stmt.setString(4, answer.getCodeValue());
            stmt.setInt(5, orderVal);
            
            if (answer.getScore() != Integer.MIN_VALUE) {
            	stmt.setDouble(6, answer.getScore());
            }
            else {
            	stmt.setNull(6, java.sql.Types.NUMERIC);
            }
            
            stmt.setString(7, answer.getSubmittedValue());

            stmt.executeUpdate();
            answer.setId(this.getInsertId(conn, "questionanswer_seq"));
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateObjectException("An answer with the same display already exists in the system: " + e.getMessage(), e);
            }
            else {
                throw new CtdbException("Unable to create new answer in the system: " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateObjectException("An answer with the same display already exists in the system: " + e.getMessage(), e);
            }
            else {
                throw new CtdbException("Unable to create new answer in the system: " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
        }
    }

    public void createImageMapQuestionAnswer(int imageMapId, ImageMapOption answer) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "insert into imagemapvalues (imageMapId, imagerow, imagecolumn, imageoption, codevalue) values (?, ?, ?, ?, ?) ";
            stmt = this.conn.prepareStatement(sql);
            
            for (Iterator<String> iter = answer.getCoordinates().keySet().iterator(); iter.hasNext();) {
                String row = iter.next();
                List<String> cols = answer.getCoordinates().get(row);
                
                for (String col : cols ) {
                    stmt.setLong(1, imageMapId);
                    stmt.setInt(2, Integer.parseInt(row));
                    stmt.setInt(3, Integer.parseInt(col));
                    stmt.setString(4, answer.getOption());
                    stmt.setString(5, answer.getValue()); //will be null
                    stmt.executeUpdate();
                }
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to create new Image Map answer in the system: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }
    
    /***
     * Method to create imageMapValues while doing form export import
     * @param imageMapId
     * @param answer
     * @throws DuplicateObjectException
     * @throws CtdbException
     */
	public void createImageMapQuestionAnswerForExportImport(int imageMapId, ImageMapValuesExportImport answer) 
			throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		
		try {
			String sql = "insert into imagemapvalues (imageMapId, imagerow, imagecolumn, imageoption, codevalue) values (?, ?, ?, ?, NULL) ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, imageMapId);
			stmt.setInt(2, answer.getImageMapRow());
			stmt.setInt(3, answer.getImageMapColumn());
			stmt.setString(4, answer.getImageOption());
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to create new Image Map answer in the system: " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

    /**
     * Versions a question answers in the CTDB System.
     *
     * @param questionId      The question ID to version
     * @param questionVersion The question version to version
     * @throws DuplicateObjectException Thrown if the question already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void versionQuestionAnswer(int questionId, int questionVersion) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("insert into questionanswer select questionid, ");
            sql.append("questionversion+1 as questionversion, answerid,  ");
            sql.append("display, codevalue,  orderval, score ");
            sql.append("from questionanswer where questionid = ? and ");
            sql.append("questionversion = ? ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            stmt.setInt(2, questionVersion);

            stmt.executeUpdate();
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateObjectException("A question with the same version already exists in the system archive: " + e.getMessage(), e);
            }
            else {
                throw new CtdbException("Unable to version question answer: " + e.getMessage(), e);
            }
        } 
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateObjectException("A question with the same version already exists in the system archive: " + e.getMessage(), e);
            }
            else {
                throw new CtdbException("Unable to version question answer: " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
        }
    }


    /**
     * Versions a Question in the CTDB System.
     *
     * @param questionId The question ID to version
     * @throws DuplicateObjectException Thrown if the question already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void versionQuestion(int questionId) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "insert into questionarchive select * from question where questionid = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, questionId);

            stmt.executeUpdate();
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateObjectException("A question with the same version already exists in the system archive: " + e.getMessage(), e);
            }
            else {
                throw new CtdbException("Unable to version question: " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateObjectException("A question with the same version already exists in the system archive: " + e.getMessage(), e);
            }
            else {
                throw new CtdbException("Unable to version question: " + e.getMessage(), e);
            }
        }
        finally {
               this.close(stmt);
        }
    }

    /**
     * Deletes the skip rule questions based on the unique identifier question id
     *
     * @param questionId The question unique identifier
     * @throws CtdbException Thrown if any errors occur
     * @deprecated moved to QuestionManagerDao
     */
    public void deleteSkippedQuestions(int questionId) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("delete FROM skiprulequestion where questionid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);

            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to delete skip questions for question " + questionId + " : " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    public void removeSkipRule(int questionId, int questionVersion, int sectionId) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("update questionattributes set skipruleflag = 0, skipruletype = null, skipruleoperatortype = null, skipruleequals = null ");
            sql.append(" where questionattributesid = (select questionattributesid from sectionquestion where questionid = ? ");
            sql.append(" and questionversion = ? and sectionid = ?) ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            stmt.setInt(2, questionVersion);
            stmt.setLong(3, sectionId);

            stmt.executeUpdate();
            stmt.close();

            sql = new StringBuffer(200);
            sql.append("delete from skiprulequestion where questionid = ? and sectionid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            stmt.setLong(2, sectionId);

            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to delete skiprule for question " + questionId + " : " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }
    
    /**
     * Deletes the question group in the system for the question
     *
     * @param questionId The question unique identifier to delete its group
     * @throws CtdbException Thrown if any errors occur
     */
    public void deleteQuestionGroup(int questionId) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "delete FROM questiongrpquestion where questionid = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, questionId);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to delete question groups for question " + questionId + " : " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    public void deleteImageMapOptions(int imageMapId) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "delete FROM imagemapvalues where imagemapid = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, imageMapId);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to delete image map values for imagemap " + imageMapId + " : " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Associates the group to the question.
     *
     * @param questionId The question unique identifier to associate the group to
     * @param groupId    The group unique identifier to be associated to the question
     * @throws CtdbException Thrown if any errors occur
     */
    public void associateQuestionGroup(int questionId, int groupId) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "insert into questiongrpquestion (questiongroupid, questionid) values (?, ?) ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, groupId);
            stmt.setLong(2, questionId);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
        	throw new CtdbException("Unable to associate question with group: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Deletes the calculated questions for the question
     *
     * @param questionId The question unique identifier to delete its calculated questions for
     * @throws CtdbException Thrown if any errors occur
     */
    public void deleteCalculatedQuestions(int questionId) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "delete FROM calculatequestion where questionid = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, questionId);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to delete calculated questions for question " + questionId + " : " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Associates the calculated question to the base question
     *
     * @param baseQuestionId       The base question unique identifier to associate the calculated question for
     * @param calculatedQuestionId The calculated question unique identifier to be associated to
     * @param orderVal             The calculated question order value
     * @throws DuplicateObjectException Thrown if the calculated question is already associated to the question
     * @throws CtdbException            Thrown if any other errors occur
     */
    public void associateCalculatedQuestions(int baseQuestionId, int calculatedQuestionId, int orderVal)
            throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "insert into calculatequestion (questionid, calculatequestionid, orderval) values (?, ?, ?) ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, baseQuestionId);
            stmt.setLong(2, calculatedQuestionId);
            stmt.setInt(3, orderVal);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to associate calculated question: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Creates question images in the system.
     *
     * @param questionImage The question image object
     * @param prefix        The prefix string for the file name
     * @param i             The index to the image to create
     * @throws CtdbException Thrown iff any errors occur while processing
     */
    public void createQuestionImage(QuestionImage questionImage, String prefix, int i) throws CtdbException {
        File file = questionImage.getFiles().get(i);
        
        if (file == null) {
            return;
        }
        else if (file.length() == 0) {
        	return;
        }
        
        PreparedStatement stmt = null;
        
        try {
            String sql = "insert into questionimage (questionid, imagefilename) values (?, ?) ";
            
            stmt = this.conn.prepareStatement(sql.toString());
            int id = this.getNextSequenceValue(this.conn, "questionimage_seq");
            
            stmt.setLong(1, questionImage.getId());  // question ID
            
            String name = this.getImageFileName(prefix, id, (String) questionImage.getNames().get(i));
            questionImage.getNames().set(i, name);
            
            stmt.setString(2, (String) questionImage.getNames().get(i));
            
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to create question image in questionimage table: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }
    
    public void createQuestionImageForImport(QuestionImage questionImage, String prefix, int i) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "insert into questionimage (questionid, imagefilename) values (?, ?) ";

            stmt = this.conn.prepareStatement(sql);
            int id = this.getNextSequenceValue(this.conn, "questionimage_seq");

            stmt.setLong(1, questionImage.getId());  // question ID
            
            String name = this.getImageFileName(prefix, id, (String) questionImage.getNames().get(i));
            questionImage.getNames().set(i, name);

            stmt.setString(2, (String) questionImage.getNames().get(i));

            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to create question image in questionimage table: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }
    
    public void createQuestionImageForImport2(QuestionImage questionImage, String prefix, int i) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "insert into questionimage (questionid, imagefilename) values (?, ?) ";

            stmt = this.conn.prepareStatement(sql);
            //int id = this.getNextSequenceValue(this.conn, "questionimage_seq");

            stmt.setLong(1, questionImage.getId());  // question ID
            
            //String name = this.getImageFileName(prefix, id, (String) questionImage.getNames().get(i));
            //questionImage.getNames().set(i, name);

            stmt.setString(2, (String) questionImage.getNames().get(i));

            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to create question image in questionimage table: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Delete a question image from the system.
     *
     * @param questionId The question ID
     * @param name       The image name
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void deleteQuestionImage(int questionId, String name) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "delete FROM questionimage where questionid = ? and imagefilename = ? ";
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, questionId);
            stmt.setString(2, name);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to delete question image in questionimage table: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }
    
    public void deleteQuestionImages(int questionId) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "delete FROM questionimage where questionid = ?";
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, questionId);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to delete question image in questionimage table: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /************************************************************************
     * UPDATE QUESTION METHODS
     ***********************************************************************/

    /**
     * Updates a Question in the CTDB System.
     *
     * @param question    The question to update
     * @param versionFlag True if this update should update the version, false otherwise
     * @throws ObjectNotFoundException  Thrown if the question does not exist in the system
     * @throws DuplicateObjectException Thrown if the question already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void updateQuestion(Question question, boolean versionFlag, boolean editArchive)
            throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            String tableName = " question ";
            
            StringBuffer sql = new StringBuffer(100);
            sql.append("update " + tableName);
            sql.append(this.getBaseUpdateColumnsValues(versionFlag));
            sql.append(" where questionid = ? ");

            stmt = this.conn.prepareStatement(sql.toString());

            int colNum = this.setBaseUpdateData(question, stmt);
            stmt.setLong(colNum, question.getId());

            int rowsUpdated = stmt.executeUpdate();
            
            if (rowsUpdated == 0) {
                throw new ObjectNotFoundException("Question with ID = " + question.getId() + " does not exist in the system.");
            }
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateObjectException("A question with the name " + question.getName() + " already exists in the system: " + e.getMessage(), e);
            }
            else {
                throw new CtdbException("Unable to update question with ID = " + question.getId() + ": " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateObjectException("A question with the name " + question.getName() + " already exists in the system: " + e.getMessage(), e);
            }
            else {
                throw new CtdbException("Unable to update question with ID = " + question.getId() + ": " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
        }
    }

    public void updateImageMapQuestion(ImageMapQuestion question) throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(100);
            sql.append("update imagemap set  resolutionsid = ?, height = ?, width = ?, displaygrid = ? ");
            sql.append("where imagemapid = ? and questionversion = ? ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, question.getGridResolution());
            stmt.setInt(2, (int) question.getHeightInt());
            stmt.setInt(3, (int) question.getWidthInt());
            stmt.setBoolean(4, question.isShowGrid());
            stmt.setInt(5, question.getImageMapId());
            stmt.setInt(6, question.getVersion().getVersionNumber());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                throw new ObjectNotFoundException("Image Map Question with ID = " + question.getId() + " does not exist in the system.");
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update question with ID = " + question.getId() + ": " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Gets extra information associated with questions
     *
     * @param question The main question object
     * @throws ObjectNotFoundException Thrown if the question does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur during the process
     */
    private void getQuestionExtraInfo(Question question) throws ObjectNotFoundException, CtdbException {
        try {
            int questionId = question.getId();
            question.setAnswers(this.getAnswers(questionId, question.getVersion().getVersionNumber()));
            question.setGroupsAssociatedWith(this.getQuestionGroups(questionId));
            question.setImages(this.getQuestionImages(questionId));
        }
        catch (Exception e) {
            throw new CtdbException("Unable to get question extra info : " + e.getMessage(), e);
        }
    }

    /**
     * gets extra information associated with questions
     *
     * @param question
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    private void getQuestionExtraInfo(Question question, int version) throws ObjectNotFoundException, CtdbException {
        try {
            int questionId = question.getId();
            question.setAnswers(this.getAnswers(questionId, version));
            question.setGroupsAssociatedWith(this.getQuestionGroups(questionId));
            question.setImages(this.getQuestionImages(questionId));
        }
        catch (Exception e) {
            throw new CtdbException("Unable to get question extra info : " + e.getMessage(), e);
        }
    }

    /************************************************************************
     * GET QUESTION METHODS
     ***********************************************************************/

    /**
     * Retrieves a the most recent Question from the system based on the unique identifier.
     *
     * @param questionId The unique identifier of the Question to retrieve
     * @return Question data object
     * @throws ObjectNotFoundException Thrown if the question does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Question getQuestion(int questionId) throws ObjectNotFoundException, CtdbException {
    	Question question = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "select q.* from question q where q.questionid = ? ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, questionId);

            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new ObjectNotFoundException("The question with ID = " + questionId + " does not exist in the system.");
            }

            question = this.rsToQuestion(rs);
            question.setAnswers(this.getAnswers(questionId));
            question.setGroupsAssociatedWith(this.getQuestionGroups(questionId));

            question.setImages(this.getQuestionImages(questionId));

            if (question.getInstanceType().equals(InstanceType.IMAGE_MAP_QUESTION)) {

                getImageMapQuestion((ImageMapQuestion) question);
            }
            else if (question.getInstanceType().equals(InstanceType.VISUAL_SCALE_QUESTION)){
                getVisualScale((VisualScale)question);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get question with ID = " + questionId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return question;
    }

    /**
     * Retrieves a Question from the system based on the unique identifier with the meta data only.
     *
     * @param questionId The unique identifier of the Question to retrieve
     * @return Question data object
     * @throws ObjectNotFoundException Thrown if the question does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Question getMiniQuestion(int questionId) throws ObjectNotFoundException, CtdbException {
    	Question question = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select q.* from question q where q.questionid = ? ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, questionId);
            
            rs = stmt.executeQuery();
            
            if ( !rs.next() ) {
                throw new ObjectNotFoundException("The question with ID = " + questionId + " does not exist in the system.");
            }

            question = this.rsToQuestion(rs);
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get question with ID = " + questionId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return question;
    }

    /**
     * Retrieves a Question for the section with the meta data only.
     *
     * @param questionId The unique identifier of the Question to retrieve
     * @param sectionId  the section that has the question attached
     * @return Question data object
     * @throws ObjectNotFoundException Thrown if the question does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Question getMiniSectionQuestion(int questionId, int sectionId) throws ObjectNotFoundException, CtdbException {
    	Question question = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select q.* from question_view q, sectionquestion sq ")
                .append("where q.questionid = ? ")
                .append("and q.questionid = sq.questionid ")
                .append("and sq.sectionid = ? ")
                .append("and q.version = sq.questionversion ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            stmt.setLong(2, sectionId);

            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new ObjectNotFoundException("The question with ID = " + questionId + " in the section " + 
                	sectionId + " does not exist in the system.");
            }

            question = this.rsToQuestion(rs);
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get the question with ID = " + questionId + " for the section "
                    + sectionId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return question;
    }

     public void getVisualScale(VisualScale q) throws ObjectNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select * from visualScale where questionid = ? and questionversion = ? ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, q.getId());
            stmt.setInt(2, q.getVersion().getVersionNumber());

            rs = stmt.executeQuery();
            
            if ( rs.next() ) {
	            q.setWidth(rs.getInt("widthmm"));
	            q.setRangeEnd(rs.getInt("endRange"));
	            q.setRangeStart(rs.getInt("startRange"));
	            q.setLeftText(rs.getString("leftText"));
	            q.setRightText(rs.getString("righttext"));
	            q.setCenterText(rs.getString("centertext"));
	            q.setShowHandle(Boolean.parseBoolean(rs.getString("showhandle")));
            }
            else {
            	throw new ObjectNotFoundException("Could not find the visual scale for question ID = " + q.getId() + 
            		" and question version = " + q.getVersion().getVersionNumber());
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get Visual scale question with ID = " + q.getId() + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public void getImageMapQuestion(ImageMapQuestion q) throws ObjectNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select * from imagemap where questionid = ? and questionversion = ? ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, q.getId());
            stmt.setLong(2, q.getVersion().getVersionNumber());
            
            rs = stmt.executeQuery();
            
            if ( rs.next() ) {
	            q.setImageMapId(rs.getInt("imagemapid"));
	            q.setImageFileName(rs.getString("imagefilename"));
	            q.setGridResolution(rs.getInt("resolutionsid"));
	            q.setHeight(rs.getString("height"));
	            q.setWidth(rs.getString("width"));
	            q.setShowGrid(rs.getBoolean("displaygrid"));
	            getImageMapAnswers(q);
            }
            else {
            	throw new ObjectNotFoundException("Could not find the image map for question ID = " + q.getId() + 
                		" and question version = " + q.getVersion().getVersionNumber());
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get image map info for question with ID = " + q.getId() + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public void getImageMapAnswers(ImageMapQuestion q) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select * from imagemapvalues where imagemapid = ? order by imageoption ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, q.getImageMapId());

            rs = stmt.executeQuery();

            List<ImageMapOption> answers = new ArrayList<ImageMapOption>();
            try {
                rs.next(); // next the rs to start

                theAnswerLoop:
                while (true) {
                    ImageMapOption imo = new ImageMapOption();
                    imo.setOption(rs.getString("imageoption"));
                    imo.setValue(rs.getString("codevalue"));

                    theCoordinatesLoop:
                    while (true) {
                        String row = rs.getString("imagerow");
                        if (imo.getCoordinates().get(row) != null) {
                            imo.getCoordinates().get(row).add(rs.getString("imagecolumn"));
                        } else {
                            // no entry into coordinates for this row yet
                            List<String> al = new ArrayList<String>();
                            al.add(rs.getString("imagecolumn"));
                            imo.getCoordinates().put(row, al);
                        }
                        if (!rs.next()) {
                            answers.add(imo);
                            break theAnswerLoop;
                        }
                        if (rs.getString("imageoption").equals(imo.getOption())) {
                            // still the same option
                            continue theCoordinatesLoop;
                        } else {
                            // new option  continue to create a new option obj
                            answers.add(imo);
                            continue theAnswerLoop;
                        }
                    }
                }

            } catch (SQLException sqle) {
                if (sqle.getMessage().indexOf("exhausted") < 0) {
                    throw sqle;
                }
            }
            // the exception was due to the rs next() going too far, which means fininshed
            q.setOptions(answers);
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get image map info for question with ID = " + q.getId() + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }


    /**
     * Retrieves a Question from the system based on the unique identifier and version.
     *
     * @param questionId The unique identifier of the Question to retrieve
     * @param version    The version to retrieve
     * @return Question data object
     * @throws ObjectNotFoundException Thrown if the question does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Question getQuestion(int questionId, int version) throws ObjectNotFoundException, CtdbException {
    	Question question = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select q.* from question_view q where q.questionid = ? and q.version = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, questionId);
            stmt.setInt(2, version);

            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new ObjectNotFoundException("The question with ID = " + questionId + " does not exist in the system.");
            }

            question = this.rsToQuestion(rs);
            question.setAnswers(this.getAnswers(question, version));
            question.setGroupsAssociatedWith(this.getQuestionGroups(questionId));
            question.setImages(this.getQuestionImages(questionId));
            question.setHasDecimalPrecision(false);
            question.setHasCalDependent(false);
            question.setPrepopulation(false);
            
            // Set skip parent.
            FormQuestionAttributes attrs = question.getFormQuestionAttributes();
            attrs.setHasSkipRule(this.isSkipParent(questionId, version));
            
            if (question.getInstanceType().equals(InstanceType.IMAGE_MAP_QUESTION)) {
                getImageMapQuestion((ImageMapQuestion) question);
            }
            else if (question.getInstanceType().equals(InstanceType.VISUAL_SCALE_QUESTION)){
                getVisualScale((VisualScale)question);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get question with ID = " + questionId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return question;
    }
    
    /**
     * Gets the question id based on question name
     * 
     * question name is in syntax of formid_dataelementname
     *
     */
    public int getQuestionId(String questionName) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int questionId = -1;
        
        try {
            String sql = "select q.questionid from question_view q where q.name = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, questionName);

            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new ObjectNotFoundException("The question with Name = " + questionName + " does not exist in the system.");
            }
            
            questionId = rs.getInt("questionid");
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get question with Name = " + questionName + ": " + e.getMessage(), e);
        }
        catch (ObjectNotFoundException e) {
        	return -1;
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return questionId;
    }

    /**
     * Retrieves a Question's metadata from the system based on the unique identifier.
     *
     * @param questionId The unique identifier of the Question to retrieve
     * @return Question data object containing only metadata information
     * @throws ObjectNotFoundException Thrown if the question does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Question getQuestionMetaData(int questionId) throws ObjectNotFoundException, CtdbException {
    	Question question = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select question_view.* from question_view where questionid = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, questionId);

            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new ObjectNotFoundException("The question with ID = " + questionId + " does not exist in the system.");
            }

            question = this.rsToSimpleQuestion(rs);
            
            if (question.getInstanceType().equals(InstanceType.IMAGE_MAP_QUESTION)) {
                getImageMapQuestion((ImageMapQuestion) question);
            }
            else if (question.getInstanceType().equals(InstanceType.VISUAL_SCALE_QUESTION)){
                getVisualScale((VisualScale)question);
            }
            else {
                List<Answer> answers = this.getAnswers(questionId);
                question.setAnswers(answers);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get protocol question with ID = " + questionId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return question;
    }

    /**
     * Retrieves a Question's metadata from the system based on the unique identifier.
     *
     * @param questionId The unique identifier of the Question to retrieve
     * @return Question data object containing only metadata information
     * @throws ObjectNotFoundException Thrown if the question does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Question getQuestionMetaData(int questionId, int questionVersion) throws ObjectNotFoundException, CtdbException {
    	Question question = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select question_view.* from question_view where questionid = ? and version = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, questionId);
            stmt.setInt(2, questionVersion);

            rs = stmt.executeQuery();
            
            if (!rs.next()) {
                throw new ObjectNotFoundException("The question with ID = " + questionId + " does not exist in the system.");
            }

            question = this.rsToSimpleQuestion(rs);
            
            if (question.getInstanceType().equals(InstanceType.IMAGE_MAP_QUESTION)) {
                getImageMapQuestion((ImageMapQuestion) question);
            }
            else if (question.getInstanceType().equals(InstanceType.VISUAL_SCALE_QUESTION)){
                getVisualScale((VisualScale)question);
            }
            else {
                List<Answer> answers = this.getAnswers(questionId);
                question.setAnswers(answers);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get protocol question with ID = " + questionId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return question;
    }

    /**
     * Retrieves a list of Question's metadata from the system based on the unique identifier.
     *
     * @param map the container contains question id(key) and question version(value).
     * @return List of question objects containing only metadata information.
     * @throws ObjectNotFoundException Thrown if the question does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
	public List<Question> getQuestionMetaData(Map<Integer, Integer> map, List<Integer> list) throws ObjectNotFoundException, CtdbException {
		List<Question> qList = new ArrayList<Question>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int qId = Integer.MIN_VALUE;
        
        try {
            String sql = "select question_view.* from question_view where questionid = ? and version = ? ";

            stmt = this.conn.prepareStatement(sql);
            
            for ( Integer key : list ) {
                Integer version = map.get(key);

                stmt.setInt(1, key.intValue());
                stmt.setInt(2, version.intValue());
                qId = key.intValue();
                rs = stmt.executeQuery();

                if (!rs.next()) {
                    throw new ObjectNotFoundException("The question with ID = " + qId + " does not exist in the system.");
                }

                Question question = this.rsToSimpleQuestion(rs);
                qList.add(question);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get protocol question with ID = " + qId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return qList;
    }

    /**
     * Retrieves all Answers for a Question from the system based on the unique identifier.
     * Use the most current version.
     *
     * @param q The unique identifier of the Question to retrieve
     * @return List of answer objects data object, empty list if no answers exist (This case
     *         should never occur)
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Answer> getAnswers(Question q) throws ObjectNotFoundException, CtdbException {
        int questionVersion = this.getQuestionVersion(q.getId());
        return getAnswers(q, questionVersion);
    }

    public List<Answer> getAnswers(Question q, int questionVersion) throws ObjectNotFoundException, CtdbException {
        List<Answer> l = this.getAnswers(q.getId(), questionVersion);
        return l;
    }

    /**
     * Retrieves all Answers for a Question from the system based on the unique identifier.
     * Use the most current version.
     *
     * @param questionId The unique identifier of the Question to retrieve
     * @return List of answer objects data object, empty list if no answers exist (This case
     *         should never occur)
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Answer> getAnswers(int questionId) throws ObjectNotFoundException, CtdbException {
        int questionVersion = this.getQuestionVersion(questionId);
        return this.getAnswers(questionId, questionVersion);
    }


    /**
     * Retrieves all Answers for a Question from the system based on the unique identifiers.
     *
     * @param questionId      The unique identifier of the Question to retrieve
     * @param questionVersion The question version for the answer
     * @return List of answer objects data object, empty list if no answers exist (This case
     *         should never occur)
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Answer> getAnswers(int questionId, int questionVersion) throws ObjectNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select * from questionanswer where questionid = ? and questionversion = ? order by orderval");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            stmt.setInt(2, questionVersion);

            rs = stmt.executeQuery();

            List<Answer> answers = new ArrayList<Answer>();

            while (rs.next()) {
                answers.add(this.rsToAnswer(rs));
            }

            return answers;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get answers for question with ID = " + questionId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Retrieves all questions with name and id only in the question library in the system.
     *
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionIdNames() throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select questionid, name from question order by questionid");

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            List<Question> questions = new ArrayList<Question>();
            while (rs.next()) {
                Question q = new Question();
                q.setId(rs.getInt(1));
                q.setName(rs.getString(2));
                questions.add(q);
            }
            return questions;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all questions: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    /**
     * Gets a listing of questions from the database that are not of textblock type. Then converts the
     * results to a JSON array, which is formatted as an array of questions entries that are also stored
     * as a JSON array.
     * 
     * @param excludeList - A list of question IDs to exclude from the returned JSON array
     * @return	A string representation of the JSON arrays containing data to be displayed on a DisplayTable.
     * @throws SQLException	When a database error occurs.
     */
    public String getQuestionLibraryAsJSON(List<Long> excludeList) throws SQLException
    {
    	String json = "[]";
    	StringBuffer query = new StringBuffer(100);
    	
    	query.append("select questionid, version, name, text, type from question where type not in (9, 12) and copyright <> 1 ");
    	
    	// Check if the exclude list needs to be included
    	if ( !excludeList.isEmpty() )
    	{
    		query.append("and questionid not in (");
    		
    		for ( Iterator<Long> it = excludeList.iterator(); it.hasNext(); )
    		{
    			it.next();
    			query.append("?");
    			
    			// Check if a comma needs to be appended
    			if ( it.hasNext() )
    			{
    				query.append(", ");
    			}
    		}
    		
    		query.append(") ");
    	}
    	
    	query.append("order by name ");
    	
    	PreparedStatement stmt = conn.prepareStatement(query.toString());
        ResultSet rs = null;
        
        try
        {
        	// Add exclude values if needed
        	if ( !excludeList.isEmpty() )
        	{
        		int i = 1;
        		
        		for ( Long l : excludeList )
        		{
        			stmt.setLong(i, l.longValue());
        			i++;
        		}
        	}
			
			rs = stmt.executeQuery();
			
			// Build the JSON object.
        	JSONArray table = new JSONArray();
			
			while ( rs.next() )
			{
				// Add JSON row to the JSON table
				JSONArray row = new JSONArray();
				Version version = new Version(rs.getInt("version"));
				QuestionType type = QuestionType.getByValue(rs.getInt("type"));
				
				row.put("<input type=\"radio\" value=\"" + rs.getString("questionid") + "," + rs.getString("version") + "\"/>");
				row.put(rs.getLong("questionid"));
				row.put(rs.getString("name") + " (" + version.toString() + ")");
				row.put(rs.getString("text"));
				row.put(type.toString());
				
				table.put(row);
			}
			
			// Convert the JSON object to a string
			json = table.toString();
        }
        finally
        {
        	close(rs);
        	close(stmt);
        }
    	
    	return json;
    }

    /**
     * Retrieves all questions in the question library in the system.
     *
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestions() throws CtdbException {
    	List<Question> questions = new ArrayList<Question>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select q.* from question q order by upper(q.name) ";

            stmt = this.conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(this.rsToQuestion(rs));
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all questions: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return questions;
    }

    /**
     * Retrieves all questions in question library excluding the list of questions
     * passed in.  Only question id and name are retrieved for a given question.
     *
     * @param exclude List of questions to exclude
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionIdNamesExcluding(List<Question> exclude) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Question> questions = new ArrayList<Question>();

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select questionid, name from question ");

            if ( exclude != null && !exclude.isEmpty() ) {
                sql.append("where questionid not in (");
                
                // Populate the place holders for the in clause.
                for ( Iterator<Question> itr = exclude.iterator(); itr.hasNext(); ) {
                    sql.append("?");
                    
                    if ( itr.hasNext() ) {
                        sql.append(", ");
                    }
                }

                sql.append(") ");
            }
            
            sql.append("order by questionid ");
            
            stmt = this.conn.prepareStatement(sql.toString());
            
            // Populate the values of the in clause if needed.
            if ( exclude != null && !exclude.isEmpty() ) {
            	int n = 1;
            	
            	for ( Question q : exclude ) {
            		stmt.setLong(n, q.getId());
            		n++;
            	}
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                Question q = new Question();
                q.setId(rs.getInt(1));
                q.setName(rs.getString(2));
                questions.add(q);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all questions: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return questions;
    }

    /**
     * Retrieves all questions in question library excluding the list of questions
     * passed in.
     *
     * @param exclude List of questions to exclude
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionsExcluding(List<Question> exclude) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Question> questions = new ArrayList<Question>();

        try {
            StringBuffer sql = new StringBuffer(50);
            
            sql.append("select question.* from question ");

            // Check for any question IDs to exclude.
            if ( exclude != null && !exclude.isEmpty() ) {
                sql.append("where questionid not in (");
                
                // Populate the place holders for the in clause.
                for ( Iterator<Question> itr = exclude.iterator(); itr.hasNext(); ) {
                    sql.append("?");
                    
                    if ( itr.hasNext() ) {
                        sql.append(", ");
                    }
                }

                sql.append(") ");
            }
            
            // Append order by statement.
            sql.append("order by upper(name) ");

            stmt = this.conn.prepareStatement(sql.toString());
            
            // Populate the values for the in clause if needed.
            if ( exclude != null && !exclude.isEmpty() ) {
	            int n = 1;
	            
	            for ( Question q : exclude ) {
	            	stmt.setLong(n, q.getId());
	            	n++;
	            }
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(this.rsToSimpleQuestion(rs));
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all questions: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return questions;
    }

    /**
     * Retrieves all questions in the list to include
     *
     * @param include List of questions to include
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionsIncluding(List<Question> include) throws CtdbException {
    	List<Question> questions = new ArrayList<Question>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        if ( include != null && !include.isEmpty() ) {
        	return questions;
        }

        try {
            StringBuffer sql = new StringBuffer(100);
            
            sql.append("select question.* from question where question.questionid in (");
            
            // Populate the place holders for the in clause.
            for ( Iterator<Question> itr = include.iterator(); itr.hasNext(); ) {
                sql.append("?");
                
                if ( itr.hasNext() ) {
                    sql.append(", ");
                }
            }

            sql.append(") order by questionid ");

            stmt = this.conn.prepareStatement(sql.toString());
            
            // Populate the values for the in clause.
            int n = 1;
            
            for ( Question q : include ) {
            	stmt.setLong(n, q.getId());
            	n++;
            }
            
            rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(this.rsToSimpleQuestion(rs));
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all questions: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return questions;
    }

    /**
     * Retrieves all questions in the list to include taking into account whether the question
     * has an attached version or not.
     *
     * @param include List of questions to include
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionsWithVersionsIncluding(List<Question> include) throws CtdbException {
        List<Question> questions = new ArrayList<Question>();

        if (include != null && !include.isEmpty()) {
            for ( Question question : include ) {
                if (question.getVersion() == null) {
                    questions.add(getQuestion(question.getId()));
                } else {
                    questions.add(getQuestion(question.getId(), question.getVersion().getVersionNumber()));
                }
            }
        }
        return questions;
    }

    /**
     * Get all questions attached to the sections on the form in the list passed in to include
     *
     * @param include - The list of questions to include
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getSectionQuestionsIncluding(List<Question> include) throws CtdbException {
        List<Question> questions = new ArrayList<Question>();
        
        if (include != null && !include.isEmpty()) {
            for (Question question : include) {
                if (question.getVersion() == null) {
                    questions.add(getQuestion(question.getId()));
                }
                else {
                    questions.add(getQuestion(question.getId(), question.getVersion().getVersionNumber()));
                }
            }
        }
        return questions;
    }

    /**
     * Retrieves all questions that are not Calculated in question library excluding the list of questions
     * passed in.
     *
     * @param exclude List of questions to exclude
     * @param type    The AnswerType of the questions to return
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionsExcluding(List<Question> exclude, AnswerType type, int formid) throws CtdbException {
    	List<Question> questions = new ArrayList<Question>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            StringBuffer sql = new StringBuffer(50);
            
            // TODO unsure how this query works, have do to some testing in order to make sure it works properly
            sql.append("select distinct question.* , ' ' as status from ");
            sql.append(" question_view question, questionattributes, sectionquestion, ");
            sql.append("( select sectionquestion.questionid, sectionquestion.sectionid ");
            sql.append("from section, sectionquestion ");
            sql.append("where section.sectionid=sectionquestion.sectionid ");
            sql.append("and section.FORMID = ? ) questions ");
            sql.append("where questions.questionid = question.questionid ");
            sql.append("and questionattributes.QUESTIONID = questions.questionid ");
            sql.append(" and question.version = sectionquestion.questionversion ");
            sql.append("and questionattributes.QUESTIONATTRIBUTESID = sectionquestion.QUESTIONATTRIBUTESID ");
            sql.append("and sectionquestion.sectionid = questions.sectionid ");
            sql.append("and questionattributes.answertype = ? ");
            // can't use a calculated q in another calculated q - otherwise we can get some circular references
            sql.append("and questionattributes.calculatedflag = 0 ");
            // Text Box Only
            sql.append("and question.type = ? ");
            
            // Populate the place holders for the in clause if needed.
            if (exclude != null && !exclude.isEmpty()) {
                sql.append("and question.questionid not in (");
                
                for (Iterator<Question> itr = exclude.iterator(); itr.hasNext();) {
                    sql.append("?");
                    
                    if (itr.hasNext()) {
                        sql.append(", ");
                    }
                }

                sql.append(") ");
            }
            
            sql.append("order by upper(question.name)");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, formid);
            stmt.setInt(2, type.getValue());
            stmt.setInt(3, QuestionType.TEXTBOX.getValue());
            
            // Populate the values of the in clause if needed.
            if (exclude != null && !exclude.isEmpty()) {
            	int n = 4;
            	
            	for ( Question q : exclude ) {
            		stmt.setLong(n, q.getId());
            		n++;
            	}
            }
            
            rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(this.rsToSimpleQuestion(rs));
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all questions: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return questions;
    }

    /**
     * Retrieves all questions in the list to include
     *
     * @param include List of questions to include
     * @param type    The AnswerType of the questions to return
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getQuestionsIncluding(List<Question> include, AnswerType type) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Question> questions = new ArrayList<Question>();
        
        if (include != null && !include.isEmpty()) {
        	return questions;
        }

        try {
            StringBuffer sql = new StringBuffer(50);
            
            sql.append("select distinct question.* from question, questionanswer ");
            sql.append("where question.questionid = questionanswer.questionid ");
            sql.append("and question.version = questionanswer.questionversion ");
            sql.append("and questionanswer.answertype = ? ");
            sql.append("and question.questionid in (");
            
            // Populate the place holders for the in clause.
            for ( Iterator<Question> itr = include.iterator(); itr.hasNext(); ) {
                sql.append("?");
                
                if ( itr.hasNext() ) {
                    sql.append(", ");
                }
            }

            sql.append(") order by upper(name) ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, type.getValue());
            
            // Populate the values for the in clause.
            int n = 2;
            
            for ( Question q : include ) {
            	stmt.setLong(n, q.getId());
            	n++;
            }
            
            rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(this.rsToSimpleQuestion(rs));
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all questions: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return questions;
    }

    /**
     * Retrieves all skip rule questions for a question in the section
     *
     * @param questionId the question for the list of skip rule question
     * @param sectionId  the section that the question attached to
     * @return The list of questions on the form. The list will be empty if no
     *         questions exist on the form.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getSkipQuestions(int questionId, int sectionId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select sq.skipquestionid,sq.skipsectionid ");
            sql.append("from skiprulequestion sq, form, section ");
            sql.append("where sq.questionid = ? ");
            sql.append("and sq.sectionid = section.sectionid ");
            sql.append("and section.formid = form.formid ");
            sql.append("and section.sectionid = ? ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            stmt.setLong(2, sectionId);

            rs = stmt.executeQuery();

            List<Question> questions = new ArrayList<Question>();
            Question q = new Question();
            while (rs.next()) {
            	q = this.getQuestionMetaData(rs.getInt("skipquestionid"));
            	q.setSectionId(sectionId);
            	q.setSkipSectionId(rs.getInt("skipsectionid"));
                questions.add(q);
            }

            return questions;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all skip questions for question " + questionId
                    + " and on the section " + sectionId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    public String disableSkipRule(int formId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;    	
        String disableStr = "";
        
        try {        
	        StringBuffer sql = new StringBuffer(200);
	        sql.append("select sk.*,tb.skipruleoperatortype,tb.skipruleequals from skiprulequestion sk join ");
	        sql.append("(select sq.sectionid,sq.questionid,qa.skipruleoperatortype,qa.skipruleequals from  section s ");
	        sql.append(" join sectionquestion sq on s.sectionid = sq.sectionid");
	        sql.append(" join questionattributes qa on qa.questionattributesid = sq.questionattributesid");       
	        sql.append(" where s.formid = ? and qa.skipruletype = 2) tb ");
	        sql.append("on sk.sectionid = tb.sectionid and sk.questionid = tb.questionid"); 
	        
	        stmt = this.conn.prepareStatement(sql.toString());
	        stmt.setLong(1, formId);
	        rs = stmt.executeQuery();
	        
	        while ( rs.next() ) {
	        	String qNo = "S_" + rs.getString("sectionid") + "_Q_" + rs.getString("questionid");
	        	String skNo = "S_" + rs.getString("skipsectionid") + "_Q_" + rs.getString("skipquestionid");
	        	SkipRuleOperatorType operatorType=SkipRuleOperatorType.getByValue(rs.getInt("skipruleoperatortype"));
	        	String opType = operatorType.getDispValue();
	        	String value = rs.getString("skipruleequals");
	        	disableStr += "applyskiprule(document.getElementById('" + qNo + "'), ['" + skNo + "'], '" + opType + "','Disable','" + value + "');\n";
	        }
        }
        catch (SQLException e) {
	            throw new CtdbException("disableSkipRule ", e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return disableStr;
    }
    
    private List<Integer> findSectionQuestionId(String targetString) { //[sectionId, QuestionId]
    	List<Integer> sectionQuestionIds = new ArrayList<Integer>();
    	
    	if( (targetString != null) && (targetString.length() > 0) && (targetString.indexOf("S_") > -1) ) {	
    		int s_ind = targetString.indexOf("S_");   	
			int q_ind = targetString.indexOf("_Q");
			String secId = targetString.substring(s_ind+2, q_ind);
			String qId = targetString.substring(q_ind+3, targetString.length()-1);
			
			sectionQuestionIds.add(Integer.valueOf(secId));
			sectionQuestionIds.add(Integer.valueOf(qId));
    	}
    	
    	return sectionQuestionIds;
    }

    /**
     * 
     * @param questionSectionList
     * @param sectionId
     * @return
     * @throws CtdbException
     */
    public List<Question> getCalculateQuestions(List<String> questionSectionList, int sectionId) throws CtdbException {
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
        List<Question> questions = new ArrayList<Question>();
        
        if (questionSectionList.size() < 1) {
            return questions;
        }

        try {
        	String sql = "select answertype from questionattributes where questionattributesid = " +
        		"(select questionattributesid from sectionquestion where sectionid= ? and questionid = ?) ";
        	stmt = this.conn.prepareStatement(sql);
        	
            for ( String secQidString : questionSectionList ) {
            	List<Integer> sectionQuestion = findSectionQuestionId(secQidString);
            	
            	if ( !sectionQuestion.isEmpty() ) {
	            	int secId = sectionQuestion.get(0).intValue();
	            	int qId = sectionQuestion.get(1).intValue();
	            	
	                stmt.setLong(1, secId);
	                stmt.setLong(2, qId);
	                rs = stmt.executeQuery();
	                
	                while (rs.next()) {
	                	Question q = this.getQuestionMetaData(qId);
	                    q.getFormQuestionAttributes().setAnswerType(AnswerType.getByValue(rs.getInt("answertype")));
	                    q.setSectionId(secId);  //NISH
	                    questions.add(q);
	                }
	                
	                rs.close();
            	}
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all calculate questions for section " + sectionId
                    + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return questions;
    }

    /**
     * Retrieves all calculate questions for a question in the section
     *
     * @param questionId the question for the list of calculate questions
     * @param sectionId  the section that the question attached to
     * @return The list of questions in the system. The list will be empty if no
     *         questions exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getCalculateQuestions(int questionId, int sectionId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);

            sql.append("select calculatequestion.CALCULATEQUESTIONID, questionattributes.ANSWERTYPE ");
            sql.append("from calculatequestion, sectionquestion, questionattributes ");
            sql.append("where calculatequestion.CALCULATEQUESTIONID = sectionquestion.QUESTIONID ");
            sql.append("and calculatequestion.SECTIONID = sectionquestion.SECTIONID ");
            sql.append("and sectionquestion.QUESTIONATTRIBUTESID = questionattributes.QUESTIONATTRIBUTESID ");
            sql.append("and calculatequestion.questionid = ? and sectionquestion.sectionid = ? order by orderval ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            stmt.setLong(2, sectionId);

            rs = stmt.executeQuery();

            List<Question> questions = new ArrayList<Question>();

            while (rs.next()) {
                Question q = this.getQuestionMetaData(rs.getInt("calculatequestionid"));
                q.getFormQuestionAttributes().setAnswerType(AnswerType.getByValue(rs.getInt("answertype")));
                questions.add(q);
            }

            return questions;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all calculate questions for question " + questionId
                    + " on the section " + sectionId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    public boolean isSkipRuleDependent (int sectionId, int questionId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int num = 0;
        
        try {
            String sql = "select count(0) num from skiprulequestion where skipsectionid = ? and skipquestionid = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, sectionId);
            stmt.setLong(2, questionId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                num = rs.getInt("num");
            }
        }
        catch (SQLException e) {
            throw new CtdbException("SkipRuleDependentUnable to associate skipped question: " + e.getMessage(), e);
        }
        finally {
        	this.close(rs);
            this.close(stmt);
        }
        
        return num > 0;
    }

    /**
     * Retrieves all questions in the form based on search values in QuestionResultControl object
     *
     * @param formId     The form to retrieve the question from
     * @param questionId The question to retrieve
     * @return The question in the form.
     * @throws ObjectNotFoundException Thrown if the question/form combination does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Question getQuestionsInForm(int formId, int questionId) throws ObjectNotFoundException, CtdbException {
    	Question question =  null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            
            sql.append("select question_view.*, sectionquestion.suppressflag from question_view, section, sectionquestion ");
            sql.append("where section.sectionid = sectionquestion.sectionid ");
            sql.append("and sectionquestion.questionid = question_view.questionid ");
            sql.append("and sectionquestion.questionversion = question_view.version ");
            sql.append("and section.formid = ? ");
            sql.append("and question_view.questionid = ? ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, formId);
            stmt.setLong(2, questionId);

            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new ObjectNotFoundException("The form/question combination could not be found in the system.");
            }

            question = this.rsToSimpleSectionQuestion(rs);
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all questions: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return question;
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
    public List<Question> getQuestionVersions(int questionId) throws CtdbException {
    	List<Question> questions = new ArrayList<Question>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("SELECT question_view.*, created.username created_username, updated.username updated_username ")
                    .append("FROM question_view, usr created, usr updated ")
                    .append("WHERE question_view.createdby = created.usrid and question_view.updatedby = updated.usrid and questionid = ? ")
                    .append("order by version desc");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, questionId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Question question = this.rsToQuestion(rs);
                
                question.setUpdatedByUsername(rs.getString("updated_username"));
                question.setCreatedByUsername(rs.getString("created_username"));
                question.setAnswers(this.getAnswers(question.getId(), question.getVersion().getVersionNumber()));
                question.setGroupsAssociatedWith(this.getQuestionGroups(questionId));
                questions.add(question);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all questions: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return questions;
    }

    /**
     * 
     * 
     * @param section
     * @return
     * @throws CtdbException
     */
    public List<Question> getQuestions(Section section) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Question> questions = new ArrayList<Question>();
        
        try {
            StringBuffer sql = new StringBuffer(100);
            
            sql.append("select question.*, sectionquestion.suppressflag, questionorder, questionorder_col, questionattributes.dtconversionfactor ");
            sql.append("from question, sectionquestion, questionattributes ");
            sql.append("where sectionquestion.questionid = question.questionid ");
            sql.append("and sectionquestion.questionversion = question.version ");
            sql.append("and sectionquestion.questionattributesid = questionattributes.questionattributesid ");
            sql.append("and sectionquestion.sectionid = ? ");
            sql.append("union ");
            sql.append("select questionarchive.*, sectionquestion.suppressflag, questionorder, questionorder_col, questionattributes.dtconversionfactor ");
            sql.append("from questionarchive, sectionquestion, questionattributes ");
            sql.append("where sectionquestion.questionid = questionarchive.questionid ");
            sql.append("and sectionquestion.questionversion = questionarchive.version ");
            sql.append("and sectionquestion.questionattributesid = questionattributes.questionattributesid ");
            sql.append("and sectionquestion.sectionid = ? ");
            sql.append("order by questionorder ");
            
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, section.getId());
            stmt.setLong(2, section.getId());

            rs = stmt.executeQuery();

            while ( rs.next() ) {
                Question q = this.rsToSectionQuestion(rs);
                this.getQuestionExtraInfo(q, rs.getInt("version"));
                
                // Get the latest version of the question from the QUESTION table
                q.setLatestVersion(new Version(rs.getInt("version")));
                
                if (q.getType().equals(QuestionType.IMAGE_MAP)) {
                    getImageMapQuestion((ImageMapQuestion) q);
                }
                else if (q.getInstanceType().equals(InstanceType.VISUAL_SCALE_QUESTION)){
                     getVisualScale((VisualScale) q);
                }
                
                q.setSectionId(section.getId());
                q.setQuestionOrder(rs.getInt("questionOrder"));
                q.setQuestionOrderCol(rs.getInt("questionorder_col"));
                q.setParentSectionName(section.getName());
                questions.add(q);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all questions for the section with ID = " + section.getId() + ": "
                    + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return questions;
    }

    /**
     * Return all questions assigned to the form section from the current form.
     * Only question meta data are retrieved.  This is light weight method
     * to get a question compared with the heavy weight method getQuestions().
     *
     * @param section The form section to get questions for
     * @return The list of questions in the form section. The list will be empty if no
     *         questions exist in the form section.
     * @throws CtdbException Thrown if any errors occur
     */
    public List<Question> getMiniQuestions(Section section) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select questionid, questionversion from sectionquestion ");
            sql.append("where sectionid = ?");
            sql.append(" order by questionorder");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, section.getId());

            rs = stmt.executeQuery();

            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            List<Integer> list = new ArrayList<Integer>();

            while (rs.next()) {
                list.add(new Integer(rs.getInt("questionid")));
                map.put(new Integer(rs.getInt("questionid")), new Integer(rs.getInt("questionversion")));
            }

            return this.getQuestionMetaData(map, list);
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all questions for the section with ID = " + section.getId() + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Return all questions assigned to the form section from the versioned form.
     *
     * @param section     The form section to get questions for, which contains formId
     * @param formVersion The form version
     * @return The list of questions in the form section. The list will be empty if no
     *         questions exist in the form section.
     * @throws CtdbException Thrown if any errors occur
     */
    public List<Question> getQuestions(Section section, int formVersion) throws CtdbException {
    	List<Question> questions = new ArrayList<Question>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {

            StringBuffer sql = new StringBuffer(50);

            sql.append("select question.*, sectionquestion_view.suppressflag, questionorder ");
            sql.append("from question, sectionquestion_view ");
            sql.append("where question.questionid = sectionquestion_view.questionid ");
            sql.append("and question.version = sectionquestion_view.questionversion ");
            sql.append("and sectionquestion_view.sectionid = ? ");
            sql.append("and sectionquestion_view.formid = ? ");
            sql.append("and sectionquestion_view.formversion = ? ");
            sql.append("union ");
            sql.append("select questionarchive.*, sectionquestion_view.suppressflag, questionorder ");
            sql.append("from questionarchive, sectionquestion_view ");
            sql.append("where questionarchive.questionid = sectionquestion_view.questionid ");
            sql.append("and questionarchive.version = sectionquestion_view.questionversion ");
            sql.append("and sectionquestion_view.sectionid = ? ");
            sql.append("and sectionquestion_view.formid = ? ");
            sql.append("and sectionquestion_view.formversion = ? ");
            sql.append("order by questionorder ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, section.getId());
            stmt.setLong(2, section.getFormId());
            stmt.setInt(3, formVersion);
            stmt.setLong(4, section.getId());
            stmt.setLong(5, section.getFormId());
            stmt.setInt(6, formVersion);

            rs = stmt.executeQuery();

            while (rs.next()) {
                Question q = this.rsToSectionQuestion(rs);
                this.getQuestionExtraInfo(q);
                
                if (q.getType().equals(QuestionType.IMAGE_MAP)) {
                    getImageMapQuestion((ImageMapQuestion) q);
                }
                else if (q.getInstanceType().equals(InstanceType.VISUAL_SCALE_QUESTION)) {
                     getVisualScale((VisualScale)q);
                }
                
                questions.add(q);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all questions for the section with ID = " + section.getId() + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return questions;
    }


    /**
     * Retrieves all images associated with a question.
     *
     * @param questionId The unique identifier of the Question to retrieve the images
     * @return A list of all image names.  If the question does not exist an empty
     *         list will be returned.
     * @throws CtdbException Thrown if any errors occur
     */
    public List<String> getQuestionImages(int questionId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            List<String> imageFileNames = new ArrayList<String>();

            StringBuffer sql = new StringBuffer(500);

            sql.append("select questionid,imagefilename, TO_NUMBER(substr(imagefilename, instr(imagefilename,'_') + 1, instr(imagefilename,'.') - instr(imagefilename,'_') - 1),'999999999999999999') as imagenumber " +
            		   "from questionimage where questionid = ? ORDER BY imagenumber");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                imageFileNames.add(rs.getString("imagefilename"));
            }

            return imageFileNames;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to retrieve question image file names for question: " + questionId + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
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
    public void createGroup(Group group) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("insert into questiongroup(questiongroupid, name, description, ");
            sql.append("createdby, createddate, updatedby, updateddate) ");
            sql.append("values(DEFAULT, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP)");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setString(1, group.getName());
            stmt.setString(2, group.getDescription());
            stmt.setInt(3, group.getCreatedBy());
            stmt.setInt(4, group.getUpdatedBy());

            stmt.executeUpdate();
            group.setId(this.getInsertId(conn, "questiongroup_seq"));
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateObjectException("A group with the name " + group.getName() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new group: " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateObjectException("A group with the name " + group.getName() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new group: " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
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
            throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("update questiongroup set name = ?, description = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP ");
            sql.append("where questiongroupid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setString(1, group.getName());
            stmt.setString(2, group.getDescription());
            stmt.setLong(3, group.getUpdatedBy());
            stmt.setLong(4, group.getId());

            int rowsUpdated = 0;

            rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                throw new ObjectNotFoundException("The group with ID: " + group.getId() + " does not exist in the system.");
            }
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateObjectException("A group with the name " + group.getName() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new group: " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateObjectException("A group with the name " + group.getName() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new group: " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
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
    public Group getGroup(int groupId) throws ObjectNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select * from questiongroup where questiongroupid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, groupId);

            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new ObjectNotFoundException("The question group with ID = " + groupId + " does not exist in the system.");
            }

            return this.rsToGroup(rs);
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get question group with ID = " + groupId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Retrieves all questions groups in the system.
     *
     * @return The list of question groups in the system. The list will be empty if no
     *         question groups exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Group> getGroups() throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Group> groups = new ArrayList<Group>();

        try {
            String sql = "select * from questiongroup where name not like 'CABIG%' order by upper(name) ";

            stmt = this.conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                groups.add(this.rsToGroup(rs));
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all question groups: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return groups;
    }

    /**
     * Retrieves all questions groups in the system based on GroupResultControl object with searching and sorting.
     *
     * @param rc GroupResultControl object which determines searching and sorting of list
     * @return The list of question groups in the system. The list will be empty if no
     *         question groups exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Group> getGroups(GroupResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select * from questiongroup ");
            sql.append(rc.getSortString());

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            List<Group> groups = new ArrayList<Group>();
            while (rs.next()) {
                groups.add(this.rsToGroup(rs));
            }

            return groups;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all question groups: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Retrieves all questions groups in the system excluding the list of groups
     * passed in.
     *
     * @param exclude The list of groups to exclude
     * @return The list of question groups in the system. The list will be empty if no
     *         question groups exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Group> getGroupsExcluding(List<Group> exclude) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select * from questiongroup ");

            if (exclude != null && !exclude.isEmpty()) {
                sql.append("where questiongroupid not in ( ");

                for (Iterator<Group> itr = exclude.iterator(); itr.hasNext();) {
                	Group group = itr.next();
                    sql.append(group.getId());
                    if (itr.hasNext()) {
                        sql.append(" , ");
                    }
                }

                sql.append(" )");
            }

            sql.append("order by upper(name)");

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            List<Group> groups = new ArrayList<Group>();
            while (rs.next()) {
                groups.add(this.rsToGroup(rs));
            }

            return groups;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all question groups: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Retrieves all questions groups in the list to include
     *
     * @param include The list of groups to inclue
     * @return The list of question groups in the system. The list will be empty if no
     *         question groups exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Group> getGroupsIncluding(List<Group> include) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            List<Group> groups = new ArrayList<Group>();

            if (include != null && !include.isEmpty()) {
                StringBuffer sql = new StringBuffer(50);
                sql.append("select * from questiongroup ");
                sql.append("where questiongroupid in ( ");

                for ( Iterator<Group> itr = include.iterator(); itr.hasNext(); ) {
                	Group group = itr.next();
                    sql.append(group.getId());
                    if (itr.hasNext()) {
                        sql.append(" , ");
                    }
                }

                sql.append(" )");
                sql.append("order by upper(name)");

                stmt = this.conn.prepareStatement(sql.toString());

                rs = stmt.executeQuery();


                while (rs.next()) {
                    groups.add(this.rsToGroup(rs));
                }
            }

            return groups;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all question groups: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }


    /**
     * Retrieves all questions groups associated with a question.
     *
     * @param questionId The question unique identifier to get groups for
     * @return The list of question groups in the system. The list will be empty if no
     *         question groups exist in the system.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Group> getQuestionGroups(int questionId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select questiongroup.* ");
            sql.append("from questiongroup, questiongrpquestion ");
            sql.append("where questiongrpquestion.questionid = ? ");
            sql.append("and questiongrpquestion.questiongroupid = questiongroup.questiongroupid ");
            sql.append("order by upper(name)");

            stmt = this.conn.prepareStatement(sql.toString());

            stmt.setLong(1, questionId);

            rs = stmt.executeQuery();

            List<Group> groups = new ArrayList<Group>();
            while (rs.next()) {
                groups.add(this.rsToGroup(rs));
            }

            return groups;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all question groups: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Retrieves a question's version.
     *
     * @param questionId The question unique identifier to get question's version
     * @return The version number of this question.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public int getQuestionVersion(int questionId) throws CtdbException,
            ObjectNotFoundException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select question.version ");
            sql.append("from question ");
            sql.append("where question.questionid = ? ");

            stmt = this.conn.prepareStatement(sql.toString());

            stmt.setLong(1, questionId);

            rs = stmt.executeQuery();

            int version = Integer.MIN_VALUE;
            if (!rs.next()) {
                throw new ObjectNotFoundException("The question with ID = " + questionId + " does not exist in the system.");
            } else
                version = rs.getInt("version");

            return version;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get the question version: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /************************************************************************
     * HELPER QUESTION METHODS
     ***********************************************************************/

    /**
     * Gets the base insert columns sql string.
     *
     * @return The string containing the base insert columns sql
     */
    private StringBuffer getBaseInsertColumns() {
        StringBuffer sql = new StringBuffer(100);
        sql.append("questionid, version, name, text, type, ");
        sql.append("createdby, createddate, updatedby, updateddate, defaultvalue, unansweredvalue");
        sql.append(", copyright, descriptionup, descriptiondown");
        sql.append(", includeother, htmltext");
        
        return sql;
    }

    /**
     * Gets the base insert sql string.
     *
     * @return The string containing the base insert sql
     */
    private StringBuffer getBaseInsertValues() {
        StringBuffer sql = new StringBuffer(100);
        sql.append("?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ? ");
        
        return sql;
    }

    /**
     * Gets the base update sql string.
     *
     * @param versionFlag The flag to determine if needs to set version
     * @return The string containing the base update sql
     */
    private StringBuffer getBaseUpdateColumnsValues(boolean versionFlag) {
        StringBuffer sql = new StringBuffer(100);
        sql.append("set name = ?, text = ?, type = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP, defaultvalue = ?, unansweredvalue = ?, ");
        sql.append("descriptionup = ?, descriptiondown = ?, includeother = ?, htmltext = ?");

        if (versionFlag) {
            sql.append(", version = version + 1 ");
        }

        return sql;
    }

    /**
     * Sets the base data for sql insert statement.
     *
     * @param question The Question object to get data for setting to PreparedStatement
     * @param stmt     The PreparedStatement
     * @param version  The question version number
     * @return The next parameter index
     * @throws SQLException Thrown if any errors occur
     */
    private int setBaseInsertData(Question question, PreparedStatement stmt, int version) throws SQLException {
    	stmt.setInt(1, question.getId());
        stmt.setInt(2, version);
        stmt.setString(3, question.getName()); 
        stmt.setString(4, question.getText());
        stmt.setInt(5, question.getType().getValue());
        stmt.setInt(6, question.getCreatedBy());
        stmt.setInt(7, question.getUpdatedBy());
        stmt.setString(8, question.getDefaultValue());
        stmt.setString(9, question.getUnansweredValue());
        
        return 10;
    }

    /**
     * Sets the base data for sql update statement.
     *
     * @param question The Question object to get data for setting to PreparedStatement
     * @param stmt     The PreparedStatement
     * @return The next parameter index
     * @throws SQLException Thrown if any errors occur
     */
    private int setBaseUpdateData(Question question, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, question.getName());
        stmt.setString(2, question.getText());
        stmt.setInt(3, question.getType().getValue());
        stmt.setInt(4, question.getUpdatedBy());
        stmt.setString(5, question.getDefaultValue());
        stmt.setString(6, question.getUnansweredValue());
        stmt.setString(7, question.getDescriptionUp());
        stmt.setString(8, question.getDescriptionDown());
        stmt.setBoolean(9, question.isIncludeOtherOption());
        stmt.setString(10, question.getHtmltext());
        
        return 11;
    }

    /**
     * Transforms a ResultSet object into a Question object
     *
     * @param rs ResultSet to transform to Question object
     * @return Question Object
     * @throws SQLException Thrown if any errors occur while retrieving data from result set
     */
    private Question rsToQuestion(ResultSet rs) throws SQLException {
        Question question = null;

        if (rs.getInt("type") == QuestionType.PATIENT_CALENDAR.getValue()) {
            question = new PatientCalendarQuestion();
        } else if (rs.getInt("type") == QuestionType.IMAGE_MAP.getValue()) {
            question = new ImageMapQuestion();
        } else if (rs.getInt("type") == QuestionType.VISUAL_SCALE.getValue()) {
            question = new VisualScale();
        } else {
            question = new Question();
        }

        question.setId(rs.getInt("questionid"));
        question.setVersion(new Version(rs.getInt("version")));
        question.setName(rs.getString("name"));
        question.setText(rs.getString("text"));

        question.setType(QuestionType.getByValue(rs.getInt("type")));
        question.setDefaultValue(rs.getString("defaultvalue"));
        question.setUnansweredValue(rs.getString("unansweredvalue"));

        question.setCreatedBy(rs.getInt("createdby"));
        question.setCreatedDate(new java.util.Date(rs.getTimestamp("createddate").getTime()));
        question.setUpdatedBy(rs.getInt("updatedby"));
        question.setUpdatedDate(new java.util.Date(rs.getTimestamp("updateddate").getTime()));
       
        //add by sunny
        question.setCde(rs.getBoolean("iscde"));
        question.setCopyfrom(rs.getInt("copyfrom"));
        question.setDescriptionUp(rs.getString("descriptionup"));
        question.setDescriptionDown(rs.getString("descriptiondown"));
        
        //added by Ching Heng
        question.setIncludeOtherOption(rs.getBoolean("includeother"));
        question.setCopyRight(rs.getInt("copyright"));
        
        try {
        	question.setHtmltext(rs.getString("htmltext"));
        }
        catch(Exception e) {
        	// if we had an exception here, it means that the text doesn't exist
        	// for that row or query.  Add it as empty, then.
        	question.setHtmltext("");
        }
        return question;
    }

    /**
     * Transforms a result set object into a question object
     * This method is used when searching for questions associated with
     * a form section and form specific question attributes are needed.
     *
     * @param rs
     * @return the new question object
     * @throws SQLException
     */
    private Question rsToSectionQuestion(ResultSet rs) throws SQLException {
        Question q = this.rsToQuestion(rs);
        
        if (!rs.getBoolean("suppressflag")) {
            q.setTextDisplayed(true);
        } else {
            q.setTextDisplayed(false);
        }
        
        return q;
    }

    /**
     * Transforms a ResultSet object into a Question object
     *
     * @param rs ResultSet to transform to Question object
     * @return Question Object
     * @throws SQLException Thrown if any errors occur while retrieving data from result set
     */
    private Question rsToSimpleQuestion(ResultSet rs) throws SQLException {
        Question question;
        if (QuestionType.getByValue(rs.getInt("type")).equals(QuestionType.IMAGE_MAP)) {
            question = new ImageMapQuestion();
        } else {
            question = new Question();
        }

        question.setId(rs.getInt("questionid"));
        question.setVersion(new Version(rs.getInt("version")));
        question.setName(rs.getString("name"));
        question.setText(rs.getString("text"));
        question.setType(QuestionType.getByValue(rs.getInt("type")));

        return question;
    }

    /**
     * Transform result set to question object
     * this method used when attributes specific to a form association
     * are needed in question object.
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    private Question rsToSimpleSectionQuestion(ResultSet rs) throws SQLException {
        Question q = this.rsToSimpleQuestion(rs);
       /* if (rs.getString("suppressflag") != null
                && rs.getInt("suppressflag") == 0) {
            q.setTextDisplayed(true);
        } else {
            q.setTextDisplayed(false);
        }*/
        if (!rs.getBoolean("suppressflag")) {
            q.setTextDisplayed(true);
        } else {
            q.setTextDisplayed(false);
        }
        return q;
    }

    /**
     * Transforms a ResultSet object into a Question Group object
     *
     * @param rs ResultSet to transform to Question Group object
     * @return Group Object
     * @throws SQLException Thrown if any errors occur while retrieving data from result set
     */
    private Group rsToGroup(ResultSet rs) throws SQLException {
        Group group = new Group();
        group.setId(rs.getInt("questiongroupid"));
        group.setName(rs.getString("name"));
        group.setDescription(rs.getString("description"));
        group.setCreatedBy(rs.getInt("createdby"));
        group.setCreatedDate(rs.getDate("createddate"));
        group.setUpdatedBy(rs.getInt("updatedby"));
        group.setUpdatedDate(rs.getDate("updateddate"));
        return group;
    }

    /**
     * Transforms a ResultSet object into an Answer object
     *
     * @param rs ResultSet to transform to Answer object
     * @return Answer Object
     * @throws SQLException Thrown if any errors occur while retrieving data from result set
     */
    private Answer rsToAnswer(ResultSet rs) throws SQLException {
        Answer answer = new Answer();
        answer.setId(rs.getInt("answerid"));
        answer.setDisplay(rs.getString("display"));
        
		if (rs.getObject("score") != null) {
			double score = rs.getDouble("score");
			answer.setScore(score);
		}
        
        String submittedValue = rs.getString("submittedValue");
        
        if (submittedValue != null && !submittedValue.isEmpty()) {
            answer.setSubmittedValue(submittedValue);
        }
        
        return answer;
    }

    /**
     * Retrieves all images associated with a group.
     *
     * @param groupId The unique identifier of the Group to retrieve
     * @return A list of all image names.  If the group does not exist an empty
     *         list will be returned.
     * @throws CtdbException Thrown if any errors occur
     */
    public List<String> getGroupImages(int groupId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            List<String> imageFileNames = new ArrayList<String>();

            StringBuffer sql = new StringBuffer(200);
            sql.append("select * from questiongrpimage where questiongroupid = ? order by imagefilename");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, groupId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                imageFileNames.add(rs.getString("imagefilename"));
            }

            return imageFileNames;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to retrieve question group image file names for group: " + groupId + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Constructs image file name
     *
     * @param prefix The prefix
     * @param id     The image ID
     * @param name   The current file name
     * @return String The new file name
     * @throws CtdbException Thrown if any errors occur
     */
    private String getImageFileName(String prefix, int id, String name) throws CtdbException {
        try {
            String nameHead = prefix + id + ".";
            String fileType;
            int lastIndex = name.lastIndexOf('.');
            if (lastIndex != -1) {
                // chanage file type to lower case.  Image file end with .GIF or .JPG is not displayed
                // in the IE browser.  Image files from MAC often end with capitalized file type.

                fileType = name.substring(lastIndex + 1, name.length()).toLowerCase();
            } else {
                fileType = "";
            }
            return nameHead + fileType;
        }
        catch (IndexOutOfBoundsException e) {
            throw new CtdbException("Unable to construct question image file name: " + name + e.getMessage(), e);
        }
    }

    /**
     * Checks if the question is administered.  Uses ID and versionNumber of the
     * domain object to check to see if it is administered.  If the versionNumber
     * in the domain object is not set (equals to Integer.MIN_VALUE), then the
     * current version of the question in the data base is used for the check.
     *
     * @param question the question object to check
     * @return if the question with the version has been administered
     * @throws CtdbException thrown if any errors occur while processing
     */
    public boolean isAdministered(Question question) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(200);

            if ((question.getVersion() == null) || (question.getVersion().getVersionNumber() == Integer.MIN_VALUE)) {
                sql.append("select count(responsedraftid) from responsedraft, question where question.questionid = ? and responsedraft.questionid = question.questionid and responsedraft.questionversion = question.version");
                stmt = this.conn.prepareStatement(sql.toString());
                stmt.setLong(1, question.getId());
            } else {
                sql.append("select count(responsedraftid) from responsedraft where questionid = ? and questionversion = ?");
                stmt = this.conn.prepareStatement(sql.toString());
                stmt.setLong(1, question.getId());
                stmt.setInt(2, question.getVersion().getVersionNumber());
            }

            rs = stmt.executeQuery();
            int num = 0;
            if (rs.next()) {
                num = rs.getInt(1);
            }
            return num > 0;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to check is administered for a question with id " + question.getId() + " : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public boolean isAdministeredInSection(Question question, int sectionId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(200);

            sql.append("select count(rd.responsedraftid) from responsedraft rd ");
            sql.append( " , section s, administeredform af, dataentrydraft ded ");
            sql.append (" where rd.questionid = ? and rd.questionversion = ? ");
            sql.append( " and rd.dataentrydraftid = ded.dataentrydraftid ");
            sql.append (" and ded.administeredformid = af.administeredformid ");
            sql.append (" and s.sectionid = ? ");
            sql.append (" and s.formid = af.formid ");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, question.getId());
            stmt.setInt(2, question.getVersion().getVersionNumber());
            stmt.setLong(3, sectionId);

            rs = stmt.executeQuery();
            int num = 0;
            if (rs.next()) {
                num = rs.getInt(1);
            }
            return num > 0;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to check is administered on form for a question with id " + question.getId() + " : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Checks if the question is attached on any forms.  Uses ID and versionNumber of the
     * domain object to check to see if it is used.  If the versionNumber
     * in the domain object is not set (equals to Integer.MIN_VALUE), then the
     * current version of the question in the data base is used for the check.
     *
     * @param question the question object to check
     * @return if the question with the version has been used on any form
     * @throws CtdbException thrown if any errors occur while processing
     */
    public boolean isAttachedToForm(Question question) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(200);

            if ((question.getVersion() == null) || (question.getVersion().getVersionNumber() == Integer.MIN_VALUE)) {
                sql.append("select count(sectionquestion.questionid) from sectionquestion, question where question.questionid = ? and sectionquestion.questionid = question.questionid and sectionquestion.questionversion = question.version");
                stmt = this.conn.prepareStatement(sql.toString());
                stmt.setLong(1, question.getId());
            } else {
                sql.append("select count(questionid) from sectionquestion where questionid = ? and questionversion = ?");
                stmt = this.conn.prepareStatement(sql.toString());
                stmt.setLong(1, question.getId());
                stmt.setInt(2, question.getVersion().getVersionNumber());
            }

            rs = stmt.executeQuery();
            int num = 0;
            if (rs.next()) {
                num = rs.getInt(1);
            }
            return num > 0;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to check is used on any forms for a question with id " + question.getId() + " : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public boolean questionNameExists(String questionName) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(200);

            sql.append("select questionId from question_view where name = ?");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setString(1, questionName);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return true;
            } else {
                return false;
            }

        }
        catch (SQLException e) {
            throw new CtdbException("Error checkign for duplicate question name " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }


    }
    /*
     * Set quesiton flag to true whenever the quesiton has been attached to a form with decimal precision
     */

    public boolean hasDecimalPrecisionAttribute(int qid) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.conn.prepareStatement("select count(1) from questionattributes where questionid = ? and decimalprecision != -1 ");
            stmt.setInt(1, qid);
            rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1)>0; 
        }
        catch (SQLException e) {
        	throw new CtdbException("Error checking for evaluating if there are any decimal precision attibute for the question " + e.getMessage(), e);
        }
        finally {
        	this.close(rs);
        	this.close(stmt);
        }
    }
    
     public boolean hasCalDependentAttribute(int qid) throws CtdbException {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = this.conn.prepareStatement("select count(1) from calculateQuestion where calculatequestionid = ?" );
                stmt.setInt(1, qid);
                rs = stmt.executeQuery();
                return rs.next() && rs.getInt(1) >0;
            }
            catch (SQLException e) {
                throw new CtdbException("Error checking for evaluating if the question has been used in any other question's calculation rule: " + e.getMessage(), e);
            }
            finally {
            	this.close(rs);
            	this.close(stmt);
            }
     }
     
     public boolean prepopulationAttribute(int qid) throws CtdbException {
                PreparedStatement stmt = null;
                ResultSet rs = null;

                try {
                    stmt = this.conn.prepareStatement("select count(1) from questionattributes where questionid = ? and prepopulation = true ");
                    stmt.setInt(1, qid);
                    rs = stmt.executeQuery();
                    return rs.next() && rs.getInt(1) >0;
                }
                catch (SQLException e) {
                    throw new CtdbException("Error checking for evaluating if there are any prepopulation values defined for the question " + e.getMessage(), e);
                }
                finally {
                	this.close(rs);
                	this.close(stmt);
                }
    }

     /*
      *Batch implementation to set several question flags that are generated across sections and forms
      */
     public void UpdateQuestionGlobalFlags(List <Question> questions) throws CtdbException {
     	if(questions == null || questions.size() ==0){
     		return;
     	}

     	PreparedStatement stmt = null;
         ResultSet rs = null;
         //Statement.() will automatically close the ResultSet as well
         try {
             stmt = this.conn.prepareStatement("select count(1) from calculateQuestion where calculatequestionid = ? " );
             for(Question q: questions){
             	stmt.setLong(1, q.getId());
             	rs = stmt.executeQuery();
             	q.setHasCalDependent( rs.next() && rs.getInt(1) >0);
             }
             stmt.close();
             stmt = this.conn.prepareStatement("select count(1) from questionattributes where questionid = ? and prepopulation = true " );
             for(Question q: questions){
             	stmt.setLong(1, q.getId());
             	rs = stmt.executeQuery();
             	q.setPrepopulation( rs.next() && rs.getInt(1) >0);
             }
             stmt.close();
             stmt = this.conn.prepareStatement("select count(1) from questionattributes where questionid = ? and decimalprecision != -1 " );
             for(Question q: questions){
             	stmt.setLong(1, q.getId());
             	rs = stmt.executeQuery();
             	q.setHasDecimalPrecision( rs.next() && rs.getInt(1) >0);
             }
         }
         catch (SQLException e) {
             throw new CtdbException("Updating Question Global Flags for CalDependent, prepopulation, and calculation rules: " + e.getMessage(), e);
         }
         finally {
         	this.close(rs);
         	this.close(stmt);
         }
     }
    public String getImageMapSequence() throws CtdbException {
        try {
            return Integer.toString(this.getNextSequenceValue(conn, "imagefilename_seq"));
        } catch (SQLException e) {
            throw new CtdbException("Unable to get the image map sequence :" + e.getMessage(), e);
        }
    }


    public boolean isSkipParent(int questionId, int questionversion) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("select skipruleflag from questionattributes where questionid = ? ");
            sql.append(" and questionversion=? ");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            stmt.setInt(2, questionversion);

            rs = stmt.executeQuery();

            if (rs.next()) {
                if (rs.getBoolean(1) == true) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }

        }
        catch (SQLException e) {
            throw new CtdbException("Error checking for skip rule parent" + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public boolean hasSkipRuleOnOtherForms(Question q, String sectionId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("select questionId from skiprulequestion where questionid = ? ");
            sql.append(" and sectionId != ? ");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, q.getId());
            stmt.setString(2, sectionId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return true;
            } else {
                return false;
            }

        }
        catch (SQLException e) {
            throw new CtdbException("Error checkign for skip rule on other forms" + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public boolean hasSkipRuleOnCurrentForm(Question q, String sectionId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("select questionId from skiprulequestion where questionid = ? ");
            sql.append(" and sectionId = ? ");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, q.getId());
            stmt.setLong(2, Long.decode(sectionId));

            rs = stmt.executeQuery();

            if (rs.next()) {
                return true;
            } else {
                return false;
            }

        }
        catch (SQLException e) {
            throw new CtdbException("Error checkign for skip rule on other forms" + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    /************************************************************************
     * DELETE QUESTION METHODS       added by Ching-Heng
     ***********************************************************************/
    /*
     * to check that Did the question be attached on any section 
     */
    public boolean isAttached(int questionId, int questionVersion) throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
    	try{
    		StringBuffer sql = new StringBuffer(200);
    		//SQL  select * from sectionquestion left join sectionarchive on sectionquestion.sectionid = sectionarchive.sectionid
    		sql.append("select * from sectionquestion left join sectionarchive on sectionquestion.sectionid = sectionarchive.sectionid where questionid = ? and questionversion = ?");
    		stmt = this.conn.prepareStatement(sql.toString());
    		stmt.setLong(1, questionId);
            stmt.setInt(2, questionVersion);
            
    		rs = stmt.executeQuery();
    		if(rs.next()){
    			return true;
    		}else{
    			return false;
    		}   		
    	}
    	catch(SQLException e){
    		throw new CtdbException("Error checkign for question is attached on any sections" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    }
    
    
    
    
    
    /**
     * check if question is being used on another form
     * @param questionId
     * @param formid
     * @return
     * @throws CtdbException
     */
    public boolean isAttachedOnAnotherForm(int questionId, int formid) throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
    	try{
    		StringBuffer sql = new StringBuffer(200);
    		//SQL  select * from sectionquestion left join sectionarchive on sectionquestion.sectionid = sectionarchive.sectionid
    		sql.append("select sq.questionid, s.formid from sectionquestion sq, section s  ");
    		sql.append("where sq.sectionid = s.sectionid and ");
    		sql.append("sq.questionid = ? and ");
    		sql.append("s.formid <> ? ");
    		stmt = this.conn.prepareStatement(sql.toString());
    		stmt.setLong(1, questionId);
            stmt.setInt(2, formid);
            
    		rs = stmt.executeQuery();
    		if(rs.next()){
    			return true;
    		}else{
    			return false;
    		}   		
    	}
    	catch(SQLException e){
    		throw new CtdbException("Error checkign for question is attached on any sections" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    }
    
    
    
    
    
    /*
     * to check that did the question be stored in the questionArchive table. Since the old version questions will be stored in that table  
     */
    public boolean inQuestionArchive(int questionId) throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
        try{
    		StringBuffer sql = new StringBuffer(200);
    		//SQL
    		sql.append("select * from questionarchive where questionid = ?");
    		stmt = this.conn.prepareStatement(sql.toString());
    		stmt.setLong(1, questionId);
    		rs = stmt.executeQuery();
    		if(rs.next()){
    			return true;
    		}else{
    			return false;
    		}   		
    	}
    	catch(SQLException e){
    		throw new CtdbException("Error checkign for question is in the questionarchive table" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    }
    
    /*
     * to delete the question image of question which will be deleted
     */
    public void deleteQuestionImageAll(int questionId) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(400);
            sql.append("delete FROM questionimage where questionid = ?");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to delete question image in questionimage table: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }
    
    /*
     * to delete the imageMap
     */
    public void deleteImageMap(int questionId,int questionVersion) throws CtdbException{
    	PreparedStatement stmt = null,stmt2=null;
        ResultSet rs = null;
        try{
        	StringBuffer sql = new StringBuffer(200);
        	sql.append("select imagemapid from imagemap where questionid = ? and questionversion = ?");
        	stmt = this.conn.prepareStatement(sql.toString());
    		stmt.setLong(1, questionId);
            stmt.setInt(2, questionVersion);
            rs = stmt.executeQuery(); // rs for imageId
            if(rs.next()){
            	int imageMapId=rs.getInt("imagemapid");
            	this.deleteImageMapOptions(imageMapId); // clean the imagemapvalues table first
            	StringBuffer sql_2 = new StringBuffer(200);
            	sql_2.append("delete from imagemap where questionid = ? and questionversion = ?");
            	stmt2 = this.conn.prepareStatement(sql_2.toString());
        		stmt2.setLong(1, questionId);
                stmt2.setInt(2, questionVersion);
                stmt2.executeUpdate(); // than clean the imagemap table
            }
        }
        catch(SQLException e){
    		throw new CtdbException("Unable to delete ImageMap " + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    }
    
    /*
     * to delete visualscale 
     */
    public void deleteVisualScale(int questionId,int questionVersion) throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
    	try{
    		StringBuffer sql = new StringBuffer(400);
            sql.append("delete FROM visualscale where questionid = ? and questionversion= ? ");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            stmt.setInt(2, questionVersion);
            stmt.executeUpdate();
    	}
    	catch(SQLException e){
    		throw new CtdbException("Unable to delete Visual Scale" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    }
    
    /*
     *  to delete the questionattributes
     */
    public void deleteQuestionAttribute (int questionId,int questionVersion) throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
    	try{
    		StringBuffer sql = new StringBuffer(400);
            sql.append("delete FROM questionattributes where questionid = ? and questionversion= ? ");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            stmt.setInt(2, questionVersion);
            stmt.executeUpdate();
    	}
    	catch(SQLException e){
    		throw new CtdbException("Unable to delete questionattributes" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    }
    
    /*
     * to delete the  question_medicalCode
     */
    public void deleteQuestionMedicalcode(int questionId,int questionVersion) throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
    	try{
    		StringBuffer sql = new StringBuffer(400);
            sql.append("delete FROM question_medicalcode where questionid = ? and questionversion= ? ");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            stmt.setInt(2, questionVersion);
            stmt.executeUpdate();
    	}
    	catch(SQLException e){
    		throw new CtdbException("Unable to delete QuestionMedicalcode" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    }
    
    /**
     * 
     * @param questionId
     * @return
     * @throws CtdbException
     */
    public int getLastedVersionInQuestionarchive(int questionId) throws CtdbException{
    	int lastVersion = 999999;
    	PreparedStatement stmt = null;
        ResultSet rs = null;
        try{
        	StringBuffer sql = new StringBuffer(200);
        	sql.append("select max (version) from questionarchive where questionid= ? ");
    		stmt = this.conn.prepareStatement(sql.toString());
    		stmt.setLong(1, questionId);
    		rs = stmt.executeQuery();
    		if(rs.next()){
    			lastVersion=rs.getInt(1);
    		}
        }
        catch(SQLException e){
    		throw new CtdbException("Error checkign for question is attached on any sections" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
        
        return lastVersion;
    }
    
    /*
     *  Copy the old version question to new one, since the new one will be deleted
     */
    public void updateQuestionByQuestionArchive(int questionId) throws CtdbException{
    	PreparedStatement stmt = null;
        try {
        	int lastVersion = this.getLastedVersionInQuestionarchive(questionId);   // move the last version question in questionarchive table to the question table
        	
        	String sql = "update question a set " + 
        			  "a.createdby = ( select createdby from questionarchive where questionid = ? and version = ? )," +
        			  "a.createddate = ( select createddate from questionarchive where questionid = ? and version = ? )," +
        			  "a.defaultvalue = ( select defaultvalue from questionarchive where questionid = ? and version = ? )," +
        			  "a.iscde = ( select iscde from questionarchive where questionid = ? and version = ? )," +
        			  "a.name = ( select name from questionarchive where questionid = ? and version = ? )," +
        			  "a.text = ( select text from questionarchive where questionid = ? and version = ? )," +
        			  "a.type = ( select type from questionarchive where questionid = ? and version = ? )," +
        			  "a.unansweredvalue = ( select unansweredvalue from questionarchive where questionid = ? and version = ? )," +
        			  "a.updatedby = ( select updatedby from questionarchive where questionid = ? and version = ? )," +
        			  "a.updateddate = ( select updateddate from questionarchive where questionid = ? and version = ? )," +
        			  "a.version = ?," +
        			  " where a.questionid = ?";
        	
        	stmt = this.conn.prepareStatement(sql);
    		stmt.setLong(1, questionId);	stmt.setInt(2, lastVersion);
    		stmt.setLong(3, questionId);	stmt.setInt(4, lastVersion);
    		stmt.setLong(5, questionId);	stmt.setInt(6, lastVersion);
    		stmt.setLong(7, questionId);	stmt.setInt(8, lastVersion);
    		stmt.setLong(9, questionId);	stmt.setInt(10, lastVersion);
    		stmt.setLong(11, questionId);	stmt.setInt(12, lastVersion);
    		stmt.setLong(13, questionId);	stmt.setInt(14, lastVersion);
    		stmt.setLong(15, questionId);	stmt.setInt(16, lastVersion);
    		stmt.setLong(17, questionId);	stmt.setInt(18, lastVersion);
    		stmt.setLong(19, questionId);	stmt.setInt(20, lastVersion);
    										stmt.setInt(21, lastVersion);
    		stmt.setLong(22, questionId);
    		stmt.executeUpdate();
        }
        catch(SQLException e){
    		throw new CtdbException("Unable to update question table" + e.getMessage(), e);
    	}
    	finally {
            this.close(stmt);
    	}
    }
    
    public boolean isCopiedQuestion(int questionId) throws CtdbException, SQLException{
    	int copyright=0;
    	boolean flag=false;
    	PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuffer sql = new StringBuffer(200);
        sql.append("select copyright from question where questionid=?");
        stmt = this.conn.prepareStatement(sql.toString());
        stmt.setInt(1, questionId);
        rs=stmt.executeQuery();
        if(rs.next()){
        	copyright=rs.getInt("copyright");
        }
        if(copyright==1){flag=true;}
        return flag;
    }
    
    /*
     * to delete the copied question Chin Heng
     */
    public void deleteCopiedQuestion(int questionId) throws CtdbException, SQLException{
    	PreparedStatement stmt = null;
    	int questionVersion = this.getQuestionVersion(questionId);
    	try{
	    	if(this.isCopiedQuestion(questionId) && !this.isAttached(questionId, questionVersion)){
	    		// delete from the questiongrpquestion table
				this.deleteQuestionGroup(questionId);
				// delete from the questionimag table
				this.deleteQuestionImageAll(questionId);
				// delete from the imagemap table
				this.deleteImageMap(questionId, questionVersion);
				// delete from the visualscale table
				this.deleteVisualScale(questionId, questionVersion);
				// delete from questionanswer
				this.deleteAnswers(questionId, questionVersion);
				// delete from the skiprulequestion table
				this.deleteSkippedQuestions(questionId);
				// delete from the calculatequestion table
				this.deleteCalculatedQuestions(questionId);
				// delete from the questionattribute
				this.deleteQuestionAttribute(questionId, questionVersion);
				//delte from question medical code
				this.deleteQuestionMedicalcode(questionId, questionVersion);
				// clean question table
				StringBuffer sql = new StringBuffer(200);
				sql.append("delete from question where questionid=? and version=?");
	    		stmt = this.conn.prepareStatement(sql.toString());
	    		stmt.setInt(1, questionId);
	    		stmt.setInt(2, questionVersion);
	    		stmt.executeUpdate();
	    		close(stmt);
	    		sql = new StringBuffer(200);
				sql.append("delete from questionarchive where questionid=?");
	    		stmt = this.conn.prepareStatement(sql.toString());
	    		stmt.setInt(1, questionId);
	    		stmt.executeUpdate();
	    	}
    	}
    	catch(SQLException e) {
    		throw new CtdbException("Unable to delete copied question" + e.getMessage(), e);
    	}
    	finally {
            this.close(stmt);
    	}
        
    }
    
    /*
     * to delete the question !! Ching Heng
     */   
    public void deleteQuestion(int questionId, int questionVersion) throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
        try{
        	if(!this.isAttached(questionId, questionVersion)){ // make sure that the question not attached on any section
        		StringBuffer sql = new StringBuffer(200);
        		if(!this.inQuestionArchive(questionId)){   // No previous version
        			// delete from the questiongrpquestion table
        			this.deleteQuestionGroup(questionId);
        			// delete from the questionimag table
        			this.deleteQuestionImageAll(questionId);
        			// delete from the imagemap table
        			this.deleteImageMap(questionId, questionVersion);
        			// delete from the visualscale table
        			this.deleteVisualScale(questionId, questionVersion);
        			// delete from questionanswer
        			this.deleteAnswers(questionId, questionVersion);
        			// delete from the skiprulequestion table
        			this.deleteSkippedQuestions(questionId);
        			// delete from the calculatequestion table
        			this.deleteCalculatedQuestions(questionId);
        			// delete from the questionattribute
        			this.deleteQuestionAttribute(questionId, questionVersion);
        			//delte from question medical code
        			this.deleteQuestionMedicalcode(questionId, questionVersion);
        			// clean question table
        			sql.append("delete from question where questionid=? and version=?");
            		stmt = this.conn.prepareStatement(sql.toString());
            		stmt.setInt(1, questionId);
            		stmt.setInt(2, questionVersion);
            		stmt.executeUpdate();
            		
        		}
        		else{
        			if(questionVersion==this.getQuestionVersion(questionId)){ // Delete new question, but it has previous version in QuestionArchive==============================
            			// update the question table by old version question
            			this.updateQuestionByQuestionArchive(questionId);
            			// delete from the imagemap table
            			this.deleteImageMap(questionId, questionVersion);
            			// delete from the visualscale table
            			this.deleteVisualScale(questionId, questionVersion);
            			// delete from questionanswer
            			this.deleteAnswers(questionId, questionVersion);
            			// delete from the questionattribute
            			this.deleteQuestionAttribute(questionId, questionVersion);
            			//delte from question medical code
            			this.deleteQuestionMedicalcode(questionId, questionVersion);
            			sql.append("delete from questionarchive where questionid=? and version='"+this.getLastedVersionInQuestionarchive(questionId)+"'");
                		stmt = this.conn.prepareStatement(sql.toString());
                		stmt.setLong(1, questionId);
                		stmt.executeUpdate();
        			}else{ // Delete the old version===================================================
            			// update the question table by old version question
            			//this.updateQuestionByQuestionArchive(questionId);
            			// delete from the imagemap table
            			this.deleteImageMap(questionId, questionVersion);
            			// delete from the visualscale table
            			this.deleteVisualScale(questionId, questionVersion);
            			// delete from questionanswer
            			this.deleteAnswers(questionId, questionVersion);
            			// delete from the questionattribute
            			this.deleteQuestionAttribute(questionId, questionVersion);
            			// delte from question medical code
            			this.deleteQuestionMedicalcode(questionId, questionVersion);
            			// clean questionarchive table
            			sql.append("delete from questionarchive where questionid=? and version=?");
                		stmt = this.conn.prepareStatement(sql.toString());
                		stmt.setLong(1, questionId);
                		stmt.setInt(2, questionVersion);
                		stmt.executeUpdate();
        			}
        		}
        		
        	}else{
        		throw new CtdbException("Unable to delete question, the question was attached on some section");
        	}
        }
        catch(SQLException e){
    		throw new CtdbException("Unable to delete question" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    }
    
    public void updateCopiedQuestion(int formId,int orgQuestionId,int newQuestionId,int questionAttributeId) throws CtdbException {
    	PreparedStatement stmt = null;
    	PreparedStatement stmt2 = null;
        ResultSet rs = null;
    	try {
            StringBuffer sql = new StringBuffer(300);
	        sql.append("select sq.sectionid from sectionquestion sq left join section s  on  sq.sectionid=s.sectionid ");
	        sql.append(" left join form f on s.formid=f.formid where f.formid=? and sq.questionid=? ");
	        stmt = this.conn.prepareStatement(sql.toString());
	        stmt.setInt(1, formId);
	        stmt.setInt(2, orgQuestionId);
	        rs = stmt.executeQuery();
	        int sectionId=0;
	        if (rs.next()) {
	        	sectionId = rs.getInt(1);
	        }
	        close(rs);
	        close(stmt);
	        
	        sql = new StringBuffer(300);
	        
	        sql.append("update sectionquestion set questionid=? ,questionversion =1 "); 
	        sql.append("where  questionattributesid =? and questionid=? ");    
	        
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, questionAttributeId);            
            stmt.setInt(3, orgQuestionId);
            
            stmt.executeUpdate();
            close(stmt);
            
	        sql = new StringBuffer(300);        
            sql.append("update questionattributes set questionid=? ,questionversion = 1 where  questionattributesid = ? "); 
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, questionAttributeId);       
            stmt.executeUpdate();
            close(stmt);
            
            //update calculation if it has calculation 
	        sql = new StringBuffer(400);        
	        sql.append("update calculatequestion set questionid=? where sectionid = ? and questionid = ? "); 
	        stmt = this.conn.prepareStatement(sql.toString());
	        
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, sectionId);            
            stmt.setInt(3, orgQuestionId);  
            stmt.executeUpdate();
            close(stmt);
            
            //update calculation if it was be operator 
	        String orgQNo = "S_"+sectionId+"_Q_"+orgQuestionId;
	        String newQNo = "S_"+sectionId+"_Q_"+newQuestionId;     
	        sql = new StringBuffer(400);        
	        sql.append("select calculation,questionattributesid  from questionattributes where  calculation like ? "); 
	        stmt = this.conn.prepareStatement(sql.toString());
	        stmt.setString(1, "%" + orgQNo + "%");
	        rs = stmt.executeQuery();
	        
	        while (rs.next()) {
	        	String orgCalculation= rs.getString(1);
	        	int questionattributesid =rs.getInt(2);
		        String newCalculation = orgCalculation.replace(orgQNo, newQNo);
		        
		        sql = new StringBuffer(400);        
		        sql.append("update questionattributes set calculation = ? where questionattributesid = ? "); 
		        stmt2 = this.conn.prepareStatement(sql.toString());
		        stmt2.setString(1, newCalculation);
	            stmt2.setInt(2, questionattributesid);   

	            stmt2.executeUpdate();
	            close(stmt2);
	        }
	        
	        close(stmt);

            //update skip
	        sql = new StringBuffer(400);        
	        sql.append("update skiprulequestion set questionid=? where sectionid=? and questionid=? "); 
	        stmt = this.conn.prepareStatement(sql.toString());
	        
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, sectionId);            
            stmt.setInt(3, orgQuestionId);  
            stmt.executeUpdate();
            close(stmt);
            
	        sql = new StringBuffer(400);        
	        sql.append("update skiprulequestion set skipquestionid =? where skipsectionid =? and skipquestionid =? "); 
	        stmt = this.conn.prepareStatement(sql.toString());
	        
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, sectionId);            
            stmt.setInt(3, orgQuestionId);  
            stmt.executeUpdate();
    	}
    	catch(SQLException e) {
    		throw new CtdbException("Unable to get all question groups: " + e.getMessage(), e);
    	}
    	finally {
    		close(rs);
            close(stmt);
            close(stmt2);
    	}     	
    }
    

    /*public int copyQuestion(int orgQuestionId) throws CtdbException {
        // TODO copyQuestionGroup?
    	int newQuestionId = getQuestionMaxId();
    	copyQuestionInfo(newQuestionId,orgQuestionId);
    	copyQuestionImageAll(newQuestionId,orgQuestionId);
    	copyQuestionGroup(newQuestionId,orgQuestionId);
    	copyQuestionImage(newQuestionId,orgQuestionId);
    	copyQuestionImageValue(newQuestionId,orgQuestionId);
    	copyVisualScale(newQuestionId,orgQuestionId);
    	copyAnswers(newQuestionId,orgQuestionId);
    	copyQuestionMedicalcode(newQuestionId,orgQuestionId);
    	
    	return newQuestionId;
    }*/
    
    //creates a new question based on an original question
    //the question name will be formId_dataElementName
    public int copyQuestionNewFormBuilder(int orgQuestionId, String newQuestionName) throws CtdbException {
        // TODO copyQuestionGroup?
    	int newQuestionId = getQuestionMaxId();
    	copyQuestionInfoNewFormBuilder(newQuestionId,orgQuestionId, newQuestionName);
    	copyQuestionImageAll(newQuestionId,orgQuestionId);
    	copyQuestionGroup(newQuestionId,orgQuestionId);
    	copyQuestionImage(newQuestionId,orgQuestionId);
    	copyQuestionImageValue(newQuestionId,orgQuestionId);
    	copyVisualScale(newQuestionId,orgQuestionId);
    	copyAnswers(newQuestionId,orgQuestionId);
    	copyQuestionMedicalcode(newQuestionId,orgQuestionId);
    	
    	return newQuestionId;
    }
    
    public int copyNonCopyRightQuestion(int orgQuestionId) throws CtdbException {
        // TODO copyQuestionGroup?
    	int newQuestionId = getQuestionMaxId();
    	copyNonCopyRightQuestionInfo(newQuestionId,orgQuestionId);
    	copyQuestionImageAll(newQuestionId,orgQuestionId);
    	copyQuestionGroup(newQuestionId,orgQuestionId);
    	copyQuestionImage(newQuestionId,orgQuestionId);
    	copyQuestionImageValue(newQuestionId,orgQuestionId);
    	copyVisualScale(newQuestionId,orgQuestionId);
    	copyAnswers(newQuestionId,orgQuestionId);
    	copyQuestionMedicalcode(newQuestionId,orgQuestionId);
    	
    	return newQuestionId;
    }
    
    /*public void copyQuestionByfromId(int formId) throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
    	try{
            StringBuffer sql = new StringBuffer(400);
	        sql.append("select sq.sectionid,sq.questionid,sq.questionattributesid ");
	        sql.append(" from sectionquestion sq left join section s  on  sq.sectionid=s.sectionid left join form f on s.formid=f.formid ");
            sql.append("where f.formid= ? ");   
	        stmt = this.conn.prepareStatement(sql.toString());
	        stmt.setInt(1, formId);
	        
	        rs = stmt.executeQuery();
	        int orgQuestionId=0;
	        int questionAttributesId=0;
	        Map<Integer, Integer> qustionMap = new  HashMap<Integer, Integer>();
	        while (rs.next()) {
	        	orgQuestionId = rs.getInt("questionid");
	        	questionAttributesId= rs.getInt("questionattributesid");
	        	boolean sameQuestionExist=false;
	        	int newQexistId=0;
	        	if(qustionMap.containsKey(orgQuestionId)){
	        		sameQuestionExist=true;
	        		newQexistId=(Integer) qustionMap.get(orgQuestionId);
	        	}
	        	int newQustionId=0;
				if(sameQuestionExist==false){
					newQustionId=copyQuestion(orgQuestionId);
				}else{
					newQustionId=newQexistId;
				}	        	
	        	
	        	qustionMap.put(orgQuestionId, newQustionId);
	        	updateCopiedQuestion(formId,orgQuestionId,newQustionId,questionAttributesId);	
	        	
	        } 	   		
	        
    	}
    	catch(SQLException e){
    		throw new CtdbException("Unable to get all question groups: " + e.getMessage(), e);

    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}  
    }*/
    
    public int getQuestionMaxId() throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
    	int newQuestionId = 0;
    	
        try {
	    	StringBuffer sql = new StringBuffer(50);
	    	sql.append("select nextval('question_seq')");
	        stmt = this.conn.prepareStatement(sql.toString());
	
	        rs = stmt.executeQuery();
	       
	        if (rs.next()) {
	        	newQuestionId = rs.getInt(1);
	        }   	
        }
        catch (Exception e) {
            throw new CtdbException("Unable to QuestionMaxId " + e.getMessage(), e);
        }
        finally {
        	this.close(rs);
            this.close(stmt);
        } 
        
        return newQuestionId;
    }
    
    public void copyQuestionInfo(int newQuestionId, int orgQuestionId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Question question = getMiniQuestion(orgQuestionId);
        int originalQuestionId = orgQuestionId;
        try {
        	
        	String sql = "select copyfrom from question where questionid = ?";
        	stmt = this.conn.prepareStatement(sql);
	        stmt.setLong(1, originalQuestionId);
	        rs = stmt.executeQuery();
	        
	        if ( rs.next() ) {
	        	int rsInt =  rs.getInt(1);
	        	if(rsInt != 0) {
	        		originalQuestionId = rsInt;
	        	}
	        }
        	
	        close(rs);
	        close(stmt);
        	
        	
        	
            sql = "select max(copyrightindex) from question where copyfrom = ? ";
	        stmt = this.conn.prepareStatement(sql);
	        stmt.setInt(1, originalQuestionId);
	        rs = stmt.executeQuery();
	        int nextIndex = 0;
	        
	        if (rs.next()) {
	        	nextIndex = rs.getInt(1) + 1;
	        }
	        
	        close(rs);
	        close(stmt);
	        
	        String orgQuestionName = question.getName();
	        int dashIndex = orgQuestionName.indexOf("-");
	        
	        if ( dashIndex > -1 ) { 
	        	// Copy a copied question...
	        	question.setName(orgQuestionName.substring(0, dashIndex + 1) + nextIndex);
	        }
	        // Copy an original question.
	        else {
	        	question.setName(orgQuestionName + "-" + nextIndex);
	        }
	        
	        sql = "insert into question (questionid, version, name, text, descriptionup, descriptiondown, type, defaultvalue, createdby, createddate, " +
	            	"updatedby, updateddate, includeother, unansweredvalue, iscde, copyfrom, copyright, copyrightindex, htmltext) " +
	        		"values (?, 1, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ? ) ";
	            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, newQuestionId);
            stmt.setString(2, question.getName());
            stmt.setString(3, question.getText());
            stmt.setString(4, question.getDescriptionUp());
            stmt.setString(5, question.getDescriptionDown());
            stmt.setInt(6, question.getType().getValue());
            stmt.setString(7, question.getDefaultValue());
            stmt.setLong(8, question.getCreatedBy());
            stmt.setLong(9, question.getUpdatedBy());
            stmt.setBoolean(10, question.isIncludeOtherOption());
            stmt.setString(11, question.getUnansweredValue());
            stmt.setBoolean(12, question.isCde());
            stmt.setLong(13, originalQuestionId);
            stmt.setInt(14, question.getCopyRight());
            stmt.setInt(15, nextIndex);
            stmt.setString(16, question.getHtmltext());
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to copyQuestionInfo  " + originalQuestionId + " : " + e.getMessage(), e);
        }
        finally {
        	close(rs);
            close(stmt);
        }    	
    }
    
    
    
    public void copyQuestionInfoNewFormBuilder(int newQuestionId, int orgQuestionId, String newQuestionName) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Question question = getMiniQuestion(orgQuestionId);
        int originalQuestionId = orgQuestionId;
        try {
        	

	        question.setName(newQuestionName);
	        String sql = "insert into question (questionid, version, name, text, descriptionup, descriptiondown, type, defaultvalue, createdby, createddate, " +
	            	"updatedby, updateddate, includeother, unansweredvalue, iscde, copyfrom, copyright, copyrightindex, htmltext) values " +
	        		"(?, 1, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ? ) ";
	            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, newQuestionId);
            stmt.setString(2, question.getName());
            stmt.setString(3, question.getText());
            stmt.setString(4, question.getDescriptionUp());
            stmt.setString(5, question.getDescriptionDown());
            stmt.setInt(6, question.getType().getValue());
            stmt.setString(7, question.getDefaultValue());
            stmt.setLong(8, question.getCreatedBy());
            stmt.setLong(9, question.getUpdatedBy());
            stmt.setBoolean(10, question.isIncludeOtherOption());
            stmt.setString(11, question.getUnansweredValue());
            stmt.setBoolean(12, question.isCde());
            stmt.setLong(13, 0);
            stmt.setInt(14, question.getCopyRight());
            stmt.setInt(15, 0);
            stmt.setString(16, question.getHtmltext());
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to copyQuestionInfo  " + originalQuestionId + " : " + e.getMessage(), e);
        }
        finally {
        	close(rs);
            close(stmt);
        }    	
    }
    
    public void copyNonCopyRightQuestionInfo(int newQuestionId, int orgQuestionId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Question question = getMiniQuestion(orgQuestionId);
        int originalQuestionId = orgQuestionId;
        
        try {
          
        	
        	String sql = "select copyfrom from question where questionid = ?";
        	stmt = this.conn.prepareStatement(sql);
	        stmt.setLong(1, originalQuestionId);
	        rs = stmt.executeQuery();
	        
	        if ( rs.next() ) {
	        	int rsInt =  rs.getInt(1);
	        	if(rsInt != 0) {
	        		originalQuestionId = rsInt;
	        	}
	        }
        	
	        close(rs);
	        close(stmt);
        	
        	
        	
        	
        	
        	
        	sql = "select max(copyrightindex) from question where copyfrom = ? ";
            
	        stmt = this.conn.prepareStatement(sql);
	        stmt.setLong(1, originalQuestionId);
	        rs = stmt.executeQuery();
	        int nextIndex = 0;
	        
	        if ( rs.next() ) {
	        	nextIndex = rs.getInt(1) + 1;
	        }
	        
	        close(rs);
	        close(stmt);
	        
	        // Generate the new question name.
	        String orgQuestionName = question.getName();
	        int dashIndex = orgQuestionName.indexOf("-");
	        
	        if ( dashIndex > -1 ) {
	        	// Copy a copied question...
	        	question.setName(orgQuestionName.substring(0, dashIndex + 1) + nextIndex);
	        }
	        // Copy an original question.
	        else {
	        	question.setName(orgQuestionName + "-" + nextIndex);
	        }
	        
            sql = "insert into question (questionid, version, name, text, descriptionup, descriptiondown, type, defaultvalue, createdby, createddate, " +
            	"updatedby, updateddate, includeother, unansweredvalue, iscde, copyfrom, copyright, copyrightindex) values " +
            	"(?, 1, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ? ) ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, newQuestionId);
            stmt.setString(2, question.getName());
            stmt.setString(3, question.getText());
            stmt.setString(4, question.getDescriptionUp());
            stmt.setString(5, question.getDescriptionDown());
            stmt.setInt(6, question.getType().getValue());
            stmt.setString(7, question.getDefaultValue());
            stmt.setLong(8, question.getCreatedBy());
            stmt.setLong(9, question.getUpdatedBy());
            stmt.setBoolean(10, question.isIncludeOtherOption());
            stmt.setString(11, question.getUnansweredValue());
            stmt.setBoolean(12, question.isCde());
            stmt.setLong(13, originalQuestionId);
            stmt.setInt(14, question.getCopyRight());
            stmt.setInt(15, nextIndex);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to copyQuestionInfo  " + originalQuestionId + " : " + e.getMessage(), e);
        }
        finally {
        	close(rs);
            close(stmt);
        }    	
    }
    
    public void copyQuestionGroup(int newQuestionId,int orgQuestionId) throws CtdbException {
        PreparedStatement stmt = null;
        try {
            StringBuffer sql = new StringBuffer(400);
            sql.append("insert into  questiongrpquestion (questiongroupid,questionid) ");
            sql.append(" (select questiongroupid,? from  questiongrpquestion where questionid=?)");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, orgQuestionId);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to copyQuestionGroup  " + orgQuestionId + " : " + e.getMessage(), e);

        }
        finally {
            this.close(stmt);
        }
    }   
    
    public void copyQuestionImageAll(int newQuestionId,int orgQuestionId) throws CtdbException {
        PreparedStatement stmt = null;
        try {
            StringBuffer sql = new StringBuffer(400);
            sql.append("insert into imagemap (imagemapid,questionid,questionversion,resolutionsid,imagefilename,height,width,displaygrid) ");
            sql.append(" (select nextval('imagemap_seq'), ?,questionversion,resolutionsid,imagefilename,height,");

            sql.append(" width,displaygrid from imagemap where questionid=?)   ");
                    
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, orgQuestionId);
            stmt.executeUpdate();
 
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to copyQuestionImageAll  " + orgQuestionId + " : " + e.getMessage(), e);

        }
        finally {
            this.close(stmt);
        }
    }
    
    public void copyQuestionImage(int newQuestionId,int orgQuestionId) throws CtdbException {
        PreparedStatement stmt = null;
        try {
            StringBuffer sql = new StringBuffer(400);
            sql.append("insert into questionimage (questionid,imagefilename) ");
            sql.append(" (select ?,imagefilename from questionimage  where questionid=?)");
                    
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, orgQuestionId);
            stmt.executeUpdate();
 
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to copyQuestionImageAll  " + orgQuestionId + " : " + e.getMessage(), e);

        }
        finally {
            this.close(stmt);
        }
    }    
    
    
    public void copyQuestionImageValue(int newQuestionId,int orgQuestionId) throws CtdbException {
        PreparedStatement stmt = null;
        try {
        	StringBuffer sql = new StringBuffer(400);
            sql = new StringBuffer(400);
            sql.append("insert into imagemapvalues( imagemapid ,imagerow,imagecolumn,imageoption,codevalue)");
            sql.append("(select (select imagemapid from  imagemap where questionid=?) ,imagerow,imagecolumn,imageoption,codevalue");
            sql.append(" from  imagemapvalues where imagemapid in (select imagemapid from  imagemap where questionid=?) )");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, orgQuestionId);
            stmt.executeUpdate();          
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to copyQuestionImageValue  " + orgQuestionId + " : " + e.getMessage(), e);

        }
        finally {
            this.close(stmt);
        }
    }
    
    
    public void copyVisualScale(int newQuestionId,int orgQuestionId) throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
    	try{
    		StringBuffer sql = new StringBuffer(400);
            sql.append("insert into  visualscale  ( questionid,questionversion,startrange,endrange,widthmm,lefttext,righttext,centertext,showhandle)  ");
            sql.append(" (select ?,questionversion,startrange,endrange,widthmm,lefttext,righttext,centertext,showhandle from visualscale where  questionid=?)");           
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, orgQuestionId);
            stmt.executeUpdate();
    	}
    	catch(SQLException e){
            throw new CtdbException("Unable to copyVisualScale  " + orgQuestionId + " : " + e.getMessage(), e);

    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    }   
    
    public void copyAnswers(int newQuestionId,int orgQuestionId) throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
    	try{
    		StringBuffer sql = new StringBuffer(400);
            sql.append("insert into  questionanswer (questionid,questionversion,answerid,display,codevalue,orderval,score,submittedvalue) ");           
            sql.append(" (select ?,questionversion,nextval('questionanswer_seq'),display,codevalue,orderval,score,submittedvalue from questionanswer where  questionid=?)");           
           


            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, orgQuestionId);
            stmt.executeUpdate();
    	}
    	catch(SQLException e){
            throw new CtdbException("Unable to copyAnswers  " + orgQuestionId + " : " + e.getMessage(), e);

    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}    	
    }
    
    
    public void copyCalculatedQuestions(int newQuestionId,int orgQuestionId) throws CtdbException {
    	PreparedStatement stmt = null;
        ResultSet rs = null;
    	try{
    		StringBuffer sql = new StringBuffer(400);
            sql.append("insert into  calculatequestion (questionid,calculatequestionid,sectionid,orderval) ");
            sql.append(" (select ?,calculatequestionid,sectionid,orderval from  calculatequestion where questionid=?)");           
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, orgQuestionId);
            stmt.executeUpdate();
    	}
    	catch(SQLException e){
            throw new CtdbException("Unable to copyCalculatedQuestions  " + orgQuestionId + " : " + e.getMessage(), e);

    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}  
    }
    public void copyQuestionAttribute (int newQuestionId,int orgQuestionId) throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
    	try{
    		StringBuffer sql = new StringBuffer(400);
            sql.append("insert into questionattributes"); 
            sql.append("(questionattributesid,questionid,questionversion,requiredflag,calculatedflag,calculation,skipruleflag,"); 
            sql.append("skipruletype,skipruleoperatortype,skipruleequals,halign,valign,textcolor,fontface,fontsize,indent,"); 
            sql.append("rangeoperator,rangevalue1,rangevalue2,dtconversionfactor,createdby,createddate,updatedby,updateddate");            
            sql.append(",label,answertype,mincharacters,maxcharacters,horizontaldisplay,textareaheight,textareawidth,"); 
            sql.append("textboxlength,emailtriggerid,dataspring,xhtmltext,horizdisplaybreak,data_element_name)"); 
            sql.append("(select  questionattributesid,?,questionversion,requiredflag,calculatedflag,calculation,skipruleflag,"); 
            sql.append("skipruletype,skipruleoperatortype,skipruleequals,halign,valign,textcolor,fontface,fontsize,indent,");     
            sql.append("rangeoperator,rangevalue1,rangevalue2,dtconversionfactor,createdby,createddate,updatedby,updateddate");            
            sql.append(",label,answertype,mincharacters,maxcharacters,horizontaldisplay,textareaheight,textareawidth,"); 
            sql.append("textboxlength,emailtriggerid,dataspring,xhtmltext,horizdisplaybreak,data_element_name"); 
            sql.append(" from questionattributes where  questionid=?)"); 
              
            
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, orgQuestionId);
            stmt.executeUpdate();
    	}
    	catch(SQLException e){
            throw new CtdbException("Unable to copyQuestionAttribute  " + orgQuestionId + " : " + e.getMessage(), e);

    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	} 
    }   
    
    public void copyQuestionMedicalcode(int newQuestionId,int orgQuestionId) throws CtdbException{
    	PreparedStatement stmt = null;
        ResultSet rs = null;
    	try{
    		StringBuffer sql = new StringBuffer(400);
            sql.append("insert into question_medicalcode (questionid,medicalcodeid,questionversion,medicalcodedefinitionid,"); 
            sql.append("createdby,createddate,updatedby,updateddate) ");  
            sql.append(" (select ?,medicalcodeid,questionversion,medicalcodedefinitionid,createdby,createddate,updatedby,updateddate");           
            sql.append(" from question_medicalcode  where  questionid=?)");          
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, newQuestionId);
            stmt.setInt(2, orgQuestionId);
            stmt.executeUpdate();
    	}
    	catch(SQLException e){
            throw new CtdbException("Unable to copyQuestionMedicalcode  " + orgQuestionId + " : " + e.getMessage(), e);

    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}  
    }   
    
    
    /************************************************************************
     * get METHODS       added by Ching-Heng
     ***********************************************************************/
    public List<Integer> getSectionIds(int questionId, int questionVersion) throws ObjectNotFoundException, CtdbException {
    	List<Integer> sectionIds = new ArrayList<Integer>();
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	try{
        	StringBuffer sql = new StringBuffer(200);
        	sql.append("select sectionid from sectionquestion where sectionquestion.questionid=? and sectionquestion.questionversion=?");
    		stmt = this.conn.prepareStatement(sql.toString());
    		stmt.setLong(1, questionId);
    		stmt.setInt(2, questionVersion);
    		rs = stmt.executeQuery();
    		while(rs.next()){
    			sectionIds.add(rs.getInt("SECTIONID"));
    		}
    		return sectionIds;
        }
        catch(SQLException e){
    		throw new CtdbException("Error getign for form(s) which had question attached" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    }
    
    public List<Integer> getOldSectionIds(int questionId, int questionVersion) throws ObjectNotFoundException, CtdbException {
    	List<Integer> oldSectionIds=new ArrayList<Integer>();
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	try{
        	StringBuffer sql = new StringBuffer(200);
        	sql.append("select sectionid from sectionquestionarchive where sectionquestionarchive.questionid=? and sectionquestionarchive.questionversion=?");
    		stmt = this.conn.prepareStatement(sql.toString());
    		stmt.setLong(1, questionId);
    		stmt.setInt(2, questionVersion);
    		rs = stmt.executeQuery();
    		while(rs.next()){
    			oldSectionIds.add(rs.getInt("SECTIONID"));
    		}
    		return oldSectionIds;
        }
        catch(SQLException e){
    		throw new CtdbException("Error getting form(s) which had question attached" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    }
    
    /*
     * get the Form id which the question is attached 
     */
    public Set<Integer> getAttachedForm(int questionId, int questionVersion) throws ObjectNotFoundException, CtdbException {
    	PreparedStatement stmt = null, stmt2 = null;
    	ResultSet rs = null, rs2 = null;
    	List<Integer> sectionIds=this.getSectionIds(questionId, questionVersion);
    	Iterator<Integer> secIte=sectionIds.iterator();    	
    	List<Integer> oldSectionids=this.getOldSectionIds(questionId, questionVersion);
    	Iterator<Integer> oldSecIte=oldSectionids.iterator();
    	Set<Integer> allSectionIds = new HashSet<Integer>(); // use the hash set to get the unique section id
    	
    	Set<Integer> allFormIds = new HashSet<Integer>(); // use the hash set to get the unique form id
    	while (secIte.hasNext()){
    		while(oldSecIte.hasNext()){
    			allSectionIds.add((Integer) oldSecIte.next());
    		}
    		allSectionIds.add((Integer) secIte.next());
    	}
    	
    	try {
    		StringBuffer sql = new StringBuffer(200);
			StringBuffer sql2 = new StringBuffer(200);
        	sql.append("select formid from section where sectionid = ?");
        	stmt = this.conn.prepareStatement(sql.toString());
        	sql2.append("select formid from sectionarchive where sectionid=?");
    		stmt2 = this.conn.prepareStatement(sql2.toString());
			Iterator<Integer> allSecite = allSectionIds.iterator();
			while (allSecite.hasNext()) {
				Long secId = Long.decode(allSecite.next().toString());
	        	
	        	try {
	        		stmt.setLong(1, secId);
	        		rs = stmt.executeQuery();
	        		if(rs.next()){
	        			allFormIds.add(rs.getInt("FORMID"));
	        		}
	        		
	        		stmt2.setLong(1, secId);
	        		rs2 = stmt2.executeQuery();
	        		if(rs2.next()){
	        			allFormIds.add(rs2.getInt("FORMID"));
	        		}
	        	}
	        	catch(SQLException e) {
	         		throw new CtdbException("Error for form(s) which had question attached" + e.getMessage(), e);
	         	}
	        	finally {
	        		close(rs);
	                close(rs2);
	        	}
			}
    	}
    	catch(SQLException e) {
     		throw new CtdbException("Error for form(s) which had question attached" + e.getMessage(), e);
     	}
    	catch(NumberFormatException e) {
    		throw new CtdbException("Error for form(s) which had question attached" + e.getMessage(), e);
    	}
    	finally {
    		close(stmt);
    		close(stmt2);
    	}
    	
    	return allFormIds;
    }
    
    /*
     * get the Form names which the question has been used in a calculation rule 
     */
    public Set<String> getCalDependentFormNames(int questionId) throws ObjectNotFoundException, CtdbException {
    	Set<String> calDependentFormNames = new HashSet<String>(); 
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	try{
        	StringBuffer sql = new StringBuffer(200);
        	sql.append("select distinct f.name as name from form f, section s, calculatequestion cq where f.formid = s.formid and s.sectionid = cq.sectionid and cq.calculatequestionid =?");
    		stmt = this.conn.prepareStatement(sql.toString());
    		stmt.setLong(1, questionId);
    		rs = stmt.executeQuery();
    		while(rs.next()){
    			calDependentFormNames.add(rs.getString("name"));
    		}
    		return calDependentFormNames;
        }
        catch(SQLException e){
    		throw new CtdbException("Error getign for form names which had question attached" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    }
    
    public String getImageMapFileName (int questionId, int questionVersion) throws ObjectNotFoundException, CtdbException {
    	String imageMapFileName="";
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	try{
        	StringBuffer sql = new StringBuffer(200);
        	sql.append("select imagefilename from imagemap where questionid = ? and questionversion = ?");
    		stmt = this.conn.prepareStatement(sql.toString());
    		stmt.setLong(1, questionId);
    		stmt.setInt(2, questionVersion);
    		rs = stmt.executeQuery();
    		if(rs.next()){
    			imageMapFileName=rs.getString("imagefilename");
    		}
        }
        catch(SQLException e){
    		throw new CtdbException("Error getting imageMap file name" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    	return imageMapFileName;
    }
    

    
    /***
     * Will return the image map object for given questionId and questinoVersion
     * @param questionId
     * @param questionVersion
     * @return
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    
    public ImageMapExportImport getImageMapExport(int questionId, int questionVersion) throws ObjectNotFoundException, CtdbException {
    	ImageMapExportImport iMap = new ImageMapExportImport();
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	try{
        	StringBuffer sql = new StringBuffer(200);
        	sql.append("select imagemapid , questionid,questionversion,resolutionsid,imagefilename,height,width,displaygrid from imagemap where questionid = ? and questionversion = ?");
    		stmt = this.conn.prepareStatement(sql.toString());
    		stmt.setLong(1, questionId);
    		stmt.setInt(2, questionVersion);
    		rs = stmt.executeQuery();
    		if(rs.next()){
    			iMap.setId(rs.getInt("questionid"));
    			iMap.setImageMapId(rs.getInt("imagemapid"));
    			iMap.setImageFileName(rs.getString("imagefilename"));
    			iMap.setHeight(rs.getInt("height"));
    			iMap.setWidth(rs.getInt("width"));
    			iMap.setShowGrid(rs.getBoolean("displaygrid"));
    			iMap.setGridResolution(rs.getInt("resolutionsid"));
    		}
        }
        catch(SQLException e){
    		throw new CtdbException("Error getting imageMap Object" + e.getMessage(), e);
    	}
    	finally{
    		this.close(rs);
            this.close(stmt);
    	}
    	return iMap;
    }
    
    
    public List<String> getImageMapOptions(int questionId, int questionVersion) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String> answers = new ArrayList<String>();
        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select * from imagemapvalues where imagemapid = (select imagemapid from imagemap where questionid = ? and questionversion = ?)");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
    		stmt.setInt(2, questionVersion);
            rs = stmt.executeQuery();
            while(rs.next()){
            	answers.add(rs.getString("IMAGEOPTION"));
            }
           	return answers;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get imageMap options: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    
    public List<ImageMapValuesExportImport> getImageMapValuesList(int questionId, int questionVersion) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<ImageMapValuesExportImport> iMapValuesList = new ArrayList<ImageMapValuesExportImport>();
        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select imagemapid,imagerow,imagecolumn,imageoption from imagemapvalues where imagemapid = (select imagemapid from imagemap where questionid = ? and questionversion = ?)");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
    		stmt.setInt(2, questionVersion);
            rs = stmt.executeQuery();
            ImageMapValuesExportImport iMapValues;
            while(rs.next()){
            	iMapValues = new ImageMapValuesExportImport();
            	iMapValues.setImageMapId(rs.getInt("imagemapid"));
            	//iMapValues.setCodeValue(rs.getInt("codevalue"));
            	iMapValues.setImageMapColumn(rs.getInt("imagecolumn"));
            	iMapValues.setImageMapRow(rs.getInt("imagerow"));
            	iMapValues.setImageOption(rs.getString("imageoption"));
            	iMapValuesList.add(iMapValues);
            }
           	return iMapValuesList;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get imageMap values: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    
    public String getVisualScaleInfo(int questionId, int questionVersion) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String texts="";
        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select * from VISUALSCALE where questionid = ? and questionversion = ?");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
    		stmt.setInt(2, questionVersion);
            rs = stmt.executeQuery();
            while(rs.next()){
            	texts=rs.getString("LEFTTEXT")+StrutsConstants.alienSymbol+rs.getString("CENTERTEXT")+StrutsConstants.alienSymbol+rs.getString("RIGHTTEXT")+StrutsConstants.alienSymbol+Integer.toString(rs.getInt("STARTRANGE"))+StrutsConstants.alienSymbol+Integer.toString(rs.getInt("ENDRANGE"))+StrutsConstants.alienSymbol+Integer.toString(rs.getInt("WIDTHMM"))+StrutsConstants.alienSymbol+rs.getString("SHOWHANDLE");
            }
           	return texts;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get VisualScale Texts: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    /**
     * Will return the VisualScale Object for given questionId and questionVersion
     * @param questionId
     * @param questionVersion
     * @return
     * @throws CtdbException
     * @throws SQLException
     */
    public VisualScale getVisualScale(int questionId, int questionVersion) throws CtdbException,SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        VisualScale vs=new VisualScale();
        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select questionid,questionversion,startrange,endrange,widthmm,lefttext,righttext,centertext,showhandle from VISUALSCALE where questionid = ? and questionversion = ?");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
    		stmt.setInt(2, questionVersion);
            rs = stmt.executeQuery();
            Version version;
            while(rs.next()){
            	vs.setId(rs.getInt("questionid"));
            	version = new Version();
            	version.setVersionNumber(rs.getInt("questionversion"));
            	vs.setVersion(version);
            	vs.setRangeStart(rs.getInt("startrange"));
            	vs.setRangeEnd(rs.getInt("endrange"));
            	vs.setWidth(rs.getInt("widthmm"));
            	vs.setLeftText(rs.getString("lefttext"));
            	vs.setRightText(rs.getString("righttext"));
            	vs.setCenterText(rs.getString("centertext"));
            	vs.setShowHandle(rs.getBoolean("showhandle"));
            }
           	return vs;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get VisualScale Texts: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    public List<String> getAssociatedGroupIds(int questionId) throws CtdbException {
    	PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> associatedGroupIds = new ArrayList<String>();
        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select questiongroupid from questiongrpquestion where questionid= ?");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, questionId);
            rs = stmt.executeQuery();
            while( rs.next() ) {
            	associatedGroupIds.add(rs.getString("questiongroupid"));
            }
           	return associatedGroupIds;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get VisualScale Texts: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
  
    
    public static void main(String args[]) throws CtdbException, SQLException {
    	QuestionManagerDao test= new QuestionManagerDao();
    	test.deleteCopiedQuestion(449);
    }
    
	public boolean isNameExist(String name)  throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isDuplicated = false;
        // Proceed only if the name is valid
        if( (name == null) || (name.length() == 0) ) {
        	return false;
        }

        try {
            String sql = "select count(1) from question where upper(name) = upper(?) ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, name);
            
            rs = stmt.executeQuery();
            int count = 0;
            
            if  (rs.next() ) {
                count = rs.getInt(1);
            }
            
            if ( count > 0 ) {
                isDuplicated = true;
            }
            
            return isDuplicated;
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to check if the question name already exists in the system: " + e.getMessage(), e);
        }
        finally {
        	this.close(rs);
            this.close(stmt);
        }
	}

	/**
	 * Gets the next ID of the question table.
	 * 
	 * @return The next ID that would be assigned to a new question entry.
	 * @throws CtdbException	When the next question ID can't be determined.
	 */
	public int getNextQuestionId() throws CtdbException {
		PreparedStatement stmt = null;
        ResultSet rs = null;
        int nextId = -1;
        
        try {
        	stmt = this.conn.prepareStatement("select max(questionid) from question");
        	rs = stmt.executeQuery();
        	
        	if ( rs.next() ) {
        		nextId = rs.getInt(1) + 1;
        	}
        	else {
        		throw new CtdbException("Couldn't find the next ID for a question.");
        	}
        }
        catch ( SQLException sqle ) {
        	throw new CtdbException("Unable to determine the next ID for a question.", sqle);
        }
        finally {
        	close(rs);
        	close(stmt);
        }
		
		return nextId;
	}
}
