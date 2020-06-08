package gov.nih.nichd.ctdb.form.domain;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.question.domain.CalculationType;
import gov.nih.nichd.ctdb.question.domain.ConversionFactor;
import gov.nih.nichd.ctdb.question.domain.Question;

/**
 * Created by IntelliJ IDEA.
 * User: 513320
 * Date: Aug 31, 2004
 * Time: 9:53:28 AM
 * To change this template use Options | File Templates.
 */
public class CalculatedFormQuestionAttributes extends FormQuestionAttributes {

   	private static final long serialVersionUID = -1812320124571993107L;
	
   	private CalculationType calculationType;
    private ConversionFactor conversionFactor;
    private List<Question> questionsToCalculate = null;
    private List<String> questionIdsToCalculate;
    private String calculation;
    private Boolean conditionalForCalc;
    private boolean isCount = false;
    
    public CalculatedFormQuestionAttributes()
    {

    }

      /**
       * Gets calculation type for the calculated question.
       *
       * @return CalculationType The calculated question calculationType.
       */
      public CalculationType getCalculationType()
      {
          return calculationType;
      }

      /**
       * Sets the calculated question's calculationType
       *
       * @param calculationType The CalculationType this calculated question will be set to.
       */
      public void setCalculationType(CalculationType calculationType)
      {
          this.calculationType = calculationType;
      }

      /**
       * Gets conversion vactor for the calculated question.
       *
       * @return ConversionFactor The calculated question conversionFactor.
       */
      public ConversionFactor getConversionFactor()
      {
          return conversionFactor;
      }

      /**
       * Sets the calculated question's conversionFactor
       *
       * @param conversionFactor The ConversionFactor this calculated question will be set to.
       */
      public void setConversionFactor(ConversionFactor conversionFactor)
      {
          this.conversionFactor = conversionFactor;
      }

      /**
       * Gets a list of questions which will be used for calculated question calculation.
       *
       * @return A list of questions for question calculation, or an empty list if the associated 
       * member variable is null.
       */
      public List<Question> getQuestionsToCalculate()
      {
          return questionsToCalculate != null ? questionsToCalculate : new ArrayList<Question>();
      }

      /**
       * Sets the list of questions for calculation to this list of questions.
       *
       * @param questionsToCalculate The list of questions for calculation.
       */
      public void setQuestionsToCalculate(List<Question> questionsToCalculate)
      {
          this.questionsToCalculate = questionsToCalculate;
      }

	public List<String> getQuestionIdsToCalculate() {
		return questionIdsToCalculate;
	}

	public void setQuestionIdsToCalculate(List<String> questionIdsToCalculate) {
		this.questionIdsToCalculate = questionIdsToCalculate;
	}

	public String getCalculation() {
          return calculation != null ? calculation : "";
      }

      public void setCalculation(String calculation) {
          this.calculation = calculation;
      }

      public Boolean getConditionalForCalc() {
		return conditionalForCalc;
	}

	public void setConditionalForCalc(Boolean conditionalForCalc) {
		this.conditionalForCalc = conditionalForCalc;
	}
	
	

	public boolean isCount() {
		return isCount;
	}

	public void setIsCount(boolean isCount) {
		this.isCount = isCount;
	}

	public boolean equals(Object o) {

          // theoretically a calcAttribute and a fqAttr are equal if there is no calc in the calcAtt
          if (o instanceof FormQuestionAttributes && !(o instanceof CalculatedFormQuestionAttributes)) {
                if (this.calculation.length() > 0) {
                    return false;
                }
           }

          if (o instanceof CalculatedFormQuestionAttributes) {
              CalculatedFormQuestionAttributes cfqa = (CalculatedFormQuestionAttributes) o;

              if (!(this.calculation == null && cfqa.getCalculation() == null) &&
                  this.calculation == null && cfqa.getCalculation() != null && cfqa.getCalculation().length() > 0 ||
                  this.calculation != null && this.calculation.length() > 0 && cfqa.getCalculation() == null ||
                  !this.calculation.equals(cfqa.getCalculation())) {
                   return false;
              }

              /*
              if (!this.calculationType.equals(cfqa.getCalculationType())) {
                  return false;
              } */

              if (!(this.conversionFactor == null || cfqa.getConversionFactor() == null) && (
                  this.conversionFactor == null && cfqa.getConversionFactor() != null ||
                  this.conversionFactor != null && cfqa.getConversionFactor() == null ||
                  !this.conversionFactor.equals(cfqa.getConversionFactor()))) {
                    return false;
              }
          }

          return super.equals(o);
      }

}
