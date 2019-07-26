package gov.nih.nichd.ctdb.question.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.nih.nichd.ctdb.common.Version;

/**
 * importQuestion DomainObject for the HL7 intergration
 *
 * @author Ching Heng Lin
 * @version 1.0
 */
public class ImportQuestion
{
	// question Info
	private int importQuestionId=Integer.MIN_VALUE;
    private String type;
    private Version latestVersion;
    private List answers = new ArrayList();
    private int sectionId;
    private String theAnswer;
    private String dataElementName;
  
	// question attribute
    private boolean required = false;
    //private long dataElementId = Integer.MIN_VALUE;
    
    /**
     * Default Constructor for the Question Domain Object
     */
    public ImportQuestion()
    {
        //default constructor
    }
    
    public String getTheAnswer() {
		return theAnswer;
	}


	public void setTheAnswer(String theAnswer) {
		this.theAnswer = theAnswer;
	}
    
    public int getImportQuestionId() {
		return importQuestionId;
	}


	public void setImportQuestionId(int importQuestionId) {
		this.importQuestionId = importQuestionId;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public boolean isRequired() {
		return required;
	}


	public void setRequired(boolean required) {
		this.required = required;
	}


	/*public long getDataElementId() {
		return dataElementId;
	}


	public void setDataElementId(long dataElementId) {
		this.dataElementId = dataElementId;
	}*/


	/**
     *  Get the latest version of the quesiton
     * @return  Version object
     */
    public Version getLatestVersion() {
        return latestVersion;
    }
    public String getDataElementName() {
		return dataElementName;
	}

	public void setDataElementName(String dataElementName) {
		this.dataElementName = dataElementName;
	}

	/**
     *  set attribute for question's latest version
     * @param latestVersion
     */
    public void setLatestVersion(Version latestVersion) {
        this.latestVersion = latestVersion;
    }
    
    public int getSectionId() {
		return sectionId;
	}
	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}
	
    public List getAnswers()
    {
        return this.answers;
    }

    public void setAnswers(List answers)
    {
        this.answers = answers;
    }


    /**
     * Compare if this Object is equal to Object o through member-wise comparison.
     *
     * @param o Object the object to compare with
     * @return True if they are equal, false otherwise
     */
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof ImportQuestion))
        {
            return false;
        }

        final ImportQuestion question = (ImportQuestion) o;

        if(!this.type.equals(question.type))
        {
            return false;
        }

        if( this.answers.size() != question.answers.size())
        {
            return false;
        }
        else
        {
            boolean isEqual = true;
            for(int i = 0; i < this.answers.size(); i++)
            {
                Answer answer1 = (Answer) this.answers.get(i);
                Answer answer2 = (Answer) question.answers.get(i);
                if( !answer1.equals(answer2) )
                {
                    isEqual = false;
                    break;
                }
            }

            if( !isEqual )
            {
                return false;
            }
        }

        return true;
    }
    
    
    public void questionToImportQuestion(Question q){
        this.importQuestionId=q.getId();
    	this.type=q.getType().getDispValue();
    	
    	List ansList = new ArrayList();
    	for(Iterator it=q.getAnswers().iterator();it.hasNext();){
    		Answer answer=(Answer)it.next();
    		ansList.add(answer.getDisplay());
    	}
        this.answers = ansList;
        this.sectionId=q.getSectionId();
        this.required = q.getFormQuestionAttributes().isRequired();
        //this.dataElementId = q.getFormQuestionAttributes().getDataElementId();
        this.theAnswer="";
        this.dataElementName = q.getFormQuestionAttributes().getDataElementName();
    }
}
