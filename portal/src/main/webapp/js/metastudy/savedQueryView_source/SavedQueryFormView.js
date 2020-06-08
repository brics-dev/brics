/**
 * 
 */
SavedQueryViewEngine.SavedQueryFormView  = BaseView.extend({
	className : "viewQuery_formContainer",
	events : {
		"click .viewQuery_formHeader" : "formClickHandler"
	},
	
	initialize : function() {
		SavedQueryViewEngine.SavedQueryFormView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("formItem");
		
		EventBus.on("close:all", this.destroy, this);
		EventBus.on("change:activeStudy", this.destroy, this);
		EventBus.on("destroy:all", this.destroy, this);
	},
	
	render : function($container) {
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		
		SavedQueryViewEngine.SavedQueryFormView.__super__.render.call(this);
		this.determineFiltered();
		this.renderRgs();
	},
	
	formClickHandler : function() {
		var $container = this.$(".viewQuery_formData");
		if ($container.is(":visible")) {
			this.$(".viewQuery_plusMinus")
				.text(" + ")
				.removeClass("viewQuery_plus")
				.addClass("viewQuery_minus");
			$container.hide();
		}
		else {
			this.$(".viewQuery_plusMinus")
				.text(" - ")
				.removeClass("viewQuery_minus")
				.addClass("viewQuery_plus");
			$container.show();
		}
	},
	
	determineFiltered : function() {
		var $filterIcon = this.$(".viewQuery_formFiltered");
		if (this.model.get("filtered")) {
			$filterIcon.show().addClass("viewQuery_filtered");
		}
		else {
			$filterIcon.hide().addClass("viewQuery_nonFiltered");
		}
	},
	
	renderRgs : function() {
		var $container = this.$(".viewQuery_formData");
		var groups = this.model.savedQueryRgs;
		if (groups.length > 0) {
			groups.forEach(function(rg) {
				var rgView = new SavedQueryViewEngine.SavedQueryRgView({model: rg});
				rgView.render($container);
			});
		}
		else {
			$container.text("There are no visible data elements in this form");
		}
	}
	
});