package gov.nih.nichd.ctdb.response.util;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;

/**
 * Created by Booz Allen Hamilton
 * Date: Jul 2, 2004
 * 
 */
public class FormRetrevialThread extends Thread {

    private String sessionId;
    private int formId;

    public FormRetrevialThread (String sessionId, int formId) {
        this.sessionId= sessionId;
        this.formId = formId;
        setDaemon(false);
    }


    public void run () {
        try {
            new FormThreadManager().setLoading(this.sessionId);
            this.setPriority(9);
            FormManager fm = new FormManager();
            Form form = fm.getFormAndSetofQuestions(this.formId);
            FormThreadManager.addForm(this.sessionId, form);
            new FormThreadManager().finishedLoading(this.sessionId);

        } catch (ObjectNotFoundException onfe) {
            this.stop();
        }
        catch (CtdbException ce) {
            this.stop();
        }
    }
}
