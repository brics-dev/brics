package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import gov.nih.tbi.commons.model.QuestionType;
import gov.nih.tbi.commons.model.WebServiceStringToLongAdapter;
import gov.nih.tbi.dictionary.model.hibernate.BtrisMapping;

@Entity
@Table(name = "QUESTION")
@XmlRootElement(name = "Question")
@XmlAccessorType(XmlAccessType.FIELD)
public class Question implements Serializable{

	private static final long serialVersionUID = -669515455860022075L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "QUESTION_SEQ")
	@SequenceGenerator(name = "QUESTION_SEQ", sequenceName = "QUESTION_SEQ", allocationSize = 1)
	@XmlID
	@XmlJavaTypeAdapter(WebServiceStringToLongAdapter.class)
	private Long id;
	
	@Column(name = "NAME")
	private String name;

	@Column(name = "TEXT")
	private String text;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "TYPE_ID")
	private QuestionType type; //needs validation

	@Column(name = "DEFAULT_VALUE")
	private String defaultValue;

	@Column(name = "UNANSWERED_VALUE")
	private String unansweredValue;

	@Column(name = "IS_CDE")
	private Boolean isCde;
	
	// added by Ching-Heng
	@Column(name = "CAT_OID")
	private String catOid;
	
	@Column(name = "FORM_ITEM_OID")
	private String formItemOid;
	
	@Column(name = "COPY_RIGHT")
	private Integer copyRight;

	@Column(name = "COPY_RIGHT_INDEX")
	@XmlTransient
	private Integer copyRightIndex;

	@Column(name = "DESCRIPTION_UP")
	private String descriptionUp;
	
	@Column(name ="DESCRIPTION_DOWN")
	private String descriptionDown;

	@Column(name = "INCLUDE_OTHER")
	private Boolean includeOther;
	
	@Column(name = "DISPLAY_PV")
	private Boolean displayPV;


	@Column(name = "HTMLTEXT")
	private String htmltext;
	
	@XmlElement(name ="QuestionAttribute")
	@OneToOne(fetch = FetchType.EAGER, orphanRemoval=true, cascade = CascadeType.ALL)
	@JoinColumn(name = "QUESTION_ATTRIBUTE_ID", nullable = true)
	private QuestionAttribute questionAttribute;
	
	@XmlElement(name ="VisualScale")
	@OneToOne(orphanRemoval=true, cascade = CascadeType.ALL)
        @JoinColumn(name = "VISUAL_SCALE_ID", nullable = true)
	private VisualScale visualScale; 
	
	@XmlElement(name = "QuestionDocument")
	@OneToMany(fetch = FetchType.EAGER, orphanRemoval=false, cascade = CascadeType.ALL)
	@JoinColumn(name = "QUESTION_ID", nullable = true, updatable = false)
	private Set<QuestionDocument> questionDocument;
	
	@XmlElement(name ="QuestionAnswerOption")
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "QUESTION_ID", nullable = true)
	private Set<QuestionAnswerOption> questionAnswerOption = new LinkedHashSet<QuestionAnswerOption>(); //needs validation
	
	@XmlElement(name = "BtrisMapping")
	@OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "BTRIS_MAPPING_ID", nullable = true)
	private BtrisMapping btrisMapping = null;

	public Question(){
	}
	
	public Question(Question question){
		
		this.setId(null);
		this.setName(question.getName());
		this.setText(question.getText());
		this.setType(question.getType());
		this.setDefaultValue(question.getDefaultValue());
		this.setUnansweredValue(question.getUnansweredValue());
		this.setIsCde(question.getIsCde());
		this.setCatOid(question.getCatOid());
		this.setFormItemOid(question.getFormItemOid());
		this.setCopyRight(question.getCopyRight());
		this.setCopyRightIndex(question.getCopyRightIndex());
		this.setDescriptionUp(question.getDescriptionUp());
		this.setDescriptionDown(question.getDescriptionDown());
		this.setIncludeOther(question.getIncludeOther());
		if(question.getDisplayPV() == null) {
			this.setDisplayPV(false);
		}else {
			this.setDisplayPV(question.getDisplayPV());
		}
		this.setHtmltext(question.getHtmltext());
		this.setQuestionAttribute(new QuestionAttribute(question.getQuestionAttribute()));
		this.setVisualScale(new VisualScale(question.getVisualScale()));
		
		Set<QuestionAnswerOption> copyQuestionAnswerOption = new HashSet<QuestionAnswerOption>();
		for(QuestionAnswerOption qao : question.getQuestionAnswerOption()){
			copyQuestionAnswerOption.add(new QuestionAnswerOption(qao));
		}
		this.setQuestionAnswerOption(copyQuestionAnswerOption);
		
		Set<QuestionDocument> copyQuestionDocument = new HashSet<QuestionDocument>();
		if(question.getQuestionDocument() != null){
		   copyQuestionDocument.addAll(question.getQuestionDocument());
		}
		this.setQuestionDocument(copyQuestionDocument);

		this.setBtrisMapping(question.getBtrisMapping());
	}
	
	public Question(Long questionId){
		setId(questionId);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public QuestionType getType() {
		return type;
	}

	public String getHtmltext() {
		return htmltext;
	}

	public void setHtmltext(String htmltext) {
		this.htmltext = htmltext;
	}

	public void setType(QuestionType type) {
		this.type = type;
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

	public Boolean getIsCde() {
		return isCde;
	}

	public void setIsCde(Boolean isCde) {
		this.isCde = isCde;
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

	public Integer getCopyRight() {
		return copyRight;
	}

	public void setCopyRight(Integer copyRight) {
		this.copyRight = copyRight;
	}

	public Integer getCopyRightIndex() {
		return copyRightIndex;
	}

	public void setCopyRightIndex(Integer copyRightIndex) {
		this.copyRightIndex = copyRightIndex;
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

	public Boolean getIncludeOther() {
		return includeOther;
	}

	public void setIncludeOther(Boolean includeOther) {
		this.includeOther = includeOther;
	}
	
	public Boolean getDisplayPV() {
		return displayPV;
	}

	public void setDisplayPV(Boolean displayPV) {
		this.displayPV = displayPV;
	}
	
	public QuestionAttribute getQuestionAttribute() {
		return questionAttribute;
	}

	public void setQuestionAttribute(QuestionAttribute questionAttribute) {
		this.questionAttribute = questionAttribute;
	}

	public VisualScale getVisualScale() {
		return visualScale;
	}

	public void setVisualScale(VisualScale visualScale) {
		this.visualScale = visualScale;
	}

	public Set<QuestionDocument> getQuestionDocument() {
		return questionDocument;
	}

	public void setQuestionDocument(Set<QuestionDocument> questionDocument) {
	    this.questionDocument = questionDocument;
	}

	public Set<QuestionAnswerOption> getQuestionAnswerOption() {
		return questionAnswerOption;
	}

	public void setQuestionAnswerOption(Set<QuestionAnswerOption> questionAnswerOption) {
		this.questionAnswerOption = questionAnswerOption;
	}
	
	public void addQuestionAnswerOption(QuestionAnswerOption questionAnswerOption){
		this.questionAnswerOption.add(questionAnswerOption);
	}

	public BtrisMapping getBtrisMapping() {
		return btrisMapping;
	}

	public void setBtrisMapping(BtrisMapping btrisMapping) {
		this.btrisMapping = btrisMapping;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((questionAttribute == null) ? 0 : questionAttribute.hashCode());
		result = prime * result + ((visualScale == null) ? 0 : visualScale.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Question other = (Question) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (questionAttribute == null) {
			if (other.questionAttribute != null)
				return false;
		} else if (!questionAttribute.equals(other.questionAttribute))
			return false;
		if (visualScale == null) {
			if (other.visualScale != null)
				return false;
		} else if (!visualScale.equals(other.visualScale))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Question [Question Id=" + id + ", name=" + name + "]";
	}

}