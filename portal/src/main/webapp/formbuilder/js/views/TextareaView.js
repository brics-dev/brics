/**
 * 
 */
var TextareaView  = QuestionView.extend({
	className : "question formGrid-1",
	typeClassName : "textarea",
	
	initialize : function() {
		TextareaView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("textareaQuestionTemplate");
		this.listenTo(this.model, "change:defaultValue", this.onChangeDefaultValue);
	},
	
	
	render : function($after) {
		RadioView.__super__.render.call(this, $after);
		this.onChangeDefaultValue();
	},
	
	onChangeDefaultValue:function(){
		this.$('textarea').val(this.model.get("defaultValue"));
	}
});