package gov.nih.tbi.dictionary.validation.eform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.dictionary.constants.EformValidationConstants;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;
import gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion;

public class EformPartialValidation {
	
	private Eform eform;
	
	private FormStructure associatedFormStructure;
	
	private List<String> validationErrorMessages = new ArrayList<String>();
	
	private Map<String,DataElement> requiredDataElements;
	
	private Map<String,Question> requiredQuestions;
	
	private Set<DataElement> depricatedRetiredDataElements;
	
	public EformPartialValidation(Eform eform, FormStructure associatedFormStructure){
		this.eform = eform;
		this.associatedFormStructure = associatedFormStructure;
	}
	
	public List<String> eformMinimumValidation(){
		
		if(isFormStructureNull()){
			validationErrorMessages.add(EformValidationConstants.FORM_STRUCTURE_NULL);
			questionValidation();
		} else {
			validationWithFormStructure();			
		}
		return validationErrorMessages;
	}
	
	private void validationWithFormStructure(){
		buildDataElementValidationLists();
		
		if(doesEformContainDepricatedOrRetiredDataElements()){
			validationErrorMessages.add(EformValidationConstants.RETIRED_DEPRICATED_DATA_ELEMENTS); //this should be a warning...
		}
		
		//execute question validation
		questionValidation();
		
		if(doesRequiredQuestionsMatchRequiredDataElements()){
			validationErrorMessages.add(EformValidationConstants.REQUIRED_QUESTIONS_DATA_ELEMENTS);
		}
		validateEformDEwithFormStructure();
	}
	
	private void buildDataElementValidationLists(){
		requiredDataElements = new HashMap<String,DataElement>();
		depricatedRetiredDataElements = new HashSet<DataElement>();
		
		for(RepeatableGroup rg : associatedFormStructure.getRepeatableGroups()){
			for(MapElement me : rg.getMapElements()){
				DataElement de = associatedFormStructure.getDataElements().get(me.getStructuralDataElement().getNameAndVersion());
				if(isDataElementRetiredOrDepricated(de)){
					depricatedRetiredDataElements.add(de);
				}
				if(isDataElementRequired(me)){
					requiredDataElements.put(buildRequiredDataElementKey(rg,de), de);
				}
			}
		}
	}
	
	/*
	 * Begin validation methods for the class.
	 */
	
	private void questionValidation(){
		requiredQuestions = new HashMap<String,Question>();
		for(Section section : eform.getSectionList()){
			//no need to validate text sections or child sections
			if(!section.getSectionQuestion().isEmpty() && section.getRepeatedSectionParent() == null){
				for(SectionQuestion sq : section.getSectionQuestion()){
					//add required question for later validation
					if(isQuestionRequired(sq.getQuestion())){
						requiredQuestions.put(buildRequiredQuestionKey(section,sq.getQuestion()), sq.getQuestion());
					}
					if(!questionAnswerOptionValidation(sq.getQuestion())){
						validationErrorMessages.add(EformValidationConstants.QUESTION_ANSWER_OPTION_VALIDATION);
					}
				}
			}
		}
	}
	
	private void validateEformDEwithFormStructure(){
				
		Set<String> eformDE = new HashSet<String>();

		for(Section section: eform.getSectionList()){
			if(section!=null){
				for(SectionQuestion sectionQstn: section.getSectionQuestion()){
					if(sectionQstn != null){
						Question question = sectionQstn.getQuestion();
						if(question != null){
							if(question.getQuestionAttribute() != null){
								eformDE.add(question.getQuestionAttribute().getDataElementName());										
							}					
						}
					}
				}
			}
		}
		validateDataElement(eformDE);
	}
	
	private void validateDataElement(Set <String> eformDE){
		
		Collection <DataElement> formStructureDE = associatedFormStructure.getDataElements().values();
		for(String de: eformDE){
			if(!isValidDE(de, formStructureDE.iterator())){
				validationErrorMessages.add(EformValidationConstants.EFORM_DE_INVALID);
				break;
			}
		}
	}
	
	private Boolean isValidDE(String eformDE, Iterator <DataElement> formStructureDE){

		while(formStructureDE.hasNext()){
						
			if(eformDE.equalsIgnoreCase(formStructureDE.next().getName().trim())){				
				return Boolean.TRUE;
			}
		}		
		return Boolean.FALSE;
	}
	
	private Boolean doesEformContainDepricatedOrRetiredDataElements(){
		return !depricatedRetiredDataElements.isEmpty();
	}
	
	private Boolean doesRequiredQuestionsMatchRequiredDataElements(){ 
		return requiredDataElements.size() > requiredQuestions.size();
	}
	
	private Boolean questionAnswerOptionValidation(Question question){
		if(question.getQuestionAnswerOption() != null && !question.getQuestionAnswerOption().isEmpty()){
			switch(question.getType()){
				case CHECKBOX :
					return Boolean.TRUE;
				case MULTI_SELECT :
					return Boolean.TRUE;
				case SELECT :
					return Boolean.TRUE;
				case RADIO :
					return Boolean.TRUE;
				case TEXTBOX :
					return Boolean.TRUE;
				case TEXTAREA :
					return Boolean.TRUE;
				case FILE:
					return Boolean.TRUE;
				default :
					return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}
	
	/*
	 * End validation methods for the class.
	 */
	/*
	 * Begin helper methods for the class.
	 */
	private String buildRequiredDataElementKey(RepeatableGroup rg, DataElement de){
		return rg.getName() + "." + de.getName();
	}
	
	private String buildRequiredQuestionKey(Section section, Question question){
		return Long.toString(section.getId()) + "." + Long.toString(question.getId());
	}
	
	private Boolean isQuestionRequired(Question question){
		return  Boolean.valueOf(question.getQuestionAttribute().getRequiredFlag());
	}
	
	private Boolean isDataElementRequired(MapElement mapElement){
		return Boolean.valueOf(mapElement.getRequiredType().equals(RequiredType.REQUIRED));
	}
	
	private Boolean isDataElementRetiredOrDepricated(DataElement dataElement){
		return Boolean.valueOf(dataElement.getStatus().equals(DataElementStatus.DEPRECATED) || dataElement.getStatus().equals(DataElementStatus.RETIRED));
	}
	
	private Boolean isFormStructureNull(){
		if(this.associatedFormStructure == null){
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	/*
	 * End helper methods for the class.
	 */

}
