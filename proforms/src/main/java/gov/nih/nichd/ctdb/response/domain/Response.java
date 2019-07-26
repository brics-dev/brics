package gov.nih.nichd.ctdb.response.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;


/**
 * Response DomainObject for the NICHD CTDB Application
 *
 * @author  Booz Allen Hamilton
 * @version 1.0
 */

public class Response extends CtdbDomainObject
{
    private static final long serialVersionUID = -1743062169161349924L;
	
    private Response response1;
    private Response response2;
    private Question question;
    private AdministeredForm administeredForm;
    private List<String> answers = new ArrayList<String>();
	private List<String> submitAnswers = new ArrayList<String>();
	//private List<String> deComments = new ArrayList<String>();
	private String comment = "";
    private String editReason;
    private int editedBy = Integer.MIN_VALUE;
    private Date editedDate;
    private List<String> editAnswers = new ArrayList<String>();
    private List<Response> versionResponses = new ArrayList<Response>();
    private boolean isFlag = false;
    
    public boolean isAnswerIncludesOtherPleaseSpecify() {
		return answerIncludesOtherPleaseSpecify;
	}

	public void setAnswerIncludesOtherPleaseSpecify(
			boolean answerIncludesOtherPleaseSpecify) {
		this.answerIncludesOtherPleaseSpecify = answerIncludesOtherPleaseSpecify;
	}

	private boolean answerIncludesOtherPleaseSpecify = false;
    


	/**
     * Default Constructor for the Response Domain Object
     */
    public Response()
    {
        //default constructor
    }

    /**
     * Gets Response domain object response1
     *
     * @return response1
     */
    public Response getResponse1()
    {
        return response1;
    }

    /**
     * Sets Response domain object response1
     *
     * @param response1   the response domain object
     */
    public void setResponse1(Response response1)
    {
        this.response1 = response1;
    }

    
    /**
     * Get a response based on the given number
     * 
     * @param i
     * @return
     */
    public Response getResponse(int i)
    {        
        System.out.println("getResponse(" + i +")");
        if (i == 1)
        {
            return getResponse1();   
        }
        else if (i == 2) 
        {
            return getResponse2();
        }
        else
        {
            return null;
        }
    }
    
        public void setResponse(int i, Response response)
        {        
            if (i == 1)
            {
                this.setResponse1(response);   
            }
            else if (i == 2) 
            {
                this.setResponse2(response);
            }
            else
            {
                return;
            }
        }
    
        
    /**
     * Gets Response domain object
     *
     * @return response2
     */
    public Response getResponse2()
    {
        return response2;
    }

    /**
     * Sets Response domain object
     *
     * @param response2  the response domain object
     */
    public void setResponse2(Response response2)
    {
        this.response2 = response2;
    }

    /**
     * Gets the question domain object
     *
     * @return question
     */
    public Question getQuestion()
    {
        return question;
    }

    
    public List<String> getSubmitAnswers() {
		return submitAnswers;
	}

	public void setSubmitAnswers(List<String> submitAnswers) {
		this.submitAnswers = submitAnswers;
	}
	
    /**
     * Sets the Question domain object
     *
     * @param question The Question domain object
     */
    public void setQuestion(Question question)
    {
        this.question = question;
    }

    /**
     * Gets the administered form domain object
     *
     * @return administeredForm
     */
    public AdministeredForm getAdministeredForm()
    {
        return administeredForm;
    }

    /**
     * Sets the administered form domain object
     *
     * @param administeredForm  The administered form domain object
     */
    public void setAdministeredForm(AdministeredForm administeredForm)
    {
        this.administeredForm = administeredForm;
    }

    /**
     * Gets the answer list for the response
     *
     * @return Answers for the response
     */
    public List<String> getAnswers()
    {
        return answers;
    }

    /**
     * Sets the answer list for the response
     *
     * @param answers Answers for the response
     */
    public void setAnswers(List<String> answers)
    {
        this.answers = answers;
    }

    
    public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
   
    /**
     * Get's the reason for editing the answer
     *
     * @return The reason for editing the answer
     */
    public String getEditReason()
    {
        return editReason;
    }

    /**
     * Set's the reason for editing the answer
     *
     * @param editReason The reason for editing the answer
     */
    public void setEditReason(String editReason)
    {
        this.editReason = editReason;
    }

    /**
     * Gets the Edited By user ID
     *
     * @return The edited by user ID
     */
    public int getEditedBy()
    {
        return editedBy;
    }

    /**
     * Sets the edited by user ID
     *
     * @param editedBy The edited by user ID
     */
    public void setEditedBy(int editedBy)
    {
        this.editedBy = editedBy;
    }

    /**
     * Gets the edited date
     *
     * @return The edited date
     */
    public Date getEditedDate()
    {
        return editedDate;
    }

    /**
     * Gets the edited date
     *
     * @param editedDate The edited date
     */
    public void setEditedDate(Date editedDate)
    {
        this.editedDate = editedDate;
    }

