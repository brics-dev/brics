/**
 * 
 */
var ImageMapView  = QuestionView.extend({
	className : "question imageMap formGrid-1",
	
	initialize : function() {
		ImageMapView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("imageMapQuestionTemplate");
		
	}	
});








