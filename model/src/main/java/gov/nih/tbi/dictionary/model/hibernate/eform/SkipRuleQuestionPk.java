package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.Serializable;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name="SkipRuleQuestion")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkipRuleQuestionPk implements Serializable {

	private static final long serialVersionUID = -6365185044705098575L;

	@XmlTransient
	@ManyToOne(optional = false)
	@JoinColumn(name = "QUESTION_ID")
	private Question question;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "SKIP_QUESTION_ID")
	@XmlIDREF
	private Question skipRuleQuestion;
	
	@XmlTransient
	@ManyToOne(optional = false)
	@JoinColumn(name = "SECTION_ID")
	private Section section;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "SKIP_SECTION_ID")
	@XmlIDREF
	private Section skipRuleSection;
	
	public SkipRuleQuestionPk(){}
	
	public SkipRuleQuestionPk(Section section, Question question, Section skipRuleSection, Question skipRuleQuestion){
		this.setQuestion(question);
		this.setSection(section);
		this.setSkipRuleQuestion(skipRuleQuestion);
		this.setSkipRuleSection(skipRuleSection);
	}
	
	public SkipRuleQuestionPk(Section skipRuleSection, Question skipRuleQuestion){
		this.setSkipRuleQuestion(skipRuleQuestion);
		this.setSkipRuleSection(skipRuleSection);
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Question getSkipRuleQuestion() {
		return skipRuleQuestion;
	}

	public void setSkipRuleQuestion(Question skipRuleQuestion) {
		this.skipRuleQuestion = skipRuleQuestion;
	}

	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	public Section getSkipRuleSection() {
		return skipRuleSection;
	}

	public void setSkipRuleSection(Section skipRuleSection) {
		this.skipRuleSection = skipRuleSection;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		/*result = prime * result + ((question == null) ? 0 : question.hashCode());
		result = prime * result + ((section == null) ? 0 : section.hashCode());*/
		result = prime * result + ((skipRuleQuestion == null) ? 0 : skipRuleQuestion.hashCode());
		result = prime * result + ((skipRuleSection == null) ? 0 : skipRuleSection.hashCode());
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
		SkipRuleQuestionPk other = (SkipRuleQuestionPk) obj;
		/*if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		if (section == null) {
			if (other.section != null)
				return false;
		} else if (!section.equals(other.section))
			return false;*/
		if (skipRuleQuestion == null) {
			if (other.skipRuleQuestion != null)
				return false;
		} else if (!skipRuleQuestion.equals(other.skipRuleQuestion))
			return false;
		if (skipRuleSection == null) {
			if (other.skipRuleSection != null)
				return false;
		} else if (!skipRuleSection.equals(other.skipRuleSection))
			return false;
		return true;
	}
	
}
