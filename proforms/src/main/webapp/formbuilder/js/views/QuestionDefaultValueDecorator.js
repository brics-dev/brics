/**
 * Effectively a snippet class that will be merged into the concrete Question
 * View class.  The properties here will NOT overwrite anything in the concrete
 * class.
 * 
 */
var QuestionDefaultValueDecorator = {
	commonsName : "QuestionDefaultValueDecorator",
	events : {
		
	},
	
	/**
	 * Renders this commons to the editor
	 * 
	 * @param model the question model
	 */
	renderDefaultValueCommons : function(model) {
		// the below shows an example of how to listen for changes to the model
		// this.model.on("change:repeatable", this.showHideRepeatable, this);
		var linkHtml = TemplateManager.getTemplate("editQuestionDefaultValueTabLabel");
		var contentHtml = TemplateManager.getTemplate("editQuestionDefaultValueTab");
		this.addTab(linkHtml, contentHtml);
	},
	
	config : {
		name : "QuestionDefaultValueDecorator",
		render : "renderDefaultValueCommons"
	},
	
	
	
	validationRules : [
	                   new ValidationRule({
	                	   fieldName : "defaultValue",
		             	   description : "Default Value must match one of the options",
		             	  match : function(model) {
		             		 if(model.get("questionType") == 4 || model.get("questionType") == 5 || model.get("questionType") == 6 || model.get("questionType") == 7) {
				             	var defaultValue =  FormBuilder.page.get("activeEditorView").$("#defaultValue").val();
				             			 
				             			
				             	if(defaultValue.trim() != "") {		
				             			var questionOptionsObjectArray = model.get("questionOptionsObjectArray");
				             			if(questionOptionsObjectArray.length > 0) {
											for(var i=0;i<questionOptionsObjectArray.length;i++) {
												var questionOptionsObject = questionOptionsObjectArray[i];
												var option = questionOptionsObject.option.trim();
												if(defaultValue.trim() == option) {
				             						  return true;
				             					}
											}
											return false;
										}else {
											return true;
										}	
				             	}else {
				             		return true;
				             	}
		             			 
		             		 }
		             		 return true;
		             	  }  
	                   })     
	                   
	]
};