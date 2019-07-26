/**
 * 
 */
var SelectEditView = QuestionEditView.extend({
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
			            	})
	                   ],


	initialize : function() {
		SelectEditView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate(this.templateName);
		this.initCommons();
		this.registerCommons(this, QuestionAdditionalTextDecorator);
		this.registerCommons(this, QuestionDefaultValueDecorator);
		this.registerCommons(this, QuestionFormatDecorator);
		this.registerCommons(this, QuestionValidationDecorator);
		this.registerCommons(this, QuestionPrepopulationDecorator);
		this.registerCommons(this, QuestionOptionDecorator);
		this.registerCommons(this, QuestionSkipRuleDecorator);
		this.registerCommons(this, QuestionGraphicDecorator);/*added by Cing Heng*/
		this.registerCommons(this, QuestionEmailDecorator);/*added by Cing Heng*/
	},
	render : function(model) {
		this.model = model;
		
		this.$el.html(this.template(model.attributes));
		SelectEditView.__super__.render.call(this, model);
		this.renderOptions(model);
		
		this.$('select[name="questionType"] option').prop("disabled", false);
		this.$('select[name="questionType"] option[value="1"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="2"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="9"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="10"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="11"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="12"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="5"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="6"]').prop("disabled", true);

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
				score = optionsObject.score;
				submittedValue = optionsObject.submittedValue;
				
				optionRowTemplate = TemplateManager.getTemplate("editQuestionOptionsRow");
				
				optionRowsHtml = optionRowsHtml + optionRowTemplate({
					option : option,
					score : score,
					submittedValue : submittedValue
				});
				
			}
			
			this.$(".inputOptionsRow").before(optionRowsHtml);
			
			this.$(".optionChoiceInput").val('');
			this.$(".optionScoreInput").val('');
			this.$(".optionSubmittedValueInput").val('');
		}
	},
	onChangeQuestionType : function() {
		//this.model.set("questionType", this.$('select[name="questionType"].val()') );
		//$("select[name='questionType']").change( function() {
			var msg = "Are you sure you want to change the question type?";
			var qModel = this.model;
			var previous = qModel.previous("questionType");
			
			$.ibisMessaging("dialog", "info", msg, {
				width: 500,
				dialogClass : "formBuilder_editor",
				modal : true,
				buttons: [
				{
					text: "Ok",
					click : function() {
//						var changed = qModel.get("questionType");
//						$('select[name="questionType"]').val(changed);
						EventBus.trigger("change:questionType", FormBuilder.page.get("activeEditorView").model);
						$(this).dialog("close");
				    }
				},
				{
					text: "Cancel",
					click : function() {
						//var previous = qModel.previous("questionType");
						//$('select[name="questionType"]').val(previous);
						FormBuilder.page.get("activeEditorView").model.set("questionType", previous);
						EventBus.trigger("change:questionType", FormBuilder.page.get("activeEditorView").model);
						$(this).dialog("close");
					}
				}]
				               
			});
			
	 }
	
});