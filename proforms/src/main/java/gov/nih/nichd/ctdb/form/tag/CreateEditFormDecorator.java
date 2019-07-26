package gov.nih.nichd.ctdb.form.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;






public class CreateEditFormDecorator extends ActionDecorator {

	
	public CreateEditFormDecorator() {
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
    	String name = ds.getShortName();
    	String version = ds.getVersion();
    	//String fullName = name + "(" + version + ")";

    	text = "<input type=\"radio\" name=\"dataStructureRadio\" value=\"" + name + "\" onclick=\"setDataStructure(\'" + name + "\'," + version + ");setCopyright(" + ds.getIsCopyrighted() + "); \" />\n";
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
