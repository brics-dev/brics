package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@Embeddable
@XmlRootElement(name="CalculationQuestion")
@XmlAccessorType(XmlAccessType.FIELD)
public class CalculationQuestionPk implements Serializable {

	private static final long serialVersionUID = -7582822064824866755L;

	@XmlTransient
	@ManyToOne(optional = false)
	@JoinColumn(name = "QUESTION_ID")
	private Question question;
	
	@XmlIDREF
	@ManyToOne(optional = false)
	@JoinColumn(name = "CALCULATION_QUESTION_ID")
	private Question calculationQuestion;
	
	@XmlTransient
	@ManyToOne(optional = false)
	@JoinColumn(name = "SECTION_ID")
	private Section section;
	
	@XmlIDREF
	@ManyToOne(optional = false)
	@JoinColumn(name = "CALCULATION_SECTION_ID")
	private Section calculationSection;
	
	public CalculationQuestionPk(){}
	
	public CalculationQuestionPk(Section section, Question question, Section calculationSection, Question calculationQuestion){
		this.setSection(section);
		this.setQuestion(question);
		this.setCalculationSection(calculationSection);
		this.setCalculationQuestion(calculationQuestion);
	}
	
	public CalculationQuestionPk(Section calculationSection, Question calculationQuestion){
		this.setCalculationSection(calculationSection);
		this.setCalculationQuestion(calculationQuestion);
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Question getCalculationQuestion() {
		return calculationQuestion;
	}

	public void setCalculationQuestion(Question calculationQuestion) {
		this.calculationQuestion = calculationQuestion;
	}

	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	public Section getCalculationSection() {
		return calculationSection;
	}

	public void setCalculationSection(Section calculationSection) {
		this.calculationSection = calculationSection;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((calculationQuestion == null) ? 0 : calculationQuestion.hashCode());
		result = prime * result + ((calculationSection == null) ? 0 : calculationSection.hashCode());
		result = prime * result + ((question == null) ? 0 : question.hashCode());
		result = prime * result + ((section == null) ? 0 : section.hashCode());
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
		CalculationQuestionPk other = (CalculationQuestionPk) obj;
		if (calculationQuestion == null) {
			if (other.calculationQuestion != null)
				return false;
		} else if (!calculationQuestion.equals(other.calculationQuestion))
			return false;
		if (calculationSection == null) {
			if (other.calculationSection != null)
				return false;
		} else if (!calculationSection.equals(other.calculationSection))
			return false;
		if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		if (section == null) {
			if (other.section != null)
				return false;
		} else if (!section.equals(other.section))
			return false;
		return true;
	}
	
}
