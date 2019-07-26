package gov.nih.nichd.ctdb.form.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Oct 18, 2005
 * Time: 1:39:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class CellFormatting extends CtdbDomainObject {
	private static final long serialVersionUID = -479538849464366905L;
	
	private int formId = Integer.MIN_VALUE;
    private int row = Integer.MIN_VALUE;
    private int col = Integer.MIN_VALUE;
    private int height = Integer.MIN_VALUE;
    private int width = Integer.MIN_VALUE;
    private int padding = Integer.MIN_VALUE;
    private String align;
    private String valign;
    private String bgColor = "white";
    private String wrap;
    private int rowSpan =Integer.MIN_VALUE;
    private int colSpan = Integer.MIN_VALUE;
 

    public String getHtmlStyleString () {
        String style = "";
        if (height != Integer.MIN_VALUE) {
            style += "height:" + height + "; ";
        }
        if (width != Integer.MIN_VALUE) {
            style += "width:" + width + "; ";
        }
        if (padding != Integer.MIN_VALUE) {
            style += "padding:" + padding + ";";
        }
        if (align != null && ! align.trim().equals("")) {
            style += "text-align:" + align +"; ";
        }
        if (valign != null && ! valign.trim().equals("")) {
            style += "vertical-align:" + valign +"; ";
        }
        if (bgColor != null && ! bgColor.trim().equals("")) {
            style += "background-color:" + bgColor +"; ";
        }
        if (wrap != null && ! wrap.trim().equals("")) {
            style += "word-wrap:" + wrap +"; ";
        }


        return style;
    }

    public String getHtmlRowSpan() {
        if (rowSpan < 2) {
            return "1";
        } else {
            return Integer.toString(rowSpan);
        }
    }

    public String getHtmlColSpan() {
        if (colSpan < 2) {
            return "1";
        } else {
            return Integer.toString(colSpan);
        }
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public String getValign() {
        return valign;
    }

    public void setValign(String valign) {
        this.valign = valign;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    public String getWrap() {
        return wrap;
    }

    public void setWrap(String wrap) {
        this.wrap = wrap;
    }

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }

    public int getColSpan() {
        return colSpan;
    }

    public void setColSpan(int colSpan) {
        this.colSpan = colSpan;
    }



	public  Document toXML() throws TransformationException {
        throw new UnsupportedOperationException("toXML() not implemented in CellFormatting.");
    }

}
