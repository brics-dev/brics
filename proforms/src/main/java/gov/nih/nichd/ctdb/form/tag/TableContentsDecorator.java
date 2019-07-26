package gov.nih.nichd.ctdb.form.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.form.domain.Section;


/**
 * TableContentsDecorator enables a table to have a column
 * with Action links (Edit/View/..). This
 * class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class TableContentsDecorator extends ActionDecorator
{
    /**
     *  Default constructor
     */
    public TableContentsDecorator()
    {
    }

    /**
     * Creates the checkbox for each section on the form.
     *
     * @return String of checkbox
     */
    public String getCheckbox()
    {
        Section section = (Section) this.getObject();
        String checkbox = "<input type='checkbox' name='selectedSections' id='" + this.getListIndex()
                + "' value='";
        checkbox += section.getId() + "'";
        if (section.isIntob()) {
            checkbox += " checked ";
        }
        //checkbox += " onclick='checkAltLabel(" + this.getListIndex() + ");'>";
        checkbox += ">";
        return checkbox;
    }

    /**
     * Creates the textbox for the alternative label for each section.
     *
     * @return  String of the textbox
     */
    public String getAltLabel()
    {
        Section section = (Section) this.getObject();
        String aLabel = "<input type='text' name='altlabel_" + this.getListIndex() + "' id='altlabel_" + this.getListIndex()
        + "' maxlength='255' size='35' ";
        if (section.getAltLabel() != null)
        {
            aLabel += "value='" + section.getAltLabel() + "'";
        }
        aLabel += ">";
        aLabel += "<input type='hidden' name='sectionid_" + this.getListIndex() + "' value='" + section.getId() + "'>";
        return aLabel;
    }
}
