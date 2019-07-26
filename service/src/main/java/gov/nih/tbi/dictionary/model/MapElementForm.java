
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/**
 * This class masks the static fields of Data Element form.
 * 
 * @author Francis Chen
 * 
 */
public class MapElementForm
{

    static Logger logger = Logger.getLogger(MapElementForm.class);

    /*************************************************************/

    private Integer position;

    private String section;

    private RequiredType requiredType;

    // TODO: Use this for alias input
    // private String aliases;

    /*************************************************************************************/

    public MapElementForm()
    {

    }

    /**
     * Constructor fetches data for each column in dataElement object
     * 
     * @param dataElement
     */
    public MapElementForm(MapElement mapElement)
    {

        Field[] fields = this.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++)
        {
            Field current = fields[i];
            Object value = null;

            if (!current.getName().equals("logger"))
            {
                try
                {
                    String getMethodName = "get" + current.getName().substring(0, 1).toUpperCase()
                            + current.getName().substring(1);

                    Method setMethod = mapElement.getClass().getMethod(getMethodName);

                    try
                    {
                        value = setMethod.invoke(mapElement);
                    }
                    catch (InvocationTargetException ex)
                    {
                        if (ex.getCause() instanceof UnsupportedOperationException)
                        {
                            logger.error("Could not call method defined by setMethod.");
                        }
                        else
                        {
                            throw ex;
                        }
                    }

                    current.set(this, value);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    logger.error("There was an exception caught in MapElementFrom MapElementForm()" + e.toString());

                }
            }
        }
    }

    /*************************************************************/

    public Integer getPosition()
    {

        return position;
    }

    public void setPosition(Integer position)
    {

        this.position = position;
    }

    public String getSection()
    {

        return section;
    }

    public void setSection(String section)
    {

        this.section = section;
    }

    public RequiredType getRequiredType()
    {

        return requiredType;
    }

    public void setRequiredType(RequiredType requiredType)
    {

        this.requiredType = requiredType;
    }

    /*************************************************************/

    /**
     * Read the form fields on the page and set the columns in the dataElement column.
     * 
     * @param dataStructure
     * @param annotation
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void adapt(MapElement mapElement, Boolean enforceStaticFields)
    {

        Field[] fields = this.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++)
        {
            Field current = fields[i];

            if (!current.getName().equals("logger"))
            {
                try
                {
                    if (enforceStaticFields == false || current.getAnnotation(StaticField.class) == null)
                    {
                        Object value = current.get(this);

                        String setMethodName = "set" + current.getName().substring(0, 1).toUpperCase()
                                + current.getName().substring(1);

                        Method setMethod = mapElement.getClass().getMethod(setMethodName, current.getType());

                        try
                        {
                            setMethod.invoke(mapElement, value);
                        }
                        catch (InvocationTargetException ex)
                        {
                            if (ex.getCause() instanceof UnsupportedOperationException)
                            {
                                // logger.error("Cannot invoke method defined in setMethod.");
                            }
                            else
                            {
                                throw ex;
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    logger.error("There was an exception caught in MapElementFrom adapt()" + e.toString());
                }
            }
        }
    }
}
