package gov.nih.tbi;

/**
 * Created by amakar on 2/27/2017.
 */

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.DecoratorMapper;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.mapper.AbstractDecoratorMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Properties;

public class SessionKeyDecoratorMapper extends AbstractDecoratorMapper {

    private String DEFAULT_BRICS_DECORATOR = "default-style";

    public void init(Config config, Properties properties, DecoratorMapper parent) throws InstantiationException
    {

        super.init(config, properties, parent);
        // To get a decorator property, call properties.getProperty(key) where key = param name on the sitemesh.xml file
    }

    /**
     * The getDecorator() is invoked every time the app looks for a suitable decorator. We extend
     * AbstractDecoratorMapper, which allows us to call super.getDecorator() to delegate the search to the parent
     * mappers, if our time condition is not met.
     */
    public Decorator getDecorator(HttpServletRequest request, Page page)
    {
        //String stylingId = ModulesConstants.getStylingID();

        HttpSession sess = request.getSession();
        String styleID = (String)sess.getAttribute(ModulesConstants.SESSION_STYLE_KEY);
        //String fooStr = (String)foo;

        if(styleID == null) {
            styleID = request.getParameter(ModulesConstants.SESSION_STYLE_KEY);
        }

        if(styleID == null) {
            styleID = ModulesConstants.getDefaultStylingKey();
        }


        Decorator custom = getNamedDecorator(request, styleID); // Get the decorator based on the hostname
        if (custom != null)
            return custom;

        return getNamedDecorator(request, DEFAULT_BRICS_DECORATOR);
    }
}
