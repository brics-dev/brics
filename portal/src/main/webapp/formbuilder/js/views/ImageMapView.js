/**
 * 
 */
var ImageMapView  = QuestionView.extend({
	className : "question formGrid-1",
	typeClassName : "imageMap",
	
	initialize : function() {
		ImageMapView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("imageMapQuestionTemplate");
		
	}	
});








