package gov.nih.nichd.ctdb.question.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.btris.domain.BtrisObject;
import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.form.domain.CalculatedFormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.commons.util.EformFormViewXmlUtil;

/**
 * Question DomainObject for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class Question extends CtdbDomainObject implements Comparable<Question>
{
	private static final long serialVersionUID = -8118559881991376370L;
	
	protected String name;
	protected QuestionType type;
	protected String text;
	protected List<String> images = null;
	protected List<Group> groupsAssociatedWith = null;
	protected boolean textDisplayed;
	protected Version latestVersion;
	protected String defaultValue;
	protected String unansweredValue;
	protected List<Answer> answers = null;
	protected int sectionId;
	protected int questionOrder;
	protected int questionOrderCol;
	protected String parentSectionName;
	protected FormQuestionAttributes formQuestionAttributes;
	protected CalculatedFormQuestionAttributes calculatedFormQuestionAttributes = new CalculatedFormQuestionAttributes();
	protected boolean hasDecimalPrecision = false;
	protected boolean hasCalDependent = false;
	protected boolean prepopulation = false;
	protected int copyfrom;
	protected String descriptionUp;
	protected String descriptionDown;  
	protected int skipSectionId;
	protected int copyRight;
	protected boolean includeOtherOption = false;
	protected boolean displayPV = false;
	protected QuestionImage imageHolder = null;
	protected String htmltext;
	protected boolean cde = false;
	protected String catOid;
	protected String formItemOid;
	protected String idText;
	protected BtrisObject btrisObject = null;
	
	public static final String EXTENSION_PDF = ".pdf";
	
	
	
	/**
	 * S_$SID_Q_$QID
	 * @return
	 */
	public String getIdText() {
		return idText;
	}

	/**
	 * S_$SID_Q_$QID
	 * @param idText
	 */
	public void setIdText(String idText) {
		this.idText = idText;
	}

	public QuestionImage getImageHolder() {
		return imageHolder;
	}
	
	public void setImageHolder(QuestionImage imageHolder) {
		this.imageHolder = imageHolder;
	}
	
	public void setHasDecimalPrecision (boolean flag) {
		this.hasDecimalPrecision = flag;
	}
	
	public boolean getHasDecimalPrecision () {
		return this.hasDecimalPrecision;
	}
	
    public boolean getHasCalDependent() {
		return hasCalDependent;
	}
    
	public void setHasCalDependent(boolean hasCalDependent) {
		this.hasCalDependent = hasCalDependent;
	}
	
	public boolean getPrepopulation() {
		return prepopulation;
	}
	
	public void setPrepopulation(boolean prepopulation) {
		this.prepopulation = prepopulation;
	}
    
	public boolean isIncludeOtherOption() {
		return includeOtherOption;
	}
	public void setIncludeOtherOption(boolean includeOtherOption) {
		this.includeOtherOption = includeOtherOption;
	}
	
	public boolean isDisplayPV() {
		return displayPV;
	}

	public void setDisplayPV(boolean displayPV) {
		this.displayPV = displayPV;
	}

	/**
     * Default Constructor for the Question Domain Object
     */
    public Question() {
        super();
        formQuestionAttributes = new FormQuestionAttributes();
        formQuestionAttributes.setInstanceType(InstanceType.QUESTION);
    }
    
    /**
     * Question constructor for setting the object's ID at creation time.
     * 
     * @param id - The ID of the new Question object.
     */
    public Question(int id) {
    	super();
        setId(id);
        formQuestionAttributes = new FormQuestionAttributes();
        formQuestionAttributes.setInstanceType(InstanceType.QUESTION);
    }

    /**
     * Gets the question name
     *
     * @return The question name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the question name.
     *
     * @param name The question name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Get the question type
     *
     * @return The question type
     */
    public QuestionType getType()
    {
        return type;
    }

    /**
     * Set the question type.
     *
     * @param type The question type
     */
    public void setType(QuestionType type)
    {
        this.type = type;
    }

    /**
     * Get the question text.
     *
     * @return The question text
     */
    public String getText()
    {
        return text;
    }

    /**
     * Set the question text
     *
     * @param text The question text
     */
    public void setText(String text)
    {
        this.text = text;
    }

    public FormQuestionAttributes getFormQuestionAttributes() {
        return formQuestionAttributes;
    }

    public void setFormQuestionAttributes(FormQuestionAttributes formQuestionAttributes) {
        this.formQuestionAttributes = formQuestionAttributes;
    }

     public CalculatedFormQuestionAttributes getCalculatedFormQuestionAttributes() {
        return calculatedFormQuestionAttributes;
    }

    public void setCalculatedFormQuestionAttributes(CalculatedFormQuestionAttributes calculatedFormQuestionAttributes) {
        this.calculatedFormQuestionAttributes = calculatedFormQuestionAttributes;
    }

    /**
        * Gets the image list for this question.
        *
        * @return The image list for this question
        */
       public List<String> getImages()
       {
           return images != null ? images : new ArrayList<String>();
       }

       /**
        * Set the image list property of this question
        *
        * @param images The image list for this question
        */
       public void setImages(List<String> images)
       {
           this.images = images;
       }

       /**
        * Get a list of groups this question belongs.
        *
        * @return The list of groups this question associated with
        */
       public List<Group> getGroupsAssociatedWith()
       {
           return groupsAssociatedWith != null ? groupsAssociatedWith : new ArrayList<Group>();
       }

       /**
        * Set the question associated groups.
        *
        * @param groupsAssociatedWith List the list of groups to be associated with this question.
        */
       public void setGroupsAssociatedWith(List<Group> groupsAssociatedWith)
       {
           this.groupsAssociatedWith = groupsAssociatedWith;
       }

        /**
         * Set the question associated groups.
         *
         * @param separator Separator for list of groups associated with this question.
         */
        public String getAssociatedGroupNameString(String separator)
        {
            StringBuffer sb = new StringBuffer(200);
            
            for ( Group g : getGroupsAssociatedWith() ) {
                sb.append(g.getName() + separator );
            }
            
            if ( sb.toString().isEmpty() ) {
                sb.append("No Association");
            }
            
            return sb.toString();
        }

    /**
     *  Should the text of the question be displayed during print / online date entry
     * @return  true / false
     */
    public boolean isTextDisplayed() {
        return textDisplayed;
    }
    
    /**
     *  set attribute defining text displayed during print / online data entry
     * @param textDisplayed
     */
    public void setTextDisplayed(boolean textDisplayed) {
        this.textDisplayed = textDisplayed;
    }

    /**
     *  Get the latest version of the question
     * @return  Version object
     */
    public Version getLatestVersion() {
        return latestVersion;
    }
    /**
     *  set attribute for question's latest version
     * @param latestVersion
     */
    public void setLatestVersion(Version latestVersion) {
        this.latestVersion = latestVersion;
    }

    /**
     * Get the default value of the question.
     *
     * @return The question default value
     */
    public String getDefaultValue()
    {
        return this.defaultValue;
    }

    /**
     * Set the question default value.
     *
     * @param defaultValue The default value for this question
     */
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public String getUnansweredValue() {
        return unansweredValue;
    }

    public int getQuestionOrder() {
		return questionOrder;
	}
    
	public void setQuestionOrder(int questionOrder) {
		this.questionOrder = questionOrder;
	}
	
	public int getQuestionOrderCol() {
		return questionOrderCol;
	}

	public void setQuestionOrderCol(int questionOrder_col) {
		this.questionOrderCol = questionOrder_col;
	}

	public void setUnansweredValue(String unansweredValue) {
        this.unansweredValue = unansweredValue;
    }

    public int getSectionId() {
		return sectionId;
	}
    
	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}
	
	public String getCatOid() {
		return catOid;
	}

	public void setCatOid(String catOid) {
		this.catOid = catOid;
	}

	public String getFormItemOid() {
		return formItemOid;
	}

	public void setFormItemOid(String formItemOid) {
		this.formItemOid = formItemOid;
	}

	/**
     * Create a question based upon an existing question.
     *
     * @param question The question to be cloned from
     */
    public void clone(Question question)
    {
        this.name = question.getName();
        this.type = question.getType();
        this.text = question.getText();
        this.images = question.getImages();
        this.groupsAssociatedWith = question.getGroupsAssociatedWith();
        this.textDisplayed = question.isTextDisplayed();
        this.latestVersion = question.getLatestVersion();
        this.defaultValue = question.getDefaultValue();
        this.answers = question.getAnswers();
        this.setVersion(question.getVersion());
        this.unansweredValue = question.getUnansweredValue();
        this.descriptionUp=question.getDescriptionUp();
        this.descriptionDown=question.getDescriptionDown();
        this.includeOtherOption=question.isIncludeOtherOption();
        this.hasDecimalPrecision=question.getHasDecimalPrecision();
        this.hasCalDependent = question.getHasCalDependent();
        this.prepopulation = question.getPrepopulation();
        this.formQuestionAttributes = question.getFormQuestionAttributes();
        this.catOid = question.getCatOid();
        this.formItemOid = question.getFormItemOid();
    }

    /**
     * Compare if this Object is equal to Object o through member-wise comparison.
     *
     * @param o Object the object to compare with
     * @return True if they are equal, false otherwise
     */
    public boolean equals(Object o)
    {
    	if (o == null)
    	{
    		return false;
    	}
    	
        if (this == o)
        {
            return true;
        }
        
        if (!(o instanceof Question))
        {
            return false;
        }

        final Question question = (Question) o;

        if(!this.name.equals(question.name))
        {
            return false;
        }
        if(!this.text.equals(question.text))
        {
            return false;
        }
        if(!this.type.equals(question.type))
        {
            return false;
        }
        if( !(this.formQuestionAttributes.isRequired() == question.formQuestionAttributes.isRequired()) )
        {
            return false;
        }

        if( this.getAnswers().size() != question.getAnswers().size())
        {
            return false;
        }
        else
        {
            boolean isEqual = true;
            
            for ( int i = 0; i < this.getAnswers().size(); i++ )
            {
                Answer answer1 = this.getAnswers().get(i);
                Answer answer2 = question.getAnswers().get(i);
                
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
    
    public Document toXML() throws TransformationException {
    	return null;
    }

	/**
	 * This method allows the transformation of a Question into an XML Document.
	 * If no implementation is available at this time, an
	 * UnsupportedOperationException will be thrown.
	 *
	 * @return XML Document
	 * @throws TransformationException
	 *             is thrown if there is an error during the XML tranformation
	 * @throws CtdbException 
	 * @throws ObjectNotFoundException 
	 * @throws DOMException 
	 */
    public Document toXML(int sectionId, String bgColor,int floorColSpanTD,int widthTD) throws TransformationException, DOMException, ObjectNotFoundException, CtdbException
    {
        Document document = super.newDocument();
        Element root = super.initXML(document, "question");
        
        if ( (formQuestionAttributes != null) && (formQuestionAttributes.isCalculatedQuestion() || formQuestionAttributes.isCountQuestion()) && (calculatedFormQuestionAttributes != null) ) {
        	root.setAttribute("type", "Calculated");
        	root.setAttribute("isCount", Boolean.toString(calculatedFormQuestionAttributes.isCount()));
	        root.setAttribute("calculation", calculatedFormQuestionAttributes.getCalculation());
	        root.setAttribute("decimalPrecision", String.valueOf(formQuestionAttributes.getDecimalPrecision()));
	        root.setAttribute("conditionalForCalc", Boolean.toString(calculatedFormQuestionAttributes.getConditionalForCalc().booleanValue()));

	        if ( calculatedFormQuestionAttributes.getAnswerType().equals(AnswerType.NUMERIC) ) {
	            root.setAttribute("answertype", "numeric");
	        }
	        else if ( calculatedFormQuestionAttributes.getAnswerType().equals(AnswerType.DATE) ) {
	            root.setAttribute("answertype", "date");
	        }
	        else if ( calculatedFormQuestionAttributes.getAnswerType().equals(AnswerType.DATETIME) ) {
	        	root.setAttribute("answertype", "datetime");
	        }
        }
        else {
        	if ( formQuestionAttributes.getAnswerType().equals(AnswerType.DATE) ) {
        		root.setAttribute("answertype", "date");
        	} else if ( formQuestionAttributes.getAnswerType().equals(AnswerType.DATETIME) ){
        		root.setAttribute("answertype", "datetime");
        	}
        	
        	root.setAttribute("type", type.getDispValue());
        }

        root.setAttribute("displayText", Boolean.toString(textDisplayed));

        Element nameNode = document.createElement("name");
        nameNode.appendChild(document.createTextNode(name));
        root.appendChild(nameNode);
        //added by Ching-Heng
        Element parentSectionName = document.createElement("parentSectionName");
        parentSectionName.appendChild(document.createTextNode(this.parentSectionName));
        root.appendChild(parentSectionName);
        
        Element catOidNode = document.createElement("catOid");
        catOidNode.appendChild(document.createTextNode(this.catOid));
        root.appendChild(catOidNode);
        
        Element deNameNode = document.createElement("deName");
        String deName = name.substring(name.indexOf("_")+1, name.length());
        deNameNode.appendChild(document.createTextNode(deName));
        root.appendChild(deNameNode);
        
        //bg-color
        Element bgColorNode = document.createElement("bgColor");
        bgColorNode.appendChild(document.createTextNode(bgColor));
        root.appendChild(bgColorNode);
        //colSpan for question td
        root.setAttribute("floorColSpanTD", String.valueOf(floorColSpanTD));
        //width of question td
        root.setAttribute("widthTD", String.valueOf(widthTD));
        
        //includeOther
        Element includeOtherNode = document.createElement("includeOther");

        if ( includeOtherOption ) {
        	includeOtherNode.appendChild(document.createTextNode("true"));
        }
        else {
        	includeOtherNode.appendChild(document.createTextNode("false"));
        }
        
        root.appendChild(includeOtherNode);
        
        // descriptionUp
        Element descriptionUpNode = document.createElement("descriptionUp");
        String descriptionUpString = descriptionUp == null ? "" : descriptionUp;
        descriptionUpNode.appendChild(document.createTextNode(descriptionUpString));
        root.appendChild(descriptionUpNode);
        
        if ( !descriptionUpString.isEmpty() ) {
        	root.setAttribute("upDescription", "true");        	
        }
        else {
        	root.setAttribute("upDescription", "false");
        }
        
        // descriptionDown
        Element descriptionDownNode = document.createElement("descriptionDown");
        String descriptionDownString = descriptionDown == null ? "" : descriptionDown;
        descriptionDownNode.appendChild(document.createTextNode(descriptionDownString ));
        root.appendChild(descriptionDownNode);
        
        if ( !descriptionDownString.isEmpty() ) {
        	root.setAttribute("downDescription", "true");        	
        }
        else {
        	root.setAttribute("downDescription", "false");
        }
        
        // htmltext for textblock        
        Element htmltextNode = document.createElement("htmltext");
        String htmltextString = htmltext == null ? "" : htmltext;
        htmltextNode.appendChild(document.createTextNode(htmltextString));
        root.appendChild(htmltextNode);
        
        Element textNode = document.createElement("text");
        textNode.appendChild(document.createTextNode(text));        
        root.appendChild(textNode);
        
        if ( getDefaultValue() != null ) {
	        Element defaultValueNode = document.createElement("defaultValue");
	        defaultValueNode.appendChild(document.createTextNode(getDefaultValue()));
	        root.appendChild(defaultValueNode);
        }
        else {
            Element defaultValueNode = document.createElement("defaultValue");
            defaultValueNode.appendChild(document.createTextNode(""));
            root.appendChild(defaultValueNode);
        }
        
        if ( type.getValue() == QuestionType.File.getValue() ) {
        	Element defaultValueNode = document.createElement("attachmentId");
            defaultValueNode.appendChild(document.createTextNode(Integer.toString(Integer.MIN_VALUE)));
            root.appendChild(defaultValueNode);
        }
        else {
        	Element defaultValueNode = document.createElement("attachmentId");
            defaultValueNode.appendChild(document.createTextNode(""));
            root.appendChild(defaultValueNode);
        }
        
        Element questionSectionNode = document.createElement("questionSectionNode"); 
        questionSectionNode.appendChild(document.createTextNode(String.valueOf(sectionId)));
        root.appendChild(questionSectionNode);
        
        // For text box field in other option
        Element otherBoxNode = document.createElement("otherBox"); 
        otherBoxNode.appendChild(document.createTextNode(""));
        root.appendChild(otherBoxNode);
        
        //Data element comment box
        Element deCommentBoxNode = document.createElement("deComment"); 
        deCommentBoxNode.appendChild(document.createTextNode(""));
        root.appendChild(deCommentBoxNode);
        
        // When it's only one option, don't show the blank option
        Element blankOptionNode = document.createElement("blankOption");
        
        if ( getAnswers().size() < 2 ) {
        	blankOptionNode.appendChild(document.createTextNode("false"));
        }
        else {
        	blankOptionNode.appendChild(document.createTextNode("true"));
        }
        
        root.appendChild(blankOptionNode);
        
        Element answersNode = document.createElement("answers");
        String scoreStr = "";
        
        for ( Answer answer : getAnswers() ){
            
        	answer.setIncludeOther(includeOtherOption); 
        	//answer.setDisplayPV(displayPV);
        	scoreStr = scoreStr +answer.getDisplay()+"|"+answer.getScore()+"|";
            Document answerDom = answer.toXML();

            answersNode.appendChild(document.importNode(answerDom.getDocumentElement(), true));
        }        
        root.appendChild(answersNode);
        
        Element scoreStrNode = document.createElement("scoreStr");
        scoreStrNode.appendChild(document.createTextNode(scoreStr));
        root.appendChild(scoreStrNode);        

        if ( (images != null) && !images.isEmpty() )
        {
			Element imagesNode = null;
			Element filesNode = null;
			Element fileNameNode = null;
			for (String fileName : images) {
				String fileExtesion = FilenameUtils.getExtension(fileName);
				if (EformFormViewXmlUtil.QUESTION_FILE_TYPES.contains(fileExtesion.toLowerCase())) {
					filesNode = document.createElement("files");
				} else {
					imagesNode = document.createElement("images");
				}
			}
            
			for (String fileName : images)
            {
				fileNameNode = document.createElement("filename");
				fileNameNode.appendChild(document.createTextNode(fileName));
				String fileExtesion = FilenameUtils.getExtension(fileName);
				if (EformFormViewXmlUtil.QUESTION_FILE_TYPES.contains(fileExtesion.toLowerCase())) {
					try {
						fileName = URLEncoder.encode(fileName, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
					String restfulUrl =
							restfulDomain + "portal/ws/public/eforms/question/"
							+ String.valueOf(this.getId())
							+ SysPropUtil.getProperty("webservice.restful.eform.graphic.url2")
							+ fileName;

					fileNameNode.setAttribute("fileLink", restfulUrl);
					filesNode.appendChild(fileNameNode);
				} else {
					imagesNode.appendChild(fileNameNode);
				}
            }
			if (imagesNode != null) {
				root.appendChild(imagesNode);
			}
			if (filesNode != null) {
				root.appendChild(filesNode);
			}
        }
        
        // BtrisMapping
		if (this.btrisObject != null) {
			root.setAttribute("hasBtrisMapping", "true");
			Element btrisMappingNode = document.createElement("btrisMapping");
			btrisMappingNode.setAttribute("btrisObservationName", this.btrisObject.getBtrisObservationName());
			btrisMappingNode.setAttribute("btrisRedCode", this.btrisObject.getBtrisRedCode());
			btrisMappingNode.setAttribute("btrisUnitOfMeasure", this.btrisObject.getBtrisUnitOfMeasure());
			btrisMappingNode.setAttribute("btrisRange", this.btrisObject.getBtrisRange());
			btrisMappingNode.setAttribute("btrisTable", this.btrisObject.getBtrisTable());
			root.appendChild(btrisMappingNode);
		} else {
			root.setAttribute("hasBtrisMapping", "false");
        }


        Document qAttrsDom = formQuestionAttributes.toXML();       
        
        root.appendChild(document.importNode(qAttrsDom.getDocumentElement(), true));

        return document;
    }

    /**
     * Get the list of answers for this question.
     *
     * @return The list of answers for this question.
     */
    public List<Answer> getAnswers()
    {
        return answers != null ? answers : new ArrayList<Answer>();
    }

    /**
     * Set the question answers.
     *
     * @param answers The list of answers for this question
     */
    public void setAnswers(List<Answer> answers)
    {
        this.answers = answers;
    }

    /**
     * Get the InstanceType of the question.
     *
     * @return The instanceType for the question
     */
    public InstanceType getInstanceType()
    {
        return this.formQuestionAttributes.getInstanceType();
    }

    public String getParentSectionName() {
        return parentSectionName;
    }

    public void setParentSectionName(String parentSectionName) {
        this.parentSectionName = parentSectionName;
    }
    
	public String getDescriptionUp() {
		return descriptionUp;
	}
	public void setDescriptionUp(String descriptionUp) {
		this.descriptionUp = descriptionUp;
	}
	public String getDescriptionDown() {
		return descriptionDown;
	}
	public void setDescriptionDown(String descriptionDown) {
		this.descriptionDown = descriptionDown;
	}   
	
	public int getCopyfrom() {
		return copyfrom;
	}
	public void setCopyfrom(int copyfrom) {
		this.copyfrom = copyfrom;
	}
    public int getSkipSectionId() {
		return skipSectionId;
	}
	public void setSkipSectionId(int skipSectionId) {
		this.skipSectionId = skipSectionId;
	}	
    public int getCopyRight() {
		return copyRight;
	}
	public void setCopyRight(int copyRight) {
		this.copyRight = copyRight;
	}
	public String getHtmltext() {
		return htmltext;
	}
	public void setHtmltext(String questiontext) {
		this.htmltext = questiontext;
	}

	/**
	 * @return the cde
	 */
	public boolean isCde() {
		return cde;
	}

	/**
	 * @param cde the cde to set
	 */
	public void setCde(boolean cde) {
		this.cde = cde;
	}

	public BtrisObject getBtrisObject() {
		return this.btrisObject;
	}

	public void setBtrisObject(BtrisObject btrisObject) {
		this.btrisObject = btrisObject;
	}

	@Override
	public int compareTo(Question arg0) {
		return(this.getQuestionOrder() - arg0.getQuestionOrder());
	}
}
