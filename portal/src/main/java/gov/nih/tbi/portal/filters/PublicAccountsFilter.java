package gov.nih.tbi.portal.filters;


import gov.nih.tbi.ModulesConstants;

import javax.servlet.ServletRequest;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.GenericFilterBean;


/**
 * The purpose of this filter is to inject "style.key" parameter in the session
 * with the value from modules.properties so that our Sitemesh decorator mapper
 * (SessionKeyDecoratorMapper) can do style rendering based on it.
 * Normally, this injection is done via the CAS filter but this is for a URL pattern
 * which is open to public so it needs its own session parameter injection.
 *
 * Created by amakar on 5/3/2017.
 */


public class PublicAccountsFilter extends GenericFilterBean {

    @Autowired
    ModulesConstants modulesConstants;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        //the reason we don't need the disease ID is because this is only in portal
        //so it is safe to take the singular value from modules.properties
        String styleID = this.modulesConstants.getStylingKey(null);

        HttpServletRequest httpRequest = (HttpServletRequest)request;

        httpRequest.getSession().setAttribute(ModulesConstants.SESSION_STYLE_KEY, styleID);
        chain.doFilter(request, response);
    }
}
