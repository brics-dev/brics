
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.dictionary.dao.hibernate.RepeatableGroupDaoImpl;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/**
 * Form object for capturing and setting form fields on the site
 * 
 * @author mvalei
 */
public class RepeatableGroupForm
{

    static Logger logger = Logger.getLogger(RepeatableGroupDaoImpl.class);

    /**********************************************************************/

    protected String name;
    protected RepeatableType type;
    protected Integer threshold;

    /**********************************************************************/

    public RepeatableGroupForm()
    {

    }

    /**
     * Creates a repeatableGroupFrom from the data fields in repeatableGroup object
     * 
     * @param repeatableGroup
     *            - repeatableGroup will contain data captured by struts
     */
    public RepeatableGroupForm(RepeatableGroup repeatableGroup)
    {

        Field[] fields = this.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++)
        {
            Field current = fields[i];

            if (!current.getName().equals("logger"))
            {
                try
                {
                    String getMethodName = "get" + current.getName().substring(0, 1).toUpperCase()
                            + current.getName().substring(1);

                    Method setMethod = repeatableGroup.getClass().getMethod(getMethodName);

                    Object value = setMethod.invoke(repeatableGroup);

                    current.set(this, value);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    logger.error("There was an exception caught in RepeatableGroupForm RepeatableGroupForm()" + e.toString());
                }
            }
        }
    }

    /**********************************************************************/

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public RepeatableType getType()
    {

        return type;
    }

    public void setType(String type)
    {

        if (RepeatableType.EXACTLY.getValue().equals(type))
        {
            this.type = RepeatableType.EXACTLY;
        }
        else
            if (RepeatableType.LESSTHAN.getValue().equals(type))
            {
                this.type = RepeatableType.LESSTHAN;
            }
            else
            {
                this.type = RepeatableType.MORETHAN;
            }
    }

    // public void setType(RepeatableType type)
    // {
    //
    // this.type = type;
    // }

    public Integer getThreshold()
    {

        return threshold;
    }

    public void setThreshold(Integer threshold)
    {

        this.threshold = threshold;
    }

    /**********************************************************************/

    /**
     * Reads data from the text fields on the page and writes to the dataStructure object
     * 
     * @param dataStructure
     * @param annotation
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */

    public void adapt(RepeatableGroup rg, Boolean enforceStaticFields)
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

                        Method setMethod = rg.getClass().getMethod(setMethodName, current.getType());

                        setMethod.invoke(rg, value);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    logger.error("There was an exception caught in RepeatableGroupForm adapt()" + e.toString());
                }
            }
        }
    }

}
