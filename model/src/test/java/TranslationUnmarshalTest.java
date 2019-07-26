import gov.nih.tbi.dictionary.model.Translations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class TranslationUnmarshalTest
{

    /**
     * @param args
     * @throws JAXBException
     * @throws IOException
     */
    public static void main(String[] args) throws JAXBException, IOException
    {

        FileInputStream in = new FileInputStream(new File("C:\\Users\\fchen\\desktop\\sample.xml"));
        ClassLoader cl = gov.nih.tbi.dictionary.model.TranslationObjectFactory.class.getClassLoader();
        JAXBContext jc = JAXBContext.newInstance(Translations.class);
        Unmarshaller um = jc.createUnmarshaller();
        Translations translationRule = (Translations) um.unmarshal(in);
        in.close();
    }

}
