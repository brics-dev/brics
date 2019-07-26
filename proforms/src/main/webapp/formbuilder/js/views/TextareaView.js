/**
 * 
 */
var TextareaView  = QuestionView.extend({
	className : "question textbox formGrid-1",
	
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