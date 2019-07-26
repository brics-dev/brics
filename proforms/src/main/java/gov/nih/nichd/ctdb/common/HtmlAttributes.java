package gov.nih.nichd.ctdb.common;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * HtmlAttributes implements different type of HTML attributes
 * used when displaying data to the user. The attributes
 * format alignment, fonts, and coloring.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class HtmlAttributes extends CtdbDomainObject {
	private static final long serialVersionUID = -8876866442534007866L;
	
	private String align = "left";
    private String vAlign = "top";
    private String color = "black";
    private String fontFace = "arial";
    private String fontSize;
    private int indent = 0;
    private boolean border = false;

    /**
     * Default Constructor for the HtmlAttributes Object
     */
    public HtmlAttributes() {
        // default constructor
    }

    /**
     * Gets the HTML attribute align
     *
     * @return The align attribute
     */
    public String getAlign() {
        return align;
    }

    /**
     * Sets the HTML attribute align
     *
     * @param align The align value
     */
    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * Gets the HTML attribute valign
     *
     * @return The valign attribute
     */
    public String getvAlign() {
        return vAlign;
    }

    /**
     * Sets the HTML attribute valign
     *
     * @param vAlign The valign value
     */
    public void setvAlign(String vAlign) {
        this.vAlign = vAlign;
    }

    /**
     * Gets the HTML attribute color
     *
     * @return The color attribute
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the HTML attribute color
     *
     * @param color The color value
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Gets the HTML attribute font face
     *
     * @return The font face attribute
     */
    public String getFontFace() {
        return fontFace;
    }

    /**
     * Sets the HTML attribute font face
     *
     * @param fontFace The font face value
     */
    public void setFontFace(String fontFace) {
        this.fontFace = fontFace;
    }

    /**
     * Gets the HTML attribute font size
     *
     * @return The font size value
     */
    public String getFontSize() {
        return fontSize;
    }

    /**
     * Sets the HTML attribute font size
     *
     * @param fontSize The font size value
     */
    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Gets the HTML attribute indent
     *
     * @return The indent attribute
     */
    public int getIndent() {
        return indent;
    }

    /**
     * Sets the HTML attribute indent
     *
     * @param indent The indent value
     */
    public void setIndent(int indent) {
        this.indent = indent;
    }

    /**
     * Gets the HTML attribute border flag
     *
     * @return True if the element should have a border, false otherwise.
     */
    public boolean getBorder() {
        return border;
    }

    /**
     * Sets the HTML attribute border flag. True if the element
     * must have a border, false otherwise.
     *
     * @param border The border flag
     */
    public void setBorder(boolean border) {
        this.border = border;
    }

    /**
     * This method allows the transformation of a Html Attributes into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException       is thrown if there is an
     *                                       error during the XML tranformation
     * @throws UnsupportedOperationException is thrown if this method
     *                                       is currently unsupported and not implemented.
     */
    public Document toXML() throws TransformationException {
        Document document = super.newDocument();
        Element root = super.initXML(document, "htmlAttributes");

        Element alignNode = document.createElement("align");
        alignNode.appendChild(document.createTextNode(this.align));
        root.appendChild(alignNode);

        Element vAlignNode = document.createElement("valign");
        vAlignNode.appendChild(document.createTextNode(this.vAlign));
        root.appendChild(vAlignNode);

        Element colorNode = document.createElement("color");
        colorNode.appendChild(document.createTextNode(this.color));
        root.appendChild(colorNode);

        Element fontFaceNode = document.createElement("fontFace");
        fontFaceNode.appendChild(document.createTextNode(this.fontFace));
        root.appendChild(fontFaceNode);

        if (this.fontSize != null) {
            Element fontSizeNode = document.createElement("fontSize");
            if(this.fontSize.equals("0")){
            	fontSizeNode.appendChild(document.createTextNode("2"));
            }else{
            	fontSizeNode.appendChild(document.createTextNode(this.fontSize));
            }
            root.appendChild(fontSizeNode);
        }

        Element indentNode = document.createElement("indent");
        indentNode.appendChild(document.createTextNode(Integer.toString(this.indent)));
        root.appendChild(indentNode);

        Element borderNode = document.createElement("border");
        if (this.border) {
            borderNode.appendChild(document.createTextNode("1"));
        } else {
            borderNode.appendChild(document.createTextNode("0"));
        }
        root.appendChild(borderNode);

        return document;
    }
}

