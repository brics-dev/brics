package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@Embeddable
@XmlRootElement(name="QuestionDocumentPk")
@XmlAccessorType(XmlAccessType.FIELD)
public class QuestionDocumentPk implements Serializable {

	private static final long serialVersionUID = 1109491634315365716L;

	@Column(name = "FILE_NAME")
	protected String fileName;

	@XmlIDREF
	@ManyToOne(optional = false)
	@JoinColumn(name = "QUESTION_ID")
	@XmlElement(name="question")
	protected Question question;
	
	public QuestionDocumentPk(){}
	
	public QuestionDocumentPk(Question question, String fileName){
		setQuestion(question);
		setFileName(fileName);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((question == null) ? 0 : question.hashCode());
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
		QuestionDocumentPk other = (QuestionDocumentPk) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		return true;
	}

}
