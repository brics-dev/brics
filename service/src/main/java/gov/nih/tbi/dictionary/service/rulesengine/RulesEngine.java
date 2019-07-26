
package gov.nih.tbi.dictionary.service.rulesengine;

import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.ExternalId;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.service.rulesengine.model.Field;
import gov.nih.tbi.dictionary.service.rulesengine.model.FieldList;
import gov.nih.tbi.dictionary.service.rulesengine.model.InvalidOperationException;
import gov.nih.tbi.dictionary.service.rulesengine.model.ObjectFactory;
import gov.nih.tbi.dictionary.service.rulesengine.model.Rule;
import gov.nih.tbi.dictionary.service.rulesengine.model.RulesEngineException;
import gov.nih.tbi.dictionary.service.rulesengine.model.RulesEngineRules;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * The Rules Engine class represents a black box system that compares the changes between two Data Elements or two Form
 * Structure and determine the severity of the changes.
 * 
 * @author mgree1
 * 
 */

@Service
@Scope("singleton")
public class RulesEngine implements RulesEngineInterface
{

    // Field that utilize collections
    public final static String VALUE_RANGE_LIST = "valuerangelist";
    public final static String CLASSIFICATION_ELEMENT_LIST = "classificationelementlist";
    public final static String KEYWORD_LIST = "keywordlist";
    public final static String ALIAS_LIST = "aliaslist";
    public final static String EXTERNAL_ID_SET = "externalidset";

    public final static String KEY = "Key: ";
    public final static String VALUE = "Value: ";

    /* Data Dictionary Object Strings*/
    public final static String FORM_STRUCTURE = "Form Structure";
    public final static String DATA_ELEMENT = "Data Element";

    RulesEngineRules rulesEngineRules;

    ApplicationContext ctx;

    private Resource resource;

    /***
     * /** Instantiates the Rules Engine along with the fields of both Data Elements and Form Structures.
     * 
     * @throws JAXBException
     * @throws IOException
     * 
     * @throws RulesEngineException
     */
    @SuppressWarnings("unchecked")
    public RulesEngine() throws JAXBException, IOException
    {

        /*     ctx = new ClassPathXmlApplicationContext(new String[] { "rulesEngineContext.xml" }, RulesEngine.class);

             // ctx = new FileSystemXmlApplicationContext("src/test/resources/context.xml");

             // File xmlFile = (File) ctx.getBean("fileBean");

             resource = ctx.getResource("file:///c:/brics/RulesEngine.xml");*/

        InputStream rulesEngineXml = getClass().getResourceAsStream("/RulesEngine.xml");

        // ctx = new FileSystemXmlApplicationContext("service/rulesengine/rulesEngineContext.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<RulesEngineRules> unmarshalledObject = (JAXBElement<RulesEngineRules>) unmarshaller
                .unmarshal(rulesEngineXml);

        if (unmarshalledObject != null)
        {
            rulesEngineRules = unmarshalledObject.getValue();
        }
    }

    /**
     * Responsible for returning a collection containing a list of the changes and their respective changes. Checks if
     * the two Data Elements arguments are not null and calls helper methods which determine the changes and their
     * severity. Single entry point into the Rules engine for the Data Elements.
     * 
     * @param originalDataElement
     * @param incomingDataElement
     * @return
     * @throws RulesEngineException
     * @throws InvalidOperationException
     * @throws NoSuchMethodException
     */
    public List<SeverityRecord> evaluateDataElementChangeSeverity(DataElement originalDataElement,
            DataElement incomingDataElement) throws InvalidOperationException, RulesEngineException
    {

        List<SeverityRecord> dataElementChanges = null;

        // If either of the data elements are null, then the a null list will be returned

        if (originalDataElement == null || incomingDataElement == null)
        {
            // return an illegalArgument exception
            throw new IllegalArgumentException();
        }

        try
        { // If the Original Data Element is in draft or AP, it does not enter the Rules Engine.
            if (originalDataElement.getStatus().compareTo(DataElementStatus.DRAFT) == 0
                    || originalDataElement.getStatus().compareTo(DataElementStatus.AWAITING) == 0)
            {
                dataElementChanges = new ArrayList<SeverityRecord>();
            }
            else
            {
                dataElementChanges = determineChangesBetweenDataElements(originalDataElement, incomingDataElement);
            }

        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
            throw new RulesEngineException(RulesEngineConstants.INVALID_LIST_OBJECT_KEY);
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            throw new RulesEngineException(e);

        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            throw new RulesEngineException(e);

        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
            throw new RulesEngineException(e);

        }

        return dataElementChanges;
    }

