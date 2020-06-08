package gov.nih.nichd.ctdb.form.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.HtmlAttributes;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.InstanceType;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.SkipRuleOperatorType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleType;

/**
 * Created by IntelliJ IDEA.
 * User: 513320
 * Date: Aug 30, 2004
 * Time: 2:25:40 PM
 * To change this template use Options | File Templates.
 */
public class FormQuestionAttributes extends CtdbDomainObject implements Serializable {

    private static final long serialVersionUID = 793342563155261547L;
	
	private boolean required = false;
    private HtmlAttributes htmlAttributes = new HtmlAttributes();
    private boolean skipRule = false;
    private SkipRuleType skipRuleType;
    private String skipRuleEquals;
    private SkipRuleOperatorType skipRuleOperatorType;
    private List<Question> questionsToSkip = new ArrayList<Question>();
    private List<String> questionIdsToSkip = new ArrayList<String>();

    private InstanceType instanceType;
    private boolean calculatedQuestion = false;
    private boolean countQuestion = false;

    private String rangeOperator;
    private String rangeValue1;
    private String rangeValue2;

    private int sectionId = Integer.MIN_VALUE;

    private AnswerType answerType;
    private int minCharacters = 0;
    private int maxCharacters = 4000;
    
    private String label;
    private int questionId = Integer.MIN_VALUE;
    private Version questionVersion;

    private boolean horizontalDisplay = false;
    private boolean horizDisplayBreak = true;
    private int textareaHeight = 60;
    private int textareaWidth = 100;
    private int textboxLength = 20;

    private EmailTrigger emailTrigger = new EmailTrigger();
    private boolean deleteTrigger = false;
    private boolean dataSpring = false;

    private String htmlText;
    
    private String dataElementName = "none";
    private String repeatableGroupName = "none";

    
    private boolean prepopulation = false;
    private String prepopulationValue = "";
    
    
    private int decimalPrecision = -1;
    
    
    private boolean hasUnitConversionFactor = false;
    private String unitConversionFactor = "";
    
    private boolean showText = true;
    private int tableHeaderType = 0;

    public boolean isShowText() {
		return showText;
	}

	public void setShowText(boolean showText) {
		this.showText = showText;
	}

	public int getTableHeaderType() {
		return tableHeaderType;
	}

	public void setTableHeaderType(int tableHeaderType) {
		this.tableHeaderType = tableHeaderType;
	}

	public boolean isHasUnitConversionFactor() {
		return hasUnitConversionFactor;
	}

	public void setHasUnitConversionFactor(boolean hasUnitConversionFactor) {
		this.hasUnitConversionFactor = hasUnitConversionFactor;
	}

	public String getUnitConversionFactor() {
		return unitConversionFactor;
	}

	public void setUnitConversionFactor(String unitConversionFactor) {
		this.unitConversionFactor = unitConversionFactor;
	}

	public int getDecimalPrecision() {
		return decimalPrecision;
	}

	public void setDecimalPrecision(int decimalPrecision) {
		this.decimalPrecision = decimalPrecision;
	}

	public boolean isPrepopulation() {
		return prepopulation;
	}

	public void setPrepopulation(boolean prepopulation) {
		this.prepopulation = prepopulation;
	}

	public String getPrepopulationValue() {
		return prepopulationValue;
	}

	public void setPrepopulationValue(String prepopulationValue) {
		this.prepopulationValue = prepopulationValue;
	}

	public boolean isRequired()
    {
        return required;
    }

    /**
     * Set the required property for this question.
     *
     * @param required The required property of this question
     */
    public void setRequired(boolean required)
    {
        this.required = required;
    }

    /**
     * Gets the atttibutes of this question.
     *
     * @return The HTML attributes object of the question
     */
    public HtmlAttributes getHtmlAttributes()
    {
        return htmlAttributes;
    }

    /**
     * Set the question's display attributes.
     *
     * @param htmlAttributes The HTML attributes of the question
     */
    public void setHtmlAttributes(HtmlAttributes htmlAttributes)
    {
        this.htmlAttributes = htmlAttributes;
    }

