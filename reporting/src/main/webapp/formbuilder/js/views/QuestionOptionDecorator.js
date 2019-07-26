/**
 * Effectively a snippet class that will be merged into the concrete Question
 * View class.  The properties here will NOT overwrite anything in the concrete
 * class.
 * 
 * 
 */
var QuestionOptionDecorator = {
	// note: horizDisplayBreak and horizontalDisplay are only enabled for
	// select, multi-select, radio, and checkbox
	commonsName : "QuestionOptionDecorator",
	events : {
		"click .addOption":"addOptionClick",
		"click .editButton":"editOptionClick",
		"click .deleteButton":"deleteOptionClick"	
	},
	optionEditMode: false,
	//add validation 
	validationRules : [
	    
	     new ValidationRule({  
    	   fieldName : "divOptions",
	       description: Config.language.validateOptionsSet,
    	   match : function(model) {
    		   var questionOptionsObjectArray = model.get("questionOptionsObjectArray");
    		   if(questionOptionsObjectArray.length > 0) {
    			   return true;
    		   }else {
    			   return false;
    		   }
    	   }
	      }),		 
	      new ValidationRule({  
	    	   fieldName : "divOptions",
		       description: Config.language.validateAllOrNoneScore,
	    	   match : function(model) {
	    		   var type = model.get("questionType");
	    		   if (type === Config.questionTypes.select || type === Config.questionTypes.radio) {
	    			   var questionOptionsObjectArray = model.get("questionOptionsObjectArray");
	    			   if(questionOptionsObjectArray.length > 0) {

	    				   var emptyScore=0;
	    				   var valueScore=0;
	    				   
	    				   for(var i=0;i<questionOptionsObjectArray.length;i++) {
	    					   var questionOptionsObject = questionOptionsObjectArray[i];
							   var score = questionOptionsObject.score;
	    					   if(score == null || score == "" ) {
	    						   emptyScore = 1;
	    					   }else {
	    						   valueScore = 1;
	    					   }  
	    				   }
	    				   if(emptyScore == valueScore) {
	    					   return false;
	    				   }else {
	    					   return true;
	    				   }
	    			   }   
	    		   }
	    		   return true;
	    		   
	    	   }
	      }),
	      new ValidationRule({  
	    	  fieldName : "divOptions",
		       description: "You have an unadded option. Please add the option or remove it.",
		       match : function(model) {
		    	   var type = model.get("questionType");
		    	   var activeEditor = FormBuilder.page.get("activeEditorView");
		    	   if (type === Config.questionTypes.select || type === Config.questionTypes.radio) {
		    		   var oChoice = activeEditor.$(".optionChoiceInput").val().trim();
		    		   var oScore = activeEditor.$(".optionScoreInput").val().trim();
		    		   var oSub = activeEditor.$(".optionSubmittedValueInput").val().trim();
		    		   if(oChoice != "" || oScore != "" || oSub != "") {
		    			   return false;
		    		   }else {
		    			   return true;
		    		   }
		    		   
		    	   }else {
		    		   var oChoice = activeEditor.$(".optionChoiceInput").val().trim();
		    		   var oSub = activeEditor.$(".optionSubmittedValueInput").val().trim();
		    		   if(oChoice != "" ||  oSub != "") {
		    			   return false;
		    		   }else {
		    			   return true;
		    		   }
		    		   
		    	   }

	    	   }
	      })
	      
	],
	/**
	 * Renders this commons to the editor
	 * 
	 * @param model the question model
	 */
	renderOptionCommons : function(model) {
		// the below shows an example of how to listen for changes to the model
		// this.model.on("change:repeatable", this.showHideRepeatable, this);
		var template;
		var type = model.get("questionType");
		var tempThis = this;
		this.optionEditMode = false;

		if(type === Config.questionTypes.radio || type === Config.questionTypes.select){
			if(this.$("#dialog_editQuestion_tab1_container1").length < 1) {
				var container1 = "<div id='dialog_editQuestion_tab1_container1' class='col-md-5'></div>";
				this.$("#dialog_editQuestion_tab1 .row").wrapAll(container1);
				this.resizeExistingInputs();
				var container2 = "<div id='dialog_editQuestion_tab1_container2' class='col-md-7'></div>";
				this.$("#dialog_editQuestion_tab1").append(container2);
				template = TemplateManager.getTemplate("editQuestionOptions");
				this.$("#dialog_editQuestion_tab1_container2").html(template(model.attributes));
				//this.addInputOptionsRow();
				this.$( ".divOptions" ).sortable({
					items : ".optionRow",
					stop: function( event, ui ) {
						//update model with new order which will render also
						tempThis.updateModelQuestionOptions();
					}
				});
			}
		}else if(type === Config.questionTypes.checkbox || type === Config.questionTypes.multiSelect){
			if(this.$("#dialog_editQuestion_tab1_container1").length < 1) {
				var container1 = "<div id='dialog_editQuestion_tab1_container1' class='col-md-5'></div>";
				this.$("#dialog_editQuestion_tab1 .row").wrapAll(container1);
				this.resizeExistingInputs();
				var container2 = "<div id='dialog_editQuestion_tab1_container2' class='col-md-7'></div>";
				this.$("#dialog_editQuestion_tab1").append(container2);
				template = TemplateManager.getTemplate("editQuestionOptions_noScore");
				this.$("#dialog_editQuestion_tab1_container2").html(template(model.attributes));
				this.$( ".divOptions" ).sortable({
					items : ".optionRow",
					stop: function( event, ui ) {
						//update model with new order which will render also
						tempThis.updateModelQuestionOptions();
					}
				});
			}
		}
		this.$("#dialog_editQuestion_tab1").append("<div class='clearfix'></div>");
		this.ellipsis(this.$(".optionRow"),100);
		

	},
	
	addInputOptionsRow: function() {
		
		var inputOptionsRowTemplate = TemplateManager.getTemplate("editQuestionInputOptionsRow");
		var element = this.$(".questionOptionsContainer");
		var html = inputOptionsRowTemplate();
		element.empty().append(html);
		
		
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
	
	config : {
		name : "QuestionOptionDecorator",
		render : "renderOptionCommons"
	},
	
	
	addOptionClick : function(event) {
		//first do whatever checks that need to get done
		var validate = this.validateOptions();
		
		//if it doesnt validate, do not add the option
		if(!validate) {
			return;
		}

		//passed validation..so add option
		var optionRowTemplate;
		var optionRowHtml = "";
		
		 
		var type = this.model.get("questionType");
		
		if (type === Config.questionTypes.select || type === Config.questionTypes.radio) {
			
			optionRowTemplate = TemplateManager.getTemplate("editQuestionOptionsRow");
			
			optionRowHtml = optionRowTemplate({
				option : this.$(".optionChoiceInput").val().trim(),
				score : this.$(".optionScoreInput").val().trim(),
				submittedValue : this.$(".optionSubmittedValueInput").val().trim()
			});

			//this inserts new optionrowhtml before the inputrow and since we are doing inline editing, the next line detaches the inputrow and puts it at the botton
			this.$(".inputOptionsRow").before(optionRowHtml);
			this.$(".inputOptionsRow").hide();
			//this.$(".inputOptionsRow").detach().appendTo(this.$( ".divOptions" ));
			
			this.$(".optionChoiceInput").val('');
			this.$(".optionScoreInput").val('');
			this.$(".optionSubmittedValueInput").val('');
			
			this.updateModelQuestionOptions();	
		}else if(type === Config.questionTypes.multiSelect || type === Config.questionTypes.checkbox) {
			
			optionRowTemplate = TemplateManager.getTemplate("editQuestionOptionsRow_noScore");
			
			optionRowHtml = optionRowTemplate({
				option : this.$(".optionChoiceInput").val().trim(),
				submittedValue : this.$(".optionSubmittedValueInput").val().trim()
			});
			
			//this inserts new optionrowhtml before the inputrow and since we are doing inline editing, the next line detaches the inputrow and puts it at the botton
			this.$(".inputOptionsRow").before(optionRowHtml);
			this.$(".inputOptionsRow").hide();
			//this.$(".inputOptionsRow").detach().appendTo(this.$( ".divOptions" ));
			
			
			this.$(".optionChoiceInput").val('');
			this.$(".optionSubmittedValueInput").val('');
			
			this.updateModelQuestionOptions();
		}
		
		var d = this.$('.divOptions');
		d.scrollTop(d.prop("scrollHeight"));
		this.optionEditMode=false;
	},
	
	validateOptions : function() {
		
		//get question type
		var type = this.model.get("questionType");
		 var questionOptionsObjectArray = this.model.get("questionOptionsObjectArray");

		//set up temp model to validate against
		this.updateModelQuestionOptions(); // update the option array list which doesn't contain the active row
		var tempOptionModel = new Backbone.Model();
		tempOptionModel.set("optionChoice",this.$(".optionChoiceInput").val().trim());
		if (type === Config.questionTypes.select || type === Config.questionTypes.radio) {
			tempOptionModel.set("optionScore",this.$(".optionScoreInput").val().trim());	
		}
		
		tempOptionModel.set("optionSubmittedValue",this.$(".optionSubmittedValueInput").val().trim());
		tempOptionModel.set("questionOptionsObjectArray",  questionOptionsObjectArray);

		//set up validation rules
		var optionValidationRules = [];
		var rule1 =  new ValidationRule({
     	   fieldName: "optionChoice", 
    	   required: true, 
    	   description: Config.language.validateRequiredOption
         });
		var rule2 = new ValidationRule({
			   fieldName : "optionScore",
			   description : Config.language.validateNumericScore,
			   match : function (model) {
				   if(model.get("optionScore") == null || model.get("optionScore") == "") {
					   return true;
				   }else {
					   if(isNaN(model.get("optionScore"))) {
						   return false;
					   }else {
						   return true;
					   }
				   } 
			   }
		 });
		
		var rule3  = new ValidationRule({  
	    	   fieldName : "divOptions",
		       description: Config.language.validateDuplicatedOption,
	    	   match : function(model) {
 				var questionOptionsObjectArray = model.get("questionOptionsObjectArray");
	    		  
	    		   var newOptionText = model.get("optionChoice");
	    		   if(questionOptionsObjectArray.length > 0) {
	    			   
	    			   for(var i=0;i<questionOptionsObjectArray;i++) {
	    				   var questionOptionsObject = questionOptionsObjectArray[i];
	    				   
	    				   var option = questionOptionsObject.option;
	    				  
	    				   if(newOptionText != null && option.trim() != null 
	    						   && newOptionText.toLowerCase() == option.trim().toLowerCase()){
	    					   return false;
	    				   }
	    			   }
	    		   }
	    		   return true;
	    	   }
	      });
		
		
		
		var rule4 = new ValidationRule({
			   fieldName : "optionChoice",
			   description : "Option Text can not contain |",
			   match : function (model) {
				  var optionChoice = model.get("optionChoice");
				   if(optionChoice == null || optionChoice == "") {
					   return true;
				   }else {
					  if(optionChoice.indexOf("|") > -1) {
						  return false;
					  }else {
						  return true;
					  }
				   } 
			   }
		 });
		
		
		var rule5 = new ValidationRule({
			   fieldName : "optionSubmittedValue",
			   description : "Option Submitted Value can not contain |",
			   match : function (model) {
				  var optionSubmittedValue = model.get("optionSubmittedValue");
				   if(optionSubmittedValue == null || optionSubmittedValue == "") {
					   return true;
				   }else {
					  if(optionSubmittedValue.indexOf("|") > -1) {
						  return false;
					  }else {
						  return true;
					  }
				   } 
			   }
		 });
		
		

		optionValidationRules.push(rule1);
		if (type === Config.questionTypes.select || type === Config.questionTypes.radio) {
			optionValidationRules.push(rule2);
			optionValidationRules.push(rule3);
			optionValidationRules.push(rule4);
			optionValidationRules.push(rule5);
			
		}
		else if (type === Config.questionTypes.multiSelect || type === Config.questionTypes.checkbox) {
			optionValidationRules.push(rule3);
			optionValidationRules.push(rule4);
			optionValidationRules.push(rule5);
		}		
		//perform the validation
		var validate = this.validateEditor(optionValidationRules,tempOptionModel);
		
		//return validation result
		return validate;
	},
	
	editOptionClick : function(event) {
		var type = this.model.get("questionType");
		if(this.optionEditMode){
			this.hideEditorWarning();
			this.showEditorWarning(Config.language.multipleEditing);
			return false;
		};
		if (type === Config.questionTypes.select || type === Config.questionTypes.radio) {
			$element = $(event.target);
			var optionRow =  $element.parents('.optionRow');
			var option = optionRow.find('.optionChoice').text();
			var score = optionRow.find('.optionScore').text();
			var submittedValue = optionRow.find('.optionSubmittedValue').text();
			this.$(".optionChoiceInput").val(option);
			this.$(".optionScoreInput").val(score);
			this.$(".optionSubmittedValueInput").val(submittedValue);

			this.$(".inputOptionsRow").detach().insertAfter(optionRow);
			this.$(".inputOptionsRow").show();
			
			optionRow.remove();
			// optionRow.find('.editButton').attr("href", "javascript:;"
		}else if(type === Config.questionTypes.multiSelect || type === Config.questionTypes.checkbox) {
			$element = $(event.target);
			var optionRow =  $element.parents('.optionRow');
			var option = optionRow.find('.optionChoice').text();
			var submittedValue = optionRow.find('.optionSubmittedValue').text();
			this.$(".optionChoiceInput").val(option);
			this.$(".optionSubmittedValueInput").val(submittedValue);
			
			this.$(".inputOptionsRow").detach().insertAfter(optionRow);
			this.$(".inputOptionsRow").show();
			
			optionRow.remove();
		}
		
		this.optionEditMode=true;
	},
	
	deleteOptionClick : function(event) {

		$element = $(event.target);
		var optionRow =  $element.parents('.optionRow');
		optionRow.remove();
		
		//update the questionOptionsHidden
		this.updateModelQuestionOptions();
	},
	
	updateModelQuestionOptions : function() {
		var type = this.model.get("questionType");

		var divOptions = this.$(".divOptions").children(".optionRow");
		
		var optionsArray = new Array();
		divOptions.each(function(index) {
			var $row = $(this);
			var option = $row.find('.optionChoice').text();
			var score = "";
			if (type === Config.questionTypes.select || type === Config.questionTypes.radio) {
				score = $row.find('.optionScore').text();
			}
			
			var submittedValue = $row.find('.optionSubmittedValue').text();
			
			var optionConfig = {};
			//optionConfig.orderVal=index+1;
			optionConfig.option = option;
			optionConfig.submittedValue = submittedValue;
			optionConfig.score = score;
			
			optionsArray.push(new OptionsObject(optionConfig));
			
		});
		
		this.model.set("questionOptionsObjectArray", optionsArray);
		EventBus.trigger("resize:question", this.model);
	}
};