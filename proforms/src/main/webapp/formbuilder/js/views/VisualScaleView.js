var VisualScaleView  = QuestionView.extend({
	className : "question visualscale formGrid-1",
	
	initialize : function() {
		VisualScaleView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("visualScaleQuestionTemplate");
		this.listenTo(this.model, "change:vscaleWidth", this.changeWidth);
		this.listenTo(this.model, "change:horizDisplayBreak", this.changeHorizDisplayBreakVS);
	},
	render : function($after) {
		VisualScaleView.__super__.render.call(this, $after);
		this.changeHorizDisplayBreakVA();
		this.changeWidth();
	},
	changeWidth: function(){
		var width = this.model.get("vscaleWidth");
		this.$(".vsContainer").css("width", width + "mm");
	},
	changeHorizDisplayBreakVA : function() {
		var horizDispBreak = this.model.get("horizDisplayBreak");
		if (horizDispBreak) {
			this.$(".vsContainer").css("display", "block");
		}
		else {
			this.$(".vsContainer").css("display", "inline-block");
		}
	}

});