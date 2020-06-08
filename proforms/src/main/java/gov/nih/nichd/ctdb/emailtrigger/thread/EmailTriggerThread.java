package gov.nih.nichd.ctdb.emailtrigger.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTriggerValue;
import gov.nih.nichd.ctdb.emailtrigger.manager.EmailTriggerManager;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.response.domain.Response;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Mar 20, 2007
 * Time: 10:12:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class EmailTriggerThread extends Thread {
	private static final Logger log = Logger.getLogger(EmailTriggerThread.class);
	
    private EmailTrigger et;
    private Response r;
	private QuestionType questionType;
	private AnswerType answerType;
	private String condStrWithVal;

	public EmailTriggerThread(EmailTrigger et, Response r, QuestionType qt, AnswerType at, String condStrWithVal) {
        this.et = et;
        Response resp = new Response();
        resp.clone(r);
        this.r = resp;
		this.questionType = qt;
		this.answerType = at;
		this.condStrWithVal = condStrWithVal;

    }

    public void run () {
        try {

        	List<String> triggeredAnswers;
			List<String> triggeredValues;
        	if(r.getEditAnswers().size() > 0) {
				// triggeredAnswers = this.triggeredAnswers(et, r.getEditAnswers());
				triggeredValues = this.triggeredValues(et, r.getEditAnswers(), questionType, answerType);
        	}else {
				// triggeredAnswers = this.triggeredAnswers(et, r.getAnswers());
				triggeredValues = this.triggeredValues(et, r.getAnswers(), questionType, answerType);
        	}
        	
            

			// if (triggeredAnswers.size() > 0) {
			// EmailTriggerManager eman = new EmailTriggerManager();
			// r.getAdministeredForm().getPatient().setId(r.getAdministeredForm().getPatient().getId());
			// eman.checkSendEmail (et, r, triggeredAnswers);
			// }
			if (triggeredValues.size() > 0) {
                EmailTriggerManager eman = new EmailTriggerManager();
                r.getAdministeredForm().getPatient().setId(r.getAdministeredForm().getPatient().getId());
				eman.checkSendEmail(et, r, triggeredValues, questionType, answerType, condStrWithVal);
            }
        }
        catch (Exception e) {
           log.error("Error occurred while trying to send an email.", e);
        }

    }

    /**
     * Compares email trigger answers to selected answers in response object to
     * determine if trigger is triggered!
     * @param et
     * @param answers
     * @return
     */
	// private List<String> triggeredAnswers (EmailTrigger et, List<String> answers) {
	// List<String> al = new ArrayList<String>();
	//
	// for ( String triggerAnswer : et.getTriggerAnswers() ) {
	// triggerAnswer = triggerAnswer.trim();
	//
	// if ( triggerAnswer.equals(CtdbConstants.OTHER_OPTION_DISPLAY) ) {
	// if (r.isAnswerIncludesOtherPleaseSpecify()) {
	// al.add(triggerAnswer);
	// }
	// }
	// else {
	// for ( String a : answers ) {
	// a = a.trim();
	//
	// if ( triggerAnswer.equals(a)) {
	// al.add(triggerAnswer);
	// }
	// }
	// }
	// }
	//
	// return al;
	// }

	private List<String> triggeredValues(EmailTrigger et, List<String> answers, QuestionType qt, AnswerType at) {
    	List<String> al  = new ArrayList<String>();
    	String conditionStr = "";
    	
    	List<EmailTriggerValue> emailTriggerValues = new ArrayList<EmailTriggerValue>(et.getTriggerValues());
		if(qt == QuestionType.RADIO || qt == QuestionType.CHECKBOX 
				|| qt == QuestionType.SELECT || qt == QuestionType.MULTI_SELECT) {
			for (EmailTriggerValue triggerValue : emailTriggerValues) {

				String triggerAnswer = triggerValue.getAnswer();
	 
				if (triggerAnswer.equals(CtdbConstants.OTHER_OPTION_DISPLAY)) {
	    			if (r.isAnswerIncludesOtherPleaseSpecify()) {
						al.add(triggerAnswer);
	    			}
	    		}
	    		else {
	    			for ( String a : answers ) {
	    				a = a.trim();
	
						if (triggerAnswer.equals(a)) {
							al.add(triggerAnswer);
	    				}
	    			}
	    		}
    	   }

      } else if (qt == QuestionType.TEXTBOX && at == AnswerType.NUMERIC) {
    	  String qAnswer = answers.get(0).trim();
			conditionStr = emailTriggerValues.get(0).getTriggerCondition();
    	  al.add(conditionStr);
	  }

	  return al;
	}
}