    @Override
    /**
     * Submits two Data Element for comparison, then returns the highest severity level from the changes made. Will return null if no changes were made.
     */
    public SeverityLevel highestSeverityLevel(DataElement originalDataElement, DataElement incomingDataElement)
            throws InvalidOperationException, RulesEngineException
    {

        List<SeverityRecord> changesMade = null;
        changesMade = evaluateDataElementChangeSeverity(originalDataElement, incomingDataElement);
        SeverityLevel highest = null;
        if (changesMade != null)
        {
            if (!changesMade.isEmpty())
            {
                for (SeverityRecord sr : changesMade)
                {
                    if (highest == null)
                        highest = sr.getSeverityLevel();
                    else
                    {
                        if (highest.compareTo(sr.getSeverityLevel()) == 1)
                        {
                            highest = sr.getSeverityLevel();
                        }
                    }
                }
            }
        }
        return highest;
    }

    /**
     * Takes the two Data Elements arguments and compares them to one another. All attributes would be compared, and if
     * there is no changes between the two Data Elements, then a empty list would be returned. If changes do exist, a
     * severityRecord is created and added to the list.
     * 
     * @param originalDataElement
     * @param incomingDataElement
     * @return
     * @throws NoSuchMethodException
     * @throws RulesEngineException
     * @throws InvalidOperationException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * 
     * @see SeverityRecord
     */
    private List<SeverityRecord> determineChangesBetweenDataElements(DataElement originalDataElement,
            DataElement incomingDataElement) throws InvalidOperationException, RulesEngineException,
            NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {

        List<SeverityRecord> dataElementChanges = new ArrayList<SeverityRecord>();
        RulesEngineOperationHandler handler = new RulesEngineOperationHandler();

        for (Field dataElementField : rulesEngineRules.getDataElementRules().getField())
        {

            Object originalDEFieldValue = handler.retrieveFieldObjectForComparison(originalDataElement,
                    dataElementField.getName());
            Object incomingDEFieldValue = handler.retrieveFieldObjectForComparison(incomingDataElement,
                    dataElementField.getName());
            List<SeverityRecord> changeHasBeenMade = null;
            StringBuilder dataElements = new StringBuilder();
            dataElements.append(RulesEngineUtils.DATA_ELEMENT + ": ");
            if (originalDEFieldValue != null || incomingDEFieldValue != null)
            {
                changeHasBeenMade = compareTo(originalDEFieldValue, incomingDEFieldValue, dataElementField,
                        new StringBuilder(), false, DATA_ELEMENT);
                if (changeHasBeenMade != null && !changeHasBeenMade.isEmpty())
                {
                    dataElementChanges.addAll(changeHasBeenMade);

                }
            }

            // Assuming no change has be made

        }

        return dataElementChanges;
    }

