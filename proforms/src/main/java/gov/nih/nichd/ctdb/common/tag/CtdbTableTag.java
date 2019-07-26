package gov.nih.nichd.ctdb.common.tag;

import org.apache.taglibs.display.TableTag;

import javax.servlet.jsp.JspException;
import javax.servlet.http.HttpServletRequest;


/**
 * This tag extends TableTag class from the Display Tag Library and includes
 * customization for CTDB
 *
 * @author Booz Allen Hamilton
 * @version 1.1
 */

public class CtdbTableTag extends TableTag {
    /**
     * doStartTag resets the pageNumber to 1 when no page parameter is contained in the request.
     *
     * @throws JspException
     */
    public int doStartTag() throws JspException {

        HttpServletRequest req = (HttpServletRequest) this.pageContext.getRequest();

        if (req.getParameter("page") == null) {
            this.reset();
        }

        return super.doStartTag();
    }
}

