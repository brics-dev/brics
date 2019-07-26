
package gov.nih.tbi.dictionary.model.hibernate.eform;

import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocumentPk;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;


@Entity
@Table(name = "Question_Document")
@XmlAccessorType(XmlAccessType.FIELD)
public class QuestionDocument implements Serializable {	
	private static final long serialVersionUID = 1232382995335763942L;

	@EmbeddedId
	@XmlElement(name="QuestionDocumentPk")
        private QuestionDocumentPk questionDocumentPk;
	
	@Transient
        private boolean removeFile;

	//@ManyToOne(optional = false, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	@ManyToOne(optional = false, cascade = { CascadeType.ALL })
	@JoinColumn(name = "USER_FILE_ID")
	@XmlElement(name="UserFile")
	private UserFile userFile;

	
	
	public void setQuestionDocumentPk(QuestionDocumentPk questionDocumentPk){
		this.questionDocumentPk = questionDocumentPk;
	}
	
	public QuestionDocumentPk getQuestionDocumentPk(){
		return this.questionDocumentPk;
	}

	public UserFile getUserFile() {

		return userFile;
	}

	public void setUserFile(UserFile userFile) {

		this.userFile = userFile;
	}

	public void setRemoveFile(boolean removeFile) {

		this.removeFile = removeFile;
	}

	public boolean getRemoveFile() {

		return removeFile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((questionDocumentPk == null) ? 0 : questionDocumentPk.hashCode());
		result = prime * result + ((userFile == null) ? 0 : userFile.hashCode());
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
		QuestionDocument other = (QuestionDocument) obj;
		if (questionDocumentPk == null) {
			if (other.questionDocumentPk != null)
				return false;
		} else if (!questionDocumentPk.equals(other.questionDocumentPk))
			return false;
		if (userFile == null) {
			if (other.userFile != null)
				return false;
		} else if (!userFile.equals(other.userFile))
			return false;
		return true;
	}

}
