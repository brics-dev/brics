package gov.nih.tbi.taglib.datatableDecorators;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.SessionStudy;
import gov.nih.tbi.repository.model.hibernate.StudyForm;

public class StudyFsIdtDecorator extends IdtDecorator {

	
	protected SessionStudy sessionStudy;

	SemanticFormStructure sfs;
	StudyForm studyForm;
	Set<StudyForm> studyFormStructures;

	public StudyFsIdtDecorator(SessionStudy s) {
		super();
		studyFormStructures = s.getStudy().getStudyForms();
	}

	public String initRow(Object obj, int viewIndex) {
		String feedback = super.initRow(obj, viewIndex);
		if (obj instanceof SemanticFormStructure) {
			sfs = (SemanticFormStructure) obj;
		}

		studyForm = new StudyForm(sfs.getShortName(), sfs.getVersion(), sfs.getTitle(), sfs.getSubmissionType());
		return feedback;
	}

	public String getIsLinked() {
		return (studyFormStructures.contains(studyForm)) ? "true" : "false";
	}
}