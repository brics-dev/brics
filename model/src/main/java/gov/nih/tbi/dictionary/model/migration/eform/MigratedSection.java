package gov.nih.tbi.dictionary.model.migration.eform;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;


public class MigratedSection {
	
	@XmlElement(name = "proformsSectionId")
	private Long proformsSectionId;
	
	@XmlElement(name = "dictionarySectionId")
	private Long dictionarySectionId;
	
	@XmlElementWrapper(name = "migratedQuestionIds")
	@XmlElement(name = "migratedQuestion")
	private ArrayList<MigratedQuestion> questionList = new ArrayList<MigratedQuestion>();

	public MigratedSection() {}

	public MigratedSection(Long proformsSectionId, Long dictionarySectionId) {
		this.proformsSectionId = proformsSectionId;
		this.dictionarySectionId = dictionarySectionId;
	}

	public Long getProformsSectionId() {
		return this.proformsSectionId;
	}

	public Long getDictionarySectionId() {
		return this.dictionarySectionId;
	}
	
	public void addMigratedQuestion(MigratedQuestion mq){
		this.questionList.add(mq);
	}
	
	public ArrayList<MigratedQuestion> getMigratedQuestion(){
		return this.questionList;
	}
}