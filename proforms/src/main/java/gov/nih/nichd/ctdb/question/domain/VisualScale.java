package gov.nih.nichd.ctdb.question.domain;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Feb 6, 2009
 * Time: 10:26:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class VisualScale extends Question implements Cloneable {

    private static final long serialVersionUID = 3347866067099639736L;
    
	private int rangeStart;
    private int rangeEnd;
    private int width;
    private String rightText;
    private String leftText;
    private String centerText;
    private boolean showHandle;

    /**
     * Default constructor.
     */
    public VisualScale() {
    	super();
    	this.formQuestionAttributes.setInstanceType(InstanceType.VISUAL_SCALE_QUESTION);
    	this.type = QuestionType.VISUAL_SCALE;
    }

    public int getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(int rangeStart) {
        this.rangeStart = rangeStart;
    }

    public int getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(int rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getRightText() {
        return rightText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    public String getLeftText() {
        return leftText;
    }

    public void setLeftText(String leftText) {
        this.leftText = leftText;
    }

    public String getCenterText() {
        return centerText;
    }

    public void setCenterText(String centerText) {
        this.centerText = centerText;
    }

    public boolean isShowHandle() {
        return showHandle;
    }

    public void setShowHandle(boolean showHandle) {
        this.showHandle = showHandle;
    }

    public void clone (VisualScale question) {
        super.clone(question);
        this.rangeStart = question.getRangeStart();
        this.rangeEnd = question.getRangeEnd();
        this.width = question.getWidth();
        this.leftText = question.getLeftText();
        this.rightText = question.getRightText();
        this.centerText = question.getCenterText();
        this.showHandle = question.isShowHandle();
                                                                          
    }

    public Document toXML(int sectionId, String bgColor,int floorColSpanTD,int widthTD) throws TransformationException {
        Document document = super.newDocument();
        Element root = super.initXML(document, "question");

        root.setAttribute("instanceType", this.getInstanceType().getDispValue());
        root.setAttribute("required", Boolean.toString(this.getFormQuestionAttributes().isRequired()));
        root.setAttribute("type", this.getType().getDispValue());
        root.setAttribute("hasSkipRule", Boolean.toString(this.formQuestionAttributes.hasSkipRule()));
        root.setAttribute("displayText", Boolean.toString(this.isTextDisplayed()));
        if (this.formQuestionAttributes.hasSkipRule()) {
            root.setAttribute("skipOperator", this.formQuestionAttributes.getSkipRuleOperatorType().getDispValue());
            root.setAttribute("skipRule", this.formQuestionAttributes.getSkipRuleType().getDispValue());
            root.setAttribute("skipEquals", this.formQuestionAttributes.getSkipRuleEquals());
        }

        root.setAttribute("scaleMin", Integer.toString(this.getRangeStart()));
        root.setAttribute("scaleMax", Integer.toString(this.getRangeEnd()));
        root.setAttribute("width", Integer.toString(this.getWidth()));
        root.setAttribute("leftText", this.getLeftText());
        root.setAttribute("rightText", this.getRightText());
        root.setAttribute("centerText", this.getCenterText());
        root.setAttribute("showHandle", Boolean.toString(this.showHandle).toLowerCase());


        Element nameNode = document.createElement("name");
        nameNode.appendChild(document.createTextNode(this.getName()));
        root.appendChild(nameNode);
        //bg-color
        Element bgColorNode = document.createElement("bgColor");
        bgColorNode.appendChild(document.createTextNode(bgColor));
        root.appendChild(bgColorNode);
        //colSpan for question td
        root.setAttribute("floorColSpanTD", String.valueOf(floorColSpanTD));
        //width of question td
        root.setAttribute("widthTD", String.valueOf(widthTD));
        
        //descriptionUp
        Element descriptionUpNode = document.createElement("descriptionUp");
        String descriptionUpString = this.getDescriptionUp()==null?"":this.getDescriptionUp();
        descriptionUpNode.appendChild(document.createTextNode(descriptionUpString));
        root.appendChild(descriptionUpNode);
        if(!descriptionUpString.equals("")){
        	root.setAttribute("upDescription", "true");        	
        }else{
        	root.setAttribute("upDescription", "false");
        }
        
        //descriptionDown
        Element descriptionDownNode = document.createElement("descriptionDown");
        String descriptionDownString = this.getDescriptionDown()==null?"":this.getDescriptionDown();
        descriptionDownNode.appendChild(document.createTextNode(descriptionDownString ));
        root.appendChild(descriptionDownNode);
        if(!descriptionDownString.equals("")){
        	root.setAttribute("downDescription", "true");        	
        }else{
        	root.setAttribute("downDescription", "false");
        }
        // 
        
        
        Element textNode = document.createElement("text");

         if (StringUtils.isNotEmpty(this.getFormQuestionAttributes().getHtmlText()))
        {
            textNode.appendChild(document.createTextNode(this.getFormQuestionAttributes().getHtmlText()));
        }
        else
        {
            textNode.appendChild(document.createTextNode(this.getText()));
        }
       // textNode.appendChild(document.createTextNode(this.getText()));
        root.appendChild(textNode);

        if (this.getDefaultValue() != null) {
            Element defaultValueNode = document.createElement("defaultValue");
            defaultValueNode.appendChild(document.createTextNode(this.getDefaultValue()));
            root.appendChild(defaultValueNode);
        } else {
            Element defaultValueNode = document.createElement("defaultValue");
            defaultValueNode.appendChild(document.createTextNode(""));
            root.appendChild(defaultValueNode);
        }

        Document qAttrsDom = this.getFormQuestionAttributes().toXML();
        root.appendChild(document.importNode(qAttrsDom.getDocumentElement(), true));

        if (this.getImages() != null && !this.getImages().isEmpty()) {
            Element imagesNode = document.createElement("images");
            Element fileNameNode = null;
            for (Iterator<String> imageIterator = this.getImages().iterator(); imageIterator.hasNext();) {
                String imageFileName = imageIterator.next();
                fileNameNode = document.createElement("filename");
                fileNameNode.appendChild(document.createTextNode(imageFileName));
                imagesNode.appendChild(fileNameNode);
            }
            root.appendChild(imagesNode);
        }

        if (this.getFormQuestionAttributes().getQuestionsToSkip() != null && !this.getFormQuestionAttributes().getQuestionsToSkip().isEmpty()) {
            Element questionToSkipNode = document.createElement("questionsToSkip");
            StringBuffer qIds = new StringBuffer(20);
            for (Iterator<Question> questionIterator = this.getFormQuestionAttributes().getQuestionsToSkip().iterator(); questionIterator.hasNext();) {
                Question skipQuestion = questionIterator.next();
                
                qIds.append("'S_"+Integer.toString(skipQuestion.getSectionId())+"Q_" + Integer.toString(skipQuestion.getId()) + "'");
                
                if (questionIterator.hasNext()) {
                    qIds.append(",");
                }
            }
            questionToSkipNode.appendChild(document.createTextNode(qIds.toString()));
            root.appendChild(questionToSkipNode);
        }
        // added by Ching Hneg
        Element questionSectionNode = document.createElement("questionSectionNode"); 
        questionSectionNode.appendChild(document.createTextNode(String.valueOf(sectionId)));
        root.appendChild(questionSectionNode);
        return document;
    }
}
