package gov.nih.nichd.ctdb.form.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.common.CtdbConstants;

import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;

/**
 * QuestionSearchDecorator enables a table to have a column with Action links (Edit/View/..). This
 * class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionSearchDecorator extends ActionDecorator
{
    /**
     * Default Constructor
     */
    public QuestionSearchDecorator()
    {
        super();
    }

    /**
     * Retrieves the checkbox value associated with the question.
     *
     * @return  HTML string displaying the question's checkbox on a Row
     */
    public String getCheckbox()
    {
        Question question = (Question) this.getObject();
        String questionId = Integer.toString(question.getId());

        boolean checked = false;
        if (this.getPageContext().getSession().getAttribute("selectedQuestionIds") != null) {
            Set qIds = (Set) this.getPageContext().getSession().getAttribute("selectedQuestionIds");
            for(Iterator iter = qIds.iterator(); iter.hasNext(); )
            {
                if(questionId.equalsIgnoreCase((String)iter.next()))
                {
                    checked = true;
                    break;
                }
            }
        }
        if( !checked )
        {
            return "<input type=\"checkbox\" name=\"selectedQuestions\" value=\"" + questionId + "\">";
        }
        else
        {
            return "<input type=\"checkbox\" name=\"selectedQuestions\" value=\"" + questionId + "\" checked>";
        }

    }

    /**
     * Retrieves the question type along with its question name and text.
     *
     * @return  HTML string displaying the question type on a Row
     */
    public String getQuestionType()
    {
        Question question = (Question) this.getObject();
        StringBuffer qt  = new StringBuffer();
        qt.append("<div id=\"" + CtdbConstants.QUESTION_VERSION_TEXTBOX_TAG + question.getId() + "yy");
        qt.append("\">" + question.getType() + "</div>");
        return qt.toString();
    }

    public String getQuestionText() {
        StringBuffer qt = new StringBuffer();
        Question question = (Question) this.getObject();
        
        qt.append("<div id=\"" + CtdbConstants.QUESTION_VERSION_TEXTBOX_TAG + question.getId() + "xx");
        qt.append("\">" + question.getText() + "</div>");
        
        return qt.toString();   
    }


    public String getVersionAction() {
		Question question = (Question) this.getObject();
        HashMap versionMap = (HashMap) this.getPageContext().getSession().getAttribute("versionMap");
        String curVersion;
        if (this.getPageContext().getSession().getAttribute("versionMap") != null &&
                versionMap.get(Integer.toString(question.getId())) != null) {
            curVersion = (String)versionMap.get (Integer.toString(question.getId()));
        } else {
            curVersion = question.getVersion().toString();
        }

        String action = "<input type=\"hidden\" name=\"" + CtdbConstants.QUESTION_VERSION_TEXTBOX_TAG + question.getId()
                            + "\" size=1 maxlength=2 readonly='yes'	value=\"" + curVersion + "\">";
           action +=  "&nbsp;<a href=\"Javascript:popupWideWindow('questionVersionChange.do?id=" + question.getId() + "')\">";
          action += "<span id='question_"+question.getId() + "_displayVersion'>"+curVersion +"</span></a>";
          return action;

      /*
        if (question.getVersion().getVersionNumber() == 1) {
            return "A&nbsp;<a href=\"Javascript:popupWindow('questionVersionChange.do?id="+ question.getId() + "')\">view</a>";
        }
        else {
            String action = "<input type=\"text\" name=\""
                            + CtdbConstants.QUESTION_VERSION_TEXTBOX_TAG + question.getId()
                            + "\" size=1 maxlength=2 readonly='yes'	value=\"";

            if (this.getPageContext().getSession().getAttribute("versionMap") != null) {
                HashMap versionMap = (HashMap) this.getPageContext().getSession().getAttribute("versionMap");
                if (versionMap.get(Integer.toString(question.getId())) != null) {
                    action += versionMap.get(Integer.toString(question.getId()));
                } else {
                    action += question.getVersion();
                }
            } else {
                action += question.getVersion();
            }
            action +=  "\">&nbsp;<a href=\"Javascript:popupWindow('questionVersionChange.do?id="
                        + question.getId() + "')\">change</a>";


            return action;
	      }
	          */
    }

 

}
