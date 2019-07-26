package gov.nih.nichd.ctdb.form.tag;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.question.domain.Question;

/**
 * QuestionVersionDecorator formats a radio choice out of a Version letter
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionVersionDecorator extends ActionDecorator {

    public QuestionVersionDecorator() {
        super();
    }

    /**
     * Formats the column value to a radio choice option
     *
     * @return The formated columnValue
     */
    public String getQuestionVersion() {

        CtdbDomainObject domainObject = (CtdbDomainObject) this.getObject();
        Question question = (Question) domainObject;

        return "<input type=\"radio\" name=\"version\" value=\"" + question.getVersion().toString() + "\">" + question.getVersion().toString();
    }


    /**
     * Formats the column value to a radio choice option
     *
     * @return The formated columnValue
     */
    public String getQuestionText() {
        CtdbDomainObject domainObject = (CtdbDomainObject) this.getObject();
        Question question = (Question) domainObject;

        return "<div id=\"" + question.getVersion() + "_div\">" + question.getText() + "</div>";
    }

    public String getQuestionType() {
        CtdbDomainObject domainObject = (CtdbDomainObject) this.getObject();
        Question question = (Question) domainObject;

        return "<div id=\"" + question.getVersion() + "_type_div\">" + question.getType() + "</div>";
    }



    private String notNull (String s) {
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }
}