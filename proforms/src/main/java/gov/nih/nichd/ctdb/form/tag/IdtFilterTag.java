package gov.nih.nichd.ctdb.form.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * This tag contains only javascript so there's no much going on here.  It only
 * holds the javascript string.
 * 
 * @author jpark1
 *
 */
public class IdtFilterTag extends BodyTagSupport implements Cloneable {
	
	private String name;
	private String filterDefault;

	public int doEndTag() throws JspException {
		Object parent = this.getParent();

        boolean foundTableTag = false;

        while (!foundTableTag) {
            if (parent == null) {
                throw new JspException("Can not use column tag outside of a JSONTag. Invalid parent = null");
            }

            if (!(parent instanceof JSONTag)) {
                if (parent instanceof TagSupport)
                    parent = ((TagSupport) parent).getParent();
                else
                    throw new JspException("Can not use column tag outside of a JSONTag. Invalid parent = " + parent.getClass().getName());
            }
            else
                foundTableTag = true;
        }

        // Need to clone the IdtConfigTag before passing it to the TableTag as
        // the ColumnTags can be reused by some containers, and since we are
        // using the IdtConfigTag as basically containers of data, we need to
        // save the original values, and not the values that are being changed
        // as the tag is being reused...

        IdtFilterTag copy = this;
        try {
            copy = (IdtFilterTag) this.clone();
        }
        catch (CloneNotSupportedException e) {
        } // shouldn't happen...

        ((JSONTag30) parent).addFilterTag(copy);

        return super.doEndTag();
	}
	
	public String getFilter() {
		return this.getBodyContent().getString();
	}

	public String getDefault() {
		return filterDefault;
	}

	public void setDefault(String filterDefault) {
		this.filterDefault = filterDefault;
	}
}
