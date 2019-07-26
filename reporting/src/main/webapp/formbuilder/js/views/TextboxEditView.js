/**
 * 
 */
var TextboxEditView = QuestionEditView.extend({
	templateName : "editQuestionTemplate", // this is tab 1: basic options
	validationRules : [new ValidationRule({fieldName: "questionName", required: true, description: Config.language.questionNameRequired}),
	                   new ValidationRule({  
		                	   fieldName : "questionName",
		            	       description: Config.language.validateQuestionNameSpecial,
		                	   match : function(model) {
		                		   var questionName = model.get("questionName");
		                		   var iChars = "!@#$%^&*()+=[]\\\';,./{}|\":<>?";
		                		   for(var i = 0; i < questionName.length; i++){
			                		   if (iChars.indexOf(questionName.charAt(i)) != -1) {
			                			   return false;
			                		   }
		                		   }
		                		   return true;
		                	   }
		                  }),
		                  new ValidationRule({  
		                	   fieldName : "questionName",
		            	       description: Config.language.validateQuestionNameWhite,
		                	   match : function(model) {
		                		   var questionName = model.get("questionName");
		                		   if (/\s/g.test(questionName)) {
		                			   return false;
		                		   }else {
		                			   return true;
		                		   }
		                	   }
		                  }),
			              new ValidationRule({
			            		fieldName : "questionText",
			            		description : Config.language.maxQuestionText,
			            		match : function(model) {
			            			var questionTextStr = model.get("questionText");
			            			if(questionTextStr!=null && questionTextStr.length > Config.questionText.maxQuestionText){
			            				return false;
			            			}else{
			            				return true;
			            			}
			            		}
			            	}),
			            	 new ValidationRule({
			            		fieldName : "descriptionUp",
			            		description : Config.language.descriptionAboveText,
			            		match : function(model) {
			            			var descriptionUpStr = model.get("descriptionUp");
			            			if(descriptionUpStr!=null && descriptionUpStr.length > Config.questionText.maxDescription){
			            				return false;
			            			}else{
			            				return true;
			            			}
			            		}
				            }),
				            new ValidationRule({
			            		fieldName : "descriptionDown",
			            		description : Config.language.descriptionBelowText,
			            		match : function(model) {
			            			var descriptionDownStr = model.get("descriptionDown");
			            			if(descriptionDownStr!=null && descriptionDownStr.length > Config.questionText.maxDescription){
			            				return false;
			            			}else{
			            				return true;
			            			}
			            		}
				            }),
			                new ValidationRule({
			            		fieldName : "defaultValue",
			            		description : "Specified Range didn't validate.",
			            		match : function(model) {
			            			var defaultVal = model.get("defaultValue");
			            			var lowerRange = model.get("rangeValue1");
			            			var upperRange = model.get("rangeValue2");
			            			var answerType = model.get("answerType");
			            			var maxCharacters = model.get("maxCharacters");
			            			var rangeOperator = model.get("rangeOperator");

			            			
			            			if(defaultVal=='') {
			            				return true;
			            			}
			            			
			            			if(answerType==1){
			            				if(defaultVal.length>maxCharacters){
			            					this.description ="The default value range should be less than maxCharacters "+ maxCharacters+" specified in the validation tab."
			            					return false;
			            				}
			            				
			            			}
			            			
			            			if(lowerRange=='' && upperRange=='' ){
			            				return true;
			            			}
			            			
			            			
			            			if(rangeOperator==1) {
			            				if(Number(defaultVal)!=Number(upperRange)){
			            					
			            				}else {
			            					return true;
			            				}
			            				
			            				
			            			}
			            			
			            			
			            			if(rangeOperator==2) {
			            				if(Number(defaultVal)>Number(upperRange)){
			            					this.description ="The default value range should be less than or equal to "+ upperRange;
			            				}else {
			            					return true;
			            				}
			            				
			            				
			            			}

			            			if(rangeOperator==3) {
			            				if(Number(defaultVal)<Number(lowerRange)){
			            					this.description ="The default value range should be greater than or equal to "+ lowerRange;
			            				}else {
			            					return true;
			            				}
			            				
			            			}
			            			
			            			if(rangeOperator==4) {
			            				if((Number(defaultVal)>Number(upperRange)) || (defaultVal!='' && Number(defaultVal)<Number(lowerRange))){
				            				this.description ="The default value range should be between "+ lowerRange+" and "+upperRange;
				            				return false;
				            			}else{
				            				return true;
				            			}
			            			}
			            			

            		
			            		}
			            	})
	                   ],
   	
	initialize : function() {
		TextboxEditView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate(this.templateName);
		this.initCommons();
		this.registerCommons(this, QuestionAdditionalTextDecorator);
		this.registerCommons(this, QuestionDefaultValueDecorator);
		this.registerCommons(this, QuestionFormatDecorator);
		this.registerCommons(this, QuestionValidationDecorator);
//		this.registerCommons(this, QuestionPrepopulationDecorator);
		this.registerCommons(this, QuestionSkipRuleDecorator);
		this.registerCommons(this, QuestionGraphicDecorator);/*added by Cing Heng*/
		this.registerCommons(this, QuestionCalculationRuleDecorator);
		this.registerCommons(this, QuestionConversionFactorDecorator);
		//for testing
		//this.registerCommons(this, QuestionOptionDecorator);
		
	},
	render : function(model) {
		this.model = model;
		
		this.$el.html(this.template(model.attributes));
		TextboxEditView.__super__.render.call(this, model);
		if(    this.model.get("calDependent")      || ''!=this.model.get("calculation") 
			|| this.model.get("skipRuleDependent") || this.model.get("skiprule") || this.model.get("isNew")
		  )
		{
			this.$('select[name="questionType"] option').prop("disabled", true);
		}
		else
		{ 
		this.$('select[name="questionType"] option').prop("disabled", false);
		this.$('select[name="questionType"] option[value="3"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="4"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="5"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="6"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="9"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="11"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="12"]').prop("disabled", true);
		}

		return this;
	}
	
});