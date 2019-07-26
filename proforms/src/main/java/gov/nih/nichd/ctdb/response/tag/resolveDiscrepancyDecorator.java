package gov.nih.nichd.ctdb.response.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.ImageMapOption;
import gov.nih.nichd.ctdb.question.domain.ImageMapQuestion;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleOperatorType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleType;
import gov.nih.nichd.ctdb.response.domain.Response;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;

/**
 * DoubleKeyDataEntryDecorator is used to control data display that
 * are obtained from double key data entry.
 * This class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class resolveDiscrepancyDecorator  extends ActionDecorator {
    /**
     * Default Constructor
     */
    public resolveDiscrepancyDecorator(){
        super();
    }


    /**
     * Retrieves the string that will be displayed as a checkbox for
     * setting answer to null for resolving discrepancy.
     *
     * @return HTML string displaying the data as a unit on a row.
     */
    public String getBlank() throws JspException {
        Response response = (Response) this.getObject();
        Question q = response.getQuestion();
        String qId = "bk_" + Integer.toString(q.getId());
        List answers = response.getAnswers();

        if (q.getType() == QuestionType.PATIENT_CALENDAR) {
            return "N/A for Patient Calendar";
        }

        boolean checked = false;

        if (!answers.isEmpty()) {
            for (Iterator it = answers.iterator(); it.hasNext();) {
                String dummy = (String) it.next();
                if (dummy.length() == 0)
                    checked = true;
            }
        }

        //The following skip rule javascript function call
        //is included in case that at least one of the child
        //responses has discrepancy.
        String skipRuleString = null;
        if (q.getFormQuestionAttributes().hasSkipRule() && q.getFormQuestionAttributes().getSkipRuleType().equals(SkipRuleType.DISABLE)
                && q.getFormQuestionAttributes().getSkipRuleOperatorType().equals(SkipRuleOperatorType.IS_BLANK))
            skipRuleString = getSkipRuleString(q, "bk_");

        //text.append("Set Answer to blank:<br>");
        ////if ( q.getFormQuestionAttributes().isRequired() || q.getType() == QuestionType.PATIENT_CALENDAR)// || answers.isEmpty())
        if (q.getType() == QuestionType.PATIENT_CALENDAR)// || answers.isEmpty())
            return "";
        else {
            StringBuffer text = new StringBuffer();

            if (skipRuleString != null) {
                text.append("<input ");
                text.append(skipRuleString);
                text.append(" type=\"checkbox\" id=\"" + qId + "\" name=\"" + qId + "\" value=\"" + qId + "\"");

                if (checked)
                    text.append("checked");

            } else {
                text.append("<input type=\"checkbox\" id=\"" + qId + "\" name=\"" + qId + "\" value=\"" + qId + "\"");

                if (checked)
                    text.append("checked");


            }
            ////String setBlank = (String)this.getPageContext().getRequest().getAttribute("setBlank");
            ////if (q.getFormQuestionAttributes().isRequired() && setBlank == null)
            ////{
            ////text.append(" disabled ");
            ////}

            text.append(">");
            return text.toString();
        }
    }


    /**
     * Retrieves the all data from double key data entry for final edit.
     *
     * @return HTML string displaying the data as a unit on a row.
     */

    /**
     * Retrieves the discrepant data from double key data entry.
     *
     * @return HTML string displaying the discrepant data as a unit on a row.
     */
    public String getTwoAnwsers() throws JspException {
    	this.getPageContext().getRequest().getParameter("action");
        Response response = (Response) this.getObject();
        List responseAnswers = response.getAnswers();
        Question q = response.getQuestion();
        String qId = Integer.toString(q.getId());
        Response r1 = response.getResponse1();
        List answers1 = r1.getAnswers();

        Response r2 = response.getResponse2();
        List answers2 = r2.getAnswers();
        this.getWebRoot();

        StringBuffer text = new StringBuffer();

        String lastSection = (String) this.getPageContext().getSession().getAttribute("LastSectionName");
        if (lastSection != null) {
            if (!lastSection.equalsIgnoreCase(response.getQuestion().getParentSectionName())) {
                // section changed, print section name
                text.append("<span style='width:100%'> <b>");
                text.append(response.getQuestion().getParentSectionName() + "</b></span><br><br>");
            }
            this.getPageContext().getSession().setAttribute("LastSectionName", response.getQuestion().getParentSectionName());
        } else {
            // firstTime
            text.append("<span style='width:100%'> <b>");
            text.append(response.getQuestion().getParentSectionName() + "</b></span><br><br>");
            this.getPageContext().getSession().setAttribute("LastSectionName", response.getQuestion().getParentSectionName());
        }



            List answers = new ArrayList();
            if (!responseAnswers.isEmpty()) {
                for (int i = 0; i < responseAnswers.size(); i++) {
                    String str = (String) responseAnswers.get(i);
                    if (str.length() > 0) {
                        answers.add(str);
                    } else {
                        answers.clear();
                        break;
                    }
                }
            }

            text.append("<table width=\"100%\" border=0 cellspacing=4 cellpadding=4>\n");
            text.append("<tr><td colspan=\"2\"  valign=\"top\" align=\"left\">\n");
            text.append("[Question]");
            text.append(q.getText());
            text.append("</td></tr>");

            text.append("<tr><td  width=\"50%\" valign=\"top\" align=\"left\">\n");
            text.append("Data Entry 1: ");
 
            if (answers1.size() == 1) {
                text.append((String) answers1.get(0));
            } else {
                for (int i = 0; i < answers1.size(); i++) {
                    if (i > 0)
                        text.append("; ");
                    text.append((String) answers1.get(i));
                }
            }
            text.append("<br/>");
            
            text.append("Data Entry 2: ");
          
            if (answers2.size() == 1)
                text.append((String) answers2.get(0));
            else {
                for (int i = 0; i < answers2.size(); i++) {
                    if (i > 0)
                        text.append("; ");
                    text.append((String) answers2.get(i));
                }
            }
            text.append("</td>");
            text.append("<td class=\"tableCell\"  valign=\"top\" align=\"left\">\n");
            // it hide for file type,it should be show in future require
            QuestionType type = q.getType();
            if(!type.equals(QuestionType.File)){
            text.append("Final Answer:  ");
            }

            ////String disableSkipRule = null;

            ////if (q.getFormQuestionAttributes() != null && !q.getFormQuestionAttributes().isCalculatedQuestion())
            ////{
            ////if (q.getFormQuestionAttributes().hasSkipRule() && q.getFormQuestionAttributes().getSkipRuleOperatorType().equals(SkipRuleOperatorType.EQUALS)
            ////&& q.getFormQuestionAttributes().getSkipRuleType() == SkipRuleType.DISABLE)
            ////{
            ////disableSkipRule = this.getActiveDisableSkipRule(q);
            ////}
            ////}

            //Calculated question always is treated as no discrepancy.
            if (q.getFormQuestionAttributes() != null && q.getFormQuestionAttributes().isCalculatedQuestion()) {
                text.append("<input ");
                text.append(this.getCalculateString(q));
                text.append("type=\"text\" name=\"");
                text.append(qId);
                text.append("\" id=\"Q_");
                text.append(qId);
                text.append("\"  size=\"25\" value=\"");
                if (answers.size() > 0)
                    text.append((String) answers.get(0));
                text.append("\" readonly=\"yes\" >");

                /*text.append("<input type=\"text\" name=\"");
                text.append(qId);
                text.append("\"  size=\"25\" value=\"");
                text.append("readonly=\"yes\"");
                text.append("\">");*/
            } else if (type.equals(QuestionType.MULTI_SELECT)) {
                text.append("<select name=\"" + qId + "\" id=\"Q_" + qId + "\" multiple=\"true\" ");
                ////if (disableSkipRule != null)
                ////{
                ////text.append(disableSkipRule);
                ////}
                text.append(" >");

                List choices = q.getAnswers();

                for (int i = 0; i < choices.size(); i++) {
                    Answer an = (Answer) choices.get(i);
                    String value = an.getDisplay();
                    text.append("<option value=\"" + value + "\"");

                    if (answers.size() > 0) {
                        boolean found = false;
                        for (int j = 0; j < answers.size(); j++) {
                            String str = (String) answers.get(j);
                            if (value.equalsIgnoreCase(str)) {
                                found = true;
                                text.append(" selected >");
                                break;
                            }
                        }
                        if (!found)
                            text.append(">");
                    } else
                        text.append(">");

                    text.append(value);
                    text.append("</option>");
                }
                text.append("</select>");
            } else if (type.equals(QuestionType.IMAGE_MAP)) {
                text.append("<select name=\"" + qId + "\" id=\"Q_" + qId + "\" multiple=\"true\" ");
                ////if (disableSkipRule != null)
                ////{
                ////text.append(disableSkipRule);
                ////}
                text.append(" >");
                List choices = ((ImageMapQuestion) q).getOptions();

                outerLoop:
                for (int i = 0; i < choices.size(); i++) {
                    ImageMapOption imo = (ImageMapOption) choices.get(i);
                    text.append("<option value=\"" + imo.getOption() + "\"");

                    if (answers.size() > 0) {
                        boolean found = false;
                        innerLoop:
                        for (int j = 0; j < answers.size(); j++) {
                            String str = (String) answers.get(j);
                            if (imo.getOption().equalsIgnoreCase(str)) {
                                found = true;
                                text.append(" selected >");
                                break innerLoop;
                            }
                        }
                        if (!found)
                            text.append(">");
                    } else
                        text.append(">");

                    text.append(imo.getOption());
                    text.append("</option>");
                }
                text.append("</select>");
            } else {
                if (type.equals(QuestionType.TEXTBOX) || type.equals(QuestionType.VISUAL_SCALE)) {
                    text.append("<input ");
                    ////if (disableSkipRule != null)
                    ////{
                    ////text.append(disableSkipRule);
                    ////}
                    text.append(" type=\"text\" name=\"");
                    text.append(qId);
                    text.append("\" id=\"Q_");
                    text.append(qId);
                    text.append("\" size=\"25\" value=\"");
                    if (answers.size() > 0) {
                        String str = (String) answers.get(0);
                        text.append(str);
                        text.append("\">");
                    } else
                        text.append("\">");
                } else if (type.equals(QuestionType.File)) {
                    text.append("<input ");
                    ////if (disableSkipRule != null)
                    ////{
                    ////text.append(disableSkipRule);
                    ////}
                    text.append(" type=\"hidden\" name=\"");
                    text.append(qId);
                    text.append("\" id=\"Q_");
                    text.append(qId);
                    text.append("\" size=\"25\" value=\"nan:0\">");
                         	
                } else if (type.equals(QuestionType.TEXTAREA)) {
                    ////text.append("<textarea ");
                    ////if (disableSkipRule != null)
                    ////{
                    ////text.append(disableSkipRule);
                    ////}
                    text.append("<textarea name=\"");
                    text.append(qId);
                    text.append("\" id=\"Q_");
                    text.append(qId);
                    text.append("\" cols=\"50\" rows=\"5\">");
                    if (answers.size() > 0) {
                        String str = (String) answers.get(0);
                        text.append(str);
                        text.append("</textarea>");
                    } else
                        text.append("</textarea>");
                } else if (type.equals(QuestionType.SELECT)) {
                    text.append("<select ");
                    ////if (disableSkipRule != null)
                    ////{
                    ////text.append(disableSkipRule);
                    ////}
                    text.append(" name=\"");
                    text.append(qId);
                    text.append("\" id=\"Q_");
                    text.append(qId);
                    text.append("\">");
                    List choices = q.getAnswers();

                    if (answers.size() == 0 || (answers.size() == 1 && ((String) answers.get(0)).length() == 0)) {
                        //in this case, the first select is blank
                        text.append("<option value=\"");
                        text.append("");
                        text.append("\"");
                        text.append(">");
                        text.append("");
                        text.append("</option>");

                        for (int i = 0; i < choices.size(); i++) {
                            Answer an = (Answer) choices.get(i);
                            String value = an.getDisplay();
                            text.append("<option value=\"");
                            text.append(value);
                            text.append("\"");

                            text.append(">");

                            text.append(value);
                            text.append("</option>");
                        }
                    } else {
                        for (int i = 0; i < choices.size(); i++) {
                            Answer an = (Answer) choices.get(i);
                            String value = an.getDisplay();
                            text.append("<option value=\"");
                            text.append(value);
                            text.append("\"");

                            if (value.equalsIgnoreCase((String) answers.get(0)))
                                text.append(" selected >");
                            else
                                text.append(">");

                            text.append(value);
                            text.append("</option>");
                        }
                    }
                    text.append("</select>");
                } else if (type.equals(QuestionType.RADIO)) {
                    List choices = q.getAnswers();

                    for (int i = 0; i < choices.size(); i++) {
                        Answer an = (Answer) choices.get(i);
                        String value = an.getDisplay();
                        text.append("<input ");
                        ////if (disableSkipRule != null)
                        ////{
                        ////text.append(disableSkipRule);
                        ////}
                        text.append(" type=\"radio\" name=\"");
                        text.append(qId);
                        text.append("\" id=\"Q_");
                        text.append(qId);
                        text.append("\" value=\"");
                        text.append(value);
                        text.append("\"");
                        if (answers.size() > 0) {
                            if (value.equalsIgnoreCase((String) answers.get(0)))
                                text.append(" checked=\"checked\">");
                            else
                                text.append(">");
                        } else
                            text.append(">");

                        text.append(value);
                    }
                } else if (type.equals(QuestionType.CHECKBOX)) {
                    List choices = q.getAnswers();

                    for (int i = 0; i < choices.size(); i++) {
                        Answer an = (Answer) choices.get(i);
                        String value = an.getDisplay();
                        ////text.append("<input ");
                        ////if (disableSkipRule != null)
                        ////{
                        ////text.append(disableSkipRule);
                        ////}
                        text.append("<input type=\"checkbox\" name=\"");
                        text.append(qId);
                        text.append("\" id=\"Q_");
                        text.append(qId);
                        text.append("\" value=\"");
                        text.append(value);
                        text.append("\"");
                        
                       

                        if (answers.size() > 0) {
                            boolean found = false;
                            for (int j = 0; j < answers.size(); j++) {
                                String str = (String) answers.get(j);
                                if (value.equalsIgnoreCase(str)) {
                                    found = true;
                                    text.append(" checked >");
                                }
                            }
                            if (!found)
                                text.append(">");
                        } else
                            text.append(">");
                        text.append(value);
                      /* if(value.equals(CtdbConstants.OTHER_OPTION_DISPLAY)){
                        //added by yogi to give user option to insert other answer type for select type question
	                        text.append("<input type=\"text\" name=\"");
	                        text.append(qId);
	                        text.append("\" id=\"Q_");
	                        text.append(qId);
	                        text.append("_otherBox");
	                        text.append("\" value=\"");
	                       // text.append(value);
	                        text.append("\"");
	                        text.append("/input>");
                       }*/
                        
                        if (i % 2 != 0)
                            text.append("<br>");
                    }
                } //else if (type.equals(QuestionType.VISUAL_SCALE)) {
                //}

            }
            text.append("<br/>comment:  <textarea name=\"comment_"+qId+"\"   rows=\"3\" cols=\"10\" >");
            if(response.getComment()!=null){
            	 text.append(response.getComment());
            }
            text.append("</textarea>");
            text.append("</td></tr>");
            text.append("</table>");
        return text.toString();
    }

    /**
     * Populates the JavaScript function call for disable skip rules
     *
     * @param q Question
     * @return JavaScript string for the skip rule calls
     */
    private String getActiveDisableSkipRule(Question q) {
        StringBuffer sb = new StringBuffer();
        boolean hasRequiredSkipQuestion = false;
        List qToSkip = q.getFormQuestionAttributes().getQuestionsToSkip();
        int j = 0;
        for (int i = 0; i < qToSkip.size(); i++) {
            Question qs = (Question) qToSkip.get(i);
            if (qs.getFormQuestionAttributes().isRequired()) {
                hasRequiredSkipQuestion = true;
                if (j != 0) {
                    sb.append(", \'");
                } else {
                    sb.append("onclick=\"activedisableskiprule(this, [\'");
                }
                sb.append(Integer.toString(qs.getId()));
                sb.append("\'");
                j++;
            }
        }
        if (hasRequiredSkipQuestion) {
            sb.append("], \'");
            sb.append(q.getFormQuestionAttributes().getSkipRuleOperatorType().toString());
            sb.append("\', \'");
            sb.append(q.getFormQuestionAttributes().getSkipRuleType().toString());
            sb.append("\', \'");
            if (q.getFormQuestionAttributes().getSkipRuleEquals() != null && q.getFormQuestionAttributes().getSkipRuleEquals().length() > 0)
                sb.append(q.getFormQuestionAttributes().getSkipRuleEquals());

            sb.append("\')\"");
            return sb.toString();
        } else {
            return null;
        }
    }

    /**
     * Populates the JavaScript function call for skip rules
     *
     * @param q Question
     * @return JavaScript string for the skip rule calls
     */
    private String getSkipRuleString(Question q) {
        StringBuffer sb = new StringBuffer();
        sb.append("onchange=\"applyskiprule(this, [\'");
        List qToSkip = q.getFormQuestionAttributes().getQuestionsToSkip();
        for (int i = 0; i < qToSkip.size(); i++) {
            Question qs = (Question) qToSkip.get(i);
            if (i != 0)
                sb.append(", \'");
            sb.append(Integer.toString(qs.getId()));
            sb.append("\'");
        }
        sb.append("], \'");
        sb.append(q.getFormQuestionAttributes().getSkipRuleOperatorType().toString());
        sb.append("\', \'");
        sb.append(q.getFormQuestionAttributes().getSkipRuleType().toString());
        sb.append("\', \'");
        if (q.getFormQuestionAttributes().getSkipRuleEquals() != null && q.getFormQuestionAttributes().getSkipRuleEquals().length() > 0)
            sb.append(q.getFormQuestionAttributes().getSkipRuleEquals());

        sb.append("\')\"");
        return sb.toString();
    }


    private String appendValidation(String str, int qId) {
        if (str == null || str.equals("")) {
            str = "onchange=\"";
        }
        str += "changeForValidation ('Q_" + qId + "', this.value);\"  ";
        return str;
    }

    /**
     * Populates the JavaScript function call for skip rules. This is the
     * overloaded method of getSkipRuleString(Question q) by taking in
     * a postfix string.
     *
     * @param q Question
     * @return JavaScript string for the skip rule calls
     */
    private String getSkipRuleString(Question q, String postfix) {
        StringBuffer sb = new StringBuffer();
        sb.append("onClick=\"applyskipruleisblank(this, [\'");
        List qToSkip = q.getFormQuestionAttributes().getQuestionsToSkip();
        for (int i = 0; i < qToSkip.size(); i++) {
            Question qs = (Question) qToSkip.get(i);
            if (i != 0)
                sb.append(", \'");
            sb.append(postfix + Integer.toString(qs.getId()));
            sb.append("\'");
        }
        sb.append("])\"");

        return sb.toString();
    }


    /**
     * Populates the JavaScript function call for calculations
     *
     * @param q Question
     * @return JavaScript string for the calculation calls
     */
    private String getCalculateString(Question q) {

        if (q.getFormQuestionAttributes() == null ||
                q.getCalculatedFormQuestionAttributes() == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        sb.append("onclick=\"calculate(this, \'");
        sb.append(q.getCalculatedFormQuestionAttributes().getCalculation());
        sb.append("\',\'");
        if (q.getFormQuestionAttributes().getAnswerType().getValue() == AnswerType.NUMERIC.getValue()) {
            sb.append("numeric");
        } else if (q.getFormQuestionAttributes().getAnswerType().getValue() == AnswerType.DATE.getValue() ||
                q.getFormQuestionAttributes().getAnswerType().getValue() == AnswerType.DATETIME.getValue()) {
            sb.append("datetime");
        }

        sb.append("\',\'1\')\" ");

        return sb.toString();
    }
}
