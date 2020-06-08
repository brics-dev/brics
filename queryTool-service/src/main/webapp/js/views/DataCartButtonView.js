/**
 * 
 */
QT.DataCartButtonView = BaseView.extend({

	events : {
		"click" : "goToRefineData"
	},

	initialize : function() {
		this.template = TemplateManager.getTemplate("dataCartButton");
		QT.DataCartButtonView.__super__.initialize.call(this);
		EventBus.on("dataCart:countChange", this.onDataCartCountChange, this);
	},

	render : function() {
		this.$el.html(this.template(this.model.attributes));
		QT.DataCartButtonView.__super__.render.call(this);
		this.enableDisable();
	},

	goToRefineData : function() {
		if (this.model.forms.length > 0) {
			EventBus.trigger("change:stepTab", "stepTwoTab");
		}
	},
	
	onDataCartCountChange : function() {
		this.enableDisable();
		this.flash();
	},
	
	enableDisable : function() {
		var $button = this.$(".dataCartButton");
		if (this.model.get("countForms") == 0) {
			$button.addClass("disabled");
		}
		else {
			$button.removeClass("disabled");
		}
	},
	
	flash : _.debounce(function(fadeTime) {
		fadeTime = (!!fadeTime) ? fadeTime : 100;
		this.$el
		.fadeOut(fadeTime).fadeIn(fadeTime)
		.fadeOut(fadeTime).fadeIn(fadeTime)
		.fadeOut(fadeTime).fadeIn(fadeTime);
	}, 1000, true)
});
