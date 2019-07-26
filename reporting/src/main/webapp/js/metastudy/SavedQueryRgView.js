/**
 * 
 */
SavedQueryViewEngine.SavedQueryRgView  = BaseView.extend({
	className : "viewQuery_rgContainer",
	events : {

	},
	
	initialize : function() {
		SavedQueryViewEngine.SavedQueryRgView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("rgItem");
		
		EventBus.on("close:all", this.destroy, this);
		EventBus.on("change:activeStudy", this.destroy, this);
		EventBus.on("destroy:all", this.destroy, this);
	},
	
	render : function($container) {
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		
		SavedQueryViewEngine.SavedQueryRgView.__super__.render.call(this);
		this.renderDes();
		
	},
	
	renderDes : function() {
		var $container = this.$(".viewQuery_deListContainer");
		var des = this.model.savedQueryDes;
		des.forEach(function(de) {
			var deView = new SavedQueryViewEngine.SavedQueryDeView({model: de});
			deView.render($container);
		});
	}
	
});