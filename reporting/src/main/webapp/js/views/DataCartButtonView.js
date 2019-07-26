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
	},

	render : function() {
		this.$el.html(this.template(this.model.attributes));
		QT.DataCartButtonView.__super__.render.call(this);
	},

	goToRefineData : function() {
		if (this.model.forms.length > 0) {
			EventBus.trigger("change:stepTab", "stepTwoTab");
		}
	}
});
