package gov.nih.nichd.ctdb.emailtrigger.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;
import gov.nih.nichd.ctdb.emailtrigger.manager.EmailTriggerManager;
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

    public EmailTriggerThread (EmailTrigger et, Response r) {
        this.et = et;
        Response resp = new Response();
        resp.clone(r);
        this.r = resp;

    }

    public void run () {
        try {

        	List<String> triggeredAnswers;
        	if(r.getEditAnswers().size() > 0) {
        		triggeredAnswers = this.triggeredAnswers(et, r.getEditAnswers());
        	}else {
        		triggeredAnswers = this.triggeredAnswers(et, r.getAnswers());
        	}
        	
            

            if (triggeredAnswers.size() > 0) {
                EmailTriggerManager eman = new EmailTriggerManager();
                r.getAdministeredForm().getPatient().setId(r.getAdministeredForm().getPatient().getId());
                eman.checkSendEmail (et, r, triggeredAnswers);
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
    private List<String> triggeredAnswers (EmailTrigger et, List<String> answers) {
    	List<String> al  = new ArrayList<String>();

    	for ( String triggerAnswer : et.getTriggerAnswers() ) {
    		triggerAnswer = triggerAnswer.trim();

    		if ( triggerAnswer.equals(CtdbConstants.OTHER_OPTION_DISPLAY) ) {
    			if (r.isAnswerIncludesOtherPleaseSpecify()) {
    				al.add(triggerAnswer);
    			}
    		}
    		else {
    			for ( String a : answers ) {
    				a = a.trim();

    				if ( triggerAnswer.equals(a)) { 
    					al.add(triggerAnswer);
    				}
    			}
    		}
    	}

    	return al;
    }
}
