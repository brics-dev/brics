/**
 * 
 */
QT.FilterNumericRangeView = QT.GenericQueryFilterView.extend({
	
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
		this.template = TemplateManager.getTemplate("filterNumericRange");
		QT.FilterNumericRangeView.__super__.initialize.call(this);
		this.listenTo(this.model,"change:selectedMinimum",this.onMinChange);
		this.listenTo(this.model,"change:selectedMaximum",this.onMaxChange);
	},
	
	
	render : function($container) {
		this.$el.html(this.logicBeforeTemplate() + this.genericTemplate(this.model.attributes) + this.logicAfterTemplate());
		$container.append(this.$el);
		
		this.onRenderHandleSubFilters();
		
		QT.FilterNumericRangeView.__super__.render.call(this);
		this.populateFormStructureTitleAndElement();
	},
	
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

		var minimum = subFilterModel.get("minimum");
		var maximum = subFilterModel.get("maximum");
		var selectedMin = subFilterModel.get("selectedMinimum");
		var selectedMax = subFilterModel.get("selectedMaximum");
		if(!selectedMin) {
			selectedMin = minimum;
		}
		if(!selectedMax) {
			selectedMax = maximum;
		}
		
		var view = this;
		$container.find(".filterNumericRangeSlider").slider({
			range : true,
			min : minimum,
			max : maximum,
			values : [selectedMin,selectedMax],
			slide: function(event, ui) {view.onSlideChange.call(view, event, ui);}
		});

		subFilterModel.set("selectedMinimum",selectedMin);
		$container.find(".filterNumericRangeMinTextBox").val(selectedMin);
		subFilterModel.set("selectedMaximum",selectedMax);
		$container.find(".filterNumericRangeMaxTextBox").val(selectedMax);
	},
	
	onMinChange : _.debounce(function(event, ui) {
		var model = null;
		var minimum = null;
		if (typeof event.cid !== "undefined") {
			// the first parameter is a model, this is responding to the model change
			model = event;
			minimum = model.get("selectedMinimum");
		}
		else {
			// otherwise, it's a UI element and we need to SET the minimum from the UI
			var $target = $(event.target);
			if (isNaN($target.val())) {
				this.showError("The maximum must be numeric");
			}
			else {
				minimum = Number($target.val());
				model = this.getThisModel($target);
			}
		}
		model.set("selectedMinimum", minimum);

		this.closeError();
		if (this.validateMinimum(model) && this.validateMaxMinDifference(model)) {
			this.updateSlider(model);
		}
		EventBus.trigger("populateQueryBox");
	}, 300),
	
	onMaxChange : _.debounce(function(event, ui) {
		var model = null;
		var maximum = null;
		if (typeof event.cid !== "undefined") {
			// the first parameter is a model, this is responding to the model change
			model = event;
			maximum = model.get("selectedMaximum");
		}
		else {
			// otherwise, it's a UI element and we need to SET the minimum from the UI
			var $target = $(event.target);
			if (isNaN($target.val())) {
				this.showError("The maximum must be numeric");
			}
			else {
				maximum = Number($target.val());
				model = this.getThisModel($target);
			}
		}
		model.set("selectedMaximum", maximum);
		
		this.closeError();
		if (this.validateMaximum(model) && this.validateMaxMinDifference(model)) {
			this.updateSlider(model);
		}
		EventBus.trigger("populateQueryBox");
	}, 300),
	
	/**
	 * Responds to a change in the slider.  Set up in the sub-filter render
	 */
	onSlideChange : function(event, ui) {
		var $target = $(event.target);
		var $logicRow = $target.parents(".filterLogicRow").first();
		var index = this.$(".filterNumericRangeSlider").index($target);
		var model = this.getSubFilterAt(index);
		var sliderMin = ui.values[0];
		var sliderMax = ui.values[1];
		
		model.set("selectedMinimum", sliderMin);
		this.$(".filterNumericRangeMinTextBox").eq(index).val(sliderMin);
		model.set("selectedMaximum", sliderMax);
		this.$(".filterNumericRangeMaxTextBox").eq(index).val(sliderMax);
		EventBus.trigger("populateQueryBox");
	},
	
	validateMinimum : function(changingModel) {
		var index = this.model.get("filters").indexOf(changingModel);
		// if we don't find the model in filters, then it's the parent model and we 
		// don't need to validate the incoming values
		if (index >= 0) {
			var min = Number(changingModel.get("selectedMinimum"));
			var modelMin = changingModel.get("minimum");
			var validationMinimum = (typeof modelMin == "string" || typeof modelMin == "number") ? Number(modelMin) : null;
			if (typeof min != "undefined") {
				if (isNaN(min)) {
					this.model.set("selectedMinimum", '', {silent: true});
					this.$("input[name='selectedMinimum']").val('');
					return false;
				}
				
				if (validationMinimum != null && validationMinimum != undefined && min < validationMinimum) {
					var newMin = changingModel.previous("selectedMinimum");
					this.showError("The minimum cannot be set less than the filter's minimum value");
					this.model.set("selectedMinimum", newMin, {silent: true});
					this.$("input[name='selectedMinimum']").val(newMin);
					return false;
				}
			}
			// convert to a number
			this.model.set("selectedMinimum", min, {silent: true});
		}
		return true;
	},
	
	validateMaximum : function(changingModel) {
		var index = this.model.get("filters").indexOf(changingModel);
		// if we don't find the model in filters, then it's the parent model and we 
		// don't need to validate the incoming values
		if (index >= 0) {
			var max = Number(changingModel.get("selectedMaximum"));
			var modelMax = changingModel.get("maximum");
			var validationMax = (typeof modelMax == "string" || typeof modelMax == "number") ? Number(modelMax) : null;
			if (typeof max != "undefined") {
				if (isNaN(max)) {
					this.model.set("selectedMaximum", '', {silent: true});
					this.$("input[name='selectedMaximum']").val('');
					return false;
				}
				
				if (validationMax != null && validationMax != undefined && max > validationMax) {
					var newMax = this.model.get("maximum");
					this.showError("The maximum cannot be set greater than the filter's maximum value");
					this.model.set("selectedMaximum", newMax, {silent: true});
					this.$("input[name='selectedMaximum']").val(newMax);
					return false;
				}
			}
			this.model.set("selectedMaximum", max, {silent: true});
		}
		return true;
	},
	
	validateMaxMinDifference : function(changingModel) {
		var min = changingModel.get("selectedMinimum");
		var max = changingModel.get("selectedMaximum");
		var subFilterIndex = this.model.get("filters").indexOf(changingModel);
		if (typeof min != "undefined" && max != "undefined") {
			if (min > max) {
				// revert whichever was changing
				if (typeof changingModel.changed.selectedMinimum != "undefined" && typeof changingModel.changed.selectedMaximum != "undefined") {
					this.model.set("selectedMinimum", changingModel.previous("selectedMinimum"), {silent: true});
					this.showError("The minimum cannot be greater than the maximum");
					this.$(".filterNumericRangeMinTextBox").eq(subFilterIndex).focus();
				}
				if (typeof changingModel.changed.selectedMaximum != "undefined") {
					this.model.set("selectedMaximum", changingModel.previous("selectedMaximum"), {silent: true});
					this.showError("The minimum cannot be greater than the maximum");
					this.$(".filterNumericRangeMaxTextBox").eq(subFilterIndex).focus();
				} else {
					this.model.set("selectedMaximum", changingModel.previous("selectedMaximum"), {silent: true});
					this.showError("Please enter a maximum value");
					this.$(".filterNumericRangeMinTextBox").eq(subFilterIndex).focus();
				}
				
				
				// show a message
				
				return false;
			}
		}
		return true;
	},
	
	updateSlider : function(model) {
		var min = model.get("selectedMinimum");
		if (typeof min == "undefined") {
			min = model.get("minimum");
		}
		var max = model.get("selectedMaximum");
		if (typeof max == "undefined") {
			max = model.get("maximum");
		}
		
		// find the correct slider, which model is this?
		var modelIndex = this.model.get("filters").indexOf(model);
		this.$(".filterNumericRangeSlider").eq(modelIndex).slider("values",[min,max]);
		this.closeError();
	},
	
	/**
	 * Fills the on-page elements with values from this filter model (and sub-filters from
	 * their sub-models).
	 * 
	 * called from RefineDataFiltersView after initialize() and render() on this view
	 */
	fillData : function() {
		var model = this.model;
		QT.FilterNumericRangeView.__super__.fillData.call(this);
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
			this.updateSlider(filter);
			$subFilter.find(".filterLogicSelect").val(filter.get("logicBefore"));
		}
	}
});


