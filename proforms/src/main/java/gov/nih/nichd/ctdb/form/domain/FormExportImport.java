package gov.nih.nichd.ctdb.form.domain;
import gov.nih.nichd.ctdb.question.domain.QuestionExportImport;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FormExportImport{
	FormInfoExportImport formInfo;
	private String name;
	private String description;
	
	//List of Unique question in the form
	private List<QuestionExportImport> question;
	@XmlElementWrapper( name="questions" )
	public List<QuestionExportImport> getQuestion() {
		return question;
	}
	@XmlElement
	public void setQuestion(List<QuestionExportImport> question) {
		this.question = question;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public FormInfoExportImport getFormInfo() {
		return formInfo;
	}
	@XmlElement(name = "formInfo")
	public void setFormInfo(FormInfoExportImport formInfo) {
		this.formInfo = formInfo;
	}
}
