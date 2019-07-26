/**
 * 
 */
SavedQueryViewEngine.SavedQueryView  = BaseView.extend({
	
	events : {
		"click .viewQuery_queryHeader" : "onOpenCloseQueryDetailsClick"
	},
	
	initialize : function() {
		SavedQueryViewEngine.SavedQueryView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("container");
		
		EventBus.on("close:all", this.close, this);
		EventBus.on("destroy:all", this.destroy, this);
	},
	
	render : function(model) {
		// closes all current views
		EventBus.trigger("close:all");
        var html = $('#container').html();
        var template = Handlebars.compile(html);
        this.$el.html(template(this.model.attributes));
		
		// start up the magic - by appending to the main container
		$(".viewQueryContainer").append(this.$el);
		
		var studies = this.model.studies;
		var $container = this.$(".viewQuery_studiesContainer");
		studies.forEach(function(study) {
			var studyView = new SavedQueryViewEngine.SavedQueryStudyView({model: study});
			studyView.render($container);
		});
		
	},
	
	onOpenCloseQueryDetailsClick : function() {
		var $container = this.$(".viewQuery_queryDetails");
		if ($container.is(":visible")) {
			this.$(".viewQuery_detailsPlusMinus")
				.text(" + ")
				.removeClass("viewQuery_plus")
				.addClass("viewQuery_minus");
			$container.hide();
		}
		else {
			this.$(".viewQuery_detailsPlusMinus")
				.text(" - ")
				.removeClass("viewQuery_minus")
				.addClass("viewQuery_plus");
			$container.show();
		}
	}
	
	
});