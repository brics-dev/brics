package gov.nih.nichd.ctdb.question.domain;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "id", "text", "descriptionUp", "descriptionDown","defaultValue", "unansweredValue", "type", "copyRight", "questionOptions","questionsGraphics","visualScaleParameters","imageMap","textBlockHtmlText", 
						"questionOrder", "questionOrderCol"})
public class QuestionExportImport {
	private int id;
	private String name;
	private String text;
	private String descriptionUp;
	private String descriptionDown;
	private String defaultValue;
	private String unansweredValue;
	private int type;
	private String textBlockHtmlText;
	private int questionOrder = 0;
	private int questionOrderCol = 0;
	
	private int copyRight;
	//This will set the question options/ answer types for select multi select radio etc
	private ArrayList<QuestionOptionsExportImport> questionOptions;
	//This is hold the list of images that will be linked to the Question
	private ArrayList<QuestionGraphic> questionsGraphics;
	//Set Visual Scale Parameters for Question Id
	private VisualScaleExportImport visualScaleParameters;
	//Set Image Map parameters for Question Id
	private ImageMapExportImport imageMap;
	
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getUnansweredValue() {
		return unansweredValue;
	}

	public void setUnansweredValue(String unansweredValue) {
		this.unansweredValue = unansweredValue;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCopyRight() {
		return copyRight;
	}

	public void setCopyRight(int copyRight) {
		this.copyRight = copyRight;
	}

	@XmlElementWrapper(name = "answersOptions")
	public ArrayList<QuestionOptionsExportImport> getQuestionOptions() {
		return questionOptions;
	}

	@XmlElement(name = "answerOption")
	public void setQuestionOptions(ArrayList<QuestionOptionsExportImport> questionOptions) {
		this.questionOptions = questionOptions;
	}

	@XmlElementWrapper(name = "questionGraphics")
	public ArrayList<QuestionGraphic> getQuestionsGraphics() {
		return questionsGraphics;
	}
	@XmlElement(name = "questionGraphic")
	public void setQuestionsGraphics(ArrayList<QuestionGraphic> questionsGraphics) {
		this.questionsGraphics = questionsGraphics;
	}
	
	public VisualScaleExportImport getVisualScaleParameters() {
		return visualScaleParameters;
	}
	@XmlElement(name = "visualScale")
	public void setVisualScaleParameters(
			VisualScaleExportImport visualScaleParameters) {
		this.visualScaleParameters = visualScaleParameters;
	}

	public ImageMapExportImport getImageMap() {
		return imageMap;
	}

	public void setImageMap(ImageMapExportImport imageMap) {
		this.imageMap = imageMap;
	}

	public String getTextBlockHtmlText() {
		return textBlockHtmlText;
	}

	public void setTextBlockHtmlText(String textBlockHtmlText) {
		this.textBlockHtmlText = textBlockHtmlText;
	}

	public int getQuestionOrder() {
		return questionOrder;
	}

	public void setQuestionOrder(int questionOrder) {
		this.questionOrder = questionOrder;
	}

	public int getQuestionOrderCol() {
		if (questionOrderCol != 0) {
			return questionOrderCol;
		}
		return 1;
	}

	public void setQuestionOrderCol(int questionOrderCol) {
		this.questionOrderCol = questionOrderCol;
	}





}