    /**
     * Responsible for returning a collection containing a list of the changes and their respective changes. Checks if
     * the two Form Structures arguments are not null and calls helper methods which determine the changes and their
     * severity. Single entry point into the Rules engine for the Form Structure.
     * 
     * @param originalFormStructure
     * @param incomingFormStructure
     * @return
     * @throws NoSuchMethodException
     * @throws RulesEngineException
     * @throws InvalidOperationException
     */
    public List<SeverityRecord> evaluateFormStructureChangeSeverity(FormStructure originalFormStructure,
            FormStructure incomingFormStructure) throws InvalidOperationException, RulesEngineException
    {

        List<SeverityRecord> formStructureChanges = null;

        // If either of the data elements are null, then the a null list will be returned
        if (originalFormStructure == null || incomingFormStructure == null)
        {
            throw new IllegalArgumentException();
        }

        try
        {
            if (originalFormStructure.getStatus().compareTo(StatusType.PUBLISHED) == 0)
            {
                formStructureChanges = determineChangesBetweenFormStructures(originalFormStructure,
                        incomingFormStructure);
            }
            else
            {
                formStructureChanges = new ArrayList<SeverityRecord>();
            }
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
            throw new RulesEngineException(RulesEngineConstants.INVALID_LIST_OBJECT_KEY);
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            throw new RulesEngineException(e);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            throw new RulesEngineException(e);

        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
            throw new RulesEngineException(e);

        }

        return formStructureChanges;
    }

    /**
     * Submits two Form Structures for comparison, then returns the highest severity level from the changes made. Will
     * return null if no changes were made.
     * 
     * @throws NoSuchMethodException
     * @throws RulesEngineException
     * @throws InvalidOperationException
     */
    @Override
    public SeverityLevel highestSeverityLevel(FormStructure originalFormStructure, FormStructure incomingFormStructure)
            throws InvalidOperationException, RulesEngineException, NoSuchMethodException
    {

        List<SeverityRecord> changesMade = evaluateFormStructureChangeSeverity(originalFormStructure,
                incomingFormStructure);
        SeverityLevel highest = null;
        if (changesMade != null)
        {
            if (!changesMade.isEmpty())
            {
                for (SeverityRecord sr : changesMade)
                {
                    if (highest == null)
                        highest = sr.getSeverityLevel();
                    else
                    {
                        if (highest.compareTo(sr.getSeverityLevel()) == 1)
                        {
                            highest = sr.getSeverityLevel();
                        }
                    }
                }
            }
        }
        return highest;
    }

    /**
     * Takes the two Form Structures arguments and compares them to one another. All attributes would be compared, and
     * if there is no changes between the two Form Structures, then null would be returned. If changes do exist, a
     * severityRecord is created and added to the list.
     * 
     * @param originalFormStructure
     * @param incomingFormStructure
     * @return
     * @throws NoSuchMethodException
     * @throws RulesEngineException
     * @throws InvalidOperationException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * 
     * @see SeverityRecord
     */
    private List<SeverityRecord> determineChangesBetweenFormStructures(FormStructure originalFormStructure,
            FormStructure incomingFormStructure) throws InvalidOperationException, RulesEngineException,
            NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {

        List<SeverityRecord> formStructureChanges = new ArrayList<SeverityRecord>();
        RulesEngineOperationHandler handler = new RulesEngineOperationHandler();
        for (Field formStructureField : rulesEngineRules.getFormStructureRules().getField())
        {
            if (formStructureField != null)
            {

                Object originalFSFieldValue = handler.retrieveFieldObjectForComparison(originalFormStructure,
                        formStructureField.getName());
                Object incomingFSFieldValue = handler.retrieveFieldObjectForComparison(incomingFormStructure,
                        formStructureField.getName());
                // Assuming no change has be made

                List<SeverityRecord> changeHasBeenMade = null;

                if (originalFSFieldValue != null || incomingFSFieldValue != null)
                {
                    changeHasBeenMade = compareTo(originalFSFieldValue, incomingFSFieldValue, formStructureField,
                            new StringBuilder(), false, FORM_STRUCTURE);

                    if (changeHasBeenMade != null && !changeHasBeenMade.isEmpty())
                    {
                        formStructureChanges.addAll(changeHasBeenMade);
                    }
                }
            }

        }
        return formStructureChanges;
    }

