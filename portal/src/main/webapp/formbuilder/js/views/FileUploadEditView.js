/**
 * 
 */
var FileUploadEditView = QuestionEditView.extend({
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
		FileUploadEditView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate(this.templateName);
		this.initCommons();
		this.registerCommons(this, QuestionAdditionalTextDecorator);
		this.registerCommons(this, QuestionFormatDecorator);
		this.registerCommons(this, QuestionValidationDecorator);
//		this.registerCommons(this, QuestionPrepopulationDecorator);
		this.registerCommons(this, QuestionGraphicDecorator);/*added by Cing Heng*/
		//for testing
		//this.registerCommons(this, QuestionOptionDecorator);
		
	},
	render : function(model) {
		this.model = model;
		
		this.$el.html(this.template(model.attributes));
		FileUploadEditView.__super__.render.call(this, model);
		
		this.$('select[name="questionType"] option').prop("disabled", true);

		return this;
	}
});