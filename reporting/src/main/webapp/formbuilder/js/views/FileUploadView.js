/**
 * 
 */
var FileUploadView  = QuestionView.extend({
	className : "question file-upload formGrid-1",
	
	initialize : function() {
		FileUploadView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("fileUploadQuestionTemplate");
		

	}
});