    /**
     * The compareTo function is responsible for calling the RulesEngineOperation. The compareTo function for fields
     * that contained Set<?>. Using the field name, the Object is determined and the Set<Object> is cast. Calls Object
     * specific CompareTo which returns true or false.
     * 
     * @param originalValue
     * @param incomingChange
     * @param fieldName
     * @return
     * @throws RulesEngineException
     * @throws InvalidOperationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private List<SeverityRecord> compareTo(Object originalValue, Object incomingChange, Field field,
            StringBuilder fieldLocation, Boolean isASubField, String dataDictionaryObject)
            throws InvalidOperationException, RulesEngineException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException
    {

        List<SeverityRecord> srList = new ArrayList<SeverityRecord>();

        RulesEngineOperationHandler handler = new RulesEngineOperationHandler();
        // Unless the field passed is a list this should be null;
        FieldList fieldList = field.getFieldList();
        List<Field> subFields = field.getSubField();
        List<Rule> rules = field.getRule();

        formatServerityRecordField(field, fieldLocation, isASubField);
        // Field list is being used to see if the object passed in is a list or not.
        if (fieldList != null)
        {
            if (!subFields.isEmpty() || !rules.isEmpty())
            {
                throw new RulesEngineException(RulesEngineConstants.INVALID_SUBFIELD_RULES);
            }
            List<SeverityRecord> srFieldList = compareToList((Collection<?>) originalValue,
                    (Collection<?>) incomingChange, fieldList, field, fieldLocation, dataDictionaryObject);
            if (!srFieldList.isEmpty())
                srList.addAll(srFieldList);
        }
        else
        {
            for (Field subField : subFields)
            {

                Object originalSubFieldValue = handler.retrieveFieldObjectForComparison(originalValue,
                        subField.getName());
                Object incomingSubFieldValue = handler.retrieveFieldObjectForComparison(incomingChange,
                        subField.getName());
                fieldLocation.append(RulesEngineUtils.FIELD_HAS_A_SUBFIELD);
                List<SeverityRecord> srFieldList = compareTo(originalSubFieldValue, incomingSubFieldValue, subField,
                        fieldLocation, true, dataDictionaryObject);

                if (!srFieldList.isEmpty())
                    srList.addAll(srFieldList);

            }

            if (rules != null)
            {
                for (Rule rule : field.getRule())
                {
                    SeverityRecord ruleSR = handler.evaluate(originalValue, incomingChange, fieldLocation, rule,
                            fieldList, dataDictionaryObject);

                    if (ruleSR != null)
                        srList.add(ruleSR);
                }
            }
        }
        return srList;

    }

    /**
     * @param field
     * @param fieldLocation
     */
    private void formatServerityRecordField(Field field, StringBuilder fieldLocation, boolean isASubField)
    {

        if (field.getSubField().isEmpty() && field.getFieldList() == null)
        {
            if (isASubField)
                fieldLocation.append(RulesEngineUtils.SUB_CASE);
            else
                fieldLocation.append(RulesEngineUtils.BASE_CASE);
        }
        else
            if (field.getSubField().isEmpty() && field.getFieldList() != null)
            {
                fieldLocation.append(RulesEngineUtils.LIST_CASE);
            }

        String fieldDisplayName = field.getDisplayName();
        if (fieldDisplayName != null)
            fieldLocation.append(fieldDisplayName);
        else
            fieldLocation.append(field.getName());

    }

