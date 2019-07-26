package gov.nih.nichd.ctdb.form.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import gov.nih.nichd.ctdb.common.CacheException;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jul 6, 2006
 * Time: 2:19:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImportedRetrevialMaster {


    private static StringBuffer getImportedHtml(String filePath) throws CtdbException {
        StringBuffer sb = new StringBuffer();
        BufferedReader bf = null;
        try {

            FileInputStream in = new FileInputStream(filePath);
            bf = new BufferedReader(new InputStreamReader(in));

            String readLine;
            readLine = bf.readLine();
            while (readLine != null) {
                sb.append(readLine);
                sb.append("\r\n");
                readLine = bf.readLine();
            }
            return sb;
        }
        catch (Exception e) {
            throw new CtdbException("Unable to read from the imported form file: " + e.getMessage(), e);
        }
        finally {
            try {
                bf.close();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public  static String getImportedHtml (ServletContext context, Form f)  throws CtdbException {
        try {
            return HtmlFormCache.getInstance().getHtml(f.getId()).toString();
        } catch (CacheException ce) {
            // Probably not loaded
             String formFileUploadPath = SysPropUtil.getProperty("app.formfilepath");
             String filePath = context.getRealPath(formFileUploadPath);

             if(!filePath.endsWith(new Character(File.separatorChar).toString())) {
                filePath += File.separatorChar;
            }
            filePath += f.getImportFileName();
            StringBuffer sb =getImportedHtml(filePath);
            HtmlFormCache.getInstance().setHtml(f.getId(), sb);
            return sb.toString();
        }
    }

    public static String getImportedFormAnswerPopulatingJavascript(List responses) {

        StringBuffer sb = new StringBuffer();
        sb.append("<script language='javascript'> \n");
        for (Iterator i = responses.iterator(); i.hasNext();) {
            Response r = (Response) i.next();
            List answers;
            if ((r.getEditAnswers().size() > 0)
                    || (r.getEditReason() != null && r.getEditAnswers().size() == 0)) {
                answers = r.getEditAnswers();
            } else {
                answers = r.getAnswers();
            }
            if (answers != null && answers.size() > 0) {

                Question q = r.getQuestion();
                if (q.getType().getValue() == QuestionType.TEXTAREA.getValue() || q.getType().getValue() == QuestionType.TEXTBOX.getValue()) {

                    sb.append(" document.getElementById('Q_").append(q.getId()).append("').value = \"\"");
                    String answer = ((String) answers.get(0));

                    /*
                    answer = answer.replaceAll("\n", "!!@@!!"); // placeholder for newline characters
                    answer = answer.replaceAll("\r", "");   // remove carrige returns
                    answer = answer.replaceAll("\\\\n", "!!@@!!"); // placeholder for javascript newline characters that may be in the string
                    answer = answer.replaceAll("\"", "'");  // replace double quotes w/ single
                    answer = answer.replaceAll("\\\\", "/");  // no backslashes!
                    // remove trailing newlines.
                    if (answer.endsWith("!!@@!!"))  {
                        answer = answer.substring(0, answer.length() - 5) ;
                    }

                    answer = answer.replaceAll("!!@@!!", "\\\\n");  // put in a javascript newline, escaped
                      */
                    answer = java.net.URLEncoder.encode (answer);

                    // line is too long for JS, get separate lines
                    for (int j = 0; j < (answer.length() / 80) + 1; j++) {
                        sb.append("+ \"").append(answer.substring(j * 80, min((j + 1) * 80, answer.length()))).append("\" \n");
                    }
                    sb.append("+\"\"; \n \n");

                    sb.append(" document.getElementById('Q_").append(q.getId()).append("').value = URLDecode (document.getElementById('Q_").append(q.getId()).append("').value );");
                    //   for (int k =0; k < answer.length(); k++) {
                    //       System.err.println( "==============" +(answer.charAt(k)) + new Integer(answer.charAt(k)));
                    //   }

                } else
                if (q.getType().getValue() == QuestionType.SELECT.getValue() || q.getType().getValue() == QuestionType.MULTI_SELECT.getValue()) {
                    sb.append(" opts = document.getElementById('Q_").append(q.getId()).append("').options; \n");
                    sb.append(" answers = new Array (); \n");
                    for (int j = 0; j < answers.size(); j++) {
                        String aAnswer = (String) answers.get(j);
                        aAnswer = aAnswer.replaceAll("\"", "\\\\\"");  // replace double quotes w/ single

                        sb.append(" answers[").append(j).append("] = \"").append(aAnswer).append("\"; \n");
                    }
                    sb.append("    for (i = 0; i < opts.length; i++){ \n");
                    sb.append("        for (j=0; j< answers.length; j++) { \n");
                    sb.append("            if (opts[i].value == answers[j]) { \n");
                    sb.append("                opts[i].selected = true; \n");
                    sb.append("            } \n        } \n        }\n\n");
                } else
                if (q.getType().getValue() == QuestionType.CHECKBOX.getValue() || q.getType().getValue() == QuestionType.RADIO.getValue()) {

                    sb.append(" elems = document.getElementsByName('Q_").append(q.getId()).append("'); \n");
                    sb.append(" var answers = new Array (); \n");
                    for (int j = 0; j < answers.size(); j++) {
                        String aAnswer = (String) answers.get(j);
                        aAnswer = aAnswer.replaceAll("\"", "\\\\\"");
                        sb.append(" answers[").append(j).append("] = \"").append(aAnswer).append("\"; \n");
                    }
                    sb.append("    for (i = 0; i < elems.length; i++){ \n");
                    sb.append("        for (j=0; j< answers.length; j++) { \n");
                    sb.append("            if (elems[i].value  == answers[j]) { \n");
                    sb.append("                elems[i].checked = true; \n");
                    sb.append("            } \n        } \n        }\n\n");

                }
            }
        }                                                                                         
        sb.append("</script>");

        return sb.toString();
    }

    private static int min(int a, int b) {
        if (a < b) {
            return a;
        } else {
            return b;
        }
    }
}
