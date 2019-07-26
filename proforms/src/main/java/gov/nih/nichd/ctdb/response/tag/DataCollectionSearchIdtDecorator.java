package gov.nih.nichd.ctdb.response.tag;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.patient.common.PatientResultControl;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.response.form.DataCollectionLandingForm;

public class DataCollectionSearchIdtDecorator extends ActionIdtDecorator{
	List<Patient> patients;

    /** Creates a new instance of DataCollectionSearchDecorator */
    public DataCollectionSearchIdtDecorator()
    {
        super();
        try {
	        PatientManager pm = new PatientManager();
	 	   PatientResultControl prc = new PatientResultControl();
	 	   patients = pm.getMinimalPatients(prc);
        }catch(Exception e) {
        	e.printStackTrace();
        }
    }
    
    /**
     * getProtocolNumber decorator returns the visit date to the jsp page
     * @return
     * @throws JspException
     */
    public String getProtocolNumber() throws JspException
    {
    	DataCollectionLandingForm dtForm = (DataCollectionLandingForm) this.getObject();
    	return dtForm.getProtocolNumber();
    }
    
    
  
    
    /**
     * getProtocolName decorator returns the visit date to the jsp page
     * @return
     * @throws JspException
     */
    public String getProtocolName() throws JspException
    {
    	DataCollectionLandingForm dtForm = (DataCollectionLandingForm) this.getObject();
    	return dtForm.getProtocolName();
    }
    
    /**
     * getVisitdate decorator returns the visit date to the jsp page
     * @return
     * @throws JspException
     */
    public String getVisitDate() throws JspException
    {
    	DataCollectionLandingForm dtForm = (DataCollectionLandingForm) this.getObject();
    	return dtForm.getVisitDate();
    }
    
    /**
     * getIntervalName decorator returns the interval name to the jsp page
     * @return String, the html
     * @throws JspException
     */
    
    public String getIntervalName() throws JspException
    {
    	DataCollectionLandingForm dtForm = (DataCollectionLandingForm) this.getObject();
    	return dtForm.getIntervalName();
    }
    
    
    /**
     * getPatientName decorator returns the patient name to the jsp page
     * @return String, the html
     * @throws JspException
     */
    public String getPatientName() throws JspException
    {
    	DataCollectionLandingForm dtForm = (DataCollectionLandingForm) this.getObject();
    	return dtForm.getPatientName();
    	
    }
    
    
    /**
     * getPatientId decorator returns the patient id to the jsp page 
     * @return String, the html
     * @throws JspException
     */
    public int getPatientId() throws JspException
    {
    	DataCollectionLandingForm dtForm = (DataCollectionLandingForm) this.getObject();
    	return dtForm.getPatientId();
    }
    
    /**
     * getNihRecordNo decorator returns the nih record no to the jsp page 
     * @return String, the html
     * @throws JspException
     */
    public String getNihRecordNo() throws JspException
    {
    	DataCollectionLandingForm dtForm = (DataCollectionLandingForm) this.getObject();
    	return dtForm.getSubjectId();
    }
    
    
    /**
     * getFormName decorator returns the form name to the jsp page
     * @return String, the html
     * @throws JspException
     */
    public String getFormName() throws JspException
    {
    	DataCollectionLandingForm dtForm = (DataCollectionLandingForm) this.getObject();
    	 String root = this.getWebRoot();
    	 String fName = dtForm.getFormName();
    	 int fId = dtForm.getFormId();
    	  String anchorTag = "<a href=\"Javascript:popupWindowWithMenu ('" + root + "/form/viewFormDetail.action?source=popup&id=" + fId + "');\">" + fName + "</a>";   	   	
    	  return anchorTag;

    }
    
    /**
     * getFormStatusName decorator returns the form name to the jsp page
     * @return
     * @throws JspException
     */
    public String getFormStatusName() throws JspException
    {
    	DataCollectionLandingForm dtForm = (DataCollectionLandingForm) this.getObject();
    	return dtForm.getFormStatusName();
    }
    
    /**
     * getFormStatus decorator returns the form status to the jsp page
     * @return String, the html
     * @throws JspException
     */
    public int getFormStatus() throws JspException
    {
    	DataCollectionLandingForm dtForm = (DataCollectionLandingForm) this.getObject();
    	return dtForm.getFormStatus();
    }
    
