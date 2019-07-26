package gov.nih.nichd.ctdb.form.domain;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jul 24, 2006
 * Time: 11:20:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResponseImage extends CtdbDomainObject {
	private static final long serialVersionUID = -4909058613210295572L;
	
	private String fileName;
    private int administeredFormId;
    private int sectionId;
    private String sectionName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getAdministeredFormId() {
        return administeredFormId;
    }

    public void setAdministeredFormId(int administeredFormId) {
        this.administeredFormId = administeredFormId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }


    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public Document toXML () throws TransformationException{
    	Document document = super.newDocument();
    	Element root = super.initXML(document, "ResponseImage");
    	root.setAttribute("fileName", this.getFileName());
    	return document;
    }
}
