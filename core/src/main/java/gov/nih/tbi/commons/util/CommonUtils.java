package gov.nih.tbi.commons.util;

import java.io.Serializable;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by amakar on 10/19/2016.
 */
public class CommonUtils {

    /**
     * Returns true if the string can be parsed into a double, false otherwise
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str)
    {

        try
        {
            double d = Double.parseDouble(str);
        }
        catch (NumberFormatException e)
        {
            return false;
        }

        return true;
    }

    public static String getEntityXML(Serializable entity) {

        Class entityClass = entity.getClass();

        if(!entityClass.isAnnotationPresent(XmlRootElement.class)) {
            return null;
        }

        String result = null;

        try {
            StringWriter writer = new StringWriter();

            JAXBContext jaxbContext = JAXBContext.newInstance(entityClass);

            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(entity, writer);

            result = writer.toString();

            return result;

        } catch(JAXBException jaxbEx) {
            return null;
        }



    }
}
