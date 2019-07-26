package gov.nih.nichd.ctdb.response.util;

import java.util.Date;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.form.domain.Form;

/**
 * Created by Booz Allen Hamilton Date: Jul 8, 2004
 *
 */
public class XmlRetrevialThread extends Thread {

	private Form form;

	public XmlRetrevialThread(Form form) {
		this.form = form;
		setDaemon(false);
	}

	public void run() {
		try {
			XmlThreadManager xtm = new XmlThreadManager();
			String key = this.form.getId() + this.form.getVersion().getToString();

			xtm.setLoading(key);
			Document doc = form.toXML();
			TimestamptedXML tXml = new TimestamptedXML();
			tXml.setTime(new Date());
			tXml.setDoc(doc);
			xtm.addXml(key, tXml);
			xtm.finishedLoading(key);

		} catch (TransformationException te) {
			System.err.println(" TRANSFORMATION EXCEPTION IN XML RETREVIAL THREAD FAILURE");
			System.err.println(" Original Msg : " + te.getMessage());
			te.printStackTrace();
			this.stop();
		}
	}
}