    private List<SeverityRecord> compareToList(Collection<?> original, Collection<?> incoming, FieldList fieldList,
            Field field, StringBuilder fieldLocation, String dataDictionaryObject) throws RulesEngineException,
            InvalidOperationException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {

        RulesEngineOperationHandler handler = new RulesEngineOperationHandler();
        List<SeverityRecord> srList = new ArrayList<SeverityRecord>();
        List<Rule> rules = fieldList.getRule();

        if (rules != null && !rules.isEmpty())
        {
            for (Rule rule : rules)
            {

                SeverityRecord ruleSR = handler.evaluate(original, incoming, fieldLocation, rule, fieldList,
                        dataDictionaryObject);
                if (ruleSR != null)
                    srList.add(ruleSR);
            }
        }
        for (Object org : original)
        {
            Boolean found = false;
            Iterator<Object> it = (Iterator<Object>) incoming.iterator();
            while (!found && it.hasNext())
            {
                Object inc = it.next();
                Object orgValue = handler.retrieveFieldObjectForComparison(org, fieldList.getKey());
                Object incValue = handler.retrieveFieldObjectForComparison(inc, fieldList.getKey());

				// Looking for a match based on the value key passed in
				if (orgValue != null || incValue != null) {
					if (handler.visitCompare(orgValue, incValue)) {
						List<Field> subFieldList = fieldList.getSubField();
						if (subFieldList != null && !subFieldList.isEmpty()) {
							for (Field subField : fieldList.getSubField()) {
								Object orgSubfieldValue =
										handler.retrieveFieldObjectForComparison(org, subField.getName());
								Object incSubfieldValue =
										handler.retrieveFieldObjectForComparison(inc, subField.getName());

								if (orgSubfieldValue != null || incSubfieldValue != null) {
									StringBuilder subFieldSB = new StringBuilder();
									subFieldSB.append(fieldLocation.toString());
									// Typically the ID Field would be used in the Field Name string, but it doesnt make
									// sense to do that for Repeatable Groups cause the name is more identifiable.
									listFieldLocationStringBuilder(org, orgValue, subFieldSB);
									List<SeverityRecord> srFieldList =
											compareTo(orgSubfieldValue, incSubfieldValue, subField, subFieldSB, true,
													dataDictionaryObject);
									if (!srFieldList.isEmpty())
										srList.addAll(srFieldList);
								}
							}
						}
					}
				}
            }
        }
        return srList;

    }

    /**
     * @param org
     * @param orgValue
     * @param subFieldSB
     */
    private void listFieldLocationStringBuilder(Object org, Object orgValue, StringBuilder subFieldSB)
    {

        String objectClassName = org.getClass().getName();
        if (objectClassName.equals(RepeatableGroup.class.getName()))
        {
            RepeatableGroup rg = (RepeatableGroup) org;
            subFieldSB.append(" | " + RulesEngineUtils.LIST_ITEM_CASE + retrieveObjectClassName(org) + " "
                    + RulesEngineUtils.ID + " " + rg.getName() + " | ");
        }
        else
            if (objectClassName.equals(ExternalId.class.getName()))
            {
                ExternalId externalID = (ExternalId) org;
                subFieldSB.append(" | " + RulesEngineUtils.LIST_ITEM_CASE + retrieveObjectClassName(org) + " "
                        + RulesEngineUtils.EXTERNAL_ID + " " + externalID.getSchema().getName() + " | ");
            }
            else
            {

                subFieldSB.append(" | " + RulesEngineUtils.LIST_ITEM_CASE + retrieveObjectClassName(org) + " "
                        + RulesEngineUtils.ID + " " + orgValue.toString() + " | ");
            }
    }

    /**
     * @param org
     * @return
     */
    private String retrieveObjectClassName(Object org)
    {

        String objectName = org.getClass().getName();
        objectName = objectName.substring(objectName.lastIndexOf('.') + 1);
        return objectName;
    }

    public void setResource(Resource resource)
    {

        this.resource = resource;
    }

    /**
     * @return the ctx
     */
    public ApplicationContext getCtx()
    {

        return ctx;
    }

    /**
     * @param ctx
     *            the ctx to set
     */
    public void setCtx(ApplicationContext ctx)
    {

        this.ctx = ctx;
    }
}
