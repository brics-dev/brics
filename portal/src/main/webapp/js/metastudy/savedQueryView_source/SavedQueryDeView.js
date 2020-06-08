/**
 * 
 */
SavedQueryViewEngine.SavedQueryDeView  = BaseView.extend({
	className : "viewQuery_deItem",
	initialize : function() {
		SavedQueryViewEngine.SavedQueryRgView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("deItem");
		
		EventBus.on("close:all", this.destroy, this);
		EventBus.on("change:activeStudy", this.destroy, this);
		EventBus.on("destroy:all", this.destroy, this);
	},
	
	render : function($container) {
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		
		if (this.model.get("filtered")) {
			this.$(".viewQuery_filterIcon").addClass("viewQuery_filtered");
			this.$(".viewQuery_filterValue").text(this.model.getRenderString());
		}
		else {
			this.$(".viewQuery_filterIcon").addClass("viewQuery_nonFilered");
		}
		
		SavedQueryViewEngine.SavedQueryDeView.__super__.render.call(this);
	}
});