    /**
     * getFormLastUpdated decorator returns the form last updated date to the jsp page
     * @return String, the html
     * @throws JspException
     */
   public String getFormLastUpdatedDate() throws JspException
   {
	   DataCollectionLandingForm dtForm = (DataCollectionLandingForm) this.getObject();
	   /*SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	   if(dtForm.getFormLastUpdatedDate()!=null){
		   return df.format(dtForm.getFormLastUpdatedDate());
	   }else{
		   return "N/A";
	   }*/
	  
	   return dtForm.getFormLastUpdatedDate();
   }
   
   
   public Date getDate() throws JspException, ParseException
   {
	   DataCollectionLandingForm dtForm = (DataCollectionLandingForm) this.getObject();
	   String updateDate = dtForm.getFormLastUpdatedDate();
	   DateFormat formatter ; 
	   Date date ; 
	   formatter = new SimpleDateFormat("default.system.dateformat");
	   date = (Date)formatter.parse(updateDate); 
	   /*SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	   if(dtForm.getFormLastUpdatedDate()!=null){
		   return df.format(dtForm.getFormLastUpdatedDate());
	   }else{
		   return "N/A";
	   }*/
	  
	   return date;
   }
    
   
   public String getFormId()
   {
	   DataCollectionLandingForm domainObject = (DataCollectionLandingForm) this.getObject();
       int formId = domainObject.getFormId();
       //return "<input type='checkbox' name='selectFormId' id=\"" + formId +"\" onclick=\"validateSelectedDataCollection()\" value=\"" + formId + "\"/>";
      // return "<input type='checkbox' name='selectProtocolId' id=\"" + protocolId + "\" onclick=\"validateSelectedProtocols()\"/>";
       return String.valueOf(formId);
   }
   
   
   public String getNpFormId()
   {
	   Form form = (Form) this.getObject();
	   int  nonPatFormId = form.getId();
	   int npFormType = form.getFormType();
	   //return "<input type='checkbox' name='selectFormId' id=\"" + nonPatFormId +"\" onclick=\"validateSelectedNonPatientDataCollection("+npFormType+")\"  value=\""+ nonPatFormId + "\"  />";
	  return String.valueOf(nonPatFormId);
	   
   }
   
   public String getPatId()
   {
	   DataCollectionLandingForm domainObject = (DataCollectionLandingForm) this.getObject();
	/*   String patRecNumber = domainObject.getPatientRecordNumber();
	   int patId = -1;
	   
	   


       List patientOptions = new ArrayList();
       Iterator iter = patients.iterator();
       while (iter.hasNext()) {
          Patient p = (Patient) iter.next();
          String recNumber = p.getNihRecordNumber();
          if(recNumber.equals(patRecNumber)) {
        	  patId = p.getId();
        	  break;
          }
       }*/
       
       
	   //domainObject.getPatientRecordNumber();
       /*return "<input type='checkbox' name='selectFormId' id=\"" + patId +"\" onclick=\"validateSelectedDataCollection()\"/>";*/
       /*return "<input type='checkbox' name='selectFormId' id=\"" + domainObject.getPvVisitDateId() +"\" onclick=\"validateSelectedDataCollection()\"/>";*/
       return domainObject.getPvVisitDateId();
       
   }
   
   
   /**
    * Get nonPatient Form Name
    * @return
    * @throws JspException
    */
   public String getName() throws JspException
   {
	   Form nonPatFormName = (Form) this.getObject();
	   String root = this.getWebRoot();
	   String name = nonPatFormName.getName();
	   int formId = nonPatFormName.getId();
	   String anchorTag = "<a href=\"Javascript:popupWindowWithMenu ('" + root + "/form/viewFormDetail.action?source=popup&id=" + formId + "');\">" + name + "</a>"; 	   	
   	   return anchorTag;
   }
   
   /**
    * getProtocolNumber decorator returns the visit date to the jsp page
    * @return
    * @throws JspException
    */
   public String getNpFormStatus() throws JspException
   {
	   Form npFormStatus = (Form) this.getObject();
   	   return npFormStatus.getStatus().getShortName();
   }
   
   /**
    * getPvVisitDate decorator returns the visit date to the jsp page
    * @return
    * @throws JspException
    */
   public String getPatientViewVisitDate() throws JspException{
	   DataCollectionLandingForm domainObject = (DataCollectionLandingForm) this.getObject();
	   return domainObject.getPvVisitDate();
		/* SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
         if (domainObject.getPvVisitDate() != null) {
             return df.format(domainObject.getPvVisitDate());
         } else {
             return "N/A";
         }
	   */
   }
   
   public String getDateNP() throws JspException{
	   Form form = (Form) this.getObject();
		 SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
       if (form.getUpdateDate() != null) {
           return df.format(form.getUpdateDate());
       } else {
           return "N/A";
       }
	   
   }
   
   /**
    * getPvPatientId decorator returns the patient id to the jsp page from patient table
    * @return
    * @throws JspException
    */
   public int getPvPatientId() throws JspException{
	   DataCollectionLandingForm domainObject = (DataCollectionLandingForm) this.getObject();
	   return domainObject.getPvPatientId();
   }
   
   public String getMrn() throws JspException{
	   DataCollectionLandingForm domainObject = (DataCollectionLandingForm) this.getObject();
	   return domainObject.getMrn();
   }
   
   public String getpvGuid() throws JspException{
	   DataCollectionLandingForm domainObject = (DataCollectionLandingForm) this.getObject();
	   return domainObject.getPvGuid();
   }
   
   
   /**
    * getPvPatientName decorator returns the patient name to the jsp page from the patient table
    * @return
    * @throws JspException
    */
   public String getPvPatientName() throws JspException{
	   DataCollectionLandingForm domainObject = (DataCollectionLandingForm) this.getObject();
	   return domainObject.getPvPatientName();
   }
   
   public String getPatientRecordNumber() throws JspException{
	   DataCollectionLandingForm domainObject = (DataCollectionLandingForm) this.getObject();
	   return domainObject.getSubjectId();
	}

   public String getPvIntervalName() throws JspException{
	   DataCollectionLandingForm domainObject = (DataCollectionLandingForm) this.getObject();
	   return domainObject.getPvIntervalName();
   }

}