    /**
     * Get the skipRule of this question.
     *
     * @return The Skip Rule of this question
     */
    public boolean hasSkipRule()
    {
        return skipRule;
    }

    /**
     * Set the hasSkipRule flag of this question.
     *
     * @param skipRule Skip Rule flag value
     */
    public void setHasSkipRule(boolean skipRule)
    {
        this.skipRule = skipRule;
    }

    /**
     * Get the skipRuleType for this question.
     *
     * @return The skipRuleType of this question
     */
    public SkipRuleType getSkipRuleType()
    {
        return skipRuleType;
    }

    /**
     * Set the skipRuleType for this question.
     *
     * @param skipRuleType SkipRuleType for this question
     */
    public void setSkipRuleType(SkipRuleType skipRuleType)
    {
        this.skipRuleType = skipRuleType;
    }

    /**
     * Get question's skipRuleEquals.
     *
     * @return The skipRuleEquals of this question
     */
    public String getSkipRuleEquals()
    {
        return skipRuleEquals;
    }

    /**
     * Set the skipRuleEquals of this question.
     *
     * @param skipRuleEquals The skipRuleEquals to be set to
     */
    public void setSkipRuleEquals(String skipRuleEquals)
    {
        this.skipRuleEquals = skipRuleEquals;
    }

    /**
     * Get question's skipRuleOperatorType.
     *
     * @return The SkipRuleOperatorType of this question
     */
    public SkipRuleOperatorType getSkipRuleOperatorType()
    {
        return skipRuleOperatorType;
    }

    /**
     * Set the skipRuleOperatorType of this question.
     *
     * @param skipRuleOperatorType The SkipRuleOperatorType to be set to
     */
    public void setSkipRuleOperatorType(SkipRuleOperatorType skipRuleOperatorType)
    {
        this.skipRuleOperatorType = skipRuleOperatorType;
    }

    /**
     * Get the list of questions to skip.
     *
     * @return The list of questions to skip
     */
    public List<Question> getQuestionsToSkip()
    {
        return questionsToSkip;
    }

    /**
     * Set the questions to skip.
     *
     * @param questionsToSkip The list of questions to skip
     */
    public void setQuestionsToSkip(List<Question> questionsToSkip)
    {
        this.questionsToSkip = questionsToSkip;
    }

    
    
    
    
    
    
    public List<String> getQuestionIdsToSkip() {
		return questionIdsToSkip;
	}

	public void setQuestionIdsToSkip(List<String> questionIdsToSkip) {
		this.questionIdsToSkip = questionIdsToSkip;
	}

	/**
     * Get the InstanceType of the question.
     *
     * @return The instanceType for the question
     */
    public InstanceType getInstanceType()
    {
        return instanceType;
    }

    /**
     * Set the instanceType of this question.
     *
     * @param instanceType The InstanceType to be set
     */
    public void setInstanceType(InstanceType instanceType)
    {
        this.instanceType = instanceType;
    }

    /** Getter for property rangeValue2.
     * @return Value of property rangeValue2.
     */
    public String getRangeValue2()
    {
        return rangeValue2;
    }

    /** Setter for property rangeValue2.
     * @param rangeValue2 New value of property rangeValue2.
     */
    public void setRangeValue2(String rangeValue2)
    {
        this.rangeValue2 = rangeValue2;
    }

    /** Getter for property rangeValue1.
     * @return Value of property rangeValue1.
     */
    public String getRangeValue1()
    {
        return rangeValue1;
    }

    /** Setter for property rangeValue1.
     * @param rangeValue1 New value of property rangeValue1.
     */
    public void setRangeValue1(String rangeValue1)
    {
        this.rangeValue1 = rangeValue1;
    }

    /** Getter for property rangeOperator.
     * @return Value of property rangeOperator.
     */
    public String getRangeOperator()
    {
        return rangeOperator;
    }

    /** Setter for property rangeOperator.
     * @param rangeOperator New value of property rangeOperator.
     */
    public void setRangeOperator(String rangeOperator)
    {
        this.rangeOperator = rangeOperator;
    }

