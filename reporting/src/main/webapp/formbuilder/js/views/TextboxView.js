/**
 * 
 */
var TextboxView  = QuestionView.extend({
	className : "question textbox formGrid-1",
	
	initialize : function() {
		TextBlockView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("textQuestionTemplate");
		this.listenTo(this.model, "change:defaultValue", this.onChangeDefaultValue);
	},
	
	
	render : function($after) {
		RadioView.__super__.render.call(this, $after);
		this.onChangeDefaultValue();
	},
	
	onChangeDefaultValue:function(){
		this.$('input[type="text"]').val(this.model.get("defaultValue"));
	}
});