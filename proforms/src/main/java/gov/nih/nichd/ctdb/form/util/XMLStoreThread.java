package gov.nih.nichd.ctdb.form.util;

import gov.nih.nichd.ctdb.form.manager.FormManager;

/**
 * Created by IntelliJ IDEA. User: breymaim Date: Jun 19, 2006 Time: 11:04:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class XMLStoreThread extends Thread {
	private int formId = Integer.MIN_VALUE;

	public XMLStoreThread(int formId) {
		this.formId = formId;
	}

	public void run() {
		try {
			FormManager fm = new FormManager();
			fm.storeXml(this.formId);
		} catch (Exception e) {
			System.err.println("FAILURE RETREIVING AND STORING XML FOR FORM ");
			e.printStackTrace();
		}
	}
}
