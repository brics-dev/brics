package gov.nih.nichd.ctdb.form.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.FileUploadException;
import gov.nih.nichd.ctdb.common.FileUploader;
import gov.nih.nichd.ctdb.common.InvalidRemovalException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.emailtrigger.dao.EmailTriggerDao;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;
import gov.nih.nichd.ctdb.form.common.AdministeredSectionRemovalException;
import gov.nih.nichd.ctdb.form.common.CalculatedDependencyException;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.form.common.FormResultControl;
import gov.nih.nichd.ctdb.form.common.QuestionRemovalException;
import gov.nih.nichd.ctdb.form.common.SectionDependencyException;
import gov.nih.nichd.ctdb.form.common.SectionSkipRuleDependencyException;
import gov.nih.nichd.ctdb.form.common.SkipRuleDependencyException;
import gov.nih.nichd.ctdb.form.dao.FormManagerDao;
import gov.nih.nichd.ctdb.form.domain.CalculatedFormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.CellFormatting;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormGroup;
import gov.nih.nichd.ctdb.form.domain.FormLayout;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.util.FormCache;
import gov.nih.nichd.ctdb.form.util.FormCacheThread;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.form.util.HtmlFormCache;
import gov.nih.nichd.ctdb.form.util.XMLStoreThread;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.question.dao.QuestionManagerDao;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.InstanceType;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.util.XmlThreadManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.util.CasServiceUserHelper;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.adapters.EformMigrationAdapter;

/**
 * FormManager is a business layer object with interacts with the FormManagerDao.
 * The role of the FormManager is to enforce business rule logic and delegate data
 * layer manipulation to the FormManagerDao.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */

public class FormManager extends CtdbManager {
	
	private static Logger logger = Logger.getLogger(FormManager.class);

