package gov.nih.nichd.ctdb.form.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;






public class NewCreateEditFormDecorator extends ActionDecorator {

	
	public NewCreateEditFormDecorator() {
		super();
	}
	
	
	
	
	/**
     * retrieves the checkbox and adds an onclick method to it to uncheck the others
     * 
     * @return
     */
    public String getDataStructureRadio() {

    	String text = "";
    	FormStructure ds = (FormStructure)this.getObject();
    	//long id = ds.getId();
    	String name = ds.getShortName();
    	String version = ds.getVersion();
    	//String fullName = name + "(" + version + ")";

    	//text = "<input type=\"radio\" name=\"dataStructureRadio\" value=\"" + fullName + "\" onclick=\"setCopyright(" + ds.getIsCopyrighted() + "); \" />\n";
    	text = "<input type=\"radio\" name=\"dataStructureRadio\" value=\"" + name + "\" />\n";
    	return text;
    	
    }
	
	
	
	public String getIsCopyrighted() {
		FormStructure ds = (FormStructure)this.getObject();
		if(ds.getIsCopyrighted()) {
			return "Yes";
		}else {
			return "No";
		}
	}
	

}
