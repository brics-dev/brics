package gov.nih.tbi.dictionary.model.hibernate.eform.adapters;

import gov.nih.tbi.commons.model.QuestionType;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;
import gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestionPk;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * XmlAdapter for section object for web service
 */

public class SkipRuleQuestionAdapter extends XmlAdapter<SkipRuleQuestionAdapter.XmlSkipRuleQuestionWrapper, Set<SkipRuleQuestion>> {

    /**
     * Object that contains a list of cell value properties
     * 
     * @author fchen
     * 
     */
    protected static class XmlSkipRuleQuestionWrapper {
        public Set<XmlSkipRuleQuestionAdapter> skipRuleQuestions = new HashSet<XmlSkipRuleQuestionAdapter>();
    }


    protected static class XmlSkipRuleQuestionAdapter {

        @XmlElement(name = "skipSectionId")
        private Long skipSectionId;

        @XmlElement(name = "skipQuestionId")
        private Long skipQuestionId;
        
        @XmlElement(name = "skipQuestionName")
        private String skipQuestionName;
        
        @XmlElement(name = "skipQuestionText")
        private String skipQuestionText;
        
        @XmlElement(name = "skipQuestionType" , type = QuestionType.class)
        private QuestionType skipQuestionType;
       
    }
    
    @Override
    public Set<SkipRuleQuestion> unmarshal(XmlSkipRuleQuestionWrapper adaptedSection) throws Exception {
    	Set<SkipRuleQuestion> unmarshalledSet = new HashSet<SkipRuleQuestion>();
    	
    	for(XmlSkipRuleQuestionAdapter sectionAndQuestion : adaptedSection.skipRuleQuestions){
    		Section unmarshallSection = new Section();
    		unmarshallSection.setId(sectionAndQuestion.skipSectionId);
    		
    		Question unmarshallQuestion = new Question();
    		unmarshallQuestion.setId(sectionAndQuestion.skipQuestionId);
    		unmarshallQuestion.setName(sectionAndQuestion.skipQuestionName);
    		unmarshallQuestion.setText(sectionAndQuestion.skipQuestionText);
    		unmarshallQuestion.setType(sectionAndQuestion.skipQuestionType);
    		
    		SkipRuleQuestionPk skipRulepk = new SkipRuleQuestionPk(null, null, unmarshallSection, unmarshallQuestion);
    		SkipRuleQuestion unmarshalledSkipLogic = new SkipRuleQuestion(skipRulepk);
    		unmarshalledSet.add(unmarshalledSkipLogic);
    	}
    	
    	return unmarshalledSet;
    }
    
    @Override
    public XmlSkipRuleQuestionWrapper marshal(Set<SkipRuleQuestion> unmarshalledSet) throws Exception {
    	XmlSkipRuleQuestionWrapper marshalledSkipRuleLogic = new XmlSkipRuleQuestionWrapper();
    	
    	for(SkipRuleQuestion marshallSkipRule : unmarshalledSet){
    		XmlSkipRuleQuestionAdapter adaptedClass = new XmlSkipRuleQuestionAdapter();
    		Section skipSection = marshallSkipRule.getSkipRuleQuestionCompositePk().getSkipRuleSection();
    		Question skipQuestion = marshallSkipRule.getSkipRuleQuestionCompositePk().getSkipRuleQuestion();
    		
    		adaptedClass.skipSectionId = skipSection.getId();
    		adaptedClass.skipQuestionId = skipQuestion.getId();
    		adaptedClass.skipQuestionName = skipQuestion.getName();
    		adaptedClass.skipQuestionText = skipQuestion.getText();
    		adaptedClass.skipQuestionType = skipQuestion.getType(); 		
    		
    		marshalledSkipRuleLogic.skipRuleQuestions.add(adaptedClass);
    	}

    	return marshalledSkipRuleLogic;
    }
}
