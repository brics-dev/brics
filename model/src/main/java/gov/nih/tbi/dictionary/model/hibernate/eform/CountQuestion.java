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
@Table(name = "COUNT_QUESTION")
@XmlRootElement(name = "CountQuestions")
@XmlAccessorType(XmlAccessType.FIELD)
public class CountQuestion implements Serializable {

	@EmbeddedId
	@XmlElement(name ="CountQuestionReference")
	private CountQuestionPk countQuestionCompositePk;
	
	
	public CountQuestionPk getCountQuestionCompositePk() {
		return countQuestionCompositePk;
	}

	public void setCountQuestionCompositePk(CountQuestionPk countQuestionCompositePk) {
		this.countQuestionCompositePk = countQuestionCompositePk;
	}
	
	public CountQuestion(){}
	
	public CountQuestion(Section section, Question question, Section countSection, Question countQuestion){
		this.setCountQuestionCompositePk(new CountQuestionPk(section, question, countSection, countQuestion));
	}
	
	public CountQuestion(Section countSection, Question countQuestion){
		this.setCountQuestionCompositePk(new CountQuestionPk(countSection, countQuestion));
	}
	
	public CountQuestion(CountQuestionPk countQuestionPk){
		this.setCountQuestionCompositePk(countQuestionPk);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((countQuestionCompositePk == null) ? 0 : countQuestionCompositePk.hashCode());
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
		CountQuestion other = (CountQuestion) obj;
		if (countQuestionCompositePk == null) {
			if (other.countQuestionCompositePk != null)
				return false;
		} else if (!countQuestionCompositePk.equals(other.countQuestionCompositePk))
			return false;
		return true;
	}
	
	
}
