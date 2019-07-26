package gov.nih.nichd.ctdb.common.tag;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * CtdbTag is a base Tag class for all Custom CTDB Tag Libraries. <code>getWebRoot</code> will
 * only work within a web application using Struts with ApplicationResources
 * properties defined in the configuration.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class CtdbTag extends TagSupport {
    protected ServletContext application;
    protected HttpServletRequest request;
    protected HttpSession session;
    protected HttpServletResponse response;

    /**
     * Default ConstructorRe
     */
    public CtdbTag() {
        super();
    }

    /**
     * doInitTag initializes the call to the Tag Library and
     * sets standard Tag variables.
     *
     * @throws JspException
     */
    public void doInitTag() throws JspException {
        application = pageContext.getServletContext();
        request = (HttpServletRequest) pageContext.getRequest();
        session = pageContext.getSession();
        response = (HttpServletResponse) pageContext.getResponse();
    }

    /**
     * Resets all Tag variables for this instance.
     */
    public void release() {
        application = null;
        request = null;
        session = null;
        response = null;
        super.release();
    }

    /**
     * Gets the application webroot from ApplicationResources
     *
     * @return Application Web Root (http://.....)
     */
    public String getWebRoot() throws JspException {
    	return SysPropUtil.getProperty("app.webroot");
    }
}