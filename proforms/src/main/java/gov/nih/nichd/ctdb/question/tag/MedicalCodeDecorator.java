package gov.nih.nichd.ctdb.question.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;

/* 
* MedicalCodeDecorator enables a table to have a column with Action links (Edit/View/..). This
* class works with the <code>display</code> tag library.
*
* @author Ching-Heng
* @version 1.0
* It is need to do more thing!!!!
*/
public class MedicalCodeDecorator extends ActionDecorator{
	public MedicalCodeDecorator(){
		super();
	}
	
	public String getMedicalCodeCheckbox(){
		return "<input name=\"medicalCodeId\" type=\"checkbox\"/>";
	}
	public String getMedicalGroupName(){
		return "LALALA~MedicalGroupName";
	}
	public String getMedicalDescription(){
		return "WOWOWO~MedicalDescription";
	}
}
