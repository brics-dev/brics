package gov.nih.nichd.ctdb.form.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.question.domain.*;



/**
 * SkipRuleQuestionDecorator enables a table to have columns. This
 * class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SkipRuleQuestionDecorator extends ActionDecorator
{
    /**
     * Default Constructor
     */
    public SkipRuleQuestionDecorator()
    {
        // default constructor
        super();
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

    /**
     * Retrieves the skip rule operator for a skip rule question.
     *
     * @return  HTML string displaying the skip rule operator on a Row
     */
    public String getOperator()
    {
        Question question = (Question) this.getObject();

        if (question.getFormQuestionAttributes().hasSkipRule())
        {
            String operator = question.getFormQuestionAttributes().getSkipRuleOperatorType().getDispValue();
            if (operator.equals(SkipRuleOperatorType.EQUALS.getDispValue()))
            {
                operator = operator + "/" + question.getFormQuestionAttributes().getSkipRuleEquals();
            }
            return operator;
        }
        else
        {
            return "";
        }
    }

    /**
     * Retrieves the skip rule type for the skip rule question.
     *
     * @return  HTML string displaying the skip rule type on a Row
     */
    public String getSkiprule()
    {
        Question question = (Question) this.getObject();

        if (question.getFormQuestionAttributes().hasSkipRule())
        {
            return question.getFormQuestionAttributes().getSkipRuleType().getDispValue();
        }
        else
        {
            return "";
        }
    }


}
