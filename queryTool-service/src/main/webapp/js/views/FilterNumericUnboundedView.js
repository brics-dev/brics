/**
 * 
 */
QT.FilterNumericUnboundedView = QT.GenericQueryFilterView.extend({
	events : {
		"click .filterToggle" : "toggleFilterBody",
		"click .filterClose" : "closeFilter",
		"change .genericLogicSelect" : "onChangeGenericLogicSelect",
		"change .filterLogicSelect" : "onChangeFilterLogicSelect",
		"keyup .filterNumericRangeMinTextBox" : "onMinChange",
		"keyup .filterNumericRangeMaxTextBox" : "onMaxChange",
		"change .filterLogic" : "onChangeCombLogic",
		"change .filterLogicBeforeTwo,.filterLogicBeforeOne,.filterLogicAfterOne,.filterLogicAfterTwo" : "onChangeGroupingContainer",
		"click .includeBlanksCheckbox" : "updateBlanks",
	},

	initialize : function() {
		this.template = TemplateManager.getTemplate("filterNumericUnbounded");
		QT.FilterNumericUnboundedView.__super__.initialize.call(this);
		this.listenTo(this.model, "change:selectedMinimum", this.onModelChange);
		this.listenTo(this.model, "change:selectedMaximum", this.onModelChange);
	},
	
	render : function($container) {
		this.$el.html(this.logicBeforeTemplate() + this.genericTemplate(this.model.attributes) + this.logicAfterTemplate());
		$container.append(this.$el);
		
		this.onRenderHandleSubFilters();
		
		QT.FilterNumericUnboundedView.__super__.render.call(this);
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
		
		// we just added the last instance of .filterLogicRow to this.$, so we can operate on it
		var $container = this.$(".filterLogicRow").last();
		
		// pre-populates the input fields
		var minimum = subFilterModel.get("minimum");
		var maximum = subFilterModel.get("maximum");
		var selectedMin = subFilterModel.get("selectedMinimum");
		var selectedMax = subFilterModel.get("selectedMaximum");
		if(selectedMin == null) {
			selectedMin = minimum;
		}
		if(selectedMax == null) {
			selectedMax = maximum;
		}

		subFilterModel.set("selectedMinimum",selectedMin);
		$container.find(".filterNumericRangeMinTextBox").val(selectedMin);
		subFilterModel.set("selectedMaximum",selectedMax);
		$container.find(".filterNumericRangeMaxTextBox").val(selectedMax);
	},
	
	/**
	 * Responds to UI events that change the min vaue.  NOT model changes
	 */
	onMinChange : _.debounce(function(event, ui) {
		this.onTextChange($(event.target), "selectedMinimum");
	}, 300),
	
	/**
	 * Responds to UI events that change the max value.  NOT model changes
	 */
	onMaxChange : _.debounce(function(event, ui) {
		this.onTextChange($(event.target), "selectedMaximum");
	}, 300),
	
	/**
	 * Handles changing the given model field with the given target's value
	 * 
	 * @param $target the changing target input field
	 * @param the model field to assign to the target's value
	 */
	onTextChange : function($target, modelField) {
		this.closeError();
		var value = this.validateNumeric($target.val());
		if (value !== false) {
			// this will kick the process over to onModelChange()
			this.getThisModel($target).set(modelField, value);
		}
		EventBus.trigger("populateQueryBox");
	},
	
	/**
	 * Responds to the model changing either the minimum or maximum
	 * note: this view is unbounded so we don't need to compare max and min
	 */
	onModelChange : function(changingModel) {
		var changingValue;
		var changingField;
		
		// determine which value is changing and validate that as its own value
		if (typeof changingModel.changed.selectedMinimum != "undefined") {
			// minimum is changing
			changingValue = changingModel.changed.selectedMinimum;
			changingField = "selectedMinimum";
		}
		if (typeof changingModel.changed.selectedMaximum != "undefined") {
			// maximum is changing
			changingValue = changingModel.changed.selectedMaximum;
			changingField = "selectedMaximum";
		}
		
		// validate that the value is numeric and make sure max is greater than min
		if (!this.validateNumeric(changingValue) || !this.validateMaxMinDifference(changingModel)) {
			changingModel.set(changingField, changingModel.previous(changingField), {silent: true});
		}
		EventBus.trigger("populateQueryBox");
	},
	
	/**
	 * Checks that the incoming value is a number and shows an error
	 * if it is not.
	 */
	validateNumeric : function(value) {
		if (isNaN(value)) {
			this.showError("The input values must be numeric");
			return false;
		}
		else {
			return Number(value);
		}
	},
	
	/**
	 * Checks that the minimum is less than the maximum.  If that check fails, 
	 * show an error and return false.
	 * 
	 * @param changingModel the model that is changing
	 * @return boolean true if input is valid, otherwise false
	 */
	validateMaxMinDifference : function(changingModel) {
		var min = changingModel.get("selectedMinimum");
		var max = changingModel.get("selectedMaximum");
		if (typeof min != "undefined" && typeof max != "undefined") {
			if (min > max) {
				this.showError("The minimum cannot be greater than the maximum");
				return false;
			}
		}
		return true;
	},
	
	/**
	 * Fills the on-page elements with values from this filter model (and sub-filters from
	 * their sub-models).
	 * 
	 * called from RefineDataFiltersView after initialize() and render() on this view
	 */
	fillData : function() {
		var model = this.model;
		QT.FilterNumericUnboundedView.__super__.fillData.call(this);
		var filters = model.get("filters");
		var $subFilters = this.$(".filterLogicRow");
		for (var i = 0, filterSize = filters.length; i < filterSize; i++) {
			var filter = filters.at(i);
			var $subFilter = $subFilters.eq(i);
			var min = filter.get("selectedMinimum");
			var max = filter.get("selectedMaximum");
			if (min) {
				$subFilter.find(".filterNumericRangeMinTextBox").val(min);
			}
			if (max) {
				$subFilter.find(".filterNumericRangeMaxTextBox").val(max);
			}
			$subFilter.find(".filterLogicSelect").val(filter.get("logicBefore"));
		}
	}

});


