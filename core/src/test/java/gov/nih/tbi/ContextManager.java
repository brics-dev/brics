
package gov.nih.tbi;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ContextManager
{

    private static ContextManager _singleton;

    private ApplicationContext context;

    private ContextManager()
    {

        if (context == null)
        {
            context = new ClassPathXmlApplicationContext("context.xml");
        }
    }

    public static ContextManager getInstance()
    {

        if (_singleton == null)
        {
            _singleton = new ContextManager();
        }

        return _singleton;
    }

    public static Object getBean(String beanName)
    {

        return ContextManager.getInstance().context.getBean(beanName);
    }

}
