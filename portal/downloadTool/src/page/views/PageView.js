let PageView;
export default PageView = BaseView.extend({
	config: {},
	pageName: "",
	templateUrl: "",
	$container: null,

	initialize: function(params) {
		this.config = params.config;
		this.$container = $(params.container);
		PageView.__super__.initialize.call(this);
	},

	render: function() {
		this.isRendered = true;
		PageView.__super__.render.call(this);
	},

	renderTemplate: function(template, context, callback) {
		context.template = template;
		context.isRendered = true;
		context.$el.html(template(context.model.attributes));
		context.$container.append(context.$el);
		callback.call(context);
	}
});
