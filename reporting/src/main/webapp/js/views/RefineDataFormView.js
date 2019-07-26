/**
 * 
 */
QT.RefineDataFormView = BaseView.extend({
	className : "dataCartForm draggable",
	
	events : {
		"click .form_cartIcon_remove" : "openRemoveDialog",
		"click" : "onActivate"
	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("refineDataForm");
		EventBus.on("removeFromDataCart", this.removeFromDataCart, this);
		EventBus.on("select:stepTab", this.resize, this);
		EventBus.on("change:filterPane", this.resize, this);
		EventBus.on("dataCart:countChange", this.checkExistence, this);
		
		QT.RefineDataFormView.__super__.initialize.call(this);
	},
	
	render : function($container) {
		this.$el.attr("id","refineDataForm_" + this.model.get("uri"));
		this.$el.attr("name", "title"); // sets up model binding for title
		this.$el.html(this.template(this.model.attributes));
		//this.$el.html(this.model.get("title"));
		$container.append(this.$el);
		this.resize();
		
		QT.RefineDataFormView.__super__.render.call(this);
	},
	
	removeFromDataCart : function(uri) {
		var view = this;
		if(uri == this.model.get("uri")) {
			this.$( ".draggable" ).each(function(index, item) {
				var $this = view.$el;
				if (typeof $this.draggable("instance") !== "undefined") {
					$this.draggable("destroy");
					view.destroy();
				}
			});
		}
	},
	
	resize : function() {
		//this.$el.css("width", "90%");
		//this.$el.width((this.$el.innerWidth() * .9) - 25); // 25 = padding-right
		var $this = this.$el;
		var paddingRight = Number($this.css("padding-right").replace("px", ""));
		$this.width(($this.parent().width() - paddingRight) * 0.9);
	},
	
	/**
	 * Checks to make sure this form is still in the data cart.  If not, destroy!
	 */
	checkExistence : function() {
		var formUri = this.model.get("uri");
		if (!QueryTool.page.get("dataCart").forms.get(formUri)) {
			this.destroy(); // destroy!
		}
	},
	
	onActivate : function() {
		// warning: this references an area outside of this.$el
		$(".refineDataFormContainer .dataCartForm").removeClass("dataCartActive");
		this.$el.addClass("dataCartActive");
		
		// run the query
		var thisUri = this.model.get("uri");
		var formUris = [thisUri];
		QueryTool.query.set("selectedForms", formUris);
		var obj = { notOnActivate:false, formUris:formUris };
		EventBus.trigger("runQuery", obj);
	},
	
	openRemoveDialog : function(event) {
		event.stopImmediatePropagation();
		var uriString = $(event.target).parent().attr("id");
		if (uriString) {
			var uri = uriString.replace("refineDataForm_","");
			var model = QueryTool.page.get("forms").get(uri);
			EventBus.trigger("open:dataCartRemove", model);
		}
	}
});