    /**
     * Get if the question is calculated question.
     *
     * @return If the question is calculated question
     */
    public boolean isCalculatedQuestion()
    {
        return calculatedQuestion;
    }

    /**
     * Set the isCalculatedQuestion flag of this question.
     *
     * @param isCalculatedQuestion Is Calculated Question flag value
     */
    public void setIsCalculatedQuestion(boolean isCalculatedQuestion)
    {
        this.calculatedQuestion = isCalculatedQuestion;
    }


    public boolean isCountQuestion() {
		return countQuestion;
	}

	public void setIsCountQuestion(boolean isCountQuestion) {
		this.countQuestion = isCountQuestion;
	}

	/**
     * Gets the section ID.
     *
     * @return  The section ID
     */
    public int getSectionId()
    {
        return sectionId;
    }

    /**
     * Sets the section ID.
     *
     * @param  sectionId   the section ID
     */
    public void setSectionId(int sectionId)
    {
        this.sectionId = sectionId;
    }

    /**
     * Gets the question answer type
     *
     * @return The question answer type
     */
    public AnswerType getAnswerType()
    {
        return answerType;
    }

    /**
     * Sets the question answer type
     *
     * @param type The question answer type
     */
    public void setAnswerType(AnswerType type)
    {
        this.answerType = type;
    }

    /**
     * Gets the question answer minimum character property.
     *
     * @return The question answer minimum character property.
     */
    public int getMinCharacters()
    {
        return minCharacters;
    }

    /**
     * Sets the question answer minimum character property.
     *
     * @param minCharacters The question answer minimum length.
     */
    public void setMinCharacters(int minCharacters)
    {
        this.minCharacters = minCharacters;
    }

    /**
     * Gets the question answer maximum character property.
     *
     * @return The question answer maximum character property.
     */
    public int getMaxCharacters()
    {
        return maxCharacters;
    }

    /**
     * Sets the question answer maximum character property.
     *
     * @param maxCharacters The question answer maximum length.
     */
    public void setMaxCharacters(int maxCharacters)
    {
        this.maxCharacters = maxCharacters;
    }

    /**
     * Getter for property label.
     * @return Value of property label.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Setter for property label.
     * @param label New value of property label.
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * Gets the question ID.
     *
     * @return  The question ID
     */
    public int getQuestionId()
    {
        return questionId;
    }

    /**
     * Sets the question ID.
     *
     * @param  questionId   the question ID
     */
    public void setQuestionId(int questionId)
    {
        this.questionId = questionId;
    }

    /**
     * Gets the version for the question
     *
     * @return The question version
     */
    public Version getQuestionVersion()
    {
        return questionVersion;
    }

    /**
     * Sets the version for the question
     *
     * @param questionVersion The question version
     */
    public void setQuestionVersion(Version questionVersion)
    {
        this.questionVersion = questionVersion;
    }

    /**
     * Create a question attributes based upon an existing question attributes.
     *
     * @param formQuestionAttributes The question attributes to be cloned from
     */
    public void clone(FormQuestionAttributes formQuestionAttributes)
    {
        this.questionsToSkip = formQuestionAttributes.getQuestionsToSkip();
    }

    public boolean isHorizontalDisplay() {
        return horizontalDisplay;
    }

    public void setHorizontalDisplay(boolean horizontalDisplay) {
        this.horizontalDisplay = horizontalDisplay;
    }

    public int getTextareaHeight() {
        return textareaHeight;
    }

    public void setTextareaHeight(int textareaHeight) {
        this.textareaHeight = textareaHeight;
    }

    public int getTextareaWidth() {
        return textareaWidth;
    }

    public void setTextareaWidth(int textareaWidth) {
        this.textareaWidth = textareaWidth;
    }

    public int getTextboxLength() {
        return textboxLength;
    }

    public void setTextboxLength(int textboxLength) {
        this.textboxLength = textboxLength;
    }

    public boolean isHorizDisplayBreak() {
        return horizDisplayBreak;
    }

    public void setHorizDisplayBreak(boolean horizDisplayBreak) {
        this.horizDisplayBreak = horizDisplayBreak;
    }

