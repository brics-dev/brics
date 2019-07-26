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
@Table(name = "CALCULATION_QUESTION")
@XmlRootElement(name = "CalculationQuestions")
@XmlAccessorType(XmlAccessType.FIELD)
public class CalculationQuestion implements Serializable{
	
	private static final long serialVersionUID = 1215860598270630657L;

	@EmbeddedId
	@XmlElement(name ="CalculationQuestionReference")
	private CalculationQuestionPk calculationQuestionCompositePk;

	public CalculationQuestionPk getCalculationQuestionCompositePk() {
		return calculationQuestionCompositePk;
	}

	public void setCalculationQuestionCompositePk(CalculationQuestionPk calculationQuestionCompositePk) {
		this.calculationQuestionCompositePk = calculationQuestionCompositePk;
	}
	
	public CalculationQuestion(){}
	
	public CalculationQuestion(Section section, Question question, Section calculationSection, Question calculationQuestion){
		this.setCalculationQuestionCompositePk(new CalculationQuestionPk(section, question, calculationSection, calculationQuestion));
	}
	
	public CalculationQuestion(Section calculationSection, Question calculationQuestion){
		this.setCalculationQuestionCompositePk(new CalculationQuestionPk(calculationSection, calculationQuestion));
	}
	
	public CalculationQuestion(CalculationQuestionPk calculationQuestionPk){
		this.setCalculationQuestionCompositePk(calculationQuestionPk);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((calculationQuestionCompositePk == null) ? 0 : calculationQuestionCompositePk.hashCode());
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
		CalculationQuestion other = (CalculationQuestion) obj;
		if (calculationQuestionCompositePk == null) {
			if (other.calculationQuestionCompositePk != null)
				return false;
		} else if (!calculationQuestionCompositePk.equals(other.calculationQuestionCompositePk))
			return false;
		return true;
	}

}
