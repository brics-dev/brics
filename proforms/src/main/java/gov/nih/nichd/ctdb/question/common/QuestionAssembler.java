package gov.nih.nichd.ctdb.question.common;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbAssembler;
import gov.nih.nichd.ctdb.question.domain.Group;
import gov.nih.nichd.ctdb.question.domain.ImageMapQuestion;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.VisualScale;
import gov.nih.nichd.ctdb.question.form.AddEditQuestionForm;
import gov.nih.nichd.ctdb.question.form.QuestionWizardStartForm;

/**
 * Enables assembly of a Question domain object into a QuestionForm object and vice-versa.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionAssembler extends CtdbAssembler {
    
    public static Question questionWizardStart(QuestionWizardStartForm form, Question q) {
        q.setName(form.getName());
        q.setText(form.getText());
        q.setType(QuestionType.getByValue(form.getType()));
        q.setDescriptionUp(form.getDescriptionUp());
        q.setDescriptionDown(form.getDescriptionDown());
        // added by josh park
        q.setHtmltext(form.getHtmlText());
        return q;
    }
    
    // added by Ching Heng for add question within form creation ========================================================================================================
    public static void OptionsQuestionsInForm(AddEditQuestionForm form, Question q) {
   	 ((VisualScale) q).setRightText(form.getRightText());
		 ((VisualScale) q).setLeftText(form.getLeftText());
		 ((VisualScale) q).setRangeEnd(form.getRangeEnd());
		 ((VisualScale) q).setRangeStart(form.getRangeStart());
		 ((VisualScale) q).setWidth(form.getWidth());
		 ((VisualScale) q).setCenterText (form.getCenterText());
		 ((VisualScale) q).setShowHandle (form.isShowHandle());
   }
    
    public static void QuestionWizardStartInForm(AddEditQuestionForm form, Question q) {
    	// questionWizardStart part
    	q.setName(form.getQuestionName());
        q.setText(form.getText());
        q.setType(QuestionType.getByValue(form.getType()));
        q.setHtmltext(form.getHtmlText());
        
        // add by sunny 
        q.setDescriptionUp(form.getDescriptionUp());
        q.setDescriptionDown(form.getDescriptionDown());
        
        // added by Ching Heng for Other option
        q.setIncludeOtherOption(form.isIncludeOtherOption());
        
        // added by Josh Park
        q.setHtmltext(form.getHtmlText());
        
        // questionWizardGroupUmls part
        if (form.getDefaultValue() != null) {
            q.setDefaultValue(form.getDefaultValue().trim());
        }else{
        	q.setDefaultValue("");
        }
        if (form.getUnansweredValue() != null) {
            q.setUnansweredValue(form.getUnansweredValue().trim());
        }else{
        	q.setUnansweredValue("");
        }
        // SET GROUPS SELECTED
        if (form.getQuestionGroupIds() != null) {
            int[] questionGroups = form.getQuestionGroupIds();
            List<Group> groups = new ArrayList<Group>();
            
            for (int idx = 0; idx < questionGroups.length; idx++) {
            	Group group = new Group();
                group.setId(questionGroups[idx]);
                groups.add(group);
            }
            q.setGroupsAssociatedWith(groups);
        } else {
            q.setGroupsAssociatedWith(new ArrayList<Group>());
        }
    }

    public static Question createQuestion(QuestionType type) {
    	Question q=null;
    	if (type.equals(QuestionType.IMAGE_MAP)){
    		q =  new ImageMapQuestion();
    	}
    	else if(type.equals(QuestionType.VISUAL_SCALE) ){
    		q =  new VisualScale();
    	}
    	else{
    		q = new Question();
    	}
    	q.setType(type);
    	return q;
    }     
}