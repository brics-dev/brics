package gov.nih.nichd.ctdb.form.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.question.domain.Question;


/**
 * SectionDispalyattributeDecorator enables a table to have a column
 * with Action links (Edit/View/..). This
 * class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SectionDisplayAttributesDecorator extends ActionDecorator
{
    /**
     *  Default constructor
     */
    public SectionDisplayAttributesDecorator()
    {
    }

    public String getActions ()
    {
        Question q = (Question) this.getObject();
        String actions = "<input type='checkbox' name='selectedQuestions' value='";
        actions += q.getId() + "'";
        if (!q.isTextDisplayed()) {
            actions += " checked ";
        }
        actions += ">";
        return actions;
    }

    public String getQuestionLabel()
    {
        Question q = (Question)this.getObject();
        FormQuestionAttributes qAttrs = q.getFormQuestionAttributes();
        String qLabel = "<input type='text' name='textlabel_" + this.getListIndex() + "' maxlength='250' size='25' ";
        if (qAttrs.getLabel() != null)
        {
            qLabel += "value=\"" + qAttrs.getLabel() + "\"";
        }
        qLabel += ">";
        qLabel += "<input type='hidden' name='questionid_" + this.getListIndex() + "' value='" + q.getId() + "'>";
        return qLabel;
    }
}
