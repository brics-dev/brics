/**
 * 
 */
var FileUploadView  = QuestionView.extend({
	className : "question formGrid-1",
	typeClassName : "file-upload",
	
	initialize : function() {
		FileUploadView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("fileUploadQuestionTemplate");
		

	}
});