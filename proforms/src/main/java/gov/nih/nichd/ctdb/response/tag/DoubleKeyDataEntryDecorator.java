package gov.nih.nichd.ctdb.response.tag;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.common.util.XslTransformer;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.ImageMapOption;
import gov.nih.nichd.ctdb.question.domain.ImageMapQuestion;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleOperatorType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleType;
import gov.nih.nichd.ctdb.response.domain.CalendarResponse;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * DoubleKeyDataEntryDecorator is used to control data display that
 * are obtained from double key data entry.
 * This class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class DoubleKeyDataEntryDecorator extends ActionDecorator {
    private final String tableString = "<table width=\"100%\" cellspacing=0 cellpadding=0>";
    private final String startString = "<tr><td class=\"tableCell\" width=\"130\" valign=\"top\" align=\"right\">";
    private final String middleString = "</td><td class=\"tableCell\" valign=\"top\" align=\"left\">";
    private final String specialMiddleString = "</td><td class=\"tableCell\" valign=\"middle\" align=\"left\">";
    private final String endString = "</td></tr>";
    private final String closeString = "</table>";
    private final String startInString = "<tr><td class=\"tableCell\" width=\"130\" valign=\"middle\" align=\"center\">";
    private final String middleInString = "</td><td class=\"tableCell\" valign=\"middle\" align=\"left\">";


    /**
     * Default Constructor
     */
    public DoubleKeyDataEntryDecorator() {
        super();
    }

    /**
     * Retrieves the string that will be displayed as a checkbox for
     * setting answer to null.
     *
     * @return HTML string displaying the data as a unit on a row.
     */
    public String getSetBlank() throws JspException {
        Response response = (Response) this.getObject();
        Question q = response.getQuestion();
        String qId = "bk_" + Integer.toString(q.getId());
        List answers = response.getAnswers();

        List editedAnswers = response.getEditAnswers();
        boolean checked = false;

        if (q.getType() == QuestionType.PATIENT_CALENDAR) {
            return "N/A for Patient Calendar";
        }

        if (!editedAnswers.isEmpty()) {
            for (Iterator it = editedAnswers.iterator(); it.hasNext();) {
                String dummy = (String) it.next();
                if (dummy.length() == 0)
                    checked = true;
            }
        }

        String skipRuleString = null;
        if (q.getFormQuestionAttributes().hasSkipRule() && q.getFormQuestionAttributes().getSkipRuleType().equals(SkipRuleType.DISABLE)
                && q.getFormQuestionAttributes().getSkipRuleOperatorType().equals(SkipRuleOperatorType.IS_BLANK))
            skipRuleString = getSkipRuleString(q, "bk_");

        //text.append("Set Answer to blank:<br>");
        if (q.getFormQuestionAttributes().isRequired() || answers.isEmpty() || q.getType() == QuestionType.PATIENT_CALENDAR)
            ////if (answers.isEmpty() || q.getType() == QuestionType.PATIENT_CALENDAR)
            return "";
        else {
            StringBuffer text = new StringBuffer();

            if (skipRuleString != null) {
                text.append("<input ");
                text.append(skipRuleString);
                text.append(" type=\"checkbox\" id=\"" + qId + "\" name=\"" + qId + "\" value=\"" + qId + "\"");
            } else {
                text.append("<input type=\"checkbox\" id=\"" + qId + "\" name=\"" + qId + "\" value=\"" + qId + "\"");
            }
            if (checked) {
                text.append("checked");
            }
            ////String setBlank = (String)this.getPageContext().getRequest().getAttribute("setBlank");
            ////if (q.getFormQuestionAttributes().isRequired() && setBlank == null)
            ////{
            ////text.append(" disabled ");
            ////}
            text.append(" >");
            return text.toString();
        }
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
    public String getResponseEdit() throws JspException {

        Response response = (Response) this.getObject();
        Question q = response.getQuestion();
        String qId = Integer.toString(q.getId());

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


        if (q.getType() == QuestionType.PATIENT_CALENDAR) {
            try {
                String xsl = SysPropUtil.getProperty("question.xsl.patientcalendar.edit");
                InputStream stream = this.getPageContext().getServletContext().getResourceAsStream(xsl);
                String patientCalendar = XslTransformer.transform(((CalendarResponse) response).toXML(), stream, CtdbConstants.GLOBAL_XSL_PARAMETER_MAP);

                text.append(patientCalendar);
            }
            catch (TransformationException te) {
                text.append("<br><br>There was an XSL translation error");
            }

            text.append("<input type=\"hidden\" id=\"reason_" + qId + "\" name=\"reason_" + qId + "\" value=\"\">");
            text.append("<input type=\"hidden\" id=\"row_" + qId + "\" name=\"row_" + qId + "\" value=\"\" >");
            text.append("<input type=\"hidden\" id=\"col_" + qId + "\" name=\"col_" + qId + "\" value=\"\" >");
            text.append("<input type=\"hidden\" id=\"create_" + qId + "\" name=\"create_" + qId + "\" value=\"\" >");
            text.append("<input type=\"hidden\" id=\"" + qId + "\" name=\"" + qId + "\" value=\"\" >");

            /*
             *  need to fix the blank functionality
             */
            text.append("<input type=\"hidden\"  id=\"bk_" + qId + "\" name=\"bk_" + qId + "\" value=\"\" >");


            text.append("<input type=\"hidden\" id=\"response_" + qId + "\" name=\"response_" + qId + "\" value=\"\" >");
            text.append("<div id=\"text_" + qId + "\"></div>");

        } else {

            Response r1 = response.getResponse1();
            List answers1 = r1.getAnswers();
            List editedAnswers = response.getEditAnswers();
            Response r2 = response.getResponse2();
            List answers2 = r2.getAnswers();
            String reason = response.getEditReason();

            List answers = response.getAnswers();


            String skipRuleString = null;
            if (q.getFormQuestionAttributes().hasSkipRule() && q.getFormQuestionAttributes().getSkipRuleType().equals(SkipRuleType.DISABLE)
                    && !q.getFormQuestionAttributes().getSkipRuleOperatorType().equals(SkipRuleOperatorType.IS_BLANK)) {
                skipRuleString = getSkipRuleString(q);
            }
            // why do anything with the skip rule?  it is applied on the
            // back end for answer edit.
            skipRuleString = null;
            // append validation range code to skipRule String already
            // using onChange event.
            if (q.getFormQuestionAttributes().getRangeOperator() != null
                    && !q.getFormQuestionAttributes().getRangeOperator().equals("")
                    && !q.getFormQuestionAttributes().getRangeOperator().equals("0")) {
                // has a validation...
                if (skipRuleString != null) {
                    // need to take off last "
                    skipRuleString = skipRuleString.substring(0, skipRuleString.length() - 2);
                } else {
                    skipRuleString = "";
                }
                skipRuleString += appendValidation(skipRuleString, q.getId());
            }

            text.append(tableString);
            if (q.getFormQuestionAttributes().getLabel() != null && !q.getFormQuestionAttributes().getLabel().equals("")) {
                text.append(startString);
                text.append("");
                text.append(middleString);
                text.append(q.getFormQuestionAttributes().getLabel());
                text.append(endString);
            }
            text.append(startString);
            text.append("Question:&nbsp;&nbsp;&nbsp;&nbsp;");
            text.append(middleString);
            text.append(q.getText());
            text.append(endString);

            text.append(startString);
            text.append("Data Entry 1 Answer:&nbsp;&nbsp;&nbsp;&nbsp;");

            text.append(middleString);
            if (answers1.size() == 1)
                text.append((String) answers1.get(0));
            else {
                for (int i = 0; i < answers1.size(); i++) {
                    if (i > 0)
                        text.append("; ");
                    text.append((String) answers1.get(i));
                }
            }
            text.append(endString);
            text.append(startString);
            text.append("Data Entry 2 Answer:&nbsp;&nbsp;&nbsp;&nbsp;");
            text.append(middleString);
            if (answers2.size() == 1)
                text.append((String) answers2.get(0));
            else {
                for (int i = 0; i < answers2.size(); i++) {
                    if (i > 0)
                        text.append("; ");
                    text.append((String) answers2.get(i));
                }
            }
            text.append(endString);

            text.append(startString);
            text.append("Final Answer:&nbsp;&nbsp;&nbsp;&nbsp;");
            text.append(middleString);
            if (answers.size() == 1)
                text.append((String) answers.get(0));
            else {
                for (int i = 0; i < answers.size(); i++) {
                    if (i > 0)
                        text.append("; ");
                    text.append((String) answers.get(i));
                }
            }
            text.append(endString);

            QuestionType type = q.getType();
            text.append(startString);
            text.append("Edit:&nbsp;&nbsp;&nbsp;&nbsp;");
            text.append(middleString);
            StringBuffer t1 = new StringBuffer();
            String answer = "";
            if (answers.size() > 0) {
                answer = (String) answers.get(0);
            }
            t1.append("\n\n<input type='hidden' name=\"Q_" + q.getId() + "\" id='Q_" + q.getId());
            //t1.append ("\n\n<input type='hidden' name='"+ q.getId()+"' id='"+q.getId()+"_default");
            t1.append("' value=\"" + answer + "\"/>\n\n");
            text.append(t1);

            ////String disableSkipRuleCheckBox = null;
            ////if (q.getFormQuestionAttributes() != null && !q.getFormQuestionAttributes().isCalculatedQuestion())
            ////{
            ////if (q.getFormQuestionAttributes().hasSkipRule() && q.getFormQuestionAttributes().getSkipRuleOperatorType().equals(SkipRuleOperatorType.EQUALS)
            ////&& q.getFormQuestionAttributes().getSkipRuleType() == SkipRuleType.DISABLE)
            ////{
            ////disableSkipRuleCheckBox = this.getActiveDisableSkipRule(q);
            ////}
            ////}

            if (q.getFormQuestionAttributes() != null &&
                    q.getFormQuestionAttributes().isCalculatedQuestion()) {
                text.append("<input ");
                text.append(this.getCalculateString(q));
                text.append("type=\"text\" name=\"");
                text.append(qId);
                text.append("\" id=\"");
                text.append(qId);
                text.append("\"  size=\"25\" value=\"");
                if (editedAnswers.size() > 0)
                    text.append((String) editedAnswers.get(0));

                text.append("\" readonly=\"yes\" >");
            } else if (type.equals(QuestionType.MULTI_SELECT)) {
                text.append("<select ");

                if (skipRuleString != null)
                    text.append(skipRuleString);

                ////if (disableSkipRuleCheckBox != null)
                ////{
                ////text.append(disableSkipRuleCheckBox);
                ////}

                text.append("multiple ");

                text.append("name=\"");
                text.append(qId);
                text.append("\" id=\"");
                text.append(qId);
                text.append("\">");
                List choices = q.getAnswers();
                for (int i = 0; i < choices.size(); i++) {
                    Answer an = (Answer) choices.get(i);
                    String value = an.getDisplay();
                    text.append("<option value=\"");
                    text.append(value);
                    text.append("\"");
                    if (editedAnswers.size() > 0) {
                        boolean found = false;
                        for (int j = 0; j < editedAnswers.size(); j++) {
                            String val = (String) editedAnswers.get(j);
                            if (value.equalsIgnoreCase(val)) {
                                found = true;
                                text.append(" selected >");
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
                text.append("<select ");
                ////if (disableSkipRuleCheckBox != null)
                ////{
                ////text.append(disableSkipRuleCheckBox);
                ////}
                text.append(" name=\"" + qId + "\" id=\"" + qId + "\" multiple=\"true\" >");
                List choices = ((ImageMapQuestion) q).getOptions();

                for (int i = 0; i < choices.size(); i++) {
                    ImageMapOption imo = (ImageMapOption) choices.get(i);
                    // String value = imo.getValue();
                    text.append("<option value=\"" + imo.getOption() + "\"");

                    if (editedAnswers.size() > 0) {
                        boolean found = false;
                        for (int j = 0; j < editedAnswers.size(); j++) {
                            String str = (String) editedAnswers.get(j);
                            if (imo.getOption().equalsIgnoreCase(str)) {
                                found = true;
                                text.append(" selected >");
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
            } else if (type.equals(QuestionType.TEXTAREA)) {
                text.append("<textarea ");
                if (skipRuleString != null)
                    text.append(skipRuleString);

                ////if (disableSkipRuleCheckBox != null)
                ////{
                ////text.append(disableSkipRuleCheckBox);
                ////}

                text.append(" name=\"");
                text.append(qId);
                text.append("\" id=\"");
                text.append(qId);
                text.append("\" cols=\"50\" rows=\"5\">");
                if (editedAnswers.size() > 0) {
                    String str = (String) editedAnswers.get(0);
                    text.append(str);
                }
                text.append("</textarea><a href='Javascript:void(0);' onClick='copyAnswers(" + qId + ");'><img src='/ctdb/images/copyIcon.gif' height=18 width=18 border=0></a>");
            } else {
                if (type.equals(QuestionType.TEXTBOX)) {
                    text.append("<input ");
                    if (skipRuleString != null)
                        text.append(skipRuleString);

                    ////if (disableSkipRuleCheckBox != null)
                    ////{
                    ////text.append(disableSkipRuleCheckBox);
                    ////}

                    text.append(" type=\"text\" name=\"");
                    text.append(qId);
                    text.append("\" id=\"");
                    text.append(qId);
                    text.append("\" size=\"25\" value=\"");
                    if (editedAnswers.size() > 0)
                        text.append((String) editedAnswers.get(0));

                    text.append("\"><a href='Javascript:void(0);' onClick='copyAnswers(" + qId + ");'><img src='/ctdb/images/copyIcon.gif' height=18 width=18 border=0></a>");
                } else if (type.equals(QuestionType.SELECT)) {
                    text.append("<select ");

                    if (skipRuleString != null)
                        text.append(skipRuleString);

                    ////if (disableSkipRuleCheckBox != null)
                    ////{
                    ////text.append(disableSkipRuleCheckBox);
                    ////}

                    text.append(" name=\"");
                    text.append(qId);
                    text.append("\" id=\"");
                    text.append(qId);
                    text.append("\">");
                    List choices = q.getAnswers();

                    if (editedAnswers.size() == 0 || (editedAnswers.size() == 1 && ((String) editedAnswers.get(0)).length() == 0)) {
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

                            boolean found = false;
                            for (int j = 0; j < editedAnswers.size(); j++) {
                                String val = (String) editedAnswers.get(j);
                                if (value.equalsIgnoreCase(val)) {
                                    found = true;
                                    text.append(" selected >");
                                }
                            }
                            if (!found)
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

                        if (skipRuleString != null)
                            text.append(skipRuleString);

                        ////if (disableSkipRuleCheckBox != null)
                        ////{
                        ////text.append(disableSkipRuleCheckBox);
                        ////}

                        text.append(" type=\"radio\" name=\"");
                        text.append(qId);
                        text.append("\" id=\"");
                        text.append(qId);
                        text.append("\" value=\"");
                        text.append(value);
                        text.append("\"");

                        if (editedAnswers.size() > 0) {
                            boolean found = false;
                            for (int j = 0; j < editedAnswers.size(); j++) {
                                String val = (String) editedAnswers.get(j);
                                if (value.equalsIgnoreCase(val)) {
                                    text.append(" checked=\"checked\">");
                                    found = true;
                                }
                            }
                            if (!found)
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
                        text.append("<input ");

                        if (skipRuleString != null)
                            text.append(skipRuleString);

                        ////if (disableSkipRuleCheckBox != null)
                        ////{
                        ////text.append(disableSkipRuleCheckBox);
                        ////}

                        text.append("type=\"checkbox\" name=\"");
                        text.append(qId);
                        text.append("\" id=\"");
                        text.append(qId);
                        text.append("\" value=\"");
                        text.append(value);
                        text.append("\"");
                        if (editedAnswers.size() > 0) {
                            boolean found = false;
                            for (int j = 0; j < editedAnswers.size(); j++) {
                                String val = (String) editedAnswers.get(j);
                                if (value.equalsIgnoreCase(val)) {
                                    text.append(" checked>");
                                    found = true;
                                }
                            }
                            if (!found)
                                text.append(">");
                        } else
                            text.append(">");

                        text.append(value);
                        if (i % 2 != 0)
                            text.append("<br>");
                    }
                }
                text.append(endString);
            }
            text.append(startString);

            text.append("Reason for Change:&nbsp;&nbsp;&nbsp;&nbsp;");
            String reasonId = "reason_" + qId;
            text.append(middleString);
            text.append("<textarea name=\"");
            text.append(reasonId);
            text.append("\" id=\"");
            text.append(reasonId);
            text.append("\" cols=\"50\" rows=\"5\">");

            if (reason != null && reason.length() > 0)
                text.append(reason);

            text.append("</textarea>");
            text.append(endString);
            text.append(closeString);
        }

        return text.toString();
    }

    /**
     * Retrieves the discrepant data from double key data entry.
     *
     * @return HTML string displaying the discrepant data as a unit on a row.
     */
    public String getTwoAnwsers() throws JspException {
        Response response = (Response) this.getObject();
        List responseAnswers = response.getAnswers();
        Question q = response.getQuestion();
        String qId = Integer.toString(q.getId());
        Response r1 = response.getResponse1();
        List answers1 = r1.getAnswers();

        Response r2 = response.getResponse2();
        List answers2 = r2.getAnswers();
        String root = this.getWebRoot();

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


        if (q.getType() == QuestionType.PATIENT_CALENDAR) {
            try {
                String xsl = SysPropUtil.getProperty("question.xsl.patientcalendar.edit");
                InputStream stream = this.getPageContext().getServletContext().getResourceAsStream(xsl);
                String patientCalendar = XslTransformer.transform(((CalendarResponse) response).toXML(true), stream, CtdbConstants.GLOBAL_XSL_PARAMETER_MAP);
                text.append(patientCalendar);

            }
            catch (TransformationException te) {
                text.append("<br><br>There was an XSL translation error");
                System.out.println(te.getMessage() + "\n" + te.toString());
            }

            text.append("<input type=\"hidden\" id=\"row_" + qId + "\" name=\"row_" + qId + "\" value=\"\" >");
            text.append("<input type=\"hidden\" id=\"col_" + qId + "\" name=\"col_" + qId + "\" value=\"\" >");
            text.append("<input type=\"hidden\" id=\"" + qId + "\" name=\"" + qId + "\" value=\"\" >");
            text.append("<input type=\"hidden\" id=\"responseid1_" + qId + "\" name=\"responseid1_" + qId + "\" value=\"\" >");
            text.append("<input type=\"hidden\" id=\"responseid2_" + qId + "\" name=\"responseid2_" + qId + "\" value=\"\" >");
            /*
             *  need to fix the blank functionality
             */
            // text.append("<input type=\"hidden\" name=\"bk_" + qId + "\" value=\"\" >");

            text.append("<div id=\"text_" + qId + "\"></div>");

        } else {

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

            text.append(tableString);
            text.append(startString);
            if (response.getIsFlag()) {
                if (q.getFormQuestionAttributes().getLabel() != null && !q.getFormQuestionAttributes().getLabel().equals("")) {
                    text.append(tableString);
                    text.append(startString);
                    text.append("&nbsp;&nbsp;");
                    text.append(middleInString);
                    text.append("&nbsp;&nbsp;");
                    text.append(endString);
                    text.append(closeString);
                    text.append(specialMiddleString);
                    text.append(q.getFormQuestionAttributes().getLabel());
                    text.append(endString);
                }
                text.append(startString);
                text.append(tableString);
                text.append(startString);
                text.append("<img width=\"25\" height=\"22\"  src=\"" + root + "/images/hand.gif\"/>");
                text.append("<img width=\"15\" height=\"1\"  src=\"" + root + "/images/space.gif\"/>");
                text.append(middleInString);
                text.append("Question:&nbsp;&nbsp;&nbsp;&nbsp;");
                text.append(endString);
                text.append(closeString);
                text.append(specialMiddleString);
                text.append(q.getText());
                text.append(endString);
            } else {
                if (q.getFormQuestionAttributes().getLabel() != null && !q.getFormQuestionAttributes().getLabel().equals("")) {
                    text.append(startString);
                    text.append("");
                    text.append(middleString);
                    text.append(q.getFormQuestionAttributes().getLabel());
                    text.append(endString);
                }
                text.append(startString);
                text.append("Question:&nbsp;&nbsp;&nbsp;&nbsp;");
                text.append(middleString);
                text.append(q.getText());
                text.append(endString);
            }
            text.append(startString);
            text.append("Data&nbsp;Entry&nbsp;1&nbsp;Answer:&nbsp;&nbsp;&nbsp;&nbsp;");
            text.append(middleString);
            if (answers1.size() == 1) {
                text.append((String) answers1.get(0));
            } else {
                for (int i = 0; i < answers1.size(); i++) {
                    if (i > 0)
                        text.append("; ");
                    text.append((String) answers1.get(i));
                }
            }
            text.append(endString);
            text.append(startString);
            text.append("Data&nbsp;Entry&nbsp;2&nbsp;Answer:&nbsp;&nbsp;&nbsp;&nbsp;");
            text.append(middleString);
            if (answers2.size() == 1)
                text.append((String) answers2.get(0));
            else {
                for (int i = 0; i < answers2.size(); i++) {
                    if (i > 0)
                        text.append("; ");
                    text.append((String) answers2.get(i));
                }
            }
            text.append(endString);
            text.append(startString);
            text.append("Final&nbsp;Answer:&nbsp;&nbsp;&nbsp;&nbsp;");
            text.append(middleString);

            ////String disableSkipRule = null;
            QuestionType type = q.getType();

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
                        if (i % 2 != 0)
                            text.append("<br>");
                    }
                } //else if (type.equals(QuestionType.VISUAL_SCALE)) {
                //}

            }
            text.append(endString);
            text.append(closeString);
        }

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