    /**
     * Gets the edited answers
     *
     * @return The list of edited answers
     */
    public List<String> getEditAnswers()
    {
        return editAnswers;
    }

    /**
     * Sets the list of edited answers
     *
     * @param editAnswers The list of edited answers
     */
    public void setEditAnswers(List<String> editAnswers)
    {
        this.editAnswers = editAnswers;
    }

    /**
     * Gets the version response list for the response for edit answers
     *
     * @return List version response list for the response for edit answers
     */
    public List<Response> getVersionResponses()
    {
        return versionResponses;
    }

    /**
     * Sets the version response list for the response for edit answers
     *
     * @param versionResponses responses for each version for edit answers
     */
    public void setVersionResponses(List<Response> versionResponses)
    {
        this.versionResponses = versionResponses;
    }

    /**
     * Gets the isFlag flag.
     *
     * @return boolean the display flag with an arrow.
     */
    public boolean getIsFlag()
    {
        return isFlag;
    }

    /**
     * Sets the flag for display in the jsp page with an arrow.
     *
     * @param isFlag the flag to be set
     */
    public void setIsFlag(boolean isFlag)
    {
        this.isFlag = isFlag;
    }

    
    
    



	public void clone (Response r) {
        AdministeredForm af = new AdministeredForm();
        af.cloneWithId(r.getAdministeredForm());
        this.setAdministeredForm(af);
        if(r.getAdministeredForm().getForm().getProtocol() != null){
        	this.getAdministeredForm().getForm().setProtocol(r.getAdministeredForm().getForm().getProtocol());
        }
        
        Question q = new Question();
        q.clone(r.getQuestion());
        this.setQuestion(q);

        List ans = new ArrayList();
        ans.addAll(r.getAnswers());
        this.setAnswers(ans);
        
        List submitAns = new ArrayList();
        submitAns.addAll(r.getSubmitAnswers());
        this.setSubmitAnswers(submitAns);

        if (r.getEditReason() != null) {
            this.setEditReason(new String(r.getEditReason()));
        }
        this.setEditedBy(r.getEditedBy());
        if (r.getEditedDate() != null) {
            this.setEditedDate((Date)r.getEditedDate().clone()) ;
        }

        if (r.getEditAnswers() != null) {
            List edAns = new ArrayList();
            edAns.addAll(r.getEditAnswers());
            this.setEditAnswers(edAns);
        }
        
        if(r.isAnswerIncludesOtherPleaseSpecify()) {
        	this.setAnswerIncludesOtherPleaseSpecify(true);
        }

    }

    /**
     * Determines if an object is equal to the current Response Object.
     * Equal is based on if the first name and last name are equal.
     *
     * @param   o The object to determine if it is equal to the current Response
     * @return  True if the object is equal to the Response.
     *          False if the object is not equal to the Response.
     */
    public boolean equals(Object o)
    {
        if(question.getFormQuestionAttributes() != null &&
           question.getFormQuestionAttributes().isCalculatedQuestion())
        {
            return true; // do not compare results for calculated questions
            // if all children are the same, then the calculated result will be the same
            // after the calculation is updated.
        }

        if(this == o)
        {
            return true;
        }

        if(!(o instanceof Response))
        {
            return false;
        }

        final Response response = (Response) o;

        boolean found = false;

        if(this.answers != null && response.getAnswers() != null)
        {
            if(this.answers.size() != response.getAnswers().size())
            {
                return false;
            }
        }

        String thisAnswer;
        String oAnswer;
        for(Iterator outer = this.answers.iterator(); outer.hasNext();)
        {
            thisAnswer = (String) outer.next();
            found = false;
            for(Iterator inner = response.getAnswers().iterator(); inner.hasNext();)
            {
                oAnswer = (String) inner.next();
                if(thisAnswer.equalsIgnoreCase(oAnswer))
                {
                    found = true;
                }
            }

            if(!found)
            {
                return false;
            }
        }

        return true;
    }

	/**
	 * This method allows the transformation of a Response into an XML Document.
	 * If no implementation is available at this time, an
	 * UnsupportedOperationException will be thrown.
	 *
	 * @return XML Document
	 * @throws TransformationException
	 *             is thrown if there is an error during the XML transformation.
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
            
            for (Iterator<String> it = this.answers.iterator(); it.hasNext();)
            {
                String answer = it.next();
                
                if (!Utils.isBlank(answer))
                {
                    answersNode.appendChild(document.createTextNode(answer));
                }
            }
            
            root.appendChild(answersNode);

            return document;
        }
        catch(Exception ex)
        {
            throw new TransformationException("Unable to transform object " + this.getClass().getName()
                                                    + " with id = " + this.getId());
        }
    }

//	public List<String> getDeComments() {
//		return deComments;
//	}
//
//	public void setDeComments(List<String> deComments) {
//		this.deComments = deComments;
//	}
}
