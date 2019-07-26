package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.repository.model.hibernate.StudyForm;

public class StudyFormListDecorator extends Decorator {
	StudyForm studyForm;

	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);
		studyForm = (StudyForm) obj;
		return feedback;
	}
	
	public String getTitleLink() {
		return "<a href=\"javascript:viewFsDetails('" + studyForm.getShortName() + "')\">" + studyForm.getTitle() + "</a>";
	}
}
