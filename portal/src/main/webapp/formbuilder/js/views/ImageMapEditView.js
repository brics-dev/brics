/**
 * 
 */
var ImageMapEditView = QuestionEditView.extend({
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
		                	  fieldName : "imageFileName",
		            	      description: Config.language.validateImageMap,
		                	  match : function(model) {
		                		  var imageFileName = model.get("imageFileName");
			                	  if (imageFileName != null) {
			                		  return true;
			                	  }
		                		  return false;
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
		ImageMapEditView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate(this.templateName);
		this.initCommons();
		this.registerCommons(this, QuestionAdditionalTextDecorator);
		this.registerCommons(this, QuestionDefaultValueDecorator);
		this.registerCommons(this, QuestionImageMapDecorator);
		this.registerCommons(this, QuestionFormatDecorator);
		this.registerCommons(this, QuestionValidationDecorator);
		this.registerCommons(this, QuestionGraphicDecorator);
		
	},
	
	events : $.extend({}, QuestionEditView.prototype.events,{
		"click .question-changeImageMap":"popImageMapEditorOnclick"
	}),
	
	render : function(model) {
		this.model = model;		
		this.$el.html(this.template(model.attributes));
		ImageMapEditView.__super__.render.call(this, model);
		this.$('select[name="questionType"] option').prop("disabled", true);
		this.resizeExistingInputs();
		return this;
	},
	
	resizeExistingInputs : function() {
		var $resize = this.$("#dialog_editQuestion_tab1_container1").find(".col-md-2");
		$resize.each(function(index) {
			if (index % 2 == 0) {
				$(this).removeClass("col-md-2").addClass("col-md-5");
			}
			else {
				$(this).removeClass("col-md-2").addClass("col-md-6");
			}
		});
	},
	
	popImageMapEditorOnclick : function(){
		FormBuilder.page.get("imageMapProcessView").render(this.model);
	}
});