    public EmailTrigger getEmailTrigger() {
        return emailTrigger;
    }

    public void setEmailTrigger(EmailTrigger emailTrigger) {
        this.emailTrigger = emailTrigger;
    }

    public boolean isDeleteTrigger() {
        return deleteTrigger;
    }

    public void setDeleteTrigger(boolean deleteTrigger) {
        this.deleteTrigger = deleteTrigger;
    }

    public boolean isDataSpring() {
        return dataSpring;
    }

    public void setDataSpring(boolean dataSpring) {
        this.dataSpring = dataSpring;
    }

    public String getHtmlText()
    {
        return htmlText;
    }

    public void setHtmlText(String htmlText)
    {
        this.htmlText = htmlText;
    }
    
    

    public String getDataElementName() {
		return dataElementName;
	}

	public void setDataElementName(String dataElementName) {
		this.dataElementName = dataElementName;
	}
	
	public String getRepeatableGroupName() {
		return this.repeatableGroupName;
	}

	public void setRepeatableGroupName(String repeatableGroupName) {
		this.repeatableGroupName = repeatableGroupName;
	}



	/**
     * Compare if this Object is equal to Object o through member-wise comparison.
     *
     * @param o Object the object to compare with
     * @return True if they are equal, false otherwise
     */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof FormQuestionAttributes || o instanceof CalculatedFormQuestionAttributes))
        {
            return false;
        }

        final FormQuestionAttributes questionAttrs = (FormQuestionAttributes) o;
        if (!(this.required == questionAttrs.required))
        {
            return false;
        }
        if (!this.getHtmlAttributes().getAlign().equals(questionAttrs.getHtmlAttributes().getAlign()))
        {
            return false;
        }
        if (!this.getHtmlAttributes().getvAlign().equals(questionAttrs.getHtmlAttributes().getvAlign()))
        {
            return false;
        }
        if (!this.getHtmlAttributes().getColor().equals(questionAttrs.getHtmlAttributes().getColor()))
        {
            return false;
        }
        if (!this.getHtmlAttributes().getFontFace().equals(questionAttrs.getHtmlAttributes().getFontFace()))
        {
            return false;
        }
        String fontSize = this.getHtmlAttributes().getFontSize();
        if (fontSize != null && !fontSize.equals(questionAttrs.getHtmlAttributes().getFontSize()))
        {
            return false;
        }
        if (this.getHtmlAttributes().getIndent() != questionAttrs.getHtmlAttributes().getIndent())
        {
            return false;
        }
        System.out.println("** " + questionAttrs.getAnswerType().getDispValue()); 
       System.out.println(this.getAnswerType().getDispValue());
        if (!this.getAnswerType().equals(questionAttrs.getAnswerType()))
        {
            return false;
        }
        if(this.minCharacters != Integer.MIN_VALUE && questionAttrs.getMinCharacters() != Integer.MIN_VALUE)
        {
            if(this.minCharacters != questionAttrs.getMinCharacters())
            {
                return false;
            }
        }
        else if(this.minCharacters == Integer.MIN_VALUE && questionAttrs.getMinCharacters() != Integer.MIN_VALUE)
        {
            return false;
        }
        else if(this.minCharacters != Integer.MIN_VALUE && questionAttrs.getMinCharacters() == Integer.MIN_VALUE)
        {
            return false;
        }
        if(this.maxCharacters != Integer.MIN_VALUE && questionAttrs.getMaxCharacters() != Integer.MIN_VALUE)
        {
            if(this.maxCharacters != questionAttrs.getMaxCharacters())
            {
                return false;
            }
        }
        else if(this.maxCharacters == Integer.MIN_VALUE && questionAttrs.getMaxCharacters() != Integer.MIN_VALUE)
        {
            return false;
        }
        else if(this.maxCharacters != Integer.MIN_VALUE && questionAttrs.getMaxCharacters() == Integer.MIN_VALUE)
        {
            return false;
        }
        if (this.rangeOperator != null && questionAttrs.getRangeOperator() != null)
        {
            if (!this.rangeOperator.equals(questionAttrs.getRangeOperator()))
            {
                return false;
            }
        }
        if(this.rangeValue1 != null && questionAttrs.getRangeValue1() != null)
        {
            if(!this.rangeValue1.equals(questionAttrs.getRangeValue1()))
            {
                return false;
            }
        }
        else if(this.rangeValue1 == null && questionAttrs.getRangeValue1() != null)
        {
            return false;
        }
        else if(this.rangeValue1 != null && questionAttrs.getRangeValue1() == null)
        {
            return false;
        }
        if(this.rangeValue2 != null && questionAttrs.getRangeValue2() != null)
        {
            if(!this.rangeValue2.equals(questionAttrs.getRangeValue2()))
            {
                return false;
            }
        }
        else if(this.rangeValue2 == null && questionAttrs.getRangeValue2() != null)
        {
            return false;
        }
        else if(this.rangeValue2 != null && questionAttrs.getRangeValue2() == null)
        {
            return false;
        }
        if (this.skipRuleOperatorType != null)
        {
            if(!this.skipRuleOperatorType.equals(questionAttrs.getSkipRuleOperatorType()))
            {
                return false;
            }
            else
            {
                if(this.skipRuleEquals != null && questionAttrs.getSkipRuleEquals() != null)
                {
                    if (!this.skipRuleEquals.equals(questionAttrs.getSkipRuleEquals()))
                    {
                        return false;
                    }
                }
                else if (this.skipRuleEquals == null && questionAttrs.getSkipRuleEquals() != null)
                {
                    return false;
                }
                else if (this.skipRuleEquals != null && questionAttrs.getSkipRuleEquals() == null)
                {
                    return false;
                }
            }
        }
        if (this.skipRuleType != null && !this.skipRuleType.equals(questionAttrs.getSkipRuleType()))
        {
            return false;
        }
        if( this.questionsToSkip.size() != questionAttrs.getQuestionsToSkip().size())
        {
            return false;
        }
        if (this.isHorizontalDisplay() != questionAttrs.isHorizontalDisplay())
        {
            return false;
        }
        if (this.horizontalDisplay && this.horizDisplayBreak != questionAttrs.isHorizDisplayBreak()) {
            return false;
        }
        if (this.textareaWidth != questionAttrs.getTextareaWidth() || this.textareaHeight != questionAttrs.getTextareaHeight()) {
            return false;
        }
        if (this.textboxLength != questionAttrs.getTextboxLength()) {
            return false;
        }

        else
        {
            boolean isEqual = true;
            int skipQuestionsSize = this.questionsToSkip.size();
            for(int i = 0; i < skipQuestionsSize; i++)
            {
                Question question1 = (Question) this.questionsToSkip.get(i);
                boolean found = this.inSkipRuleQuestions(questionAttrs, question1, skipQuestionsSize);
                if (!found)
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

    private boolean inSkipRuleQuestions(FormQuestionAttributes questionAttrs, Question q, int skipSize)
    {
        boolean found = false;
        List questions = questionAttrs.questionsToSkip;
        for (Iterator it=questions.iterator(); it.hasNext();)
        {
            Question question = (Question)it.next();
            if (question.getId() == q.getId())
            {
                found = true;
                break;
            }
        }
        return found;
    }
    public Document toXML() throws TransformationException
    {
        Document document = super.newDocument();
        Element root = super.initXML(document, "formQuestionAttributes");
        
        root.setAttribute("htmltext", htmlText);


        Document htmlAttDom = this.htmlAttributes.toXML();
        root.appendChild(document.importNode(htmlAttDom.getDocumentElement(), true));

        root.setAttribute("required", (Boolean.toString(this.required)).toLowerCase());

        Element labelNode = document.createElement("label");
        labelNode.appendChild(document.createTextNode(this.label));
        root.appendChild(labelNode);

        Element answerTypeNode = document.createElement("answerType");
        answerTypeNode.appendChild(document.createTextNode(this.answerType.getDispValue()));
        root.appendChild(answerTypeNode);

        Element minNode = document.createElement("minCharacters");
        minNode.appendChild(document.createTextNode(this.minCharacters + ""));
        root.appendChild(minNode);

        Element maxNode = document.createElement("maxCharacters");
        maxNode.appendChild(document.createTextNode(this.maxCharacters + ""));
        root.appendChild(maxNode);

        Element rangeOperatorNode = document.createElement("rangeOperator");
        rangeOperatorNode.appendChild(document.createTextNode(this.rangeOperator));
        root.appendChild(rangeOperatorNode);

        Element rangeValue1Node = document.createElement("rangeValue1");
        rangeValue1Node.appendChild(document.createTextNode(this.rangeValue1));
        root.appendChild(rangeValue1Node);

        Element rangeValue2Node = document.createElement("rangeValue2");
        rangeValue2Node.appendChild(document.createTextNode(this.rangeValue2));
        root.appendChild(rangeValue2Node);
        
        Element horizontalDisplayNode = document.createElement ("horizontalDisplay");
        horizontalDisplayNode.appendChild(document.createTextNode(Boolean.toString(this.horizontalDisplay)));
        horizontalDisplayNode.setAttribute("horizDisplayBreak", Boolean.toString(this.horizDisplayBreak));
        root.appendChild(horizontalDisplayNode);



        Element textareaRows  = document.createElement ("textareaHeight");
        textareaRows.appendChild(document.createTextNode(Integer.toString(this.textareaHeight)));
        root.appendChild(textareaRows);

        Element textareaCols = document.createElement ("textareaWidth");
        textareaCols.appendChild(document.createTextNode(Integer.toString(this.textareaWidth)));
        root.appendChild(textareaCols);
        
        // added by Ching-Heng
        root.setAttribute("dataElementName", this.dataElementName);
        
        // determine the question table class name based on tableGroupId and tableHeaderType
        //root.setAttribute("tableHeaderType", Integer.toString(this.tableHeaderType));
       root.setAttribute("tableHeaderClassName", this.getTableClassName());
       root.setAttribute("showTextClassName", (this.showText) ? "showText" : "hideText");

       //  Element textboxSize = document.createElement ("textboxSize");
        //textboxSize.appendChild(document.createTextNode(Integer.toString(this.textboxLength)));
       // root.appendChild(textboxSize);
        root.setAttribute("textboxSize", Integer.toString(this.textboxLength));
        root.setAttribute("hasSkipRule", Boolean.toString(this.hasSkipRule()));

        if(this.hasSkipRule())
        {
            root.setAttribute("skipOperator", this.getSkipRuleOperatorType().getDispValue());
            root.setAttribute("skipRule", this.getSkipRuleType().getDispValue());
            root.setAttribute("skipEquals", this.getSkipRuleEquals());
        }

        if(this.getQuestionsToSkip() != null && !this.getQuestionsToSkip().isEmpty())
        {
            Element questionToSkipNode = document.createElement("questionsToSkip");
            StringBuffer qIds = new StringBuffer(20);
            for(Iterator questionIterator = this.getQuestionsToSkip().iterator(); questionIterator.hasNext();)
            {
                Question skipQuestion = (Question) questionIterator.next();
                //qIds.append("'Q_" + Integer.toString(skipQuestion.getId()) + "'");
                //qIds.append("'Q_" + Integer.toString(skipQuestion.getId()) + "'");
                qIds.append("'S_"+Integer.toString(skipQuestion.getSkipSectionId())+"_Q_" + Integer.toString(skipQuestion.getId()) + "'");            
                if(questionIterator.hasNext())
                {
                    qIds.append(",");
                }
            }
            questionToSkipNode.appendChild(document.createTextNode(qIds.toString()));
            root.appendChild(questionToSkipNode);
        }
        return document;
    }
    
	protected String getTableClassName() {
		String className = "noHeader";
		int tableHeaderType = this.tableHeaderType;
		if (tableHeaderType == 1) {
			className = "rowHeader";
		}
		else if (tableHeaderType == 2) {
			className = "columnHeader";
		}
		else if (tableHeaderType == 3) {
			className = "tableHeader";
		}
		return className;
	}
}
