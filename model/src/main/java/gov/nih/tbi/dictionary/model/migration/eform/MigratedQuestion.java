package gov.nih.tbi.dictionary.model.migration.eform;

import javax.xml.bind.annotation.XmlElement;

public class MigratedQuestion {
	
	@XmlElement(name = "proformsQuestionId")
	private Long proformsQuestionId;
	
	@XmlElement(name = "dictionaryQuestionId")
	private Long dictionaryQuestionId;

	public MigratedQuestion() {}

	public MigratedQuestion(Long proformsQuestionId, Long dictionaryQuestionId) {
		this.proformsQuestionId = proformsQuestionId;
		this.dictionaryQuestionId = dictionaryQuestionId;
	}

	public Long getProformsQuestionId() {
		return this.proformsQuestionId;
	}

	public Long getDictionaryQuestionId() {
		return this.dictionaryQuestionId;
	}
}
