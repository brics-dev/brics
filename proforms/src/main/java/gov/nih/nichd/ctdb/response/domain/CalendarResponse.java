package gov.nih.nichd.ctdb.response.domain;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.question.domain.PatientCalendarQuestion;


/**
 * Response DomainObject for the NICHD CTDB Application
 *
 * @author  Booz Allen Hamilton
 * @version 1.0
 */

public class CalendarResponse extends PatientCalendarCellResponse
{
    
    private static final long serialVersionUID = -3933180384825636850L;
    
	private int adminFormId; 
    
     /**
     * Default Constructor for the Response Domain Object
     */
    public CalendarResponse()
    {
        super();       
    }

    public Document toXML() throws TransformationException
    {
        return this.toXML(false);
    }

    /**
     * This method allows the transformation of a Response into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException is thrown if there is an error during the XML transformation.
     */
    public Document toXML(boolean resolveDiscrepancies) throws TransformationException
    {
        try
        {           
            Document document = super.newDocument();
            Element root = super.initXML(document, "response");
            
            root.setAttribute("adminformid", Integer.toString(this.getAdminFormId()));
            
            String path = "question[@id=?]/calendarrows/calendarrow[@row=?]/rowcells/rowcell[@cell=?]/";
            
            Document dom = this.getQuestion().toXML();
                        
            JXPathContext context = JXPathContext.newContext(dom);
            
            String pathCurrent = "";
            
            PatientCalendarQuestion pcq = (PatientCalendarQuestion) this.getQuestion();

            if (resolveDiscrepancies)
            {
                context.setValue("question[@id=" + pcq.getId() + "]/@editType", "draftEdit");
            }
            else
            {
                context.setValue("question[@id=" + pcq.getId() + "]/@editType", "finalEdit");
            }

            int i, ii;  
            for (i=0; i< pcq.getNumberOfColumns(); i++)
            {
                for (ii=0; ii < pcq.getNumberOfRows(); ii++)
                {
                    
                    pathCurrent = StringUtils.replace(path, "@id=?", "@id=" + pcq.getId());
                    pathCurrent = StringUtils.replace(pathCurrent, "@row=?", "@row=" + ii);
                    pathCurrent = StringUtils.replace(pathCurrent, "@cell=?", "@cell=" + i);
                                   
                    PatientCalendarQuestion pcqResponse1 = (PatientCalendarQuestion) this.getResponse1().getQuestion();                                        
                    String data1 = pcqResponse1.getCalendarData(i,ii);
                    if ( data1 == null )
                    {
                        data1="-";
                    }

                    PatientCalendarQuestion pcqResponse2 = (PatientCalendarQuestion) this.getResponse2().getQuestion(); 
                    String data2 = pcqResponse2.getCalendarData(i,ii);
                    if (data2 == null)
                    {
                        data2 = "-";
                    }
    
                    String displayText;
    
                    displayText = ((PatientCalendarQuestion) this.getQuestion()).getCalendarData(i,ii);
    
                    if (resolveDiscrepancies && displayText == null)
                    {
                        if (!data1.equals(data2))
                        {
                            context.setValue(pathCurrent + "celldata/@discrepancy", "yes");
                            displayText = "R";                            
                        }
                        else
                        {
                        	displayText = data1;
                        }
                    }
                    else
                    {                         
                        if (displayText == null)
                        {
                            displayText = "NA";
                        }
                        else if (displayText.trim().length() < 1)
                        {
                            displayText = "-";
                        }
                    }
                    
                    context.setValue(pathCurrent + "displaytext" , displayText);                
                    context.setValue(pathCurrent + "responseid" , new Integer (((PatientCalendarQuestion) this.getQuestion()).getResponseId(i,ii)));
                }
            }
            
            try
            {            
                root.appendChild( document.importNode (dom.getDocumentElement(), true));
            }
            catch (DOMException de)
            {
                System.out.println(de.getMessage());                
            }
                        
            Element answersNode = document.createElement("answers");
            answersNode.appendChild(document.createTextNode("PatientCalendarResponses handle answers in the question itself"));
            root.appendChild(answersNode);

            return document;
        }
        catch(Exception ex)
        {
            throw new TransformationException("Unable to transform object " + this.getClass().getName()
                                                    + " with id = " + this.getId());
        }
    }
    
    public boolean hasDataEntryDiscrepancy() 
    {
        
        int i, ii;
        PatientCalendarQuestion resp1Calendar = (PatientCalendarQuestion) this.getResponse1().getQuestion();
        PatientCalendarQuestion resp2Calendar = (PatientCalendarQuestion) this.getResponse2().getQuestion();
        PatientCalendarQuestion finalCalendar = (PatientCalendarQuestion) this.getQuestion();                          
         
        for (i=0; i< resp1Calendar.getNumberOfColumns(); i++)
        {
            for (ii=0; ii < resp1Calendar.getNumberOfRows(); ii++)
            {
                if (finalCalendar.getCalendarData(i,ii) == null)
                {
                    if (resp1Calendar.getCalendarData(i, ii) != null && resp2Calendar.getCalendarData(i,ii) == null)
                    {   
                        return true;
                    }
                    else if (resp2Calendar.getCalendarData(i, ii) != null && resp1Calendar.getCalendarData(i,ii) == null)
                    {
                        return true;
                    }
                    else if (!(resp1Calendar.getCalendarData(i, ii)==null && resp2Calendar.getCalendarData(i, ii)==null))
                    {
                        if (!resp1Calendar.getCalendarData(i, ii).equals(resp2Calendar.getCalendarData(i, ii)))
                        { 
                            return true;      
                        }
                    }
                }
            }
        }   
        
        return false; 
    }
    
    /**
     * @return
     */
    public int getAdminFormId() {
        return adminFormId;
    }

    /**
     * @param i
     */
    public void setAdminFormId(int i) {
        adminFormId = i;
    }

}