    /**
     * For data migration use only, no other user will be allowed
     * access. This method accepts a fully populated form object and saves all
     * sections, section form relationships, and question section relationships.
     *
     * @param form The form to migrate
     * @throws DuplicateObjectException Thrown if the form already exists in the system
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void saveMigratedForm(Form form) throws DuplicateObjectException, CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);

            if ( form.getCreatedBy() == 2 ) {
                FormManagerDao dao = FormManagerDao.getInstance(conn);
                
                for ( List<Section> row : form.getRowList() ) {
                    for ( Section section : row ) {
                        if (section != null) {
                            section.setFormId(form.getId());
                            dao.createSection(section);
                            dao.addQuestionsToSection(section.getId(), section.getQuestionList());
                        }
                    }
                }
                
                commit(conn);
            }
            else {
                throw new CtdbException("NON DATA MIGRATION USER ATTEMPTING TO SAVE FORM USING DATA MIGRATION ONLY METHOD");
            }
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    
    public boolean isFormLegacy(int formid) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).isFormLegacy(formid);
        }
        finally {
            this.close(conn);
        }
    } 
    

    /**
     * Creates a form for the protocol. The form is used to administer questions
     * and collect protocol data.
     *
     * @param form the form to create
     * @throws DuplicateObjectException Thrown if the form with the same name already
     *                                  exists for the protocol
     * @throws CtdbException            Thrown if any errors occur while processing
     */
    public int createForm(Form form) throws DuplicateObjectException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            int formId = dao.createForm(form);
            return formId;
        }
        finally {
            this.close(conn);
        }
    }
    
    public Form getForm(int protocolId, String name) throws CtdbException {
    	 Connection conn = null;

         try {
             conn = CtdbManager.getConnection();
             FormManagerDao dao = FormManagerDao.getInstance(conn);

             Form f = dao.getForm(protocolId, name);
             
             return f;

         }
         finally {
             this.close(conn);
         }
    }


    public boolean isAdministered(Form form) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).isAdministered(form);
        }
        finally {
            this.close(conn);
        }
    }

    public boolean isAdministeredSection(int sectionId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).isAdministeredSection(sectionId);
        }
        finally {
            this.close(conn);
        }
    }

    public boolean isAdministeredSection(String sectionId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).isAdministeredSection(Integer.parseInt(sectionId));
        }
        finally {
            this.close(conn);
        }
    }

        
    /**
     * Retrieves public forms for protocols not in current protocol based on search and sort options
     * in FormResultControl.
     *
     * @param currentProtocolId The Current Protocol ID for retrieving all public forms not in it
     * @param formResultControl The FormResultControl object that wraps the search and sort options
     * @return A list of all public forms that matchs the result control option
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<Form> getPublicForms(int currentProtocolId, FormResultControl formResultControl) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getPublicForms(currentProtocolId, formResultControl);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Delete the unadministered form. Database cascade delete will be performed on other tables, such
     * as FORMLAYOUT, SECTION, and SECTIONQUESTION.
     *
     * @param formId The unadministered form
     * @throws CtdbException thrown if any errors occur when processing
     */
    public void deleteForm(int formId,boolean copyright) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            Form f = dao.getForm(formId);
            QuestionManagerDao daoQ = QuestionManagerDao.getInstance(conn);
            QuestionManager qm = new QuestionManager();
            List<Integer> delQuestionList = new ArrayList<Integer>(); 
            
            for ( List<Section> row : f.getRowList() ) {
                for ( Section section : row ) {
                    if (section != null) {
                    	List<Question> questions = getSectionQuestions(section.getId());
                    	
                        if (!questions.isEmpty() && questions.size() > 0) {
                            this.deleteQuestionAttributes(questions, section.getId());
                        }
                        
                        
                            for ( Question question : questions ) {
        						delQuestionList.add(question.getId());
        		            }                        	

                    }
                }
            }
            
            //need to also delete orphan questions
            List<Integer> orphanQuestionIds = dao.getOrphanQuestionIds(formId);
            if(orphanQuestionIds.size() > 0) {
            	// add it to the list of questions to delete
            	for(int i =0;i<orphanQuestionIds.size();i++) {
            		Integer oQId = orphanQuestionIds.get(i);
            		delQuestionList.add(oQId);	
            	}
            }
            
            
            
            dao.disassociateFormGroups(formId);
            dao.deleteFormIntervalAssociations(formId);
            dao.deleteFormFormatting(formId);
            dao.deleteFormFormattingArchive(formId);
            dao.deleteForm(formId,f.getName());
            FormCache.getInstance().removeForm(formId);
            commit(conn);
            
            //remove duplicates
            Set<Integer> uniqueDelQuestionsList = new TreeSet<Integer>(delQuestionList);
            Iterator iter = uniqueDelQuestionsList.iterator();
            while(iter.hasNext()) {
            	Integer qId = (Integer)iter.next();
            	qm.deleteQuestions(qId,1);
            	
            }


        }
        catch ( SQLException sqle ) {
            if ( sqle.getMessage().toLowerCase().indexOf("constraint") > -1 ) {
                throw new InvalidRemovalException("Constraints violated deleting a question : ", sqle);
            }
            else {
                throw new CtdbException(sqle.getMessage(), sqle);
            }
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    public void deleteCopyQuestion(int formId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            QuestionManager qm = new QuestionManager();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            QuestionManagerDao daoQ = QuestionManagerDao.getInstance(conn);
            
            for ( List<Section> row : dao.getForm(formId).getRowList() ) {
                for ( Section section : row ) {
                    for ( Question question : getSectionQuestions(section.getId()) ) {
						int version = daoQ.getQuestionVersion(question.getId());
						
						qm.deleteQuestions(question.getId(),version);
                    }
                }
            }
            
            commit(conn);
        }
        catch ( SQLException sqle ) {
            if ( sqle.getMessage().toLowerCase().indexOf("constraint") > -1 ) {
                throw new InvalidRemovalException("Constraints violated deleting a question : ", sqle);
            }
            else {
                throw new CtdbException(sqle.getMessage(), sqle);
            }
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
            
    }

    /**
     * Updates the display attributes for form sections
     * specifically, the display of section and question text
     * may be extended to update any attributes by adding to map.
     *
     * @param map
     * @throws DuplicateObjectException
     * @throws CtdbException
     */
    public void updateDisplayAttributes(Form form, Map<String, Object> map) throws DuplicateObjectException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.updateDisplayAttributes(form, map);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Updates the table of contents for form sections.
     *
     * @param map contains the info to update
     * @throws DuplicateObjectException
     * @throws CtdbException
     */
    public void updateSectionsTOB(Map<String, Object> map) throws DuplicateObjectException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.updateSectionsTOB(map);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    public void addTable(int formId, int numRows, int numCols) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            for (int i = 0; i < numRows; i++) {
                dao.addRow(formId, numCols);
            }
            
            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Creates a form for the protocol. The form is used to administer questions
     * and collect protocol data.
     *
     * @param formId  the form to create
     * @param numCols the number of columns
     * @throws DuplicateObjectException Thrown if the form with the same name already
     *                                  exists for the protocol
     * @throws CtdbException            Thrown if any errors occur while processing
     */
    public void addRow(int formId, int numCols) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.addRow(formId, numCols);
            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    /**
     * Creates a form for the protocol. The form is used to administer questions
     * and collect protocol data.
     *
     * @param formId  the form to create
     * @param numCols the number of columns
     * @throws DuplicateObjectException Thrown if the form with the same name already
     *                                  exists for the protocol
     * @throws CtdbException            Thrown if any errors occur while processing
     */
    public void deleteAllRows(int formId) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.deleteAllRows(formId);
            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * adds a section to the form row
     *
     * @param formId the form to manipulate
     * @param rowId  the row to add to
     * @throws DuplicateObjectException Thrown if the form with the same name already
     *                                  exists for the protocol
     * @throws CtdbException            Thrown if any errors occur while processing
     */
    public void addSection(int formId, int rowId) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.addSection(formId, rowId);
            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    public void deleteSection(int formId, int rowId, int colId) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.deleteCellFromLayout(formId, rowId, colId);
            dao.updateSectionsAfterDeleteCell(formId, rowId, colId);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * adds a section to the form row
     *
     * @param formId the form to manipulate
     * @param row1Id the row to add to
     * @param row2Id the row to add to
     * @throws DuplicateObjectException Thrown if the form with the same name already
     *                                  exists for the protocol
     * @throws CtdbException            Thrown if any errors occur while processing
     */
    public void swapSections(int formId, int row1Id, int row2Id, int col1Id, int col2Id) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.swapSections(formId, row1Id, row2Id, col1Id, col2Id);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Creates a form from the other form. The form is used to administer questions
     * and collect protocol data.
     *
     * @param form1 The form to create
     * @param form2 The form to copy from
     * @throws DuplicateObjectException Thrown if the form with the same name already
     *                                  exists for the protocol
     * @throws CtdbException            Thrown if any errors occur while processing
     */
    public void createSaveAsForm(Form form1, Form form2, boolean isSameDataStructureId) throws DuplicateObjectException, CtdbException, SQLException {
    	int newFormId = this.createForm(form1);
    	boolean toIsCopyright = form1.isCopyRight();
    	boolean fromIsCopyright = form2.isCopyRight();

        //get the sections and questions from the form2
        List<List<Section>> sectionTable = form2.getRowList();
        boolean nonCopyrightToCopyright = false;
    
        if (fromIsCopyright == false && toIsCopyright == true) {
        	nonCopyrightToCopyright = true;
        }
        
        HashMap<Integer, Integer> oldSectionIdNewSectionIdMap = new HashMap<Integer, Integer>();
        QuestionManager qm = new QuestionManager();
        
        
        HashMap<Integer, Integer> oldQuestionIdNewQuestionIdMap = new HashMap<Integer, Integer>();
        
        for ( List<Section> row : sectionTable ) {
            for ( Section section : row ) {
                if ( section != null ) {
                	 section.setFormId(form1.getId());
                     section.setUpdatedBy(form1.getUpdatedBy());
                     
                     int oldSectionId = section.getId();
                     int newSecId = this.createSection(section);
                     
                     
                     oldSectionIdNewSectionIdMap.put(new Integer(oldSectionId), new Integer(newSecId)); 
                     section.setId(oldSectionId);  //doing this b/c the createSection puts the new section...but we need the old section further down
                     
                     
                     List<Question> questions = section.getQuestionList();
                     
                     Iterator iter = questions.iterator();
                     while(iter.hasNext()) {
                     	Question q = (Question)iter.next();
                     	int oldQId = q.getId();
                     	if(!oldQuestionIdNewQuestionIdMap.containsKey(new Integer(oldQId))) {
                     		String newQuestionName = newFormId + q.getName().substring(q.getName().indexOf("_"));
                     		int newQId = qm.copyQuestionNewFormBuilder(oldQId, newQuestionName);
                     		oldQuestionIdNewQuestionIdMap.put(new Integer(oldQId), new Integer(newQId));
                     	}
                     	
                     }
                }
 
            }

       }
        
        
        

        
        for ( List<Section> row : sectionTable ) {
            for ( Section section : row ) {
                if (section != null) {
                    List<Question> questions = section.getQuestionList();
                    
                    /*Iterator iter = questions.iterator();
                    while(iter.hasNext()) {
                    	Question q = (Question)iter.next();
                    	int oldQId = q.getId();
                    	if(!oldQuestionIdNewQuestionIdMap.containsKey(new Integer(oldQId))) {
                    		String newQuestionName = newFormId + q.getName().substring(q.getName().indexOf("_"));
                    		int newQId = qm.copyQuestionNewFormBuilder(oldQId, newQuestionName);
                    		oldQuestionIdNewQuestionIdMap.put(new Integer(oldQId), new Integer(newQId));
                    	}
                    	
                    }*/
                                    
                    
                    int oldSectionId = section.getId();
                    int newSectionId = oldSectionIdNewSectionIdMap.get(new Integer(oldSectionId));
                    
                    // TODO we need to update the repeated section parent for repeatable sections
                    int oldRepSecParent = section.getRepeatedSectionParent();
                    boolean isRepeatable = section.isRepeatable();
                    
                    if ( isRepeatable && oldRepSecParent != -1 ) {
                    	int newRepSecParent = oldSectionIdNewSectionIdMap.get(new Integer(oldRepSecParent));
                    	
                    	Section sec = new Section();
                    	
						sec.setId(newSectionId);
		                sec.setRepeatedSectionParent(newRepSecParent);
		                updateRepeatedSectionParent(sec);
                    }
                 
                    this.addQuestionsToSectionForSaveAs(oldSectionId, newSectionId, questions, isSameDataStructureId, nonCopyrightToCopyright, oldSectionIdNewSectionIdMap,oldQuestionIdNewQuestionIdMap);                    
                    this.addSkipRuleQuestionsForSaveAs(oldSectionId, newSectionId, oldSectionIdNewSectionIdMap,oldQuestionIdNewQuestionIdMap);
                    this.addCalculationsForSaveAs(oldSectionId, newSectionId, oldSectionIdNewSectionIdMap,oldQuestionIdNewQuestionIdMap);
                }
            }
        }
        
        this.updateFormLayout4SaveAs(form1, form2);
    }

    /**
     * Adds the skip rule questions to the section ID for form "Save As" function.
     *
     * @param oldSectionId The section ID for skip rule question to copy from
     * @param sectionId    The section ID for skip rule questions to add to
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void addSkipRuleQuestionsForSaveAs(int oldSectionId, int sectionId, HashMap<Integer, Integer> oldSectionIdNewSectionIdMap, HashMap<Integer, Integer> oldQuestionIdNewQuestionIdMap) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.addSkipRuleQuestionsForSaveAs(oldSectionId, sectionId, oldSectionIdNewSectionIdMap, oldQuestionIdNewQuestionIdMap);
            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Adds the calculation questions to the section ID for form "Save As" function.
     *
     * @param oldSectionId The section ID for calculation question to copy from
     * @param sectionId    The section ID for calculation questions to add to
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void addCalculationsForSaveAs(int oldSectionId, int sectionId, HashMap<Integer, Integer> oldSectionIdNewSectionIdMap, HashMap<Integer, Integer> oldQuestionIdNewQuestionIdMap) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.addCalculationsForSaveAs(oldSectionId, sectionId, oldSectionIdNewSectionIdMap, oldQuestionIdNewQuestionIdMap);
            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * @param form1 The form to create
     * @param form2 The form to copy from
     * @throws CtdbException Thrown if any errors occur while processing
     */
    private void updateFormLayout4SaveAs(Form form1, Form form2) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.updateFormLayout4SaveAs(form1, form2);
            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Updates the form for the protocol.  If passed isVersioningForced flag is true,
     * then the system archives and versions the form.  Otherwise,
     * The system archives and versions the form according to the rules in
     * updateForm(form).
     *
     * @param form               The form data object to update
     * @param isVersioningForced If the form should be forced to version.
     * @throws DuplicateObjectException Thrown if the form with the same name already
     *                                  exists for the protocol
     * @throws ObjectNotFoundException  Thrown if the form does not exist
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void updateForm(Form form, boolean isVersioningForced) throws DuplicateObjectException, ObjectNotFoundException, CtdbException {
        if (!isVersioningForced) {
            this.updateForm(form);
            return;
        }

        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.createFormArchive(form.getId());
            dao.updateForm(form, true);
            
            // currently this method is only used when a form is imported.
            // so delete the form cache..
            // TODO cache imported forms, here
            FormCache.getInstance().removeForm(form.getId());
            HtmlFormCache.getInstance().removeHtml(form.getId());
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Updates the form for the protocol. If the form metadata(status, description) has
     * been modified and the form is administed, it archives and versions the form.
     *
     * @param form The form data object to update
     * @throws DuplicateObjectException Thrown if the form with the same name already
     *                                  exists for the protocol
     * @throws ObjectNotFoundException  Thrown if the form does not exist
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void updateForm(Form form) throws DuplicateObjectException, ObjectNotFoundException, CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);

            Form oldForm = dao.getForm(form.getId());
            //if the form is administered, the form metadata has changed, and the status is not
            //checked out(for import), then needs to create form archive
            if ((dao.isAdministered(form)) && (!oldForm.equals(form))
                    && (form.getStatus().getId() != FormConstants.STATUS_CHECKEDOUT)) {
                dao.createFormArchive(form.getId());
                dao.updateForm(form, true);
            } else {
                dao.updateForm(form, false);
            }
            dao.disassociateFormGroups(form.getId());

            if (form.getFormGroups() != null && form.getFormGroups().length > 0) {
                dao.associateFormToGroups(form.getId(), form.getFormGroups());
            }
            
            commit(conn);
            
            if (form.getStatus().getId() == FormConstants.STATUS_EXTERNAL) {
                // update the xml in the form table
                FormCache.getInstance().removeForm(form.getId());
                HtmlFormCache.getInstance().removeHtml(form.getId());
                new XMLStoreThread(form.getId()).start();
            }
            else if (form.getStatus().getId() == FormConstants.STATUS_ACTIVE) {
                new FormCacheThread(form.getId()).start();
            }
            else {
                FormCache.getInstance().removeForm(form.getId());
                HtmlFormCache.getInstance().removeHtml(form.getId());
            }
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    /**
     * Updates the form for the protocol. If the form metadata(status, description) has
     * been modified and the form is administed, it archives and versions the form.
     *
     * @param form The form data object to update
     * @throws DuplicateObjectException Thrown if the form with the same name already
     *                                  exists for the protocol
     * @throws ObjectNotFoundException  Thrown if the form does not exist
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void updateFormInfo(Form form) throws DuplicateObjectException, ObjectNotFoundException, CtdbException {

        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            Form oldForm = dao.getForm(form.getId());
            
            //if the form is administered, the form metadata has changed, and the status is not
            //checked out(for import), then needs to create form archive
            if ((dao.isAdministered(form)) && (!oldForm.equals(form))
                    && (form.getStatus().getId() != FormConstants.STATUS_CHECKEDOUT)) {
                dao.createFormArchive(form.getId());
                dao.updateForm(form, true);
            } else {
                dao.updateForm(form, false);
            }

            commit(conn);
            
            if (form.getStatus().getId() == FormConstants.STATUS_EXTERNAL) {
                // update the xml in the form table
                FormCache.getInstance().removeForm(form.getId());
                HtmlFormCache.getInstance().removeHtml(form.getId());
                new XMLStoreThread(form.getId()).start();
            }
            else if (form.getStatus().getId() == FormConstants.STATUS_ACTIVE) {
                new FormCacheThread(form.getId()).start();
            }
            else {
                FormCache.getInstance().removeForm(form.getId());
                HtmlFormCache.getInstance().removeHtml(form.getId());
            }
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Updates the form status for the protocol.
     *
     * @param form The form data object to update.
     * @throws ObjectNotFoundException Thrown if the form does not exist
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public void updateFormStatus(Form form) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.updateFormStatus(form);

            commit(conn);
            FormCache.getInstance().removeForm(form.getId());
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Retrieves a form for the protocol based on the unique ID. The form is considered
     * as minimun form for only having id, name, description, or something which can
     * be added later.  The form definitely can't have section and questions retrieved.
     *
     * @param formId The Form ID to retrieve
     * @return Form data object
     * @throws ObjectNotFoundException Thrown if the protocol form does not exist
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Form getMiniForm(int formId) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            Form form = dao.getMiniForm(formId);

            return form;
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Retrieves all question ids for a form in order of their questionorder value.
     *
     * @param formid The form to retrieve the question from
     * @return The list of question ids in the form ordered by their questionorder values.
     * @throws ObjectNotFoundException Thrown if the question/form combination does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public List<Integer> getQuestionOrderInForm(int formid) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;
        List<Integer> questionIds = new ArrayList<Integer>();

        try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);

            for ( List<Section> row : dao.getSections(formid) ) {
                for ( Section section : row ) {
                    if (section != null) {
                        dao.getQuestionIdsInSection(section.getId(), questionIds);
                    }
                }
            }
        }
        finally {
            this.close(conn);
        }
        
        return questionIds;
    }

    /**
     * Retrieves all question ids for a form of a given version in order of their questionorder value.
     *
     * @param formid      The form to retrieve the question from
     * @param formVersion The form version number.
     * @return The list of question ids in the form ordered by their questionorder values.
     * @throws ObjectNotFoundException Thrown if the question/form combination does not exist in the system
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public List<Integer> getQuestionOrderInForm(int formid, int formVersion) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;
        List<Integer> questionIds = new ArrayList<Integer>();

        try {
            conn = CtdbManager.getConnection();
            
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            for ( List<Section> row : dao.getSections(formid, formVersion) ) {
                for ( Section section : row ) {
                    if (section != null) {
                        dao.getQuestionIdsInSection(section.getId(), formVersion, questionIds);
                    }
                }
            }
        }
        finally {
            this.close(conn);
        }
        
        return questionIds;
    }
    
    /**
     * Retrieves number of questions in a form
     *
     * @param formId      The form ID
     * @param formVersion The form version
     * @return number of questions
     * @throws CtdbException thrown if any errors occur while processing
     */
    public int getNumberQuestions(int formId, int formVersion) throws CtdbException {
        Connection conn = null;
        int numQuestions = 0;

        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            numQuestions = dao.getNumberQuestions(formId, formVersion);
        }
        finally {
            this.close(conn);
        }
        
        return numQuestions;
    }

    public Form getForm(int formId, boolean NOCACHE) throws ObjectNotFoundException, CtdbException {
    	return getFormAndSetofQuestionIds(formId, new TreeSet<Integer>());
    }

    public Form getFormAndSetofQuestions(int formId) throws ObjectNotFoundException, CtdbException { 
    	// TODO Tsega: need to split this into two functions: getForm & getFormAndSetofQuestions
        Form f = getFormAndSetofQuestionIds(formId, new TreeSet<Integer>());
        return f;
    }
    
    public String getFormTypeString(int formType) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;
    	
    	try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            String name = dao.getFormTypeString(formType);
            return name;
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Method to get form id for particular form name and protocol
     * @param protocolId
     * @param name
     * @return formId
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public int getFormID(int protocolId, String name) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;

    	try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            return dao.getFormID(protocolId, name);
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Method to get form id for particular form shortname and protocol
     * @param protocolId
     * @param shortname
     * @return formId
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public int getFormIdByShortName(int protocolId, String shortname) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;

    	try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            return dao.getFormIdByShortName(protocolId, shortname);
        }
        finally {
            this.close(conn);
        }
    }
    
    
    
    /**
     * Method to get eform shortnmae for particular eformid and protocol
     * @param protocolId
     * @param name
     * @return formId
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public String getEFormShortNameByEFormId( int eformId) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;

    	try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            return dao.getEFormShortNameByEFormId(eformId);
        }
        finally {
            this.close(conn);
        }
    }
    
    
    
    /**
     * Method to get eform name for particular eformid and protocol
     * @param protocolId
     * @param name
     * @return formId
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public String getEFormNameByEFormId( int eformId) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;

    	try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            return dao.getEFormNameByEFormId(eformId);
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Method to get eform shortnmae for particular eformid and protocol
     * @param protocolId
     * @param name
     * @return formId
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public String getEFormShortNameByAFormId( int aformId) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;

    	try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            return dao.getEFormShortNameByAFormId(aformId);
        }
        finally {
            this.close(conn);
        }
    }
    
    public boolean areAllAFormsTheSameEForm(int[] aformIds) throws CtdbException {
    	Connection conn = null;
    	
    	try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            return dao.areAllAFormsTheSameEForm(aformIds);
        }
        finally {
            this.close(conn);
        }
    }
    
    public boolean areAllAFormsLocked(int[] aformIds) throws CtdbException {
    	Connection conn = null;
    	
    	try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            return dao.areAllAFormsLocked(aformIds);
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Method to get eform shortnmae for particular eformid and protocol
     * @param protocolId
     * @param name
     * @return formId
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public String[] getEFormShortNameAndProtocolIdByAFormId(int aformId) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;

    	try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            return dao.getEFormShortNameAndProtocolIdByAFormId(aformId);
        }
        finally {
            this.close(conn);
        }
    }
    
    
    
    
    
    
    
    
    /**
     * Method to get formName for particular formId and protocol
     * @param protocolId
     * @param formId
     * @return formName
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public String getFormNameNP(int protocolId, int formId) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;

    	try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            return dao.getFormNameNP(protocolId, formId);
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Method to get formName for particular formId and protocol
     * @param protocolId
     * @param formId
     * @return formName
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public String getFormNameP(int protocolId, int formId) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;

    	try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            return dao.getFormNameP(protocolId, formId);
        }
        finally {
            this.close(conn);
        }
    }
    
    public String getFormNameForEFormId(int protocolId, int eformId) throws ObjectNotFoundException, CtdbException {
     	Connection conn = null;

     	try {
             conn = CtdbManager.getConnection();

             FormManagerDao dao = FormManagerDao.getInstance(conn);
             return dao.getFormNameForEFormId(protocolId, eformId);
         }
         finally {
             this.close(conn);
         }
     }
    
    public Question getSectionQuestion(int sectionId, int questionId) throws CtdbException {
    	Connection conn = null;
     	try {
             conn = CtdbManager.getConnection();
             FormManagerDao dao = FormManagerDao.getInstance(conn);
             return dao.getSectionQuestion(sectionId, questionId);
         }
         finally {
             this.close(conn);
         }
    }

    /**
     * Retrieves a current form with the most recent version for the protocol based on the unique ID. The form has all its
     * sections and intervals. The section has its all questions.
     *
     * @param formId The Form ID to retrieve
     * @return Form data object
     * @throws ObjectNotFoundException Thrown if the protocol form does not exist
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Form getFormAndSetofQuestionIds(int formId, Set<Integer> tehSet) throws ObjectNotFoundException, CtdbException {
    	logger.info("FormManager->getFormAndSetofQuestionIds()");
        Connection conn = null;
        // TODO this method only called directly from import form action, don't consider cache?
        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            Form form = dao.getForm(formId);

            QuestionManager qm = new QuestionManager();
            
            int rowNum = 0;
            for ( List<Section> row : form.getRowList() ) {
                for ( Section section : row ) {
                    if (section != null) {
                        List<Question> qList = new ArrayList<Question>();
                        int questionOrder = 0;
                        int i = 0;
                        
                        for ( Question question : qm.getQuestions(section) ) {
                            FormQuestionAttributes qAttrs = dao.getFormQuestionAttributes(section.getId(), question.getId());
                            qAttrs.setInstanceType(question.getInstanceType());
                            question.setFormQuestionAttributes(qAttrs);
                            
                            if (qAttrs.hasSkipRule()) {
                                form.setHasSkipRule(true);
                                List<Question> skipQuestions = qm.getSkipQuestions(question.getId(), section.getId());
                                qAttrs.setQuestionsToSkip(skipQuestions);
                            }

                            if (qAttrs instanceof CalculatedFormQuestionAttributes) {
                                question.setCalculatedFormQuestionAttributes((CalculatedFormQuestionAttributes) qAttrs);
                                form.setHasCalculationRule(true);
                            }

                            // added by Ching Hneg
                            if( question.getType().getValue() == QuestionType.IMAGE_MAP.getValue() ) {
                            	form.setHasImageMap(true);
                            }

                            qList.add(question);
                            tehSet.add(new Integer(question.getId()));
                            form.getQuestionLocator().put(question.getId(), ((rowNum << 18) | (i << 9) | (questionOrder)));
                            questionOrder++;
                            i++;
                        }
                        section.setQuestionList(qList);
                    }
                }
                
                rowNum++;
            }

            form.setFormGroups(dao.getFormsGroups(form.getId()));
            form.setCellFormatting(dao.getCellFormattingMap(form.getId()));

            return form;
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Gets a ProFoRMS Form object from a BRICS eForm object via a webservice login through
     * CAS.  This allows ProFoRMS to log in in place of the user and enables CAS-protected
     * webservices.
     * 
     * @param formShortName short name of form to retrieve
     * @return Form form or null if unable to retrieve
     */
    public Form getFormWithoutSession(String formShortName) {
    	Form output = null;
    	FormDataStructureUtility fsUtil = new FormDataStructureUtility();
		String loginUrl = SysPropUtil.getProperty("webservice.cas.ticket.url");
		String serviceUrl = SysPropUtil.getProperty("webservice.cas.dd.service.url");
		String username = SysPropUtil.getProperty("webservice.cas.proforms.username");
		String password = SysPropUtil.getProperty("webservice.cas.proforms.password");
		Eform eform = CasServiceUserHelper.getEformWithoutSession(loginUrl, serviceUrl, username, password, formShortName);
		if (eform != null) {
			output = fsUtil.transformBricsEform(eform);
		}
		return output;
    }

    public List<Form> getCompleteExternalForms() throws ObjectNotFoundException, CtdbException {
        Connection conn = null;
        QuestionManager qm = new QuestionManager();
        List<Form> forms = null;

        try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            forms = dao.getExternalForms();
            
            for ( Form form : forms ) {
                for ( List<Section> row : form.getRowList() ) {
                    for ( Section section : row ) {
                        if (section != null) {
                            section.setQuestionList(qm.getMiniQuestions(section));
                        }
                    }
                }
            }
        }
        finally {
            this.close(conn);
        }
        
        return forms;
    }

    /**
     * Retrieves a form based on the unique ID. The form has all its sections without questions.
     *
     * @param formId The Form ID to retrieve
     * @return Form data object
     * @throws ObjectNotFoundException Thrown if the protocol form does not exist
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Form getFormAndSections(int formId) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            Form form = dao.getFormAndSections(formId);
            form.setCellFormatting(dao.getCellFormattingMap(form.getId()));

            return form;
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Retrieves a form for the protocol based on form ID and form Version. The form has all its
     * sections and intervals. The section has its all questions which are full question domain objects.
     * The form questions are retrieved based on the form version.
     *
     * @param formId      The form ID to retrieve
     * @param formVersion The form version to retrieve
     * @return Form data object
     * @throws ObjectNotFoundException Thrown if the protocol form does not exist
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Form getForm(int formId, int formVersion) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            Form form = dao.getForm(formId, formVersion);
            QuestionManager qm = new QuestionManager();
            List<Question> questions = new ArrayList<Question>();
            int rowNum = 0;
            
            for ( List<Section> row : form.getRowList() ) {
                for ( Section section : row ) {
                    if (section != null) {
                        questions = qm.getQuestions(section, formVersion);
                        List<Question> qList = new ArrayList<Question>();
                        int questionOrder = 0;
                        int i = 0;
                        
                        for ( Question question : questions ) {
                            InstanceType currType = question.getInstanceType();
                            FormQuestionAttributes qAttrs = dao.getFormQuestionAttributes(section.getId(), question.getId());
                            
                            if (qAttrs.hasSkipRule()) {
                                List<Question> skipQuestions = qm.getSkipQuestions(question.getId(), section.getId());
                                qAttrs.setQuestionsToSkip(skipQuestions);
                            }

                            if (qAttrs.isCalculatedQuestion()) {
                            	CalculatedFormQuestionAttributes calFormQuestAttrs = (CalculatedFormQuestionAttributes) qAttrs;
                                calFormQuestAttrs.setQuestionsToCalculate(qm.getCalculateQuestions(calFormQuestAttrs, section.getId()));
                            }
                            
                            qAttrs.setSectionId(section.getId());
                            question.setFormQuestionAttributes(qAttrs);
                            qAttrs.setInstanceType(currType);  // instance type was deleted when getting question attributes.

                            if (qAttrs instanceof CalculatedFormQuestionAttributes) {
                                question.setCalculatedFormQuestionAttributes((CalculatedFormQuestionAttributes) qAttrs);
                            }

                            qList.add(question);
                            form.getQuestionLocator().put(question.getId(), ((rowNum << 18) | (i << 9) | (questionOrder)));
                            questionOrder++;
                            i++;
                        }
                        
                        section.setQuestionList(questions);
                    }
                }
                
                rowNum++;
            }
            
            form.setCellFormatting(dao.getCellFormattingMap(form.getId(), formVersion));
            
            return form;
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Retrieves a form for the protocol based on form ID and form Version. The form has all its
     * sections and intervals. No section questions are retrieved.
     *
     * @param formId The form ID to retrieve
     * @return Form data object
     * @throws ObjectNotFoundException Thrown if the protocol form does not exist
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Form getFormNoQuestions(int formId) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            Form form = dao.getForm(formId);

            return form;
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Retrieves a form for the protocol based on form ID and form Version. The form has all its
     * sections and intervals. No question attributes are retrieved.
     *
     * @param formId
     * @return
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public Form getFormNoQuestionAttribues(int formId) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;
        Form form = null;

        try {
            conn = CtdbManager.getConnection();
            
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            form = dao.getForm(formId);
            QuestionManager qm = new QuestionManager();
            
            for ( List<Section> row : form.getRowList() ) {
                for ( Section section : row ) {
                    if (section != null) {
                        section.setQuestionList(qm.getQuestions(section));
                    }
                }
            }
        }
        finally {
            this.close(conn);
        }
        
        return form;
    }

    /**
     * Retrieves forms with only form names and ids for the protocol based on
     * the unique protocol ID
     *
     * @param protocolId The Protocol ID for retrieving all its forms
     * @return A list of all forms for the protocol
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<Form> getFormIdNames(int protocolId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getFormIdNames(protocolId);
        }
        catch (Exception e) {
            throw new CtdbException("Unable to get forms for the protocol ID: " + protocolId + " : " + e.getMessage(), e);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Retrieves all forms for the protocol based on the unique protocol ID
     *
     * @param protocolId The protocol ID for retrieving all forms
     * @return a list of all forms for the protocol
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Form> getForms(int protocolId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getForms(protocolId);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Retrieves all forms for the protocol based on the unique protocol ID
     *
     * @param protocolId The protocol ID for retrieving all forms
     * @return a list of all forms for the protocol
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Form> getStudyForms(int protocolId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getStudyForms(protocolId);
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Retrieves only the subject forms that are associated to a specific study.
     * 
     * @param studyId - The ID of the associated study
     * @return	A list of Form objects.
     * @throws CtdbException	If a database error occurred while querying the data.
     */
    public List<Form> getStudySubjectForms(long studyId) throws CtdbException
    {
    	Connection conn = null;
    	List<Form> formList = null;
    	
    	try {
    		conn = getConnection();
    		formList = FormManagerDao.getInstance(conn).getStudySubjectForms(studyId);
    	}
    	finally {
    		close(conn);
    	}
    	
    	return formList;
    }
    
    /**
     * Retrieves all forms with Active status for the protocol based on the unique protocol ID
     *
     * @param protocolId The protocol ID for retrieving all forms
     * @return List  a list of all active forms for the protocol
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Form> getActiveAndInProgressForms(int protocolId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getActiveAndInProgressForms(protocolId);
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Retrieves all forms with Active status for the protocol based on the unique protocol ID
     *
     * @param protocolId The protocol ID for retrieving all forms
     * @return List  a list of all active forms for the protocol
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Form> getActiveForms(int protocolId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getActiveForms(protocolId);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Retrieves all forms with Active Flag for the protocol set
     *
     * @param protocolIds The set of protocol IDs for retrieving all forms
     * @param activeFlag  The active flag for all forms
     * @return List  a list of all active forms for the protocol
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Form> getAllForms(Set<String> protocolIds, boolean activeFlag) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getAllForms(protocolIds, activeFlag);
        }
        finally {
            this.close(conn);
        }
    }

    public StringBuffer getJSFormMap() throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getJSFormMap();
        }
        finally {
            this.close(conn);
        }
    }
    
    public List<Form> getFormsWithoutSample(int protocolId, FormResultControl formResultControl) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getFormsWithoutSample(protocolId, formResultControl);
        }
        finally {
            this.close(conn);
        }
    }
    
    public String[] getDataElementGeoupAndName(int sectionId, int questionId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getDataElementGroupAndName(sectionId, questionId);
        }
        finally {
            this.close(conn);
        }
    }
    
    public String getDataStructureName(int formid) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getDataStructureName(formid);
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Retrieves forms for the protocol based on search values in
     * FormResultControl object
     *
     * @param protocolId        the protocol ID
     * @param formResultControl FormResultControl object wraps the search values
     * @return List of all forms that matchs the search values
     */
    public List<Form> getForms(int protocolId, FormResultControl formResultControl) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getForms(protocolId, formResultControl);
        }
        finally {
            this.close(conn);
        }
    }
    
    public List<Form> getOtherStudyMineForms(int protocolId,int userId, FormResultControl formResultControl) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getOtherStudyMineForms(protocolId,userId,formResultControl);
        }
        finally {
            this.close(conn);
        }
    }  
    
    
    public List<Form> getOtherStudyAllForms(int protocolId,int userId ,FormResultControl formResultControl) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getOtherStudyAllForms(protocolId,userId,formResultControl);
        }
        finally {
            this.close(conn);
        }
    }        
    
    public String getStatusName(int id) throws CtdbException {
    	 Connection conn = null;
         try {
             conn = CtdbManager.getConnection();
             return FormManagerDao.getInstance(conn).getStatusName(id);
         }
         finally {
             this.close(conn);
         }
    	
    }

     public Map<Integer, List<Form>> getNpFormMap(int protocolId, FormResultControl formResultControl) throws CtdbException {
        Connection conn = null;
        Map<Integer, List<Form>> a = new HashMap<Integer, List<Form>>();
        
        try {
            conn = CtdbManager.getConnection();
            formResultControl.setSortBy(" form.formtypeid, orderval " );
            List<Form> forms = FormManagerDao.getInstance(conn).getForms(protocolId, formResultControl);
            int curtype = Integer.MAX_VALUE;
            List<Form> formList = new ArrayList<Form>();
            
            for ( Form f : forms ) {
                if (curtype != f.getFormType() && curtype != Integer.MAX_VALUE){
                    a.put(curtype, formList);
                    formList = new ArrayList<Form>();
                }
                
                formList.add(f);
                curtype = f.getFormType();
            }
            
            a.put(curtype, formList);
        }
        finally {
            this.close(conn);
        }
        
        return a;
    }
     
    /**
     * Retrieves the forms for the interval ID.
     *
     * @param intervalId The interval id for the form
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Form> getFormsForInterval(int intervalId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getFormsForInterval(intervalId);
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Updates the ordering of active and in progress forms for the protocol.
     *
     * @param protocolId The protocol ID that the forms belong to
     * @param orderedIds The form IDs in order of display
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void updateActiveFormOrdering(int protocolId, String[] orderedIds, int userId) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);

            dao.updateFormOrderingBetter(orderedIds, userId);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Creates a section for the form. It saves the current version of form to the archive
     * table and creates the new version for the form.
     *
     * @param section The section to create
     * @throws DuplicateObjectException Thrown if the section with the same name already
     *                                  exists for the form
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public int createSection(Section section) throws DuplicateObjectException, CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            if (dao.isAdministered(section.getFormId())) {
                dao.createFormArchive(section.getFormId());
                dao.updateFormVersion(section.getUpdatedBy(), section.getFormId());
            }
            int secId = dao.createSection(section);
            commit(conn);
            return secId;
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    /**
     * Creates a section for the form. It saves the current version of form to the archive
     * table and creates the new version for the form.
     *
     * @param section The section to create
     * @throws DuplicateObjectException Thrown if the section with the same name already
     *                                  exists for the form
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public int createSection(Section section, boolean returnId) throws DuplicateObjectException, CtdbException {
        Connection conn = null;
        int sectionId = 0;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            if (dao.isAdministered(section.getFormId())) {
                dao.createFormArchive(section.getFormId());
                dao.updateFormVersion(section.getUpdatedBy(), section.getFormId());
            }
            
            sectionId = dao.createSection(section,true);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
        
        return sectionId;
    }

    /**
     * Updates the section for the form. If the section metadata has modified and
     * the form is administered, it archives and versions the form.
     *
     * @param section - The section data object to update
     * @throws DuplicateObjectException Thrown if the section already exists
     * @throws ObjectNotFoundException  Thrown if the form does not exist
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void updateSection(Section section) throws DuplicateObjectException, ObjectNotFoundException, CtdbException {

        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);

            Section oldSection = dao.getSection(section.getId());

            Form tmpform = new Form();
            tmpform.setId(section.getFormId());
            
            // TODO josh note: I swapped the following line and the if statement below because the other way
            // around was breaking on section dragging.  I'm testing this methodology.
            dao.updateSection(section);
            if (dao.isAdministered(tmpform) && !oldSection.equals(section)) {
                dao.createFormArchive(section.getFormId());
                Form form = dao.getForm(section.getFormId());
                dao.updateFormVersion(form);
            }
            
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    /**
     * Updates the section for the form. If the section metadata has modified and
     * the form is administed, it archives and versions the form.
     *
     * @param section The section data object to update
     * @throws DuplicateObjectException Thrown if the section already exists
     * @throws ObjectNotFoundException  Thrown if the form does not exist
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void updateRepeatedSectionParent(Section section) throws DuplicateObjectException, ObjectNotFoundException, CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);

            dao.updateRepeatedSectionParent(section);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    public int getQuestionIdFromSectionQuestion(int sectionId, int questionOrder) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;
    	
    	try {
    		conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
    		FormManagerDao dao = FormManagerDao.getInstance(conn);
    		
    		return  dao.getQuestionIdFromSectionQuestion(sectionId, questionOrder);
    	}
    	finally {
    		this.close(conn);
    	}
	}
    
    /**
     * Retrieves a section based on the unique ID
     *
     * @param sectionId The section to retrieve
     * @return Section  Section data object
     * @throws ObjectNotFoundException Thrown if the section does not exist
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Section getSection(int sectionId) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;
        Section section = null;

        try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            section = dao.getSection(sectionId);
            QuestionManager qm = new QuestionManager();
            List<Question> newList = new ArrayList<Question>();
            
            for ( Question question : qm.getQuestions(section) ) {
                FormQuestionAttributes qAttrs = dao.getFormQuestionAttributes(sectionId, question.getId());
                
                if (qAttrs instanceof CalculatedFormQuestionAttributes) {
                    question.setCalculatedFormQuestionAttributes((CalculatedFormQuestionAttributes) qAttrs);
                }

                question.setFormQuestionAttributes(qAttrs);
                newList.add(question);
            }
            section.setQuestionList(newList);
        }
        finally {
            this.close(conn);
        }
        
        return section;
    }

    /**
     * Retrieves a section based on the unique ID
     * The returned section contains questions that only have meta data.
     *
     * @param sectionId The section to retrieve
     * @return Section  Section data object
     * @throws ObjectNotFoundException Thrown if the section does not exist
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Section getMiniSection(int sectionId) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;
        Section section = null;

        try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            section = dao.getSection(sectionId);
            QuestionManager qm = new QuestionManager();
            List<Question> questions = qm.getMiniQuestions(section);
            section.setQuestionList(questions);
        }
        finally {
            this.close(conn);
        }
        
        return section;
    }

    /**
     * Retrieves a section based on the unique ID. The returned section contains questions that only have meta data
     * includes question attributes.
     *
     * @param sectionId The section to retrieve
     * @return Section  Section data object
     * @throws ObjectNotFoundException Thrown if the section does not exist
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public Section getSectionForSectionHome(int sectionId) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;
        Section section = null;

        try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            section = dao.getSection(sectionId);
            QuestionManager qm = new QuestionManager();
            List<Question> newList = new ArrayList<Question>();
            
            for ( Question question : qm.getMiniQuestions(section) ) {
                FormQuestionAttributes qAttrs = dao.getFormQuestionAttributes(sectionId, question.getId());
                question.setFormQuestionAttributes(qAttrs);

                if (qAttrs instanceof CalculatedFormQuestionAttributes) {
                    question.setCalculatedFormQuestionAttributes((CalculatedFormQuestionAttributes) qAttrs);
                }

                newList.add(question);
            }
            
            section.setQuestionList(newList);
        }
        finally {
            this.close(conn);
        }
        
        return section;
    }

    /**
     * Retrieves all sections for the form based on the unique form ID
     * The questions in the sections contain only meta data.
     *
     * @param formId The form ID for retrieving all sections
     * @return A list of all sections for the form
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<List<Section>> getMiniSections(int formId) throws CtdbException {
        Connection conn = null;
        List<List<Section>> rows = null;
        
        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            rows = dao.getSections(formId);
            QuestionManager qm = new QuestionManager();
            
            for ( List<Section> row : rows ) {
                for ( Section section : row ) {
                    if (section != null) {
                    	List<Question> questions = qm.getMiniQuestions(section);
                        section.setQuestionList(questions);
                    }
                }
            }
        }
        finally {
            this.close(conn);
        }
        
        return rows;
    }

    /**
     * Retrieves all sections for the form based on the unique form ID
     *
     * @param formId The form ID for retrieving all sections
     * @return A list of all sections for the form
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<List<Section>> getSections(int formId) throws CtdbException {
        Connection conn = null;
        List<List<Section>> sections = null;
        
        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            sections = dao.getSections(formId);

            QuestionManager qm = new QuestionManager();
            
            for ( List<Section> row : sections ) {
                for ( Section section : row ) {
                    if (section != null) {
                        List<Question> questions = qm.getQuestions(section);
                        section.setQuestionList(questions);
                    }
                }
            }
        }
        finally {
            this.close(conn);
        }
        
        return sections;
    }

    /**
     * Retrieves all sections for the form based on the unique form ID
     *
     * @param formId The form ID for retrieving all sections
     * @return A list of all sections for the form
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Section> getSectionsNoRows(int formId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            return dao.getSectionsNoRows(formId);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Retrieves all sections for the form based on the unique form ID
     *
     * @param formId The form ID for retrieving all sections
     * @return A list of all sections for the form
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Section> getSectionsNoRows(int formId, Question currentQuestion) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            List<Section> sections = dao.getSectionsNoRows(formId);
            QuestionManager qm = new QuestionManager();
            List<Question> questions;
            
            for ( Section section : sections ) {
                questions = qm.getMiniQuestions(section);
                List<Question> newList = new ArrayList<Question>();
                
                for ( Question question : questions ) {
                    if (!this.isParentSkipRuleExcluding(formId, question, currentQuestion) &&
                            !this.isChildSkipRuleExcluding(formId, question, currentQuestion)) {
                        newList.add(question);
                    }
                }
                
                section.setQuestionList(newList);
            }

            return sections;
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Checks to see if the question is a child of any skip rules on the form.
     *
     * @param formId   The form ID to look for all skip rules.
     * @param question The question to check
     * @return boolean   true if the question is a child of the skip rules on the form; false otherwise.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public boolean isChildSkipRule(int formId, Question question) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            return dao.isChildSkipRule(formId, question);
        }
        finally {
            this.close(conn);
        }
    }

    public boolean isNameExist(String name, int studyId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            return dao.isFormNameExist(name, studyId);
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Checks to see if the question is a child of any skip rules on the section.
     *
     * @param formId          The form ID to look for all skip rules.
     * @param question        The question to check
     * @param currentQuestion the current question to exclude
     * @param currentQuestion the current question to exclude for the check
     * @return boolean   true if the question is a child of the skip rules on the form; false otherwise.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public boolean isChildSkipRuleExcluding(int formId, Question question, Question currentQuestion) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            return dao.isChildSkipRuleExcluding(formId, question, currentQuestion);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Checks to see if the question is a parent of any skip rules on the current section.
     *
     * @param formId          The form ID to look for all skip rules.
     * @param question        The question to check
     * @param currentQuestion the current question to exclude
     * @return boolean   true if the question is a child of the skip rules on the form; false otherwise.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public boolean isParentSkipRuleExcluding(int formId, Question question, Question currentQuestion) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            return dao.isParentSkipRuleExcluding(formId, question, currentQuestion);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Checks to see if the question is involved in any skip rules on the form.
     *
     * @param formId   The form ID to look for all skip rules.
     * @param question The question to check
     * @return boolean   true if the question is involved in the skip rules on the form; false otherwise.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public boolean inSkipRules(int formId, Question question, Section currentSection) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            return dao.inSkipRules(formId, question, currentSection);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Updates the ordering of sections for the form. It archives and versions the form
     * if the form is administed. This ordering will be used when displaying sections for
     * the form.
     *
     * @param formId     The form ID that the sections belong to
     * @param orderedIds The section IDs in order of display
     * @throws CtdbException Thrown if any errors occur while processing
     * @throws NumberFormatException	If a section ID cannot be converted to a number.
     */
    public void updateSectionOrdering(int formId, String[] orderedIds) throws CtdbException, NumberFormatException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            Form tmpform = new Form();
            tmpform.setId(formId);
            
            if (dao.isAdministered(tmpform)) {
                dao.createFormArchive(formId);
                Form form = dao.getForm(formId);
                dao.updateFormVersion(form);
            }
            
            Section section;
            
            for (int idx = 0; idx < orderedIds.length; idx++) {
                int sectionId = Integer.parseInt(orderedIds[idx]);
                section = dao.getSection(sectionId);
                dao.updateSectionOrdering(section, idx + 1);
            }
            
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    public void deleteRow(int formId, int rowId, User u) throws CtdbException, SectionDependencyException,
            SectionSkipRuleDependencyException, CalculatedDependencyException, AdministeredSectionRemovalException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            Form form = this.getFormAndSetofQuestions(formId);
            this.validateDeleteRow(form, rowId, dao);
            
            // delete the sections if ok
            for ( List<Section> row : form.getRowList() ) {
                for ( Section s : row ) {
                    if (s != null && s.getRow() == rowId) {
                        deleteSection(s.getId(), u);
                    }
                }
            }

            // delete the row from the db also.
            dao.deleteRow(formId, rowId);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Moves a row on the layout page based on the direction.
     *
     * @param formId    The form object that contains the row
     * @param rowId     The row id indicates the number of rows
     * @param direction The direction indicates moving the row UP or DOWN
     * @throws CtdbException Thrown if any errors occur during the process
     */
    public void reorderRow(int formId, int rowId, String direction) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);

            int row2;
            if (direction.equals("UP")) {
                row2 = rowId - 1;
            } else {
                row2 = rowId + 1;
            }
            dao.swapAllSections(rowId, row2, formId);
            dao.swapRows(rowId, row2, formId);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Deletes a section from the system based on the unique identifier. It deletes
     * all section questions as well. Questions are deleted by using cascade delete.
     *
     * @param sectionId The Section ID to delete
     * @param user      The person who carries this deletion operation.
     * @throws CtdbException Thrown if any other errors occur while processing
     */
    public void deleteSection(int sectionId, User user) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);

            FormManagerDao dao = FormManagerDao.getInstance(conn);

            int formId = dao.getSection(sectionId).getFormId();
            Form tmpform = new Form();
            tmpform.setId(formId);
            if (dao.isAdministered(tmpform)) {
                dao.createFormArchive(formId);
                Form form = dao.getMiniForm(formId);
                form.setUpdatedBy(user.getId());
                dao.updateFormVersion(form);
            }
            Section section = this.getMiniSection(sectionId);
            List<Question> questions = section.getQuestionList();
            
            if (!questions.isEmpty() && questions.size() > 0) {
                this.deleteQuestionAttributes(questions, sectionId);
            }
            dao.deleteSection(sectionId);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Retrieves the questions for a section based on the section ID
     *
     * @param sectionId The section ID
     * @return The questions for the section ID
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Question> getSectionQuestions(int sectionId) throws CtdbException {
        Section section = new Section();
        section.setId(sectionId);

        QuestionManager qm = new QuestionManager();

        return qm.getQuestions(section);
    }
    
    public void clearDataElementAndGroupNameAssociationsForForm(Form f) throws CtdbException {
    	Connection conn = null;
    	
    	try {
	    	conn = CtdbManager.getConnection();
	        FormManagerDao dao = FormManagerDao.getInstance(conn);
	    	 
			for ( List<Section> row : f.getRowList() ) {
                for ( Section section : row ) {
                    if ( section != null ) {
                    	int sectionId = section.getId();
                   	 	dao.clearSectionGroupNameAssociationsForForm(sectionId);
                   	 	List<Question> questions = getSectionQuestions(sectionId);
                   	 
                        for( Question question : questions ) {
                            int questionId = question.getId();
                            FormQuestionAttributes fqa = getFormQuestionAttributes(sectionId, questionId);
                            int questionAttributesId = fqa.getId();
                            dao.clearDataElementAssociationsForForm(questionAttributesId);
                        }
                    }
                }
			}
    	}
    	finally {
            this.close(conn);
        }
    }

    /**
     * Gets the section ID from section question with formId and questionId.
     *
     * @param formId     The form that has the section
     * @param questionId The question that in the section
     * @return Section the Section object
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public Section getSectionByFormAndQuestion(int formId, int questionId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            return dao.getSectionByFormAndQuestion(formId, questionId);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Adds the questions to the section ID. It archives the section questions and versions
     * the form if the form is administed.
     *
     * @param sectionId   The section ID for questions to add to
     * @param questionIds The list of question IDs to be added to the section
     * @throws ObjectNotFoundException  Thrown if the section does not exist
     * @throws DuplicateObjectException Thrown if the question already exists in the section
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     * @deprecated use public void addQuestionsToSection(int sectionId, String[] questionIds, FormQuestionAttributes questionAttributes)
     *             throws ObjectNotFoundException, CtdbException, DuplicateObjectException
     */
    public void addQuestionsToSection(int sectionId, String[] questionIds) throws ObjectNotFoundException, CtdbException, DuplicateObjectException {
        performAddQuestionsToSection(sectionId, questionIds, null);
    }

    /**
     * Performs the work for the addQuestionToSection methods and allows for
     * the polymorphism.
     *
     * @param sectionId   The section ID for questions to add to
     * @param questionIds The list of question IDs to be added to the section
     * @param versions    A HashMap containing Question IDs and their corresponding versions
     * @throws ObjectNotFoundException  Thrown if the section does not exist
     * @throws DuplicateObjectException Thrown if the question already exists in the section
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     * @deprecated use private void performAddQuestionsToSection(int sectionId, String[] questionIds, HashMap versions, FormQuestionAttributes questionAttributes)
     *             throws ObjectNotFoundException, CtdbException, DuplicateObjectException
     */
    private void performAddQuestionsToSection(int sectionId, String[] questionIds, 
    		HashMap<String, String> versions) throws ObjectNotFoundException, CtdbException, DuplicateObjectException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);

            FormManagerDao dao = FormManagerDao.getInstance(conn);

            QuestionManager qm = new QuestionManager();

            //get Section and Form objects
            Section section = dao.getSection(sectionId);
            int formId = section.getFormId();
            Form form = dao.getForm(formId);

            //if the form is administed, versions the form including its sections and questions before changes the form questions
            if (dao.isAdministered(form)) {
                //archives form
                dao.createFormArchive(formId);
                //update form version
                dao.updateFormVersion(form);
            }

            List<Question> questionList = new ArrayList<Question>();
            Question q;
            for (int idx = 0; idx < questionIds.length; idx++) {
                int qId = Integer.parseInt(questionIds[idx]);
                q = new Question();
                q.setId(qId);
                if (versions != null) {
                    if (versions.containsKey(Integer.toString(qId))) {
                        q.setVersion(new Version(versions.get(Integer.toString(qId))));
                    }
                }
                questionList.add(q);
            }
            
            //attach questions to the section ID
            dao.addQuestionsToSection(sectionId, qm.getQuestionsIncluding(questionList));
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Adds the questions to the section ID. It archives the section questions and versions
     * the form if the form is administed.
     *
     * @param sectionId   The section ID for questions to add to
     * @param questionIds The list of question IDs to be added to the section
     * @throws ObjectNotFoundException  Thrown if the section does not exist
     * @throws DuplicateObjectException Thrown if the question already exists in the section
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void addQuestionsToSection(int sectionId, String[] questionIds, FormQuestionAttributes questionAttributes, Integer[] rows, Integer[] cols) throws ObjectNotFoundException, CtdbException, DuplicateObjectException {
        performAddQuestionsToSection(sectionId, questionIds, null, questionAttributes, rows, cols);
    }

    /**
     * Adds the questions to the section ID. It archives the section questions and versions
     * the form if the form is administed.
     *
     * @param sectionId   The section ID for questions to add to
     * @param questionIds The list of question IDs to be added to the section
     * @param versions    A HashMap containing Question IDs and their corresponding versions
     * @throws ObjectNotFoundException  Thrown if the section does not exist
     * @throws DuplicateObjectException Thrown if the question already exists in the section
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void addQuestionsToSection(int sectionId, String[] questionIds, Map<String, String> versions, FormQuestionAttributes questionAttributes, Integer[] rows, Integer[] cols) throws ObjectNotFoundException, CtdbException, DuplicateObjectException {
        performAddQuestionsToSection(sectionId, questionIds, versions, questionAttributes, rows, cols);
    }

    /**
     * Performs the work for the addQuestionToSection methods and allows for
     * the polymorphism.
     *
     * @param sectionId   The section ID for questions to add to
     * @param questionIds The list of question IDs to be added to the section
     * @param versions    A HashMap containing Question IDs and their corresponding versions
     * @throws ObjectNotFoundException  Thrown if the section does not exist
     * @throws DuplicateObjectException Thrown if the question already exists in the section
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    private void performAddQuestionsToSection(int sectionId, String[] questionIds, Map<String, String> versions, 
    		FormQuestionAttributes questionAttributes, Integer[] rows, Integer[] cols) throws ObjectNotFoundException, 
    		CtdbException, DuplicateObjectException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);

            FormManagerDao dao = FormManagerDao.getInstance(conn);

            QuestionManager qm = new QuestionManager();

            //get Section and Form objects
            Section section = dao.getSection(sectionId);
            int formId = section.getFormId();
            Form form = dao.getForm(formId);

            //if the form is administed, versions the form including its sections and questions before changes the form questions
            if (dao.isAdministered(form)) {
                //archives form
                dao.createFormArchive(formId);
                //update form version
                dao.updateFormVersion(form);
            }

            List<Question> questionList = new ArrayList<Question>();
            Question q;
            for (int idx = 0; idx < questionIds.length; idx++) {
                int qId = Integer.parseInt(questionIds[idx]);
                q = new Question();
                q.setId(qId);
                if (versions != null) {
                    if (versions.containsKey(Integer.toString(qId))) {
                        q.setVersion(new Version(versions.get(Integer.toString(qId))));
                    }
                }
                questionList.add(q);
            }

            //attach questions to the section ID
            dao.addQuestionsToSection(sectionId, qm.getQuestionsIncluding(questionList), questionAttributes, rows, cols);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    public int createDefaultQuestionAttributes(Question question, FormQuestionAttributes questionAttributes) throws  CtdbException {
    	
    	 Connection conn = null;
    	 
         try {
             conn = CtdbManager.getConnection();
             return FormManagerDao.getInstance(conn).createDefaultQuestionAttributes(question, questionAttributes);
         }
         finally {
             this.close(conn);
         }
    }
    
    public void updateSectionQuestionQuestionAttributesIDAndQVersion(int sectionId, Question question, int qaId) throws CtdbException {
    	
    	 Connection conn = null;
    	 
         try {
             conn = CtdbManager.getConnection();
             FormManagerDao.getInstance(conn).updateSectionQuestionQuestionAttributesIDAndQVersion(sectionId, question, qaId);
         }
         finally {
             this.close(conn);
         }
    }

    /**
     * Adds the questions to the section ID for form "Save As" function.
     *
     * @param sectionId   The section ID for questions to add to
     * @param questions The list of question IDs to be added to the section
     * @throws ObjectNotFoundException  Thrown if the section does not exist
     * @throws DuplicateObjectException Thrown if the question already exists in the section
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void addQuestionsToSectionForSaveAs(int oldSectionId, int sectionId, List<Question> questions, boolean isSameDataStructureId, 
    		boolean nonCopyrightToCopyright, HashMap<Integer, Integer> oldSectionIdNewSectionIdMap, HashMap<Integer, Integer> oldQuestionIdNewQuestionIdMap) throws ObjectNotFoundException, 
    		CtdbException, DuplicateObjectException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);

            FormManagerDao dao = FormManagerDao.getInstance(conn);

            //attach questions to the section ID
            dao.addQuestionsToSectionForSaveAs(oldSectionId, sectionId, questions, isSameDataStructureId, 
            		nonCopyrightToCopyright, oldSectionIdNewSectionIdMap, oldQuestionIdNewQuestionIdMap);

            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Attaches the questions to the section and to the imported file as well.
     *
     * @param form        The Form object contains the imported file name and its upload path.
     * @param questionIds The list of question IDs to be added to the imported file.
     * @throws DuplicateObjectException Thrown if the question already exists in the section
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void addQuestionsToSection(Form form, int sectionId, String[] questionIds, FormQuestionAttributes questionAttributes, 
    		Integer[] rows, Integer[] cols) throws CtdbException, DuplicateObjectException {
        FileOutputStream out = null;
        
        try {
            //save section questions info to the db
            this.addQuestionsToSection(sectionId, questionIds, questionAttributes, rows, cols);

            QuestionManager qm = new QuestionManager();

            String path = form.getFormFileUploadPath();
            if (!path.endsWith(new Character(File.separatorChar).toString())) {
                path += File.separatorChar;
            }
            String fileName = path + form.getImportFileName();

            out = new FileOutputStream(new File(fileName), true);

            if (questionIds.length > 0) {
                Question question;
                String html = getBeginHTMLString();

                for (int idx = 0; idx < questionIds.length; idx++) {
                    int qId = Integer.parseInt(questionIds[idx]);

                    question = qm.getQuestion(qId);

                    html += buildHTMLString(question);
                }
                html += getEndHTMLString();

                out.write(html.getBytes());
            }
        }
        catch (FileNotFoundException fnfe) {
            throw new CtdbException("Unable to attach questions to the imported file: " + fnfe.getMessage(), fnfe);
        }
        catch (IOException ioe) {
            throw new CtdbException("Unable to attach questions to the imported file: " + ioe.getMessage(), ioe);
        }
        finally {
            try {
            	if ( out != null ) {
            		out.close();
            	}
            }
            catch (IOException ioex) {
                logger.warn("Could not close file stream.", ioex);
            }
        }
    }

    /**
     * Attachs the questions to the section and to the imported file as well.
     *
     * @param form        The Form object contains the imported file name and its upload path.
     * @param questionIds The list of question IDs to be added to the imported file.
     * @throws DuplicateObjectException Thrown if the question already exists in the section
     *                                  based on the unique constraints
     * @throws CtdbException            Thrown if any other errors occur while processing
     */
    public void addQuestionsToSection(Form form, int sectionId, String[] questionIds, Map<String, String> Versions, 
    		FormQuestionAttributes questionAttributes, Integer[] rows, Integer[] cols) throws CtdbException, DuplicateObjectException {
        FileOutputStream out = null;
        
        try {
            //save section questions info to the db
            this.addQuestionsToSection(sectionId, questionIds, Versions, questionAttributes, rows, cols);

            QuestionManager qm = new QuestionManager();

            String path = form.getFormFileUploadPath();
            if (!path.endsWith(new Character(File.separatorChar).toString())) {
                path += File.separatorChar;
            }
            String fileName = path + form.getImportFileName();

            out = new FileOutputStream(new File(fileName), true);

            if (questionIds.length > 0) {
                Question question;
                String html = getBeginHTMLString();

                for (int idx = 0; idx < questionIds.length; idx++) {
                    int qId = Integer.parseInt(questionIds[idx]);

                    question = qm.getQuestion(qId);
                    question.setFormQuestionAttributes(this.getFormQuestionAttributes(sectionId, qId));

                    html += buildHTMLString(question);
                }
                html += getEndHTMLString();

                out.write(html.getBytes());
            }
        }
        catch (FileNotFoundException fnfe) {
            throw new CtdbException("Unable to attach questions to the imported file: " + fnfe.getMessage(), fnfe);
        }
        catch (IOException ioe) {
            throw new CtdbException("Unable to attach questions to the imported file: " + ioe.getMessage(), ioe);
        }
        finally {
            try {
            	if ( out != null ) {
            		out.close();
            	}
            }
            catch (IOException ioex) {
                logger.warn("Could not close the file stream.", ioex);
            }
        }
    }

    /**
     * Gets the attributes for the question in the form section.
     *
     * @param sectionId  The section that has the question
     * @param questionId The question needs the attributes
     * @return FormQuestionAttributes object
     * @throws ObjectNotFoundException Thrown if no attributes found
     * @throws CtdbException           Thrown if there is any other errors occur during the process
     */
    public FormQuestionAttributes getFormQuestionAttributes(int sectionId, int questionId) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;
        FormQuestionAttributes qAttrs = null;

        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            qAttrs = dao.getFormQuestionAttributes(sectionId, questionId);
            QuestionManager qm = new QuestionManager();
            
            if (qAttrs.hasSkipRule()) {
                qAttrs.setQuestionsToSkip(qm.getSkipQuestions(questionId, sectionId));
            }

            if (qAttrs.isCalculatedQuestion()) {
                CalculatedFormQuestionAttributes calAttrs = (CalculatedFormQuestionAttributes) qAttrs;
                calAttrs.setQuestionsToCalculate(qm.getCalculateQuestions(calAttrs, sectionId));
            }
            
            qAttrs.setSectionId(sectionId);
            
            // EMAIL Trigger
            if (qAttrs.getEmailTrigger().getId() != Integer.MIN_VALUE) {
                qAttrs.setEmailTrigger(EmailTriggerDao.getInstance(conn).getEmailTrigger(qAttrs.getEmailTrigger().getId()));
                qAttrs.getEmailTrigger().setQuestionattributesid(qAttrs.getId());
            }
        }
        finally {
            this.close(conn);
        }
        
        return qAttrs;
    }

    /**
     * Gets the attributes for the question in the form section for the form version.
     *
     * @param sectionId   The section that has the question
     * @param questionId  The question needs the attributes
     * @param formVersion The form version number
     * @return FormQuestionAttributes object
     * @throws ObjectNotFoundException Thrown if no attributes found
     * @throws CtdbException           Thrown if there is any other errors occur during the process
     */
    public FormQuestionAttributes getFormQuestionAttributes(int sectionId, int questionId, int formVersion) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;
        FormQuestionAttributes qAttrs = null;

        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            qAttrs = dao.getFormQuestionAttributes(sectionId, questionId);
            QuestionManager qm = new QuestionManager();
            
            if (qAttrs.hasSkipRule()) {
                qAttrs.setQuestionsToSkip(qm.getSkipQuestions(questionId, sectionId));
            }

            if (qAttrs.isCalculatedQuestion()) {
                ((CalculatedFormQuestionAttributes) qAttrs).setQuestionsToCalculate(
                        qm.getCalculateQuestions((CalculatedFormQuestionAttributes) qAttrs, sectionId));
            }
            
            qAttrs.setSectionId(sectionId);
        }
        finally {
            this.close(conn);
        }
        
        return qAttrs;
    }

    /**
     * Updates the form question attributes. If the form attributes have
     * been modified and the form is administed, it archives and versions the form.
     *
     * @param question The question domain object that contains the question attributes domain object to update
     * @throws ObjectNotFoundException Thrown if the form question attributes does not exist
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public void updateQuestionAttributes(Question question) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);

            int sectionId = question.getFormQuestionAttributes().getSectionId();
            Section section = this.getSection(sectionId);
            Form form = this.getFormAndSetofQuestions(section.getFormId());

            FormQuestionAttributes oldQuestionAttributes = this.getFormQuestionAttributes(sectionId, question.getId());
            question.getFormQuestionAttributes().setId(oldQuestionAttributes.getId());
            question.getCalculatedFormQuestionAttributes().setId(oldQuestionAttributes.getId());

            // this checks to see if calculation data changed
            boolean isCalcChanged = (question.getFormQuestionAttributes().isCalculatedQuestion()
                    == oldQuestionAttributes.isCalculatedQuestion())
                    && (oldQuestionAttributes instanceof CalculatedFormQuestionAttributes)
                    && (!question.getFormQuestionAttributes().equals(oldQuestionAttributes));

            // EMAIL TRIGGER

            if (question.getFormQuestionAttributes().getEmailTrigger().getToEmailAddress() != null
                    && !question.getFormQuestionAttributes().getEmailTrigger().getToEmailAddress().trim().equals("")) {
                if (question.getFormQuestionAttributes().getEmailTrigger().getId() == Integer.MIN_VALUE) {
                    // new trigger create
                    EmailTriggerDao.getInstance(conn).createEmailTrigger(question.getFormQuestionAttributes().getEmailTrigger());
                } else {
                    EmailTriggerDao.getInstance(conn).updateEmailTrigger(question.getFormQuestionAttributes().getEmailTrigger());
                }
            }

            //if the form is administered, the form metadata has changed, and the status is not
            //checked out(for import), then needs to create form archive
            if ((dao.isAdministered(form)) && (!(question.getFormQuestionAttributes()).equals(oldQuestionAttributes) || isCalcChanged)
                    && (form.getStatus().getId() != FormConstants.STATUS_CHECKEDOUT)) {
                dao.createFormArchive(form.getId());
                dao.updateQuestionAttributes(question);
                dao.updateFormVersion(form);
            }
            else {
                dao.updateQuestionAttributes(question);
            }

            if ( question.getFormQuestionAttributes() == null
                    && question.getCalculatedFormQuestionAttributes() == null
                    && question.getFormQuestionAttributes().isCalculatedQuestion() == true
                    && (question.getCalculatedFormQuestionAttributes().getCalculation() == null
                    || question.getCalculatedFormQuestionAttributes().getCalculation().length() < 1) ) {

                question.getFormQuestionAttributes().setIsCalculatedQuestion(false);
                question.setCalculatedFormQuestionAttributes(new CalculatedFormQuestionAttributes());
            }

            if (question.getFormQuestionAttributes().isDeleteTrigger()) {
                EmailTriggerDao.getInstance(conn).deleteEmailTrigger(question.getFormQuestionAttributes().getEmailTrigger().getId());
                question.getFormQuestionAttributes().setEmailTrigger(new EmailTrigger());
            }
            
            // set skip rule questions if any
            dao.deleteSkippedQuestions(question.getId(), sectionId);
            
            if (question.getFormQuestionAttributes().getQuestionsToSkip() != null
                    && !question.getFormQuestionAttributes().getQuestionsToSkip().isEmpty()) {
                for ( Question questionToSkip : question.getFormQuestionAttributes().getQuestionsToSkip() ) {
                    dao.associateSkippedQuestions(question.getId(), questionToSkip.getId(), sectionId, questionToSkip.getSectionId());
                }
            }
            
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    /**
     * Updates the form question attributes. If the form attributes have
     * been modified and the form is administered, it archives and versions the form.
     *
     * @param question The question domain object that contains the question attributes domain object to update
     * @throws ObjectNotFoundException Thrown if the form question attributes does not exist
     * @throws CtdbException           Thrown if any other errors occur while processing
     */
    public void updateQuestionAttributes(List<Question> questions, int formId) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;
        Question question = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            Form form = this.getFormAndSetofQuestions(formId);
            
            for ( Iterator<Question> iter = questions.iterator(); iter.hasNext(); ) {
            	question = iter.next();
	            int sectionId = question.getFormQuestionAttributes().getSectionId();
	            FormQuestionAttributes oldQuestionAttributes = this.getFormQuestionAttributes(sectionId, question.getId());
	            
	            // Must retain the same attr_id for the current question. The original one may be lagacy ID from XML export.
	            question.getFormQuestionAttributes().setId(oldQuestionAttributes.getId());
	            question.getCalculatedFormQuestionAttributes().setId(oldQuestionAttributes.getId());
	
	            // This checks to see if calculation data changed
	            boolean isCalcChanged = (question.getFormQuestionAttributes().isCalculatedQuestion()
	                    == oldQuestionAttributes.isCalculatedQuestion())
	                    && (oldQuestionAttributes instanceof CalculatedFormQuestionAttributes)
	                    && (!question.getFormQuestionAttributes().equals(oldQuestionAttributes));
	
	            // EMAIL TRIGGER
	            if ( question.getFormQuestionAttributes().getEmailTrigger().getToEmailAddress() != null && 
	            		!question.getFormQuestionAttributes().getEmailTrigger().getToEmailAddress().trim().isEmpty() ) {
	            	
	                if (question.getFormQuestionAttributes().getEmailTrigger().getId() == Integer.MIN_VALUE) {
	                    // Create new trigger
	                    EmailTriggerDao.getInstance(conn).createEmailTrigger(question.getFormQuestionAttributes().getEmailTrigger());
	                }
	                else {
	                    EmailTriggerDao.getInstance(conn).updateEmailTrigger(question.getFormQuestionAttributes().getEmailTrigger());
	                }
	            }
	
	            // If the form is administered, the form metadata has changed, and the status is not
	            if ( (dao.isAdministered(form)) && (!(question.getFormQuestionAttributes()).equals(oldQuestionAttributes) || isCalcChanged )
	                    && (form.getStatus().getId() != FormConstants.STATUS_CHECKEDOUT)) {
	                dao.createFormArchive(form.getId());
	                dao.updateQuestionAttributes(question);
	                dao.updateFormVersion(form);
	            }
	            else {
	                dao.updateQuestionAttributes(question);
	            }
	            
	            if ( (question.getFormQuestionAttributes().getEmailTrigger().getToEmailAddress() == null) || 
	            		question.getFormQuestionAttributes().getEmailTrigger().getToEmailAddress().trim().isEmpty() ) {
	            	
	            	if (question.getFormQuestionAttributes().isDeleteTrigger()) {
	 	                EmailTriggerDao.getInstance(conn).deleteEmailTrigger(question.getFormQuestionAttributes().getEmailTrigger().getId());
	 	                question.getFormQuestionAttributes().setEmailTrigger(new EmailTrigger());
	 	            }
	            }
	
	            if ( question.getFormQuestionAttributes() == null
	                    && question.getCalculatedFormQuestionAttributes() == null
	                    && question.getFormQuestionAttributes().isCalculatedQuestion() == true
	                    && (question.getCalculatedFormQuestionAttributes().getCalculation() == null
	                    || question.getCalculatedFormQuestionAttributes().getCalculation().length() < 1) ) {
	
	                question.getFormQuestionAttributes().setIsCalculatedQuestion(false);
	                question.setCalculatedFormQuestionAttributes(new CalculatedFormQuestionAttributes());
	            }
	           
	            // set skip rule questions if any
	            dao.deleteSkippedQuestions(question.getId(), sectionId);
	            
	            if ( question.getFormQuestionAttributes().getQuestionsToSkip() != null
	                    && !question.getFormQuestionAttributes().getQuestionsToSkip().isEmpty() ) {
	                for ( Question questionToSkip : question.getFormQuestionAttributes().getQuestionsToSkip() ) {
	                    dao.associateSkippedQuestions(question.getId(), questionToSkip.getId(), sectionId, questionToSkip.getSectionId());
	                }
	            }
            }
            
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Builds the html for the begin of the question to be appended to the end of the import file
     *
     * @return The html string
     */
    private String getBeginHTMLString() {
        String cr = "\r\n";
        String html = cr + "<%-- the following question(s) were attached to the form on line --%>" + cr;
        html += "<tr>" + cr;
        html += "<td>" + cr;
        html += "<table cellspacing=\"2\" cellpadding=\"2\" border=\"0\" width=\"100%\">" + cr;
        return html;
    }

    /**
     * Builds the html for the end of the question to be appended to the end of the import file
     *
     * @return The html string
     */
    private String getEndHTMLString() {
        String cr = "\r\n";
        String html = "</table>" + cr;
        html += "</td>" + cr;
        html += "</tr>" + cr;
        return html;
    }

    /**
     * Builds the html for the question to be appended to the end of the import file
     *
     * @param question Question to be appended
     * @return The html string
     */
    private String buildHTMLString(Question question) {
        String cr = "\r\n";
        StringBuffer html = new StringBuffer("<tr>");
        FormQuestionAttributes qAttrs = question.getFormQuestionAttributes();
        
        html.append(cr);
        html.append("<td valign=\"" + qAttrs.getHtmlAttributes().getvAlign() + "\" align=\"" + qAttrs.getHtmlAttributes().getAlign() + "\">");
        html.append("<img border=\"0\" alt=\"\" height=\"1\" width=\"" + qAttrs.getHtmlAttributes().getIndent() + "\" src=\"/common/images/spacer.gif\"></td>");
        html.append("<td style=\"font-family: " + qAttrs.getHtmlAttributes().getFontFace() + "; color: " + qAttrs.getHtmlAttributes().getColor() + ";\" valign=\"");
        html.append(qAttrs.getHtmlAttributes().getvAlign() + "\" align=\"" + qAttrs.getHtmlAttributes().getAlign() + "\">");
        
        if ( qAttrs.getHtmlAttributes().getFontSize() != null ) {
            html.append("<font size=\"" + qAttrs.getHtmlAttributes().getFontSize() + "\">");
            html.append(question.getName() + "</font>");
        }
        else {
            html.append(question.getName());
        }
        
        html.append("</td>");
        html.append("<td valign=\"" + qAttrs.getHtmlAttributes().getvAlign() + "\" align=\"" + qAttrs.getHtmlAttributes().getAlign() + "\">");
        
        if ( question.getType().equals(QuestionType.TEXTBOX) ) { //Textbox
            html.append("<input name=\"Q_" + question.getId() + "\" id='Q_" + question.getId() + "' type=\"text\">");
        } 
        else if ( question.getType().equals(QuestionType.TEXT_BLOCK) ) { // Text block
        	html.append(question.getHtmltext()); // this will be HTML
        }
        else if ( question.getType().equals(QuestionType.TEXTAREA) )  //Textarea
        {
            html.append("<textarea id=\"Q_" + question.getId() + "\"  name=\"Q_" + question.getId() + "\"></textarea>");
        }
        else if ( question.getType().equals(QuestionType.SELECT) || question.getType().equals(QuestionType.MULTI_SELECT) ) { //Select or Multi-Select
            if (question.getType().equals(QuestionType.SELECT)) { //Select
                html.append("<select id=\"Q_" + question.getId() + "\"  name=\"Q_" + question.getId() + "\">");
            }
            else { //Multi-Select
                html.append("<select id=\"Q_" + question.getId() + "\"  name=\"Q_" + question.getId() + "\" multiple=\"true\" size=\"5\">");
            }
            
            for ( Answer answer : question.getAnswers() ) {
                html.append("<option value=\"" + answer.getDisplay() + "\">" + answer.getDisplay() + "</option>");
            }
            
            html.append("</select>");
        }
        else if ( question.getType().equals(QuestionType.RADIO) || question.getType().equals(QuestionType.CHECKBOX) ) { // Radio button or Check box
            for ( Answer answer : question.getAnswers() ) {
                html.append("<input id=\"Q_" + question.getId() + "\"  name=\"Q_" + question.getId() + "\" type=\"");
                
                if ( question.getType().equals(QuestionType.RADIO) ) { // Radio button
                    html.append("radio");
                }
                else { // Check box
                    html.append("checkbox");
                }
                
                html.append("\">" + answer.getDisplay() + "</input>");
            }
        }
        else if ( qAttrs.isCalculatedQuestion() ) { //Calculated
            html.append("<input id=\"Q_" + question.getId() + "\"  type=\"text\" name=\"Q_" + question.getId() + "\" disabled=\"true\">");
        }

        html.append("</td>" + cr);
        html.append("</tr>" + cr);

        return html.toString();
    }

    /**
     * Updates the ordering of questions for the section. If the form is administed,
     * it archives the section questions and the form, and versions the form as well.
     * This ordering will be used when displaying questions for the section.
     *
     * @param sectionId  The section ID
     * @param orderedIds The question IDs in order of display [0]=ID, [1]=questionOrder, [2]=questionOrder_col
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void updateQuestionOrdering(int sectionId, String[][] orderedIds) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            //if the form is administed, archives section questions and form,
            //also versions the form as well
            Section section = dao.getSection(sectionId);
            Form form = dao.getForm(section.getFormId());
            if (dao.isAdministered(form)) {
                //archives form
                dao.createFormArchive(form.getId());
                //versions form
                dao.updateFormVersion(form);
            }

            List<Question> questions = new ArrayList<Question>();
            
            for ( int idx = 0; idx < orderedIds.length; idx++ ) {
                int qId = Integer.parseInt(orderedIds[idx][0]);
                Question question = dao.getSectionQuestion(sectionId, qId);
                question.setQuestionOrderCol(Integer.parseInt(orderedIds[idx][2]));
                question.setQuestionOrder(Integer.parseInt(orderedIds[idx][1]));
                questions.add(question);
                
            }

            dao.deleteQuestionOrdering(sectionId);
            dao.addQuestionsToSection(sectionId, questions);

            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * This method is called when a user edits a question that is attached to a form, from the
     * section home page.  The form will be updated to utilize the new version of the question.
     * If the form has been administered, the form will be versioned.
     *
     * @param sectionId The section ID
     * @param question  The question that has been updated
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void updateFormQuestion(int sectionId, Question question) throws CtdbException {

        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            // If the form is administered, archives section questions and form. Also versions the form as well.
            Section section = dao.getSection(sectionId);
            Form form = dao.getForm(section.getFormId());
            
            if ( dao.isAdministered(form) ) {
                // Archives form
                dao.createFormArchive(form.getId());
                
                // Versions form
                dao.updateFormVersion(form);
            }
            
            dao.updateFormQuestion(sectionId, question);
            commit(conn);
            
            // Remove existing XML
            XmlThreadManager xtm = new XmlThreadManager();
            xtm.removeXml(form.getId() + form.getVersion().getToString());
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Deletes the question attributes before deleting questions from the section.
     *
     * @param deletedQuestions The list of questions to be deleted from the section.
     * @param sectionId        The section Id that has the deleted questions.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void deleteQuestionAttributes(List<Question> deletedQuestions, int sectionId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao fromDao = FormManagerDao.getInstance(conn);
            fromDao.deleteQuestionAttributes(deletedQuestions, sectionId);

            for ( Question q : deletedQuestions ) {
                if ( q.getFormQuestionAttributes().isCalculatedQuestion() ) {
                	fromDao.removeDependentCalculatedQuestions(q.getId(), sectionId);
                }
            }
            
            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Deletes the question skip rules before deleting questions from the section.
     *
     * @param deletedQuestions The list of questions to be deleted from the section.
     * @param sectionId        The section Id that has the deleted questions.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void deleteSkipRules(List<Question> deletedQuestions, int sectionId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao.getInstance(conn).deleteSkipRules(deletedQuestions, sectionId);
            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    public void validateFormVersionQuestionRemoval(Form f, List<Question> deletedQuestions) throws CtdbException, QuestionRemovalException {

        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).validateFormVersionQuestionRemoval(f, deletedQuestions);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Updates the association of section and questions for all sections of a form. If the form is administed,
     * it archives the section questions and the form, and versions the form as well.
     * This ordering will be updated according to the order in the question array list also
     * <p/>
     * This method is similar to the method updateQuestionOrdering(int sectionId, string[] orderedIds).
     * The section question associations for the section is removed from the sectionquestion table
     * first and, then, the new associations are inserted.
     * <p/>
     * All sections in the map parameter needs to be in the same form.   At least one section
     * needs to exist in the form.
     *
     * @param sectionQuestions HashMap from sectionIds (Integer) to ArrayList of questions.  The
     *                         question in the list only needs its id and version properties to be set.
     *                         The order of questions in the array list is used for the question ordering.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void updateQuestionOrdering(Map<Integer, List<Question>> sectionQuestions) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);

            List<Integer> sectionIds = new ArrayList<Integer>(sectionQuestions.keySet());
            
            if (sectionIds.size() < 1) {
                return;
            }
            
            FormManagerDao dao = FormManagerDao.getInstance(conn);

            //if the form is administered, archives section questions and form,
            //also versions the form as well
            int oneSectionId = sectionIds.get(0);
            Section section = dao.getSection(oneSectionId);
            Form form = dao.getForm(section.getFormId());
            
            if (dao.isAdministered(form)) {
                //archives form
                dao.createFormArchive(form.getId());
                //versions form
                dao.updateFormVersion(form);
            }
            
            for ( Integer sectionId : sectionIds ) {
                List<Question> questions = sectionQuestions.get(sectionId);

                this.checkSkipRules(form.getId(), sectionId.intValue(), questions);
                dao.deleteQuestionOrdering(sectionId.intValue());
                dao.addQuestionsToSection(sectionId.intValue(), questions);
            }

            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Checks to see if the skip rules need to update for moving questions between sections.
     *
     * @param formId    The form ID that the moving questions happened
     * @param sectionId The new section ID for the question
     * @param questions The questions need to check to see if they are moved to new section and if any skip rules need
     *                  to update.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void checkSkipRules(int formId, int sectionId, List<Question> questions) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();

            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            for ( Question question : questions ) {
                Section orgSection = this.getSectionByFormAndQuestion(formId, question.getId());
                
                if (orgSection.getId() != sectionId) {
                    if (question.getFormQuestionAttributes().hasSkipRule()) {
                        dao.updateSkipRules(orgSection.getId(), sectionId, question.getId());
                    }
                }
            }
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Deletes a section question based on the section ID and question ID
     *
     * @param sectionId  The section ID
     * @param questionId The question ID to delete.
     * @param user       The person who carries this deletion operation.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public void deleteSectionQuestion(int sectionId, int questionId, User user) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);

            FormManagerDao dao = FormManagerDao.getInstance(conn);

            //if the form is administed, archives section questions and form,
            //also versions the form as well
            Section section = dao.getSection(sectionId);
            Form form = dao.getMiniForm(section.getFormId());
            if (dao.isAdministered(form)) {
                //archives form
                dao.createFormArchive(form.getId());
                //versions form
                form.setUpdatedBy(user.getId());
                dao.updateFormVersion(form);
            }

            // added by Ching Heng
            dao.deleteSingleQuestionAttribute(questionId, sectionId);//delete the question attribute
            //
            dao.deleteSectionQuestion(sectionId, questionId);
            dao.removeDependentCalculatedQuestions(questionId, sectionId);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    /**
     * Retrieves all form versions from the archive table for a form
     *
     * @param formId The form ID for retrieving all its versions
     * @return A list of all versions of the form
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Form> getFormVersions(int formId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getFormVersions(formId);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Retrieves all form versions from the archive table for a form
     *
     * @param formId The form ID for retrieving all its versions
     * @return A list of all versions of the form
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Version> getFormVersionIds(int formId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getFormVersionIds(formId);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Retrieves a list of intervals for a form
     *
     * @param formId The form ID
     * @return The list of intervals for the form
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Interval> getIntervals(int formId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getIntervals(formId);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Imports the form with a form ID. It validates the following:
     * 1. if the same user importing the form.
     * 2. if the questions are identical (same number of questions and ids are the same)
     * If the validation fails, it throws exception and will not import the form.
     *
     * @param form - The form to import
     * @param formFile - The upload form file
     * @param user - The User domain object used to get the user ID
     * @param importedQuestions
     * @throws FileUploadException Thrown if any errors occur while processing
     * @throws CtdbException	If there are any database errors.
     */
    public int importForm(Form form, File formFile, User user, Set<Integer> importedQuestions) throws FileUploadException, CtdbException {
        int returnCode = 0;
        BufferedReader bf = null;
        String searchStr = "name=\"Q_";
        String searchStr1 = "NAME=\"Q_";
        
        List<String> qIds = new ArrayList<String>();
        int numOfQuestions = 0;
        Connection conn = null;
        
        try {
        	conn = CtdbManager.getConnection();
        	
            if (user.getId() != form.getCheckOutBy()) {
                returnCode = 1;
                
                return returnCode;
            }

           // InputStream in = formFile.getInputStream();
            FileInputStream in = new FileInputStream(formFile);
            bf = new BufferedReader(new InputStreamReader(in));

            String readLine;
            String qId = null;
            readLine = bf.readLine();

            int previousType = 0;  // 0: others, 1: checkbox, 2: radio
            String previousQId = "";
            Pattern patternCheckbox = Pattern.compile("<[^>]*?type\\s*=\\s*\"checkbox.*?>");
            Pattern patternRadio = Pattern.compile("<[^>]*?type\\s*=\\s*\"radio.*?>");
            Matcher matcherCheckbox = patternCheckbox.matcher(" ");
            Matcher matcherRadio = patternRadio.matcher(" ");
            
            while ( readLine != null ) {
                int beginIndex = -1;
                int endIndex = -1;
                boolean qIdFound = false;
                
                if ( readLine.indexOf(searchStr) != -1 ) {
                    beginIndex = readLine.indexOf(searchStr) + searchStr.length();
                    String tmpStr = readLine.substring(beginIndex);
                    endIndex = beginIndex + tmpStr.indexOf("\"");
                    qId = readLine.substring(beginIndex, endIndex).trim();
                    qIdFound = true;
                }
                else if ( readLine.indexOf(searchStr1) != -1 ) {
                    beginIndex = readLine.indexOf(searchStr1) + searchStr1.length();
                    String tmpStr = readLine.substring(beginIndex);
                    endIndex = beginIndex + tmpStr.indexOf("\"");
                    qId = readLine.substring(beginIndex, endIndex).trim();
                    qIdFound = true;
                }
                
                if ( qIdFound ) {
                    matcherCheckbox.reset(readLine.toLowerCase());
                    matcherRadio.reset(readLine.toLowerCase());
                    int currentType = 0;
                    
                    if ( matcherCheckbox.find() ) {
                        currentType = 1;
                    }
                    else if ( matcherRadio.find() ) {
                        currentType = 2;
                    }
                    
                    if ( !(previousType != 0 && currentType == previousType && previousQId.equals(qId)) ) {
                        numOfQuestions++;
                        qIds.add(qId);
                        importedQuestions.add(new Integer(qId));
                    }
                    
                    previousQId = qId;
                    previousType = currentType;
                }

                readLine = bf.readLine();
            }

            if ( numOfQuestions != form.getNumQuestions() ) {
                logger.error("Number of questions on form does not match database number of questions. Is import greater than db -> " + 
                		Boolean.toString(numOfQuestions > form.getNumQuestions()));
                logger.error("System captured question ids" + qIds.toString());
                returnCode = 2;
                
                return returnCode;
            }
            else if ( numOfQuestions != 0 ) {
                //check to see if question ids match
                boolean found = verifyQuestions(form, qIds);
                
                if ( !found ) {
                    returnCode = 2;
                    
                    return returnCode;
                }
            }

            List<String> fileTypes = new ArrayList<String>();
            fileTypes.add("html");
            FileUploader.upload(formFile, form.getFormFileUploadPath(), form.getImportFileName(), fileTypes);
            FormManagerDao.getInstance(conn).storeHTML(form.getId(), formFile);
            
            return returnCode;
        }
        catch (IOException e) {
            throw new FileUploadException("Can't upload the file: " + e.getMessage(), e);
        }
        finally {
            close(conn);
            try {
            	if ( bf != null ) {
            		bf.close();
            	}
            }
            catch (IOException ex) {
                logger.warn("Could not close the buffer.", ex);
            }
        }
    }

    /**
     * Verifies if the form question IDs are the same in an imported file
     *
     * @param form Form object contains the questions for each section
     * @param qIds Question IDs in the imported file
     * @return True if all question IDs match; false otherwise
     */
    private boolean verifyQuestions(Form form, List<String> qIds) {
        boolean valid = false;
        
        for ( List<Section> row : form.getRowList() ) {
            for ( Section section : row ) {
                if (section != null) {
                    for ( Question question : section.getQuestionList() ) {
                        valid = isFound(question.getId(), qIds);
                        
                        if (!valid) {
                        	logger.error("The question with ID: " + question.getId() + 
                        		" was not found in the list of questions from the imported form");
                            break;
                        }
                    }
                }
            }
        }
        
        return valid;
    }

    /**
     * Finds the question ID in the question ID list from an imported file
     *
     * @param qId  Question id to search for
     * @param qIds Question ids in the imported file
     * @return True if found; false otherwise
     */
    private boolean isFound(int qId, List<String> qIds) {
        boolean found = false;
        
        for ( String strId : qIds ) {
            if ( Integer.toString(qId).equals(strId) ) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Validates the calculated question.  Checks for dependency condition in the current form.
     *
     * @param form        The Form object
     * @param questionIds Array of strings with question IDs
     * @throws CalculatedDependencyException Thrown if dependecy condition fails
     * @throws CtdbException                 Thrown if any other errors occur while processing
     */
    public void validateCalculatedQuestion(Form form, String[] questionIds) throws CalculatedDependencyException, CtdbException {
        List<Question> questionsMissing = new ArrayList<Question>();
        Question currentQuestion = new Question();
        
        QuestionManager qm = new QuestionManager();
        List<Question> questions = new ArrayList<Question>();
        Map<Integer, Question> questionsAll = new HashMap<Integer, Question>();
        
        for ( int idx = 0; idx < questionIds.length; idx++ ) {
            int qId = Integer.parseInt(questionIds[idx]);
            Question question = qm.getQuestion(qId);
            questions.add(question);
            questionsAll.put(new Integer(question.getId()), question);
        }
        
        // get questions for the current form
        for ( List<Section> row : form.getRowList() ) {
            for ( Section sec : row ) {
                if ( sec != null ) {
                    for ( Question question : qm.getQuestions(sec) ) {
                        questionsAll.put(new Integer(question.getId()), question);
                    }
                }
            }
        }
        
        for ( Iterator<Question> it = questions.iterator(); it.hasNext(); ) {
        	currentQuestion = it.next();
        	
            if ( currentQuestion.getFormQuestionAttributes().isCalculatedQuestion() ) {
                for ( Question question : currentQuestion.getCalculatedFormQuestionAttributes().getQuestionsToCalculate() ) {
                    if (!questionsAll.containsKey(new Integer(question.getId()))) {
                        questionsMissing.add(question);
                    }
                }
                
                if ( questionsMissing.size() != 0 ) {
                    break;
                }
            }
        }

        if ( !questionsMissing.isEmpty() ) {
            String a = currentQuestion.getName();
            StringBuffer b = new StringBuffer();
            
            for ( Iterator<Question> it = questionsMissing.iterator(); it.hasNext(); ) {
                b.append(it.next().getName());
                
                if ( it.hasNext() ) {
                    b.append("; ");
                }
            }
            
            throw new CalculatedDependencyException(a, b.toString());
        }
    }

    /**
     * Validates the skip rule question.  Checks for dependency condition in the current form.
     *
     * @param form        The Form object
     * @param questionIds Array of strings with question IDs
     * @throws SkipRuleDependencyException Thrown if dependecy condition fails
     * @throws CtdbException               Thrown if any other errors occur while processing
     */
    public void validateSkipRuleQuestion(Form form, String[] questionIds) throws SkipRuleDependencyException, CtdbException {
        Question currentQuestion = new Question();
        List<Question> questionsMissing = new ArrayList<Question>();
        
        QuestionManager qm = new QuestionManager();

        List<Question> questions = new ArrayList<Question>();
        Map<Integer, Question> questionsAll = new HashMap<Integer, Question>();
        
        for ( int idx = 0; idx < questionIds.length; idx++ ) {
            int qId = Integer.parseInt(questionIds[idx]);
            Question question = qm.getQuestion(qId);
            questions.add(question);
            questionsAll.put(new Integer(question.getId()), question);
        }
        
        // get questions for the current form
        for ( List<Section> row : form.getRowList() ) {
            for ( Section sec : row ) {
                if (sec != null) {
                    for ( Question question : qm.getQuestions(sec) ) {
                        questionsAll.put(new Integer(question.getId()), question);
                    }
                }
            }
        }
        
        for ( Iterator<Question> iter = questions.iterator(); iter.hasNext(); ) {
            currentQuestion = iter.next();
            
            if ( currentQuestion.getFormQuestionAttributes().hasSkipRule() ) {
                for ( Question question : currentQuestion.getFormQuestionAttributes().getQuestionsToSkip() ) {
                    if ( !questionsAll.containsKey(new Integer(question.getId())) ) {
                        questionsMissing.add(question);
                    }
                }
                
                if ( questionsMissing.size() != 0 ) {
                    break;
                }
            }
        }
        
        if ( !questionsMissing.isEmpty() ) {
            String a = currentQuestion.getName();
            StringBuffer b = new StringBuffer();
            
            for ( Iterator<Question> it = questionsMissing.iterator(); it.hasNext(); ) {
                b.append(it.next().getName());
                
                if ( it.hasNext() ) {
                    b.append("; ");
                }
            }
            
            throw new SkipRuleDependencyException(a, b.toString());
        }
    }

    /**
     * Validates the delete of calculated questions.  Checks for dependency condition in the current form.
     *
     * @param form      The Form object contains the form ID
     * @param questions a list of being deleted questions.
     * @param exclude   if excluding a section. If false, parameter sectionId is ignored.
     * @param sectionId the section to exclude
     * @throws CalculatedDependencyException Thrown if dependency condition fails
     */
    public void validateDeleteQuestion(Form form, List<Question> questions, boolean exclude, int sectionId) throws CalculatedDependencyException {
        List<String> sectionIds = new ArrayList<String>();
        sectionIds.add(Long.toString(sectionId));
        validateDeleteQuestion(form, questions, exclude, sectionIds);
    }

    public void validateDeleteQuestion(Form form, List<Question> questions, boolean exclude, 
    			List<String> sectionIds) throws CalculatedDependencyException {
            String name = null;
            String parentName = null;
            boolean child = false;
           
            for ( Question deletedQuestion : questions ) {
                boolean isParentInDelete = false;
            isParentInDelete = this.isParentQuestionInDelete(questions, deletedQuestion);
            
            if ( !isParentInDelete ) {
                //The passed in Form object is a fully populated form object, therefore
                //we just need to get what we want from this form object.

                // get questions for the current form
                for ( Iterator<List<Section>> iter = form.getRowList().iterator(); (iter.hasNext() && !child); ) {
                    for ( Iterator<Section> it = iter.next().iterator(); (it.hasNext() && !child); ) {
                        Section section = it.next();
                        
                        if (section != null && !(exclude && (sectionIds.contains(Integer.toString(section.getId()))))) {
                            for ( Iterator<Question> iter1 = section.getQuestionList().iterator(); (iter1.hasNext() && !child); ) {
                                Question currentQuestion = iter1.next();
                                
                                if ( currentQuestion.getFormQuestionAttributes().isCalculatedQuestion() ) {
                                    List<Question> questsToCal = currentQuestion.getCalculatedFormQuestionAttributes().getQuestionsToCalculate();
                                    
                                    for ( Iterator<Question> iter2 = questsToCal.iterator(); (iter2.hasNext() && !child); ) {
                                        Question question = iter2.next();
                                        
                                        if ( question.getId() == deletedQuestion.getId() ) {
                                            parentName = currentQuestion.getName();
                                            name = deletedQuestion.getName();
                                            child = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if ( child ) {
            throw new CalculatedDependencyException(name, parentName);
        }
    }

    /**
     * This method finds if the question qId is the child question of any Skip Rule or Calculated questions
     * within questionIds.
     *
     * @param questions       the question list to check against for deleted question
     * @param deletedQuestion the question  to check for if it is the child of skip rule or calculated questions
     * @return boolean  return ture if the deleted question is any of skip rule or calculated questions' dependent question;
     *         false if else.
     */
    private boolean isParentQuestionInDelete(List<Question> questions, Question deletedQuestion) {
    	boolean parentFound = false;

        for ( Question question : questions ) {
            int qsId = question.getId();
            if (qsId != deletedQuestion.getId()) {
                if (question.getFormQuestionAttributes().hasSkipRule()) {
                    for ( Question qq : question.getFormQuestionAttributes().getQuestionsToSkip() ) {
                        if (qq.getId() == deletedQuestion.getId()) {
                            parentFound = true;
                            return parentFound;
                        }
                    }
                }
                
                if (question.getFormQuestionAttributes().isCalculatedQuestion()) {
                    for ( Question qq : question.getCalculatedFormQuestionAttributes().getQuestionsToCalculate() ) {
                        if (qq.getId() == deletedQuestion.getId()) {
                            parentFound = true;
                            return parentFound;
                        }
                    }
                }
            }
        }
        
        return parentFound;
    }

    /**
     * Validates the delete of skip rule question.  Checks for dependency condition in the current form.
     *
     * @param form      The Form object contains the form ID
     * @param questions a list of being deleted questions.
     * @param exclude   if excluding a section. If false, parameter sectionId is ignored.
     * @param sectionId the section to exclude
     * @throws SkipRuleDependencyException Thrown if dependecy condition fails
     */

    public void validateDeleteSkipRuleQuestion(Form form, List<Question> questions, boolean exclude, int sectionId) throws SkipRuleDependencyException {
        List<String> sectionIds = new ArrayList<String>();
        sectionIds.add(Integer.toString(sectionId));
        validateDeleteSkipRuleQuestion(form, questions, exclude, sectionIds);
    }

    /**
     * Validates the deleted skip rule questions. Checks for dependency condition in the current form.
     *
     * @param form       The current form object.
     * @param questions  The list of questions being deleted.
     * @param exclude    true if excluding a section; false if parameter sectionIds are ignored.
     * @param sectionIds the section to exclude
     * @throws SkipRuleDependencyException Thrown if dependency condition fails
     */
    public void validateDeleteSkipRuleQuestion(Form form, List<Question> questions, boolean exclude, 
    		List<String> sectionIds) throws SkipRuleDependencyException {
        String name = null;
        String parentName = null;
        boolean child = false;

        for ( Question deletedQuestion : questions ) {
            boolean isParentInDelete = false;

            //if the deleted question has no skip rules, check to see if the question is the skip rule question for
            //other questions
            if (!deletedQuestion.getFormQuestionAttributes().hasSkipRule()) {
                isParentInDelete = this.isParentQuestionInDelete(questions, deletedQuestion);
                
                if (!isParentInDelete) {
                    //The passed in Form object is a fully populated form object, therefore
                    //we just need to get what we want from this form object.

                    // get questions for the current form
                    for ( Iterator<List<Section>> iter = form.getRowList().iterator(); (iter.hasNext() && !child); ) {
                        for ( Iterator<Section> sIt = iter.next().iterator(); (sIt.hasNext() && !child); ) {
                            Section section = sIt.next();
                            
                            if (section != null && !(exclude && (sectionIds.contains(Long.toString(section.getId()))))) {
                                for ( Iterator<Question> iter1 = section.getQuestionList().iterator(); (iter1.hasNext() && !child); ) {
                                    Question currentQuestion = iter1.next();
                                    
                                    if (currentQuestion.getFormQuestionAttributes().hasSkipRule()) {
                                    	List<Question> skipQuestions = currentQuestion.getFormQuestionAttributes().getQuestionsToSkip();
                                        for ( Iterator<Question> iter2 = skipQuestions.iterator(); (iter2.hasNext() && !child); ) {
                                            Question question = iter2.next();
                                            
                                            if ( question.getId() == deletedQuestion.getId() ) {
                                                parentName = currentQuestion.getName();
                                                name = deletedQuestion.getName();
                                                child = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (child) {
            throw new SkipRuleDependencyException(name, parentName);
        }
    }

    private void validateDeleteRow(Form form, int rowId, FormManagerDao dao)
            throws CalculatedDependencyException, SkipRuleDependencyException, CtdbException, AdministeredSectionRemovalException {
        List<String> sectionIds = new ArrayList<String>();
        List<Question> questions = new ArrayList<Question>();

        for ( List<Section> row : form.getRowList() ) {
            for ( Section s : row ) {
                if ( s != null ) {
                    if (s.getRow() == rowId) {
                        sectionIds.add(Long.toString(s.getId()));
                        
                        for ( Question question : s.getQuestionList() ) {
                            questions.add(question);
                        }
                    }
                }
            }
        }
        
        if (sectionIds.size() > 0 && dao.isAdministeredSection(sectionIds)) {
            throw new AdministeredSectionRemovalException();
        }
        
        this.validateDeleteQuestion(form, questions, true, sectionIds);
        validateDeleteSkipRuleQuestion(form, questions, true, sectionIds);
    }

    /**
     * Validates the delete of a section containing calculated question.  Checks for dependency condition in the current form.
     *
     * @param form    The Form object contains Form ID
     * @param section The Section object contains Section ID
     * @throws CalculatedDependencyException If an error occurs while validating question deletion.
     * @throws SkipRuleDependencyException If an error occurs while validating skip rule deletion.
     */
    public void validateDeleteSection(Form form, Section section) throws CalculatedDependencyException, SkipRuleDependencyException  {
        List<Question> questions = section.getQuestionList();
        int sectionId = section.getId();
        
        for ( Question q : questions ) {
            List<Question> qList = new ArrayList<Question>();
            qList.add(q);
            validateDeleteQuestion(form, qList, true, sectionId);
            validateDeleteSkipRuleQuestion(form, qList, true, sectionId);
        }
    }

    /**
     * Checks to see if the form has questions attached.
     *
     * @param form the Form object to check on
     * @return boolean  true: has questions attached; false: no question attached
     * @throws CtdbException thrown if any errors occurs
     */
    public boolean getHasQuestionsAttached(Form form) throws CtdbException {
        boolean hasQuestions = false;
        
        for ( List<Section> row : form.getRowList() ) {
            for ( Section section : row ) {
                if ( section != null ) {
                    if ( !getSectionQuestions(section.getId()).isEmpty() ) {
                        hasQuestions = true;
                        break;
                    }
                }
            }
        }
        
        return hasQuestions;
    }
    
    public List<Question> getQuestions(Form form) throws CtdbException {
        List<Question> allQuestions = new ArrayList<Question>();
        
        for ( List<Section> row : form.getRowList() ) {
            for ( Section section : row ) {
                if (section != null) {
                	allQuestions.addAll(getSectionQuestions(section.getId()));
                }
            }
        }
        
        return allQuestions;
    }

    public void updateFormQuestionCalcAttributes(AdministeredForm admForm) throws CtdbException {

        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).updateFormQuestionCalcAttributes(admForm);
        }
        finally {
            this.close(conn);
        }
    }

    public void updateAdministeredForms(Patient patient, List<AdministeredForm> activeAdminForms) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).updateAdministeredForms(patient, activeAdminForms);
        }
        finally {
            this.close(conn);
        }
    }

    public void createFormGroup(FormGroup fg, int[] formIds) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).createFormGroup(fg, formIds);
        }
        catch (Exception e) {
            throw new CtdbException("Unable to create form group" + e.getMessage(), e);
        }
        finally {
            this.close(conn);
        }
    }
    
    
    public void deleteFormGroup(int formGroupId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).deleteFormGroup(formGroupId);
        }
        finally {
            this.close(conn);
        }
    }

    public void associateFormsToGroup(List<Form> forms, FormGroup fg) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).associateFormsToGroup(forms, fg);
        }
        finally {
            this.close(conn);
        }
    }
    
    
    public void associateFormsToGroup(int[] formIds, int fgId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).associateFormsToGroup(formIds, fgId);
        }
        finally {
            this.close(conn);
        }
    }
    
    
    public void disassociateFormsFromGroup(int[] formIds, FormGroup fg) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).disassociateFormsFromGroup(formIds, fg);
        }
        finally {
            this.close(conn);
        }
    }


    public void associateFormToGroup(Form form, FormGroup fg) throws CtdbException {
        List<Form> formList = new ArrayList<Form>();
        formList.add(form);
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).associateFormsToGroup(formList, fg);
        }
        finally {
            this.close(conn);
        }
    }


    public void asociateFormToGroup(Form form, int formGroupId) throws CtdbException {
        FormGroup fg = new FormGroup();
        fg.setId(formGroupId);
        List<Form> formList = new ArrayList<Form>();
        formList.add(form);
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).associateFormsToGroup(formList, fg);
        }
        finally {
            this.close(conn);
        }
    }

    public void asociateFormToGroup(int formId, int formGroupId) throws CtdbException {
        FormGroup fg = new FormGroup();
        fg.setId(formGroupId);
        Form form = new Form();
        form.setId(formId);
        List<Form> formList = new ArrayList<Form>();
        formList.add(form);

        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).associateFormsToGroup(formList, fg);
        }
        finally {
            this.close(conn);
        }
    }

    public void associateFormsToGroup(List<Form> forms, int formGroupId) throws CtdbException {
        FormGroup fg = new FormGroup();
        fg.setId(formGroupId);
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).associateFormsToGroup(forms, fg);
        }
        finally {
            this.close(conn);
        }
    }

    public FormGroup getFormGroup(int formGroupId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getFormGroup(formGroupId);
        }
        finally {
            this.close(conn);
        }
    }
    
    public int[] getAssociatedFormIdsForFormGroup(int formGroupId) throws CtdbException {
    	int[] formIds = null;
    	Connection conn = null;
    	
    	try {
    		conn = getConnection();
    		formIds = FormManagerDao.getInstance(conn).getAssociatedFormIdsForFormGroup(formGroupId);
    	}
    	finally {
    		close(conn);
    	}
    	
    	return formIds;
    }

    public void updateFormGroup(FormGroup fg, int[] formIds) throws CtdbException {

        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).updateFormGroup(fg, formIds);
        }
        finally {
            this.close(conn);
        }
    }

    public List<FormGroup> getFormGroups(int protocolId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getFormGroups(protocolId);
        }
        finally {
            this.close(conn);
        }
    }

    public List<FormGroup> getAssociatedFormGroups(int formId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getAssociatedFormGroups(formId);
        }
        finally {
            this.close(conn);
        }
    }

    public List<FormGroup> getAvailiableFormGroups(int formId, int protocolId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            
            return dao.getAvailiableFormGroups(formId, protocolId);
        }
        finally {
            this.close(conn);
        }
    }

    public void updatedFormGroupOrder(String[] ids) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).updatedFormGroupOrder(ids);
        }
        finally {
            this.close(conn);
        }
    }

    public void updateFormIntervalAssociations(ArrayList<String> formIds, int intervalId) throws CtdbException, InvalidRemovalException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            FormManagerDao dao = FormManagerDao.getInstance(conn);
            dao.disassociateFormsFromInterval(intervalId, formIds);
            dao.associateFormsToInterval(formIds, intervalId);
            commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    public HashMap<String,String> getAvailiableFormsForInterval(int intervalId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getAvailiableFormsForInterval(intervalId);
        }
        finally {
            this.close(conn);
        }
    }

    public HashMap<String,String> getAssociatedFormsForInterval(int intervalId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getAssociatedFormsForInterval(intervalId);
        }
        finally {
            this.close(conn);
        }
    }
    
    /**
     * Method to get colum and row values for given form fromId and studyId formLaout table
     * @param formId
     * @param studyId
     * @return
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public ArrayList<FormLayout> getFormLayoutRowAndColumn(int formId, int studyId) throws ObjectNotFoundException, CtdbException {
    	   Connection conn = null;
    	   
           try {
               conn = CtdbManager.getConnection();
               return FormManagerDao.getInstance(conn).getFormLayoutRowAndColumn(formId,studyId);
           }
           finally {
               this.close(conn);
           }
    }

    public void saveCellFormatting(CellFormatting cf) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            FormManagerDao.getInstance(conn).saveCellFormatting(cf);
        }
        finally {
            this.close(conn);
        }
    }

    public List<CellFormatting> getCellFormatting(int formId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getCellFormatting(formId);
        }
        finally {
            this.close(conn);
        }
    }

    public Map<String, CellFormatting> getCellFormattingMap(int formId) throws CtdbException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getCellFormattingMap(formId);
        }
        finally {
            this.close(conn);
        }
    }

    public List<CellFormatting> getCellFormatting(int formId, int row, int col) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getCellFormatting(formId, row, col);
        }
        finally {
            this.close(conn);
        }
    }
    
   /**
    * Method to get the section group for parent sectionId 
    * @param parentSectionId
    * @return
    * @throws CtdbException
    */
    public List<Integer> getRepeableSectionGroup(int parentSectionId) throws CtdbException {
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection();
			return FormManagerDao.getInstance(conn).getRepeableSectionGroup(parentSectionId);
		}
		finally {
			this.close(conn);
		}
    }
    
    
    
    /**
     * Method to get the parent repeatable section ids for all repeatable groups in form
     * @param parentSectionId
     * @return
     * @throws CtdbException
     */
     public List<Integer> getRepeableSectionParentSectionIds(int formId) throws CtdbException {
 		Connection conn = null;
 		
 		try {
 			conn = CtdbManager.getConnection();
 			return FormManagerDao.getInstance(conn).getRepeableSectionParentSectionIds(formId);
 		}
 		finally {
 			this.close(conn);
 		}
     }
     
     
    
    /**
     * Method to get unique list of questions for a given study and form 
     * @param formId
     * @param studyId
     * @return
     * @throws CtdbException
     */
    public List<Integer> getUniqueQuestionsForFormInStudy(int formId, int studyId) throws CtdbException {
 	   Connection conn = null;
 	   
        try {
            conn = CtdbManager.getConnection();
            return FormManagerDao.getInstance(conn).getUniqueQuestionsForFormInStudy(formId ,studyId);
        }
        finally {
            this.close(conn);
        }
    }
    
    public void storeXml(int formId) throws CtdbException, TransformationException {
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            Form f = this.getFormAndSetofQuestions(formId);
            FormManagerDao.getInstance(conn).storeXml(formId, f.toXML());
            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    public int getEmailTriggerVersion(int questionAttributeId) throws CtdbException {
    	Connection conn = null;
    	
    	try {
    		conn = CtdbManager.getConnection();
    		return FormManagerDao.getInstance(conn).getEmailTriggerVersion(questionAttributeId);
    	}
    	finally {
    		this.close(conn);
    	}
    }
    
    // added by Ching Heng to update question version
    public void updateQuestionVersion(int questionId,int questionVersion) throws CtdbException {
    	 Connection conn = null;
    	 
         try {
             conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
             FormManagerDao dao = FormManagerDao.getInstance(conn);
             dao.updateQuestionVersionInQuestionAttribute(questionId, questionVersion);
             dao.updateQuestionVersionInSectionQuestion(questionId, questionVersion);
             commit(conn);
         }
         finally {
        	 this.rollback(conn);
             this.close(conn);
         }
    }
    
    /**
     * Method to insert formLayout informaiton in formlayout table for given formId row and column informaiton for XML export import story
     * @param formId
     * @param rowNo
     * @param colNo
     * @throws CtdbException
     */
    public void addRowInFormLayoutTableForGivenFormId(int formId, int rowNo, int colNo) throws CtdbException {
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			FormManagerDao.getInstance(conn).addRowInFormLayoutTableForGivenFormId(formId, rowNo,colNo);
			commit(conn);
		}
		finally {
			rollback(conn);
			close(conn);
		}
	}
    
    /**
     * Gets a JSON array of forms that have not been migrated yet.
	 * 
	 * TODO Remove this method once eform migration is done.
     * 
     * @return A JSON array of forms to migrate. Each JSON object attribute name corresponds to
     * the form table column name.
     * @throws CtdbException When a database error occurs.
     * @throws JSONException When there is an error when converting the DB results to a JSON object.
     */
    public JSONArray getFormsToMigrate() throws CtdbException, JSONException {
    	JSONArray formArray = null;
    	Connection conn = null;
    	
    	try {
    		conn = CtdbManager.getConnection();
    		formArray = FormManagerDao.getInstance(conn).getFormsToMigrate();
    	}
    	finally {
    		close(conn);
    	}
    	
    	return formArray;
    }
    
    
    
    public void updateProformsTablesForMigration(int formId, EformMigrationAdapter eformMigrationAdapter) throws CtdbException {
    	Connection conn = null;
    	
    	try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			FormManagerDao dao = FormManagerDao.getInstance(conn);
			dao.updateProformsTablesForMigration(formId, eformMigrationAdapter);
			this.commit(conn);
		}
    	finally {
			this.rollback(conn);
			this.close(conn);
		}
    }
    
    public Integer getFormXsubmissionStatus(Integer formid) throws CtdbException {
        Connection conn = null;
        Integer formXsubmissionStatus = null;
        try {
            conn = CtdbManager.getConnection();
            formXsubmissionStatus = FormManagerDao.getInstance(conn).getAdministeredFormSubmissionStatus(formid);
        }finally {
            this.close(conn);
        }
        if(formXsubmissionStatus == null){
        	return -1;
        } else {
        	return formXsubmissionStatus;
        }
    } 
    
}
