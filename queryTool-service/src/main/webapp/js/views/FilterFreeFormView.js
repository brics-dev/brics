QT.FilterFreeFormView = QT.GenericQueryFilterView.extend({
	
	events : {
		"click .filterToggle" : "toggleFilterBody",
		"click .filterClose" : "closeFilter"
	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("filterFreeForm");
		QT.FilterFreeFormView.__super__.initialize.call(this);
		
	},
	
	render : function($container) {
		
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		QT.FilterFreeFormView.__super__.render.call(this);
		this.populateFormStructureTitleAndElement();
	},
	
	fillData : function() {
		var freeFormVal = this.model.get("selectedFreeFormValue");
		if (freeFormVal) {
			this.$(".filterFreeFormTextBox").val(freeFormVal);
		}
	}

});