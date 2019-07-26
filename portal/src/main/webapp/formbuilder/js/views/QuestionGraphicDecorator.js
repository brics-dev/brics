/**
 * Effectively a snippet class that will be merged into the concrete Question
 * View class.  The properties here will NOT overwrite anything in the concrete
 * class.
 * 
 */
var QuestionGraphicDecorator = {
	// Created by Ching Heng 
	commonsName : "QuestionGraphicDecorator",
	events : {
		
	},
	
	/**
	 * Renders this commons to the editor
	 * 
	 * @param model the question model
	 */
	renderGraphicCommons : function(model) {
		// the below shows an example of how to listen for changes to the model
		// this.model.on("change:repeatable", this.showHideRepeatable, this);
		var linkHtml = TemplateManager.getTemplate("editQuestionGraphicTabLabel");
		
		var contentHtml;
		if(FormBuilder.form.get("isCAT") && FormBuilder.form.get("measurementType") != 'shortForm'){
			contentHtml = TemplateManager.getTemplate("editQuestionGraphicTabForCAT");
		}else{
			contentHtml = TemplateManager.getTemplate("editQuestionGraphicTab");
		}
		
		this.addTab(linkHtml, contentHtml);
		
		var url = "";
		/*added by Ching Heng*/
		if(model.get("graphicNames") == null || model.get("graphicNames") == ""){
			url = baseUrl+"showQuestionImage!showQuestionImage.ajax";	
		}else{
			url = baseUrl+"showQuestionImage!showQuestionImage.ajax?questionId="+model.get("questionId")+"&imageNames="+this.model.get("graphicNames");	
		}
		
		url = url.replace("portal//","portal/");
		this.$('#graphicFr').attr("src",url);
		
	},
	
	config : {
		name : "QuestionGraphicDecorator",
		render : "renderGraphicCommons"
	}
};