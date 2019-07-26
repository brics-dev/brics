
package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.service.rulesengine.RulesEngineUtils;
import gov.nih.tbi.dictionary.service.rulesengine.model.InvalidOperationException;
import gov.nih.tbi.dictionary.service.rulesengine.model.RulesEngineException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * @author mgree1
 * 
 */
public class RulesEngineTestAction extends BaseDictionaryAction
{

    /**
     * 
     */
    static Logger logger = Logger.getLogger(RulesEngineTestAction.class);

    private static final long serialVersionUID = 3776608008037137135L;

    public String dataDictionaryObj;
    public String original;
    public String incoming;
    public ArrayList<SeverityRecord> severityRecords;
    public ArrayList<String> readableSeverityRecords;
    public boolean completedTest;

    public String rulesEngineEvaluation()
    {

        if (original == null || incoming == null)
        {
            logger.error("Fatal Error: User failed to enter both an incoming and original DDO");
            addActionError("ERROR!!! Please specify an Original and Incoming Data Dictionary Object for Rules Engine Evaluation");
            completedTest = true;

            return PortalConstants.ACTION_INPUT;
        }
        readableSeverityRecords = new ArrayList<String>();
        try
        {
            if (dataDictionaryObj.contentEquals("Data Element"))
            {
                logger.info("User is testing Data Element. \n");
                evaluateDataElement(original, incoming);
            }
            else
                if (dataDictionaryObj.contentEquals("Form Structure"))
                {
                    logger.info("User is testing Form Structure. \n");
                    evaluateFormStructure(original, incoming);
                }
        }
        catch (InvalidOperationException e)
        {
            // TODO Auto-generated catch block
            addActionError(e.getMessage());
        }
        catch (RulesEngineException e)
        {
            // TODO Auto-generated catch block
            addActionError(e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            addActionError(e.getMessage());

        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            addActionError(e.getMessage());
        }
        catch (UserPermissionException e)
        {
            // TODO Auto-generated catch block
            addActionError(e.getMessage());
        }
        completedTest = true;

        return PortalConstants.ACTION_INPUT;
    }

    public void evaluateDataElement(String original, String incoming) throws InvalidOperationException,
            UnsupportedEncodingException, UserPermissionException
    {

        DataElement originalDataElement = getDataElement(original);
        if (originalDataElement.getSemanticObject() == null)
            logger.info("The Original Data Element did not load Semantic Object. \n");
        if (originalDataElement.getStructuralObject() == null)
            logger.info("The Original Data Element did not load Structural Object. \n");

        DataElement incomingDataElement = getDataElement(incoming);
        if (incomingDataElement.getSemanticObject() == null)
            logger.info("The Incoming Data Element did not load Semantic Object. \n");
        if (incomingDataElement.getStructuralObject() == null)
            logger.info("The Incoming Data Element did not load Structural Object. \n");
        // Find o

        severityRecords = null;
        		try{
        			severityRecords = (ArrayList<SeverityRecord>) dictionaryService.evaluateDataElementChangeSeverity(
        					originalDataElement, incomingDataElement);
        		}catch(Exception e){
        			logger.error("There was an exception. You failed. " + e.getMessage());
        		}
        if (severityRecords != null)
        {
            logger.info("The Rules Engine retuned a null list");
        }

        for (SeverityRecord sr : severityRecords)
        {
            readableSeverityRecords.add(RulesEngineUtils.generateSeverityRecordString(sr));
        }

    }

    public void evaluateFormStructure(String original, String incoming) throws InvalidOperationException,
            RulesEngineException, UnsupportedEncodingException, UserPermissionException
    {

        FormStructure originalFormStructure = getFormStructure(original);
        if (originalFormStructure.getFormStructureRDFObject() == null)
            logger.info("The Original Form Structure did not load Semantic Object. \n");
        if (originalFormStructure.getFormStructureRDFObject() == null)
            logger.info("The Original Form Structure did not load Structural Object. \n");
        FormStructure incomingFormStructure = getFormStructure(incoming);
        if (incomingFormStructure.getFormStructureSqlObject() == null)
            logger.info("The Incoming Form Structure did not load Semantic Object. \n");
        if (incomingFormStructure.getFormStructureSqlObject() == null)
            logger.info("The Incoming Form Structure did not load Structural Object. \n");

        severityRecords = (ArrayList<SeverityRecord>) dictionaryService.evaluateFormStructureChangeSeverity(
                originalFormStructure, incomingFormStructure);
        if (severityRecords != null)
        {
            logger.info("The Rules Engine retuned a null list");
        }
        for (SeverityRecord sr : severityRecords)
        {
            readableSeverityRecords.add(RulesEngineUtils.generateSeverityRecordString(sr));
        }

    }

    public String input()
    {

        completedTest = false;
        severityRecords = new ArrayList<SeverityRecord>();
        getActionErrors().clear();
        return PortalConstants.ACTION_INPUT;
    }

    public String reset()
    {

        completedTest = false;
        severityRecords = new ArrayList<SeverityRecord>();
        getActionErrors().clear();
        return PortalConstants.ACTION_INPUT;
    }

    /**
     * @return the dataDictionaryObj
     */
    public String getDataDictionaryObj()
    {

        return dataDictionaryObj;
    }

    /**
     * @param dataDictionaryObj
     *            the dataDictionaryObj to set
     */
    public void setDataDictionaryObj(String dataDictionaryObj)
    {

        this.dataDictionaryObj = dataDictionaryObj;
    }

    /**
     * @return the original
     */
    public String getOriginal()
    {

        return original;
    }

    /**
     * @param original
     *            the original to set
     */
    public void setOriginal(String original)
    {

        this.original = original;
    }

    /**
     * @return the incoming
     */
    public String getIncoming()
    {

        return incoming;
    }

    /**
     * @param incoming
     *            the incoming to set
     */
    public void setIncoming(String incoming)
    {

        this.incoming = incoming;
    }

    /**
     * @return the severity
     */
    public ArrayList<SeverityRecord> getSeverityRecords()
    {

        return severityRecords;
    }

    /**
     * @param severity
     *            the severity to set
     */
    public void setSeverity(ArrayList<SeverityRecord> severity)
    {

        this.severityRecords = severity;
    }

    /**
     * @return the completedTest
     */
    public boolean isCompletedTest()
    {

        return completedTest;
    }

    /**
     * @param completedTest
     *            the completedTest to set
     */
    public void setCompletedTest(boolean completedTest)
    {

        this.completedTest = completedTest;
    }

    private DataElement getDataElement(String ide) throws UnsupportedEncodingException, UserPermissionException
    {

        return dictionaryManager.getLatestDataElementByName(ide);
    }

    private FormStructure getFormStructure(String ide) throws UnsupportedEncodingException, UserPermissionException
    {

        return dictionaryManager.getDataStructureLatestVersion(ide);
    }

    public String organizeSeverityRecords(List<SeverityRecord> srs)
    {

        if (srs != null)
        {
            StringBuilder changeString = new StringBuilder();
            for (SeverityRecord sr : srs)
            {
                changeString.append(RulesEngineUtils.generateSeverityRecordString(sr) + "\n");

            }
            return changeString.toString();
        }
        return "The Rules Engine Returned a null list of Severity Records";
    }

    /**
     * @return the readableSeverityRecords
     */
    public ArrayList<String> getReadableSeverityRecords()
    {

        return readableSeverityRecords;
    }

    /**
     * @param readableSeverityRecords
     *            the readableSeverityRecords to set
     */
    public void setReadableSeverityRecords(ArrayList<String> readableSeverityRecords)
    {

        this.readableSeverityRecords = readableSeverityRecords;
    }

    /**
     * @param severityRecords
     *            the severityRecords to set
     */
    public void setSeverityRecords(ArrayList<SeverityRecord> severityRecords)
    {

        this.severityRecords = severityRecords;
    }

}
