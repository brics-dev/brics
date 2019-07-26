package gov.nih.tbi.dictionary.model.hibernate.eform.adapters;

import gov.nih.tbi.commons.model.AnswerType;
import gov.nih.tbi.commons.model.QuestionType;
import gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestionPk;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAnswerOption;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAttribute;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * XmlAdapter for section object for web service
 */

public class CalculationQuestionAdapter extends XmlAdapter<CalculationQuestionAdapter.XmlCalculationQuestionWrapper, Set<CalculationQuestion>> {

    protected static class XmlCalculationQuestionWrapper {
        public Set<XmlCalculationQuestionAdapter> calculationQuestions = new HashSet<XmlCalculationQuestionAdapter>();
    }


    protected static class XmlCalculationQuestionAdapter {

        @XmlElement(name = "calculationSectionId")
        private Long calculationSectionId;

        @XmlElement(name = "calculationQuestionId")
        private Long calculationQuestionId;
        
        @XmlElement(name = "calculationQuestionName")
        private String calculationQuestionName;
        
        @XmlElement(name = "calculationQuestionText")
        private String calculationQuestionText;
        
        @XmlElement(name = "calculationQuestionType")
        private String calculationQuestionType;
        
        @XmlElement(name= "calculationQuesitonAnswerType")
        private Integer calculationQuesitonAnswerType;
        
        @XmlElement(name= "calculationQuesitonAnswerOption")
        private Set<QuestionAnswerOption> questionAnswerOption;
       
    }
    
    @Override
    public Set<CalculationQuestion> unmarshal(XmlCalculationQuestionWrapper adaptedSection) throws Exception {
    	Set<CalculationQuestion> unmarshalledSet = new HashSet<CalculationQuestion>();
    	
    	for(XmlCalculationQuestionAdapter sectionAndQuestion : adaptedSection.calculationQuestions){
    		Section unmarshallSection = new Section();
    		unmarshallSection.setId(sectionAndQuestion.calculationSectionId);
    		
    		Question unmarshallQuestion = new Question();
    		unmarshallQuestion.setId(sectionAndQuestion.calculationQuestionId);
    		unmarshallQuestion.setName(sectionAndQuestion.calculationQuestionName);
    		unmarshallQuestion.setText(sectionAndQuestion.calculationQuestionText);
    		unmarshallQuestion.setType(QuestionType.getByValue(Integer.parseInt(sectionAndQuestion.calculationQuestionType)));
    		unmarshallQuestion.setQuestionAnswerOption(sectionAndQuestion.questionAnswerOption);
    		
    		QuestionAttribute qa = new QuestionAttribute();
    		qa.setAnswerType(AnswerType.getByValue(sectionAndQuestion.calculationQuesitonAnswerType));
    		unmarshallQuestion.setQuestionAttribute(qa);
    		
    		CalculationQuestionPk calculationPk = new CalculationQuestionPk(null, null, unmarshallSection, unmarshallQuestion);
    		CalculationQuestion unmarshalledCalculationLogic = new CalculationQuestion(calculationPk);
    		unmarshalledSet.add(unmarshalledCalculationLogic);
    	}
    	
    	return unmarshalledSet;
    }
    
    @Override
    public XmlCalculationQuestionWrapper marshal(Set<CalculationQuestion> unmarshalledSet) throws Exception {
    	XmlCalculationQuestionWrapper marshalledCalculationLogic = new XmlCalculationQuestionWrapper();
    	
    	for(CalculationQuestion marshallCacluation : unmarshalledSet){
    		XmlCalculationQuestionAdapter adaptedClass = new XmlCalculationQuestionAdapter();
    		Section calculationSection = marshallCacluation.getCalculationQuestionCompositePk().getCalculationSection();
    		Question calculationQuestion = marshallCacluation.getCalculationQuestionCompositePk().getCalculationQuestion();
    		
    		adaptedClass.calculationSectionId = calculationSection.getId();
    		adaptedClass.calculationQuestionId = calculationQuestion.getId();
    		adaptedClass.calculationQuestionName = calculationQuestion.getName();
    		adaptedClass.calculationQuestionText = calculationQuestion.getText();
    		adaptedClass.calculationQuestionType = Integer.toString(calculationQuestion.getType().getValue());
    		adaptedClass.calculationQuesitonAnswerType = calculationQuestion.getQuestionAttribute().getAnswerType().getValue();
    		adaptedClass.questionAnswerOption = calculationQuestion.getQuestionAnswerOption();
    		
    		marshalledCalculationLogic.calculationQuestions.add(adaptedClass);
    	}

    	return marshalledCalculationLogic;
    }
}
