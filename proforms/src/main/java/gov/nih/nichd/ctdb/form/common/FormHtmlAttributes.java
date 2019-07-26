package gov.nih.nichd.ctdb.form.common;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.HtmlAttributes;
import gov.nih.nichd.ctdb.common.TransformationException;


/**
 * FormAttributes implements different type of HTML attributes used when displaying
 * a form. The attributes include format, alignment, fonts, and coloring.
 *
 * @author  Booz Allen Hamilton
 * @version 1.0
 */

public class FormHtmlAttributes extends HtmlAttributes
{
    private boolean formBorder = true;
    private boolean sectionBorder = true;
    private String formColor = "Black";
    private String sectionColor = "Black";
    private String formFont = "Arial";
    private String sectionFont = "Arial";
    private int formFontSize = 10;
    private int cellpadding = 2;


    /**
     * Default Constructor for the FormAttributes Object
     */
    public FormHtmlAttributes()
    {
        // default constructor
    }

    /**
     * Gets the form border.
     *
     * @return True if a form border is needed, false otherwise
     */
    public boolean getFormBorder()
    {
        return formBorder;
    }

    /**
     * Sets the form border
     *
     * @param formBorder  The form border decision
     */
    public void setFormBorder(boolean formBorder)
    {
        this.formBorder = formBorder;
    }

    /**
     * Gets the section border.
     *
     * @return True if the section border is needed, false otherwise
     */
    public boolean getSectionBorder()
    {
        return sectionBorder;
    }

    /**
     * Sets the section border
     *
     * @param sectionBorder  The section border decision
     */
    public void setSectionBorder(boolean sectionBorder)
    {
        this.sectionBorder = sectionBorder;
    }

    /**
     * Gets the form color
     *
     * @return The form color
     */
    public String getFormColor()
    {
        return formColor;
    }

    /**
     * Sets the form color
     *
     * @param formColor  The form color
     */
    public void setFormColor(String formColor)
    {
        this.formColor = formColor;
    }

    /**
     * Gets the section color
     *
     * @return The section color
     */
    public String getSectionColor()
    {
        return sectionColor;
    }

    /**
     * Sets the section color
     *
     * @param sectionColor  The section color
     */
    public void setSectionColor(String sectionColor)
    {
        this.sectionColor = sectionColor;
    }

    /**
     * Gets the form font face
     *
     * @return The form font face
     */
    public String getFormFont()
    {
        return formFont;
    }

    /**
     * Sets the form font face
     *
     * @param formFont  The form font face
     */
    public void setFormFont(String formFont)
    {
        this.formFont = formFont;
    }

    /**
     * Gets the section font face
     *
     * @return The section font face
     */
    public String getSectionFont()
    {
        return sectionFont;
    }

    /**
     * Sets the section font face
     *
     * @param sectionFont  The section font face
     */
    public void setSectionFont(String sectionFont)
    {
        this.sectionFont = sectionFont;
    }


    public int getFormFontSize() {
        return formFontSize;
    }

    public void setFormFontSize(int formFontSize) {
        this.formFontSize = formFontSize;
    }

    public int getCellpadding() {
        return cellpadding;
    }

    public void setCellpadding(int cellpadding) {
        this.cellpadding = cellpadding;
    }

    /**
     * This method allows the transformation of a Form Html Attributes into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return      XML Document
     * @exception   TransformationException is thrown if there is an
     *              error during the XML tranformation
     * @exception   UnsupportedOperationException is thrown if this method
     *              is currently unsupported and not implemented.
     */
    public Document toXML() throws TransformationException
    {
        Document document = super.newDocument();
        Element root = super.initXML(document, "htmlAttributes");

        Element formBorderNode = document.createElement("formBorder");
        if(this.formBorder)
        {
            formBorderNode.appendChild(document.createTextNode("1"));
        }
        else
        {
            formBorderNode.appendChild(document.createTextNode("0"));
        }
        root.appendChild(formBorderNode);

        Element sectionBorderNode = document.createElement("sectionBorder");
        if(this.sectionBorder)
        {
            sectionBorderNode.appendChild(document.createTextNode("1"));
        }
        else
        {
            sectionBorderNode.appendChild(document.createTextNode("0"));
        }
        root.appendChild(sectionBorderNode);

        Element formColorNode = document.createElement("formColor");
        formColorNode.appendChild(document.createTextNode(this.formColor));
        root.appendChild(formColorNode);

        Element formFontNode = document.createElement("formFont");
        formFontNode.appendChild(document.createTextNode(this.formFont));
        root.appendChild(formFontNode);

        Element sectionColorNode = document.createElement("sectionColor");
        sectionColorNode.appendChild(document.createTextNode(this.sectionColor));
        root.appendChild(sectionColorNode);

        Element sectionFontNode = document.createElement("sectionFont");
        sectionFontNode.appendChild(document.createTextNode(this.sectionFont));
        root.appendChild(sectionFontNode);


        Element formFontSize = document.createElement("formFontSize");
        formFontSize.appendChild(document.createTextNode(Integer.toString(this.formFontSize)));
        root.appendChild(formFontSize);

        Element cellpadding = document.createElement("cellpadding");
        cellpadding.appendChild(document.createTextNode(Integer.toString(this.cellpadding)));
        root.appendChild(cellpadding);


        return document;
    }
}
