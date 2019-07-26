
package gov.nih.tbi;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.DecoratorMapper;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.mapper.AbstractDecoratorMapper;

public class URLDecoratorMapper extends AbstractDecoratorMapper
{

    private String DEFAULT_BRICS_DECORATOR = "default";
    private String HOSTAME_HEADER_PARAMETER = "host";

    /**
     * The init() method is given the sitemesh.xml parameters as "properties".
     */
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

        String hostname = request.getHeader(HOSTAME_HEADER_PARAMETER);
        if (hostname == null)
            return getNamedDecorator(request, DEFAULT_BRICS_DECORATOR); // If something went wrong, use the default
                                                                        // BRICS

        Decorator custom = getNamedDecorator(request, hostname); // Get the decorator based on the hostname
        if (custom != null)
            return custom;

        return getNamedDecorator(request, DEFAULT_BRICS_DECORATOR);
    }

}
