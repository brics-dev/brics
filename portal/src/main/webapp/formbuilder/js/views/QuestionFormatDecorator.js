/**
 * Effectively a snippet class that will be merged into the concrete Question
 * View class.  The properties here will NOT overwrite anything in the concrete
 * class.
 * 
 */
var QuestionFormatDecorator = {
	// note: horizDisplayBreak and horizontalDisplay are only enabled for
	// select, multi-select, radio, and checkbox
	commonsName : "QuestionFormatDecorator",
	events : {
		
	},
	//add validation by Ching Heng
	validationRules : [
       new ValidationRule({  
    	   fieldName : "indent",
	       description: Config.language.validateIndent,
    	   match : function(model) {
    		   var indent = model.get("indent");
    		   var intRegex = /^[0-9]+$/igm;
    		   if(!(intRegex.test(indent)) || indent < 0) {
    			   return false;
    		   }else {
    			   return true;
    		   }
    	   }
      }),
      new ValidationRule({  
    	   fieldName : "indent",
	       description: Config.language.validateIndentValue,
	   	   match : function(model) {
	   		   var indent = model.get("indent");
	   		   if(indent > 50) {
	   			   return false;
	   		   }else {
	   			   return true;
	   		   }
	   	   }
     })
	],
	
	/**
	 * Renders this commons to the editor
	 * 
	 * @param model the question model
	 */
	renderFormatCommons : function(model) {
		// the below shows an example of how to listen for changes to the model
		// this.model.on("change:repeatable", this.showHideRepeatable, this);
		var linkHtml = TemplateManager.getTemplate("editQuestionFormattingTabLabel");
		var contentHtml = TemplateManager.getTemplate("editQuestionFormattingTab");
		this.addTab(linkHtml, contentHtml);
		
		if (model.get("questionType") == Config.questionTypes.radio || model.get("questionType") == Config.questionTypes.checkbox) {
			this.$(".questionFormatHorizontalDisplay").show();
			this.$(".questionFormatHorizontalDisplayBreak").show();
		}
		else {
			this.$(".questionFormatHorizontalDisplay").hide();
			this.$(".questionFormatHorizontalDisplayBreak").hide();
		}
	},
	
	config : {
		name : "QuestionFormatDecorator",
		render : "renderFormatCommons"
	}
};