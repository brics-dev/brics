package gov.nih.nichd.ctdb.question.domain;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Answer DomainObject for the NICHD CTDB Applicatio
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class Answer extends CtdbDomainObject
{
	private static final long serialVersionUID = 5707802404473914715L;
	
	private String display = "";
	public String pvd = "";
    private String codeValue;
    private double score = Integer.MIN_VALUE;
    private String submittedValue = "";
    private AnswerType type;
    private int minCharacters = Integer.MIN_VALUE;
    private int maxCharacters = Integer.MIN_VALUE;
    private boolean selected = false;
    private boolean includeOther = false;
    private String itemResponseOid;
    private String idText;

    
    public boolean isIncludeOther() {
		return includeOther;
	}


	public void setIncludeOther(boolean includeOther) {
		this.includeOther = includeOther;
	}


	/**
     * Default Constructor for the Answer Domain Object
     */
    public Answer()
    {
        // default constructor
        super();
        //default to string
        type = AnswerType.STRING;
    }


    public Answer(int _id, String display)
      {
          setId(_id);
          setDisplay(display);


      }

    /**
     * Overloaded Constructor for the Answer Domain Object
     *
     * @param id Question Answer Value
     * @param display Question Answer Display text
     * @param type Question Answer Type
     */
    public Answer(int id, String display, AnswerType type)
    {
        super();
        this.setId(id);
        this.display = display;
        this.type = type;
    }

    /**
     * Gets the question answer display text
     *
     * @return The question answer display text
     */
    public String getDisplay()
    {
        return display;
    }

    /**
     * Sets the question answer display text.
     *
     * @param display The question answer display text
     */
    public void setDisplay(String display)
    {
        this.display = display;
    }

    /**
     * Gets the question answer code value text
     *
     * @return The question answer code value text
     */
    public String getCodeValue()
    {
        return codeValue;
    }

    /**
     * Sets the question answer code value text.
     *
     * @param codeValue The question answer code value text
     */
    public void setCodeValue(String codeValue)
    {
        this.codeValue = codeValue;
    }

    public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}


	
	public String getPvd() {
		return pvd;
	}


	public void setPvd(String pvd) {
		this.pvd = pvd;
	}

	
	public String getSubmittedValue() {
		return submittedValue;
	}


	public void setSubmittedValue(String submittedValue) {
		this.submittedValue = submittedValue;
	}


	/**
     * Gets the question answer type
     *
     * @return The question answer type
     * @deprecated moved to FormQuestionAttributes domain object
     */
    public AnswerType getType()
    {
        return type;
    }

    /**
     * Sets the question answer type
     *
     * @param type The question answer type
     * @deprecated moved to FormQuestionAttributes domain object
     */
    public void setType(AnswerType type)
    {
        this.type = type;
    }

    /**
     * Gets the question answer minimum character property.
     *
     * @return The question answer minimum character property.
     * @deprecated moved to FormQuestionAttributes domain object
     */
    public int getMinCharacters()
    {
        return minCharacters;
    }

    /**
     * Sets the question answer minimum character property.
     *
     * @param minCharacters The question answer minimum length.
     * @deprecated moved to FormQuestionAttributes domain object
     */
    public void setMinCharacters(int minCharacters)
    {
        this.minCharacters = minCharacters;
    }

    /**
     * Gets the question answer maximum character property.
     *
     * @return The question answer maximum character property.
     * @deprecated moved to FormQuestionAttributes domain object
     */
    public int getMaxCharacters()
    {
        return maxCharacters;
    }

    /**
     * Sets the question answer maximum character property.
     *
     * @param maxCharacters The question answer maximum length.
     * @deprecated moved to FormQuestionAttributes domain object
     */
    public void setMaxCharacters(int maxCharacters)
    {
        this.maxCharacters = maxCharacters;
    }

    /**
     * Determine's if this answer should be selected when viewing the form.
     *
     * @return True if the answer should be selected; false otherwise
     */
    public boolean isSelected()
    {
        return selected;
    }

    /**
     * Set's the selected value for this answer.
     *
     * @param selected True if the answer should be selected; false otherwise
     */
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

	

    public String getItemResponseOid() {
		return itemResponseOid;
	}


	public void setItemResponseOid(String itemResponseOid) {
		this.itemResponseOid = itemResponseOid;
	}


	public String getIdText() {
		return idText;
	}


	public void setIdText(String idText) {
		this.idText = idText;
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
        if(!(o instanceof Answer))
        {
            return false;
        }

        final Answer answer = (Answer) o;

        if((this.display == null && answer.display != null)
            || (this.display != null && answer.display == null)) {
            return false;
        }
        else if (this.display != null && answer.display != null && !this.display.trim().equals(answer.display.trim()))
        {
            return false;
        }

        if((this.codeValue == null && answer.codeValue != null)
            || (this.codeValue != null && answer.codeValue == null)) {
            return false;
        }
        else if (this.codeValue != null && answer.codeValue != null && !this.codeValue.trim().equals(answer.codeValue.trim()))
        {
            return false;
        }
        if(this.score != answer.score)
        {
            return false;
        }
        if(this.minCharacters != answer.minCharacters)
        {
            return false;
        }
        if(this.maxCharacters != answer.maxCharacters)
        {
            return false;
        }
        if(!this.type.equals(answer.type))
        {
            return false;
        }
        return true;
    }

	/**
	 * This method allows the transformation of a Answer into an XML Document.
	 * If no implementation is available at this time, an
	 * UnsupportedOperationException will be thrown.
	 *
	 * @return XML Document
	 * @throws TransformationException
	 *             is thrown if there is an error during the XML tranformation
	 */
	public Document toXML() throws TransformationException {
		Document document = super.newDocument();
		Element root = super.initXML(document, "answer");

		root.setAttribute("type", this.type.getDispValue());

		if (this.display != null) {
			Element displayNode = document.createElement("display");
			displayNode.appendChild(document.createTextNode(this.display));
			root.appendChild(displayNode);
			
			Element pvidNode = document.createElement("pvid");
			pvidNode.appendChild(document.createTextNode(String.valueOf(getId())));
			root.appendChild(pvidNode);
		}

		Element includeOtherNode = document.createElement("includeOther");
		
		if (includeOther && display.equals(CtdbConstants.OTHER_OPTION_DISPLAY)) {
			includeOtherNode.appendChild(document.createTextNode("true"));
		} else {
			includeOtherNode.appendChild(document.createTextNode("false"));
		}
		
		root.appendChild(includeOtherNode);

		if (this.codeValue != null) {
			Element codeValueNode = document.createElement("codeValue");
			codeValueNode.appendChild(document.createTextNode(this.codeValue));
			root.appendChild(codeValueNode);
		}
		
		if (this.score != Integer.MIN_VALUE) {
			Element scoreNode = document.createElement("score");
			scoreNode.appendChild(document.createTextNode(Double.toString(this.score)));
			root.appendChild(scoreNode);
		}

		Element selectedNode = document.createElement("selected");
		selectedNode.appendChild(document.createTextNode(Boolean.toString(this.selected)));
		root.appendChild(selectedNode);

		if (this.minCharacters != Integer.MIN_VALUE) {
			Element minNode = document.createElement("minCharacters");
			minNode.appendChild(document.createTextNode(Integer.toString(minCharacters)));
			root.appendChild(minNode);
		}

		if (this.maxCharacters != Integer.MIN_VALUE) {
			Element maxNode = document.createElement("maxCharacters");
			maxNode.appendChild(document.createTextNode(Integer.toString(maxCharacters)));
			root.appendChild(maxNode);
		}
		
		//added by Ching-heng
		Element itemResponseOidNode = document.createElement("itemResponseOid");
		itemResponseOidNode.appendChild(document.createTextNode(this.itemResponseOid));
		root.appendChild(itemResponseOidNode);

		return document;
	}
}
