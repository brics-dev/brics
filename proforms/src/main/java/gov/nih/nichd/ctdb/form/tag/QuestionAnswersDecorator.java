package gov.nih.nichd.ctdb.form.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.question.domain.*;

import java.util.List;
import java.util.Iterator;



/**
 * QuestionAnswersDecorator enables a table to have columns. This
 * class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionAnswersDecorator extends ActionDecorator
{
    /**
     * Default Constructor
     */
    public QuestionAnswersDecorator()
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
     * Retrieves the question's answers.
     *
     * @return  HTML string displaying the skip rule operator on a Row
     */
    public String getAnswers()
    {
        Question question = (Question) this.getObject();

        List answers = question.getAnswers();
        String answerStr = null;
        for (Iterator it = answers.iterator(); it.hasNext();)
        {
            Answer answer = (Answer)it.next();
            if (answerStr != null)
            {
                answerStr += "<br>" + " - " + answer.getDisplay();
            }
            else
            {
                answerStr = " - " + answer.getDisplay();
            }
        }
        return answerStr;
    }
}
