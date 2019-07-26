package gov.nih.nichd.ctdb.response.util;

import gov.nih.nichd.ctdb.form.domain.Form;

import java.util.HashMap;


/**
 * Created by Booz Allen Hamilton
 * Date: Jul 2, 2004
 * 
 */
public class FormThreadManager {

    private static HashMap forms;
    private static HashMap sessionsExecuting;

    static {
        forms = new HashMap();
        sessionsExecuting = new HashMap();
    }

    public static void addForm (String sessionId, Form form) {
        forms.put(sessionId, form);
    }

    synchronized public Form getForm (String sessionId, int formId) {
        Form f = null;
        while (true) {

            if (forms.get(sessionId) == null) {
                try {
                    if (sessionsExecuting.get(sessionId) != null) {
                        // the thread is still executing, wait.
                        wait (200);
                    } else {
                        // form requested is not availiable and is not loading
                        System.err.println (" INVALID FORM REQUESTED FROM THREAD.... LOADING");
                        FormRetrevialThread frt = new FormRetrevialThread(sessionId, formId);
                        frt.start();
                        wait (2000);
                    }
                } catch (InterruptedException ie) {
                }
            continue;
            }
             f = (Form)forms.get(sessionId);
            forms.remove(sessionId);
            break;
        }
        return f;
    }

    public void setLoading (String sessionId) {
          sessionsExecuting.put (sessionId, Boolean.valueOf(true));
        // remove any form that exists but has not been claimed
        forms.remove(sessionId);
    }

    public boolean isLoading (String sessionId) {
        if (sessionsExecuting.get(sessionId) != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public void finishedLoading (String sessionId) {
        sessionsExecuting.remove(sessionId);
    }






}
