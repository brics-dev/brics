package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "SKIP_RULE_QUESTION")
@XmlRootElement(name = "SkipRuleQuestions")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkipRuleQuestion implements Serializable{

	private static final long serialVersionUID = 1215860593870630657L;

	@EmbeddedId
	@XmlElement(name ="SkipRuleQuestionReference")
	private SkipRuleQuestionPk skipRuleQuestionCompositePk;

	public SkipRuleQuestionPk getSkipRuleQuestionCompositePk() {
		return skipRuleQuestionCompositePk;
	}

	public void setSkipRuleQuestionCompositePk(SkipRuleQuestionPk skipRuleQuestionCompositePk) {
		this.skipRuleQuestionCompositePk = skipRuleQuestionCompositePk;
	}
	
	public SkipRuleQuestion(){}
	
	public SkipRuleQuestion(Section section, Question question, Section skipRuleSection, Question skipRuleQuestion){
		SkipRuleQuestionPk skipRuleQuestionPk = new SkipRuleQuestionPk(section, question, skipRuleSection, skipRuleQuestion);
		this.skipRuleQuestionCompositePk = skipRuleQuestionPk;
	}
	
	public SkipRuleQuestion(Section skipRuleSection, Question skipRuleQuestion){
		SkipRuleQuestionPk skipRuleQuestionPk = new SkipRuleQuestionPk(skipRuleSection, skipRuleQuestion);
		this.skipRuleQuestionCompositePk = skipRuleQuestionPk;
	}
	
	public SkipRuleQuestion(SkipRuleQuestionPk skipRuleQuestionPk){
		this.skipRuleQuestionCompositePk = skipRuleQuestionPk;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((skipRuleQuestionCompositePk == null) ? 0 : skipRuleQuestionCompositePk.hashCode());
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
		SkipRuleQuestion other = (SkipRuleQuestion) obj;
		if (skipRuleQuestionCompositePk == null) {
			if (other.skipRuleQuestionCompositePk != null)
				return false;
		} else if (!skipRuleQuestionCompositePk.equals(other.skipRuleQuestionCompositePk))
			return false;
		return true;
	}

}