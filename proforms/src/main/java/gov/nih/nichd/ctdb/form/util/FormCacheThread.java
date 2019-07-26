package gov.nih.nichd.ctdb.form.util;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;

/**
 * Created by IntelliJ IDEA. User: breymaim Date: Apr 9, 2008 Time: 7:44:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class FormCacheThread extends Thread {

	private int formId = Integer.MIN_VALUE;
	private Form form = null;

	public FormCacheThread(int formId) {
		this.formId = formId;
	}

	public FormCacheThread(Form f) {
		this.formId = f.getId();
		this.form = f;
	}

	public void run() {
		try {
			this.setPriority(9);
			if (form == null) {
				FormManager fm = new FormManager();
				form = fm.getFormAndSetofQuestions(this.formId);
			}
			Document d = form.toXML();
			form.setFormXml(d);
			FormCache.getInstance().setForm(this.formId, form);

		} catch (Exception e) {
			System.err.println("FAILURE Cacheing Form");
			e.printStackTrace();
		}
	}
}
