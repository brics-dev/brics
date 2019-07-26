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
		var contentHtml = TemplateManager.getTemplate("editQuestionGraphicTab");
		this.addTab(linkHtml, contentHtml);
		/*added by Ching Heng*/
		if(model.get("graphicNames") == null || model.get("graphicNames") == ""){
			this.$('#graphicFr').attr("src",baseUrl+"/question/showQuestionImage.action");			
		}else{
			this.$('#graphicFr').attr("src",baseUrl+"/question/showQuestionImage.action?questionId="+model.get("questionId"));
		}
	},
	
	config : {
		name : "QuestionGraphicDecorator",
		render : "renderGraphicCommons"
	}
};