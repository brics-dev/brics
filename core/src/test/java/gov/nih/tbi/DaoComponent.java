
package gov.nih.tbi;

import gov.nih.tbi.commons.dao.GenericDao;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;

public class DaoComponent
{

    static Logger logger = Logger.getLogger(DaoComponent.class);

    @SuppressWarnings("unchecked")
    public void getAll() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {

        Field[] fields = this.getClass().getDeclaredFields();

        PrintStream ps = new PrintStream(System.out);

        for (int i = 0; i < fields.length; i++)
        {
            try
            {
                if (fields[i].getName().contains("Dao"))
                {
                    Field currentField = fields[i];
                    System.out.println("\n\n" + currentField);

                    GenericDao dao = (GenericDao) currentField.get(this);

                    List output = dao.getAll();

                    for (Object obj : output)
                    {
                        // System.out.println(obj);
                        // print(obj, ps, "\t");

                        logger.debug("\t" + obj);
                        logger.debug("");
                        XmlExporter.writeXmlTree(obj, obj.getClass(), System.out);
                        // System.out.println();
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        ps.close();
    }

}
