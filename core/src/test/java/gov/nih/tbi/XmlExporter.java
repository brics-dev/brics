
package gov.nih.tbi;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class XmlExporter
{

    public static void writeXmlTree(Object obj, Class<?> clazz, java.io.OutputStream outStream)
    {

        try
        {
            JAXBContext jaxContext = JAXBContext.newInstance(clazz);
            Marshaller marshaller = jaxContext.createMarshaller();

            marshaller.marshal(obj, outStream);
        }
        catch (Exception ex)
        {
            System.out.println("==== CANT MAKE XML DOCUMENT ");

            // ex.printStackTrace();

        }
    }

}
