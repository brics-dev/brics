package gov.nih.nichd.ctdb.form.form;
import gov.nih.nichd.ctdb.common.CtdbForm;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

/**
 * The ImportForm represents the Java class behind the HTML site link form used on the system
 * to import a form.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */

public class XmlImportForm extends CtdbForm {
	private String existingFormName = null;
	private String newFormName = null;
	private HashMap <String,String> questionMap = new HashMap<String, String>();
	private boolean formNameError =false;
	
	private boolean formStructureDataElementsError = false;
	private String newNames = null;
	private File document = null;
    private String documentContentType = null;
    private String documentFileName = null;
    private InputStream documentContent = null;  
   
    public String getNewNames() {
		return newNames;
	}
	public void setNewNames(String newNames) {
		this.newNames = newNames;
	}
	public String getExistingFormName() {
		return existingFormName;
	}
	public void setExistingFormName(String existingFormName) {
		this.existingFormName = existingFormName;
	}
	public String getNewFormName() {
		return newFormName;
	}
	public void setNewFormName(String newFormName) {
		this.newFormName = newFormName;
	}
	public HashMap<String, String> getQuestionMap() {
		return questionMap;
	}
	public void setQuestionMap(HashMap<String, String> questionMap) {
		this.questionMap = questionMap;
	}
	public boolean isFormNameError() {
		return formNameError;
	}
	public void setFormNameError(boolean formNameError) {
		this.formNameError = formNameError;
	}
	
	public void setValue(String key, String value){
		questionMap.put(key, value);
	}
	public String getValue(String key){
		return questionMap.get(key);
	}

	public File getDocument() {
		return document;
	}
	public void setDocument(File document) {
		this.document = document;
	}
	public String getDocumentContentType() {
		return documentContentType;
	}
	public void setDocumentContentType(String documentContentType) {
		this.documentContentType = documentContentType;
	}
	public String getDocumentFileName() {
		return documentFileName;
	}
	public void setDocumentFileName(String documentFileName) {
		this.documentFileName = documentFileName;
	}
	public InputStream getDocumentContent() {
		return documentContent;
	}
	public void setDocumentContent(InputStream documentContent) {
		this.documentContent = documentContent;
	}
	public boolean isFormStructureDataElementsError() {
		return formStructureDataElementsError;
	}
	public void setFormStructureDataElementsError(boolean formStructureDataElementsError) {
		this.formStructureDataElementsError = formStructureDataElementsError;
	}
}
