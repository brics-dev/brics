package gov.nih.tbi.dictionary.model;

import java.util.Comparator;

import gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion;

public class QuestionDisplayOrderComparator implements Comparator<SectionQuestion> {

	public int compare(SectionQuestion sqOne, SectionQuestion sqTwo) {
		if (sqOne.getSection().getId() != sqTwo.getSection().getId()) {
			return 0;
		}
		if (sqOne.getQuestionOrder() > sqTwo.getQuestionOrder()) {
			return 1;
		} else if (sqOne.getQuestionOrder() < sqTwo.getQuestionOrder()) {
			return -1;
		}
		if (sqOne.getQuestionOrderColumn() > sqTwo.getQuestionOrderColumn()) {
			return 1;
		} else if (sqOne.getQuestionOrderColumn() < sqTwo.getQuestionOrderColumn()) {
			return -1;
		}
		return 0;
	}

}
