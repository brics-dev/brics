QT.FilterFreeFormView = QT.GenericQueryFilterView.extend({
	
	events : {
		"click .filterToggle" : "toggleFilterBody",
		"click .filterClose" : "closeFilter",
		"change .genericLogicSelect" : "onChangeGenericLogicSelect",
		"change .filterLogicSelect" : "onChangeFilterLogicSelect",
		"change .filterFreeFormTextBox" : "onUpdateInput",
		"change .filterLogic" : "onChangeCombLogic",
		"click .filterMode": "updateFilterMode",
		"change .filterLogicBeforeTwo,.filterLogicBeforeOne,.filterLogicAfterOne,.filterLogicAfterTwo" : "onChangeGroupingContainer",
		"click .includeBlanksCheckbox" : "updateBlanks",
	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("filterFreeForm");
		QT.FilterFreeFormView.__super__.initialize.call(this);
	},
	
	render : function($container) {
		this.$el.html(this.logicBeforeTemplate() + this.genericTemplate(this.model.attributes) + this.logicAfterTemplate());
		$container.append(this.$el);
		
		this.onRenderHandleSubFilters();
		
		QT.FilterFreeFormView.__super__.render.call(this);
		this.populateFormStructureTitleAndElement();
	},
	
	/**
	 * Renders a single sub-filter by model.  This handles all business logic for that
	 * rendering.
	 * 
	 * @param subFilterModel Model describing the sub-filter
	 */
	renderSubFilter : function(subFilterModel) {
		var $filterLogicContainer = this.$(".filterLogicContainer");
		// if this is the first sub-filter, the logic options "and", "or" should be disabled
		if (this.model.get("filters").length == 1) {
			subFilterModel.set('showFilterLogicOr',false);
			subFilterModel.set('showFilterLogicAnd',false);
		}
		$filterLogicContainer.find(".genericLogicSelect").before(this.template(subFilterModel.attributes));
		
	},
	
	/**
	 * There's only one view per data element which could cover multiple inputs.
	 * So, find out which one was changed, then update that model
	 */
	onUpdateInput : function(event) {
		var $target = $(event.target);
		var index = this.$(".filterFreeFormTextBox").index($target);
		this.getSubFilterAt(index).set("selectedFreeFormValue", $target.val());
		EventBus.trigger("populateQueryBox");
	},
	
	/**
	 * Fills the on-page elements with values from this filter model (and sub-filters from
	 * their sub-models).
	 * 
	 * called from RefineDataFiltersView after initialize() and render() on this view
	 */
	fillData : function() {
		var model = this.model;
		QT.FilterFreeFormView.__super__.fillData.call(this);
		var filters = model.get("filters");
		var $subFilters = this.$(".filterLogicRow");
		for (var i = 0, filterSize = filters.length; i < filterSize; i++) {
			var filter = filters.at(i);
			var $subFilterContainer = $subFilters.eq(i);
			$subFilterContainer.find(".filterFreeFormTextBox").val(filter.get("selectedFreeFormValue") || "");
			$subFilterContainer.find(".filterLogicSelect").val(filter.get("logicBefore"));
		}
	}
});