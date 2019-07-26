package gov.nih.nichd.ctdb.form.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

public class IdtConfigTag extends BodyTagSupport implements Cloneable {
	private static final long serialVersionUID = -5750317840052149232L;

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

        IdtConfigTag copy = this;
        try {
            copy = (IdtConfigTag) this.clone();
        }
        catch (CloneNotSupportedException e) {
        } // shouldn't happen...

        ((JSONTag30) parent).addConfigTag(copy);

        return super.doEndTag();
	}
	
	public String getConfig() {
		return this.getBodyContent().getString();
	}

}
