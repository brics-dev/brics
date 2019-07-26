package gov.nih.tbi.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

public class JSONColumnTag extends BodyTagSupport implements Cloneable {
    private String property;
    private String title;
    private String nulls;
    private String sort;
    private String autolink;
    private String group; /* If this property is set then the values have to be grouped */

    private String href;
    private String target;
    private String paramId;
    private String paramName;
    private String paramProperty;
    private String paramScope;
    private int maxLength;
    private int maxWords;
    private int titleColSpan = 1;

    private String width;
    private String align;
    private String background;
    private String bgcolor;
    private String height;
    private String nowrap;
    private String valign;
    private String styleClass;
    private String headerStyleClass;

    private String value;

    private String doubleQuote;

    private String decorator;
    
    private boolean visible = true;

    // -------------------------------------------------------- Accessor methods

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNulls() {
        return nulls;
    }

    public void setNulls(String nulls) {
        this.nulls = nulls;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getAutolink() {
        return autolink;
    }

    public void setAutolink(String autolink) {
        this.autolink = autolink;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamProperty() {
        return paramProperty;
    }

    public void setParamProperty(String paramProperty) {
        this.paramProperty = paramProperty;
    }

    public String getParamScope() {
        return paramScope;
    }

    public void setParamScope(String paramScope) {
        this.paramScope = paramScope;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxWords() {
        return maxWords;
    }

    public void setMaxWords(int maxWords) {
        this.maxWords = maxWords;
    }

    public int getTitleColSpan() {
        return titleColSpan;
    }

    public void setTitleColSpan(int titleColSpan) {
        this.titleColSpan = titleColSpan;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getBgcolor() {
        return bgcolor;
    }

    public void setBgcolor(String bgcolor) {
        this.bgcolor = bgcolor;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getNowrap() {
        return nowrap;
    }

    public void setNowrap(String nowrap) {
        this.nowrap = nowrap;
    }

    public String getValign() {
        return valign;
    }

    public void setValign(String valign) {
        this.valign = valign;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getHeaderStyleClass() {
        return headerStyleClass;
    }

    public void setHeaderStyleClass(String headerStyleClass) {
        this.headerStyleClass = headerStyleClass;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDoubleQuote() {
        return doubleQuote;
    }

    public void setDoubleQuote(String doubleQuote) {
        this.doubleQuote = doubleQuote;
    }

    public String getDecorator() {
        return decorator;
    }

    public void setDecorator(String decorator) {
        this.decorator = decorator;
    }

    // --------------------------------------------------------- Tag API methods

    /**
     * Passes attribute information up to the parent TableTag.
     *
     * <p>When we hit the end of the tag, we simply let our parent (which better
     * be a TableTag) know what the user wants to do with this column.
     * We do that by simple registering this tag with the parent.  This tag's
     * only job is to hold the configuration information to describe this
     * particular column.  The TableTag does all the work.</p>
     *
     * @throws JspException if this tag is being used outside of a
     *    &lt;display:list...&gt; tag.
     */
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

        // Need to clone the ColumnTag before passing it to the TableTag as
        // the ColumnTags can be reused by some containers, and since we are
        // using the ColumnTags as basically containers of data, we need to
        // save the original values, and not the values that are being changed
        // as the tag is being reused...

        JSONColumnTag copy = this;
        try {
            copy = (JSONColumnTag) this.clone();
        }
        catch (CloneNotSupportedException e) {
        } // shouldn't happen...

        ((JSONTag) parent).addColumn(copy);

        return super.doEndTag();
    }


    /**
     * Takes all the column pass-through arguments and bundles them up as a
     * string that gets tacked on to the end of the td tag declaration.
     */
    protected String getCellAttributes() {
        StringBuffer results = new StringBuffer();

        if (this.styleClass != null) {
            results.append(" class=\"");
            results.append(this.styleClass);
            results.append("\"");
        }
        else {
            results.append(" class=\"tableCell\"");
        }

        if (this.width != null) {
            results.append(" width=\"");
            results.append(this.width);
            results.append("\"");
        }

        if (this.align != null) {
            results.append(" align=\"");
            results.append(this.align);
            results.append("\"");
        }
        else {
            results.append(" align=\"left\"");
        }

        if (this.background != null) {
            results.append(" background=\"");
            results.append(this.background);
            results.append("\"");
        }

        if (this.bgcolor != null) {
            results.append(" bgcolor=\"");
            results.append(this.bgcolor);
            results.append("\"");
        }

        if (this.height != null) {
            results.append(" height=\"");
            results.append(this.height);
            results.append("\"");
        }

        if (this.nowrap != null) {
            results.append(" nowrap");
        }

        if (this.valign != null) {
            results.append(" valign=\"");
            results.append(this.valign);
            results.append("\"");
        }
        else {
            results.append(" valign=\"top\"");
        }

        return results.toString();
    }

    public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
     * Returns a String representation of this Tag that is suitable for
     * printing while debugging.  The format of the string is subject to change
     * but it currently:
     *
     * <p><code>SmartColumnTag([title],[property],[href])</code></p>
     *
     * <p>Where the placeholders in brackets are replaced with their appropriate
     * instance variables.</p>
     */
    public String toString() {
        return "SmartColumnTag(" + title + "," + property + "," + href + ")";
    }
}
