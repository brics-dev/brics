package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "SECTION_QUESTION")
@XmlRootElement(name = "SectionQuestion")
@XmlAccessorType(XmlAccessType.FIELD)
public class SectionQuestion implements Serializable, Comparable<SectionQuestion>{

	private static final long serialVersionUID = -5572530150957782232L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SECTION_QUESTION_SEQ")
	@SequenceGenerator(name = "SECTION_QUESTION_SEQ", sequenceName = "SECTION_QUESTION_SEQ", allocationSize = 1)
	@XmlTransient
	private Long id;

	@Column(name = "QUESTION_ORDER")
	private Integer questionOrder;

	@Column(name = "SUPPRESS_FLAG")
	@XmlTransient
	private Boolean suppressFlag;

	@Column(name = "QUESTIONORDER_COL")
	private Integer questionOrderColumn;
	
	@Column(name = "CALCULATION")
	private String calculation;
	
	@Column(name = "COUNT_FORMULA")
	private String countFormula;
	
	@ManyToOne(targetEntity = Question.class, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "QUESTION_ID")
	@XmlElement(name ="Question")
	private Question question;
	
	@XmlTransient
	@ManyToOne(targetEntity = Section.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "SECTION_ID")
	private Section section;
	
	@OneToMany(cascade = CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinColumn(name = "section_question_id", nullable = true)
	@XmlElementWrapper(name = "CalculatedQuestionSet")
	@XmlElement(name ="CalculationQuestion")
	private List<CalculationQuestion> calculatedQuestion;
	
	@OneToMany(cascade = CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinColumn(name = "section_question_id", nullable = true)
	@XmlElementWrapper(name = "SkipRuleQuestionSet")
	@XmlElement(name ="SkipRuleQuestion")
	private List<SkipRuleQuestion> skipRuleQuestion;
	
	
	@OneToMany(cascade = CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinColumn(name = "section_question_id", nullable = true)
	@XmlElementWrapper(name = "CountQuestionSet")
	@XmlElement(name ="CountQuestion")
	private List<CountQuestion> countQuestion;
	
	
	public SectionQuestion(){}

	public SectionQuestion(SectionQuestion sectionQuestion, Section section, Question question, String calculation, String countFormula){
		this.setId(null);
		this.setQuestionOrder(sectionQuestion.getQuestionOrder());
		this.setSuppressFlag(sectionQuestion.getSuppressFlag());
		this.setQuestionOrderColumn(sectionQuestion.getQuestionOrderColumn());
		this.setSection(section);
		this.setQuestion(question);
		this.setCalculation(calculation);
		this.setCountFormula(countFormula);
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getQuestionOrder() {
		return questionOrder;
	}

	public void setQuestionOrder(Integer questionOrder) {
		this.questionOrder = questionOrder;
	}

	public Boolean getSuppressFlag() {
		return suppressFlag;
	}

	public void setSuppressFlag(Boolean suppressFlag) {
		this.suppressFlag = suppressFlag;
	}

	public Integer getQuestionOrderColumn() {
		return questionOrderColumn;
	}

	public void setQuestionOrderColumn(Integer questionOrderColumn) {
		this.questionOrderColumn = questionOrderColumn;
	}
	
	public String getCalculation(){
		return this.calculation;
	}
	
	public void setCalculation(String calculation){
		this.calculation = calculation;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	public String getCountFormula() {
		return countFormula;
	}

	public void setCountFormula(String countFormula) {
		this.countFormula = countFormula;
	}

	public List<CalculationQuestion> getCalculatedQuestion() {
		return calculatedQuestion;
	}

	public void setCalculatedQuestion(List<CalculationQuestion> calculatedQuestion) {
		this.calculatedQuestion = calculatedQuestion;
	}
	
	public void addCalculatedQuestion(CalculationQuestion calculatedQuestion){
		if(this.calculatedQuestion == null){
			List<CalculationQuestion> newCalculationQuestion = new ArrayList<CalculationQuestion>();
			setCalculatedQuestion(newCalculationQuestion);
		}
		this.calculatedQuestion.add(calculatedQuestion);
	}
	
	
	public List<CountQuestion> getCountQuestion() {
		return countQuestion;
	}

	public void setCountQuestion(List<CountQuestion> countQuestion) {
		this.countQuestion = countQuestion;
	}
	
	public void addCountQuestion(CountQuestion countQuestion){
		if(this.countQuestion == null){
			List<CountQuestion> newCountQuestion = new ArrayList<CountQuestion>();
			setCountQuestion(newCountQuestion);
		}
		this.countQuestion.add(countQuestion);
	}

	public List<SkipRuleQuestion> getSkipRuleQuestion() {
		return skipRuleQuestion;
	}

	public void setSkipRuleQuestion(List<SkipRuleQuestion> skipRuleQuestion) {
		this.skipRuleQuestion = skipRuleQuestion;
	}
	
	public void addSkipRuleQuestion(SkipRuleQuestion skipRuleQuestion){
		if(this.skipRuleQuestion == null){
			List<SkipRuleQuestion> newSkipRuleQuestion = new ArrayList<SkipRuleQuestion>();
			setSkipRuleQuestion(newSkipRuleQuestion);
		}
		this.skipRuleQuestion.add(skipRuleQuestion);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		/*result = prime * result + ((calculatedQuestion == null) ? 0 : calculatedQuestion.hashCode());*/
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((question == null) ? 0 : question.hashCode());
		result = prime * result + ((questionOrder == null) ? 0 : questionOrder.hashCode());
		result = prime * result + ((questionOrderColumn == null) ? 0 : questionOrderColumn.hashCode());
		result = prime * result + ((section == null) ? 0 : section.hashCode());
		result = prime * result + ((calculation == null) ? 0 : calculation.hashCode());
		result = prime * result + ((suppressFlag == null) ? 0 : suppressFlag.hashCode());
		result = prime * result + ((countFormula == null) ? 0 : countFormula.hashCode());
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
		SectionQuestion other = (SectionQuestion) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		if (questionOrder == null) {
			if (other.questionOrder != null)
				return false;
		} else if (!questionOrder.equals(other.questionOrder))
			return false;
		if (questionOrderColumn == null) {
			if (other.questionOrderColumn != null)
				return false;
		} else if (!questionOrderColumn.equals(other.questionOrderColumn))
			return false;
		if (calculation == null) {
			if (other.calculation != null)
				return false;
		} else if (!calculation.equals(other.calculation))
			return false;
		if (section == null) {
			if (other.section != null)
				return false;
		} else if (!section.equals(other.section))
			return false;
		if (suppressFlag == null) {
			if (other.suppressFlag != null)
				return false;
		} else if (!suppressFlag.equals(other.suppressFlag))
			return false;
		if (countFormula == null) {
			if (other.countFormula != null)
				return false;
		} else if (!countFormula.equals(other.countFormula))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SectionQuestion [SectionQuestion Id=" + id + ", questionOrder=" + questionOrder + ", questionOrderColumn=" + questionOrderColumn + "]";
	}
	
	@Override
	public int compareTo(SectionQuestion sq) {
		return this.getQuestionOrder().compareTo(sq.getQuestionOrder());
	}
}