package gov.nih.nichd.ctdb.question.action;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.question.common.QuestionAssembler;
import gov.nih.nichd.ctdb.question.common.QuestionConstants;
import gov.nih.nichd.ctdb.question.domain.ImageMapQuestion;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.form.QuestionWizardStartForm;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;


public class ImageMapWizardAction extends BaseAction {
    
	private static final long serialVersionUID = 6587037592930946681L;

	QuestionWizardStartForm questionForm = new QuestionWizardStartForm();
	  
    public String addImageType() {
     	Question question = new ImageMapQuestion();
     	QuestionAssembler.questionWizardStart(questionForm, question);
     	session.put(QuestionConstants.QUESTION_IN_PROGRESS, question);
     	return SUCCESS;
    }
    
    public String editImageType() {
    	int id = Integer.parseInt(request.getParameter("id"));
		
    	try {
	        QuestionManager qm = new QuestionManager();
	        Question question = qm.getQuestion(id);
			session.put(QuestionConstants.QUESTION_IN_PROGRESS, question);
			session.put(QuestionConstants.ORIGINAL_QUESTION_OBJ, question);
			
		} catch (CtdbException ce) {
	    	return StrutsConstants.FAILURE;
	    }
	        
		return SUCCESS; 
    }
    
    
	public QuestionWizardStartForm getQuestionForm() {
		return questionForm;
	}

	public void setQuestionForm(QuestionWizardStartForm questionForm) {
		this.questionForm = questionForm;
	}

}