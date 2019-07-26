package gov.nih.nichd.ctdb.form.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;

/**
 * Developed by CIT.
 * @author Shashi Rudrappa
 * Date: June 01, 2012
 * @version 1.0
 */

public class ProtocolFormDecorator extends ActionDecorator
{
    /**
     * Default Constructor
     */
    public ProtocolFormDecorator()
    {
        super();
    }
    
    
    public String getShortNameCheckBox() throws JspException
    {	
    	BasicEform form = (BasicEform) this.getObject();
    	
    	
    	String text = "<input type=\"checkbox\" name=\"formShortNameCheckbox_" + form.getShortName()+ 
        		"\" value=\"" + form.getShortName() + "\" isCat=\""+form.getIsCAT().booleanValue()+"\" measurementType=\""+form.getMeasurementType()+ "\"/>"; // modified by Ching-Heng
        
        return  text;
    }

    /**
     * retrieves the radio button and adds an onclick method to it
     * 
     * @return
     */
	public String getIsMandatory()
	{
		String text = "";
		BasicEform form = (BasicEform) this.getObject();
		
		String disabled = (form.getIsMandatory()) ? "" : " disabled=\"disabled\"";
		
		
		text = "<input type=\"radio\" name=\"isMandatory_" + form.getShortName() + "\"" + disabled + " value=\"true\"" + 
		       " onclick=\"processRadioBtn('" + form.getShortName() + "', 'true')\" /> Required " +
 			   "<input type=\"radio\" name=\"isMandatory_" + form.getShortName() + "\"" + disabled + " checked=\"checked\" value=\"false\"" + 
		       " onclick=\"processRadioBtn('" + form.getShortName()+ "', 'false')\" /> Optional";
    	
    	return text;
    }
	
    /**
     * retrieves the radio button and adds an onclick method to it
     * 
     * @return
     */
	public String getIsSelfReport()
	{
		String text = "";
		BasicEform eform = (BasicEform) this.getObject();
	
		String disabled = (eform.getIsSelfReport()) ? "" : " disabled=\"disabled\"";
		
		text = "<input type=\"radio\" name=\"isSelfReport_" +eform.getShortName() + "\"" + disabled + " value=\"true\"" + 
		       " onclick=\"processSelfReportRadioBtn('" + eform.getShortName()+ "', 'true')\" /> Yes " +
 			   "<input type=\"radio\" name=\"isSelfReport_" + eform.getShortName() + "\"" + disabled + " checked=\"checked\" value=\"false\"" + 
		       " onclick=\"processSelfReportRadioBtn('" + eform.getShortName() + "', 'false')\" /> No";
    	
    	return text;
    }
	
	public String getName() throws JspException {
		BasicEform form = (BasicEform) this.getObject();
        long formId = form.getId();
        String title = form.getTitle();
        String shortName = form.getShortName();
//        
		String url = this.getWebRoot() + "/form/viewFormDetail.action?source=popup&id=" + formId + "&shortName=" + shortName;
      	String anchorTag = "<a href=\"Javascript:popupWindowWithMenu('"+url+" ');\">" + title + "</a>";

        return anchorTag;
    }
	
	public String getDescription() throws JspException {
		//modified by Ching-Heng
		BasicEform form = (BasicEform) this.getObject();
		long formId = form.getId();
	    String shortName = form.getShortName();
		String descriptionInfo = form.getDescription();
		String descLine = "";
		String url = this.getWebRoot() + "/form/viewPromisFormInfo.action?source=popup&id=" + formId + "&shortName=" + shortName;
		if(descriptionInfo==null) {
			descriptionInfo ="";
		}
		if(descriptionInfo.length() < 49) {
			if(form.getIsCAT().booleanValue()) {
				descLine = "<a href=\"Javascript:popupWindowWithMenu('"+url+" ');\">" + descriptionInfo + "</a>";
			}else {
				descLine = descriptionInfo;				
			}
		}else {
			if(form.getIsCAT().booleanValue()) {
				descLine = "<span class=\"descBeginning\">";
				descLine += descriptionInfo.substring(0, 47);
				descLine += "<a href=\"Javascript:popupWindowWithMenu('"+url+" ');\">...</a>";
			}else {
				descLine = "<span class=\"descBeginning\">";
				descLine += descriptionInfo.substring(0, 47);
				descLine +=	" <a href=\"javascript:;\" class=\"ellipsisExpandCollapse\" onclick=\"ellipsisExpandCollapse(this)\">...</a></span>";
				descLine += "<span style=\"display:none\">";
				descLine += descriptionInfo;
				descLine +=	" <a href=\"javascript:;\" class=\"ellipsisExpandCollapse\" onclick=\"ellipsisExpandCollapse(this)\">collapse</a></span>";
			}
		}
        return descLine;
    }
}
