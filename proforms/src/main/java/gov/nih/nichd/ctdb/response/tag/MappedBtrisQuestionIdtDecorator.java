package gov.nih.nichd.ctdb.response.tag;

import gov.nih.nichd.ctdb.response.domain.MappedBtrisQuestion;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class MappedBtrisQuestionIdtDecorator extends IdtDecorator {

	/**
	 * Default Constructor
	 */
	public MappedBtrisQuestionIdtDecorator() {
		super();
	}

	public String getSectionQuestionId() {
		MappedBtrisQuestion mappedQ = (MappedBtrisQuestion) getObject();
		return mappedQ.getSectionQuestionId();
	}

	public String getSectionName() {
		MappedBtrisQuestion mappedQ = (MappedBtrisQuestion) getObject();
		return mappedQ.getSectionName();
	}

	public String getQuestionName() {
		MappedBtrisQuestion mappedQ = (MappedBtrisQuestion) getObject();
		return mappedQ.getQuestionName();
	}
}
