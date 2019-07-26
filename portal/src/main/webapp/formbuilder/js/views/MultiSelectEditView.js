/**
 * 
 */
var MultiSelectEditView = QuestionEditView.extend({
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
			            	})
	                   ],
	
	initialize : function() {
		MultiSelectEditView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate(this.templateName);
		this.initCommons();
		this.registerCommons(this, QuestionAdditionalTextDecorator);
		//this.registerCommons(this, QuestionDefaultValueDecorator);
		this.registerCommons(this, QuestionFormatDecorator);
		this.registerCommons(this, QuestionValidationDecorator);
		this.registerCommons(this, QuestionOptionDecorator);
		this.registerCommons(this, QuestionSkipRuleDecorator);
		this.registerCommons(this, QuestionGraphicDecorator);/*added by Cing Heng*/
		this.registerCommons(this, QuestionEmailDecorator);/*added by Cing Heng*/
	},
	render : function(model) {
		this.model = model;
		
		this.$el.html(this.template(model.attributes));
		MultiSelectEditView.__super__.render.call(this, model);
		this.renderOptions(model);
		
		this.$('select[name="questionType"] option').prop("disabled", false);
		this.$('select[name="questionType"] option[value="1"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="2"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="9"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="10"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="11"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="12"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="3"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="4"]').prop("disabled", true);
		this.ellipsis(this.$('.optionChoice'), 100);
		return this;
	},

	renderOptions : function(model) {
		var qOptionsObjectArray = model.get("questionOptionsObjectArray");
		if(qOptionsObjectArray.length > 0) {
			var optionsObject;
			var option;
			var submittedValue;
			var optionRowTemplate;
			var optionRowsHtml = "";
			for(var i=0;i<qOptionsObjectArray.length;i++) {
				optionsObject = qOptionsObjectArray[i];
				option = optionsObject.option;
				submittedValue = optionsObject.submittedValue;
				
				optionRowTemplate = TemplateManager.getTemplate("editQuestionOptionsRow_noScore");
				
				optionRowsHtml = optionRowsHtml + optionRowTemplate({
					option : option,
					submittedValue : submittedValue
				});
				
			}
			
			this.$(".inputOptionsRow").before(optionRowsHtml);
			
			this.$(".optionChoiceInput").val('');
			this.$(".optionSubmittedValueInput").val('');
		}
	}
});