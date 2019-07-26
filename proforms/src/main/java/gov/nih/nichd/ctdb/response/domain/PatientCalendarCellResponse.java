package gov.nih.nichd.ctdb.response.domain;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;


/**
 * Response DomainObject for the NICHD CTDB Application
 *
 * @author  Booz Allen Hamilton
 * @version 1.0
 */

public class PatientCalendarCellResponse extends Response
{
    private static final long serialVersionUID = -3647538402488692026L;
    
	private int row; 
    private int col; 
    private int rowId; 
    
    /**
     * Default Constructor for the Response Domain Object
     */
    public PatientCalendarCellResponse()
    {
        //default constructor
        super();
    }

    /**
      * @return
      */
     public int getCol() {
         return col;
     }

     /**
      * @return
      */
     public int getRow() {
         return row;
     }

     /**
      * @param i
      */
     public void setCol(int i) {
         col = i;
     }

     /**
      * @param i
      */
     public void setRow(int i) {
         row = i;
     }

    /**
     * This method allows the transformation of a Response into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException is thrown if there is an error during the XML tranformation
     */
    public Document toXML() throws TransformationException
    {
        try
        {
            Document document = super.newDocument();
            Element root = super.initXML(document, "response");

            Element questionNode = document.createElement("question");
            int questionId = this.getQuestion().getId();
            QuestionManager qm = new QuestionManager();
            String qText = qm.getQuestion(questionId).getText();
            questionNode.appendChild(document.createTextNode(qText));
            root.appendChild(questionNode);

            Element answersNode = document.createElement("answers");
            for(Iterator it = this.getAnswers().iterator(); it.hasNext();)
            {
                String answer = (String) it.next();
                if(answer != null && !answer.equals(""))
                {
                    answersNode.appendChild(document.createTextNode(answer));
                }
            }
            root.appendChild(answersNode);

            return document;
        }
        catch(Exception ex)
        {
            throw new UnsupportedOperationException("Unable to transform object " + this.getClass().getName()
                                                    + " with id = " + this.getId());
        }
    }
    /**
     * @return
     */
    public int getRowId() {
        return rowId;
    }

    /**
     * @param i
     */
    public void setRowId(int i) {
        rowId = i;
    }

}
