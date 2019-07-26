import gov.nih.tbi.repository.model.DataFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class DataFileUnmarshalTest
{

    /**
     * @param args
     * @throws JAXBException
     * @throws IOException
     */
    public static void main(String[] args) throws JAXBException, IOException
    {

        FileInputStream in = new FileInputStream(
                new File("C:\\Users\\fchen\\desktop\\test\\dataFile-1335381862914.xml"));
        ClassLoader cl = gov.nih.tbi.repository.model.ObjectFactory.class.getClassLoader();
        JAXBContext jc = JAXBContext.newInstance("gov.nih.tbi.repository.model", cl);
        Unmarshaller um = jc.createUnmarshaller();
        DataFile dataFile = (DataFile) um.unmarshal(in);
        in.close();
    }

}
