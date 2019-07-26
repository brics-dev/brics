package gov.nih.nichd.ctdb.form.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.question.domain.*;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.CalculatedFormQuestionAttributes;




/**
 * CalculatedQuestionDecorator enables a table to have columns. This
 * class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class CalculatedQuestionDecorator extends ActionDecorator
{
    /**
     * Default Constructor
     */
    public CalculatedQuestionDecorator()
    {
        // default constructor
        super();
    }

    public String getCalculation()
    {
        Question question = (Question)this.getObject();
        FormQuestionAttributes qAttrs = question.getFormQuestionAttributes();
        if (qAttrs.isCalculatedQuestion())
        {
            return ((CalculatedFormQuestionAttributes)qAttrs).getCalculation();
        }
        else
        {
            return "";
        }
    }
    /**
     * Retrieves the text of a question in a way that the text length
     * is limited to about 90 characters on display.
     *
     * @return  HTML string displaying the text on a Row
     */
    public String getTextdisplay()
    {
        Question question = (Question) this.getObject();

        String text = question.getText();

        if(text == null)
            return text;

        int index = text.indexOf(" ", 90);

        if(index != -1)
            return text.substring(0, index) + "...";
        else
            return text;
    }

}
