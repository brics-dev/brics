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
@XmlRootElement(name="CountQuestion")
@XmlAccessorType(XmlAccessType.FIELD)
public class CountQuestionPk implements Serializable {
	
	
	@XmlTransient
	@ManyToOne(optional = false)
	@JoinColumn(name = "QUESTION_ID")
	private Question question;
	
	@XmlIDREF
	@ManyToOne(optional = false)
	@JoinColumn(name = "COUNT_QUESTION_ID")
	private Question countQuestion;
	
	@XmlTransient
	@ManyToOne(optional = false)
	@JoinColumn(name = "SECTION_ID")
	private Section section;
	
	@XmlIDREF
	@ManyToOne(optional = false)
	@JoinColumn(name = "COUNT_SECTION_ID")
	private Section countSection;
	
	public CountQuestionPk() {}
	
	public CountQuestionPk(Section section, Question question, Section countSection, Question countQuestion){
		this.setSection(section);
		this.setQuestion(question);
		this.setCountSection(countSection);
		this.setCountQuestion(countQuestion);
	}
	
	public CountQuestionPk(Section countSection, Question countQuestion){
		this.setCountSection(countSection);
		this.setCountQuestion(countQuestion);
	}
	
	
	
	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Question getCountQuestion() {
		return countQuestion;
	}

	public void setCountQuestion(Question countQuestion) {
		this.countQuestion = countQuestion;
	}

	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	public Section getCountSection() {
		return countSection;
	}

	public void setCountSection(Section countSection) {
		this.countSection = countSection;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((countQuestion == null) ? 0 : countQuestion.hashCode());
		result = prime * result + ((countSection == null) ? 0 : countSection.hashCode());
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
		CountQuestionPk other = (CountQuestionPk) obj;
		if (countQuestion == null) {
			if (other.countQuestion != null)
				return false;
		} else if (!countQuestion.equals(other.countQuestion))
			return false;
		if (countSection == null) {
			if (other.countSection != null)
				return false;
		} else if (!countSection.equals(other.countSection))
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
